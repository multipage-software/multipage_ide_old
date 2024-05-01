/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 23-04-2024
 */
package org.multipage.generator;

/**
 * Thread information diplayed in debug view/
 * @author vakol
 */
public class DebuggerThread {
	
	/**
	 * Thread ID.
	 */
	public String threadId = null;
	
	/**
	 * Thread name.
	 */
	public String threadName = null;

	/**
	 * Constructor.
	 * @param threadId
	 * @param threadName
	 */
	public DebuggerThread(String threadId, String threadName) {
		
		this.threadId = threadId;
		this.threadName = threadName;
	}
}
