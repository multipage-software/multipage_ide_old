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
public class TextPaneEx extends JTextPane {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TextPaneEx() {
		
		new TextPopupMenu(this);
		
		setDragEnabled(true);
	}
}
