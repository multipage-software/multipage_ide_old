/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * 
 * @author
 *
 */
public class SlotData {

	public Long areaId;
	public String alias;
	public Long revision;
	public Timestamp created;
	public Long localizedTextValueId;
	public String textValue;
	public Long integerValue;
	public Double realValue;
	public String access;
	public Long id;
	public boolean hidden;
	public Boolean booleanValue;
	public Long enumerationValueId;
	public Long color;
	public Long descriptionId;
	public boolean isDefault;
	public String name;
	public String valueMeaning;
	public boolean preferred;
	public boolean userDefined;
	public String specialValue;
	public Long areaValue;
	public String externalProvider;
	public Boolean readsInput;
	public Boolean writesOutput;
	
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
		SlotData other = (SlotData) obj;
		return Objects.equals(alias, other.alias) && Objects.equals(areaId, other.areaId);
	}
}