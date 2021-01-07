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
public class VersionData implements Element {

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * New ID.
	 */
	private Long newId;
	
	/**
	 * Alias.
	 */
	private String alias;
	
	/**
	 * Description ID.
	 */
	private Long descriptionId;
	
	/**
	 * Constructor.
	 */
	public VersionData() {
		
		this.id = 0L;
		this.newId = null;
		this.alias = "";
		this.descriptionId = null;
	}
	/**
	 * Constructor.
	 * @param id
	 * @param alias
	 * @param descriptionId
	 */
	public VersionData(long id, String alias, Long descriptionId) {
		
		this.id = id;
		this.newId = null;
		this.alias = alias;
		this.descriptionId = descriptionId;
	}

	/**
	 * Set ID.
	 * @param id
	 */
	public void setId(long id) {
		
		this.id = id;
	}

	/**
	 * Set alias.
	 * @param alias
	 */
	public void setAlias(String alias) {
		
		this.alias = alias;
	}

	/**
	 * Set description ID.
	 * @param descriptionId
	 */
	public void setDescriptionId(Long descriptionId) {
		
		this.descriptionId = descriptionId;
	}

	/**
	 * Get ID.
	 */
	@Override
	public long getId() {
		
		return id;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		
		return alias;
	}

	/**
	 * @return the descriptionId
	 */
	public Long getDescriptionId() {
		
		return descriptionId;
	}
	
	/**
	 * Set new ID.
	 * @param newId
	 */
	public void setNewId(Long newId) {
		
		this.newId = newId;
	}
	
	/**
	 * @return the newId
	 */
	public Long getNewId() {
		return newId;
	}
}
