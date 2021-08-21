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
public class AreaVersion {

	/**
	 * Area reference.
	 */
	private Area area;
	
	/**
	 * Version reference.
	 */
	private VersionObj version;

	/**
	 * Constructor.
	 * @param area
	 * @param version
	 */
	public AreaVersion(Area area, VersionObj version) {

		this.area = area;
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((area == null) ? 0 : area.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaVersion other = (AreaVersion) obj;
		if (area == null) {
			if (other.area != null)
				return false;
		} else if (!area.equals(other.area))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	/**
	 * Get area.
	 * @return
	 */
	public Area getArea() {
		
		return area;
	}

	/**
	 * Get version.
	 * @return
	 */
	public VersionObj getVersion() {
		
		return version;
	}
	
}
