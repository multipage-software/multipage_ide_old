/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public enum TextHolderType {

	UNKNOWN("middle.textTextHolderUnknown"),
	AREA("middle.textTextHolderArea"),
	RESOURCE("middle.textTextHolderResource"),
	AREASLOT("middle.textTextHolderAreaSlot"),
	PROGRAM("middle.textTextHolderProgram"),
	VERSION("middle.textHolderVersion");
	
	/**
	 * Text.
	 */
	private String text;
	
	/**
	 * Constructor.
	 */
	TextHolderType(String text) {
		
		this.text = Resources.getString(text);
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
}
