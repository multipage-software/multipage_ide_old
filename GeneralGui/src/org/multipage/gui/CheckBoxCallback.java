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
public interface CheckBoxCallback<T> {

	/**
	 * Must return true if the object matches.
	 * @param object
	 * @return
	 */
	 boolean matches(T object);
}
