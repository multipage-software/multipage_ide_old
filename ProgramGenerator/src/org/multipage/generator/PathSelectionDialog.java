/*
 * Copyright 2010-2020 (C) Vaclav Koarcik
 * 
 * Created on : 24-02-2020
 *
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.multipage.generator.ProgramPaths.PathSupplier;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author user
 *
 */
public class PathSelectionDialog extends JDialog {
	
	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Window position
	 */
	private static Rectangle bounds;
	
	/**
	 * Set default state
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}
	
	/**
	 * Load state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Save state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Confirmation flag
	 */
	private boolean confirmed = false;
	private JComboBox<PathSupplier> comboBoxPaths;
	private JLabel labelSelectPath;
	private JButton buttonCancel;
	private JButton buttonOk;
	
	/**
	 * Show dialog.
	 * @param parent
	 * @param slot 
	 * @param area 
	 * @return
	 */
	public static ProgramPaths.PathSupplier showDialog(Component parent, Area area) {
		
		PathSelectionDialog dialog = new PathSelectionDialog(parent);
		dialog.loadPaths(area);
		dialog.setVisible(true);
		
		if (!dialog.confirmed) {
			return null;
		}
		
		return dialog.getPathSupplier();
	}
	
	/**
	 * Constructor.
	 * @param parent
	 */
	public PathSelectionDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		
		// Initialize components.
		initComponents();
		
		// Post creation.
		postCreation(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		setBounds(new Rectangle(0, 0, 500, 300));
		
		setPreferredSize(new Dimension(500, 170));
		setTitle("org.multipage.generator.textSelectPath");
		
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		comboBoxPaths = new JComboBox<PathSupplier>();
		springLayout.putConstraint(SpringLayout.WEST, comboBoxPaths, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboBoxPaths, -10, SpringLayout.EAST, getContentPane());
		comboBoxPaths.setPreferredSize(new Dimension(200, 25));
		getContentPane().add(comboBoxPaths);
		
		labelSelectPath = new JLabel("org.multipage.generator.textSelectPath");
		springLayout.putConstraint(SpringLayout.NORTH, labelSelectPath, 16, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, comboBoxPaths, 6, SpringLayout.SOUTH, labelSelectPath);
		springLayout.putConstraint(SpringLayout.WEST, labelSelectPath, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSelectPath);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, comboBoxPaths);
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		localize();
		setIcons();
		
		loadDialog();
	}
	
	/**
	 * Localize components
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelSelectPath);
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
	
	/**
	 * On OK
	 */
	protected void onOk() {
		
		// Confirm revision
		confirmed = true;
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On cancel
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		if (!bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
	}
	
	/**
	 * Load paths.
	 * @param area
	 */
	private void loadPaths(Area area) {
		
		GeneratorUtilities.loadProgramPaths(comboBoxPaths);
		GeneratorUtilities.loadAreaPaths(comboBoxPaths, area);
	}
	
	/**
	 * Save dialog
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * Get path supplier.
	 * @return
	 */
	private PathSupplier getPathSupplier() {
		
		ProgramPaths.PathSupplier pathSupplier = (ProgramPaths.PathSupplier) comboBoxPaths.getSelectedItem();
		return pathSupplier;
	}
}