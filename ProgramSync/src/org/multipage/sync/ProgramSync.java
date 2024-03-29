package org.multipage.sync;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.multipage.gui.SerializeStateAdapter;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.StateSerializer;
import org.multipage.util.Resources;

/**
 * 
 * @author user
 *
 */
public class ProgramSync {
	
	/**
	 * Initialize data.
	 * @param language
	 * @param country
	 * @param serializer
	 * @return
	 */
	public static boolean initialize(String language, String country, StateSerializer serializer) {
		
		// Initialize resources.
		Resources.setLanguageAndCountry(language, country);
		Resources.loadResource("org.multipage.sync.properties.messages");
		
		// Add state serializer.
		if (serializer != null) {
			serializer.add(new SerializeStateAdapter() {
				// On read state.
				@Override
				protected void onReadState(StateInputStream inputStream)
						throws IOException, ClassNotFoundException {
					// Serialize program dictionary.
					seriliazeData(inputStream);
				}
				// On write state.
				@Override
				protected void onWriteState(StateOutputStream outputStream)
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
		
		AccessStringFrame.setDefaultData();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		AccessStringFrame.serializeData(inputStream);
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {
		
		AccessStringFrame.serializeData(outputStream);
	}
}
