/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 23-10-2017
 *
 */
package org.multipage.util;

/**
 * Thread synchronization objects and functions
 * @author user
 */
public class Lock {
	
	/**
	 * Lock identifier
	 */
	private String id;
	
	/**
	 * A signal from notify method
	 */
	private boolean notified = false;
	
	/**
	 * Constructor
	 */
	public Lock() {
		this("");
	}
	
	/**
	 * Constructor
	 * @param id - lock identifier
	 */
	public Lock(String id) {
		
		this.id = id;
	}
	
	/**
	 * Wait for lock
	 * @param lock
	 * @return true if the waiting state has been interrupted
	 */
	public static boolean waitFor(Lock lock) {
		
		synchronized (lock) {
			try {
				if (lock.notified) {
					lock.notified = false; // reset the signal
					return false;
				}
				lock.wait();
				return false;
			}
			catch (InterruptedException e) {
				return true;
			}
		}
	}
	
	/**
	 * Wait for lock with timeout
	 * @param lock
	 * @param milliseconds
	 * @return true if the timeout has elapsed
	 */
	public static boolean waitFor(Lock lock, long milliseconds) {
		
		long start = System.currentTimeMillis();
		synchronized (lock) {
			try {
				if (lock.notified) {
					lock.notified = false; // reset the signal
					return false;
				}
				lock.wait(milliseconds);
			}
			catch (InterruptedException e) {
			}
		}
		
		lock.notified = false;
		
		long delta = System.currentTimeMillis() - start;
		if (delta >= milliseconds) {
			return true;
		}
		
		final long accuracy = 90;  // Timeout accuracy in percent
		long deltaPercent = 100 - (milliseconds - delta) * 100 / milliseconds;
		
		return deltaPercent >= accuracy;
	}
	
	/**
	 * Notify lock and write a message to log
	 * @param lock
	 * @param logMessage
	 */
	public static void notify(Lock lock, String logMessage) {
		
		synchronized (lock) {
			lock.notify();
			lock.notified = true;
			//j.log("err", "NTF " + lock.id);
			if (logMessage != null)
				j.log(logMessage);
		}
		// Switch to another thread
		try {
			Thread.sleep(0);
		}
		catch (InterruptedException e) {
		}
	}
	
	/**
	 * Notify lock
	 * @param lock
	 */
	public static void notify(Lock lock) {
		
		notify(lock, null);
	}

	/**
	 * Notify lock and write a message to log
	 * @param lock
	 * @param logMessage
	 */
	public static void notifyAll(Lock lock, String logMessage) {
		
		synchronized (lock) {
			lock.notifyAll();
			lock.notified = true;
			//j.log("err", "NTF " + lock.id);
			if (logMessage != null)
				j.log(logMessage);
		}
		// Switch to another thread
		try {
			Thread.sleep(0);
		}
		catch (InterruptedException e) {
		}
	}
	
	/**
	 * Notify lock
	 * @param lock
	 */
	public static void notifyAll(Lock lock) {
		
		notifyAll(lock, null);
	}
	
	/**
	 * Get text representation of the lock state
	 */
	@Override
	public String toString() {
		return "Lock [id=" + id + ", notified=" + notified + "]";
	}
}
