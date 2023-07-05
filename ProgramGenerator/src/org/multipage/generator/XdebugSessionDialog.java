/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 10-06-2023
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HexFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.maclan.server.XdebugCommand;
import org.maclan.server.XdebugListenerSession;
import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * Class for the XdebugSessionDialog dialogs. Use the showDialog(...) method to make this dialog visible.
 * @author user
 *
 */
public class XdebugSessionDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Table font size.
	 */
	private static final int TABLE_FONT_SIZE = 9;
	
	/**
	 * Table header height.
	 */
	private static final int TABLE_HEADER_HEIGHT = 12;
	
	/**
	 * Frame window boundaries.
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Frame controls.
	 */
	private JButton buttonOk;
	private JButton buttonCancel;
	private TextFieldEx textSessionId;
	private JLabel labelSessionId;
	private JLabel labelDebuggedUri;
	private TextFieldEx textDebuggedUri;
	private JLabel labelClient;
	private TextFieldEx textDebugClient;
	private JLabel labelDebugServer;
	private TextFieldEx textDebugServer;
	private JLabel labelProtocolState;
	private TextFieldEx textProtocolState;
	private JLabel labelProtocolFeatures;
	private JTable tableFeatures;
	private JLabel labelTransactions;
	private JScrollPane scrollProtocolTransactions;
	private JTable tableTransactions;
	
	//$hide>>$
	/**
	 * Frame object fields.
	 */
	
	// TODO Herein add new frame object fields.

	//$hide<<$
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = null;
	}
	
	/**
	 * Read states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Write states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Show the dialog window.
	 * @param xdebugSession 
	 * @param parent 
	 * @param parent
	 * @throws Exception 
	 */
	public static void showDialog(Component parent, XdebugListenerSession xdebugSession)
			 throws Exception {
		
		// Create a new frame object and make it visible.
		XdebugSessionDialog dialog = new XdebugSessionDialog(parent);
		dialog.setSession(xdebugSession);
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public XdebugSessionDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		postCreate(); //$hide$
	}

	/**
	 * Initialize dialog components.
	 */
	private void initComponents() {

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		setTitle("org.multipage.titleXdebugSessionDialog");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		labelSessionId = new JLabel("org.multipage.generator.textSessionId:");
		springLayout.putConstraint(SpringLayout.NORTH, labelSessionId, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelSessionId, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSessionId);
		
		textSessionId = new TextFieldEx();
		textSessionId.setFont(new Font("Tahoma", Font.BOLD, 11));
		textSessionId.setHorizontalAlignment(SwingConstants.CENTER);
		textSessionId.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textSessionId, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textSessionId, 6, SpringLayout.EAST, labelSessionId);
		getContentPane().add(textSessionId);
		textSessionId.setColumns(6);
		
		labelDebuggedUri = new JLabel("org.multipage.generator.textDebuggedUri");
		springLayout.putConstraint(SpringLayout.NORTH, labelDebuggedUri, 22, SpringLayout.SOUTH, labelSessionId);
		springLayout.putConstraint(SpringLayout.WEST, labelDebuggedUri, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelDebuggedUri);
		
		textDebuggedUri = new TextFieldEx();
		textDebuggedUri.setFont(new Font("Tahoma", Font.PLAIN, 11));
		springLayout.putConstraint(SpringLayout.NORTH, textDebuggedUri, 0, SpringLayout.NORTH, labelDebuggedUri);
		springLayout.putConstraint(SpringLayout.WEST, textDebuggedUri, 6, SpringLayout.EAST, labelDebuggedUri);
		springLayout.putConstraint(SpringLayout.EAST, textDebuggedUri, -10, SpringLayout.EAST, getContentPane());
		textDebuggedUri.setHorizontalAlignment(SwingConstants.CENTER);
		textDebuggedUri.setEditable(false);
		textDebuggedUri.setColumns(6);
		getContentPane().add(textDebuggedUri);
		
		labelClient = new JLabel("org.multipage.generator.textDebugClient");
		springLayout.putConstraint(SpringLayout.NORTH, labelClient, 24, SpringLayout.SOUTH, labelDebuggedUri);
		springLayout.putConstraint(SpringLayout.WEST, labelClient, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelClient);
		
		textDebugClient = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textDebugClient, 0, SpringLayout.WEST, textDebuggedUri);
		textDebugClient.setHorizontalAlignment(SwingConstants.CENTER);
		textDebugClient.setEditable(false);
		textDebugClient.setFont(new Font("Tahoma", Font.PLAIN, 11));
		springLayout.putConstraint(SpringLayout.EAST, textDebugClient, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, textDebugClient, 0, SpringLayout.NORTH, labelClient);
		getContentPane().add(textDebugClient);
		textDebugClient.setColumns(10);
		
		labelDebugServer = new JLabel("org.multipage.generator.textDebugServer");
		springLayout.putConstraint(SpringLayout.NORTH, labelDebugServer, 23, SpringLayout.SOUTH, labelClient);
		springLayout.putConstraint(SpringLayout.WEST, labelDebugServer, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelDebugServer);
		
		textDebugServer = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textDebugServer, 0, SpringLayout.WEST, textDebuggedUri);
		textDebugServer.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.EAST, textDebugServer, -10, SpringLayout.EAST, getContentPane());
		textDebugServer.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textDebugServer, 0, SpringLayout.NORTH, labelDebugServer);
		getContentPane().add(textDebugServer);
		textDebugServer.setColumns(10);
		
		labelProtocolState = new JLabel("org.multipage.generator.textDebugProtocolState");
		springLayout.putConstraint(SpringLayout.NORTH, labelProtocolState, 28, SpringLayout.SOUTH, labelDebugServer);
		springLayout.putConstraint(SpringLayout.WEST, labelProtocolState, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelProtocolState);
		
		textProtocolState = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textProtocolState, 0, SpringLayout.WEST, textDebuggedUri);
		springLayout.putConstraint(SpringLayout.EAST, textProtocolState, -10, SpringLayout.EAST, getContentPane());
		textProtocolState.setHorizontalAlignment(SwingConstants.CENTER);
		textProtocolState.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textProtocolState, 0, SpringLayout.NORTH, labelProtocolState);
		getContentPane().add(textProtocolState);
		textProtocolState.setColumns(10);
		
		labelProtocolFeatures = new JLabel("org.multipage.generator.textDebugProtocolFeatures");
		springLayout.putConstraint(SpringLayout.NORTH, labelProtocolFeatures, 30, SpringLayout.SOUTH, labelProtocolState);
		springLayout.putConstraint(SpringLayout.WEST, labelProtocolFeatures, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelProtocolFeatures);
		
		JScrollPane scrollProtocolFeatures = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollProtocolFeatures, 5, SpringLayout.SOUTH, labelProtocolFeatures);
		springLayout.putConstraint(SpringLayout.WEST, scrollProtocolFeatures, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollProtocolFeatures, 150, SpringLayout.SOUTH, labelProtocolFeatures);
		springLayout.putConstraint(SpringLayout.EAST, scrollProtocolFeatures, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollProtocolFeatures);
		
		tableFeatures = new JTable();
		scrollProtocolFeatures.setViewportView(tableFeatures);
		
		labelTransactions = new JLabel("org.multipage.generator.textDebugProtocolTransactions");
		springLayout.putConstraint(SpringLayout.NORTH, labelTransactions, 25, SpringLayout.SOUTH, scrollProtocolFeatures);
		springLayout.putConstraint(SpringLayout.WEST, labelTransactions, 0, SpringLayout.WEST, labelSessionId);
		getContentPane().add(labelTransactions);
		
		scrollProtocolTransactions = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollProtocolTransactions, 6, SpringLayout.SOUTH, labelTransactions);
		springLayout.putConstraint(SpringLayout.WEST, scrollProtocolTransactions, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollProtocolTransactions, 163, SpringLayout.SOUTH, labelTransactions);
		springLayout.putConstraint(SpringLayout.EAST, scrollProtocolTransactions, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollProtocolTransactions);
		
		tableTransactions = new JTable();
		scrollProtocolTransactions.setViewportView(tableTransactions);
	}

	/**
	 * Post creation of the dialog controls.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		createFeatureTable();
		createTransactionTable();
		
		// Add post creation function that initialize the dialog.
		loadDialog();
	}
	
	/**
	 * Create feature table.
	 */
	private void createFeatureTable() {

		// Create features table.
        @SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel() {
        	// Disable cell modification.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				// Do nothing.
			}
        };
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerFeatureName"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerFeatureValue"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerFeatureSupported"));
        tableFeatures.setModel(model);
        
        // Create column model.
        TableColumnModel columnModel = tableFeatures.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(50);

        // Create the table renderer.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        tableFeatures.setDefaultRenderer(Object.class, renderer);
        
        // Set the JTable properties.
        tableFeatures.setPreferredScrollableViewportSize(tableFeatures.getPreferredSize());
        
        // Set column editor font size.
        Font tableFont = tableFeatures.getFont().deriveFont(TABLE_FONT_SIZE);
        Utility.setCellEditorFont(tableFeatures, tableFont);
        
        // Set column font size.
        JTableHeader header = tableFeatures.getTableHeader();
        header.setFont(tableFont);
        header.setPreferredSize(new Dimension(0, TABLE_HEADER_HEIGHT));
	}
	

	/**
	 * Create transactions table.
	 */
	private void createTransactionTable() {

		// Create transactions table.
        @SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel() {
        	// Disable cell modification.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				// Do nothing.
			}
        };
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerTransactionId"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerTransactionCommand"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerTransactionArguments"));
        model.addColumn(Resources.getString("org.multipage.generator.textDebugerTransactionHexaData"));
        tableTransactions.setModel(model);
        
        // Create column model.
        TableColumnModel columnModel = tableTransactions.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(200);
        columnModel.getColumn(3).setPreferredWidth(200);
        
        // Create the table renderer.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        tableTransactions.setDefaultRenderer(Object.class, renderer);
        
        // Set the JTable properties.
        tableTransactions.setPreferredScrollableViewportSize(tableTransactions.getPreferredSize());
        
        // Set column editor font size.
        Font tableFont = tableTransactions.getFont().deriveFont(TABLE_FONT_SIZE);
        Utility.setCellEditorFont(tableTransactions, tableFont);
        
        // Set column font size.
        JTableHeader header = tableTransactions.getTableHeader();
        header.setFont(tableFont);
        header.setPreferredSize(new Dimension(0, TABLE_HEADER_HEIGHT));
	}


	/**
	 * Localize texts of the dialog controls.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSessionId);
		Utility.localize(labelDebuggedUri);
		Utility.localize(labelClient);
		Utility.localize(labelDebugServer);
		Utility.localize(labelProtocolState);
		Utility.localize(labelTransactions);
	}
	
	/**
	 * Set frame icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/gui/images/main.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}
	
	/**
	 * Set the controls of the dialog using the session.
	 * @param xdebugSession
	 * @throws Exception 
	 */
	private void setSession(XdebugListenerSession xdebugSession)
			throws Exception {
		
		// Display session ID.
		textSessionId.setText(String.valueOf(xdebugSession.sessionId));
		
		// Display connection string.
		String debuggedUri = xdebugSession.debuggedUri;
		textDebuggedUri.setText(debuggedUri);
		
		// Display debuged client.
		InetSocketAddress socketAddress = (InetSocketAddress) xdebugSession.client.getRemoteAddress();
		String debugClient = socketAddress.getHostString() + ":" + socketAddress.getPort();	
		textDebugClient.setText(debugClient);
		
		// Display debuged server.
		socketAddress = (InetSocketAddress) xdebugSession.server.getLocalAddress();
		String debugServer = socketAddress.getHostString() + ":" + socketAddress.getPort();	
		textDebugServer.setText(debugServer);
		
		// Display protocol state.
		String protocolState = xdebugSession.getProtocolStateText();
		textProtocolState.setText(protocolState);
		
		// Display protocol features.
		DefaultTableModel featuresModel = (DefaultTableModel) tableFeatures.getModel();
		featuresModel.setRowCount(0);
		xdebugSession.features.forEach((name, feature) -> 
			featuresModel.addRow(new Object [] { name, feature.value, feature.supported }));
		
		// Number of Xdebug transactions.
		DefaultTableModel transactionsModel = (DefaultTableModel) tableTransactions.getModel();
		xdebugSession.transactions.forEach((tid, transaction) -> {
			
			XdebugCommand command = transaction.command;
			
			String argumentLine = "";
			String divider = "";
			for (String [] argument : command.arguments) {
				argumentLine += divider + argument[0] + ' ' + argument[1];
				divider = " ";
			}
			
			String hexaData = "";
			if (command.data != null) {
				hexaData = HexFormat.of().formatHex(command.data);
				if (hexaData == null) {
					hexaData = "";
				}
			}
			
			transactionsModel.addRow(new Object [] { tid, command.name, argumentLine, hexaData }); 
		});
	}
	
	/**
	 * The frame confirmed by the user click on the [OK] button.
	 */
	protected void onOk() {
		
		saveDialog();
		dispose();
	}

	/**
	 * The frame has been canceled with the [Cancel] or the [X] button.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Load and set initial state of the frame window.
	 */
	private void loadDialog() {
		
		// Set dialog window boundaries.
		if (bounds != null && !bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
	}
	
	/**
	 * Save current state of the frame window.
	 */
	private void saveDialog() {
		
		// Save current dialog window boundaries.
		bounds = getBounds();
	}
	public JTable getTableTransactions() {
		return tableTransactions;
	}
}
