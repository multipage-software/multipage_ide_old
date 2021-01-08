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
public class AreasTreeTabState extends AreasTabState implements Serializable {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Set of area IDs currently displayed (expanded) in the tree view
	 */
	public Long [] displayedArea = null;
	
	/**
	 * Constructor
	 */
	public AreasTreeTabState() {
		
		type = TabType.areasTree;
	}
}
