/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
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
