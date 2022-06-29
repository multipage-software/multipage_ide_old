/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 25-05-2022
 *
 */

package org.multipage.addinloader;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Miscellaneous helper functions.
 * @author vakol
 *
 */
public class AddInsUtility {
	
	/**
	 * Named properties.
	 */
	private static class NamedProperties {
	
		/**
		 * Properties.
		 */
		private Properties properties;
		
		/**
		 * Name.
		 */
		private String name;
		
		/**
		 * Constructor.
		 * @param properties
		 * @param name
		 */
		public NamedProperties(Properties properties, String name) {
	
			this.properties = properties;
			this.name = name;
		}
	
		/**
		 * Get name.
		 * @return
		 */
		public String getName() {
	
			return name;
		}
	
		/**
		 * Get properties.
		 * @return
		 */
		public Properties getProperties() {
	
			return properties;
		}
	}
	
	/**
	 * Primary language.
	 */
	private static String primaryLanguage = "en";
	private static String primaryCountry = "US";

	/**
	 * Inform user switch.
	 */
	public static final boolean informUserAboutError = false;

	/**
	 * Primary named properties list.
	 */
	private static LinkedList<NamedProperties> primaryNamedProperties = new LinkedList<NamedProperties>();
	
	/**
	 * Named properties list.
	 */
	private static LinkedList<NamedProperties> namedProperties = new LinkedList<NamedProperties>();
	
	/**
	 * Language and country that is used for current project.
	 */
	private static String language = "";
	private static String country = "";
	
	/**
	 * Set language and country.
	 * @wbp.parser.entryPoint
	 */
	public static void setLanguageAndCountry(String languagePar, String countryPar) {
		
		language = languagePar;
		country = countryPar;
	}
	
