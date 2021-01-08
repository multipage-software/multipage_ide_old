/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package com.maclan.server;

/**
 * CSS lookup table value.
 * @author user
 *
 */
class CssLookupTableValue {
	
	// Fields.
	public String propertyName;
	public boolean isImportant;
	public boolean process;

	// Constructor.
	public CssLookupTableValue(String propertyName, boolean isImportant, boolean process) {
		
		this.propertyName = propertyName;
		this.isImportant = isImportant;
		this.process = process;
	}
}