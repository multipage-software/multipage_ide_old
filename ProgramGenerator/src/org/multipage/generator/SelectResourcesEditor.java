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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class SelectResourcesEditor extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * Selected editor constants.
	 */
	public static final int VISIBLE_RESOURCES = 0;
	public static final int AREA_RESOURCES = 1;
	public static final int NONE = -1;

	
	/**
	 * Get selected editor.
	 */
	public static int getSelectedEditor(Component parent) {
		
		SelectResourcesEditor dialog = new SelectResourcesEditor(parent);
		dialog.setVisible(true);
		
		if (!dialog.confirm) {
			return NONE;
		}
		
		if (dialog.radioVisibleResources.isSelected()) {
			return VISIBLE_RESOURCES;
		}
		
		if (dialog.radioAreaResources.isSelected()) {
			return AREA_RESOURCES;
		}
		
		return NONE;
	}
	
	/**
	 * Components.
	 */
	private JRadioButton radioAreaResources;
	private JRadioButton radioVisibleResources;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton buttonCancel;
	private JButton buttonOk;

	/**
	 * Create the dialog.
	 */
	public SelectResourcesEditor(Component parent) {
		
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		setTitle("org.multipage.generator.textSelectResourcesEditor");
		setBounds(100, 100, 360, 251);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		radioAreaResources = new JRadioButton("org.multipage.generator.textAreasResourcesEditor");
		radioAreaResources.setSelected(true);
		buttonGroup.add(radioAreaResources);
		getContentPane().add(radioAreaResources);
		
		radioVisibleResources = new JRadioButton("org.multipage.generator.textVisibleResourcesEditor");
		springLayout.putConstraint(SpringLayout.NORTH, radioVisibleResources, 112, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, radioVisibleResources, 84, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, radioAreaResources, 0, SpringLayout.WEST, radioVisibleResources);
		springLayout.putConstraint(SpringLayout.SOUTH, radioAreaResources, -24, SpringLayout.NORTH, radioVisibleResources);
		buttonGroup.add(radioVisibleResources);
		getContentPane().add(radioVisibleResources);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(2, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(2, 0, 0, 0));
		getContentPane().add(buttonOk);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		localize();
		setIcons();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(radioVisibleResources);
		Utility.localize(radioAreaResources);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOK() {
		
		confirm = true;
		dispose();
	}
}
