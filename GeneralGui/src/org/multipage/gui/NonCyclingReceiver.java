/*
 * Copyright 2010-2023 (C) multipage-software.org
 * 
 * Created on : 17-06-2020
 *
 */
package org.multipage.gui;

import java.util.LinkedList;

/**
 * Interface for all modules that must avoid infinite message cycles.
 * @author user
 *
 */
public interface NonCyclingReceiver {
		
	/**
	 * Get list of previous messages.
	 */
	public LinkedList<Message> getPreviousMessages();
}

