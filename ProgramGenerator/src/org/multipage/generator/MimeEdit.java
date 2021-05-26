/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;


/**
 * 
 * @author
 *
 */
public class MimeEdit extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Confirmation flag.
	 */
	private boolean confirm = false;

	/**
	 * References to type and extension strings.
	 */
	private Obj<String> type;

	private Obj<String> extension;

	/**
	 * Preference flag.
	 */
	private Obj<Boolean> preference;

	/**
	 * Components.
	 */
	private JLabel labelType;
	private JTextField textType;
	private JLabel labelExtension;
	private JTextField textExtension;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JCheckBox checkboxMimePreference;
	private JSeparator separator;

	/**
	 * Launch the dialog.
	 * @param preference 
	 */
	public static boolean showDialog(Component parent, Obj<String> type,
			Obj<String> extension, Obj<Boolean> preference) {
		
		MimeEdit dialog = new MimeEdit(parent, type, extension,
				preference);
		dialog.setVisible(true);
		
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param extension 
	 * @param type 
	 * @param parent 
	 * @param preference 
	 */
	public MimeEdit(Component parent, Obj<String> type, Obj<String> extension,
			Obj<Boolean> preference) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		this.type = type;
		this.extension = extension;
		this.preference = preference;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// Initialize components.
		initComponents();
		// Post creation.
		postCreate();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(220, 230));
		setTitle("org.multipage.generator.textMimeEdit");
		setBounds(100, 100, 316, 238);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelType = new JLabel("org.multipage.generator.textLabelMimeType");
		springLayout.putConstraint(SpringLayout.NORTH, labelType, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelType, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelType);
		
		textType = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textType, 6, SpringLayout.SOUTH, labelType);
		springLayout.putConstraint(SpringLayout.WEST, textType, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textType, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textType);
		textType.setColumns(10);
		
		labelExtension = new JLabel("org.multipage.generator.textLabelMimeExtension");
		springLayout.putConstraint(SpringLayout.NORTH, labelExtension, 6, SpringLayout.SOUTH, textType);
		springLayout.putConstraint(SpringLayout.WEST, labelExtension, 0, SpringLayout.WEST, labelType);
		getContentPane().add(labelExtension);
		
		textExtension = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textExtension, 6, SpringLayout.SOUTH, labelExtension);
		springLayout.putConstraint(SpringLayout.WEST, textExtension, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textExtension, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textExtension);
		textExtension.setColumns(10);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, textType);
		getContentPane().add(buttonCancel);
		buttonCancel.setMargin(new Insets(2, 4, 2, 4));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		buttonOk.setMargin(new Insets(2, 4, 2, 4));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		
		checkboxMimePreference = new JCheckBox("org.multipage.generator.textMimePreference");
		springLayout.putConstraint(SpringLayout.WEST, checkboxMimePreference, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, checkboxMimePreference, 0, SpringLayout.EAST, textType);
		checkboxMimePreference.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(checkboxMimePreference);
		
		separator = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, checkboxMimePreference, 6, SpringLayout.SOUTH, separator);
		springLayout.putConstraint(SpringLayout.NORTH, separator, 6, SpringLayout.SOUTH, textExtension);
		springLayout.putConstraint(SpringLayout.WEST, separator, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, separator, 10, SpringLayout.SOUTH, textExtension);
		springLayout.putConstraint(SpringLayout.EAST, separator, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(separator);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Set text fields.
		textType.setText(type.ref);
		textType.selectAll();
		textExtension.setText(extension.ref);
		// Set checkbox.
		checkboxMimePreference.setSelected(preference.ref);
		// Localize elements.
		localize();
		// Set icons.
		setIcons();
		// Center dialog.
		Utility.centerOnScreen(this);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(labelType);
		Utility.localize(labelExtension);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(checkboxMimePreference);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {

		type.ref = textType.getText();
		extension.ref = textExtension.getText();
		preference.ref = checkboxMimePreference.isSelected();
		
		confirm = true;
		dispose();
	}
}
