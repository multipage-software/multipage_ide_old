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
public class StartResource {

	/**
	 * Resource ID.
	 */
	public Long resourceId;
	
	/**
	 * MIME type.
	 */
	public String mimeType;

	/**
	 * MIME extension.
	 */
	public String mimeExtension;
	
	/**
	 * Resource not localized.
	 */
	public Boolean notLocalized;

	/**
	 * Found area.
	 */
	public Area foundArea;

	/**
	 * Constructor.
	 * @param resourceId
	 * @param mimeType
	 * @param mimeExtension
	 * @param notLocalized 
	 * @param foundArea
	 */
	public StartResource(long resourceId, String mimeType,
			String mimeExtension, Boolean notLocalized, Area foundArea) {
		
		this.resourceId = resourceId;
		this.mimeType = mimeType;
		this.mimeExtension = mimeExtension;
		this.notLocalized = notLocalized;
		this.foundArea = foundArea;
	}
}
