/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.multipage.gui.Packet;
import org.multipage.gui.PacketBlock;
import org.multipage.gui.PacketElement;
import org.multipage.gui.PacketSession;
import org.multipage.gui.PacketSymbol;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * Xdebug listener session that stores session states and transactions.
 * @author vakol
 *
 */
public class XdebugListenerSession {
	
	/**
	 * Session constants
	 */
	private static final int RESPONSE_TIMEOUT_MS = 3000;
	
	/**
	 * Xdebug protocol constants.
	 */
	public static final int UNINITIALIZED = 0;
	public static final int NEGOTIATE_XDEBUG_FEATURES = 1;
	public static final int ACCEPT_XDEBUG_COMMANDS = 2;

	/**
	 * Maximum session ID.
	 */
	private static final int MAXIMUM_SESSION_ID = 1024;
	
	/**
	 * Legth of the buffer that stores lenght of the XML content.
	 */
	private static final int LENGHT_BUFFER_SIZE = 16;
	
	/**
	 * Legth of the buffer which stores XML content.
	 */
	private static final int DATA_BUFFER_SIZE = 1024;
	
	/**
	 * Packet parameters.
	 */
	private static final int DATA_LENGTH = 1;
	private static final int XML_BODY = 2;

	/**
	 * Timeout in milliseconds of the send packet operation.
	 */
	private static final long SENDING_PACKET_TIMEOUT_MS = 3000;
	
	/**
	 * UTF-8 encoder.
	 */
	private static final CharsetEncoder UTF8_ENCODER = StandardCharsets.UTF_8.newEncoder();
	
	/**
	 * Null symbol.
	 */
	private static final PacketSymbol NULL_SYMBOL = new PacketSymbol(new byte [] { (byte) 0x00 });
	
	/**
	 * Last generated session ID.
	 */
	private static Obj<Integer> generatedSessionId = new Obj<Integer>(0);
	
	/**
	 * Current Xdebug client parameters.
	 */
	private CurrentXdebugClientData currentClientData = null;
	
	/**
	 * Session ID.
	 */
	protected int sessionId = -1;
	
	/**
	 * Debugged process.
	 */
	private XdebugProcess process = null;
	
	/**
	 * Packet session.
	 */
	private PacketSession packetSession = null;
	
	/**
	 * Data length.
	 */
	private PacketElement dataLength = new PacketBlock(LENGHT_BUFFER_SIZE, LENGHT_BUFFER_SIZE, NULL_SYMBOL, -1);
	
	/**
	 * XML content.
	 */
	private PacketElement xmlBody = new PacketBlock(DATA_BUFFER_SIZE, DATA_BUFFER_SIZE, NULL_SYMBOL, -1);
	
	/**
	 * Debugged URI.
	 */
	private String debuggedUri = null;
	
	/**
	 * Xdebug protocol state.
	 */
	private int xdebugProtocolState = UNINITIALIZED;
	
	/**
	 * Debug listener reference.
	 */
	protected XdebugListener listener = null;
	
	/**
	 * Client socket reference.
	 */
	protected AsynchronousSocketChannel client = null;
	
	/**
	 * Socket server reference.
	 */
	protected AsynchronousServerSocketChannel server = null;
	
	/**
	 * Xdebug transactions.
	 */
	private final Map<Integer, XdebugTransaction> transactions = Collections.synchronizedMap(new LinkedHashMap<Integer, XdebugTransaction>());
	
	/**
	 * Feature map for the session.
	 */
	private final Map<String, XdebugFeature> features = Collections.synchronizedMap(new LinkedHashMap<String, XdebugFeature>());
	
	/**
	 * Xdebug client contexts.
	 */
	private LinkedHashMap<String, Integer> contextNameMap = null;
	
	/**
	 * A flag that indicates whether the session is finished.
	 */
	private boolean isFinished = false;
	
	/**
	 * Callback invoked when the session is ready for Xdebug commands. It is called after feature negotiation.
	 */
	private Runnable onReadyForCommandsLambda = null;
	
