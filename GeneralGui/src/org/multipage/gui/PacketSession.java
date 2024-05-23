/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 19-09-2023
 *
 */
package org.multipage.gui;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.multipage.util.j;

/**
 * Packet session that reads sent packets and writes outcome packets.
 * @author vakol
 *
 */
public class PacketSession {

	/**
	 * Incomming buffer size.
	 */
	private static final int INPUT_BUFFER_SIZE = 1024;
	
	/**
	 * Session name.
	 */
	public String name = null;
	
	/**
	 * Receiving socket address.
	 */
	private InetSocketAddress socketAddress = null;
	
	/**
	 * Auxiliary properties which can be set.
	 */
	public HashMap<Integer, Object> userProperties = new HashMap<Integer, Object>();
	
	/**
	 * Currently read packet.
	 */
	public Packet readPacket = null;
	
	/**
	 * Read synchronization object.
	 */
	private Object readSync = new Object();
	
	/**
	 * Remaining bytes from input buffer when packets are read.
	 */
	private byte [] remainingBytes = null;
	
	/**
	 * Reference to client socket channel.
	 */
	private AsynchronousSocketChannel clientSocketChannel = null;
	
	/**
	 * Constructor.
	 * @param name - name of the session
	 */
	public PacketSession(String name) {
		
		this.name = name;
	}
	
	/**
	 * Constructor.
	 * @param name - name of the session
	 * @param clientSocketClient
	 */
	public PacketSession(String name, AsynchronousSocketChannel clientSocketClient) {
		
		this.name = name;
		this.clientSocketChannel = clientSocketClient;
	}

	/**
	 * Returns socket address.
	 * @return
	 */
	public InetSocketAddress getSocketAddress() {
		
		return this.socketAddress;
	}
		
	/**
	 * Create new completion handler for read operation.
	 * @param completedLambda
	 * @param failedLambda
	 * @param nextReadLambda
	 * @return
	 */
	private CompletionHandler<Integer, ByteBuffer> newReadCompletionHandler(
				Consumer<CompletionHandler<Integer, ByteBuffer>> nextReadLambda,
				BiConsumer<Integer, ByteBuffer> completedLambda,
				Consumer<Throwable> failedLambda) {
		
		// Create completion handler.
        CompletionHandler<Integer, ByteBuffer> readCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {
			
        	@Override
			public void completed(Integer result, ByteBuffer inputBuffer) {
        		
        		// Check connection.
        		boolean isOpen = clientSocketChannel.isOpen();
        		if (!isOpen) {
        			return;
        		}
        		
        		boolean isEof = (result < 0);
        		
        		// When requested close the session.
        		if (isEof) {
        			closeClientSocket();
        			return;
        		}
        		
        		// TODO: <---DEBUG On read completed. Check if channel is open.
        		j.log("On read completed. Channel is open %b. Result is %d", isOpen, result);
        		
				// Call lambda function for completion.
				completedLambda.accept(result, inputBuffer);
				
        		// Create new handler and invoke next read operation.
				CompletionHandler<Integer, ByteBuffer> nextCompletionHandler = newReadCompletionHandler(nextReadLambda, completedLambda, failedLambda);
				nextReadLambda.accept(nextCompletionHandler); 
 			}
        	
			@Override
			public void failed(Throwable exception, ByteBuffer inputBuffer) {
								
				// Call lambda function when operation failed.
				failedLambda.accept(exception);
			}
        };
        return readCompletionHandler;
	}
	
