/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;

/**
 * 
 * @author
 *
 */
public class SelectedAreasFrame extends SelectedAreasFrameBase {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private final ButtonGroup buttonGroupContent = new ButtonGroup();
	private JButton buttonReload;
	private JButton buttonClose;
	private JSplitPane splitPane;
	private JPanel panelLeft;
	private JRadioButton radioDescriptions;
	private JRadioButton radioAliases;
	private JLabel labelList;
	private JScrollPane scrollPane;
	private JList list;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkExactMatch;
	private JCheckBox checkWholeWords;
	private JPopupMenu popupMenu;
	private JMenuItem menuSelectArea;
	private JMenuItem menuSelectAreaWithSubareas;
	private JSeparator separator;

	/**
	 * Create the frame.
	 */
	public SelectedAreasFrame() {
		
		// Initialize components.
		initComponents();
		// $hide>>$
		setComponentsReferences(
				labelList,
				list,
				radioAliases,
				radioDescriptions,
				buttonReload,
				popupMenu,
				menuSelectArea,
				labelFilter,
				textFilter,
				checkCaseSensitive,
				checkWholeWords,
				checkExactMatch,
				buttonClose,
				menuSelectAreaWithSubareas,
				splitPane);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setAlwaysOnTop(true);
		setTitle("org.multipage.generator.textSelectedAreas");
		setBounds(100, 100, 678, 445);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonReload = new JButton("org.multipage.generator.textReload");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonReload, -10, SpringLayout.SOUTH, getContentPane());
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		buttonReload.setMargin(new Insets(0, 0, 0, 0));
		buttonReload.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonReload);
		
		buttonClose = new JButton("textClose");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonReload, -54, SpringLayout.WEST, buttonClose);
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonClose.setPreferredSize(new Dimension(80, 25));
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonClose);
		
		splitPane = new JSplitPane();
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -6, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, 0, SpringLayout.EAST, splitPane);
		splitPane.setResizeWeight(0.5);
		getContentPane().add(splitPane);
		
		panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		SpringLayout sl_panelLeft = new SpringLayout();
		panelLeft.setLayout(sl_panelLeft);
		
		radioDescriptions = new JRadioButton("org.multipage.generator.textDescriptions");
		buttonGroupContent.add(radioDescriptions);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, radioDescriptions, 10, SpringLayout.NORTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.WEST, radioDescriptions, 10, SpringLayout.WEST, panelLeft);
		radioDescriptions.setSelected(true);
		radioDescriptions.setOpaque(false);
		panelLeft.add(radioDescriptions);
		
		radioAliases = new JRadioButton("org.multipage.generator.textAliases");
		buttonGroupContent.add(radioAliases);
		sl_panelLeft.putConstraint(SpringLayout.NORTH, radioAliases, 0, SpringLayout.NORTH, radioDescriptions);
		sl_panelLeft.putConstraint(SpringLayout.WEST, radioAliases, 6, SpringLayout.EAST, radioDescriptions);
		radioAliases.setOpaque(false);
		panelLeft.add(radioAliases);
		
		labelList = new JLabel("org.multipage.generator.textSelectedAreas");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, labelList, 6, SpringLayout.SOUTH, radioDescriptions);
		sl_panelLeft.putConstraint(SpringLayout.WEST, labelList, 0, SpringLayout.WEST, radioDescriptions);
		panelLeft.add(labelList);
		
		scrollPane = new JScrollPane();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelList);
		sl_panelLeft.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.SOUTH, scrollPane, -110, SpringLayout.SOUTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelLeft);
		panelLeft.add(scrollPane);
		
		list = new JList();
		scrollPane.setViewportView(list);
		
		popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);
		
		menuSelectArea = new JMenuItem("org.multipage.generator.menuSelectArea");
		popupMenu.add(menuSelectArea);
		
		menuSelectAreaWithSubareas = new JMenuItem("org.multipage.generator.menuSelectAreaAndSubareas");
		popupMenu.add(menuSelectAreaWithSubareas);
		
		separator = new JSeparator();
		popupMenu.add(separator);
		
		labelFilter = new JLabel("org.multipage.generator.textNameFilter");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, labelFilter, 25, SpringLayout.SOUTH, scrollPane);
		sl_panelLeft.putConstraint(SpringLayout.WEST, labelFilter, 0, SpringLayout.WEST, radioDescriptions);
		panelLeft.add(labelFilter);
		
		textFilter = new JTextField();
		sl_panelLeft.putConstraint(SpringLayout.NORTH, textFilter, 0, SpringLayout.NORTH, labelFilter);
		sl_panelLeft.putConstraint(SpringLayout.WEST, textFilter, 6, SpringLayout.EAST, labelFilter);
		sl_panelLeft.putConstraint(SpringLayout.EAST, textFilter, -10, SpringLayout.EAST, panelLeft);
		textFilter.setColumns(10);
		panelLeft.add(textFilter);
		
		checkCaseSensitive = new JCheckBox("org.multipage.generator.textCaseSensitive");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 6, SpringLayout.SOUTH, textFilter);
		sl_panelLeft.putConstraint(SpringLayout.WEST, checkCaseSensitive, 0, SpringLayout.WEST, radioDescriptions);
		checkCaseSensitive.setOpaque(false);
		panelLeft.add(checkCaseSensitive);
		
		checkExactMatch = new JCheckBox("org.multipage.generator.textExactMatch");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, checkExactMatch, 6, SpringLayout.SOUTH, textFilter);
		sl_panelLeft.putConstraint(SpringLayout.WEST, checkExactMatch, 6, SpringLayout.EAST, checkCaseSensitive);
		checkExactMatch.setOpaque(false);
		panelLeft.add(checkExactMatch);
		
		checkWholeWords = new JCheckBox("org.multipage.generator.textWholeWords");
		sl_panelLeft.putConstraint(SpringLayout.NORTH, checkWholeWords, 7, SpringLayout.SOUTH, checkCaseSensitive);
		sl_panelLeft.putConstraint(SpringLayout.WEST, checkWholeWords, 0, SpringLayout.WEST, radioDescriptions);
		checkWholeWords.setOpaque(false);
		panelLeft.add(checkWholeWords);
		splitPane.setDividerLocation(300);
	}
}
