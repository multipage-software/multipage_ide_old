/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.Objects;

/**
 * Area resource reference.
 * @author
 *
 */
public class AreaResourceRef {

	public Long areaId;
	public Long resourceId;
	public String localDescription;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AreaResourceRef [areaId=" + areaId + ", resourceId="
				+ resourceId + ", localDescription=" + localDescription + "]";
	}
	
	/**
	 * Check if input object equals to this object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaResourceRef other = (AreaResourceRef) obj;
		return Objects.equals(areaId, other.areaId);
	}
	
	
}