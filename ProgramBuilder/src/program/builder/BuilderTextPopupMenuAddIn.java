/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.event.*;

import javax.swing.*;

import org.multipage.generator.*;
import org.maclan.*;

/**
 * @author
 *
 */
public class BuilderTextPopupMenuAddIn extends GeneratorTextPopupMenuAddIn {
	
	/**
	 * Constructor.
	 * @param slot
	 */
	public BuilderTextPopupMenuAddIn(Slot slot) {
		
		super(slot);
	}

	/**
	 * Add trayMenu.
	 */
	@Override
	public void addMenu(JPopupMenu popupMenu, JEditorPane textPane) {
		
		// Add separator.
		popupMenu.addSeparator();
		
		// Create trayMenu.
		// Insert slot.
		JMenuItem menuInsertSlot = new JMenuItem(Resources.getString("builder.menuInsertSlot"));
		menuInsertSlot.setIcon(Images.getIcon("org/multipage/generator/images/slot.png"));
		popupMenu.add(menuInsertSlot);
		menuInsertSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertInheritedSlot();
			}
		});
		
		// Insert area slot.
		JMenuItem menuInsertAreaSlot = new JMenuItem(Resources.getString("builder.menuInsertAreaSlot"));
		menuInsertAreaSlot.setIcon(Images.getIcon("org/multipage/generator/images/slot_area.png"));
		popupMenu.add(menuInsertAreaSlot);
		menuInsertAreaSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertAreaSlot();
			}
		});
		
		// Add inherited trayMenu items.
		super.addMenu(popupMenu, textPane);
	}

	/**
	 * Insert area slot.
	 */
	protected void insertAreaSlot() {
		
		Object [] result = SelectAreaSlot.showDialog(Utility.findWindow(textPane), slot);
		if (result == null) {
			return;
		}
		
		String local = (Boolean) result[2] ? ", local" : "";
		String slotText = null;
		
		if (result[0] instanceof Integer) {
			
			int areaIndex = (Integer) result[0];
			
			if (areaIndex == 1) {
				slotText = String.format("[@TAG slot=#%s]",
					(String) result[1]);
			}
			else if (areaIndex == 2) {
				slotText = String.format("[@TAG startArea, slot=#%s%s]",
					(String) result[1], local);
			}
		}
		else if (result[0] instanceof String) {
			slotText = String.format("[@TAG areaAlias=#%s, slot=#%s%s]",
				(String) result[0], (String) result[1], local);
		}
		
		// Replace text.
		if (slotText != null) {
			textPane.replaceSelection(slotText);
		}
	}
}
