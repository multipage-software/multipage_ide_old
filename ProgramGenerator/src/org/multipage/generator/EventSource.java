/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 23-02-2021
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Window;
import java.util.Objects;

import org.multipage.gui.Utility;

/**
 * Initiator of conditional event.
 * @author vakol
 *
 */
public class EventSource {
	
	/**
	 * Enumeration of sources.
	 */
	// Unknown event source.
	public static final EventSource UNKNOWN = new EventSource("UNKNOWN");
	// The Generator Main Frame source.
	public static final EventSource GENERATOR_MAIN_FRAME = new EventSource("GENERATOR_MAIN_FRAME");
	// The Area Editor source.
	public static final EventSource AREA_EDITOR = new EventSource("AREA_EDITOR");
	// The Local Pop Up Menu source.
	public static final EventSource LOCAL_POPUP_MENU = new EventSource("LOCAL_POPUP_MENU");
	// The Area Trace source.
	public static final EventSource AREA_TRACE = new EventSource("AREA_TRACE");
	
	/**
	 * Name of the event source.
	 */
	private String name = null;
		
	/**
	 * Flag that determines user direct event.
	 */
	private boolean userInitiated = false;
		
	/**
	 * Reference to source object .
	 */
	private Object sourceObject = null;
	
	/**
	 * Source message.
	 */
	private Message sourceMessage = null;
		
	/**
	 * Reference to previous event source that causes the event.
	 */
	private EventSource previousEventSource = null;
	
	/**
	 * Basic event source which is set to one of the above listed basic constant sources.
	 */
	private EventSource basicEventSource = null;
	
	/**
	 * Constructor.
	 */
	public EventSource() {
		
	}
	
	/**
	 * Constructor.
	 * @param name
	 */
	public EventSource(String name) {
		
		this.name = name;
	}
	
	/**
	 * Get description of the event source.
	 * @return
	 */
	public String getDescription() {
		
		String className = getClass().getSimpleName();
		String sourceName = name != null ? name : "unknown";
		String initiator = userInitiated ? "user" : "machine";
		
		String description = String.format("%s %s initiated by %s", className, sourceName, initiator);
		return description;
	}

	/**
	 * Clone new event source for user action.
	 * @param initiatorObject
	 * @param initiatorMessage
	 * @return
	 */
	public EventSource userAction(Object initiatorObject, Message initiatorMessage) {
		
		// Clone the event source.
		EventSource clonedEventSource = clone(initiatorObject, true, initiatorMessage);
		
		// Return cloned event source.
		return clonedEventSource;
	}
	
	/**
	 * Clone new event source for machine action.
	 * @param initiatorObject
	 * @param sourceMessage
	 * @return
	 */
	public EventSource machineAction(Object initiatorObject, Message intiatorMessage) {
		
		// Clone the event source.
		EventSource clonedEventSource = clone(initiatorObject, false, sourceMessage);
		
		// Return cloned event source.
		return clonedEventSource;
	}
	
	/**
	 * Clone event source.
	 * @param initiatorObject
	 * @param userInitiated
	 * @param intiatorMessage
	 * @return
	 */
	public EventSource clone(Object initiatorObject, boolean userInitiated, Message intiatorMessage) {
		
		// Create new the event source.
		EventSource clonedEventSource = new EventSource();
		
		// Set name.
		clonedEventSource.name = name;
				
		// Set user initiation flag.
		clonedEventSource.userInitiated = userInitiated;
		
		// Preserve basic source reference.
		clonedEventSource.basicEventSource = basicEventSource;
		
		// Set initiator object.
		clonedEventSource.sourceObject = initiatorObject;
		
		// Set initiating message.
		clonedEventSource.sourceMessage = intiatorMessage;
		
		// Return cloned event source.
		return clonedEventSource;
	}
	
	/**
	 * Check if input object equals to this event source.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventSource other = (EventSource) obj;
		return Objects.equals(basicEventSource, other.basicEventSource)
				&& Objects.equals(sourceMessage, other.sourceMessage)
				&& Objects.equals(sourceObject, other.sourceObject) && userInitiated == other.userInitiated;
	}

	/**
	 * Get initiator message.
	 * @return
	 */
	public Message getInitiatorMessage() {
		
		return sourceMessage;
	}
}
