/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-08-2023
 *
 */
package org.multipage.gui;

import java.awt.Dimension;
import java.time.format.DateTimeFormatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * Panel with information about selected console.
 * @author vakol
 *
 */
public class ConsoleProperties extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Timestamp spanning format.
	 */
	private static final DateTimeFormatter TIMESTAMP_SPANNING_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	
	/**
	 * Components.
	 */
	private TextFieldEx textConsoleName;
	private TextFieldEx textInputSocket;
	private TextFieldEx textMinimumTimestamp;
	private TextFieldEx textMaximumTimestamp;
	private TextFieldEx textMessageCount;

	/**
	 * Create the panel.
	 */
	public ConsoleProperties() {

		initComponents();
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(130, 300));
		setPreferredSize(new Dimension(130, 300));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		JLabel labelConsoleName = new JLabel("Console name:");
		springLayout.putConstraint(SpringLayout.NORTH, labelConsoleName, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelConsoleName, 6, SpringLayout.WEST, this);
		add(labelConsoleName);
		
		textConsoleName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textConsoleName, 6, SpringLayout.WEST, this);
		textConsoleName.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textConsoleName, 3, SpringLayout.SOUTH, labelConsoleName);
		springLayout.putConstraint(SpringLayout.EAST, textConsoleName, -6, SpringLayout.EAST, this);
		add(textConsoleName);
		textConsoleName.setColumns(10);
		
		JLabel labelnputSocket = new JLabel("Input socket:");
		springLayout.putConstraint(SpringLayout.NORTH, labelnputSocket, 6, SpringLayout.SOUTH, textConsoleName);
		springLayout.putConstraint(SpringLayout.WEST, labelnputSocket, 6, SpringLayout.WEST, this);
		add(labelnputSocket);
		
		textInputSocket = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textInputSocket, 6, SpringLayout.WEST, this);
		textInputSocket.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textInputSocket, 3, SpringLayout.SOUTH, labelnputSocket);
		springLayout.putConstraint(SpringLayout.EAST, textInputSocket, -6, SpringLayout.EAST, this);
		add(textInputSocket);
		textInputSocket.setColumns(10);
		
		JLabel labelMinimumTimestamp = new JLabel("Minimum timestamp:");
		springLayout.putConstraint(SpringLayout.NORTH, labelMinimumTimestamp, 6, SpringLayout.SOUTH, textInputSocket);
		springLayout.putConstraint(SpringLayout.WEST, labelMinimumTimestamp, 6, SpringLayout.WEST, this);
		add(labelMinimumTimestamp);
		
		textMinimumTimestamp = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textMinimumTimestamp, 6, SpringLayout.WEST, this);
		textMinimumTimestamp.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textMinimumTimestamp, 3, SpringLayout.SOUTH, labelMinimumTimestamp);
		springLayout.putConstraint(SpringLayout.EAST, textMinimumTimestamp, -6, SpringLayout.EAST, this);
		add(textMinimumTimestamp);
		textMinimumTimestamp.setColumns(10);
		
		JLabel labelMaximumTimestamp = new JLabel("Maximum timestamp:");
		springLayout.putConstraint(SpringLayout.NORTH, labelMaximumTimestamp, 6, SpringLayout.SOUTH, textMinimumTimestamp);
		springLayout.putConstraint(SpringLayout.WEST, labelMaximumTimestamp, 6, SpringLayout.WEST, this);
		add(labelMaximumTimestamp);
		
		textMaximumTimestamp = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textMaximumTimestamp, 6, SpringLayout.WEST, this);
		textMaximumTimestamp.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textMaximumTimestamp, 3, SpringLayout.SOUTH, labelMaximumTimestamp);
		springLayout.putConstraint(SpringLayout.EAST, textMaximumTimestamp, -6, SpringLayout.EAST, this);
		add(textMaximumTimestamp);
		textMaximumTimestamp.setColumns(10);
		
		JLabel labelMessageCount = new JLabel("Number of messages:");
		springLayout.putConstraint(SpringLayout.NORTH, labelMessageCount, 6, SpringLayout.SOUTH, textMaximumTimestamp);
		springLayout.putConstraint(SpringLayout.WEST, labelMessageCount, 6, SpringLayout.WEST, this);
		add(labelMessageCount);
		
		textMessageCount = new TextFieldEx();
		textMessageCount.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textMessageCount, 3, SpringLayout.SOUTH, labelMessageCount);
		springLayout.putConstraint(SpringLayout.WEST, textMessageCount, 6, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, textMessageCount, -6, SpringLayout.EAST, this);
		add(textMessageCount);
		textMessageCount.setColumns(10);
	}
	
	/**
	 * Display console name.
	 * @param name
	 */
	public void displayProperties(LogConsole console) {
		
		// Display console name.
		textConsoleName.setText(console.name);
		
		// Display input socket address.
		String socketAddressText = null;
		if (console.socketAddress != null) {
			socketAddressText = console.socketAddress.getHostString() + ":" + console.socketAddress.getPort();
		}
		else {
			socketAddressText = "unknown";
		}
		textInputSocket.setText(socketAddressText);
		
		// Display timestamp spanning.
		String timestampText = null;
		if (console.minimumTimestamp != null) {
			timestampText = console.minimumTimestamp.format(TIMESTAMP_SPANNING_FORMAT);
		}
		else {
			timestampText = "unknown";
		}
		textMinimumTimestamp.setText(timestampText);
		
		timestampText = null;
		if (console.maximumTimestamp != null) {
			timestampText = console.maximumTimestamp.format(TIMESTAMP_SPANNING_FORMAT);
		}
		else {
			timestampText = "unknown";
		}
		textMaximumTimestamp.setText(timestampText);
		
		// Display the number of logged records.
		int recordsCount = console.getRecordsCount();
		String recordsCountText = String.valueOf(recordsCount);
		textMessageCount.setText(recordsCountText);
	}
		
	/**
	 * Reset console components.
	 */
	public void resetComponents() {
		
		textConsoleName.setText("");
	}
}
