/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import javax.swing.*;

import org.maclan.SlotType;
import org.multipage.generator.GeneratorUtility;

/**
 * @author
 *
 */
public class SlotTypeCombo extends JComboBox {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 */
	public SlotTypeCombo() {
		
		load();
	}

	/**
	 * Load combo box.
	 */
	private void load() {

		GeneratorUtility.loadSlotTypesCombo(this);
	}
	
	/**
	 * Get selected type.
	 */
	public SlotType getSelected() {
		
		Object selected = getSelectedItem();
		if (selected instanceof SlotType) {
			
			SlotType type = (SlotType) selected;
			return type;
		}
		
		return SlotType.UNKNOWN;
	}
	
	/**
	 * Select type.
	 */
	public void setSelected(SlotType type) {
		
		setSelectedItem(type);
	}
}
