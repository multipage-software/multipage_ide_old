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
public class AreaSourceData {

	/**
	 * Resource ID.
	 */
	public long resourceId;
	
	/**
	 * Version ID.
	 */
	public long versionId;
	
	/**
	 * Not localized flag.
	 */
	public boolean notLocalized;

	/**
	 * Constructor.
	 * @param resourceId
	 * @param notLocalized
	 * @param versionId
	 */
	public AreaSourceData(long resourceId, long versionId, boolean notLocalized) {

		this.resourceId = resourceId;
		this.versionId = versionId;
		this.notLocalized = notLocalized;
	}
}
