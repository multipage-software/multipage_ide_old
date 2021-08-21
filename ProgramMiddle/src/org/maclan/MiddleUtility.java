/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.awt.Component;
import java.awt.Cursor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.maclan.server.AreaServer;
import org.maclan.server.Request;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author
 *
 */
public class MiddleUtility {
	
	/**
	 * Path to middle objects.
	 */
	private static String pathToMiddle = "";

	/**
	 * Application properties.
	 */
	private static Properties applicationProperties = new Properties();
	
	/**
	 * Server properties.
	 */
	private static Properties serversProperties = new Properties();

	/**
	 * Program, version, application names
	 */
	private static String programName = "Multipage";
	private static String cloneName = "UnknownClone";
	private static String versionName = "UnknownVersion";
	
	/**
	 * File with servers settings.
	 */
	private static String serversSettingsFile = "servers.properties";
	
	/**
	 * Web interface folder
	 */
	private static final String webInterfaceFolder = "WebInterface";
	
	/**
	 * XML validation file.
	 */
	public static final String xmlValidationFile = "/program/middle/properties/mime_types.xsd";

	/**
	 * Load application name from application properties
	 * @param defaultApplicationNaming
	 */
	public static void setApplicationNaming(String defaultApplicationNaming) {

		String[] parts = defaultApplicationNaming.split(" ");
		
		int lenght = parts.length;
		
		MiddleUtility.programName = lenght >= 1 ? parts[0] : "";
		MiddleUtility.cloneName = lenght >= 2 ? parts[1] : "";
		MiddleUtility.versionName = lenght >= 3 ? parts[2] : "";
		
		applicationProperties.put("application_name", defaultApplicationNaming);
	}
	
	/**
	 * Get application name
	 */
	public static String getApplicationNaming() {
		
		// Load "program version application" definition string from properties
		String applicationNaming = applicationProperties.getProperty("application_name", "Multipage");
		return applicationNaming;
	}
	
	/**
	 * Gets element with given ID
	 * @param <T>
	 * @param elements
	 * @param id
	 * @return
	 */
	public static <T> T getElementWithId(LinkedList<T> elements, long id) {
		
		for (T element : elements) {
			
			if (element instanceof Element) {
				if (((Element) element).getId() == id) {
					return element;
				}
			}
		}
		
		return null;
	}

