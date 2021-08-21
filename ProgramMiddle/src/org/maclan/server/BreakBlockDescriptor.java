/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

/**
 * 
 * @author
 *
 */
public class BreakBlockDescriptor extends BlockDescriptor {

	/**
	 * Last item flag.
	 */
	protected boolean breaked = false;

	/**
	 * Discard flag.
	 */
	protected boolean discard = false;

	/**
	 * Set last list item.
	 * @param discard 
	 */
	public void setBreaked(boolean discard) {
		
		this.breaked = true;
		this.discard = discard;
	}

	/**
	 * Returns true if the loop is breaked by @LAST tag.
	 * @return
	 */
	public boolean isBreaked() {
		
		return breaked;
	}
	
	/**
	 * Get discard flag.
	 */
	public boolean getDiscard() {
		
		return discard;
	}
}
