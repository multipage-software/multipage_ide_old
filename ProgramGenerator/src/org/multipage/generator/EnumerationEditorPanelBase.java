/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.maclan.EnumerationValue;
import org.maclan.Slot;
import org.multipage.gui.StringValueEditor;

/**
 * @author
 *
 */
public class EnumerationEditorPanelBase extends JPanel implements SlotValueEditorPanelInterface {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Slot reference.
	 */
	protected Slot slot;
	
	/**
	 * Components' references.
	 */
	private JComboBox<EnumerationValue> comboEnumerationValue;
	
	/**
	 * Set components' references.
	 * @param comboEnumerationValue
	 */
	protected void setComponentsReferences(JComboBox<EnumerationValue> comboEnumerationValue) {
		
		this.comboEnumerationValue = comboEnumerationValue;
	}
	
	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {
		
		// Return selected enumeration value.
		return comboEnumerationValue.getSelectedItem();
	}

	/**
	 * Set value.
	 */
	@Override
	public void setValue(Object value) {
		
		if (value instanceof EnumerationValue) {
			
			EnumerationValue enumerationValue = (EnumerationValue) value;
			
			// Select enumeration type and value.
			selectEnumerationValue(enumerationValue.getId());
		}
	}

	/**
	 * Select enumeration value.
	 * @param valueId
	 */
	protected void selectEnumerationValue(long valueId) {
		
		for (int index = 0; index < comboEnumerationValue.getItemCount();
				index++) {
			
			EnumerationValue enumerationValue = comboEnumerationValue.getItemAt(index);
			if (enumerationValue.getId() == valueId) {
				
				// Select combo box item.
				comboEnumerationValue.setSelectedIndex(index);
				break;
			}
		}
	}

	/**
	 * Set slot reference.
	 * @param slot
	 */
	public void setSlot(Slot slot) {

		// Override this method.
	}
	

	/**
	 * On reset.
	 */
	protected void onReset() {
		
		comboEnumerationValue.setSelectedIndex(-1);
	}

	/**
	 * Set enumeration value.
	 * @param ref
	 */
	public void setEnumerationValue(EnumerationValue enumerationValue) {
		
		setValue(enumerationValue);
	}

	/**
	 * Get enumeration value.
	 * @return
	 */
	public EnumerationValue getEnumerationValue() {
		
		// Return selected enumeration value.
		return (EnumerationValue) getValue();
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
		
		return StringValueEditor.meansEnumeration;
	}
}
