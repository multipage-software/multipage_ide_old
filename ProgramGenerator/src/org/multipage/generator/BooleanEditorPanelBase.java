/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.multipage.gui.StringValueEditor;
import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public class BooleanEditorPanelBase extends JPanel implements SlotValueEditorPanelInterface {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components' references.
	 */
	private JRadioButton radioTrue;
	private JRadioButton radioFalse;
	private ButtonGroup buttonGroup;
	private JLabel labelSelectValue;

	/**
	 * Set components' referneces.
	 * @param radioTrue
	 * @param radioFalse
	 * @param buttonGroup
	 * @param labelSelectValue
	 */
	protected void setComponentsReferences(JRadioButton radioTrue,
			JRadioButton radioFalse,
			ButtonGroup buttonGroup,
			JLabel labelSelectValue) {
		
		this.radioTrue = radioTrue;
		this.radioFalse = radioFalse;
		this.buttonGroup = buttonGroup;
		this.labelSelectValue = labelSelectValue;
	}
	
	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		localize();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(radioTrue);
		Utility.localize(radioFalse);
		
		if (labelSelectValue != null) {
			Utility.localize(labelSelectValue);
		}
	}
	/**
	 * Set value.
	 * @param booleanValue
	 */
	@Override
	public void setValue(Object value) {
		
		if (!(value instanceof Boolean)) {
			return;
		}
		
		boolean booleanValue = (Boolean) value;
		
		ButtonModel buttonModel = null;
		
		if (booleanValue) {
			buttonModel = radioTrue.getModel();
		}
		else {
			buttonModel = radioFalse.getModel();
		}
		
		buttonGroup.setSelected(buttonModel, true);
	}

	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {
		
		return radioTrue.isSelected();
	}

	/**
	 * Set default value state.
	 */
	@Override
	public void setDefault(boolean isDefault) {
		
		// Enable / disable controls.
		boolean enable = !isDefault;
		
		Color disabledColor = Color.GRAY;
		
		radioTrue.setForeground(enable ? new Color(0, 100, 0) : disabledColor);
		radioFalse.setForeground(enable ? new Color(255, 0, 0) : disabledColor);
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return StringValueEditor.meansBoolean;
	}
}
