/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

//graalvm import org.graalvm.polyglot.HostAccess;

/**
 * @author
 *
 */
public class Response implements BoxedObject {

	/**
	 * Area server reference.
	 */
	org.maclan.server.AreaServer server;

	/**
	 * Constructor.
	 * @param server
	 */
	public Response(org.maclan.server.AreaServer server) {
		
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
	//graalvm @HostAccess.Export
	public void setHeader(String headerName, String headerContent) {
		
		server.state.response.setHeader(headerName, headerContent);
	}
	
	/**
	 * Set render class.
	 * @param renderClassName
	 * @param renderClassText
	 */
	//graalvm @HostAccess.Export
	public void setRenderClass(String renderClassName, String renderClassText) {
		
		server.state.response.setRenderClass(renderClassName, renderClassText);
	}
	
	/**
	 * Get render class.
	 */
	//graalvm @HostAccess.Export
	public RenderClass getRenderClass() {
		
		if (server.isRendering()) {
			return new RenderClass(server);
		}
		return null;
	}
}
