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
public class EnumerationData {

	/**
	 * Identifier.
	 */
	public long id;
	
	/**
	 * Description.
	 */
	public String description;

	/**
	 * New identifier.
	 */
	private long newId;

	/**
	 * "Created new" flag.
	 */
	private boolean createdNew = false;

	/**
	 * Set new ID.
	 * @param newId
	 */
	public void setNewId(long newId) {
		
		this.newId = newId;
	}

	/**
	 * Get new ID.
	 * @return
	 */
	public long getNewId() {
		
		return newId;
	}

	/**
	 * Set "created new" flag.
	 */
	public void setCreatedNew() {
		
		createdNew = true;
	}

	/**
	 * Returns true value if the enumeration is new.
	 * @return
	 */
	public boolean isCreatedNew() {
		
		return createdNew;
	}
}
