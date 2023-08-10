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
	 * Incomming buffers' sizes.
	 */
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int TIMESTAMP_BUFFER_SIZE = 16;
	private static final int LOG_MESSAGE_BUFFER_SIZE = INPUT_BUFFER_SIZE;
	
	/**
	 * Input reader states.
	 */
	private static final int READ_TIMESTAMP = 0;
	private static final int READ_LOG_MESSAGE = 1;
	
	/**
	 * Panel borders.
	 */
	private static Border selectionBorder = null;
	private static Border simpleBorder = null;
	
	/**
	 * Constructor.
	 */
	static {
		
		// Create panel borders.
		selectionBorder = BorderFactory.createLineBorder(Color.RED);
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
	public Obj<ByteBuffer> timestampBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(TIMESTAMP_BUFFER_SIZE));
	public Obj<ByteBuffer> logMessageBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(LOG_MESSAGE_BUFFER_SIZE));

	
	private int inputReaderState = READ_TIMESTAMP;
	
	/**
	 * Last read timestamp.
	 */
	private LocalTime readTimestamp = null;
	
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
			
			case READ_TIMESTAMP:
				endOfReading = Utility.readUntil(inputBuffer, timestampBuffer, TIMESTAMP_BUFFER_SIZE, Consoles.DIVIDER_SYMBOL, terminated);
				if (terminated.ref) {
					inputReaderState = READ_LOG_MESSAGE;
					readTimestamp = getTimestamp(timestampBuffer.ref);
				}
				break;
				
			case READ_LOG_MESSAGE:
				endOfReading = Utility.readUntil(inputBuffer, logMessageBuffer, LOG_MESSAGE_BUFFER_SIZE, Consoles.TERMINAL_SYMBOL, terminated);
				if (terminated.ref) {
					
					inputReaderState = READ_TIMESTAMP;
					String logMessageText = getLogMessage(logMessageBuffer.ref);
					
					MessageRecord messageRecord = new MessageRecord(readTimestamp, logMessageText);
					logMessageLambda.accept(messageRecord);
					
					messageAccepted = true;
				}
				break;
			}
		}
		
		return messageAccepted;
	}

	/**
	 * Get timestamp from the input buffer.
	 * @param timestampBuffer
	 * @return
	 * @throws Exception 
	 */
	private LocalTime getTimestamp(ByteBuffer timestampBuffer)
			throws Exception {
		
		// Prepare the timestamp buffer for reading the XML length.
		timestampBuffer.flip();
		
		// Get length of the nuffer.
		int arrayLength = timestampBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents.
		timestampBuffer.get(bytes);
		
		// Convert bytes into UTF-8 encoded string.
		String timstampText = new String(bytes, "UTF-8");
		
		// Convert text to timstamp object.
		LocalTime timestamp = LocalTime.parse(timstampText, Consoles.TIMESTAMP_FORMAT);
		
		// Reset the timestamp buffer.
		timestampBuffer.clear();
		
		// Return result.
		return timestamp;
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