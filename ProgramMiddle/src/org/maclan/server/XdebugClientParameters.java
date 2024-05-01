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
	public String computer = null;
	
	/**
	 * Process ID
	 */
	public Long pid = null;
	
	/**
	 * Thread ID.
	 */
	public Long tid = null;
	
	/**
	 * Thread name.
	 */
	public String threadName = null;
	
	/**
	 * Area ID.
	 */
	public String aid = null;
	
	/**
	 * Area name.
	 */
	public String areaName = null;
	
	/**
	 * Area Server state hash.
	 */
	public String statehash = null;
	
	/**
	 * Returns true value if all parameters are set.
	 * @return
	 */
	public boolean isInitialized() {
		
		boolean initialized = (computer != null && pid != null && tid != null & aid != null & statehash != null);
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
					&& Objects.equals(pid, other.pid)
					&& Objects.equals(tid, other.tid);
	}
	
}