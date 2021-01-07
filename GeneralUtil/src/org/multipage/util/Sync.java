/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 20-11-2018
 *
 */
package org.multipage.util;

import java.util.HashMap;

/**
 * @author user
 *
 */
public class Sync {
	
	/**
	 * Gets synchronization object
	 * @return
	 * @throws Exception
	 */
	public static Object from(HashMap<String, Object> syncs) {
		
		StackTraceElement [] stack = Thread.currentThread().getStackTrace();
		StackTraceElement exepoint = stack[3];
		String key = String.format("%s#%d", exepoint.getClassName(), exepoint.getLineNumber());
		Object sync = syncs.get(key);
		if (sync == null) {
			sync = new Object();
			syncs.put(key, sync);
		}
		return sync;
	}
}
