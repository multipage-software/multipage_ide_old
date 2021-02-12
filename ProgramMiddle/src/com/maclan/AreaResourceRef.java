/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

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
}