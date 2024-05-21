/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 14-05-2024
 *
 */
package org.maclan.server;

/**
 * Xdebug operations.
 * @author vakol
 */
public enum XdebugOperation {

	/**
	 * No operation.
	 */
	no_operation,
	
	/**
	 * Step over.
	 */
	step_over,
	
	/**
	 * Step into sub tags.
	 */
	step_into,
	
	/**
	 * Step to return from sub tag.
	 */
	step_out,
	
	/**
	 * Run program.
	 */
	run,
	
	/**
	 * Stop program.
	 */
	stop
}
