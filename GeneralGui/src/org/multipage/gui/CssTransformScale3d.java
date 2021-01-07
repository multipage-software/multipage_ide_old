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
public class CssTransformScale3d extends CssTransform {

	/**
	 * Parameters.
	 */
	public float sx, sy, sz;
	
	/**
	 * Constructor.
	 */
	public CssTransformScale3d() {
		
		this.sx = 0.0f;
		this.sy = 0.0f;
		this.sz = 0.0f;
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("scale3d(%s, %s, %s)",
				Utility.removeFloatNulls(String.valueOf(sx)),
				Utility.removeFloatNulls(String.valueOf(sy)),
				Utility.removeFloatNulls(String.valueOf(sz))
				);
	}

	/**
	 * Set values.
	 * @param scale
	 */
	public void setFrom(CssTransformScale3d scale) {

		this.sx = scale.sx;
		this.sy = scale.sy;
		this.sz = scale.sz;
	}
}
