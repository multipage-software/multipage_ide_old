/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

/**
 * 
 * @author
 *
 */
public class EditorPaneEx extends JEditorPane {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public EditorPaneEx() {
		
		new TextPopupMenu(this);
		
		setDragEnabled(true);
	}
}
