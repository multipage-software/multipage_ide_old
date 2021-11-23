/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 11-12-2021
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.BiFunction;

import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * 
 * @author vakol
 *
 */
public class XdebugClient {
	
	/**
	 * Text encoding for the Xdebug protocol.
	 */
	public static final String protocolEncoding = "UTF-8";

	/**
	 * Size of the input buffer in bytes.
	 */
	private static final int inputBufferSize = 1025;
	
	/**
	 * Socket channel socketSelector.
	 */
	public Selector socketSelector = null;
	
	/**
	 * Socket object reference.
	 */
	public SocketChannel socketChannel = null;
	
	/**
	 * Constructor.
	 * @param ideHostName
	 * @param xdebugPort
	 * @throws Exception
	 */
	public static XdebugClient connect(String ideHostName, int xdebugPort)
			throws Exception {
		
		// Create new Xdebug state.
		XdebugClient state = new XdebugClient();
		
		// Connect socket channel.
		state.socketChannel = SocketChannel.open();
		state.socketChannel.configureBlocking(false);
		boolean connectedFlag = state.socketChannel.connect(new InetSocketAddress(ideHostName, xdebugPort));
		Obj<Boolean> isConnected = new Obj<Boolean>(connectedFlag);
		
		// Create new socket channel socket selector object.
		state.socketSelector = Selector.open();
		state.socketChannel.register(state.socketSelector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		
		// Wait for connection.
		int numAttempts = 3;
		while (!isConnected.ref && numAttempts > 0) {
			
			state.socketSelector.select(key -> {
				
				boolean isConnectable = key.isConnectable();
				if (isConnectable) {
					
					// Get channel and finish the connection.
					SocketChannel currentChannel = (SocketChannel) key.channel();
					try {
						isConnected.ref = currentChannel.finishConnect();
					}
					catch (Exception e) {
					}
				}
				j.log(1);
				
			}, XdebugListener.xdebugClientTimeoutMs);
			
			numAttempts--;
		}
		
		// Return the state.
		return state;
	}
	
	/**
	 * Returns true value, if the debugger is in active state.
	 * @return
	 */
	public boolean checkActive() throws Exception {
		
		// Check states.
		if (socketChannel == null || socketSelector == null) {
			close();
			return false;
		}
		
		// Check Xdebug socket connection.
		boolean isConnected = socketChannel.isConnected();
		return isConnected;
	}
	
	/**
	 * Close the Xdebug connection.
	 */
	private void close() throws Exception {
		
		// Try to close the socket.
		if (socketChannel != null) {
			socketChannel.close();
		}
		
		// Reset the references.
		this.socketChannel = null;
	}
	
	/**
	 * Write a message to Xdebug channel.
	 * @param message
	 */
	public void write(String message)
			throws Exception {
		
		// Create bytes with prepended message length.
		int messageLength = message.length();
		String messageLengthString = String.valueOf(messageLength);
		
		int bufferBytesCount = 2 * (messageLengthString.length() + 2 + messageLength);
		ByteBuffer buffer = ByteBuffer.allocate(bufferBytesCount);
		
		buffer.put(messageLengthString.getBytes(protocolEncoding));
		buffer.putChar('\0');
		buffer.put(message.getBytes(protocolEncoding));
		
		// Write bytes to the socket channel.
		writeMessage(socketChannel, "testing statement");
//		this.socketChanel.write(buffer);
	}
	
	/**
	 * Write a message to Xdebug channel.
	 * @param message
	 */
	public void writeMessage(SocketChannel socketChannel, String message)
			throws Exception {
		
		// Convert to bytes.
		ByteBuffer bytes = ByteBuffer.wrap(message.getBytes(protocolEncoding));
		socketChannel.write(bytes);
	}
	
	/**
	 * Communicate with debugger.
	 * @param statementLambda
	 * @throws IOException
	 */
	public void communicate(BiFunction<SocketChannel, String, Boolean> statementLambda)
			throws IOException {
		
		// Xdebug protocol communication loop.
		Obj<Boolean> exit = new Obj<Boolean>(false);
		while (!exit.ref) {
			
			try {
				// Wait for incomming statement.
				socketSelector.select(key -> {
				
					if (key.isReadable()) {
						try {
							
							// Read input statement.
							SocketChannel channel = (SocketChannel) key.channel();
							
							ByteBuffer bytes = ByteBuffer.allocate(inputBufferSize);
							channel.read(bytes);
							
							String inputStatement = new String(bytes.array(), protocolEncoding);
							
							// Process input statement. Break the loop on exit flag.
							exit.ref = statementLambda.apply(channel, inputStatement);
						}
						catch (IOException e) {
						}
					}
		
				}, XdebugListener.xdebugClientTimeoutMs);
			}
			catch (Exception e) {
			}
		}
	}
}
