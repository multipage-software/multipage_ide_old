/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.multipage.gui.Packet;
import org.multipage.gui.PacketBlock;
import org.multipage.gui.PacketElement;
import org.multipage.gui.PacketSession;
import org.multipage.gui.PacketSymbol;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;

/**
 * Xdebug listener session object that stores session states.
 * @author vakol
 *
 */
public class XdebugListenerSession extends DebugListenerSession {
	
	/**
	 * Sesson constants
	 */
	private static final int FEATURE_NEGOTIATION_TIMEOUT_MS = 5000;
	
	/**
	 * Xdebug protocol constants.
	 */
	public static final int UNINITIALIZED = 0;
	public static final int NEGOTIATE_XDEBUG_FEATURES = 1;
	public static final int ACCEPT_XDEBUG_COMMANDS = 2;

	/**
	 * Packet session.
	 */
	private static PacketSession packetSession = null;
	
	/**
	 * Xdebug protocol state.
	 */
	public int xdebugProtocolState = UNINITIALIZED;
	
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
	 * Null symbol.
	 */
	private PacketSymbol nullSymbol = new PacketSymbol(new byte [] { (byte) 0x00 });
	
	/**
	 * Data length.
	 */
	private PacketElement dataLength = new PacketBlock(LENGHT_BUFFER_SIZE, LENGHT_BUFFER_SIZE, nullSymbol, -1);
	
	/**
	 * XML content.
	 */
	private PacketElement xmlBody = new PacketBlock(DATA_BUFFER_SIZE, DATA_BUFFER_SIZE, nullSymbol, -1);
	
	/**
	 * List of debugged URIs.
	 */
	public String debuggedUri = null;
	
	/**
	 * Client parameters.
	 */
	public XdebugClientParameters clientParameters = null;
	
	/**
	 * Xdebug transactions. These commands are either waiting for sending via Xdebug protocol or waiting for
	 * response from debugging client (the debugging probe).
	 */
	public LinkedHashMap<Integer, XdebugTransaction> transactions =  new LinkedHashMap<Integer, XdebugTransaction>();
	
	/**
	 * Feature map for the session.
	 */
	public LinkedHashMap<String, XdebugFeature> features = new LinkedHashMap<String, XdebugFeature>();
	
	/**
	 * Callback invoked when the session is ready for Xdebug commands. It is after feature negotiation.
	 */
	public Runnable onReady = null;
	
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
		
		// Delegate the call.
		super(server, client, xdebugListener);
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
		packetSession = new PacketSession("XdebugListener", socketClient) {

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
			protected void onEndOfPacket(Packet packet) {
				xdebugSession.onEndOfPacket(this, packet);
			}
		};

