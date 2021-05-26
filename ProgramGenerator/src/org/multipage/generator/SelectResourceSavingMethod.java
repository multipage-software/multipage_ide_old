/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.multipage.gui.CheckTextFile;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class SelectResourceSavingMethod extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * File reference.
	 */
	private File file;

	/**
	 * Confirmation.
	 */
	private boolean confirm = false;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JRadioButton buttonText;
	private JRadioButton buttonBinary;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JLabel labelSelectMethod;
	private JButton buttonCheckFile;
	private JComboBox comboEncodings;
	private JLabel labelFileName;
	private JLabel labelDisplayFileName;
	private JButton buttonOpenFile;

	/**
	 * Launch the dialog.
	 * @param parentComponent
	 * @param file
	 * @param saveAsText
	 * @param encoding 
	 */
	public static boolean showDialog(Component parentComponent, File file,
			Obj<Boolean> saveAsText, Obj<String> encoding) {

		SelectResourceSavingMethod dialog = new SelectResourceSavingMethod(parentComponent,
				file);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			// Set output values.
			saveAsText.ref = dialog.buttonText.isSelected();
			if (saveAsText.ref) {
				encoding.ref = (String) dialog.comboEncodings.getSelectedItem();
			}
		}
		
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentComponent 
	 * @param file 
	 */
	public SelectResourceSavingMethod(Component parentComponent, File file) {
		super(Utility.findWindow(parentComponent), ModalityType.DOCUMENT_MODAL);
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreation(file);
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(430, 240));

		setResizable(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("org.multipage.generator.textSelectResourceSavingMethod");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 426, 241);
		
		buttonText = new JRadioButton("org.multipage.generator.textSaveResourceAsText");
		buttonText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isTextFile(true);
			}
		});
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.NORTH, buttonText, 86, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, buttonText, 30, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonText, 160, SpringLayout.WEST, getContentPane());
		getContentPane().setLayout(springLayout);
		buttonGroup.add(buttonText);
		getContentPane().add(buttonText);
		
		buttonBinary = new JRadioButton("org.multipage.generator.textSaveResourceAsBinary");
		springLayout.putConstraint(SpringLayout.NORTH, buttonBinary, 10, SpringLayout.SOUTH, buttonText);
		springLayout.putConstraint(SpringLayout.WEST, buttonBinary, 30, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonBinary, -10, SpringLayout.EAST, getContentPane());
		buttonBinary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isTextFile(false);
			}
		});
		buttonGroup.add(buttonBinary);
		getContentPane().add(buttonBinary);
		
		labelSelectMethod = new JLabel("org.multipage.generator.textSelectResourceSavingMethod");
		springLayout.putConstraint(SpringLayout.WEST, labelSelectMethod, 0, SpringLayout.WEST, buttonText);
		springLayout.putConstraint(SpringLayout.EAST, labelSelectMethod, -30, SpringLayout.EAST, getContentPane());
		labelSelectMethod.setHorizontalAlignment(SwingConstants.LEFT);
		getContentPane().add(labelSelectMethod);
		
		buttonCheckFile = new JButton("org.multipage.generator.textCheckFile");
		springLayout.putConstraint(SpringLayout.SOUTH, labelSelectMethod, -7, SpringLayout.NORTH, buttonCheckFile);
		springLayout.putConstraint(SpringLayout.WEST, buttonCheckFile, -90, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCheckFile, -10, SpringLayout.EAST, getContentPane());
		buttonCheckFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCheckFile();
			}
		});
		buttonCheckFile.setMargin(new Insets(0, 0, 0, 0));
		buttonCheckFile.setMinimumSize(new Dimension(80, 25));
		buttonCheckFile.setMaximumSize(new Dimension(80, 25));
		buttonCheckFile.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonCheckFile);
		
		comboEncodings = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, buttonCheckFile, 0, SpringLayout.NORTH, comboEncodings);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCheckFile, 0, SpringLayout.SOUTH, comboEncodings);
		springLayout.putConstraint(SpringLayout.NORTH, comboEncodings, 86, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboEncodings, -10, SpringLayout.WEST, buttonCheckFile);
		springLayout.putConstraint(SpringLayout.WEST, comboEncodings, 6, SpringLayout.EAST, buttonText);
		springLayout.putConstraint(SpringLayout.SOUTH, comboEncodings, 0, SpringLayout.SOUTH, buttonText);
		getContentPane().add(comboEncodings);
		
		buttonOk = new JButton("textOk");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, 0, SpringLayout.EAST, comboEncodings);
		getContentPane().add(buttonOk);
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMinimumSize(new Dimension(80, 25));
		buttonOk.setMaximumSize(new Dimension(80, 25));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		
		buttonCancel = new JButton("textCancel");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, buttonBinary);
		getContentPane().add(buttonCancel);
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMinimumSize(new Dimension(80, 25));
		buttonCancel.setMaximumSize(new Dimension(80, 25));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		
		labelFileName = new JLabel("org.multipage.generator.textFileName");
		springLayout.putConstraint(SpringLayout.NORTH, labelSelectMethod, 10, SpringLayout.SOUTH, labelFileName);
		springLayout.putConstraint(SpringLayout.NORTH, labelFileName, 11, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelFileName, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, labelFileName, 38, SpringLayout.NORTH, getContentPane());
		getContentPane().add(labelFileName);
		
		labelDisplayFileName = new JLabel("filename");
		springLayout.putConstraint(SpringLayout.NORTH, labelDisplayFileName, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelDisplayFileName, 6, SpringLayout.EAST, labelFileName);
		springLayout.putConstraint(SpringLayout.SOUTH, labelDisplayFileName, 38, SpringLayout.NORTH, getContentPane());
		labelDisplayFileName.setForeground(Color.RED);
		labelDisplayFileName.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(labelDisplayFileName);
		
		buttonOpenFile = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonOpenFile, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, buttonOpenFile, -38, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOpenFile, 38, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOpenFile, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelDisplayFileName, -6, SpringLayout.WEST, buttonOpenFile);
		buttonOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFileOpen();
			}
		});
		getContentPane().add(buttonOpenFile);
	}

	/**
	 * Post creation.
	 * @param file 
	 */
	private void postCreation(File file) {
		
		this.file = file;

		// Localize components.
		localize();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set icons.
		setIcons();
		// Load encodings.
		Utility.loadEncodings(comboEncodings, "UTF-8");
		// Set file.
		setFile(file);
	}

	/**
	 * Set file.
	 * @param file
	 */
	private void setFile(File file) {
		
		this.file = file;
		
		// Display file name.
		labelDisplayFileName.setText(file.getName());
		
		// Get file length.
		long fileLength = file.length();
		
		boolean tooLong = fileLength > Settings.getMaximumTextResSize();

		// If the file length is too large or binary, do not save the file as text.
		if (tooLong	|| !Utility.isTextFileExtension(file)) {
			
			buttonBinary.setSelected(true);
			// If the file is too long, disable controls.
			if (tooLong) {
				buttonBinary.setEnabled(false);
				buttonText.setEnabled(false);
				// Inform user.
				labelSelectMethod.setText(
						Resources.getString("org.multipage.generator.messageFileTooLongStoredAsBinary"));
			}
			isTextFile(false);
		}
		else {
			buttonBinary.setEnabled(true);
			buttonText.setEnabled(true);
			buttonText.setSelected(true);
			buttonBinary.setSelected(false);
			isTextFile(true);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSelectMethod);
		Utility.localize(buttonText);
		Utility.localize(buttonBinary);
		Utility.localize(buttonCheckFile);
		Utility.localize(labelFileName);
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

		confirm = true;
		
		dispose();
	}

	/**
	 * Sets icons.
	 */
	private void setIcons() {

		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonOpenFile.setIcon(Images.getIcon("org/multipage/generator/images/load_icon.png"));
		buttonCheckFile.setIcon(Images.getIcon("org/multipage/generator/images/check_icon.png"));
	}

	/**
	 * On check file.
	 */
	protected void onCheckFile() {
		
		// Get current encoding.
		String encodingText = (String) comboEncodings.getSelectedItem();

		// Check file content.
		Obj<Boolean> isTextFile = new Obj<Boolean>();
		Obj<String> encoding = new Obj<String>(encodingText);
		
		if (!CheckTextFile.showDialog(GeneratorMainFrame.getFrame(),
				file, isTextFile, encoding)) {
			return;
		}
		
		// Select encoding.
		Utility.selectComboItem(comboEncodings, encoding.ref);
		// Set file type.
		setRadioButton(isTextFile.ref);
	}

	/**
	 * Set file type components.
	 * @param isText
	 */
	private void setRadioButton(Boolean isText) {

		if (isText) {
			buttonText.setSelected(true);
		}
		else {
			buttonBinary.setSelected(true);
		}
		isTextFile(isText);
	}

	/**
	 * Sets controls.
	 * @param isText
	 */
	protected void isTextFile(boolean isText) {

		comboEncodings.setEnabled(isText);
		buttonCheckFile.setEnabled(isText);
	}

	/**
	 * On file open.
	 */
	protected void onFileOpen() {
		
		// Get path.
		String path = file.getPath();
		// Choose file.
		File choosenFile = GeneratorUtilities.chooseFile(this, path, true);
		if (choosenFile == null) {
			return;
		}
		
		// Set file.
		setFile(choosenFile);
	}
}
