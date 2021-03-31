/**
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 25-02-2021
 * 
 */
package org.multipage.generator;

import org.multipage.generator.ConditionalEvents.Message;
import org.multipage.gui.Utility;

/**
 * @author vakol
 *
 */
public class ScheduledEvent {
	
	/**
	 * Event handle.
	 */
	EventHandle eventHandle;
	
	/**
	 * An incoming message that will be processed with the above event handle.
	 */
	Message message;
	
	/**
	 * Minimum execution moment.
	 */
	long executionTime;
	
	/**
	 * Constructor.
	 * @param currentTime
	 * @param message
	 * @param eventHandle
	 */
	public ScheduledEvent(long currentTime, Message message, EventHandle eventHandle) {
		
		// Initialize members.
		this.eventHandle = eventHandle;
		this.message = message;
		this.executionTime = currentTime + eventHandle.coalesceTimeSpanMs;
	}
	
	/**
	 * Get text representatioon.
	 */
	@Override
	public String toString() {
		
		return String.format("Event [signal=%s, time=%s]", message.signal.name(), Utility.formatTime(executionTime));
	}
}
