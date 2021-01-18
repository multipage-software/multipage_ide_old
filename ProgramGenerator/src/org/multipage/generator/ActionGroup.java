/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 21-07-2017
 *
 */
package org.multipage.generator;

/**
 * Groups.
 * @author user
 *
 */
public enum ActionGroup {
	
	// Sets prirority of actions. (First groups have higher priority.)
	
	// Change of area model.
	areaModelChange,
	// Change os slot model.
	slotModelChange,
	// Change of area view state.
	areaViewStateChange,
	// Change of slot view state.
	slotViewStateChange,
	// Change of GUI state.
	guiStateChange,
	// Change of area view.
	areaViewChange,
	// Change of slot view.
	slotViewChange,
	// Change of GUI.
	guiChange
}