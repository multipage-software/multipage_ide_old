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
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.function.Consumer;
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
import org.multipage.util.j;

/**
 * Console class.
 * @author vakol
 */
class LogConsole {
	
	/**
	 * Incomming buffers' sizes.
	 */
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int TIMESTAMP_BUFFER_SIZE = 16;
	private static final int LOG_MESSAGE_BUFFER_SIZE = INPUT_BUFFER_SIZE;
	
	/**
	 * Input reader states.
	 */
	private static final int READ_HEADER = 0;
	private static final int READ_LOG_MESSAGE = 1;
	
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
	 * Minimum and maximum timestamps.
	 */
	protected LocalTime minimumTimestamp = null;
	protected LocalTime maximumTimestamp = null;
	
	/**
	 * Console port number.
	 */
	protected int port = -1;
	
	/**
	 * Console input buffers.
	 */
	protected ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
	public Obj<ByteBuffer> headerBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(TIMESTAMP_BUFFER_SIZE));
	public Obj<ByteBuffer> logMessageBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(LOG_MESSAGE_BUFFER_SIZE));

	
	private int inputReaderState = READ_HEADER;
	
	/**
	 * Last read timestamp and color.
	 */
	private Obj<LocalTime> readTimestamp = new Obj<LocalTime>(null);
	private Obj<Color> readColor = new Obj<Color>(null);
	private Obj<String> readStatement = new Obj<String>(null);
	
	/**
	 * Message record list that maps time axis to the records.
	 */
	protected LinkedList<MessageRecord> consoleRecords = new LinkedList<>();
	
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
	 * @param logMessage
	 * @return
	 */
	public boolean runStatement(MessageRecord logMessage) {
		
		// On clear console.
		if ("CLEAR".equalsIgnoreCase(logMessage.statment)) {
			
			// Clear console contents.
			clear();
			return true;
		}
		return false;
	}

	/**
	 * Add new message record.
	 * 
	 * @param messageRecord
	 */
	public void addMessageRecord(MessageRecord messageRecord) {

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

		// Create new message record and put it into the message map.
		synchronized (consoleRecords) {
			consoleRecords.add(messageRecord);
		}
	}

	/**
	 * Clear console content.
	 */
	public void clear() {

		consoleRecords.clear();
		maximumTimestamp = null;
		minimumTimestamp = null;
		textPane.setText("");
	}
	
	/**
	 * Read log messages from the input buffer.
	 * @param logMessageLambda
	 * @throws Exception 
	 */
	public boolean readLogMessages(Consumer<MessageRecord> logMessageLambda)
			throws Exception {
		
		// Prepare input buffer for reading.
		inputBuffer.flip();
		
		// If there are no remaining bytes in the input buffer, return false value.
		if (!inputBuffer.hasRemaining()) {
			return false;
		}
		
		boolean messageAccepted = false;
		
		// Read until end of input buffer.
		boolean endOfReading = false;
		while (!endOfReading) {
			
			Obj<Boolean> terminated = new Obj<Boolean>(false);
			
			// Determine protocol state from input byte value and invoke related action.
			switch (inputReaderState) {
			
			case READ_HEADER:
				endOfReading = Utility.readUntil(inputBuffer, headerBuffer, TIMESTAMP_BUFFER_SIZE, Consoles.DIVIDER_SYMBOL, terminated);
				if (terminated.ref) {
					inputReaderState = READ_LOG_MESSAGE;
					parseHeaderBytes(headerBuffer.ref, readTimestamp, readColor, readStatement);
				}
				break;
				
			case READ_LOG_MESSAGE:
				endOfReading = Utility.readUntil(inputBuffer, logMessageBuffer, LOG_MESSAGE_BUFFER_SIZE, Consoles.TERMINAL_SYMBOL, terminated);
				if (terminated.ref) {
					
					inputReaderState = READ_HEADER;
					String logMessageText = getLogMessage(logMessageBuffer.ref);
					
					MessageRecord messageRecord = new MessageRecord(readTimestamp.ref, readColor.ref, logMessageText, readStatement.ref);
					logMessageLambda.accept(messageRecord);
					
					messageAccepted = true;
				}
				break;
			}
		}
		
		return messageAccepted;
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
	private boolean parseHeaderBytes(ByteBuffer headerBuffer, Obj<LocalTime> outputTimestamp, Obj<Color> outputColor, Obj<String> outputStatement)
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
	private String getLogMessage(ByteBuffer logMessageBuffer)
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
	public int getRecordsCount() {
		
		int recordsCount = consoleRecords.size();
		return recordsCount;
	}
}