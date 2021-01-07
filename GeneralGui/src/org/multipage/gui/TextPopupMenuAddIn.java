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
public interface TextPopupMenuAddIn {

	/**
	 * Add menu.
	 * @param popupMenu
	 * @param plainTextPane
	 */
	void addMenu(JPopupMenu popupMenu, JEditorPane plainTextPane);
	
	/**
	 * Update information.
	 */
	void updateInformation();
}
