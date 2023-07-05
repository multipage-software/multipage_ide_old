/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 28-11-2018
 *
 */
package org.multipage.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.multipage.util.Obj;
import org.multipage.util.j;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

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
	 * Open ports and created views.
	 */
	public static int [] openPorts = new int [] { 48000, 48001, 48002 };
	private static Map<String, JTextPane> consoleViews = new ConcurrentHashMap<>();
	
	/**
	 * Socket tiemout in milliseconds.
	 */
	private static final int SOCKET_TIMEOUT_MS = 250;
	
	/**
	 * Stop symbol for incomming log message byte string.
	 */
	private static final byte [] STOP_SYMBOL = { 0, 0 };
	
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
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		applicationState = STARTUP;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Consoles frame = new Consoles();
					frame.setAlwaysOnTop(true);
					frame.setVisible(true);
				}
				catch (Exception e) {
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
		main(new String [] {});
		
		// Wait for application listening to ports.
		while (applicationState != LISTENING) {
			
			if (applicationState == SHUTDOWN) {
				return false;
			}
			
			try {
				Thread.sleep(STARTUP_TIMEOUT_MS);
			}
			catch (Exception e) {
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
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
	}
	
	/**
	 * Add new console view.
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
		textPane.setContentType("text/plain");
		scrollPane.setViewportView(textPane);
		
		tabbedPane.addTab(consoleName, null, panel, null);
		
		consoleViews.put(consoleName, textPane);
		
		// Open console port.
		openConsole(consoleName, port);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		// Open ports for consoles.
		addConsoleView("Console1", 48000);
		addConsoleView("Console2", 48001);
		addConsoleView("Console3", 48002);
		
		applicationState = LISTENING;
	}
	
	/**
	 * Open port for console with given name.
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
	            	catch (SocketTimeoutException e) {
	            		j.log("TICK");
	            		continue;
	            	}
	            	
	                // Handle the client connection in a separate thread.
	                Thread serverThread = new Thread(() -> {
	                	
	                	while (!exitApplication) {

	                		try {
		                		// Read log message until we reach the stop symbol.
		                		String logMesssage = readMessage(clientSocket.ref, STOP_SYMBOL);
		                		if (logMesssage != null) {
		                			
		                			// Display log message on console.
			                		displayMessage(consoleName, logMesssage);
		                		}
	                		}
	                		catch (SocketTimeoutException e) {
	                			continue;
	                		} 
	                		catch (Exception e) {
								e.printStackTrace();
							}
	                	}
	                });
	                serverThread.start();
	            }
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	        finally {
	        	try {
	        		serverSocket.close();
	        	}
	        	catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        }
			
		});
		connectionThread.start();
	}
	
	/**
	 * Read a message from the client socket until the stop symbol is reached.
	 * @param clientSocket
	 * @param stopSymbol
	 * @return
	 * @throws SocketTimeoutException
	 */
	private String readMessage(Socket clientSocket, byte[] stopSymbol)
			throws Exception {
		
		int timoutMs = 10 * SOCKET_TIMEOUT_MS;
		
		InputStream stream = clientSocket.getInputStream();
		
		byte [] inputBytes = new byte [BUFFER_SIZE];
		Obj<ByteBuffer> outputBuffer = new Obj<ByteBuffer>(ByteBuffer.allocate(BUFFER_SIZE));
		Obj<Boolean> terminated = new Obj<Boolean>(false);
		
		while (!exitApplication) {
			
			// Read socket input bytes.
			try {
				int bytesRead = stream.read(inputBytes);
				if (bytesRead > 0) {
					
					Utility.readUntil(inputBytes, outputBuffer, BUFFER_SIZE, stopSymbol, terminated);
					
					if (terminated.ref) {
						
						// Return the messahe.
						outputBuffer.ref.flip();
						int length = outputBuffer.ref.limit();
						byte [] output = new byte [length];
						
						outputBuffer.ref.get(output);
						String message = new String(output, "UTF-8");
						
						return message;
					}
				}
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
			}
			else {
				throw new SocketTimeoutException("Read message timeout.");
			}
		}
		
		return null;
	}
	
	/**
	 * Display the log message on the console designated with input name.
	 * @param consoleName
	 * @param logMessage
	 */
	private void displayMessage(String consoleName, String logMessage)
			throws Exception {
		
		// Tru to get console view by its name.
		JTextPane consoleView = consoleViews.get(consoleName);
		if (consoleView == null) {
			new IllegalStateException("Console " + consoleName + " not found");
		}
		
		// Get contents.
		String contents = consoleView.getText().trim();
		
		// Add new log message at the end.
		contents += '\n' + logMessage.trim();
		consoleView.setText(contents);
		
        int endPosition = consoleView.getDocument().getLength();
        consoleView.setCaretPosition(endPosition);
	}
}
