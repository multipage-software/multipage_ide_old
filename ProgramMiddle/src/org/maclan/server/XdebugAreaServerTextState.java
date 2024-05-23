/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 23-05-2024
 *
 */
package org.maclan.server;

/**
 * Area server text replacement state.
 * @author vakol
 */
public class XdebugAreaServerTextState {
	
	/**
	 * Tag start position.
	 */
	private int tagStartPosition = -1;
	
	/**
	 * Position.
	 */
	private int position = -1;
	
	/**
	 * Set tag start position.
	 * @param parseLong
	 */
	public void setTagStartPosition(int tagStartPosition) {
		
		this.tagStartPosition = tagStartPosition;
	}
	
	/**
	 * Get tag start position.
	 * @return Returns the tag start position.
	 */
	public int getTagStartPosition() {
		
		return tagStartPosition;
	}
	
	/**
	 * Set text position.
	 * @param position
	 */
	public void setPosition(int position) {

		this.position = position;
	}
	
	/**
	 * Get current text position.
	 * @return Returns current AreaServer text position.
	 */
	public int getPosition() {

		return position;
	}
}