		return xdebugSession;
	}
	
	/**
	 * Get packet session.
	 * @return
	 */
	public PacketSession getPacketSession() {
		
		return packetSession;
	}
			
	/**
	 * Initialize session using Xdebug client response.
	 * @param clientResponse
	 * @throws Exception 
	 */
	public void initialize(XdebugClientResponse clientResponse) 
			throws Exception {
		
		debuggedUri = clientResponse.getDebuggedUri();
		clientParameters = XdebugClientResponse.parseDebuggedUri(debuggedUri);
	}
	
	/**
	 * Get session thread ID.
	 * @return
	 */
	public long getTid() {

		long threadId = -1L;
		
		if (clientParameters != null) {
			Long tid = clientParameters.tid;
			if (tid != null) {
				threadId = tid;
			}
		}
		return threadId;
	}
	
	/**
	 * Get name of the debugged area.
	 * @return
	 */
	public String getAreaName() {
		
		String areaName = "Unknown";
		
		if (clientParameters != null) {
			String name = clientParameters.areaName;
			if (name != null) {
				areaName = name;
			}
		}
		return areaName;
	}
	
	/**
	 * Get name of the session thread.
	 * @return
	 */
	public String getThreadName() {

		String threadName = "Unknown";
		
		if (clientParameters != null) {
			String name = clientParameters.threadName;
			if (name != null) {
				threadName = name;
			}
		}
		return threadName;
	}
	
	/**
	 * Check if the session is initialized.
	 * @return
	 */
	public boolean isInitialized() {
		
		boolean initialized = (debuggedUri != null && !debuggedUri.isEmpty() && clientParameters != null && clientParameters.isInitialized());
		return initialized;
	}

	/**
	 * Get next packet element.
	 * @param packetSession 
	 */
	protected PacketElement getNewPacketElement(PacketSession packetSession, Packet packet)
			throws Exception {
		
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
	
	/**
	 * Called when the packet block has been read.
	 * @param packetSession 
	 * @param block
	 * @return
	 */
	protected boolean onBlock(PacketSession packetSession, PacketBlock block) {
		
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
				throw new IllegalStateException();
			}
			
			// Save the XML text. 
			packetSession.readPacket.userProperties.put(XML_BODY, xmlText);
			return true;
		}
		
		return false;
	}
	
	/**
	 * On end of the input packet.
	 * @param packetSession 
	 * @param packet
	 */
	protected void onEndOfPacket(PacketSession packetSession, Packet packet) {
		
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
					
				}
			}
		}
		
		dataLength.reset();
		xmlBody.reset();
		packetSession.readPacket.reset();
		packetSession.readPacket.packetParts.clear();
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	@Override
	public long getPid() {
		
		if (clientParameters == null) {
			return -1L;
		}
		return clientParameters.pid;
	}
	
	/**
	 * Send Xdebug command.
	 * @param commandName
	 * @param arguments
	 * @param responseLambda
	 * @return
	 */
	public int createTransaction(String commandName, String [][] arguments, Consumer<XdebugClientResponse> responseLambda) {
		
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
	public int createTransaction(String commandName, String [][] arguments, String textData, Consumer<XdebugClientResponse> responseLambda) {
		
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
	public int createTransaction(String commandName, String [][] arguments, byte [] data, Consumer<XdebugClientResponse> responseLambda) {
		
		// Create new Xdebug command.
		XdebugCommand command = XdebugCommand.create(commandName, arguments, data);
		
		// Prepare new transaction in the current session.
		XdebugTransaction newTransaction = XdebugTransaction.create(command, responseLambda);
		int transactionId = newTransaction.id;
		
		newTransaction.state = XdebugTransactionState.scheduled;
		synchronized (transactions) {
			transactions.put(transactionId, newTransaction);
		}
		
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
			createTransaction(XdebugCommand.FEATURE_GET, new String [][] {{"-n", featureName}}, responsePacket -> {
				
				// Get Xdebug feature and add it to feature map.
				try {
					XdebugFeature feature = responsePacket.getFeatureValue();
					String responseFeatureName = feature.name;
					
					features.put(responseFeatureName, feature);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		// Send command via Xdebug protocol.
		beginTransactions(transactions, FEATURE_NEGOTIATION_TIMEOUT_MS);
	}
	
	/**
	 * Send IDE features to the debugging client (the debugging probe).
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
			
			createTransaction(XdebugCommand.FEATURE_SET, new String [][] {{"-n", featureName}, {"-v", featureValue}},  responsePacket -> {
				
				// Get Xdebug feature and add it to feature map.
				try {
					boolean success = responsePacket.getSettingFeatureResult();
					
					// Callback on last transaction complete.
					int transactionId = responsePacket.getTransactionId();
					if (transactionId >= 0 && transactionId == lastTransactionID.ref && onComplete != null) {
						onComplete.run();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		
		// Send command via Xdebug protocol.
		lastTransactionID.ref = beginTransactions(transactions, FEATURE_NEGOTIATION_TIMEOUT_MS);
	}
	
	/**
	 * Start sending commands via Xdebug protocol.
	 * @param transactions 
	 * @param timeoutMs - if the timeout value is negative the current thread is blocked until all responses are received. 
	 */
	private synchronized int beginTransactions(LinkedHashMap<Integer, XdebugTransaction> transactions, int timeoutMs)
			throws Exception {
		
		// Initialization.
		int lastTransactionId = -1;
		
		// Loop through all transactions.
		synchronized (transactions) {
			for (XdebugTransaction transaction : transactions.values()) {
				lastTransactionId = beginTransaction(transaction);
			}
		}
		
		return lastTransactionId;
	}
	
	/**
	 * Start sending command via Xdebug protocol.
	 * @param transactionId
	 */
	public void beginTransaction(int transactionId) 
			throws Exception {
		
		synchronized (transactions) {
			
			boolean success = transactions.containsKey(transactionId);
			if (!success) {
				return;
			}
			
			XdebugTransaction transaction = transactions.get(transactionId);
			beginTransaction(transaction);
		}
	}
	
	/**
	 * Begin Xdebug transaction. The method sends Xdebug command
	 * @param transaction
	 */
	private int beginTransaction(XdebugTransaction transaction)
			throws Exception {
		
		// Initialization.
		int transactionId = -1;
		
		// Check transaction state.
		if (transaction.state != XdebugTransactionState.scheduled) {
			return transactionId;
		}
		
		try {
			// Get Xdebug command and transaction ID.
			XdebugCommand command = transaction.command;
			transactionId = transaction.id;
			
			// Compile Xdebug command string that will be sent to the Xdebug client.
			CharBuffer commandString = command.compile(transactionId);
			
			// Send command bytes to the Xdebug client. On completion event check if the number of bytes sent macthes.
			CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			
			// Encode packet bytes. Add NULL byte at the end of the message.
			ByteBuffer buffer = encoder.encode(commandString);
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
					
					CharBuffer command = transaction.command.compile(transaction.id);
					
					// Notinfy completion.
					Lock.notify(lockSending);
				}
				
				// On sending error.
				@Override
				public void failed(Throwable exception, XdebugTransaction transaction) {
					
					// Set transaction error.
					transaction.setWriteException(exception);
				}
			});
			
			// Wait for completion of transaction.
			Lock.waitFor(lockSending);
			
			// Set transaction state to "sent".
			transaction.state = XdebugTransactionState.sent;
		}
		catch (Exception e) {
			e.printStackTrace();
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
			
			SwingUtilities.invokeLater(() -> {
				try {
					// Do negotiate.
					negotiateXdebugFeatures(() -> {
						
						// On negotiation completed.
						xdebugProtocolState = ACCEPT_XDEBUG_COMMANDS;
						
						// Invoke callback.
						if (onReady != null) {
							onReady.run();
						}
					});
				}
				catch (Exception e) {
					showMessage("org.multipage.generator.messageXdebugFeatureNegotiationError", e.getLocalizedMessage());
				}
			});
			return;
		}
		
		// On NEGOTIATE FEATURES.
		if (xdebugProtocolState == NEGOTIATE_XDEBUG_FEATURES) {
			// Process feature responses.
			SwingUtilities.invokeLater(() -> {
				try {
					processXdebugCommandResponse(clientResponse);
				}
				catch (Exception e) {
					// Handle exception.
					showException(e);
				}
			});
			return;
		}
		
		// On Xdebug command response.
		if (xdebugProtocolState == ACCEPT_XDEBUG_COMMANDS) {
			// Process command response.
			SwingUtilities.invokeLater(() -> {
				try {
					// Do process command response.
					processXdebugCommandResponse(clientResponse);
				}
				catch (Exception e) {
					// Handle exception.
					showException(e);
				}
			});
		}
	}
	
	/**
	 * Show message.
	 * @param stringResourceId
	 * @param message
	 */
	private void showMessage(String stringResourceId, String message) {
		
		if (listener != null) {
			listener.showMessage(stringResourceId, message);
		}
	}

	/**
	 * Show exception.
	 * @param exception
	 */
	private void showException(Exception exception) {
		
		if (listener != null) {
			listener.showException(exception);
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
		synchronized (transactions) {
			transaction = transactions.get(transactionId);
			transactions.remove(transactionId);
		}
		
		// If the transaction was not found, throw exception.
		if (transaction == null) {
			Utility.throwException("org.maclan.server.messageTransactionNotFound", transactionId);
		}
		
		// Call reponse lambda for the transaction.
		if (transaction.responseLambda != null) {
			transaction.responseLambda.accept(clientResponse);
		}
	}
	
	/**
	 * Negotiate Xdebug features.
	 * @param onComplete
	 * @throws Exception 
	 */
	private void negotiateXdebugFeatures(Runnable onComplete) 
			throws Exception {
		
		// Get Xdebug client (the debugging probe) features and save them into the session state.
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
		
		// Send IDE features to the client (the debugging probe).
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
			Utility.throwException("org.multipage.generator.messageXdebugIdeKeyDoesntMatch",
					foundIdeKey.ref, XdebugClientResponse.MULTIPAGE_IDE_KEY);
		}
		
		// Check debugged application ID.
		Obj<String> foundAppId = new Obj<String>();
		matches = clientResponse.checkAppId(XdebugClientResponse.APPLICATION_ID, foundAppId);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugAppIdDoesntMatch",
					foundAppId.ref, XdebugClientResponse.APPLICATION_ID);
		}
		
		// Check debugged language name.
		Obj<String> languageName = new Obj<String>();
		matches = clientResponse.checkLanguage(XdebugClientResponse.LANGUAGE_NAME, languageName);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugLanguageNameDoesntMatch",
					languageName.ref, XdebugClientResponse.LANGUAGE_NAME);
		}
		
		// Check debugged protocol version.
		Obj<String> protocolVersion = new Obj<String>();
		matches = clientResponse.checkProtocolVersion(XdebugClientResponse.PROTOCOL_VERSION, protocolVersion);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugProtocolVersionDoesntMatch",
					protocolVersion.ref, XdebugClientResponse.PROTOCOL_VERSION);
		}
		
		// Get debugged process URI.
		String debuggedUri = clientResponse.getDebuggedUri();
		if (debuggedUri == null) {
			Utility.throwException("org.multipage.generator.messageXdebugNullFileUri");
		}
		
		// Check debugged URIs.
		if (!debuggedUri.equals(debuggedUri)) {
			Utility.throwException("org.multipage.generator.messageXdebugBadSession");
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
	public void source(Consumer<String> sourceCodeLambda) throws Exception {
		
		int transactionId = createTransaction("source", new String [][] {{"-f", debuggedUri}}, response -> {
			
			try {
				String sourceCode = response.getSourceResult();
				sourceCodeLambda.accept(sourceCode);
			}
			catch (Exception e) {
				e.printStackTrace();
			};
		});
		beginTransaction(transactionId);
	}
	
	/**
	 * Check whether input object is equal to current object.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (clientParameters == null || !(obj instanceof XdebugListenerSession)) {
			return false;
		}
		
		XdebugListenerSession session = (XdebugListenerSession) obj;
		return clientParameters.equals(session.clientParameters);
	}
}
