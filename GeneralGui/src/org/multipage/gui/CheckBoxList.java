/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

import javax.swing.*;

import org.multipage.util.*;

/**
 * @author
 *
 */
public class CheckBoxList<T> extends JList {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Manager.
	 */
	private CheckBoxListManager<T> manager;

	/**
	 * Sets content manager.
	 */
	@SuppressWarnings("serial")
	public void setContentManager(final CheckBoxListManager<T> manager) {

		this.manager  = manager;
		
		// Create default model.
		DefaultListModel model = new DefaultListModel();
		setModel(model);

		// Load list items.
		Obj<T> object = new Obj<T>();
		Obj<String> text = new Obj<String>();
		Obj<Boolean> selected = new Obj<Boolean>();
		
		int index = 0;
		
		while (true) {
			if (!manager.loadItem(index, object, text, selected)) {
				break;
			}
			// Create list item and add it to the model.
			CheckListItem<T> item = new CheckListItem<T>(object.ref, text.ref,
					selected.ref);
			model.addElement(item);
			
			index++;
		}
		
		// Create and set cell renderer.
		DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
			// Return list item renderer.
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				// Check object type.
				if (!(value instanceof CheckListItem)) {
					return null;
				}
				CheckListItem<T> item = (CheckListItem<T>) value;
				
				return new CheckListItemLabel(item.text, item.selected, index);
			}
		};
		
		setCellRenderer(renderer);
		
		// Set mouse listener.
		addMouseListener(new MouseAdapter() {
			// On mouse released.
			@Override
			public void mouseReleased(MouseEvent e) {
				// Get list reference and mouse position.
				JList list = (JList) e.getSource();
				Point mousePosition = e.getPoint();
				// Get item index.
				int index = list.locationToIndex(mousePosition);
				if (index == -1) {
					return;
				}
				// Check position.
				Rectangle itemRectangle = list.getCellBounds(index, index);
				if (itemRectangle == null) {
					return;
				}
				if (!itemRectangle.contains(mousePosition)) {
					return;
				}
				// Get list item.
				CheckListItem<T> item = (CheckListItem<T>) list.getModel().getElementAt(index);
				// Fire change event.
				if (manager.processChange(item.object, !item.selected)) {
					// Toggle item selection.
					item.toggleSelection();
					// Repaint cell.
					list.repaint(list.getCellBounds(index, index));
				}
			}
		});
	}

	/**
	 * Select all.
	 */
	public void selectAll(boolean select) {

		// Get model.
		ListModel model = getModel();
		int count = model.getSize();
		
		for (int index = 0; index < count; index++) {
			
			// Get list item.
			Object itemObject = model.getElementAt(index);
			if (itemObject instanceof CheckListItem) {
				CheckListItem<T> item = (CheckListItem<T>) itemObject;
				item.selected = select;
				// Process change.
				if (manager != null) {
					manager.processChange(item.object, select);
				}
			}
		}
		
		repaint();
	}

	/**
	 * Select object.
	 * @param callback
	 */
	public void selectObject(CheckBoxCallback<T> callback) {

		// Get model.
		ListModel model = getModel();
		int count = model.getSize();
		
		for (int index = 0; index < count; index++) {
			
			// Get list item.
			Object itemObject = model.getElementAt(index);
			if (itemObject instanceof CheckListItem) {
				CheckListItem<T> item = (CheckListItem<T>) itemObject;
				item.selected = callback.matches(item.object);
			}
		}
		
		repaint();		
	}
	
	/**
	 * Get selected objects.
	 * @param selectedObjects
	 */
	public void getSelectedObjects(LinkedList<T> selectedObjects) {

		// Reset output list.
		selectedObjects.clear();
		
		// Get model.
		ListModel model = getModel();
		int count = model.getSize();
		
		for (int index = 0; index < count; index++) {
			
			// Get list item.
			Object itemObject = model.getElementAt(index);
			if (itemObject instanceof CheckListItem) {
				CheckListItem<T> item = (CheckListItem<T>) itemObject;
				
				if (item.selected) {
					selectedObjects.add(item.object);
				}
			}
		}		
	}
}

/**
 * 
 * @author
 *
 */
class CheckListItem<T> {
	
	/**
	 * Object.
	 */
	T object;
	
	/**
	 * Text.
	 */
	String text;
	
	/**
	 * Selection.
	 */
	boolean selected;
	
	/**
	 * Constructor.
	 */
	public CheckListItem(T object, String text, boolean selected) {
		
		this.object = object;
		this.text = text;
		this.selected = selected;
	}

	/**
	 * Toggles selection.
	 */
	public void toggleSelection() {

		selected = !selected;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return text;
	}
}

/**
 * @author
 *
 */
class CheckListItemLabel extends JCheckBox {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param index 
	 */
	public CheckListItemLabel(String text, boolean selected, int index) {

		// Set text.
		setText(text);
		// Check.
		setSelected(selected);
		// Get background color.
		Color backGroundColor = Utility.itemColor(index);
		// Set color.
		setBackground(backGroundColor);
	}
}

