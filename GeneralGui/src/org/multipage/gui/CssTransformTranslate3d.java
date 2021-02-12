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
public class CssTransformTranslate3d extends CssTransform {

	/**
	 * Parameters.
	 */
	public float tx, ty, tz;
	public String txUnits, tyUnits, tzUnits;
	
	/**
	 * Constructor.
	 */
	public CssTransformTranslate3d() {
		
		this.tx = 0.0f;
		this.ty = 0.0f;
		this.tz = 0.0f;
		this.txUnits = "px";
		this.tyUnits = "px";
		this.tzUnits = "px";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("translate3d(%s%s, %s%s, %s%s)",
				Utility.removeFloatNulls(String.valueOf(tx)),
				txUnits,
				Utility.removeFloatNulls(String.valueOf(ty)),
				tyUnits,
				Utility.removeFloatNulls(String.valueOf(tz)),
				tzUnits
				);
	}

	/**
	 * Set values.
	 * @param translate
	 */
	public void setFrom(CssTransformTranslate3d translate) {

		this.tx = translate.tx;
		this.ty = translate.ty;
		this.tz = translate.tz;
		this.txUnits = translate.txUnits;
		this.tyUnits = translate.tyUnits;
		this.tzUnits = translate.tzUnits;
	}
}
