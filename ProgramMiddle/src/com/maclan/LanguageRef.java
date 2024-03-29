/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * 
 * @author
 *
 */
public class LanguageRef {

	public Long id;
	public String alias;
	public String description;
	public long priority;

	public Long dataStart;
	public Long dataEnd;
	
	// Auxiliary fields.
	public long newId;

	/**
	 * Returns true value if flag exists.
	 * @return
	 */
	public boolean existsFlag() {
		
		if (dataStart == null || dataEnd == null) {
			return false;
		}
		
		return dataStart < dataEnd;
	}
}