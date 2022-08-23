/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : DD-MM-YYYY
 */
package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.Utility;

/**
 * Class for the XXXFrame frames. Use the showFrame(...) method to make this form visible.
 * @author user
 *
 */
public class GenKeyDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Frame window boundaries.
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Frame controls.
	 */
	private JButton buttonOk;
	private JButton buttonCancel;
	
	//$hide>>$
	/**
	 * Frame object fields.
	 */
	
	// TODO Herein add new frame object fields.

	//$hide<<$
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
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
	}

	/**
	 * Write states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Show the dialog.
	 * @param parent
	 */
	public static void showDialog() {
		
		// Create a new frame object and make it visible.
		GenKeyDialog dialog = new GenKeyDialog();
		dialog.setVisible(true);
	}
	
	/**
	 * Create the frame.
	 */
	public GenKeyDialog() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		postCreate(); //$hide$
	}

	/**
	 * Initialize frame components.
	 */
	private void initComponents() {
		setBounds(new Rectangle(0, 0, 400, 400));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		setTitle("org.multipage.titleGenKeyDialog");
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
	}

	/**
	 * Post creation of the frame controls.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		// TODO Add post creation function that initialize the dialog.
		loadDialog();
	}
	
	/**
	 * Localize texts of the frame controls.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		
		// TODO Localize additional dialog texts.
	}
	
	/**
	 * Set frame icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/gui/images/main.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		
		// TODO Set additional dialog icons.
	}
	
	/**
	 * The frame confirmed by the user click on the [OK] button.
	 */
	protected void onOk() {
		
		saveDialog();
		dispose();
	}

	/**
	 * The frame has been canceled with the [Cancel] or the [X] button.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Load and set initial state of the frame window.
	 */
	private void loadDialog() {
		
		// Set dialog window boundaries.
		if (bounds != null && !bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
		
		// TODO Load additional states.
		
	}
	
	/**
	 * Save current state of the frame window.
	 */
	private void saveDialog() {
		
		// Save current dialog window boundaries.
		bounds = getBounds();
		
		// TODO Save additional states.
		
	}
}
