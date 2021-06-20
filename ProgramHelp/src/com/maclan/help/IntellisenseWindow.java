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
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.multipage.gui.Utility;
import org.multipage.util.j;

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
		dialog = new IntellisenseWindow(Utility.findWindow(parent));
		dialog.setFocusable(false);
		dialog.setVisible(false);
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
	public static final void displayAtCaret(JTextPane textPane, Caret caret, LinkedList<String> suggestions) {
		
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
	
	//$hide<<$
	
	/**
	 * Components.
	 */
	private JList<String> list;

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
		
		list = new JList<String>();
		scrollPane.setViewportView(list);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		
	}
	
	/**
	 * Load suggestions.
	 * @param suggestions
	 */
	private void loadSuggestions(LinkedList<String> suggestions) {
		// TODO Auto-generated method stub
		
	}
}
