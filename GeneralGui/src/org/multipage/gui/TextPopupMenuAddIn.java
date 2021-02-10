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
