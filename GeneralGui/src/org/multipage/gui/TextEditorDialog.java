/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 
 * @author
 *
 */
public class TextEditorDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Editor.
	 */
	private TextEditorPane editor;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonCancel;
	private JButton buttonOk;

	/**
	 * Show dialog.
	 * @param parent
	 * @param text 
	 * @param resource
	 */
	public static String showDialog(Component parent, String text) {
		
		TextEditorDialog dialog = new TextEditorDialog(Utility.findWindow(parent));
		
		dialog.editor.setText(text);
		dialog.setLocation(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			return dialog.editor.getText();
		}
		return null;
	}
	
	/**
	 * Set location of this dialog on the screen.
	 * @param parent
	 */
	private void setLocation(Component parent) {
		
		Point location = parent.getLocationOnScreen();
		Dimension size = getBounds().getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// Height of the task bar.
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		int taskBarSize = scnMax.bottom;
		screenSize.height -= taskBarSize;
		
		// Trim location.
		if (location.x + size.width > screenSize.width) {
			location.x = screenSize.width - size.width;
		}
		if (location.y + size.height > screenSize.height) {
			location.y = screenSize.height - size.height;
		}
		
		setLocation(location);
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public TextEditorDialog(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.gui.textTextEditor");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		
		setBounds(100, 100, 450, 341);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 35));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonOk);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// Add editor.
		editor = new TextEditorPane(this, true);
		getContentPane().add(editor, BorderLayout.CENTER);
		
		localize();
		setIcons();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
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
}