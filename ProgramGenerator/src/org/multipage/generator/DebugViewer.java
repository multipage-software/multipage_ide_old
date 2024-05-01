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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.server.AreaServerSignal;
import org.maclan.server.DebugListener;
import org.maclan.server.DebugListenerSession;
import org.maclan.server.XdebugClientParameters;
import org.maclan.server.XdebugListenerOld;
import org.maclan.server.XdebugListenerSession;
import org.maclan.server.XdebugPacketOld;
import org.multipage.gui.AlertWithTimeout;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.Callback;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.DOM;
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
	 * Table font size.
	 */
	private static final int TABLE_FONT_SIZE = 9;
	
	/**
	 * Table header height.
	 */
	private static final int TABLE_HEADER_HEIGHT = 12;
	
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
				
				XdebugClientParameters parameters = xdebugSession.clientParameters;
				if (parameters != null) {
					long pid = parameters.pid;
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
				
				XdebugClientParameters parameters = xdebugSession.clientParameters;
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
		treeThreads.setRootVisible(false);
		scrollPaneThreads.setViewportView(treeThreads);
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
		
		establishWatchDog();
		
		initStackDump();
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
			
			// Constructor.
			{
				renderer.setPreferredSize(new Dimension(200, 24));
			}
			
			// Callback function.
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				if (value instanceof XdebugThreadNode) {
					renderer.setIcon(threadIcon);
				}
				else if (value instanceof XdebugProcessNode) {
					renderer.setIcon(processIcon);
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
	public void attachDebugger(DebugListener listener) {
		
		// Assign debug viewer.
		listener.setViewerComponent(this);
		
		// Add new lambda methods to the DebugListener object to connect callbacks comming from the debug listener (server).
		
		// Open Xdebug viewer.
		listener.openDebugViever = currentSession -> {
			
			DebugViewer.this.currentSession = currentSession;
			
			try {
				SwingUtilities.invokeLater(() -> {

					// Show dialog window and display threads.
					DebugViewer.this.setVisible(true);
					displayThreads(currentSession);
				});
			}
			catch (Exception e) {
				onDebugProtocolError(e);
			}
			
			// When ready for commands, load debugged source code.
			currentSession.onReady = () -> {
				updateSourceCode();
			};
		};
		
		debugListener = listener;
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
		try {
			updateSessionView();			
		}
		catch (Exception e) {
			e.printStackTrace();
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
		
		// Display session.
		List<DebugListenerSession> sessions = debugListener.getSessions();
		for (DebugListenerSession session : sessions) {
			loadSessionState(session);
		}
		
		// Display threads and source code..
		displayThreads(currentSession);
	}
	
	/**
	 * Load session state.
	 * @param session
	 * @throws Exception 
	 */
	private void loadSessionState(DebugListenerSession session)
			throws Exception {
		
		// Process Xdebug expr to get thread name.
		if (session instanceof XdebugListenerSession) {
			
			XdebugListenerSession xdebugSession = (XdebugListenerSession) session;
			XdebugClientParameters xdebugParameters = xdebugSession.clientParameters;
			
			if (xdebugParameters == null || !xdebugParameters.isInitialized()) {
				return;
			}
			
			Long threadId = xdebugParameters.tid;
			
			// Get current thread name.
			String expression = String.format("server.getThreadName(%d)", threadId);
			int transactionId = xdebugSession.createTransaction("expr", null, expression, response -> {
				
				// Try to get the transaction result and set thread name.
				try {
					String threadName = response.getExprResult();
					xdebugSession.clientParameters.threadName = threadName;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
			xdebugSession.beginTransaction(transactionId);
			
			// Get current area name.
			expression = "server.thisArea.name";
			transactionId = xdebugSession.createTransaction("expr", null, expression, response -> {
				
				// Try to get the transaction result and set thread name.
				try {
					String areaName = response.getExprResult();
					xdebugSession.clientParameters.areaName = areaName;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
			xdebugSession.beginTransaction(transactionId);
		}
	}

	/**
	 * Display session threads.
	 * @param currentSession 
	 */
	private synchronized void displayThreads(DebugListenerSession currentSession) {
		
		// Check debug listsner.
		if (debugListener == null) {
			return;
		}
		
		// Get all opened sessions.
		List<DebugListenerSession> sessions = debugListener.getSessions();
		for (DebugListenerSession session : sessions) {
			
			if (session instanceof XdebugListenerSession) {
				XdebugListenerSession xdebugSession = (XdebugListenerSession) session;
				putXdebugProcess(threadsRootNode, xdebugSession);
			}
		}
	}

	/**
	 * Put items into the threads tree.
	 * @param threadsRootNode
	 * @param session
	 */
	private void putXdebugProcess(DefaultMutableTreeNode threadsRootNode, XdebugListenerSession session) {
		
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
				putXdebugThread(processNode, session);
				return;
			}
		}
		
		// Add process and thread nodes.
		XdebugProcessNode processNode = new XdebugProcessNode(session);
		threadsRootNode.add(processNode);
		putXdebugThread(processNode, session);
		
		Utility.expandAll(treeThreads, true);
	}
	
	/**
	 * Put thread node.
	 * @param processNode
	 * @param session
	 */
	private void putXdebugThread(XdebugProcessNode processNode, XdebugListenerSession session) {
		
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
			return null;
		}
		
		Object lastComponent = selectedPath.getLastPathComponent();
		if (!(lastComponent instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastComponent;
		Object userObject = node.getUserObject();
		if (!(userObject instanceof XdebugListenerSession)) {
			return null;
		}
		
		XdebugListenerSession session = (XdebugListenerSession) userObject;
		return session;
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
		
		ApplicationEvents.removeReceivers(this);
		
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
		Utility.localize(labelThreads);
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
		
		updateSourceCode();
	}
	
	/**
	 * Update the source code fot current session.
	 */
	private void updateSourceCode() {
		
		try {
			// Get selected Xdebug session.
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			String debuggedAreaName = xdebugSession.getAreaName();
			
			// Load the Xdebug session source code from client.
			xdebugSession.source(sourceCode -> {

				displaySourceCode(debuggedAreaName, sourceCode);
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
		
	/**
	 * Display source code.
	 * @param header
	 * @param sourceCode
	 */
	public void displaySourceCode(String header, String sourceCode) {
		
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
		}
	}

	/**
	 * On run till the end of the script
	 */
	protected void onRun() {
		
		// Process run command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			int transactionId = xdebugSession.createTransaction("run", null, response -> {
				
				updateSourceCode();
			});
			xdebugSession.beginTransaction(transactionId);
		}
		catch (Exception e) {
		}
	}
	
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

	
	/**
	 * Do debugging step
	 */
	protected void step(String command) {
		
		// TODO: <---DEBUGGER MAKE Transmit "step" Xdebug signal.
		ApplicationEvents.transmit(this, AreaServerSignal.debugStatement, command);
		
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
}
