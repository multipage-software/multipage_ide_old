/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * MIME data.
 * @author
 *
 */
public class Mime {

	public Long mimeId;
	public String extension;
	public String type;
	public Boolean preference;
	
	// Auxiliary fields.
	public Long newId;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Mime [mimeId=" + mimeId + ", extension=" + extension
				+ ", type=" + type + ", preference=" + preference + "]";
	}
}