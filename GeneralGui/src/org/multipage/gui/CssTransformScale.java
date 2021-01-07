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
public class CssTransformScale extends CssTransform {

	/**
	 * Parameters.
	 */
	public float sx, sy;
	
	/**
	 * Constructor.
	 */
	public CssTransformScale() {
		
		this.sx = 0.0f;
		this.sy = 0.0f;
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("scale(%s, %s)",
				Utility.removeFloatNulls(String.valueOf(sx)),
				Utility.removeFloatNulls(String.valueOf(sy))
				);
	}

	/**
	 * Set values.
	 * @param scale
	 */
	public void setFrom(CssTransformScale scale) {

		this.sx = scale.sx;
		this.sy = scale.sy;
	}
}
