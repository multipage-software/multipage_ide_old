/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import com.maclan.Area;
import com.maclan.VersionObj;

/**
 * 
 * @author
 *
 */
public class AmbiguousFileNameItem {
	
	/**
	 * Area.
	 */
	public Area area;
	
	/**
	 * Version.
	 */
	public VersionObj version;
	
	/**
	 * Constructor.
	 * @param area
	 * @param version
	 */
	public AmbiguousFileNameItem(Area area, VersionObj version) {
		
		this.area = area;
		this.version = version;
	}
}