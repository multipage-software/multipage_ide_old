/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 01-06-2024
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.maclan.server.DebugWatchItem;
import org.maclan.server.DebugWatchItemType;
import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;

/**
 * The dialog enables to select debugger watch list item.
 * @author user
 *
 */
public class DebugWatchDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog window boundaries.
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Selected type.
	 */
	private static int selectedType = 0;
	
	/**
	 * Dialog controls.
	 */
	private JButton buttonOk;
	private JButton buttonCancel;
	private TextFieldEx textName;
	private JComboBox<DebugWatchItemType> comboType;
	private JLabel labelType;
	private JLabel labelName;

	//$hide>>$
	/**
	 * Dialog object fields.
	 */
	
	private DebugWatchItem watchItem;
	//$hide<<$
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = null;
		selectedType = 0;
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
		selectedType = inputStream.readInt();
	}

	/**
	 * Write states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(selectedType);
	}
	
	/**
	 * Show the dialog window.
	 * @param parent
	 */
	public static DebugWatchItem showDialog(Component parent) {
		
		// Create a new frame object and make it visible.
		DebugWatchDialog dialog = new DebugWatchDialog(parent);
		dialog.setVisible(true);
		return dialog.watchItem;
	}
	
	/**
	 * Create the dialog.
	 */
	public DebugWatchDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		postCreate(); //$hide$
	}

	/**
	 * Initialize dialog components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(400, 220));
		setPreferredSize(new Dimension(450, 300));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		setTitle("org.multipage.generator.titleDebugWatchDialog");
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
		
		labelType = new JLabel("org.multipage.generator.textDebugWatchItemType");
		springLayout.putConstraint(SpringLayout.NORTH, labelType, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelType, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelType);
		
		comboType = new JComboBox<>();
		springLayout.putConstraint(SpringLayout.NORTH, comboType, 3, SpringLayout.SOUTH, labelType);
		springLayout.putConstraint(SpringLayout.WEST, comboType, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboType, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(comboType);
		
		labelName = new JLabel("org.multipage.generator.textDebugWatchItemName");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 6, SpringLayout.SOUTH, comboType);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 0, SpringLayout.WEST, labelType);
		getContentPane().add(labelName);
		
		textName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textName, 3, SpringLayout.SOUTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textName, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textName, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textName);
		textName.setColumns(10);
	}

	/**
	 * Post creation of the dialog controls.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		loadWatchTypes();
		loadDialog();
	}

	/**
	 * Localize texts of the dialog controls.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelType);
		Utility.localize(labelName);
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
	 * The dialog confirmed by the user click on the [OK] button.
	 */
	protected void onOk() {
		
		// Create output watch item.
		watchItem = createWatchItem();
		
		saveDialog();
		dispose();
	}

	/**
	 * The frame has been canceled with the [Cancel] or the [X] button.
	 */
	protected void onCancel() {
		
		watchItem = null;
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Load watch types.
	 */
	private void loadWatchTypes() {
		
		DebugWatchItemType [] types = DebugWatchItemType.values();
		for (DebugWatchItemType type : types) {
			
			comboType.addItem(type);
		}
	}
	
	/**
	 * Create and return watch item object.
	 * @return
	 */
	private DebugWatchItem createWatchItem() {
		
		// Get watch item type.
		Object selectedObject = comboType.getSelectedItem();
		if (!(selectedObject instanceof DebugWatchItemType)) {
			return null;
		}
		DebugWatchItemType itemType = (DebugWatchItemType) selectedObject;
		
		// Get watch item name.
		String itemName = textName.getText();
		if (itemName == null || itemName.isEmpty()) {
			return null;
		}
		
		DebugWatchItem watchItem = new DebugWatchItem(itemName, itemType);
		return watchItem;
	}
	
	/**
	 * Load and set initial state of the frame window.
	 */
	private void loadDialog() {
		
		// Set dialog window boundaries and control values.
		if (bounds != null && !bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			bounds = new Rectangle(350, 200);
			setBounds(bounds);
			Utility.centerOnScreen(this);
		}
		
		comboType.setSelectedIndex(selectedType);
	}
	
	/**
	 * Save current state of the frame window.
	 */
	private void saveDialog() {
		
		// Save current dialog window boundaries and control values
		bounds = getBounds();
		selectedType = comboType.getSelectedIndex();
	}
}
