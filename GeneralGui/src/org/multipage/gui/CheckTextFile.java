/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.*;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CheckTextFile extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Maximum data length.
	 */
	private static final long maximumDataLength = 64 * 1024;
	
	/**
	 * Parent frame.
	 */
	private JFrame frame;
	
	/**
	 * File reference.
	 */
	private File file;
	
	/**
	 * Selected encoding.
	 */
	private String currentEncoding;
	
	/**
	 * Dialog initializedCombo flag.
	 */
	private boolean initializedCombo = false;

	/**
	 * Is text file flag.
	 */
	private Boolean textFile;

	/**
	 * File encoding.
	 */
	private String fileEncoding;

	/**
	 * Confirmation flag.
	 */
	private boolean confirmed = false;

	/**
	 * Dialog components.
	 */
	private JLabel labelCharacterEncoding;
	private JComboBox comboxEncoding;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JButton buttonSelect;
	private JRadioButton radioTextFile;
	private JRadioButton radioBinaryFile;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton buttonCancel;

	/**
	 * Launch the dialog.
	 * @param file 
	 */
	public static boolean showDialog(JFrame frame, File file,
			Obj<Boolean> isText, Obj<String> encoding) {

		CheckTextFile dialog = new CheckTextFile(frame, file, encoding.ref);
		// Show modal dialog and get results.
		dialog.setVisible(true);
		isText.ref = dialog.textFile;
		encoding.ref = dialog.fileEncoding;
		
		return dialog.confirmed;
	}

	/**
	 * Create the dialog.
	 * @param frame 
	 * @param file 
	 * @param encoding 
	 */
	public CheckTextFile(JFrame frame, File file, String encoding) {
		super(frame, true);
		this.frame = frame;
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreate(file, encoding);
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.gui.textCheckTextFile");
		setBounds(100, 100, 666, 491);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelCharacterEncoding = new JLabel("org.multipage.gui.textCharacterEncodingOfFile");
		springLayout.putConstraint(SpringLayout.NORTH, labelCharacterEncoding, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelCharacterEncoding, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelCharacterEncoding, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelCharacterEncoding);
		
		comboxEncoding = new JComboBox();
		comboxEncoding.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onComboBoxAction(e);
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, comboxEncoding, 6, SpringLayout.SOUTH, labelCharacterEncoding);
		springLayout.putConstraint(SpringLayout.WEST, comboxEncoding, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboxEncoding, 203, SpringLayout.WEST, getContentPane());
		getContentPane().add(comboxEncoding);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, comboxEncoding);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -41, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
		textPane.setToolTipText("org.multipage.gui.tooltipCheckTextUsingGivenEncoding");
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		buttonSelect = new JButton("textSelect");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSelect, 6, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, buttonSelect, -107, SpringLayout.EAST, getContentPane());
		buttonSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelect();
			}
		});
		buttonSelect.setMargin(new Insets(0, 0, 0, 0));
		buttonSelect.setMaximumSize(new Dimension(80, 25));
		buttonSelect.setMinimumSize(new Dimension(80, 25));
		buttonSelect.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonSelect);
		
		radioTextFile = new JRadioButton("org.multipage.gui.textItIsTextFile");
		radioTextFile.setForeground(Color.RED);
		radioTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsTextFile();
			}
		});
		radioTextFile.setFont(new Font("Tahoma", Font.BOLD, 12));
		buttonGroup.add(radioTextFile);
		springLayout.putConstraint(SpringLayout.WEST, radioTextFile, 46, SpringLayout.EAST, comboxEncoding);
		springLayout.putConstraint(SpringLayout.SOUTH, radioTextFile, 0, SpringLayout.SOUTH, comboxEncoding);
		getContentPane().add(radioTextFile);
		
		radioBinaryFile = new JRadioButton("org.multipage.gui.textItIsBinaryFile");
		radioBinaryFile.setForeground(Color.RED);
		radioBinaryFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsBinaryFile();
			}
		});
		radioBinaryFile.setFont(new Font("Tahoma", Font.BOLD, 12));
		buttonGroup.add(radioBinaryFile);
		springLayout.putConstraint(SpringLayout.WEST, radioBinaryFile, 6, SpringLayout.EAST, radioTextFile);
		springLayout.putConstraint(SpringLayout.SOUTH, radioBinaryFile, 0, SpringLayout.SOUTH, comboxEncoding);
		getContentPane().add(radioBinaryFile);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setMinimumSize(new Dimension(80, 25));
		buttonCancel.setMaximumSize(new Dimension(80, 25));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonCancel, 6, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, labelCharacterEncoding);
		getContentPane().add(buttonCancel);
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirmed = false;
		
		dispose();
	}

	/**
	 * On is binary file.
	 */
	protected void onIsBinaryFile() {

		setEnableTextControls(false);
	}

	/**
	 * On is text file.
	 */
	protected void onIsTextFile() {

		setEnableTextControls(true);
	}

	/**
	 * Enable / disable text controls.
	 * @param enable
	 */
	private void setEnableTextControls(boolean enable) {

		comboxEncoding.setEnabled(enable);
		labelCharacterEncoding.setEnabled(enable);
		textPane.setEnabled(enable);
	}

	/**
	 * Post creation.
	 * @param encoding 
	 * @param file2 
	 */
	private void postCreate(File file, String encoding) {
		
		this.file = file;
		
		// Center dialog.
		Utility.centerOnScreen(this);
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Load encodings.
		loadEncodings(encoding);
		
		initializedCombo = true;
		
		// Load file.
		loadFile(true);

		// Initialize selection.
		radioTextFile.setSelected(true);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localizeTooltip(textPane);
		Utility.localize(buttonSelect);
		Utility.localize(buttonCancel);
		Utility.localize(radioTextFile);
		Utility.localize(radioBinaryFile);
		// Set label text.
		labelCharacterEncoding.setText(
				String.format(
						Resources.getString(labelCharacterEncoding.getText()),
						file.getName()));
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		buttonSelect.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Load encodings.
	 * @param encoding 
	 */
	private void loadEncodings(String encoding) {

		// Load combobox.
		Utility.loadEncodings(comboxEncoding, encoding);
	}

	/**
	 * Load file content.
	 * @param showTooLongMessage 
	 */
	private void loadFile(boolean showTooLongMessage) {

		// If the dialog is not initializedCombo, exit the method.
		if (!initializedCombo) {
			return;
		}
		
		// Get encoding.
		final String encoding = (String) comboxEncoding.getSelectedItem();
		
		// Get file length.
		long fileLength = file.length();
		final long dataLength;
		
		// If the file is too long, inform user.
		if (fileLength > maximumDataLength) {
			
			// If to show message.
			if (showTooLongMessage) {
				Utility.show2(this, String.format(
						Resources.getString("org.multipage.gui.messageFileIsTooLongReadingFirstPart"),
						fileLength,
						maximumDataLength));
			}
			dataLength = maximumDataLength;
		}
		else {
			dataLength = fileLength;
		}
		
		// Create progress dialog.
		ProgressDialog<StringBuilder> dialog = new ProgressDialog<StringBuilder>(
				frame,
				Resources.getString("org.multipage.gui.textLoadingTextFile"),
				String.format(Resources.getString("org.multipage.gui.textLoadingTextFileToCheck"),
						file.getName()));
		
		// Run progress dialog.
		ProgressResult progressResult = dialog.execute(new SwingWorkerHelper<StringBuilder>() {
			// Thread.
			@Override
			protected StringBuilder doBackgroundProcess() throws Exception {
			
				// Create string builder.
				StringBuilder stringBuilder = new StringBuilder();

				long totalCharactersRead = 0;

				// Create input stream.
				InputStreamReader reader = new InputStreamReader(
						new FileInputStream(file), encoding);
				
				char [] characterBuffer = new char [50];
				int charactersRead;
				
				// Try to read the stream.
				while ((charactersRead = reader.read(characterBuffer)) != -1) {

					// On cancel exit the thread.
					if (isScheduledCancel()) {
						reader.close();
						throw new Exception();
					}
					// Append data to the string.
					stringBuilder.append(characterBuffer, 0, charactersRead);
					
					// Set counter.
					totalCharactersRead += charactersRead;
					if (totalCharactersRead > dataLength) {
						break;
					}
					
					long progressPercent = 100 * totalCharactersRead / dataLength;
					
					// Set progress.
					setProgressBar((int) progressPercent);
				}
				
				setProgressBar(100);

				reader.close();
				return stringBuilder;
			}
		});
		
		// Check result.
		if (progressResult == ProgressResult.OK) {
			
			// Show wait cursor.
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			// Get thread output.
			StringBuilder stringBuilder = dialog.getOutput();
			
			// Get current view port position.
			Point oldViewportPosition = Utility.getScrollPosition(scrollPane);
			
			// Begin wait.
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			// Show the text.
			textPane.setText(stringBuilder.toString());
			
			// Scroll to the old position.
			Utility.scrollToPosition(scrollPane, oldViewportPosition);
			
			// Show default cursor.
			setCursor(Cursor.getDefaultCursor());
		}
		else if (progressResult == ProgressResult.EXECUTION_EXCEPTION) {
			// Show exception.
			Utility.show2(dialog.getException().getLocalizedMessage());
		}
	}

	/**
	 * On combo box action.
	 * @param e
	 */
	protected void onComboBoxAction(ActionEvent e) {

		// Get source.
		Object source = e.getSource();
		// Check it.
		if (!(source instanceof JComboBox)) {
			return;
		}
		// Convert the reference type.
		JComboBox comboBox = (JComboBox) source;
		String newEncoding = (String) comboBox.getSelectedItem();
		
		// If the encoding changes, reload the file.
		if (!newEncoding.equals(currentEncoding)) {
			
			currentEncoding = newEncoding;
			// Load file.
			loadFile(false);
		}
	}

	/**
	 * On select.
	 */
	protected void onSelect() {
		
		// Set outputs.
		textFile = radioTextFile.isSelected();
		if (textFile) {
			fileEncoding = (String) comboxEncoding.getSelectedItem();
		}
		else {
			fileEncoding = "BINARY_FILE";
		}
		
		confirmed = true;
		
		// Close the dialog.
		dispose();
	}
}
