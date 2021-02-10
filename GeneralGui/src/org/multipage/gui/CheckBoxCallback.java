/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
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
