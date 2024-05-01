/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 18-04-2024
 *
 */
package org.maclan.server;

/**
 * Xdeabug transaction states.
 * @author vakol
 */
public enum XdebugTransactionState {
		
	/**
	 * Transaction is created.
	 */
	created,
	
	/**
	 * Transaction is scheduled in transaction list.
	 */
	scheduled,
	
	/**
	 * Transaction has sent all data.
	 */
	sent
}
