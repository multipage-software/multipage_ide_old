/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import org.multipage.util.Obj;

/**
 * @author
 *
 */
public class CheckBoxListManager<T> {

	/**
	 * Loads item.
	 * @param index
	 * @param text
	 * @param selected
	 * @return
	 */
	protected boolean loadItem(int index, Obj<T> object,
			Obj<String> text, Obj<Boolean> selected) {
		return false;
	}

	/**
	 * Processes change.
	 * @param object
	 * @param selected
	 * @return
	 */
	protected boolean processChange(T object, boolean selected) {
		return false;
	}
}
