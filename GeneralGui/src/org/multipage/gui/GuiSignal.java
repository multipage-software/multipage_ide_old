/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 30-10-2022
 *
 */
package org.multipage.gui;

import java.util.HashSet;

/**
 * Signal definitions for GUI.
 * @author vakol
 *
 */
public class GuiSignal extends Signal {
	
	// Display area properies.
	public static final Signal displayAreaProperties = new Signal(
			params(
				HashSet.class // Set of area IDs.
			));
	// Select all areas.
	public static Signal selectAll = new Signal();
	// Unselect all areas.
	public static Signal unselectAll = new Signal();
	// On show/hide IDs in areas diagram..
	public static Signal showOrHideIds = new Signal();
	// Focus on area.
	public static Signal focusArea = new Signal(
			params(
					Long.class
			));
	// Focus on the home area.
	public static Signal focusHomeArea = new Signal();	
	// Focus on the tab area.
	public static Signal focusTopArea = new Signal();
	// Focus on the Basic Area.
	public static Signal focusBasicArea = new Signal();
	// Select area in the diagram. Area is determined by its ID.
	public static Signal selectDiagramArea = new Signal(
			params(
					Long.class,	// Area ID.
					Boolean.class // Select single area (false) or add area to previous selections (true).		
			));
	// Select areas in the diagram. The set of areas' IDs is sent in related info inside the message.
	public static Signal selectDiagramAreas = new Signal(
			params(
					HashSet.class // The list of area IDs.
			));
	// Select area with sub areas. 
	public static final Signal selectDiagramAreaWithSubareas = new Signal(
			params(
					Long.class, // Area ID.
					Boolean.class
			));
	// Reset SWT html browser.
	public static final Signal resetSwtBrowser = new Signal();
	// Monitor home page in web browser.
	public static Signal displayHomePage = new Signal();
	// Reactivate GUI
	public static Signal reactivateGui = new Signal();

	/**
	 * Static constructor.
	 */
	static {
		// Unnecessary signals in static constructor.
		//addUnnecessary();
		
		// Describe signals.
		reflectSignals(GuiSignal.class);
	}
	
	/**
	 * Check if an input object equals this signal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof GuiSignal) {
			
			Signal signal = (GuiSignal) obj;
			boolean isSame = this.name.equals(signal.name);
			
			return isSame;
		}
		return false;
	}
}
