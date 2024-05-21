/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 04-04-2024
 *
 */
package org.multipage.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;

import org.multipage.util.Obj;

/**
 * Log console object.
 * @author vakol
 */
public class LogConsole {

	/**
	 * Maximum number of records.
	 */
	private static final int MAXIMUM_RECORDS = 300;
	
	/**
	 * Panel borders.
	 */
	private static Border selectionBorder = null;
	private static Border simpleBorder = null;
	
	/**
	 * Static constructor.
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
	 * Socket chnnel port number.
	 */
	private int port = -1;
	
	/**
	 * Input packet channel.
	 */
	private PacketChannel packetChannel = null;
	
	/**
	 * Minimum and maximum timestamps.
	 */
	protected LocalTime minimumTimestamp = null;
	protected LocalTime maximumTimestamp = null;
	
	/**
	 * Message record list that maps time axis to the records.
	 */
	protected ConcurrentLinkedQueue<LogMessageRecord> consoleRecords = new ConcurrentLinkedQueue<>();
	
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
	 * Constructor.
	 * @param consoleName
	 * @param splitPane
	 * @param port
	 * @wbp.parser.entryPoint
	 */
	public LogConsole(String consoleName, JSplitPane splitPane, int port)
			throws Exception {
		
		this.name = consoleName;
		this.splitPane = splitPane;
		this.port = port;
		
		// Create panel components.
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

		// Reset selection.
		setSelected(false); //$hide$
	}
	
	/**
	 * Open input socket channel.
	 * @throws IOException 
	 */
	public void openInputSocket()
			throws Exception {
		
		packetChannel = new PacketChannel() {
			
			// Create log packet reader when accepting socket connection.
			@Override
			protected PacketSession onStartListening(AsynchronousSocketChannel client) {
				LogReader logReader = new LogReader(LogConsole.this);
				return logReader;
			}
		};
		packetChannel.listen("localhost", port);
	}
	
	/**
	 * Get socket address.
	 * @return
	 */
	public InetSocketAddress getSocketAddress() {
		
		InetSocketAddress socketAddress = packetChannel.getSocketAddress();
		return socketAddress;
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
	 * Try to run console statment.
	 * @param messageRecord
	 * @return
	 */
	public boolean runStatement(LogMessageRecord messageRecord) {
		
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
	public synchronized void cacheMessageRecord(LogMessageRecord messageRecord) {
		
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
				consoleRecords.poll();
			}
		}
		
		// Append new record to the end of the list.
		consoleRecords.add(messageRecord);
		
		update();
	}
	
	/**
	 * Update the text panel contents.
	 */
	protected synchronized void update() {
		
		// Check timestamps for null values.
		if (maximumTimestamp == null || minimumTimestamp == null) {
			return;
		}

		// Compile text contents.
		Obj<String> contents = new Obj<String>("<html>");
		
		// TODO: <---FIX Concurrent modification error.
		consoleRecords.forEach(messageRecord -> {
			
			String messageText = Utility.htmlSpecialChars(messageRecord.messageText);
			String colorString = Utility.getCssColor(messageRecord.color);
			String messageHtml = String.format("<div style='color: %s; font-family: Consolas; font-size: 14pt; white-space:nowrap;'>%s</div>", colorString, messageText);
			contents.ref += messageHtml;
		});
		
		// Set text of the text view.
		textPane.setText(contents.ref);
		
		// Move caret to the end of the view.
		int endPosition = textPane.getDocument().getLength();
		textPane.setCaretPosition(endPosition);
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
	 * Get string representation of console.
	 */
	@Override
	public String toString() {
		if (name == null) {
			return "null " + super.toString();
		}
		return name + ' ' + super.toString();
	}
}
