/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 01-05-2024
 *
 */
package org.maclan.server;

import org.multipage.util.Lock;
import org.multipage.util.RepeatedTask;
import org.multipage.util.Resources;

/**
 * Debugger tag information.
 * @author vakol
 */
public class DebugInfo {
	
	/**
	 * Debugger lock timeout in milliseconds.
	 */
	private static final long DEBUGGER_LOCK_TIMEOUT_MS = 200;
	
	/**
	 * Exit debugger flag. Is true when application exits.
	 */
	private static boolean exitDebugger = false;
	
	/**
	 * Tag information for debugger.
	 */
	private DebugTagInfo tagInfo = null;
	
	/**
	 * Code source information for debugger.
	 */
	private DebugSourceInfo sourceInfo = null;
	
	/**
	 * Thread information for the debugger.
	 */
	private DebugThreadInfo threadInfo = null;
	
	/**
	 * Xdebug client.
	 */
	private XdebugClient debugClient = null;
	
	/**
	 * Debugger lock.
	 */
	private Lock debuggerLock = null;
	
	/**
	 * Server ready flag.
	 */
	private boolean serverReady = false;
	
	/**
	 * Xdebug opration.
	 */
	private XdebugOperation debugOperation = XdebugOperation.no_operation;

	/**
	 * Get tag information.
	 * @return
	 */
	public DebugTagInfo getTagInfo() {
		return tagInfo;
	}
	
	/**
	 * Set tag information.
	 * @param tagInfo
	 */
	public void setTagInfo(DebugTagInfo tagInfo) {
		this.tagInfo = tagInfo;
	}
	
	/**
	 * Get source information.
	 * @return
	 */
	public DebugSourceInfo getSourceInfo() {
		return sourceInfo;
	}
	
