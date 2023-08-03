/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 28-11-2018
 *
 */
package org.multipage.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.multipage.util.Obj;
import org.multipage.util.RepeatedTask;
import org.multipage.util.j;

/**
 * 
 * @author vakol
 *
 */
public class Consoles extends JFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Text panel MIME content type.
	 */
	private String TEXT_PANE_MIME_TYPE = "text/html";

	/**
	 * Application start up timeout in milliseconds.
	 */
	private static final long STARTUP_TIMEOUT_MS = 3000;
	
	/**
	 * Format of time stamps.
	 */
	public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * Open ports that enable consoles input.
	 */
	public static int[] openPorts = new int[] { 48000, 48001, 48002 };

	/**
	 * Message record class.
	 */
	public static final class MessageRecord {
		
		/**
		 * Flag that can switch between message timestamps and console timestamps.
		 */
		public static boolean useConsoleTimeStamps = false;
		
		/**
		 * Timestamp.
		 */
		private LocalTime timestamp = null;
		
		/**
		 * Record text.
		 */
		private String messageText = null;
		
		/**
		 * Time when the message was written into the console
		 */
		private LocalTime consoleWriteTime = null;
		
		/**
		 * Constructor.
		 * 
		 * @param timestamp
		 * @param messageString
		 */
		public MessageRecord(LocalTime timestamp, String messageString) {
			
			this.timestamp = timestamp;
			this.messageText = messageString;
		}
		
		/**
		 * Get string representation of the log record.
		 */
		@Override
		public String toString() {
			
			return (useConsoleTimeStamps ? consoleWriteTime : timestamp) + messageText;
		}
	}

	/**
	 * Console class.
	 */
	private static final class Console {
		
		/**
		 * Console text pane.
		 */
		protected JTextPane textPane = null;

		/**
		 * Minimum and maximum timestamps.
		 */
		protected LocalTime minimumTimestamp = null;
		protected LocalTime maximumTimestamp = null;
		
		/**
		 * Console port number.
		 */
		protected int port = -1;
		
		/**
		 * Incomming buffer size.
		 */
		private static final int INPUT_BUFFER_SIZE = 1024;
		private static final int TIMESTAMP_BUFFER_SIZE = 16;
		private static final int LOG_MESSAGE_BUFFER_SIZE = INPUT_BUFFER_SIZE;
		
		/**
		 * Console input buffers.
		 */
		protected ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
		public Obj<ByteBuffer> timestampBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(TIMESTAMP_BUFFER_SIZE));
		public Obj<ByteBuffer> logMessageBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(LOG_MESSAGE_BUFFER_SIZE));
		/**
		 * Input reader states and constants.
		 */
		private static final int READ_TIMESTAMP = 0;
		private static final int READ_LOG_MESSAGE = 1;
		
		private int inputReaderState = READ_TIMESTAMP;
		
		/**
		 * Last read timestamp.
		 */
		private LocalTime readTimestamp = null;
		
		/**
		 * Message record list that maps time axis to the records.
		 */
		protected LinkedList<MessageRecord> consoleRecords = new LinkedList<>();
		
		/**
		 * 
		 * @param consoleName
		 * @param textPane
		 * @param port
		 */
		public Console(String consoleName, JTextPane textPane, int port) {
			
			this.textPane = textPane;
			this.port = port;
		}

		/**
		 * Add new message record.
		 * 
		 * @param messageRecord
		 */
		public void addMessageRecord(MessageRecord messageRecord) {

			// Get current time.
			LocalTime timeNow = LocalTime.now();
			
			messageRecord.consoleWriteTime = timeNow;

			// Set maximum and minimum timestamp.
			if (minimumTimestamp == null) {
				minimumTimestamp = timeNow;
			}
			if (maximumTimestamp == null || maximumTimestamp.compareTo(timeNow) < 0) {
				maximumTimestamp = timeNow;
			}

			// Create new message record and put it into the message map.
			synchronized (consoleRecords) {
				consoleRecords.add(messageRecord);
			}
		}

		/**
		 * Clear console content.
		 */
		public void clear() {

			consoleRecords.clear();
			maximumTimestamp = null;
			minimumTimestamp = null;
			textPane.setText("");
		}
		
		/**
		 * Read log messages from the input buffer.
		 * @param logMessageLambda
		 * @throws Exception 
		 */
		public boolean readLogMessages(Consumer<MessageRecord> logMessageLambda)
				throws Exception {
			
			// Prepare input buffer for reading.
			inputBuffer.flip();
			
			// If there are no remaining bytes in the input buffer, return false value.
			if (!inputBuffer.hasRemaining()) {
				return false;
			}
			
			boolean messageAccepted = false;
			
			// Read until end of input buffer.
			boolean endOfReading = false;
			while (!endOfReading) {
				
				Obj<Boolean> terminated = new Obj<Boolean>(false);
				
				// Determine protocol state from input byte value and invoke related action.
				switch (inputReaderState) {
				
				case READ_TIMESTAMP:
					endOfReading = Utility.readUntil(inputBuffer, timestampBuffer, TIMESTAMP_BUFFER_SIZE, DIVIDER_SYMBOL, terminated);
					if (terminated.ref) {
						inputReaderState = READ_LOG_MESSAGE;
						readTimestamp = getTimestamp(timestampBuffer.ref);
					}
					break;
					
				case READ_LOG_MESSAGE:
					endOfReading = Utility.readUntil(inputBuffer, logMessageBuffer, LOG_MESSAGE_BUFFER_SIZE, TERMINAL_SYMBOL, terminated);
					if (terminated.ref) {
						
						inputReaderState = READ_TIMESTAMP;
						String logMessageText = getLogMessage(logMessageBuffer.ref);
						
						MessageRecord messageRecord = new MessageRecord(readTimestamp, logMessageText);
						logMessageLambda.accept(messageRecord);
						
						messageAccepted = true;
					}
					break;
				}
			}
			
			return messageAccepted;
		}
	
		/**
		 * Get timestamp from the input buffer.
		 * @param timestampBuffer
		 * @return
		 * @throws Exception 
		 */
		private LocalTime getTimestamp(ByteBuffer timestampBuffer)
				throws Exception {
			
			// Prepare the timestamp buffer for reading the XML length.
			timestampBuffer.flip();
			
			// Get length of the nuffer.
			int arrayLength = timestampBuffer.limit();
			byte [] bytes = new byte [arrayLength];
			
			// Read buffer contents.
			timestampBuffer.get(bytes);
			
			// Convert bytes into UTF-8 encoded string.
			String timstampText = new String(bytes, "UTF-8");
			
			// Convert text to timstamp object.
			LocalTime timestamp = LocalTime.parse(timstampText, TIMESTAMP_FORMAT);
			
			// Reset the timestamp buffer.
			timestampBuffer.clear();
			
			// Return result.
			return timestamp;
		}
		
		/**
		 * Get log message from the input buffer.
		 * @param logMessageBuffer
		 * @return
		 * @throws Exception 
		 */
		private String getLogMessage(ByteBuffer logMessageBuffer)
				throws Exception {
			
			// Prepare the log message buffer for reading.
			logMessageBuffer.flip();
			
			// Get length of the nuffer.
			int arrayLength = logMessageBuffer.limit();
			byte [] bytes = new byte [arrayLength];
			
			// Read buffer contents.
			logMessageBuffer.get(bytes);
			
			// Convert bytes into UTF-8 encoded string.
			String logMessageText = new String(bytes, "UTF-8");
			
			// Reset the log message buffer.
			logMessageBuffer.clear();
			
			// Return result.
			return logMessageText;
		}
	}
	
	/**
	 * 
	 */
	private static Map<String, Console> consoles = new ConcurrentHashMap<>();

	/**
	 * Socket idle timeout in milliseconds.
	 */
	private static final int IDLE_TIMEOUT_MS = 250;

	/**
	 * Log message divider and stop symbols.
	 */
	private static final byte[] DIVIDER_SYMBOL = { 0, 0 };
	private static final byte[] TERMINAL_SYMBOL = { 0, 0, 0, 0 } ;
	
	/**
	 * Set this flag to true on application exit.
	 */
	private static boolean exitApplication = false;

	/**
	 * Application state.
	 */
	public static final int UNINITIALIZED = 0;
	public static final int STARTUP = 1;
	public static final int LISTENING = 2;
	public static final int SHUTDOWN = 3;

	public static int applicationState = UNINITIALIZED;

	/**
	 * Initialize application state. Must be called prior to any log message.
	 */
	public static void initialize() {

		// Bind logging callback lambda.
		j.ensureConsolesRunningLambda = () -> ensureApplicationRunning();
	}

	/**
	 * Components.
	 */
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JToolBar toolBar;
	private JSlider sliderTimeSpan;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		applicationState = STARTUP;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Consoles frame = new Consoles();
					frame.setAlwaysOnTop(true);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Ensure that the application is running.
	 */
	public static boolean ensureApplicationRunning() {

		// Start application.
		main(new String[] {});

		// Wait for application listening to ports.
		while (applicationState != LISTENING) {

			if (applicationState == SHUTDOWN) {
				return false;
			}

			try {
				Thread.sleep(STARTUP_TIMEOUT_MS);
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Create the frame.
	 */
	public Consoles() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosing();
			}
		});
		initComponents();
		postCreation();
	}

	/**
	 * Close the application.
	 */
	protected void onClosing() {

		applicationState = SHUTDOWN;
		exitApplication = true;
	}

	/**
	 * Initialize GUI components.
	 */
	private void initComponents() {
		setTitle("Consoles for multitasking event logs");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 768, 618);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane);

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);

		sliderTimeSpan = new JSlider();
		sliderTimeSpan.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onSliderChange();
			}
		});
		sliderTimeSpan.setValue(100);
		sliderTimeSpan.setMinorTickSpacing(1);
		sliderTimeSpan.setMajorTickSpacing(10);
		sliderTimeSpan.setPreferredSize(new Dimension(200, 44));
		sliderTimeSpan.setPaintTicks(true);
		sliderTimeSpan.setPaintLabels(true);
		contentPane.add(sliderTimeSpan, BorderLayout.SOUTH);
	}

	/**
	 * Add new console view.
	 * 
	 * @param consoleName
	 * @param port
	 * @return
	 */
	private void addConsoleView(String consoleName, int port) {

		// Create new console and add it to the map.
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);

		JTextPane textPane = new JTextPane();
		textPane.setContentType(TEXT_PANE_MIME_TYPE);
		scrollPane.setViewportView(textPane);

		tabbedPane.addTab(consoleName, null, panel, null);

		Console console = new Console(consoleName, textPane, port);
		consoles.put(consoleName, console);

		// Open console port.
		openConsole(consoleName);
	}

	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Create toolbar.
		createToolbar();

		// Open ports for consoles.
		addConsoleView("Console1", 48000);
		addConsoleView("Console2", 48001);
		addConsoleView("Console3", 48002);

		applicationState = LISTENING;
	}

	/**
	 * Create toolbar.
	 */
	private void createToolbar() {

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", "#Clear console", () -> onClearConsole());
	}

	/**
	 * On clear cosnole.
	 * 
	 * @return
	 */
	private void onClearConsole() {

		// Get selected console.
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}

		// Get console name.
		String consoleName = tabbedPane.getTitleAt(selectedIndex);

		// Get console view and reset it.
		Console console = consoles.get(consoleName);
		if (console == null) {
			return;
		}

		// Ask user if to delete console contents.
		boolean confirmed = Utility.ask2(this, "Clear console contents?");
		if (!confirmed) {
			return;
		}

		// Clear console contents.
		console.clear();
	}

	/**
	 * Open port for console with given name.
	 * @param consoleName
	 */
	private void openConsole(String consoleName) {
		
		try {
			// Try to get console by its name.
			Console console = consoles.get(consoleName);
			if (console == null) {
				// Show error message.
				Utility.show2(this, consoleName + " not found.");
			}
			
			// Open asynchornous server socket.
	        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
	        InetSocketAddress socketAddress = new InetSocketAddress("localhost", console.port);
	        server.bind(socketAddress);
	        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
	        
	        	// When connection is completed...
	        	@Override
				public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel server) {
	        		try {
                		
                		// TODO: <---DEBUG
                		j.log("server.accept.completed");
	        			
	        			// Start a loop to read input data. 
	        			RepeatedTask.loopBlocking(consoleName, -1, IDLE_TIMEOUT_MS, (exit, exception) -> {
	        				
	        				// TODO: <---DEBUG
	                		j.log("client.read");
                    		
							// Read input data bytes.
		                    client.read(console.inputBuffer, console, new CompletionHandler<Integer, Console>() {
		                    	
		                    	// After read completed...
		                    	public void completed(Integer result, Console console) {
		                    		
		                    		try {
			                    		// Read log messages from input buffer.
			                    		console.readLogMessages(logMessage -> {
			                    			
			                    			try {
			    								// Add log message
			                    				console.addMessageRecord(logMessage);
			                    				
			                    				// TODO: <---DEBUG
						                		j.log("LOG %s", logMessage);
			                    			}
			                    			catch (Exception e) {
			                    				// Show error message.
			                    				Utility.show2(Consoles.this, e.getLocalizedMessage());
			                    			}
			                    		});
			                    		
			                    		// TODO: <---DEBUG
				                		j.log("updateConsole");
			                    		
	    								// Update the console.
	    								updateConsole(console);
			                    		
		                        		// Prepare input buffer for the next write operation.
		                        		Utility.reuseInputBuffer(console.inputBuffer);
		                    		}
		                    		catch (Exception e)	{
		                    			// Show error message.
			                    		Utility.show2(Consoles.this, e.getLocalizedMessage());
		                    		}
		                    	}
		                    	
		                    	// On read error...
								public void failed(Throwable e, Console console) {
				        			// Show error message.
				        			Utility.show2(Consoles.this, e.getLocalizedMessage());
		                        }
		                    });
	        				
	        				// Exit the blocking loop on application exit.
	        				boolean running = !exitApplication;
	        				return running;
	        			});
	        		}
	        		catch (Exception e) {
	        			// Show error message.
	        			Utility.show2(Consoles.this, e.getLocalizedMessage());
	        		}
	        	}
	        	
				// If the connection failed...
	            public void failed(Throwable exception, AsynchronousServerSocketChannel server) {
	    			// Show error message.
	    			Utility.show2(Consoles.this, exception.getLocalizedMessage());
	            }
	        });	
		}
		catch (Exception e) {
			// Show error message.
			Utility.show2(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Update the console.
	 * 
	 * @param console
	 */
	private void updateConsole(Console console) {
		
		SwingUtilities.invokeLater(() -> {
					
			// Check timestamps for null values.
			if (console.maximumTimestamp == null || console.minimumTimestamp == null) {
				return;
			}
	
			// Get text view.
			JTextPane textPane = console.textPane;
	
			// Get time axis slider value and compute slider timespan.
			int sliderValue = sliderTimeSpan.getValue();
			long maxNanos = console.maximumTimestamp.toNanoOfDay();
			long minNanos = console.minimumTimestamp.toNanoOfDay();
			long nanosSpan = maxNanos - minNanos;
			long sliderNanos = nanosSpan * sliderValue / 100;
			LocalTime sliderTimestamp = console.minimumTimestamp.plusNanos(sliderNanos);
	
			// Compile text contents.
			Obj<String> contents = new Obj<String>("<html>");
			Obj<Boolean> highlighted = new Obj<Boolean>(false);
			
			console.consoleRecords.forEach(messageRecord -> {

				String color = null;
				if (!highlighted.ref && messageRecord.timestamp.compareTo(sliderTimestamp) > 0) {
					color = "red";
					highlighted.ref = true;
				}
				else {
					color = "black";
				}
				
				String messageText = Utility.htmlSpecialChars(messageRecord.messageText);
				String messageHtml = String.format("<span style='color:%s'>%s</span><br>", color, messageText);
				contents.ref += messageHtml;
			});
			
			// Set text of the text view.
			textPane.setText(contents.ref);
			
			// Move caret to the end of the view.
			int endPosition = textPane.getDocument().getLength();
			textPane.setCaretPosition(endPosition);
		});
	}

	/**
	 * On slider change.
	 */
	protected void onSliderChange() {

		// Get selected console.
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}

		// Get console name.
		String consoleName = tabbedPane.getTitleAt(selectedIndex);
		Console console = consoles.get(consoleName);
		
		if (console == null) {
			return;
		}

		// Update console.
		updateConsole(console);
	}
}
