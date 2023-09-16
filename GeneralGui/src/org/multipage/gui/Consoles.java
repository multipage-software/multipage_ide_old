/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 24-06-2023
 *
 */
package org.multipage.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.multipage.util.Obj;

/**
 * Multitask log consoles that can be also run as a standalone application with LocConsole and ConsolePropeties classes
 * included,
 * @author vakol
 *
 */
public class Consoles extends JFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Format of time stamps.
	 */
	public static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	/**
	 * Main frame boundaries.
	 */
	private static Rectangle bounds;

	/**
	 * Open ports that enable consoles input.
	 */
	public static int[] openPorts = new int[] { 48000, 48001, 48002, 48003 };
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = null;
	}

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
		public LocalTime timestamp = null;
		
		/**
		 * Record text.
		 */
		public String messageText = null;
		
		/**
		 * Displayed message color.
		 */
		public Color color = Color.BLACK;
		
		/**
		 * Console statement.
		 */
		public String statment = null;
		
		/**
		 * Time when the message was written into the console
		 */
		LocalTime consoleWriteTime = null;
		
		/**
		 * Constructor.
		 * 
		 * @param timestamp
		 * @param color 
		 * @param messageString
		 * @param statement 
		 */
		public MessageRecord(LocalTime timestamp, Color color, String messageString, String statement) {
			
			this.timestamp = timestamp;
			this.messageText = messageString;
			this.color = color;
			this.statment = statement;
		}
		
		/**
		 * Get message text with maximum allowed characters.
		 * @param maximumCharacters
		 * @return
		 */
		public String getMessageText(int maximumCharacters) {
			
			if (messageText == null || messageText.isEmpty()) {
				return "";
			}
			
			String resultMessageString = messageText.trim();
			int length = resultMessageString.length();
			
			// Trim maximum haracters.
			if (maximumCharacters > length) {
				maximumCharacters = length;
			}
			
			// Get specified number of characters from the message beginning.
			resultMessageString = resultMessageString.substring(0, maximumCharacters);
			return resultMessageString;
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
	 * 
	 */
	private static Map<String, LogConsole> consoles = new ConcurrentHashMap<>();

	/**
	 * Log message divider and stop symbols.
	 */
	static final byte[] START_OF_HEADING = { (byte) 0x00, (byte) 0x01 };
	static final byte[] START_OF_TEXT = { (byte) 0x00, (byte) 0x02 };
	static final byte[] END_OF_TRANSMISSION = { (byte) 0x00, (byte) 0x04 };
	
	/**
	 * Application state.
	 */
	public static final int UNINITIALIZED = 0;
	public static final int STARTUP = 1;
	public static final int LISTENING = 2;
	public static final int SHUTDOWN = 3;

	public static int applicationState = UNINITIALIZED;
	
	/**
	 * Scroll panel dimensions.
	 */
	private static final int SCROLL_WIDTH = 500;
	private static final int SCROLL_HEIGHT = 300;
	
	/**
	 * Proportion of console propeties panel.
	 */
	private static final double PROPERTIES_PROPORTION = 0.25;
	
	/**
	 * Splitter size.
	 */
	private static final int SPLITTER_SIZE = 10;
	
	/**
	 * Reference to main frame window of the application.
	 */
	protected static Consoles mainFrame = null;
	
	/**
	 * Components.
	 */
	private JPanel contentPane;
	private JToolBar toolBar;
	private JPanel panelConsolesContainer;

	/**
	 * Referene to last created split panel.
	 */
	private JSplitPane lastCreatedSplitPanel = null;
	
	/**
	 * Reference to last component focused by user.
	 */
	protected JTextPane lastFocusedTextPane = null;

	/**
	 * Properties panel.
	 */
	private ConsoleProperties propertiesPanel = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		applicationState = STARTUP;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				try {
					mainFrame  = new Consoles();
					mainFrame.setAlwaysOnTop(true);
					mainFrame.setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
	}

	/**
	 * Initialize GUI components.
	 */
	private void initComponents() {
		setTitle("Consoles for multitasking event logs");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 859, 621);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		panelConsolesContainer = new JPanel();
		panelConsolesContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
		panelConsolesContainer.setPreferredSize(new Dimension(600, 10));
		scrollPane.setViewportView(panelConsolesContainer);
		panelConsolesContainer.setLayout(new BoxLayout(panelConsolesContainer, BoxLayout.X_AXIS));
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		// Create toolbar.
		createToolbar();

		// Open ports for consoles.
		int count = openPorts.length;
		for (int index = 0; index < count; index++) {
			
			int port = openPorts[index];
			addConsoleView("Console" + (index + 1), port);
		}
		
		// Add properties panel.
		addConsolePropertiesPanel();
		
		// Reset consoles' dimesnions. 
		restoreConsolesDimensions();
		
		// Load dialog.
		loadDialog();
		
		applicationState = LISTENING;
	}

	/**
	 * Create toolbar.
	 */
	private void createToolbar() {

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", "#Clear console", () -> onClearConsole());
	}

	/**
	 * Add new console view.
	 * 
	 * @param consoleName
	 * @param port
	 * @return
	 */
	private void addConsoleView(String consoleName, int port) {
		
		JSplitPane splitPane = null;
		
		if (lastCreatedSplitPanel == null) {
			
			// Main scroll panel for all consoles.
			JScrollPane scrollPaneConsole = new JScrollPane();
			panelConsolesContainer.add(scrollPaneConsole);
			
			// Create first split pane for consoles.
			splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.5);
			scrollPaneConsole.setViewportView(splitPane);
		}
		else {
			// Create new split panel in the right component of the last split panel.
			splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.5);
			lastCreatedSplitPanel.setRightComponent(splitPane);
		}
		
		// Create scroll bars in the left component of the split panel.
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		splitPane.setDividerSize(SPLITTER_SIZE);
		
		// Create text panel for the console.
		JTextPane textPane = new JTextPane();
		textPane.setBackground(Color.BLACK);
		textPane.setContentType("text/html");
		textPane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				
				// Remeber last focused console.
				lastFocusedTextPane = textPane;
				
				// Select console by its name.
				selectConsole(consoleName);
				
				// Display console properties.
				displayConsoleProperties(consoleName);
			}
		});
		scrollPane.setViewportView(textPane);
		
		// Remember last split panel.
		lastCreatedSplitPanel = splitPane;
		
		try {
			// Create new console object and put it into the consoles collection.
			LogConsole console = new LogConsole(consoleName, splitPane, port);
			consoles.put(consoleName, console);
	
			// Open console port.
			openConsole(consoleName);
		}
		catch (Exception e) {
			
			// Display error message.
			Utility.show2(this, e.getLocalizedMessage());
		}
	}

	/**
	 * Add console properties panel to last created split panel.
	 */
	private void addConsolePropertiesPanel() {
		
		// Create the properties panel.
		propertiesPanel = new ConsoleProperties();
		
		// Put the properties panel to right pane.
		lastCreatedSplitPanel.setRightComponent(propertiesPanel);
	}
	
	/**
	 * Display console properties.
	 * @param consoleName
	 */
	protected void displayConsoleProperties(String consoleName) {
		
		// Try to get console object.
		LogConsole console = consoles.get(consoleName);
		if (console == null) {
			
			propertiesPanel.resetComponents();
			return;
		}
		
		// Set console properties.
		propertiesPanel.displayProperties(console);
	}
	
	/**
	 * Restore consoles dimensions.
	 */
	private void restoreConsolesDimensions() {
		
		// Get number of consoles.
		int consolesCount = consoles.size();
		
		// Scroll panel dimensions.
		final Dimension scrollDimension = new Dimension(SCROLL_WIDTH, SCROLL_HEIGHT);
		
		// Set consoles' dimensions and states.
		int index = 0;
		for (Entry<String, LogConsole> entry : consoles.entrySet()) {
			
			LogConsole console = entry.getValue();
			
			// Set splitter ratio.
			double proportion;
			if (index < consolesCount - 1) {
				proportion = 1.0 / (1 + consolesCount - index);
			}
			else {
				proportion = 1.0 - PROPERTIES_PROPORTION;
			}
			console.splitPane.setDividerLocation(proportion);
			
			// Set scroll panel width.
			console.scrollPane.setPreferredSize(scrollDimension);
			
			index++;
		}
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds == null) {
			bounds = new Rectangle(1000, 700);
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Returns main frame window boundaries.
	 * @return
	 */
	public static Rectangle getFrameBounds() {
		
		if (mainFrame == null) {
			return new Rectangle();
		}
		
		Rectangle bounds = mainFrame.getBounds();
		return bounds;
	}
	
	/**
	 * Set main frame window boundaries.
	 * @param bounds
	 */
	public static void setFrameBounds(Rectangle bounds) {
		
		if (mainFrame == null) {
			return;
		}
		
		mainFrame.setBounds(bounds);
	}
	
	/**
	 * Get splitter positions.
	 * @return
	 */
	public static Integer [] getSplitterPositions() {
		
		// Initialize output array.
		int count = consoles.size();
		Integer [] splitterPositions = new Integer [count];
		
		// Set array items.
		Obj<Integer> index = new Obj<Integer>(0);
		consoles.forEach((name, console) -> {
			
			splitterPositions[index.ref++] = console.splitPane.getDividerLocation();
		});
		
		// Returns ooutput array.
		return splitterPositions;
	}
	
	/**
	 * Set splitter positions.
	 * @param splitterPositions
	 * @return
	 */
	public static boolean setSplitterPositions(Integer [] splitterPositions) {
		
		// Get consoles count.
		int count = consoles.size();
		
		// Set array items.
		Obj<Integer> index = new Obj<Integer>(0);
		for (Entry<String, LogConsole> entry : consoles.entrySet()) {
			
			if (index.ref >= count) {
				return false;
			}
			
			LogConsole console = entry.getValue();
			console.splitPane.setDividerLocation(splitterPositions[index.ref++]);
		};
		
		return true;
	}
	
	/**
	 * Select console with given name.
	 * @param consoleName
	 */
	protected void selectConsole(String consoleName) {
		
		// Try to set consoles selection states.
		consoles.forEach((name, console) -> {
			
			boolean isSelected = (name == consoleName);
			console.setSelected(isSelected);
		});
	}

	/**
	 * On clear cosnole.
	 * 
	 * @return
	 */
	private void onClearConsole() {
		
		// Find console by its text panel component.
		LogConsole console = findConsoleObject(lastFocusedTextPane);
		if (console == null) {
			return;
		}
		
		SwingUtilities.invokeLater(() -> {
			
			// Ask user if to delete console contents.
			boolean confirmed = Utility.ask2(this, "Clear \"%s\" contents?", console.name);
			if (!confirmed) {
				return;
			}
	
			// Clear console contents and display new console properties.
			console.clear();
			propertiesPanel.displayProperties(console);
		});
	}
	
	/**
	 * Find console by its text panel component.
	 * @param textPanel
	 * @return - console object that owns the text panel component or null if not found
	 */
	private LogConsole findConsoleObject(JTextPane textPanel) {
		
		// Check input value.
		if (textPanel == null) {
			return null;
		}
		
		// Try to find the console with input text panel.
		for (Entry<String, LogConsole> entry : consoles.entrySet()) {
			
			LogConsole console = entry.getValue();
			JTextPane listedTextPane = console.textPane;
			
			if (listedTextPane.equals(textPanel)) {
				return console;
			}
		}
		
		return null;
	}
	
	/**
	 * Create new read completion handler.
	 * @param completedLambda
	 * @param failedLambda
	 * @param nextReadLambda
	 * @return
	 */
	private CompletionHandler<Integer, String> newReadHandler(
				BiFunction<Integer, String, Boolean> completedLambda,
				BiConsumer<Throwable, String> failedLambda,
				BiConsumer<CompletionHandler<Integer, String>, String> nextReadLambda) {
		
		// Create completion handler.
        CompletionHandler<Integer, String> completionHandler = new CompletionHandler<Integer, String>() {
			@Override
			public void completed(Integer result, String consoleName) {
				
				SwingUtilities.invokeLater(() -> {
					
					// Call lambda for completion.
					boolean success = completedLambda.apply(result, consoleName);
					if (!success) {
						return;
					}
					
					CompletionHandler<Integer, String> nextHandler = newReadHandler(completedLambda, failedLambda, nextReadLambda);
					nextReadLambda.accept(nextHandler, consoleName);					
				});
			}
			@Override
			public void failed(Throwable exception, String consoleName) {
				// Call lambda for failed operation.
				failedLambda.accept(exception, consoleName);
			}
        };
        return completionHandler;
	}

	/**
	 * Open port for console with given name.
	 * @param consoleName
	 */
	private void openConsole(String consoleName) {
		
		try {
			// Try to get console by its name.
			LogConsole openedConsole = consoles.get(consoleName);
			if (openedConsole == null) {
				// Show error message.
				Utility.show2(this, consoleName + " not found.");
			}
			
			// Open asynchornous server socket.
	        openedConsole.inputSocket = AsynchronousServerSocketChannel.open();
	        openedConsole.socketAddress = new InetSocketAddress("localhost", openedConsole.port);
	        openedConsole.inputSocket.bind(openedConsole.socketAddress);
	        
	        // Set event that accept connections to input socket.
	        openedConsole.inputSocket.accept(consoleName, new CompletionHandler<AsynchronousSocketChannel, String>() {
	        
	        	// Event that is run when the socket connection is completed.
	        	@Override
				public void completed(AsynchronousSocketChannel client, String consoleName) {
	        		
    				try {
                		// Empty wrapper for the read failed exception.
                		Obj<Exception> readFailedException = new Obj<Exception>(null);
                		
        				// Accept successful read operation.
                		BiFunction<Integer, String, Boolean> completedLambda = (readResult, outputConsoleName) -> {
                			
                			synchronized (consoles) {
	                			// TODO: <---DEBUG
	                			System.out.format("[Enter completed]");
	                    		try {
                    			
	                    			// Try to get output cosole.
	                    			LogConsole outputConsole = consoles.get(outputConsoleName);
	                    			if (outputConsole == null) {
	                    				return false;
	                    			}
	                    			
		                    		// Read log messages from input buffer.
	                    			int messageCount = outputConsole.readLogMessages();
		                    		if (messageCount > 0) {
		                    			// Update the console after all messages from input buffer are read.
										updateConsole(outputConsole);
		                    		}
		                    		
									// Renew input buffer.
									if (outputConsole.inputBuffer.hasRemaining()) {
										outputConsole.inputBuffer.compact();
									}
									else {
										outputConsole.renewInputBuffer();
									}
									
									// TODO: <---DEBUG
									System.out.format("[Reading DONE]");
	                    		}
	                    		catch (Exception e)	{
	                    			readFailedException.ref = e;
	                    			// TODO: <---DEBUG
	                    			e.printStackTrace();
		                    		return false;
	                    		}									
                			}
                    		return true;
        				};
        				// Read exception.
        				BiConsumer<Throwable, String> failedLambda = (readException, outputConsoleName) -> {
        					
        					if (readException instanceof InterruptedByTimeoutException) {
        						return;
        					}
        					readFailedException.ref = new Exception(readException);
        					
        					// TODO: <---DEBUG
        					System.err.format("READING EXCEPTION\n");
        					readFailedException.ref.printStackTrace();
        					
        				};
        				// Read operation.
        				BiConsumer<CompletionHandler<Integer, String>, String> readLambda = (handler, outputConsoleName) -> {
        					synchronized (consoles) {
                    			// Try to get output cosole.
                    			LogConsole outputConsole = consoles.get(outputConsoleName);
                    			if (outputConsole == null) {
                    				return;
                    			}
        						client.read(outputConsole.inputBuffer, 10, TimeUnit.SECONDS, outputConsoleName, handler);
        					}
        				};
        				// Create completion handler.
                		CompletionHandler<Integer, String> handler = newReadHandler(completedLambda, failedLambda, readLambda);
                		// Run first read operation with firt completion handler. 
                		readLambda.accept(handler, consoleName);
    				}
	        		catch (Exception e) {
	        			// Show error message.
	        			e.printStackTrace();
	        		}
	        	}
	        	
				// If the connection failed...
	            public void failed(Throwable exception, String consoleNname) {
	    			// Show error message.
	    			exception.printStackTrace();
	            }
	        });	
		}
		catch (Exception e) {
			// Show error message.
			Utility.show2(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Get exception cause.
	 * @param ref
	 * @return
	 */
	protected Throwable getCause(Exception exception) {

		if (exception == null) {
			return new NullPointerException("Unknown exception object.");
		}
		Throwable cause = exception.getCause();
		if (cause == null) {
			return new NullPointerException("Unknown cause of exception.");
		}
		return cause;
	}

	/**
	 * Update the console.
	 * @param console
	 */
	private static void updateConsole(LogConsole console) {
	
		SwingUtilities.invokeLater(() -> {
			
			synchronized (console) {
				
				// Check timestamps for null values.
				if (console.maximumTimestamp == null || console.minimumTimestamp == null) {
					return;
				}
		
				// Get text view.
				JTextPane textPane = console.textPane;
		
				// Compile text contents.
				Obj<String> contents = new Obj<String>("<html>");
				
				// TODO: <---FIX Concurrent modification error.
				console.consoleRecords.forEach(messageRecord -> {
					
					String messageText = Utility.htmlSpecialChars(messageRecord.messageText);
					String colorString = Utility.getCssColor(messageRecord.color);
					String messageHtml = String.format("<div style='color: %s; font-family: Consolas; font-size: 14pt; white-space:nowrap;'>%s</div>", colorString, messageText);
					contents.ref += messageHtml;
				});
				
				// Set text of the text view.
				textPane.setText(contents.ref);
				
				// Move caret to the end of the view.
				int endPosition = textPane.getDocument().getLength();
				textPane.setCaretPosition(endPosition);
			}
		});
	}
	
	/**
	 * JUnit testing probe.
	 */
	public static int getJUnitProbe1() {
		
		LogConsole console1 = consoles.get("Console1");
		if (console1 == null) {
			return 0;
		}
		int count = console1.consoleRecords.size();
		return count;
	}
	
	
	/**
	 * JUnit testing probe.
	 */
	public static void runJUnitProbe2() {
		
		LogConsole console1 = consoles.get("Console1");
		if (console1 == null) {
			return;
		}		
		console1.clear();
		LogConsole console2 = consoles.get("Console2");
		if (console2 == null) {
			return;
		}			
		console2.clear();
		LogConsole console3 = consoles.get("Console3");
		if (console3 == null) {
			return;
		}			
		console3.clear();
		updateConsole(console1);
		updateConsole(console2);
		updateConsole(console3);
	}
}
