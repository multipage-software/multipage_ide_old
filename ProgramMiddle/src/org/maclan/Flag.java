/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

/**
 * @author
 *
 */
public class Flag {
	
	public static final int NONE = 0;				// Initial state.
	public static final int SET = 1;				// Active state.
	public static final int PROCESSING = 2;			// Processing state.
	public static final int FINISHED = 4;			// Finished state.
	public static final int PROCESSED = 8;			// Processed state.
	public static final int REVERSED = 16;
}
