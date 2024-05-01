/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 04-04-2024
 *
 */
package org.multipage.gui;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Packet channel that uses sockets, server side.
 * @author vakol
 */
public class PacketChannel {
	
	/**
	 * Socket address object.
	 */
	private InetSocketAddress socketAddress = null;
	
	/**
	 * Input socket object.
	 */
	private AsynchronousServerSocketChannel serverSocketChannel = null;
	
	
	/**
	 * Connected server socket address.
	 */
	private InetSocketAddress serverSocketAddress = null;
	
	/**
	 * Client socket channel.
	 */
	public AsynchronousSocketChannel clientSocketChannel = null;
	
	/**
	 * Constructor.
	 */
	public PacketChannel() {
		
	}
	
	/**
	 * Constructor.
	 * @param clientSocketChannel
	 */
	public PacketChannel(AsynchronousSocketChannel clientSocketChannel) {
		
		this.clientSocketChannel = clientSocketChannel;
	}

	/**
	 * Open receiving socket.
	 * @param hostname
	 * @param port
	 * @throws Exception 
	 */
	public void openReceivingSocket(String hostname, int port)
			throws Exception {
		
		// Open input socket.
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        socketAddress = new InetSocketAddress(hostname, port);
        serverSocketChannel.bind(socketAddress);
        
        // Set event that can accept connections from input socket.
        serverSocketChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, PacketChannel>() {
        
        	// Event that is run when the socket connection is completed.
        	@Override
			public void completed(AsynchronousSocketChannel client, PacketChannel packetChannel) {
        		
        		clientSocketChannel = client;

    			// Callback function.
    			PacketSession packetSession = onAccepted(client);
    			if (packetSession == null) {
    				return;
    			}

        		// Read incoming packets until the connection is closed or interrupted.
				packetSession.startReadingPackets(client);
        	}
        	
			// If the connection failed...
            public void failed(Throwable exception, PacketChannel packetChannel) {
    			// Show error message.
    			exception.printStackTrace();
            }
        });	
	}
	
	/**
	 * Callback function called after accepting new connection to the server. 
	 * @param client
	 * @return
	 */
	protected PacketSession onAccepted(AsynchronousSocketChannel client) {
		
		// You can override this method.
		return null;
	}
	
	/**
	 * Connect to server socket.
	 * @param hostname
	 * @param port
	 * @throws Exception 
	 */
	public void connectToSocket(String hostname, int port)
			throws Exception {
		
		try {
			// Remember server socket address.
			serverSocketAddress = new InetSocketAddress(hostname, port);
			clientSocketChannel = AsynchronousSocketChannel.open();
			
			// Create non-blocking socket channel and connect it.
	        clientSocketChannel.connect(serverSocketAddress, this, new CompletionHandler<Void, PacketChannel>() {

				@Override
				public void completed(Void result, PacketChannel packetChannel) {
					
	    			// Callback function.
	    			PacketSession packetSession = onConnected(packetChannel);
					
	        		// Read incoming packets until the connection is closed or interrupted.
					packetSession.startReadingPackets(clientSocketChannel);
				}

				@Override
				public void failed(Throwable exc, PacketChannel packetChannel) {
					// TODO Auto-generated method stub
					
				}
	        });
		}
		catch (Exception e) {

		}
	}

	/**
	 * Callback function called after established connection to server.
	 * @param packetChannel
	 * @return
	 */
	protected PacketSession onConnected(PacketChannel packetChannel) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get server socket channel.
	 * @return
	 */
	public AsynchronousServerSocketChannel getServerSocket() {
		
		return serverSocketChannel;
	}
	
	/**
	 * Get socket address.
	 * @return
	 */
	public InetSocketAddress getSocketAddress() {
		
		return socketAddress;
	}
}
