/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.multipage.gui.Packet;
import org.multipage.gui.PacketBlock;
import org.multipage.gui.PacketChannel;
import org.multipage.gui.PacketElement;
import org.multipage.gui.PacketSession;
import org.multipage.gui.PacketSymbol;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Resources;

/**
 * Xdebug client for Area Server (a client connected to the XdebugServer).
 * @author vakol
 *
 */
public class XdebugClient {
	
	/**
	 * Default client connection timeout in milliseconds.
	 */
	private static final int DEFAUL_CONNECTION_TIMEOUT_MS = 3000;
	
	/**
	 * Response character encoding.
	 */
	private static final Charset RESPONSE_CHARSET = Charset.forName("UTF-8");
	
	/**
	 * Map of constant features.
	 */
	private static final HashMap<String, Object> CONSTANT_FEATURES = new HashMap<>();
	
	/**
	 * Legth of the buffer that stores Xdebug statement.
	 */
	private static final int STATEMENT_BUFFER_SIZE = 1024;
	
	/**
	 * Statement property key.
	 */
	private static final int STATEMENT_PROP = 1;
	
	/**
	 * Null symbol definition for Xdebug protocol.
	 */
	private static final PacketSymbol NULL_SYMBOL = new PacketSymbol(new byte [] { (byte) 0x00 });
	
	/**
	 * Contexts for retrieving Xdebug properties.
	 */
	private static final Map<String, Integer> CONTEXTS = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * List available Xdebug contexts.
	 */
	public static final String AREA_SERVER_CONTEXT = "AreaServer";
	public static final String LOCAL_CONTEXT = "Local";
	public static final String GLOBAL_CONTEXT = "Global";
	public static final String AREA_CONTEXT = "Area";
	
	/**
	 * Packet session.
	 */
	private PacketSession packetSession = null;

	/**
	 * Client socket channel connected to the server.
	 */
	private PacketChannel packetChannel = null;

	/**
	 * Data length element.
	 */
	private PacketElement statementBlock = new PacketBlock(STATEMENT_BUFFER_SIZE, STATEMENT_BUFFER_SIZE, NULL_SYMBOL, -1);
	
	/**
	 * Callback lambda function.
	 */
	private Function<XdebugCommand, XdebugClientResponse> processCommandLambda = null;
	
	/**
	 * Server ready flag.
	 */
	private boolean serverReady = false;
		
	/**
	 * Debugger encoding.
	 */
	private String debuggerEncoding = "UTF-8";
	
	/**
     * Maximum number of debugged child objects.
     */
	private int debuggedChildren = 25;
	
	/**
     * Maximum legth of debugged data.
     */
	private int debuggedData = 50;
	
	/**
     * Maximum number of debugged stack frames.
     */
	private int debuggedDepth = 20;
	
	/**
     * Flags that indicates debugging of hidden langauge objects.
     */
	private boolean debugHiddenObjects = false;
	
	/**
     * Current encoding for data inside the Xdebug.
     */
	private String currentDataEncoding = "base64";	// can be switched to "none" 
	
	/**
	 * Enables Xdebug with multiple sessions.
	 */
	private boolean multipleSessions = true;
	
	/**
	 * Static constructor.
	 */
	static {
        
		// List constant Xdebug features.
		CONSTANT_FEATURES.put("language_supports_threads", "1"); 
		CONSTANT_FEATURES.put("language_name", "Maclan"); 
		CONSTANT_FEATURES.put("language_version", "1.0"); 
		CONSTANT_FEATURES.put("protocol_version", "1"); 
		CONSTANT_FEATURES.put("supports_async", "1"); 
		CONSTANT_FEATURES.put("breakpoint_languages", "Maclan,PHP,JavaScript"); 
		CONSTANT_FEATURES.put("multiple_sessions", "1"); 
		CONSTANT_FEATURES.put("breakpoint_details", "1"); 
		CONSTANT_FEATURES.put("extended_properties", "1"); 
		CONSTANT_FEATURES.put("notify_ok", "1"); 
		CONSTANT_FEATURES.put("resolved_breakpoints", "1"); 
		CONSTANT_FEATURES.put("supports_postmortem", "1");
		
		// List debug contexts.
		CONTEXTS.put(LOCAL_CONTEXT, 0);
		CONTEXTS.put(GLOBAL_CONTEXT, 1);
		CONTEXTS.put(AREA_CONTEXT, 2);
		CONTEXTS.put(AREA_SERVER_CONTEXT, 3);
    }
	
