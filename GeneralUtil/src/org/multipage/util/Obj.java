/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.util;

/**
 * @author
 * 
 * Use this class as a parameter to a method. Method than sets ref property.
 * After the method returns the ref property is set and you can use it.
 * This class is a wrapper of T type object.
 */
public class Obj<T> {

	/**
	 * Reference.
	 */
	public T ref;
	
	/**
	 * Constructor.
	 */
	public Obj() {

		ref = null;
	}
	
	/**
	 * Constructor.
	 * @param referencedObject
	 */
	public Obj(T referencedObject) {

		ref = referencedObject;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return ref.toString();
	}
}
