/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Xdebug listener session object that stores session states.
 * @author vakol
 *
 */
public class XdebugListenerSession extends DebugListenerSession {
	
	/**
	 * Cosntructor.
	 * @param serverSocket
	 * @param remoteClientSocket
	 * @param attachment
	 */
	public XdebugListenerSession(AsynchronousServerSocketChannel serverSocket,
			AsynchronousSocketChannel remoteClientSocket, Void attachment) {
		
		// Delegate the call.
		super(serverSocket, remoteClientSocket, attachment);
	}
}
