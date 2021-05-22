/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 24-03-2021
 *
 */
package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
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

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
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

import org.multipage.generator.ConditionalEvents.Message;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

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
	 * Events tree update interval in milliseconds.
	 */
	private static final int treeUpdateIntervalMs = 3000;
	
	/**
	 * Bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Splitter position.
	 */
	private static Integer eventsWindowSplitter;
	
	/**
	 * Selected tab.
	 */
	private static Integer selectedTab;
	
	/**
	 * Dark green color constant.
	 */
	private static final Color DARK_GREEN = new Color(0, 128, 0);

	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		eventsWindowSplitter = -1;
		selectedTab = 0;
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
		omittedOrChosenSignals = Utility.readInputStreamObject(inputStream, HashSet.class);
		eventsWindowSplitter = inputStream.readInt();
		selectedTab = inputStream.readInt();
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(omittedOrChosenSignals);
		outputStream.writeInt(eventsWindowSplitter);
		outputStream.writeInt(selectedTab);
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
		 * Incoming message.
		 */
		public Message message = null;
		
		/**
		 * Error flags.
		 */
		public Long executionTime = null;
	}
	
	/**
	 * Logged messages.
	 */
	private static LinkedList<LoggedMessage> messages = new LinkedList<LoggedMessage>();
	
	/**
	 * Logged events.
	 * Maps: Signal -> Message -> Execution time -> Event
	 */
	private static LinkedHashMap<Signal, LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>> events = new LinkedHashMap<Signal, LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>>();
	
	/**
	 * Omitted signals.
	 */
	private static HashSet<Signal> omittedOrChosenSignals = new HashSet<Signal>();
	
	// $hide>>$
	/**
	 * Singleton dialog object.
	 */
	private static LoggingDialog dialog = null;

	/**
	 * Message limit.
	 */
	private static final int messageLimit = 20;
	
	/**
	 * Initialize this dialog.
	 */
	public static void initialize(Component parent) {
		
		Window parentWindow = Utility.findWindow(parent);
		dialog = new LoggingDialog(parentWindow);
	}
	
	/**
	 * Tree view for logged events.
	 */
	private JTree tree;
	
	/**
	 * Tree model for displaying logged events.
	 */
	private DefaultTreeModel treeModel;
	
	/**
	 * Root node of the events tree.
	 */
	private DefaultMutableTreeNode treeRootNode;
	
	/**
	 * Update tree timer.
	 */
	private Timer updateTimer;

	//$hide<<$
	
	/**
	 * Components.
	 */
	protected JTextArea textAreaDescription;
	private JTabbedPane tabbedPane;
	private JList<Signal> listOmittedSignals;
	private DefaultListModel<Signal> listModelOmittedSignals;
	private JEditorPane editorPaneDescription;
	private JSplitPane splitPaneEvents;
	private JCheckBox checkOmitOrChooseSignals;
	private JToolBar toolBar;
	
	/**
	 * Show dialog.
	 * @param parent
	 */
	public static void showDialog(Component parent) {
		
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 */
	public LoggingDialog(Window parentWindow) {
		super(parentWindow, ModalityType.MODELESS);
		
		initComponents();
		postCreate(); //$hide$
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
		setTitle("Logging dialog");
		setBounds(100, 100, 557, 471);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 3, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 3, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -3, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -3, SpringLayout.EAST, getContentPane());
		getContentPane().add(tabbedPane);
		
		textAreaDescription = new JTextArea();
		
		JScrollPane scrollPaneMessages = new JScrollPane();
		scrollPaneMessages.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textLoggedMessages", null, scrollPaneMessages, null);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneMessages, 332, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneMessages, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneMessages, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneMessages, -10, SpringLayout.EAST, getContentPane());
		scrollPaneMessages.setViewportView(textAreaDescription);
		
		splitPaneEvents = new JSplitPane();
		splitPaneEvents.setResizeWeight(0.8);
		splitPaneEvents.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tabbedPane.addTab("org.multipage.generator.textLoggedConditionalEvents", null, splitPaneEvents, null);
		
		JScrollPane scrollPaneEvents = new JScrollPane();
		splitPaneEvents.setLeftComponent(scrollPaneEvents);
		
		tree = new JTree();
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(selectionModel);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onEventSelection();
			}
		});
		scrollPaneEvents.setViewportView(tree);
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		scrollPaneEvents.setColumnHeaderView(toolBar);
		
		JScrollPane scrollPaneDescription = new JScrollPane();
		splitPaneEvents.setRightComponent(scrollPaneDescription);
		
		editorPaneDescription = new JEditorPane();
		editorPaneDescription.setContentType("text/html");
		editorPaneDescription.setEditable(false);
		scrollPaneDescription.setViewportView(editorPaneDescription);
		
		JScrollPane scrollPaneOmittedSignals = new JScrollPane();
		scrollPaneOmittedSignals.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textOmittedSignals", null, scrollPaneOmittedSignals, null);
		
		listOmittedSignals = new JList();
		listOmittedSignals.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onOmittedOrChosenSignalClick(e);
			}
		});
		scrollPaneOmittedSignals.setViewportView(listOmittedSignals);
		
		JPanel panel = new JPanel();
		scrollPaneOmittedSignals.setColumnHeaderView(panel);
		
		checkOmitOrChooseSignals = new JCheckBox("org.multipage.generator.textOmitSignals");
		checkOmitOrChooseSignals.setSelected(true);
		checkOmitOrChooseSignals.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				onOmitChooseSignals();
			}
		});
		panel.add(checkOmitOrChooseSignals);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		createEventToolBar();
		localize();
		setIcons();
		loadDialog();
		createEventTree();
		createOmittedSignalList();
	}
	
	/**
	 * Creates a tool bar with buttons that run actions on logged events tree.
	 */
	private void createEventToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/close_all.png", "org.multipage.generator.tooltipClearLoggedEvents", () -> onClearEvents());
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(tabbedPane);
		Utility.localize(checkOmitOrChooseSignals);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		
		// Set tree icons.
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		if (eventsWindowSplitter != -1) {
			splitPaneEvents.setDividerLocation(eventsWindowSplitter);
		}
		else {
			splitPaneEvents.setDividerLocation(0.8);
		}
		tabbedPane.setSelectedIndex(selectedTab);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		eventsWindowSplitter = splitPaneEvents.getDividerLocation();
		selectedTab = tabbedPane.getSelectedIndex();
	}
	
	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Save dialog state.
		saveDialog();
	}
	
	/**
	 * Create a tree with categorized events.
	 */
	private void createEventTree() {
		
		// Create and set tree model.
		treeRootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(treeRootNode);
		tree.setModel(treeModel);
		
		// Set tree node renderer.
		tree.setCellRenderer(new TreeCellRenderer() {
			
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
						renderer.setText(String.format("[0x%08X] %s message", message.hashCode(), Utility.formatTime(message.receiveTime)));
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
		updateTimer = new Timer(treeUpdateIntervalMs, event -> {
			
			SwingUtilities.invokeLater(() -> {
				synchronized (events) {
					updateEventTree(events);
				}
			});
		});
		updateTimer.start();
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
		
		listOmittedSignals.setModel(listModelOmittedSignals);
		
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
		listOmittedSignals.setCellRenderer(renderer);
	}
	
	/**
	 * On omitted/chosen check box click.
	 */
	protected void onOmitChooseSignals() {
		
		// Redraw the window.
		repaint();
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
			Signal signal = listOmittedSignals.getSelectedValue();
			
			if (!omittedOrChosenSignals.contains(signal)) {
				omittedOrChosenSignals.add(signal);
			}
			else {
				omittedOrChosenSignals.remove(signal);
			}
			
			// Redraw list of signals.
			listOmittedSignals.updateUI();
		}
	}
	
	/**
	 * On event selection.
	 * @param e 
	 */
	protected void onEventSelection() {
		
		synchronized (tree) {
			
			// Get selected tree item.
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}
			
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
		Object eventPart = node.getUserObject();
		String description = getEventPartDescription(eventPart);

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
	 * Get event part description.
	 * @param eventPart
	 * @return
	 */
	private String getEventPartDescription(Object eventPart) {
		
		String description = "";
		
		// Get signal description.
		if (eventPart instanceof Signal) {
			
			Signal signal = (Signal) eventPart;
			description = String.format(
					"<html>"
					+ "<b>signal</b>: %s<br>"
					+ "<b>priority</b>: %d<br>"
					+ "<b>types</b>: %s<br>"
					+ "</html>",
					signal.name(),
					signal.getPriority(),
					getSignalTypesDescription(signal)
					);
		}
		else if (eventPart instanceof Message) {
			
			Message message = (Message) eventPart;
			description = String.format(
					"<html>"
					+ "<b>signal</b>: %s<br>"
					+ "<b>[hashcode] execution time</b>: [0x%08X] %s<br>"
					+ "<b>source</b>: %s<br>"
					+ "<b>target</b>: %s<br>"
					+ "<b>info</b>: %s<br>"
					+ "<b>+infos</b>: %s<br>"
					+ "<b>code</b>: %s<br>"
					+ "</html>",
					message.signal.name(),
					message.hashCode(), Utility.formatTime(message.receiveTime),
					getObjectDescription(message.source),
					getObjectDescription(message.target),
					getDataDescription(message.relatedInfo),
					getArrayDescription(message.additionalInfos),
					getReflectionDescription(message.reflection)
					);
		}
		else if (eventPart instanceof LoggedEvent) {
			
			LoggedEvent loggedEvent = (LoggedEvent) eventPart;
			description = String.format(
					"<html>"
					+ "<b>[hashcode] event"
					+ "<b>handle ID</b>: %s<br>"
					+ "<b>coalesce</b>: %d ms<br>"
					+ "<b>code</b>: %s<br>"
					+ "</html>",
					loggedEvent.hashCode(),
					loggedEvent.eventHandle.identifier,
					loggedEvent.eventHandle.coalesceTimeSpanMs,
					getReflectionDescription(loggedEvent.eventHandle.reflection)
					);
		}
		else if (eventPart != null)  {
			description = eventPart.toString();
		}
		return description;
	}

	/**
	 * Log message.
	 * @param messageText
	 */
	public static void log(String messageText) {
		
		// Add new message.
		LoggedMessage message = new LoggedMessage(messageText);
		messages.add(message);
		
		// Message limit.
		int extraMessagesCount = messages.size() - messageLimit;

		// Remove extra messages from the list beginning.
		while (extraMessagesCount-- > 0) {
			messages.removeFirst();
		}
		
		// Compile messages.
		compileMessages();
	}
	
	/**
	 * Log incoming message.
	 * @param incommingMessage
	 */
	public static void log(Message incomingMessage) {
		
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
		
		// Get message signal.
		Signal signal = message.signal;
		
		// Get message map.
		LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>> messageMap = events.get(signal);
		
		// Check if the incoming message is missing.
		boolean missingMessage = messageMap.containsKey(message);
		message.userObject = new Object [] { "missingMessage", true };
		
		// Add missing incoming message
		if (missingMessage) {
			messageMap = addMessage(message);
		}
		
		// Try to get execution time map.
		LinkedHashMap<Long, LinkedList<LoggedEvent>> timeMap = messageMap.get(messageMap);
		if (timeMap == null) {
			timeMap = new LinkedHashMap<Long, LinkedList<LoggedEvent>>();
			messageMap.put(message, timeMap);
		}
		
		// Try to get events list.
		LinkedList<LoggedEvent> events = timeMap.get(executionTime);
		if (events == null) {
			events = new LinkedList<LoggedEvent>();
			timeMap.put(executionTime, events);
		}
		
		// Append new event.
		LoggedEvent event = new LoggedEvent();
		event.message = message;
		event.eventHandle = eventHandle;
		event.executionTime = executionTime;
		
		events.add(event);
	}
	
	/**
	 * Add incoming message and return logged event object.
	 * @param incomingMessage
	 */
	public static LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>> addMessage(Message incomingMessage) {
		
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
	private static void compileMessages() {
		
		String resultingText = "";
		
		for (LoggedMessage message : messages) {
			resultingText += message.getText() + '\n';
		}
		
		dialog.textAreaDescription.setText(resultingText);
	}
	
	/**
	 * Restore event selection.
	 * @param selectedEventObject
	 */
	private void restoreEventSelection(DefaultMutableTreeNode selectedNode) {
		
		// Check the node.
		if (selectedNode == null) {
			return;
		}
		
		// Get event object.
		Object selectedEventObject = selectedNode.getUserObject();
		
		// Check the event object.
		if (selectedEventObject == null) {
			return;
		}
		
		// Select found node.
		Enumeration<TreeNode> enumeration = treeRootNode.depthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			
			// Get the node event object.
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object eventObject = node.getUserObject();
			
			// If the event object matches, select that node.
			if (eventObjectsEqual(selectedEventObject, eventObject)) {
				
				TreeNode [] treeNodes = node.getPath();
				if (treeNodes.length > 0) {
					
					// Set selection path.
					TreePath treePath = new TreePath(treeNodes);
					tree.setSelectionPath(treePath);
					
					return;
				}
			}
		}
	}
	
	/**
	 * Check if the vents object area equal.
	 * @param eventObject1
	 * @param eventObject2
	 * @return
	 */
	private boolean eventObjectsEqual(Object eventObject1, Object eventObject2) {
		
		// Check null objects.
		if (eventObject1 == null) {
			return eventObject2 == null;
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
		
		// Perform standard check.
		return eventObject1.equals(eventObject2);
	}

	/**
	 * Reload event tree.
	 * @param events
	 */
	private void updateEventTree(LinkedHashMap<Signal, LinkedHashMap<Message, LinkedHashMap<Long, LinkedList<LoggedEvent>>>> events) {
		
		synchronized (tree) {
			
			// Save current selection.
			DefaultMutableTreeNode selectedNode = null;
			TreePath selectedPath = tree.getSelectionPath();
			
			if (selectedPath != null) {
				selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
			}
			
			// Clear old tree of logged events.
			treeRootNode.removeAllChildren();
			
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
				treeRootNode.add(signalNode);
				
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
					});
				}
			});
			
			// Reload the tree model.
			treeModel.reload(treeRootNode);
			// Expand all nodes.
			Utility.expandAll(tree, true);
			// Restore selection.
			restoreEventSelection(selectedNode);
		}
	}
	
	/**
	 * Clear logged events.
	 */
	private void onClearEvents() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.tooltipShallClearLoggedEvents")) {
			return;
		}
		
		synchronized (events) {
			// Clear events.
			events.clear();
		}
		
		synchronized (tree) {
			// Update the events tree.
			updateEventTree(events);
		}
	}
}
