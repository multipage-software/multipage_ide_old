/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 16-05-2023
 *
 */
package org.maclan.server;

import java.util.List;
import java.util.Map;

/**
 * Xdebug commands that sends the IDE.
 * @author vakol
 *
 */
public class XdebugCommand {
	
	/**
	 * Xdebug command names.
	 */
	public static final String FEATURE_GET = "feature_get";

	/**
	 * Command name.
	 */
	public String name = null;
	
	/**
	 * Arguments for the above command.
	 */
	public String[][] arguments = null;
	
	/**
	 * Data attached to the command.
	 */
	public byte [] data = null;
	
	/**
	 * Unique transaction ID.
	 */
	public int transactionId = -1;
	
	/**
	 * Create new Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @return
	 */
	public static XdebugCommand create(String commandName, String[][] arguments) {
		
		XdebugCommand command = new XdebugCommand();
		command.name = commandName;
		command.arguments = arguments;
		command.transactionId = -1;
		return command;
	}
}
