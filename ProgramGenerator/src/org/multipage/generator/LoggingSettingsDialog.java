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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * 
 * @author vakol
 *
 */
public class LoggingSettingsDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	//$hide>>$
	
	/**
	 * Lambda call backs.
	 */
	private Function<Integer, Boolean> setUpdateIntervalLambda = null;
	
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
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Initialize this dialog.
	 */
	public static void showDialog(Component parent, Function<Integer, Boolean> setUpdateIntervalLambda) {
		
		Window parentWindow = Utility.findWindow(parent);
		
		LoggingSettingsDialog dialog = new LoggingSettingsDialog(parentWindow);
		
		dialog.setUpdateIntervalLambda = setUpdateIntervalLambda;
		
		dialog.setVisible(true);
	}

	//$hide<<$
	
	/**
	 * Components.
	 */
	private JTextField textUpdateInterval;
	
	/**
	 * Create the dialog.
	 */
	public LoggingSettingsDialog(Window parentWindow) {
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
		
		JButton buttonOk = new JButton("New button");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.EAST, getContentPane());
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		getContentPane().add(buttonOk);
		
		JLabel labelUpdateItnterval = new JLabel("New label");
		springLayout.putConstraint(SpringLayout.NORTH, labelUpdateItnterval, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelUpdateItnterval, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelUpdateItnterval);
		
		textUpdateInterval = new JTextField();
		textUpdateInterval.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onSetUpdateInterval();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textUpdateInterval, 0, SpringLayout.NORTH, labelUpdateItnterval);
		springLayout.putConstraint(SpringLayout.WEST, textUpdateInterval, 6, SpringLayout.EAST, labelUpdateItnterval);
		getContentPane().add(textUpdateInterval);
		textUpdateInterval.setColumns(10);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		loadDialog();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
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
		
		textUpdateInterval.setBorder(new LineBorder(Color.BLACK));
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
	 * On update interval.
	 */
	protected void onSetUpdateInterval() {
		
		// Get update interval.
		String intervalText = textUpdateInterval.getText();
		
		// Set the interval.
		Integer intervalMs = null;
		try {
			intervalMs = Integer.parseInt(intervalText);
		}
		catch (Exception e) {
		}
		Boolean success = setUpdateIntervalLambda.apply(intervalMs);
		
		// Set control border.
		textUpdateInterval.setBorder(new LineBorder(success ? Color.BLACK : Color.RED));
	}
}		