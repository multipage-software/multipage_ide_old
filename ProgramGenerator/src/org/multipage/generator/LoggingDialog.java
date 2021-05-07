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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.multipage.generator.ConditionalEvents.Message;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
		omittedSignals = Utility.readInputStreamObject(inputStream, HashSet.class);
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(omittedSignals);
	}
	
	/**
	 * String constants.
	 */
	private static String scheduledNodesCaption = null;
	private static String invokedNodesCaption = null;
	
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
		 * Incoming message.
		 */
		public Message incomingMessage = null;
		
		/**
		 * Scheduled event variants. 
		 */
		public LinkedList<ScheduledEvent> scheduledEventVariants = null;
		
		/**
		 * Invoked event variants.
		 */
		public LinkedList<ScheduledEvent> invokedEventVariants = null;
		
		/**
		 * Error flags.
		 */
		public boolean missingIncomingMessage = false;
		public boolean missingScheduledEvent = false;
	}
	
	/**
	 * Logged messages.
	 */
	private static LinkedList<LoggedMessage> messages = new LinkedList<LoggedMessage>();
	
	/**
	 * Logged events.
	 */
	private static LinkedHashMap<Signal, LinkedList<LoggedEvent>> events = new LinkedHashMap<Signal, LinkedList<LoggedEvent>>();
	
	/**
	 * Omitted signals.
	 */
	private static HashSet<Signal> omittedSignals = new HashSet<Signal>();
	
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
	public static void initialize() {
		
		dialog = new LoggingDialog();
	}
	
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
	protected JTextArea textArea;
	private JTabbedPane tabbedPane;
	private JTree tree;
	private JList<Signal> listOmittedSignals;
	private DefaultListModel<Signal> listModelOmittedSignals;
	
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
	public LoggingDialog() {

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
		
		textArea = new JTextArea();
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 3, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 3, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -3, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -3, SpringLayout.EAST, getContentPane());
		getContentPane().add(tabbedPane);
		
		JScrollPane scrollPaneMessages = new JScrollPane();
		scrollPaneMessages.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textLoggedMessages", null, scrollPaneMessages, null);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneMessages, 332, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneMessages, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneMessages, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneMessages, -10, SpringLayout.EAST, getContentPane());
		scrollPaneMessages.setViewportView(textArea);
		
		JScrollPane scrollPaneEvents = new JScrollPane();
		scrollPaneEvents.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textLoggedConditionalEvents", null, scrollPaneEvents, null);
		
		tree = new JTree();
		scrollPaneEvents.setViewportView(tree);
		
		JScrollPane scrollPaneOmittedSignals = new JScrollPane();
		scrollPaneOmittedSignals.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textOmittedSignals", null, scrollPaneOmittedSignals, null);
		
		listOmittedSignals = new JList();
		listOmittedSignals.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onOmittedSignalClick(e);
			}
		});
		scrollPaneOmittedSignals.setViewportView(listOmittedSignals);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		loadDialog();
		createTree();
		createOmittedSignalList();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(tabbedPane);
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
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
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
	private void createTree() {
		
		// Set string constants.
		if (scheduledNodesCaption == null) {
			scheduledNodesCaption = Resources.getString("org.multipage.generator.textLoggedScheduledEvents");
		}
		if (invokedNodesCaption == null) {
			invokedNodesCaption = Resources.getString("org.multipage.generator.textLoggedInvokedEvents");
		}
		
		// Create and set tree model.
		treeRootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(treeRootNode);
		tree.setModel(treeModel);
		
		// Create update timer.
		updateTimer = new Timer(treeUpdateIntervalMs, event -> {
			
			SwingUtilities.invokeLater(() -> {
				synchronized (events) {
					updateTree(events);
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
				if (omittedSignals.contains(signal)) {
					renderer.setForeground(Color.RED);
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
	 * On omitted signals click.
	 * @param event 
	 */
	protected void onOmittedSignalClick(MouseEvent event) {
		
		// Check double click.
		if (event.getClickCount() != 2) {
			return;
		}
		
		synchronized (omittedSignals) {
			
			// Add/remove omitted signal.
			Signal signal = listOmittedSignals.getSelectedValue();
			
			if (!omittedSignals.contains(signal)) {
				omittedSignals.add(signal);
			}
			else {
				omittedSignals.remove(signal);
			}
			
			// Redraw list of signals.
			listOmittedSignals.updateUI();
		}
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
	 * Add incoming message and return logged event object.
	 * @param incommingMessage
	 */
	public static LoggedEvent addMessage(Message incomingMessage) {
		
		// Get message signal.
		Signal signal = incomingMessage.signal;
		
		// Create new logged event.
		LoggedEvent loggedEvent = new LoggedEvent();
		loggedEvent.incomingMessage = incomingMessage;
		
		// Get events mapped to this signal and append a new item.
		LinkedList<LoggedEvent> loggedEvents = events.get(signal);
		if (loggedEvents == null) {
			loggedEvents = new LinkedList<LoggedEvent>();
			events.put(signal, loggedEvents);
		}
		
		loggedEvents.add(loggedEvent);
		return loggedEvent;
	}
	
	/**
	 * Log scheduled event.
	 * @param incommingMessage
	 * @param isInvoked
	 */
	public static void log(ScheduledEvent scheduledEvent, boolean isInvoked) {
		
		synchronized (events) {
			
			// Get incoming message.
			Message incomingMessage = scheduledEvent.message;
			
			// Get message signal.
			Signal signal = incomingMessage.signal;
			
			// Get events mapped to this signal.
			LinkedList<LoggedEvent> loggedEvents = events.get(signal);
			if (loggedEvents == null) {
				loggedEvents = new LinkedList<LoggedEvent>();
				events.put(signal, loggedEvents);
			}
			
			// Initialize flags.
			Obj<Boolean> missingIncommingMessage = new Obj<Boolean>(true);
			Obj<Boolean> missingScheduledEvent = new Obj<Boolean>(true);
			
			// Try to find incoming message between logged events and append new event.
			loggedEvents.stream().parallel()
				.filter(event -> event.incomingMessage.equals(incomingMessage))
				.forEach(foundLoggedEvent -> {
					
					// Add this event variant.
					if (!isInvoked) {
						if (foundLoggedEvent.scheduledEventVariants == null) {
							foundLoggedEvent.scheduledEventVariants = new LinkedList<ScheduledEvent>();
						}
						foundLoggedEvent.scheduledEventVariants.add(scheduledEvent);
						
						// Reset the flags.
						missingIncommingMessage.ref = false;
						missingScheduledEvent.ref = false;
					}
					else {
						if (foundLoggedEvent.invokedEventVariants == null) {
							foundLoggedEvent.invokedEventVariants = new LinkedList<ScheduledEvent>();
						}
						foundLoggedEvent.invokedEventVariants.add(scheduledEvent);
						
						// Reset the flag.
						missingIncommingMessage.ref = false;
					}
				});
			
			// If the incoming message was not found, add new message.
			if (missingIncommingMessage.ref) {
				LoggedEvent loggedEvent = addMessage(incomingMessage);
				
				loggedEvent.missingIncomingMessage = true;
				loggedEvent.missingScheduledEvent = missingScheduledEvent.ref;
			}
		}
	}
	
	/**
	 * Compile messages.
	 */
	private static void compileMessages() {
		
		String resultingText = "";
		
		for (LoggedMessage message : messages) {
			resultingText += message.getText() + '\n';
		}
		
		dialog.textArea.setText(resultingText);
	}
	
	/**
	 * Reload events tree.
	 * @param events
	 */
	private void updateTree(LinkedHashMap<Signal, LinkedList<LoggedEvent>> events) {
		
		// Clear old tree of logged events.
		treeRootNode.removeAllChildren();
		
		// Add events.
		events.forEach((signal, loggedEvents) -> {
			
			// Check if the signal is omitted.
			synchronized (omittedSignals) {
				if (omittedSignals.contains(signal)) {
					return;
				}
			}
			
			// Create signal node and insert it into the root node.
			DefaultMutableTreeNode signalNode = new DefaultMutableTreeNode(signal);
			treeRootNode.add(signalNode);
			
			// Create message and event nodes in the signal node.
			loggedEvents.forEach(loggedEvent -> {
				
				// Add message node.
				Message incommingMessage = loggedEvent.incomingMessage;
				DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(incommingMessage);
				signalNode.add(messageNode);
				
				// Add scheduled event variants.
				DefaultMutableTreeNode scheduledNodes = new DefaultMutableTreeNode(scheduledNodesCaption);
				messageNode.add(scheduledNodes);
				if (loggedEvent.scheduledEventVariants != null) {
					loggedEvent.scheduledEventVariants.forEach(event -> {
						
						// Create event node and insert it into the message node.
						DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
						scheduledNodes.add(eventNode);
					});
				}
				
				// Add invoked event variants.
				DefaultMutableTreeNode invokedNodes = new DefaultMutableTreeNode(invokedNodesCaption);
				messageNode.add(invokedNodes);
				if (loggedEvent.invokedEventVariants != null) {
					loggedEvent.invokedEventVariants.forEach(event -> {
						
						// Create event node and insert it into the message node.
						DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
						invokedNodes.add(eventNode);
					});
				}
			});
		});
		
		// Reload the tree model.
		treeModel.reload(treeRootNode);
		// Expand all nodes.
		Utility.expandAll(tree, true);
	}
}
