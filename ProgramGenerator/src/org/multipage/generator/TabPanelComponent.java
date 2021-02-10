/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 07-05-2020
 *
 */
package org.multipage.generator;

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
}
