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
	 * Unknown operation.
	 */
	skip(false, false, false),
	
	/**
	 * Step into sub tags.
	 */
	step_into(true, true, true),
	
	/**
	 * Step over.
	 */
	step_over(true, false, true),
	
	/**
	 * Step to return from sub tag.
	 */
	step_out(false, false, true),
	
	/**
	 * Run program.
	 */
	run(false, false, false),
	
	/**
	 * Stop program.
	 */
	stop(false, false, false);
	
	/**
	 * This flag is true if a debug point steps at same level.
	 */
	private boolean stepSameLevel = false;
	
	/**
	 * This flag is true if a debug point skips steps at sub levels.
	 */
	private boolean stepSubLevel = false;
	
	/**
	 * This flag is true if a debug point skips steps at super levels.
	 */
	private boolean stepSuperLevel = false;
	
	/**
	 * Constructor.
	 * @param stepSameLevel
	 * @param stepSubLevel
	 * @param stepSuperLevel
	 */
	XdebugOperation(boolean stepSameLevel, boolean stepSubLevel, boolean stepSuperLevel) {
		
		this.stepSameLevel = stepSameLevel;
		this.stepSubLevel = stepSubLevel;
		this.stepSuperLevel = stepSuperLevel;
	}
	
	/**
	 * Step same level flag.
	 * @return
	 */
	public boolean isStepSameLevel() {
		
		return stepSameLevel;
	}
	
	/**
	 * Step sub level flag.
	 * @return
	 */
	public boolean canStepSubLevel() {
		
		return stepSubLevel;
	}
	
	/**
	 * Step super level flag.
	 * @return
	 */
	public boolean canStepSuperLevel() {
		
		return stepSuperLevel;
	}
}
