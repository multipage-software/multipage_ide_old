/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 25-06-2024
 *
 */
package org.maclan.server;

/**
 * Code source information for debugger.
 * @author vakol
 */
public class DebugSourceInfo {
	
	/**
	 * ID of a resource that supplies source code.
	 */
	private Long sourceResourceId = null;

	/**
	 * ID of a slot that supplies source code.
	 */
	private Long sourceSlotId = null;
	
	/**
	 * Get resource ID.
	 * @return
	 */
	public Long getSourceResourceId() {
		return sourceResourceId;
	}

	/**
	 * Set resource ID.
	 * @param sourceResourceId
	 */
	public void setSourceResourceId(Long sourceResourceId) {
		this.sourceResourceId = sourceResourceId;
	}
	
	/**
	 * Get slot ID.
	 * @return
	 */
	public Long getSourceSlotId() {
		return sourceSlotId;
	}

	/**
	 * Set slot ID.
	 * @param sourceSlotId
	 */
	public void setSourceSlotId(Long sourceSlotId) {
		this.sourceSlotId = sourceSlotId;
	}
}
