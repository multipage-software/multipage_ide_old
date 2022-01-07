/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import org.multipage.util.Resources;
import org.multipage.util.j;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.security.ArrayTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

/**
 * State serializer is useful for storing and retrieving application settings to and from defined file.
 * @author vakol
 */
public class StateSerializer {
	
	/**
	 * Root tag name within the settings file.
	 */
	private static final String settingsRootTagName = "settings";
	
	/**
	 * Specify character set for a text file.
	 */
	private static final Charset settingsOutputCharset = StandardCharsets.UTF_8;
	
	/**
	 * Follows a list of converter classes used by the XStream library.
	 * If some of the objects cannot be serialized with default converters, you
	 * must create a new converter class for that kind of objects. Place the new
	 * converter classes nested in StateSerializer class and register
	 * them inside the registerXStreamConverters(...) method defined below. 
	 */
	
	/**
	 * XStream converter defined for types objects.
	 */
	private static class GenericXStreamConverter<T> implements Converter {
		
		/**
		 * Gets default object of type T.
		 */
		private Supplier<T> defaultObjectLambda = null;
		
		/**
		 * Auxiliary field map for the reader.
		 */
		private HashMap<String, String> auxiliaryFieldMap = null;
		
		/**
		 * Writer lambda function.
		 */
		private Function<HierarchicalStreamWriter, Consumer<T>> writeObjectLambda = null;
		
		/**
		 * Reader lambda function.
		 */
		private Function<HierarchicalStreamReader, Function<String, Function<String, Function<HashMap<String, String>, Consumer<T>>>>> readerObjectLambda = null;
		
		/**
		 * Lambda function which finalizes the object.
		 */
		private Function<HashMap<String, String>, T> finalizeObjectLambda = null;

		/**
		 * Register a new converter.
		 * @param <T> - type of object to register
		 * @param xStream
		 * @param defaultObjectLambda
		 * @param writeObjectLambda
		 * @param readerObjectLambda
		 * @param finalizeObjectLambda
		 */
		public static <T> void register(XStream xStream,
				Supplier<T> defaultObjectLambda,
				Function<HierarchicalStreamWriter, Consumer<T>> writeObjectLambda,
				Function<HierarchicalStreamReader, Function<String, Function<String, Function<HashMap<String, String>, Consumer<T>>>>> readerObjectLambda,
				Function<HashMap<String, String>, T> finalizeObjectLambda) 
						throws Exception {
			
			// Check input.
			if (xStream == null || defaultObjectLambda == null || writeObjectLambda == null || readerObjectLambda == null) {
				throw new NullPointerException();
			}
			
			// Create, setup and register a new converter based on the default object type.
			GenericXStreamConverter<T> converter = new GenericXStreamConverter<T>();
			
			// Assign lambda functions.
			converter.defaultObjectLambda = defaultObjectLambda;
			converter.writeObjectLambda = writeObjectLambda;
			converter.readerObjectLambda = readerObjectLambda;
			converter.finalizeObjectLambda = finalizeObjectLambda;
			
			// Register above converter.
			xStream.registerConverter(converter);
		}

		/**
		 * Check the object class if it can be converted.
		 */
		@SuppressWarnings("rawtypes")
		public boolean canConvert(Class theClass) {
			
			if (defaultObjectLambda == null) {
				return true;
			}
			
			boolean canConvert = theClass.equals(defaultObjectLambda.get().getClass());
			return canConvert;
		}
	
		/**
		 * Do marshaling of the typed object.
		 */
		@SuppressWarnings("unchecked")
		public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
			
			if (value != null && canConvert(value.getClass())) {
				
				writeObjectLambda.apply(writer).accept((T) value);
			}
		}
		
		/**
		 * Do unmarshaling of the typed object.
		 */
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			
			// Get default object.
			T defaultObject = defaultObjectLambda.get();
			
			// Check current node name with object type name.
			String nodeName = reader.getNodeName();
			String typeName = defaultObject.getClass().getName();
			
			if (!nodeName.equals(typeName)) {
				return null;
			}
			
			// Make initialization.
			auxiliaryFieldMap = new HashMap<String, String>();
			String value = null;
			
			// Read all fields contained in current object.
			while (reader.hasMoreChildren()) {
				
				reader.moveDown();

				// Get field name and value.
				nodeName = reader.getNodeName();
				value = reader.getValue();
				
				// Apply lambda function on them. Also enable to use an auxiliary field map.
				readerObjectLambda.apply(reader).apply(nodeName).apply(value).apply(auxiliaryFieldMap).accept(defaultObject);
				
				reader.moveUp();
			}
			
			// Possibly finalize object using the field map.
			T finalObject = null;
			
			if (finalizeObjectLambda != null) {
				finalObject = finalizeObjectLambda.apply(auxiliaryFieldMap);
			}
					
			if (finalObject == null) {
				finalObject = defaultObject;
			}
			
			// Release field map.
			auxiliaryFieldMap = null;
			
