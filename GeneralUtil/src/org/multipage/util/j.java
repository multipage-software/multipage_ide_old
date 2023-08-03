/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 28-11-2018
 *
 */
package org.multipage.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author user
 *
 */
public class j {
	
	/**
	 * Format of time stamps.
	 */
	public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * Display time stamp switch
	 */
	private static boolean logTimeSpan = true;
	
	/**
	 * Last time stamp.
	 */
	private static long lastTimeStampMs = System.currentTimeMillis();
	
	/**
	 * Enable logging time delta.
	 */
	private static boolean logTimeDelta = false;
	
	/**
	 * Lock object for log
	 */
	private static final Object synclog = new Object();
	
	/**
	 * Log message divider and stop symbols.
	 */
	private static final byte[] DIVIDER_SYMBOL = { 0, 0 };
	private static final byte[] TERMINAL_SYMBOL = { 0, 0, 0, 0 } ;
	
	/**
	 * Open consoles for mutlitask logging.
	 */
	private static InetSocketAddress [] openConsoles = {
			new InetSocketAddress("localhost", 48000),
			new InetSocketAddress("localhost", 48001),
			new InetSocketAddress("localhost", 48002),
		};
	public static Socket [] openConsoleSockets = new Socket [openConsoles.length];
	
	/**
	 * Lambda function to ensure that the consoles application is running.
	 */
	public static Supplier<Boolean> ensureConsolesRunningLambda = null;

	/**
	 * Log formatted message on stdout or stderr or on "test"
	 * displayDelta - if is true, the delta time between time stamps is displayed.
	 * @param parameter - can be omitted or "out" or "err" or of type LogParameter (with type and indentation)
	 * @param strings
	 */
	synchronized public static void log(String parameter, Object ... strings) {
		
		// Delegate the call.
		log(-1, parameter, strings);
	}
	
	/**
	 * Log formatted message on stdout or stderr or on "test"
	 * displayDelta - if is true, the delta time between time stamps is displayed.
	 * @param consoleIndex - index of Eclipse output console; if the index is -1, then use STDOUT 
	 * @param parameter - can be omitted or "out" or "err" or of type LogParameter (with type and indentation)
	 * @param strings
	 */
	@SuppressWarnings("resource")
	public synchronized static void log(int consoleIndex, Object parameter, Object ... strings) {
		
		synchronized (synclog) {
			
			String type = "";
			String indentation = "";
			
			long currentTimeMs = System.currentTimeMillis();
			long timeDeltaMs = currentTimeMs - lastTimeStampMs;
			
			Timestamp timeStamp = new Timestamp(lastTimeStampMs);
			String timeStampText = logTimeSpan ? new SimpleDateFormat("kk:mm:ss.SSS").format(timeStamp) + ": " : "";
			lastTimeStampMs = currentTimeMs;
			
			if (logTimeDelta) {
				formatToConsole(consoleIndex, timeStamp, "delta %dms ", timeDeltaMs);
			}
			
			if (parameter instanceof LogParameter) {
				LogParameter logparam = (LogParameter) parameter;
				indentation = logparam.getIndentation();
				type = logparam.getType();
			}
			else if (parameter instanceof String) {
				type = (String) parameter;
			}
			
			if (!type.isEmpty()) {
				
				PrintStream os = "out".equals(type) || "test".equals(type) ? System.out : ("err".equals(type) ? System.err : null);
				if (strings.length > 0) {
					if (os != null) {
						Object [] parameters = Arrays.copyOfRange(strings, 1, strings.length);
						os.format(indentation + timeStampText + strings[0].toString() + '\n', parameters);
					}
					else {
						formatToConsole(consoleIndex, timeStamp, indentation + timeStampText + type + '\n', strings);
					}
					
				}
				else {
					if (os != null) {
						os.format(indentation + timeStampText + type);
					}
					else {
						formatToConsole(consoleIndex, timeStamp, indentation + timeStampText + type + '\n');
					}
				}
			}
			else {
				formatToConsole(consoleIndex, timeStamp, indentation + parameter.toString() + '\n', strings);
			}
		}
	}
	
	/**
	 * Output formatted text to appropriate output console.
	 * @param consoleIndex
	 * @param timeStamp
	 * @param format
	 * @param strings
	 * @throws Exception 
	 */
	private static void formatToConsole(int consoleIndex, Timestamp timeStamp, String format, Object... strings) {
		
		int count = openConsoles.length;
		
		if (consoleIndex > 0 && consoleIndex < count) {
			
			String message = String.format(format, strings);
			try {
				byte [] bytes = message.getBytes("UTF-8");
				
				// Try to get socket connected to a given console.
				Socket consoleSocket = getConnectedSocket(consoleIndex - 1);
				
				// Write the log message to the console.
				OutputStream stream = consoleSocket.getOutputStream();
				stream.write(timeStamp.toLocalDateTime().format(TIMESTAMP_FORMAT).getBytes("UTF-8"));	
				stream.write(DIVIDER_SYMBOL);
				stream.write(bytes);
				stream.write(TERMINAL_SYMBOL);
				stream.flush();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.err.format(format, strings);
		} 
	}
	
	/**
	 * Get connected socket.
	 * @param consoleIndex
	 * @return
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	private static Socket getConnectedSocket(int consoleIndex)
			throws Exception {
		
		Socket socket = openConsoleSockets[consoleIndex];
		if (socket == null) {
			
			// Ensurte that the consoles are running.
			if (ensureConsolesRunningLambda == null) {
				throw new IllegalStateException();
			}
			Boolean running = ensureConsolesRunningLambda.get();
			if (!running) {
				throw new IllegalStateException();
			}
			
			// Connect to given port.
			InetSocketAddress socketAddress = openConsoles[consoleIndex];
			
			String server = socketAddress.getHostName();
			int port = socketAddress.getPort();
			
			socket = new Socket(server, port);
			openConsoleSockets[consoleIndex] = socket;
		}
		return socket;
	}

	/**
	 * Log time stamp next time the message is displayed.
	 */
	public static void enableTimeSpan(boolean enable) {
		
		logTimeSpan = enable;
	}
	
	/**
	 * Log time delta next time the message is displayed.
	 */
	public static void enableTimeDelta(boolean enable) {
		
		logTimeDelta = enable;
	}

	/**
	 * Log message.
	 * @param stringResource - identifier of a message
	 * @param strings
	 */
	public static void logMessage(String stringResource, Object ...strings) {
		
		// Try to load string resource.
		String message = Resources.getString(stringResource);
		log(-1, message, strings);
	}
	
	/**
	 * Print stack trace
	 * @param caption
	 */
	public static void printStackTrace(String caption) {
		
		try {
			throw new Exception(caption);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get function on stack.
	 * @param stackLevel
	 * @return
	 */
	public static String stack(int stackLevel) {
		
		// Omit current and the caller level.
		stackLevel += 2;
		
		// Get stack info.
		StackTraceElement [] stack = Thread.currentThread().getStackTrace();
		
		// Try to get stack element at given level.
		if (stackLevel > 0 && stackLevel <= stack.length) {
			StackTraceElement stackElement = stack[stackLevel];
			
			// Dump stack level.
			String stackElementDump = stackElement.toString();
			return stackElementDump;
		}
		
		return "unknown";
	}
}
