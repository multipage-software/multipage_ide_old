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
import java.util.regex.Matcher;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author
 *
 */
public class CssQuotesPanel extends InsertPanel implements StringValueEditor {

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
		
		bounds = new Rectangle(0, 0, 469, 450);
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
	
	/**
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

	/**
	 * Start setting controls.
	 */
	public void startSettingControls() {
		
		settingControls = true;
	}

	/**
	 * Stop setting controls.
	 */
	public void stopSettingControls() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				settingControls = false;
			}
		});
	}
	
	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelLeftQuote;
	private JTextField textLeftQuote;
	private JLabel labelRightQuote;
	private JTextField textRightQuote;
	private JComboBox comboLeftQuote;
	private JComboBox comboRightQuote;
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssQuotesPanel(String initialString) {

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

		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);

		
		labelLeftQuote = new JLabel("org.multipage.gui.textCssLeftQuote");
		springLayout.putConstraint(SpringLayout.NORTH, labelLeftQuote, 41, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelLeftQuote, 42, SpringLayout.WEST, this);
		add(labelLeftQuote);
		
		textLeftQuote = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textLeftQuote, -3, SpringLayout.NORTH, labelLeftQuote);
		add(textLeftQuote);
		textLeftQuote.setColumns(10);
		
		labelRightQuote = new JLabel("org.multipage.gui.textCssRightQuote");
		springLayout.putConstraint(SpringLayout.NORTH, labelRightQuote, 0, SpringLayout.NORTH, labelLeftQuote);
		springLayout.putConstraint(SpringLayout.WEST, labelRightQuote, 16, SpringLayout.EAST, textLeftQuote);
		add(labelRightQuote);
		
		textRightQuote = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textRightQuote, -3, SpringLayout.NORTH, labelLeftQuote);
		add(textRightQuote);
		textRightQuote.setColumns(10);
		
		comboLeftQuote = new JComboBox();
		comboLeftQuote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLeftComboChanged();
			}
		});
		comboLeftQuote.setFont(new Font("Tahoma", Font.BOLD, 16));
		springLayout.putConstraint(SpringLayout.WEST, textLeftQuote, 6, SpringLayout.EAST, comboLeftQuote);
		springLayout.putConstraint(SpringLayout.NORTH, comboLeftQuote, -3, SpringLayout.NORTH, labelLeftQuote);
		springLayout.putConstraint(SpringLayout.WEST, comboLeftQuote, 6, SpringLayout.EAST, labelLeftQuote);
		comboLeftQuote.setPreferredSize(new Dimension(40, 20));
		add(comboLeftQuote);
		
		comboRightQuote = new JComboBox();
		comboRightQuote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRightComboChanged();
			}
		});
		comboRightQuote.setFont(new Font("Tahoma", Font.BOLD, 16));
		springLayout.putConstraint(SpringLayout.WEST, textRightQuote, 6, SpringLayout.EAST, comboRightQuote);
		springLayout.putConstraint(SpringLayout.NORTH, comboRightQuote, -3, SpringLayout.NORTH, labelLeftQuote);
		springLayout.putConstraint(SpringLayout.WEST, comboRightQuote, 5, SpringLayout.EAST, labelRightQuote);
		comboRightQuote.setPreferredSize(new Dimension(40, 20));
		add(comboRightQuote);
	}

	/**
	 * On right combo changed.
	 */
	protected void onRightComboChanged() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textRightQuote.setText("");
		
		stopSettingControls();
	}

	/**
	 * On left combo box changed.
	 */
	protected void onLeftComboChanged() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textLeftQuote.setText("");
		
		stopSettingControls();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		setFromInitialString();
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
		
		loadComboBoxes();
		
		loadDialog();
		setListeners();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		Utility.setTextChangeListener(textLeftQuote, () -> {
			
			if (settingControls) {
				return;
			}
			startSettingControls();
			
			comboLeftQuote.setSelectedIndex(0);
			
			stopSettingControls();
		});
		
		Utility.setTextChangeListener(textRightQuote, () -> {
			
			if (settingControls) {
				return;
			}
			startSettingControls();
			
			comboRightQuote.setSelectedIndex(0);
			
			stopSettingControls();
		});
	}

	/**
	 * Load combo boxes.
	 */
	private void loadComboBoxes() {
		
		final String [] quotes = new String [] {
				"", "\"", "'", "‹", "›", "«", "»", "‘", "’","“", "”", "„"
		};
		
		Utility.loadItems(comboLeftQuote, quotes);
		comboLeftQuote.setSelectedIndex(1);
		
		Utility.loadItems(comboRightQuote, quotes);
		comboRightQuote.setSelectedIndex(1);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return getQuoteString(textLeftQuote, comboLeftQuote) + " " + getQuoteString(textRightQuote, comboRightQuote);
	}

	/**
	 * Get quote string.
	 * @param textField
	 * @param comboBox
	 * @return
	 */
	private String getQuoteString(JTextField textField, JComboBox comboBox) {
		
		String quoteText = textField.getText();
		if (!quoteText.isEmpty()) {
			
			if (quoteText.contains("'")) {
				return "\"" + quoteText + "\"";
			}
			
			return "'" + quoteText + "'";
		}
		
		// Get combo value.
		quoteText = (String) comboBox.getSelectedItem();
		if (quoteText.isEmpty()) {
			return "none";
		}
		
		if (quoteText.equals("'")) {
			return "\"" + quoteText + "\"";
		}
		return "'" + quoteText + "'";
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			try {
				Obj<Integer> position = new Obj<Integer>(0);
				
				// Get 'none' value.
				int positionAux = position.ref;
				
				String text = Utility.getNextMatch(initialString, position, "\\G\\s*none");
				if (text != null) {
					
					comboLeftQuote.setSelectedIndex(0);
					comboRightQuote.setSelectedIndex(0);
					textLeftQuote.setText("");
					textRightQuote.setText("");
					
					return;
				}

				position.ref = positionAux;
				
				// Get controls' values.
				if (!setQuoteControls(initialString, position, comboLeftQuote, textLeftQuote)) {
					return;
				}
				
				// Get space between.
				text = Utility.getNextMatch(initialString, position, "\\G\\s+");
				if (text == null) {
					return;
				}
				
				if (!setQuoteControls(initialString, position, comboRightQuote, textRightQuote)) {
					return;
				}
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Set quote controls.
	 * @param initialString
	 * @param position
	 * @param comboBox
	 * @param textField
	 * @return
	 */
	private boolean setQuoteControls(String initialString,
			Obj<Integer> position, JComboBox comboBox,
			JTextField textField) {

		// Initialize controls.
		textField.setText("");
		comboBox.setSelectedIndex(0);
		
		Obj<Matcher> matcher = new Obj<Matcher>();

		// Get combo or text value.
		String text = Utility.getNextMatch(initialString, position, "\\G\\s*\\'([^\\']*)\\'", matcher);
		
		if (text == null) {
			text = Utility.getNextMatch(initialString, position, "\\G\\s*\\\"([^\\\"]*)\\\"", matcher);
		}
		
		if (text != null && matcher.ref.groupCount() == 1) {
			text = matcher.ref.group(1);
				
			// Select quote if it exists.
			DefaultComboBoxModel model = (DefaultComboBoxModel) comboBox.getModel();
			int index = model.getIndexOf(text);
			
			if (index >= 0) {
				comboBox.setSelectedIndex(index);
				return true;
			}
			
			textField.setText(text);
			return true;
		}
		
		return false;
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelLeftQuote);
		Utility.localize(labelRightQuote);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssQuotesBuilder");
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
		
		CssQuotesPanel.bounds = bounds;
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
	 * @return
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 * @return
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 * @param string
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 * @return
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssQuotes;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
