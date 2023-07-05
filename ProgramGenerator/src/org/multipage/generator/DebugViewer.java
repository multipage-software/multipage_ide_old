/*
 * Copyright 2010-2018 (C) vakol
 * 
 * Created on : 31-07-2018
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.maclan.server.AreaServerSignal;
import org.maclan.server.DebugListener;
import org.maclan.server.DebugListenerSession;
import org.maclan.server.XdebugClientParameters;
import org.maclan.server.XdebugListener;
import org.maclan.server.XdebugListenerOld;
import org.maclan.server.XdebugListenerSession;
import org.maclan.server.XdebugPacketOld;
import org.multipage.gui.AlertWithTimeout;
import org.multipage.gui.Callback;
import org.multipage.gui.ConditionalEvents;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.DOM;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;
import org.w3c.dom.Node;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;

/**
 * This is GUI for debugging
 * @author vakol
 *
 */
public class DebugViewer extends JFrame {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * GUI watchdog timeout in ms.
	 */
	private static final int WATCHDOG_TIMEOUT_MS = 1000;
	
	/**
	 * Table font size.
	 */
	private static final int TABLE_FONT_SIZE = 9;
	
	/**
	 * Table header height.
	 */
	private static final int TABLE_HEADER_HEIGHT = 12;
	
	// $hide>>$
	/**
	 * Window boundary
	 */
	private static Rectangle bounds;
	
	/**
	 * Text constants that are dislpayed with the dialog.
	 */
	private static String textConnected = "org.multipage.generator.textDebuggerConnected";
    private static String textNotConnected = "org.multipage.generator.textDebuggerNotConnected";
    
