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
public class EnumerationValueData {

	/**
	 * Identifier.
	 */
	public long id;
	
	/**
	 * Enumeration ID.
	 */
	public long enumerationId;

	/**
	 * Value.
	 */
	public String value;

	/**
	 * Description.
	 */
	public String description;

	/**
	 * New identifier.
	 */
	private long newId;

	/**
	 * Set new ID.
	 * @param newId
	 */
	public void setNewId(long newId) {
		
		this.newId = newId;
	}
	
	/**
	 * Get new ID.
	 */
	public long getNewId() {
		
		return newId;
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
		EnumerationValueData other = (EnumerationValueData) obj;
		return id == other.id;
	}
}
