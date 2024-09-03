/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 24-06-2023
 *
 */
package org.multipage.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Repeated task object.
 * @author vakol
 *
 */
public class RepeatedTask {
	
	/**
	 * Idle time and overall timeout taken into account while stopping the application tasks.
	 */
	private static final long DEFAULT_IDLE_TIMEOUT_MS = 250;
	private static final long STOPPING_TASKS_IDLE_MS = 500;
	private static final long STOP_TIMEOUT_MS = 8000;
	
	/**
	 * All running tasks.
	 */
	private static final Map<String, RepeatedTask> allTasks = new ConcurrentHashMap<>();
	
	
	/**
	 * Task name.
	 */
	protected String name = "unknown";
	
	/**
	 * Task thread.
	 */
	protected Thread thread = null;
	
	/**
	 * Task running flag.
	 */
	protected boolean running = false;
	
	/**
	 * The flag is true if the task timeout is reached.
	 */
	private boolean isTimeout = false;
	
	/**
	 * Stop flag that terminates the main loop.
	 */
	private boolean stop = false;
	
	/**
	 * Main loop of this task.
	 * @param taskName
	 * @param startDelayMs
	 * @param idleTimeMs
	 * @param timeoutMs
	 * @param taskLambda (exit, exception) -> returns flag "continue running"
	 * @throws InterruptedException 
	 */
	public static void loopBlocking(String taskName, long startDelayMs, long idleTimeMs, long timeoutMs,
			BiFunction<Boolean, Obj<Exception>, Boolean> taskLambda)
					throws Exception {
		
		// Check if the name already exists.
		if (allTasks.containsKey(taskName)) {
			throw new Exception("Task name '" + taskName + "' already exists.");
		}
		
		// Create a new task.
		RepeatedTask task = new RepeatedTask();
		task.name = taskName;
		
		// Trim idle timeout.
		if (idleTimeMs < 0) {
			idleTimeMs = DEFAULT_IDLE_TIMEOUT_MS;
		}
		final Long idleTime = idleTimeMs;
		
		// Put the new task into the list of all tasks.
		allTasks.put(taskName, task);
		
		// Run main loop of this task.
		Obj<Exception> exception = new Obj<Exception>(null);
		
		try {
			
			// Delay start.
			if (startDelayMs > 0) {
				Thread.sleep(startDelayMs);
			}
			
			// Get task end time.
			long endTimeMs = -1L;
			boolean isTimeoutChecked = (timeoutMs >= 0L);
			
			if (isTimeoutChecked) {
				long startTimeMs = System.currentTimeMillis();
				endTimeMs = startTimeMs + timeoutMs;
			}
			
			// Task loop.
			task.running = true;
			while (task.running && !task.stop) {
				
				// Invoke lambda function.
				exception.ref = null;
	        	task.running = taskLambda.apply(task.running, exception);
	        	
	        	// Check timeout and possibly end the task loop.
	        	if (isTimeoutChecked) {
	        		
	        		long currentTimeMs = System.currentTimeMillis();
	        		if (currentTimeMs >= endTimeMs) {
	        			
	        			task.isTimeout = true;
	        			task.stop = true;
		        		break;
	        		}
	        	}
	        	
	        	// Idle timeout.
	        	Thread.sleep(idleTime);
	        	
	        	// Exit n exception.
	        	if (exception.ref != null) {
	        		task.stop = true;
	        		break;
	        	}
	        }
		}
		catch (Exception e) {
			exception.ref = e;
		}
		
		task.running = false;
		
		// Remove task from the list of tasks.
		allTasks.remove(task.name);
		
		// Throw possible exception.
		if (exception.ref != null) {
			throw exception.ref;
		}
	}
	
