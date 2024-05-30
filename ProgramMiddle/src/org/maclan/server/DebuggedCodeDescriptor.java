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
	private TagsSource tagsSource = null;
	
	/**
	 * Debugged tag name.
	 */
	private String tagName = null;
	
	/**
	 * Debugged tag properties.
	 */
	private Properties properties = null;
	
	/**
	 * Start position for replacement.
	 */
	private int cmdBegin = -1;
	
	/**
	 * Stop position for replacement.
	 */	
	private int cmdEnd = -1;
	
	/**
	 * Inner text.
	 */
	private String innerText = null;
	
	/**
	 * Current replacement text.
	 */
	private String replacement = null;
	
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
		this.cmdBegin = start;
		this.cmdEnd = stop;
		this.replacement = replacement;
	}
	
	/**
	 * Set debugged code properties.
	 * @param tagName
	 * @param properties
	 * @param start
	 * @param stop
	 * @param innerText
	 * @param replacement
	 */
	public void set(String tagName, Properties properties, int start, int stop, String innerText,
			String replacement) {
		
		this.tagName = tagName;
		this.properties = properties;
		this.cmdBegin = start;
		this.cmdEnd = stop;
		this.innerText = innerText;
		this.replacement = replacement;		
	}
	
	/**
	 * Get tags source.
	 * @return
	 */
	public TagsSource gatTagsSource() {
		
		return tagsSource;
	}

	/**
	 * Get command begin position.
	 * @return
	 */
	public int getCmdBegin() {
		
		return cmdBegin;
	}

	/**
	 * Get command end position.
	 * @return
	 */
	public int getCmdEnd() {
		
		return cmdEnd;
	}
}
