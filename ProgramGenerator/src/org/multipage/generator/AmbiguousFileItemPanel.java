/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AmbiguousFileItemPanel extends JPanel {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * File name item.
	 */
	private AmbiguousFileNameItem item;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelArea;
	private JTextField textArea;
	private JButton buttonAreaEditor;
	private JButton buttonFocus;
	private JLabel labelVersion;

	/**
	 * Create the panel.
	 * @param item 
	 */
	public AmbiguousFileItemPanel(AmbiguousFileNameItem item) {

		this.item = item; // $hide$
		initComponents();
		
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setOpaque(false);
		setPreferredSize(new Dimension(437, 29));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelArea = new JLabel("org.multipage.generator.textAreaName");
		springLayout.putConstraint(SpringLayout.NORTH, labelArea, 4, SpringLayout.NORTH, this);
		add(labelArea);
		
		textArea = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.EAST, textArea, 170, SpringLayout.EAST, labelArea);
		textArea.setPreferredSize(new Dimension(200, 22));
		springLayout.putConstraint(SpringLayout.WEST, textArea, 6, SpringLayout.EAST, labelArea);
		textArea.setOpaque(false);
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 4, SpringLayout.NORTH, this);
		textArea.setEditable(false);
		add(textArea);
		textArea.setColumns(10);
		
		buttonAreaEditor = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonAreaEditor, 4, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, buttonAreaEditor, 6, SpringLayout.EAST, textArea);
		buttonAreaEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onEditArea();
			}
		});
		buttonAreaEditor.setPreferredSize(new Dimension(22, 22));
		add(buttonAreaEditor);
		
		buttonFocus = new JButton("");
		springLayout.putConstraint(SpringLayout.WEST, buttonFocus, 3, SpringLayout.EAST, buttonAreaEditor);
		buttonFocus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onFocusArea();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonFocus, 0, SpringLayout.NORTH, labelArea);
		buttonFocus.setPreferredSize(new Dimension(22, 22));
		add(buttonFocus);
		
		labelVersion = new JLabel("version");
		springLayout.putConstraint(SpringLayout.WEST, labelVersion, 10, SpringLayout.EAST, buttonFocus);
		labelVersion.setFont(new Font("Arial", Font.ITALIC, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelVersion, 0, SpringLayout.NORTH, labelArea);
		add(labelVersion);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		
		setIcons();
		
		setToolTips();
		
		// Set area name.
		textArea.setText(item.area.getDescriptionForced(true));
		textArea.setCaretPosition(0);
		
		// Set version.
		labelVersion.setText(String.format(
				Resources.getString("org.multipage.generator.textAreaVersionLabel"), item.version.toString()));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelArea);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonAreaEditor.setIcon(Images.getIcon("org/multipage/generator/images/edit.png"));
		buttonFocus.setIcon(Images.getIcon("org/multipage/generator/images/search_icon.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		String areaName = item.area.toString();
		
		buttonAreaEditor.setToolTipText(String.format(
				Resources.getString("org.multipage.generator.tooltipEditArea"), areaName));
		buttonFocus.setToolTipText(String.format(
				Resources.getString("org.multipage.generator.tooltipFocusOnArea"), areaName));
	}
	
	/**
	 * On edit area.
	 */
	protected void onEditArea() {
		
		// Execute area editor.
		AreaEditorFrame.showDialog(null, item.area);
	}

	/**
	 * Focus on area.
	 */
	protected void onFocusArea() {
		
		GeneratorMainFrame.getFrame().getAreaDiagramEditor().focusArea(item.area.getId());
	}
}