	/**
	 * Start reading packets from socket channel.
	 * @param clientSocketChannel
	 */
	protected <T> void startReadingPackets(AsynchronousSocketChannel clientSocketChannel)
			throws Exception {
		
		// Check socket channel.
		boolean isOpen = clientSocketChannel.isOpen();
		if (!isOpen) {
			return;
		}
		
		// Remember socket channel reference.
		this.clientSocketChannel = clientSocketChannel;
		
		// Process successful result of the read operation.
		BiConsumer<Integer, ByteBuffer> readCompletedLambda = (bytesToRead, inputBuffer) -> {
			
    		try {
        		// Read packets from input buffer.
    			readPacketElements(inputBuffer);
    		}
    		catch (Exception e)	{
    			onException(e);
    		}									
		};
		
		// Read exception.
		Consumer<Throwable> readFailedLambda = readException -> {
			
			// If the channel is closed do not show exception.
			if (readException instanceof AsynchronousCloseException) {
				return;
			}
			// Show exception.
			onException(readException);			
		};
		
		// Read operation.
		Consumer<CompletionHandler<Integer, ByteBuffer>> readPacketLambda = readCompletionHandler -> {
			
			synchronized (PacketSession.this.readSync) {
				
				// Check if the socket channel is opened.
				boolean isOpened = clientSocketChannel.isOpen();
				if (!isOpened) {
					return;
				}
				
				try {
					// Start reading from the socket channel.
					ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
	        		clientSocketChannel.read(inputBuffer, inputBuffer, readCompletionHandler);
	        		return;
				}
				catch (Exception e) {
					onException(e);
				}
			}
		};
		
		// Create completion handler.
		CompletionHandler<Integer, ByteBuffer> readCompletionHandler = newReadCompletionHandler(readPacketLambda, readCompletedLambda, readFailedLambda);
		// Run first read operation with first completion handler. 
		readPacketLambda.accept(readCompletionHandler);
	}
	
	/**
	 * Read packet elements from the input buffer.
	 * @param inputBuffer
	 * @throws Exception 
	 */
	protected int readPacketElements(ByteBuffer inputBuffer)
			throws Exception {
		
		// Put remaining bytes at the beginning of input buffer.
		int remainingLength = 0;
		if (remainingBytes != null) {
			
			remainingLength = remainingBytes.length;
			if (remainingLength > 0) {
				
				inputBuffer.flip();
				int inputLength = inputBuffer.limit();
				byte [] inputBytes = new byte[inputLength];
				
				int length = remainingLength + inputLength;
				ByteBuffer newInputBuffer = ByteBuffer.allocateDirect(length);

				newInputBuffer.put(remainingBytes);
				newInputBuffer.put(inputBytes);
			}
		}
		
		// Prepare input buffer for reading.
		inputBuffer.flip();
		
		// If there are no remaining bytes in the input buffer, return false value.
		if (!inputBuffer.hasRemaining()) {
			return 0;
		}
		
		// If the packet doesn't exist, create it.
		if (readPacket == null) {
			readPacket = new Packet();
		}
		
		// Initialize end of packet flag.
		boolean endOfPacket = false;
		
		PacketElement newElement = null;
		
		// Read until end of input buffer.
		boolean endOfInputBuffer = false;
		while (!endOfInputBuffer) {
			
			// Get new packet element.
			newElement = getNewPacketElement(readPacket);
			readPacket.throwException();
			if (newElement == null) {
				
				// There are no more elements.
				break;
			}
			
			// Read new packet element of given type.
			if (newElement instanceof PacketSymbol) {
				PacketSymbol symbol = (PacketSymbol) newElement;
				endOfInputBuffer = readSymbol(inputBuffer, symbol);
				
				if (symbol.isCompact) {
					readPacket.packetParts.add(symbol);
					endOfPacket = onSymbol(symbol);
				}
			}
			else if (newElement instanceof PacketNumber) {
				PacketNumber number = (PacketNumber) newElement;
				endOfInputBuffer = readInt(inputBuffer, number);
				
				if (number.isCompact) {
					readPacket.packetParts.add(number);
					endOfPacket = onNumber(number);
				}
			}
			else if (newElement instanceof PacketBlock) {	
				PacketBlock block = (PacketBlock) newElement;
				endOfInputBuffer = readBlock(inputBuffer, block);
				
				if (block.isCompact) {
					readPacket.packetParts.add(block);
					endOfPacket = onBlock(block);
				}
			}
			else {
				Exception exception = new IllegalArgumentException();
				onException(exception);
			}
			
			// Call callback method on the end of the input packet.
			if (endOfPacket) {
				onEndOfPacket(readPacket);
			}
		}
		
		// Return remaining bytes in input buffer.
		int remaining = inputBuffer.remaining();
		
		// Save the remaining bytes.
		if (remaining > 0) {
			remainingBytes = new byte [remaining];
			inputBuffer.get(remainingBytes);
		}
		
		return remaining;
	}

