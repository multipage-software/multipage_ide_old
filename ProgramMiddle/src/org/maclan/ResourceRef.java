/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.Objects;

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
		ResourceRef other = (ResourceRef) obj;
		return Objects.equals(resourceId, other.resourceId);
	}
}