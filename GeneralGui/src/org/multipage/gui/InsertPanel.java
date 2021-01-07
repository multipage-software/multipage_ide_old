/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Rectangle;

import javax.swing.*;

/**
 * @author
 *
 */
public class InsertPanel extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Get panel title.
	 */
	public String getWindowTitle() {
		
		// Override this method.
		return "";
	}
	
	/**
	 * Get result text.
	 */
	public String getResultText() {
				
		// Override this method.
		return "";
	}

	/**
	 * Save dialog.
	 */
	public void saveDialog() {
		
		// Override this method.
	}

	/**
	 * Get container dialog bounds.
	 * @return
	 */
	public Rectangle getContainerDialogBounds() {
		
		// Override this method.
		return new Rectangle();
	}

	/**
	 * Set container dialog bounds.
	 * @param bounds
	 */
	public void setContainerDialogBounds(Rectangle bounds) {
		
		// Override this method.
	}

	/**
	 * Bounds set flag.
	 * @return
	 */
	public boolean isBoundsSet() {
		
		// Override this method.
		return false;
	}

	/**
	 * Set bounds set.
	 * @param set
	 */
	public void setBoundsSet(boolean set) {
		
		// Override this method.
	}
}
