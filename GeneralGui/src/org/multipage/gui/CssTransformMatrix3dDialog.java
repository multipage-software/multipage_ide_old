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
public class CssTransformMatrix3dDialog extends JDialog {

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
	private JTextField textA1;
	private JLabel labelA1;
	private JLabel labelB1;
	private TextFieldEx textB1;
	private JLabel labelC1;
	private TextFieldEx textC1;
	private JLabel labelD1;
	private TextFieldEx textD1;
	private JLabel labelA2;
	private TextFieldEx textA2;
	private JLabel labelA3;
	private TextFieldEx textA3;
	private JLabel labelA4;
	private TextFieldEx textA4;
	private TextFieldEx textB2;
	private JLabel labelB2;
	private TextFieldEx textB3;
	private JLabel labelB3;
	private TextFieldEx textB4;
	private JLabel labelB4;
	private TextFieldEx textC2;
	private JLabel labelC2;
	private TextFieldEx textC3;
	private JLabel labelC3;
	private TextFieldEx textC4;
	private JLabel labelC4;
	private TextFieldEx textD2;
	private JLabel labelD2;
	private TextFieldEx textD3;
	private JLabel labelD3;
	private TextFieldEx textD4;
	private JLabel labelD4;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssTransformMatrix3d showDialog(Component parent) {
		
		CssTransformMatrix3dDialog dialog = new CssTransformMatrix3dDialog(parent);
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
			CssTransformMatrix3d matrix) {
		
		CssTransformMatrix3dDialog dialog = new CssTransformMatrix3dDialog(parent);
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
	private void setMatrix(CssTransformMatrix3d matrix) {
		
		textA1.setText(String.valueOf(matrix.a1));
		textB1.setText(String.valueOf(matrix.b1));
		textC1.setText(String.valueOf(matrix.c1));
		textD1.setText(String.valueOf(matrix.d1));
		textA2.setText(String.valueOf(matrix.a2));
		textB2.setText(String.valueOf(matrix.b2));
		textC2.setText(String.valueOf(matrix.c2));
		textD2.setText(String.valueOf(matrix.d2));
		textA3.setText(String.valueOf(matrix.a3));
		textB3.setText(String.valueOf(matrix.b3));
		textC3.setText(String.valueOf(matrix.c3));
		textD3.setText(String.valueOf(matrix.d3));
		textA4.setText(String.valueOf(matrix.a4));
		textB4.setText(String.valueOf(matrix.b4));
		textC4.setText(String.valueOf(matrix.c4));
		textD4.setText(String.valueOf(matrix.d4));
	}

	/**
	 * Get matrix.
	 * @return
	 */
	private CssTransformMatrix3d getMatrix() {
		
		CssTransformMatrix3d matrix = new CssTransformMatrix3d();
		
		matrix.a1 = Utility.getFloat(textA1, 0.0f);
		matrix.b1 = Utility.getFloat(textB1, 0.0f);
		matrix.c1 = Utility.getFloat(textC1, 0.0f);
		matrix.d1 = Utility.getFloat(textD1, 0.0f);
		matrix.a2 = Utility.getFloat(textA2, 0.0f);
		matrix.b2 = Utility.getFloat(textB2, 0.0f);
		matrix.c2 = Utility.getFloat(textC2, 0.0f);
		matrix.d2 = Utility.getFloat(textD2, 0.0f);
		matrix.a3 = Utility.getFloat(textA3, 0.0f);
		matrix.b3 = Utility.getFloat(textB3, 0.0f);
		matrix.c3 = Utility.getFloat(textC3, 0.0f);
		matrix.d3 = Utility.getFloat(textD3, 0.0f);
		matrix.a4 = Utility.getFloat(textA4, 0.0f);
		matrix.b4 = Utility.getFloat(textB4, 0.0f);
		matrix.c4 = Utility.getFloat(textC4, 0.0f);
		matrix.d4 = Utility.getFloat(textD4, 0.0f);
		
		return matrix;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssTransformMatrix3dDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCssTransformMatrix3dDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 598, 271);
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
		
		textA1 = new TextFieldEx();
		getContentPane().add(textA1);
		textA1.setColumns(10);
		
		labelA1 = new JLabel("a1 = ");
		springLayout.putConstraint(SpringLayout.NORTH, textA1, -3, SpringLayout.NORTH, labelA1);
		springLayout.putConstraint(SpringLayout.WEST, textA1, 6, SpringLayout.EAST, labelA1);
		springLayout.putConstraint(SpringLayout.NORTH, labelA1, 42, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelA1, 36, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelA1);
		
		labelB1 = new JLabel("b1 = ");
		springLayout.putConstraint(SpringLayout.EAST, labelB1, 0, SpringLayout.EAST, labelA1);
		getContentPane().add(labelB1);
		
		textB1 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelB1, 3, SpringLayout.NORTH, textB1);
		springLayout.putConstraint(SpringLayout.NORTH, textB1, 6, SpringLayout.SOUTH, textA1);
		springLayout.putConstraint(SpringLayout.WEST, textB1, 0, SpringLayout.WEST, textA1);
		textB1.setColumns(10);
		getContentPane().add(textB1);
		
