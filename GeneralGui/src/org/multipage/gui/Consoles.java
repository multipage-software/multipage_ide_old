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
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.multipage.util.Obj;
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
	private static final class MessageRecord {
		
		/**
		 * Timestamp.
		 */
		LocalTime timestamp = null;
		
		/**
		 * Record text.
		 */
		String messageText = null;
		
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
	}

	/**
	 * Console class.
	 */
	private static final class Console {

		/**
		 * Console name.
		 */
		String name = null;

		/**
		 * Console text pane.
		 */
		JTextPane textPane = null;

		/**
		 * Minimum and maximum timestamps.
		 */
		LocalTime minimumTimestamp = null;
		LocalTime maximumTimestamp = null;
		
		/**
		 * Console port number.
		 */
		int port = -1;

		/**
		 * Message record list that maps time axis to the records.
		 */
		LinkedList<MessageRecord> consoleRecords = new LinkedList<>();

		/**
		 * 
		 * @param consoleName
		 * @param textPane
		 * @param port
		 */
		public Console(String consoleName, JTextPane textPane, int port) {

			name = consoleName;
			this.textPane = textPane;
			this.port = port;
		}

		/**
		 * Add new message record.
		 * 
		 * @param messageString
		 */
		public void addMessageRecord(String messageString) {

			// Get current time.
			LocalTime timeNow = LocalTime.now();

			// Set maximum and minimum timestamp.
			if (minimumTimestamp == null) {
				minimumTimestamp = timeNow;
			}
			if (maximumTimestamp == null || maximumTimestamp.compareTo(timeNow) < 0) {
				maximumTimestamp = timeNow;
			}

			// Create new message record and put it into the message map.
			MessageRecord messageRecord = new MessageRecord(timeNow, messageString);
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
	}

	/**
	 * 
	 */
	private static Map<String, Console> consoleViews = new ConcurrentHashMap<>();

	/**
	 * Socket tiemout in milliseconds.
	 */
	private static final int SOCKET_TIMEOUT_MS = 250;

	/**
	 * Log message divider and stop symbols.
	 */
	private static final byte[] DIVIDER_SYMBOL = { 0, 0 };
	private static final byte[] STOP_SYMBOL = { 0, 0, 0, 0 } ;

	/**
	 * Input buffer size.
	 */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Set this flag to true on application exit.
	 */
	private boolean exitApplication = false;

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
		textPane.setContentType("text/html");
		scrollPane.setViewportView(textPane);

		tabbedPane.addTab(consoleName, null, panel, null);

		Console console = new Console(consoleName, textPane, port);
		consoleViews.put(consoleName, console);

		// Open console port.
		openConsole(consoleName, port);
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

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", "#Clear console",
				() -> onClearConsole());
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
		Console console = consoleViews.get(consoleName);
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
	 * 
	 * @param consoleName
	 * @param portNumber
	 */
	private void openConsole(String consoleName, int portNumber) {

		Thread connectionThread = new Thread(() -> {

			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(portNumber);
				System.out.println("Console " + consoleName + " listening on port " + portNumber);

				// Set socket timeout.
				serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);

				while (!exitApplication) {

					Obj<Socket> clientSocket = new Obj<Socket>(null);
					try {
						clientSocket.ref = serverSocket.accept();
						System.out.println("New connection from " + clientSocket.ref.getInetAddress().getHostAddress());
					}
					// When timout ellapsed, continue to with the loop.
					catch (SocketTimeoutException e) {
						continue;
					}

					// Handle the client connection in a separate thread.
					Thread serverThread = new Thread(() -> {

						while (!exitApplication) {

							try {
								// Read log message until we reach the stop symbol.
								MessageRecord logMesssage = readMessage(clientSocket.ref, STOP_SYMBOL);
								if (logMesssage != null) {

									// Add log message
									addMessage(consoleName, logMesssage.messageText);

									// Update the console.
									updateConsole(consoleName);
								}
							} catch (SocketTimeoutException e) {
								continue;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					serverThread.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		connectionThread.start();
	}

	/**
	 * Read a message from the client socket until the stop symbol is reached.
	 * 
	 * @param clientSocket
	 * @param terminalSymbol
	 * @return
	 * @throws SocketTimeoutException
	 */
	private MessageRecord readMessage(Socket clientSocket, byte[] terminalSymbol) throws Exception {

		int timoutMs = 10 * SOCKET_TIMEOUT_MS;

		InputStream stream = clientSocket.getInputStream();

		byte[] inputBytes = new byte[BUFFER_SIZE];
		Obj<ByteBuffer> outputBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(BUFFER_SIZE));
		Obj<Boolean> terminated = new Obj<Boolean>(false);

		while (!exitApplication) {

			// Read socket input bytes.
			try {
				int bytesRead = stream.read(inputBytes);
				if (bytesRead > 0) {

					Utility.readUntil(inputBytes, outputBuffer, BUFFER_SIZE, terminalSymbol, terminated);

					if (terminated.ref) {

						// Return the messahe.
						outputBuffer.ref.flip();
						
						int length = outputBuffer.ref.limit();
						byte[] output = new byte[length];
						outputBuffer.ref.get(output);
						
						List<String> byteStrings = Utility.splitBytesToStrings(output, BUFFER_SIZE, DIVIDER_SYMBOL, terminalSymbol);
						int stringCount = byteStrings.size();
						
						// Get message timestamp.
						LocalTime timestamp = null;
						if (stringCount > 0) { 
							String timeStampText = byteStrings.get(0);
							timestamp = LocalTime.parse(timeStampText, TIMESTAMP_FORMAT);
						}	
						
						// Get message text.
						String messageString = null;
						if (stringCount > 1) {
							messageString = byteStrings.get(1);
						}
						
						MessageRecord messageRecord = new MessageRecord(timestamp, messageString);
						return messageRecord;
					}
				}
			}
			catch (DateTimeParseException  e) {
				throw e;
			}
			catch (IOException e) {
				// EOF reached. Below wait for the next socket read operation.
			}
			catch (Exception e) {
				throw new IllegalStateException("Error reading form socket.");
			}

			// Idle timeout.
			Thread.sleep(SOCKET_TIMEOUT_MS);
			timoutMs -= SOCKET_TIMEOUT_MS;

			if (timoutMs >= 0) {
				continue;
			} else {
				throw new SocketTimeoutException("Read message timeout.");
			}
		}

		return null;
	}

	/**
	 * Add the log message into the console with given name.
	 * 
	 * @param consoleName
	 * @param logMessage
	 */
	private void addMessage(String consoleName, String logMessage) throws Exception {

		// Check input message.
		if (logMessage == null) {
			return;
		}

		// Try to get console by its name.
		Console console = consoleViews.get(consoleName);
		if (console == null) {
			new IllegalStateException("Console " + consoleName + " not found");
		}

		logMessage = logMessage.trim();
		if (logMessage.isEmpty()) {
			return;
		}

		console.addMessageRecord(logMessage.trim());
	}

	/**
	 * Update the console.
	 * 
	 * @param consoleName
	 */
	private void updateConsole(String consoleName) {

		// Try to get console by its name.
		Console console = consoleViews.get(consoleName);
		if (console == null) {
			new IllegalStateException("Console " + consoleName + " not found");
		}
		
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

		synchronized (console.consoleRecords) {
			console.consoleRecords.forEach(messageRecord -> {

				String color = null;
				if (!highlighted.ref && messageRecord.timestamp.compareTo(sliderTimestamp) > 0) {
					color = "red";
					highlighted.ref = true;
				} else {
					color = "black";
				}
				contents.ref += String.format("<span style='color:%s;'>%s</span><br>", color, messageRecord.messageText);
			});
		}

		// Set text of the text view.
		textPane.setText(contents.ref);

		// Move caret to the end of the view.
		int endPosition = textPane.getDocument().getLength();
		textPane.setCaretPosition(endPosition);
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

		// Update console.
		updateConsole(consoleName);
	}
}
