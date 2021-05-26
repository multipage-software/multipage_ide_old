/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 02-04-2020
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextEditorPane;
import org.multipage.gui.Utility;

/**
 * @author user
 *
 */
public class ExternalSlotEditorPanel extends JPanel implements SlotValueEditorPanelInterface {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Text editor panel.
	 */
	private TextEditorPane textEditorPanel;
	
	/**
	 * Constructor.
	 */
	public ExternalSlotEditorPanel() {
		
		// Initialize components.
		initComponents();
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		textEditorPanel = new TextEditorPane(Utility.findWindow(this), false);
		add(textEditorPanel);
	}
	
	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {
		
		return textEditorPanel.getText();
	}
	
	/**
	 * Set value.
	 */
	@Override
	public void setValue(Object value) {
		
		if (value == null) {
			value = "";
		}
		textEditorPanel.setText(value.toString());
	}
	
	/**
	 * Set default.
	 */
	@Override
	public void setDefault(boolean isDefault) {
		
	}
	
	/**
	 * Return value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return StringValueEditor.meansExternalProvider;
	}
}
