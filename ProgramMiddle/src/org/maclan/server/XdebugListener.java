/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 16-05-2018
 *
 */
package org.maclan.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.multipage.gui.Callback;
import org.multipage.gui.ConditionalEvents;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author user
 *
 */
public class XdebugListener extends DebugListener {
	
	/**
	 * Xdebug port number. Set default port number to 9001, because JVM Xdebug already uses the port number 9000.
	 */
	public static final int xdebugPort = 9001;
	
	/**
	 * Required IDE key for communication in the Xdebug protocol.
	 */
	public static final String requiredIdeKey = "multipage-xdebug";
	
	/**
	 * Tiout in milliseconds for client connections with Xdebug protocol.
	 */
	public static final long xdebugClientTimeoutMs = 1000;
	
	/**
	 * The loop timeout for idle operations (in milliseconds).
	 */
	private long listenerIdleTimeoutMs = 200;
	
	/**
	 * Xdebug listener singleton object
	 */
	private static XdebugListener xdebugListenerSingleton;
	
	/**
	 * Gets the Xdebug client singleton
	 * @return
	 */
	public static XdebugListener getSingleton() {
		
		return xdebugListenerSingleton;
	}
	
	/**
	 * Socket channel
	 */
	private ServerSocketChannel channel;
	
	/**
	 * Port
	 */
	private int port;
	
	/**
	 * This flag when set to true terminates listening loop
	 */
	private boolean terminate = false;

	/**
	 * Debug viewer listener
	 */
	private DebugViewerCallback debugViewerCallback;
	
	/**
	 * Transaction number
	 */
	private int transaction;

	/**
	 * Exception of a transaction
	 */
	private LinkedList<Exception> transactionExceptions = new LinkedList<Exception>();

	/**
	 * Transaction flags
	 */
	private int transactionFlags = 0;

	/**
	 * Negotiated features
	 */
	private XdebugFeatures xdebugFeatures = new XdebugFeatures();

	/**
	 * Socket service thread
	 */
	private Thread socketChannelThread;

	/**
	 * Bad Xdebug message exception constant
	 */
	private static final Exception noResponse = new Exception("No response");

	/**
	 * When set, the transaction breaks on first exception found
	 */
	public static final int BREAK_ON_FIRST_EXCEPTION = 1;
	
	/**
	 * Lock objects
	 */
	private Lock sessionReady = new Lock("SESSREADY");
	private Lock commandSet = new Lock("CMDSET");
	private Lock commandProcessed = new Lock("CMDPROC");
	private Lock resultForwarded = new Lock("RESFORW");
	
	/**
	 * Session state
	 */
	private Session session = Session.none;
	
	/**
	 * Channel breakdown exception
	 */
	class ChannelBreakDownException extends Exception {

		/**
		 * Constructor
		 * @param message
		 */
		public ChannelBreakDownException(String message) {
			super(message);
		}

		/**
		 * Version
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Returns true value if the debugger is enabled
	 */
	public static boolean debuggingEnabled() {
		
		if (enableListener != null) {
			Object returned =  enableListener.run();
			
			if (returned instanceof Boolean) {
				return (Boolean) returned;
			}
		}
		return false;
	}
	
	/**
	 * Send name class
	 */
	static class XdebugStatement {
		
		/**
		 * Response wrappers.
		 */
		final String simpleResponseWrapper = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
											 "<response xmlns=\"urn:debugger_protocol_v1\" %s transaction_id=\"%s\"/>";
		final String complexResponseWrapper = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				 							  "<response xmlns=\"urn:debugger_protocol_v1\" %s transaction_id=\"%s\">%s</response>";
		/**
		 * Command name
		 */
		private String name = null;
		
		/**
		 * Command parameters.
		 */
		private HashMap<String, String> parameters = null;
		
		/**
		 * Command data
		 */
		private byte [] data = null;
		
		/**
		 * Command response
		 */
		private XdebugPacket response = null;
		
		/**
		 * Caught exceptions
		 */
		private LinkedList<Exception> exceptions = null;
		
		/**
		 * Constructor
		 * @param commandName
		 */
		public XdebugStatement(String commandName) {
			
			this(commandName, null);
		}
		
		/**
		 * Constructor
		 * @param commandName
		 * @param parameters
		 */
		public XdebugStatement(String commandName, HashMap<String, String> parameters) {
			
			this.name = commandName;
			this.parameters = parameters;
		}
		
