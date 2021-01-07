/*
 * Copyright 2010-2017 (C) vakol
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
