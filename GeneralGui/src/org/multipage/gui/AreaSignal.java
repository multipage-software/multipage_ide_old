/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 07-12-2023
 *
 */
package org.multipage.gui;

import java.awt.Component;

/**
 * Area signals definitions.
 * @author vakol
 */
public class AreaSignal extends Signal {

	/**
	 * List of area signals.
	 */
	
	// Set home area in the model. Area is determined by its ID.
	public static Signal setHomeArea = new Signal(
			params(
					Long.class,	// Home area ID.
					Component.class, // Parent GUI component used in dialogs.
					Runnable.class // Runnable lambda that can be run when signal is received.
				));
	
	/**
	 * Static constructor.
	 */
	static {
		// Unnecessary signals in static constructor.
		addUnnecessary(GuiSignal.displayOrRedrawToolTip, GuiSignal.removeToolTip);
		
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
