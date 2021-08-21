/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

/**
 * @author
 *
 */
public class LocalizedText {

	/**
	 * Text.
	 */
	private String text;
	
	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Language ID.
	 */
	private long languageId;
	
	/**
	 * Constructor.
	 */
	public LocalizedText(long id, String text, long languageId) {
		
		this.id = id;
		this.text = text;
		this.setLanguageId(languageId);
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param languageId the languageId to set
	 */
	public void setLanguageId(long languageId) {
		this.languageId = languageId;
	}

	/**
	 * @return the languageId
	 */
	public long getLanguageId() {
		return languageId;
	}
}
