/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server.lang_elements;

/**
 * @author
 *
 */
public class Response implements BoxedObject {

	/**
	 * Area server reference.
	 */
	com.maclan.server.AreaServer server;

	/**
	 * Constructor.
	 * @param server
	 */
	public Response(com.maclan.server.AreaServer server) {
		
		this.server = server;
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return server.state.response;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return "[Response object]";
	}
	
	/**
	 * Set response header.
	 * @param headerName
	 * @param headerContent
	 */
	public void setHeader(String headerName, String headerContent) {
		
		server.state.response.setHeader(headerName, headerContent);
	}
	
	/**
	 * Set render class.
	 * @param renderClassName
	 * @param renderClassText
	 */
	public void setRenderClass(String renderClassName, String renderClassText) {
		
		server.state.response.setRenderClass(renderClassName, renderClassText);
	}
	
	/**
	 * Get render class.
	 */
	public RenderClass getRenderClass() {
		
		if (server.isRendering()) {
			return new RenderClass(server);
		}
		return null;
	}
}
