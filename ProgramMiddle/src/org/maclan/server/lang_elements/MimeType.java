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
public class MimeType implements BoxedObject {

	/**
	 * Middle layer object.
	 */
	org.maclan.MimeType mimeType;
	
	/**
	 * Public fields.
	 */
	//graalvm @HostAccess.Export
	public final String type;
	//graalvm @HostAccess.Export
	public final String extension;
	
	/**
	 * Constructor.
	 * @param mimeType
	 */
	public MimeType(org.maclan.MimeType mimeType) {
		
		this.mimeType = mimeType;
		
		this.type = mimeType.type;
		this.extension = mimeType.extension;
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return mimeType;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return "[MimeType object]";
	}
}
