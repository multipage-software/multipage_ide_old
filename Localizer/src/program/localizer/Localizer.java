/**
 * 
 */
package program.localizer;

import java.io.*;

import org.multipage.gui.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class Localizer {

	/**
	 * Initialize program basic layer.
	 * @param serializer 
	 * @param dynamicMiddle 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
		// Load resources.
		Resources.setLanguageAndCountry(language, country);
		Resources.loadResource("program.localizer.properties.messages");

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

		LocalizerFrame.setDefaultData();
		PropertyEditor.setDefaultData();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		LocalizerFrame.seriliazeData(inputStream);
		PropertyEditor.seriliazeData(inputStream);
	}

	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		LocalizerFrame.serializeData(outputStream);
		PropertyEditor.serializeData(outputStream);
	}
}