	/**
	 * Create new Xdebug client and connect it to Xdebug listener running on specific host and port.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param areaServerStateLocator
	 * @return
	 */
	public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String areaServerStateLocator)
			throws Exception {

		// Create new client obejct.
		XdebugClient client = new XdebugClient();
		client.connect(ideHostName, xdebugPort, areaServerStateLocator, DEFAUL_CONNECTION_TIMEOUT_MS);
		return client;
	}
	
	/**
	 * Create new Xdebug client and connect it to the Xdebug listener running on specific host and port.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param areaServerStateLocator
	 * @return
	 */
	public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String areaServerStateLocator, int timeoutMs)
			throws Exception {
		
		// Create new client object.
		XdebugClient client = new XdebugClient();
		client.connect(ideHostName, xdebugPort, areaServerStateLocator, timeoutMs);
		return client;
	}
	
	/**
	 * Get list of debugger contexts.
	 * @return
	 */
	public static Map<String, Integer> getContexts() {
		
		return CONTEXTS;
	}
	
	/**
	 * Get context ID by context name.
	 * @return
	 */
	public static int getContextId(String contextName) 
			throws Exception {
		
		Integer contextId = CONTEXTS.get(contextName);
		if (contextId == null) {
			throw new NoSuchElementException();
		}
		return contextId;
	}

	/**
	 * Connect this client to specific host name and port number.
	 * @param ideHostName - host name on which Xdebug listens
	 * @param xdebugPort - port number of Xdebug listener
	 * @param breakPointUri - URI of current Area Server breakpoint
	 * @param timeoutMs - connection timeout in milliseconds
	 */
	private void connect(String ideHostName, int xdebugPort, String breakPointUri, int timeoutMs)
			throws Exception {
		
		// Use packet client channel to connect to IDE debug viewer.
		try {
			Lock lock = new Lock();
			packetChannel = new PacketChannel() {
				@Override
				protected PacketSession onConnected(PacketChannel packetChannel) {
					
					// Create new Xdebug protocol session. Delegate session actions to Xdebug client instance.
					AsynchronousSocketChannel clientSocketChannel = getClientSocketChannel();
					packetSession = new PacketSession("XdebugClient", clientSocketChannel) {
						@Override
						protected PacketElement getNewPacketElement(Packet packet) throws Exception {
							return XdebugClient.this.getNewPacketElement(packet);
						}
						@Override
						protected boolean onBlock(PacketBlock block) throws Exception {
							return XdebugClient.this.onBlock(block);
						}
						@Override
						protected void onEndOfPacket(Packet packet) {
							XdebugClient.this.onEndOfPacket(packet);
						}
						@Override
						protected void onException(Throwable e) {
							XdebugClient.this.onException(e);
						}
					};
					// Send INIT packet to Xdebug server on IDE side.
			        XdebugClientResponse initPacket;
					try {
						initPacket = XdebugClientResponse.createInitPacket(breakPointUri);
						AsynchronousSocketChannel clientChannel = packetChannel.getClientSocketChannel();
						sendResponsePacket(clientChannel, initPacket);
					}
					catch (Exception e) {
						onException(e);
					}
					
					Lock.notify(lock);
					
					return packetSession;
				}
			};
			packetChannel.connect(ideHostName, xdebugPort);
			
			// Wait for connection to be complete.
			boolean ellapsedTimeout = Lock.waitFor(lock, timeoutMs);
			if (ellapsedTimeout) {
				onThrownException("org.maclan.server.messageXdebugConnectionTimeout", ideHostName, xdebugPort);
			}
		}
		catch (Exception e) {
			onThrownException("org.maclan.server.messageXdebugConnectionError",
								   ideHostName, xdebugPort, e.getLocalizedMessage());
		}
	}

	/**
	 * Check if this client is connected to Xdebug server (on IDE side).
	 * @return
	 * @throws IOException 
	 */
	public boolean isConnected() throws Exception {
		
		AsynchronousSocketChannel clientSocketChannel = packetChannel.getClientSocketChannel();

		// Check if the client socket channel exists.
		if (clientSocketChannel == null) {
            return false;
        }
        
        // Check if the client socket channel is connected.
		try {
			SocketAddress clientSocketAddress = clientSocketChannel.getRemoteAddress();
	        if (clientSocketAddress != null) {
	            return true;
	        }
		}
		catch (Exception e) {
		}
		return false;
	}
	
	/**
	 * Close connection.
	 */
	public void close() {
		
		// Close session client socket.
		synchronized (packetSession) {
			packetSession.closeClientSocket();
		}
	}
	
	/**
	 * Get new packet element.
	 * @param packet
	 * @return
	 */
	protected PacketElement getNewPacketElement(Packet packet) {
		
		PacketElement element = null;
		
		// Get the last element in the packet.
		if (!packet.packetParts.isEmpty()) {
			element = packet.packetParts.getLast();
		}
		
		// If the current packet element doesn't exit, initialize packet sequence.
		if (element == null) {
			element = statementBlock;
		}
		// If current packet element is not finished, return current element.
		else if (!element.isCompact) {
			return element;
		}
		else {
			element = null;
		}
		
		// Rteurn current element.
		return element;
	}	
	
	/**
	 * On block received.
	 * @param block
	 * @return
	 */
	protected boolean onBlock(PacketBlock block) {
		
		// Herein read Xdebug statement...
		if (block.equals(statementBlock)) {
			
			// Flip buffer to read its contents.
			block.buffer.flip();
			
			// Read buffer bytes.
			int length = block.buffer.limit();
			byte [] numberBytes = new byte [length];
			block.buffer.get(numberBytes);
			
			// Get statement text.
			String statementText = new String(numberBytes);
			packetSession.readPacket.userProperties.put(STATEMENT_PROP, statementText);
			return true;
		}
		return false;
	}	
	
	/**
	 * On end of packet.
	 * @param packet
	 */
	protected void onEndOfPacket(Packet packet) {
		
		try {
			// Check properties if statement was read.
			boolean success = packet.userProperties.containsKey(STATEMENT_PROP);
			if (!success) {
				return;
			}
			
			// Parse Xdebug statement.
			String statementText = (String) packet.userProperties.get(STATEMENT_PROP);
			
		    // If stop symbol was found in the input buffer, parse the command bytes and process the command.
	    	XdebugCommand command = XdebugCommand.parseCommand(statementText);
	    	
	    	// Process the command and get response packet.
	    	if (processCommandLambda != null) {
		        XdebugClientResponse responsePacket = processCommandLambda.apply(command);
		        
		        // Send response packet back to the debugger server (on the IDE side).
		        AsynchronousSocketChannel clientSocketChannel = packetChannel.getClientSocketChannel();
		        if (responsePacket != null) {
		        	sendResponsePacket(clientSocketChannel, responsePacket);
		        }
		        else {
		        	// Command error response.
		        	sendErrorPacket(clientSocketChannel, command, XdebugError.UNKNOWN_ERROR);
		        }
			}
		}
		catch (Exception e) {
			onException(e);
		}
		
		packet.reset();
		packet.packetParts.clear();
		statementBlock.reset();
	}
	
	/**
	 * Send Xdebug error response.
	 * @param clientSocketChannel
	 * @param command
	 * @param xdebugError
	 * @throws Exception 
	 */
	private void sendErrorPacket(AsynchronousSocketChannel clientSocketChannel, XdebugCommand command, XdebugError xdebugError)
			throws Exception {
		
		// Check socket channel.
		if (clientSocketChannel == null) {
			return;
		}

		synchronized (packetSession) {
			
			// Create error packet and delegate call to send the packet to Xdebug listener.
			XdebugClientResponse response = XdebugClientResponse.createErrorPacket(command, xdebugError, null);
			sendResponsePacket(clientSocketChannel, response);
		}
	}
	
	/**
	 * Send response Xdebug packet.
	 * @param clientSocketChannel
	 * @param response
	 * @throws Exception 
	 */
	private void sendResponsePacket(AsynchronousSocketChannel clientSocketChannel, XdebugClientResponse response)
			throws Exception {
		
		// Check socket channel.
		if (clientSocketChannel == null) {
			return;
		}

		synchronized (packetSession) {
			
			// If the socket channel is closed, do nothing.
			boolean isOpened = clientSocketChannel.isOpen();
			if (!isOpened) {
				return;
			}
			
			try {
				// Get result packet bytes.
				byte [] packetBytes = response.getBytes();
				if (packetBytes == null) {
					return;
				}
				int packetLength = packetBytes.length;
				if (packetLength <= 0) {
					return;
				}
				
				// Get leght of the packet.
				int dataLenght = packetBytes.length;
				String dataLengthString = String.valueOf(dataLenght);
				byte [] dataLengthBytes = dataLengthString.getBytes(RESPONSE_CHARSET);
				
				// Compute buffer size and allocate the buffer.
				int bufferSize = dataLengthBytes.length + XdebugClientResponse.NULL_SIZE + packetLength + XdebugClientResponse.NULL_SIZE;
				ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
				
				// Put bytes into the buffer. 
				buffer.put(dataLengthBytes);
				buffer.put(XdebugClientResponse.NULL_SYMBOL);
				buffer.put(packetBytes);
				buffer.put(XdebugClientResponse.NULL_SYMBOL);
				buffer.flip();
				
				// Write the buffer with response bytes into socket channel.
				Future<Integer> sent = clientSocketChannel.write(buffer);
				/*int sentBytes =*/ sent.get();
			}
			catch (Exception e) {
				onThrownException(e);
			}
		}
	}
	
	/**
     * Accept debug commands with input lambda function.
	 * @throws Exception 
     */
	public void setAcceptCommands(Function<XdebugCommand, XdebugClientResponse> processCommandLambda)
			throws Exception {
		
		this.processCommandLambda = processCommandLambda;
	}
	
	/**
	 * Xdebug client entry point that accepts incomming commands and sends responses to them.
	 * @param server
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public XdebugClientResponse xdebugClient(AreaServer server, XdebugCommand command)
			throws Exception {
		
		// Get command name.
		String name = command.getName();
		String commandName = name;
		
		// Get debugger information.
		DebugInfo debugInfo = null;
		AreaServerState state = server.state;
		if (state != null) {
			debugInfo = state.debugInfo;
		}
		if (debugInfo == null) {
			return null;
		}
		
		// Rules for incomming command actions that are processed with the debug client...
		
		// On getting Xdebug feature.
		if ("feature_get".equals(commandName)) {
			
			XdebugClientResponse resultPacket = createFeatureGetResponse(command, server);
            return resultPacket;
        }
		// On setting the Xdebug feature.
		else if ("feature_set".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getFeatureSetResponse(command, server);
            return resultPacket;
        }
		// On getting source code.
		else if ("source".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getSourceCodeResponse(command, server);
            return resultPacket;
        }
		// On getting list of contexts.
		else if ("context_names".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getContextNamesResponse(command, server);
            return resultPacket;
		}
		// On getting property.
		else if ("property_get".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getPropertyResponse(command, server);
            return resultPacket;
		}
		// On setting property.
		else if ("property_set".equals(commandName)) {
			
			String propertyName = command.getArgument("-n");
			
			if ("server_ready".equals(propertyName)) {
				setServerReady();
			}
			
			XdebugClientResponse resultPacket = setPropertyResponse(command, server);
            return resultPacket;
		}
		// On evaluating an expression.
		else if ("expr".equals(commandName)) {

			XdebugClientResponse resultPacket = getExprResponse(command, server);
			return resultPacket;
		}
		// On continue running Area Server.
		else if ("run".equals(commandName)) {
			
			// Set the run operation.
			debugInfo.setDebugOperation(XdebugOperation.run);
			
			// Get run response.
			XdebugClientResponse resultPacket = continuationCommandResponse(command, server);
            return resultPacket;
		}
		// On step into command.
		else if ("step_into".equals(commandName)) {
			
			// Set the step into operation.
			debugInfo.setDebugOperation(XdebugOperation.step_into);
			
			// Get step into response.
			XdebugClientResponse resultPacket = continuationCommandResponse(command, server);
            return resultPacket;
		}
		// On step over command.
		else if ("step_over".equals(commandName)) {
			
			// Set the step over operation.
			debugInfo.setDebugOperation(XdebugOperation.step_over);
			
			// Get step over response.
			XdebugClientResponse resultPacket = continuationCommandResponse(command, server);
            return resultPacket;
		}
		// On step out command.
		else if ("step_out".equals(commandName)) {
			
			// Set the step out operation.
			debugInfo.setDebugOperation(XdebugOperation.step_out);
			AreaServerState parentState = server.state.parentState;
			if (parentState != null) {
				
				// Reset debug client.
				debugInfo.setDebugClient(null);
			}
					
			// Get step out response.
			XdebugClientResponse resultPacket = continuationCommandResponse(command, server);
            return resultPacket;
		}
		// On stop running Area Server.
		else if ("stop".equals(commandName)) {
			
			// Set the stop operation.
			debugInfo.setDebugOperation(XdebugOperation.stop);
			
			// Get stop response.
			XdebugClientResponse resultPacket = continuationCommandResponse(command, server);
            return resultPacket;
		}
		// On get stack information.
		else if ("stack_get".equals(commandName)) {
			
			// Get stack response.
			XdebugClientResponse resultPacket = getStackGetResponse(command, server);
			return resultPacket;
		}
		// On get context information.
		else if ("context_get".equals(commandName)) {
			
			// Get context properties response.
			XdebugClientResponse resultPacket = getContextGetResponse(command, server);
            return resultPacket;
		}
        
		return null;
	}

	/**
     * Create Xdebug feature packet.
     * @param command
     * @param areaServer
     * @return
	 * @throws Exception 
     */
	private XdebugClientResponse createFeatureGetResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get feature name.
		String featureName = command.getArgument("-n");
		
		// Try to find the constant feature.
		Object featureValue = CONSTANT_FEATURES.get(featureName);
        if (featureValue == null) {
        	
        	// Get variable features.
        	switch (featureName) {
        	case "encoding":
        		featureValue = getCurrentEncoding();
        		break;
        	case "supported_encodings":
            	featureValue = AreaServer. getSupportedEncodings();
        		break;
        	case "data_encoding":
        		featureValue = getCurrentDataEncoding();
                break;
            case "breakpoint_types":
            	featureValue = getBreakpointTypes(areaServer);
                break;
            case "max_children":
            	featureValue = getMaxDebuggedChildren();
                break;
            case "max_data":
            	featureValue = getMaxDebuggedData();
                break;
            case "max_depth":
            	featureValue = getMaxDebuggedDepth();
                break;
            case "show_hidden":
            	featureValue = getDebugHidden();
            	break;
        	default:
        		featureValue = Utility.newException("org.maclan.server.messageXdebugFeatureNot", featureName);
        	}
        }
        
        // Create response packet.
        XdebugClientResponse featurePacket = XdebugClientResponse.createGetFeatureResult(command, featureValue);
		return featurePacket;
	}

	/**
	 * Set Xdebug feature and get response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getFeatureSetResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get feature name and value.
		String featureName = command.getArgument("-n");
		String featureValue = command.getArgument("-v");
		
		boolean success = false;
		
    	switch (featureName) {
	    case "encoding":
	    	setCurrentEncoding(featureValue);
	    	success = true;
	    	break;
		case "multiple_sessions":
			setMultipleSessions(featureValue);
			success = true;
			break;
		case "max_children":
			setMaxDebuggedChildren(featureValue);
			success = true;
			break;
		case "max_data":
			setMaxDebuggedData(featureValue);
			success = true;
			break;
		case "max_depth":
			setMaxDebuggedDepth(featureValue);
			success = true;
			break;
		case "show_hidden":
			setDebugHidden(featureValue);
			success = true;
			break;			
		case "breakpoint_details":
		case "extended_properties":
		case "notify_ok":
			CONSTANT_FEATURES.put(featureName, featureValue);
			success = true;
			break;
    	}
    	
    	// Create response packet.
        XdebugClientResponse setFeatureResponse = XdebugClientResponse.createSetFeatureResult(command, success);
		return setFeatureResponse;
	}
	
	/**
	 * Set server ready.
	 */
	public void setServerReady() {
		
		serverReady = true;
	}
	
	/**
	 * Get server ready flag.
	 * @return
	 */
	public boolean isServerReady() {
		
		return serverReady;
	}
	
	/**
	 * Notify server that a breakpoint has been resolved.
	 * @throws Exception 
	 */
	public void notifyBreakpointResolved()
			throws Exception {
		
		// Create notification packet.
		XdebugClientResponse notification = XdebugClientResponse.createBreakpointNotification();
		
		// Send notification to the server.
    	AsynchronousSocketChannel clientSocketChannel = packetChannel.getClientSocketChannel();
    	sendResponsePacket(clientSocketChannel, notification);
	}
	
	/**
	 * Get response packet with source code.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getSourceCodeResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
    	String sourceCode = areaServer.state.text.toString();
    	
		// Create response packet.
        XdebugClientResponse sourceResponse = XdebugClientResponse.createSourceResult(command, sourceCode);
		return sourceResponse;
	}
	
	/**
	 * Get response packet with context names.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getContextNamesResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		XdebugClientResponse response = XdebugClientResponse.createContextNamesResult(command, areaServer);
		return response;
	}
	
	/**
	 * Get response with property values.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getPropertyResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get property name and context ID.
		String propertyName = command.getArgument("-n");
		
		int contextId = -1;
		String contextIdText = command.getArgument("-c");
		if (contextIdText != null) {
			contextId = Integer.parseInt(contextIdText);
		}
		
		if (contextId < 0) {
			String name = command.getName();
			onThrownException("org.maclan.server.messageMissingXdebugContext", name, propertyName);
		}
		
		XdebugClientResponse response = null;
		
		int areaServerContextId = CONTEXTS.get(AREA_SERVER_CONTEXT);
		int localContextId = CONTEXTS.get(LOCAL_CONTEXT);
		int globalContextId = CONTEXTS.get(GLOBAL_CONTEXT);
		int areaContextId = CONTEXTS.get(AREA_CONTEXT);

		// For Area Server context.
		if (contextId == localContextId) {
			
			String propertyType = command.getArgument("-t");
			
			// For block properties...
			if (DebugWatchItemType.blockVariable.checkTypeName(propertyType)) {
				
				// Find Area State with given hash code.
				String stateHashCodeText = command.getArgument("-h");
				if (stateHashCodeText == null || stateHashCodeText.isEmpty()) {
					onThrownException("org.maclan.server.messageXdebugAreaStateHashCode");
				}
				int stateHashCode = Integer.parseInt(stateHashCodeText);
				
				AreaServerState state = areaServer.findState(stateHashCode);
				if (state == null) {
					onThrownException("org.maclan.server.messageXdebugAreaUnknownStateHashCode", stateHashCode);
				}
				
				// Create reponse.
				response = XdebugClientResponse.createBlockVariableResponse(command, state, propertyName);
			}
		}
		
		// Return response. If it is null, the error packet is sent to the server.
		return response;
	}
	
	/**
	 * Get response with property values.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse setPropertyResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get property name and context ID.
		String propertyName = command.getArgument("-n");
		
		XdebugClientResponse response = XdebugClientResponse.createSetPropertyResponse(command, propertyName);
		return response;
	}
	
	/**
	 * Run expr() command and return response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getExprResponse(XdebugCommand command, AreaServer areaServer) 
			throws Exception {
		
		// Get expression to evaluate with Area Server.
		byte [] data = command.getData();
		String dataText = new String(data);
		String exprResultText = areaServer.evaluateText(dataText, String.class, true);
		
		// Create response packet.
		XdebugClientResponse exprResponse = XdebugClientResponse.createExprResult(command, exprResultText);
		return exprResponse;
	}
	
	/**
	 * On continuation command release debug thread lock to run the code. Create command response.
	 * @param command
	 * @param server
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse continuationCommandResponse(XdebugCommand command, AreaServer server)
			throws Exception {
		
		AreaServerState state = server.state;
		if (state == null) {
			return null;
		}
		DebugInfo debugInfo = state.debugInfo;
		if (debugInfo == null) {
			return null;
		}
		
		// Unlock current Area Server thread.
		Lock debuggerLock = debugInfo.getDebuggerLock();
		if (debuggerLock != null) {
			Lock.notify(debuggerLock);
		}
		
		// Create response packet.
		XdebugClientResponse continuationResponse = XdebugClientResponse.createContinuationCommandResult(command);
		return continuationResponse;
	}
	
	/**
	 * On stack get command. Creates command response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getStackGetResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get current thread ID and name.
		DebugInfo debugInfo = areaServer.state.debugInfo;
		
		long processId = -1L;
		String processName = "";
		long threadId = -1L;
		String threadName = "";
		
		if (debugInfo != null) {
			
			processId = debugInfo.getProcessId();
			processName = debugInfo.getProcessName();
			
			threadId = debugInfo.getThreadId();
			threadName = debugInfo.getThreadName();
		}
		
		// Get stack list.
		LinkedList<XdebugStackLevel> stack = new LinkedList<XdebugStackLevel>();
		AreaServerState state = areaServer.state;
		
		int levelNumber = 0;
		while (state != null) {
			
			stack.add(new XdebugStackLevel(levelNumber, "eval", state));
			
			state = state.parentState;
			levelNumber++;
		}
		
		// Create response packet.
		XdebugClientResponse stackGetResponse = XdebugClientResponse.createStackGetResult(command, processId, processName,
				threadId, threadName, stack);
		return stackGetResponse;
	}
	
	/**
	 * On context get command. Creates command response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getContextGetResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Initialization.
		LinkedList<DebugWatchItem> watchItems = new LinkedList<DebugWatchItem>();
		String commandName = command.getName();

		// Get context ID and Area Server state hash.
		String contextIdText = command.getArgument("-c");
		if (contextIdText == null) {
			onThrownException("org.maclan.server.messageXdebugMissingContextId", commandName);
		}
		int contextId = -1;
		try {
			contextId = Integer.parseInt(contextIdText);
		}
		catch (Exception e) {
			onThrownException(e, "org.maclan.server.messageXdebugBadContextId", commandName);
		}

		String stateHashText = command.getArgument("-h");
		if (stateHashText == null) {
			onThrownException("org.maclan.server.messageXdebugMissingStateHashCode", commandName);
		}
		int stateHash = -1;
		try {
			stateHash = Integer.parseInt(stateHashText);
		}
		catch (Exception e) {
			onThrownException(e, "org.maclan.server.messageXdebugBadStateHashCode", commandName);
		}
		
		// Find Area Server state by its hash code.
		AreaServerState state = areaServer.findState(stateHash);
		if (state == null) {
			
			// Create null state response.
			XdebugClientResponse nullContextResponse = XdebugClientResponse.createContextGetNullResult(command);
			return nullContextResponse;
		}
		
		// Load tag properties, block variables and block procedures.
		loadTagProperties(watchItems, contextId, state);
		loadBlockVariables(watchItems, contextId, state);
		loadBlockProcedures(watchItems, contextId, state);
		
		// Create response packet.
		XdebugClientResponse contextGetResponse = XdebugClientResponse.createContextGetResult(command, watchItems);
		return contextGetResponse;
	}

	/**
	 * Load tag properties.
	 * @param watchItems
	 * @param contextId
	 * @param state
	 */
	private void loadTagProperties(LinkedList<DebugWatchItem> watchItems, int contextId, AreaServerState state) {
		
		// TODO: <---MAKE Load current tag properties.
		
	}
	
	/**
	 * Load block variables.
	 * @param watchItems
	 * @param contextId
	 * @param state
	 */
	private void loadBlockVariables(LinkedList<DebugWatchItem> watchItems, int contextId, AreaServerState state) {
		
		// Get names of accessible block variables and add them to the watch list.
		LinkedList<DebugWatchItem> watchedVariables = state.blocks.getVariableWatchList();
		watchItems.addAll(watchedVariables);
	}
	
	/**
	 * Load block procedures.
	 * @param watchItems
	 * @param contextId
	 * @param state
	 */
	private void loadBlockProcedures(LinkedList<DebugWatchItem> watchItems, int contextId, AreaServerState state) {
		
		// Get names of accessible block procedures and add them to the watch list.
		LinkedList<DebugWatchItem> watchedProcedures = state.blocks.getLocalProcedureWatchList();
		watchItems.addAll(watchedProcedures);
	}

	/**
	 * Converts input text to Xdebug value of boolean type.
	 * @param valueText
	 * @return
	 * @throws Exception
	 */
	private boolean getBooleanValue(String valueText)
			throws Exception {
		
		if ("0".equals(valueText)) {
			return true;
		}
		else if ("1".equals(valueText)) {
			return false;
		}
		
		onThrownException("org.maclan.server.messageXdebugNotBoolean", valueText);
		return false;
	}
	
	/**
	 * Set current Xdebug session encoding.
	 * @param debuggerEncoding
	 */
	private void setCurrentEncoding(String debuggerEncoding) {
		
		this.debuggerEncoding = debuggerEncoding;
	}
	
	/**
	 * Get current Xdebug session encoding.
	 * @return
	 */
	private String getCurrentEncoding() {
		
		return debuggerEncoding;
	}
	
	/**
	 * Set multiple sessions flag.
	 * @param multipleSessionsText
	 * @throws Exception 
	 */
	private void setMultipleSessions(String multipleSessionsText)
			throws Exception {
		
		multipleSessions = getBooleanValue(multipleSessionsText);
	}
	
	/**
	 * Set multiple sessions flag.
	 * @param debuggedChildrenText
	 */
	private void setMaxDebuggedChildren(String debuggedChildrenText) 
			throws Exception {
		
		debuggedChildren = Integer.valueOf(debuggedChildrenText);
	}

	/**
	 * Get the number of debugged object children.
	 * @return
	 */
	private int getMaxDebuggedChildren() {
		
		return debuggedChildren;
	}
	
	/**
	 * Set the number of debugged object data.
	 * @param debuggedDataText
	 */
	private void setMaxDebuggedData(String debuggedDataText) {
		
		debuggedChildren = Integer.valueOf(debuggedDataText);
	}
	
	/**
	 * Get the number of debugged object data.
	 * @return
	 */
	private int getMaxDebuggedData() {
		
		return debuggedData;
	}
	
	/**
	 * Set maximum debugged stack depth.
	 * @param debuggedDepthText
	 */
	private void setMaxDebuggedDepth(String debuggedDepthText) {
		
		debuggedDepth = Integer.valueOf(debuggedDepthText);
	}
	
	/**
	 * Get maximum debugged stack depth.
	 * @return
	 */
	private int getMaxDebuggedDepth() {
		
		return debuggedDepth;
	}
	
	/**
	 * Set flag that indicates whether to debug hidden objects.
	 * @param debugHiddenText
	 * @throws Exception 
	 */
	private void setDebugHidden(String debugHiddenText)
			throws Exception {
		
		debugHiddenObjects = getBooleanValue(debugHiddenText);
	}
	
	/**
	 * Get flag that indicates whether to debug hidden objects.
	 * @return
	 */
	private int getDebugHidden() {
		
		return debugHiddenObjects ? 1 : 0;
	}
	
	/**
	 * Get current breakpoint types.
	 * @param areaServer
	 * @return
	 */
	private String getBreakpointTypes(AreaServer areaServer) {
		
		return "line"; // also other types: call, return, exception, conditional, watch.
	}
	
	/**
	 * Get current data encoding (base64 or none).
	 * @return
	 */
	private String getCurrentDataEncoding() {
		
		return currentDataEncoding;
	}
	
	/**
	 * On exception.
	 * @param messageFormatId
	 * @param parameters
	 */
	private void onThrownException(String messageFormatId, Object ... parameters)
			throws Exception {
		
		// Delegate the call.
		onThrownException(null, messageFormatId, parameters);
	}
	
	/**
	 * On exception.
	 * @param e
	 * @param messageFormatId
	 * @param parameters
	 */
	private void onThrownException(Exception e, String messageFormatId, Object ... parameters)
			throws Exception {
		
		String messageFormat = Resources.getString(messageFormatId);
		String message = String.format(messageFormat, parameters);
		
		if (e != null) {
			message = e.getLocalizedMessage() + ' ' + message;
		}
		
		Exception exception = new Exception(message);
		onException(exception);
		throw exception;
	}
	
	/**
	 * On exception.
	 * @param e
	 */
	protected void onThrownException(Throwable e)
			throws Exception {
		
		Exception exception = new Exception(e);
		onException(exception);
		throw exception;		
	}

	/**
	 *  On exception. 
	 * @param e
	 */
	protected void onException(Throwable e) {
		
		// Override this method. 
		e.printStackTrace();
	}
}
