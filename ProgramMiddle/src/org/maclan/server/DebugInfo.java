/**
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 01-05-2024
 *
 */
package org.maclan.server;

import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.RepeatedTask;

/**
 * Debug information .
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
	 * Xdebug opration.
	 */
	private XdebugOperation debugOperation = XdebugOperation.skip;
	
	/**
	 * Flag is true if debugger can debug current Area Server state.
	 */
	private boolean canDebug = false;
	
	/**
	 * Flag is true if debugger has debugged current Area Server state.
	 */
	private boolean debugged = false;

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
	 * Connect to debugger via Xdebug protocol.
	 * @param server
	 * @param ideHostName
	 * @param xdebugPort
	 */
	public static XdebugClient connectXdebug(AreaServer server, String ideHostName, int xdebugPort)
			throws Exception {
		
		// Check if the server is debugged.
		boolean success = server.isDebuggerEnabled();
		if (!success) {
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
	 * Set debug information about Xdebug client.
	 * @param server
	 * @param debugClient
	 */
	public static void setBreakDebugInfo(AreaServer server, XdebugClient debugClient) {
		
		// Check if the server is debugged.
		boolean success = server.isDebuggerEnabled();
		if (!success) {
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
		
		// Set initial operation and "can isit" flag.
		debugInfo.debugOperation = XdebugOperation.step_into;
		debugInfo.setCanVisit(true);
	}
	
	/**
	 * Set information for Xdebug.
	 * @param server
	 * @param source 
	 */
	public static void setDebugInfo(AreaServer server, TagsSource source) {
		
		// Create debug information.
		DebugInfo debugInfo = server.state.getDebugInfo();
		if (debugInfo == null) {
			debugInfo = new DebugInfo();
			server.state.setDebugInfo(debugInfo);
		}
		
		// Create information about source of the code.
		DebugSourceInfo sourceInfo = debugInfo.getSourceInfo();
		if (sourceInfo == null) {
			sourceInfo = new DebugSourceInfo();
			debugInfo.setSourceInfo(sourceInfo);
		}
		
		Long sourceResourceId = source.resourceId;
		Long sourceSlotId = source.slotId;
		
		sourceInfo.setSourceResourceId(sourceResourceId);
		sourceInfo.setSourceSlotId(sourceSlotId);
		
		// Create information about process and thread.
		DebugThreadInfo threadinfo = debugInfo.getThreadInfo();
		if (threadinfo == null) {
			threadinfo = new DebugThreadInfo();
			debugInfo.setThreadInfo(threadinfo);
		}
		
		Obj<Long> processId = new Obj<>(-1L);
		Obj<String> processName= new Obj<>("");
		Obj<Long> threadId = new Obj<>(-1L);
		Obj<String> threadName= new Obj<>("");
		
		Utility.getProcessAndThread(processId, processName, threadId, threadName);
		
		threadinfo.setProcessId(processId.ref);
		threadinfo.setProcessName(processName.ref);
		threadinfo.setThreadId(threadId.ref);
		threadinfo.setThreadName(threadName.ref);
	}
	
	/**
	 * Set information for Xdebug protocol.
	 * @param server
	 * @param tagName
	 * @param properties
	 * @param cmdBegin
	 * @param cmdEnd
	 * @param innerText
	 */
	public static void setDebugInfo(AreaServer server, String tagName, TagProperties properties, int cmdBegin, int cmdEnd,
			String innerText) {

		// Check if the server is debugged.
		boolean success = server.isDebuggerEnabled();
		if (!success) {
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
	}
	
	/**
	 * Set final debugger information after all Area Server tags have been processed.
	 * @param server
	 */
	public static void setFinalDebugInfo(AreaServer server) {
		
		String innerText = server.state.text.toString();
		int cmdEnd = innerText.length();
		TagProperties properties = new TagProperties();
		
		// Delegate the call.
		setDebugInfo(server, "", properties, cmdEnd, cmdEnd, innerText);
		
		// Set the "can visit" flag to true.
		server.setDebuggerCanVisit(true);
	}
	
	/**
	 * Accept incoming debugging statements.
	 */
	public static void debugPoint(AreaServer server)
			throws Exception {
		
		// Check if the server is debugged.
		boolean success = server.isDebuggerEnabled();
		if (!success) {
			return;
		}
		
		AreaServerState state = server.state;
		DebugInfo debugInfo = state.debugInfo;
		if (debugInfo == null) {
			return;
		}
		
		boolean canVisit = debugInfo.canVisit();
		if (!canVisit) {
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
		
		// Get current thread ID.
		long threadId = Thread.currentThread().getId();
		String threadIdText = String.valueOf(threadId);
		
		// Set timeouts.
		long startDelayMs = 0L;
		long idleTimeMs = 200L;
		long timeoutMs = 3000L;
		
		// Wait for initialized server. Send "break point resolved" notification.
		RepeatedTask.loopBlocking("WaitDebuggerReady" + threadIdText, startDelayMs, idleTimeMs, timeoutMs, (isRunning, exception) -> {
			
			// If server is not ready, continue loop.
			boolean serverReady = debugClient.isServerReady();
			if (!serverReady) {
				return true;
			}
				
			// Send notification about resolving the breakpoint.
			try {
				debugClient.notifyBreakpointResolved();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		});
		
		// Set timeouts.
		startDelayMs = 0L;
		idleTimeMs = 1L;
		timeoutMs = -1L;

		// Wait for the continuation command.
		RepeatedTask.loopBlocking("DebuggerLock" + threadIdText, startDelayMs, idleTimeMs, timeoutMs, (isRunning, exception) -> {
			
			if (!isRunning || exitDebugger) {
				return false;
			}
			boolean isTimeout = Lock.waitFor(debuggerLock, DEBUGGER_LOCK_TIMEOUT_MS);
			if (isTimeout) {
				return true;
			}
			
			return false;
		});
		
		// On stop command, throw Area Server exception.
		XdebugOperation operation = debugInfo.getDebugOperation();
		if (XdebugOperation.stop.equals(operation)) {
			
			AreaServer.throwError("org.maclan.server.messageDebuggerStopOperation");
		}
	}
	
	/**
	 * Final debug operation.
	 * @param server
	 * @throws Exception 
	 */
	public static void finalDebugPoint(AreaServer server)
			throws Exception {
		
		// Check if the server is debugged.
		boolean success = server.isDebuggerEnabled();
		if (!success) {
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
		
		// Get current thread ID.
		long threadId = Thread.currentThread().getId();
		String threadIdText = String.valueOf(threadId);
		
		// Set timeouts.
		long startDelayMs = 0L;
		long idleTimeMs = 200L;
		long timeoutMs = 3000L;
		
		// Wait for server final operations.
		debugClient.notifyFinalDebugInfo();
		
		RepeatedTask.loopBlocking("WaitDebuggerFinish" + threadIdText, startDelayMs, idleTimeMs, timeoutMs, (isRunning, exception) -> {
			
			boolean serverFinished = debugClient.isServerFinished();
			if (serverFinished) {
				
				return false;
			}
			return true;
		});
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
	 * Clone debug information.
	 * @return
	 */
	public DebugInfo cloneDebugInfo() {
		
		DebugInfo clonedDebugInfo = new DebugInfo();
		
		// Clone debug info parts.
		clonedDebugInfo.tagInfo = tagInfo.cloneTagInfo();
		clonedDebugInfo.sourceInfo = sourceInfo.cloneSourceInfo();
		clonedDebugInfo.threadInfo = threadInfo.cloneThreadInfo();
		
		// Set debugger client connection.
		clonedDebugInfo.debugClient = debugClient;
		
		// Set the debugger operation.
		clonedDebugInfo.debugOperation = debugOperation;
		
		// Set if debgger can visit sub level.
		boolean canVisitSubLevel = debugOperation.canStepSubLevel();
		clonedDebugInfo.setCanVisit(canVisitSubLevel);
				 
		return clonedDebugInfo;
	}
	
	/**
	 * Propagate debug information from sub info.
	 * @param debugSubInfo
	 */
	public void progateFromSubInfo(DebugInfo debugSubInfo) {
		
		// Copy debug client and debug operation.
		debugClient = debugSubInfo.debugClient;
		debugOperation = debugSubInfo.debugOperation;
		
		// Set if debugger can visit super level.
		boolean wasDebugged = wasDebugged();
		if (XdebugOperation.step_over.equals(debugOperation)) {
			
			setCanVisit(wasDebugged);
			return;
		}
		
		wasDebugged = wasDebugged();
		if (XdebugOperation.step_out.equals(debugOperation)) {
			
			debugSubInfo.setCanVisit(wasDebugged);
			return;
		}
		
		boolean canVisitSuperLevel = debugSubInfo.debugOperation.canStepSuperLevel();
		setCanVisit(canVisitSuperLevel);
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
		Long slotId = sourceInfo.getSourceSlotId();
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
	
	/**
	 * Get flag that indicates whether debugger can debug current Area Server state
	 * @return
	 */
	public boolean canVisit() {
		
		return canDebug;
	}
	
	/**
	 * Set flag that indicates whether debugger can debug current Area Server state.
	 * @param canDebug
	 */
	public void setCanVisit(boolean canDebug) {
		
		this.canDebug = canDebug;
	}
	
	/**
	 * Get flag that indicates whether current Area Server state was debugged.
	 * @return
	 */
	public boolean wasDebugged() {
		
		return debugged;
	}

	/**
	 * Set flag that indicates whether current Area Server state was debugged.
	 * @param debugged
	 */
	public void setDebugged(boolean debugged) {
		
		this.debugged = debugged;
	}
}
