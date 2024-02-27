/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 02-11-2023
 *
 */
package org.multipage.gui;

import java.util.HashSet;

/**
 * Signal definitions for updating modules.
 * @author vakol
 *
 */
public class UpdateSignal extends Signal {
	
	/**
	 * Update signals.
	 */
	public static Signal updateAreasModel = new Signal();
	
	public static Signal updateAreasDiagram = new Signal(
			params(
				HashSet.class,	// Set of selected area IDs.
				HashSet.class	// Set of selected slot IDs.
			));
	
	public static Signal updateAreasTreeEditor = new Signal();
	
	public static Signal updateAreasTraceFrame = new Signal();
	
	public static Signal updateMonitorPanel = new Signal();
	
	public static Signal updateAreasProperties = new Signal();
	
	public static Signal updateAreaSlotValue = new Signal(
			params(
				Long.class // Updated slot ID.
			));
	
	/**
	 * Static constructor.
	 */
	static {
		// Describe signals.
		reflectSignals(UpdateSignal.class);
	}
	
	/**
	 * Check if an input object equals this signal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof UpdateSignal) {
			
			Signal signal = (UpdateSignal) obj;
			boolean isSame = this.name.equals(signal.name);
			
			return isSame;
		}
		return false;
	}
}
