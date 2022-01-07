/**
 * 
 */
package program.builder.dynamic;

import java.io.*;

import general.gui.*;
import general.util.*;
import program.builder.*;
import program.generator.GeneratorMainFrame;
import program.middle.*;
import program.middle.dynamic.*;
import program.middle.postgresql.dynamic.*;

/**
 * 
 * @author
 *
 */
public class BuilderDynamicMain {

	/**
	 * Resource location.
	 */
	private static final String resourcesLocation = "program.builder.dynamic.properties.messages";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Use PostgreSQL middle layer.
		BuilderMain.main(args, "program.middle.postgresql", true, new ExtensionsToDynamic() {
			
			// Initialize this level.
			@Override
			public boolean initializeLevel(String language, String country,
					StateSerializer serializer) {
				
				return initialize(language, country, serializer);
			}
			
			// Create new main frame.
			@Override
			public GeneratorMainFrame newMainFrame() {
				
				return new BuilderMainFrameDynamic();
			}

			// Create middle layer.
			@Override
			public Middle getMiddle() {
				
				return new MiddleImplDynamic();
			}

			// Create new areas model.
			@Override
			public AreasModel newAreasModel() {
				
				return new AreasModelDynamic();
			}
		});
	}

	/**
	 * Initialize level.
	 * @param language
	 * @param country
	 * @param serializer
	 * @return
	 */
	private static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
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
		
		StepsDiagram.sweepsCount = 20;
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException 
	 */
	protected static void serializeData(ObjectOutputStream outputStream) throws IOException {
		
		// Write crossings removal strength.
		outputStream.writeInt(StepsDiagram.sweepsCount);
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException 
	 */
	protected static void seriliazeData(ObjectInputStream inputStream) throws IOException {
		
		// Read crossings removal strength.
		StepsDiagram.sweepsCount = inputStream.readInt();
	}
}
