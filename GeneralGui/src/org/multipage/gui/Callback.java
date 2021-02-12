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
@FunctionalInterface
public interface Callback {

	/**
	 * Callback method.
	 * @param input
	 * @return
	 */
	public abstract Object run(Object input);
}
