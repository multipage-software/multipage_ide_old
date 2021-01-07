/*
 * Copyright 2010-2017 (C) vakol
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
