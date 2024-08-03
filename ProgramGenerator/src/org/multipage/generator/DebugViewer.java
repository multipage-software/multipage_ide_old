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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
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

import org.maclan.server.CurrentXdebugClientData;
import org.maclan.server.DebugWatchItem;
import org.maclan.server.DebugWatchItemType;
import org.maclan.server.XdebugClient;
import org.maclan.server.XdebugListener;
import org.maclan.server.XdebugListenerSession;
import org.maclan.server.XdebugProcess;
import org.maclan.server.XdebugStackLevel;
import org.maclan.server.XdebugThread;
import org.multipage.gui.AlertWithTimeout;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * This is GUI for debugging.
 * @author vakol
 *
 */
public class DebugViewer extends JFrame {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	// $hide>>$
	
	/**
	 * GUI watchdog timeout in ms.
	 */
	private static final int WATCHDOG_TIMEOUT_MS = 1000;
	
	/**
	 * Transaction timeout in milliseconds.
	 */
	private static final int TRANSACTION_TIMEOUT_MS = 3000;
	
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
	 * Window boundary
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Main splitter position.
	 */
	private static int mainSplitterPosition = 0;

    /**
	 * Debug viewer singleton object.
	 */
    private static DebugViewer debugViewerInstance = null;
    
	/**
	 * Lines of code to display or null if there is nothing to display
	 */
	private LinkedList<String> codeLines = null;
	
	/**
	 * A line number of debugger step
	 */
	private int stepLineNumber = -1;
	
	/**
	 * Sessions tree model and root node.
	 */
	private DefaultTreeModel treeSessionsModel = null;
	private DefaultMutableTreeNode sessionsRootNode = null;
	
	/**
	 * Watch table model.
	 */
	private DefaultTableModel tableWatchModel = null;
	
	/**
	 * Current debugged session.
	 */
	private XdebugListenerSession currentSession = null;

	/**
	 * Object status
	 */
	private Object status = null;
	
	/**
	 * GUI watchdog timer.
	 */
	private Timer watchdogTimer = null;
	