    /**
     * Load text resources.
     */
    private static void loadTextResources() {
    		
    	// Load static texts from text resources.
		textConnected = Resources.getString(textConnected);
		textNotConnected = Resources.getString(textNotConnected);
    }
    
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		
		// Load static texts from text resources.
		loadTextResources();
	}
	
	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		
		// Load static texts from text resources.
		loadTextResources();
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(StateOutputStream outputStream)
		throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Debug viewer singleton object.
	 */
    private static DebugViewer instance;
    
    /**
     * Get debug viewer singleton.
     * @param parent 
     * @return
     */
    public static DebugViewer getInstance(Component parent) {
        if (instance == null) {
            instance = new DebugViewer(parent);
        }
        return instance;
    }
    
    /**
     * Get debug viewer singleton.
     * @return
     */
	public static DebugViewer getInstance() {
		
		// Delegate the call.
		return getInstance(null);
	}
	
	/**
	 * Header to display
	 */
	private String header;
	
	/**
	 * Lines of code to display or null if there is nothing to display
	 */
	private LinkedList<String> codeLines;
	
	/**
	 * Script file name
	 */
	private String scriptFileName = "";
	
	/**
	 * A line number of debugger step
	 */
	private int stepLineNumber = -1;
	
	/**
	 * Watch list model
	 */
	private DefaultListModel<String> listWatchModel;
	
	/**
	 * Stack list model
	 */
	private DefaultListModel<Node> listStackModel;
	
	/**
	 * Class for log items.
	 */
	private static class LogMessage extends LoggingDialog.LoggedMessage {
		
		/**
		 * List of log messages.
		 */
		private static LinkedList<LogMessage> listLoggedMessages = new LinkedList<LogMessage>();
		
		/**
		 * Filter string.
		 */
		private static String filterString = "*";
		
		/**
		 * Filter flags.
		 */
		private static boolean caseSensitive = false;
		private static boolean wholeWords = false;
		private static boolean exactMatch = false;
		
		/**
		 * Constructor.
		 * @param message
		 */
		public LogMessage(String message) {
			super(message);
		}
		
		/**
		 * Add new log message.
		 * @param message
		 */
		public static void addLogMessage(String message) {
			
			LogMessage logMessage = new LogMessage(message);
			listLoggedMessages.addLast(logMessage);
		}
		
		/**
		 * Display HTML log.
		 * @param textPane
		 */
		public static void displayHtmlLog(JTextPane textPane) {
			
			String logContent = "";
			
			for (LogMessage logMessage : listLoggedMessages) {
				
				String messageText = logMessage.getText();
				
				// Filter messages.
				if (!filter(messageText)) {
					continue;
				}
				
				// Append message text.
				logContent += messageText + "<br/>";
			}
			
			// Wrap content with HTML tags.
			logContent = String.format("<html>%s</html>", logContent);
			textPane.setText(logContent);
		}
		
		/**
		 * Returns false if the message is filtered or true if the message passes.
		 * @param messageText
		 * @return
		 */
		private static boolean filter(String messageText) {

			boolean matches = Utility.matches(messageText, filterString, caseSensitive, wholeWords, exactMatch);
			
			// TODO: <---DEBUGGER Check filter match.
			j.log("FILTER MATCH %b", matches);
			
			return matches;
		}

		/**
		 * Set filter and display filtered log messages.
		 * @param filterString
		 * @param caseSensitive
		 * @param wholeWords
		 * @param exactMatch
		 */
		public static void setFulltextFilter(String filterString, boolean caseSensitive, boolean wholeWords, boolean exactMatch) {
			
			LogMessage.filterString = !filterString.isEmpty() ?  filterString : "*";
			LogMessage.caseSensitive = caseSensitive;
			LogMessage.wholeWords = wholeWords;
			LogMessage.exactMatch = exactMatch;
		}
	}
	
	/**
	 * Display code callbacks
	 */
	private class DisplayCodeInterface {
		
		/**
		 * Get line
		 */
		String line() {
			return "";
		}
	}

	/**
	 * Object status
	 */
	private Object status;
	
	/**
	 * GUI watchdog timer.
	 */
	private Timer watchdogTimer = null;
	
	/**
	 * Attached listener.
	 */
	private DebugListener attachedListener = null;

	// $hide<<$
	
	/**
	 * Controls
	 */
	private JEditorPane textCode;
	private JEditorPane textInfo;
	private JTextField textCommand;
	private JButton buttonSend;
	private JPanel panelBottom;
	private JTabbedPane tabbedPane;
	private JPanel panelWatch;
	private JPanel panelStack;
	private JPanel panelCommand;
	private JScrollPane scrollPaneOutput;
	private JScrollPane scrollPaneWatch;
	private JScrollPane scrollPaneStack;
	private JPanel panelRight;
	private JPanel panelLeft;
	private JToolBar toolBar;
	private JButton buttonRun;
	private JScrollPane scrollCodePane;
	private JPanel panelStatus;
	private JLabel labelStatus;
	private JButton buttonExit;
	private JButton buttonStepInto;
	private JButton buttonStepOut;
	private JButton buttonStepOver;
	private JList listWatch;
	private JList listStack;

    /**
     * Constructor.
     * @param parent 
     */
    private DebugViewer(Component parent) {
    	
		initComponents();
		postCreate(); // $hide$    	
    }

	/**
	 * Initialize components
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
			@Override
			public void windowOpened(WindowEvent arg0) {
				onOpen();
			}
		});
		
		setBounds(100, 100, 909, 654);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.7);
		getContentPane().add(splitPane);
		
		panelRight = new JPanel();
		splitPane.setRightComponent(panelRight);
		panelRight.setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		panelRight.add(tabbedPane, BorderLayout.CENTER);
		
		panelWatch = new JPanel();
		tabbedPane.addTab("Watch", null, panelWatch, null);
		panelWatch.setLayout(new BorderLayout(0, 0));
		
		scrollPaneWatch = new JScrollPane();
		panelWatch.add(scrollPaneWatch);
		
		listWatch = new JList();
		scrollPaneWatch.setViewportView(listWatch);
		
		panelStack = new JPanel();
		tabbedPane.addTab("Stack", null, panelStack, null);
		panelStack.setLayout(new BorderLayout(0, 0));
		
		scrollPaneStack = new JScrollPane();
		panelStack.add(scrollPaneStack);
		
		listStack = new JList();
		scrollPaneStack.setViewportView(listStack);
		
		panelOutput = new JPanel();
		tabbedPane.addTab("Output", null, panelOutput, null);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panelOutput.add(scrollPane);
		
		textOutput = new JTextArea();
		textOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textOutput.setEditable(false);
		scrollPane.setViewportView(textOutput);
		
		panelCommand = new JPanel();
		tabbedPane.addTab("Command", null, panelCommand, null);
		panelCommand.setLayout(new BorderLayout(0, 0));
		
		scrollPaneOutput = new JScrollPane();
		scrollPaneOutput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelCommand.add(scrollPaneOutput);
		
		textInfo = new JEditorPane();
		textInfo.setPreferredSize(new Dimension(30, 30));
		scrollPaneOutput.setViewportView(textInfo);
		textInfo.setEditable(false);
		
		panelBottom = new JPanel();
		panelCommand.add(panelBottom, BorderLayout.SOUTH);
		panelBottom.setPreferredSize(new Dimension(10, 19));
		panelBottom.setLayout(new BorderLayout(0, 0));
		
		textCommand = new JTextField();
		textCommand.setPreferredSize(new Dimension(6, 24));
		panelBottom.add(textCommand, BorderLayout.CENTER);
		textCommand.setColumns(10);
		
		buttonSend = new JButton("Send");
		panelBottom.add(buttonSend, BorderLayout.EAST);
		
		panelExceptions = new JPanel();
		panelExceptions.setBackground(Color.WHITE);
		panelExceptions.setOpaque(false);
		tabbedPane.addTab("Exceptions", null, panelExceptions, null);
		panelExceptions.setLayout(new BorderLayout(0, 0));
		
		scrollPaneExceptions = new JScrollPane();
		scrollPaneExceptions.setBorder(null);
		scrollPaneExceptions.setOpaque(false);
		scrollPaneExceptions.setBackground(Color.WHITE);
		panelExceptions.add(scrollPaneExceptions, BorderLayout.CENTER);
		
		textExceptions = new JTextPane();
		textExceptions.setContentType("text/html");
		textExceptions.setBorder(null);
		scrollPaneExceptions.setViewportView(textExceptions);
		
		panelSearch = new JPanel();
		panelSearch.setBorder(null);
		panelSearch.setOpaque(false);
		panelSearch.setBackground(Color.WHITE);
		panelSearch.setPreferredSize(new Dimension(10, 52));
		panelExceptions.add(panelSearch, BorderLayout.SOUTH);
		SpringLayout sl_panelSearch = new SpringLayout();
		panelSearch.setLayout(sl_panelSearch);
		
		labelFilter = new JLabel("org.multipage.generator.messageFilterDebugVievewLog");
		sl_panelSearch.putConstraint(SpringLayout.NORTH, labelFilter, 6, SpringLayout.NORTH, panelSearch);
		sl_panelSearch.putConstraint(SpringLayout.WEST, labelFilter, 10, SpringLayout.WEST, panelSearch);
		panelSearch.add(labelFilter);
		
		textFilter = new TextFieldEx();
		sl_panelSearch.putConstraint(SpringLayout.NORTH, textFilter, 6, SpringLayout.NORTH, panelSearch);
		sl_panelSearch.putConstraint(SpringLayout.WEST, textFilter, 3, SpringLayout.EAST, labelFilter);
		sl_panelSearch.putConstraint(SpringLayout.EAST, textFilter, -10, SpringLayout.EAST, panelSearch);
		panelSearch.add(textFilter);
		textFilter.setColumns(10);
		
		checkCaseSensitive = new JCheckBox("org.multipage.generator.textCaseSensitive");
		checkCaseSensitive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCaseSensitiveChange();
			}
		});
		checkCaseSensitive.setOpaque(false);
		sl_panelSearch.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 6, SpringLayout.SOUTH, labelFilter);
		sl_panelSearch.putConstraint(SpringLayout.WEST, checkCaseSensitive, 0, SpringLayout.WEST, labelFilter);
		panelSearch.add(checkCaseSensitive);
		
		checkWholeWords = new JCheckBox("org.multipage.generator.textWholeWords");
		checkWholeWords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onWholeWordsChange();
			}
		});
		sl_panelSearch.putConstraint(SpringLayout.WEST, checkWholeWords, 6, SpringLayout.EAST, checkCaseSensitive);
		sl_panelSearch.putConstraint(SpringLayout.SOUTH, checkWholeWords, 0, SpringLayout.SOUTH, checkCaseSensitive);
		checkWholeWords.setOpaque(false);
		panelSearch.add(checkWholeWords);
		
		checkExactMatch = new JCheckBox("org.multipage.generator.textExactMatch");
		checkExactMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExactMatchChange();
			}
		});
		sl_panelSearch.putConstraint(SpringLayout.WEST, checkExactMatch, 6, SpringLayout.EAST, checkWholeWords);
		sl_panelSearch.putConstraint(SpringLayout.SOUTH, checkExactMatch, 0, SpringLayout.SOUTH, checkCaseSensitive);
		checkExactMatch.setOpaque(false);
		panelSearch.add(checkExactMatch);
		
		panelDebuggers = new JPanel();
		tabbedPane.addTab("Processes", null, panelDebuggers, null);
		SpringLayout sl_panelDebuggers = new SpringLayout();
		panelDebuggers.setLayout(sl_panelDebuggers);
		
		JPanel panelProcesses = new JPanel();
		sl_panelDebuggers.putConstraint(SpringLayout.WEST, panelProcesses, 3, SpringLayout.WEST, panelDebuggers);
		panelProcesses.setPreferredSize(new Dimension(150, 100));
		sl_panelDebuggers.putConstraint(SpringLayout.NORTH, panelProcesses, 3, SpringLayout.NORTH, panelDebuggers);
		panelDebuggers.add(panelProcesses);
		panelProcesses.setLayout(new BorderLayout(0, 0));
		
		labelProcesses = new JLabel("org.multipage.generator.textDebuggedProcesses");
		panelProcesses.add(labelProcesses, BorderLayout.NORTH);
		
		JScrollPane scrollPaneProcesses = new JScrollPane();
		panelProcesses.add(scrollPaneProcesses, BorderLayout.CENTER);
		
		tableProcesses = new JTable();
		tableProcesses.setRowHeight(12);
		tableProcesses.setFont(new Font("Tahoma", Font.PLAIN, 9));
		tableProcesses.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPaneProcesses.setViewportView(tableProcesses);
		
		JPopupMenu popupMenu = new JPopupMenu();
		menuDisplaySessionProperties = new JMenuItem("org.multipage.generator.messageDisplayXdebugSessionProperties");
		menuDisplaySessionProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displaySession();
			}
		});
		popupMenu.add(menuDisplaySessionProperties);
		addPopup(tableProcesses, popupMenu);
		
		JPanel panelThreads = new JPanel();
		sl_panelDebuggers.putConstraint(SpringLayout.NORTH, panelThreads, 3, SpringLayout.SOUTH, panelProcesses);
		sl_panelDebuggers.putConstraint(SpringLayout.WEST, panelThreads, 0, SpringLayout.WEST, panelProcesses);
		sl_panelDebuggers.putConstraint(SpringLayout.SOUTH, panelThreads, -3, SpringLayout.SOUTH, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.EAST, panelThreads, 0, SpringLayout.EAST, panelProcesses);
		panelDebuggers.add(panelThreads);
		panelThreads.setLayout(new BorderLayout(0, 0));
		
		labelThreads = new JLabel("org.multipage.generator.textDebuggedThreads");
		panelThreads.add(labelThreads, BorderLayout.NORTH);
		
		JScrollPane scrollPaneThreads = new JScrollPane();
		panelThreads.add(scrollPaneThreads, BorderLayout.CENTER);
		
		tableThreads = new JTable();
		tableThreads.setFont(new Font("Tahoma", Font.PLAIN, 9));
		tableThreads.setRowHeight(12);
		scrollPaneThreads.setViewportView(tableThreads);
		
		JPanel panelThreadStack = new JPanel();
		sl_panelDebuggers.putConstraint(SpringLayout.NORTH, panelThreadStack, 3, SpringLayout.NORTH, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.WEST, panelThreadStack, 3, SpringLayout.EAST, panelProcesses);
		sl_panelDebuggers.putConstraint(SpringLayout.SOUTH, panelThreadStack, -3, SpringLayout.SOUTH, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.EAST, panelThreadStack, 3, SpringLayout.EAST, panelDebuggers);
		panelDebuggers.add(panelThreadStack);
		panelThreadStack.setLayout(new BorderLayout(0, 0));
		
		labelThreadStack = new JLabel("org.multipage.generator.textDebuggedStack");
		panelThreadStack.add(labelThreadStack, BorderLayout.NORTH);
		
		JScrollPane scrollPaneThreadStack = new JScrollPane();
		panelThreadStack.add(scrollPaneThreadStack, BorderLayout.CENTER);
		
		tableStack = new JTable();
		scrollPaneThreadStack.setViewportView(tableStack);
		buttonSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSendCommand();
			}
		});
		
		panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(new BorderLayout(0, 0));
		
		scrollCodePane = new JScrollPane();
		scrollCodePane.setPreferredSize(new Dimension(30, 30));
		panelLeft.add(scrollCodePane, BorderLayout.CENTER);
		
		textCode = new JEditorPane();
		scrollCodePane.setViewportView(textCode);
		textCode.setPreferredSize(new Dimension(20, 20));
		textCode.setContentType("text/html");
		textCode.setEditable(false);
		
		toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		
		buttonRun = new JButton("run");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRun();
			}
		});
		
		buttonStepInto = new JButton("step into");
		buttonStepInto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepInto();
			}
		});
		buttonStepInto.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepInto);
		
		buttonStepOver = new JButton("step over");
		buttonStepOver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepOver();
			}
		});
		buttonStepOver.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOver);
		
		buttonStepOut = new JButton("step out");
		buttonStepOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onStepOut();
			}
		});
		buttonStepOut.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOut);
		buttonRun.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonRun);
		
		buttonExit = new JButton("exit");
		buttonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onExit();
			}
		});
		buttonExit.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonExit);
				
		panelStatus = new JPanel();
		panelStatus.setPreferredSize(new Dimension(10, 25));
		getContentPane().add(panelStatus, BorderLayout.SOUTH);
		FlowLayout fl_panelStatus = new FlowLayout(FlowLayout.RIGHT, 5, 5);
		panelStatus.setLayout(fl_panelStatus);
		
		buttonConnected = new JButton("");
		buttonConnected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConnectedClick();
			}
		});
		buttonConnected.setPreferredSize(new Dimension(80, 16));
		buttonConnected.setAlignmentY(0.0f);
		buttonConnected.setMargin(new Insets(0, 0, 0, 0));
		panelStatus.add(buttonConnected);
		
		labelStatus = new JLabel("status");
		panelStatus.add(labelStatus);
	}

	/**
	 * Post creation
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		createViews();
		setListeners();
		
		establishWatchDog();
		
		initStackDump();
		initWatch();
		
		loadDialog();
		
		// Update dialog status panel.
		updateStatusPanel();
		
		// Start watch dog.
		startWatchDog();
	}
	
	/**
	 * Create views placed in this dialog that display debug information.
	 */
	private void createViews() {
		
		// Create processes view.
		createProcessesView();
		
		// Create thread view.
		createThreadView();
	}
	
	/**
	 * Create table that can display debugged processes.
	 */
	private void createProcessesView() {
		
		// Create the table model.
        @SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel() {
        	// Disable cell modification.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				// Do nothing.
			}
        };
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerSessionId"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebuggedHost"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebuggedPort"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebuggedProcess"));
        tableProcesses.setModel(model);
        
        // Create column model.
        TableColumnModel columnModel = tableProcesses.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(50);
        
        // Create the table renderer.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        tableProcesses.setDefaultRenderer(Object.class, renderer);
        
        // Set the JTable properties.
        tableProcesses.setPreferredScrollableViewportSize(tableProcesses.getPreferredSize());
        
        // Set column editor font size.
        Font tableFont = tableProcesses.getFont().deriveFont(TABLE_FONT_SIZE);
        Utility.setCellEditorFont(tableProcesses, tableFont);
        
        // Set column font size.
        JTableHeader header = tableProcesses.getTableHeader();
        header.setFont(tableFont);
        header.setPreferredSize(new Dimension(0, TABLE_HEADER_HEIGHT));
	}
	
	/**
	 * Create thread view.
	 */
	private void createThreadView() {
		
		// Create the table model.
        @SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel() {
        	// Disable cell modification.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				// Do nothing.
			}
        };
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerThreadId"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebuggedThreadName"));
        tableThreads.setModel(model);
        
        // Create column model.
        TableColumnModel columnModel = tableProcesses.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);

        // Create the table renderer.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        tableThreads.setDefaultRenderer(Object.class, renderer);
        
        // Set the JTable properties.
        tableThreads.setPreferredScrollableViewportSize(tableThreads.getPreferredSize());
        
        // Set column editor font size.
        Font tableFont = tableThreads.getFont().deriveFont(TABLE_FONT_SIZE);
        Utility.setCellEditorFont(tableThreads, tableFont);
        
        // Set column font size.
        JTableHeader header = tableThreads.getTableHeader();
        header.setFont(tableFont);
        header.setPreferredSize(new Dimension(0, TABLE_HEADER_HEIGHT));
	}
	
	/**
	 * Display session information.
	 * @throws Exception 
	 */
	protected void displaySession() {
		
		// Get current selected session.
		int selectedRow = tableProcesses.getSelectedRow();
		if (selectedRow < 0) {
			return;
		}
		
		Object cellValue = tableProcesses.getModel().getValueAt(selectedRow, 0);
		if (!(cellValue instanceof Long)) {
			return;
		}
		
		Long sessionId = (Long) cellValue;
		if (attachedListener == null) {
			return;
		}
		
		try {
			// Display session properties. Also dsplay connection URL.
			DebugListenerSession session = attachedListener.getSession(sessionId);
			if (session instanceof XdebugListenerSession) {
				
				XdebugListenerSession xdebugSession = (XdebugListenerSession) session;
				XdebugSessionDialog.showDialog(this, xdebugSession);
			}
		}
		catch (Exception e) {
			String errorMessage = e.getLocalizedMessage();
			Utility.show2(this, errorMessage);
		}
	}
	
	/**
	 * Starts watch dog that polls miscelaneous debug states.
	 */
	private void startWatchDog() {
		
		watchdogTimer = new Timer(WATCHDOG_TIMEOUT_MS, new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        onWatchdogTick();
		    }
		});
		watchdogTimer.setRepeats(true);
		watchdogTimer.setCoalesce(true);
		watchdogTimer.setInitialDelay(WATCHDOG_TIMEOUT_MS);
		watchdogTimer.setDelay(WATCHDOG_TIMEOUT_MS);
		watchdogTimer.start();
	}
	
	/**
	 * Dospose watchdog.
	 */
	private void disposeWatchdog() {
		
		// Stop GUI watchdog.
		if (watchdogTimer != null) {
			watchdogTimer.stop();
		}
	}


	/**
	 * Attach debug listener.
	 * @param listener
	 */
	public void attachDebugger(DebugListener listener) {
		
		// Assign debug viewer.
		listener.setViewerComponent(this);
		
		// Add new lambda methods to the DebugListener object to connect callbacks comming from the debug listener (server).
		
		// Accept Xdebug sessions.
		listener.acceptSessionLambda = session -> {
			
			try {
				SwingUtilities.invokeLater(() -> {
					
					// Update dialog status panel.
					updateStatusPanel();
					
					// Show dialog window.
					DebugViewer.this.setVisible(true);
				});
			}
			catch (Exception e) {
				onDebugProtocolError(e);
			}
		};
		
		// Accept incomming debug packets.
		listener.inputPacketLambda = (session, inputPacket) -> {
			
			try {
				// Xdebug protocol rules.
				if (listener instanceof XdebugListener && session instanceof XdebugListenerSession) {
					updateSessionView();
				}
			}
			catch (Exception e) {
				onDebugProtocolError(e);
			}
		};
		attachedListener = listener;
	}
	
	/**
	 * On debug protocol exception
	 * @param exception
	 */
	private void onDebugProtocolError(Exception exception) {
		
		Utility.show(this, "org.multipage.generator.messageXdebugProtocolException", exception.getLocalizedMessage());
	}
	
	/**
	 * On repeated watchdog ticks.
	 */
	protected void onWatchdogTick() {
		
		// Update GUI elements.
		updateSessionView();
		updateStatusPanel();
	}

	/**
	 * Update session view with list of current debug sessions.
	 */
	private void updateSessionView() {
		
		// Check attached debugger..
		if (attachedListener == null) {
			return;
		}
		
		// Display session.
		List<DebugListenerSession> sessions = attachedListener.getSessions();
		displaySessions(sessions);
	}
	
	/**
	 * Display list of sessions.
	 * @param sessions
	 */
	private void displaySessions(List<DebugListenerSession> sessions) {
		
		TableModel model = tableProcesses.getModel();
		if (!(model instanceof DefaultTableModel)) {
			return;
		}
		
		// Remember current row selection.
		int selectedRow = tableProcesses.getSelectedRow();
		
		// Load table items.
		DefaultTableModel modelProcesses = (DefaultTableModel) model;
		modelProcesses.setRowCount(0);
		
		try {
			for (DebugListenerSession session : sessions) {
				SocketAddress remoteAddress = (SocketAddress) session.getClientSocket();
				if (remoteAddress instanceof InetSocketAddress) {
					
					InetSocketAddress inetAddress = (InetSocketAddress) remoteAddress;
					
					long sessionId = session.getSessionId();
					String hostName = inetAddress.getHostName();
					int port = inetAddress.getPort();
					String pid = session.getPid();
					
					modelProcesses.addRow(new Object [] { sessionId, hostName, port, pid});
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Restote table row selection.
		if (selectedRow >= 0) {
			tableProcesses.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	/**
	 * Display threads
	 * @param sessionThread
	 * @param threadIds
	 */
	private synchronized void displayThreads(String sessionThread, LinkedList<String> threadIds) {
		
		// Get table model.
		TableModel model = tableThreads.getModel();
		if (!(model instanceof DefaultTableModel)) {
			return;
		}
		DefaultTableModel mutableModel = (DefaultTableModel) model;
		
		// Check columns.
		int columnCount = mutableModel.getColumnCount();
		if (columnCount < 1) {
			return;
		}
		
		try {
			// Ensure the table displays same threads as input list.
			HashSet<Integer> removeRows = new HashSet<Integer>();
			HashSet<String> newThreadIds = new HashSet<String>(threadIds);
			
			int rowCount = mutableModel.getRowCount();
			for (int row = 0; row < rowCount; row++) {
				
				// Get thread ID.
				Object value = mutableModel.getValueAt(row, 0);
				if (!(value instanceof String)) {
					continue;
				}
				String threadId = (String) value;
				
				// Check if the thread ID is in the input list.
				boolean success = threadIds.contains(threadId);
				if (success) {
					newThreadIds.remove(threadId);
					continue;
				}
				
				// Save the row to be removed later.
				removeRows.add(row);
			}
			
			// Remove old table rows.
			for (int row : removeRows) {
				mutableModel.removeRow(row);
			}
			
			// Add new thread IDs to the table.
			for (String newThreadId : newThreadIds) {
				mutableModel.addRow(new Object [] { newThreadId, "" });
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update status panel.
	 */
	private void updateStatusPanel() {
		/*
		if (serverSocketAddress != null && clientSocketAddress != null) {
			
			buttonConnected.setText(textConnected);
			buttonConnected.setBackground(Color.GREEN);
		}
		else {
			buttonConnected.setText(textNotConnected);
			buttonConnected.setBackground(Color.RED);
		}
		*/
	}
	
	/**
	 * On click "connected/not connected button".
	 */
	protected void onConnectedClick() {
		/*
		if (serverSocketAddress != null && clientSocketAddress != null) {
			
			Utility.show(this, "org.multipage.generator.messageDebuggerConnectionDetails", serverSocketAddress, clientSocketAddress);
		}
		*/
	}

	/**
	 * Called when the windows is opened
	 */
	protected void onOpen() {
		
	}
	
	/**
	 * Called when a user closes this window with click on window close button
	 */
	protected void onClose() {
		
		saveDialog();
		disposeWatchdog();
		dispose();
	}
	
	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		// Set window boundary
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog
	 */
	private void saveDialog() {
		
		// Save window boundary
		bounds = getBounds();
	}
	
	/**
	 * Localize components
	 */
	private void localize() {
		
		// Set window title
		setTitle(Resources.getString("org.multipage.generator.textApplicationDebug"));
		
		Utility.localize(labelFilter);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(labelProcesses);
		Utility.localize(labelThreads);
		Utility.localize(labelThreadStack);
		Utility.localize(menuDisplaySessionProperties);
	}

	/**
	 * Set icons
	 */
	private void setIcons() {
		
		// Set main icon
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
	}

	/**
	 * Opens file for debugging using debug command
	 * @param sourceUri
	 * @throws Exception 
	 */
	public int openFile(String sourceUri) {
		
		if (sourceUri.isEmpty()) {
			return -1;
		}
		
		try {
			URI uri = new URI(sourceUri);
			
			String scheme = uri.getScheme();
			if ("file".equals(scheme)) {
				
				File sourceFile = new File(uri);
				if (sourceFile.exists() && sourceFile.canRead()) {
					
					BufferedReader br = new BufferedReader(new FileReader(sourceFile));
					Obj<String> sourceCode = new Obj<String>("");
					
					Obj<Integer> lines = new Obj<Integer>(0);
					
					br.lines().forEach((String line) -> {
						sourceCode.ref += line + "\n";
						lines.ref++;
					});
					br.close();
					
					this.header = sourceUri;
					
					openSource(header, sourceCode.ref);
					scriptFileName = sourceUri;
					
					return lines.ref;
				}
			}
			else if ("debug".equals(scheme)) {
				
				// Set source link for future load operation
				this.header = sourceUri;

				// Transmit the "source" statement as a signal.
				ConditionalEvents.transmit(this, AreaServerSignal.debugStatement, "source", sourceUri);
			}
		}
		catch (Exception e) {
			
			// Display exception.
			Utility.show(this, "org.multipage.generator.messageDebuggerBadFileUri", sourceUri);
		}
		
		return -1;
	}
	
	/**
	 * Open source code
	 * @param header
	 * @param sourceCode
	 */
	public void openSource(String header, String sourceCode) {
		
		// Split the source code into lines
		String [] inputLines = sourceCode.split("\n");
		
		final Obj<Integer> lineNumber = new Obj<Integer>(0);
		final int lines = inputLines.length;

		codeLines = new LinkedList<String>();
		
		// Display source
		displaySourceCode(header, new DisplayCodeInterface() {
			
			@Override
			public String line() {
				
				if (lineNumber.ref >= lines) {
					return null;
				}
				String inputLine = inputLines[lineNumber.ref++];
				codeLines.addLast(inputLine);
				return inputLine;
			}
		});
		
		// Show the window
		setVisible(true);
	}
	
	/**
	 * Invoked whenever the debug session state changes.
	 * @param debugger 
	 * @param ready
	 */
	public void onSessionStateChanged(DebugListener debugger, boolean ready) {
		
		// TODO: <---DEBUGGER FINISH IT OR REMOVE IT
	}

	/**
	 * Displays source code. The callback is utilized for miscellaneous code sources
	 * @param header 
	 * @param callbacks
	 */
	private void displaySourceCode(String header, DisplayCodeInterface callbacks) {
		
		final String tabulator = "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String code = "<html>"
				+ "<head>"
				+ "<style>"
				+ "body {"
				+ "		white-space:nowrap;"
				+ "}"
				+ "#header {"
				+ "		font-family: Monospaced;"
				+ "		background-color: #DDDDDD;"
				+ "		color: #FFFFFF;"
				+ "}"
				+ ".lino {"
				+ "		font-family: Monospaced;"
				+ "		background-color: #DDDDDD;"
				+ "		color: #FFFFFF;"
				+ "}"
				+ ".code {"
				+ "		font-family: Monospaced;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>";
		
		// Display header
		if (header != null) {
			code += String.format("<div id='header'><center>%s</center></div>", header);
		}
		
		// Display lines
		String inputLine;
		int lineNumber = 1;
		
        for (;;) {
        	
        	Object returned = callbacks.line();
        	if (returned == null) {
        		break;
        	}
        	
        	inputLine = returned.toString();
        	inputLine = Utility.htmlSpecialChars(inputLine);
        	
			inputLine = inputLine.replaceAll("\\t", tabulator);
        	inputLine = inputLine.replaceAll("\\s", "&nbsp;");
        	String linoText = String.format("% 3d ", lineNumber);
        	linoText = linoText.replaceAll("\\s", "&nbsp;");
        	
    		code += String.format("<span class='lino'>%s</span>&nbsp;<span id='line%d' class='code'>%s</span><br>", linoText, lineNumber, inputLine);
    		lineNumber++;
        }

		code += "</body>"
				+ "</html>";
		
		// Display code (preserve scroll position)
		Point viewPosition = scrollCodePane.getViewport().getViewPosition();
		textCode.setText(code);
		SwingUtilities.invokeLater(() -> {
			scrollCodePane.getViewport().setViewPosition(viewPosition);
		});
	}
	
	/**
	 * Print message on console
	 * @param message
	 */
	private void consolePrint(String message) {
		
		String content = textInfo.getText() + message + "\r\n\r\n";
		textInfo.setText(content);
	}

	/**
	 * On send command to Xdebug server
	 */
	protected void onSendCommand() {
		
		String command = textCommand.getText();
		if (command.isEmpty()) {
			return;
		}
		
		// TODO: <---DEBUGGER FINISH
	}
	
	/**
	 * Shows reload alert depending on input parameter
	 * @param show
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void pageReloadException(boolean show) throws Exception {
		
		if (show) {
			Utility.show(this, "org.multipage.generator.messageReloadPageToStartDebugger");
			return;
		}
	}
	
	/**
	 * Starts debugging
	 */
	private void startDebugging() throws Exception {
		
		// Start debug viewer.
		boolean accepted = XdebugListenerOld.getSingleton().startDebugging();
		//pageReloadException(!accepted);
	}
	
	/**
	 * Set viewer state
	 * @param debugging
	 * @throws Exception 
	 */
	private void setState(EditorState debugging) {
		
		try {
			setEditorCss(debugging.cssRule);
		}
		catch (Exception e) {
		}
	}

	/**
	 * On run till the end of the script
	 */
	protected void onRun() {
		
		// Process run command
		try {
			startDebugging();
			// TODO: <---DEBUGGER FINISH
			//String resultText = responsePacket.getPacketText();
			//consolePrint(resultText);
		}
		catch (Exception e) {
		}
	}
	
	static int jlog = 1;
	private JPanel panelOutput;
	private JScrollPane scrollPane;
	private JTextArea textOutput;
	private JPanel panelExceptions;
	private JScrollPane scrollPaneExceptions;
	private JPanel panelSearch;
	private JLabel labelFilter;
	private TextFieldEx textFilter;
	private JTextPane textExceptions;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWords;
	private JCheckBox checkExactMatch;
	private JPanel panelDebuggers;
	private JButton buttonConnected;
	private JTable tableProcesses;
	private JLabel labelProcesses;
	private JLabel labelThreads;
	private JTable tableThreads;
	private JLabel labelThreadStack;
	private JTable tableStack;
	private JMenuItem menuDisplaySessionProperties;

	
	/**
	 * Do debugging step
	 */
	protected void step(String command) {
		
		// TODO: <---DEBUGGER MAKE Transmit "step" Xdebug signal.
		ConditionalEvents.transmit(this, AreaServerSignal.debugStatement, command);
		
//		// Process step into command
//		try {
//			startDebugging();
//			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand(command);
//			if (responsePacket.isEmpty()) {
//				return;
//			}
//			String resultText = responsePacket.getPacketText();
//			consolePrint(resultText);
//			
//			String filename = responsePacket.getString("/response/message/@filename");
//			if (!filename.isEmpty() && !filename.equals(scriptFileName)) {
//				openFile(filename);
//			}
//			
//			String lineNumber = responsePacket.getString("/response/message/@lineno");
//			if (!lineNumber.isEmpty()) {
//				resetStepHighlight();
//				
//				final Color color = new Color(255, 255, 255);
//				final Color bkColor = new Color(255, 0, 0);
//				
//				highlightCurrentStep(Integer.parseInt(lineNumber),  color, bkColor);
//				
//				listWatchModel.clear();
//			}
//			
//			// Show output buffer content, stack dump and breakpoint context
//			showOutput();
//			showStackDump();
//			processContext(3);
//			processContext(1);
//			
//		}
//		catch (Exception e) {
//			j.log("Xdebug: " + e.getLocalizedMessage());
//		}
	}
	
	/**
	 * Show output buffer
	 */
	private void showOutput() {
		
		// TODO: <---DEBUGGER FINISH
		/*
		try {
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommandT("eval", "ob_get_contents()");
			if (!responsePacket.isEmpty()) {
				String base64 = responsePacket.getString("/response/property/text()");
				if (base64 != null) {
					byte [] bytes = Utility.decodeBase64(base64);
					String valueText = new String(bytes);
					textOutput.setText(valueText);
				}
			}
		}
		catch (Exception e) {
			j.log(e.getMessage());
		}
		*/
	}

	/**
	 * Show stack dump
	 */
	private void showStackDump() {
		
		// TODO: <---DEBUGGER REFACTOR Process run command
		/*try {
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand("stack_get");
			if (!responsePacket.isEmpty()) {
				
				String resultText = responsePacket.getPacketText();
				consolePrint(resultText);
				
				// Clear output window
				listStackModel.clear();
				
				// Get stack items
				NodeList nodes = responsePacket.getNodes("/response/stack");
				if (nodes != null) {
					
					int count = nodes.getLength();
					for (int index = 0; index < count; index++) {
						
						Node node = nodes.item(index);
						if (node != null) {
							listStackModel.addElement(node);
						}
					}
				}
			}
		}
		catch (Exception e) {
			j.log(e.getMessage());
		}*/
	}
	
	/**
	 * Initialize stack dump window
	 */
	private void initStackDump() {
		
		listStackModel = new DefaultListModel<Node>();
		listStack.setModel(listStackModel);
		
		listStack.setCellRenderer(new ListCellRenderer<Node>() {
			
			// Set renderer.
			final RendererJLabel label = new RendererJLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends Node> list, Node node, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				label.set(isSelected, cellHasFocus, index);
				
				DOM dom = DOM.use(node);
				
				String level = dom.attribute("level");
				String where = dom.attribute("where");
				String lineno = dom.attribute("lineno");
				String type = dom.attribute("type");
				String filename = dom.attribute("filename");
				
				label.setText(String.format(
						  "<html>"
						+ "<div style='margin: 3;'>"
						+ "<font size='16px'>"
						+ "%s <b>%s</b> on line <font color='#FF0000'><b>%s</b></font> in <font color='#FF0000'><b>%s</b></font> <font color='#CCCCCC'>(type %s)</font>"
						+ "</font>"
						+ "</div>"
						+ "</html>", level, where, lineno, filename, type));
				return label;
			}
		});
	}
	
	/**
	 * Initialize watch window
	 */
	private void initWatch() {
		
		listWatchModel = new DefaultListModel<String>();
		listWatch.setModel(listWatchModel);
		
		listWatch.setCellRenderer(new ListCellRenderer<String>() {
			
			// Set renderer.
			final RendererJLabel label = new RendererJLabel();

			@Override
			public Component getListCellRendererComponent(JList<? extends String> list, String text, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				label.set(isSelected, cellHasFocus, index);
				
				label.setText(String.format(
						  "<html>"
						+ "<div style='margin: 3;'>"
						+ "<font size='16px'>"
						+ "%s"
						+ "</font>"
						+ "</div>"
						+ "</html>", text));
				return label;
			}
		});
	}

	/**
	 * Process context
	 */
	protected void processContext(int number) {
		
		// TODO: <---DEBUGGER REFACTOR
		// Process context_get command
		/*try {
			String command = String.format("context_get -c %s", number);
			XdebugPacket responsePacket = XdebugListener.getSingleton().postCommand(command);
			if (responsePacket.isEmpty()) {
				return;
			}
			
			NodeList properties = responsePacket.getNodes("/response/*");
			if (properties == null) {
				return;
			}
			
			for (int index = 0; index < properties.getLength(); index++) {
				
				Node property = properties.item(index);
				if (property == null) {
					return;
				}
				
				NamedNodeMap attributes = property.getAttributes();
				if (attributes == null) {
					return;
				}
				
				Node nameAttribute = attributes.getNamedItem("fullname");
				if (nameAttribute == null) {
					return;
				}
				
				String variableName = nameAttribute.getNodeValue();
				if (variableName == null) {
					return;
				}
				
				String type = responsePacket.getString(String.format("/response/property/@type", variableName));
				if (type != null) {
					listWatchModel.addElement(String.format("<b>%s</b> (%s)", variableName, type));
				}
				
				String value = "";
				if ("array".contentEquals(type)) {
					
					NodeList nodes = responsePacket.getNodes(String.format("/response/property[@fullname='%s']/*", variableName));
					if (nodes != null) {
						int length = nodes.getLength();
						
						for (int item = 0; item < length; item++) {
							
							Node node = nodes.item(item);
							if (node != null) {
								
								String valueText = node.getTextContent();
								valueText = new String(Utility.decodeBase64(valueText));
								
								Node nameNode = node.getAttributes().getNamedItem("name");
								if (nameNode != null) {
									String itemName = nameNode.getTextContent();
									
									listWatchModel.addElement(String.format("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>%s</b> => <font color='#FF0000'><b>%s</b></font>", itemName, valueText));
								}
							}
						}
					}
				}
				else if (!"uninitialized".contentEquals(type)) {
					
					value = responsePacket.getString(String.format("/response/property[@fullname='%s']/text()", variableName));
					listWatchModel.addElement(String.format("     = <font color='#FF0000'><b>%s</b></font>", value));
				}
			}
		}
		catch (Exception e) {
		}
		*/
	}

	/**
	 * On step into
	 */
	protected void onStepInto() {
		
		// Process step into command
		step("step_into");
	}

	/**
	 * On step out
	 */
	protected void onStepOut() {
		
		// Process step into command
		step("step_out");
	}

	/**
	 * On step over
	 */
	protected void onStepOver() {
		
		// Process step into command
		step("step_over");
	}
	
	/**
	 * Reset highlights
	 * @throws Exception 
	 */
	private void resetLastStepHighlight() {
		
		// TODO: test
		showOutput();
		resetStepHighlight();
		
	}
	
	/**
	 * Reset highlights
	 * @throws Exception 
	 */
	private void resetStepHighlight() {
		
		try {
			
			HTMLDocument document = (HTMLDocument) textCode.getDocument();
			StyleSheet documentCss = document.getStyleSheet();
			
			if (stepLineNumber >= 0) {
				String cssRule = String.format("#line%d {color: #000000; background-color: #FFFFFF;}", stepLineNumber);
				documentCss.addRule(cssRule);
				
				stepLineNumber = -1;
			}
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Highlight given code line
	 * @param lineNumber
	 * @param bkColor 
	 */
	private void highlightCurrentStep(int lineNumber, Color color, Color bkColor) throws Exception {
		
		resetStepHighlight();
		
		if (lineNumber > 0 && color != null && bkColor != null) {
			
			String elementId = String.format("line%s", lineNumber);
			
			String foregroundColor = Utility.getCssColor(color);
			String backgroundColor = Utility.getCssColor(bkColor);
			
			String cssRule = String.format("#%s {color: %s; background-color: %s;}", elementId, foregroundColor, backgroundColor);
			setEditorCss(cssRule);
		}
				
		stepLineNumber = lineNumber;
	}
	
	/**
	 * Sets CSS rule for code editor
	 * @param rule
	 */
	private void setEditorCss(String rule) throws Exception {
		
		HTMLDocument document = (HTMLDocument) textCode.getDocument();
		StyleSheet documentCss = document.getStyleSheet();
		documentCss.addRule(rule);
	}

	/**
	 * Updates source code view
	 */
	private void updateSourceCodeView() {
		
		if (codeLines == null || codeLines.isEmpty()) {
			return;
		}
		
		final Obj<Integer> lineIdex = new Obj<Integer>(0);
		final int count = codeLines.size();
		
		displaySourceCode(header, new DisplayCodeInterface() {
			@Override
			public String line() {
				
				if (lineIdex.ref >= count) {
					return null;
				}
				return codeLines.get(lineIdex.ref++);
			}
		});
	}

	/**
	 * On exit
	 */
	protected void onExit() {
		
		XdebugListenerOld client = XdebugListenerOld.getSingleton();
		if (client == null) {
			return;
		}
		
		// Process stop command
		client.stopSession();
	}

	/**
	 * A watch dog that scans Xdebug status
	 */
	private void establishWatchDog() {
		
		XdebugListenerOld client = XdebugListenerOld.getSingleton();
		if (client == null) {
			return;
		}

		client.setWatchDogCallback(new Callback() {
			@Override
			public Object run(Object status) {
				
				// Process Xdebug status
				SwingUtilities.invokeLater(() -> {
					
					processStatus(status);
					labelStatus.setText(status.toString());
				});
				return null;
			}
		});
	}
	
	/**
	 * Update log. Use control values.
	 */
	private void updateLog() {
		
		// Get filter string.
		String filterString = textFilter.getText();
		
		// Get filter flags.
		boolean caseSensitive = checkCaseSensitive.isSelected();
		boolean wholeWords = checkWholeWords.isSelected();
		boolean exactMatch = checkExactMatch.isSelected();
		
		// Display filtered log.
		LogMessage.setFulltextFilter(filterString, caseSensitive, wholeWords, exactMatch);
		LogMessage.displayHtmlLog(textExceptions);		
	}
	
	/**
	 * Sets listeners.
	 */
	private void setListeners() {
		
		// Receive debug signals.
		ConditionalEvents.receiver(this, AreaServerSignal.debugResponse, message -> {
			
			// Get response packet.
			XdebugPacketOld responsePacket = message.getRelatedInfo();
			if (responsePacket == null) {
				logException("org.multipage.generator.messageNullDebugResponsePacket");
				return;
			}
			
			// On error display exception message.
			String errorMessage = responsePacket.getString("/response/error/message/text()");
			if (errorMessage != null) {
				logException(responsePacket);
				return;
			}
		});
		
		// Full text filter.
		Utility.onChangeText(textFilter, filterString -> {
			
			updateLog();
		});
		
		// Selection listener.
        // Get the selection model
        ListSelectionModel selectionModel = tableProcesses.getSelectionModel();

        // Register the selection listener
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                	onProcessSelection();
                }
            }
        });
	}
	
	/**
	 * On change filter case sensitive.
	 */
	protected void onCaseSensitiveChange() {
		
		updateLog();
	}

	/**
	 * On change filter whole words.
	 */
	protected void onWholeWordsChange() {
		
		updateLog();
	}

	/**
	 * On change filter exact match.
	 */
	protected void onExactMatchChange() {
		
		updateLog();
	}
	
	/**
	 * On selection of debugged process.
	 */
	protected void onProcessSelection() {
		
		try {
			// Get selected process.
			int selectedRow = tableProcesses.getSelectedRow();
			
		    // Get the table model
		    TableModel tableModel = tableProcesses.getModel();
		    int rowCount = tableModel.getRowCount();
		    
		    // Check the row index.
			if (selectedRow < 0 || selectedRow >= rowCount) {
				return;
			}
			
			// Get selected session ID.
		    Object value = tableModel.getValueAt(selectedRow, 0);
		    if (!(value instanceof Long)) {
		    	return;
		    }
		    long selectedSessionId = (long) value;
		    
		    // Find session.
		    DebugListenerSession session = attachedListener.getSession(selectedSessionId);
		    if (session == null) {
		    	Utility.show(this, "org.multipage.generator.messageCannotFindSessionWithId", selectedSessionId);
		    	return;
		    }
		    
		    // On Xdebug session...
		    if (session instanceof XdebugListenerSession) {
		    	
		    	// Get Xdebug session and its parameters.
		    	XdebugListenerSession xdebugSession = (XdebugListenerSession) session;
		    	XdebugClientParameters parameters = xdebugSession.clientParameters;
		    	if (parameters == null) {
		    		return;
		    	}
		    	
		    	// Get reference to the Xdebug listener.
		    	XdebugListener listener = xdebugSession.listener;
		    	if (listener == null) {
		    		return;
		    	}
		    	
			    // Get debugged process ID for the selected session.
			    String pid = xdebugSession.clientParameters.pid;
			    if (pid == null) {
			    	return;
			    }
			    
			    // Finad all sessions with above PID and display corresponding debugged thread IDs.
			    List<DebugListenerSession> sessions = listener.getSessions();
			    LinkedList<String> threadIds = new LinkedList<String>();
			    
			    for (DebugListenerSession listedSession : sessions) {
			    	
			    	if (listedSession instanceof XdebugListenerSession) {
			    		XdebugListenerSession listedXdebugSession = (XdebugListenerSession) listedSession;
			    		
			    		XdebugClientParameters listedParameters = listedXdebugSession.clientParameters;
			    		if (listedParameters == null) {
			    			continue;
			    		}
			    		
			    		// If a matching PID of the session has been found...
			    		if (pid.equals(listedParameters.pid)) {
			    			
			    			// Get deugged thread ID and add it to the table view.
			    			String threadId = listedParameters.tid;
			    			if (threadId != null && !threadId.isEmpty()) {
			    				threadIds.add(threadId);
			    			}
			    		}
			    	}
			    }
			    
			    String sessionThread = parameters.tid;
			    
			    // TODO: <---MAKE Select session thread in the thread view.
			    displayThreads(sessionThread, threadIds);
		    }
		}
		catch (Exception e) {
			Utility.show(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Write exception message to log panel.
	 * @param message
	 */
	private void logException(String message) {
		
		// Create new log message.
		LogMessage.addLogMessage(message);
		
		// Get log content.
		LogMessage.displayHtmlLog(textExceptions);
		
		// Select panel with exceptions.
		int tabIndex = tabbedPane.indexOfComponent(panelExceptions);
		if (tabIndex >= 0) {
			tabbedPane.setSelectedIndex(tabIndex);
		}
	}
	
	/**
	 * Write error packet to log panel.
	 * @param errorPacket
	 */
	private void logException(XdebugPacketOld errorPacket) {
		
		// Get transaction ID.
		String transactionId = errorPacket.getString("/response/@transaction_id");
		
		// Get statement name.
		String debugStatement = errorPacket.getString("/response/@command");
		
		// Get error code.
		String errorCode = errorPacket.getString("/response/error/@code");
		
		// Application specific code.
		String applicationCode = errorPacket.getString("/response/error/@apperr");
		
		// Get response message.
		String errorMessage = errorPacket.getString("/response/error/message/text()");
		
		// Format final message.
		String finalMessage = String.format("%s\t%s\terr%s.%s:\n<b>%s</b>", transactionId, debugStatement, errorCode, applicationCode, errorMessage);
		
		// Write message to log panel.
		logException(finalMessage);
	}

	/**
	 * Processes Xdebug server status
	 * @param status 
	 */
	protected void processStatus(Object status) {
		
		// If the status doesn't change, do nothing
		if (this.status == status) {
			return;
		}
		
		// Status actions
		if ("stopping".equals(status) || "no connection".equals(status) ||
			"disconnected".equals(status) || "connection breakdown".equals(status)) {
			resetLastStepHighlight();
		}
		else {
			// Set editor state
			setState(EditorState.debugging);
		}
		
		this.status = status;
		
		setViewerState();
	}
	
	/**
	 * Set viewer state
	 */
	private void setViewerState() {
		
		if ("no connection".equals(status) ||
			"disconnected".equals(status) || 
			"connection breakdown".equals(status)) {
			setState(EditorState.initial);
		}
		else {
			setState(EditorState.debugging);
		}
	}
	
	/**
	 * Show user alert
	 * @param message
	 * @param timeout 
	 */
	public void showUserAlert(String message, int timeout) {
		
		SwingUtilities.invokeLater(() -> {
			AlertWithTimeout.showDialog(this, message, timeout);
		});
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
