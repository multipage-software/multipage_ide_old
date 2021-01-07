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
public class CssTransformRotate3dDialog extends JDialog {

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
	private JLabel labelA;
	private TextFieldEx textA;
	private JComboBox comboUnits;
	private TextFieldEx textX;
	private JLabel labelX;
	private TextFieldEx textY;
	private JLabel labelY;
	private TextFieldEx textZ;
	private JLabel labelZ;


	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssTransformRotate3d showDialog(Component parent) {
		
		CssTransformRotate3dDialog dialog = new CssTransformRotate3dDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getRotate();
		}
		return null;
	}

	/**
	 * Show edit dialog.
	 * @param parent
	 * @param rotate
	 * @return
	 */
	public static boolean editDialog(Component parent,
			CssTransformRotate3d rotate) {
		
		CssTransformRotate3dDialog dialog = new CssTransformRotate3dDialog(parent);
		dialog.setRotate(rotate);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			rotate.setFrom(dialog.getRotate());
		}
		return false;
	}

	/**
	 * Set rotate edit fields.
	 * @param rotate
	 */
	private void setRotate(CssTransformRotate3d rotate) {
		
		textX.setText(String.valueOf(rotate.x));
		textY.setText(String.valueOf(rotate.y));
		textZ.setText(String.valueOf(rotate.z));
		textA.setText(String.valueOf(rotate.a));
		comboUnits.setSelectedItem(rotate.aUnits);
	}

	/**
	 * Get rotate.
	 * @return
	 */
	private CssTransformRotate3d getRotate() {
		
		CssTransformRotate3d rotate = new CssTransformRotate3d();
		
		rotate.x = Utility.getFloat(textX, 0.0f);
		rotate.y = Utility.getFloat(textY, 0.0f);
		rotate.z = Utility.getFloat(textZ, 0.0f);
		rotate.a = Utility.getFloat(textA, 0.0f);
		rotate.aUnits = (String) comboUnits.getSelectedItem();

		return rotate;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssTransformRotate3dDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCssTransformRotate3dDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 297, 318);
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
		
		labelA = new JLabel("a = ");
		getContentPane().add(labelA);
		
		textA = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelA, 3, SpringLayout.NORTH, textA);
		textA.setColumns(10);
		getContentPane().add(textA);
		
		comboUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboUnits, -3, SpringLayout.NORTH, labelA);
		springLayout.putConstraint(SpringLayout.WEST, comboUnits, 0, SpringLayout.EAST, textA);
		comboUnits.setPreferredSize(new Dimension(50, 20));
		getContentPane().add(comboUnits);
		
		textX = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.EAST, textA, 0, SpringLayout.EAST, textX);
		springLayout.putConstraint(SpringLayout.NORTH, textX, 54, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textX, 95, SpringLayout.WEST, getContentPane());
		textX.setColumns(10);
		getContentPane().add(textX);
		
		labelX = new JLabel("x = ");
		springLayout.putConstraint(SpringLayout.WEST, labelA, 0, SpringLayout.WEST, labelX);
		springLayout.putConstraint(SpringLayout.NORTH, labelX, 3, SpringLayout.NORTH, textX);
		springLayout.putConstraint(SpringLayout.EAST, labelX, -3, SpringLayout.WEST, textX);
		getContentPane().add(labelX);
		
		textY = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textY, 15, SpringLayout.SOUTH, textX);
		springLayout.putConstraint(SpringLayout.WEST, textY, 0, SpringLayout.WEST, textX);
		textY.setColumns(10);
		getContentPane().add(textY);
		
		labelY = new JLabel("y = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelY, 3, SpringLayout.NORTH, textY);
		springLayout.putConstraint(SpringLayout.EAST, labelY, 0, SpringLayout.EAST, labelX);
		getContentPane().add(labelY);
		
		textZ = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textA, 15, SpringLayout.SOUTH, textZ);
		springLayout.putConstraint(SpringLayout.NORTH, textZ, 15, SpringLayout.SOUTH, textY);
		springLayout.putConstraint(SpringLayout.WEST, textZ, 0, SpringLayout.WEST, textX);
		textZ.setColumns(10);
		getContentPane().add(textZ);
		
		labelZ = new JLabel("z = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelZ, 3, SpringLayout.NORTH, textZ);
		springLayout.putConstraint(SpringLayout.WEST, labelZ, 0, SpringLayout.WEST, labelX);
		getContentPane().add(labelZ);
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
		
		Utility.loadCssAngleUnits(comboUnits);
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
		
		textX.setText("0.0");
		textY.setText("0.0");
		textZ.setText("0.0");
		textA.setText("0.0");
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
