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
public class Request implements BoxedObject {

	/**
	 * Area server reference.
	 */
	com.maclan.server.AreaServer server;
	
	/**
	 * Constructor.
	 * @param server
	 */
	public Request(com.maclan.server.AreaServer server) {
		
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
	public String getParameter(String name) {
		
		return server.state.request.getParameter(name);
	}
	
	/**
	 * Get parameter.
	 * @param name
	 * @return
	 */
	public String getHeaderProperty(String name) {
		
		String headerProperty = server.state.request.getOriginalRequest().getHeader(name);
		return headerProperty;
	}
	
	/**
	 * Read all POSTed data using character encoding of the request.
	 * @return
	 */
	public String post() throws Exception {
		
		return server.state.request.post();
	}
}
