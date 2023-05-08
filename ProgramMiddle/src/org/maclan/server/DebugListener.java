/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 08-08-2018
 *
 */

package org.maclan.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.multipage.gui.CallbackNoArg;

/**
 * 
 * @author user
 *
 */
public abstract class DebugListener {
	
	/**
	 * List of current sessions. The list is synchronize and can be edited with concurrent threads.
	 */
	protected List<DebugListenerSession> sessions = Collections.synchronizedList(new LinkedList<DebugListenerSession>());
	
	/**
	 * Listener that determines if code should be debugged
	 */
	protected static CallbackNoArg enableListener = null;
	
    /**
     * Invoked when new connection to debug server has been accepted.
     */
    public Consumer<XdebugListenerSession> acceptConnectionLambda = null;
	
    /**
     * Invoked when input packet has been received by the debug server.
     */
    public Consumer<XdebugInputPacket> inputPacketLambda = null;
    
	
	/**
	 * Sets listener that determines if debugger is enabled
	 * @param listener
	 */
	public static void setDebugPhpListener(CallbackNoArg listener) {
		
		enableListener = listener;
	}
	
	/**
	 * Set open debug viewer listeners.
	 */
	public abstract void setDebugViewerListeners(DebugViewerCallback callback);
	
	/**
	 * Activates debugger. The method creates a main thread that communicates with client.
	 */
	protected abstract void activate() throws Exception;
	
	/**
	 * Start debugging
	 */
	public abstract boolean startDebugging();
	
	/**
	 * Get current debug listener sessions.
	 */
	public List<DebugListenerSession> getSessions() {
		
		return sessions;
	}
	
	/**
	 * Stop debugging
	 */
	public abstract void stopDebugging();
	
	/**
	 * Close debugger
	 */
	public abstract void close();
}
