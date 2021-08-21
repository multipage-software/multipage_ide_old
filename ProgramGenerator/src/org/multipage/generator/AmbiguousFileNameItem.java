/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import org.maclan.Area;
import org.maclan.VersionObj;

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