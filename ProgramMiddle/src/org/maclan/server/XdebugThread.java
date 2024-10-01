/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 09-07-2024
 *
 */
package org.maclan.server;

import java.util.LinkedList;

/**
 * Xdebug thread node.
 * @author vakol
 */
public class XdebugThread {
	
	/**
	 * Thread ID.
	 */
	private long threadId = -1L;
	
	/**
	 * Thread name.
	 */
	private String threadName = "";
	
	/**
	 * List of stack levels.
	 */
	private LinkedList<XdebugStackLevel> stack = null;
	
	/**
	 * Current stack level.
	 */
	private XdebugStackLevel currentStackLevel = null;
	
	/**
	 * Set thread
	 * @param threadId
	 * @param threadName
	 */
	public void setThread(Long threadId, String threadName) {
		
		this.threadId = threadId;
		this.threadName = threadName;
	}
	
	/**
	 * Get thread ID.
	 * @return
	 */
	public long getThreadId() {

		return threadId;
	}
	
	/**
	 * Get thread name.
	 * @return
	 */
	public String getThreadName() {
		
		return threadName;
	}
	
	/**
	 * Set thread stack.
	 * @param stack
	 */
	public void setStack(LinkedList<XdebugStackLevel> stack) {
		
		this.stack = stack;
	}
	
	/**
	 * Get thread stack.
	 * @return
	 */
	public LinkedList<XdebugStackLevel> getStack() {
		
		return stack;
	}
	
	/**
	 * Set current stack level.
	 * @param stackLevel
	 * @return
	 */
	public XdebugStackLevel setCurrentStackLevel(XdebugStackLevel stackLevel) {
		
		if (stackLevel == null) {
			currentStackLevel = null;
			return null;
		}
		
		XdebugStackLevel foundStackLevel = null;
		int hashCode = stackLevel.getStateHashCode();
		
		for (XdebugStackLevel stackLevelItem : stack) {
			
			int foundhashCode = stackLevelItem.getStateHashCode();
			if (foundhashCode == hashCode) {
				
				foundStackLevel = stackLevelItem;
				break;
			}
		}
		
		if (foundStackLevel == null) {
			currentStackLevel = null;
			return null;
		}
		
		currentStackLevel = foundStackLevel;
		return currentStackLevel;
	}
	
	/**
	 * Get current stack level.
	 * @return
	 */
	public XdebugStackLevel getCurrentStackLevel() {
		
		return currentStackLevel;
	}
	
	/**
	 * Get Xdebug process ID.
	 */
	@Override
	public String toString() {
		
		String text = threadName + " #" + String.valueOf(threadId);
		return text;
	}
}