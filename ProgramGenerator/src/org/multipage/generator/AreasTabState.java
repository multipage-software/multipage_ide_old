/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 14-12-2020
 *
 */
package org.multipage.generator;

import java.io.Serializable;

/**
 * 
 * @author sechance
 *
 */
public class AreasTabState extends TabState implements Serializable {
	
	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Area ID.
	 */
	public long areaId = 0L;
		
	/**
	 * Set this tab state from the input tab state
	 * @param tabState
	 */
	public void setTabStateFrom(AreasTabState tabState) {
		
		super.setTabStateFrom(tabState);
		
		areaId = tabState.areaId;
	}
}
