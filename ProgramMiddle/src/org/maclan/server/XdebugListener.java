/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.multipage.util.j;

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
	 * Incomming buffer size.
	 */
	private int INCOMING_BUFFER_SIZE = 1024;
	
	/**
	 * Singleton object.
	 */
    private static XdebugListener instance;
     
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
	private void openListenerPort(int port) {
		
		// Open asynchornous server socket.
		try {
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
            InetSocketAddress socketAddress = new InetSocketAddress("localhost", port);
            server.bind(socketAddress);
            server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                
            	// When connection completed...
            	@Override
				public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel server) {
					try {
						// Create and remember new session object.
						XdebugListenerSession session = new XdebugListenerSession(server, client);
						XdebugListener.this.sessions.add(session);
						
						// Call the "accept connection" lambda.
						onAcceptConnection(session);
	                	
	                    // Read incomming Xdebug data packet.
	                    ByteBuffer buffer = ByteBuffer.allocate(INCOMING_BUFFER_SIZE);
	                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
	                        public void completed(Integer result, ByteBuffer buffer) {
	                        	
	                        	try {
		                            buffer.flip();
		                            
		                            XdebugPacket inputPacket = XdebugPacket.readPacket(buffer);
		                            session.initialize(inputPacket);
		                            onInputPacket(session, inputPacket);
		                            
		                            buffer.clear();
		                            // receive more data from the client
		                            client.read(buffer, buffer, this);
	                        	}
	                        	catch (Exception e) {
	                        	}
	                        }
							public void failed(Throwable exc, ByteBuffer buffer) {
	                            // handle the failure
	                        }
	                    });
	                    
	                    // Accept next Xdebug connection.
	                    server.accept(null, this);
					}
					catch (Exception e) {
						
					}
                }
				
				// If the connection failed...
                public void failed(Throwable exception, AsynchronousServerSocketChannel server) {
                    // handle the failure
                }
            });
        }
		catch (Exception e) {
            // handle the exception
        }
		
		// TODO: <---REMOVE IT
		j.log("XDEBUG IS NOW LISTENING ON PORT %d", port);
	}
	
	/**
	 * On accept connection.
	 * @param server
	 * @param client
	 * @param attachment
	 */
	protected void onAcceptConnection(XdebugListenerSession listenerSession) {
		
		if (acceptConnectionLambda != null) {
			acceptConnectionLambda.accept(listenerSession);
		}
	}
	
	/**
	 * On input packet.
	 * @param session 
	 * @param inputPacket
	 */
    private void onInputPacket(XdebugListenerSession session, XdebugPacket inputPacket) {
		
		if (inputPacketLambda != null) {
			inputPacketLambda.accept(session, inputPacket);
		}
	}
}
