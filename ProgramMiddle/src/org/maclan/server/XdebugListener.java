/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.awt.Component;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import org.multipage.gui.PacketChannel;
import org.multipage.gui.PacketSession;

/**
 * Xdebug listener is built-in socket server that accepts debugging requests.
 * @author vakol
 *
 */
public class XdebugListener extends DebugListener {
	
	/**
	 * Default Xdebug port. It is set to port number 9004 because JVM Xdebug already uses port number 9000.
	 */
	public static final int DEFAULT_PORT = 9004;
	
	/**
	 * Singleton object.
	 */
    private static XdebugListener instance = null;
	
	/**
	 * Reference to debug viewer component.
	 */
	private Component debugViewerComponent = null;
	
	/**
	 * Packet channel that accepts incomming packets.
	 */
	private PacketChannel packetChannel = null;

	
    /**
     * Get singleton object.
     * @return
     */
    public static synchronized XdebugListener getInstance() {
        if (instance == null) {
            instance = new XdebugListener();
        }
        return instance;
    }
	
	/**
	 * Remember the debug viewer component.
	 */
	@Override
	public void setViewerComponent(Component debugViewerComponent) {
		
		this.debugViewerComponent = debugViewerComponent;
	}
	
	/**
	 * Activate Xdebug listener.
	 */
	@Override
	protected void activate()
			throws Exception {
		
		// Opens Xdebug soket for the IDE.
		openListenerPort(DEFAULT_PORT);
	}
	
	/**
	 * Opens listener port.
	 * @param port
	 */
	private void openListenerPort(int port)
			throws Exception {
		
		packetChannel = new PacketChannel() {
			@Override
			protected PacketSession onStartListening(AsynchronousSocketChannel client) {
				
				PacketSession packetSession = XdebugListener.this.onOpenDebugViewer(client);
				return packetSession;
			}
		};
		
		// Open packet channel.
		packetChannel.listen("localhost", port);
	}
	
	/**
	 * On accepting incomming connection.
	 * @param socketClient
	 * @param packetReader
	 */
	protected PacketSession onOpenDebugViewer(AsynchronousSocketChannel socketClient) {
		
		// Create and remember new session object.
		try {
			
			// Create Xdebug session.
			AsynchronousServerSocketChannel socketServer = packetChannel.getServerSocket();
			XdebugListenerSession xdebugSession = XdebugListenerSession.newSession(socketServer, socketClient, this);
			XdebugListener.this.sessions.add(xdebugSession);
			
			// Call the "accept session" callback.
			onOpenDebugViewer(xdebugSession);
			
			return xdebugSession.getPacketSession();
		}
		catch (Exception e) {
			onException(e);
		}
		return null;
	}

	/**
	 * On open Xdebug viewer.
	 * @param server
	 * @param client
	 * @param attachment
	 */
	protected void onOpenDebugViewer(XdebugListenerSession listenerSession) {
		
		if (openDebugViever != null) {
			openDebugViever.accept(listenerSession);
		}
	}
	
	/**
	 * TODO: <---MAKE On close debugger.
	 */
	@Override
	public void onClose() {
		
	}
}
