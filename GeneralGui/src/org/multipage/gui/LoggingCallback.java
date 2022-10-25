/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 25-10-2022
 *
 */
package org.multipage.gui;

import java.util.LinkedList;

/**
 * Logging callback methods.
 * @author vakol
 *
 */
public class LoggingCallback {
	
	/**
	 * Log message.
	 * @param message
	 */
	public static void log(Message message) {
		
		// Override this method.
	}
	
	/**
	 * Log message.
	 * @param message
	 * @param eventHandle
	 * @param executionTime
	 */
	public static void log(Message message, EventHandle eventHandle, long executionTime) {
		
		// Override this method.
	}
	
	/**
	 * Log queue snapshot.
	 * @param queueSnapshot
	 * @param now
	 */
	public static void addMessageQueueSnapshot(LinkedList<Message> queueSnapshot, long now) {
		
		// Override this method.
	}
	
	/**
	 * Break logging.
	 * @param signal
	 */
	public static void breakPoint(Signal signal) {
		
		// Override this method.
	}
}
