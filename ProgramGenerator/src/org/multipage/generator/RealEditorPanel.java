/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class RealEditorPanel extends JPanel implements SlotValueEditorPanelInterface {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Double value.
	 */
	private Double number = null;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JTextField textReal;
	private JLabel labelMessage;
	private JButton buttonE;
	private JButton buttonPi;

	/**
	 * Create the panel.
	 */
	public RealEditorPanel() {

		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		textReal = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textReal, 16, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, textReal, 0, SpringLayout.WEST, this);
		add(textReal);
		textReal.setColumns(10);
		
		labelMessage = new JLabel("");
		springLayout.putConstraint(SpringLayout.NORTH, labelMessage, 6, SpringLayout.SOUTH, textReal);
		springLayout.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelMessage, 0, SpringLayout.EAST, this);
		labelMessage.setFont(new Font("Tahoma", Font.ITALIC, 11));
		labelMessage.setForeground(Color.RED);
		labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		add(labelMessage);
		
		buttonE = new JButton("e");
		springLayout.putConstraint(SpringLayout.NORTH, buttonE, 0, SpringLayout.NORTH, textReal);
		springLayout.putConstraint(SpringLayout.EAST, buttonE, 0, SpringLayout.EAST, labelMessage);
		buttonE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setEuler();
			}
		});
		buttonE.setPreferredSize(new Dimension(20, 20));
		buttonE.setMargin(new Insets(0, 0, 0, 0));
		add(buttonE);
		
		buttonPi = new JButton("\u03C0");
		springLayout.putConstraint(SpringLayout.EAST, textReal, 0, SpringLayout.WEST, buttonPi);
		springLayout.putConstraint(SpringLayout.EAST, buttonPi, 0, SpringLayout.WEST, buttonE);
		buttonPi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPi();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonPi, 0, SpringLayout.NORTH, textReal);
		buttonPi.setPreferredSize(new Dimension(20, 20));
		buttonPi.setMargin(new Insets(0, 0, 0, 0));
		add(buttonPi);
	}

	/**
	 * Set PI.
	 */
	protected void setPi() {
		
		setValue(Math.PI);
	}

	/**
	 * Add value of Euler number.
	 */
	protected void setEuler() {
		
		setValue(Math.E);
	}

	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {

		if (number == null && !ProgramGenerator.isExtensionToBuilder()) {
			number = 0.0;
		}
		return number;
	}

	/**
	 * Set value.
	 */
	@Override
	public void setValue(Object value) {

		if (value instanceof Double) {
			number = (Double) value;
			textReal.setText(String.valueOf(number));
		}
		else {
			number = null;
			textReal.setText("");
		}
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Set document listener.
		setDocumentListener();
		// Set tool tips.
		setToolTips();
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonE.setToolTipText(Resources.getString("org.multipage.generator.tooltipInsertsEulerNumber"));
		buttonPi.setToolTipText(Resources.getString("org.multipage.generator.tooltipInsertsLudolphsNumber"));
	}

	/**
	 * Clear editor.
	 */
	public void clear() {

		number = null;
		textReal.setText("");
	}

	/**
	 * Set document listener.
	 */
	private void setDocumentListener() {

		textReal.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onEditorChange();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onEditorChange();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onEditorChange();
			}
		});
	}

	/**
	 * On editor change.
	 */
	protected void onEditorChange() {

		// Try to convert the value.
		String text = textReal.getText();
		String message = "";
		
		try {
			if (!text.isEmpty()) {
				number = Double.parseDouble(text);
			}
			else {
				number = null;
			}
		}
		catch (NumberFormatException e) {
			
			number = null;
			message = Resources.getString("org.multipage.generator.messageErrorRealNumber");
		}
		
		labelMessage.setText(message);
	}

	/**
	 * @return the textReal
	 */
	public JTextField getTextReal() {
		return textReal;
	}

	/**
	 * Set default value state.
	 */
	@Override
	public void setDefault(boolean isDefault) {
		
		// Nothing to do.
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return StringValueEditor.meansReal;
	}
}
