/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

/**
 * @author
 *
 */
public class AreaResourceExtension {
	
	/**
	 * Area reference.
	 */
	protected Area area;
	
	/**
	 * Local description.
	 */
	protected String localDescription;
	
	/**
	 * Identifier.
	 */
	protected long id;

	/**
	 * Return the local description.
	 */
	public String getLocalDescription() {
		return localDescription;
	}

	/**
	 * Set local description.
	 */
	public void setLocalDescription(String localDescription) {
		this.localDescription = localDescription;
	}

	/**
	 * @return the area
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * @param area the area to set
	 */
	public void setArea(Area area) {
		this.area = area;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof AreaResourceExtension) {
			AreaResourceExtension extension = (AreaResourceExtension) obj;
			
			if (extension.area == null ^ area == null) {
				return false;
			}
			
			if (extension.area != null && !extension.area.equals(area)) {
				return false;
			}

			if (extension.localDescription == null ^ localDescription == null) {
				return false;
			}
			
			if (extension.localDescription != null && !extension.localDescription.equals(localDescription)) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * Get area resource ID.
	 * @return
	 */
	public long getId() {
		
		return id;
	}

	/**
	 * Set ID.
	 * @param id
	 */
	public void setId(long id) {
		
		this.id = id;
	}
}
