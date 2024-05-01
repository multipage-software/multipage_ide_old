package org.multipage.gui;

import java.awt.Color;
import java.time.LocalTime;

/**
 * Message record class.
 */
public final class LogMessageRecord {
	
	/**
	 * Flag that can switch between message timestamps and console timestamps.
	 */
	public static boolean useConsoleTimeStamps = false;
	
	/**
	 * Timestamp.
	 */
	public LocalTime timestamp = null;
	
	/**
	 * Record text.
	 */
	public String messageText = null;
	
	/**
	 * Displayed message color.
	 */
	public Color color = Color.BLACK;
	
	/**
	 * Console statement.
	 */
	public String statment = null;
	
	/**
	 * Time when the message was written into the console
	 */
	LocalTime consoleWriteTime = null;
	
	/**
	 * Constructor.
	 * 
	 * @param timestamp
	 * @param color 
	 * @param messageString
	 * @param statement 
	 */
	public LogMessageRecord(LocalTime timestamp, Color color, String messageString, String statement) {
		
		this.timestamp = timestamp;
		this.messageText = messageString;
		this.color = color;
		this.statment = statement;
	}
	
	/**
	 * Get message text with maximum allowed characters.
	 * @param maximumCharacters
	 * @return
	 */
	public String getMessageText(int maximumCharacters) {
		
		if (messageText == null || messageText.isEmpty()) {
			return "";
		}
		
		String resultMessageString = messageText.trim();
		int length = resultMessageString.length();
		
		// Trim maximum haracters.
		if (maximumCharacters > length) {
			maximumCharacters = length;
		}
		
		// Get specified number of characters from the message beginning.
		resultMessageString = resultMessageString.substring(0, maximumCharacters);
		return resultMessageString;
	}
	
	/**
	 * Get string representation of the log record.
	 */
	@Override
	public String toString() {
		
		return (useConsoleTimeStamps ? consoleWriteTime : timestamp) + messageText;
	}
}