/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 06-03-2023
 *
 */
package org.maclan.server;

/**
 * Maclan exception object.
 * @author vakol
 *
 */
public class MaclanException  {
	
	/**
	 * Reference to thrown exception.
	 */
	public Exception exception = null;
	
	/**
	 * Set exception reference.
	 * @param exception
	 */
	public void set(Exception exception) {
		
		this.exception = exception;
	}
	
	/**
	 * Get message text.
	 * @return
	 */
	public String getMessageText() {
		
		String messageText = exception.getLocalizedMessage();
		return messageText;
	}
}
