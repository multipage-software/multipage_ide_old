package org.multipage.generator;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.multipage.gui.TextFieldEx;

/**
 * 
 * @author user
 *
 */
public interface SlotEditorGenerator {
	
	/**
	 * Expose dialog components.
	 */
	JTextField getTextAlias();
	JCheckBox getCheckDefaultValue();
	TextFieldEx getTextSpecialValue();
	JCheckBox getCheckLocalizedFlag();
	JTextField getTextHolder();
	Container getPanelEditor();
	JCheckBox getCheckLocalizedText();
	JLabel getLabelSpecialValue();
	JButton getButtonSpecialValue();
	Component getComponent();
	JToggleButton getToggleDebug();
	JCheckBox getCheckInterpretPhp();
	JLabel getLabelInheritable();
}
