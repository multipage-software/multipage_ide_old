/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 16-06-2021
 *
 */

package com.maclan.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.multipage.gui.Utility;
import org.multipage.util.j;

import com.maclan.help.Intellisense.Suggestion;

/**
 * 
 * @author vakol
 *
 */
public class IntellisenseWindow extends JDialog {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Singleton dialog object.
	 */
	private static IntellisenseWindow dialog = null;
	
	//$hide>>$
	
	/**
	 * Create new window.
	 * @param parent
	 */
	public static void createNew(Component parent) {
		
		// Try to close old window.
		closeOld();
		
		// Create new hidden window.
		Window parentWindow = Utility.findWindow(parent);
		dialog = new IntellisenseWindow(parentWindow);
		dialog.setAlwaysOnTop(true);
		dialog.setFocusable(false);
		dialog.setVisible(false);
		
		// Set parent window close handler.
		parentWindow.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				
				// Hide the dialog.
				closeOld();
				
				// Delegate the call.
				super.windowClosed(e);
				
				// Remove listener.
				parentWindow.removeWindowListener(this);
			}
		});
	}
	
	/**
	 * Try to dispose the window object.
	 */
	private static void closeOld() {
		
		if (dialog != null) {
			
			dialog.dispose();
			dialog = null;
		}
	}

	/**
	 * Display intellisense window with suggestions near the text editor caret.
	 * @param textPane
	 * @param caret
	 * @param suggestions
	 */
	public static final void displayAtCaret(JTextPane textPane, Caret caret, LinkedList<Suggestion> suggestions) {
		
		// Check dialog and possibly create new one.
		if (dialog == null) {
			createNew(textPane);
		}
		
		// Get current caret position in text and its location.
		int caretPosition = caret.getDot();
		Rectangle2D caretBounds = null;
		try {
			caretBounds = textPane.modelToView2D(caretPosition);
		}
		catch (Exception e) {
			
			dialog.setVisible(false);
			return;
		}
		Point caretLocation = new Point();
		caretLocation.x = (int) caretBounds.getX() + 10;
		caretLocation.y = (int) caretBounds.getY();
		
		// Load suggestions.
		dialog.loadSuggestions(suggestions);
		
		// Trim the coordinates.
		SwingUtilities.convertPointToScreen(caretLocation, textPane);
		
		// Display window at caret location.
		SwingUtilities.invokeLater(() -> dialog.setLocation(caretLocation));
		
		// Update the window.
		SwingUtilities.invokeLater(() -> dialog.setVisible(false));
		SwingUtilities.invokeLater(() -> dialog.setVisible(true));
		
		// Return focus.
		SwingUtilities.invokeLater(() -> textPane.grabFocus());
	}
	
	/**
	 * Hide window.
	 */
	public static void hideWindow() {
		
		// Hide the dialog window.
		if (dialog != null) {
			dialog.setVisible(false);
		}
	}
	
	/**
	 * List model.
	 */
	private DefaultListModel<Suggestion> listModel;
	
	//$hide<<$
	
	/**
	 * Components.
	 */
	private JList<Suggestion> list;

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public IntellisenseWindow(Window parent) {
		super(parent, ModalityType.MODELESS);
		
		initComponents();
		postCreate(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		setUndecorated(true);
		setMinimumSize(new Dimension(250, 100));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		list = new JList<Suggestion>();
		scrollPane.setViewportView(list);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		createList();
	}
	
	/**
	 * Create list of suggestions.
	 */
	private void createList() {
		
		// Create list model.
		listModel = new DefaultListModel<Suggestion>();
		list.setModel(listModel);
	}

	/**
	 * Load suggestions.
	 * @param suggestions
	 */
	private void loadSuggestions(LinkedList<Suggestion> suggestions) {
		
		// Clear the list.
		listModel.clear();
		
		// Insert suggestions.
		listModel.addAll(suggestions);
		list.updateUI();
		
		j.log("DISPLAYED SUGGESTIONS %s", suggestions.toString());
	}
}