	/**
     * Go through input buffer bytes until the input symbol is read.
     * @param inputBuffer
     * @param symbol
     * @return end of buffer
     */
	public boolean readSymbol(ByteBuffer inputBuffer, PacketSymbol symbol)
			throws Exception {
		
		// Initialization.
		final byte [] symbolBytes = symbol.bytes;
		int startLength = symbolBytes.length;
		symbol.isCompact = false;
		
		// Loop for input bytes.
		while (inputBuffer.hasRemaining()) {
			
			// Read current byte from the buffer.
			byte theByte = inputBuffer.get();
			
			// Try to match bytes with the start symbol.
			if (theByte == symbolBytes[symbol.index]) {
				symbol.index++;
				if (symbol.index >= startLength) {
					
					symbol.isCompact = true;
					symbol.index = 0;
					return false;
				}
				continue;
			}
		}
		return true;
	}
	
	/**
	 * Read integer from the input buffer.
	 * @param inputBuffer
	 * @param number
	 * @return end of buffer
	 */
	public boolean readInt(ByteBuffer inputBuffer, PacketNumber number) 
			throws Exception {
		
		// Initialization.
		number.isCompact = false;
		
		// Integer vaules occupy 4 bytes.
		// Read input bytes.
		while (number.index <= 3) {
			
			// If there are no remaining bytes, exit the method.
			if (!inputBuffer.hasRemaining()) {
				return true;
			}
			
			// Read current byte from the buffer.
			byte theByte = inputBuffer.get();
			
			// Compile integer value.
			number.value |= theByte << ((3 - number.index) * 8);
			
			number.index++;
		}
		
		number.isCompact = true;
		number.index = 0;
		
		// Return end of buffer flag.
		boolean endOfBuffer = !inputBuffer.hasRemaining();
		return endOfBuffer;
	}
	
	/**
     * Read bytes from input buffer until the terminal symbol is found. If the terminal is not found set
     * the "terimnated" flag to false. 
     * @param inputBuffer
     * @param block
     * @return true value if the end of obuffer has been reached
     */
	public boolean readBlock(ByteBuffer inputBuffer, PacketBlock block)
			throws Exception {
		
		boolean endOfBuffer = false;
		
		// Delegate this call to appropriate method.
		if (block.length > 0) {
			endOfBuffer = readLongBlock(inputBuffer, block);
		}
		else if (block.terminalSymbol != null && block.terminalSymbol.bytes != null && block.terminalSymbol.bytes.length > 0) {
			endOfBuffer = readTerminatedBlock(inputBuffer, block);
		}
		else {
			Exception exception = new InvalidParameterException();
			onException(exception);
		}
		return endOfBuffer;
	}
	
	/**
	 * Read block of bytes from input buffer.
	 * @param inputBuffer
	 * @param block
	 * @return
	 */
	public boolean readLongBlock(ByteBuffer inputBuffer, PacketBlock block)
			throws Exception {
		
		// Loop for input bytes.
		while (inputBuffer.hasRemaining()) {
			
			// Check length.
			if (block.index >= block.length) {
				
				block.isCompact = true;
				break;
			}
			
			// Read current byte from the buffer.
			byte theByte = inputBuffer.get();
			
			block.index++;
						
			// Increase buffer capacity.
			ensureBufferCapacity(block);
			
			// Output current byte.
			block.buffer.put(theByte);
		}
		
		// Return a value indicating wheter the input buffer has remaining bytes.
		boolean endOfBuffer = !inputBuffer.hasRemaining();
		return endOfBuffer;
	}
	
