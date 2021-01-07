/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

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
}
