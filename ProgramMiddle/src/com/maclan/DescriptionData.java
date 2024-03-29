/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * @author
 *
 */
public class DescriptionData {

	/**
	 * Identifier.
	 */
	public Long id;
	
	/**
	 * Description.
	 */
	public String description;

	/**
	 * New ID. (while importing)
	 */
	public Long newId;

	/**
	 * Constructor.
	 * @param id
	 * @param description
	 */
	public DescriptionData(Long id, String description) {
		
		this.id = id;
		this.description = description;
	}
}
