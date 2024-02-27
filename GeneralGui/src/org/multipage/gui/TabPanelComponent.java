/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 07-05-2020
 *
 */
package org.multipage.gui;

import javax.swing.event.ChangeEvent;

/**
 * 
 * @author user
 *
 */
public interface TabPanelComponent {

	/**
	 * On tab panel change event.
	 * @param e
	 * @param selectedIndex
	 */
	void onTabPanelChange(ChangeEvent e, int selectedIndex);
	
	/**
	 * Before tab panel removed.
	 */
	void beforeTabPanelRemoved();
	
	/**
	 * Called when tab panel needs to recreate contents.
	 */
	// TODO: <---COPY to new version
	void recreateContent();
}
