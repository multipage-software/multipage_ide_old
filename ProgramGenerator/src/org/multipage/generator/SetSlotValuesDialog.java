/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
public class SetSlotValuesDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

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
	private JCheckBox checkIsDefault;

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public SetSlotValuesDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textSetSlotDefaultNormalValues");
		setBounds(100, 100, 281, 148);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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
				onOK();
			}
		});
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		checkIsDefault = new JCheckBox("org.multipage.generator.textSlotNormalValues");
		checkIsDefault.setSelected(true);
		checkIsDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangeIsDefaultValue();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, checkIsDefault, 68, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, checkIsDefault, -23, SpringLayout.NORTH, buttonCancel);
		getContentPane().add(checkIsDefault);
	}

	/**
	 * On OK.
	 */
	protected void onOK() {
		
		confirm = true;
		dispose();
	}

	/**
	 * On cancel dialog.
	 */
	protected void onCancel() {
		
		confirm = false;
		dispose();
	}

	/**
	 * Show dialog.
	 * @param parent
	 * @param isDefault
	 * @return
	 */
	public static boolean showDialog(Component parent,
			Obj<Boolean> isDefault) {
		
		SetSlotValuesDialog dialog = new SetSlotValuesDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			isDefault.ref = dialog.checkIsDefault.isSelected();
		}
		
		return dialog.confirm;
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Utility.centerOnScreen(this);
		
		localize();
		setIconsAndCursors();
		
		onChangeIsDefaultValue();
	}

	/**
	 * Set icons.
	 */
	private void setIconsAndCursors() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		
		checkIsDefault.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}
	
	/**
	 * On change "is default value" flag.
	 */
	protected void onChangeIsDefaultValue() {
		
		if (checkIsDefault.isSelected()) {
			checkIsDefault.setText("org.multipage.generator.textSlotDefaultValues");
			checkIsDefault.setIcon(Images.getIcon("org/multipage/generator/images/default_value.png"));
		}
		else {
			checkIsDefault.setText("org.multipage.generator.textSlotNormalValues");
			checkIsDefault.setIcon(Images.getIcon("org/multipage/generator/images/normal_value.png"));
		}
		
		Utility.localize(checkIsDefault);
	}
}
