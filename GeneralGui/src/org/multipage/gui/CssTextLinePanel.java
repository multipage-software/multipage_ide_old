/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author
 *
 */
public class CssTextLinePanel extends InsertPanel implements StringValueEditor {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 500, 330);
		boundsSet = false;
	}

	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		boundsSet = true;
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Initial string.
	 */
	private String initialString;
	
	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelText;
	private JTextField textField;
	private JButton buttonEscapeQuotes;
	private JButton buttonUnescapeQuotes;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssTextLinePanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelText = new JLabel("org.multipage.gui.textInsertText");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelText, 10, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelText, 10, SpringLayout.WEST, this);
		add(labelText);
		
		
		textField = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textField, 6, SpringLayout.SOUTH, labelText);
		sl_panelMain.putConstraint(SpringLayout.WEST, textField, 10, SpringLayout.WEST, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, textField, -10, SpringLayout.EAST, this);
		add(textField);
		textField.setColumns(10);
		
		buttonEscapeQuotes = new JButton("org.multipage.gui.textEscapeQuotes");
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonEscapeQuotes, 6, SpringLayout.SOUTH, textField);
		buttonEscapeQuotes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEscapeQuotes();
			}
		});
		buttonEscapeQuotes.setMargin(new Insets(0, 0, 0, 0));
		buttonEscapeQuotes.setPreferredSize(new Dimension(100, 25));
		buttonEscapeQuotes.setSelected(true);
		add(buttonEscapeQuotes);
		
		buttonUnescapeQuotes = new JButton("org.multipage.gui.textUnescapeQuotes");
		buttonUnescapeQuotes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUnescapeQuotes();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonEscapeQuotes, -6, SpringLayout.WEST, buttonUnescapeQuotes);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonUnescapeQuotes, 6, SpringLayout.SOUTH, textField);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonUnescapeQuotes, 0, SpringLayout.EAST, textField);
		buttonUnescapeQuotes.setSelected(true);
		buttonUnescapeQuotes.setPreferredSize(new Dimension(100, 25));
		buttonUnescapeQuotes.setMargin(new Insets(0, 0, 0, 0));
		add(buttonUnescapeQuotes);
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
	}

	/**
	 * On escape quotes.
	 */
	protected void onEscapeQuotes() {
		
		String text = textField.getText();
		text = text.replace("\"", "\\\"");
		
		textField.setText(text);
	}
	
	/**
	 * On unescape quotes.
	 */
	protected void onUnescapeQuotes() {
		
		String text = textField.getText();
		text = text.replace("\\\"", "\"");
		
		textField.setText(text);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String specification = textField.getText();
		
		return specification;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			textField.setText(initialString);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelText);
		Utility.localize(buttonEscapeQuotes);
		Utility.localize(buttonUnescapeQuotes);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssUrlBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getResultText()
	 */
	@Override
	public String getResultText() {
		
		return getSpecification();
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getContainerDialogBounds()
	 */
	@Override
	public Rectangle getContainerDialogBounds() {
		
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		CssTextLinePanel.bounds = bounds;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#boundsSet()
	 */
	@Override
	public boolean isBoundsSet() {

		return boundsSet;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setBoundsSet(boolean)
	 */
	@Override
	public void setBoundsSet(boolean set) {
		
		boundsSet = set;
	}

	/**
	 * Get component.
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssTextLine;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
