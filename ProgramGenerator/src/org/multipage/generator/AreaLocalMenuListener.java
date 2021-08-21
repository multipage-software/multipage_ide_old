/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;

import org.maclan.Area;

/**
 * 
 * @author
 *
 */
public class AreaLocalMenuListener {

	/**
	 * Get current area.
	 * @return
	 */
	protected Area getCurrentArea() {
		
		// Override this method.
		return null;
	}

	/**
	 * Get current parent area.
	 * @return
	 */
	public Area getCurrentParentArea() {
		
		return null;
	}

	/**
	 * Get parent component.
	 * @return
	 */
	public Component getComponent() {
		
		return null;
	}
	
	/**
	 * On new area added callback.
	 */
	public void onNewArea(Long newAreaId) {
		
		// Override this method.
	}
}
