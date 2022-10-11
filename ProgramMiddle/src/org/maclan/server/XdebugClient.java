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
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.maclan.server.XdebugListener.XdebugStatement;
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
	 * Size of the input and output buffers in bytes.
	 */
	private static final int inputBufferSize = 1025;
	private static final int outputBufferSize = 1025;
	
	/**
	 * Xdebug idle state in milliseconds.
	 */
	protected final static int xdebugIdleMs = 200;
	
	/**
	 * Socket channel socketSelector.
	 */
	public Selector socketSelector = null;
	
	/**
	 * Socket object reference.
	 */
	public SocketChannel socketChannel = null;
	
	/**
	 * Sequence number.
	 */
	private int statementNumber = 0;
	
	/**
	 * Transaction number.
	 */
	private int transactionNumber = 0;
	
	/**
	 * Callback interface.
	 */
	public static abstract class Callback {
		
		/**
		 * Accept Xdebug statement.
		 * @param xdebugStatement
		 */
		protected String accept(XdebugStatement xdebugStatement) {
			// Override it.
			return null;
		}
	}
	
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
		
		int bufferBytesCount = (messageLengthString.length() + 2 + messageLength);
		ByteBuffer buffer = ByteBuffer.allocate(bufferBytesCount);
		
		buffer.put(messageLengthString.getBytes(protocolEncoding));
		buffer.put((byte) 0);
		buffer.put(message.getBytes(protocolEncoding));
		
		buffer.rewind();
		
		socketChannel.write(buffer);
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
	 * Connect to IDE and return new Xdebug client.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param sourceLocator
	 * @return
	 * @throws Exception 
	 */
	synchronized public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String sourceLocator)
			throws Exception {
		
		// Get current process and its parent process IDs.
		ProcessHandle processHandle = ProcessHandle.current();
		long processId = processHandle.pid();
		
		// Get current thread ID.
		long threadId = Thread.currentThread().getId();
		
		// Create Xdebug client.
		XdebugClient xdebugClient = XdebugClient.connect(ideHostName, xdebugPort);
		
		// Initialize Xdebug protocol communication.
		String initialXmlPacket = String.format(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<init appid=\"Area Server pid(%d)\" " +
				"idekey=\"%s\" " +
				"session=\"\" " +
				"thread=\"%d\" " +
				"parent=\"\" " +
				"language=\"Maclan\" " +
				"protocol_version=\"1.0\" " +
				"fileuri=\"%s\"/>",
				processId,
				XdebugListener.requiredIdeKey,
				threadId,
				sourceLocator);
		
		xdebugClient.write(initialXmlPacket);
		
		return xdebugClient;
	}
	
	/**
	 * Communicate with debugger.
	 * @param statementLambda
	 * @throws IOException
	 */
	synchronized public void communicate(Callback callback)
			throws IOException {
		
		// Initialize transaction number.
		transactionNumber = 1;
		
		// Xdebug protocol communication loop.
		Obj<Boolean> exit = new Obj<Boolean>(false);
		while (!exit.ref) {
			
			try {
				// Wait for incoming statement.
				socketSelector.select(key -> {
				
					if (key.isValid() && key.isReadable()) {
						
						try {
							// Read input statement.
							SocketChannel channel = (SocketChannel) key.channel();
							
							ByteBuffer inputBuffer = ByteBuffer.allocate(inputBufferSize);
							channel.read(inputBuffer);
							
							String statementText = new String(inputBuffer.array(), protocolEncoding);
							
							XdebugStatement xdebugStatement = XdebugStatement.parse(statementText);
							if (xdebugStatement == null) {
								return;
							}
							
							// Check transaction ID.
							int readTransactionNumber = xdebugStatement.getTransactionNumber();
							if (readTransactionNumber != transactionNumber) {
								return;
							}
							
							// Process input statement. Break the loop on exit flag.
							String responseText = callback.accept(xdebugStatement);
							if (responseText == null) {
								responseText = "0";
							}

							// Write returned text.
							final String divider = "\0";
							int textLength = responseText.length();
							String lengthString = String.valueOf(textLength);
							
							byte [] lengthBytes = lengthString.getBytes(StandardCharsets.UTF_8);
							byte [] dividerBytes = divider.getBytes(StandardCharsets.UTF_8);
							byte [] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
							
							int bufferSize = lengthBytes.length + dividerBytes.length + responseBytes.length;
							ByteBuffer outputBuffer = ByteBuffer.allocate(bufferSize);
							
							outputBuffer.clear();
							outputBuffer.put(lengthBytes);
							outputBuffer.put(dividerBytes);
							outputBuffer.put(responseBytes);
							outputBuffer.rewind();
							
							int bytesWritten = channel.write(outputBuffer);
							
							// TODO: <---DEBUG WRITE RESPONSE
							j.log("WRITE RESPONSE len=%d \"%s\"", bytesWritten, responseText);
							
							// Increment transaction number.
							transactionNumber++;
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
		
				}, XdebugListener.xdebugClientTimeoutMs);
				
				// Idle time span.
				Thread.sleep(xdebugIdleMs);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkTransactionNumber(int transactionNumber) {
		// TODO Auto-generated method stub
		return false;
	}
}
