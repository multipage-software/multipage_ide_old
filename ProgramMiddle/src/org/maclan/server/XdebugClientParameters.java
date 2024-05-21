/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 10-06-2023
 *
 */
package org.maclan.server;

import java.util.Objects;

/**
 * Parameters of debugger client.
 */
public class XdebugClientParameters {
	
	/**
	 * Computer name.
	 */
	private String computer = null;
	
	/**
	 * Process ID
	 */
	private Long processId = null;
	
	/**
	 * Process name.
	 */
	private String processName = null;
	
	/**
	 * Thread ID.
	 */
	private Long threadId = null;
	
	/**
	 * Thread name.
	 */
	private String threadName = null;
	
	/**
	 * Area ID.
	 */
	private Long areaId = null;
	
	/**
	 * Area name.
	 */
	private String areaName = null;
	
	/**
	 * Area Server state hash.
	 */
	private Integer statehash = null;
	
	/**
	 * Returns true value if all parameters are set.
	 * @return
	 */
	public boolean isInitialized() {
		
		boolean initialized = (computer != null && processId != null && threadId != null & areaId != null & statehash != null);
		return initialized;
	}

	/**
	 * Returns true value if all parameters of input object are equal to parameters of current object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XdebugClientParameters other = (XdebugClientParameters) obj;
		return Objects.equals(computer, other.computer)
					&& Objects.equals(processId, other.processId)
					&& Objects.equals(threadId, other.threadId);
	}
	
	/**
	 * Set computer name.
	 * @param computer
	 */
	public void setComputer(String computer) {
		
		this.computer = computer;
	}
	
	/**
	 * Get computer name.
	 * @return
	 */
	public String getComputer() {
		
		return computer;
	}
	
	/**
	 * Set process ID.
	 * @param groupLong
	 */
	public void setProcessId(Long processId) {
		
		this.processId = processId;
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public Long getProcessId() {
		
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
	 * Set thread ID.
	 * @param threadId
	 */
	public void setThreadId(Long threadId) {
		
		this.threadId = threadId;
	}
	
	/**
	 * Get thread ID.
	 * @return
	 */
	public Long getThreadId() {
		
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
	 * Set area ID.
	 * @param areaId
	 */
	public void setAreaId(Long areaId) {
		
		this.areaId = areaId;
	}
	
	/**
	 * Get area ID.
	 * @return
	 */
	public Long getAreaId() {
		
		return areaId;
	}
	
	/**
	 * Set area name.
	 * @param areaName
	 */
	public void setAreaName(String areaName) {

		this.areaName = areaName;
	}
	
	/**
	 * Get area name.
	 * @return
	 */
	public String getAreaName() {
		
		return areaName;
	}

	/**
	 * Set state hash code.
	 * @param stateHash
	 */
	public void setStatehash(Integer stateHash) {
		
		this.statehash = stateHash;
	}
	
	/**
	 * Get state hash code.
	 * @return
	 */
	public Integer getStatehash() {
		
		return statehash;
	}
}