/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 31-10-2022
 *
 */
package org.maclan.server;

import org.multipage.gui.EventCondition;
import org.multipage.gui.Signal;

/**
 * Signal definitions for Area Server.
 * @author vakol
 *
 */
public class AreaServerSignal extends Signal {
	
	/**
	 * Debug statement.
	 */
	public static final AreaServerSignal debugStatement = new AreaServerSignal();
	
	/**
	 * Debug response.
	 */
	public static final AreaServerSignal debugResponse = new AreaServerSignal();

	/**
	 * Static constructor.
	 */
	static {
		// Unnecessary signals in static constructor.
		addUnnecessary(/* Add them as parameters. */);
		
		// Describe signals.
		describeSignals(AreaServerSignal.class);
	}
	
	/**
	 * Check if an input object equals this signal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof AreaServerSignal) {
			
			Signal signal = (AreaServerSignal) obj;
			boolean isSame = this.name.equals(signal.name);
			
			return isSame;
		}
		return false;
	}
}
