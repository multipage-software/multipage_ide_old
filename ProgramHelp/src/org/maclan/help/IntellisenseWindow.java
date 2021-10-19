/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 16-06-2021
 *
 */

package org.maclan.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.maclan.help.Intellisense.Suggestion;
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

	/**
	 * The flag informs that intellisense window can be disposed.
	 */
	private static boolean canDispose = true;
	
	/**
	 * Window size.
	 */
	public Dimension windowSize = new Dimension(250, 100);
	
	/**
	 * Scroll bars size in pixels.
	 */
	private static final int scrollbarSizePx = 5;

	/**
	 * List model.
	 */
	private DefaultListModel<Suggestion> listModel;
	/**
	 * Create new window.
	 * @param parent
	 */
	public static void createNew(Component parent) {
		
		// Try to close old window.
		closeIntellisense();
		
		// Create new hidden window.
		Window parentWindow = Utility.findWindow(parent);
		dialog = new IntellisenseWindow(parentWindow);
		dialog.setAlwaysOnTop(true);
		dialog.setFocusable(false);
		dialog.setVisible(false);
		
		// Add action listener for the list.
		dialog.list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
				// Check button bounds.
				if (IntellisenseItemPanel.linkButtonSize != null) {
				
					// Try to get selected list item index.
					int selectedIndex = dialog.list.getSelectedIndex();
					if (selectedIndex >= 0) {
						
						// Get item bounds and the mouse pointer position.
						Rectangle itemBounds = dialog.list.getCellBounds(selectedIndex, selectedIndex);
						Point mousePoint = e.getPoint();
						
						// Trim boundaries to link button.
						itemBounds.x = itemBounds.x + (itemBounds.width - IntellisenseItemPanel.linkButtonSize.width);
						
						// Get selected suggestion.
						Suggestion selectedSuggestion = dialog.list.getSelectedValue();
						
						// If the mouse pointer is on the link button, display help page.
						boolean isOnLinkButton = itemBounds.contains(mousePoint);
						if (isOnLinkButton) {
							
							Intellisense.displayHelpPage(selectedSuggestion);
						}
						// Else apply suggestion.
						else {
							Intellisense.acceptSuggestion(selectedSuggestion);
						}
					}
				}
				
				// Delegate call.
				super.mouseClicked(e);
			}
		});
		
		// Add focus listener.
		dialog.list.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				
				// Close the intellisense window.
				SwingUtilities.invokeLater(() -> closeIntellisense());
				
				// Delegate the call.
				super.focusLost(e);
			}
		});
		
		// Add focus listener.
		final FocusListener focusListener = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				
				// Reset the flag.
				canDispose = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				
				// Check flag.
				if (!canDispose) {
					return;
				}
				
				// Check dialog.
				if (dialog == null) {
					return;
				}
				
				// Check if mouse pointer is on intellisense window.
				Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
				Rectangle intellisenseBoonds = dialog.getBounds();
				
				if (intellisenseBoonds.contains(mouseLocation)) {
					return;
				}
				
				// Hide the dialog.
				SwingUtilities.invokeLater(() -> closeIntellisense());
			}
		};
		parent.addFocusListener(focusListener);
		
		// Add parent window close handler.
		parentWindow.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				
				// Hide the dialog.
				SwingUtilities.invokeLater(() -> closeIntellisense());
				
				// Delegate the call.
				super.windowClosed(e);
				
				// Remove listener.
				parent.removeFocusListener(focusListener);
				
				// Remove listener.
				parentWindow.removeWindowListener(this);
			}
		});
	}
	
	/**
	 * Try to dispose the window object.
	 */
	private static void closeIntellisense() {
		

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
		
		// Set flag.
		canDispose = false;
		
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
		SwingUtilities.invokeLater(() -> {
			if (dialog != null) {
				dialog.setVisible(false);
			}
		});
		SwingUtilities.invokeLater(() -> {
			if (dialog != null) {
				dialog.setVisible(true);
			}
		});
		
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
	 * Controls.
	 */
	private JScrollPane scrollPane = null;
	private JList<Suggestion> list = null;
	
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
		setMinimumSize(windowSize );
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(scrollbarSizePx, 0));
		scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(scrollbarSizePx, 10));
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
		
		// Create renderer.
		list.setCellRenderer(new ListCellRenderer<Suggestion>() {
			
			// Renderer of the suggestion.
			IntellisenseItemPanel renderer = new IntellisenseItemPanel(IntellisenseWindow.this);
			
			// Callback method.
			@Override
			public Component getListCellRendererComponent(JList<? extends Suggestion> list, Suggestion suggestion, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				renderer.setSuggestion(suggestion, index, isSelected, cellHasFocus);
				return renderer;
			}
			
		});
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
