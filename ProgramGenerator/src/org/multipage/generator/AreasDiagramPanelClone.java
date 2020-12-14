/*
 * Copyright 2010-2020 (C) sechance
 * 
 * Created on : 11-12-2020
 *
 */
package org.multipage.generator;

import javax.swing.event.ChangeEvent;

/**
 * Areas diagram clone.
 */
class AreasDiagramPanelClone extends AreasDiagramTabState implements TabItemInterface {
	
	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Create new diagram using information retrieved from a genuine diagram panel.
	 * @param genuineDiagram
	 * @return
	 */
	public static AreasDiagramPanelClone create(AreasDiagramPanel genuineDiagram) {
		
		// Create new clone
		AreasDiagramPanelClone newClone = new AreasDiagramPanelClone();
		
		// Get genuine tab state
		TabState genuineTabState = genuineDiagram.getTabState();
		
		// Set initial state and return the clone
		newClone.setTabStateFrom(genuineTabState);
		return newClone;
	}
	


	/**
	 * Create new diagram using information retrieved from a clone.
	 * @param diagramClone
	 * @return
	 */
	public static AreasDiagramPanelClone create(AreasDiagramPanelClone diagramClone) {
		
		return null;
	}

	/**
	 * No tab text.
	 */
	@Override
	public String getTabDescription() {
		
		return "";
	}
	
	/**
	 * Update panel.
	 */
	@Override
	public void reload() {
		
	}

	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTabPanelRemoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TabState getTabState() {
		// TODO Auto-generated method stub
		return null;
	}
}