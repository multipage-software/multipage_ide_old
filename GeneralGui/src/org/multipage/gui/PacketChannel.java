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

import org.multipage.util.j;

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
	 * Client socket channel.
	 */
	private AsynchronousSocketChannel clientSocketChannel = null;
	
	/**
	 * Connected server socket address.
	 */
	private InetSocketAddress serverSocketAddress = null;
	
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
	 * Open server socket.
	 * @param hostname
	 * @param port
	 * @throws Exception 
	 */
	public void listen(String hostname, int port)
			throws Exception {
		
		// Open listening socket.
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        socketAddress = new InetSocketAddress(hostname, port);
        serverSocketChannel.bind(socketAddress);
        
        // Set event that accepts incoming connections from input socket.
        serverSocketChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, PacketChannel>() {
        
        	// An event that is invoked when socket connection is completed.
        	@Override
			public void completed(AsynchronousSocketChannel client, PacketChannel packetChannel) {
        		
        		try {
        			// Remember connected socket channel.
	        		clientSocketChannel = client;
	
	    			// Invoke callback function.
	    			PacketSession packetSession = onStartListening(client);
	    			if (packetSession == null) {
	    				return;
	    			}
	
	        		// Read incoming packets until the connection is closed or interrupted.
					packetSession.startReadingPackets(client);
        		}
        		catch (Exception e) {
        			onException(e);
        		}
        	}
        	
			// If the connection failed...
            public void failed(Throwable e, PacketChannel packetChannel) {
            	onException(e);
            }
        });	
	}
	
	/**
	 * Callback function called after accepting new connection to the server. 
	 * @param client
	 * @return
	 */
	protected PacketSession onStartListening(AsynchronousSocketChannel client)
			throws Exception {
		
		// You can override this method.
		return null;
	}
	
	/**
	 * Connect client socket.
	 * @param hostname
	 * @param port
	 * @throws Exception 
	 */
	public void connect(String hostname, int port)
			throws Exception {
		
		try {
			// Remember server socket address.
			serverSocketAddress = new InetSocketAddress(hostname, port);
			clientSocketChannel = AsynchronousSocketChannel.open();
			
			// TODO: <---DEBUG Display connecting thread name.
			j.log("Connecting thread: %s Client socket: %d", Thread.currentThread().getName(), clientSocketChannel.hashCode());
			
			// Create non-blocking socket channel and connect it.
	        clientSocketChannel.connect(serverSocketAddress, this, new CompletionHandler<Void, PacketChannel>() {
	        	
	        	// On successful connection.
				@Override
				public void completed(Void result, PacketChannel packetChannel) {
					
					try {
						// Callback function.
		    			PacketSession packetSession = onConnected(packetChannel);
						
		        		// Read incoming packets until the connection is closed or interrupted.
						packetSession.startReadingPackets(clientSocketChannel);
					}
		    		catch (Exception e) {
		    			onException(e);
		    		}
				}
				
				// On failed connection.
				@Override
				public void failed(Throwable e, PacketChannel packetChannel) {
					onException(e);
				}
	        });
		}
		catch (Exception e) {
			onThrownException(e);
		}
	}

	/**
	 * Callback function called after connection to server is established.
	 * @param packetChannel
	 * @return
	 */
	protected PacketSession onConnected(PacketChannel packetChannel) 
			throws Exception {

		// Override this method. 
		return null;
	}
	
	/**
	 * Get socket address.
	 * @return
	 */
	public InetSocketAddress getSocketAddress() {
		
		return socketAddress;
	}
	
	/**
	 * Get server socket channel.
	 * @return
	 */
	public AsynchronousServerSocketChannel getServerSocket() {
		
		return serverSocketChannel;
	}
	
	/**
	 * Get client socket channel.
	 * @return
	 */
	public AsynchronousSocketChannel getClientSocketChannel() {
		
		return clientSocketChannel;
	}
	
	/**
	 * Fired on packet exception.
	 * @param e
	 */
	protected void onThrownException(Throwable e)
			throws Exception {
		
		// Override this method.
		onException(e);
		Exception exception = new Exception(e);
		throw exception;
	}
	
	/**
	 * Fired on packet exception.
	 * @param e
	 */
	protected void onException(Throwable e) {
		
		// Override this method.
		e.printStackTrace();
	}
}
