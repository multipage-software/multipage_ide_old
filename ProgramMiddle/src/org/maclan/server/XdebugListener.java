/*
 * Copyright 2010-2018 (C) vakol
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
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * Xdebug listener is built-in socket server that accepts debugging requests.
 * @author vakol
 *
 */
public class XdebugListener extends DebugListener {
	
	/**
	 * Default Xdebug port. It is set to port number 9004 because JVM Xdebug already uses the port number 9000.
	 */
	public static final int DEFAULT_PORT = 9004;
	
	/**
	 * Singleton object.
	 */
    private static XdebugListener instance = null;
	
	/**
	 * Reference to debug viewer component.
	 */
	public Component debugViewerComponent = null;
	
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

	@Override
	public void setDebugViewerListeners(DebugViewerCallback callback) {
		// TODO Auto-generated method stub
		
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

	@Override
	public boolean startDebugging() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stopDebugging() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Opens listener port.
	 * @param port
	 */
	private void openListenerPort(int port)
			throws Exception {
		
		packetChannel = new PacketChannel() {
			@Override
			protected PacketSession onAccepted(AsynchronousSocketChannel client) {
				
				PacketSession packetSession = XdebugListener.this.onOpenDebugViewer(client);
				return packetSession;
			}
		};
		
		// Open packet channel.
		packetChannel.openReceivingSocket("localhost", port);
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
			e.printStackTrace();
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
     * Show exception.
     * @param exception
     */
	public void showException(Exception exception) {
		
		String errorMessage = exception.getLocalizedMessage();
		Utility.show(this.debugViewerComponent, errorMessage);
	}
	
	/**
	 * Show message.
	 * @param stringResourceId
	 * @param message
	 */
	public void showMessage(String stringResourceId, String message) {
		
		String templateText = Resources.getString(stringResourceId);
		String messageText = String.format(templateText, message);
		Utility.show(this.debugViewerComponent, messageText);
	}
}
