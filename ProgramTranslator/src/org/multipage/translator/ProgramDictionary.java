/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.multipage.gui.SerializeStateAdapter;
import org.multipage.gui.StateSerializer;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class ProgramDictionary {

	/**
	 * Resource location.
	 */
	private static final String resourcesLocation = "org.multipage.translator.properties.messages";
	
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
	 * Initialize program basic layer.
	 * @param serializer 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
		// Remember the serializer
		ProgramDictionary.serializer = serializer;
		
		// Set local identifiers.
		Resources.setLanguageAndCountry(language, country);
		
		// Load resources file.
		if (!Resources.loadResource(resourcesLocation)) {
			return false;
		}

		// Add state serializer.
		if (serializer != null) {
			serializer.add(new SerializeStateAdapter() {
				// On read state.
				@Override
				protected void onReadState(ObjectInputStream inputStream)
						throws IOException, ClassNotFoundException {
					// Serialize program dictionary.
					seriliazeData(inputStream);
				}
				// On write state.
				@Override
				protected void onWriteState(ObjectOutputStream outputStream)
						throws IOException {
					// Serialize program dictionary.
					serializeData(outputStream);
				}
				// On set default state.
				@Override
				protected void onSetDefaultState() {
					// Set default data.
					setDefaultData();
				}
			});
		}
		
		return true;
	}

	/**
	 * Set default data.
	 */
	protected static void setDefaultData() {

		// Default dictionary data.
		TranslatorDialog.setDefaultData();
		// Default text dialog data.
		LocalizeTextDialog.setDefaultData();
		// Set default data.
		OrderLanguagesDialog.setDefaultData();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load dictionary data.
		TranslatorDialog.serializeData(inputStream);
		// Load text dialog data.
		LocalizeTextDialog.serializeData(inputStream);
		// Load data.
		OrderLanguagesDialog.serializeData(inputStream);
	}

	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {

		// Save dictionary data.
		TranslatorDialog.serializeData(outputStream);
		// Save text dialog data.
		LocalizeTextDialog.serializeData(outputStream);
		// Save data.
		OrderLanguagesDialog.serializeData(outputStream);
	}
}
