/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Debug listener session object that stores session states.
 * @author vakol
 *
 */
public class DebugListenerSession {

	/**
	 * Server socket reference.
	 */
	protected AsynchronousServerSocketChannel listenerSocket = null;
	
	/**
	 * Remote client socket reference.
	 */
	protected AsynchronousSocketChannel remoteProbeSocket = null;
	
	/**
	 * Connection attachment.
	 */
	protected Void connectionAttachment = null;
	
	/**
	 * Cosntructor.
	 * @param serverSocket
	 * @param remoteClientSocket
	 * @param attachment
	 */
	public DebugListenerSession(AsynchronousServerSocketChannel serverSocket, AsynchronousSocketChannel remoteClientSocket,
			Void attachment) {
		
		// Set members.
		this.listenerSocket = serverSocket;
		this.remoteProbeSocket = remoteClientSocket;
		this.connectionAttachment = attachment;
	}
	
	/**
	 * Get debug listener socket address.
	 */
	public AsynchronousServerSocketChannel getListenerSocket() {
		
		return listenerSocket;
	}
	
	/**
	 * Get connected remote debugger probe address of socket.
	 */
	public AsynchronousSocketChannel getRemoteProbeSocket() {
		
		return remoteProbeSocket;
	}
}
