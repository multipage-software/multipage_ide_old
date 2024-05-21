/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 10-08-2023
 *
 */
package org.multipage.gui;

import java.awt.Color;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.multipage.util.Obj;

/**
 * Console reader class.
 * @author vakol
 */
class LogReader extends PacketSession {

	/**
	 * Incomming buffers' sizes.
	 */
	private static final int HEADER_BUFFER_SIZE = 180;
	private static final int BODY_BUFFER_SIZE = 1024;
	
	/**
	 * Paremeter keys.
	 */
	private static final int TIMESTAMP 	= 1;
	private static final int COLOR 		= 2;
	private static final int STATEMENT 	= 3;
	private static final int BODY 		= 4;

	// Parse timestamp and color.
	private static Pattern regexTimsestampAndColor = null;
	
	/**
	 * Static constructor.
	 */
	static {
		
		// Compile regular expression matcher.
		regexTimsestampAndColor = Pattern.compile("(?<stamp>^[^#]*)#rgb\\((?<red>\\w+),(?<green>\\w+),(?<blue>\\w+)\\)");
	}
	
	/**
	 * Reference to the log console that will display messages.
	 */
	private LogConsole logConsole = null;
	
	/**
	 * Packet elements definition.
	 */
	private PacketSymbol startOfHeading = new PacketSymbol(new byte [] { (byte) 0x00, (byte) 0x01 });
	private PacketSymbol startOfText = new PacketSymbol(new byte [] { (byte) 0x00, (byte) 0x02 });
	private PacketSymbol endOfTransmission = new PacketSymbol(new byte [] { (byte) 0x00, (byte) 0x04 });
	private PacketBlock headerBytes = new PacketBlock(HEADER_BUFFER_SIZE, HEADER_BUFFER_SIZE, startOfText, -1);
	private PacketBlock bodyBytes = new PacketBlock(BODY_BUFFER_SIZE, BODY_BUFFER_SIZE, endOfTransmission, -1);
	
	
	/**
	 * Constructor.
	 * @param logConsole
	 */
	public LogReader(LogConsole logConsole) {
		
		super(logConsole.name);
		this.logConsole = logConsole;
	}

	/**
	 * Get next packet element.
	 */
	@Override
	protected PacketElement getNewPacketElement(Packet packet)
			throws Exception {
		
		PacketElement element = null;
		
		// Get the last element in the packet.
		if (!packet.packetParts.isEmpty()) {
			element = packet.packetParts.getLast();
		}
		
		// If current packet element doesn't exit, initialize the packet sequence.
		if (element == null) {
			element = startOfHeading;
		}
		// If current packet element is not finished, return the same value.
		else if (!element.isCompact) {
			return element;
		}
		// Otherwise if the element is finished use transition rules to determine next one.
		else if (element == startOfHeading) {
			element = headerBytes;
		}
		else if (element == headerBytes) {
			element = bodyBytes;
		}
		else if (element == bodyBytes) {
			element = startOfHeading;
		}
		else {
			element = null;
		}
		
		// Set new current element.
		return element;
	}
	
	/**
	 * On symbol element.
	 * @param symbol
	 * @return - True on end of the packet.
	 */
	@Override
	protected boolean onSymbol(PacketSymbol symbol)
			throws Exception {

		// Return true on end of packet transfer.
		if ((symbol == endOfTransmission) && symbol.isCompact) {
			return true;
		}
		return false;
	}
	
	/**
	 * On number element.
	 * @param number
	 * @return - True on end of the packet.
	 */
	@Override
	protected boolean onNumber(PacketNumber number)
			throws Exception {
	
		// On unknown number element.
		readPacket.packetException = new IllegalArgumentException("Invalid packet element of type integer.");
		return false;
	}
	
