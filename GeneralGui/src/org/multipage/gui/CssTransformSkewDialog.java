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
public class CssTransformSkewDialog extends JDialog {

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
	private JLabel labelAx;
	private TextFieldEx textAx;
	private JComboBox comboXUnits;
	private TextFieldEx textAy;
	private JComboBox comboYUnits;
	private JLabel labelAy;


	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssTransformSkew showDialog(Component parent) {
		
		CssTransformSkewDialog dialog = new CssTransformSkewDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getSkew();
		}
		return null;
	}

	/**
	 * Show edit dialog.
	 * @param parent
	 * @param skew
	 * @return
	 */
	public static boolean editDialog(Component parent,
			CssTransformSkew skew) {
		
		CssTransformSkewDialog dialog = new CssTransformSkewDialog(parent);
		dialog.setSkew(skew);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			skew.setFrom(dialog.getSkew());
		}
		return false;
	}

	/**
	 * Set skew edit fields.
	 * @param skew
	 */
	private void setSkew(CssTransformSkew skew) {
		
		textAx.setText(String.valueOf(skew.ax));
		comboXUnits.setSelectedItem(skew.axUnits);
		textAy.setText(String.valueOf(skew.ay));
		comboYUnits.setSelectedItem(skew.ayUnits);
	}

	/**
	 * Get skew.
	 * @return
	 */
	private CssTransformSkew getSkew() {
		
		CssTransformSkew skew = new CssTransformSkew();
		
		skew.ax = Utility.getFloat(textAx, 0.0f);
		skew.axUnits = (String) comboXUnits.getSelectedItem();
		skew.ay = Utility.getFloat(textAy, 0.0f);
		skew.ayUnits = (String) comboYUnits.getSelectedItem();

		return skew;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssTransformSkewDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCssTransformSkewDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 345, 255);
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
		
		labelAx = new JLabel("ax = ");
		springLayout.putConstraint(SpringLayout.WEST, labelAx, 77, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelAx);
		
		textAx = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAx, 70, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textAx, 109, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, labelAx, 3, SpringLayout.NORTH, textAx);
		springLayout.putConstraint(SpringLayout.EAST, labelAx, -6, SpringLayout.WEST, textAx);
		textAx.setColumns(10);
		getContentPane().add(textAx);
		
		comboXUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboXUnits, 0, SpringLayout.NORTH, textAx);
		springLayout.putConstraint(SpringLayout.WEST, comboXUnits, 0, SpringLayout.EAST, textAx);
		comboXUnits.setPreferredSize(new Dimension(50, 20));
		getContentPane().add(comboXUnits);
		
		textAy = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAy, 6, SpringLayout.SOUTH, textAx);
		springLayout.putConstraint(SpringLayout.WEST, textAy, 0, SpringLayout.WEST, textAx);
		textAy.setColumns(10);
		getContentPane().add(textAy);
		
		comboYUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboYUnits, 6, SpringLayout.SOUTH, textAx);
		springLayout.putConstraint(SpringLayout.WEST, comboYUnits, 0, SpringLayout.WEST, comboXUnits);
		comboYUnits.setPreferredSize(new Dimension(50, 20));
		getContentPane().add(comboYUnits);
		
		labelAy = new JLabel("ay = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelAy, 3, SpringLayout.NORTH, textAy);
		springLayout.putConstraint(SpringLayout.WEST, labelAy, 77, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelAy, 0, SpringLayout.EAST, labelAx);
		getContentPane().add(labelAy);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadUnits();
		
		loadDialog();
	}

	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		Utility.loadCssAngleUnits(comboXUnits);
		Utility.loadCssAngleUnits(comboYUnits);
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
		
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(this);
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

		textAx.setText("0.0");
		textAy.setText("0.0");
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
