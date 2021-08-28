/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 25-05-2020
 *
 */
package org.maclan;

/**
 * Tells area server how to load slot.
 * @author vakol
 *
 */
public class LoadSlotHint {
	
	public static final int area = 1;			// Inherits from single area.
	public static final int superAreas = 2;		// Inherits from super areas.
	public static final int subAreas = 4;		// Inherits from sub areas.
	
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