	/**
	 * Read terminated block of bytes from the input buffer.
	 * @param inputBuffer
	 * @param block
	 * @return
	 */
	public boolean readTerminatedBlock(ByteBuffer inputBuffer, PacketBlock block) 
			throws Exception {
		
		// Initialization.
		byte [] terminalSymbol = block.terminalSymbol.bytes;
		block.isCompact = false;
		
		// Loop for input bytes.
		while (inputBuffer.hasRemaining()) {
						
			// Read current byte from the buffer.
			byte theByte = inputBuffer.get();
			
			block.index++;
			
			// Try to match terminal symbol with read bytes.
			try {
				if (theByte == terminalSymbol[block.terminalIndex]) {
					
					block.terminalIndex++;
					
					if (block.terminalIndex >= terminalSymbol.length) {
						block.isCompact = true;
						break;
					}
					continue;
				}
				else {
					// If terminal symbol was not found, reset the index.
					block.terminalIndex = 0;
				}
			}
			catch (Exception e) {
				onException(e);
			}
			
			// Increase buffer capacity.
			ensureBufferCapacity(block);
			
			// Output current byte.
			block.buffer.put(theByte);
		}
		
		// Return a value indicating wheter the input buffer has remaining bytes.
		boolean endOfBuffer = !inputBuffer.hasRemaining();
		return endOfBuffer;
	}
	
	/**
	 * If the buffer is full, increase the buffer capacity.
	 * @param block
	 */
	private static void ensureBufferCapacity(PacketBlock block)
			throws Exception {
		
		int position = block.buffer.position();
		int capacity = block.buffer.capacity();
		
		if (!block.buffer.hasRemaining() && position >= capacity) {
			
			block.buffer.flip();
			
			// Increase buffer capacity.
			ByteBuffer increasedOutputBuffer = ByteBuffer.allocate(capacity + block.increaseSize);
			
			increasedOutputBuffer.put(block.buffer);
			block.buffer = increasedOutputBuffer;
		}
	}
	
	/**
	 * Close this session.
	 * @throws IOException 
	 */
	public void closeClientSocket() {
		
		if (clientSocketChannel == null || !clientSocketChannel.isOpen()) {
			return;
		}
		
		try {
			// Close the client socket.
			clientSocketChannel.shutdownOutput();
			clientSocketChannel.shutdownInput();
			clientSocketChannel.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get next packet element to be processed. Calls a lambda function.
	 * @param packet
	 * @return
	 */
	protected PacketElement getNewPacketElement(Packet packet)
			throws Exception {
		
		// Override this method.
		return null;
	}
	
	/**
	 * Fired on successful processing of the sent symbol.
	 * @param symbol
	 * @return - True on end of the packet.
	 */
	protected boolean onSymbol(PacketSymbol symbol) 
			throws Exception {
		
		// Override this method.
		return false;
	}
	
	/**
	 * Fired on successful processing of the sent number.
	 * @param number
	 * @return - True on end of the packet.
	 */
	protected boolean onNumber(PacketNumber number)
			throws Exception {
		
		// Override this method.
		return false;
	}	
	
	/**
	 * Fired on successful processing of the sent block of bytes.
	 * @param block
	 * @return - True on end of the packet.
	 */	
	protected boolean onBlock(PacketBlock block)
			throws Exception {
		
		// Override this method.
		return false;
	}
	
	/**
	 * Fired on successful processing of whole packet.
	 * @param packet
	 */
    protected void onEndOfPacket(Packet packet) 
    		throws Exception {
		
    	// Override this method.
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
