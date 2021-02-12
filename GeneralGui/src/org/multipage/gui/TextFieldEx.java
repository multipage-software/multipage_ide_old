/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

/**
 * @author
 *
 */
public class TextFieldEx extends JTextField {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Popup menu.
	 */
	private TextPopupMenu popupMenu;

	/**
	 * Constructor.
	 */
	public TextFieldEx() {
		
		popupMenu = new TextPopupMenu(this);
		
		setDragEnabled(true);
	}
	
	/**
	 * Get popup menu.
	 * @return
	 */
	public TextPopupMenu getMenu() {
		
		return popupMenu;
	}
}
