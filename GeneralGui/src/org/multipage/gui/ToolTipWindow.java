/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author
 *
 */
public class ToolTipWindow extends JWindow {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Label.
	 */
	private JLabel label = new JLabel();

	/**
	 * Constructor.
	 */
	public ToolTipWindow(Component parent) {
		super(Utility.findWindow(parent));
		
		// Add label to the window.
		setLayout(new BorderLayout());
		label.setBorder(new LineBorder(Color.BLACK));
		add(label, BorderLayout.CENTER);
	}

	/**
	 * Show window.
	 */
	public void showw(Point topleft, String tooltip) {
		
		// Set window position.
		setLocation(topleft);
		
		// Set label.
		label.setText(tooltip);
		pack();
		
		setVisible(true);
	}
	
	/**
	 * Hide window.
	 */
	public void hidew() {
		
		setVisible(false);
	}

	/**
	 * Set background color.
	 * @param color
	 */
	public void setBackgroundLabel(Color color) {
		
		label.setBackground(color);
		label.setOpaque(true);
	}
}
