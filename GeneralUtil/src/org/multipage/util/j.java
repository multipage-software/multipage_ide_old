/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 28-11-2018
 *
 */
package org.multipage.util;

import java.awt.Color;
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

/**
 * @author user
 *
 */
public class j {
	
	/**
	 * Constant divider line.
	 */
	public static final String DIVIDER_LINE = "-----------------------------------------------------------------------";
	
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
	 * Log message protocol control symbols.
	 */
	private static final byte[] START_OF_HEADING = { (byte) 0x00, (byte) 0x01 };
	private static final byte[] START_OF_TEXT = { (byte) 0x00, (byte) 0x02 };
	private static final byte[] END_OF_TRANSMISSION = { (byte) 0x00, (byte) 0x04 };
	
	/**
	 * Open consoles for mutlitask logging.
	 */
	private static InetSocketAddress [] openConsoles = {
			new InetSocketAddress("localhost", 48000),
			new InetSocketAddress("localhost", 48001),
			new InetSocketAddress("localhost", 48002),
		};
	
	/**
	 * Open sockets and its synchronization objects.
	 */
	public static Socket [] openConsoleSockets = new Socket [openConsoles.length];
	private static Object [] consoleSynchronization = new Object [openConsoles.length];

	/**
	 * Static constructor. 
	 */
	static {
		
		// Fill in array of locks.
		int count = consoleSynchronization.length;
		
		for (int index = 0; index < count; index++) {
			consoleSynchronization[index] = new Object();
		}
	}

	/**
	 * Log formatted message on stdout or stderr or on "test"
	 * displayDelta - if is true, the delta time between time stamps is displayed.
	 * @param parameter - can be omitted or "out" or "err" or of type LogParameter (with type and indentation)
	 * @param strings
	 */
	synchronized public static void log(String parameter, Object ... strings) {
		
		// Delegate the call.
		log(-1, Color.BLACK, parameter, strings);
	}
	
	/**
	 * Log formatted message on stdout or stderr or on "test"
	 * displayDelta - if is true, the delta time between time stamps is displayed.
	 * @param consoleIndex - index of Eclipse output console; if the index is 0 or negative, then use STDOUT 
	 * @param color - if console index is greater then 0 the value sets the color of the console message
	 * @param parameter - can be omitted or "out" or "err" or of type LogParameter (with type and indentation)
	 * @param strings
	 */
	@SuppressWarnings("resource")
	public synchronized static void log(int consoleIndex, Color color, Object parameter, Object ... strings) {
		
		// Check console index.
		if (consoleIndex <= 0 || consoleIndex > openConsoleSockets.length) {
			return;
		}

		String type = "";
		String indentation = "";
		
		long currentTimeMs = System.currentTimeMillis();
		long timeDeltaMs = currentTimeMs - lastTimeStampMs;
		
		Timestamp timeStamp = new Timestamp(lastTimeStampMs);
		String timeStampText = logTimeSpan ? new SimpleDateFormat("kk:mm:ss.SSS").format(timeStamp) + ": " : "";
		lastTimeStampMs = currentTimeMs;
		
		if (logTimeDelta) {
			formatToConsole(consoleIndex, timeStamp, "delta %dms ", color, timeDeltaMs);
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
					formatToConsole(consoleIndex, timeStamp, indentation + timeStampText + type + '\n', color, strings);
				}
				
			}
			else {
				if (os != null) {
					os.format(indentation + timeStampText + type);
				}
				else {
					formatToConsole(consoleIndex, timeStamp, indentation + timeStampText + type + '\n', color);
				}
			}
		}
		else {
			formatToConsole(consoleIndex, timeStamp, indentation + parameter.toString() + '\n', color, strings);
		}
	}
	
	/**
	 * Output formatted text to appropriate output console.
	 * @param consoleIndex
	 * @param timeStamp
	 * @param format
	 * @param color
	 * @param strings
	 * @throws Exception 
	 */
	private static void formatToConsole(int consoleIndex, Timestamp timeStamp, String format, Color color, Object... strings) {
		
		int count = openConsoles.length;
		
		if (consoleIndex > 0 && consoleIndex <= count) {
			
			synchronized (consoleSynchronization[consoleIndex - 1]) {
			
				String body = String.format(format, strings);
				try {
					// Get timestamp and color.
					String timeStampText = timeStamp.toLocalDateTime().format(TIMESTAMP_FORMAT);
					String colorText = String.format("rgb(%02X,%02X,%02X)", color.getRed(), color.getGreen(), color.getBlue());
					String header = timeStampText + '#' + colorText;
					
					// Try to get socket connected to a given console.
					Socket consoleSocket = getConnectedSocket(consoleIndex - 1);
					
					// Write the log message to the console.
					OutputStream stream = consoleSocket.getOutputStream();
					
					// Get header and body bytes.
					byte [] headerBytes = header.getBytes("UTF-8");
					byte [] bodyBytes = body.getBytes("UTF-8");
					
					// Get lengths.
					int headerLength = headerBytes.length;
					int bodyLength = bodyBytes.length;
					
					// Send message to socket stream.
					stream.write(START_OF_HEADING);
					writeMsbInteger(stream, headerLength);
					stream.write(headerBytes);	
					stream.write(START_OF_TEXT);
					writeMsbInteger(stream, bodyLength);
					stream.write(bodyBytes);
					stream.write(END_OF_TRANSMISSION);
					stream.flush();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			System.err.format(format, strings);
		} 
	}
	
	/**
	 * Write MSB integer value to stream.
	 * @param stream
	 */
	private static void writeMsbInteger(OutputStream stream, int intValue) 
			throws Exception {
		
		for (int index = 3; index >= 0; index--) {
			
			// Shift bytes to get the resulting byte.
			byte theByte = (byte) (intValue >>> (index * 8));
			stream.write(theByte);
		}
	}

	/**
	 * Clear console contents.
	 * @param consoleIndex
	 */
	public static void logClear(int consoleIndex) {
		
		// Check console index.
		if (consoleIndex <= 0 || consoleIndex > openConsoleSockets.length) {
			return;
		}
		
		synchronized (consoleSynchronization[consoleIndex - 1])	{
			
			try {
				// Try to get socket connected to a given console.
				Socket consoleSocket = getConnectedSocket(consoleIndex - 1);
				
				// Write the log message to the console.
				OutputStream stream = consoleSocket.getOutputStream();
				
				byte [] bytes = "CLEAR".getBytes("UTF-8");
				stream.write(bytes);
				stream.write(START_OF_TEXT);
				stream.write('_');
				stream.write(END_OF_TRANSMISSION);
				stream.flush();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
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
			
			// Connect to given port.
			InetSocketAddress socketAddress = openConsoles[consoleIndex];
			
			String server = socketAddress.getHostName();
			int port = socketAddress.getPort();
			
			// Connect to socket.
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
		log(-1, Color.BLACK, message, strings);
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