		/**
		 * Constructor
		 * @param commandName
		 * @param data
		 */
		public XdebugStatement(String commandName, Object data) {
			
			this.name = commandName;
			
			if (data != null) {
				byte [] bytes = data.toString().getBytes();
				this.data = Base64.getEncoder().encode(bytes);
			}
		}
		
		/**
		 * Set command
		 * @param commandName
		 */
		synchronized public void set(String commandName) {
			
			set(commandName, null);
		}
		
		/**
		 * Sets command with data
		 * @param commandName
		 * @param data
		 */
		synchronized public void set(String commandName, byte [] data) {
			
			this.name = commandName;
			this.data = data;
		}
		
		/**
		 * Get parameter.
		 * @param name
		 * @return
		 */
		public String getParameter(String name) {
			
			String value = parameters.get(name);
			return value;
		}

		/**
		 * Returns true if the name is null
		 */
		synchronized boolean isEmpty() {
			
			if (this.name == null) {
				return true;
			}
			
			if (this.name.equals(nop)) {
				return true;
			}
			
			return false;
		}
		
		/**
		 * Returns true on stop command
		 * @return
		 */
		synchronized public boolean isStop() {
			
			return stop.equals(name);
		}

		/**
		 * Add exception
		 * @param exception
		 */
		synchronized public void addException(Exception exception) {
			
			if (exception != null) {
				
				if (exceptions == null) {
					exceptions = new LinkedList<Exception>();
				}
				
				exceptions.add(exception);
			}
		}
		
		/**
		 * Get name string
		 */
		@Override
		synchronized public String toString() {
			return name;
		}

		/**
		 * Check if command name equals
		 */
		synchronized public boolean equals(XdebugStatement command) {
			
			return this.name.equals(command.name);
		}
		
		/**
		 * Check if command name equals
		 */
		synchronized public boolean equals(String commandName) {
			
			return this.name.equals(commandName);
		}
		
		/**
		 * Parse Xdebug command text.
		 * @param commandText
		 * @return
		 */
		public static XdebugStatement parse(String commandText) {
			
			// Command name parser.
			final Pattern nameRegex = Pattern.compile("^\\s*(?<name>\\w+)");
			// Parameter parser.
			final Pattern parameterRegex = Pattern.compile("-(?<flag>\\w+)\\s+(?<param>(\\w+)|\"(.*?)(?<!\\\\)\")");
			
			// Find command name.
			Matcher commandNameMatcher = nameRegex.matcher(commandText);
			boolean found = commandNameMatcher.find();
			int count = commandNameMatcher.groupCount();
			
			if (found && count == 1) {
				
				String commandName = commandNameMatcher.group("name");
				
				// Find parameters.
				HashMap<String, String> parameters = new HashMap<String, String>();
				Matcher parametersMatcher = parameterRegex.matcher(commandText);
				do {
					found = parametersMatcher.find();
					count = parametersMatcher.groupCount();
					
					if (!(found && count == 4)) {
						break;
					}
					
					String flag = parametersMatcher.group("flag");
					String parameter = parametersMatcher.group("param");
					
					parameters.put(flag, parameter);
				}
				while (true);
				
				// Create Xdebug command and return it.
				XdebugStatement xdebugCommand = new XdebugStatement(commandName, parameters);
				return xdebugCommand;
			}
			
			return null;
		}
		
		/**
		 * Get transaction number.
		 * @return
		 */
		public int getTransactionNumber() {
			
			if (parameters == null) {
				return 0;
			}
			
			String transactionId = parameters.get("i");
			if (transactionId == null || transactionId.length() < 2) {
				return 0;
			}
			
			transactionId = transactionId.substring(1);
			int transactionNumber = Integer.parseInt(transactionId);
			return transactionNumber;
		}
		
		/**
		 * Check Xdebug statement name
		 * @param statementName
		 * @return
		 */
		public boolean is(String statementName) {
			
			if (name == null) {
				return false;
			}
			return name.equals(statementName);
		}
		
		/**
		 * Get feature name.
		 * @return
		 */
		public String getFeatureName() {
			
			if (parameters == null) {
				return null;
			}
			
			String feature = parameters.get("n");
			return feature;
		}
		
