/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.maclan.Area;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreaNameFileFolderDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returned value.
	 */
	private WizardReturned returned = WizardReturned.UNKNOWN;
	
	/**
	 * Parent area reference.
	 */
	private Area parentArea;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonNext;
	private JPanel panelMain;
	private JButton buttonCancel;
	private JButton buttonPrevious;
	private JButton buttonSkip;
	private JLabel labelInsertFileFolder;
	private JLabel labelFileName;
	private JTextField textFile;
	private JTextField textFolder;
	private JLabel labelFolderName;
	private JButton buttonShowInheritedFolders;
	private TextFieldEx textDescription;
	private JLabel labelDescription;
	private TextFieldEx textSubName;
	private JLabel labelSubName;

	/**
	 * Show dialog.
	 * @param parent
	 * @param parentArea 
	 * @param isLastDialog 
	 * @param areaFolder 
	 * @param areaFile 
	 * @param areaFolder2 
	 * @param areaFolder2 
	 * @param resource
	 */
	public static WizardReturned showDialog(Component parent, Area parentArea, 
			boolean isLastDialog, Obj<String> areaDescription, Obj<String> subName,
			Obj<String> areaFile, Obj<String> areaFolder) {
		
		AreaNameFileFolderDialog dialog = new AreaNameFileFolderDialog(Utility.findWindow(parent), isLastDialog);
		dialog.parentArea = parentArea;
		
		// Load description, file and folder name.
		if (areaDescription.ref != null) {
			dialog.textDescription.setText(areaDescription.ref);
			dialog.textDescription.selectAll();
		}
		if (subName.ref != null) {
			dialog.textSubName.setText(subName.ref);
		}
		if (areaFile.ref != null) {
			dialog.textFile.setText(areaFile.ref);
		}
		if (areaFolder.ref != null) {
			dialog.textFolder.setText(areaFolder.ref);
		}
		
		dialog.setVisible(true);
		
		// Save description, file and folder name.
		areaDescription.ref = dialog.textDescription.getText();
		subName.ref = dialog.textSubName.getText();
		areaFile.ref = dialog.textFile.getText();
		areaFolder.ref = dialog.textFolder.getText();
		
		return dialog.returned;
	}

	/**
	 * Set inherited folders.
	 */
	private void onShowInheritedFolders() {
		
		AreaInheritedFoldersDialog.showDialog(this, parentArea);
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param isLastDialog 
	 */
	public AreaNameFileFolderDialog(Window parentWindow, boolean isLastDialog) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate(isLastDialog);
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
		setTitle("org.multipage.generator.textAreaFileFolder");
		
		setBounds(100, 100, 450, 300);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonNext = new JButton("org.multipage.generator.textNext");
		buttonNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNext();
			}
		});
		buttonNext.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonNext, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonNext, -10, SpringLayout.EAST, panel);
		buttonNext.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonNext);
		
		buttonCancel = new JButton("textCancel");
		sl_panel.putConstraint(SpringLayout.NORTH, buttonCancel, 0, SpringLayout.NORTH, buttonNext);
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		buttonPrevious = new JButton("org.multipage.generator.textPrevious");
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -24, SpringLayout.WEST, buttonPrevious);
		buttonPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrevious();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonPrevious, 0, SpringLayout.SOUTH, buttonNext);
		sl_panel.putConstraint(SpringLayout.EAST, buttonPrevious, -6, SpringLayout.WEST, buttonNext);
		buttonPrevious.setPreferredSize(new Dimension(80, 25));
		buttonPrevious.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonPrevious);
		
		buttonSkip = new JButton("org.multipage.generator.textSkip");
		buttonSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSkip();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSkip, 0, SpringLayout.NORTH, buttonNext);
		sl_panel.putConstraint(SpringLayout.WEST, buttonSkip, 10, SpringLayout.WEST, panel);
		buttonSkip.setPreferredSize(new Dimension(80, 25));
		buttonSkip.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonSkip);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelInsertFileFolder = new JLabel("org.multipage.generator.textInsertAreaFileAndFolder");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelInsertFileFolder, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelInsertFileFolder, 10, SpringLayout.WEST, panelMain);
		labelInsertFileFolder.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelMain.add(labelInsertFileFolder);
		
		labelFileName = new JLabel("org.multipage.generator.textAreaFile");
		labelFileName.setHorizontalAlignment(SwingConstants.RIGHT);
		labelFileName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panelMain.add(labelFileName);
		
		textFile = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFileName, 0, SpringLayout.NORTH, textFile);
		sl_panelMain.putConstraint(SpringLayout.EAST, textFile, -20, SpringLayout.EAST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelFileName, -6, SpringLayout.WEST, textFile);
		textFile.setFont(new Font("Tahoma", Font.BOLD, 14));
		panelMain.add(textFile);
		textFile.setColumns(10);
		
		textFolder = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textFolder, 10, SpringLayout.SOUTH, textFile);
		sl_panelMain.putConstraint(SpringLayout.WEST, textFolder, 0, SpringLayout.WEST, textFile);
		sl_panelMain.putConstraint(SpringLayout.EAST, textFolder, -20, SpringLayout.EAST, panelMain);
		textFolder.setFont(new Font("Tahoma", Font.BOLD, 14));
		textFolder.setColumns(10);
		panelMain.add(textFolder);
		
		labelFolderName = new JLabel("org.multipage.generator.textAreaFolder");
		labelFolderName.setHorizontalAlignment(SwingConstants.RIGHT);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFolderName, 0, SpringLayout.NORTH, textFolder);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelFolderName, 0, SpringLayout.EAST, labelFileName);
		labelFolderName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panelMain.add(labelFolderName);
		
		buttonShowInheritedFolders = new JButton("org.multipage.generator.textInheritedFolders");
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonShowInheritedFolders, 10, SpringLayout.SOUTH, textFolder);
		buttonShowInheritedFolders.setMargin(new Insets(0, 0, 0, 0));
		buttonShowInheritedFolders.setPreferredSize(new Dimension(120, 25));
		buttonShowInheritedFolders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onShowInheritedFolders();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonShowInheritedFolders, 0, SpringLayout.WEST, textFile);
		panelMain.add(buttonShowInheritedFolders);
		
		textDescription = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textDescription, 15, SpringLayout.SOUTH, labelInsertFileFolder);
		sl_panelMain.putConstraint(SpringLayout.WEST, textDescription, 100, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, textDescription, -20, SpringLayout.EAST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, textFile, 0, SpringLayout.WEST, textDescription);
		textDescription.setFont(new Font("Tahoma", Font.BOLD, 14));
		textDescription.setColumns(10);
		panelMain.add(textDescription);
		
		labelDescription = new JLabel("org.multipage.generator.textAreaDescriptionLabel");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelDescription, 0, SpringLayout.NORTH, textDescription);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelDescription, -6, SpringLayout.WEST, textDescription);
		labelDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		labelDescription.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panelMain.add(labelDescription);
		
		textSubName = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textFile, 10, SpringLayout.SOUTH, textSubName);
		sl_panelMain.putConstraint(SpringLayout.NORTH, textSubName, 10, SpringLayout.SOUTH, textDescription);
		sl_panelMain.putConstraint(SpringLayout.WEST, textSubName, 0, SpringLayout.WEST, textDescription);
		sl_panelMain.putConstraint(SpringLayout.EAST, textSubName, 0, SpringLayout.EAST, textFile);
		textSubName.setFont(new Font("Tahoma", Font.BOLD, 14));
		textSubName.setColumns(10);
		panelMain.add(textSubName);
		
		labelSubName = new JLabel("org.multipage.generator.textSubNameLabel");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSubName, 0, SpringLayout.NORTH, textSubName);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelSubName, -6, SpringLayout.WEST, textSubName);
		labelSubName.setHorizontalAlignment(SwingConstants.RIGHT);
		labelSubName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		panelMain.add(labelSubName);
	}

	/**
	 * Post creation.
	 * @param isLastDialog 
	 */
	private void postCreate(boolean isLastDialog) {
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		
		localize(isLastDialog);
		setIcons();
		
		// If there is no builder extension, hide skip button.
		buttonSkip.setVisible(ProgramGenerator.isExtensionToBuilder());
	}

	/**
	 * Localize components.
	 * @param isLastDialog 
	 */
	private void localize(boolean isLastDialog) {
		
		Utility.localize(this);
		buttonNext.setText(Resources.getString(isLastDialog ? "textOk" : "org.multipage.generator.textNext"));
		Utility.localize(buttonCancel);
		Utility.localize(buttonPrevious);
		Utility.localize(buttonSkip);
		Utility.localize(labelInsertFileFolder);
		Utility.localize(labelFileName);
		Utility.localize(labelFolderName);
		Utility.localize(buttonShowInheritedFolders);
		Utility.localize(labelDescription);
		Utility.localize(labelSubName);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonNext.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonPrevious.setIcon(Images.getIcon("org/multipage/generator/images/previous_icon.png"));
		buttonSkip.setIcon(Images.getIcon("org/multipage/generator/images/skip.png"));
		buttonShowInheritedFolders.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
	}
	
	/**
	 * On skip.
	 */
	protected void onSkip() {
		
		returned = WizardReturned.SKIP;
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		returned = WizardReturned.CANCEL;
		dispose();
	}
	
	/**
	 * On Next.
	 */
	protected void onNext() {

		// Check alias.
		String typedAlias = textSubName.getText();
		if (ProgramGenerator.getAreasModel().existsAreaAlias(typedAlias)) {
			
			Utility.show(this, "org.multipage.generator.messageAreaWithAliasAlreadyExists", typedAlias);
			return;
		}
		
		returned = WizardReturned.NEXT;
		dispose();
	}
	
	/**
	 * On previous.
	 */
	protected void onPrevious() {
		
		returned = WizardReturned.PREVIOUS;
		dispose();
	}
}