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
@FunctionalInterface
public interface Callback {

	/**
	 * Callback method.
	 * @param input
	 * @return
	 */
	public abstract Object run(Object input);
}
