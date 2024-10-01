/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-05-2024
 *
 */
package org.maclan.server;

import java.util.LinkedList;

import org.multipage.util.j;

/**
 * Area Srever stack level information for Xdebug viewer.
 * @author vakol
 */
public class XdebugStackLevel {
	
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
	 * Command begin position.
	 */
	private int cmdBegin = 0;
	
	/**
	 * Command end position.
	 */
	private int cmdEnd = 0;
	
	/**
	 * Session reference.
	 */
	private XdebugListenerSession session = null;
	
	/**
	 * Constructor.
	 * @param type 
	 * @param level 
	 * @param state
	 */
	public XdebugStackLevel(int level, String type, AreaServerState state) {
		
		this.level = level;
		this.type = type;
		this.stateHashCode = state.hashCode();
		
		if (state.text != null) {
			this.sourceCode = state.text.toString();
		}
		
		this.cmdBegin = state.debugInfo.getCmdBegin();
		this.cmdEnd = state.debugInfo.getCmdEnd();
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
	public XdebugStackLevel(int level, String type, int stateHashCode, int cmdBegin, int cmdEnd, String sourceCode) {
		
		this.level = level;
		this.type = type;
		this.stateHashCode = stateHashCode;
		this.sourceCode = sourceCode;

		this.cmdBegin = cmdBegin;
		this.cmdEnd = cmdEnd;
	}
	
	/**
	 * Set session references.
	 * @param stack
	 * @param xdebugListenerSession
	 */
	public static void setSessionReferences(LinkedList<XdebugStackLevel> stack,
			XdebugListenerSession xdebugListenerSession) {
		
		stack.forEach(level -> level.session = xdebugListenerSession);
	}
	
	/**
	 * Get session.
	 * @return
	 */
	public XdebugListenerSession getSession() {
		
		return session;
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
			
		return cmdBegin;
	}

	/**
	 * Get tag end position.
	 * @return
	 */
	public int getCmdEnd() {
		return cmdEnd;
	}
	
	/**
	 * Get text representation of the stack level object.
	 */
	@Override
	public String toString() {

		final int maxLength = 30;
		
		int end = sourceCode.length() - 1;
		if (end > maxLength) {
			end = maxLength;
		}
		
		return String.format("[%d] %s", level, sourceCode).substring(0, end);
	}
}