			return finalObject;
		}
	}
	
	/**
	 * Register converters for XStream library.
	 * @param xStream
	 * @throws Exception 
	 */
	public void registerAllXStreamConverters(XStream xStream)
			throws Exception {
		
		// Register the "java.awt.Rectangle" converter.
		GenericXStreamConverter.register(xStream,
				// Default value for the converter.
				() -> new java.awt.Rectangle(),
				// A writer defined for the rectangle object.
				writer -> rectangle -> {
					
					{
						writer.startNode("x");
						writer.setValue(String.valueOf(rectangle.x));
						writer.endNode();
					}
					{
						writer.startNode("y");
						writer.setValue(String.valueOf(rectangle.y));
						writer.endNode();
					}
					{
						writer.startNode("width");
						writer.setValue(String.valueOf(rectangle.width));
						writer.endNode();
					}
					{
						writer.startNode("height");
						writer.setValue(String.valueOf(rectangle.height));
						writer.endNode();
					}
				},
				// A reader for the rectangle object.
				reader -> nodeName -> textValue -> auxiliaryFieldMap -> rectangle -> {
					
					if ("x".equals(nodeName)) {
						rectangle.x = Integer.parseInt(textValue);
					}
					else if ("y".equals(nodeName)) {
						rectangle.y = "y".equals(nodeName) ? Integer.parseInt(textValue) : 0;
					}
					else if ("width".equals(nodeName)) {
						rectangle.width = "width".equals(nodeName) ? Integer.parseInt(textValue) : 0;
					}
					else if ("height".equals(nodeName)) {
						rectangle.height = "height".equals(nodeName) ? Integer.parseInt(textValue) : 0;
					}
				},
				null
			);
		
		// Register the "java.awt.Point" converter.
		GenericXStreamConverter.register(xStream,
				// Default value for the converter.
				() -> new java.awt.Point(),
				// Define writer for the point object.
				writer -> point -> {
					{
						writer.startNode("x");
						writer.setValue(String.valueOf(point.x));
						writer.endNode();
					}
					{
						writer.startNode("y");
						writer.setValue(String.valueOf(point.y));
						writer.endNode();
					}
				},
				// A reader for the point object.
				reader -> nodeName -> textValue -> auxiliaryFieldMap -> point -> {
					
					if ("x".equals(nodeName)) {
						point.x = Integer.parseInt(textValue);
					}
					else if ("y".equals(nodeName)) {
						point.y = "y".equals(nodeName) ? Integer.parseInt(textValue) : 0;
					}
				},
				null
			);
		
		// Register the "java.awt.Fon"t converter.
		GenericXStreamConverter.register(xStream,
				// Default value.
				() -> new java.awt.Font("Arial", java.awt.Font.PLAIN, 12),
				// Writer for the font.
				writer -> font -> {
					{
						writer.startNode("name");
						writer.setValue(String.valueOf(font.getFontName()));
						writer.endNode();
					}
					{
						writer.startNode("size");
						writer.setValue(String.valueOf(font.getSize()));
						writer.endNode();
					}
					{
						writer.startNode("style");
						writer.setValue(String.valueOf(font.getStyle()));
						writer.endNode();
					}
				},
				// Reader for the font object.
				reader -> nodeName -> textValue -> auxiliaryFieldMap -> font -> {
					
					reader.moveDown();
					auxiliaryFieldMap.put(nodeName, textValue);
					reader.moveUp();
				},
				// Finalize the font object.
				auxiliaryFieldMap -> new java.awt.Font(
						auxiliaryFieldMap.get("name"),
						Integer.parseInt(auxiliaryFieldMap.get("size")),
						Integer.parseInt(auxiliaryFieldMap.get("style")))
			);
		
		// Set permissions for all the converters above.
		xStream.addPermission(NoTypePermission.NONE);
		xStream.addPermission(NullPermission.NULL);
		xStream.addPermission(PrimitiveTypePermission.PRIMITIVES);
		xStream.addPermission(ArrayTypePermission.ARRAYS);
		
		xStream.allowTypesByWildcard(new String[] {
			    "java.lang.**",
			    "java.util.**",
				"java.awt.**",
				"org.maclan.**",
				"org.multipage.**"
			});
	}
	
	/**
	 * Name of a file with stored application settings.
	 */
	public String settingsFileName = null;
	
	/**
	 * An input stream which is used when loading application states.
	 */
	public StateInputStreamImpl stateInputStream = null;
	
	/**
	 * An output stream which is used when saving application states.
	 */
	public StateOutputStreamImpl stateOutputStream = null;

	/**
	 * Additionally allowed types. Wild cards are used in each specification of group of types (for example ["javax.swing.**", ...]).
	 */
	private String[] additionalAllowedTypes = null;
	
	/**
	 * List of connected listeners. 
	 */
	private static LinkedList<SerializeStateAdapter> serializeStateListenersRef =
		new LinkedList<SerializeStateAdapter>();

	/**
	 * Constructor.
	 * @param settingsFileName
	 */
	public StateSerializer(String settingsFileName) {

		this.settingsFileName = settingsFileName;
	}
	
	/**
	 * Constructor.
	 * @param settingsFilePath
	 */
	public StateSerializer(Path settingsFilePath) {
		
		this.settingsFileName = settingsFilePath.toString();
	}

	/**
	 * Constructor.
	 */
	public StateSerializer() {
		this("");
	}

	/**
	 * Add new listener.
	 * @param listener
	 */
	public void add(SerializeStateAdapter listener) {

		serializeStateListenersRef.add(listener);
	}
	
	/**
	 * Set additional types. You must use wild cards to specify group of types.
	 * @param additionalAllowedTypes
	 */
	public void setAllowedTypesByWildcard(String ... additionalAllowedTypes) {	
		
		this.additionalAllowedTypes = additionalAllowedTypes;
	}
	
	/**
	 * Open input stream.
	 * @param settingsFileName 
	 */
	public StateInputStreamImpl openStateInputStream(String settingsFileName) {
		
		try {
			
			// Open settings file and create input stream.
			StateInputStreamImpl stateInputStream = StateInputStreamImpl.newXStreamInstance(settingsFileName);
			XStream xStream = stateInputStream.getXStream();
			
			// Register all available converters from and to XML with the XStream library.
			registerAllXStreamConverters(xStream);
			if (additionalAllowedTypes != null && additionalAllowedTypes.length > 0) {
				xStream.allowTypesByWildcard(additionalAllowedTypes);
			}
			
			return stateInputStream;
		}
		catch (Exception e) {
			
			// Inform user about raised exception and return a null value.
			JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotFindApplicationStateUsingDef"));
			return null;
		}
	}
	
	/**
	 * Close the input stream.
	 */
	public boolean closeStateInputStream() {
		
		try {
			// Try to close the stream.
			stateInputStream.close();
			return true;
		}
		catch (Exception e) {
			
			// Inform user about the error.
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return false;
	}

	/**
	 * Opens output stream which can save application settings.
	 * @param settingsFileName 
	 * @return - true if the file can be found, otherwise returns a false value
	 */
	public StateOutputStreamImpl openStateOutputStream(String settingsFileName) {
		
		try {
			
			// Opens the settings file and creates output stream of objects.
			StateOutputStreamImpl stateOutputStream = StateOutputStreamImpl.newXStreamInstance(settingsFileName, settingsRootTagName);
			XStream xStream = stateOutputStream.getXStream();
			
			// Registers all known XML converters.
			registerAllXStreamConverters(xStream);
			if (additionalAllowedTypes != null && additionalAllowedTypes.length > 0) {
				xStream.allowTypesByWildcard(additionalAllowedTypes);
			}
			
			return stateOutputStream;
		}
		catch (Exception e) {
			
			// Inform user about an error and return false value.
			JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotSaveApplicationState"));
			return null;
		}
	}
	
	/**
	 * Closes current output stream.
	 * @return - return true if the stream is closed, otherwise it returns false.
	 */
	public boolean closeStateOutputStream() {
		
		try {
			// Try to close the stream.
			stateOutputStream.close();
			return true;
		}
		catch (Exception e) {
			
			// Inform user about an error.
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return false;
	}
	
	/**
	 * Load default settings.
	 */
	public void loadDefaultStates() {

		// Do loop for all known listeners. Invoke related event on each of them.
		for (SerializeStateAdapter listener : serializeStateListenersRef) {
			// Invoke the event.
			listener.onSetDefaultState();
		}

	}
	
	/**
	 * Starts to loading application settings.
	 */
	public void startLoadingSerializedStates() {
		
		// Open input stream.
		stateInputStream = openStateInputStream(settingsFileName);
		if (stateInputStream != null) {
			
			j.log("Loading application settings form \"%s\"", settingsFileName);
			try {
				// Do loop for all known listeners. Invoke events that can read the settings.
				for (SerializeStateAdapter listener : serializeStateListenersRef) {
					
					// Invoke lister.
					listener.onReadState(stateInputStream);
				}
			}
			catch (Exception e) {
				
				// Inform user about an exception.
				JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotFindApplicationStateUsingDef"));
				
				// Load default settings.
				loadDefaultStates();
			}
			
			// Close the stream.
			closeStateInputStream();
		}
		else {
			// Load default settings.
			loadDefaultStates();
		}
	}

	/**
	 * Start to save application settings.
	 */
	public void startSavingSerializedStates() {
		
		// Open new output stream.
		stateOutputStream = openStateOutputStream(settingsFileName);
		if (stateOutputStream != null) {
			
			j.log("Saving application settings to \"%s\"", settingsFileName);
			try {
				// Get direct output stream and write a BOM in the beginning.
				OutputStream rawOutputStream = stateOutputStream.getRawOutputStream();
				stateOutputStream.writeBom();
				
				// Write XML header into the output stream.
				Utility.writeXmlHeader(rawOutputStream, settingsOutputCharset, true);
				
				for (SerializeStateAdapter listener : serializeStateListenersRef) {
					
					// Invoke listers which can write application objects to the output stream.
					listener.onWriteState(stateOutputStream);
				}
			}
			catch (Exception e) {
				
				// Inform user.
				JOptionPane.showMessageDialog(null, Resources.getString("org.multipage.gui.errorCannotSaveApplicationState"));
			}
			
			// Close the stream.
			closeStateOutputStream();
		}
	}
}
