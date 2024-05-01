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
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
public class LogConsoles extends JFrame {

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
	 * Maps console names to console objects.
	 */
	private static Map<String, LogConsole> consoles = new ConcurrentHashMap<>();

	/**
	 * Log message divider and stop symbols.
	 */
	static final byte [] START_OF_HEADING = { (byte) 0x00, (byte) 0x01 };
	static final byte [] START_OF_TEXT = { (byte) 0x00, (byte) 0x02 };
	static final byte [] END_OF_TRANSMISSION = { (byte) 0x00, (byte) 0x04 };
	
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
	protected static LogConsoles mainFrame = null;
	
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
					mainFrame  = new LogConsoles();
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
	public LogConsoles() {

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
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosing();
			}
		});
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
			addConsoleView(consoles, "Console" + (index + 1), port);
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
	private void addConsoleView(Map<String, LogConsole> consoles, String consoleName, int port) {
		
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
		
		// Try to get packet channel object.
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

		// Ask user if to delete console contents.
		boolean confirmed = Utility.ask2(this, "Clear \"%s\" contents?", console.name);
		if (!confirmed) {
			return;
		}

		// Clear console contents and display new console properties.
		console.clear();
		propertiesPanel.displayProperties(console);
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
	 * Open port for console with given name.
	 * @param consoleName
	 */
	private void openConsole(String consoleName) {
		
		try {
			// Try to get console by its name.
			LogConsole console = consoles.get(consoleName);
			if (console == null) {
				// Show error message.
				Utility.show2(this, consoleName + " not found.");
			}
			
			// Open asynchornous server socket.
			console.openInputSocket();
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
		console1.update();
		console2.update();
		console3.update();
	}
}