		labelC1 = new JLabel("c1 = ");
		springLayout.putConstraint(SpringLayout.EAST, labelC1, 0, SpringLayout.EAST, labelA1);
		getContentPane().add(labelC1);
		
		textC1 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelC1, 3, SpringLayout.NORTH, textC1);
		springLayout.putConstraint(SpringLayout.NORTH, textC1, 6, SpringLayout.SOUTH, textB1);
		springLayout.putConstraint(SpringLayout.EAST, textC1, 0, SpringLayout.EAST, textA1);
		textC1.setColumns(10);
		getContentPane().add(textC1);
		
		labelD1 = new JLabel("d1 = ");
		springLayout.putConstraint(SpringLayout.EAST, labelD1, 0, SpringLayout.EAST, labelA1);
		getContentPane().add(labelD1);
		
		textD1 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelD1, 3, SpringLayout.NORTH, textD1);
		springLayout.putConstraint(SpringLayout.NORTH, textD1, 6, SpringLayout.SOUTH, textC1);
		springLayout.putConstraint(SpringLayout.WEST, textD1, 0, SpringLayout.WEST, textA1);
		textD1.setColumns(10);
		getContentPane().add(textD1);
		
		labelA2 = new JLabel("a2 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelA2, 3, SpringLayout.NORTH, textA1);
		springLayout.putConstraint(SpringLayout.WEST, labelA2, 6, SpringLayout.EAST, textA1);
		getContentPane().add(labelA2);
		
		textA2 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textA2, 0, SpringLayout.NORTH, textA1);
		springLayout.putConstraint(SpringLayout.WEST, textA2, 6, SpringLayout.EAST, labelA2);
		textA2.setColumns(10);
		getContentPane().add(textA2);
		
		labelA3 = new JLabel("a3 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelA3, 0, SpringLayout.NORTH, labelA1);
		springLayout.putConstraint(SpringLayout.WEST, labelA3, 6, SpringLayout.EAST, textA2);
		getContentPane().add(labelA3);
		
		textA3 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textA3, 0, SpringLayout.NORTH, textA1);
		springLayout.putConstraint(SpringLayout.WEST, textA3, 6, SpringLayout.EAST, labelA3);
		textA3.setColumns(10);
		getContentPane().add(textA3);
		
		labelA4 = new JLabel("a4 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelA4, 0, SpringLayout.NORTH, labelA1);
		springLayout.putConstraint(SpringLayout.WEST, labelA4, 6, SpringLayout.EAST, textA3);
		getContentPane().add(labelA4);
		
		textA4 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textA4, 0, SpringLayout.NORTH, textA1);
		springLayout.putConstraint(SpringLayout.WEST, textA4, 6, SpringLayout.EAST, labelA4);
		textA4.setColumns(10);
		getContentPane().add(textA4);
		
		textB2 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textB2, -3, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.WEST, textB2, 0, SpringLayout.WEST, textA2);
		textB2.setColumns(10);
		getContentPane().add(textB2);
		
		labelB2 = new JLabel("b2 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelB2, 0, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.WEST, labelB2, 6, SpringLayout.EAST, textB1);
		getContentPane().add(labelB2);
		
		textB3 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textB3, -3, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.WEST, textB3, 0, SpringLayout.WEST, textA3);
		textB3.setColumns(10);
		getContentPane().add(textB3);
		
		labelB3 = new JLabel("b3 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelB3, 0, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.EAST, labelB3, 0, SpringLayout.EAST, labelA3);
		getContentPane().add(labelB3);
		
		textB4 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textB4, -3, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.WEST, textB4, 0, SpringLayout.WEST, textA4);
		textB4.setColumns(10);
		getContentPane().add(textB4);
		
		labelB4 = new JLabel("b4 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelB4, 0, SpringLayout.NORTH, labelB1);
		springLayout.putConstraint(SpringLayout.EAST, labelB4, 0, SpringLayout.EAST, labelA4);
		getContentPane().add(labelB4);
		
		textC2 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textC2, -3, SpringLayout.NORTH, labelC1);
		springLayout.putConstraint(SpringLayout.WEST, textC2, 0, SpringLayout.WEST, textA2);
		textC2.setColumns(10);
		getContentPane().add(textC2);
		
		labelC2 = new JLabel("c2 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelC2, 0, SpringLayout.NORTH, labelC1);
		springLayout.putConstraint(SpringLayout.EAST, labelC2, 0, SpringLayout.EAST, labelA2);
		getContentPane().add(labelC2);
		
		textC3 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textC3, 0, SpringLayout.NORTH, textC1);
		springLayout.putConstraint(SpringLayout.WEST, textC3, 0, SpringLayout.WEST, textA3);
		textC3.setColumns(10);
		getContentPane().add(textC3);
		
		labelC3 = new JLabel("c3 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelC3, 0, SpringLayout.NORTH, labelC1);
		springLayout.putConstraint(SpringLayout.EAST, labelC3, 0, SpringLayout.EAST, labelA3);
		getContentPane().add(labelC3);
		
		textC4 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textC4, 0, SpringLayout.NORTH, textC1);
		springLayout.putConstraint(SpringLayout.EAST, textC4, 0, SpringLayout.EAST, textA4);
		textC4.setColumns(10);
		getContentPane().add(textC4);
		
		labelC4 = new JLabel("c4 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelC4, 0, SpringLayout.NORTH, labelC1);
		springLayout.putConstraint(SpringLayout.EAST, labelC4, 0, SpringLayout.EAST, labelA4);
		getContentPane().add(labelC4);
		
		textD2 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textD2, 0, SpringLayout.NORTH, textD1);
		springLayout.putConstraint(SpringLayout.WEST, textD2, 0, SpringLayout.WEST, textA2);
		textD2.setColumns(10);
		getContentPane().add(textD2);
		
		labelD2 = new JLabel("d2 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelD2, 0, SpringLayout.NORTH, labelD1);
		springLayout.putConstraint(SpringLayout.EAST, labelD2, 0, SpringLayout.EAST, labelA2);
		getContentPane().add(labelD2);
		
		textD3 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textD3, 0, SpringLayout.NORTH, textD1);
		springLayout.putConstraint(SpringLayout.WEST, textD3, 0, SpringLayout.WEST, textA3);
		textD3.setColumns(10);
		getContentPane().add(textD3);
		
		labelD3 = new JLabel("d3 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelD3, 0, SpringLayout.NORTH, labelD1);
		springLayout.putConstraint(SpringLayout.EAST, labelD3, 0, SpringLayout.EAST, labelA3);
		getContentPane().add(labelD3);
		
		textD4 = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textD4, 0, SpringLayout.NORTH, textD1);
		springLayout.putConstraint(SpringLayout.WEST, textD4, 0, SpringLayout.WEST, textA4);
		textD4.setColumns(10);
		getContentPane().add(textD4);
		
		labelD4 = new JLabel("d4 = ");
		springLayout.putConstraint(SpringLayout.NORTH, labelD4, 0, SpringLayout.NORTH, labelD1);
		springLayout.putConstraint(SpringLayout.EAST, labelD4, 0, SpringLayout.EAST, labelA4);
		getContentPane().add(labelD4);
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
		
		textA1.setText("0.0");
		textB1.setText("0.0");
		textC1.setText("0.0");
		textD1.setText("0.0");
		textA2.setText("0.0");
		textB2.setText("0.0");
		textC2.setText("0.0");
		textD2.setText("0.0");
		textA3.setText("0.0");
		textB3.setText("0.0");
		textC3.setText("0.0");
		textD3.setText("0.0");
		textA4.setText("0.0");
		textB4.setText("0.0");
		textC4.setText("0.0");
		textD4.setText("0.0");
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