	/**
	 * Callback invoked when the session is receives notifications.
	 */
	private Consumer<XdebugClientResponse> onNotificationLambda = null;
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @param xdebugListener 
	 * @throws Exception 
	 */
	public XdebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client,
			XdebugListener xdebugListener)
					throws Exception {
		
		// Set members.
		this.sessionId = generateNewSessionId();
		this.client = client;
		this.server = server;
		this.listener = xdebugListener;
	}
	
	/**
	 * Get session ID.
	 */
	protected static int generateNewSessionId() {
		
		synchronized (generatedSessionId) {	
			if (generatedSessionId.ref < MAXIMUM_SESSION_ID) {
				generatedSessionId.ref++;
			}
			else {
				generatedSessionId.ref = 1;
			}
			return generatedSessionId.ref;
		}
	}
	
	/**
	 * Get session ID.
	 * @return
	 */
	public int getSessionId() {
		
		if (sessionId < 1 && sessionId >= MAXIMUM_SESSION_ID) {
			sessionId = generateNewSessionId();
		}
		return sessionId; 
	}
	
	/**
	 * Get list of process threads.
	 * @return
	 */
	public HashMap<Long, XdebugThread> getThreads() {
		
		if (process == null) {
			return null;
		}
		
		HashMap<Long, XdebugThread> threads = process.getThreads();
		return threads;
	}
	
	/**
	 * Set current thread and stack level.
	 * @param thread
	 * @param stackLevel
	 */
	public void setCurrent(XdebugThread thread, XdebugStackLevel stackLevel) {
		
		if (process == null) {
			return;
		}
		
		thread = process.setCurrentThread(thread);
		if (thread != null) {
			thread.setCurrentStackLevel(stackLevel);
		}
	}
	
	/**
	 * Get current thread and stack level.
	 * @param threadId
	 * @param stackLevel
	 */
	public void setCurrent(Long threadId, XdebugStackLevel stackLevel) {
		
		HashMap<Long, XdebugThread> threads = process.getThreads();
		XdebugThread thread = threads.get(threadId);
		
		if (thread == null) {
			return;
		}
		
		thread.setCurrentStackLevel(stackLevel);
	}
	
	/**
	 * Get current stack level.
	 * @return
	 */
	public XdebugStackLevel getCurrentStackLevel() {
		
		if (process == null) {
			return null;
		}
		
		XdebugThread thread = process.getCurrentThread();
		if (thread == null) {
			return null;
		}
		
		XdebugStackLevel stackLevel = thread.getCurrentStackLevel();
		return stackLevel;
	}
	
	/**
	 * Get debug client (the probe) socket address.
	 * @throws IOException 
	 */
	public InetSocketAddress getClientSocket()
			throws Exception {
		
		InetSocketAddress socketAddress = (InetSocketAddress) client.getRemoteAddress();
		return socketAddress;
	}
	
	/**
	 * Get client socket channel.
	 * @return
	 */
	public AsynchronousSocketChannel getClientSocketChannel() {
		
		return client;
	}
	
	/**
	 * Get server socket channel.
	 * @return
	 */
	public AsynchronousServerSocketChannel getServerSocketChannel() {
		
		return server;
	}
	
	/**
	 * Check if this session is opened.
	 * @return
	 */
	public boolean isOpen() {
		
		if (client == null) {
			return false;
		}
		
		boolean isOpen = client.isOpen();
		return isOpen;
	}
	
	/**
	 * Send bytes to the debugging client (the debugging probe).
	 * @param bytes
	 * @param session
	 * @param completionHandler
	 */
	protected void sendBytes(ByteBuffer bytes, XdebugTransaction transaction, CompletionHandler<Integer, XdebugTransaction> completionHandler)
			throws Exception {
		
		// Write bytes.
		synchronized (client) {
			try {
				client.write(bytes, transaction, completionHandler);
			}
			catch (Exception e) {
				onThrownException(e);
			}
		}
	}
	
	/**
	 * Get debugged URI.
	 * @return
	 */
	public String getDebuggedUri() {
		
		return debuggedUri;
	}
	
	/**
	 * Get client parameters.
	 * @return
	 */
	public CurrentXdebugClientData getCurrentClientData() {
		
		return currentClientData;
	}
	
	/**
	 * Get list of transactions.
	 * @return
	 */
	public Map<Integer, XdebugTransaction> getTransactions() {
		
		return transactions;
	}
	
	/**
	 * Get list of features.
	 * @return
	 */
	public Map<String, XdebugFeature> getFeatures() {
		
		return features;
	}
	
	/**
	 * Get context ID.
	 * @param contextName
	 * @return
	 */
	public Integer getContextId(String contextName) {
		
		Integer contextId = contextNameMap.get(contextName);
		return contextId;
	}
	
	/**
	 * Set lambda callback function that is invoked when session is ready for sending Xdebug commands.
	 * @param onReadyForCommandsLambda
	 */
	public void setReadyForCommands(Runnable onReadyForCommandsLambda) {
		
		this.onReadyForCommandsLambda = onReadyForCommandsLambda;
	}
	
	/**
	 * Set callback for notifications.
	 * @param object
	 */
	public void setReceivingNotifications(Consumer<XdebugClientResponse> onNotificationLambda) {
		
		this.onNotificationLambda = onNotificationLambda;
	}
	
	/**
	 * Create new Xdebug listener session.
	 * @param socketServer
	 * @param socketClient
	 * @param xdebugListener 
	 * @return
	 * @throws Exception 
	 */
	public static XdebugListenerSession newSession(AsynchronousServerSocketChannel socketServer,
			AsynchronousSocketChannel socketClient, XdebugListener xdebugListener) 
					throws Exception {

		XdebugListenerSession xdebugSession = new XdebugListenerSession(socketServer, socketClient, xdebugListener);
		
		//  Create packet session.
		xdebugSession.packetSession = new PacketSession("XdebugListener", socketClient) {

			// Delegate the calls.
			@Override
			protected PacketElement getNewPacketElement(Packet packet) throws Exception {
				PacketElement element = xdebugSession.getNewPacketElement(this, packet);
				return element;
			}

			@Override
			protected boolean onBlock(PacketBlock block) throws Exception {
				return xdebugSession.onBlock(this, block);
			}

			@Override
			protected void onEndOfPacket(Packet packet) throws Exception {
				xdebugSession.onEndOfPacket(this, packet);
			}
		};

		return xdebugSession;
	}
	
	/**
	 * Initialize session using Xdebug client response.
	 * @param clientResponse
	 * @throws Exception 
	 */
	public void initialize(XdebugClientResponse clientResponse) 
			throws Exception {
		
		debuggedUri = clientResponse.getDebuggedUri();
		currentClientData = XdebugClientResponse.parseDebuggedUri(debuggedUri);
	}
	
	/**
	 * Check if the session is initialized.
	 * @return
	 */
	public boolean isInitialized() {
		
		boolean initialized = (debuggedUri != null && !debuggedUri.isEmpty() && currentClientData != null && currentClientData.isInitialized());
		return initialized;
	}
	
	/**
	 * Get packet session.
	 * @return
	 */
	public PacketSession getPacketSession() {
		
		return packetSession;
	}
	
	/**
	 * Set process ID.
	 * @param processId 
	 * @return
	 */
	public void setProcessId(Long processId) {
		
		if (processId == null) {
			return;
		}
		
		if (currentClientData == null) {
			currentClientData = new CurrentXdebugClientData();
		}
		currentClientData.setProcessId(processId);
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	public long getProcessId() {
		
		if (currentClientData == null) {
			return -1L;
		}
		return currentClientData.getProcessId();
	}
	
	/**
	 * Set process name.
	 * @param processId 
	 * @param processName
	 */
	private void setCurrentProcess(Long processId, String processName) {
		
		if (currentClientData == null) {
			currentClientData = new CurrentXdebugClientData();
		}
		
		if (processId != null) {
			currentClientData.setProcessId(processId);
		}
		if (processName != null) {
			currentClientData.setProcessName(processName);
		}
		
		if (process == null) {
			process = new XdebugProcess();
		}
		process.setProcess(processId, processName);
	}
	
	/**
	 * Get session thread ID.
	 * @return
	 */
	public long getThreadId() {
		
		if (currentClientData != null) {
			
			Long threadId = currentClientData.getThreadId();
			if (threadId != null) {
				return threadId;
			}
		}
		return -1L;
	}
	
	/**
	 * Get name of the session thread.
	 * @return
	 */
	public String getThreadName() {
		
		if (currentClientData != null) {
			
			String threadName = currentClientData.getThreadName();
			if (threadName != null) {
				return threadName;
			}
		}
		return "";
	}
	
	/**
	 * Get name of the debugged area.
	 * @return
	 */
	public String getAreaName() {
		
		if (currentClientData != null) {
			
			String areaName = currentClientData.getAreaName();
			if (areaName != null) {
				return areaName;
			}
		}
		return "";
	}
	
	/**
	 * Set input stack information for this session.
	 * @param processId 
	 * @param processName
	 * @param threadId
	 * @param threadName
	 * @param stack
	 */
	public void putStack(Long processId, String processName, Long threadId, String threadName,
			LinkedList<XdebugStackLevel> stack) {
		
		// Set process name.
		setCurrentProcess(processId, processName);
		
		if (process == null) {
			return;
		}
		
		process.putThread(threadId, threadName);
		
		XdebugThread thread = process.getThread(threadId);
		if (thread == null) {
			return;
		}
		
		thread.setStack(stack);
	}

	/**
	 * Get next packet element.
	 * @param packetSession 
	 */
	protected PacketElement getNewPacketElement(PacketSession packetSession, Packet packet)
			throws Exception {
		
		try {
			PacketElement element = null;
			
			// Get the last element in the packet.
			if (!packet.packetParts.isEmpty()) {
				element = packet.packetParts.getLast();
			}
			
			// If current packet element doesn't exit, initialize the packet sequence.
			if (element == null) {
				element = dataLength;
			}
			// If current packet element is not finished, return the same value.
			else if (!element.isCompact) {
				return element;
			}
			// Otherwise if the element is finished use transition rules to determine next one.
			else if (element == dataLength) {
				element = xmlBody;
			}
			else {
				element = null;
			}
			// Set new current element.
			return element;
		}
		catch (Exception e) {
			onThrownException(e);
		}
		return null;
	}
	
	/**
	 * Called when the packet block has been read.
	 * @param packetSession 
	 * @param block
	 * @return
	 */
	protected boolean onBlock(PacketSession packetSession, PacketBlock block)
			throws Exception {
		
		try {
			// Read number representing XML body length.
			if (block.equals(dataLength)) {
				
				// Flip buffer to read its contents.
				block.buffer.flip();
				
				// Read buffer bytes.
				int length = block.buffer.limit();
				byte [] numberBytes = new byte [length];
				block.buffer.get(numberBytes);
				
				// Get the length value.
				String numberText = new String(numberBytes);
				int lengthValue = Integer.valueOf(numberText);
				
				// Save the length value. 
				packetSession.readPacket.userProperties.put(DATA_LENGTH, lengthValue);
				return false;
			}
			else if (block.equals(xmlBody)) {
				
				// Flip buffer to read its contents.
				block.buffer.flip();
				
				// Read buffer bytes.
				int length = block.buffer.limit();
				byte [] numberBytes = new byte [length];
				block.buffer.get(numberBytes);
				
				// Get XML text.
				String xmlText = new String(numberBytes);
				
				// Check XML length.
				int xmlLength = (Integer) packetSession.readPacket.userProperties.get(DATA_LENGTH);
				int xmlTextLength = xmlText.length();
				if (xmlTextLength != xmlLength) {
					Exception exception =  new IllegalStateException();
					onThrownException(exception);
				}
				
				// Save the XML text. 
				packetSession.readPacket.userProperties.put(XML_BODY, xmlText);
				return true;
			}
		}
		catch (Exception e) {
			onThrownException(e);
		}
		return false;
	}
	
	/**
	 * On end of the input packet.
	 * @param packetSession 
	 * @param packet
	 */
	protected void onEndOfPacket(PacketSession packetSession, Packet packet)
			throws Exception {
		
		try {
			// Get XML body of the input packet.
			boolean success = packet.userProperties.containsKey(XML_BODY);
			if (success) {
				Object propertyValue = packet.userProperties.get(XML_BODY);
				if (propertyValue instanceof String) {
					
					String xmlText = (String) propertyValue;
	
					try {
						// Create Xdebug client response object from the input packet.
						XdebugClientResponse clientResponse = XdebugClientResponse.getXmlContent(xmlText);
						
						// Process client response with session object.
						
						// When init packet is received, initialize the session
						boolean isInitPacket = clientResponse.isInitPacket();
						if (isInitPacket) {
							initialize(clientResponse);
						}
						
						// After initialization process the client response.
						boolean isSessionInitialized = isInitialized();
						if (isSessionInitialized) {
							
							processXdebugResponse(clientResponse);						
						}
					}
					catch (Exception e) {
						onThrownException(e);
					}
				}
			}
			
			dataLength.reset();
			xmlBody.reset();
			packetSession.readPacket.reset();
			packetSession.readPacket.packetParts.clear();
		}
		catch (Exception e) {
			onThrownException(e);
		}
	}
	
	/**
	 * Send Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @param responseLambda
	 * @return
	 */
	public int createTransaction(String commandName, String [][] arguments, Consumer<XdebugClientResponse> responseLambda)
			throws Exception {
		
		if (isFinished) {
			return -1;
		}
		
		int transactionId = createTransaction(commandName, arguments, "", responseLambda);
		return transactionId;
	}
	
	/**
	 * Send Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @param string
	 * @param responseLambda
	 */
	public int createTransaction(String commandName, String [][] arguments, String textData, Consumer<XdebugClientResponse> responseLambda)
			throws Exception {
		
		if (isFinished) {
			return -1;
		}
		
		byte [] data = textData.getBytes();
		int transactionId = createTransaction(commandName, arguments, data, responseLambda);
		return transactionId;
	}
	
	/**
	 * Send Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @param responseLambda
	 * @return
	 */
	public int createTransaction(String commandName, String [][] arguments, byte [] data, Consumer<XdebugClientResponse> responseLambda)
			throws Exception {
		
		if (isFinished) {
			return -1;
		}
		
		// Check connection.
		boolean isOpen = client.isOpen();
		if (!isOpen) {
			return -1;
		}
		
		// Create new Xdebug command.
		XdebugCommand command = XdebugCommand.create(commandName, arguments, data);
		
		// Prepare new transaction in the current session.
		XdebugTransaction newTransaction = XdebugTransaction.create(command, responseLambda);
		int transactionId = newTransaction.getId();

		newTransaction.setState(XdebugTransactionState.scheduled);
		transactions.put(transactionId, newTransaction);
		
		return transactionId;
	}
	
	/**
	 * Load specific features from the client and save  them in session state.
	 * @param featureNames
	 */
	public void loadFeaturesFromClient(String ... featureNames)
		throws Exception {
		
		// Prepare commands that will be sent to the debug client.
		for (String featureName : featureNames) {
			createTransaction(XdebugCommand.FEATURE_GET, new String [][] {{"-n", featureName}}, response -> {
				
				// Get Xdebug feature and add it to feature map.
				try {
					throwPossibleException(response);
					
					XdebugFeature feature = response.getFeatureValue();
					String responseFeatureName = feature.getName();
					
					features.put(responseFeatureName, feature);
				}
				catch (Exception e) {
					onException(e);
				}
			});
		}
		// Send command via Xdebug protocol.
		beginTransactions(transactions);
	}
	
	/**
	 * Send IDE features to the debugging client.
	 * @param ideFeatureValues
	 * @param onComplete
	 * @throws Exception 
	 */
	public void sendIdeFeaturesToClient(String[][] ideFeatureValues, Runnable onComplete)
			throws Exception {
		
		// Initialization.
		Obj<Integer> lastTransactionID = new Obj<Integer>(-1);
		
		// Prepare commands that will be sent to the debug client.
		for (String [] feature : ideFeatureValues) {
			
			String featureName = feature[0];
			String featureValue = feature[1];
			
			createTransaction(XdebugCommand.FEATURE_SET, new String [][] {{"-n", featureName}, {"-v", featureValue}},  response -> {
				
				// Get Xdebug feature and add it to feature map.
				try {
					
					XdebugListenerSession.throwPossibleException(response);
					
					boolean success = response.getSettingFeatureResult();
					if (!success) {
						onException("org.mclan.server.messageErrorSettingXdebugFeature", featureName);
					}
					
					// Callback on last transaction complete.
					int transactionId = response.getTransactionId();
					if (transactionId >= 0 && transactionId == lastTransactionID.ref && onComplete != null) {
						onComplete.run();
					}
				}
				catch (Exception e) {
					onException(e);
				}
			});
		}
		
		// Send command via Xdebug protocol.
		lastTransactionID.ref = beginTransactions(transactions);
	}

	/**
	 * Start sending commands via Xdebug protocol.
	 * @param transactions 
	 * @param timeoutMs - if the timeout value is negative the current thread is blocked until all responses are received. 
	 */
	private int beginTransactions(Map<Integer, XdebugTransaction> transactions)
			throws Exception {
		
		if (isFinished) {
			return -1;
		}
		
		// Initialization.
		int lastTransactionId = -1;
		
		// Loop through all transactions.
		for (XdebugTransaction transaction : transactions.values()) {
			lastTransactionId = beginTransaction(transaction);
		}
		
		return lastTransactionId;
	}
	
	/**
	 * Start sending command via Xdebug protocol.
	 * @param transactionId
	 */
	public void beginTransaction(int transactionId) 
			throws Exception {
		
		if (isFinished) {
			return;
		}
		
		// Check transaction ID.
		if (transactionId < 0) {
			return;
		}
		
		XdebugTransaction transaction = null;
		boolean success = transactions.containsKey(transactionId);
		if (!success) {
			return;
		}		
		transaction = transactions.get(transactionId);
		
		beginTransaction(transaction);
	}
	
	/**
	 * Begin Xdebug transaction. The method sends Xdebug command and waits for completion.
	 * @param transactionId
	 * @param responseTimeoutMs
	 * @throws Exception 
	 */
	public void beginTransactionWait(int transactionId, int responseTimeoutMs)
			throws Exception {
		
		if (isFinished) {
			return;
		}
		
		boolean success = transactions.containsKey(transactionId);
		if (!success) {
			return;
		}
		
		// Get transaction object. Set response lock with timeout.
		final XdebugTransaction transaction = transactions.get(transactionId);
		
		transaction.responseLock = new Lock();
		transaction.responseLockTimeoutMs = responseTimeoutMs;
		
		// Start transaction.
		beginTransaction(transaction);
		
		// Wait for response.
		boolean isTimeout = Lock.waitFor(transaction.responseLock, responseTimeoutMs);
		if (isTimeout) {
			onThrownException("org.maclan.server.messageXdebugCommandTimeout");
		}
	}
	
	/**
	 * Begin Xdebug transaction. The method sends Xdebug command.
	 * @param transaction
	 */
	private int beginTransaction(XdebugTransaction transaction)
			throws Exception {
		
		if (isFinished) {
			return -1;
		}
		
		// Initialization.
		int transactionId = -1;
		
		// Check connection.
		boolean isOpen = client.isOpen();
		if (!isOpen) {
			return -1;
		}
		
		try {
			// Check transaction state.
			if (transaction.getState() != XdebugTransactionState.scheduled) {
				return transactionId;
			}
			
			// Get Xdebug command and transaction ID.
			XdebugCommand command = transaction.getCommand();
			transactionId = transaction.getId();
			
			// Compile Xdebug command string that will be sent to the Xdebug client.
			CharBuffer commandString = command.compile(transactionId);
			
			// Send command bytes to the Xdebug client. On completion event check if the number of bytes sent macthes.
			
			// Encode packet bytes. Add NULL byte at the end of the message.
			ByteBuffer buffer = UTF8_ENCODER.encode(commandString);
			int limit = buffer.limit();
			buffer.position(limit - 1);
			buffer.put(XdebugCommand.NULL_SYMBOL);
			buffer.flip();
			
			// Get the number of bytes to send.
			int bytesLength = buffer.limit();
			transaction.setBytesToWrite(bytesLength);
			
			// Create lock for completion of the transaction.
			Lock lockSending = new Lock();
			
			// Send Xdebug command.
			sendBytes(buffer, transaction, new CompletionHandler<Integer, XdebugTransaction>() {
				
				// When sending bytes to the debugging client succeded.
				@Override
				public void completed(Integer bytesWritten, XdebugTransaction transaction) {
					
					// Check the number of written bytes.
					transaction.checkWrittenBytes(bytesWritten);
					
					// Notinfy completion.
					Lock.notify(lockSending);
				}
				
				// On sending error.
				@Override
				public void failed(Throwable exception, XdebugTransaction transaction) {
					
					// Set transaction error.
					onException(exception);
				}
			});
			
			// Wait for completion of transaction.
			boolean isTimeout = Lock.waitFor(lockSending, SENDING_PACKET_TIMEOUT_MS);
			if (isTimeout) {
				return -1;
			}
			
			// Set transaction state to "sent".
			transaction.setState(XdebugTransactionState.sent);
		}
		catch (Exception e) {
			onThrownException(e);
		}
		return transactionId;
	}

	/**
	 * Process Xdebug client response.
	 * @param listener
	 * @param clientResponse
	 * @throws Exception 
	 */
	public void processXdebugResponse(XdebugClientResponse clientResponse)
			throws Exception {
		
		// On INIT packet.
		if (xdebugProtocolState == UNINITIALIZED && clientResponse.isInitPacket()) {
			
			// Check session.
			checkInitResponse(clientResponse);
			// Start negotiating features.
			xdebugProtocolState = NEGOTIATE_XDEBUG_FEATURES;
			
			try {
				// Do negotiate.
				negotiateXdebugFeatures(() -> {
					
					// On negotiation completed.
					xdebugProtocolState = ACCEPT_XDEBUG_COMMANDS;
					
					// Invoke callback.
					if (onReadyForCommandsLambda != null) {
						onReadyForCommandsLambda.run();
					}
				});
			}
			catch (Exception e) {
				onException("org.multipage.generator.messageXdebugFeatureNegotiationError", e);
			}
			return;
		}
		
		// On negotiate features.
		if (xdebugProtocolState == NEGOTIATE_XDEBUG_FEATURES) {
			// Process feature responses.
			try {
				processXdebugCommandResponse(clientResponse);
			}
			catch (Exception e) {
				// Handle exception.
				onException(e);
			}
			return;
		}
		
		// On Xdebug command response.
		if (xdebugProtocolState == ACCEPT_XDEBUG_COMMANDS) {
			// Process command response.
			try {
				// Process notification.
				boolean isNotification = clientResponse.isNotificationPacket();
				if (isNotification) {
					processXdebugNotification(clientResponse);
				}
				else {
					// Process command response.
					processXdebugCommandResponse(clientResponse);
				}
			}
			catch (Exception e) {
				// Handle exception.
				onException(e);
			}
		}
	}
	
	/**
	 * Process Xdebug notification.
	 * @param clientResponse
	 * @return
	 */
	private void processXdebugNotification(XdebugClientResponse clientResponse) {
		
		if (onNotificationLambda != null) {
			onNotificationLambda.accept(clientResponse);
		}
	}

	/**
	 * Process Xdebug command response.
	 * @param clientResponse
	 */
	private void processXdebugCommandResponse(XdebugClientResponse clientResponse)
			throws Exception {
		
		// Get transaction ID.
		int transactionId = clientResponse.getTransactionId();
		
		// Find transaction object with matching transaction ID and pop it from the list of transactions.
		XdebugTransaction transaction = null;
		transaction = transactions.get(transactionId);
		transactions.remove(transactionId);
		
		// If the transaction was not found, throw exception.
		if (transaction == null) {
			onThrownException("org.maclan.server.messageTransactionNotFound", transactionId);
		}
		
		// Call reponse lambda for the transaction.
		Consumer<XdebugClientResponse> responseLambda = transaction.getResponseLambda();
		if (responseLambda != null) {
			responseLambda.accept(clientResponse);
		}
		
		// Notify result lock if it exists.
		if (transaction.responseLock != null) {
			Lock.notify(transaction.responseLock);
		}
	}

	/**
	 * Negotiate Xdebug features.
	 * @param onComplete
	 * @throws Exception 
	 */
	private void negotiateXdebugFeatures(Runnable onComplete) 
			throws Exception {
		
		// Get Xdebug client features and save them into the session state.
		loadFeaturesFromClient(
				"language_supports_threads", 
				"language_name", 
				"language_version", 
				"encoding", 
				"protocol_version", 
				"supports_async", 
				"data_encoding", 
				"breakpoint_languages", 
				"breakpoint_types", 
				"multiple_sessions", 
				"max_children", 
				"max_data", 
				"max_depth", 
				"breakpoint_details", 
				"extended_properties", 
				"notify_ok", 
				"resolved_breakpoints", 
				"supported_encodings", 
				"supports_postmortem", 
				"show_hidden");		
		
		// Send IDE features to the client.
		sendIdeFeaturesToClient(new String [][] {
			    {"encoding", "UTF-8"},
				{"multiple_sessions", "1"},
				{"max_children", "10"},
				{"max_data", "1024"},
				{"max_depth", "8"},
				{"breakpoint_details", "1"},
				{"extended_properties", "0"},
				{"notify_ok", "1"},
				{"show_hidden", "1"}},
				// Completion callback.
				onComplete);
	}

	/**
	 * Initialize Xdebug session.
	 * @param listener 
	 * @param clientResponse
	 * @throws Exception 
	 */
	private void checkInitResponse(XdebugClientResponse clientResponse)
			throws Exception {
		
		// Check IDE key.
		Obj<String> foundIdeKey = new Obj<String>();
		boolean matches = clientResponse.checkIdeKey(XdebugClientResponse.MULTIPAGE_IDE_KEY, foundIdeKey);
		if (!matches) {
			onThrownException("org.multipage.generator.messageXdebugIdeKeyDoesntMatch",
					foundIdeKey.ref, XdebugClientResponse.MULTIPAGE_IDE_KEY);
		}
		
		// Check debugged application ID.
		Obj<String> foundAppId = new Obj<String>();
		matches = clientResponse.checkAppId(XdebugClientResponse.APPLICATION_ID, foundAppId);
		if (!matches) {
			onThrownException("org.multipage.generator.messageXdebugAppIdDoesntMatch",
					foundAppId.ref, XdebugClientResponse.APPLICATION_ID);
		}
		
		// Check debugged language name.
		Obj<String> languageName = new Obj<String>();
		matches = clientResponse.checkLanguage(XdebugClientResponse.LANGUAGE_NAME, languageName);
		if (!matches) {
			onThrownException("org.multipage.generator.messageXdebugLanguageNameDoesntMatch",
					languageName.ref, XdebugClientResponse.LANGUAGE_NAME);
		}
		
		// Check debugged protocol version.
		Obj<String> protocolVersion = new Obj<String>();
		matches = clientResponse.checkProtocolVersion(XdebugClientResponse.PROTOCOL_VERSION, protocolVersion);
		if (!matches) {
			onThrownException("org.multipage.generator.messageXdebugProtocolVersionDoesntMatch",
					protocolVersion.ref, XdebugClientResponse.PROTOCOL_VERSION);
		}
		
		// Get debugged process URI.
		String debuggedUri = clientResponse.getDebuggedUri();
		if (debuggedUri == null) {
			onThrownException("org.multipage.generator.messageXdebugNullFileUri");
		}
		
		// Check debugged URIs.
		if (!debuggedUri.equals(debuggedUri)) {
			onThrownException("org.multipage.generator.messageXdebugBadSession");
		}
	}
		
	/**
	 * Get protocol state.
	 * @return
	 */
	public String getProtocolStateText() {
		
		switch (xdebugProtocolState) {
		case UNINITIALIZED:
			return "UNINITIALIZED";
		case NEGOTIATE_XDEBUG_FEATURES:
			return "NEGOTIATE_XDEBUG_FEATURES";
		case ACCEPT_XDEBUG_COMMANDS:
			return "ACCEPT_XDEBUG_COMMANDS";
		}
		return "UNKNOWN";
	}
	
	/**
	 * Get source code from the Xdebug client.
	 * @return
	 * @throws Exception 
	 */
	public void source(Consumer<String> sourceCodeLambda)
			throws Exception {
		
		int transactionId = createTransaction("source", new String [][] {{"-f", debuggedUri}}, response -> {
			
			try {
				XdebugListenerSession.throwPossibleException(response);
				
				String sourceCode = response.getSourceResult();
				sourceCodeLambda.accept(sourceCode);
			}
			catch (Exception e) {
				onException(e);
			};
		});
		beginTransactionWait(transactionId, RESPONSE_TIMEOUT_MS);
	}
	
	/**
	 * Load Xdebug client contexts.
	 * @throws Exception 
	 */
	public void loadClientContexts(Runnable contextReadyLambda)
			throws Exception {
		
		// Get Area Server contexts.
		contextNameMap = new LinkedHashMap<String, Integer>();
		int transactionId = createTransaction("context_names", null, response -> {
			
			try {
				// Check error response.
				throwPossibleException(response);
				
				contextNameMap = response.getContextNames();
			}
			catch (Exception e) {
				onException(e);
			}		
		});	
		
		// Create new thread. Do not block thread from I/O thread pool.
		new Thread(() -> {
			try {
				// Start transaction and wait for transaction completion.
				beginTransactionWait(transactionId, RESPONSE_TIMEOUT_MS);
				
				// Run callback function.
				if (contextReadyLambda != null) {
					contextReadyLambda.run();
				}
			}
			catch (Exception e) {
				onException(e);
			}
			
		}).start();
	}
	
	/**
	 * Get Area Server stack trace.
	 * @param areaServerStackLambda
	 * @throws Exception 
	 */
	public void stackGet(Function<LinkedList<XdebugStackLevel>, BiFunction<Long, String, BiConsumer<Long, String>>> areaServerStackLambda)
			throws Exception {
		
		try {
			// Get Area Server text state.
			int transactionId = createTransaction("stack_get", null, response -> {
				
				// Process Area Server state response.
				try {
					XdebugListenerSession.throwPossibleException(response);
					
					Obj<Long> processId = new Obj<>();
					Obj<String> processName = new Obj<>();
					Obj<Long> threadId = new Obj<>();
					Obj<String> threadName = new Obj<>();
					
					LinkedList<XdebugStackLevel> stack = response.getXdebugAreaServerStack(processId, processName, threadId, threadName);
					XdebugStackLevel.setSessionReferences(stack, this);
					
					areaServerStackLambda.apply(stack).apply(processId.ref, processName.ref).accept(threadId.ref, threadName.ref);
				}
				catch (Exception e) {
					onException(e);
				}
			});
			
			beginTransactionWait(transactionId, RESPONSE_TIMEOUT_MS);
		}
		catch (Exception e) {
			onThrownException(e);
		}
	}
	
	/**
	 * Load Area Server context properties that can be placed in the debugger watch list.
	 * @param contextId
	 * @param stackLevel
	 * @param watchItemsLambda
	 */
	public void contextGet(int contextId, XdebugStackLevel stackLevel, Consumer<LinkedList<DebugWatchItem>> watchItemsLambda)
			throws Exception {
		
		try {
			// Get state hash code and convert context ID to string value.
			int stateHashCode = stackLevel.getStateHashCode();
			String stateHashText = String.valueOf(stateHashCode);
			String contextIdText = String.valueOf(contextId);
			
			// Get Area Server context properties.
			int transactionId = createTransaction("context_get", new String [][] {{ "-h", stateHashText }, { "-c", contextIdText }}, response -> {
				
				// Process Area Server context properties.
				try {
					throwPossibleException(response);
					
					LinkedList<DebugWatchItem> watchList = response.getContextProperties();
					watchItemsLambda.accept(watchList);
				}
				catch (Exception e) {
					onException(e);
				}
			});
			beginTransactionWait(transactionId, RESPONSE_TIMEOUT_MS);
		}
		catch (Exception e) {
			onThrownException(e);
		}		
	}
	
	/**
	 * Set the "finshed" state.
	 */
	public void setFinished() {
		
		isFinished = true;
		if (packetSession != null) {
			
			packetSession.setFinished();
		}
	}
	
	/**
	 * Close session.
	 */
	public void close() {
		
		try {
			boolean isOpen = client.isOpen();
			if (!isOpen) {
				return;
			}
			
			client.close();
		}
		catch (Exception e) {
			onException(e);
		}
	}
	
	/**
	 * Check whether input object is equal to current object.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (currentClientData == null || !(obj instanceof XdebugListenerSession)) {
			return false;
		}
		
		XdebugListenerSession session = (XdebugListenerSession) obj;
		return currentClientData.equals(session.currentClientData);
	}
	
	/**
	 * Get text description.
	 */
	@Override
	public String toString() {
		
		if (currentClientData == null) {
			String sessionIdText = String.valueOf(sessionId);
			return sessionIdText;
		}
		
		String computerName = currentClientData.getComputer();
		
		String formatText = Resources.getString("org.maclan.server.textDebugSession");
		String description = String.format(formatText, computerName, sessionId);
		return description;
	}
	
	/**
	 * If the sesponse contains error packet, then throw the exception.
	 * @param response
	 * @throws Exception
	 */
	public static void throwPossibleException(XdebugClientResponse response)
			throws Exception {

		// Check error response.
		boolean isError = response.isErrorPacket();
		if (isError) {
			response.throwPacketException();
		}
	}	
	
	/**
	 * Fired on Xdebug exception.
	 * @param messageId
	 * @throws Throwable 
	 */
	private void onThrownException(String messageId)
			throws Exception {
		
		String message = Resources.getString(messageId);
		Exception exception = new Exception(message);
		
		// Delegte the call.
		onThrownException(exception);
	}
	
	/**
	 * Fired on Xdebug exception.
	 * @param e
	 * @throws Throwable 
	 */
	protected void onThrownException(Throwable e)
			throws Exception {
		
		// Override this method.
		e.printStackTrace();
		throw new Exception(e);
	}
	
	/**
	 * Fired on Xdebug exception.
	 * @param messageFormatId
	 * @param parameters
	 * @throws Exception
	 */
	private void onThrownException(String messageFormatId, Object ... parameters)
			throws Exception {
		
		Exception exception = onException(messageFormatId, parameters);
		throw exception;
	}
	
	/**
	 * 
	 * @param messageFormatId
	 * @param parameters
	 */
	private Exception onException(String messageFormatId, Object ... parameters) {
		
		String messageFormat = Resources.getString(messageFormatId);
		String message = String.format(messageFormat, parameters);
		
		Exception exception = new Exception(message);
		onException(exception);
		return exception;
	}
	
	/**
	 * Fired on Xdebug exception.
	 * @param formatMesssageId 
	 * @param e
	 */
	protected void onException(String formatMesssageId, Throwable e) {
		
		// Create new exception.
		String formatMessage = Resources.getString(formatMesssageId);
		String exceptionText = e.getLocalizedMessage();
		String message = String.format(formatMessage, exceptionText);
		Exception exception = new Exception(message);
		
		// Delegate the call.
		onException(exception);
	}
	
	/**
	 * Fired on Xdebug exception.
	 * @param e
	 */
	protected void onException(Throwable e) {
		
		// Override this method.
		e.printStackTrace();
	}
	
	/**
	 * Fired on exception.
	 * @param e
	 * @throws Exception
	 */
	protected void onThrownException(Exception e)
			throws Exception {
		
		// Override this method.
		onException(e);
		throw e;
	}
	
	/**
	 * Fired on exception.
	 * @param e
	 */
	protected void onException(Exception e) {
		
		// Override this method.
		e.printStackTrace();
	}
}
