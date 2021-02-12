/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server.lang_elements;

//graalvm import org.graalvm.polyglot.HostAccess;

/**
 * @author
 *
 */
public class RenderClass implements BoxedObject {

	/**
	 * Middle layer object.
	 */
	com.maclan.server.AreaServer server;
	
	/**
	 * Name field.
	 */
	//graalvm @HostAccess.Export
	public final String name;
	
	/**
	 * Text field.
	 */
	//graalvm @HostAccess.Export
	public final String text;
	
	/**
	 * Constructor.
	 * @param server
	 */
	public RenderClass(com.maclan.server.AreaServer server) {
		
		this.server = server;
		
		if (server.state.response.renderClass != null) {
			
			this.name = server.state.response.renderClass.getName();
			this.text = server.state.response.renderClass.getText();
		}
		else {
			this.name = null;
			this.text = null;
		}
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return server.state.response.renderClass;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return "[RenderClass object]";
	}
}
