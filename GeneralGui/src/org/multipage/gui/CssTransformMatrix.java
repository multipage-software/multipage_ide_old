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
public class CssTransformMatrix extends CssTransform {

	/**
	 * Parameters.
	 */
	public float a,b,c,d,tx,ty;
	
	/**
	 * Constructor.
	 */
	public CssTransformMatrix() {
		
		this.a = 0.0f;
		this.b = 0.0f;
		this.c = 0.0f;
		this.d = 0.0f;
		this.tx = 0.0f;
		this.ty = 0.0f;
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
				
		return String.format("matrix(%s, %s, %s, %s, %s, %s)",
				Utility.removeFloatNulls(String.valueOf(a)),
				Utility.removeFloatNulls(String.valueOf(b)),
				Utility.removeFloatNulls(String.valueOf(c)),
				Utility.removeFloatNulls(String.valueOf(d)),
				Utility.removeFloatNulls(String.valueOf(tx)),
				Utility.removeFloatNulls(String.valueOf(ty))
				);
	}

	/**
	 * Set values.
	 * @param matrix
	 */
	public void setFrom(CssTransformMatrix matrix) {
		
		this.a = matrix.a;
		this.b = matrix.b;
		this.c = matrix.c;
		this.d = matrix.d;
		this.tx = matrix.tx;
		this.ty = matrix.ty;
	}
}
