/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

/**
 * @author
 *
 */
public class CssTransformRotate extends CssTransform {

	/**
	 * Parameters.
	 */
	public float a;
	public String units;
	
	/**
	 * Constructor.
	 */
	public CssTransformRotate() {
		
		this.a = 0.0f;
		this.units = "deg";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("rotate(%s%s)",
				Utility.removeFloatNulls(String.valueOf(a)),
				units
				);
	}

	/**
	 * Set values.
	 * @param rotate
	 */
	public void setFrom(CssTransformRotate rotate) {

		this.a = rotate.a;
		this.units = rotate.units;
	}
}
