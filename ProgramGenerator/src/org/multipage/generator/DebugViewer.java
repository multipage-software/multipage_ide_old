/*
 * Copyright 2010-2024 (C) vakol
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.maclan.server.AreaServerSignal;
import org.maclan.server.DebugListener;
import org.maclan.server.DebugListenerSession;
import org.maclan.server.DebugWatchItem;
import org.maclan.server.DebugWatchItemType;
import org.maclan.server.XdebugAreaServerStackLevel;
import org.maclan.server.XdebugAreaServerTextState;
import org.maclan.server.XdebugClient;
import org.maclan.server.XdebugClientParameters;
import org.maclan.server.XdebugClientResponse;
import org.maclan.server.XdebugListenerSession;
import org.multipage.gui.AlertWithTimeout;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.w3c.dom.Node;

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
	 * Interlinear annotation characters.
	 */
	private static final char INTERLINEAR_ANNOTATION_ANCHOR = '\uFFF9';
	private static final char INTERLINEAR_ANNOTATION_TERMINATOR = '\uFFFB';

	/**
	 * Watch table column indices.
	 */
	private static final int WATCHED_NAME_COLUMN_INDEX = 0;
	private static final int WATCHED_FULLNAME_COLUMN_INDEX = 1;
	private static final int WATCHED_PROPERTY_TYPE_COLUMN_INDEX = 2;
	private static final int WATCHED_VALUE_COLUMN_INDEX = 3;
	private static final int WATCHED_VALUE_TYPE_COLUMN_INDEX = 4;
	
	/**
	 * Xdebug process node.
	 */
	@SuppressWarnings("serial")
	private static class XdebugProcessNode extends DefaultMutableTreeNode {
		
		/**
		 * Constructor.
		 */
		public XdebugProcessNode(XdebugListenerSession session) {
			
			setUserObject(session);
		}
		
		/**
		 * Get Xdebug session.
		 */
		public XdebugListenerSession getXdebugSession() {
			
			Object userObject = getUserObject();
			if (!(userObject instanceof XdebugListenerSession)) {
				return null;
			}
			
			XdebugListenerSession xdebugSession = (XdebugListenerSession) userObject;
			return xdebugSession;
		}
		
		/**
		 * Get process ID.
		 * @return
		 */
		public long getPid() {

			long pid = -1L;
			XdebugListenerSession xdebugSession = getXdebugSession();
			if (xdebugSession != null) {
				pid = xdebugSession.getPid();
			}
			return pid;
		}
		
		/**
		 * Get Xdebug process ID.
		 */
		@Override
		public String toString() {
			
			String pidText = "unknown";
			XdebugListenerSession xdebugSession = getXdebugSession();
			if (xdebugSession != null) {
				
				XdebugClientParameters parameters = xdebugSession.getClientParameters();
				if (parameters != null) {
					long pid = parameters.getProcessId();
					String pidTextFormat = Resources.getString("org.multipage.generator.textXdebugProcessId");
					pidText = String.format(pidTextFormat, pid);
				}
			}
			return pidText;
		}
	}
	
	/**
	 * Xdebug process node.
	 */
	@SuppressWarnings("serial")
	private static final class XdebugThreadNode extends XdebugProcessNode {
		
		/**
		 * Constructor.
		 * @param session
		 */
		public XdebugThreadNode(XdebugListenerSession session) {
			super(session);
		}
		
		/**
		 * Get thread ID.
		 * @return
		 */
		public long getTid() {

			long tid = -1L;
			XdebugListenerSession xdebugSession = getXdebugSession();
			if (xdebugSession != null) {
				tid = xdebugSession.getTid();
			}
			return tid;
		}
		
		/**
		 * Get thread name.
		 * @return
		 */
		private String getThreadName() {

			String treadName = "Unknown";
			XdebugListenerSession xdebugSession = getXdebugSession();
			if (xdebugSession != null) {
				treadName = xdebugSession.getThreadName();
			}
			return treadName;
		}
		
		/**
		 * Get Xdebug process ID.
		 */
		@Override
		public String toString() {
			
			String tidText = "unknown";
			XdebugListenerSession xdebugSession = getXdebugSession();
			if (xdebugSession != null) {
				
				XdebugClientParameters parameters = xdebugSession.getClientParameters();
				if (parameters != null) {
					
					long tid = getTid();
					String threadName = getThreadName();
					
					String pidTextFormat = Resources.getString("org.multipage.generator.textXdebugThreadName");
					tidText = String.format(pidTextFormat, tid, threadName);
				}
			}
			return tidText;
		}
	}
	
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
	 * Watch table model.
	 */
	private DefaultTableModel tableWatchModel = null;
	
	/**
	 * Stack list model
	 */
	private DefaultListModel<Node> listStackModel = null;
	
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
	 * Current debugged session.
	 */
	private XdebugListenerSession currentSession = null;

	/**
	 * Object status
	 */
	private Object status;
	
	/**
	 * GUI watchdog timer.
	 */
	private Timer watchdogTimer = null;
	
	/**
	 * Attached debug listener.
	 */
	private DebugListener debugListener = null;
	
	/**
	 * Threads tree model.
	 */
	private DefaultTreeModel threadsTreeModel = null;
	
	/**
	 * Threads tree root node.
	 */
	private DefaultMutableTreeNode threadsRootNode = null;
	
	/**
	 * Current raw source code received from the Xdebug client.
	 */
	private String rawSourceCode = null;

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
	private JPanel panelCommand;
	private JScrollPane scrollPaneOutput;
	private JScrollPane scrollPaneWatch;
	private JPanel panelRight;
	private JPanel panelLeft;
	private JToolBar toolBar;
	private JButton buttonRun;
	private JScrollPane scrollCodePane;
	private JPanel panelStatus;
	private JLabel labelStatus;
	private JButton buttonStop;
	private JButton buttonStepInto;
	private JButton buttonStepOut;
	private JButton buttonStepOver;
	private JTable tableWatch;
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
	private JLabel labelThreads;
	private JButton buttonTest;
	private JPanel panelThreads;
	private JTree treeThreads;
	private JPopupMenu menuWatch;
	private JMenuItem menuAddToWatch;
	private JMenuItem menuRemoveFromWatch;
	
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
		
		panelDebuggers = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textDebuggerProcesses", null, panelDebuggers, null);
		SpringLayout sl_panelDebuggers = new SpringLayout();
		panelDebuggers.setLayout(sl_panelDebuggers);
		
		labelThreads = new JLabel("org.multipage.generator.textDebuggedThreads");
		sl_panelDebuggers.putConstraint(SpringLayout.WEST, labelThreads, 3, SpringLayout.WEST, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.NORTH, labelThreads, 3, SpringLayout.NORTH, panelDebuggers);
		panelDebuggers.add(labelThreads);
		
		panelThreads = new JPanel();
		sl_panelDebuggers.putConstraint(SpringLayout.NORTH, panelThreads, 3, SpringLayout.SOUTH, labelThreads);
		sl_panelDebuggers.putConstraint(SpringLayout.WEST, panelThreads, 3, SpringLayout.WEST, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.SOUTH, panelThreads, -3, SpringLayout.SOUTH, panelDebuggers);
		sl_panelDebuggers.putConstraint(SpringLayout.EAST, panelThreads, -3, SpringLayout.EAST, panelDebuggers);
		panelDebuggers.add(panelThreads);
		panelThreads.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneThreads = new JScrollPane();
		panelThreads.add(scrollPaneThreads);
		
		treeThreads = new JTree();
		treeThreads.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				onSelectProcessNode();
			}
		});
		treeThreads.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int clickCount = e.getClickCount();
				if (clickCount == 2) {
					onDoubleClickProcessNode();
				}
			}
		});
		treeThreads.setRootVisible(false);
		scrollPaneThreads.setViewportView(treeThreads);
		
		panelWatch = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textDebuggerWatch", null, panelWatch, null);
		panelWatch.setLayout(new BorderLayout(0, 0));
		
		scrollPaneWatch = new JScrollPane();
		panelWatch.add(scrollPaneWatch, BorderLayout.CENTER);
		
		tableWatch = new JTable();
		tableWatch.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPaneWatch.setViewportView(tableWatch);
		
		menuWatch = new JPopupMenu();
		addPopup(scrollPaneWatch, menuWatch);
		addPopup(tableWatch, menuWatch);
		
		menuAddToWatch = new JMenuItem("org.multipage.generator.menuDebuggerAddToWatch");
		menuAddToWatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddToWatch();
			}
		});
		menuWatch.add(menuAddToWatch);
		
		menuRemoveFromWatch = new JMenuItem("org.multipage.generator.menuDebuggerRemoveFromWatch");
		menuRemoveFromWatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveWatch();
			}
		});
		menuWatch.add(menuRemoveFromWatch);
		
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
		textInfo.setFont(new Font("Consolas", Font.PLAIN, 15));
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
		
		buttonRun = new JButton("org.multipage.generator.textDebuggerRun");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRun();
			}
		});
		buttonRun.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonRun);
		
		buttonStepOver = new JButton("org.multipage.generator.textDebuggerStepOver");
		buttonStepOver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepOver();
			}
		});
		buttonStepOver.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOver);
		
		buttonStepOut = new JButton("org.multipage.generator.textDebuggerStepOut");
		buttonStepOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onStepOut();
			}
		});
		
		buttonStepInto = new JButton("org.multipage.generator.textDebuggerStepInto");
		buttonStepInto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStepInto();
			}
		});
		buttonStepInto.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepInto);
		buttonStepOut.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStepOut);
		
		buttonStop = new JButton("org.multipage.generator.textDebuggerStop");
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onStop();
			}
		});
		buttonStop.setPreferredSize(new Dimension(20, 20));
		toolBar.add(buttonStop);
		
		buttonTest = new JButton("TEST");
		buttonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onTest();
			}
		});
		toolBar.add(buttonTest);
				
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
		
		initWatch();
		
		loadDialog();
		
		// Start watch dog.
		startWatchDog();
	}
	
	/**
	 * Create views placed in this dialog that display debug information.
	 */
	private void createViews() {
		
		// Create thread view.
		createThreadView();
	}
	
	/**
	 * Create thread view.
	 */
	private void createThreadView() {
		
		// Create thread view model.
		threadsRootNode = new DefaultMutableTreeNode();
		threadsTreeModel = new DefaultTreeModel(threadsRootNode);
		treeThreads.setModel(threadsTreeModel);
		
		// Create thread view renderer.
		treeThreads.setCellRenderer(new TreeCellRenderer() {
			
			// Renderer.
			RendererJLabel renderer = new RendererJLabel();
			
			// Icons for tree nodes.
			ImageIcon processIcon = Images.getIcon("org/multipage/generator/images/process.png");
			ImageIcon threadIcon = Images.getIcon("org/multipage/generator/images/thread.png");
			ImageIcon stackLevelIcon = Images.getIcon("org/multipage/generator/images/area_node.png");
			
			// Constructor.
			{
				renderer.setPreferredSize(new Dimension(200, 24));
			}
			
			// Callback function for thread nodes renderer.
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				if (value instanceof XdebugThreadNode) {
					renderer.setIcon(threadIcon);
				}
				else if (value instanceof XdebugProcessNode) {
					renderer.setIcon(processIcon);
				}
				else if (value instanceof DefaultMutableTreeNode) {
					
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					Object userObject = node.getUserObject();
					if (userObject instanceof XdebugAreaServerStackLevel) {
						value = userObject;
					}
					
					renderer.setIcon(stackLevelIcon);
				}
				else if (value == null) {
					return null;
				}
				
				renderer.setText(value.toString());
				renderer.set(sel, hasFocus, row);
				return renderer;
			}
		});
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
	public void attachDebugListener(DebugListener listener) {
		
		// Assign debug viewer.
		listener.setViewerComponent(this);
		
		// Add new lambda methods to the DebugListener object to connect callbacks comming from the debug listener (server).
		
		// Open Xdebug viewer.
		listener.openDebugViever = newSession -> {
			
			// Remember current session.
			DebugViewer.this.currentSession = newSession;
			
			try {
				SwingUtilities.invokeLater(() -> {

					// Show dialog window and display threads.
					DebugViewer.this.setVisible(true);
					displaySessions();
				});
			}
			catch (Exception e) {
				onException("org.multipage.generator.messageXdebugProtocolException", e);
			}
			
			// When ready for commands, load debugged source code.
			newSession.setReadyForCommands(() -> {
				try {
					newSession.loadClientContexts(() -> {
						// After loading contexts.
						updateViews();
					});
					
				}
				catch (Exception e) {
					onException(e);
				}
			});
		};
		
		debugListener = listener;
	}

	/**
	 * On repeated watchdog ticks.
	 */
	protected void onWatchdogTick() {
		
		// Update GUI elements.
		try {
			updateSessionView();			
		}
		catch (Exception e) {
			onException(e);
		}
	}

	/**
	 * Update session view with list of current debug sessions.
	 * @throws Exception 
	 */
	private void updateSessionView() throws Exception {
		
		// Check attached debugger.
		if (debugListener == null) {
			return;
		}
		
		// Display sessions.
		displaySessions();
	}

	/**
	 * Display sessions.
	 */
	private void displaySessions() {
		
		// Check debug listener.
		if (debugListener == null) {
			return;
		}
		
		// Get all opened sessions.
		List<DebugListenerSession> sessions = debugListener.getSessions();
		for (DebugListenerSession session : sessions) {
			
			if (session instanceof XdebugListenerSession) {
				XdebugListenerSession xdebugSession = (XdebugListenerSession) session;
				putSession(threadsRootNode, xdebugSession);
			}
		}
	}

	/**
	 * Put items into the tree.
	 * @param threadsRootNode
	 * @param session
	 */
	private void putSession(DefaultMutableTreeNode threadsRootNode, XdebugListenerSession session) {
		
		// Get process ID.
		long processId = session.getPid();
		if (processId < 0) {
			return;
		};
		
		// Add new node only if it doesn't already exist.
		int processNodeCount = threadsRootNode.getChildCount();
		for (int index = 0; index < processNodeCount; index++) {
			
			// Get child node.
			TreeNode treeNode = threadsRootNode.getChildAt(index);
			if (!(treeNode instanceof XdebugProcessNode)) {
				continue;
			}
			
			XdebugProcessNode processNode = (XdebugProcessNode) treeNode;
			long pid = processNode.getPid();
			
			// Compare process IDs. If they are equal, do not add new process node and add new thread node instead.
			if (pid == processId) {
				putThread(processNode, session);
				return;
			}
		}
		
		// Add process and thread nodes.
		XdebugProcessNode processNode = new XdebugProcessNode(session);
		threadsRootNode.add(processNode);
		putThread(processNode, session);
		
		Utility.expandAll(treeThreads, true);
	}
	
	/**
	 * Put thread node.
	 * @param processNode
	 * @param session
	 */
	private void putThread(XdebugProcessNode processNode, XdebugListenerSession session) {
		
		// Get thread ID.
		long threadId = session.getTid();
		if (threadId < 0) {
			return;
		};		
		
		// Add new thread node. If it already exists, do not add it.
		int threadNodeCount = processNode.getChildCount();
		for (int index = 0; index < threadNodeCount; index++) {
			
			// Get child node.
			TreeNode treeNode = processNode.getChildAt(index);
			if (!(treeNode instanceof XdebugThreadNode)) {
				continue;
			}
			
			XdebugThreadNode threadNode = (XdebugThreadNode) treeNode;
			long tid = threadNode.getTid();	
			
			// Compare thread IDs. If they are equal, do not add new thread node.
			if (tid == threadId) {
				return;
			}
		}
		
		// Add new thread node.
		XdebugThreadNode threadNode = new XdebugThreadNode(session);
		processNode.add(threadNode);
	}
	
	/**
	 * Get selected Xdebug session.
	 * @return
	 */
	private XdebugListenerSession getSelectedXdebugSession() {
		
		TreePath selectedPath = treeThreads.getSelectionPath();
		if (selectedPath == null) {
			return currentSession;
		}
		
		Object lastComponent = selectedPath.getLastPathComponent();
		if (!(lastComponent instanceof DefaultMutableTreeNode)) {
			return currentSession;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastComponent;
		Object userObject = node.getUserObject();
		if (!(userObject instanceof XdebugListenerSession)) {
			return currentSession;
		}
		
		XdebugListenerSession session = (XdebugListenerSession) userObject;
		return session;
	}
	
	/**
	 * Get selected stack level or null value if it is not selected.
	 * @return
	 */
	private XdebugAreaServerStackLevel getSelectedStackLevel() {
		
		TreePath selectedPath = treeThreads.getSelectionPath();
		if (selectedPath == null) {
			return null;
		}
		
		Object lastComponent = selectedPath.getLastPathComponent();
		if (!(lastComponent instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastComponent;
		Object userObject = treeNode.getUserObject();
		if (!(userObject instanceof XdebugAreaServerStackLevel)) {
			return null;
		}
		
		XdebugAreaServerStackLevel stackLevel = (XdebugAreaServerStackLevel) userObject;
		return stackLevel;
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
	 * On add to watch list.
	 */
	protected void onAddToWatch() {
		
		DebugWatchItem watchItem = DebugWatchDialog.showDialog(this);
		if (watchItem == null) {
			return;
		}
		
		String watchedItemName = watchItem.getName();
		DebugWatchItemType watchedItemType = watchItem.getType();
		
		tableWatchModel.addRow(new Object [] { watchedItemName, null, watchedItemType, null, null });
		
		// Load watched values.
		loadWatchListValues();
	}
	
	/**
	 * On remove watch item.
	 */
	protected void onRemoveWatch() {
		
		// Get selected watch item.
		int selectedRow = tableWatch.getSelectedRow();
		if (selectedRow < 0) {
			return;
		}
		
		Object cellValueObject = tableWatch.getValueAt(selectedRow, WATCHED_NAME_COLUMN_INDEX);
		if (cellValueObject == null) {
			return;
		}
		
		// Ask user if selected watch item should be removed.
		String watchedItemName = cellValueObject.toString();

		boolean confirmed = Utility.ask(this, "org.multipage.generator.messageDebuggerRemoveWatchedItem", watchedItemName);
		if (!confirmed) {
			return;
		}
		
		// Remove selected watch item.
		tableWatchModel.removeRow(selectedRow);
		tableWatch.updateUI();
	}
	
	/**
	 * Called when a user closes this window with click on window close button
	 */
	protected void onClose() {
		
		ApplicationEvents.removeReceivers(this);
		
		saveDialog();
		closeSessions();
		disposeWatchdog();
		dispose();
	}
	
	/**
	 * Close debugger sessions.
	 */
	private void closeSessions() {
		
		Object rootObject = threadsTreeModel.getRoot();
		if (!(rootObject instanceof DefaultMutableTreeNode)) {
			return;
		}
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;
		Enumeration<? extends TreeNode> processNodes = rootNode.children();
		
		while (processNodes.hasMoreElements()) {
			
			TreeNode processNode = processNodes.nextElement();
			Enumeration<? extends TreeNode> threadNodes = processNode.children();
			
			while (threadNodes.hasMoreElements()) {
				
				TreeNode threadNode = threadNodes.nextElement();
				if (!(threadNode instanceof XdebugThreadNode)) {
					continue;
				}
				
				// Get Xdebug session and close it.
				XdebugThreadNode xdebugThreadNode = (XdebugThreadNode) threadNode;
				XdebugListenerSession session = xdebugThreadNode.getXdebugSession();
				if (session == null) {
					continue;
				}
				
				stopSession(session, () -> session.close());
			}
		}
		
		// Remove tree nodes.
		rootNode.removeAllChildren();
		threadsTreeModel.reload();
		treeThreads.updateUI();
	}

	/**
	 * On select process tree node.
	 */
	protected void onSelectProcessNode() {
		
		// Get selected stack level.
		XdebugAreaServerStackLevel stackLevel = getSelectedStackLevel();
		if (stackLevel == null) {
			return;
		}
		
		// Get source code from the stack level.
		String sourceCode = stackLevel.getSourceCode();
		
		// Load text state object from stak information.
		Obj<XdebugAreaServerTextState> textState = new Obj<>();
		stackLevel.loadAreaServerTextState(textState);
		
		// Display the selected stack level.
		SwingUtilities.invokeLater(() -> displaySourceCodeTextState(sourceCode, textState.ref));
	}

	/**
	 * On process double click.
	 */
	protected void onDoubleClickProcessNode() {
		
		// Display session dialog.
		try {
			XdebugSessionDialog.showDialog(this, currentSession);
		}
		catch (Exception e) {
			onException(e);
		}
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
		
		Utility.localize(tabbedPane);
		Utility.localize(labelFilter);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(labelThreads);
		Utility.localize(buttonRun);
		Utility.localize(buttonStepOver);
		Utility.localize(buttonStepInto);
		Utility.localize(buttonStepOut);
		Utility.localize(buttonStop);
		Utility.localize(menuAddToWatch);
		Utility.localize(menuRemoveFromWatch);
	}

	/**
	 * Set icons
	 */
	private void setIcons() {
		
		// Set main icon
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
		buttonRun.setIcon(Images.getIcon("org/multipage/generator/images/run.png"));
		buttonStepOver.setIcon(Images.getIcon("org/multipage/generator/images/step_over.png"));
		buttonStepInto.setIcon(Images.getIcon("org/multipage/generator/images/step_into.png"));
		buttonStepOut.setIcon(Images.getIcon("org/multipage/generator/images/step_out.png"));
		buttonStop.setIcon(Images.getIcon("org/multipage/generator/images/stop.png"));
		menuAddToWatch.setIcon(Images.getIcon("org/multipage/generator/images/watch_debug.png"));
		menuRemoveFromWatch.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
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
					
					displaySourceCode(header, sourceCode.ref);
					scriptFileName = sourceUri;
					
					return lines.ref;
				}
			}
			else if ("debug".equals(scheme)) {
				
				// Set source link for future load operation
				this.header = sourceUri;

				// Transmit the "source" statement as a signal.
				ApplicationEvents.transmit(this, AreaServerSignal.debugStatement, "source", sourceUri);
			}
		}
		catch (Exception e) {
			
			// Display exception.
			Utility.show(this, "org.multipage.generator.messageDebuggerBadFileUri", sourceUri);
		}
		
		return -1;
	}
	
	/**
	 * On test button clicked.
	 */
	protected void onTest() {
		
		// TODO: <---DEBUG On Test button clicked.
		getCurrentStack();
	}
	
	/**
	 * Update the source code fot current session and other debugger views.
	 */
	private void updateViews() {
		
		try {
			// Get selected Xdebug session.
			Obj<XdebugListenerSession> xdebugSession = new Obj<XdebugListenerSession>();
			xdebugSession.ref = getSelectedXdebugSession();
			if (xdebugSession.ref == null) {
				xdebugSession.ref = currentSession;
			}
			if (xdebugSession.ref == null) {
				return;
			}
			
			String debuggedAreaName = xdebugSession.ref.getAreaName();
			
			// Load the Xdebug session source code from client.
			xdebugSession.ref.source(sourceCode -> {
				
				// Remember current raw source code from Xdebug client.
				rawSourceCode = sourceCode;
				// Display source code in text panel.
				displaySourceCode(debuggedAreaName, sourceCode);
			});
			
			// Load Area Server state from Xdebug client. Update session.
			xdebugSession.ref.getAreaServerState(state -> {
				
				xdebugSession.ref.loadAreaServerState(state);
			});
			
			// Load Area Server text state from Xdebug client. Update source code highlights.
			xdebugSession.ref.getAreaServerTextState(textState -> {
				
				displaySourceCodeTextState(rawSourceCode, textState);
			});
			
			// Load Area Server stack.
			final int STACK_GET_WAIT_TIMEOUT_MS = 1000;
			Lock stackGetLock = new Lock();
			xdebugSession.ref.stackGet(stack -> {
				
				// Display stack information.
				displayStack(xdebugSession.ref, stack);
				Lock.notify(stackGetLock);
			});
			Lock.waitFor(stackGetLock, STACK_GET_WAIT_TIMEOUT_MS);
			
			// Load watched values.
			loadWatchListValues();
		}
		catch (Exception e) {
			onException(e);
		}		
	}

	/**
	 * Display source code.
	 * @param caption
	 * @param sourceCode
	 */
	public void displaySourceCode(String caption, String sourceCode) {
		
		// Split the source code into lines
		String [] inputLines = sourceCode.split("\n");
		
		final Obj<Integer> lineNumber = new Obj<Integer>(0);
		final int lines = inputLines.length;

		codeLines = new LinkedList<String>();
		
		// Display source
		displaySourceCode(caption, new DisplayCodeInterface() {
			
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
	}
	
	/**
	 * Display the source code form Xdebug client with highlighted Area Server text states.
	 * @param rawSourceCode
	 * @param textState
	 */
	private void displaySourceCodeTextState(String rawSourceCode, XdebugAreaServerTextState textState) {
		
		// Initializaction.
		String sourceCode = null;
		
		// Mark tag start position and current Area Server text poition in source code.
		int tagStartPosition = textState.getTagStartPosition();
		int position = textState.getPosition();
		int sourceLength = rawSourceCode.length();
		
		if (tagStartPosition >= 0 && position >= 0 && tagStartPosition <= position
			&& tagStartPosition <= sourceLength && position <= sourceLength) {
			
			// Put anchor and terminator into the source code.
			sourceCode = Utility.insertCharacter(rawSourceCode, tagStartPosition, INTERLINEAR_ANNOTATION_ANCHOR);
			sourceCode = Utility.insertCharacter(sourceCode, position + 1, INTERLINEAR_ANNOTATION_TERMINATOR);
		}
		
		// Do not use text highlights.
		if (sourceCode == null) {
			sourceCode = rawSourceCode;
		}
		
		// Display the source code.
		String caption = "";
		if (currentSession != null) {
			caption = currentSession.getAreaName();
		}
		displaySourceCode(caption, sourceCode);
	}
	
	/**
	 * Displays source code. The callback is utilized for miscellaneous code sources
	 * @param caption 
	 * @param callbacks
	 */
	private void displaySourceCode(String caption, DisplayCodeInterface callbacks) {
		
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
				+ ".currentReplacement {"
				+ "     background-color: #DD0000;"
				+ "		color: #FFFFFF;"
				+ "}"
				+ "</style>"
				+ "</head>"
				+ "<body>";
		
		// Display header
		if (caption != null) {
			code += String.format("<div id='header'><center>%s</center></div><div class='code'>", caption);
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
        	
        	inputLine = inputLine.replaceAll(INTERLINEAR_ANNOTATION_ANCHOR + "", "<span class='currentReplacement'>");
        	inputLine = inputLine.replaceAll(INTERLINEAR_ANNOTATION_TERMINATOR + "", "</span>");
        	
    		code += String.format("<span class='lino'>%s</span>&nbsp;%s<br>", linoText, inputLine);
    		lineNumber++;
        }

		code += "</div></body>"
				+ "</html>";
		
		// Display code (preserve scroll position)
		Point viewPosition = scrollCodePane.getViewport().getViewPosition();
		textCode.setText(code);
		SwingUtilities.invokeLater(() -> {
			scrollCodePane.getViewport().setViewPosition(viewPosition);
		});
	}
	
	/**
	 * Display Area Server stack information.
	 * @param session 
	 * @param stack
	 */
	private void displayStack(XdebugListenerSession session, LinkedList<XdebugAreaServerStackLevel> stack) {
		
		// Get displayed thread node, if it exists.
		XdebugThreadNode threadNode = findThreadNode(session);
		if (threadNode == null) {
			return;
		}
		
		// Clear thread node children.
		threadNode.removeAllChildren();
		
		// Add stack level information to the thread node.
		for (XdebugAreaServerStackLevel stackLevel : stack) {
			
			DefaultMutableTreeNode stackLevelNode = new DefaultMutableTreeNode(stackLevel);
			threadNode.add(stackLevelNode);
		}
		
		// Update the tree view to reflect the new stack information.
		treeThreads.updateUI();
		
		// Select the top level of the stack.
		selectStackTop();
	}

	/**
	 * Find thread tree node for input session.
	 * @param treeThreads
	 * @param session
	 * @return
	 */
	private XdebugThreadNode findThreadNode(XdebugListenerSession session) {
		
		// Check tree model.
		if (threadsTreeModel == null) {
			return null;
		}
		
		// Get process nodes which are child nodes of the root node.
		Object rootObject = threadsTreeModel.getRoot();
		if (!(rootObject instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;
		Enumeration<TreeNode> processNodes = rootNode.children();
		while (processNodes.hasMoreElements()) {
			
			// Get the process node.
			TreeNode treeNode = processNodes.nextElement();
			if (!(treeNode instanceof XdebugProcessNode)) {
				continue;
			}
			
			// Check process node.
			XdebugProcessNode processNode = (XdebugProcessNode) treeNode;
			XdebugListenerSession nodeSession = processNode.getXdebugSession();
			if (nodeSession.equals(session)) {
				
				// Get thread node.
				Enumeration<TreeNode> childNodes = processNode.children();
				
				boolean success = childNodes.hasMoreElements();
				if (!success) {
					continue;
				}
				
				TreeNode childNode = childNodes.nextElement();
				if (!(childNode instanceof XdebugThreadNode)) {
					continue;
				}
				
				XdebugThreadNode threadNode = (XdebugThreadNode) childNode;
				return threadNode;
			}				
		}
		return null;
	}
	
	/**
	 * Select the top level of the stack.
	 */
	private void selectStackTop() {
		
		// Check current session.
		if (currentSession == null) {
			return;
		}
		
		// Find current thread node.
		XdebugThreadNode threadNode = findThreadNode(currentSession);
		if (threadNode == null) {
			return;
		}
		
		// Get top level of stack.
		Enumeration<TreeNode> stackLevelNodes = threadNode.children();
		boolean success = stackLevelNodes.hasMoreElements();
		if (!success) {
			return;
		}
		
		// Select first child, the top of the stack.
		TreeNode stackTopNode = stackLevelNodes.nextElement();
		if (!(stackTopNode instanceof DefaultMutableTreeNode)) {
			return;
		}
		DefaultMutableTreeNode stackTopTreeNode = (DefaultMutableTreeNode) stackTopNode;
		TreeNode [] nodes = stackTopTreeNode.getPath();
		TreePath nodePath = new TreePath(nodes);
		
		TreeSelectionModel selectionModel = treeThreads.getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.addSelectionPath(nodePath);
	}
	
	/**
	 * Load watch list values from Xdebug client.
	 */
	private void loadWatchListValues() {
		
		// Get current stack.
		XdebugAreaServerStackLevel stackLevel = getCurrentStack();
		if (stackLevel == null) {
			return;
		}
		
		// Get watched items and get its current values.
		LinkedList<DebugWatchItem> watchedItems = getWatchedItems();
		for (DebugWatchItem watchedItem : watchedItems) {
			
			loadWatchedValue(stackLevel, watchedItem);
		}
	}
	
	/**
	 * Load watched value.
	 * @param stackLevel
	 * @param watchedItem
	 * @return
	 * @throws Exception 
	 */
	private void loadWatchedValue(XdebugAreaServerStackLevel stackLevel, DebugWatchItem watchedItem) {
		
		try {
			int stateHashCode = stackLevel.getStateHashCode();
			String stateHashText = String.valueOf(stateHashCode);
			String watchedName = watchedItem.getName();
			
			DebugWatchItemType watchedType = watchedItem.getType();
			String watchedTypeText = watchedType.getName();
			Integer debuggerContextId = currentSession.getContextId(XdebugClient.LOCAL_CONTEXT);
			String contextIdText = String.valueOf(debuggerContextId);
			
			int transactionId = currentSession.createTransaction("property_get", 
					new String [][] { { "-h", stateHashText }, { "-n", watchedName }, { "-t", watchedTypeText }, { "-c", contextIdText } },
					"", response -> {
				
				try {
					// Get watched item from Xdebug result and view it.
					DebugWatchItem watchedItemResult = response.getXdebugWathItemResult(watchedType);
					displayWatchedValue(watchedItemResult);
				}
				catch (Exception e) {
					onException(e);
				}
			});
		
			currentSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
	}

	/**
	 * Display watched item value.
	 * @param watchedItemResult
	 */
	private void displayWatchedValue(DebugWatchItem watchedItemResult) {
		
		// Try to find watched item by its name and property type.
		int rowCount = tableWatchModel.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			
			// Get table cell values.
			Object nameObject = tableWatchModel.getValueAt(row, WATCHED_NAME_COLUMN_INDEX);
			Object propertyTypeObject = tableWatchModel.getValueAt(row, WATCHED_PROPERTY_TYPE_COLUMN_INDEX);
			
			// Check the cell values.
			if (!(nameObject instanceof String) || !(propertyTypeObject instanceof DebugWatchItemType)) {
				continue;
			}
			
			String name = (String) nameObject;
			DebugWatchItemType propertyType = (DebugWatchItemType) propertyTypeObject;
			
			// If the watched item matches the input item, update its full name, value and value type.
			if (watchedItemResult.matches(name, propertyType)) {
				
				String fullNameText = watchedItemResult.getFullName();
				tableWatchModel.setValueAt(fullNameText, row, WATCHED_FULLNAME_COLUMN_INDEX);
				
				String valueText = watchedItemResult.getValue();
				tableWatchModel.setValueAt(valueText, row, WATCHED_VALUE_COLUMN_INDEX);
				
				String valueTypeText = watchedItemResult.getValueType();
				tableWatchModel.setValueAt(valueTypeText, row, WATCHED_VALUE_TYPE_COLUMN_INDEX);
			}
		}
		
		// Update wathed items table.
		tableWatch.updateUI();
	}

	/**
	 * Get list of watched items.
	 * @return
	 */
	private LinkedList<DebugWatchItem> getWatchedItems() {
		
		LinkedList<DebugWatchItem> watchedList = new LinkedList<DebugWatchItem>();
		
		int rowCount = tableWatchModel.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			
			// Get item name and type.
			Object cellValue = tableWatchModel.getValueAt(row, WATCHED_NAME_COLUMN_INDEX);
			if (!(cellValue instanceof String)) {
				continue;
			}
			String name = (String) cellValue;
			
			cellValue = tableWatchModel.getValueAt(row, WATCHED_PROPERTY_TYPE_COLUMN_INDEX);
			if (!(cellValue instanceof DebugWatchItemType)) {
				continue;
			}
			DebugWatchItemType type = (DebugWatchItemType) cellValue;
			
			DebugWatchItem watchedItem = new DebugWatchItem(name, type);
			watchedList.add(watchedItem);
		}
		
		return watchedList;
	}

	/**
	 * Get current stack.
	 */
	private XdebugAreaServerStackLevel getCurrentStack() {
		
		// Get selected tree node.
		Object currentNode = treeThreads.getLastSelectedPathComponent();
		if (currentNode == null) {
			return null;
		}
		
		// If the selected node is process node, get first child, it should be the thread node.
		if (currentNode instanceof XdebugProcessNode) {
			
			XdebugProcessNode processNode = (XdebugProcessNode) currentNode;
			int childrenCount = processNode.getChildCount();
			if (childrenCount <= 0) {
				return null;
			}
			
			currentNode = processNode.getChildAt(0);
		}
		
		// If current node is thread node, get first child, it should be the stack node.
		if (currentNode instanceof XdebugThreadNode) {
			
			XdebugThreadNode threadNode = (XdebugThreadNode) currentNode;
			int childrenCount = threadNode.getChildCount();
			
			if (childrenCount <= 0) {
				return null;
			}
			
			currentNode = threadNode.getChildAt(0);
		}
		
		// If current node is stack node, return the stack object.
		if (!(currentNode instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode currentMutableNode = (DefaultMutableTreeNode) currentNode;
		Object userObject = currentMutableNode.getUserObject();
		
		if (!(userObject instanceof XdebugAreaServerStackLevel)) {
			return null;
		}
		
		XdebugAreaServerStackLevel stackLevel = (XdebugAreaServerStackLevel) userObject;
		return stackLevel;
	}

	/**
	 * Print message on console
	 * @param message
	 */
	private void consolePrint(String message) {
		
		String content = textInfo.getText() + message + "\r\n";
		textInfo.setText(content);
	}

	/**
	 * On send command to Xdebug server
	 * @throws Exception 
	 */
	protected void onSendCommand() {
		
		// Get command text.
		String command = textCommand.getText();
		if (command.isEmpty()) {
			return;
		}
		
		try {
			// Send command to Xdebug client.
			int transactionId = currentSession.createTransaction("expr", null, command, response -> {
				
				try {
					// On error, display error message.
					boolean isError = response.isErrorPacket();
					if (isError) {
						String errorMessage = response.getErrorMessage();
						consolePrint(errorMessage);
						return;
					}
					
					// Print result in text view.
					String resultText = response.getExprResult();
					consolePrint(resultText);
				} 
				catch (Exception e) {
					onException(e);
				}
			});
			currentSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
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
	 * Set viewer state
	 * @param debugging
	 * @throws Exception 
	 */
	private void setState(EditorState debugging) {
		
		try {
			setEditorCss(debugging.cssRule);
		}
		catch (Exception e) {
			onException(e);
		}
	}

	/**
	 * On run command.
	 */
	protected void onRun() {
		
		// Process run command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			int transactionId = xdebugSession.createTransaction("run", null, response -> {
				
				updateViews();
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
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
	 * Initialize watch window
	 */
	private void initWatch() {
		
		final int [] columnWidths = { 100, 100, 100, 100, 100 };
		final int columnCount = columnWidths.length;
		
		tableWatchModel = new DefaultTableModel(0, columnCount);
		tableWatch.setModel(tableWatchModel);
		
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		
		// Name column.
		final int nameCellWidth = columnWidths[WATCHED_NAME_COLUMN_INDEX];
		TextFieldEx nameTextField = new TextFieldEx();
		DefaultCellEditor nameEditor = new DefaultCellEditor(nameTextField);
		DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer();
		TableColumn nameColumn = new TableColumn(WATCHED_NAME_COLUMN_INDEX, nameCellWidth, nameRenderer, nameEditor);
		String nameHeaderText = Resources.getString("org.multipage.generator.textDebugWatchItemName");
		nameColumn.setHeaderValue(nameHeaderText);
		columnModel.addColumn(nameColumn);
		
		// Full name column.
		final int fullNameCellWidth = columnWidths[WATCHED_FULLNAME_COLUMN_INDEX];
		TextFieldEx fullNameTextField = new TextFieldEx();
		DefaultCellEditor fullNameEditor = new DefaultCellEditor(fullNameTextField);
		DefaultTableCellRenderer fullNameRenderer = new DefaultTableCellRenderer();
		TableColumn fullNameColumn = new TableColumn(WATCHED_FULLNAME_COLUMN_INDEX, fullNameCellWidth, fullNameRenderer, fullNameEditor);
		String fullNameHeaderText = Resources.getString("org.multipage.generator.textDebugWatchItemFullName");
		fullNameColumn.setHeaderValue(fullNameHeaderText);
		columnModel.addColumn(fullNameColumn);
		
		// Property type column.
		final int typeCellWidth = columnWidths[WATCHED_PROPERTY_TYPE_COLUMN_INDEX];
		TextFieldEx typeTextField = new TextFieldEx();
		typeTextField.setEditable(false);
		DefaultCellEditor typeEditor = new DefaultCellEditor(typeTextField);
		DefaultTableCellRenderer typeRenderer = new DefaultTableCellRenderer();
		TableColumn typeColumn = new TableColumn(WATCHED_PROPERTY_TYPE_COLUMN_INDEX, typeCellWidth, typeRenderer, typeEditor);
		String typeHeaderText = Resources.getString("org.multipage.generator.textDebugWatchItemType");
		typeColumn.setHeaderValue(typeHeaderText);
		columnModel.addColumn(typeColumn);
		
		// Value column.
		final int valueCellWidth = columnWidths[WATCHED_VALUE_COLUMN_INDEX];
		TextFieldEx valueTextField = new TextFieldEx();
		DefaultCellEditor valueEditor = new DefaultCellEditor(valueTextField);
		DefaultTableCellRenderer valueRenderer = new DefaultTableCellRenderer();
		TableColumn valueColumn = new TableColumn(WATCHED_VALUE_COLUMN_INDEX, valueCellWidth, valueRenderer, valueEditor);
		String valueHeaderText = Resources.getString("org.multipage.generator.textDebugWatchItemValue");
		valueColumn.setHeaderValue(valueHeaderText);
		columnModel.addColumn(valueColumn);
		
		// Value type column.
		final int valueTypeCellWidth = columnWidths[WATCHED_VALUE_TYPE_COLUMN_INDEX];
		TextFieldEx valueTypeTextField = new TextFieldEx();
		DefaultCellEditor valueTypeEditor = new DefaultCellEditor(valueTypeTextField);
		DefaultTableCellRenderer valueTypeRenderer = new DefaultTableCellRenderer();
		TableColumn valueTypeColumn = new TableColumn(WATCHED_VALUE_TYPE_COLUMN_INDEX, valueTypeCellWidth, valueTypeRenderer, valueTypeEditor);
		String valueTypeHeaderText = Resources.getString("org.multipage.generator.textDebugWatchItemValueType");
		valueTypeColumn.setHeaderValue(valueTypeHeaderText);
		columnModel.addColumn(valueTypeColumn);		
		
		tableWatch.setColumnModel(columnModel);
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
		
		// Process step command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			int transactionId = xdebugSession.createTransaction("step_into", null, response -> {
				
				updateViews();
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
	}

	/**
	 * On step over
	 */
	protected void onStepOver() {
		
		// Process step command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			int transactionId = xdebugSession.createTransaction("step_over", null, response -> {
				
				updateViews();
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
	}	

	/**
	 * On step out
	 */
	protected void onStepOut() {
		
		// Process step command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			int transactionId = xdebugSession.createTransaction("step_out", null, response -> {
				
				updateViews();
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
	}
	
	/**
	 * On stop.
	 */
	protected void onStop() {
		
		// Process stop command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			stopSession(xdebugSession, null);
		}
		catch (Exception e) {
			onException(e);
		}
	}
	
	/**
	 * Stop session.
	 * @param xdebugSession
	 * @param completedLambda
	 */
	private void stopSession(XdebugListenerSession xdebugSession, Runnable completedLambda) {
		
		// Process stop command
		try {
			int transactionId = xdebugSession.createTransaction("stop", null, response -> {
				
				updateViews();
				
				if (completedLambda != null) {
					completedLambda.run();
				}
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
			onException(e);
		}
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
		
		// Full text filter.
		Utility.onChangeText(textFilter, filterString -> {
			
			updateLog();
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
	private void logException(XdebugClientResponse errorPacket) {
		
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
	
	/**
	 * Fired on exception.
	 * @param e
	 */
	protected void onThrownException(Throwable e) throws Exception {
		
		// Override this method.
		onException(e);
		throw new Exception(e);
	}
	
	/**
	 * Fired on exception.
	 * @param messageFormatId
	 * @param exception
	 */
	private void onException(String messageFormatId, Exception exception) {
		
		String messageFormat = Resources.getString(messageFormatId);
		String errorMessage = exception.getLocalizedMessage();
		String messageText = String.format(messageFormat, errorMessage);
		
		Exception e = new Exception(messageText);
		onException(e);
	}
	
	/**
	 * Fired on exception.
	 * @param e
	 */
	private void onException(Throwable e) {
		
		// Override this method.
		e.printStackTrace();
	}
	
	/**
	 * Adds popup menu.
	 * @param component
	 * @param popup
	 */
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
