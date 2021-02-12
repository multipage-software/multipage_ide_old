/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.basic;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author VK
 *
 */
public class SelectDatabaseDialog extends JDialog {

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
	private JLabel label;
	private JComboBox comboBox;
	private JButton buttonCancel;
	private JButton buttonOk;
	
	/**
	 * Get database name.
	 * @param parent
	 * @param databaseNames
	 * @return
	 */
	public static String showDialog(Component parent,
			LinkedList<String> databaseNames) {
		
		SelectDatabaseDialog dialog = new SelectDatabaseDialog(Utility.findWindow(parent));
		dialog.loadNames(databaseNames);
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			return dialog.getSelectedName();
		}
		return null;
	}

	/**
	 * Create the dialog.
	 * @param window 
	 */
	public SelectDatabaseDialog(Window window) {
		super(window, ModalityType.APPLICATION_MODAL);

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("org.multipage.basic.textSelectDatabaseName");
		setBounds(100, 100, 301, 185);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		label = new JLabel("org.multipage.basic.textSelectDatabaseNameLabel");
		springLayout.putConstraint(SpringLayout.NORTH, label, 29, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, label, 28, SpringLayout.WEST, getContentPane());
		getContentPane().add(label);
		
		comboBox = new JComboBox();
		comboBox.setPreferredSize(new Dimension(28, 25));
		springLayout.putConstraint(SpringLayout.NORTH, comboBox, 6, SpringLayout.SOUTH, label);
		springLayout.putConstraint(SpringLayout.WEST, comboBox, 0, SpringLayout.WEST, label);
		springLayout.putConstraint(SpringLayout.EAST, comboBox, 236, SpringLayout.WEST, label);
		getContentPane().add(comboBox);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		confirm = true;
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		confirm = false;
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		Utility.centerOnScreen(this);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(label);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/basic/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/basic/images/cancel_icon.png"));
	}

	/**
	 * Load names.
	 * @param databaseNames
	 */
	private void loadNames(LinkedList<String> databaseNames) {
		
		Collections.sort(databaseNames);
		
		for (String databaseName : databaseNames) {
			comboBox.addItem(databaseName);
		}
	}

	/**
	 * Get selected name.
	 * @return
	 */
	private String getSelectedName() {
		
		return (String) comboBox.getSelectedItem();
	}
}
