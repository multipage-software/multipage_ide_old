/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.util;

/**
 * @author
 *
 */
public class CommonUtil {
	
	/**
	 * Start time.
	 */
	private static long startTime;

	/**
	 * Start measure the time.
	 */
	public static void startMeasureTime() {
	
		startTime = System.currentTimeMillis();
	}

	/**
	 * Stop measure the time.
	 */
	public static long stopMeasureTime() {
	
		long stopTime = System.currentTimeMillis();
		long deltaT = stopTime - startTime;
		
		System.out.println("delta t = " + deltaT + " ms");
		return deltaT;
	}

}
