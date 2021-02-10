/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 25-05-2020
 *
 */
package com.maclan;

/**
 * Tells area server how to load slot.
 * @author user
 *
 */
public enum LoadSlotHint {
	
	area(1),			// Inherits from single area.
	superAreas(2),		// Inherits from super areas.
	subAreas(4);
	
	/**
	 * Hint code.
	 */
	public int code;

	/**
	 * Constructor.
	 * @param code
	 */
	LoadSlotHint(int code) {
		
		this.code = code;
	}
}
