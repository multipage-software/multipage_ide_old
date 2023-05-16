/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import org.multipage.util.Obj;

/**
 * Debug listener session object that stores session states.
 * @author vakol
 *
 */
public class DebugListenerSession {
	
	/**
	 * Maximum session ID.
	 */
	private static final int MAXIMUM_SESSION_ID = 1024;
	
	/**
	 * Current session ID.
	 */
	private static Obj<Integer> generatedSessionId = new Obj<Integer>(0);
	
	/**
	 * Get session ID.
	 */
	protected static int generateNewSessionId() {
		
		synchronized (generatedSessionId) {	
			if (generatedSessionId.ref < MAXIMUM_SESSION_ID) {
				generatedSessionId.ref++;
			}
			else {
				generatedSessionId.ref = 1;
			}
			return generatedSessionId.ref;
		}
	}
	
	/**
	 * Session ID.
	 */
	public int sessionId = -1;

	/**
	 * Client socket address.
	 */
	public InetSocketAddress clientSocket = null;
	
	/**
	 * Socket servver reference.
	 */
	public AsynchronousServerSocketChannel server = null;
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @throws Exception 
	 */
	public DebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client)
			throws Exception {
		
		// Set members.
		this.sessionId = generateNewSessionId();
		this.clientSocket = (InetSocketAddress) client.getRemoteAddress();
		this.server = server;
	}
	
	/**
	 * Get session ID.
	 * @return
	 */
	public int getSessionId() {
		
		if (sessionId < 1 && sessionId >= MAXIMUM_SESSION_ID) {
			sessionId = generateNewSessionId();
		}
		return sessionId; 
	}
	
	/**
	 * Get debug client (the probe) socket address.
	 */
	public InetSocketAddress getClientSocket() {
		
		return clientSocket;
	}

	/**
	 * Get process ID.
	 * @return
	 */
	public String getPid() {
		
		return "";
	}
}
