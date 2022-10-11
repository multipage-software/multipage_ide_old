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
 * This class is a wrapper for objects of type T.
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
