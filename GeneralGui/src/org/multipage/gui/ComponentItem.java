/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import org.multipage.util.SimpleMethodRef;

/**
 * 
 * @author
 *
 */
public class ComponentItem {

	/**
	 * Description.
	 */
	private String description;
	
	/**
	 * Method.
	 */
	private SimpleMethodRef method;

	/**
	 * Constructor.
	 * @param description
	 * @param method
	 */
	public ComponentItem(String description, SimpleMethodRef method) {

		this.description = description;
		this.method = method;
	}

	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return description;
	}

	/**
	 * @return the method
	 */
	public SimpleMethodRef getMethod() {
		return method;
	}
}
