/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 09-07-2024
 *
 */
package org.maclan.server;

import java.util.HashMap;
import java.util.LinkedList;

import org.multipage.util.Resources;

/**
 * Xdebug process.
 * @author vakol
 */
public class XdebugProcess {
	
	/**
	 * Process ID.
	 */
	private long processId = -1L;
	
	/**
	 * Process name.
	 */
	private String processName = "";
	
	/**
	 * List of Xdebug threads.
	 */
	private HashMap<Long, XdebugThread> threads = null;
	
	/**
	 * Current thread.
	 */
	private XdebugThread currentThread = null;
	
	/**
	 * Set process ID and name.
	 * @param processId
	 * @param processName
	 */
	public void setProcess(Long processId, String processName) {
		
		this.processId = processId;
		this.processName = processName;
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public long getProcessId() {

		return processId;
	}
	
	/**
	 * Put thread.
	 * @param threadId
	 * @param threadName
	 */
	public void putThread(Long threadId, String threadName) {
		
		if (threads == null ) {
			threads = new HashMap<>();
		}
		
		XdebugThread thread = threads.get(threadId);
		if (thread == null) {
			
			thread = new XdebugThread();
			threads.put(threadId, thread);
		}
		
		thread.setThread(threadId, threadName);
	}
	
	/**
	 * Get list of threads.
	 * @return
	 */
	public HashMap<Long, XdebugThread> getThreads() {
		
		return threads;
	}
	
	/**
	 * Get thread.
	 * @param threadId
	 * @return
	 */
	public XdebugThread getThread(Long threadId) {
		
		if (threadId == null) {
			return null;
		}
		
		XdebugThread thread = threads.get(threadId);
		return thread;
	}
	
	/**
	 * Set current thread.
	 * @param thread
	 * @return
	 */
	public XdebugThread setCurrentThread(XdebugThread thread) {
		
		if (thread == null) {
			
			currentThread = null;
			return null;
		}
		
		long threadId = thread.getThreadId();
		
		boolean found = threads.containsKey(threadId);
		if (!found) {
			
			currentThread = null;
			return null;
		}
		
		// Set current thread.
		currentThread = thread;
		return currentThread;
	}
	
	/**
	 * Get current thread.
	 * @return
	 */
	public XdebugThread getCurrentThread() {
		
		return currentThread;
	}
	
	/**
	 * Get Xdebug process ID.
	 */
	@Override
	public String toString() {
		
		String text = processName + " #" + String.valueOf(processId);
		return text;
	}
}