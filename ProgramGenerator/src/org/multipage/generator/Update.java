/*
 * Copyright 2020 (C) multipage-software.org
 * 
 * Created on : 17-06-2020
 *
 */
package org.multipage.generator;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

import org.multipage.generator.LoggingDialog.LoggedMessage;
import org.multipage.gui.Utility;
import org.multipage.util.j;

/**
 * 
 * @author user
 *
 */
public interface Update {
	
	/**
	 * Inner class with update state.
	 */
	class State {
		
		/**
		 * Time stamp of initial update event.
		 */
		private Timestamp initialTimestamp = null;
		
		/**
		 * Set current time span.
		 */
		public void setTimestamp() {
			
			initialTimestamp = Utility.getCurrentTimestamp();
		}
		
		/**
		 * Reset current time stamp.
		 */
		public void clearTimestamp() {
			
			initialTimestamp = null;
		}
		
		/**
		 * Returns true value if current time exceeds the input delay measured
		 * from initial time stamp.
		 * @param delayMs
		 * @return
		 */
		public boolean checkTimestamp(long delayMs) {
			
			// Compare initial time stamp with delay and current time stamp.
			long currentTimeMs = System.currentTimeMillis() + delayMs;
			Timestamp currentTimestamp = new Timestamp(currentTimeMs);
			
			int resultOfCompare = currentTimestamp.compareTo(initialTimestamp);
			boolean isGreater = resultOfCompare > 0;
			return isGreater;
		}
	}
	
	/**
	 * States of the update operation.
	 */
	public State updateState = new State();
	
	/**
	 * Define groups to update.
	 */
	public static final int GROUP_ALL = (int) -1;
	public static final int GROUP_AREAS = 1;
	public static final int GROUP_MONITOR = 2;
	
	/**
	 * Define sequence of update signals.
	 */
	static Object [][] updateSignalsSequence = 
			new Object [][] {
				{ Signal.updateAreasModel, GROUP_AREAS },
				{ Signal.updateAreasDiagram, GROUP_AREAS },
				{ Signal.updateAreasTreeEditor, GROUP_AREAS },
				{ Signal.updateMonitorPanel, GROUP_MONITOR }
			};
	
	/**
	 * An entry point that performs update of IDE components.
	 * @param enabledGroups - groups of components to update by this method
	 * @param eventSource - source of the update event
	 */
	public static void run(int enabledGroups, EventSource eventSource) {
		
		// Transmit all enabled signals.
		for (Object [] sequenceItem : updateSignalsSequence) {
			
			Signal signal = (Signal) sequenceItem[0];
			int signalGroups = (int) sequenceItem[1];
			
			// Check enabled groups.
			if ((enabledGroups & signalGroups) == 0x0) {
				continue;
			}
			
			// TODO: log transmitted signals.
			LoggingDialog.log("TRASMITTED SIGNAL %s (BY USER)", signal);
			
			// Transmit signal.
			ConditionalEvents.transmit(eventSource, signal);
		}
	}
}