		/**
		 * Compile response.
		 * @param - arguments
		 * @return
		 */
		public String compileResponse(String arguments) {
			
			// Delegate call.
			return compileResponse(arguments, null, simpleResponseWrapper);
		}
		
		
		/**
		 * Compile response.
		 * @param - arguments
		 * @return
		 */
		public String compileResponse(String arguments, String responseText) {
			
			// Delegate call.
			return compileResponse(arguments, responseText, complexResponseWrapper);
		}
		
		/**
		 * Compile response.
		 * @param arguments
		 * @param responseText
		 * @return
		 */
		public String compileResponse(String arguments, String responseText, String xmlTemplate) {
			
			// Try to get statement name.
			String allArguments = "command=\"" + this.name + "\"";
			
			// Try to get feature name.
			String feature = getFeatureName();
			if (feature != null) {
				allArguments += " feature=\"" + feature + "\"";
			}
			
			// Trim input parameter.
			if (arguments == null) {
				arguments = "";
			}
			
			// Add leading space.
			if (!arguments.isEmpty()) {
				allArguments += ' ' + arguments;
			}
			
			// Compile response text.
			String transactionId = parameters.get("i");
			String response = null;
			
			if (responseText != null)
				response = String.format(xmlTemplate, allArguments, transactionId, responseText);
			else
				response = String.format(xmlTemplate, allArguments, transactionId);
			
			return response;
		}
	}
	
	/**
	 * No operation
	 */
	private final static String nop = "nop";
	
	/**
	 * Stop command
	 */
	private final static String stop = "stop";
	
	/**
	 * Xdebug idle state in milliseconds.
	 */
	protected final static int xdebugIdleMs = 200;
	
	/**
	 * Input buffer size.
	 */
	private static final int inputBufferSize = 256;
	
	/**
	 * Xdebug connection.
	 */
	private static class Connection {
		
	}
	
	/**
	 * Xdebug name
	 */
	private XdebugStatement command = new XdebugStatement(nop, null);

	/**
	 * List of pending connections.
	 */
	private ConcurrentLinkedQueue<Connection> pendingConnections = null;

	/**
	 * Callbacks
	 */
	private Callback watchDogCallback = null;

	/**
	 * Xdebug status
	 */
	private String status = "not ready";
	
	/**
	 * Set debug viewer listener
	 */
	@Override
	public void setDebugViewerListener(DebugViewerCallback callback) {
		
		debugViewerCallback = callback;
	}
	
	/**
	 * Creates singleton
	 */
	public static XdebugListener createInstance() throws Exception {
		
		// Use default Xdebug port number.
		xdebugListenerSingleton = new XdebugListener(xdebugPort);
		return xdebugListenerSingleton;
	}
	
	/**
	 * Listen to port
	 * @param port
	 * @return
	 * @throws IOException 
	 */
	private ServerSocketChannel listenTo(int port) {
		
		try {
			if (channel != null) {
				channel.close();
			}
			
			channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(new InetSocketAddress(port), 1);
			
			this.port = port;
		}
		catch (Exception e) {
			watchDogAlerts("Cannot listen to Xdebug");
			channel = null;
		}
		
		return channel;
	}
	
	/**
	 * Close connection
	 * @param connection
	 * @throws Exception 
	 */
	private void close(SocketChannel connection) {
		
		try {
			if (connection != null && connection.isConnected()) {
				connection.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			watchDogAlerts("not connected");
		}
	}
	
	/**
	 * Process received command and get response packet.
	 * @param name
	 * @return
	 */
	private XdebugPacket processReceivedCommand(String command) throws Exception {
		
		// Delegate the call
		return processReceivedCommand(command, null);
	}

	/**
	 * Process received command and get response packet.
	 * @param commandName
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	private XdebugPacket processReceivedCommandT(String commandName, String data) throws Exception {
		
		byte [] bytes = data.getBytes();
		return processReceivedCommand(commandName, bytes);
	}

	/**
	 * Process received command and get response packet.
	 * @param commandName
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	private XdebugPacket processReceivedCommand(String commandName, byte [] data) throws Exception {
		
		// If session is stopping, exit the method
		if (Session.stopping.equals(session)) {
			stopSession();
			return XdebugPacket.empty;
		}
		
		// If the session is not ready, exit the method
		if (!Session.ready.equals(session)) {
			//showUserAlert("org.maclan.server.messageRelodWebPageToStartDebugger");
			return XdebugPacket.empty;
		}
		
		// Check if a channel thread exists
		if (socketChannelThread == null) {
			throw new Exception("USERTHREAD: socket channel thread not created!");
		}
		
		// Wait for session ready
		boolean timeout = Lock.waitFor(sessionReady, 10000);
		if (timeout) {
			throw new Exception("USERTHREAD: session is not ready, a timeout occured");
		}
		
		// Set command
		this.command.set(commandName, data);
		
		// Notify command receiver
		Lock.notify(commandSet/*, "USERTHREAD: command set signal, " + commandName*/);
		
