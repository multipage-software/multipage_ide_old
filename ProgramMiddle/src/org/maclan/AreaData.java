/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.LinkedList;
import java.util.Objects;

/**
 * 
 * @author
 *
 */
public class AreaData {
	
	public Long id;
	public String guid;
	public Long startResourceId;
	public Long descriptionId;
	public Boolean visible;
	public String alias;
	public Boolean readOnly;
	public String help;
	public Boolean localized;
	public String filename;
	public Long versionId;
	public String folder;
	public Long constructorsGroupId;
	public Long constructorHolderId;
	public Boolean startResourceNotLocalized;
	public Long relatedAreaId;
	public String fileExtension;
	public String constructorAlias;
	public Boolean canImport;
	public Boolean projectRoot;

	// Auxiliary fields.
	public boolean mark = false;
	public LinkedList<AreaData> subAreaDataList = new LinkedList<AreaData>();
	public long newId;
	public Boolean enabled;
	
	/**
	 * Covert to string.
	 */
	@Override
	public String toString() {
		if (id == null) {
			return "null";
		}
		return String.valueOf(id);
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
		AreaData other = (AreaData) obj;
		return Objects.equals(id, other.id);
	}
}