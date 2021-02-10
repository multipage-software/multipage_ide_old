/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;

import javax.swing.*;

import org.multipage.gui.*;

import com.maclan.Area;

import java.awt.event.*;
import java.util.LinkedList;

/**
 * 
 * @author
 *
 */
public class AreasProperties extends AreasPropertiesBase {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JLabel labelAreaDescription;
	private JTextField textDescription;
	private JButton buttonSaveArea;
	private JMenuBar menuBar;
	private JMenu menuArea;
	private JMenuItem menuEditResources;
	private JButton buttonDeleteText;
	private JSplitPane splitPane;
	private SlotListPanel panelSlotList;
	private JLabel labelAreaAlias;
	private JTextField textAlias;
	private JButton buttonSaveAlias;
	private JPanel panelExtension;
	private JMenuItem menuEditDependencies;
	private JMenuItem menuAreaEdit;

	/**
	 * Create the panel.
	 */
	public AreasProperties(boolean isPropertiesPanel) {

		this.isPropertiesPanel = isPropertiesPanel;
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		panelSlotList = ProgramGenerator.newSlotListPanel();
		splitPane.setLeftComponent(panelSlotList);
		
		menuArea.setIcon(Images.getIcon("org/multipage/generator/images/edit.png"));
		
		setComponentsReferences(
			labelAreaDescription,
			textDescription,
			buttonSaveArea,
			menuArea,
			menuEditResources,
			buttonDeleteText,
			splitPane,
			panelSlotList,
			labelAreaAlias,
			textAlias,
			buttonSaveAlias,
			panelExtension,
			menuEditDependencies,
			menuAreaEdit);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelAreaDescription = new JLabel("org.multipage.generator.textAreaDescription");
		springLayout.putConstraint(SpringLayout.WEST, labelAreaDescription, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelAreaDescription, -10, SpringLayout.EAST, this);
		add(labelAreaDescription);
		
		textDescription = new TextFieldEx();
		textDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onDescriptionEnter();
				}
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textDescription, 6, SpringLayout.SOUTH, labelAreaDescription);
		springLayout.putConstraint(SpringLayout.WEST, textDescription, 10, SpringLayout.WEST, this);
		add(textDescription);
		textDescription.setColumns(10);
		
		buttonSaveArea = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, textDescription, -3, SpringLayout.WEST, buttonSaveArea);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveArea, 6, SpringLayout.SOUTH, labelAreaDescription);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSaveArea, 0, SpringLayout.SOUTH, textDescription);
		buttonSaveArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDescription();
			}
		});
		buttonSaveArea.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveArea.setPreferredSize(new Dimension(18, 18));
		add(buttonSaveArea);
		
		menuBar = new JMenuBar();
		menuBar.setPreferredSize(new Dimension(0, 24));
		springLayout.putConstraint(SpringLayout.EAST, menuBar, 0, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaDescription, 10, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.SOUTH, labelAreaDescription, 26, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.NORTH, menuBar, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, this);
		add(menuBar);
		
		menuArea = new JMenu("org.multipage.generator.menuArea");
		menuBar.add(menuArea);
		
		menuEditResources = new JMenuItem("org.multipage.generator.menuAreaEditResources");
		menuEditResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditor.RESOURCES);
			}
		});
		
		menuAreaEdit = new JMenuItem("org.multipage.generator.menuAreaEdit");
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditor.NOT_SPECIFIED);
			}
		});
		menuArea.add(menuAreaEdit);
		menuArea.addSeparator();
		menuArea.add(menuEditResources);
		
		menuEditDependencies = new JMenuItem("org.multipage.generator.menuAreaEditDependencies");
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditor.DEPENDENCIES);
			}
		});
		menuArea.add(menuEditDependencies);
		
		buttonDeleteText = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, buttonDeleteText, -10, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveArea, -3, SpringLayout.WEST, buttonDeleteText);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonDeleteText, 0, SpringLayout.SOUTH, textDescription);
		buttonDeleteText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteLocalText();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonDeleteText, 6, SpringLayout.SOUTH, labelAreaDescription);
		buttonDeleteText.setPreferredSize(new Dimension(18, 18));
		buttonDeleteText.setMargin(new Insets(0, 0, 0, 0));
		add(buttonDeleteText);
		
		splitPane = new JSplitPane();
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(true);
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, -10, SpringLayout.EAST, this);
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		labelAreaAlias = new JLabel("org.multipage.generator.textAreaAlias2");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaAlias, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.WEST, labelAreaAlias, 10, SpringLayout.WEST, this);
		add(labelAreaAlias);
		
		textAlias = new TextFieldEx();
		textAlias.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onAliasEnter();
				}
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 6, SpringLayout.SOUTH, textAlias);
		
		panelExtension = new JPanel();
		panelExtension.setBorder(null);
		splitPane.setRightComponent(panelExtension);
		panelExtension.setLayout(new BorderLayout(0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 6, SpringLayout.EAST, labelAreaAlias);
		add(textAlias);
		textAlias.setColumns(10);
		
		buttonSaveAlias = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, textAlias, -3, SpringLayout.WEST, buttonSaveAlias);
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveAlias, -10, SpringLayout.EAST, this);
		buttonSaveAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAlias();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveAlias, 0, SpringLayout.NORTH, textAlias);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSaveAlias, 0, SpringLayout.SOUTH, textAlias);
		buttonSaveAlias.setPreferredSize(new Dimension(18, 18));
		buttonSaveAlias.setMargin(new Insets(0, 0, 0, 0));
		add(buttonSaveAlias);
	}
	
	/**
	 * On set areas.
	 */
	@Override
	protected void onSetAreas(LinkedList<Area> areas) {
		
		if (areas.size() != 1) {
			return;
		}
		
		Area area = areas.getFirst();
		textAlias.setEnabled(!area.isProtected());
	}
}
