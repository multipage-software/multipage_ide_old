/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.util.LinkedList;

import org.maclan.Area;
import org.maclan.VersionObj;

/**
 * 
 * @author
 *
 */
public class AmbiguousFileName {
	
	/**
	 * File name.
	 */
	public String fileName;
	
	/**
	 * List of areas.
	 */
	public LinkedList<AmbiguousFileNameItem> items = new LinkedList<AmbiguousFileNameItem>();

	/**
	 * Constructor.
	 * @param fileName
	 */
	public AmbiguousFileName(String fileName) {
		
		this.fileName = fileName;
	}

	/**
	 * Add item.
	 * @param area
	 * @param version
	 */
	public void addItem(Area area, VersionObj version) {
		
		for (AmbiguousFileNameItem item : items) {
			if (item.area.equals(area) && item.version.equals(version)) {
				return;
			}
		}
		
		items.add(new AmbiguousFileNameItem(area, version));
	}

	/**
	 * Returns true if the file is ambiguous.
	 * @return
	 */
	public boolean isAmbiguous() {
		
		return items.size() > 1;
	}
}