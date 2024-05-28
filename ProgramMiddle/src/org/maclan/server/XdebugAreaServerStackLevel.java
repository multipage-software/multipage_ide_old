/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-05-2024
 *
 */
package org.maclan.server;

import org.multipage.util.Obj;

/**
 * Area Srever stack level information for Xdebug viewer.
 * @author vakol
 */
public class XdebugAreaServerStackLevel {
	
	/**
	 * Stack level number.
	 */
	private int level = -1;

	/**
	 * Stack source code type.
	 */
	private String type = "eval";

	/**
	 * Source code.
	 */
	private String sourceCode = null;
	
	/**
	 * Start position of current tag in the source code text.
	 */
	private int cmdBegin = -1;

	/**
	 * Get current source code position.
	 */
	private int cmdEnd = -1;

	/**
	 * Constructor.
	 * @param type 
	 * @param level 
	 * @param state
	 */
	public XdebugAreaServerStackLevel(int level, String type, AreaServerState state) {
		
		this.level = level;
		this.type = type;
		
		if (state.text != null) {
			this.sourceCode = state.text.toString();
		}
		this.cmdBegin = state.tagStartPosition;
		this.cmdEnd  = state.position;
	}
	
	/**
	 * Constructor.
	 * @param level
	 * @param type
	 * @param cmdBegin
	 * @param cmdEnd
	 * @param sourceCode
	 */
	public XdebugAreaServerStackLevel(int level, String type, int cmdBegin, int cmdEnd, String sourceCode) {
		
		this.level = level;
		this.type = type;
		this.cmdBegin = cmdBegin;
		this.cmdEnd = cmdEnd;
		this.sourceCode = sourceCode;
	}
	
	/**
	 * Get stack level.
	 * @return
	 */
	public int getLevel() {
		
		return level;
	}
	
	/**
	 * Get source code type.
	 * @return
	 */
	public String getType() {
		
		return type;
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
	public int getCmdBegin() {
		
		return cmdBegin;
	}

	/**
	 * Get current source code position.
	 * @return
	 */
	public int getCmdEnd() {
		
		return cmdEnd;
	}
	
	/**
	 * Load area server text state from current stack level.
	 * @param textState
	 */
	public void loadAreaServerTextState(Obj<XdebugAreaServerTextState> textState) {
		
		// Create text state object.
		textState.ref = new XdebugAreaServerTextState();
		textState.ref.setTagStartPosition(cmdBegin);
		textState.ref.setPosition(cmdEnd);
	}
	
	/**
	 * Get text representation of the stack level object.
	 */
	@Override
	public String toString() {
		
		return String.format("[%d] %s", level, sourceCode);
	}
}
