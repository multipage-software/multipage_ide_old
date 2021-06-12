/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync.full;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.multipage.gui.GeneralGui;
import org.multipage.gui.StateSerializer;
import org.multipage.gui.Utility;
import org.multipage.sync.ProgramSync;
import org.multipage.sync.SyncMain;

/**
 * 
 * @author user
 *
 */
public class SyncFullMain {
	
	/**
	 * Default language and country.
	 */
	private static final String defaultLanguage = "en";
	private static final String defaultCountry = "US";
	
	/**
	 * Main entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		
	    // Set System L&F.
        try {
			UIManager.setLookAndFeel(
			    UIManager.getSystemLookAndFeelClassName());
		}
        catch (Exception e) {
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
					"Error application arguments.\nUse:\n\tjava ProgramSync language COUNTRY accessString",
					"Program Sync",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
		
		// Set locale.
		Locale.setDefault(new Locale(language, country));
		
		// Create serializer and initialize module.
		Path userFolder = Paths.get(Utility.getUserFolder(), "MultipageSync");
		userFolder.toFile().mkdirs();
		StateSerializer serializer = new StateSerializer(Paths.get(userFolder.toString(), "org.multipage.sync.full.settings"));
		
		GeneralGui.initialize(language, country, serializer);
		ProgramSync.initialize(language, country, serializer);
		
		// Load serialized data.
		serializer.startLoadingSerializedStates();
		
		// Delegate call.
		try {
			SyncMain.isStandalone = true;
			//SyncMain.initialize(args);
			SyncMain.startService(false);
		}
		catch (Exception e) {
			Utility.show2(e.getLocalizedMessage());
		}
		
		// Save serialized data.
		serializer.startSavingSerializedStates();
	}
}
