/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import javax.swing.*;

import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.multipage.basic.*;
import org.multipage.gui.*;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class TranslatorMain {
	
	/**
	 * Default language and country.
	 */
	private static final String defaultLanguage = "en";
	private static final String defaultCountry = "US";
	
	/**
	 * Serialized data location.
	 */
	private static final String serilizedDataLocation = "org.multipage.translator.settings";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;

	/**
	 * Application entry point.
	 * @param args - args[0] is language alias,
	 *             - args[1] is country alias
	 */
	public static void main(String[] args) {
		
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
					"Error application arguments.\nUse:\n\tjava ProgramDictionary language COUNTRY",
					"Swing GUI Builder",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
		
		// Set path to middle objects.
		MiddleUtility.setPathToMiddle("org.maclan.postgresql");
		
		// Create serializer.
		serializer = new StateSerializer(serilizedDataLocation);
		
		// Initialize general GUI module.
		if (!GeneralGui.initialize(language, country, serializer)) {
			System.exit(2);
			return;
		}
		// Initialize program basic module.
		if (!ProgramBasic.initialize(language, country, serializer, null)) {
			System.exit(3);
			return;
		}
		// Initialize this module.
		if (!ProgramDictionary.initialize(language, country, serializer)) {
			System.exit(4);
			return;
		}
		
		// Create and show user interface.
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
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

		// Load serializer data.
		serializer.startLoadingSerializedStates();
		
		// Database login.
		MiddleResult loginResult = ProgramBasic.loginDialog(null, Resources.getString("org.multipage.translator.textLoginDialog"));
		if (loginResult.isOK()) {
			
			// Show dictionary dialog.
			TranslatorDialog.showDialog(null);
		}
		
		// Save serializer data.
		serializer.startSavingSerializedStates();
		
		// Exit application.
		System.exit(0);
	}
}
