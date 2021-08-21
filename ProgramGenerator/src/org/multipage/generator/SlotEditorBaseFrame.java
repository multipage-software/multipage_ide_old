/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.JFrame;

import org.maclan.Slot;

/**
 * @author
 *
 */
public abstract class SlotEditorBaseFrame extends JFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Bounds.
	 */
	public static Rectangle bounds;
	
	/**
	 * Set default data.
	 */
	protected static void setDefaultData() {
		
		bounds = new Rectangle();
	}
	
	/**
	 * Load data.
	 * @param inputStream
	 */
	protected static void seriliazeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object data = inputStream.readObject();
		if (!(data instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) data;
		
		TextSlotEditorPanel.openHtmlEditor = inputStream.readBoolean();
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	protected static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		outputStream.writeObject(bounds);
		outputStream.writeBoolean(TextSlotEditorPanel.openHtmlEditor);
	}

	/**
	 * Created editors.
	 */
	protected static LinkedList<SlotEditorBaseFrame> createdSlotEditors = new LinkedList<SlotEditorBaseFrame>();
	
	/**
	 * Gets slot editor panel helper.
	 * @return
	 */
	public abstract SlotEditorHelper getHelper();
	
	/**
	 * Remember opened editor.
	 * @param slot
	 * @return
	 */
	protected static boolean remeberEditor(SlotEditorBaseFrame newFrame) {
		
		Slot slot = newFrame.getHelper().editedSlot;
		
		// Dispose not visible editors.
		disposeNotVisibleEditors();
		
		// Do loop for all created editors.
		for (SlotEditorBaseFrame frame : createdSlotEditors) {
			
			Slot editorSlot = frame.getHelper().editedSlot;
			
			if (editorSlot.getHolder().getId() == slot.getHolder().getId()
					&& editorSlot.getAlias().equals(slot.getAlias())) {
				
				// Slot not found.
				return false;
			}
		}
		
		// Add new editor.
		createdSlotEditors.add(newFrame);
		return true;
	}
	
	/**
	 * Show existing dialog.
	 * @param slot
	 * @return
	 */
	protected static boolean showExisting(Slot slot) {
		
		// Dispose not visible editors.
		disposeNotVisibleEditors();
		
		// Do loop for all created editors.
		for (SlotEditorBaseFrame frame : createdSlotEditors) {
			
			Slot editorSlot = frame.getHelper().editedSlot;
			
			if (editorSlot.getHolder().getId() == slot.getHolder().getId()
					&& editorSlot.getAlias().equals(slot.getAlias())) {
				
				frame.setVisible(true);
				frame.setExtendedState(NORMAL);
				frame.toFront();
				
				return true;
			}
		}
		
		return false;
	}


	/**
	 * Dispose not visible editors.
	 */
	private static void disposeNotVisibleEditors() {
		
		LinkedList<SlotEditorBaseFrame> editorsToRemove = new LinkedList<SlotEditorBaseFrame>();
		
		// Do loop for all created slot editors.
		for (SlotEditorBaseFrame slotEditor : createdSlotEditors) {
			
			if (!slotEditor.isVisible()) {
				
				slotEditor.dispose();
				editorsToRemove.add(slotEditor);
			}
		}
		
		// Remove editors from list.
		createdSlotEditors.removeAll(editorsToRemove);
	}
}
