/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 08-08-2018
 *
 */

package org.maclan.server;

import java.awt.Component;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.multipage.gui.CallbackNoArg;
import org.multipage.util.Resources;

/**
 * Debug listener that maintains list of sessions.
 * @author vakol
 *
 */
public abstract class DebugListener {
	
	/**
	 * List of current sessions. The list is synchronized and can be edited with concurrent threads.
	 */
	protected List<DebugListenerSession> sessions = Collections.synchronizedList(new LinkedList<DebugListenerSession>());
	
	/**
	 * Listener that determines if code should be debugged
	 */
	protected static CallbackNoArg enableListener = null;
	
    /**
     * Invoked when a new Xdebug viewer should be opened.
     */
    public Consumer<XdebugListenerSession> openDebugViever = null;
    
	/**
	 * Sets listener that determines if debugger is enabled
	 * @param listener
	 */
	public static void setDebugPhpListener(CallbackNoArg listener) {
		
		enableListener = listener;
	}
	
	/**
	 * Activates debugger. The method creates a main thread that communicates with client.
	 */
	protected abstract void activate() throws Exception;

	/**
	 * Get current debug listener sessions.
	 */
	public List<DebugListenerSession> getSessions() {
		
		return sessions;
	}
	
	/**
	 * Get current debug viewer component.
	 * @param debugViewerComponent
	 */
	public abstract void setViewerComponent(Component debugViewerComponent);
	
	/**
	 * On close debugger.
	 */
	public abstract void onClose();
	
	/**
	 * Fired on exception.
	 * @param messageId
	 * @param parameters
	 * @throws Exception
	 */
	protected void onThrownException(String messageId, Object ... parameters)
			throws Exception {
		
		String messageFormat = Resources.getString(messageId); 
		String message = String.format(messageFormat, parameters);
		Exception e = new Exception(message);
		onThrownException(e);
	}
	
	/**
	 * Fired on exception.
	 * @param e
	 * @throws Exception
	 */
	protected void onThrownException(Exception e)
			throws Exception {
		
		// Override this method.
		onException(e);
		throw e;
	}
	
	/**
	 * Fired on exception.
	 * @param e
	 */
	protected void onException(Exception e) {
		
		// Override this method.
		e.printStackTrace();
	}	
}
