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
import java.util.HashMap;
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

/**
 * Xdebug probe for Area Server (a client connected to the XdebugServer).
 * @author vakol
 *
 */
public class XdebugClient {
	
	/**
	 * Default client connection timeout in milliseconds.
	 */
	private static final int DEFAUL_CONNECTION_TIMEOUT_MS = 3000;
	
	/**
	 * Response character set.
	 */
	private static final Charset RESPONSE_CHARSET = Charset.forName("UTF-8");
	
	/**
	 * Map of constant features.
	 */
	private static final HashMap<String, Object> CONSTANT_FEATURES = new HashMap<String, Object>();
	
	/**
	 * Legth of the buffer that stores Xdebug statement.
	 */
	private static final int STATEMENT_BUFFER_SIZE = 1024;
	
	/**
	 * Key to statement property.
	 */
	private static final int STATEMENT_PROP = 1;
	
	/**
	 * Null symbol.
	 */
	private PacketSymbol nullSymbol = new PacketSymbol(new byte [] { (byte) 0x00 });
	
	/**
	 * Packet reader.
	 */
	private PacketSession packetSession = null;

	/**
	 * Client socket channel connected to server.
	 */
	private PacketChannel packetChannel = null;

	/**
	 * Data length.
	 */
	private PacketElement statementBlock = new PacketBlock(STATEMENT_BUFFER_SIZE, STATEMENT_BUFFER_SIZE, nullSymbol, -1);
	
	/**
	 * Callback lambda function.
	 */
	private Function<XdebugCommand, XdebugClientResponse> processCommandLambda = null;
		
	/**
	 * Debugger encoding.
	 */
	private String debuggerEncoding = "UTF-8";
	
	/**
     * Maximum number of debugged object children.
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
     * Flags that indcates debugging of hidden langauge objects..
     */
	private boolean debugHiddenObjects = false;
	
	/**
     * Current encoding for Xdebug packety data.
     */
	private String currentDataEncoding = "base64";	// can also be "none" value 
	
	/**
	 * Enables Xdebug with multiple sessions.
	 */
	private boolean multipleSessions = true;
	
