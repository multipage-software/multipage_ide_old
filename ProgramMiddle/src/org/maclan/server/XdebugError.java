/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 18-04-2024
 *
 */
package org.maclan.server;

import org.multipage.util.Resources;

/**
 * Xdebug protocol errors.
 * @author vakol
 */
public enum XdebugError {
	
	/**
	 * Xdebug errors.
	 */
	UNIMPLEMENTED_COMMAND(4, "org.maclan.server.messageXdebugUnimplementedCommand"),
	UNKNOWN_ERROR(999, "org.maclan.server.messageXdebugUnknownError");
	
	/**
	 * Error number.
	 */
	private int errorCode = 0;
	
	/**
	 * Error message format.
	 */
	private String messageFormat = null;

	/**
	 * Constructor.
	 * @param errorNumber
	 * @param messageIdentifier
	 */
	XdebugError(int errorNumber, String messageIdentifier) {
		
		this.errorCode  = errorNumber;
		this.messageFormat = Resources.getString(messageIdentifier);
	}

	/**
	 * Get message.
	 * @param parameters - parameters for the message template to replace specifiers %s, %d, %x, etc.
	 * @return
	 */
	public String getMessage(Object ... parameters) {
		
		String message = String.format(messageFormat, parameters);
		return message;
	}
	
	/**
	 * Get error code.
	 * @return
	 */
	public int getErrorCode() {

		return errorCode;
	}
}
