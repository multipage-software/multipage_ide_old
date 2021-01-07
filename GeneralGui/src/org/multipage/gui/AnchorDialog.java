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
import java.io.*;

/**
 * 
 * @author
 *
 */
public class AnchorDialog extends JDialog {

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
	private JLabel labelText;
	private JTextField textAnchorText;
	private JLabel labelUrl;
	private TextFieldEx textUrl;

	/**
	 * Show dialog.
	 * @param parent
	 * @param text 
	 * @return
	 */
	public static String showDialog(Component parent, String text) {
		
		AnchorDialog dialog = new AnchorDialog(parent);
		dialog.textAnchorText.setText(text);
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getAnchor();
		}
		return null;
	}
	
	/**
	 * Compile anchor text and return it.
	 * @return
	 */
	private String getAnchor() {
		
		return String.format("<a href=\"%s\">%s</a>", textUrl.getText(), textAnchorText.getText());
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public AnchorDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCompileWebAnchor");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				onWindowOpened();
			}
		});
		setBounds(100, 100, 361, 223);
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
		
		labelText = new JLabel("org.multipage.gui.textAnchorText");
		springLayout.putConstraint(SpringLayout.NORTH, labelText, 20, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelText, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelText);
		
		textAnchorText = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAnchorText, 6, SpringLayout.SOUTH, labelText);
		springLayout.putConstraint(SpringLayout.WEST, textAnchorText, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textAnchorText, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textAnchorText);
		textAnchorText.setColumns(10);
		
		labelUrl = new JLabel("org.multipage.gui.textAnchorUrl");
		springLayout.putConstraint(SpringLayout.NORTH, labelUrl, 6, SpringLayout.SOUTH, textAnchorText);
		springLayout.putConstraint(SpringLayout.WEST, labelUrl, 0, SpringLayout.WEST, labelText);
		getContentPane().add(labelUrl);
		
		textUrl = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textUrl, 6, SpringLayout.SOUTH, labelUrl);
		springLayout.putConstraint(SpringLayout.WEST, textUrl, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textUrl, -10, SpringLayout.EAST, getContentPane());
		textUrl.setColumns(10);
		getContentPane().add(textUrl);
	}

	/**
	 * On window opened.
	 */
	protected void onWindowOpened() {
		
		textUrl.requestFocus();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadDialog();
		
		setDefaultButton();
	}

	/**
	 * Set default button.
	 */
	private void setDefaultButton() {
		
		JRootPane rootPane = SwingUtilities.getRootPane(buttonOk); 
		rootPane.setDefaultButton(buttonOk);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
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
		Utility.localize(labelText);
		Utility.localize(labelUrl);
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
