/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.Properties;

import com.maclan.MiddleResult;

/**
 * @author
 *
 */
public abstract class ProgramHttpServer {
	
	/**
	 * Debugger client
	 */
	protected DebugClient debugger;
	
	/**
	 * Set login properties.
	 * @param loginProperties
	 */
	public abstract void setLogin(Properties loginProperties);

	/**
	 * Create server.
	 * @param login
	 * @param portNumber
	 * @throws Exception
	 */
	public abstract void create(Properties login, int portNumber) throws Exception;

	/**
	 * Stop server.
	 */
	public abstract void stop();
	
	/**
	 * Start debug client
	 */
	public MiddleResult startDebugClient() {
		
		try {
			debugger = XdebugClient.createInstance();
			debugger.activate();
			return MiddleResult.OK;
		}
		catch (Exception e) {
			String message = e.getMessage();
			return MiddleResult.DEBUGGER_NOT_STARTED.format(message);
		}
	}
	
	/**
	 * Stop debugger
	 */
	public MiddleResult stopDebugger() {
		
		if (debugger == null) {
			return MiddleResult.OK;
		}
		
		try {
			debugger.close();
			return MiddleResult.OK;
		}
		catch (Exception e) {
			String message = e.getMessage();
			return MiddleResult.DEBUGGER_NOT_STOPPED.format(message);
		}
	}

	/**
	 * Returns reference to debug client
	 * @return
	 */
	public DebugClient getDebugger() {
		
		return debugger;
	}
}
