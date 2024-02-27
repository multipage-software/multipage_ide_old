/*
 * Copyright 2010-2017 (C) sechance
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
