/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

/**
 * @author
 *
 */
public class Variable {

	/**
	 * Name.
	 */
	public String name;
	
	/**
	 * Value.
	 */
	public Object value;
	
	/**
	 * Constructor.
	 * @param name
	 * @param value
	 */
	public Variable(String name, Object value) {
		
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructor.
	 */
	public Variable() {
		
		name = "";
		value = null;
	}
}
