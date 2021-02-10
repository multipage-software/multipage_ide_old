/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 08-08-2018
 *
 */

package com.maclan.server;

import org.multipage.gui.CallbackNoArg;

/**
 * 
 * @author user
 *
 */
public abstract class DebugClient {
	
	/**
	 * Listener that determines if PHP should be debugged
	 */
	protected static CallbackNoArg enableListener = null;
	
	/**
	 * Sets listener that determines if debugger is enabled
	 * @param listener
	 */
	public static void setDebugPhpListener(CallbackNoArg listener) {
		
		enableListener = listener;
	}
	
	/**
	 * Set open debug viewer listener
	 */
	public abstract void setDebugViewerListener(DebugViewerCallback callback);
	
	/**
	 * Activates debugger. The method creates a main thread that communicates with browser
	 */
	protected abstract void activate() throws Exception;
	
	/**
	 * Start debugging
	 */
	abstract boolean startDebugging();
	
	/**
	 * Stop debugging
	 */
	abstract void stopDebugging();
	
	/**
	 * Close debugger
	 */
	abstract void close();
}