	/**
	 * On block element.
	 * @param block - The block element.
	 * @return - True on end of the packet.
	 */
	@Override
	protected boolean onBlock(PacketBlock block)
			throws Exception {
	
		// On header bytyes...
		if (block == headerBytes) {
			
			// Get header bytes.
			int headerLength = block.buffer.position();
			
			try {
				block.buffer.flip();
				
				byte [] headerBytes = new byte [headerLength];
				block.buffer.get(headerBytes);
				
				Obj<LocalTime> timestamp = new Obj<LocalTime>(null);
				Obj<Color> color = new Obj<Color>(null);
				Obj<String> statement = new Obj<String>(null);
				
				boolean success = parseHeader(headerBytes, timestamp, color, statement);
				if (success) {
					readPacket.userProperties.put(TIMESTAMP, timestamp.ref);
					readPacket.userProperties.put(COLOR, color.ref);
					readPacket.userProperties.put(STATEMENT, statement.ref);
				}
				
				block.buffer.clear();
			}
			catch (Exception e) {
				
			}
		}
		// On body bytes...
		else if (block == bodyBytes) {
			
			// Get body bytes.
			int headerLength = block.buffer.position();
			
			try {
				block.buffer.flip();
				
				byte [] bodyBytes = new byte [headerLength];
				block.buffer.get(bodyBytes);
				
				block.buffer.clear();
			
				String body = getMessageBody(bodyBytes);
				readPacket.userProperties.put(BODY, body);
				
				return true;
			}
			catch (Exception e) {
				readPacket.packetException = e;
			}
		}
		// On unknown block.
		else {
			readPacket.packetException = new IllegalArgumentException("Invalid packet block.");
		}
		return false;
	}
	
	/**
	 * On end of single packet. 
	 */
	@Override
	protected void onEndOfPacket(Packet packet) {
		
		// Create new log message.
		LocalTime timestamp = (LocalTime) readPacket.userProperties.get(TIMESTAMP);
		Color color = (Color) readPacket.userProperties.get(COLOR);
		String statement = (String) readPacket.userProperties.get(STATEMENT);
		String body = (String) readPacket.userProperties.get(BODY);
		
		LogMessageRecord messageRecord = new LogMessageRecord(timestamp, color, body, statement);
		
		// Cache the  new record.
		if (logConsole != null) {
			logConsole.cacheMessageRecord(messageRecord);
		}
		
		
		// Reset all elements.
		startOfHeading.reset();
		headerBytes.reset();
		startOfText.reset();
		bodyBytes.reset();
		endOfTransmission.reset();
		
		// Reset packet state.
		packet.reset();
	}
	
	/**
	 * Parse bytes from the header buffer.
	 * @param headerBuffer
	 * @param outputTimestamp
	 * @param outputColor
	 * @param outputStatement
	 * @return
	 * @throws Exception 
	 */
	private boolean parseHeader(byte [] bytes, Obj<LocalTime> outputTimestamp, Obj<Color> outputColor, Obj<String> outputStatement)
			throws Exception {

		// Convert bytes into UTF-8 encoded string.
		String headerString = new String(bytes, "UTF-8");
		
		// Inicialization.
		outputTimestamp.ref = LocalTime.now();
		outputColor.ref = Color.BLACK;
		outputStatement.ref = "";
		boolean success;
		
		// Parse the received string.
		Matcher matcher = regexTimsestampAndColor.matcher(headerString);
		boolean found = matcher.find();
		int groupCount = matcher.groupCount();
		
		if (found && groupCount == 4) {
			
			// Get the message timestamp and color.
			String timestampString = matcher.group(1);
			
			// Convert text to timstamp object.
			outputTimestamp.ref = LocalTime.parse(timestampString, LogConsoles.TIMESTAMP_FORMAT);
			
			// Get color components.
			String redString = matcher.group(2);
			String greenString = matcher.group(3);
			String blueString = matcher.group(4);
			int red = Integer.parseInt(redString, 16);
			int green = Integer.parseInt(greenString, 16);
			int blue = Integer.parseInt(blueString, 16);
			
			// Compile color value.
			outputColor.ref = new Color(red, green, blue);
			
			// Set output flag to success.
			success = true;
		}
		else {
			// Set output statement.
			outputStatement.ref = headerString;
			// Reset output flag.
			success = false;
		}
		
		// Return result.
		return success;
	}
	
	/**
	 * Get log message from the input buffer.
	 * @param logMessageBuffer
	 * @return
	 * @throws Exception 
	 */
	private String getMessageBody(byte [] bytes)
			throws Exception {
		
		// Convert bytes into UTF-8 encoded string.
		String logMessageText = new String(bytes, "UTF-8");
		
		// Return result.
		return logMessageText;
	}
	
	@Override
	protected void onException(Throwable exception) {
		
		// TODO: <---REFACTOR Display the exception in a dialog window.
		exception.printStackTrace();
	}
}