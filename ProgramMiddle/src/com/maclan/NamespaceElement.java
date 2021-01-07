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
public class NamespaceElement implements Element {

	/**
	 * Pattern ID.
	 */
	public Long id;

	/**
	 * Pattern description.
	 */
	protected String description;
	
	/**
	 * Parent namespace ID.
	 */
	protected Long parentNamespaceId;
	
	/**
	 * Constructor.
	 */
	public NamespaceElement(String description, Long parentNamespaceId, Long id) {

		this.description = description;
		this.parentNamespaceId = parentNamespaceId;
		this.id = id;
	}
	
	/**
	 * Constructor.
	 */
	public NamespaceElement() {

		description = "";
		parentNamespaceId = 0L;
		id = 0L;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the parentNamespaceId
	 */
	public long getParentNamespaceId() {
		return parentNamespaceId;
	}

	/**
	 * @param parentNamespaceId the parentNamespaceId to set
	 */
	public void setParentNamespaceId(Long parentNamespaceId) {
		this.parentNamespaceId = parentNamespaceId;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
}
