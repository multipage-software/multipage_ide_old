/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 21-07-2017
 *
 */
package org.multipage.gui;

import java.util.LinkedList;

/**
 * Signal groups.
 * @author user
 *
 */
public enum SignalGroup implements ApplicationEvent {
	
	/**
	 * Update groups.
	 */
	UPDATE_AREAS(
			UpdateSignal.updateAreasModel,
			UpdateSignal.updateAreasDiagram,
			UpdateSignal.updateAreasTreeEditor,
			UpdateSignal.updateAreasTraceFrame,
			UpdateSignal.updateAreasProperties
			),
	
	UPDATE_DIAGRAM(
			UpdateSignal.updateAreasDiagram
			),	
	
	UPDATE_PROPERTIES(
			UpdateSignal.updateAreasProperties
			),
	
	UPDATE_SLOT_VALUE(
			UpdateSignal.updateAreaSlotValue
			),

	UPDATE_TREE(
			UpdateSignal.updateAreasTreeEditor,
			UpdateSignal.updateAreasTraceFrame
			),
	
	UPDATE_MONITOR(
			UpdateSignal.updateMonitorPanel
			),
	
	/**
	 * Group that contains all update signals.
	 */
	UPDATE_ALL(
			UPDATE_AREAS,
			UPDATE_PROPERTIES,
			UPDATE_SLOT_VALUE,
			UPDATE_DIAGRAM,
			UPDATE_TREE,
			UPDATE_MONITOR			
			);
	
	/**
	 * Signals.
	 */
	public LinkedList<Signal> signals = null;
	
	/**
	 * Priority of the signal.
	 */
	public int priority = ApplicationEvents.MIDDLE_PRIORITY;
	
	/**
	 * Constructor.
	 */
	SignalGroup() {
		
	}
	
	/**
	 * Constructor.
	 * @param signals - list of group signals
	 */
	SignalGroup(Signal ...signals) {
		
		if (signals.length == 0) {
			return;
		}
		
		for (Signal signal : signals) {
			addSignal(signal);
		}
	}
	
	/**
	 * Helper function to add a signals to the group.
	 * @param signals
	 * @return
	 */
	public static Signal [] create(Signal ...signals) {
		
		return signals;
	}
	
	/**
	 * Constructor.
	 * @param groups - list of groups with signals that will be added to this group
	 */
	SignalGroup(SignalGroup ...groups) {
		
		if (groups.length == 0) {
			return;
		}
		
		for (SignalGroup group : groups) {
			for (Signal signal : group.signals)
			addSignal(signal);
		}
	}

	/**
	 * Add signal to group.
	 * @param signal
	 */
	void addSignal(Signal signal) {
		
		if (signals == null) {
			signals = new LinkedList<Signal>();
		}
		
		if (!this.signals.contains(signal)) {
			signals.add(signal);
		}
	}
	
	/**
	 * Returns true if the incoming message is this group.
	 */
	@Override
	public boolean matches(Message incomingMessage) {
		
		return incomingMessage.signal.isInGroup(this);
	}
}