/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-05-2024
 *
 */
package org.maclan.server;

import java.util.LinkedList;

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
	 * State hash code that enables to identify the stack level in Area Server.
	 */
	private int stateHashCode = -1;

	/**
	 * Source code.
	 */
	private String sourceCode = null;
	
	/**
	 * Debugged code information.
	 */
	private DebugInfo debugInfo = null;

	/**
	 * Watched items.
	 */
	private LinkedList<DebugWatchItem> watchItems = null;

	/**
	 * Constructor.
	 * @param type 
	 * @param level 
	 * @param state
	 */
	public XdebugAreaServerStackLevel(int level, String type, AreaServerState state) {
		
		this.level = level;
		this.type = type;
		this.stateHashCode = state.hashCode();
		
		if (state.text != null) {
			this.sourceCode = state.text.toString();
		}
		
		this.setDebugInfo(state.debugInfo);
	}
	
	/**
	 * Constructor.
	 * @param level
	 * @param type
	 * @param stateHashCode
	 * @param cmdBegin
	 * @param cmdEnd
	 * @param sourceCode
	 */
	public XdebugAreaServerStackLevel(int level, String type, int stateHashCode, int cmdBegin, int cmdEnd, String sourceCode) {
		
		this.level = level;
		this.type = type;
		this.stateHashCode = stateHashCode;
		this.sourceCode = sourceCode;
		
		if (debugInfo == null) {
			debugInfo = new DebugInfo();
		}

		DebugTagInfo tagInfo = debugInfo.getTagInfo();
		if (tagInfo == null) {
			tagInfo = new DebugTagInfo();
			debugInfo.setTagInfo(tagInfo);
		}
		
		tagInfo.setCmdBegin(cmdBegin);
		tagInfo.setCmdEnd(cmdEnd);
	}
	
	/**
	 * Set debugged code information.
	 * @param debuggedCodeInfo
	 */
	public void setDebugInfo(DebugInfo debuggedCodeInfo) {
		
		this.debugInfo = debuggedCodeInfo;
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
	 * Get Area Server state hash code.
	 * @return
	 */
	public int getStateHashCode() {
		
		return stateHashCode;
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
		
		if (debugInfo == null) {
			return -1;
		}
		
		DebugTagInfo tagInfo = debugInfo.getTagInfo();
		if (tagInfo == null) {
			return -1;
		}
		
		return tagInfo.getCmdBegin();
	}

	/**
	 * Get tag end position.
	 * @return
	 */
	public int getCmdEnd() {
		
		if (debugInfo == null) {
			return -1;
		}
		
		DebugTagInfo tagInfo = debugInfo.getTagInfo();
		if (tagInfo == null) {
			return -1;
		}
		
		return tagInfo.getCmdEnd();
	}
	
	/**
	 * Get debug information.
	 * @param textState
	 */
	public DebugInfo getDebuggedCodeInfo() {
		
		return debugInfo;
	}
	
	/**
	 * Get text representation of the stack level object.
	 */
	@Override
	public String toString() {
		
		return String.format("[%d] %s", level, sourceCode);
	}
}
