/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 25-05-2020
 *
 */
package org.maclan;

/**
 * Tells area server how to load slot.
 * @author user
 *
 */
public enum LoadSlotHint {
	
	area(1),			// Inherits from single area.
	superAreas(2),		// Inherits from super areas.
	subAreas(4);		// Inherits from sub areas.
	
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
