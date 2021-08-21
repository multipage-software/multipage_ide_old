/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 16-05-2018
 *
 */
package org.maclan.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;

import org.multipage.gui.Callback;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author user
 *
 */
public class XdebugListener extends DebugListener {
	
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
	 * Command receiver flag
	 */
	private boolean readyToProcessCommand = false;

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
	public static boolean enabled() {
		
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
	static class XdebugCommand {

		/**
		 * Command name
		 */
		private String name;
		
		/**
		 * Command data
		 */
		private byte [] data;
		
		/**
		 * Command response
		 */
		private XdebugPacket response;
		
		/**
		 * Caught exceptions
		 */
		private LinkedList<Exception> exceptions;
		
		/**
		 * Constructor
		 * @param commandName
		 */
		public XdebugCommand(String commandName) {
			
			this(commandName, null);
		}
		
		/**
		 * Constructor
		 * @param commandName
		 * @param data
		 */
		public XdebugCommand(String commandName, Object data) {
			
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
		synchronized public boolean equals(XdebugCommand command) {
			
			return this.name.equals(command.name);
		}
		
		/**
		 * Check if command name equals
		 */
		synchronized public boolean equals(String commandName) {
			
			return this.name.equals(commandName);
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
	 * Xdebug name
	 */
	private XdebugCommand command = new XdebugCommand(nop, null);
	
	/**
	 * A channel for communication with Xdebug client
	 */
	protected SocketChannel connection = null;

	/**
	 * Callbacks
	 */
	private Callback watchDogCallback = null;
	private Callback newSessionCallback = null;

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
		
		// Use default port number 9001 because 9000 uses JVM Xdebug
		xdebugListenerSingleton = new XdebugListener(9001);
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
			this.connection = null;
			watchDogAlerts("not connected");
		}
	}
	
	/**
	 * Send name to Xdebug and get response packet
	 * @param name
	 * @return
	 */
	public XdebugPacket postCommand(String command) throws Exception {
		
		// Delegate the call
		return postCommand(command, null);
	}

	/**
	 * Send a name with data to Xdebug and get response packet
	 * @param commandName
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public XdebugPacket postCommandT(String commandName, String data) throws Exception {
		
		byte [] bytes = data.getBytes();
		return postCommand(commandName, bytes);
	}

	/**
	 * Send a name with data to Xdebug and get response packet
	 * @param commandName
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public XdebugPacket postCommand(String commandName, byte [] data) throws Exception {
		
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
	 * Returns true value if a command can be processed
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean readyToProcessCommand() {
		
		return this.command.isEmpty() && readyToProcessCommand;
	}
	
	/**
	 * Gets result of a command
	 * @param command
	 * @return
	 * @throws Exception 
	 */
	private XdebugPacket resultOf(XdebugCommand command) throws Exception {
		
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
				
				// Listen to port
				channel = listenTo(port);
				if (channel == null) {
					return;
				}	
				
				// Enter loop with rules and process incoming commands
				while (!terminate) {
					
					// Set flag
					readyToProcessCommand = true;
					
					// Check connection status with watch dog
					watchDogCheckStatusOf(connection);
					
					// Rule: process stop command
					if (isStop(command)) {
						endOfSession();
						continue;
					}
					
					// Rule: do nothing if the session is stopping
					if (isStopping(status)) {
						sleepFor(200);
						continue;
					}
					
					// Rule: accept incoming connection
					if (mustAcceptNewConnection()) {
						connection = waitForConnectionFrom();
						continue;
					}
					
					// Rule: initialize communication
					if (shouldBeInitialized(connection)) {
						initializeCommunication(connection);
						continue;
					}
					
					// Rule: wait for a new command
					if (isInitialized(connection) && !is(command)) {
						Lock.notify(sessionReady);
						Lock.waitFor(commandSet, 50000);
						continue;
					}
					
					// Rule: process other commands
					if (is(command)) {
						sendThrough(connection, command);
						endOfTransaction();
						continue;
					}
				}
				
				// Reset command
				command.set(nop);
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
			 * Returns true if the loop has to accept a new connection
			 * @param channel
			 * @return
			 */
			private boolean mustAcceptNewConnection() {
				
				return !isConnected(connection);
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
				
				return Session.ready.equals(session);
			}
			
			/**
			 * Returns true if the parameter is a stop command
			 * @param command
			 * @return
			 */
			private boolean isStop(XdebugCommand command) {
				
				return command.isStop();
			}
			
			/**
			 * If is new command
			 * @param command
			 * @return
			 */
			private boolean is(XdebugCommand command) {
				
				return !command.isEmpty();
			}
			
			/**
			 * Gets status of socket channel
			 * @param socketChannel
			 * @return
			 */
			private String getStatus(SocketChannel socketChannel) {
				
				if (connection == null) {
					return "no connection";
				}
				
				if (!connection.isConnected()) {
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
			 * Sleep for given tiomeoout
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
			 * Wait for a connection from socket channel
			 * @param channel
			 * @return
			 * @throws Exception 
			 */
			private SocketChannel waitForConnectionFrom() {
				
				try {
					// Reset connection
					connection = null;
					Selector selector = Selector.open();
					
					while (!terminate && !isStop(command)) {
						
						// If debugging is disabled, close listening port and wait for enable event
						if (!enabled()) {
							
							// Close port
							close(connection);
							closeChannel();
							selector.selectNow();
							
							// Wait for enabled debugging
							while (!enabled()) {
								sleepFor(200);
							}
							
							// Bind port again
							channel = listenTo(port);
							if (channel == null) {
								return null;
							}
						}
						
						// Create selector for non-blocking mode and register it on given channel
						// Wait for some amount of time
						final int timeout = 1000;
						
						channel.register(selector, SelectionKey.OP_ACCEPT);
						selector.select(timeout);
						
						selector.selectedKeys().stream().forEach((SelectionKey key) -> {
							if (key.isValid() && key.isAcceptable()) {
								ServerSocketChannel channel = (ServerSocketChannel) key.channel();
								try {
									if (connection == null) {
										connection = channel.accept();
										
										if (connection != null) {
											connection.configureBlocking(false);
										}
									}
								}
								catch (Exception e) {
								}
							}
						});
						
						if (connection != null) {
							session = Session.connected;
							break;
						}
					}
					
					return connection;
				}
				catch (Exception e) {
					return null;
				}
			}
			
			/**
			 * Initialize communication
			 * @param connection
			 * @throws Exception 
			 */
			private void initializeCommunication(SocketChannel connection) {
				
				final String requiredIdeKey = "multipage-xdebug";
				
				try {
					
					// Initialize transaction counter
					transaction = 1;
					
					// Read initial message
					String [] message = readMessage(connection, null);
					
					// Communication breakdown
					if (message.length < 2) {
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
					if (!"PHP".equals(language)) {
						throw new Exception(String.format("Xdebug: declared language '%s' doesn't match the reuqired PHP language", language));
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
					
					if (newSessionCallback != null) {
						newSessionCallback.run(null);
					}
				}
				catch (Exception e) {
					
					session = Session.notready;
				}
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
				
				XdebugCommand command = new XdebugCommand(commandName, data);
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
			private void sendThrough(SocketChannel connection, XdebugCommand command) {
				
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
			private void endOfTransaction() {
				
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
	private void endOfSession(boolean informUser) {
		
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
	private void endOfSession() {
		
		endOfSession(true);
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
		
		// TODO:
		return true;
	}
	
	/**
	 * Stop debugging
	 */
	public void stopDebugging() {
		
		// TODO:
		command.set(stop);
		Lock.notify(commandSet);
	}
	
	/**
	 * Run name and get response packet
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public XdebugPacket runCommand(String command) throws Exception {
		
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
	 * Sets watch dog callback
	 * @param callback
	 */
	public void setNewSessionCallback(Callback callback) {
		
		this.newSessionCallback = callback;
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
	public String getChannelStatus() {
		
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
	protected String getSourceCode(String fileUri) throws ChannelBreakDownException  {
		
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
			connection.configureBlocking(false);
			
			Selector selector = Selector.open();
			connection.register(selector, SelectionKey.OP_WRITE);
			
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
	 * Returns true if the channel is readable
	 * @param socketChannel
	 * @param timeout
	 * @return
	 */
	private boolean isReadable(SocketChannel socketChannel, int timeout) {
		
		Obj<Boolean> readable = new Obj<Boolean>(false);
		try {
			connection.configureBlocking(false);
			
			Selector selector = Selector.open();
			connection.register(selector, SelectionKey.OP_READ);
			
			selector.select(timeout);
			selector.selectedKeys().stream().forEach((key) -> { if (key.isValid() && key.isReadable()) readable.ref = true; });
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return readable.ref;
	}

	/**
	 * Reads Xdebug message from channel
	 * @param socketChannel
	 * @return
	 */
	private String [] readMessage(SocketChannel socketChannel, String encoding) throws Exception {
		
		final int bufferSize = 100;
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		ArrayList<Byte> messageBytes = new ArrayList<Byte>(bufferSize);
		
		try {
			
			int bytesRead;

			while (true) {
				
				final int timeout = 1000;
				if (!isReadable(socketChannel, timeout)) {
					break;
				}
				
				if ((bytesRead = socketChannel.read(buffer)) <= 0) {
					break;
				}
				
				for (int i = 0; i < bytesRead; i++) {
					messageBytes.add(buffer.get(i));
				}
				if (bytesRead < bufferSize) {
					break;
				}
				buffer.clear();
			}
			
			Byte [] bytes = new Byte [messageBytes.size()];
			messageBytes.toArray(bytes);
			String messageText = encoding != null ? new String(Utility.toPrimitives(bytes), encoding)
													: new String(Utility.toPrimitives(bytes));
			
			String [] messages = messageText.split("\0");
			return messages;
		}
		catch (Exception e) {
			throw e;
		}
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
			String [] message = readMessage(socketChannel, null);
			
			// Bad Xdebug message
			if (message.length < 2) {
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
