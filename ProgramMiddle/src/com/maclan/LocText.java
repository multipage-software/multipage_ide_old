/*
 * Copyright 2010-2017 (C) vakol
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
public class LocText {

	public Long textId;
	public Long languageId;
	public String text;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LocText [textId=" + textId + ", languageId=" + languageId
				+ ", text=" + text + "]";
	}
}