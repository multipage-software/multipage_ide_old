/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.GeneratorMainFrame;
import org.multipage.generator.ProgramGenerator;
import org.multipage.generator.Settings;
import org.multipage.gui.GeneralGui;
import org.multipage.gui.StateSerializer;
import org.multipage.sync.ProgramSync;
import org.multipage.sync.SyncMain;
import org.multipage.translator.ProgramDictionary;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.SimpleMethodRef;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.help.ProgramHelp;
import org.maclan.server.AreaServer;

/**
 * 
 * @author
 *
 */
public class BuilderMain {
	
	/**
	 * Default language and country.
	 */
	private static final String defaultLanguage = "en";
	private static final String defaultCountry = "EN";
	
	/**
	 * Serialized data location.
	 */
	private static final String serilizedDataLocation = "program.builder.settings";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * @param applicationNaming
	 * @param args
	 * @param pathToMiddleObjects 
	 * @param useLogin 
	 */
	public static void main(String applicationNaming, String[] args, String pathToMiddleObjects, final boolean useLogin,
			ExtensionsToDynamic extensionsToDynamic) {
		
		// Set default application naming. It is used for example for application user directory
		MiddleUtility.setApplicationNaming(applicationNaming);
		
	    // Set System L&F.
        try {
			UIManager.setLookAndFeel(
			    UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
        

		// Get local identifiers.
		String language;
		String country;
		
		if (args.length == 0) {
			
			language = defaultLanguage;
			country = defaultCountry;
		}
		else if (args.length == 2) {
			
			language = args[0];
			country = args[1];
		}
		else {
			// If are wrong parameters.
			JOptionPane.showMessageDialog(
					null,
					"Error application arguments.\nUse:\n\tjava ProgramBuilder language COUNTRY",
					"Swing GUI Builder",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
		
		
		// Set extensions to dynamic object.
		ProgramBuilder.setExtensionsToDynamic(extensionsToDynamic);
		
		// Load application properties.
		MiddleUtility.loadApplicationProperties();
		
		// Load server settings.
		MiddleUtility.loadServersProperties();
		
		// Set path to middle objects.
		MiddleUtility.setPathToMiddle(pathToMiddleObjects);
		
		// Create serializer.
		String userDirectory = MiddleUtility.getUserDirectory();
		String serializedFile = null;
		
		if (!userDirectory.isEmpty()) {
			serializedFile = userDirectory + File.separatorChar + serilizedDataLocation;
		}
		else {
			serializedFile = serilizedDataLocation;
		}
		
		serializer = new StateSerializer(serializedFile);
		
		// Initialize general GUI level.
		if (!GeneralGui.initialize(language, country, serializer)) {
			System.exit(2);
			return;
		}
		
		// Try to get dynamic middle layer.
		Middle dynamicMiddle = null;
		if (extensionsToDynamic != null) {
			dynamicMiddle = extensionsToDynamic.getMiddle();
		}
		
		// Initialize program basic level.
		if (!ProgramBasic.initialize(language, country, serializer, dynamicMiddle)) {
			System.exit(3);
			return;
		}
		// Initialize program dictionary level.
		if (!ProgramDictionary.initialize(language, country, serializer)) {
			System.exit(4);
			return;
		}
		// Initialize program help layer.
		if (!ProgramHelp.initialize(language, country, serializer)) {
			System.exit(5);
			return;
		}
		// Initialize program generator level.
		if (!ProgramGenerator.initialize(language, country, serializer)) {
			System.exit(6);
			return;
		}	
		// Initialize program sync layer.
		if (!ProgramSync.initialize(language, country, serializer)) {
			System.exit(7);
			return;
		}
		// Initialize program builder level.
		if (!ProgramBuilder.initialize(language, country, serializer)) {
			System.exit(8);
			return;
		}
		// Initialize possible program builder dynamic level.
		if (extensionsToDynamic != null) {
			if (!extensionsToDynamic.initializeLevel(language, country, serializer)) {
				System.exit(9);
				return;
			}
		}
		
		// Check PIN to continue with the Builder.
		if (!checkPin()) {
			System.exit(10);
			return;
		}
		
		// Set middle layer model reference.
		ProgramBasic.getMiddle().setModel(ProgramBuilder.getAreasModel());
		
		// Set application properties.
		ProgramBasic.setUseLogin(useLogin);
		
		// Initialize area server.
		AreaServer.init();

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				// User interface.
				userInterface();
            }
        });
	}
	
	/**
	 * Check pass PIN.
	 * @return
	 */
	private static boolean checkPin() {
		
		Lock pinDialogLock = new Lock();
		Obj<Boolean> pinConfirmed = new Obj<Boolean>(false);
		
		PinFrame.showFrame(confirmed -> {
			pinConfirmed.ref = confirmed;
			Lock.notify(pinDialogLock);
		});
		
		Lock.waitFor(pinDialogLock);
		return pinConfirmed.ref;
	}

	/**
	 * User interface.
	 */
	protected static void userInterface() {

		// Load serialized data.
		serializer.startLoadingSerializedStates();
		
		// Database login.
		boolean useLogin = ProgramBasic.isUsedLogin();
		MiddleResult result = ProgramBasic.loginDialog(null, Resources.getString("builder.textLoginDialog"));
		if (useLogin && result.isOK() || !useLogin) {
			
			// Create database if it doesn't exist.
			Obj<Boolean> isNewDatabase = new Obj<Boolean>(false);
			Properties loginProperties = ProgramBasic.getLoginProperties();
			
			result = ProgramBasic.getMiddle().attachOrCreateNewBasicArea(loginProperties, null, isNewDatabase);
			
			if (result.isOK()) {
				
				// Import help.
				if (isNewDatabase.ref) {
					
					System.err.println("Importing Maclan reference into Basic Area.");
					
					InputStream maclanHelpXml = ProgramHelp.openMaclanReferenceXml();
					InputStream maclanHelpDat = ProgramHelp.openMaclanReferenceDat();
					
					try {
						ProgramBasic.loginMiddle();
						result = ProgramBasic.getMiddle().importTemplate(maclanHelpXml, maclanHelpDat);
					}
					catch (Exception e) {
					}
					finally {
						ProgramBasic.logoutMiddle();
					}
				}
			}
			if (result.isNotOK()) {
				result.show(null);
			}

			// Start HTTP server.
			ProgramBasic.startHttpServer(Settings.getHttpPortNumber(), !ProgramBasic.isUsedLogin());
			
			// Initialize main frame class. Create and show main frame.
			GeneratorMainFrame mainFrame;
			ExtensionsToDynamic extensions = ProgramBuilder.getExtensionsToDynamic();
			if (extensions == null) {
				mainFrame = new BuilderMainFrame();
			}
			else {
				mainFrame = extensions.newMainFrame();
			}
			
			// Set HTTP server login.
			ProgramBasic.setHttpServerLogin();
			
			// On close main frame.
			mainFrame.setCloseListener(new SimpleMethodRef() {
				// On close window.
				@Override
				public void run() {
					closeApplication();
				}
			});
			
			// Show main frame
			mainFrame.setVisible(true);
		}
		else {
			// Close application.
			closeApplication();
			// Exit the application.
			System.exit(0);
		}
	}
	
	/**
	 * Close application.
	 */
	private static void closeApplication() {
			
		// Close HTTP server.
		ProgramBasic.stopHttpServer();
		// Stop Sync.
		SyncMain.stop();
		// Save servers properties.
		MiddleUtility.saveServerProperties();
		// Save serialized data.
		serializer.startSavingSerializedStates();
	}
}
