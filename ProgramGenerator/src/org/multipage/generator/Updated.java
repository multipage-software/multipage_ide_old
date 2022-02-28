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

import org.multipage.gui.Utility;
import org.multipage.util.j;

/**
 * 
 * @author user
 *
 */
public interface Updated {
	
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
	 * GUI groups to update.
	 */
	public static final int GUI_GROUP_ALL = (int) -1;
	public static final int GUI_GROUP_AREAS = 1;
	public static final int GUI_GROUP_MONITOR = 2;
	
	/**
	 * Map update groups to appropriate signals.
	 */
	static final HashMap<Integer, Signal []> mapGroupSignals = new HashMap<Integer, Signal []>();
	
	/**
	 * Initialize update group to signals map.
	 */
	public static void initialize() {
		
		mapGroupSignals.put(GUI_GROUP_AREAS,   new Signal [] {
				Signal.updateAreasModel,
				Signal.updateAreasDiagram,
				Signal.updateAreasTreeEditor
		});
		mapGroupSignals.put(GUI_GROUP_MONITOR, new Signal [] {
				Signal.updateMonitorPanel
		});
	}
	
	/**
	 * An entry point that performs update of GUI components.
	 * @param guiGroups - groups of GUI components to update by this method
	 * @param eventSource - source of the update event
	 */
	public static void update(int guiGroups, EventSource eventSource) {
		
		// Initialize empty set of signals.
		HashSet<Signal> foundSignals = new HashSet<Signal>();
		
		// Get signals for each input group.
		for (int bitMask = 0x1; bitMask != 0x0; bitMask <<= 1) {
			
			// Get group from input groups using current bit mask.
			int guiGroup = guiGroups & bitMask;

			// Try to get signals for this group.
			Signal [] groupSignals = mapGroupSignals.get(guiGroup);
			if (groupSignals == null) {
				continue;
			}
			
			// Add found signals to the signal set.
			for (Signal groupSignal : groupSignals) {
				foundSignals.add(groupSignal);
			}
		}
		
		// Transmit all found signals.
		for (Signal signal : foundSignals) {
			ConditionalEvents.transmit(eventSource, signal);
		}
		
		// TODO: debug
		j.log("-----------------------------------------------------");
	}
}
