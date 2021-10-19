/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 13-05-2020
 *
 */
package org.maclan.help;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.multipage.gui.SerializeStateAdapter;
import org.multipage.gui.StateSerializer;
import org.multipage.util.Resources;

/**
 * @author user
 *
 */
public class ProgramHelp {
	
	/**
	 * Resource location.
	 */
	protected static String resourcesLocation = "org.maclan.help.properties.messages";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * Log lambda function.
	 */
	private static Consumer<String> logLambda = null;
	
	/**
	 * Can log lambda function.
	 */
	private static Supplier<Boolean> canLogLambda = null;
	
	/**
	 * Initialize program basic layer.
	 * @param serializer 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
		// Remember the serializer
		ProgramHelp.serializer = serializer;
		
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
		
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
	}
	
	/**
	 * Set logging lambda function.
	 * @param logLambda
	 */
	public static void setLogLambda(Consumer<String> logLambda) {
		
		ProgramHelp.logLambda = logLambda;
	}
	
	/**
	 * Set "can log" lambda function.
	 * @param logLambda
	 */
	public static void setCanLogLambda(Supplier<Boolean> canLogLambda) {
		
		ProgramHelp.canLogLambda = canLogLambda;
	}
	
	/**
	 * Check if the application can log messages.
	 * @return
	 */
	public static boolean canLog() {
		
		if (logLambda != null && canLogLambda != null) {
			
			boolean canLog =  canLogLambda.get();
			return canLog;
		}
		return false;
	}
	
	/**
	 * Log text.
	 */
	public static void log(String logText) {
		
		if (logLambda != null) {
			logLambda.accept(logText);
		}
	}
	
	/**
	 * Log parametrized text.
	 */
	public static void log(String logText, Object ... textParameters) {
		
		if (logLambda != null) {
			
			if (textParameters.length > 0) {
				logText = String.format(logText, textParameters);
			}
			
			logLambda.accept(logText);
		}
	}
	
	/**
	 * Open Maclan reference template XML as input stream.
	 * @return
	 */
	public static InputStream openMaclanReferenceXml() {
		
		InputStream inputStream = ProgramHelp.class.getResourceAsStream("/org/maclan/reference_template/maclan_reference.xml");
		return new BufferedInputStream(inputStream);
	}
	
	/**
	 * Open Maclan reference template DAT as input stream.
	 * @return
	 */
	public static InputStream openMaclanReferenceDat() {
		
		InputStream inputStream = ProgramHelp.class.getResourceAsStream("/org/maclan/reference_template/maclan_reference.dat");
		return new BufferedInputStream(inputStream);
	}
}
