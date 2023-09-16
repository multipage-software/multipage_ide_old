/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 10-08-2023
 *
 */
package org.multipage.gui;

import java.awt.Color;
import java.awt.Component;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;

import org.multipage.gui.Consoles.MessageRecord;
import org.multipage.util.Obj;

/**
 * Console class.
 * @author vakol
 */
class LogConsole {
	
	/**
	 * Maximum number of records.
	 */
	private static final int MAXIMUM_RECORDS = 300;
	
	/**
	 * Incomming buffers' sizes.
	 */
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int TIMESTAMP_BUFFER_SIZE = 128;
	private static final int LOG_MESSAGE_BUFFER_SIZE = INPUT_BUFFER_SIZE;
	
	/**
	 * Input reader states.
	 */
	private static final int START_READING = 0;
	private static final int READ_HEADER_LENGTH = 1;
	private static final int READ_HEADER = 2;
	private static final int READ_BODY_LENGTH = 3;
	private static final int READ_BODY = 4;
	
	/**
	 * Maximum lengths of input data.
	 */
	private static final int MAXIMUM_HEADER_LENGHT = 200;
	private static final int MAXIMUM_BODY_LENGHT = 2048;
	
	/**
	 * Panel borders.
	 */
	private static Border selectionBorder = null;
	private static Border simpleBorder = null;
	
	// Parse timestamp and color.
	private static Pattern regexTimsestampAndColor = null;
	
	/**
	 * Constructor.
	 */
	static {
		
		// Create panel borders.
		selectionBorder = BorderFactory.createLineBorder(Color.RED);
		
		// Compile regular expression matcher.
		regexTimsestampAndColor = Pattern.compile("(?<stamp>^[^#]*)#rgb\\((?<red>\\w+),(?<green>\\w+),(?<blue>\\w+)\\)");
	}
	
	/**
	 * Console name.
	 */
	protected String name = "unknown";
	
	/**
	 * Receiving socket address.
	 */
	public InetSocketAddress socketAddress = null;
	
	/**
	 * Input socket chanel.
	 */
	public AsynchronousServerSocketChannel inputSocket = null;
	
	/**
	 * Console port number.
	 */
	protected int port = -1;
	
