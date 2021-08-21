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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.maclan.Resource;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class ResourcePropertiesEditor extends ResourcePropertiesEditorBase {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Components.
	 */
	private final JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JButton cancelButton;
	private JLabel labelResourceName;
	private JTextField textResourceName;
	private JLabel labelResourceIdentifier;
	private JTextField textIdentifier;
	private JLabel labelNamespace;
	private NameSpaceField panelNamespace;
	private JCheckBox checkboxVisible;
	private JLabel labelMimeType;
	private JComboBox comboBoxMime;
	private JButton buttonLoadData;
	private JLabel labelFile;
	private JButton buttonDefaultData;
	private JLabel labelLocalDescription;
	private JTextField textLocalDescription;
	private JButton buttonAssign;
	private JButton buttonClearAssignment;
	private JLabel labelAssigned;
	private JButton buttonFindMime;

	/**
	 * Create the dialog.
	 * @param parentComponent 
	 * @param resource 
	 */
	public ResourcePropertiesEditor(Component parentComponent, Resource resource) {
		super(Utility.findWindow(parentComponent), ModalityType.DOCUMENT_MODAL);
		
		this.parentWindow = Utility.findWindow(parentComponent);

		// Initialize components.
		initComponents();
		// Post create.
		// $hide>>$
		setComponentsReferences(
				okButton,
				cancelButton,
				labelResourceName,
				textResourceName,
				labelResourceIdentifier,
				textIdentifier,
				labelNamespace,
				panelNamespace,
				checkboxVisible,
				labelMimeType,
				comboBoxMime,
				buttonLoadData,
				labelFile,
				buttonDefaultData,
				labelLocalDescription,
				textLocalDescription,
				buttonAssign,
				buttonClearAssignment,
				labelAssigned,
				buttonFindMime
				);
		postCreate(resource);
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(410, 320));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				onWindowOpened();
			}
		});
		setTitle("org.multipage.generator.textResourceEditor");
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 480, 370);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);
		
		labelResourceName = new JLabel("org.multipage.generator.textResourceName");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelResourceName, 10, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelResourceName, 10, SpringLayout.WEST, contentPanel);
		contentPanel.add(labelResourceName);
		
		textResourceName = new TextFieldEx();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, textResourceName, 6, SpringLayout.SOUTH, labelResourceName);
		sl_contentPanel.putConstraint(SpringLayout.WEST, textResourceName, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, textResourceName, -10, SpringLayout.EAST, contentPanel);
		contentPanel.add(textResourceName);
		textResourceName.setColumns(10);
		{
			labelResourceIdentifier = new JLabel("org.multipage.generator.textResourceId");
			contentPanel.add(labelResourceIdentifier);
		}
		{
			textIdentifier = new TextFieldEx();
			sl_contentPanel.putConstraint(SpringLayout.WEST, textIdentifier, -90, SpringLayout.EAST, contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.EAST, labelResourceIdentifier, -6, SpringLayout.WEST, textIdentifier);
			sl_contentPanel.putConstraint(SpringLayout.EAST, textIdentifier, -10, SpringLayout.EAST, contentPanel);
			textIdentifier.setHorizontalAlignment(SwingConstants.CENTER);
			textIdentifier.setEditable(false);
			contentPanel.add(textIdentifier);
			textIdentifier.setColumns(10);
		}
		{
			labelNamespace = new JLabel("org.multipage.generator.textNameSpace");
			sl_contentPanel.putConstraint(SpringLayout.WEST, labelNamespace, 0, SpringLayout.WEST, labelResourceName);
			contentPanel.add(labelNamespace);
		}
		{
			panelNamespace = new NameSpaceField();
			sl_contentPanel.putConstraint(SpringLayout.NORTH, panelNamespace, 6, SpringLayout.SOUTH, labelNamespace);
			sl_contentPanel.putConstraint(SpringLayout.WEST, panelNamespace, 10, SpringLayout.WEST, contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.EAST, panelNamespace, -10, SpringLayout.EAST, contentPanel);
			contentPanel.add(panelNamespace);
		}

		checkboxVisible = new JCheckBox("org.multipage.generator.textResourceIdVisible");
		sl_contentPanel.putConstraint(SpringLayout.EAST, checkboxVisible, -10, SpringLayout.EAST, contentPanel);
		checkboxVisible.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(checkboxVisible);
		
		labelMimeType = new JLabel("org.multipage.generator.textMimeType");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelMimeType, 6, SpringLayout.SOUTH, panelNamespace);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelMimeType, 0, SpringLayout.WEST, labelResourceName);
		contentPanel.add(labelMimeType);
		
		comboBoxMime = new JComboBox();
		sl_contentPanel.putConstraint(SpringLayout.WEST, checkboxVisible, 50, SpringLayout.EAST, comboBoxMime);
		comboBoxMime.setPreferredSize(new Dimension(28, 24));
		sl_contentPanel.putConstraint(SpringLayout.NORTH, checkboxVisible, 0, SpringLayout.NORTH, comboBoxMime);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, comboBoxMime, 6, SpringLayout.SOUTH, labelMimeType);
		sl_contentPanel.putConstraint(SpringLayout.WEST, comboBoxMime, 0, SpringLayout.WEST, labelResourceName);
		sl_contentPanel.putConstraint(SpringLayout.EAST, comboBoxMime, -200, SpringLayout.EAST, contentPanel);
		contentPanel.add(comboBoxMime);
		
		buttonLoadData = new JButton("org.multipage.generator.textLoadData");
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, buttonLoadData, 41, SpringLayout.SOUTH, comboBoxMime);
		buttonLoadData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLoadData();
			}
		});
		buttonLoadData.setPreferredSize(new Dimension(80, 25));
		buttonLoadData.setMargin(new Insets(0, 0, 0, 0));
		buttonLoadData.setMaximumSize(new Dimension(80, 25));
		buttonLoadData.setMinimumSize(new Dimension(80, 25));
		sl_contentPanel.putConstraint(SpringLayout.NORTH, buttonLoadData, 16, SpringLayout.SOUTH, comboBoxMime);
		sl_contentPanel.putConstraint(SpringLayout.WEST, buttonLoadData, 10, SpringLayout.WEST, contentPanel);
		contentPanel.add(buttonLoadData);
		
		labelFile = new JLabel("org.multipage.generator.textOriginalData");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelFile, 0, SpringLayout.NORTH, buttonLoadData);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, labelFile, 0, SpringLayout.SOUTH, buttonLoadData);
		contentPanel.add(labelFile);
		
		buttonDefaultData = new JButton("");
		buttonDefaultData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDefaultData();
			}
		});
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelFile, 10, SpringLayout.EAST, buttonDefaultData);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, buttonDefaultData, 16, SpringLayout.SOUTH, comboBoxMime);
		sl_contentPanel.putConstraint(SpringLayout.WEST, buttonDefaultData, 6, SpringLayout.EAST, buttonLoadData);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, buttonDefaultData, 41, SpringLayout.SOUTH, comboBoxMime);
		sl_contentPanel.putConstraint(SpringLayout.EAST, buttonDefaultData, 31, SpringLayout.EAST, buttonLoadData);
		contentPanel.add(buttonDefaultData);
		
		labelLocalDescription = new JLabel("org.multipage.generator.textLocalDescription");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelResourceIdentifier, 6, SpringLayout.SOUTH, labelLocalDescription);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, textIdentifier, 6, SpringLayout.SOUTH, labelLocalDescription);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelLocalDescription, 6, SpringLayout.SOUTH, textResourceName);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelLocalDescription, 10, SpringLayout.WEST, contentPanel);
		contentPanel.add(labelLocalDescription);
		
		textLocalDescription = new TextFieldEx();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, textLocalDescription, 6, SpringLayout.SOUTH, labelLocalDescription);
		sl_contentPanel.putConstraint(SpringLayout.WEST, textLocalDescription, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelNamespace, 6, SpringLayout.SOUTH, textLocalDescription);
		sl_contentPanel.putConstraint(SpringLayout.EAST, textLocalDescription, -10, SpringLayout.WEST, labelResourceIdentifier);
		contentPanel.add(textLocalDescription);
		textLocalDescription.setColumns(10);
		
		buttonAssign = new JButton("org.multipage.generator.textAssignResource");
		buttonAssign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onAssign();
			}
		});
		sl_contentPanel.putConstraint(SpringLayout.NORTH, buttonAssign, 6, SpringLayout.SOUTH, buttonLoadData);
		sl_contentPanel.putConstraint(SpringLayout.WEST, buttonAssign, 0, SpringLayout.WEST, labelResourceName);
		buttonAssign.setPreferredSize(new Dimension(80, 25));
		buttonAssign.setMinimumSize(new Dimension(80, 25));
		buttonAssign.setMaximumSize(new Dimension(80, 25));
		buttonAssign.setMargin(new Insets(0, 0, 0, 0));
		contentPanel.add(buttonAssign);
		
		buttonClearAssignment = new JButton("");
		buttonClearAssignment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClearAssignment();
			}
		});
		buttonClearAssignment.setPreferredSize(new Dimension(25, 25));
		sl_contentPanel.putConstraint(SpringLayout.NORTH, buttonClearAssignment, 6, SpringLayout.SOUTH, buttonDefaultData);
		sl_contentPanel.putConstraint(SpringLayout.WEST, buttonClearAssignment, 0, SpringLayout.WEST, buttonDefaultData);
		contentPanel.add(buttonClearAssignment);
		
		labelAssigned = new JLabel("org.multipage.generator.textNoResourceAssigned");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, labelAssigned, 0, SpringLayout.NORTH, buttonClearAssignment);
		sl_contentPanel.putConstraint(SpringLayout.WEST, labelAssigned, 0, SpringLayout.WEST, labelFile);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, labelAssigned, 0, SpringLayout.SOUTH, buttonClearAssignment);
		contentPanel.add(labelAssigned);
		
		buttonFindMime = new JButton("");
		sl_contentPanel.putConstraint(SpringLayout.WEST, buttonFindMime, 3, SpringLayout.EAST, comboBoxMime);
		buttonFindMime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindMime();
			}
		});
		sl_contentPanel.putConstraint(SpringLayout.NORTH, buttonFindMime, 0, SpringLayout.NORTH, checkboxVisible);
		buttonFindMime.setPreferredSize(new Dimension(25, 25));
		buttonFindMime.setMargin(new Insets(0, 0, 0, 0));
		contentPanel.add(buttonFindMime);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("textOk");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOk();
					}
				});
				okButton.setMargin(new Insets(2, 4, 2, 4));
				okButton.setPreferredSize(new Dimension(80, 25));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("textCancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancel();
					}
				});
				cancelButton.setMargin(new Insets(2, 4, 2, 4));
				cancelButton.setPreferredSize(new Dimension(80, 25));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
