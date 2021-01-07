/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author
 *
 */
public class CssProperty {

	/**
	 * Property types.
	 */
	public static final char ALL = '*';
	public static final char ANIMATED = 'a';
	
	/**
	 * Properties set.
	 */
	private static HashSet<CssProperty> properties = new HashSet<CssProperty>();
	
	/**
	 * Load CSS properties.
	 */
	static {
		InputStream inputStream = null;
		
		try {
			// Load CSS properties from a text file.
			inputStream = CssProperty.class.getResourceAsStream("/org/multipage/gui/properties/css_properties.txt");
			if (inputStream != null) {
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				
				while ((line = reader.readLine()) != null) {
					
					CssProperty property = parseProperty(line);
					if (property != null) {
						properties.add(property);
					}
				}
			}
		}
		catch (Exception e) {
			
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
			}
		}
	}

	/**
	 * Parse property.
	 * @param line
	 * @return
	 */
	private static CssProperty parseProperty(String line) {
		
		String [] parts = line.split(",");
		int length = parts.length;
		
		// Get last flags.
		boolean flagsFound = false;
		String flags = null;
		
		if (length > 1) {
			flags = parts[length - 1].trim();
			
			// Check flags.
			if (checkFlags(flags)) {
				flagsFound = true;
			}
			else {
				flags = "n";
			}
		}
		
		// Get property names.
		int lastPropertyIndex = length - (flagsFound ? 2 : 1);
		LinkedList<String> propertyNames = new LinkedList<String>();
		
		for (int index = 0; index <= lastPropertyIndex; index++) {
			
			String propertyName = parts[index].trim();
			if (!propertyName.isEmpty()) {
				
				propertyNames.add(propertyName);
			}
		}
		
		// Check if a property name exists.
		if (propertyNames.isEmpty()) {
			return null;
		}
		
		// Create and return property object.
		return new CssProperty(propertyNames, flags);
	}

	/**
	 * Returns true value if flags are correct.
	 * @param flags
	 * @return
	 */
	private static boolean checkFlags(String flags) {
		
		final char [] flagArray = { 'n', 'a'};
		
		for (int index = 0; index < flags.length(); index++) {
			char character = flags.charAt(index);
			
			boolean isFound = false;
			for (int index2 = 0; index2 < flagArray.length; index2++) {
				char flagCharacter = flagArray[index2];
				
				if (character == flagCharacter) {
					isFound = true;
					break;
				}
			}
			
			if (!isFound) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Get properties.
	 * @param type 
	 * @return
	 */
	public static LinkedList<CssProperty> getProperties(char type) {

		LinkedList<CssProperty> foundProperties = new LinkedList<CssProperty>();
		
		// Find properties.
		properties.forEach(new Consumer<CssProperty>() {
			@Override
			public void accept(CssProperty property) {
				
				if (property.isFlag(type)) {
					foundProperties.add(property);
				}
			}
		});
		
		// Sort the output list.
		Collections.sort(foundProperties, new Comparator<CssProperty>() {
			@Override
			public int compare(CssProperty property1, CssProperty property2) {
				
				String primalName1 = property1.getPrimalName();
				String primalName2 = property2.getPrimalName();
				
				return primalName1.compareTo(primalName2);
			}});
		
		return foundProperties;
	}

	/**
	 * Property names.
	 */
	private LinkedList<String> propertyNames;
	
	/**
	 * Flags.
	 */
	private String flags;

	/**
	 * Constructor.
	 * @param propertyNames
	 * @param flags
	 */
	public CssProperty(LinkedList<String> propertyNames, String flags) {
		
		this.propertyNames = propertyNames;
		this.flags = flags;
	}

	/**
	 * Constructor.
	 * @param propertyName
	 */
	public CssProperty(String propertyName) {
		
		this.propertyNames = new LinkedList<String>();
		this.propertyNames.add(propertyName);
		this.flags = "n";
	}

	/**
	 * Returns true value if this property has given flag.
	 * @param flag
	 * @return
	 */
	protected boolean isFlag(char flag) {
		
		if (flag == '*') {
			return true;
		}
		return flags.indexOf(flag) != -1;
	}
	
	/**
	 * Get primal name.
	 * @return
	 */
	public String getPrimalName() {
		
		if (propertyNames.isEmpty()) {
			return "";
		}
		return propertyNames.getFirst();
	}
	
	/**
	 * Get hash code.
	 */
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((propertyNames == null) ? 0 : (propertyNames.isEmpty() ? 0 : getPrimalName().hashCode()));
		return result;
	}

	/**
	 * Returns true value if objects equal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CssProperty other = (CssProperty) obj;
		if (propertyNames == null) {
			if (other.propertyNames != null)
				return false;
		} else if (!propertyNames.equals(other.propertyNames))
			return false;
		
		return true;
	}

	/**
	 * To string.
	 */
	@Override
	public String toString() {
		return "properties=" + propertyNames.toString() + ", flags="
				+ flags + "]";
	}

	/**
	 * Get property HTML text.
	 * @return
	 */
	public String getHtmlText() {
		
		String text = "";
		boolean isFirst = true;
		
		for (String propertyName : propertyNames) {
			
			if (!isFirst) {
				text += ", ";
				text += propertyName;
			}
			else {
				text += String.format("<b>%s</b>", propertyName);
			}
			
			isFirst = false;
		}
		
		return String.format("<html>%s</html>", text);
	}

	/**
	 * Get text.
	 * @return
	 */
	public String getText() {
		
		String text = "";
		boolean isFirst = true;
		
		for (String propertyName : propertyNames) {
			
			if (!isFirst) {
				text += ", ";
				text += propertyName;
			}
			else {
				text += propertyName;
			}
			
			isFirst = false;
		}
		
		return text;
	}

	/**
	 * Get property names.
	 * @return
	 */
	public LinkedList<String> getNames() {
		
		return propertyNames;
	}
}
