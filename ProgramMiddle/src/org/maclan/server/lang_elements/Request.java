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
public class Request implements BoxedObject {

	/**
	 * Area server reference.
	 */
	org.maclan.server.AreaServer server;
	
	/**
	 * Constructor.
	 * @param server
	 */
	public Request(org.maclan.server.AreaServer server) {
		
		this.server = server;
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return server.state.request;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		return "[Request object]";
	}
	
	/**
	 * Get parameter.
	 * @param name
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getParameter(String name) {
		
		return server.state.request.getParameter(name);
	}
	
	/**
	 * Get parameter.
	 * @param name
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getHeaderProperty(String name) {
		
		String headerProperty = server.state.request.getOriginalRequest().getHeader(name);
		return headerProperty;
	}
	
	/**
	 * Read all POSTed data using character encoding of the request.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String post() throws Exception {
		
		return server.state.request.post();
	}
}