	/**
	 * Set source information.
	 * @param sourceInfo
	 */
	public void setSourceInfo(DebugSourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	
	/**
	 * Get thread information.
	 * @return
	 */
	public DebugThreadInfo getThreadInfo() {
		return threadInfo;
	}
	
	/**
	 * Set thread information.
	 * @param threadInfo
	 */
	public void setThreadInfo(DebugThreadInfo threadInfo) {
		this.threadInfo = threadInfo;
	}
	
	/**
	 * Get deugger client.
	 * @return
	 */
	public XdebugClient getDebugClient() {
		return debugClient;
	}

	/**
	 * Set debugger client.
	 * @param debugClient
	 */
	public void setDebugClient(XdebugClient debugClient) {
		this.debugClient = debugClient;
	}
	
	/**
	 * Get debugger lock.
	 * @return
	 */
	public Lock getDebuggerLock() {
		return debuggerLock;
	}
	
	/**
	 * Set debugger lock.
	 * @param debuggerLock
	 */
	public void setDebuggerLock(Lock debuggerLock) {
		this.debuggerLock = debuggerLock;
	}
	
	/**
	 * Get debugger operation.
	 * @return
	 */
	public XdebugOperation getDebugOperation() {
		return debugOperation;
	}
	
	/**
	 * Set debugger operation.
	 * @param debuggerOperation
	 */
	public void setDebugOperation(XdebugOperation debuggerOperation) {
		this.debugOperation = debuggerOperation;
	}
	
	/**
	 * Get "server ready" flag.
	 * @return
	 */
	public boolean getServerReady() {
		return serverReady;
	}
	
	/**
	 * Set "server ready" flag.
	 * @param serverReady
	 */
	public void setServerReady(boolean serverReady) {
		this.serverReady = serverReady;
	}	
	
	/**
	 * Connect to debugger via Xdebug protocol.
	 * @param server
	 * @param ideHostName
	 * @param xdebugPort
	 */
	public static XdebugClient connectXdebug(AreaServer server, String ideHostName, int xdebugPort)
			throws Exception {
		
		// Check if the server is debugged.
		if (!server.isDebugged()) {
			return null;
		}
		
		// Create new Xdebug client.
		AreaServerState state = server.state;
		String areaServer = state.request.getOriginalRequest().getServerName();
		
		long processId = ProcessHandle.current().pid();
		long threadId = Thread.currentThread().getId();
		long areaId = state.area.getId();
		long stateHash = state.hashCode();
		
		// Area server state locator.
		String areaServerStateLocator = String.format("debug://%s/?pid=%d&amp;tid=%d&amp;aid=%d&amp;statehash=%d", areaServer, processId, threadId, areaId, stateHash);
		
		XdebugClient debugClient = XdebugClient.connectNewClient(ideHostName, xdebugPort, areaServerStateLocator);
		return debugClient;
	}
	
	/**
	 * Set information for Xdebug.
	 * @param server
	 * @param source 
	 * @param replace
	 */
	public static void setDebugInfo(AreaServer server, TagsSource source, String replace) {
		
		DebugInfo debugInfo = server.state.debugInfo;
		if (debugInfo == null) {
			return;
		}
		
		DebugSourceInfo sourceInfo = debugInfo.getSourceInfo();
		if (sourceInfo == null) {
			sourceInfo = new DebugSourceInfo();
		}
		
		Long sourceResourceId = source.resourceId;
		Long sourceSlotId = source.slotId;
		
		sourceInfo.setSourceResourceId(sourceResourceId);
		sourceInfo.setSourceSlotId(sourceSlotId);
	}
	
	/**
	 * Set information for Xdebug protocol.
	 * @param server
	 * @param tagName
	 * @param properties
	 * @param cmdBegin
	 * @param cmdEnd
	 * @param innerText
	 * @param replace
	 */
	public static void setDebugInfo(AreaServer server, String tagName, TagProperties properties, int cmdBegin, int cmdEnd,
			String innerText, String replace) {

		// Check if the server is debugged.
		if (!server.isDebugged()) {
			return;
		}
		
		DebugInfo debugInfo = server.state.debugInfo;
		if (debugInfo == null) {
			debugInfo = new DebugInfo();
			server.state.setDebugInfo(debugInfo);
		}
		
		DebugTagInfo tagInfo = debugInfo.getTagInfo();
		if (tagInfo == null) {
			tagInfo = new DebugTagInfo();
			debugInfo.setTagInfo(tagInfo);
		}
		
		tagInfo.setTagName(tagName);
		tagInfo.setProperties(properties);
		tagInfo.setCmdBegin(cmdBegin);
		tagInfo.setCmdEnd(cmdEnd);
		tagInfo.setInnerText(innerText);
		tagInfo.setReplacement(replace);
	}
	
	/**
	 * Set debug information about Xdebug client.
	 * @param server
	 * @param debugClient
	 */
	public static void setDebugInfo(AreaServer server, XdebugClient debugClient) {
		
		// Check if the server is debugged.
		if (!server.isDebugged()) {
			return;
		}
		
		// Check if debug information exists. If not, create a new one.
		DebugInfo debugInfo = server.state.getDebugInfo();
		if (debugInfo == null) {
			debugInfo = new DebugInfo();
			server.state.setDebugInfo(debugInfo);
		}
		
		// Set debug client reference.
		debugInfo.setDebugClient(debugClient);
	}
	
	/**
	 * Accept incoming debugging statements.
	 */
	public static void debugPoint(AreaServer server)
			throws Exception {
		
		// Check if the server is debugged.
		if (!server.isDebugged()) {
			return;
		}
		
		AreaServerState state = server.state;
		DebugInfo debugInfo = state.debugInfo;
		if (debugInfo == null) {
			return;
		}
		
		XdebugClient debugClient = debugInfo.debugClient;
		if (debugClient == null) {
			return;
		}
		
		boolean isConnected = debugClient.isConnected();
		if (!isConnected) {
			return;
		}
		
        // Accept incoming debugger commands.
		Lock debuggerLock = debugInfo.initializeDebugLock();
		
		debugClient.setAcceptCommands(command -> {
			try {
				
				// Process incoming Xdebug commands with Xdebug client. Return result packet.
				XdebugClientResponse resultPacket = debugClient.xdebugClient(server, command);
				return resultPacket;
			}
			catch (Exception exception) {
				exception.printStackTrace();
				
				// Create error packet and return it to the debugger server.
				try {
					XdebugClientResponse errorPacket = XdebugClientResponse.createErrorPacket(command, XdebugError.UNKNOWN_ERROR, exception);
					return errorPacket;
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
                return null;
			}
		});
		
		// Wait for server ready.
		RepeatedTask.loopBlocking("WaitDebuggerReady", 0L, 200L, (isRunning, exception) -> {
			
			boolean serverReady = debugInfo.getServerReady();
			if (serverReady) {
				
				// Send notification about resolving the breakpoint.
				try {
					debugClient.notifyBreakpointResolved();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
			return true;
		});
		
		// Wait for the continuation command.
		RepeatedTask.loopBlocking("DebuggerLock", 0L, 0L, (isRunning, exception) -> {
			
			if (!isRunning || exitDebugger) {
				return false;
			}
			boolean isTimeout = Lock.waitFor(debuggerLock, DEBUGGER_LOCK_TIMEOUT_MS);
			if (isTimeout) {
				return true;
			}
			return false;
		});
		
		// On debugger stop command throw stop Area Server exception.
		XdebugOperation debugOperation = debugInfo.debugOperation;
		
		if (XdebugOperation.stop.equals(debugOperation)) {
			throwError2("org.maclan.server.messageDebuggerStoppedAreaServer");
		}
	}

	/**
	 * Initialize debugger lock.
	 */
	private Lock initializeDebugLock() {
		
		if (debuggerLock == null) {
			debuggerLock = new Lock();
		}
		
		Lock.reset(debuggerLock);
		return debuggerLock;
	}
	
	/**
	 * Set "exit debugger" flag.
	 * @param exit
	 */
	public static void setExitDebugger(boolean exit) {
		
		exitDebugger = exit;
	}
	
	/**
	 * Throw error message.
	 * @param textId
	 */
	private static void throwError(String textId, Object ... parameters)
			throws Exception {
		
		String text = Resources.getString(textId);
		throw new Exception(String.format(text, parameters));
	}
	
	/**
	 * Throw error text.
	 * @param text
	 */
	private static void throwError2(String text, Object ... parameters)
			throws Exception {

		throw new Exception(String.format(text, parameters));
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public long getProcessId() {
		
		if (threadInfo == null) {
			return -1L;
		}
		long processId = threadInfo.getProcessId();
		return processId;
	}
	
	/**
	 * Set process ID.
	 * @param processId
	 */
	public void setProcessId(long processId) {
		
		if (threadInfo == null) {
			return;
		}
		threadInfo.setProcessId(processId);
	}
	
	/**
	 * Get process name.
	 * @return
	 */
	public String getProcessName() {
		
		if (threadInfo == null) {
			return "";
		}
		String processName = threadInfo.getProcessName();
		return processName;
	}
	
	/**
	 * Set process name.
	 * @param processName
	 */
	public void setProcessName(String processName) {

		if (threadInfo == null) {
			return;
		}
		threadInfo.setProcessName(processName);
	}
	
	/**
	 * Get thread ID.
	 * @return
	 */
	public long getThreadId() {

		if (threadInfo == null) {
			return -1L;
		}
		long threadId = threadInfo.getThreadId();
		return threadId;
	}
	
	/**
	 * Set thread ID.
	 * @param threadId
	 */
	public void setThreadId(long threadId) {
		
		if (threadInfo == null) {
			return;
		}
		threadInfo.setThreadId(threadId);
	}
	
	/**
	 * Get thread name.
	 * @return
	 */
	public String getThreadName() {
		
		if (threadInfo == null) {
			return "";
		}
		String threadName = threadInfo.getThreadName();
		return threadName;	
	}
	
	/**
	 * Set thread name.
	 * @param threadName
	 */
	public void setThreadName(String threadName) {
		
		if (threadInfo == null) {
			return;
		}
		threadInfo.setThreadName(threadName);
	}
	
	/**
	 * Get source resource ID.
	 * @return
	 */
	public Long getSourceResourceId() {
		
		if (sourceInfo == null) {
			return -1L;
		}
		long resourceId = sourceInfo.getSourceResourceId();
		return resourceId;
	}
	
	/**
	 * Set source resource ID.
	 * @param resourceId
	 */
	public void setSourceResourceId(long resourceId) {
		
		if (sourceInfo == null) {
			return;
		}
		sourceInfo.setSourceResourceId(resourceId);
	}
	
	/**
	 * Get source slot ID.
	 * @return
	 */
	public Long getSourceSlotId() {

		if (sourceInfo == null) {
			return -1L;
		}
		long slotId = sourceInfo.getSourceSlotId();
		return slotId;
	}
	
	/**
	 * Set source slot ID.
	 * @param slotId
	 */
	public void setSourceSlotId(long slotId) {
		
		if (sourceInfo == null) {
			return;
		}
		sourceInfo.setSourceSlotId(slotId);
	}
	
	/**
	 * Get command begin.
	 * @return
	 */
	public int getCmdBegin() {
		
		if (tagInfo == null) {
			return -1;
		}
		int cmdBegin = tagInfo.getCmdBegin();
		return cmdBegin;
	}
	
	/**
	 * Set command begin.
	 * @param cmdBegin
	 */
	public void setCmdBegin(int cmdBegin) {
		
		if (tagInfo == null) {
			return;
		}
		tagInfo.setCmdBegin(cmdBegin);
	}
	
	/**
	 * Get command end.
	 * @return
	 */
	public int getCmdEnd() {
		
		if (tagInfo == null) {
			return -1;
		}
		int cmdEnd = tagInfo.getCmdEnd();
		return cmdEnd;
	}
	
	/**
	 * Set command end.
	 * @param cmdEnd
	 */
	public void setCmdEnd(int cmdEnd) {
		
		if (tagInfo == null) {
			return;
		}
		tagInfo.setCmdEnd(cmdEnd);
	}
}
