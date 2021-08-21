/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

import java.awt.Dimension;

//graalvm import org.graalvm.polyglot.HostAccess;

/**
 * @author
 *
 */
public class Resource implements BoxedObject {

	/**
	 * Middle layer area server reference.
	 */
	private org.maclan.server.AreaServer server;
	
	/**
	 * Middle object reference.
	 */
	org.maclan.Resource resource;
	
	/**
	 * Public fields.
	 */
	//graalvm @HostAccess.Export
	public final long id;
	//graalvm @HostAccess.Export
	public final MimeType mime;
	
	/**
	 * Constructor.
	 * @param server 
	 * @param middleResource
	 * @throws Exception 
	 */
	public Resource(org.maclan.server.AreaServer server, org.maclan.Resource resource) throws Exception {
		
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
	//graalvm @HostAccess.Export
	public long getLength() throws Exception {
		

		return server.getResourceLength(resource);
	}
	
	/**
	 * Gets image size or null if it is not an image.
	 * @return
	 * @throws Exception 
	 */
	//graalvm @HostAccess.Export
	public Dimension getImageSize() throws Exception {
		
		return server.getImageSize(resource);
	}
	
	/**
	 * Get description.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getDescription() {
		
		return resource.getDescription();
	}
}
