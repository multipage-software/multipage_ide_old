/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * @author
 *
 */
public class DefaultValue {

	/**
	 * Default value text.
	 */
	@Override
	public String toString() {
		
		return "$default";
	}

	/**
	 * Returns true value if the object is of this type.
	 */
	@Override
	public boolean equals(Object object) {
		
		return object instanceof DefaultValue;
	}
}