	/**
	 * Console input buffers.
	 */
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
	private Obj<Integer> startSymbolPosition = new Obj<Integer>(0);
	private Obj<Integer> headerLength = new Obj<Integer>(-1);
	private Obj<Integer> headerLenghtPosition = new Obj<Integer>(0);
	private Obj<ByteBuffer> headerBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(TIMESTAMP_BUFFER_SIZE));
	private Obj<Integer> headerPosition = new Obj<Integer>(0);
	private Obj<Integer> headerTerminalIndex = new Obj<Integer>(0);
	private Obj<Integer> bodyLength = new Obj<Integer>(-1);
	private Obj<Integer> bodyLengthPosition = new Obj<Integer>(0);
	private Obj<ByteBuffer> bodyBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(LOG_MESSAGE_BUFFER_SIZE));
	private Obj<Integer> bodyPosition = new Obj<Integer>(0);
	private Obj<Integer> bodyTerminalIndex = new Obj<Integer>(0);
	
	// Initial state of the input bytes reader.
	private int inputReaderState = START_READING;
	
	/**
	 * Last read timestamp and color.
	 */
	private Obj<LocalTime> timestamp = new Obj<LocalTime>(null);
	private Obj<Color> color = new Obj<Color>(null);
	private Obj<String> statement = new Obj<String>(null);
	
	/**
	 * Message record list that maps time axis to the records.
	 */
	protected LinkedList<MessageRecord> consoleRecords = new LinkedList<>();
	
	/**
	 * Minimum and maximum timestamps.
	 */
	protected LocalTime minimumTimestamp = null;
	protected LocalTime maximumTimestamp = null;
	
	/**
	 * Split panel.
	 */
	protected JSplitPane splitPane = null;
	
	/**
	 * Scroll panel
	 */
	protected JScrollPane scrollPane = null;
	
	/**
	 * Console text panel.
	 */
	protected JTextPane textPane = null;
	
	/**
	 * 
	 * @param consoleName
	 * @param splitPane
	 * @param port
	 * @wbp.parser.entryPoint
	 */
	public LogConsole(String consoleName, JSplitPane splitPane, int port)
			throws Exception {
		
		this.name = consoleName;
		this.splitPane = splitPane;
		
		Component leftComponent = splitPane.getLeftComponent();
		if (!(leftComponent instanceof JScrollPane)) {
			throw new IllegalArgumentException();
		}
		
		this.scrollPane = (JScrollPane) leftComponent;
		
		JViewport viewport = this.scrollPane.getViewport();
		Component scrollComponent = viewport.getView();
		if (!(scrollComponent instanceof JTextPane)) {
			throw new IllegalArgumentException();
		}
		
		JTextPane textPane = (JTextPane) scrollComponent;
		this.textPane = textPane;
		this.port = port;
		
		// Reset selection.
		setSelected(false); //$hide$
	}
	
	/**
	 * Try to run console statment.
	 * @param messageRecord
	 * @return
	 */
	public boolean runStatement(MessageRecord messageRecord) {
		
		boolean isStatement = false;
		
		// On clear console.
		if ("CLEAR".equalsIgnoreCase(messageRecord.statment)) {
			
			// Clear console contents.
			clear();
			
			// Set output flag.
			isStatement = true;
		}
		
		// Display statement in the log view.
		if (isStatement) {
			
			// Append new record to the end of the list.
			consoleRecords.add(messageRecord);
		}
		
		return isStatement;
	}

	/**
	 * Take new message record.
	 * @param messageRecord
	 */
	public void cacheMessageRecord(MessageRecord messageRecord) {
		
		// Try to run console statement..
		boolean success = runStatement(messageRecord);
		if (success) {
			return;
		}

		// Get current time.
		LocalTime timeNow = LocalTime.now();
		
		messageRecord.consoleWriteTime = timeNow;

		// Set maximum and minimum timestamp.
		if (minimumTimestamp == null) {
			minimumTimestamp = timeNow;
		}
		if (maximumTimestamp == null || maximumTimestamp.compareTo(timeNow) < 0) {
			maximumTimestamp = timeNow;
		}

		// If number of records exceeds the maximum, remove 10 records from the beginning of the list.
		int recordCount = consoleRecords.size();
		if (recordCount > MAXIMUM_RECORDS) {
			
			for (int index = 0; index < 10; index++) {
				consoleRecords.removeFirst();
			}
		}
		
		// Append new record to the end of the list.
		consoleRecords.add(messageRecord);
	}
	
	/**
	 * Clear console content.
	 */
	public synchronized void clear() {
		
		consoleRecords.clear();
		maximumTimestamp = null;
		minimumTimestamp = null;
		textPane.setText("");
	}
	
	/**
	 * Read log messages from the input buffer.
	 * @throws Exception 
	 */
	public int readLogMessages()
			throws Exception {
		
		// Prepare input buffer for reading.
		inputBuffer.flip();
		
		// If there are no remaining bytes in the input buffer, return false value.
		if (!inputBuffer.hasRemaining()) {
			return 0;
		}
		
		int messagesCount = 0;
		
		// Read until end of input buffer.
		boolean endOfInputBuffer = false;
		while (!endOfInputBuffer) {
			
			Obj<Boolean> terminalSymbolInterrupt = new Obj<Boolean>(false);
			
			// Determine protocol state from input byte value and invoke related action.
			switch (inputReaderState) {
			
			case START_READING:
				
				// Reset values read from buffer.
				headerLength.ref = 0;
				headerLenghtPosition.ref = 0;
				headerPosition.ref = 0;
				headerTerminalIndex.ref = 0;
				bodyLength.ref = 0;
				bodyLengthPosition.ref = 0;
				bodyPosition.ref = 0;
				bodyTerminalIndex.ref = 0;
				timestamp.ref = null;
				color.ref = null;
				statement.ref = null;
				
				endOfInputBuffer = Utility.readSymbol(inputBuffer, Consoles.START_OF_HEADING, startSymbolPosition, terminalSymbolInterrupt);
				if (terminalSymbolInterrupt.ref) {
					inputReaderState = READ_HEADER_LENGTH;
				}
				break;
			
			case READ_HEADER_LENGTH:
				
				endOfInputBuffer = Utility.readInt(inputBuffer, headerLength, headerLenghtPosition, terminalSymbolInterrupt);
				if (terminalSymbolInterrupt.ref) {
					
					// Check header length.
					if (headerLength.ref < 0 || headerLength.ref > MAXIMUM_HEADER_LENGHT) {
						throw new IllegalStateException("Header length exceeded maximum of " + MAXIMUM_HEADER_LENGHT + " bytes.");
					}
					inputReaderState = READ_HEADER;
				}
				break;
				
			case READ_HEADER:
				
				// TODO: <---DEBUG
				System.out.format("[H%d-%d]", headerLength.ref, headerPosition.ref);
				
				endOfInputBuffer = Utility.readUntil(inputBuffer, headerBuffer, TIMESTAMP_BUFFER_SIZE, Consoles.START_OF_TEXT, headerPosition, headerLength.ref, headerTerminalIndex, terminalSymbolInterrupt);
				
				// TODO: <---DEBUG
				System.out.format("[H_EOB%d]", endOfInputBuffer ? 1 : 0);
				
				if (terminalSymbolInterrupt.ref) {
					inputReaderState = READ_BODY_LENGTH;
					parseHeader(headerBuffer.ref, timestamp, color, statement);
				}
				break;
			
			case READ_BODY_LENGTH:

				endOfInputBuffer = Utility.readInt(inputBuffer, bodyLength, bodyLengthPosition, terminalSymbolInterrupt);				
				if (terminalSymbolInterrupt.ref) {
					
					// Check body length.
					if (bodyLength.ref < 0 || bodyLength.ref > MAXIMUM_BODY_LENGHT) {
						throw new IllegalStateException("Body length exceeded maximum of " + MAXIMUM_BODY_LENGHT + " bytes.");
					}
					inputReaderState = READ_BODY;
				}
				break;				
				
			case READ_BODY:
				
				// TODO: <---DEBUG
				System.out.format("[B%d-%d]", bodyLength.ref, bodyPosition.ref);
				
				endOfInputBuffer = Utility.readUntil(inputBuffer, bodyBuffer, LOG_MESSAGE_BUFFER_SIZE, Consoles.END_OF_TRANSMISSION, bodyPosition, bodyLength.ref, bodyTerminalIndex, terminalSymbolInterrupt);
				
				// TODO: <---DEBUG
				System.out.format("[B_EOB%d]", endOfInputBuffer ? 1 : 0);
				
				if (terminalSymbolInterrupt.ref) {
					
					inputReaderState = START_READING;
					String messageBody = getMessageBody(bodyBuffer.ref);
					
					MessageRecord messageRecord = new MessageRecord(timestamp.ref, color.ref, messageBody, statement.ref);
					
					// TODO<--- DEBUG Check message text.
					boolean success = messageRecord.messageText.matches("^[^\\|]+\\|Hello (computer )?world\\n");
					if (!success) {
						// Dump bytes.
						for (char theCharacter : messageRecord.messageText.toCharArray()) {
							System.out.format(" '%c' [%08X]  ", theCharacter, (int) theCharacter);
						}
						System.out.println();
					}
					
					// Take new record.
					cacheMessageRecord(messageRecord);
    				
					messagesCount++;
				}
				break;
			}
		}
		
		return messagesCount;
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
	private boolean parseHeader(ByteBuffer headerBuffer, Obj<LocalTime> outputTimestamp, Obj<Color> outputColor, Obj<String> outputStatement)
			throws Exception {
		
		// Prepare the header buffer for reading the XML length.
		headerBuffer.flip();
		
		// Get length of the nuffer.
		int arrayLength = headerBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents.
		headerBuffer.get(bytes);
		
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
			outputTimestamp.ref = LocalTime.parse(timestampString, Consoles.TIMESTAMP_FORMAT);
			
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
		
		// Reset the header buffer.
		headerBuffer.clear();
		
		// Return result.
		return success;
	}
	
	/**
	 * Get log message from the input buffer.
	 * @param logMessageBuffer
	 * @return
	 * @throws Exception 
	 */
	private String getMessageBody(ByteBuffer logMessageBuffer)
			throws Exception {
		
		// Prepare the log message buffer for reading.
		logMessageBuffer.flip();
		
		// Get length of the nuffer.
		int arrayLength = logMessageBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents.
		logMessageBuffer.get(bytes);
		
		// Convert bytes into UTF-8 encoded string.
		String logMessageText = new String(bytes, "UTF-8");
		
		// Reset the log message buffer.
		logMessageBuffer.clear();
		
		// Return result.
		return logMessageText;
	}
	
	/**
	 * Select or clear selection for this console.
	 * @param isSelected
	 */
	public void setSelected(boolean isSelected) {
		
		// Set border depending on selection.
		Border border = (isSelected ? selectionBorder : simpleBorder);
		scrollPane.setBorder(border);
	}
	
	/**
	 * Returns number of logged records.
	 * @return
	 */
	public synchronized int getRecordsCount() {
		
		int recordsCount = consoleRecords.size();
		return recordsCount;
	}
	
	/**
	 * Renew input buffer.
	 */
	public void renewInputBuffer() {
		
		inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
	}
}