/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server.lang_elements;

import java.awt.Dimension;

/**
 * @author
 *
 */
public class Resource implements BoxedObject {

	/**
	 * Middle layer area server reference.
	 */
	private com.maclan.server.AreaServer server;
	
	/**
	 * Middle object reference.
	 */
	com.maclan.Resource resource;
	
	/**
	 * Public fields.
	 */
	public final long id;
	public final MimeType mime;
	
	/**
	 * Constructor.
	 * @param server 
	 * @param middleResource
	 * @throws Exception 
	 */
	public Resource(com.maclan.server.AreaServer server, com.maclan.Resource resource) throws Exception {
		
		this.server = server;
		this.resource = resource;
		
		this.id = resource.getId();
		
		server.getMimeType(resource.getMimeTypeId());
		this.mime = new MimeType(server.getMimeType(resource.getMimeTypeId()));
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return resource;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return String.format("[Resource object id = %d]", id);
	}
	
	/**
	 * Gets resource length.
	 * @return
	 */
	public long getLength() throws Exception {
		

		return server.getResourceLength(resource);
	}
	
	/**
	 * Gets image size or null if it is not an image.
	 * @return
	 * @throws Exception 
	 */
	public Dimension getImageSize() throws Exception {
		
		return server.getImageSize(resource);
	}
	
	/**
	 * Get description.
	 * @return
	 */
	public String getDescription() {
		
		return resource.getDescription();
	}
}
