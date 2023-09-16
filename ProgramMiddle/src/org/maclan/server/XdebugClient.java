/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 08-05-2023
 *
 */
package org.maclan.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.function.Function;

import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.RepeatedTask;
import org.multipage.util.Resources;
import org.multipage.util.j;

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
     * Command idle time span in milliseconds.
     */
	private static final long COMMAND_IDLE_TIME_MS = 200;
	
	/**
     * Xdebug client input buffer size.
     */
	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * Response character set.
	 */
	private static final Charset RESPONSE_CHARSET = Charset.forName("UTF-8");
	
	/**
	 * Map of constant features.
	 */
	private static final HashMap<String, Object> CONSTANT_FEATURES = new HashMap<String, Object>();
	
	/**
	 * Task stopped, the flag.
	 */
	private static boolean taskStopped = false;
	
	/**
	 * Current listener (server) socket address to which this client connects.
	 */
	private InetSocketAddress serverSocketAddress = null;
	
	/**
	 * Socket channel used by this client.
	 */
	private SocketChannel clientSocketChannel = null;

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
	private int debugHiddenObjects = 0;
	
	/**
     * Current encoding for Xdebug packety data.
     */
	private String currentDataEncoding = "base64";	// can also be "none" value 
	
	/**
	 * Static constructor.
	 */
	static {
        
		// Constant Xdebug features.
		CONSTANT_FEATURES.put("language_supports_thread", "1"); 
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
		
		// Delegate the call.
		return connectNewClient(ideHostName, xdebugPort, areaServerStateLocator, DEFAUL_CONNECTION_TIMEOUT_MS);
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
		
		try {
			// Start timeout.
			long startTime = System.currentTimeMillis();
			
			// Remember server socket address.
			serverSocketAddress = new InetSocketAddress(ideHostName, xdebugPort);
			
			// Create non-blocking socket channel and connect it.
	        clientSocketChannel = SocketChannel.open();
	        clientSocketChannel.configureBlocking(false);
	
	        // Connect to the server.
	        clientSocketChannel.connect(serverSocketAddress);
			
	        while (!clientSocketChannel.finishConnect()) {
	        	
	            // Wait until timeout millicesonds will elapse.
	            long endTimeMs = System.currentTimeMillis();
	            long elapsedTimeMs = endTimeMs - startTime;
	            
	            if (elapsedTimeMs > timeoutMs) {
	                Utility.throwException("org.maclan.server.messageXdebugConnectionTimeoutElapsed", timeoutMs);
	            }
	        }
	        
	        // Send INIT packet.
	        XdebugResponse initPacket = XdebugResponse.createInitPacket(areaServerStateLocator);
	        sendResponsePacket(clientSocketChannel, initPacket);
		}
		catch (Exception e) {
			Utility.throwException("org.maclan.server.messageXdebugConnectionError",
								   ideHostName, xdebugPort, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Check if this client is connected to Xdebug server (the IDE).
	 * @return
	 */
	public boolean isConnected() {
		
		// Check if the client socket channel exists.
		if (clientSocketChannel == null) {
            return false;
        }
        
        // Check if the client socket channel is connected.
        if (clientSocketChannel.isConnected()) {
            return true;
        }
        
		return false;
	}
	
	/**
	 * Send response Xdebug packet.
	 * @param clientSocketChannel
	 * @param response
	 * @throws Exception 
	 */
	private void sendResponsePacket(SocketChannel clientSocketChannel, XdebugResponse response)
			throws Exception {
		
		// Check socket channel.
		if (clientSocketChannel == null) {
			return;
		}

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
			int bufferSize = dataLengthBytes.length + XdebugResponse.NULL_SIZE + packetLength + XdebugResponse.NULL_SIZE;
			ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
			
			// Put bytes into the buffer. 
			buffer.put(dataLengthBytes);
			buffer.put(XdebugResponse.NULL_SYMBOL);
			buffer.put(packetBytes);
			buffer.put(XdebugResponse.NULL_SYMBOL);
			buffer.flip();
			
			// Write the buffer with response bytes into socket channel.
			int sent = clientSocketChannel.write(buffer);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * Accept debug commands.
	 * @throws Exception 
     */
	public void acceptCommands(Function<XdebugCommand, XdebugResponse> processCommandLambda)
			throws Exception {
		
		// Create new input buffer and command string buffer..
		ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		Obj<ByteBuffer> commandBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(BUFFER_SIZE));
		
		Obj<Boolean> terminated = new Obj<Boolean>();
		
		// Loop until all byte buffers have been read.
		RepeatedTask.loopBlocking("DebugClientThread", -1, -1, (exit, exception) -> {
			try {	
				
				// Read byte chunks from the input socket stream.
			    int bytesRead = clientSocketChannel.read(inputBuffer);
			    if (bytesRead <= 0) {
			    	
			    	// If there are no data on input, enter idle state and continue after timeout has ellapsed.
			    	Thread.sleep(COMMAND_IDLE_TIME_MS);
			    	return exit;
			    }
			    
			    // Prepare input buffer for reading.
			    inputBuffer.flip();
			    
			    while (inputBuffer.hasRemaining()) {
			    
				    // Read input buffer until the NULL symbol is encountered. The output buffer can extend its capacity.
				    Utility.readUntil(inputBuffer, commandBuffer, BUFFER_SIZE, XdebugCommand.NULL_SYMBOL, null, -1, null, terminated);

					// If the input bytes are not terminated, continue reading.
				    if (!terminated.ref) {
				    	break;
				    }
				    
				    // If the stop symbol was found in the input buffer, parse the command bytes and process the command.
			    	XdebugCommand command = XdebugCommand.parseCommand(commandBuffer.ref);
			    	
				    // TODO: <---DEUG Log connads received by client.
			    	//j.log(1, "READ COMMAND: %s", command.getText());
			    	
			        XdebugResponse responsePacket = processCommandLambda.apply(command);
			        
			        // Send response packet back to the debugger server (the IDE).
			        sendResponsePacket(clientSocketChannel, responsePacket);
			    }
			    
				return exit;
			}
			catch (Exception e) {
				exception.ref = new Exception(String.format(
						Resources.getString("org.maclan.server.messageXdebugConnectionError"), e.getLocalizedMessage()));
				return true;
			}
		});
	}
	
	/**
	 * An Xdebug probe that accepts incomming commands and responses to them. It can also stop the connection.
	 * @param areaServer
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	public XdebugResponse xdebugProbe(AreaServer areaServer, XdebugCommand command)
			throws Exception {
		
		// Get command name.
		String commandName = command.name;
		
		// Rules for incomming commands which are processed with the debugging probe.
		
		// On getting the Xdebug feature.
		if ("feature_get".equals(commandName)) {
			
			XdebugResponse resultPacket = createFeaturePacket(command, areaServer);
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
	private XdebugResponse createFeaturePacket(XdebugCommand command, AreaServer areaServer)
			throws Exception {
		
		// Get feature name.
		String featureName = command.getArgument("-n");
		
		// Try to find the constant feature.
		Object featureValue = CONSTANT_FEATURES.get(featureName);
        if (featureValue == null) {
        	
        	// Get variable features.
        	switch (featureName) {
        	case "encoding":
        		featureValue = areaServer.getCurrentEncoding();
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
            	featureValue = debugHidden();
            	break;
        	default:
        		featureValue = Utility.newException("org.maclan.server.messageXdebugFeatureNot", featureName);
        	}
        }
        
        // Create response packet.
        XdebugResponse featurePacket = XdebugResponse.createFeaturePacket(command, featureValue);
		return featurePacket;
	}
	
	/**
	 * Set feature.
	 * @param feature
	 */
	private void setFeature(String [] feature) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Get the number of debugged object children.
	 * @return
	 */
	private int getMaxDebuggedChildren() {
		
		return debuggedChildren;
	}
	
	/**
	 * Get the number of debugged object data.
	 * @return
	 */
	private int getMaxDebuggedData() {
		
		return debuggedData;
	}
	
	/**
	 * Get maximum debugged stack depth.
	 * @return
	 */
	private int getMaxDebuggedDepth() {
		
		return debuggedDepth;
	}
	
	/**
	 * Get flag that indicates whether to debug hidden objects.
	 * @return
	 */
	private int debugHidden() {
		
		return debugHiddenObjects;
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
