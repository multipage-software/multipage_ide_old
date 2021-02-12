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
public class CssTransformTranslate extends CssTransform {

	/**
	 * Parameters.
	 */
	public float tx,ty;
	public String txUnits, tyUnits;
	
	/**
	 * Constructor.
	 */
	public CssTransformTranslate() {
		
		this.tx = 0.0f;
		this.ty = 0.0f;
		this.txUnits = "px";
		this.tyUnits = "px";
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("translate(%s%s, %s%s)",
				Utility.removeFloatNulls(String.valueOf(tx)),
				txUnits,
				Utility.removeFloatNulls(String.valueOf(ty)),
				tyUnits
				);
	}

	/**
	 * Set values.
	 * @param translate
	 */
	public void setFrom(CssTransformTranslate translate) {

		this.tx = translate.tx;
		this.ty = translate.ty;
		this.txUnits = translate.txUnits;
		this.tyUnits = translate.tyUnits;
	}
}
