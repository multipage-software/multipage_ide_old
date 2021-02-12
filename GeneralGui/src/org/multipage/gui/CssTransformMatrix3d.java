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
public class CssTransformMatrix3d extends CssTransform {

	/**
	 * Parameters.
	 */
	public float a1, b1, c1, d1, a2, b2, c2, d2, a3, b3, c3, d3, a4, b4, c4, d4;
	
	/**
	 * Constructor.
	 */
	public CssTransformMatrix3d() {
		
		this.a1 = 0.0f;
		this.b1 = 0.0f;
		this.c1 = 0.0f;
		this.d1 = 0.0f;
		this.a2 = 0.0f;
		this.b2 = 0.0f;
		this.c2 = 0.0f;
		this.d2 = 0.0f;
		this.a3 = 0.0f;
		this.b3 = 0.0f;
		this.c3 = 0.0f;
		this.d3 = 0.0f;
		this.a4 = 0.0f;
		this.b4 = 0.0f;
		this.c4 = 0.0f;
		this.d4 = 0.0f;
	}
	
	/**
	 * Get string value.
	 */
	@Override
	public String toString() {
		
		return String.format("matrix3d(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
				Utility.removeFloatNulls(String.valueOf(a1)),
				Utility.removeFloatNulls(String.valueOf(b1)),
				Utility.removeFloatNulls(String.valueOf(c1)),
				Utility.removeFloatNulls(String.valueOf(d1)),
				Utility.removeFloatNulls(String.valueOf(a2)),
				Utility.removeFloatNulls(String.valueOf(b2)),
				Utility.removeFloatNulls(String.valueOf(c2)),
				Utility.removeFloatNulls(String.valueOf(d2)),
				Utility.removeFloatNulls(String.valueOf(a3)),
				Utility.removeFloatNulls(String.valueOf(b3)),
				Utility.removeFloatNulls(String.valueOf(c3)),
				Utility.removeFloatNulls(String.valueOf(d3)),
				Utility.removeFloatNulls(String.valueOf(a4)),
				Utility.removeFloatNulls(String.valueOf(b4)),
				Utility.removeFloatNulls(String.valueOf(c4)),
				Utility.removeFloatNulls(String.valueOf(d4))
				);
	}

	/**
	 * Set values.
	 * @param matrix
	 */
	public void setFrom(CssTransformMatrix3d matrix) {
		
		this.a1 = matrix.a1;
		this.b1 = matrix.b1;
		this.c1 = matrix.c1;
		this.d1 = matrix.d1;
		this.a2 = matrix.a2;
		this.b2 = matrix.b2;
		this.c2 = matrix.c2;
		this.d2 = matrix.d2;
		this.a3 = matrix.a3;
		this.b3 = matrix.b3;
		this.c3 = matrix.c3;
		this.d3 = matrix.d3;
		this.a4 = matrix.a4;
		this.b4 = matrix.b4;
		this.c4 = matrix.c4;
		this.d4 = matrix.d4;
	}
}
