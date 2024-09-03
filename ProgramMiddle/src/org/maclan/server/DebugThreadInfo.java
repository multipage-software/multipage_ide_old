/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-06-2024
 *
 */
package org.maclan.server;

/**
 * Thread information for debugger.
 * @author vakol
 */
public class DebugThreadInfo {

	/**
	 * Process ID.
	 */
	private long processId = -1;

	/**
	 * Process name.
	 */
	private String processName = null;	

	/**
	 * Thread ID.
	 */
	private long threadId = -1;

	/**
	 * Thread name.
	 */
	private String threadName = null;
	
	/**
	 * Make clone of current thread information.
	 * @return
	 */
	public DebugThreadInfo cloneThreadInfo() {
		
		DebugThreadInfo clonedThreadInfo = new DebugThreadInfo();
		
		clonedThreadInfo.processId = processId;
		clonedThreadInfo.processName = processName;	
		clonedThreadInfo.threadId = threadId;
		clonedThreadInfo.threadName = threadName;
		
		return clonedThreadInfo;
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public long getProcessId() {
		return processId;
	}
	
	/**
	 * Get process ID.
	 * @param processId
	 */
	public void setProcessId(long processId) {
		this.processId = processId;
	}
	
	/**
	 * Get process name.
	 * @return
	 */
	public String getProcessName() {
		return processName;
	}
	
	/**
	 * Set process name.
	 * @param processName
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	/**
	 * Get thread ID.
	 * @return
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * Set thread ID.
	 * @param threadId
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	
	/**
	 * Get thread name.
	 * @return
	 */
	public String getThreadName() {
		return threadName;
	}
	
	/**
	 * Set thread name.
	 * @param threadName
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
}
