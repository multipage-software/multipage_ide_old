/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 11-02-2020
 *
 */
package org.multipage.generator;

import org.maclan.Area;

/**
 * @author user
 *
 */
public interface ExternalProviderInterface {
	
	/**
	 * Set editor from link string.
	 * @param area 
	 */
	void setEditor(String link, Area area);
}
