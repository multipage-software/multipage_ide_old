/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 14-04-2020
 *
 */
package org.maclan.server;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import org.maclan.server.JavaScriptDebuggerObjectDump.Action;
import org.multipage.util.Lock;
import org.openjdk.nashorn.internal.runtime.Debug;

/**
 * 
 * @author user
 *
 */
public class JavaScriptDebugger {
	
	/**
	 * Enable debugger flag.
	 */
	private static boolean enabled = false;
	
	/**
	 * Continue without debug.
	 */
	public static boolean continueit = false;
	
	/**
	 * Dialog object.
	 */
	private JavaScriptDebuggerObjectDump dialog;
	
	/**
	 * Enable debugger.
	 * @param flag
	 */
	public static void setEnabled(boolean flag) {
		
		enabled = flag;
	}
	
	/**
	 * Constructor.
	 */
	public JavaScriptDebugger() {
		
		dialog = new JavaScriptDebuggerObjectDump();
	}
	
	/**
	 * Dump object.
	 * @param debuggedObject
	 * @throws Exception
	 */
	public void display(Object name, Object debuggedObject) throws Exception {
		
		if (continueit || !enabled) {
			return;
		}
		
		String jsStack = Debug.scriptStack();
		
		// Trim name.
		String nameText = name == null ? "null" : name.toString();
		
		EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		SecondaryLoop loop = eventQueue.createSecondaryLoop();
		Lock thisThreadLock = new Lock();
		
		
		Thread thread = new Thread(() -> {
			SwingUtilities.invokeLater(() -> {
				dialog = JavaScriptDebuggerObjectDump.showDialog(nameText, debuggedObject, loop);
			});
			loop.enter();
			Lock.notify(thisThreadLock);
		});	
		thread.start();
		
		Lock.waitFor(thisThreadLock);
		
		Action action = dialog.getAction();
		switch (action) {
		
		case breakit:
			throw new Exception("break point at " + jsStack);
		case runit:
			continueit = true;
			break;
			
		default:
		}
	}
}
