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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.multipage.gui.CallbackNoArg;
import org.multipage.gui.PacketChannel;
import org.multipage.gui.PacketSession;
import org.multipage.util.Resources;

/**
 * Xdebug listener is built-in socket server that accepts debugging requests.
 * @author vakol
 *
 */
public class XdebugListener {
	
	/**
	 * Default Xdebug port. It is set to port number 9004 because JVM Xdebug already uses port number 9000.
	 */
	public static final int DEFAULT_XDEBUG_PORT = 9004;
	
	/**
	 * Singleton object.
	 */
    private static XdebugListener instance = null;
	
	/**
	 * Reference to debug viewer component.
	 */
	private Component debugViewerComponent = null;
	
    /**
     * Invoked when a new Xdebug viewer has to be opened.
     */
    public Consumer<XdebugListenerSession> openDebugViever = null;
	
	/**
	 * Packet channel that accepts incomming packets.
	 */
	private PacketChannel packetChannel = null;

	/**
	 * List of current sessions. The list is synchronized and can be edited with concurrent threads.
	 */
	protected List<XdebugListenerSession> sessions = Collections.synchronizedList(new LinkedList<>());
	
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
	 * Get current debug listener sessions.
	 */
	public List<XdebugListenerSession> getSessions() {
		
		return sessions;
	}
	
	/**
	 * Ensure that sessions are opened. Remove the closed sessions.
	 */
	public void ensureLiveSessions() {
		
		LinkedList<XdebugListenerSession> closedSessions = new LinkedList<>();
		
		// Find closed sessions.
		for (XdebugListenerSession session : sessions) {
			
			boolean isOpen = session.isOpen();
			if (!isOpen) {
				closedSessions.addLast(session);
			}
		}
		
		// Remove the closed sessions.
		removeSessions(closedSessions);
	}
	
	/**
	 * Remove input sessions from session list.
	 * @param sessionsToRemove
	 */
	public void removeSessions(List<XdebugListenerSession> sessionsToRemove) {
		
		sessions.removeAll(sessionsToRemove);
	}
	
	/**
	 * Remember the debug viewer component.
	 */
	public void setViewerComponent(Component debugViewerComponent) {
		
		this.debugViewerComponent = debugViewerComponent;
	}
	
	/**
	 * Activate Xdebug listener.
	 */
	protected void activate()
			throws Exception {
		
		// Opens Xdebug soket for the IDE.
		openListenerPort(DEFAULT_XDEBUG_PORT);
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
	public void onClose() {
		
	}
	
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
	
	/**
	 * Set PHP debug listener.
	 * @param callbackNoArg
	 */
	public static void setDebugPhpListener(CallbackNoArg callbackNoArg) {
		// TODO: <---MAKE
		
	}
}
