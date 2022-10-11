/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 08-08-2018
 *
 */

package org.maclan.server;

import org.multipage.gui.CallbackNoArg;

/**
 * 
 * @author user
 *
 */
public abstract class DebugListener {
	
	/**
	 * Listener that determines if code should be debugged
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
	 * Activates debugger. The method creates a main thread that communicates with client.
	 */
	protected abstract void activate() throws Exception;
	
	/**
	 * Start debugging
	 */
	public abstract boolean startDebugging();
	
	/**
	 * Stop debugging
	 */
	public abstract void stopDebugging();
	
	/**
	 * Close debugger
	 */
	public abstract void close();
}
