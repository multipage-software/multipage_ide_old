/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 23-02-2021
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Window;

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
	public static final EventSource UNKNOWN = new EventSource();
	// The Generator Main Frame source.
	public static final EventSource GENERATOR_MAIN_FRAME = new EventSource();
	// The Area Editor source.
	public static final EventSource AREA_EDITOR = new EventSource();
	// The Local Pop Up Menu source.
	public static final EventSource LOCAL_POPUP_MENU = new EventSource();
	// The Area Trace source.
	public static final EventSource AREA_TRACE = new EventSource();
	
	/**
	 * Basic source which is set to one of the above listed basic sources.
	 */
	private EventSource basicSource = null;
	
	/**
	 * Reference to source object .
	 */
	private Object initiatorObject = null;
	
	/**
	 * Flag that determines user direct event.
	 */
	private boolean userInitiated = false;
	
	/**
	 * Initiating message.
	 */
	private Message initiatorMessage = null;
	
	/**
	 * Reference to previous source that causes the event.
	 */
	private EventSource previousSource = null;
	
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
	 * @param initiatorMessage
	 * @return
	 */
	public EventSource machineAction(Object initiatorObject, Message intiatorMessage) {
		
		// Clone the event source.
		EventSource clonedEventSource = clone(initiatorObject, false, initiatorMessage);
		
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
		
		// Clone the event source.
		EventSource clonedEventSource = new EventSource();
		
		// Preserve basic source reference.
		clonedEventSource.basicSource = basicSource;
		
		// Set initiator object.
		clonedEventSource.initiatorObject = initiatorObject;
		
		// Set user initiation flag.
		clonedEventSource.userInitiated = userInitiated;
		
		// Set initiating message.
		clonedEventSource.initiatorMessage = intiatorMessage;
		
		// Return cloned event source.
		return clonedEventSource;
	}
	
	/**
	 * Check if the receiving object is acceptable with the event source.
	 * @param receivingObject
	 * @return
	 */
	public boolean isAcceptableWith(Object receivingObject) {
		
		// TODO: use appropriate rules to avoid messages' infinite loops.
		return false;
	}
}
