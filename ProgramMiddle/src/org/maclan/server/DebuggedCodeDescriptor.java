/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 01-05-2024
 *
 */
package org.maclan.server;

import java.util.Properties;

/**
 * Descriptor of debugged code.
 * @author vakol
 */
public class DebuggedCodeDescriptor {
	
	/**
	 * Determines source of debugged code.
	 */
	public TagsSource tagsSource = null;
	
	/**
	 * Debugged tag name.
	 */
	public String tagName = null;
	
	/**
	 * Debugged tag properties.
	 */
	public Properties properties = null;
	
	/**
	 * Start position for replacement.
	 */
	public int start = -1;
	
	/**
	 * Stop position for replacement.
	 */	
	public int stop = -1;
	
	/**
	 * Current replacement text.
	 */
	public String replacement = null;

	/**
	 * Set debugged code properties. 
	 * @param tagsSource
	 * @param replacement
	 */
	public void set(TagsSource tagsSource, String replacement) {
		
		this.tagsSource = tagsSource;
		this.replacement = replacement;
	}
	
	/**
	 * Set debugged code properties.
	 * @param tagName
	 * @param properties
	 * @param start
	 * @param stop
	 * @param replacement
	 */
	public void set(String tagName, Properties properties, int start, int stop, String replacement) {
		
		this.tagName = tagName;
		this.properties = properties;
		this.start = start;
		this.stop = stop;
		this.replacement = replacement;
	}
}