	/**
	 * Load resource.
	 * @param baseName
	 * @param key - Name representing given resource bundle.
	 */
	public static synchronized boolean loadResource(String baseName) {

		// If the properties already exist, exit the method.
		for (NamedProperties item : namedProperties) {
			
			if (item.getName().equals(baseName)) {
				return true;
			}
		}
		
		try {
			// Try to read primary resource.
			String fileName = "/" + baseName.replace('.', '/') + "_" + primaryLanguage + "_" + primaryCountry + ".properties";
			InputStream inputStream = AddInsUtility.class.getResourceAsStream(fileName);
			
			if (inputStream != null) {
				
				// Load properties from the input stream.
				Properties properties = new Properties();
				loadProperties(inputStream, properties);
				
				// Add new named properties to the list begin.
				primaryNamedProperties.addFirst(new NamedProperties(properties, baseName));
			}
			
			// Read non-primary resources.
			if (!(language.equals(primaryLanguage) && country.equals(primaryCountry))) {
				
				// Create input stream for given resource name.
				fileName = "/" + baseName.replace('.', '/') + "_" + language + "_" + country + ".properties";
				inputStream = AddInsUtility.class.getResourceAsStream(fileName);
			
				if (inputStream != null) {
					
					// Load properties from the input stream.
					Properties properties = new Properties();
					loadProperties(inputStream, properties);
					
					// Add new named properties to the list begin.
					namedProperties.addFirst(new NamedProperties(properties, baseName));
				}
			}
		}
		catch (Exception exception) {
			if (informUserAboutError) {
				JOptionPane.showMessageDialog(null, exception.getMessage(),
						"Load Resource Error", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
		


		return true;
	}
	
	/**
	 * Load properties.
	 * @param inputStream
	 * @param properties
	 */
	private static void loadProperties(InputStream inputStream,
			Properties properties) throws Exception {
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			
			boolean isFirst = true;
			
			while (true) {
				
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				
				if (isFirst) {
					// Remove possible UTF BOM (byte order mark) at the beginning of the input stream.				
					if (line.startsWith("\uFEFF")) {
						line = line.substring(1);
					}
					
					isFirst = false;
				}
				
				// Parse line.
				int equalSignPosition = line.indexOf("=");
				
				String key;
				String value;
				
				if (equalSignPosition != -1) {
					key = line.substring(0, equalSignPosition);
					value = line.substring(equalSignPosition + 1);
				}
				else {
					key = line;
					value = "";
				}
				
				// Trim texts.
				key = key.trim();
				value = value.trim();
				
				properties.put(key, value);
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			inputStream.close();
		}
	}

	/**
	 * GUI resource string getter.
	 * @param identifier
	 * @return
	 */
	public synchronized static String getString(String identifier) {
		
		String outputString = null;
		
		// Trim identifier.
		identifier = identifier.trim();
		
		// Search in list of named properties.
		for (NamedProperties item : namedProperties) {			
			
			Properties properties = item.getProperties();
			if (properties.containsKey(identifier)) {
				outputString = properties.getProperty(identifier);
				break;
			}
		}
		
		// If not found, try to search in primary properties.
		if (outputString == null) {
			
			for (NamedProperties item : primaryNamedProperties) {			
			
				Properties properties = item.getProperties();
				if (properties.containsKey(identifier)) {
					outputString = properties.getProperty(identifier);
					break;
				}
			}
		}

		if (outputString == null) {
			outputString = String.format("# %s not found #", identifier);
		}
		// Inform user about an error.
		if (outputString.isEmpty()) {
			if (informUserAboutError) {
				JOptionPane.showMessageDialog(null, "Unknown resource string: " + identifier,
						"Resource Loader Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return outputString;
	}

	/**
	 * Initializes namedResources.
	 */
	synchronized public static void initialize() {

		AddInsUtility.setLanguageAndCountry("en", "US");
	}

	/**
	 * Get language.
	 * @return
	 */
	public static String getLanguage() {

		return language;
	}

	/**
	 * Get country.
	 * @return
	 */
	public static String getCountry() {

		return country;
	}
	
	/**
	 * Get image.
	 */
	public static BufferedImage getImage(String urlString) {
		
		URL url = ClassLoader.getSystemResource(urlString);
		if (url != null) {
			try {
				BufferedImage image = ImageIO.read(url);
				return image;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * Get application path.
	 * @return
	 * @throws Exception 
	 */
	public static String getApplicationPath(Class<?> mainClass)
			throws Exception {
		
		// Get application file.
		URL applicationUrl = mainClass.getProtectionDomain().getCodeSource().getLocation();
		String applicationPath = new File(applicationUrl.getPath()).getCanonicalPath();
		return applicationPath;
	}
	
	/**
	 * Localize frame title.
	 * @param frame
	 * @param key - Resource bundle key.
	 */
	public static void localize(JFrame frame) {
		
		String title = frame.getTitle();
		title = getString(title);
		frame.setTitle(title);
	}

	/**
	 * Performs label localization.
	 * @param label
	 */
	public static void localize(JLabel label) {
		
		String caption = label.getText();
		caption = getString(caption);
		label.setText(caption);
	}
	
	/**
	 * Performs button localization.
	 * @param button
	 */
	public static void localize(JButton button) {
		
		// Localize caption.
		String caption = button.getText();
		if (caption != null && !caption.isEmpty()) {
			button.setText(getString(caption));
		}
		
		// Localize tool tip.
		String tooltip = button.getToolTipText();
		if (tooltip != null && !tooltip.isEmpty()) {
			button.setToolTipText(getString(tooltip));
		}
	}
	
	/**
	 * Show message.
	 * @param parent
	 * @param message
	 * @param parameters
	 */
	public static void show(Component parent, String message,
			Object ... parameters) {

		message = String.format(getString(message), parameters);
		JOptionPane.showMessageDialog(parent, message);
	}
	
	/**
	 * Run executable file using given command that is placed in directory
	 * designated by "path" argument.
	 * @param workingDirectoryPath
	 * @param command
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception 
	 */
	public static String runExecutable(String workingDirectoryPath, String command, Integer timeout, TimeUnit unit)
			throws Exception {
		
		StringBuilder text = new StringBuilder("");
		Exception exception = null;
		
		// Check working directory.
		File workingDirectory = new File(workingDirectoryPath);
		if (!workingDirectory.isDirectory()) {
			throw new Exception(String.format(
					getString("org.multipage.gui.messageUnknownWorkingDirectoryForExecutable"),
					workingDirectoryPath,
					command));
		}
		
		InputStream standardOutput = null;
		BufferedReader reader = null;
		try {
			
			// Run the command as a process and wait for it.
			Process process = Runtime.getRuntime().exec(command, null, workingDirectory);
			
			// Wait given time span for process termination.
			if (timeout != null && unit != null) {
				process.waitFor(timeout, unit);
	        
		        // Get its stdout and read the output text.
		        standardOutput = process.getInputStream();
				reader = new BufferedReader(new InputStreamReader(standardOutput));
				
				while (true) {
					
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					
					text.append(line);
					text.append("\n");
				}
			}
			else {
				standardOutput = process.getInputStream();
			}
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			
			// Close stdout.
			if (standardOutput != null) {
				try {
					standardOutput.close();
				}
				catch (Exception e) {
				}
			}
			
			// Close reader.
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
				}
			}
		}
		
		// If there is an exception, throw it.
		if (exception != null) {
			throw exception;
		}
		
		return text.toString();
	}
	
	/**
	 * Run executable file using given command that is placed in directory
	 * designated by "path" argument.
	 * @param workingDirectory
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public static String runExecutable(String workingDirectory, String command)
			throws Exception {
		
		// Delegate the call.
		String outputString = runExecutable(workingDirectory, command, null, null);
		return outputString;
	}
	
	/**
	 * Run executable JAR using java.exe.
	 * @param workingDirectory
	 * @param executableJarPath
	 * @param parameters
	 */
	public static String runExecutableJar(String workingDirectory, String executableJarPath, String [] parameters)
			throws Exception {
		
		// Get Java home directory.
		String javaExePath = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java.exe";
		
		// Compile java execution command.
		StringBuilder javaCommand = new StringBuilder();
		javaCommand.append('\"').append(javaExePath).append("\" -jar \"").append(executableJarPath).append('\"');
		
		// Add parameters.
		if (parameters != null) {
			for (String parameter : parameters) {
				javaCommand.append(" \"").append(parameter).append('\"');
			}
		}
		
		// Run java command.
		String result = runExecutable(workingDirectory, javaCommand.toString());
		return result;
	}
	
	/**
	 * Run executable JAR using java.exe.
	 * @param workingDirectory
	 * @param executableJarPath
	 */
	public static String runExecutableJar(String workingDirectory, String executableJarPath)
			throws Exception {
		
		// Delegate the call.
		String outputString = runExecutableJar(workingDirectory, executableJarPath, null);
		return outputString;
	}
}