	/**
	 * Gets list element using ID.
	 * @param <T>
	 * @param list
	 * @param id
	 * @return
	 */
	public static <T extends Element> T getListItem(LinkedList<T> list, long id) {
		
		for (T item : list) {
			if (item.getId() == id) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Get total tree areas.
	 * @param treeDepth
	 * @param currentDepth
	 * @return
	 */
	public static double getTotalTreeAreas(int treeWidth, int treeDepth) {
		
		double sum = 0.0;
		
		for (int layerIndex = 1; layerIndex <= treeDepth; layerIndex++) {
			 sum += Math.pow(treeWidth, layerIndex);
		}
		
		return sum;
	}
	
	/**
	 * Create new middle layer instance.
	 * @return
	 */
	public static Middle newMiddleInstance() {
		
		// Create middle layer.
		try {
			ClassLoader classLoader = MiddleUtility.class.getClassLoader();
			Class objectClass = classLoader.loadClass(pathToMiddle + ".MiddleImpl");
			
			Middle middle = (Middle) objectClass.getDeclaredConstructor().newInstance();
			return middle;
		}
		catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Create new middle layer instance.
	 * @return
	 */
	public static MiddleLight newMiddleLightInstance() {
		
		// Create middle layer.
		try {
			ClassLoader classLoader = MiddleUtility.class.getClassLoader();
			Class objectClass = classLoader.loadClass(pathToMiddle + ".MiddleLightImpl");
			
			MiddleLight middleLight = (MiddleLight) objectClass.getDeclaredConstructor().newInstance();
			return middleLight;
		}
		catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set extended functions.
	 * @param pathToMiddle
	 */
	private static void setExtendedFunctions(String pathToMiddle) {
		
		// Create middle layer.
		try {
			ClassLoader classLoader = MiddleUtility.class.getClassLoader();
			Class objectClass = classLoader.loadClass(pathToMiddle + ".MiddleLightImpl");

			Method method = objectClass.getDeclaredMethod("addExtensions");
			if (method != null) {
				
				method.invoke(null);
			}
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Set path to middle objects.
	 * @param pathToMiddleObjects
	 */
	public static void setPathToMiddle(String pathToMiddleObjects) {
		
		pathToMiddle = pathToMiddleObjects;
		
		// Set extended function from the middle layer..
		setExtendedFunctions(pathToMiddle);
	}
	
	/**
	 * Get current path to middle objects.
	 * @return
	 */
	public static String getPathToMiddle() {
		
		return pathToMiddle;
	}

	/**
	 * Import MIMEs from input stream.
	 * @param xmlInputStream
	 */
	public static void importMimeTypes(MiddleLight middle, Properties login, InputStream xmlInputStream, final Component parent) {
   	
    	// Login to the database.
    	MiddleResult result = middle.login(login);
    	if (result.isNotOK()) {
    		result.show(parent);
    		return;
    	}
    	
    	// Delegate call.
    	importMimeTypes(middle, xmlInputStream, parent);
		        	
    	// Logout from the database.
    	result = middle.logout(result);
    	if (result.isNotOK()) {
    		result.show(parent);
    	}
	}

	/**
	 * Import factory MIME types.
	 * @param parent
	 */
	public static void importFactoryMimeTypes(MiddleLight middle, Properties login, Component parent) {
    	
		// Login to database.
		MiddleResult result = middle.login(login);
		
		if (result.isNotOK()) {
			result.show(parent);
			return;
		}
		
		// Delegate call.
		importFactoryMimeTypes(middle, parent);
		
		// Logout from database.
		MiddleResult logoutResult = middle.logout(result);
		
		if (logoutResult.isNotOK()) {
			logoutResult.show(parent);
		}
	}

	/**
	 * Import factory MIME types.
	 * @param middleLightImpl
	 * @param object
	 */
	public static void importFactoryMimeTypes(MiddleLight middle, Component parent) {
		
		InputStream xmlInputStream = null;
		
		try {
			xmlInputStream = MiddleUtility.class.getResourceAsStream("/program/middle/properties/mime_types.xml");
			
			if (xmlInputStream != null) {
				
				importMimeTypes(middle, xmlInputStream, parent);
			}
		}
		catch (Exception e) {
			Utility.show2(parent, e.getMessage());
		}
		finally {
			try {
				if (xmlInputStream != null) {
					xmlInputStream.close();
				}
			}
			catch (Exception e) {
			}
		}
	}
	
	/**
	 * Import MIME types.
	 * @param middle
	 * @param xmlInputStream
	 * @param parent
	 */
	private static void importMimeTypes(MiddleLight middle,
			InputStream xmlInputStream, final Component parent) {
		
		if (parent != null) {
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		
		Exception exception = null;
		
		try {
				// Try to get parser and parse the file.
		    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db = dbf.newDocumentBuilder();
				// Error handler.
				db.setErrorHandler(new ErrorHandler() {
					@Override
					public void warning(SAXParseException exception) throws SAXException {
						JOptionPane.showMessageDialog(parent, exception.getMessage());
					}
					@Override
					public void fatalError(SAXParseException exception) throws SAXException {						
						JOptionPane.showMessageDialog(parent, exception.getMessage());
					}
					@Override
					public void error(SAXParseException exception) throws SAXException {						
						JOptionPane.showMessageDialog(parent, exception.getMessage());
					}
				});
				
		        Document document = db.parse(xmlInputStream);
		        
		        // Validate XML file.
		        
		        InputStream schemaInputStream = MiddleUtility.class.getResourceAsStream(xmlValidationFile);
		        if (schemaInputStream == null) {
		        	
		    		if (parent != null) {
		    			parent.setCursor(Cursor.getDefaultCursor());
		    		}
		        	// Inform user and exit.
		        	JOptionPane.showMessageDialog(parent, Resources.getString("middle.messageCannotLocateMimeValiationFile"));
		        	return;
		        }
		        
		        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		        Schema schema = factory.newSchema(new StreamSource(schemaInputStream));
		        Validator validator = schema.newValidator();
		        try {
		        	validator.validate(new DOMSource(document));
		        }
		        catch (SAXException e) {
		        	
		    		if (parent != null) {
		    			parent.setCursor(Cursor.getDefaultCursor());
		    		}
		        	// Set message.
		        	String message = Resources.getString("middle.messageMimeValidationException")
		        						+ "\n" + e.getMessage();
		        	JOptionPane.showMessageDialog(parent, message);
		        	return;
		        }
		        
		        schemaInputStream.close();
		        
		        // Save MIME data.
		        org.w3c.dom.Element documentElement = document.getDocumentElement();
		        NodeList mimeNodesList = documentElement.getElementsByTagName("mime");
		        
		        if (mimeNodesList != null) {
			        
		        	MiddleResult result;
		        	int length = mimeNodesList.getLength();
		        	
		        	for (int index = 0; index < length; index++) {
		        		
		        		Node mimeNode = mimeNodesList.item(index);
			        	
			        	// Type and extension nodes.
		        		Node mimeChild = mimeNode.getFirstChild();
			        	Node typeNode = Utility.getElementNode(mimeChild);
			        	Node extensionNode = Utility.getElementNode(
			        			typeNode.getNextSibling());
			        	
			        	// Get texts.
			        	String type = typeNode.getTextContent();
			        	String extension = extensionNode.getTextContent();
			        	type = type.trim();
			        	extension = extension.trim();
			        	// Get preference flag.
			        	boolean preference = type.charAt(0) == '@';
			        	if (preference) {
			        		type = type.substring(1);
			        	}
			        	
			        	// Check texts.
			        	if (type.isEmpty() || extension.isEmpty()) {
			        		JOptionPane.showMessageDialog(parent, 
			        				Resources.getString("middle.messageEmptyTypeOrExtension"));
			        		break;
			        	}
			        	
			        	// Start sub transaction.
			        	result = middle.startSubTransaction();
			        	if (result.isOK()) {
			        		
				        	// Insert new MIME into the database.
				        	result = middle.insertMime(type, extension,
				        			preference, false);
				        	
				        	// End sub transaction.
				        	MiddleResult endTransResult = middle.endSubTransaction(result);
				        	if (endTransResult.isNotOK()) {
				        		break;
				        	}
				        	if (result.isNotOK()) {
				        		result.show(parent);
				        		break;
				        	}
			        	}
			        	else {
			        		break;
			        	}
			        }
		        }
	
			
		} catch (Exception e) {
			exception = e;
		}
		finally {
			
			if (parent != null) {
				parent.setCursor(Cursor.getDefaultCursor());
			}
			if (exception != null) {
				JOptionPane.showMessageDialog(parent, exception.getMessage());
			}
		}
	}

	/**
	 * Returns true value if the list contains ID.
	 * @param list
	 * @param id
	 * @return
	 */
	public static boolean contains(LinkedList<Long> list, long id) {
		
		for (long item : list) {
			
			if (item == id) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Create localized trace table.
	 * @param descriptor
	 * @param decorated 
	 */
	public static String createLocalizedTraceTable(String [][] descriptor, boolean decorated) {

		return createTraceTablePrivate(descriptor, decorated, true);
	}
	
	/**
	 * Create localized trace table.
	 * @param descriptor
	 * @param decorated 
	 */
	public static String createTraceTable(String [][] descriptor, boolean decorated) {

		return createTraceTablePrivate(descriptor, decorated, false);
	}
	
	/**
	 * Create trace table.
	 * @param descriptor
	 * @param decorated
	 * @return
	 */
	private static String createTraceTablePrivate(String[][] descriptor,
			boolean decorated, boolean localized) {
		
		if (descriptor.length == 0) {
			return "";
		}
		
		String trace = null;
		
		if (decorated) {
			
			// Begin table.
			trace = "<table class='TraceTable' cellspacing='0' cellpadding='2'>";
			
			for (String [] row : descriptor) {
				
				// Begin row.
				trace += "<tr>";
				
				boolean isCaption = true;
				
				for (String column : row) {
	
					// Localize first column (a caption).
					if (localized && isCaption) {
						column = Resources.getString(column) + ":";
						isCaption = false;
					}
					
					// Add column.
					trace += String.format("<td>%s</td>", column != null ? column.toString() : "null");
				}
				
				// End row.
				trace += "</tr>";
			}
			
			// End table.
			trace += "</table>";
		}
		else {
			
			// Begin table.
			trace = "";
			
			for (String [] row : descriptor) {

				boolean isCaption = true;
				
				for (String column : row) {
	
					// Localize first column (a caption).
					if (localized && isCaption) {
						column = Resources.getString(column) + ":";
						isCaption = false;
					}
					
					// Add column.
					trace += String.format("%s\t", column != null ? column.toString() : "null");
				}
				
				// End row.
				trace += "\n";
			}
		}
		
		return trace;
	}

	/**
	 * Create trace list.
	 * @param items
	 * @param decorated
	 * @return
	 */
	public static String createTraceList(Object[] items,
			boolean decorated) {
		
		if (items.length == 0) {
			return "";
		}
		
		String trace = null;
		
		if (decorated) {
			
			// Begin table.
			trace = "<table class='TraceTable'>";
			
			for (Object item : items) {
				
				// Add row.
				trace += String.format("<tr><td>%s</td></tr>", item.toString());
			}
			
			// End table.
			trace += "</table>";
		}
		else {
			
			// Begin list.
			trace = "";
			
			for (Object item : items) {

				// Add row.
				trace += String.format("%s\t", item.toString());

			}
			// End list.
			trace += "\n";
		}
		
		return trace;
	}
	
	/**
	 * Trim text with tags.
	 * @param codeText
	 * @return
	 */
	public static String trimTextWithTags(String codeText, boolean decorated) {
		
		codeText = codeText.replaceAll("\\[", "@lb;");
		
		if (!decorated) {
			return codeText;
		}

		return codeText.replaceAll("<", "&lt;");
	}

	/**
	 * Returns true value if the areas contains given area.
	 * @param areas
	 * @param area
	 * @return
	 */
	public static boolean contains(LinkedList<Area> areas, Area area) {
		
		for (Area areaItem : areas) {
			if (area.getId() == areaItem.getId()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Load application properties.
	 */
	public static void loadApplicationProperties() {
		
		File applicationPropertiesFile = new File("application.properties");
		if (!applicationPropertiesFile.exists()) {
			
			return;
		}
		
		InputStreamReader inputStream = null;
		
		try {
			inputStream = new InputStreamReader(
							new FileInputStream(applicationPropertiesFile));
			
			applicationProperties.load(inputStream);
		}
		catch (Exception e) {
			Utility.show2(null, e.getMessage());
		}
		finally {
			
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					Utility.show2(null, e.getMessage());
				}
			}
		}
	}

	/**
	 * Load servers settings.
	 */
	public static void loadServersProperties() {
		
		// Get user directory.
		String userDirectory = getUserDirectory();
		
		// Load servers' settings.
		File serversPropertiesFile = new File(userDirectory, serversSettingsFile);
		if (!serversPropertiesFile.exists()) {
			
			return;
		}
		
		InputStreamReader inputStream = null;
		
		try {
			inputStream = new InputStreamReader(
							new FileInputStream(serversPropertiesFile));
			
			serversProperties.load(inputStream);
		}
		catch (Exception e) {
			Utility.show2(null, e.getMessage());
		}
		finally {
			
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					Utility.show2(null, e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Save servers settings.
	 */
	@SuppressWarnings("deprecation")
	public static void saveServersProperties() {
		
		// Get user directory.
		String userDirectory = getUserDirectory();
		
		// Save servers' settings.
		File serversPropertiesFile = new File(userDirectory, serversSettingsFile);
		
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(serversPropertiesFile);
			serversProperties.save(outputStream, "Settings for www server and Multipage database");
			outputStream.close();
		}
		catch (Exception e) {
			Utility.show2(null, e.getMessage());
		}
		finally {
			
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (IOException e) {
					Utility.show2(null, e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Get user directory.
	 * @return
	 */
	public static String getUserDirectory() {
		
		// Get user directory
		String userProfile = applicationProperties.getProperty("user.home", "");
		if (userProfile.isEmpty()) {
			userProfile = System.getenv("LOCALAPPDATA");
		}
		
		// Check it.
		File directory = new File(userProfile);
		if (!directory.exists()) {
			return "";
		}
		
		// Compile user directory and create one if it doesn't exist
		String userDirectory = userProfile;
		
		if (!programName.isEmpty()) {
			userDirectory += File.separator + programName;
		}
		if (!cloneName.isEmpty()) {
			userDirectory += File.separator + cloneName;
		}
		if (!versionName.isEmpty()) {
			userDirectory += File.separator + versionName;
		}
		
		directory = new File(userDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		return userDirectory;
	}
	
	/**
	 * Set web interface directory.
	 * @param webInterfacePath
	 */
	public static void setWebInterfaceDirectory(String webInterfacePath) {
		
		serversProperties.put("web_interface_directory", webInterfacePath);
	}
	
	/**
	 * Get default web interface directory.
	 * @return
	 */
	public static String getDefaultWebInterfaceDirectory() {
		
		String defaultDirectory = getUserDirectory() + File.separator + webInterfaceFolder;
		return defaultDirectory;
	}
	
	/**
	 * Get web interface directory
	 */
	public static String getWebInterfaceDirectory() throws Exception {
		
		// Try to get path from server settings.
		String webInterfacePath = serversProperties.getProperty("web_interface_directory", "");
		if (webInterfacePath.isEmpty()) {
			
			// If it doesn't exist, create it.
			webInterfacePath = getDefaultWebInterfaceDirectory();
		}
		
		// Make directory if it doesn't exist.
		File webInterfaceDirectory = new File(webInterfacePath);
		if (!webInterfaceDirectory.exists()) {
			webInterfaceDirectory.mkdirs();
		}
		
		// Return path.
		return webInterfaceDirectory.getCanonicalPath();
	}

	/**
	 * Set database access.
	 * @param accessString
	 */
	public static void setDatabaseAccess(String accessString) {
		
		// Set new value.
		serversProperties.put("database_accesss", accessString);
	}
	
	/**
	 * Get database access.
	 * @return
	 */
	public static String getDatabaseAccess() {
		
		String userDirectory = getUserDirectory();
		return serversProperties.getProperty("database_accesss", userDirectory);
	}
	
	/**
	 * Get temporary directory.
	 * @return
	 */
	public static String getTemporaryDirectory() {
		
		// Set default name
		String temporaryDirectory = programName + "Temp";
		
		// Get temporary directory location
		try {
			temporaryDirectory = File.createTempFile("file", Long.toString(System.nanoTime())).getParent()
					 + File.separator + applicationProperties.getProperty("temporary_folder", temporaryDirectory);
		}
		catch (IOException e) {
		}
		
		// Create the directory if it doesn't exist
		File directory = new File(temporaryDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		return temporaryDirectory;
	}

	/**
	 * Get manual directory.
	 * @return
	 */
	public static String getManualDirectory() {
		
		String manualDirectory = applicationProperties.getProperty("manual_directory", "");
		
		// Check it.
		File directory = new File(manualDirectory);
		if (directory.exists()) {
		
			return manualDirectory;
		}
		return "";
	}
	
	/**
	 * Get video URL.
	 * @return
	 */
	public static String getWebVideoUrl() {
		
		String videoUrl = applicationProperties.getProperty("video_url", "");
		
		// Check it.
		File directory = new File(videoUrl);
		if (directory.exists()) {
		
			return videoUrl;
		}
		return "";
	}

	/**
	 * Get templates directory
	 * @return
	 */
	public static String getTemplatesDirectory() {
		
		String templatesDirectory = applicationProperties.getProperty("templates_directory", "");
		
		// Check it.
		File directory = new File(templatesDirectory);
		if (directory.exists()) {
		
			return templatesDirectory;
		}
		return "";
	}
	/**
	 * Get PHP directory.
	 * @return
	 */
	public static String getPhpDirectory() {
		
		String phpDirectory = serversProperties.getProperty("php_directory", "");
		
		// Check it.
		File directory = new File(phpDirectory);
		if (directory.exists()) {
		
			return phpDirectory;
		}
		return "";
	}
	
	/**
	 * Set PHP directory.
	 * @param phpDirectory
	 */
	public static void setPhpDirectory(String phpDirectory) {
		
		serversProperties.put("php_directory", phpDirectory);
	}
	
	/**
	 * Set initial Open and Save dialog path
	 */
	public static void initOpenSavePath() {
		
		String templatesDirectory = getTemplatesDirectory();
		if (!templatesDirectory.isEmpty()) {
			Utility.setCurrentPathName(templatesDirectory);
		}
	}
	
	/**
	 * Send message as using servlet response object.
	 * @param response
	 * @param message
	 */
	private static void sendMessage(HttpServletResponse response, String message) {
		
		ServletOutputStream outputStream = null;
		
		try {
			outputStream = response.getOutputStream();
			outputStream.print(message);
		}
		catch (Exception e) {
		}
		finally {
			
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	/**
     * This method clears http server root folder. It doesn't remove necessary files and folders.
     * Method sends to user and information about the result.
	 * @param password
	 * @param request 
     * @param response
	 * @return 
     */
	public static boolean clearServer(String password, Request request, HttpServletResponse response) {
		
		// If there is no request to clear the server, exit with false.
    	String clearServerPassword = request.getParameter("clear_server");;
    	if (clearServerPassword == null) {
    		return false;
    	}
    	
		// Check password.
		if (!clearServerPassword.equals(password)) {
			sendMessage(response, Resources.getString("middle.server.messageClearServerRequestBadPassord"));
			return true;
		}
		
		// Get web application root directory.
		String path = request.getServerRootPath();	
		File root = new File(path);
		
		// Excluded directories.
		final java.util.List<String> excluded = Arrays.asList("WEB-INF", "META-INF", "java");
		
		LinkedList<String> messages = new LinkedList<String>();
		
		File[] files = root.listFiles();
		for (File file : files) {
			
			if (file.isDirectory() && excluded.contains(file.getName())) {
				messages.add(String.format(
								Resources.getString("middle.server.messageClearServerExcludedDirectory"),
								file.getName()));
				continue;
			}
			
			// Remove file or directory with its content.
			try {
				Utility.deleteFolderContent(file);
				file.delete();
				
				messages.add(String.format(
								Resources.getString("middle.server.messageClearServerRemovedFile"),
								file.getName()));
			}
			catch (Exception e) {
				messages.add(e.getLocalizedMessage());
			}
		}
		
		// Remove file "unzipped.txt" located in temporary directory of web server.
		// The file contains IDs of previously unzipped resources.
		File unzippedFile = new File(request.getServerTempPath() + File.separator + AreaServer.unzippedFileName);
		if (unzippedFile.exists()) {
			
			unzippedFile.delete();
			messages.add(String.format(
					Resources.getString("middle.server.messageClearServerRemovedFile"),
					AreaServer.unzippedFileName));
		}
		
		// Inform user about exceptions.
		String compiledMessage = "<html><body>";
		for (String message : messages) {
			compiledMessage += String.format(Resources.getString("middle.server.messageClearServerMessage"),
							message) + "<br>\n";
		}
		
		sendMessage(response, compiledMessage + "</body></html>");
		
		return true;
	}
	
	/**
	 * Send file via HTTP.
	 * @param areaServer 
	 * @param request
	 * @param response
	 */
	public static void sendFileViaHttp(AreaServer areaServer, HttpServletRequest request, HttpServletResponse response) {
		
		// Create file object identified by URI.
		String uri = request.getRequestURI();
		String pathToUri = request.getServletContext().getRealPath(uri);
		File uriFile = new File(pathToUri);
		
		// If the file doesn't exist, send an error message.
		if (!uriFile.exists()) {
			sendMessage(response, String.format(
					Resources.getString("middle.server.messageRequestedUriDoesntExist"), uri));
			return;
		}
		
		
		// Send file content to client via HTTP.
		
		FileInputStream fileInputStream = null;
		FileChannel fileChannel = null;
		ServletOutputStream servletOutputStream = null;
		WritableByteChannel httpChannel = null;
		
		Exception exception = null;
		
		try {
			// Get file input channel.
			fileInputStream = new FileInputStream(uriFile);
			fileChannel = fileInputStream.getChannel();
			
			// Get servlet output channel.
			servletOutputStream = response.getOutputStream();
			httpChannel = Channels.newChannel(servletOutputStream);
			
			// Transfer data from file input channel to HTTP output channel.
			fileChannel.transferTo(0, uriFile.length(), httpChannel);
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			
			// Close resources.
			if (fileChannel != null) {
				try { fileChannel.close(); } catch (Exception e) {};
			}
			if (fileInputStream != null) {
				try { fileInputStream.close(); } catch (Exception e) {};
			}
			if (httpChannel != null) {
				try { httpChannel.close(); } catch (Exception e) {};
			}
			if (servletOutputStream != null) {
				try { servletOutputStream.close(); } catch (Exception e) {};
			}
		}
		
		// If an exception exists, send it to client via HTTP.
		if (exception != null) {
			sendMessage(response, exception.getLocalizedMessage());
		}
	}

	/**
	 * Returns file name that should be used for PHP file that relates to area.
	 * @param area
	 * @return
	 */
	public static String getAreaFileName(Area area) {
		
		// Try to get requested area name.
		String fileName = "unknown_area";
		if (area != null) {
			
			String areaName = area.getDescription();
			long areaId = area.getId();
			
			// Replace bad characters and set area name.
			areaName = Utility.replaceNonAsciiChars(areaName);
			areaName = areaName.replaceAll("[\\W]", "_");
			
			// Truncate the name.
			final int maxLength = 30;
			if (areaName.length() > maxLength) {
				areaName = areaName.substring(0, maxLength);
			}
			
			fileName = String.format("%s_%d", areaName, areaId);
		}
		
		return fileName;
	}
	
	/**
	 * Returns areas and its providers exposed by Area Server specified with URL
	 * @param url
	 * @param user
	 * @param password
	 * @param exception
	 * @return
	 */
	public static Hashtable<String, String []> requestExposedProviders(URL url, String user,
			String password, Obj<Exception> exception) {
		
		// Call Area Server API
		exception.ref = null;
		try {
			final URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput(true);
			
			urlConnection.setRequestProperty("AreaServer-User", password);
			urlConnection.setRequestProperty("AreaServer-Password", password);
			urlConnection.setRequestProperty("AreaServer-ApiCallType", "get-exposed-providers");
			
			urlConnection.connect();
			
			final InputStream inputStream = urlConnection.getInputStream();
			
			// TODO: read input stream
			inputStream.close();
		}
		catch (Exception e) {
			exception.ref = e;
		}
		
		// test
		Hashtable<String, String []> areasProviders = new Hashtable<String, String []>();
		areasProviders.put("main", new String [] {"isPhp", "html"});
		areasProviders.put("my_area1", new String [] {"bkColor", "js", "css"});
		areasProviders.put("my_area2", new String [] {"html", "php", "css"});
		
		return areasProviders;
	}
	
	/**
	 * Get external provider link path.
	 * @param link
	 * @return
	 */
	public static Path getExternalLinkPath(String link) {
		
		Obj<Path> path = new Obj<Path>(null);
		
		// Parse link.
		new ExternalLinkParser() {
					
			// If it is a file link.
			@Override
			public MiddleResult onFile(String filePath, String fileEncoding) {
				
				path.ref = Paths.get(filePath);
				
				return MiddleResult.OK;
			}
			
		}.parse(link);
		
		return path.ref;
	}
	
	/**
	 * Load external provider text.
	 * @param slotId
	 * @param externalText
	 * @return
	 */
	public static MiddleResult loadExternalProviderText(String link, Obj<String> externalText, Obj<String> path, Obj<String> encoding) {
		
		// Reset output.
		externalText.ref = null;
		
		// Parse link.
		MiddleResult result = new ExternalLinkParser() {
					
			// If it is a file link.
			@Override
			public MiddleResult onFile(String filePath, String fileEncoding) {
				
				if (path != null) {
					path.ref = filePath;
				}
				if (encoding != null) {
					encoding.ref = fileEncoding;
				}
				
				File file = new File(filePath);
				
				if (!file.exists()) {
					return MiddleResult.FILE_DOESNT_EXIST;
				}
				
				String textValue = null;
				
				// Load text content of the file
				try {
					textValue = FileUtils.readFileToString(file, fileEncoding);
				}
				catch (Exception e) {
					return MiddleResult.exceptionToResult(e);
				}
				
				if (textValue != null) {
					
					// Save new text value.
					externalText.ref = textValue;
					return MiddleResult.OK;
				}
				
				return MiddleResult.FILE_INPUT_ERROR;
			}
			
		}.parse(link);
		
		return result;
	}
	
	/**
	 * Load slot value from external provider
	 * @param slot
	 * @return
	 */
	public static MiddleResult loadSlotValueFromExternal(Slot slot) {
		
		// Get link in form "type;encoding;rest_of_the_link".
		String link = slot.getExternalProvider();
		if (link == null) {
			return MiddleResult.NULL_POINTER;
		}
		
		// Load external text using link string.
		Obj<String> externalText = new Obj<String>();
		MiddleResult result = loadExternalProviderText(link, externalText, null, null);
		
		// On error exit the method.
		if (result.isNotOK()) {
			return result;
		}
		
		// Set slot text value.
		slot.setTextValue(externalText.ref);
		
		return MiddleResult.OK;
	}
	
	/**
	 * Save output text to external provider using link string
	 * @param externalProviderLink
	 * @param outputText
	 */
	public static MiddleResult saveValueToExternalProvider(MiddleLight middle, String externalProviderLink, String outputText) {
		
		// Trim link string.
		if (externalProviderLink == null) {
			return MiddleResult.ERROR_LINK_NOT_FOUND;
		}
		
		// Parse link.
		return new ExternalLinkParser() {

			@Override
			public MiddleResult onFile(String filePath, String encoding) {
				
				// Output stream.
				final Obj<OutputStream> outputStream = new Obj<OutputStream>();
				
				MiddleResult result = MiddleResult.OK;
				
				try {
					StringBuilder outputTextBuilder = new StringBuilder(outputText);
					
					// Output stream.
					outputStream.ref = new BufferedOutputStream(new FileOutputStream(filePath));
					
					// Output string may contain @@RESOURCE tags that will be replaced
					// with contents of binary resources.
					result = new SaveParser() {
						
						// Write text.
						@Override
						public MiddleResult onText(int textStart, int textEnd) {
							
							try {
								String textToSave = outputTextBuilder.substring(textStart, textEnd);
								outputStream.ref.write(textToSave.getBytes(encoding));
							}
							catch (Exception e) {
								return MiddleResult.exceptionToResult(e);
							}
							return MiddleResult.OK;
						}
						
						// Write resource BLOB.
						@Override
						public MiddleResult onResource(long resourceId) {
							
							if (middle == null) {
								return MiddleResult.OK;
							}
							
							MiddleResult result = middle.loadResourceBlobToStream(resourceId, outputStream.ref);
							return result;
						}
						
					}.parse(outputTextBuilder);
				} 
				catch (Exception e) {
					
					// On exception.
					result =  MiddleResult.exceptionToResult(e);
				}
				finally {
					
					// Close output stream.
					if (outputStream.ref != null) {
						try {
							outputStream.ref.close();
						}
						catch (Exception e) {
						}
					}
				}
				
				return result;
			}
			
		}.parse(externalProviderLink);
	}
	
	/**
	 * Make external slot backup.
	 * @param slot
	 * @param fileExtension 
	 */
	public static void backup(Slot slot, String fileExtension) throws Exception {
		
		String externalProviderLink = slot.getExternalProvider();
		
		// Parse link.
		MiddleResult result =  new ExternalLinkParser() {

			@Override
			public MiddleResult onFile(String filePath, String encoding) {
				
				MiddleResult result = MiddleResult.OK;
				try {
					
					// Load file.
					File file = new File(filePath);
					String content = FileUtils.readFileToString(file, encoding);
					
					// Save to backup file.
					File backupFile = new File(filePath + "." + fileExtension);
					FileUtils.writeStringToFile(backupFile, content, encoding);
				} 
				catch (Exception e) {
					
					// On exception.
					result =  MiddleResult.exceptionToResult(e);
				}
				
				return result;
			}
			
		}.parse(externalProviderLink);
		
		result.throwPossibleException();
	}
	
	/**
	 * Lock external source.
	 * @param slot
	 * @param readOnly
	 */
	public static void lock(Slot slot, boolean readOnly) throws Exception {
		
		String externalProviderLink = slot.getExternalProvider();
		
		// Parse link.
		MiddleResult result =  new ExternalLinkParser() {

			@Override
			public MiddleResult onFile(String filePath, String encoding) {
				
				MiddleResult result = MiddleResult.OK;
				try {
					
					// Load file.
					File file = new File(filePath);
					file.setWritable(!readOnly);
				} 
				catch (Exception e) {
					
					// On exception.
					result =  MiddleResult.exceptionToResult(e);
				}
				
				return result;
			}
			
		}.parse(externalProviderLink);
		
		result.throwPossibleException();
	}
	
	/**
	 * Make chain of URL properties.
	 * @param otherProperties
	 * @return
	 */
	public static String chainUrlProperties(Properties otherProperties) {
		
		String extraProperties = "";
		if (otherProperties != null) {
			
			Object name;
			Object value;
			String valueText;
			
			for (Entry entry : otherProperties.entrySet()) {
				
				name = entry.getKey();
				if (name == null) {
					continue;
				}
				
				if (!extraProperties.isEmpty()) {
					extraProperties += '&';
				}
				
				extraProperties += name.toString();
				value = entry.getValue();
				
				if (value != null) {
					valueText = value.toString();
					
					if (!valueText.isEmpty()) {
						extraProperties += '=' + valueText;		
					}
				}
			}
		}
		
		return extraProperties;
	}
	
	/**
	 * Convert GUID object to string representation.
	 * @param guidObject
	 * @return
	 */
	public static String toGuidString(Object guidObject) {
		
		String guidString = null;
		
		if (guidObject instanceof UUID) {
			UUID guid = (UUID) guidObject;
			guidString = guid.toString();
		}
		else if (guidObject instanceof byte []) {
			
			byte [] b = (byte []) guidObject;
			if (b.length == 16) {
				
				guidString = String.format("%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-%02X%02X%02X%02X%02X%02X", b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7],
						b[8], b[9], b[10], b[11], b[12], b[13], b[14], b[15] );
			}
		}
		
		if (guidString == null) {
			guidString = "FFFFFFFF-FFFF-FFFF-FFFFFFFFFFFF";
		}
		
		return guidString;
	}
	
	/**
	 * Convert GUID string to byte array.
	 * @param guidString
	 * @return
	 */
	public static byte[] toGuidBytes(String guidString) {
		
		if (guidString == null) {
			return null;
		}
		
		UUID guid = UUID.fromString(guidString);
		
		long msb = guid.getMostSignificantBits();
		long lsb = guid.getLeastSignificantBits();
		
		ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
		buffer.putLong(0, msb);
		buffer.putLong(Long.BYTES, lsb);
		
		byte [] guidBytes = buffer.array();
		return guidBytes;
	}
	
	/**
	 * Get GUID from string.
	 * @param guidString
	 * @return
	 */
	public static UUID getGuidFromString(String guidString) {
		
		if (guidString == null) {
			return null;
		}
				
		UUID guid = UUID.fromString(guidString);
		return guid;
	}
}
