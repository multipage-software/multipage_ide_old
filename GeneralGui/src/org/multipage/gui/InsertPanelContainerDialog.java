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
public class InsertPanelContainerDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * Insert panel reference.
	 */
	private InsertPanel insertPanel;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;

	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 */
	public static String showDialog(Component parent, InsertPanel insertPanel) {
		
		InsertPanelContainerDialog dialog = new InsertPanelContainerDialog(Utility.findWindow(parent), insertPanel);
		
		Utility.centerOnScreen(dialog);
		dialog.setVisible(true);
		
		if (!dialog.confirm) {
			return null;
		}
		
		return insertPanel.getResultText();
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param insertPanel 
	 */
	public InsertPanelContainerDialog(Window parentWindow, InsertPanel insertPanel) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		getContentPane().add(insertPanel, BorderLayout.CENTER);
		setTitle(insertPanel.getWindowTitle());
		
		this.insertPanel = insertPanel;
		
		postCreate();
		// $hide<<$
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
		
		setBounds(100, 100, 602, 395);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		Rectangle bounds = insertPanel.getContainerDialogBounds();
		
		if (!insertPanel.isBoundsSet()) {
			
			bounds.height += 50;
			setBounds(bounds);
			
			Utility.centerOnScreen(this);
			bounds = getBounds();
			insertPanel.setContainerDialogBounds(bounds);
			
			insertPanel.setBoundsSet(true);
		}
		else {
			setBounds(bounds);
		}
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		insertPanel.saveDialog();
		
		Rectangle bounds = getBounds();
		insertPanel.setContainerDialogBounds(bounds);
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
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
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
}