	/**
	 * Attached debug listener.
	 */
	private XdebugListener debugListener = null;
	
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
	private JTree treeSessions;
	private JPopupMenu menuWatch;
	private JMenuItem menuAddToWatch;
	private JMenuItem menuRemoveFromWatch;
	private JSplitPane splitPane;
    
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		mainSplitterPosition = 500;
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
		mainSplitterPosition = inputStream.readInt();
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(StateOutputStream outputStream)
		throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(mainSplitterPosition);
	}
    
    /**
     * Get debug viewer singleton.
     * @param parent 
     * @return
     */
    public static DebugViewer getInstance(Component parent) {
        if (debugViewerInstance == null) {
            debugViewerInstance = new DebugViewer(parent);
        }
        return debugViewerInstance;
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
		
		splitPane = new JSplitPane();
		splitPane.setMinimumSize(new Dimension(0, 0));
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1.0);
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
		
		treeSessions = new JTree();
		treeSessions.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				onSelectProcessNode();
			}
		});
		treeSessions.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int clickCount = e.getClickCount();
				if (clickCount == 2) {
					onDoubleClickProcessNode();
				}
			}
		});
		treeSessions.setRootVisible(false);
		scrollPaneThreads.setViewportView(treeSessions);
		
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
		tabbedPane.addTab("org.multipage.generator.textDebuggerOutput", null, panelOutput, null);
		panelOutput.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panelOutput.add(scrollPane);
		
		textOutput = new JTextArea();
		textOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textOutput.setEditable(false);
		scrollPane.setViewportView(textOutput);
		
		panelCommand = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textDebuggerCommands", null, panelCommand, null);
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
		tabbedPane.addTab("org.multipage.generator.textDebuggerExceptions", null, panelExceptions, null);
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
		splitPane.setDividerLocation(1.0);
		
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
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		createViews();
		setListeners();
		
		loadDialog();
		
		// Start watch dog.
		startWatchDog();
	}
	
	/**
	 * Create views that display debug information.
	 */
	private void createViews() {
		
		// Create session view.
		createSessionsView();
		
		// Create watch view.
		createWatchView();
	}
	
	/**
	 * Create sessions view.
	 */
	private void createSessionsView() {
		
		// Create tree model.
		sessionsRootNode = new DefaultMutableTreeNode();
		treeSessionsModel = new DefaultTreeModel(sessionsRootNode);
		treeSessions.setModel(treeSessionsModel);
		
		// Create session view renderer.
		treeSessions.setCellRenderer(new TreeCellRenderer() {
			
			// Renderer.
			RendererJLabel renderer = new RendererJLabel();
			
			// Icons for tree nodes.
			ImageIcon sessionIcon = Images.getIcon("org/multipage/generator/images/session_icon.png");
			ImageIcon processIcon = Images.getIcon("org/multipage/generator/images/process.png");
			ImageIcon threadIcon = Images.getIcon("org/multipage/generator/images/thread.png");
			ImageIcon stackLevelIcon = Images.getIcon("org/multipage/generator/images/area_node.png");
			
			// Constructor.
			{
				renderer.setPreferredSize(new Dimension(200, 24));
			}
			
			// Callback function for the nodes renderer.
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				// Check node type.
				if (value instanceof DefaultMutableTreeNode) {
					
					// Get node user object.
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					Object userObject = node.getUserObject();
					
					// Set node renderer properties and return the renderer.
					if (userObject instanceof XdebugListenerSession) {
						renderer.setIcon(sessionIcon);
					}
					else if (userObject instanceof XdebugProcess) {
						renderer.setIcon(processIcon);
					}
					else if (userObject instanceof XdebugThread) {
						renderer.setIcon(threadIcon);
					}
					else if (userObject instanceof XdebugStackLevel) {
						renderer.setIcon(stackLevelIcon);
					}
				
					renderer.setText(value.toString());
					renderer.set(sel, hasFocus, row);
					return renderer;
				}
				else {
					return null;
				}
			}
		});
	}
	
	/**
	 * Starts watch dog that polls miscelaneous debug states.
	 */
	private void startWatchDog() {
		
		// TODO: <---UNCOMMENT IT
		/*watchdogTimer = new Timer(WATCHDOG_TIMEOUT_MS, new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        onWatchdogTick();
		    }
		});
		watchdogTimer.setRepeats(true);
		watchdogTimer.setCoalesce(true);
		watchdogTimer.setInitialDelay(WATCHDOG_TIMEOUT_MS);
		watchdogTimer.setDelay(WATCHDOG_TIMEOUT_MS);
		watchdogTimer.start();*/
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
	public void attachDebugListener(XdebugListener listener) {
		
		// Assign debug viewer.
		listener.setViewerComponent(this);
		
		// Open Xdebug viewer.
		listener.openDebugViever = newSession -> {
			
			// Remember current session.
			DebugViewer.this.currentSession = newSession;
			
			try {
				SwingUtilities.invokeLater(() -> {

					// Show dialog window.
					DebugViewer.this.setVisible(true);
				});
			}
			catch (Exception e) {
				onException("org.multipage.generator.messageXdebugProtocolException", e);
			}
			
			// When ready for commands ...
			newSession.setReadyForCommands(() -> {
				try {
					newSession.loadClientContexts(() -> {
						
						// Set "server_ready" property to true.
						try {
							int transationId = newSession.createTransaction("property_set", new String [][] {{"-n", "server_ready"},  {"-l", "1"}}, "1", response -> {
								     
								boolean success = response.isPropertySetSuccess();
								if (!success) {
									onException("org.multipage.generator.messageDebugServerReadyError");
								}
							});
							newSession.beginTransactionWait(transationId, TRANSACTION_TIMEOUT_MS);
						}
						catch (Exception e) {
							onException(e);
						}
					});
				}
				catch (Exception e) {
					onException(e);
				}
			});
			
			// Process notifications.
			newSession.setReceivingNotifications(notification -> {

				try {
					// On breakpoint resolved notification...
					boolean breakpointResolved = notification.isBreakpointResolved();
					if (breakpointResolved) {
						
						// Update debugger views.
						SwingUtilities.invokeLater(() -> updateViews());
					}
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
	}

	/**
	 * Get selected Xdebug session.
	 * @return
	 */
	private XdebugListenerSession getSelectedXdebugSession() {
		
		if (debugListener == null) {
			return null;
		}
		
		// Check live sessions. Removes closed sessions.
		debugListener.ensureLiveSessions();
		
		TreePath selectedPath = treeSessions.getSelectionPath();
		if (selectedPath == null) {
			return currentSession;
		}
		
		Object firstComponent = selectedPath.getPathComponent(1);
		if (!(firstComponent instanceof DefaultMutableTreeNode)) {
			return currentSession;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) firstComponent;
		Object userObject = node.getUserObject();
		if (!(userObject instanceof XdebugListenerSession)) {
			return currentSession;
		}
		
		XdebugListenerSession session = (XdebugListenerSession) userObject;
		return session;
	}
	
	/**
	 * Get selected stack level or null value if it is not ed.
	 * @return
	 */
	private XdebugStackLevel getSelectedStackLevel() {
		
		TreePath selectedPath = treeSessions.getSelectionPath();
		if (selectedPath == null) {
			return null;
		}
		
		Object lastComponent = selectedPath.getLastPathComponent();
		if (!(lastComponent instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastComponent;
		Object userObject = treeNode.getUserObject();
		if (!(userObject instanceof XdebugStackLevel)) {
			return null;
		}
		
		XdebugStackLevel stackLevel = (XdebugStackLevel) userObject;
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
		
		// TODO: <---MAKE
		//stopSession(session, () -> session.close());

	}
	
	/**
	 * On select process tree node.
	 */
	protected void onSelectProcessNode() {
		
		// Get selected session.
		XdebugListenerSession session = getSelectedXdebugSession();
		if (session == null) {
			return;
		}
		
		currentSession = session;
		
		// Get selected stack level.
		XdebugStackLevel stackLevel = getSelectedStackLevel();
		if (stackLevel == null) {
			return;
		}
		
		// Get source code from the stack level.
		String sourceCode = stackLevel.getSourceCode();
		
		// Display debugged source code..
		SwingUtilities.invokeLater(() -> displayDebuggedSourceCode(sourceCode, stackLevel));
	}
	
	/**
	 * On process double click.
	 */
	protected void onDoubleClickProcessNode() {
		
		// Display session dialog.
		try {
			XdebugListenerSession session = getSelectedXdebugSession();
			XdebugSessionDialog.showDialog(this, session);
		}
		catch (Exception e) {
			onException(e);
		}
	}
	
	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		// Set window boundary and main splitter position.
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		splitPane.setDividerLocation(mainSplitterPosition);
	}
	
	/**
	 * Save dialog
	 */
	private void saveDialog() {
		
		// Save window boundary and main splitter position.
		bounds = getBounds();
		mainSplitterPosition = splitPane.getDividerLocation();
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
	 * On test button clicked.
	 */
	protected void onTest() {
		
		// TODO: <---DEBUG On Test button clicked.
		getCurrentStack();
	}
	
	/**
	 * Update the source code for current session and other debugger views.
	 */
	private void updateViews() {
		
		try {
			
			// Checks live sessions and removes closed sessions.
			debugListener.ensureLiveSessions();
			
			// Get selected Xdebug session.
			Obj<XdebugListenerSession> xdebugSession = new Obj<XdebugListenerSession>();
			xdebugSession.ref = getSelectedXdebugSession();
			if (xdebugSession.ref == null) {
				return;
			}
			
			List<XdebugListenerSession> sesisons = debugListener.getSessions();
			for (XdebugListenerSession session : sesisons) {
				
				session.stackGet(stack -> (processId, processName) -> (threadId, threadName) -> {
					
					// Save session stack.
					long sessionProcessId = session.getProcessId();
					if (sessionProcessId == -1L) {
						
						session.setProcessId(processId);
						sessionProcessId = processId;
					}
					
					// Put stack into session.
					if (processId == sessionProcessId) {
						session.putStack(processId, processName, threadId, threadName, stack);
						
						// Display stack information.
						displayStack(session);
					}
				});
			}

			
			String debuggedAreaName = xdebugSession.ref.getAreaName();
			
			// Load the Xdebug session source code from client.
			xdebugSession.ref.source(sourceCode -> {
				
				// Remember current raw source code from Xdebug client.
				rawSourceCode = sourceCode;
				// Display source code in text panel.
				displaySourceCode(debuggedAreaName, sourceCode);
			});
			
			// Load watched values.
			loadWatchListValues();
			
			// Get current stack.
			XdebugStackLevel stackLevel = getCurrentStack();
			if (stackLevel != null) {
				
				// Get current context.
				int contextId = XdebugClient.getContextId(XdebugClient.LOCAL_CONTEXT);
				
				// Load context properties.
				xdebugSession.ref.contextGet(contextId, stackLevel, watchItems -> {
					
					// Set watched items.
					SwingUtilities.invokeLater(() -> {
						
						DebugWatchDialog.setWatchItems(watchItems);
					});
				});
			}
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
	 * Display the source code form Xdebug client with highlighted parts.
	 * @param rawSourceCode
	 * @param stackLevel 
	 * @param debugInfo
	 */
	private void displayDebuggedSourceCode(String rawSourceCode, XdebugStackLevel stackLevel) {
		
		// Initializaction.
		String sourceCode = null;
		
		// Mark tag beginning and end positions in source code.
		int cmdBegin = stackLevel.getCmdBegin();
		int cmdEnd = stackLevel.getCmdEnd();
		int sourceLength = rawSourceCode.length();
		
		if (cmdBegin >= 0 && cmdEnd >= 0 && cmdBegin <= cmdEnd
			&& cmdBegin <= sourceLength && cmdEnd <= sourceLength) {
			
			// Insert "anchor" and "terminator" special characters into the source code.
			sourceCode = Utility.insertCharacter(rawSourceCode, cmdBegin, INTERLINEAR_ANNOTATION_ANCHOR);
			sourceCode = Utility.insertCharacter(sourceCode, cmdEnd + 1, INTERLINEAR_ANNOTATION_TERMINATOR);
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
		
		SwingUtilities.invokeLater(() -> {
			
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
		
			scrollCodePane.getViewport().setViewPosition(viewPosition);
		});
	}
	
	/**
	 * Display Area Server stack information.
	 * @param session 
	 */
	private void displayStack(XdebugListenerSession session) {
		
		SwingUtilities.invokeLater(() -> {
			
			// Check root node of the tree view.
			if (sessionsRootNode == null) {
				sessionsRootNode = new DefaultMutableTreeNode("root");
				treeSessionsModel.setRoot(sessionsRootNode);
			}
			
			// Find session node.
			int sessionId = session.getSessionId();
			DefaultMutableTreeNode sessionNode = getSessionNode(sessionId);
			
			// If the session node doesn't exist, create new one.
			if (sessionNode == null) {
				sessionNode = new DefaultMutableTreeNode(session);
				sessionsRootNode.add(sessionNode);
			}
			
			// Update thread nodes.
			sessionNode.removeAllChildren();
			
			HashMap<Long, XdebugThread> threads = session.getThreads();
			
			// Sort thread IDs.
			Set<Long> threadIds = threads.keySet();
			ArrayList<Long> sortedThreadIds = new ArrayList<>(threadIds);
			Collections.sort(sortedThreadIds);
			
			for (Long threadId : sortedThreadIds) {
				
				XdebugThread thread = threads.get(threadId);
				DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode(thread);
				sessionNode.add(threadNode);
				
				// Add stack levels to the thread node.
				LinkedList<XdebugStackLevel> stack = thread.getStack();
				for (XdebugStackLevel stackLevel : stack) {
					
					DefaultMutableTreeNode stackLevelNode = new DefaultMutableTreeNode(stackLevel);
					threadNode.add(stackLevelNode);
				}
			}
			
			// Update the tree view to reflect the new stack information.
			treeSessions.updateUI();
			
			Utility.expandAll(treeSessions, true);
			
			// Select the top level of the stack.
			selectStackTop();
		});
	}
	
	/**
	 * Get session node.
	 * @param sessionId
	 * @return
	 */
	private DefaultMutableTreeNode getSessionNode(int sessionId) {
		
		// Check root node.
		if (sessionsRootNode == null) {
			return null;
		}
		
		// Get session nodes.
		Enumeration<TreeNode> sessionNodes = sessionsRootNode.children();
		while (sessionNodes.hasMoreElements()) {
			
			TreeNode node = sessionNodes.nextElement();
			if (!(node instanceof DefaultMutableTreeNode)) {
				continue;
			}
			
			DefaultMutableTreeNode sessionNode = (DefaultMutableTreeNode) node;
			Object userObject = sessionNode.getUserObject();
			
			if (!(userObject instanceof XdebugListenerSession)) {
				continue;
			}
			
			XdebugListenerSession session = (XdebugListenerSession) userObject;
			int foundSessionId = session.getSessionId();
			
			// Ceck session ID and return the session object.
			if (foundSessionId == sessionId) {
				return sessionNode;
			}
		}
		return null;
	}

	/**
	 * Select the top level of the stack.
	 */
	private void selectStackTop() {

		// Find current stack level node.
		DefaultMutableTreeNode currentStackNode = findCurrentStackNode();
		if (currentStackNode == null) {
			return;
		}

		// Get stack node path.
		TreeNode [] nodes = currentStackNode.getPath();
		TreePath nodePath = new TreePath(nodes);
		
		// Select the stack node path in the sessions tree.
		TreeSelectionModel selectionModel = treeSessions.getSelectionModel();
		selectionModel.clearSelection();
		selectionModel.addSelectionPath(nodePath);
	}
	
	/**
	 * Find current stack node.
	 * @return
	 */
	private DefaultMutableTreeNode findCurrentStackNode() {
		
		// Check current session.
		if (currentSession == null || sessionsRootNode == null) {
			return null;
		}
		
		// Get information about current debugged client.
		CurrentXdebugClientData currentClient = currentSession.getCurrentClientData();
		if (currentClient == null) {
			return null;
		}
		
		// Get current process ID.
		Long processId = currentClient.getProcessId();
		if (processId == null || processId <= 0) {
			return null;
		}
		
		// Find process node.
		DefaultMutableTreeNode processNode = findProcessNode(processId);
		if (processNode == null) {
			return null;
		}
		
		// Get current thread ID.
		Long threadId = currentClient.getThreadId();
		if (threadId == null || threadId < 0) {
			return null;
		}
		
		// Find thread node.
		DefaultMutableTreeNode threadNode = findThreadNode(processNode, threadId);
		if (threadNode == null) {
			return null;
		}
		
		// Find top stack level.
		DefaultMutableTreeNode topStackNode = findTopStackNode(threadNode);
		return topStackNode;
	}

	/**
	 * Find process node in the session tree.
	 * @param processId 
	 * @return
	 */
	private DefaultMutableTreeNode findProcessNode(Long processId) {
		
		if (sessionsRootNode == null || processId == null || processId < 0) {
			return null;
		}
		
		Enumeration<TreeNode> processNodes = sessionsRootNode.children();
		while (processNodes.hasMoreElements()) {
			
			TreeNode node = processNodes.nextElement();
			if (!(node instanceof DefaultMutableTreeNode)) {
				continue;
			}
			
			DefaultMutableTreeNode processNode = (DefaultMutableTreeNode) node;
			Object userObject = processNode.getUserObject();
			
			if (!(userObject instanceof XdebugListenerSession)) {
				continue;
			}
			
			XdebugListenerSession process = (XdebugListenerSession) userObject;
			Long foundProcessId = process.getProcessId();
			
			if (processId.equals(foundProcessId)) {
				return processNode;
			}
		}
		return null;
	}
	
	/**
	 * Find thread node in the session tree.
	 * @param processNode
	 * @param threadId
	 * @return
	 */
	private DefaultMutableTreeNode findThreadNode(DefaultMutableTreeNode processNode, Long threadId) {
		
		if (processNode == null || threadId == null || threadId < 0) {
			return null;
		}
		
		Enumeration<TreeNode> threadNodes = processNode.children();
		while (threadNodes.hasMoreElements()) {
			
			TreeNode node = threadNodes.nextElement();
			if (!(node instanceof DefaultMutableTreeNode)) {
				continue;
			}
			
			DefaultMutableTreeNode threadNode = (DefaultMutableTreeNode) node;
			Object userObject = threadNode.getUserObject();
			
			if (!(userObject instanceof XdebugThread)) {
				continue;
			}
			
			XdebugThread thread = (XdebugThread) userObject;
			Long foundThreadId = thread.getThreadId();
			
			if (threadId.equals(foundThreadId)) {
				return threadNode;
			}
		}
		return null;
	}
	
	/**
	 * Find top stack node.
	 * @param threadNode
	 * @return
	 */
	private DefaultMutableTreeNode findTopStackNode(DefaultMutableTreeNode threadNode) {
		
		if (threadNode == null) {
			return null;
		}
		
		Enumeration<TreeNode> stackNodes = threadNode.children();
		
		if (!stackNodes.hasMoreElements()) {
			return null;
		}
		
		TreeNode node = stackNodes.nextElement();
		if (!(node instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode topStackNode = (DefaultMutableTreeNode) node;
		
		Object userObject = topStackNode.getUserObject();
		if (!(userObject instanceof XdebugStackLevel)) {
			return null;
		}
		
		return topStackNode;
	}

	/**
	 * Load watch list values from Xdebug client.
	 */
	private void loadWatchListValues() {
		
		// Get current stack.
		XdebugStackLevel stackLevel = getCurrentStack();
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
	private void loadWatchedValue(XdebugStackLevel stackLevel, DebugWatchItem watchedItem) {
		
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
		
		SwingUtilities.invokeLater(() ->  {
			
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
		});
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
	private XdebugStackLevel getCurrentStack() {
		
		// Get selected tree node.
		Object currentNode = treeSessions.getLastSelectedPathComponent();
		if (currentNode == null) {
			return null;
		}
		
		// If the selected node is process node, get first child, it should be the thread node.
		if (currentNode instanceof XdebugProcess) {
			
			XdebugProcess processNode = (XdebugProcess) currentNode;
			int childrenCount = 0;
			if (childrenCount <= 0) {
				return null;
			}
			
			currentNode = null;
		}
		
		// If current node is thread node, get first child, it should be the stack node.
		if (currentNode instanceof XdebugThread) {
			
			XdebugThread threadNode = (XdebugThread) currentNode;
			int childrenCount = 0;
			
			if (childrenCount <= 0) {
				return null;
			}
			
			currentNode = null;
		}
		
		// If current node is stack node, return the stack object.
		if (!(currentNode instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode currentMutableNode = (DefaultMutableTreeNode) currentNode;
		Object userObject = currentMutableNode.getUserObject();
		
		if (!(userObject instanceof XdebugStackLevel)) {
			return null;
		}
		
		XdebugStackLevel stackLevel = (XdebugStackLevel) userObject;
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
			
			updateViews();
			
			int transactionId = xdebugSession.createTransaction("run", null, response -> {
				
				try {
					XdebugListenerSession.throwPossibleException(response);
				}
				catch (Exception e) {
					onException(e);
				}
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
	private void createWatchView() {
		
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
	 * On step into
	 */
	protected void onStepInto() {
		
		// Process step command
		try {
			XdebugListenerSession xdebugSession = getSelectedXdebugSession();
			if (xdebugSession == null) {
				xdebugSession = currentSession;
			}
			
			updateViews();
			
			int transactionId = xdebugSession.createTransaction("step_into", null, response -> {
				
				try {
					XdebugListenerSession.throwPossibleException(response);
				}
				catch (Exception e) {
					onException(e);
				}
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
			
			updateViews();
			
			int transactionId = xdebugSession.createTransaction("step_over", null, response -> {
				
				try {
					XdebugListenerSession.throwPossibleException(response);
				}
				catch (Exception e) {
					onException(e);
				}
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
			
			updateViews();
			
			int transactionId = xdebugSession.createTransaction("step_out", null, response -> {
				try {
					XdebugListenerSession.throwPossibleException(response);
				}
				catch (Exception e) {
					onException(e);
				}
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
			
			updateViews();
			
			int transactionId = xdebugSession.createTransaction("stop", null, response -> {
				
				try {
					XdebugListenerSession.throwPossibleException(response);
					
					if (completedLambda != null) {
						completedLambda.run();
					}
				}
				catch (Exception e) {
					onException(e);
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
	 * Sets CSS rule for code editor
	 * @param rule
	 */
	private void setEditorCss(String rule) throws Exception {
		
		HTMLDocument document = (HTMLDocument) textCode.getDocument();
		StyleSheet documentCss = document.getStyleSheet();
		documentCss.addRule(rule);
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
		XdebugLogMessage.setFulltextFilter(filterString, caseSensitive, wholeWords, exactMatch);
		XdebugLogMessage.displayHtmlLog(textExceptions);		
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
		XdebugLogMessage.addLogMessage(message);
		
		// Get log content.
		XdebugLogMessage.displayHtmlLog(textExceptions);
		
		// Select panel with exceptions.
		int tabIndex = tabbedPane.indexOfComponent(panelExceptions);
		if (tabIndex >= 0) {
			tabbedPane.setSelectedIndex(tabIndex);
		}
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
	 * Show user alert.
	 * @param message
	 * @param timeout 
	 */
	public void showUserAlert(String message, int timeout) {
		
		SwingUtilities.invokeLater(() -> {
			AlertWithTimeout.showDialog(this, message, timeout);
		});
	}
	
	/**
	 * Called on exception.
	 * @param e
	 */
	protected void onThrownException(Throwable e) throws Exception {
		
		// Override this method.
		onException(e);
		throw new Exception(e);
	}
	
	/**
	 * Called on exception.
	 * @param messageFormatId
	 * @param exception
	 */
	private void onException(String messageFormatId, Object ... params) {
		
		String messageFormat = Resources.getString(messageFormatId);
		String errorMessage = String.format(messageFormat, params);
		
		Exception e = new Exception(errorMessage);
		onException(e);
	}
	
	/**
	 * Called on exception.
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
	 * Called on exception.
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
