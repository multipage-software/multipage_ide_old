/**
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 19-02-2021
 * 
 */
package org.multipage.generator;

import java.util.function.Consumer;

/**
 * Event handle.
 */
class EventHandle {
	
	/**
	 * An action is a lambda function. It consumes the message if it is not coalesced in a time span.
	 */
	public Consumer<Message> action;
	
	/**
	 * Time span in milliseconds for coalescing the same messages. They do not trigger the action.
	 */
	public Long coalesceTimeSpanMs;
	
	/**
	 * Reflection of the signal receiver.
	 */
	public StackTraceElement reflection;
	
	/**
	 *  Identifier of this event handle (only for debugging purposes; this property can be removed from
	 *  the class code along with the debugging code).
	 */
	public String identifier;
	
	/**
	 * A priority of the event handle. For debugging purposes.
	 */
	public Integer priority;
	
	/**
	 * An event handle key. For debugging purposes.
	 */
	public Object key;
	
	/**
	 * Constructor.
	 * @param action
	 * @param eventPriority 
	 * @param timeSpanMs
	 * @param reflection
	 * @param identifier
	 */
	EventHandle(Consumer<Message> action, Long timeSpanMs, StackTraceElement reflection, String identifier) {
		
		this.action = action;
		this.coalesceTimeSpanMs = timeSpanMs;
		this.reflection = reflection;
		this.identifier = identifier;
	}
	
	/**
	 * Trim the identifier string.
	 * @return
	 */
	String identifier() {
		return identifier != null ? identifier : "";
	}
}