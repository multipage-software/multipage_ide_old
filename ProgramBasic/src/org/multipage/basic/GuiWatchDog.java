/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 09-07-2020
 *
 */
package org.multipage.basic;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.multipage.util.Lock;

/**
 * @author user
 *
 */
@SuppressWarnings("unused")
public class GuiWatchDog {
	
	/**
	 * A reference to current Swing worker thread.
	 */
	private static Thread swingThread = null;
	
	/**
	 * When set to true value the watch do thread is terminated.
	 */
	private static boolean terminate = false;
	
	/**
	 * Timeout lock.
	 */
	private static Lock timeoutLock = new Lock();
	
	/**
	 * Restore AWT dispatch thread.
	 */
	
	private static void restoreAwtDispatch() {
		
		try {
			EventQueue newQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
			Field field = newQueue.getClass().getDeclaredField("dispatchThread");
			field.setAccessible(true);
			field.set(newQueue, null);
			Method method = newQueue.getClass().getDeclaredMethod("initDispatchThread");
			method.setAccessible(true);
			method.invoke(newQueue);
		}
		catch (Throwable e) {
		}
	}
	
	/**
	 * Start a thread that ensures Swing thread running.
	 */
	public static void start() {
		
		// Start watch dog thread.
		/*new Thread(() -> {
			
			try {
				Thread.sleep(20000);
			}
			catch (Throwable e) {
			}
			
			while (!terminate ) {
				
				// Get current Swing worker thread.
				swingThread = null;
				Lock lock = new Lock();
				SwingUtilities.invokeLater(() -> {
						swingThread = Thread.currentThread();
						Lock.notify(lock);
				});
				Lock.waitFor(lock, 1000);
				
				// Restart deaed Swing worker thread.
				try {
					if (swingThread == null || (!swingThread.isAlive() || swingThread.isInterrupted())) {
						
						if (swingThread != null) {
							swingThread.stop();
						}
						restoreAwtDispatch();
						
						System.out.format("GUI: AWT message loop restored\n");
					}
				}
				catch (Throwable e) {
				}
				
				// Watch dog timeout.
				Lock.waitFor(timeoutLock, 1000);
			}
			
		}, "GUI-Watch-Dog").start();*/
	}
	
	/**
	 * Stop watch dog.
	 */
	public static void stop() {
		
		terminate = true;
		Lock.notify(timeoutLock);
	}
}
