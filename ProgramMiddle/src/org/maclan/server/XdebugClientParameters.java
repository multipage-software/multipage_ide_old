/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 10-06-2023
 *
 */
package org.maclan.server;

/**
 * Parameters of debugger client.
 */
public class XdebugClientParameters {
	
	// Computer name.
	public String computer;
	
	// Process ID
	public String pid;
	
	// Thread ID.
	public String tid;
	
	// Area ID.
	public String aid;
	
	// Area Server state hash.
	public String statehash;
}