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
public class CssTransformRotate3d extends CssTransform {

	/**
	 * Parameters.
	 */
	public float x, y, z, a;
	public String aUnits;
	
	/**
	 * Constructor.
	 */
	public CssTransformRotate3d() {
		
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
		this.a = 0.0f;
		this.aUnits = "deg";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("rotate3d(%s, %s, %s, %s%s)",
				Utility.removeFloatNulls(String.valueOf(x)),
				Utility.removeFloatNulls(String.valueOf(y)),
				Utility.removeFloatNulls(String.valueOf(z)),
				Utility.removeFloatNulls(String.valueOf(a)),
				aUnits
				);
	}

	/**
	 * Set values.
	 * @param rotate
	 */
	public void setFrom(CssTransformRotate3d rotate) {

		this.x = rotate.x;
		this.y = rotate.y;
		this.z = rotate.z;
		this.a = rotate.a;
		this.aUnits = rotate.aUnits;
	}
}
