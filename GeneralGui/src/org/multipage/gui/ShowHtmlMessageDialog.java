/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class ShowHtmlMessageDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JButton buttonOk;
	private JScrollPane scrollPane;
	private TextPaneEx textPane;

	/**
	 * Show dialog.
	 * @param parent
	 * @param htmlMessage 
	 * @return
	 */
	public static void showDialog(Component parent, String htmlMessage) {
		
		ShowHtmlMessageDialog dialog = new ShowHtmlMessageDialog(parent);
		dialog.textPane.setText(htmlMessage);
		dialog.setVisible(true);
		
		return;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public ShowHtmlMessageDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.gui.textMessage");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onOk();
			}
		});
		setBounds(100, 100, 450, 267);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonOk = new JButton("textOk");
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.EAST, getContentPane());
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(buttonOk);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 181, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		textPane = new TextPaneEx();
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		scrollPane.setViewportView(textPane);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
				
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		Utility.centerOnScreen(this);
	}
}
