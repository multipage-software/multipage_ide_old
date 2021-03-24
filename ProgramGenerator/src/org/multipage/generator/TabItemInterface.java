/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.util.HashSet;

import javax.swing.event.ChangeEvent;

/**
 * @author
 *
 */
public interface TabItemInterface {

	/**
	 * Get tab description;
	 * @return
	 */
	String getTabDescription();
	
	/**
	 * Reload component.
	 */
	void reload();
	
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
	 * Compile and get tab state
	 * @return
	 */
	TabState getTabState();
	
	/**
	 * Set reference to a tab label object
	 * @param tabLabel
	 */
	void setTabLabel(TabLabel tabLabel);
	
	/**
	 * Set area ID
	 * @param topAreaId
	 */
	void setAreaId(Long topAreaId);
	
	/**
	 * Get selected areas.
	 */
	HashSet<Long> getSelectedAreaIds();
}
