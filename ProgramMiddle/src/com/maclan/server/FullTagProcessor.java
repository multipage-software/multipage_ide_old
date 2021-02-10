/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.Properties;


/**
 * @author
 *
 */
public class FullTagProcessor {

	/**
	 * Process text.
	 * @param server
	 * @param properties 
	 */
	public String processText(AreaServer server, String innerText, Properties properties)
		throws Exception {
		
		// Override this method.
		return "";
	}
}
