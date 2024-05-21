/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 04-05-2024
 *
 */
package org.maclan.server;

/**
 * Area Server state object used in Xdebug.
 * @author vakol
 */
public class XdebugAreaServerState {
	
	/**
	 * Process and thread information.
	 */
	private long processId = -1;
	private long threadId = -1;
	private String processName = null;
	private String threadName = null;
	
	/**
	 * Source of the code.
	 */
	private Long tagResourceId = null;
	private Long sourceTagId = null;
	
	/**
	 * Set thread ID.
	 * @param threadId
	 */
	public void setThreadId(long threadId) {
		
		this.threadId = threadId;
	}
	
	/**
	 * Get thread ID.
	 * @return
	 */
	public long getThreadId() {
		
		return threadId;
	}
	
	/**
	 * Set thread name.
	 * @param threadName
	 */
	public void setThreadName(String threadName) {
		
		this.threadName = threadName;
	}
	
	/**
	 * Get thread name.
	 * @return
	 */
	public String getThreadName() {
		
		return threadName;
	}
	
	/**
	 * Set process ID.
	 * @param processId
	 */
	public void setProcessId(long processId) {
		
		this.processId = processId;
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public long getProcessId() {
		
		return processId;
	}	
	
	/**
	 * Set process name.
	 * @param processName
	 */
	public void setProcessName(String processName) {
		
		this.processName = processName;
	}
	
	/**
	 * Get process name.
	 * @return
	 */
	public String getProcessName() {
		
		return processName;
	}
	
	/**
	 * Set ID of the resource.
	 * @param tagResourceId
	 */
	public void setTagResourceId(long tagResourceId) {
		
		this.tagResourceId = tagResourceId;
	}
	
	/**
	 * Get ID of the resource.
	 * return
	 */
	public long getTagResourceId() {
		
		return tagResourceId;
	}
	
	/**
	 * Set ID of source tag.
	 * @param sourceTagId
	 */
	public void setSourceTagId(long sourceTagId) {
		
		this.sourceTagId = sourceTagId;
	}
	
	/**
	 * Get ID of source tag.
	 * @return
	 */
	public long getSourceTagId() {
		
		return sourceTagId;
	}
}
