/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

/**
 * @author
 *
 */
public interface TabContainerComponent {

	/**
	 * Get tab description;
	 * @return
	 */
	String getTabDescription();
	
	/**
	 * Reload component.
	 */
	void reload();
}
