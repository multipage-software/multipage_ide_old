/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 24-03-2021
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.BiFunction;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.maclan.help.ProgramHelp;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.TextAreaEx;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * 
 * @author vakol
 *
 */
public class LoggingDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Enable/disable logging.
	 */
	private static Boolean enabled = false;
	
	//$hide>>$
	
	/**
	 * If the following flag is set to true, the dialog is opened when it is initialized.
	 */
	private static boolean openedWhenInitialized = false;
	
	/**
	 * Switch between list and single item view for the log.
	 */
	private static boolean logList = true;
	
	/**
	 * Message queue viewer update interval
	 */
	private static int messageQueueUpdateIntervalMs = 3000;
	
	/**
	 * Events tree update interval in milliseconds.
	 */
	private static int eventTreeUpdateIntervalMs = 1000;
	
	/**
	 * Omit/choose selected signals.
	 */
	private static boolean omitChooseSignals = true;
	
	/**
	 * Queue limit.
	 */
	private static int queueLimit = 20;
	
	/**
	 * Message limit.
	 */
	private static int logLimit = 20;
	
	/**
	 * Limit of logged events.
	 */
	private static int eventLimit = 30;

	/**
	 * Bounds.
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Splitter positions.
	 */
	private static int eventsWindowSplitter = -1;
	private static int queueWindowSplitter = -1;
	
	/**
	 * Selected tab.
	 */
	private static int selectedTab = 0;
	
	/**
	 * Index of font size for the simple log view.
	 */
	private static int logFontSizeIndex = 0;
	
	/**
	 * Dark green color constant.
	 */
	private static final Color DARK_GREEN = new Color(0, 128, 0);
	
	/**
	 * A set of available break point classes.
	 */
	private static final HashSet<Class<?>> availableBreakPointClasses = Utility.makeSet(Signal.class, Message.class, LoggedEvent.class);

	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		logFontSizeIndex = inputStream.readInt();
		openedWhenInitialized = inputStream.readBoolean();
		logList = inputStream.readBoolean();
		omitChooseSignals = inputStream.readBoolean();
		omittedOrChosenSignals = Utility.readInputStreamObject(inputStream, HashSet.class);
		eventsWindowSplitter = inputStream.readInt();
		queueWindowSplitter = inputStream.readInt();
		selectedTab = inputStream.readInt();
		messageQueueUpdateIntervalMs = inputStream.readInt();
		eventTreeUpdateIntervalMs = inputStream.readInt();
		queueLimit = inputStream.readInt();
		logLimit = inputStream.readInt();
		eventLimit = inputStream.readInt();
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(logFontSizeIndex);
		outputStream.writeBoolean(openedWhenInitialized);
		outputStream.writeBoolean(logList);
		outputStream.writeBoolean(omitChooseSignals);
		outputStream.writeObject(omittedOrChosenSignals);
		outputStream.writeInt(eventsWindowSplitter);
		outputStream.writeInt(queueWindowSplitter);
		outputStream.writeInt(selectedTab);
		outputStream.writeInt(messageQueueUpdateIntervalMs);
		outputStream.writeInt(eventTreeUpdateIntervalMs);
		outputStream.writeInt(queueLimit);
		outputStream.writeInt(logLimit);
		outputStream.writeInt(eventLimit);
	}
	
	/**
	 * Logged message class.
	 */
	public static class LoggedMessage {
		
		/**
		 * Message text
		 */
		private String messageText = "unknown";
		
		/**
		 * Time stamp.
		 */
		private long timeStamp = -1;
		
		/**
		 * Logged message.
		 * @param message
		 */
		public LoggedMessage(String message) {
			
			this.messageText = message;
			this.timeStamp = System.currentTimeMillis();
		}
		
		/**
		 * Get message text.
		 * @return
		 */
		public String getText() {
			
			Timestamp timeStamp = new Timestamp(this.timeStamp);
			return String.format("[%s] %s", timeStamp.toString(), this.messageText);
		}
	}
	
	/**
	 * Logged event class.
	 */
	private static class LoggedEvent {
		
		/**
		 * Invoked event handle.
		 */
		public EventHandle eventHandle = null;
		
		/**
		 * Error flags.
		 */
		public Long executionTime = null;
		
		/**
		 * Matching message.
		 */
		public Message matchingMessage = null;
	}
	
	/**
	 * Logged messages.
	 */
	private static LinkedList<LoggedMessage> logTexts = new LinkedList<LoggedMessage>();
	
	/**
	 * Logged message queue snapshots.
	 * Maps: Time Moment -> List of Messages
	 */
	private static LinkedHashMap<String, LinkedList<Message>> messageQueueSnapshots = new LinkedHashMap<String, LinkedList<Message>>();
	
	/**
	 * Logged events.
	 * Maps: Signal -> Message -> Execution time -> Event
	 */
	private static LinkedHashMap<Signal, LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>> events = new LinkedHashMap<Signal, LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>>();
	
	/**
	 * Omitted signals.
	 */
	private static HashSet<Signal> omittedOrChosenSignals = new HashSet<Signal>();
	
	/**
	 * Singleton dialog object.
	 */
	private static LoggingDialog dialog = null;
	
	/**
	 * Break point matching object.
	 */
	private static HashSet<Object> breakPointMatchObjects = new HashSet<Object>();
	
	/**
	 * Set enable/disable logging.
	 * @param flag
	 */
	public static void enableLogging(boolean flag) {
		
		synchronized (LoggingDialog.enabled) {
			
			LoggingDialog.enabled = flag;
		}
	}
	
	/**
	 * Returns true if the logging is enabled.
	 * @return
	 */
	public static boolean isLoggingEnabled() {
		
		synchronized (LoggingDialog.enabled) {
			
			if (LoggingDialog.enabled) {
				return true;
			}
		}
		
		if (LoggingDialog.dialog == null) {
			return false;
		}
		
		synchronized (LoggingDialog.dialog) {
			
			return LoggingDialog.dialog.isVisible();
		}
	}
	
	/**
	 * Initialize this dialog.
	 */
	public static void initialize(Component parent) {
		
		Window parentWindow = Utility.findWindow(parent);
		dialog = new LoggingDialog(parentWindow);
		
		// Attach the help module.
		ProgramHelp.setLogLambda(text -> log(text));
		ProgramHelp.setCanLogLambda(() -> isLoggingEnabled());
		
		// If the following flag was set, open the dialog.
		if (openedWhenInitialized) {
			showDialog(parentWindow);
		}
	}
	
	/**
	 * Root node of the message queue viewer.
	 */
	private DefaultMutableTreeNode messageQueueTreeRootNode;
	
	/**
	 * Message queue tree model.
	 */
	private DefaultTreeModel messageQueueTreeModel;

	/**
	 * A timer that updates message queue viewer.
	 */
	private Timer updateMessageQueueTimer = null;
	
	/**
	 * Root node of the events tree.
	 */
	private DefaultMutableTreeNode eventTreeRootNode = null;
	
	/**
	 * Tree model for displaying logged events.
	 */
	private DefaultTreeModel eventTreeModel = null;
	
	/**
	 * Update event tree timer.
	 */
	private Timer updateEventTreeTimer = null;
	
	/**
	 * List model of break points set.
	 */
	private DefaultListModel listBreakPointsModel = null;
	
	/**
	 * Recently selected event tree object.
	 */
	private Object lastSelectedTreeObject;

	//$hide<<$
	
	/**
	 * Components.
	 */
	private TextAreaEx textLog;
	private JTabbedPane tabbedPane;
	private DefaultListModel<Signal> listModelOmittedSignals;
	private JPanel panelBreakPoints;
	private JList listBreakPoints;
	private JToolBar toolBarBreakPoints;
	private JPanel panelEvents;
	private JToolBar toolBarEvents;
	private JSplitPane splitPaneEvents;
	private JScrollPane scrollPaneEvents;
	private JTree treeEvents;
	private JPanel panelOmitOrChooseSignals;
	private JCheckBox checkOmitOrChooseSignals;
	private JScrollPane scrollPaneOmitOrChoose;
	private JList<Signal> listOmittedOrChosenSignals;
	private JPanel panelLog;
	private JPopupMenu popupMenu;
	private JMenuItem menuAddBreakPoint;
	private JScrollPane scrollPaneEventsDescription;
	private JTextPane editorPaneDescription;
	private JMenuItem menuAddOmittedChosen;
	private JButton buttonClearOmitedChosen;
	private JSeparator separator;
	private JMenuItem menuEventsPrintReflection;
	private JPanel panelMessageQueue;
	private JScrollPane scrollPaneMessageQueue;
	private JTree treeMessageQueue;
	private JToolBar toolBarMessageQueue;
	private JSplitPane splitPaneMessageQueue;
	private JScrollPane scrollPaneQueueMessageDescription;
	private JTextPane textPaneQueueMessage;
	private JPopupMenu popupMenuMessageQueues;
	private JMenuItem menuMessageQueuePrintReflection;
	private JMenuItem menuMessageGoToEvent;
	private JMenuItem menuGoToQueueMessage;
	private JToolBar toolBarLog;
	private JToggleButton buttonListOrSingleItem;
	private JComboBox<Integer> comboFontSize;
	private JLabel labelLogFontSize;
	
	/**
	 * Show dialog.
	 * @param parent
	 */
	public static void showDialog(Component parent) {
		
		// Show window.
		dialog.setVisible(true);
		
		// Reset the flag.
		openedWhenInitialized = true;
	}
	
	/**
	 * Create the dialog.
	 */
	public LoggingDialog(Window parentWindow) {
		super(parentWindow, ModalityType.MODELESS);
		
		synchronized (this) {//$hide$
			initComponents();
			postCreate(); //$hide$
		}//$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		setTitle("org.multipage.generator.textLoggingDialogTitle");
		setBounds(100, 100, 557, 471);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 3, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 3, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -3, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -3, SpringLayout.EAST, getContentPane());
		getContentPane().add(tabbedPane);
		
		treeEvents = new JTree();
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeEvents.setSelectionModel(selectionModel);
		treeEvents.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onEventSelection();
			}
		});
		
		panelLog = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textLoggedMessages", null, panelLog, null);
		panelLog.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneLog = new JScrollPane();
		panelLog.add(scrollPaneLog, BorderLayout.CENTER);
		
		textLog = new TextAreaEx();
		textLog.setLineWrap(true);
		scrollPaneLog.setViewportView(textLog);
		
		toolBarLog = new JToolBar();
		toolBarLog.setFloatable(false);
		panelLog.add(toolBarLog, BorderLayout.NORTH);
		
		labelLogFontSize = new JLabel("org.multipage.generator.textLogFontSize");
		toolBarLog.add(labelLogFontSize);
		
		comboFontSize = new JComboBox<Integer>();
		comboFontSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFontSize();
			}
		});
		comboFontSize.setMaximumSize(new Dimension(50, 22));
		toolBarLog.add(comboFontSize);
		
		panelMessageQueue = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textLogMessageQueue", null, panelMessageQueue, null);
		panelMessageQueue.setLayout(new BorderLayout(0, 0));
		DefaultTreeSelectionModel queueSelectionModel = new DefaultTreeSelectionModel();
		queueSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeEvents.setSelectionModel(queueSelectionModel);
		
		toolBarMessageQueue = new JToolBar();
		toolBarMessageQueue.setFloatable(false);
		panelMessageQueue.add(toolBarMessageQueue, BorderLayout.NORTH);
		
		splitPaneMessageQueue = new JSplitPane();
		splitPaneMessageQueue.setResizeWeight(0.8);
		splitPaneMessageQueue.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panelMessageQueue.add(splitPaneMessageQueue, BorderLayout.CENTER);
		
		scrollPaneMessageQueue = new JScrollPane();
		splitPaneMessageQueue.setLeftComponent(scrollPaneMessageQueue);
		
		treeMessageQueue = new JTree();
		treeMessageQueue.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onMessageQueueObjectSelected();
			}
		});
		treeMessageQueue.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPaneMessageQueue.setViewportView(treeMessageQueue);
		
		popupMenuMessageQueues = new JPopupMenu();
		addPopup(treeMessageQueue, popupMenuMessageQueues);
		
		menuMessageQueuePrintReflection = new JMenuItem("org.multipage.generator.menuLogPrintReflection");
		menuMessageQueuePrintReflection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrintReflection(treeMessageQueue);
			}
		});
		
		menuMessageGoToEvent = new JMenuItem("org.multipage.generator.menuLogMessageGoToEvent");
		menuMessageGoToEvent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onGoToMessageEvent();
			}
		});
		popupMenuMessageQueues.add(menuMessageGoToEvent);
		popupMenuMessageQueues.add(menuMessageQueuePrintReflection);
		
		scrollPaneQueueMessageDescription = new JScrollPane();
		splitPaneMessageQueue.setRightComponent(scrollPaneQueueMessageDescription);
		
		textPaneQueueMessage = new JTextPane();
		textPaneQueueMessage.setContentType("text/html");
		scrollPaneQueueMessageDescription.setViewportView(textPaneQueueMessage);
		
		panelEvents = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textLoggedConditionalEvents", null, panelEvents, null);
		panelEvents.setLayout(new BorderLayout(0, 0));
		
		toolBarEvents = new JToolBar();
		toolBarEvents.setFloatable(false);
		panelEvents.add(toolBarEvents, BorderLayout.NORTH);
		
		splitPaneEvents = new JSplitPane();
		splitPaneEvents.setResizeWeight(0.7);
		splitPaneEvents.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panelEvents.add(splitPaneEvents, BorderLayout.CENTER);
		
		scrollPaneEvents = new JScrollPane();
		splitPaneEvents.setLeftComponent(scrollPaneEvents);
		scrollPaneEvents.setViewportView(treeEvents);
		
		popupMenu = new JPopupMenu();
		addPopup(treeEvents, popupMenu);
		
		menuAddBreakPoint = new JMenuItem("org.multipage.generator.menuAddLogBreakPoint");
		menuAddBreakPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddBreakPoint();
			}
		});
		popupMenu.add(menuAddBreakPoint);
		
		menuAddOmittedChosen = new JMenuItem("org.multipage.generator.menuAddLogOmittedChosenSignal");
		menuAddOmittedChosen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onMenuOmitChooseSignal();
			}
		});
		popupMenu.add(menuAddOmittedChosen);
		
		menuEventsPrintReflection = new JMenuItem("org.multipage.generator.menuLogPrintReflection");
		menuEventsPrintReflection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrintReflection(treeEvents);
			}
		});
		
		menuGoToQueueMessage = new JMenuItem("org.multipage.generator.menuLogMessageGoToQueue");
		menuGoToQueueMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onGoToMessageQueue();
			}
		});
		popupMenu.add(menuGoToQueueMessage);
		popupMenu.add(menuEventsPrintReflection);
		
		scrollPaneEventsDescription = new JScrollPane();
		splitPaneEvents.setRightComponent(scrollPaneEventsDescription);
		
		editorPaneDescription = new JTextPane();
		editorPaneDescription.setEditable(false);
		editorPaneDescription.setContentType("text/html");
		scrollPaneEventsDescription.setViewportView(editorPaneDescription);
		
		panelOmitOrChooseSignals = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textOmitOrChooseSignals", null, panelOmitOrChooseSignals, null);
		panelOmitOrChooseSignals.setLayout(new BorderLayout(0, 0));
		
		JPanel panelTopOmitOrChoose = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelTopOmitOrChoose.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelOmitOrChooseSignals.add(panelTopOmitOrChoose, BorderLayout.NORTH);
		
		checkOmitOrChooseSignals = new JCheckBox("org.multipage.generator.textOmitOrChoose");
		checkOmitOrChooseSignals.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOmitChooseSignals();
			}
		});
		
		buttonClearOmitedChosen = new JButton("");
		buttonClearOmitedChosen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClearOmittedChosen();
			}
		});
		buttonClearOmitedChosen.setToolTipText("org.multipage.generator.tooltipLogClearOmitedChoseSignal");
		buttonClearOmitedChosen.setPreferredSize(new Dimension(24, 24));
		buttonClearOmitedChosen.setMargin(new Insets(0, 0, 0, 0));
		panelTopOmitOrChoose.add(buttonClearOmitedChosen);
		
		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(2, 24));
		separator.setOrientation(SwingConstants.VERTICAL);
		panelTopOmitOrChoose.add(separator);
		panelTopOmitOrChoose.add(checkOmitOrChooseSignals);
		
		scrollPaneOmitOrChoose = new JScrollPane();
		panelOmitOrChooseSignals.add(scrollPaneOmitOrChoose, BorderLayout.CENTER);
		
		listOmittedOrChosenSignals = new JList();
		listOmittedOrChosenSignals.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onOmittedOrChosenSignalClick(e);
			}
		});
		scrollPaneOmitOrChoose.setViewportView(listOmittedOrChosenSignals);
		
		panelBreakPoints = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textBreakPointsInLogWindow", null, panelBreakPoints, null);
		panelBreakPoints.setLayout(new BorderLayout(0, 0));
		
		toolBarBreakPoints = new JToolBar();
		toolBarBreakPoints.setFloatable(false);
		panelBreakPoints.add(toolBarBreakPoints, BorderLayout.NORTH);
		
		JScrollPane scrollPaneBreakPoints = new JScrollPane();
		scrollPaneBreakPoints.setBorder(null);
		panelBreakPoints.add(scrollPaneBreakPoints, BorderLayout.CENTER);
		
		listBreakPoints = new JList();
		scrollPaneBreakPoints.setViewportView(listBreakPoints);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		createToolBars();
		localize();
		setIcons();
		loadDialog();
		createMessaqeQueueTree();
		createEventTree();
		createOmittedSignalList();
		createBreakPointsList();
	}
	
	/**
	 * Creates tool bars with buttons that enable user to run actions for logged items.
	 */
	private void createToolBars() {
		
		// Add tool bar for log.
		buttonListOrSingleItem = ToolBarKit.addToggleButton(toolBarLog, "org/multipage/generator/images/list.png", "org.multipage.generator.tooltipLogListOrSingleItem", () -> onListOrSingleItem());
		buttonListOrSingleItem.setSelected(true);
		loadFontSizes(comboFontSize);
		
		// A tool bar for message queue.
		ToolBarKit.addToolBarButton(toolBarMessageQueue, "org/multipage/generator/images/close_all.png", "org.multipage.generator.tooltipClearLoggedQueues", () -> onClearQueues());
		ToolBarKit.addToolBarButton(toolBarMessageQueue, "org/multipage/generator/images/settings.png", "org.multipage.generator.tooltipLoggedQueuesSettings", () -> onLogSettings());
		
		// A tool bar for logged events.
		ToolBarKit.addToolBarButton(toolBarEvents, "org/multipage/generator/images/close_all.png", "org.multipage.generator.tooltipClearLoggedEvents", () -> onClearEvents());
		ToolBarKit.addToolBarButton(toolBarEvents, "org/multipage/generator/images/settings.png", "org.multipage.generator.tooltipLoggedEventsSettings", () -> onLogSettings());
		
		// A tool bar for break points.
		ToolBarKit.addToolBarButton(toolBarBreakPoints, "org/multipage/generator/images/close_all.png", "org.multipage.generator.tooltipClearLogBreakPoints", () -> onClearBreakPoints());
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(tabbedPane);
		Utility.localize(labelLogFontSize);
		Utility.localize(checkOmitOrChooseSignals);
		Utility.localize(buttonClearOmitedChosen);
		Utility.localize(menuAddBreakPoint);
		Utility.localize(menuAddOmittedChosen);
		Utility.localize(menuEventsPrintReflection);
		Utility.localize(menuMessageQueuePrintReflection);
		Utility.localize(menuMessageGoToEvent);
		Utility.localize(menuGoToQueueMessage);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		// Set window icon.
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		
		// Set control icons.
		menuAddBreakPoint.setIcon(Images.getIcon("org/multipage/generator/images/breakpoint.png"));
		menuAddOmittedChosen.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonClearOmitedChosen.setIcon(Images.getIcon("org/multipage/generator/images/close_all.png"));
		
		// Set tree icons.
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) treeEvents.getCellRenderer();
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds == null || bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		if (queueWindowSplitter != -1) {
			splitPaneMessageQueue.setDividerLocation(queueWindowSplitter);
		}
		else {
			splitPaneMessageQueue.setDividerLocation(0.8);
		}
		if (eventsWindowSplitter != -1) {
			splitPaneEvents.setDividerLocation(eventsWindowSplitter);
		}
		else {
			splitPaneEvents.setDividerLocation(0.8);
		}
		comboFontSize.setSelectedIndex(logFontSizeIndex);
		tabbedPane.setSelectedIndex(selectedTab);
		checkOmitOrChooseSignals.setSelected(omitChooseSignals);
		buttonListOrSingleItem.setSelected(logList);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		logFontSizeIndex = comboFontSize.getSelectedIndex();
		queueWindowSplitter = splitPaneMessageQueue.getDividerLocation();
		eventsWindowSplitter = splitPaneEvents.getDividerLocation();
		selectedTab = tabbedPane.getSelectedIndex();
		omitChooseSignals = checkOmitOrChooseSignals.isSelected();
		logList = buttonListOrSingleItem.isSelected();
	}
	
	/**
	 * Add popup menu.
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
	
	/**
	 * Load font sizes.
	 * @param comboBox
	 */
	private void loadFontSizes(JComboBox comboBox) {
		
		comboBox.removeAll();
		
		for (int index = 9; index < 14; index++) {
			
			int size = (int) Math.pow(Math.E, (double) index / 3.0) / 3 + 3;
			comboBox.addItem(size);
		}
	}
	
	/**
	 * Create message queue tree.
	 */
	private void createMessaqeQueueTree() {
		
		// Create the tree model.
		messageQueueTreeRootNode = new DefaultMutableTreeNode();
		messageQueueTreeModel = new DefaultTreeModel(messageQueueTreeRootNode);
		treeMessageQueue.setModel(messageQueueTreeModel);
		
		// Set tree node renderer.
		treeMessageQueue.setCellRenderer(new TreeCellRenderer() {
			
			// Renderer.
			RendererJLabel renderer = new RendererJLabel();
			
			// Callback method.s
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				// Check value.
				if (!(value instanceof DefaultMutableTreeNode)) {
					renderer.setText("unknown");
				}
				else {
					// Get queue object.
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					Object queueObject = node.getUserObject();
					
					Color nodeColor = Color.BLACK;
					
					// Set node text.
					// On the message.
					if (queueObject instanceof Message) {
						Message message = (Message) queueObject;
						renderer.setText(String.format("[0x%08X] message %s", message.hashCode(), message.signal.name()));
						nodeColor = DARK_GREEN;
					}
					// Otherwise...
					else if (queueObject != null) {
						renderer.setText(queueObject.toString());
						nodeColor = Color.GRAY;
					}
					else {
						renderer.setText("root");
						nodeColor = Color.GRAY;
					}
					
					// Set node color.
					renderer.setForeground(nodeColor);
				}
				
				// Set renderer properties
				renderer.set(selected, hasFocus, row);
				return renderer;
			}
		});
			
		// Start update timer.
		updateMessageQueueTimer = new Timer(messageQueueUpdateIntervalMs, event -> updateMessageQueueTree());
		updateMessageQueueTimer.start();
	}
	
	/**
	 * Create a tree with categorized events.
	 */
	private void createEventTree() {
		
		// Create and set tree model.
		eventTreeRootNode = new DefaultMutableTreeNode();
		eventTreeModel = new DefaultTreeModel(eventTreeRootNode);
		treeEvents.setModel(eventTreeModel);
		treeEvents.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// Set tree node renderer.
		treeEvents.setCellRenderer(new TreeCellRenderer() {
			
			// Renderer.
			RendererJLabel renderer = new RendererJLabel();
			
			// Callback method.s
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				
				// Check value.
				if (!(value instanceof DefaultMutableTreeNode)) {
					renderer.setText("unknown");
				}
				else {
					// Get event object.
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					Object eventObject = node.getUserObject();
					
					Color nodeColor = Color.BLACK;
					
					// Set node text.
					// On the signal.
					if (eventObject instanceof Signal) {
						
						Signal signal = (Signal) eventObject;
						renderer.setText(signal.name());
						nodeColor = Color.RED;
					}
					// On the message.
					else if (eventObject instanceof Message) {
						Message message = (Message) eventObject;
						renderer.setText(String.format("[0x%08X] message", message.hashCode()));
						nodeColor = DARK_GREEN;
					}
					// On the logged event.
					else if (eventObject instanceof LoggedEvent) {
						LoggedEvent event = (LoggedEvent) eventObject;
						renderer.setText(String.format("[0x%08X] event", event.hashCode()));
						nodeColor = Color.BLUE;
					}
					// Otherwise...
					else if (eventObject != null) {
						renderer.setText(eventObject.toString());
						nodeColor = Color.GRAY;
					}
					else {
						renderer.setText("root");
						nodeColor = Color.GRAY;
					}
					
					// Set node color.
					renderer.setForeground(nodeColor);
				}
				
				// Set renderer properties
				renderer.set(selected, hasFocus, row);
				return renderer;
			}
		});
		
		// Create update timer.
		updateEventTreeTimer = new Timer(eventTreeUpdateIntervalMs, event -> {
			
			SwingUtilities.invokeLater(() -> {
				synchronized (events) {
					updateEventTree();
				}
			});
		});
		updateEventTreeTimer.start();
	}
	
	/**
	 * Create omitted signal list.
	 */
	private void createOmittedSignalList() {
		
		// Create and set list model.
		listModelOmittedSignals = new DefaultListModel();
		
		// Fill the list model.
		Arrays.stream(Signal.values())
			.sorted((s1, s2) -> s1.name().compareTo(s2.name()))
			.forEach(signal -> {
				listModelOmittedSignals.addElement(signal);
			});
		
		listOmittedOrChosenSignals.setModel(listModelOmittedSignals);
		
		// Create list items renderer.
		ListCellRenderer<Signal> renderer = new ListCellRenderer<Signal>() {
			
			// Rendered label.
			RendererJLabel renderer = new RendererJLabel();
			
			// Renderer callback.
			@Override
			public Component getListCellRendererComponent(JList list, Signal signal, int index, boolean isSelected,
					boolean cellHasFocus) {
				
				// Set caption.
				renderer.setText(signal.toString());
				
				// Set renderer properties.
				renderer.set(isSelected, cellHasFocus, index);
				
				// If it is omitted, colorize it with red.
				if (omittedOrChosenSignals.contains(signal)) {
					boolean omitted = checkOmitOrChooseSignals.isSelected();
					renderer.setForeground(omitted ? Color.RED : DARK_GREEN);
				}
				else {
					renderer.setForeground(Color.GRAY);
				}
				return renderer;
			}
		};
		listOmittedOrChosenSignals.setCellRenderer(renderer);
	}
	
	/**
	 * Create list of break points.
	 */
	private void createBreakPointsList() {
		
		// Create and assign list model.
		listBreakPointsModel = new DefaultListModel();
		listBreakPoints.setModel(listBreakPointsModel);
		
		// Create items renderer.
		listBreakPoints.setCellRenderer(new ListCellRenderer() {
			
			// Rendered label.
			private RendererJLabel renderer = new RendererJLabel();
			
			// Constructor.
			{
				// Set icon.
				renderer.setIcon(Images.getIcon("org/multipage/generator/images/breakpoint.png"));
			};
			
			// Callback method.
			@Override
			public Component getListCellRendererComponent(JList list, Object breakPointObject, int index, boolean isSelected,
					boolean cellHasFocus) {
				
				// Set break point caption.
				renderer.setText(breakPointObject.toString());
				
				// Set renderer.
				renderer.set(isSelected, cellHasFocus, index);
				return renderer;
			}
		});
	}
	
	/**
	 * On event selection.
	 * @param e 
	 */
	protected void onEventSelection() {
		
		synchronized (treeEvents) {
			
			// Get selected tree item.
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeEvents.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}
			
			// Check selection change.
			Object selectedObject = node.getUserObject();
			if (lastSelectedTreeObject == selectedObject) {
				return;
			}
			lastSelectedTreeObject = selectedObject;
			
			// Get description.
			String description = getNodeDescription(node);
			
			// Display node description.
			editorPaneDescription.setText(description);
		}
	}
	
	/**
	 * Get node description.
	 * @param node
	 * @return
	 */
	private String getNodeDescription(DefaultMutableTreeNode node) {

		// Get description of the event part.
		Object loggedObject = node.getUserObject();
		String description = getLoggedObjectDescription(loggedObject);

		return description;
	}
	
	/**
	 * Get the input object description.
	 * @param theObject
	 * @return
	 */
	private String getObjectDescription(Object theObject) {
		
		String description = null;
		if (theObject instanceof Class) {
			Class theClass = (Class) theObject;
			description = theClass.getSimpleName();
		}
		else if (theObject instanceof Integer || theObject instanceof Long || theObject instanceof Boolean
				|| theObject instanceof Character || theObject instanceof String) {
			description = theObject.toString();
		}
		else if (theObject != null)  {
			description = theObject.getClass().getSimpleName();
		}
		else {
			description = "null";
		}
		return description;
	}
	
	/**
	 * Get array description.
	 * @param additionalInfos
	 * @return
	 */
	private Object getArrayDescription(Object [] additionalInfos) {
		
		if (additionalInfos == null) {
			return "null";
		}
		
		String description = "";
		for (Object info : additionalInfos) {
			
			if (description.length() > 0) {
				description = ", " + description;
				description += String.format("[%s]", getDataDescription(info));
			}
		}
		return description;
	}
	
	/**
	 * Get reflection string.
	 * @param reflection
	 * @return
	 */
	private String getReflectionDescription(StackTraceElement reflection) {
		
		if (reflection == null) {
			return "null";
		}
		
		String description = String.format("%s", reflection.toString());
		return description;
	}

	/**
	 * Get signal types description.
	 * @param signal
	 * @return
	 */
	private String getSignalTypesDescription(Signal signal) {
		
		Obj<String> description = new Obj<String>("");
		signal.getTypes().stream().forEach(signalType -> {
			
			if (!description.ref.isEmpty()) {
				description.ref = description.ref + ", ";
			}
			description.ref += signalType.name();
		});
		return description.ref;
	}
	
	/**
	 * Get data description.
	 * @param dataObject
	 * @return
	 */
	private String getDataDescription(Object dataObject) {
		
		if (dataObject == null) {
			return "null";
		}
		String description = String.format("[%s] %s", dataObject.getClass().getSimpleName(), dataObject.toString());
		return description;
	}
	
	/**
	 * Get logged object description.
	 * @param loggedObject
	 * @return
	 */
	private String getLoggedObjectDescription(Object loggedObject) {
		
		String description = "";
		
		// Get signal description.
		if (loggedObject instanceof Signal) {
			
			Signal signal = (Signal) loggedObject;
			description = String.format(
					"<html>"
					+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
					+ "<tr><td><b>signal:</b></td><td>&nbsp;%s</td></tr>"
					+ "<tr><td><b>priority:</b></td><td>&nbsp;&nbsp;%d</td></tr>"
					+ "<tr><td><b>types:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "</table>"
					+ "</html>",
					signal.name(),
					signal.getPriority(),
					getSignalTypesDescription(signal)
					);
		}
		else if (loggedObject instanceof Message) {
			
			Message message = (Message) loggedObject;
			description = String.format(
					  "<html>"
					+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
					+ "<tr><td><b>hashcode:</b></td><td>&nbsp;&nbsp;[0x%08X]</td></tr>"
					+ "<tr><td><b>signal:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>recieve&nbsp;time:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>source:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>target:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>info:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>+infos:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>source&nbsp;code:</b></td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "</table>"
					+ "</html>",
					message.hashCode(),
					message.signal.name(),
					Utility.formatTime(message.receiveTime),
					getObjectDescription(message.source),
					getObjectDescription(message.target),
					getDataDescription(message.relatedInfo),
					getArrayDescription(message.additionalInfos),
					getReflectionDescription(message.reflection)
					);
		}
		else if (loggedObject instanceof LoggedEvent) {
			
			LoggedEvent loggedEvent = (LoggedEvent) loggedObject;
			description = String.format(
					  "<html>"
					+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
					+ "<tr><td><b>hashcode:</b></td><td>&nbsp;&nbsp;[0x%08X]&nbsp;id = %s</td></tr>"
					+ "<tr><td><b>priority:</b></td><td>&nbsp;&nbsp;%d</td></tr>"
					+ "<tr><td><b>key:</b></td><td> [0x%08X]&nbsp;%s</td></tr>"
					+ "<tr><td><b>coalesce&nbsp;time</b>:</td><td>&nbsp;&nbsp;%d ms</td></tr>"
					+ "<tr><td><b>execution&nbsp;time</b>:</td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "<tr><td><b>matching&nbsp;message</b>:</td><td>&nbsp;&nbsp;[0x%08X]</td></tr>"
					+ "<tr><td><b>source&nbsp;code</b>:</td><td>&nbsp;&nbsp;%s</td></tr>"
					+ "</table>"
					+ "</html>",
					loggedEvent.hashCode(), loggedEvent.eventHandle.identifier,
					loggedEvent.eventHandle.priority,
					loggedEvent.eventHandle.key.hashCode(), loggedEvent.eventHandle.key.getClass().getName(),
					loggedEvent.eventHandle.coalesceTimeSpanMs,
					Utility.formatTime(loggedEvent.executionTime),
					loggedEvent.matchingMessage.hashCode(),
					getReflectionDescription(loggedEvent.eventHandle.reflection)
					);
		}
		else if (loggedObject != null)  {
			description = loggedObject.toString();
		}
		return description;
	}

	/**
	 * Log message.
	 * @param logText
	 */
	public static void log(String logText) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return;
		}
		
		// Add new message.
		LoggedMessage log = new LoggedMessage(logText);
		
		if (!LoggingDialog.logList) {
			logTexts.clear();
		}
		
		logTexts.add(log);
		
		// Message limit.
		int extraMessagesCount = logTexts.size() - logLimit;

		// Remove extra messages from the list beginning.
		while (extraMessagesCount-- > 0) {
			logTexts.removeFirst();
		}
		
		// Compile logged messages.
		compileLog();
	}
	
	/**
	 * Log incoming message.
	 * @param incommingMessage
	 */
	public static void log(Message incomingMessage) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return;
		}
		
		synchronized (events) {
			
			// Delegate the call.
			addMessage(incomingMessage);
		}
	}
	
	/**
	 * Log event.
	 * @param message
	 * @param eventHandle
	 * @param executionTime
	 */
	public static void log(Message message, EventHandle eventHandle, long executionTime) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return;
		}
		
		synchronized (events) {
			
			// Get message signal.
			Signal signal = message.signal;
			
			// Get message map.
			LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>> messageMap = events.get(signal);
			
			// Check if the incoming message is missing.
			boolean missingMessage = !messageMap.containsKey(message);
			
			// Add missing incoming message
			if (missingMessage) {
				messageMap = addMessage(message);
			}
			// Limit the number of messages.
			int messageCount = messageMap.size();
			if (messageCount > logLimit) {
				
				// Remove leading entries.
				int messageRemovalCount = messageCount - logLimit;
				HashSet<Message> messagesToRemove = new HashSet<Message>();
				
				for (Message messageToRemove : messageMap.keySet()) {
					if (messageRemovalCount-- <= 0) {
						break;
					}
					messagesToRemove.add(messageToRemove);
				}
				for (Message messageToRemove : messagesToRemove) {
					messageMap.remove(messageToRemove);
				}
			}
			
			// Try to get execution time map.
			LinkedHashMap<Long, LinkedList<LoggedEvent>> timeMap = messageMap.get(message);
			if (timeMap == null) {
				timeMap = new LinkedHashMap<Long, LinkedList<LoggedEvent>>();
				messageMap.put(message, timeMap);
			}
			
			// Try to get event list.
			LinkedList<LoggedEvent> loggedEvents = timeMap.get(executionTime);
			if (loggedEvents == null) {
				loggedEvents = new LinkedList<LoggedEvent>();
				timeMap.put(executionTime, loggedEvents);
			}
			else {
				// Limit the number of logged events.
				int eventCount = loggedEvents.size();
				if (eventCount > eventLimit) {
					
					// Remove leading items.
					int eventRemovalCount = eventCount - eventLimit;
					
					while (--eventRemovalCount > 0) {
						loggedEvents.removeFirst();
					}
				}
			}
			
			// Append new event.
			LoggedEvent event = new LoggedEvent();
			event.eventHandle = eventHandle;
			event.executionTime = executionTime;
			event.matchingMessage = message;
			
			loggedEvents.add(event);
		}
	}
	
	/**
	 * Add incoming message and return logged event object.
	 * @param incomingMessage
	 */
	public static LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>> addMessage(Message incomingMessage) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return null;
		}
		
		// Get message signal.
		Signal signal = incomingMessage.signal;
		
		// Get messages mapped to this signal and append the incoming message.
		LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>> messageMap = events.get(signal);
		if (messageMap == null) {
			messageMap = new LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>();
			events.put(signal, messageMap);
		}
		
		messageMap.put(incomingMessage, null);
		
		return messageMap;
	}
		
	/**
	 * Compile messages.
	 */
	private static void compileLog() {
		
		String resultingText = "";
		
		for (LoggedMessage message : logTexts) {
			resultingText += message.getText() + '\n';
		}
		
		dialog.textLog.setText(resultingText);
	}
	
	/**
	 * Restore event selection.
	 * @param selectedNode
	 */
	private void restoreTreeNodeSelection(JTree tree, DefaultMutableTreeNode rootNode,
			TreePath selectedPath, BiFunction<Object, Object, Boolean> userObjectsEqualLambda) {
		
		// Check the node.
		if (selectedPath == null) {
			return;
		}
		
		// Get last path node.
		Object lastComponent = selectedPath.getLastPathComponent();
		if (!(lastComponent instanceof DefaultMutableTreeNode)) {
			return;
		}
		
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) lastComponent;
		
		// Get event object.
		Object selectedQueuebject = selectedNode.getUserObject();
		
		// Check the event object.
		if (selectedQueuebject == null) {
			return;
		}
		
		// Select found node.
		Enumeration<TreeNode> enumeration = rootNode.depthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			
			// Get the node event object.
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object queueObject = node.getUserObject();
			
			// If the event object matches, select that node.
			if (userObjectsEqualLambda.apply(selectedQueuebject, queueObject)) {
				
				TreeNode [] treeNodes = node.getPath();
				if (treeNodes.length > 0) {
					
					// Set selection path.
					TreePath treePath = new TreePath(treeNodes);
					tree.setSelectionPath(treePath);
					
					// Ensure that the selection is visible.
					tree.makeVisible(treePath);
					return;
				}
			}
		}
	}
	
	/**
	 * Check if event objects area equal.
	 * @param eventObject1
	 * @param eventObject2
	 * @return
	 */
	private static boolean eventObjectsEqual(Object eventObject1, Object eventObject2) {
		
		// Check null objects.
		if (eventObject1 == null) {
			return eventObject2 == null;
		}
		else if (eventObject2 == null) {
			return false;
		}
		
		// Check objects types.
		if (eventObject1.getClass() != eventObject2.getClass()) {
			return false;
		}
		
		// Check signals.
		if (eventObject1 instanceof Signal) {
			Signal signal1 = (Signal) eventObject1;
			Signal signal2 = (Signal) eventObject2;
			return signal1.name().equals(signal2.name());
		}
		// Check messages.
		else if (eventObject1 instanceof Message) {
			Message message1 = (Message) eventObject1;
			Message message2 = (Message) eventObject2;
			return message1 == message2;
		}
		// Check events.
		else if (eventObject1 instanceof LoggedEvent) {
			LoggedEvent event1 = (LoggedEvent) eventObject1;
			LoggedEvent event2 = (LoggedEvent) eventObject2;
			return event1 == event2;
		}
		
		// Perform standard check.
		return eventObject1.equals(eventObject2);
	}
	
	/**
	 * Check if message queue objects area equal.
	 * @param queueObject1
	 * @param queueObject2
	 * @return
	 */
	private boolean queueObjectsEqual(Object queueObject1, Object queueObject2) {
		
		// Check null objects.
		if (queueObject1 == null) {
			return queueObject2 == null;
		}
		else if (queueObject2 == null) {
			return false;
		}
		
		// Check objects types.
		if (queueObject1.getClass() != queueObject2.getClass()) {
			return false;
		}
		
		// Check time stamps.
		if (queueObject1 instanceof String) {
			String timeStamp1 = (String) queueObject1;
			String timeStamp2 = (String) queueObject2;
			return timeStamp1.equals(timeStamp2);
		}
		// Check messages.
		else if (queueObject1 instanceof Message) {
			Message message1 = (Message) queueObject1;
			Message message2 = (Message) queueObject2;
			return message1 == message2;
		}
		
		// Perform standard check.
		return queueObject1.equals(queueObject2);
	}
	
	/**
	 * Add break point object.
	 * @param breakPointObject
	 */
	private void addBreakPoint(Object breakPointObject) {
		
		// Add the input object into breakpoints set and update GUI list that displays the break points.
		breakPointMatchObjects.add(breakPointObject);
		updateBreakPointsList(breakPointMatchObjects);
	}
	
	/**
	 * Update the list of break points.
	 * @param breakPointObjects
	 */
	private void updateBreakPointsList(HashSet<Object> breakPointObjects) {
		
		// Clear the model.
		listBreakPointsModel.clear();
		
		// Add break points.
		listBreakPointsModel.addAll(breakPointObjects);
		
		// Repaint GUI.
		listBreakPoints.updateUI();
	}
	
	/**
	 * Add a message queue snapshot into the list.
	 * @param messageQueueSnapshot
	 * @param timeMoment 
	 */
	public static void addMessageQueueSnapshot(LinkedList<Message> messageQueueSnapshot, Long timeMoment) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return;
		}
		
		synchronized (messageQueueSnapshots) {
			
			// Check input.
			if (messageQueueSnapshot == null || timeMoment == null) {
				return;
			}
			
			// Get new formatted time moment.
			String timeMomentText = Utility.formatTime(timeMoment);

			// Insert new items.
			LinkedList<Message> oldSnapshot = messageQueueSnapshots.get(timeMomentText);
			if (oldSnapshot != null) {
				oldSnapshot.addAll(messageQueueSnapshot);
			}
			else {
				messageQueueSnapshots.put(timeMomentText, messageQueueSnapshot);
			}
			
			// Remove extra snapshots.
			int snapshotCount = messageQueueSnapshots.size();
			Obj<Integer> snapshotsToRemove = new Obj<Integer>(snapshotCount - queueLimit);
			
			if (snapshotsToRemove.ref > 0) {
				HashSet<String> timeStampsToRemove = new HashSet<String>();
				
				messageQueueSnapshots.forEach((timeStamp, snapshot) -> {
					
					if (snapshotsToRemove.ref > 0) {
						timeStampsToRemove.add(timeStamp);
					}
					snapshotsToRemove.ref--;
				});
				
				timeStampsToRemove.forEach(timeStamp -> messageQueueSnapshots.remove(timeStamp));
			}
		}
	}
	
	/**
	 * Update message queue tree.
	 */
	protected void updateMessageQueueTree() {
		
		synchronized (messageQueueSnapshots) {
			
			// Get current selection.
			TreePath selectedPath = treeMessageQueue.getSelectionPath();
			
			// Clear the tree nodes except the root node.
			messageQueueTreeRootNode.removeAllChildren();
			
			// Add snap shots.
			messageQueueSnapshots.entrySet().stream().forEach(entry -> {
				
				// Get time moment.
				String timeMomentText = entry.getKey();
				
				// Add new time node.
				DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode(timeMomentText);
				messageQueueTreeRootNode.add(timeNode);
				
				// Get message queue snapshots.
				LinkedList<Message> messageQueueSnapshot = entry.getValue();
				if (messageQueueSnapshot != null) {
					messageQueueSnapshot.forEach(message -> {
						
						// Add new message node.
						DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(message);
						timeNode.add(messageNode);
					});
				}
			});
			
			// Update tree GUI.
			treeMessageQueue.updateUI();
			// Expand all nodes.
			Utility.expandAll(treeMessageQueue, true);
			// Restore selection.
			restoreTreeNodeSelection(treeMessageQueue, messageQueueTreeRootNode, selectedPath, (object1, object2) -> queueObjectsEqual(object1, object2));
		}
	}
	
	/**
	 * Reload event tree.
	 * @param events
	 */
	private void updateEventTree() {
		
		synchronized (treeEvents) {
			
			// Save current selection.
			TreePath selectedPath = treeEvents.getSelectionPath();
			
			// Clear old tree of logged events.
			eventTreeRootNode.removeAllChildren();
			
			// Add events.
			events.forEach((signal, messageMap) -> {
				
				// Get signal omitted flag.
				boolean omitSignal = checkOmitOrChooseSignals.isSelected();
				
				// Check if the signal is omitted/chosen.
				synchronized (omittedOrChosenSignals) {
					
					// Check if the signal is omitted.
					if (omitSignal) {
						if (omittedOrChosenSignals.contains(signal)) {
							return;
						}
					}
					// Check if the signal is chosen.
					else {
						if (!omittedOrChosenSignals.contains(signal)) {
							return;
						}
					}
				}
				
				// Create signal.
				DefaultMutableTreeNode signalNode = new DefaultMutableTreeNode(signal);
				eventTreeRootNode.add(signalNode);
				
				// Create message nodes.
				if (messageMap != null) {
					messageMap.forEach((message, executionTimeMap) -> {
						
						// Add message node.
						DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(message);
						signalNode.add(messageNode);
						
						// Create execution time nodes.
						if (executionTimeMap != null) {
							executionTimeMap.forEach((executionTime, eventList) -> {
								
								// Get execution time string representation.
								String executionTimeText = Utility.formatTime(executionTime);
								
								// Add time node.
								DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode(executionTimeText);
								messageNode.add(timeNode);
								
								// Create event nodes.
								if (eventList != null) {
									eventList.forEach(event -> {
										
										// Add event node.
										DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
										timeNode.add(eventNode);
									});
								}
							});
						}
						else {
							// Informative node.
							DefaultMutableTreeNode auxNode = new DefaultMutableTreeNode("COALESCED");
							messageNode.add(auxNode);
						}
					});
				}
			});
			
			// Update tree GUI.
			treeEvents.updateUI();
			// Expand all nodes.
			Utility.expandAll(treeEvents, true);
			// Restore selection.
			restoreTreeNodeSelection(treeEvents, eventTreeRootNode, selectedPath, (object1, object2) -> eventObjectsEqual(object1, object2));
		}
	}

	/**
	 * Clear logged message queues.
	 * @return
	 */
	private void onClearQueues() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.messageShallClearLoggedQueues")) {
			return;
		}
		
		synchronized (messageQueueSnapshots) {
			// Clear message queue snapshots.
			messageQueueSnapshots.clear();
		}
		
		synchronized (treeMessageQueue) {
			// Update the queues tree.
			updateMessageQueueTree();
		}
	}
	
	/**
	 * Clear message queue snapshots viewer.
	 */
	private void onClearEvents() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.messageShallClearLoggedEvents")) {
			return;
		}

		synchronized (events) {
			// Clear events.
			events.clear();
		}
		
		synchronized (treeEvents) {
			// Update the events tree.
			updateEventTree();
		}
	}
	
	/**
	 * On omitted/chosen signal click.
	 * @param event 
	 */
	protected void onOmittedOrChosenSignalClick(MouseEvent event) {
		
		// Check double click.
		if (event.getClickCount() != 2) {
			return;
		}
		
		synchronized (omittedOrChosenSignals) {
			
			// Add/remove omitted signal.
			Signal signal = listOmittedOrChosenSignals.getSelectedValue();
			
			if (!omittedOrChosenSignals.contains(signal)) {
				omittedOrChosenSignals.add(signal);
			}
			else {
				omittedOrChosenSignals.remove(signal);
			}
			
			// Redraw list of signals.
			listOmittedOrChosenSignals.updateUI();
		}
	}
	
	/**
	 * On omitted/chosen check box click.
	 */
	protected void onOmitChooseSignals() {
		
		// Redraw the window.
		repaint();
	}
	
	/**
	 * On clear omitted/chosen signals list.
	 */
	protected void onClearOmittedChosen() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.messageLogResetOmittedChosenList")) {
			return;
		}
		
		// Clear the list and update GUI.
		omittedOrChosenSignals.clear();
		listOmittedOrChosenSignals.updateUI();
	}	
	
	/**
	 * Add break point.
	 */
	protected void onAddBreakPoint() {
				
		// Get selected event object.
		TreePath selectedPath = treeEvents.getSelectionPath();
		if (selectedPath == null) {
			return;
		}
		
		// Get tree node break point object.
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
		Object breakPointObject = treeNode.getUserObject();
		
		if (breakPointObject == null) {
			breakPointObject = "";
		}
		
		Class<?> breakPointClass = breakPointObject.getClass();
		String breakPointClassName = breakPointClass.getSimpleName();
		
		// Check available break point type.
		if (!availableBreakPointClasses.contains(breakPointClass)) {
			Utility.show(this, "org.multipage.generator.textCannotAddLogBreakPointClass", breakPointClassName);
			return;
		}
		
		// Ask user.
		if (!Utility.askParam(this, "org.multipage.generator.textShallAddLogBreakPoint", breakPointClassName)) {
			return;
		}
		
		// Add the break point to the list.
		addBreakPoint(breakPointObject);
	}
	
	/**
	 * Clear break points.
	 */
	private void onClearBreakPoints() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.textShouldClearLogBreakPoints")) {
			return;
		}
		
		// Clear break points and update the GUI list.
		breakPointMatchObjects.clear();
		updateBreakPointsList(breakPointMatchObjects);
	}
	
	/**
	 * Set update settings for log views.
	 */
	private void onLogSettings() {
		
		// Open settings.
		Obj<Boolean> isGuiEnabled = new Obj<Boolean>(true);
		
		LoggingSettingsDialog.showDialog(this,
				messageQueueUpdateIntervalMs, eventTreeUpdateIntervalMs, queueLimit, logLimit, eventLimit,
				
				enableGui -> isGuiEnabled.ref = enableGui,

				queuesUpdateIntervalMs -> {
			
					// Check interval value.
					if (queuesUpdateIntervalMs == null) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageQueuesUpdateIntervalNotNumber");
						}
						return false;
					}
					if (queuesUpdateIntervalMs < 100 || queuesUpdateIntervalMs > 10000) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageQueuesUpdateIntervalOutOfRange");
						}
						return false;
					}
					
					// Set interval.
					setQueuesUpdateInterval(queuesUpdateIntervalMs);
					return true;
				},
						
				eventsUpdateIntervalMs -> {
			
					// Check interval value.
					if (eventsUpdateIntervalMs == null) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageEventsUpdateIntervalNotNumber");
						}
						return false;
					}
					if (eventsUpdateIntervalMs < 100 || eventsUpdateIntervalMs > 10000) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageEventsUpdateIntervalOutOfRange");
						}
						return false;
					}
					
					// Set interval.
					setEventUpdateInterval(eventsUpdateIntervalMs);
					return true;
				},
				
				newQueueLimit -> {
					
					// Check queue limit.
					if (newQueueLimit == null) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageQueueLimitNotNumber");
						}
						return false;
					}
					if (newQueueLimit < 0 || newQueueLimit > 100) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageQueueLimitOutOfRange");
						}
						return false;
					}
					
					// Set limit.
					setQueueLimit(newQueueLimit);
					return true;
				},
				
				newMessageLimit -> {
					
					// Check message limit.
					if (newMessageLimit == null) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageMessagesLimitNotNumber");
						}
						return false;
					}
					if (newMessageLimit < 0 || newMessageLimit > 100) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageMessagesLimitOutOfRange");
						}
						return false;
					}
					
					// Set limit.
					setMessageLimit(newMessageLimit);
					return true;
				},
				
				newEventLimit -> {
					
					// Check event limit.
					if (newEventLimit == null) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageEventsLimitNotNumber");
						}
						return false;
					}
					if (newEventLimit < 0 || newEventLimit > 100) {
						if (isGuiEnabled.ref) {
							Utility.show(this, "org.multipage.generator.messageEventsLimitOutOfRange");
						}
						return false;
					}
					
					// Set limit.
					setEventLimit(newEventLimit);
					return true;
				});
	}

	/**
	 * On omit/choose signal menu item clicked.
	 */
	protected void onMenuOmitChooseSignal() {
		
		// Get selected signal.
		TreePath selectedPath = treeEvents.getSelectionPath();
		if (selectedPath == null) {
			return;
		}
		
		int pathNodesCount = selectedPath.getPathCount();
		
		// Reset flag.
		boolean success = false;
		
		// Check path.
		if (pathNodesCount >= 2) {
			
			// Get signal object.
			DefaultMutableTreeNode signalNode = (DefaultMutableTreeNode) selectedPath.getPathComponent(1);
			Object loggedObject = signalNode.getUserObject();
			
			// Add signal to the list.
			if (loggedObject instanceof Signal) {
				Signal signal = (Signal) loggedObject;
				
				omittedOrChosenSignals.add(signal);
				listOmittedOrChosenSignals.updateUI();
				
				// Set the flag.
				success = true;
			}
		}
		
		// If successful, update the tree. If not successful, inform the user.
		if (success) {
			updateEventTree();
		}
		else {
			Utility.show(this, "org.multipage.generator.messageLogCannotOmitOrChooseNode");
		}
	}
	
	/**
	 * On print reflection.
	 */
	protected void onPrintReflection(JTree tree) {
		
		// Get selected input message or logged event.
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		Object userObject = node.getUserObject();
		
		if (userObject instanceof Message) {
			Message message = (Message) userObject;
			System.out.println(message.reflection.toString());
		}
		else if (userObject instanceof LoggedEvent) {
			LoggedEvent event = (LoggedEvent) userObject;
			System.out.println(event.eventHandle.reflection.toString());
		}
		else {
			// Bad selection.
			Utility.show(this, "org.multipage.generator.messageLogNodeHasNoReflection");
		}
	}
	
	/**
	 * On selected message queue object.
	 */
	protected void onMessageQueueObjectSelected() {
		
		// Initialization.
		String nodeDescription = "";
		
		// Get selected message queue object and display its description.
		TreePath path = treeMessageQueue.getSelectionPath();
		if (path != null) {
			
			Object component = path.getLastPathComponent();
			if (component instanceof DefaultMutableTreeNode) {
				
				// Get node description.
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) component;
				nodeDescription = getNodeDescription(node);
			}
		}
		
		// Display the description text.
		textPaneQueueMessage.setText(nodeDescription);
	}
	
	/**
	 * Go to message event.
	 */
	protected void onGoToMessageEvent() {
		
		// Get selected message.
		TreePath path = treeMessageQueue.getSelectionPath();
		if (path == null) {
			Utility.show(this, "org.multipage.generator.messageSelectQueueMessage");
			return;
		}
		
		Object pathComponent = path.getLastPathComponent();
		if (!(pathComponent instanceof DefaultMutableTreeNode)) {
			Utility.show(this, "org.multipage.generator.messageSelectQueueMessage");
			return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathComponent;
		Object nodeObject = node.getUserObject();
		if (!(nodeObject instanceof Message)) {
			Utility.show(this, "org.multipage.generator.messageSelectQueueMessage");
			return;			
		}
		
		Message message = (Message) nodeObject;
		
		// Switch tab.
		tabbedPane.setSelectedComponent(panelEvents);
		
		// Select event.
		Utility.traverseElements(treeEvents, userObject -> treeNode -> parentNode -> {
			if (message.equals(userObject)) {
				
				TreePath selectionPath = new TreePath(treeNode.getPath());
				treeEvents.setSelectionPath(selectionPath);
				treeEvents.makeVisible(selectionPath);
			}
		});
	}
	
	/**
	 * Go to message queue.
	 */
	protected void onGoToMessageQueue() {
		
		// Get selected message.
		TreePath path = treeEvents.getSelectionPath();
		if (path == null) {
			Utility.show(this, "org.multipage.generator.messageSelectEventsMessage");
			return;
		}
		
		Object pathComponent = path.getLastPathComponent();
		if (!(pathComponent instanceof DefaultMutableTreeNode)) {
			Utility.show(this, "org.multipage.generator.messageSelectEventsMessage");
			return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathComponent;
		Object nodeObject = node.getUserObject();
		if (!(nodeObject instanceof Message)) {
			Utility.show(this, "org.multipage.generator.messageSelectEventsMessage");
			return;			
		}
		
		Message message = (Message) nodeObject;
		
		// Switch tab.
		tabbedPane.setSelectedComponent(panelMessageQueue);
		
		// Select event.
		Utility.traverseElements(treeMessageQueue, userObject -> treeNode -> parentNode -> {
			if (message.equals(userObject)) {
				
				TreePath selectionPath = new TreePath(treeNode.getPath());
				treeMessageQueue.setSelectionPath(selectionPath);
				treeMessageQueue.makeVisible(selectionPath);
			}
		});
	}
	
	/**
	 * List or single item switch.
	 * @return
	 */
	private void onListOrSingleItem() {
		
		LoggingDialog.logList = buttonListOrSingleItem.isSelected();
	}
	
	/**
	 * On font size changed.
	 */
	protected void onFontSize() {
		
		// Get font size.
		int selectedIndex = comboFontSize.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}
		
		Integer fontSize = comboFontSize.getItemAt(selectedIndex);
		if (fontSize == null) {
			return;
		}
		
		// Change text area font size.
		Font newFont = textLog.getFont().deriveFont((float) fontSize);
		textLog.setFont(newFont);
		textLog.updateUI();
	}
	
	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Save dialog state.
		saveDialog();
		
		// Reset the flag.
		openedWhenInitialized = false;
	}
	
	/**
	 * Set queues display interval.
	 * @param intervalMs
	 */
	private void setQueuesUpdateInterval(Integer intervalMs) {

		// Set event update interval.
		messageQueueUpdateIntervalMs = intervalMs;
		updateMessageQueueTimer.setDelay(messageQueueUpdateIntervalMs);
		
		// Update queues tree.
		updateMessageQueueTree();
	}
	
	/**
	 * Set event display interval.
	 * @param intervalMs
	 */
	protected void setEventUpdateInterval(int intervalMs) {
		
		// Set event update interval.
		eventTreeUpdateIntervalMs = intervalMs;
		updateEventTreeTimer.setDelay(eventTreeUpdateIntervalMs);
		
		// Update event tree.
		updateEventTree();
	}
	
	/**
	 * Set queue limit.
	 * @param newQueueLimit
	 */
	private void setQueueLimit(Integer newQueueLimit) {
		
		// Set message limit.
		queueLimit = newQueueLimit;
		
		// Update event tree.
		updateMessageQueueTree();
	}
	
	/**
	 * Set messages limit.
	 * @param newMessageLimit
	 */
	private void setMessageLimit(Integer newMessageLimit) {
		
		// Set message limit.
		logLimit = newMessageLimit;
		
		// Update event tree.
		updateEventTree();
	}
	
	/**
	 * Set events limit.
	 * @param newEventLimit
	 */
	private void setEventLimit(Integer newEventLimit) {
		
		// Set event limit.
		eventLimit = newEventLimit;
		
		// Update event tree.
		updateEventTree();
	}
	
	/**
	 * Breakpoint managed by this log window.
	 * @param breakPointObject
	 */
	public static void breakPoint(Object breakPointObject) {
		
		// Check switch.
		if (!isLoggingEnabled()) {
			return;
		}
		
		boolean isBreakPoint = false;
		
		synchronized (breakPointMatchObjects) {
				
			// Check the break point object.
			for (Object breakPointMatch : breakPointMatchObjects) {
			
				if (!breakPointObject.equals(breakPointMatch)) {
					return;
				}
				
				isBreakPoint = true;
			}
		}
		if (!isBreakPoint) {
			return;
		}
		
		// TODO: place your IDE breakpoint at the next line.
		//////////////////////////////////////////////////////
		j.log("BREAK POINT");
		//////////////////////////////////////////////////////
	}
}
