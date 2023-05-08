/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

/**
 * 
 * @author
 *
 */
public class NewEnumerationValueDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized dialog states.
	 */
	private static Rectangle bounds;

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Enumeration description.
	 */
	private String enumerationDescription;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JPanel panelMain;
	private JLabel labelNewValue;
	private JLabel labelNewDescription;
	private JLabel labelMessage;
	private JTextField textValue;
	private JTextField textDescription;

	/**
	 * Show dialog.
	 * @param parent
	 * @param enumerationDescription 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, String enumerationDescription,
			Obj<String> value, Obj<String> description) {
		
		NewEnumerationValueDialog dialog = new NewEnumerationValueDialog(Utility.findWindow(parent), enumerationDescription);
		
		dialog.setVisible(true);
		
		if (!dialog.confirm) {
			return false;
		}
		
		// Set output.
		value.ref = dialog.textValue.getText();
		description.ref = dialog.textDescription.getText();
		
		return true;
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param enumerationDescription 
	 */
	public NewEnumerationValueDialog(Window parentWindow, String enumerationDescription) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		this.enumerationDescription = enumerationDescription;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("builder.textNewEnumerationValueDialog");
		
		setBounds(100, 100, 326, 251);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelNewValue = new JLabel("builder.textNewEnumerationValueLabel");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelNewValue, 40, SpringLayout.NORTH, panelMain);
		panelMain.add(labelNewValue);
		
		labelNewDescription = new JLabel("builder.textNewEnumerationValueDescriptionLabel");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelNewDescription, 41, SpringLayout.SOUTH, labelNewValue);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelNewDescription, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelNewValue, 0, SpringLayout.WEST, labelNewDescription);
		panelMain.add(labelNewDescription);
		
		labelMessage = new JLabel("builder.textInsertEnumerationValue");
		labelMessage.setFont(new Font("Tahoma", Font.BOLD, 11));
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelMessage, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, labelNewValue);
		panelMain.add(labelMessage);
		
		textValue = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textValue, 3, SpringLayout.SOUTH, labelNewValue);
		sl_panelMain.putConstraint(SpringLayout.WEST, textValue, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, textValue, -10, SpringLayout.EAST, panelMain);
		panelMain.add(textValue);
		textValue.setColumns(10);
		
		textDescription = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textDescription, 3, SpringLayout.SOUTH, labelNewDescription);
		sl_panelMain.putConstraint(SpringLayout.WEST, textDescription, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, textDescription, 300, SpringLayout.WEST, panelMain);
		panelMain.add(textDescription);
		textDescription.setColumns(10);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
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
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelNewValue);
		Utility.localize(labelNewDescription);
		labelMessage.setText(String.format(
				Resources.getString(labelMessage.getText()), enumerationDescription));
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
}