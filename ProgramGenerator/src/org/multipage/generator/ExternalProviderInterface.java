/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 11-02-2020
 *
 */
package org.multipage.generator;

import com.maclan.Area;

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