	/**
	 * Main loop of this task.
	 * @param taskName
	 * @param startDelayMs
	 * @param idleTimeMs
	 * @param timeoutMs
	 * @param taskLambda
	 * @throws Exception 
	 */
	public static void loopNonBlocking(String taskName, long startDelayMs, long idleTimeMs, long timeoutMs,
			BiFunction<Boolean, Obj<Exception>, Boolean> taskLambda)
					throws Exception {
		
		// Check if the name already exists.
		if (allTasks.containsKey(taskName)) {
			throw new Exception("Task name '" + taskName + "' already exists.");
		}
		
		// Create a new task.
		RepeatedTask task = new RepeatedTask();
		task.name = taskName;
		
		// Trim idle time.
		if (idleTimeMs < 0) {
			idleTimeMs = DEFAULT_IDLE_TIMEOUT_MS;
		}
		final long idleTime = idleTimeMs;
		
		// Put the new task into the list of all tasks.
		allTasks.put(taskName, task);
		
		// Run main loop thread of this task.
		Obj<Exception> exception = new Obj<Exception>(null);
		task.thread = new Thread(() -> {
			
			// Task loop.
			try {
				
				// Start delay.
				if (startDelayMs > 0) {
					Thread.sleep(startDelayMs);
				}
				
				// Get task end time.
				long endTimeMs = -1L;
				boolean isTimeoutChecked = (timeoutMs >= 0L);
				
				if (isTimeoutChecked) {
					long startTimeMs = System.currentTimeMillis();
					endTimeMs = startTimeMs + timeoutMs;
				}
				
				// Task loop.
				task.running = true;
				while (task.running && !task.stop) {
					
					// Invoke lambda function.
					exception.ref = null;
		        	task.running = taskLambda.apply(task.running, exception);

		        	// Check timeout and possibly end the task loop.
		        	if (isTimeoutChecked) {
		        		
		        		long currentTimeMs = System.currentTimeMillis();
		        		if (currentTimeMs >= endTimeMs) {
		        			
		        			task.isTimeout = true;
		        			task.stop = true;
			        		break;
		        		}
		        	}
		        	
		        	// Idle timeout.
		        	Thread.sleep(idleTime);
		        	
		        	// Exit n exception.
		        	if (exception.ref != null) {
		        		task.stop = true;
		        		break;
		        	}
		        }
			}
			catch (Exception e) {
				exception.ref = e;
			}
			
			task.running = false;
			
			// Remove task from the list of tasks.
			allTasks.remove(task.name);
			
			// Print possible exception on STDOUT.
			if (exception.ref != null) {
				exception.ref.printStackTrace();
			}
			
		}, task.name);
		
		task.thread.start();
	}
	
	/**
	 * Returns true value if task is running.
	 * @return
	 */
	private boolean isRunning() {
		
		boolean isRunning = allTasks.containsKey(name);
		return isRunning;
	}
	
	/**
	 * Returns true if the task timeout is reached.
	 * @return
	 */
	public boolean isTimeout() {
		
		return isTimeout;
	}
	
	/**
	 * Returns true value if there exists a running task.
	 * @return
	 */
	private static boolean existsRunningTask() {
		
		Obj<Boolean> existsRunningTask = new Obj<Boolean>(false);
		
		allTasks.forEach((name, task) -> {
			
			if (task.running) {
				existsRunningTask.ref = true;
			}
		});
		return existsRunningTask.ref;
	}
	
	/**
	 * Stop task with given name.
	 * @param taskName
	 */
	public static boolean stopTask(String taskName) {
		
		// Get the task by name.
		RepeatedTask task = allTasks.get(taskName);
		if (task == null) {
			return true;
		}
		
		// Set stop flag for the task.
		task.stop = true;
		System.err.format("Stopping task \"%s\"\n", task.name);
		
		// Wait until the task is stopped.
		int timeSpanMs = 0;
		while (task.isRunning()) {
			
			try {
				Thread.sleep(STOPPING_TASKS_IDLE_MS);
				timeSpanMs += STOPPING_TASKS_IDLE_MS;
				
				if (timeSpanMs >= STOP_TIMEOUT_MS) {
					return false;
				}
			}
			catch (Exception e) {
			}
		}
		return true;
	}

	/**
	 * Stop all stasks.
	 * @return
	 */
	public static boolean stopAllTasks() {
		
		System.err.println("Stopping tasks");
		
		// Stop flag for each task.
		allTasks.forEach((name, task) -> {
			
			task.stop = true;
			System.err.format("Stopping task \"%s\"\n", name);
		});
		
		// Wait until all task are stopped.
		int timeSpanMs = 0;
		while (existsRunningTask()) {
			
			try {
				Thread.sleep(STOPPING_TASKS_IDLE_MS);
				timeSpanMs += STOPPING_TASKS_IDLE_MS;
				
				if (timeSpanMs >= STOP_TIMEOUT_MS) {
					return false;
				}
				
				System.err.print('#');
			}
			catch (Exception e) {
			}
		}
		return true;
	}
}
