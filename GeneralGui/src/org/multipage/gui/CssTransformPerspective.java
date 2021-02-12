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
public class CssTransformPerspective extends CssTransform {

	/**
	 * Parameters.
	 */
	public float l;
	public String units;
	
	/**
	 * Constructor.
	 */
	public CssTransformPerspective() {
		
		this.l = 0.0f;
		this.units = "px";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("perspective(%s%s)",
				Utility.removeFloatNulls(String.valueOf(l)),
				units
				);
	}

	/**
	 * Set values.
	 * @param perspective
	 */
	public void setFrom(CssTransformPerspective perspective) {

		this.l = perspective.l;
		this.units = perspective.units;
	}
}
