/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.Objects;

/**
 * @author
 *
 */
public class AreaSource {

	/**
	 * Area ID.
	 */
	public Long areaId;
	
	/**
	 * Resource ID.
	 */
	public Long resourceId;
	
	/**
	 * Version ID.
	 */
	public Long versionId;
	
	/**
	 * Not localized flag.
	 */
	public Boolean notLocalized;

	/**
	 * An empty constructor.
	 */
	public AreaSource() {
		
	}
	
	/**
	 * Constructor.
	 * @param versionId
	 * @param resourceId
	 * @param notLocalized
	 */
	public AreaSource(long areaId, long resourceId, long versionId, boolean notLocalized) {
		
		this.resourceId = resourceId;
		this.versionId = versionId;
		this.notLocalized = notLocalized;
		this.areaId = areaId;
	}

	/**
	 * Get object description.
	 */
	@Override
	public String toString() {
		return "AreaSource [areaId=" + areaId + ", versionId=" + versionId + ", resourceId=" + resourceId
				+ ", notLocalized=" + notLocalized + "]";
	}
	
	/**
	 * Check if input object equals this object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaSource other = (AreaSource) obj;
		return Objects.equals(areaId, other.areaId);
	}
}
