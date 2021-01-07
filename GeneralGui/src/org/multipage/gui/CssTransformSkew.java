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
public class CssTransformSkew extends CssTransform {

	/**
	 * Parameters.
	 */
	public float ax, ay;
	public String axUnits, ayUnits;
	
	/**
	 * Constructor.
	 */
	public CssTransformSkew() {
		
		this.ax = 0.0f;
		this.axUnits = "deg";
		this.ay = 0.0f;
		this.ayUnits = "deg";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("skew(%s%s, %s%s)",
				Utility.removeFloatNulls(String.valueOf(ax)),
				axUnits,
				Utility.removeFloatNulls(String.valueOf(ay)),
				ayUnits
				);
	}

	/**
	 * Set values.
	 * @param skew
	 */
	public void setFrom(CssTransformSkew skew) {

		this.ax = skew.ax;
		this.axUnits = skew.axUnits;
		this.ay = skew.ay;
		this.ayUnits = skew.ayUnits;
	}
}
