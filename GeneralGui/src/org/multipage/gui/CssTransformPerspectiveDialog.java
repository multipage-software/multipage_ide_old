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
public class CssTransformPerspectiveDialog extends JDialog {

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
	private JLabel labelL;
	private TextFieldEx textL;
	private JComboBox comboUnits;


	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssTransformPerspective showDialog(Component parent) {
		
		CssTransformPerspectiveDialog dialog = new CssTransformPerspectiveDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getPerspective();
		}
		return null;
	}

	/**
	 * Show edit dialog.
	 * @param parent
	 * @param perspective
	 * @return
	 */
	public static boolean editDialog(Component parent,
			CssTransformPerspective perspective) {
		
		CssTransformPerspectiveDialog dialog = new CssTransformPerspectiveDialog(parent);
		dialog.setPerspective(perspective);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			perspective.setFrom(dialog.getPerspective());
		}
		return false;
	}

	/**
	 * Set perspective edit fields.
	 * @param perspective
	 */
	private void setPerspective(CssTransformPerspective perspective) {
		
		textL.setText(String.valueOf(perspective.l));
		comboUnits.setSelectedItem(perspective.units);
	}

	/**
	 * Get perspective.
	 * @return
	 */
	private CssTransformPerspective getPerspective() {
		
		CssTransformPerspective perspective = new CssTransformPerspective();
		
		perspective.l = Utility.getFloat(textL, 0.0f);
		perspective.units = (String) comboUnits.getSelectedItem();
		
		return perspective;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssTransformPerspectiveDialog(Component parent) {
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
		setTitle("org.multipage.gui.textCssTransformPerspectiveDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 345, 232);
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
		
		labelL = new JLabel("l = ");
		getContentPane().add(labelL);
		
		textL = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textL, 70, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textL, 109, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, labelL, 3, SpringLayout.NORTH, textL);
		springLayout.putConstraint(SpringLayout.EAST, labelL, -6, SpringLayout.WEST, textL);
		textL.setColumns(10);
		getContentPane().add(textL);
		
		comboUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboUnits, 0, SpringLayout.NORTH, textL);
		springLayout.putConstraint(SpringLayout.WEST, comboUnits, 0, SpringLayout.EAST, textL);
		comboUnits.setPreferredSize(new Dimension(50, 20));
		getContentPane().add(comboUnits);
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
		
		Utility.loadCssUnits(comboUnits);
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

		textL.setText("0.0");
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
