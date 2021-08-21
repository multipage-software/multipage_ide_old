/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class AreaReference {

	/**
	 * Area ID.
	 */
	public long areaId;
	
	/**
	 * Possible area object.
	 */
	private Area area;

	/**
	 * A constructor with area ID.
	 * @param areaId
	 */
	public AreaReference(long areaId) {
		
		this.areaId = areaId;
	}
	
	/**
	 * A constructor with area.
	 * @param area
	 */
	public AreaReference(Area area) {
		
		areaId = area.getId();
		this.area = area;
	}
	
	/**
	 * Gets true value if an area object exists.
	 */
	public boolean existsAreaObject() {
		
		return area != null;
	}
	
	/**
	 * Get area object.
	 */
	public Area getAreaObject() {
		
		return area;
	}
	
	/**
	 * Set area object.
	 * 
	 */
	public void setAreaObject(Area area) {
		
		this.area = area;
	}

	/**
	 * Get text representation.
	 * @return
	 */
	public String getText() {
		
		if (existsAreaObject()) {
			return "->" + area.getDescriptionForced(true);
		}
		return "->" + String.format(Resources.getString("middle.textAreaReferenceId"), areaId);
	}
	
	/**
	 * Get string representation.
	 */
	@Override
	public String toString() {
		
		return getText();
	}

	/**
	 * Get area id.
	 * @return
	 */
	public Long getId() {

		if (existsAreaObject()) {
			return area.getId();
		}
		return null;
	}
}
