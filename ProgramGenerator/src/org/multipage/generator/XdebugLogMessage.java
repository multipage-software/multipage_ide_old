package org.multipage.generator;

import java.util.LinkedList;

import javax.swing.JTextPane;

import org.multipage.gui.Utility;

/**
 * Class for log items.
 */
class XdebugLogMessage extends LoggingDialog.LoggedMessage {
	
	/**
	 * List of log messages.
	 */
	private static LinkedList<XdebugLogMessage> listLoggedMessages = new LinkedList<XdebugLogMessage>();
	
	/**
	 * Filter string.
	 */
	private static String filterString = "*";
	
	/**
	 * Filter flags.
	 */
	private static boolean caseSensitive = false;
	private static boolean wholeWords = false;
	private static boolean exactMatch = false;
	
	/**
	 * Constructor.
	 * @param message
	 */
	public XdebugLogMessage(String message) {
		super(message);
	}
	
	/**
	 * Add new log message.
	 * @param message
	 */
	public static void addLogMessage(String message) {
		
		XdebugLogMessage logMessage = new XdebugLogMessage(message);
		listLoggedMessages.addLast(logMessage);
	}
	
	/**
	 * Display HTML log.
	 * @param textPane
	 */
	public static void displayHtmlLog(JTextPane textPane) {
		
		String logContent = "";
		
		for (XdebugLogMessage logMessage : listLoggedMessages) {
			
			String messageText = logMessage.getText();
			
			// Filter messages.
			if (!filter(messageText)) {
				continue;
			}
			
			// Append message text.
			logContent += messageText + "<br/>";
		}
		
		// Wrap content with HTML tags.
		logContent = String.format("<html>%s</html>", logContent);
		textPane.setText(logContent);
	}
	
	/**
	 * Returns false if the message is filtered or true if the message passes.
	 * @param messageText
	 * @return
	 */
	private static boolean filter(String messageText) {

		boolean matches = Utility.matches(messageText, filterString, caseSensitive, wholeWords, exactMatch);
		return matches;
	}

	/**
	 * Set filter and display filtered log messages.
	 * @param filterString
	 * @param caseSensitive
	 * @param wholeWords
	 * @param exactMatch
	 */
	public static void setFulltextFilter(String filterString, boolean caseSensitive, boolean wholeWords, boolean exactMatch) {
		
		XdebugLogMessage.filterString = !filterString.isEmpty() ?  filterString : "*";
		XdebugLogMessage.caseSensitive = caseSensitive;
		XdebugLogMessage.wholeWords = wholeWords;
		XdebugLogMessage.exactMatch = exactMatch;
	}
}