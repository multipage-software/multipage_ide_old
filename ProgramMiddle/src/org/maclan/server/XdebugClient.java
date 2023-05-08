/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.maclan.server.XdebugClientOld.Callback;
import org.multipage.gui.Utility;
import org.multipage.util.j;

/**
 * Xdebug probe for Area Server (a client connected to the XdebugServer).
 * @author vakol
 *
 */
public class XdebugClient {
	
	/**
	 * Default client connection timeout in milliseconds.
	 */
	private static final int DEFAUL_CONNECTION_TIMEOUT_MS = 3000;

	/**
	 * Current listener (server) socket address to which this client connects.
	 */
	private InetSocketAddress serverSocketAddress = null;
	
	/**
	 * Socket channel used by this client.
	 */
	private SocketChannel clientSocketChannel = null;
	
	/**
	 * Create new Xdebug client and connect it to Xdebug listener running on specific host and port.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param areaServerStateLocator
	 * @return
	 */
	public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String areaServerStateLocator)
			throws Exception {
		
		// Delegate the call.
		return connectNewClient(ideHostName, xdebugPort, areaServerStateLocator, DEFAUL_CONNECTION_TIMEOUT_MS);
	}
	
	/**
	 * Create new Xdebug client and connect it to Xdebug listener running on specific host and port.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param areaServerStateLocator
	 * @return
	 */
	public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String areaServerStateLocator, int timeoutMs)
			throws Exception {
		
		// Create new client obejct.
		XdebugClient client = new XdebugClient();
		client.connect(ideHostName, xdebugPort, areaServerStateLocator, timeoutMs);
		return client;
	}

	/**
	 * Connect this client to specific host name and port number.
	 * @param ideHostName - host name on which Xdebug listens
	 * @param xdebugPort - port number of Xdebug listener
	 * @param areaServerStateLocator - information about current Area Server breakpoint
	 * @param timeoutMs - connection timeout in milliseconds
	 */
	private void connect(String ideHostName, int xdebugPort, String areaServerStateLocator, int timeoutMs)
			throws Exception {
		
		try {
			// Start timeout.
			long startTime = System.currentTimeMillis();
			
			// Remember server socket address.
			serverSocketAddress = new InetSocketAddress(ideHostName, xdebugPort);
			
			// Create non-blocking socket channel and connect it.
	        clientSocketChannel = SocketChannel.open();
	        clientSocketChannel.configureBlocking(false);
	
	        // Connect to the server
	        clientSocketChannel.connect(serverSocketAddress);
			
	        while (!clientSocketChannel.finishConnect()) {
	        	
	            // Wait until timeout millicesonds will elapse.
	            long endTimeMs = System.currentTimeMillis();
	            long elapsedTimeMs = endTimeMs - startTime;
	            
	            if (elapsedTimeMs > timeoutMs) {
	                Utility.throwException("org.maclan.server.messageXdebugConnectionTimeoutElapsed", timeoutMs);
	            }
	        }
	        
	        // TODO: <---REMOVE IT
	        j.log("XDEBUG CLIENT CONNECTED TO PORT %d", xdebugPort);
		}
		catch (Exception e) {
			Utility.throwException("org.maclan.server.messageXdebugConnectionError",
								   ideHostName, xdebugPort, e.getLocalizedMessage());
		}
	}

	public boolean checkActive() {
		
		return false;
	}

	public void communicate(Callback callback) {
		
		
	}
}
