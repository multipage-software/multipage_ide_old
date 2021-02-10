/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * Resource reference.
 * @author
 *
 */
public class ResourceRef {

	public Long resourceId;
	public String description;
	public Long mimeTypeId;
	public boolean isProtected;
	public boolean isVisible;
	public String text;
	public Long dataStart;
	public Long dataEnd;
	public Boolean isBlob;
	
	// Auxiliary fields.
	public Long newResourceId;
	
	/**
	 * Returns true value if data exist.
	 * @return
	 */
	public boolean existsBlob() {
		
		if (isBlob != null && isBlob) {
			return true;
		}
		
		if (dataStart == null || dataEnd == null) {
			return false;
		}
		
		return dataStart < dataEnd;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResourceRef [resourceId=" + resourceId + ", description="
				+ description + ", mimeTypeId=" + mimeTypeId + ", isProtected="
				+ isProtected + ", isVisible=" + isVisible + ", text=" + text
				+ ", dataStart=" + dataStart + ", dataEnd=" + dataEnd
				+ ", newResourceId=" + newResourceId + "]";
	}
}