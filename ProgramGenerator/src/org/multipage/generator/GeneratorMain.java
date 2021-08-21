/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.help.Intellisense;
import org.maclan.help.ProgramHelp;
import org.maclan.server.AreaServer;
import org.multipage.basic.GuiWatchDog;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.GeneralGui;
import org.multipage.gui.StateSerializer;
import org.multipage.gui.TextPopupMenu;
import org.multipage.sync.ProgramSync;
import org.multipage.sync.SyncMain;
import org.multipage.translator.ProgramDictionary;
import org.multipage.util.Obj;
import org.multipage.util.SimpleMethodRef;
import org.multipage.util.j;


/**
 * @author
 *
 */
public class GeneratorMain {
	
	/**
	 * Default language and country.
	 */
	public static final String defaultLanguage = "en";
	private static final String defaultCountry = "US";
	
	/**
	 * Serialized data location.
	 */
	private static final String serilizedDataLocation = "org.multipage.generator.settings";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * Get state serializer
	 * @return
	 */
	public static StateSerializer getSerializer() {
		
		return serializer;
	}
	
	/**
	 * Main entry point.
	 * @param applicationNaming 
	 * @param args
	 * @param pathToMiddleObjects
	 * @param useLogin
	 */
	public static void main(String applicationNaming, String[] args, String pathToMiddleObjects, final boolean useLogin) {
		
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
			// If there are wrong parameters.
			JOptionPane.showMessageDialog(
					null,
					"Error application arguments.\nUse:\n\tjava ProgramBuilder language COUNTRY",
					"Swing GUI Builder",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
		
		// Set locale.
		Locale.setDefault(new Locale(language, country));
		
		// Set Sync main application title
		SyncMain.setMainApplicationTitleCallback(() -> { return ProgramGenerator.getApplicationTitle(); });
		
		// Start GUI watch dog.
		GuiWatchDog.start();
		
		// Load application properties.
		MiddleUtility.loadApplicationProperties();
		
		// Load servers settings.
		MiddleUtility.loadServersProperties();
		
		// Set path to middle objects.
		MiddleUtility.setPathToMiddle(pathToMiddleObjects);
		
		// Create application state serializer.
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
			System.exit(3);
			return;
		}
		
		// Initialize program basic level.
		if (!ProgramBasic.initialize(language, country, serializer, null)) {
			System.exit(4);
			return;
		}
		
		// Initialize program dictionary level.
		if (!ProgramDictionary.initialize(language, country, serializer)) {
			System.exit(5);
			return;
		}
				
		// Initialize program sync layer.
		if (!ProgramSync.initialize(language, country, serializer)) {
			System.exit(6);
			return;
		}
		
		// Initialize program generator layer.
		if (!ProgramGenerator.initialize(language, country, serializer)) {
			System.exit(7);
			return;
		}
		
		// Initialize intellisense.
		Intellisense.initialize();
		
		// Set middle layer model reference.
		ProgramBasic.getMiddle().setModel(ProgramGenerator.getAreasModel());

		// Set use login flag.
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
	 * User interface.
	 */
	protected static void userInterface() {

		// Load serialized data.
		serializer.startLoadingSerializedStates();
		
		// Enable/disable CSS editors in text popup trayMenu.
		TextPopupMenu.enableCss = true;
		
		// Database login.
		MiddleResult loginResult = MiddleResult.OK;
		boolean useLogin = ProgramBasic.isUsedLogin();
		if (useLogin) {
			loginResult = ProgramBasic.loginDialog();
		}
		
		if (loginResult.isOK()) {
			
			// Create basic area if it doesn't exist.
			Obj<Boolean> newBasicArea = new Obj<Boolean>();
			Properties loginProperties = ProgramBasic.getLoginProperties();
			
			MiddleResult result = ProgramBasic.getMiddle().attachOrCreateNewBasicArea(loginProperties, null, newBasicArea);
			if (result.isOK()) {
				
				
				// Import help.
				if (newBasicArea.ref) {
					
					j.log("Importing Maclan reference into Basic Area.");
					
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
			GeneratorMainFrame mainFrame = new GeneratorMainFrame();
			
			// Initialize Open and Save dialog path
			MiddleUtility.initOpenSavePath();
			
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
		MiddleUtility.saveServersProperties();
		// Save serialized data.
		serializer.startSavingSerializedStates();
		// Stop GUI watch dog.
		GuiWatchDog.stop();
	}
}
