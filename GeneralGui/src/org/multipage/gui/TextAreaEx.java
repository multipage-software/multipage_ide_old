/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.JTextArea;


/**
 * @author
 *
 */
public class TextAreaEx extends JTextArea {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TextAreaEx() {
		new TextPopupMenu(this);
		
		setDragEnabled(true);
	}
}
