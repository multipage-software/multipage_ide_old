/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import org.maclan.server.XdebugPacket.UriParameters;

/**
 * Xdebug listener session object that stores session states.
 * @author vakol
 *
 */
public class XdebugListenerSession extends DebugListenerSession {
	
	/**
	 * List of debugged URIs.
	 */
	public String debuggedUri = null;
	
	/**
	 * Xdebug listener.
	 */
	public XdebugListener listener = null;
	
	/**
	 * Client parameters.
	 */
	public UriParameters clientParameters = null;
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @throws Exception 
	 */
	public XdebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client)
			throws Exception {
		
		// Delegate the call.
		super(server, client);
	}
	
	/**
	 * initialize session using input packet.
	 * @param inputPacket
	 * @throws Exception 
	 */
	public void initialize(XdebugPacket inputPacket) 
			throws Exception {
		
		debuggedUri = inputPacket.GetDebuggedUri();
		clientParameters = XdebugPacket.parseDebuggedUri(debuggedUri);
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	@Override
	public String getPid() {
		
		if (clientParameters == null) {
			return "";
		}
		return clientParameters.pid;
	}
}
