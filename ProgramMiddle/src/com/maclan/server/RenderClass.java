/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

/**
 * @author
 *
 */
public class RenderClass {

	/**
	 * Render class name.
	 */
	private String name;
	
	/**
	 * Render class description.
	 */
	private String text;

	/**
	 * Constructor.
	 * @param name
	 * @param text
	 */
	public RenderClass(String name, String text) {
		
		this.setName(name);
		this.setText(text);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
}
