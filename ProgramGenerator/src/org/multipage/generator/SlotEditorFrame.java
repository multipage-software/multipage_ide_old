/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 18-01-2018
 *
 */
package org.multipage.generator;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.multipage.gui.Callback;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

import com.maclan.Slot;
import com.maclan.SlotType;

/**
 * 
 * @author
 *
 */
public class SlotEditorFrame extends SlotEditorBaseFrame {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		SlotEditorBaseFrame.setDefaultData();
	}
	
	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		SlotEditorBaseFrame.seriliazeData(inputStream);
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		SlotEditorBaseFrame.seriliazeData(outputStream);
	}
	
	/**
	 * Lunch the dialog.
	 * @param parentWindow
	 * @param slot
	 * @param isNew
	 * @param modal
	 * @param foundAttr
	 */
	public static void showDialog(Window parentWindow, Slot slot, boolean isNew,
			boolean modal, FoundAttr foundAttr) {
		
		if (showExisting(slot)) {
			return;
		}
		
		SlotEditorBaseFrame dialog = ProgramGenerator.newSlotEditor(parentWindow, slot, isNew, modal, true, foundAttr);
		remeberEditor(dialog);
		dialog.setVisible(true);
	}


	/**
	 * Lunch the dialog.
	 * @param parentWindow
	 * @param slot
	 * @param isNew
	 * @param modal
	 * @param foundAttr
	 */
	public static void showDialogSimple(Window parentWindow, Slot slot,
			boolean isNew, boolean modal, FoundAttr foundAttr) {

		if (showExisting(slot)) {
			return;
		}
		
		SlotEditorBaseFrame dialog = ProgramGenerator.newSlotEditor(parentWindow, slot, isNew, modal, false, foundAttr);
		remeberEditor(dialog);
		dialog.setVisible(true);
	}

	/**
	 * Lunch dialog.
	 * @param slot
	 * @param isNew
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @param onChangeEvent
	 */
	public static void showDialog(Slot slot, boolean isNew, boolean useHtmlEditor,
			FoundAttr foundAttr, Callback onChangeEvent) {

		if (showExisting(slot)) {
			return;
		}
		
		SlotEditorBaseFrame dialog = ProgramGenerator.newSlotEditor(slot, isNew, useHtmlEditor, foundAttr, onChangeEvent);
		dialog.setVisible(true);
		
		remeberEditor(dialog);
	}
	
	// $hide<<$
	
	/**
	 * Slot editor panel object.
	 */
	private SlotEditorPanel editor;
		
	/**
	 * Constructor.
	 * @param parentWindow
	 * @param slot
	 * @param isNew
	 * @param modal
	 * @param useHtmlEditor
	 * @param foundAttr
	 */
	@SuppressWarnings("serial")
	public SlotEditorFrame(Window parentWindow, Slot slot, boolean isNew, boolean modal, boolean useHtmlEditor,
			FoundAttr foundAttr) {
		
		editor = new SlotEditorPanel(parentWindow, slot, isNew, modal, useHtmlEditor, foundAttr) {
			@Override
			public SlotEditorHelper createHelper() {
				return createCustomizedHelper(this);
			}
		};

		postCreate();
	}

	
	/**
	 * Constructor.
	 * @param slot
	 * @param isNew
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @param onChangeEvent
	 */
	@SuppressWarnings("serial")
	public SlotEditorFrame(Slot slot, boolean isNew, boolean useHtmlEditor, FoundAttr foundAttr,
			Callback onChangeEvent) {
		
		editor = new SlotEditorPanel(slot, isNew, useHtmlEditor, foundAttr, onChangeEvent) {
			@Override
			public SlotEditorHelper createHelper() {
				return createCustomizedHelper(this);
			}
		};

		postCreate();
	}
	
	/**
	 * Create customized helper.
	 * @param editor
	 * @return
	 */
	private SlotEditorHelper createCustomizedHelper(SlotEditorPanel editor) {
		
		final SlotEditorBaseFrame thisFrame = this;
		
		return new SlotEditorHelper(editor) {
			
			/**
			 * On OK button.
			 */
			@Override
			public void onOk(SlotEditorGenerator editor) {
				
				super.onOk(editor);
				dispose();
				createdSlotEditors.remove(thisFrame);
			}
			
			/**
			 * On Cancel button.
			 */
			@Override
			public void onCancel(SlotEditorGenerator editor) {
				
				super.onCancel(editor);
				dispose();
				createdSlotEditors.remove(thisFrame);
			}

			/**
			 * Load dialog.
			 */
			@Override
			public void loadDialog() {
				
				if (SlotEditorBaseFrame.bounds.isEmpty()) {
					setBounds(0, 0, 800, 600);
					Utility.centerOnScreen(thisFrame);
					bounds = thisFrame.getBounds();
				}
				else {
					thisFrame.setBounds(SlotEditorBaseFrame.bounds);
				}
				super.loadDialog();
			}

			/**
			 * Save dialog.
			 */
			@Override
			protected void saveDialog() {
				
				super.saveDialog();
				SlotEditorBaseFrame.bounds = thisFrame.getBounds();
			}

			/**
			 * 
			 * Added possible frame size change to selectEitor
			 */
			@Override
			public void selectEditor(SlotType type, boolean setDefaultLanguage) {
				
				super.selectEditor(type, setDefaultLanguage);
				
				// TODO: do something with very simple editors like text line editor
				/*
				// If the slot value editor has to be reduced, reduce it.
				if (reducedEditor != null && !ProgramGenerator.isExtensionToBuilder()) {
					
					// Set minimized height.
					Rectangle minimizedBounds = new Rectangle((int) bounds.getWidth(), 400);
					
					// Set not resizeable.
					SwingUtilities.invokeLater(()->{
						thisFrame.setBounds(minimizedBounds);
						setResizable(false);
					});
				}
				else {
					// Set resizable.
					setResizable(true);
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						validate();
						repaint();
					}
				});*/
			}
		};
	}
	
	/**
	 * Gets slot editor helper.
	 */
	@Override
	public SlotEditorHelper getHelper() {
		
		return editor.getHelper();
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		// Set title.
		editor.getHelper().setTitle(this);
		
		setIcons();
		
		// Set editor and listeners
		getContentPane().add(editor);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				editor.getHelper().onCancel(editor);
			}
		});
	}
	
	/**
	 * Set icons.
	 */
	protected void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
	}
}
