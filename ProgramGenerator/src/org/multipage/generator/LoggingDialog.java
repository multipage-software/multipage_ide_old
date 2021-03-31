/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 24-03-2021
 *
 */
package org.multipage.generator;

import java.awt.Component;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

/**
 * 
 * @author vakol
 *
 */
public class LoggingDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Logged messages.
	 */
	private static LinkedList<LoggedMessage> messages = new LinkedList<LoggedMessage>();
	
	/**
	 * Singleton dialog object.
	 */
	private static LoggingDialog dialog = null;

	/**
	 * Message limit.
	 */
	private static final int messageLimit = 20;
	
	/**
	 * Text area.
	 */
	protected JTextArea textArea;
	
	/**
	 * Constructor.
	 */
	static {
		
		dialog = new LoggingDialog();
	}
	
	/**
	 * Create the dialog.
	 */
	public LoggingDialog() {

		initComponents();
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("Logging dialog");
		setBounds(100, 100, 557, 471);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
	}
	
	/**
	 * Log message.
	 * @param messageText
	 */
	public static void log(String messageText) {
		
		// Add new message.
		LoggedMessage message = new LoggedMessage(messageText);
		messages.add(message);
		
		// Message limit.
		int extraMessagesCount = messages.size() - messageLimit;

		// Remove extra messages from the list beginning.
		while (extraMessagesCount-- > 0) {
			messages.removeFirst();
		}
		
		// Compile messages.
		compileMessages();
	}
	
	/**
	 * Compile messages.
	 */
	private static void compileMessages() {
		
		String resultingText = "";
		
		for (LoggedMessage message : messages) {
			resultingText += message.getText() + '\n';
		}
		
		dialog.textArea.setText(resultingText);
	}
	
	/**
	 * Show dialog.
	 * @param parent
	 */
	public static void showDialog(Component parent) {
		
		dialog.setVisible(true);
	}
}