	/**
	 * Static constructor.
	 */
	static {
        
		// Constant Xdebug features.
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
	 * Create new Xdebug client and connect it to Xdebug listener running on specific host and port.
	 * @param ideHostName
	 * @param xdebugPort
	 * @param areaServerStateLocator
	 * @return
	 */
	public static XdebugClient connectNewClient(String ideHostName, int xdebugPort, String areaServerStateLocator, int timeoutMs)
			throws Exception {
		
		// Create new client obejct.
		XdebugClient client = new XdebugClient();
		client.connect(ideHostName, xdebugPort, areaServerStateLocator, timeoutMs);
		return client;
	}

	/**
	 * Connect this client to specific host name and port number.
	 * @param ideHostName - host name on which Xdebug listens
	 * @param xdebugPort - port number of Xdebug listener
	 * @param areaServerStateLocator - information about current Area Server breakpoint
	 * @param timeoutMs - connection timeout in milliseconds
	 */
	private void connect(String ideHostName, int xdebugPort, String areaServerStateLocator, int timeoutMs)
			throws Exception {
		
		// TODO: <---REFACTOR Use packet client channel to connect to the IDE.
		try {
			Lock lock = new Lock();
			packetChannel = new PacketChannel() {
				@Override
				protected PacketSession onConnected(PacketChannel packetChannel) {
					
					// Create new session.
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
						protected void onThrownException(Throwable e) {
							XdebugClient.this.onThrownException(e);
						}
					};
					// Send INIT packet.
			        XdebugClientResponse initPacket;
					try {
						initPacket = XdebugClientResponse.createInitPacket(areaServerStateLocator);
						sendResponsePacket(packetChannel.clientSocketChannel, initPacket);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					Lock.notify(lock);
					
					return packetSession;
				}
			};
			packetChannel.connectToSocket(ideHostName, xdebugPort);
			
			// Wait for connection completed.
			boolean ellapsedTimeout = Lock.waitFor(lock, timeoutMs);
			if (ellapsedTimeout) {
				Utility.throwException("org.maclan.server.messageXdebugConnectionTimeout", ideHostName, xdebugPort);
			}
		}
		catch (Exception e) {
			Utility.throwException("org.maclan.server.messageXdebugConnectionError",
								   ideHostName, xdebugPort, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Check if this client is connected to Xdebug server (the IDE).
	 * @return
	 * @throws IOException 
	 */
	public boolean isConnected() throws IOException {
		
		// Check if the client socket channel exists.
		if (packetChannel.clientSocketChannel == null) {
            return false;
        }
        
        // Check if the client socket channel is connected.
		SocketAddress clientSocketAddress = packetChannel.clientSocketChannel.getRemoteAddress();
        if (clientSocketAddress !=  null) {
            return true;
        }
		return false;
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
		
		// If current packet element doesn't exit, initialize the packet sequence.
		if (element == null) {
			element = statementBlock;
		}
		// If current packet element is not finished, return the same value.
		else if (!element.isCompact) {
			return element;
		}
		else {
			element = null;
		}
		
		// Set new current element.
		return element;
	}	
	
	/**
	 * On block received.
	 * @param block
	 * @return
	 */
	protected boolean onBlock(PacketBlock block) {
		
		// Read nXdebug statement..
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
			// Check if statement is read.
			boolean success = packet.userProperties.containsKey(STATEMENT_PROP);
			if (!success) {
				return;
			}
			
			// Parse Xdebug statement.
			String statementText = (String) packet.userProperties.get(STATEMENT_PROP);
			
		    // If the stop symbol was found in the input buffer, parse the command bytes and process the command.
	    	XdebugCommand command = XdebugCommand.parseCommand(statementText);
	    	
	    	// Process the command and get response packet.
	    	if (processCommandLambda != null) {
		        XdebugClientResponse responsePacket = processCommandLambda.apply(command);
		        
		        // Send response packet back to the debugger server (the IDE).
		        if (responsePacket != null) {
		        	sendResponsePacket(packetChannel.clientSocketChannel, responsePacket);
		        }
		        else {
		        	sendErrorPacket(packetChannel.clientSocketChannel, command, XdebugError.UNIMPLEMENTED_COMMAND);
		        }
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		packet.reset();
		packet.packetParts.clear();
		statementBlock.reset();
	}

	/**
	 * On exception thrown.
	 * @param e
	 */
	protected void onThrownException(Throwable e) {
		// TODO Auto-generated method stub
		
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
			
			// Create error packet and delegate the call to send it to Xdebug listener.
			XdebugClientResponse response = XdebugClientResponse.createErrorPacket(command, xdebugError);
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
			try {
				// Get result packet bytes.
				byte [] packetBytes = response.getBytes();
				if (packetBytes == null) {
					return;
				}
				int packetLength = packetBytes.length;
				if (packetLength <=0) {
					return;
				}
				
				// Get leght of packet.
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
				int sentBytes = sent.get();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Accept debug commands.
	 * @throws Exception 
     */
	public void onAcceptCommands(Function<XdebugCommand, XdebugClientResponse> processCommandLambda)
			throws Exception {
		
		this.processCommandLambda = processCommandLambda;
	}
	
	/**
	 * An Xdebug probe that accepts incomming commands and sends responses to them. It can also stop the connection.
	 * @param areaServer
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public XdebugClientResponse xdebugProbe(AreaServer areaServer, XdebugCommand command)
			throws Exception {
		
		// Get command name.
		String commandName = command.name;
		
		// Rules for incomming commands that are processed with the debugging probe.
		
		// On getting the Xdebug feature.
		if ("feature_get".equals(commandName)) {
			
			XdebugClientResponse resultPacket = createFeatureGetResponse(command, areaServer);
            return resultPacket;
        }
		else if ("feature_set".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getFeatureSetResponse(command, areaServer);
            return resultPacket;
        }
		else if ("source".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getSourceCodeResponse(command, areaServer);
            return resultPacket;
        }
		else if ("expr".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getExprResponse(command, areaServer);
            return resultPacket;
		}
		else if ("run".equals(commandName)) {
			
			XdebugClientResponse resultPacket = getRunResponse(command, areaServer);
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
	 * Response with debugged source code.
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
	 * Run expr() command and return response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getExprResponse(XdebugCommand command, AreaServer areaServer) 
			throws Exception {
		
		// Get expression to evaluate with Area Server.
		String dataText = new String(command.data);
		String exprResultText = areaServer.evaluateText(dataText, String.class, true);
		
		// Create response packet.
		XdebugClientResponse exprResponse = XdebugClientResponse.createExprResult(command, exprResultText);
		return exprResponse;
	}
	
	/**
	 * Release debugged thread lock to run the code. Create run command response.
	 * @param command
	 * @param areaServer
	 * @return
	 * @throws Exception 
	 */
	private XdebugClientResponse getRunResponse(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Unlock current Area Server thread.
		Lock debuggerLock = areaServer.state.debuggerLock;
		if (debuggerLock != null) {
			Lock.notify(debuggerLock);
		}
		
		// Create response packet.
		XdebugClientResponse runResponse = XdebugClientResponse.createRunResult(command);
		return runResponse;
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
		
		Utility.throwException("org.maclan.server.messageXdebugNotBoolean", valueText);
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
}
