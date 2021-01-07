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
public class CssTransformMatrixDialog extends JDialog {

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
	private JTextField textA;
	private JLabel labelA;
	private JLabel labelB;
	private TextFieldEx textB;
	private JLabel labelC;
	private TextFieldEx textC;
	private JLabel labelD;
	private TextFieldEx textD;
	private JLabel labelTx;
	private TextFieldEx textTx;
	private JLabel labelTy;
	private TextFieldEx textTy;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssTransformMatrix showDialog(Component parent) {
		
		CssTransformMatrixDialog dialog = new CssTransformMatrixDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getMatrix();
		}
		return null;
	}

	/**
	 * Show edit dialog.
	 * @param parent
	 * @param matrix
	 * @return
	 */
	public static boolean editDialog(Component parent,
			CssTransformMatrix matrix) {
		
		CssTransformMatrixDialog dialog = new CssTransformMatrixDialog(parent);
		dialog.setMatrix(matrix);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			matrix.setFrom(dialog.getMatrix());
		}
		return false;
	}

	/**
	 * Set matrix edit fields.
	 * @param matrix
	 */
	private void setMatrix(CssTransformMatrix matrix) {
		
		textA.setText(String.valueOf(matrix.a));
		textB.setText(String.valueOf(matrix.b));
		textC.setText(String.valueOf(matrix.c));
		textD.setText(String.valueOf(matrix.d));
		textTx.setText(String.valueOf(matrix.tx));
		textTy.setText(String.valueOf(matrix.ty));
	}

	/**
	 * Get matrix.
	 * @return
	 */
	private CssTransformMatrix getMatrix() {
		
		CssTransformMatrix matrix = new CssTransformMatrix();
		
		matrix.a = Utility.getFloat(textA, 0.0f);
		matrix.b = Utility.getFloat(textB, 0.0f);
		matrix.c = Utility.getFloat(textC, 0.0f);
		matrix.d = Utility.getFloat(textD, 0.0f);
		matrix.tx = Utility.getFloat(textTx, 0.0f);
		matrix.ty = Utility.getFloat(textTy, 0.0f);
		
		return matrix;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssTransformMatrixDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCssTransformMatrixDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 473, 236);
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
		
		textA = new TextFieldEx();
		getContentPane().add(textA);
		textA.setColumns(10);
		
		labelA = new JLabel("a = ");
		springLayout.putConstraint(SpringLayout.NORTH, textA, -3, SpringLayout.NORTH, labelA);
		springLayout.putConstraint(SpringLayout.WEST, textA, 6, SpringLayout.EAST, labelA);
		springLayout.putConstraint(SpringLayout.NORTH, labelA, 42, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelA, 36, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelA);
		
		labelB = new JLabel("b = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelB, 6, SpringLayout.SOUTH, labelA);
		springLayout.putConstraint(SpringLayout.WEST, labelB, 0, SpringLayout.WEST, labelA);
		getContentPane().add(labelB);
		
		textB = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textB, 6, SpringLayout.SOUTH, textA);
		springLayout.putConstraint(SpringLayout.WEST, textB, 0, SpringLayout.WEST, textA);
		textB.setColumns(10);
		getContentPane().add(textB);
		
		labelC = new JLabel("c = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelC, 0, SpringLayout.NORTH, textA);
		springLayout.putConstraint(SpringLayout.WEST, labelC, 20, SpringLayout.EAST, textA);
		getContentPane().add(labelC);
		
		textC = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textC, 0, SpringLayout.NORTH, textA);
		springLayout.putConstraint(SpringLayout.WEST, textC, 6, SpringLayout.EAST, labelC);
		textC.setColumns(10);
		getContentPane().add(textC);
		
		labelD = new JLabel("d = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelD, 0, SpringLayout.NORTH, textB);
		springLayout.putConstraint(SpringLayout.EAST, labelD, 0, SpringLayout.EAST, labelC);
		getContentPane().add(labelD);
		
		textD = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textD, 0, SpringLayout.NORTH, textB);
		springLayout.putConstraint(SpringLayout.WEST, textD, 0, SpringLayout.WEST, textC);
		textD.setColumns(10);
		getContentPane().add(textD);
		
		labelTx = new JLabel("tx = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelTx, 0, SpringLayout.NORTH, textA);
		springLayout.putConstraint(SpringLayout.WEST, labelTx, 20, SpringLayout.EAST, textC);
		getContentPane().add(labelTx);
		
		textTx = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textTx, 0, SpringLayout.NORTH, textA);
		springLayout.putConstraint(SpringLayout.WEST, textTx, 6, SpringLayout.EAST, labelTx);
		textTx.setColumns(10);
		getContentPane().add(textTx);
		
		labelTy = new JLabel("ty = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelTy, 0, SpringLayout.NORTH, textB);
		springLayout.putConstraint(SpringLayout.WEST, labelTy, 0, SpringLayout.WEST, labelTx);
		getContentPane().add(labelTy);
		
		textTy = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textTy, 0, SpringLayout.WEST, textTx);
		springLayout.putConstraint(SpringLayout.SOUTH, textTy, 0, SpringLayout.SOUTH, textB);
		textTy.setColumns(10);
		getContentPane().add(textTy);
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
		
		textA.setText("0.0");
		textB.setText("0.0");
		textC.setText("0.0");
		textD.setText("0.0");
		textTx.setText("0.0");
		textTy.setText("0.0");
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