		// Suspend thread
		timeout = Lock.waitFor(commandProcessed, 50000);
		
		// If a result was not received, throw exception
		if (timeout) {
			
			// Re-establish session
			session = Session.ready;
			throw new Exception("USERTHREAD: result was not received during timeout!");
		}
		
		// Get result
		XdebugPacket result = resultOf(command);
				
		// Unlock command loop, so it can process new command
		Lock.notify(resultForwarded);
		
		return result;
	}
	
	/**
	 * Gets result of a command
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	private XdebugPacket resultOf(XdebugStatement command) throws Exception {
		
		// If no response, return empty packet
		if (command.exceptions == null && command.response == null) {
			return XdebugPacket.empty;
		}
		
		// Throw first caught exception
		if (!command.exceptions.isEmpty()) {
			throw command.exceptions.getFirst();
		}
		
		// Trim response 
		if (command.response == null) {
			command.response = XdebugPacket.empty;
		}
		
		return command.response;
	}
	
	/**
	 * Enumeration of session states
	 * @author user
	 *
	 */
	enum Session {none, connected, notready, ready, stopping};
	
	/**
	 * Constructor
	 */
	public XdebugListener(int port) throws Exception {
		
		this.port = port;
	}
		
	/**
	 * Main receiving loop.
	 * @param channel
	 * @param callbackLambda
	 */
	protected void receivingLoop(ServerSocketChannel channel, Function<ByteArrayOutputStream, Boolean> callbackLambda) {
		
		// Define constants.
		final int watchDogIdleMs = 200;
		final int inputBufferSize = 1024;
		
		Obj<Boolean> exitLoop = new Obj<Boolean>(false);
		Obj<SocketChannel> connection = new Obj<SocketChannel>();
		ByteBuffer buffer = ByteBuffer.allocate(inputBufferSize);
		
		// Enter loop.
		do {
			try {
				Obj<Boolean> exit = new Obj<Boolean>(false);
				
				// Wait for socket connection.
				do {
					Selector selector = Selector.open();
					channel.register(selector, SelectionKey.OP_ACCEPT);
					selector.select(watchDogIdleMs);
					selector.selectedKeys().stream().forEach(key -> {
						
						try {
							
							// Check if selected key is valid.
							if (key.isValid()) {
								return;
							}
							
							// Get selected channel.
							ServerSocketChannel selectedChannel = (ServerSocketChannel) key.channel();
							
							// Accept new connection.
							if (key.isAcceptable()) {
								
								connection.ref = selectedChannel.accept();
								if (connection.ref != null) {
									connection.ref.configureBlocking(false);
									
									exit.ref = true;
								}
								return;
							}
						}
						catch (Exception e) {
							// Print error.
							e.printStackTrace();
						}
					});
				}
				while (exit.ref);
				
				exit.ref = false;
				
				// Process incoming statements.
				do {
					Selector selector = Selector.open();
					channel.register(selector, SelectionKey.OP_READ);
					selector.select(watchDogIdleMs);
					selector.selectedKeys().stream().forEach(key -> {
						try {
							
							// Read incoming message.
							if (key.isReadable() && connection.ref != null && connection.ref.isConnected()) {
								
								ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
								int bytesRead = 0;
								while (true) {
									bytesRead = connection.ref.read(buffer);
									if (bytesRead <= 0) {
										break;
									}
									
									int position = buffer.position();
									byte [] bytes = new byte [position];
									buffer.get(bytes, 0, position);
									buffer.clear();
									byteStream.write(bytes);
								}
								
								// Callback with receiving byte stream.
								exit.ref = callbackLambda.apply(byteStream);
							}
						}
						catch (Exception e) {
							// Print error.
							e.printStackTrace();
						}
					});
				}
				while (exit.ref);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		while (!exitLoop.ref);
	}

	/**
	 * Activates debugger
	 */
	public void activate() throws Exception {
			
		// Start main thread that accepts socket connection
		socketChannelThread = new Thread("IDE-Xdebug-Listener") {

			/**
			 * Thread entry
			 */
			@Override
			public void run() {
				
				// Enter loop with rules and process incoming commands
				try {
					
					// Listen to port
					channel = listenTo(port);
					if (channel == null) {
						return;
					}
					
					// Receiving loop.
					receivingLoop(channel, inputByteStream -> {
						
						
						return false;
					});
				}
				catch (Exception e) {
					
					e.printStackTrace();
				}
			}
			
			/**
			 * Conditions
			 */

			/**
			 * Returns true if connection is connected
			 * @param connection
			 * @return
			 */
			private boolean isConnected(SocketChannel connection) {
				
				if (connection == null) {
					return false;
				}
				
				return connection.isConnected();
			}
			
			/**
			 * Returns true if the status ends the session
			 * @param status
			 * @return
			 */
			private boolean isStopping(String status) {
				
				return "stopping".equals(status);
			}
			
			/**
			 * Returns true if the connection should be ready
			 * @param connection
			 * @return
			 */
			private boolean shouldBeInitialized(SocketChannel connection) {
				
				if (!isConnected(connection)) {
					return false;
				}
				
				return Session.connected.equals(session);
			}
			
			/**
			 * Returns true if connection is ready
			 * @param connection
			 * @return
			 */
			private boolean isInitialized(SocketChannel connection) {

				if (connection == null) {
					return false;
				}
				
				if (!connection.isConnected()) {
					return false;
				}
				
				boolean initialized = Session.ready.equals(session);
				return initialized;
			}
			
			/**
			 * Returns true if the parameter is a stop command
			 * @param command
			 * @return
			 */
			private boolean isStop(XdebugStatement command) {
				
				return command.isStop();
			}
			
			/**
			 * If is new command
			 * @param command
			 * @return
			 */
			private boolean is(XdebugStatement command) {
				
				return !command.isEmpty();
			}
			
			/**
			 * Gets status of socket channel
			 * @param socketChannel
			 * @return
			 */
			private String getStatus(SocketChannel socketChannel) {
				
				if (socketChannel == null) {
					return "no connection";
				}
				
				if (!socketChannel.isConnected()) {
					return "disconnected";
				}
				
				try {
					
					// Do transaction. Send name with data through channel
					if (Session.ready.equals(session)) {
						
						beginTransaction();
						XdebugPacket responsePacket = passTo(socketChannel, "status", null);
						status = responsePacket.getString("/response/@status");
						endTransaction();
						
						// Write watch dog message
						if (status != null) {
							return status;
						}
					}
				}
				catch (ChannelBreakDownException e) {
					return "connection breakdown";
				}
				
				if (Session.stopping.equals(session)) {
					return "stopping";
				}
				
				return "connected";
			}
			
			/**
			 * Sleep for given time out
			 * @param timeout
			 */
			private void sleepFor(int timeout) {
				
				try {
					Thread.sleep(timeout);
				}
				catch (Exception e) {
				}
			}
			
			/**
			 * Checks state and send it through channel
			 * @param socketChannel
			 * @throws ChannelBreakDownException 
			 */
			protected void watchDogCheckStatusOf(SocketChannel socketChannel) {
				
				status = getStatus(socketChannel);
				watchDogAlerts(status);
			}
			
			/**
			 * Initialize communication
			 * @param connection
			 * @throws Exception 
			 */
			private void initializeCommunication(SocketChannel connection) {
				
				try {
					
					// Initialize transaction counter
					transaction = 1;
					
					// Read initial message
					String [] message = readMessages(connection, null);
					
					// Communication breakdown
					if (message == null) {
						throw noResponse;
					}
					
					// Initialization
					XdebugPacket packet = new XdebugPacket(message[1]);
					
					// Check IDE key
					String ideKey = packet.getString("/init/@idekey");
					if (!requiredIdeKey.equals(ideKey)) {
						throw new Exception(String.format("Xdebug: IDE key \"%s\" doesn't match required key \"%s\"", ideKey, requiredIdeKey));
					}
					
					//  Check language
					String language = packet.getString("/init/@language");
					if (!("PHP".equals(language) || "Maclan".equals(language))) {
						throw new Exception(String.format("Xdebug: declared language '%s' doesn't match the required PHP or Maclan language", language));
					}
					
					// Initialization was successful. Continue the communication with
					// features negotiation
					xdebugFeatures.initialLoad(getSingleton(), connection);
					xdebugFeatures.set("show_hidden", "1");
					
					String fileUri = "";
					@SuppressWarnings("unused")
					int fileLines = -1;
					
					if (debugViewerCallback != null) {
						// Open file in debug viewer
						fileUri = packet.getString("/init/@fileuri");
						fileLines = debugViewerCallback.openFile(fileUri);
					}
					
					session = Session.ready;
				}
				catch (Exception e) {
					
					session = Session.notready;
				}
			}
			
			/**
			 * Listen for user commands.
			 */
			private void listenUserCommands(SocketChannel connection) {
				
				// Create lock of an exit event.
				Lock lockExit = new Lock();
				
				// Create new command listener and wait.
				ConditionalEvents.receiver(XdebugListener.this, AreaServerSignal.debugStatement, message -> {
					
					// Get command.
					String command = message.getRelatedInfo();
					if (command == null) {
						
						// Notify the exit event lock.
						Lock.notifyAll(lockExit);
						return;
					}
					
					// Check if Xdebug connection is ready.
					if (!connection.isConnected()) {
						return;
					}
					
					// Get statement data.
					String data = "";
					
					// Send Xdebug command to debugger.
					sendThrough(connection, command, data, transactionExceptions);
				});
				
				// Wait for exit of the listen mode.
				Lock.waitFor(lockExit);
				
				// Remove previously created receiver.
				ConditionalEvents.removeReceivers(AreaServerSignal.debugStatement);
			}
			
			/**
			 * Notify all listeners that the Xdebug session is ready.
			 */
			private void notifySessionReady() {
				
				Lock.notify(sessionReady);
				
				// Call the debug viewer callback function.
				debugViewerCallback.sessionStateChanged(true);
			}
			
			/**
			 * Send command through connection
			 * @param connection
			 * @param data 
			 * @param exceptions 
			 * @param command
			 * @throws Exception 
			 */
			private XdebugPacket sendThrough(SocketChannel connection, String commandName, String data, LinkedList<Exception> exceptions) {
				
				XdebugStatement command = new XdebugStatement(commandName, data);
				sendThrough(connection, command);
				exceptions.clear();
				exceptions.addAll(0, command.exceptions);
				return command.response;
			}
			
			/**
			 * Send command through connection
			 * @param connection
			 * @param command
			 * @throws Exception 
			 */
			private void sendThrough(SocketChannel connection, XdebugStatement command) {
				
				try {
					// Check if name exists
					if (command == null) {
						return;
					}
					
					// Do transaction. Send name with data through channel
					beginTransaction();
					command.response = passTo(connection, command.name, command.data);
					command.exceptions = endTransaction();
				}
				catch (Exception e) {
				}
			}
			
			/**
			 * End of transaction
			 */
			private void endOfTransaction(SocketChannel connection) {
				
				// Reset command
				command.set(nop);

				status = getStatus(connection);
				session = isStopping(status) ? Session.stopping : Session.ready;
				Lock.notify(commandProcessed);
				
				Lock.waitFor(resultForwarded, 10000);
			}
			

		};
		
		// Start the thread
		socketChannelThread.start();
	}

	/**
	 * 
	 * @param channel2
	 */
	private void closeChannel() {
		
		if (channel != null && channel.isOpen()) {
			try {
				channel.close();
			}
			catch (IOException e) {
			}
			channel = null;
		}
	}

	/**
	 * Returns true if a session exists
	 * @param session
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean exists(Session session) {
		
		return session != Session.none;
	}

	/**
	 * End of session
	 * @param informUser
	 */
	private void endOfSession(SocketChannel connection, boolean informUser) {
		
		try {
			
			// Finish debugging
			close(connection);
			
			// Reset command
			command.set(nop);
			
			// Reset session state
			session = Session.none;
			
			// Inform user
			/*if (informUser) {
				final int timeout = -1;
				showUserAlert("org.maclan.server.messageEndOfDebugSesion", timeout);
			}*/
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * End of session
	 */
	private void endOfSession(SocketChannel connection) {
		
		endOfSession(connection, true);
	}
	
	/**
	 * Show user alert
	 * @param message
	 */
	private void showUserAlert(String message) {
		
		showUserAlert(message, -1);
	}
	
	/**
	 * Show user message
	 * @param message
	 * @param timeout 
	 */
	private void showUserAlert(String message, int timeout) {
		
		if (debugViewerCallback != null) {
			debugViewerCallback.showUserAlert(Resources.getString(message), timeout);
			try {
				// Switch to another thread
				Thread.sleep(0);
			}
			catch (InterruptedException e) {
			}
		}
	}
	
	/**
	 * Returns true if the session is ready to process a command
	 * @param session
	 * @return
	 */
	public boolean isReady(Session session) {
		
		return Session.ready.equals(session);
	}
	
	/**
	 * Returns true value if source code is debugged
	 * @return
	 */
	protected boolean debugging() {
		
		return "starting".equals(status) || "running".equals(status) || "break".equals(status);
	}

	/**
	 * Starts debugging
	 */
	@Override
	public boolean startDebugging() {
		
		return true;
	}
	
	/**
	 * Stop debugging
	 */
	public void stopDebugging() {
		
		command.set(stop);
		Lock.notify(commandSet);
	}
	
	/**
	 * Run name and get response packet
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public XdebugPacket runCommand(SocketChannel connection, String command) throws Exception {
		
		XdebugPacket responsePacket = command(connection, command);
		return responsePacket;
	}

	/**
	 * Sets watch dog callback
	 * @param callback
	 */
	public void setWatchDogCallback(Callback callback) {
		
		this.watchDogCallback = callback;
	}
	
	/**
	 * Watch dog message
	 * @param message
	 */
	protected void watchDogAlerts(String message) {
		
		if (watchDogCallback != null) {
			watchDogCallback.run(message);
		}
	}

	/**
	 * Gets channel status
	 * @return
	 */
	public String getChannelStatus(SocketChannel connection) {
		
		if (connection == null || !connection.isConnected()) {
			return "not connected";
		}
		return "connected";
	}
	
	/**
	 * Retrieves source code from Xdebug server
	 * @param fileUri
	 * @return
	 */
	protected String getSourceCode(SocketChannel connection, String fileUri) throws ChannelBreakDownException  {
		
		beginTransaction();
		XdebugPacket responsePacket = command(connection, String.format("source -f %s", fileUri));
		LinkedList<Exception> exceptions = endTransaction();
		if (!exceptions.isEmpty()) {
			return null;
		}
		return responsePacket.getString("/response/text()");
	}
	
	/**
	 * Returns true if the channel is writable
	 * @param socketChannel
	 * @param timeout
	 * @return
	 */
	private boolean isWritable(SocketChannel socketChannel, int timeout) {
		
		Obj<Boolean> writable = new Obj<Boolean>(false);
		try {
			socketChannel.configureBlocking(false);
			
			Selector selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_WRITE);
			
			selector.select(timeout);
			selector.selectedKeys().stream().forEach((key) -> { if (key.isValid() && key.isWritable()) writable.ref = true; });
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return writable.ref;
	}
	
	/**
	 * Writes IDE message to channel
	 * @param socketChannel
	 * @param name
	 * @param data
	 * @throws Exception 
	 */
	private void writeMessage(SocketChannel socketChannel, String command, byte [] data) throws ChannelBreakDownException {
		
		// Convert data to Base64
		if (data != null) {
			data = Base64.getEncoder().encode(data);
		}
		
		// Create and fill a byte buffer
		int length = command.length();
		int dataLength = data != null ? 4 + data.length : 0;
		ByteBuffer byteBuffer = ByteBuffer.allocate(length + dataLength + 1);
		
		// Insert name and data into the buffer
		byteBuffer.put(command.getBytes());
		if (data != null) {
			String dataText = new String(data);
			String dataPart = String.format(" -- %s", dataText);
			byte [] partBytes = dataPart.getBytes();
			byteBuffer.put(partBytes);
		}
		byteBuffer.put((byte) 0);
		
		// Set pointer to zero
		byteBuffer.rewind();
		
		// Send bytes to socket channel
		final int timeout = 1000;
		if (!isWritable(socketChannel, timeout)) {
			throw new ChannelBreakDownException("Not writeable");
		}
		try {
			socketChannel.write(byteBuffer);
		}
		catch (Exception e) {
			throw new ChannelBreakDownException(e.getMessage());
		}
	}
	
	/**
	 * Read Xdebug messages separated by '\0' character from the channel
	 * @param socketChannel
	 * @return
	 */
	private String [] readMessages(SocketChannel socketChannel, String encoding)
			throws Exception {
		
		Obj<String []> messages = new Obj<String []>(null);
				
		ByteArrayOutputStream messageData = new ByteArrayOutputStream();
		ByteBuffer buffer = ByteBuffer.allocate(inputBufferSize);
		
		// Read bytes from the socket channel.			
		socketChannel.configureBlocking(false);
		
		Selector selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);
		
		// Wait for input message.
		selector.select(xdebugIdleMs);
		
		// Process the input message.
		selector.selectedKeys().stream().forEach(key -> {
			
			if (key.isValid() && key.isReadable()) {
				
				while (true) {
					try {
						int bytesRead = socketChannel.read(buffer);
						
						if (bytesRead <= 0) {
							break;
						}
						
						buffer.rewind();
						
						byte [] byteArray = new byte [bytesRead];
						buffer.get(byteArray, 0, bytesRead);
						
						messageData.write(byteArray);
			
						if (bytesRead < inputBufferSize) {
							break;
						}
						
						buffer.clear();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try {
					// Converts input bytes to messages.
					byte [] bytes = messageData.toByteArray();
					String messageText = encoding != null ? new String(bytes, encoding)	: new String(bytes);
					
					// Split the input string and create array with message length and message text.
					messages.ref = messageText.split("\0");
					if (messages.ref.length != 2) {
						messages.ref = null;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		return messages.ref ;
	}

	/**
	 * Sets stop command for current session
	 */
	public void stopSession() {
		
		command.set(stop);
		Lock.notify(commandSet);
	}

	/**
	 * Closes the Xdebug client
	 */
	public void close() {
		
		command.set(stop);
		Lock.notify(commandSet);
		terminate = true;
	}
	
	@Override
	protected void finalize() throws Exception {
		
		close();
	}

	/**
	 * Returns listening port number
	 * @return
	 */
	public int getPort() {
		
		return port;
	}
	/**
	 * Begins transaction
	 * @param flags 
	 */
	public void beginTransaction() {
		
		beginTransaction(0);
	}
			
	/**
	 * Begins transaction
	 * @param flags 
	 */
	public void beginTransaction(int flags) {
		
		transactionFlags = flags;
	}

	/**
	 * Ends tranaction
	 * @return - collected exceptions or no exceptions on success
	 */
	public LinkedList<Exception> endTransaction() {
		
		LinkedList<Exception> exceptions = new LinkedList<Exception>(transactionExceptions);
		transactionExceptions.clear();
		
		transactionFlags = 0;
		
		return exceptions;
	}

	/**
	 * Does transaction name
	 * @param socketChannel
	 * @param name
	 * @return
	 */
	public XdebugPacket command(SocketChannel socketChannel, String command) throws ChannelBreakDownException {
		
		return passTo(socketChannel, command, null);
	}
	
	/**
	 * Does transaction name
	 * @param socketChannel
	 * @param name
	 * @param data
	 */
	public XdebugPacket passTo(SocketChannel socketChannel, String command, byte [] data) throws ChannelBreakDownException {
		
		// Check previous exceptions
		if ((transactionFlags & BREAK_ON_FIRST_EXCEPTION) != 0 && !transactionExceptions.isEmpty()) {
			return XdebugPacket.empty;
		}
		
		try {
			if (command.contains(" ")) {
				// Inject transaction id into the name
				command = command.replaceFirst("\\s", " -i t" + transaction++ + " ");
			}
			else {
				// Append transaction id to the end of the name
				command += " -i t" + transaction++;
			}
			
			// Write name to the output channel
			writeMessage(socketChannel, command, data);
			
			// Get response
			String [] message = readMessages(socketChannel, null);
			
			// Bad Xdebug message
			if (message == null) {
				throw noResponse;
			}
			
			// Parse the message
			XdebugPacket packet = new XdebugPacket(message[1]);
			
			// Check if an error was sent
			String errorCode = packet.getString("/response/error/@code");
			if (!errorCode.isEmpty()) {
				
				String error = packet.getString("/response/error/message/text()");
				throw new Exception(String.format("[transaction step %d] Xdebug error number %s on name '%s' -> %s", transaction, errorCode, command, error));
			}
			
			return packet;
		}
		catch (ChannelBreakDownException breakException) {
			throw breakException;
		}
		catch (Exception e) {
			
			// Set exception of this transaction
			transactionExceptions.addLast(e);
		}
		
		// Return empty packet
		return XdebugPacket.empty;
	}
}
