/*
 * Copyright 2010-2020 (C) sechance
 * 
 * Created on : 26-03-2020
 *
 */
package com.maclan.server;

import javax.script.ScriptEngine;

/**
 * @author user
 *
 */
public class JavaScriptEngine {
	
	/**
	 * Signals signalReleased engine.
	 */
	public static boolean signalReleased = true;
	
	/**
	 * Script engine instance.
	 */
	public ScriptEngine instance = null;
	
	/**
	 * Currently used.
	 */
	public boolean used = false;
}
