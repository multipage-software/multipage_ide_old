/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.maclan.SlotType;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
public class UserSlotInput extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JTextField textSlotAlias;
	private JLabel labelSlotName;
	private JCheckBox checkInheritable;
	private JComboBox<SlotType> comboSlotType;
	private JLabel labelType;

	/**
	 * Show dialog.
	 * @param parent
	 * @param isInheritable 
	 * @return
	 */
	public static boolean showDialog(Component parent, Obj<String> slotAlias,
			Obj<SlotType> slotType, Obj<Boolean> isInheritable) {
		
		UserSlotInput dialog = new UserSlotInput(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			slotAlias.ref = dialog.textSlotAlias.getText();
			slotType.ref = (SlotType) dialog.comboSlotType.getSelectedItem();
			isInheritable.ref = dialog.checkInheritable.isSelected();
			
			return true;
		}
		return false;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public UserSlotInput(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setResizable(false);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textUserSlotNameAndType");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 346, 260);
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
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelSlotName = new JLabel("org.multipage.generator.messageInputNewUserSlotName");
		springLayout.putConstraint(SpringLayout.NORTH, labelSlotName, 32, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelSlotName, 30, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSlotName);
		
		textSlotAlias = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textSlotAlias, 6, SpringLayout.SOUTH, labelSlotName);
		springLayout.putConstraint(SpringLayout.WEST, textSlotAlias, 0, SpringLayout.WEST, labelSlotName);
		springLayout.putConstraint(SpringLayout.EAST, textSlotAlias, -30, SpringLayout.EAST, getContentPane());
		getContentPane().add(textSlotAlias);
		textSlotAlias.setColumns(10);
		{
			checkInheritable = new JCheckBox("org.multipage.generator.textUserProviderInheritable");
			springLayout.putConstraint(SpringLayout.NORTH, checkInheritable, 78, SpringLayout.SOUTH, textSlotAlias);
			springLayout.putConstraint(SpringLayout.WEST, checkInheritable, 100, SpringLayout.WEST, getContentPane());
			getContentPane().add(checkInheritable);
		}
		
		comboSlotType = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, comboSlotType, 0, SpringLayout.WEST, checkInheritable);
		comboSlotType.setPreferredSize(new Dimension(120, 20));
		getContentPane().add(comboSlotType);
		
		labelType = new JLabel("org.multipage.generator.textSlotType");
		springLayout.putConstraint(SpringLayout.NORTH, comboSlotType, 6, SpringLayout.SOUTH, labelType);
		springLayout.putConstraint(SpringLayout.NORTH, labelType, 20, SpringLayout.SOUTH, textSlotAlias);
		springLayout.putConstraint(SpringLayout.WEST, labelType, 0, SpringLayout.WEST, checkInheritable);
		getContentPane().add(labelType);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		loadTypes();
		localize();
		setIcons();
		
		loadDialog();
	}
	
	/**
	 * Load slot types
	 */
	private void loadTypes() {
		
		GeneratorUtilities.loadSlotTypesCombo(comboSlotType);
		
		comboSlotType.setSelectedItem(SlotType.TEXT);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSlotName);
		Utility.localize(checkInheritable);
		Utility.localize(labelType);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
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
}
