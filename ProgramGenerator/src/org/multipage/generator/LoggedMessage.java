/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 24-03-2021
 *
 */
package org.multipage.generator;

import java.sql.Timestamp;

/**
 * @author vakol
 *
 */
public class LoggedMessage {
	
	/**
	 * Message text
	 */
	private String messageText = "unknown";
	
	/**
	 * Time stamp.
	 */
	private long timeStamp = -1;
	
	/**
	 * Logged message.
	 * @param message
	 */
	public LoggedMessage(String message) {
		
		this.messageText = message;
		this.timeStamp = System.currentTimeMillis();
	}
	
	/**
	 * Get message text.
	 * @return
	 */
	public String getText() {
		
		Timestamp timeStamp = new Timestamp(this.timeStamp);
		return String.format("[%s] %s", timeStamp.toString(), this.messageText);
	}
}
