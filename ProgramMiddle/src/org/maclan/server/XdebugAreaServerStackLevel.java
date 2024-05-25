/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-05-2024
 *
 */
package org.maclan.server;

/**
 * Area Srever stack level information for Xdebug viewer.
 * @author vakol
 */
public class XdebugAreaServerStackLevel {
	
	/**
	 * Source code.
	 */
	private String sourceCode = null;
	
	/**
	 * Start position of current tag in the source code text.
	 */
	private int tagStartPosition = -1;
	
	/**
	 * Get current source code position.
	 */
	private int position = -1;

	/**
	 * Constructor.
	 * @param state
	 */
	public XdebugAreaServerStackLevel(AreaServerState state) {
		
		if (state.text != null) {
			this.sourceCode = state.text.toString();
		}
		this.tagStartPosition = state.tagStartPosition;
		this.position  = state.position;
	}
	
	/**
	 * Get current source code.
	 * @return
	 */
	public String getSourceCode() {
		
		return sourceCode;
	}

	/**
	 * Get tag start position.
	 * @return
	 */
	public int getTagStartPosition() {
		
		return tagStartPosition;
	}

	/**
	 * Get current source code position.
	 * @return
	 */
	public int getPosition() {
		
		return position;
	}
}
