/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 03-05-2023
 *
 */
package org.maclan.server;

import java.io.StringReader;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
	 * Xdebug protocol state.
	 */
	public int xdebugProtocolState = UNINITIALIZED;
	
	/**
	 * Incomming buffer size.
	 */
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int LENGTH_BUFFER_SIZE = 16;
	private static final int PACKET_BUFFER_SIZE = INPUT_BUFFER_SIZE;
	
	/**
	 * Input and packet buffers that can receive Xdebug packets comming from socket connection.
	 */
	public ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
	public Obj<ByteBuffer> lengthBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(LENGTH_BUFFER_SIZE));
	public Obj<ByteBuffer> xmlBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(PACKET_BUFFER_SIZE));
	
	/**
	 * Input reader states and constants.
	 */
	private static final int READ_LENGTH = 0;
	private static final int READ_XML = 1;
	
	private int inputReaderState = READ_LENGTH;
	
	/**
	 * Length of the XML packet received from the input socket.
	 */
	private int xmlLength = -1;
	
	/**
	 * Input buffer synchronization object.
	 */
	protected Object inputBufferSync = new Object();
	
	/**
	 * List of debugged URIs.
	 */
	public String debuggedUri = null;
	
	/**
	 * Xdebug listener.
	 */
	public XdebugListener listener = null;
	
	/**
	 * Client parameters.
	 */
	public XdebugClientParameters clientParameters = null;
	
	/**
	 * Xdebug transactions. These commands are either waiting for sending via Xdebug protocol or waiting for
	 * response from debugging client (the debugging probe).
	 */
	public LinkedHashMap<Integer, XdebugTransaction> transactions = new LinkedHashMap<Integer, XdebugTransaction>();
	
	/**
	 * Feature map for the session.
	 */
	public LinkedHashMap<String, XdebugFeature> features = new LinkedHashMap<String, XdebugFeature>();
	
	/**
	 * Cosntructor.
	 * @param server 
	 * @param client
	 * @throws Exception 
	 */
	public XdebugListenerSession(AsynchronousServerSocketChannel server, AsynchronousSocketChannel client)
			throws Exception {
		
		// Delegate the call.
		super(server, client);
	}
	
	/**
	 * Read Xdebug responses from the input buffer.
	 * @param xdebugResponseLambda
	 * @return returns true if the XML has been received
	 * @throws Exception
	 */
	public boolean readXdebugResponses(Consumer<XdebugResponse> xdebugResponseLambda)
			throws Exception {
		
		// Prepare input buffer for reading.
		inputBuffer.flip();
		
		// If there are no remaining bytes in the input buffer, return false value.
		if (!inputBuffer.hasRemaining()) {
			
			// TODO: <---DEBUG
			j.log("EMPTY BUFFER");
			return false;
		}
		
		boolean xmlAccepted = false;
		
		// Read until end of input buffer.
		boolean endOfReading = false;
		while (!endOfReading) {
			
			Obj<Boolean> terminated = new Obj<Boolean>(false);
			
			// Determine protocol state from input byte value and invoke related action.
			switch (inputReaderState) {
			
			case READ_LENGTH:
				endOfReading = Utility.readUntil(inputBuffer, lengthBuffer, LENGTH_BUFFER_SIZE, XdebugResponse.NULL_SYMBOL, terminated);
				if (terminated.ref) {
					inputReaderState = READ_XML;
					xmlLength = getXmlLength(lengthBuffer.ref);
				}
				break;
				
			case READ_XML:
				endOfReading = Utility.readUntil(inputBuffer, xmlBuffer, PACKET_BUFFER_SIZE, XdebugResponse.NULL_SYMBOL, terminated);
				if (terminated.ref) {
					inputReaderState = READ_LENGTH;
					XdebugResponse xdebugResponse = getXmlContent(xmlBuffer.ref);
					xdebugResponseLambda.accept(xdebugResponse);
					xmlAccepted = true;
				}
				break;
			}
		}
		
		return xmlAccepted;
	}
	
	/**
	 * Get XML length.
	 * @param lengthBuffer
	 * @return
	 * @throws Exception 
	 */
	private int getXmlLength(ByteBuffer lengthBuffer)
			throws Exception {
		
		// Prepare the length buffer for reading the XML length.
		lengthBuffer.flip();
		
		// Get the length of the buffer contents. Create byte array to hold the buffer contents.
		int arrayLength = lengthBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents into the byte array.
		lengthBuffer.get(bytes);
		
		// Convert bytes into UTF-8 encoded string.
		String lengthText = new String(bytes, "UTF-8");
		
		// Get the length of the XML from the string.
		int xmlLength = Integer.parseInt(lengthText);
		
		// Reset the length buffer.
		lengthBuffer.clear();
		
		// Return result.
		return xmlLength;
	}
	
	/**
	 * Get Xdebug XML response.
	 * @param xmlBuffer
	 * @return
	 * @throws Exception 
	 */
	private XdebugResponse getXmlContent(ByteBuffer xmlBuffer)
				throws Exception {
		
		// Prepare the XML buffer for reading the XML content.
		xmlBuffer.flip();
		
		// Get the length of the XML buffer. Create byte array to hold the buffer contents.
		int arrayLength = xmlBuffer.limit();
		byte [] bytes = new byte [arrayLength];
		
		// Read buffer contents into the byte array.
		xmlBuffer.get(bytes);
		
		// Convert bytes into UTF-8 encoded string, the XML.
		String xmlText = new String(bytes, "UTF-8");
		
        // Parse the XML string into a Document.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document xml = builder.parse(new InputSource(new StringReader(xmlText)));
		
        // Create new packet object.
    	XdebugResponse xmlResponse = new XdebugResponse(xml);
    	
    	// Reset the XML buffer.
    	xmlBuffer.clear();
    	
    	// Return XML response.
    	return xmlResponse;
	}
	
	/**
	 * initialize session using input packet.
	 * @param inputPacket
	 * @throws Exception 
	 */
	public void initialize(XdebugResponse inputPacket) 
			throws Exception {
		
		debuggedUri = inputPacket.GetDebuggedUri();
		clientParameters = XdebugResponse.parseDebuggedUri(debuggedUri);
	}
	
	/**
	 * Get process ID.
	 * @return
	 */
	@Override
	public String getPid() {
		
		if (clientParameters == null) {
			return "";
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
	public int createTransaction(String commandName, String [][] arguments, Consumer<XdebugResponse> responseLambda) {
		
		// Create new Xdebug command.
		XdebugCommand command = XdebugCommand.create(commandName, arguments);
		
		// Prepare new transaction in the current session.
		XdebugTransaction newTransaction = XdebugTransaction.create(command, responseLambda);
		int transactionId = newTransaction.id;
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
			createTransaction(XdebugCommand.FEATURE_GET, new String [][] {{"-n", featureName}}, responsePacket -> {
				finishTransaction(responsePacket, nextTransaction -> {
					
					// Get feature.
					j.log("FINISH TRANSACTION");
				});
			});
		}
		// Send command via Xdebug protocol.
		beginTransactions(transactions, FEATURE_NEGOTIATION_TIMEOUT_MS);
	}

	/**
	 * Start sending commands via Xdebug protocol.
	 * @param transactions 
	 * @param timeoutMs - if the timeout value is negative the current thread is blocked until all responses are received. 
	 */
	private synchronized void beginTransactions(LinkedHashMap<Integer, XdebugTransaction> transactions, int timeoutMs)
			throws Exception {
		
		// Loop through all transactions.
		for (XdebugTransaction transaction : transactions.values()) {
			beginTransaction(transaction);
		}
	}
	
	/**
	 * Begin Xdebug transaction. The method sends Xdebug command
	 * @param transaction
	 */
	private void beginTransaction(XdebugTransaction transaction)
		throws Exception {
		
		try {
			// Get Xdebug command and transaction ID.
			XdebugCommand command = transaction.command;
			int transactionId = transaction.id;
			
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
					
					// TODO: <---DEBUG Display number of bytes written.
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Process Xdebug responses.
	 * @param listener
	 * @param inputPacket
	 * @throws Exception 
	 */
	public void processXdebugResponse(XdebugResponse inputPacket)
			throws Exception {
		
		// On INIT packet.
		if (xdebugProtocolState == UNINITIALIZED && inputPacket.isInit()) {
			
			// Check session.
			checkXdebugSession(listener, inputPacket);
			// Start negotiating features.
			xdebugProtocolState = NEGOTIATE_XDEBUG_FEATURES;
			
			SwingUtilities.invokeLater(() -> {
				try {
					// Do negotiate.
					negotiateXdebugFeatures();
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
					processXdebugFeatureResponse(inputPacket);
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
					processXdebugCommandResponse(inputPacket);
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
	 * Process Xdebug feature response.
	 * @param inputPacket
	 * @return
	 * @throws Exception 
	 */
	private void processXdebugFeatureResponse(XdebugResponse inputPacket)
			throws Exception {
		
		// Get transaction ID.
		int transactionId = inputPacket.getTransactionId();
		
		// Find transaction object with matching transaction ID.
		XdebugTransaction transaction = transactions.get(transactionId);
		
		// If the transaction was not found, throw exception.
		if (transaction == null) {
			Utility.throwException("org.maclan.server.messageTransactionNotFound", transactionId);
		}
		
		// Get Xdebug feature and add it to feature map.
		XdebugFeature feature = inputPacket.getFeature();
		String featureName = feature.name;
		features.put(featureName, feature);
	}
	
	/**
	 * Process Xdebug command response.
	 * @param inputPacket
	 */
	private void processXdebugCommandResponse(XdebugResponse inputPacket)
			throws Exception {
		
		// Get transaction ID.
		int transactionId = inputPacket.getTransactionId();
		
		// Find transaction object with matching transaction ID.
		XdebugTransaction transaction = transactions.get(transactionId);
		
		// If the transaction was not found, throw exception.
		if (transaction == null) {
			Utility.throwException("org.maclan.server.messageTransactionNotFound", transactionId);
		}
		
		// Call reponse lambda for the transaction.
		transaction.responseLambda.accept(inputPacket);
	}
	
	/**
	 * Negotiate Xdebug features.
	 * @throws Exception 
	 */
	private void negotiateXdebugFeatures() 
			throws Exception {
		
		// Get Xdebug client (the debugging probe) features and save them into the session state.
		loadFeaturesFromClient(
				"language_supports_thread", 
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
				{"max_children", "SESSION"},
				{"max_data", "SESSION"},
				{"max_depth", "SESSION"},
				{"breakpoint_details", "1"},
				{"extended_properties", "0"},
				{"notify_ok", "1"},
				{"show_hidden", "1"}});
	}

	/**
	 * Initialize Xdebug session.
	 * @param listener 
	 * @param inputPacket
	 * @throws Exception 
	 */
	private void checkXdebugSession(XdebugListener listener, XdebugResponse inputPacket)
			throws Exception {
		
		// Check IDE key.
		Obj<String> foundIdeKey = new Obj<String>();
		boolean matches = inputPacket.checkIdeKey(XdebugResponse.MULTIPAGE_IDE_KEY, foundIdeKey);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugIdeKeyDoesntMatch",
					foundIdeKey.ref, XdebugResponse.MULTIPAGE_IDE_KEY);
		}
		
		// Check debugged application ID.
		Obj<String> foundAppId = new Obj<String>();
		matches = inputPacket.checkAppId(XdebugResponse.APPLICATION_ID, foundAppId);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugAppIdDoesntMatch",
					foundAppId.ref, XdebugResponse.APPLICATION_ID);
		}
		
		// Check debugged language name.
		Obj<String> languageName = new Obj<String>();
		matches = inputPacket.checkLanguage(XdebugResponse.LANGUAGE_NAME, languageName);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugLanguageNameDoesntMatch",
					languageName.ref, XdebugResponse.LANGUAGE_NAME);
		}
		
		// Check debugged protocol version.
		Obj<String> protocolVersion = new Obj<String>();
		matches = inputPacket.checkProtocolVersion(XdebugResponse.PROTOCOL_VERSION, protocolVersion);
		if (!matches) {
			Utility.throwException("org.multipage.generator.messageXdebugProtocolVersionDoesntMatch",
					protocolVersion.ref, XdebugResponse.PROTOCOL_VERSION);
		}
		
		// Get debugged process URI.
		String debuggedUri = inputPacket.GetDebuggedUri();
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
	 * Finish transaction.
	 * @param responsePacket
	 */
	private void finishTransaction(XdebugResponse responsePacket, Consumer<XdebugTransaction> finishTransactionLambda) {
		

	}

	/**
	 * Send IDE features to the debugging client (the debugging probe).
	 * @param ideFeatureValues
	 */
	public void sendIdeFeaturesToClient(String[][] ideFeatureValues) {
		// TODO Auto-generated method stub
		
	}
}
