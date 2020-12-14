/*
 * Copyright 2010-2020 (C) sechance
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
	 * Constructor
	 */
	public AreasTreeTabState() {
		
		type = TabType.areasTree;
	}
}
