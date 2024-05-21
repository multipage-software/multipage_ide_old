/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.multipage.util.Obj;

/**
 * Debug listener session.
 * @author vakol
 *
 */
public class DebugListenerSession {
	
	/**
	 * Maximum session ID.
	 */
	private static final int MAXIMUM_SESSION_ID = 1024;
	
	/**
	 * Last generated session ID.
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
	protected int sessionId = -1;
	
	/**
	 * Client socket reference.
	 */
	protected AsynchronousSocketChannel client = null;
	
	/**
	 * Socket server reference.
	 */
	protected AsynchronousServerSocketChannel server = null;

	/**
	 * Debug listener reference.
	 */
	protected DebugListener listener = null;
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @param debugListener 
	 * @throws Exception 
	 */
	public DebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client,
			DebugListener debugListener)
					throws Exception {
		
		// Set members.
		this.sessionId = generateNewSessionId();
		this.client = client;
		this.server = server;
		this.listener = debugListener;
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
	 * @throws IOException 
	 */
	public InetSocketAddress getClientSocket()
			throws Exception {
		
		InetSocketAddress socketAddress = (InetSocketAddress) client.getRemoteAddress();
		return socketAddress;
	}
	
	/**
	 * Get client socket channel.
	 * @return
	 */
	public AsynchronousSocketChannel getClientSocketChannel() {
		
		return client;
	}
	
	/**
	 * Get server socket channel.
	 * @return
	 */
	public AsynchronousServerSocketChannel getServerSocketChannel() {
		
		return server;
	}

	/**
	 * Get process ID.
	 * @return
	 */
	public long getPid() {
		
		// Overrride this method.
		return -1L;
	}
	
	/**
	 * Get session thread ID.
	 * @return
	 */
	public long getTid() {
		
		// Overrride this method.
		return -1L;
	}
	
	/**
	 * Send bytes to the debugging client (the debugging probe).
	 * @param bytes
	 * @param session
	 * @param completionHandler
	 */
	protected void sendBytes(ByteBuffer bytes, XdebugTransaction transaction, CompletionHandler<Integer, XdebugTransaction> completionHandler)
			throws Exception {
		
		// Write bytes.
		synchronized (client) {
			try {
				client.write(bytes, transaction, completionHandler);
			}
			catch (Exception e) {
				onThrownException(e);
			}
		}
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
