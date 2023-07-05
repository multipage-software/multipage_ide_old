/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.awt.Component;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.RepeatedTask;
import org.multipage.util.Resources;
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
	 * Listener idle timeout. 
	 */
	private static final long XDEBUG_RESULT_IDLE_MS = 200;
	
	/**
	 * Singleton object.
	 */
    private static XdebugListener instance = null;
    
	/**
	 * Index of the server connection.
	 */
	private static int serverConnectionIndex = 0;
	
	/**
	 * Reference to debug viewer component.
	 */
	public Component debugViewerComponent = null;
	
	/**
	 * Socket reader lock.
	 */
	protected Lock socketReaderLock = new Lock();
	
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
		
		// Open asynchornous server socket.
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        InetSocketAddress socketAddress = new InetSocketAddress("localhost", port);
        server.bind(socketAddress);
        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            
        	// When connection is completed...
        	@Override
			public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel server) {
        		
        		// TODO: <---REMOVE IT
        		j.log("");
        		j.log("----------------------------------------------------------------");
				try {
					// Create and remember new session object.
					XdebugListenerSession session = new XdebugListenerSession(server, client);
					XdebugListener.this.sessions.add(session);
					
					// TODO: <---DEBUG Create session object.
					j.log("CREATE SESSION %x", session.hashCode());
					
					// Call the "accept session" lambda.
					onAcceptSession(session);
					
					String taskName = String.format("serverConnectionIndex%d", ++serverConnectionIndex);
					
					Obj<Integer> readingIndex = new Obj<Integer>(0);
					
					// Enter non blocking loop.
					RepeatedTask.loopNonBlocking(taskName, -1, XDEBUG_RESULT_IDLE_MS, (exit, exception) -> {
						
						// Clear the socket reader lock.
						Lock.clear(socketReaderLock);

						// TODO: <---DEBUG Count number of reads.
						j.log("- %d.%d thread %d - INPUT BUFFER: %d bytes of free space remains", serverConnectionIndex, ++readingIndex.ref, Thread.currentThread().getId(), session.inputBuffer.remaining());
						
						// Read Xdebug data packet bytes.
	                    client.read(session.inputBuffer, session, new CompletionHandler<Integer, XdebugListenerSession>() {
	                        public void completed(Integer result, XdebugListenerSession session) {
	                        	
	                        	// If nothing read, exit the method.
	                        	if (result <= -1) {
	                        		return;
	                        	}
	                        	
								try {
	                        		// Pull received bytes from the input buffer and put them into the packet buffer.
	                        		while (session.readInputBuffer()) {
	                        			
			                            // Pull incomming Xdebug response packet from the session input buffer.
			                            XdebugResponse inputPacket = session.readPacket();
			                            
			                    		// Herein prepare input buffer for next write operation.
			                            Utility.reuseInputBuffer(session.inputBuffer);
			                            
			                            // Process OTHER incomming packet.
			                            session.processXdebugPacket(inputPacket);
			                            
			                            // Call input packet lambda.
			                            onInputPacket(session, inputPacket);
		                        	}
	                        		
	                        		// Again prepare input buffer for next write operation.
	                        		Utility.reuseInputBuffer(session.inputBuffer);
	                        	}
	                        	catch (Exception e) {
	                        		exception.ref = e;
	                        	}
								
	                        	// Notify the listener lock.
	                        	Lock.notify(socketReaderLock);
	                        }
							public void failed(Throwable e, XdebugListenerSession session) {
	                            // Handle the failure.
								exception.ref = new Exception(e);
	                        }
	                    });
	                    
	                    // Wait for the input bytes to be read from the socket channel.
	                    boolean timeoutEllapsed = Lock.waitFor(socketReaderLock, XDEBUG_RESULT_IDLE_MS);
	                    
	                    // TODO: <---DEBUG Timeout ellapsed.
	                    if (timeoutEllapsed) {
	                    	j.log("TIMEOUT %dms ELLAPSED", XDEBUG_RESULT_IDLE_MS);
	                    }
	                    return exit;
					});
				}
				catch (Exception e) {
					e.printStackTrace();
				}
            }
			
			// If the connection failed...
            public void failed(Throwable exception, AsynchronousServerSocketChannel server) {
                // handle the failure
            }
        });
	}
	
	/**
	 * On accept session.
	 * @param server
	 * @param client
	 * @param attachment
	 */
	protected void onAcceptSession(XdebugListenerSession listenerSession) {
		
		if (acceptSessionLambda != null) {
			acceptSessionLambda.accept(listenerSession);
		}
	}
	
	/**
	 * On input packet.
	 * @param session 
	 * @param inputPacket
	 */
    private void onInputPacket(XdebugListenerSession session, XdebugResponse inputPacket) {
		
		if (inputPacketLambda != null) {
			inputPacketLambda.accept(session, inputPacket);
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
