/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 10-03-2020
 *
 */
package com.maclan.server;

/**
 * Global CSS rules.
 */
class CssPropertyValue {
	
	public String property;
	public String value;
	public boolean isImportant;

	public CssPropertyValue(String property, String value, boolean isImportant) {
		
		this.property = property;
		this.value = value;
		this.isImportant = isImportant;
	}
}