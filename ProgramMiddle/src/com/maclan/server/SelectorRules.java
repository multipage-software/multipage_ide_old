/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 10-03-2020
 *
 */
package com.maclan.server;

import java.util.LinkedList;

/**
 * @author user
 *
 */
class SelectorRules {
	
	public String selector;
	public LinkedList<CssPropertyValue> cssPropertiesValues = new LinkedList<CssPropertyValue>();

	public SelectorRules(String selector) {
		
		this.selector = selector;
	}

	/**
	 * Insert property and value.
	 * @param property
	 * @param value
	 * @param isImportant 
	 */
	public void insert(String property, String value, boolean isImportant) {
		
		CssPropertyValue cssPropertyValue = new CssPropertyValue(property, value, isImportant);
		cssPropertiesValues.add(cssPropertyValue);
	}
}
