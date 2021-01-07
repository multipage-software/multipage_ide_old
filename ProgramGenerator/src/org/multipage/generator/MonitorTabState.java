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
public class MonitorTabState extends AreasTabState implements Serializable {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * URL string
	 */
	public String url = "localhost";
	
	/**
	 * Constructor
	 */
	public MonitorTabState() {
		
		type = TabType.monitor;
	}
}
