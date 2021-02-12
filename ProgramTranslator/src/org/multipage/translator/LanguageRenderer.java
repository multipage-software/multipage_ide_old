/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import javax.swing.*;

import org.multipage.gui.GraphUtility;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Font;

/**
 * 
 * @author
 *
 */
public class LanguageRenderer extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Colors
	 */
	private static final Color colorDefault = new Color(255, 200, 200);

	/**
	 * Is selected.
	 */
	private boolean isSelected;

	/**
	 * Has focus.
	 */
	private boolean hasFocus;

	// $hide<<$
	/**
	 * Dialog components.
	 */
	private JLabel labelDescription;
	private JLabel labelId;
	private JLabel labelAlias;
	private JLabel labelIcon;
	private JLabel labelAliasLabel;
	private JLabel labelIdLabel;
	private JCheckBox checkBoxIsStart;

	/**
	 * Create the panel.
	 */
	public LanguageRenderer() {
		// Initialize components.
		initComponents();
		// $hide>>$
		// Localize components.
		localize();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(503, 68));
		setMinimumSize(new Dimension(300, 30));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelDescription = new JLabel("description");
		labelDescription.setFont(new Font("Tahoma", Font.BOLD, 12));
		springLayout.putConstraint(SpringLayout.EAST, labelDescription, -10, SpringLayout.EAST, this);
		add(labelDescription);
		
		labelId = new JLabel("ID");
		springLayout.putConstraint(SpringLayout.NORTH, labelId, 10, SpringLayout.SOUTH, labelDescription);
		labelId.setFont(new Font("Tahoma", Font.PLAIN, 11));
		add(labelId);
		
		labelAlias = new JLabel("alias");
		labelAlias.setFont(new Font("Tahoma", Font.PLAIN, 11));
		add(labelAlias);
		
		labelIcon = new JLabel("");
		springLayout.putConstraint(SpringLayout.NORTH, labelDescription, 0, SpringLayout.NORTH, labelIcon);
		springLayout.putConstraint(SpringLayout.WEST, labelDescription, 10, SpringLayout.EAST, labelIcon);
		springLayout.putConstraint(SpringLayout.SOUTH, labelDescription, 0, SpringLayout.SOUTH, labelIcon);
		springLayout.putConstraint(SpringLayout.NORTH, labelIcon, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelIcon, 10, SpringLayout.WEST, this);
		labelIcon.setPreferredSize(new Dimension(30, 20));
		add(labelIcon);
		
		labelAliasLabel = new JLabel("org.multipage.translator.textAlias");
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 0, SpringLayout.NORTH, labelAliasLabel);
		springLayout.putConstraint(SpringLayout.WEST, labelAlias, 6, SpringLayout.EAST, labelAliasLabel);
		springLayout.putConstraint(SpringLayout.EAST, labelAlias, 45, SpringLayout.EAST, labelAliasLabel);
		springLayout.putConstraint(SpringLayout.NORTH, labelAliasLabel, 10, SpringLayout.SOUTH, labelIcon);
		springLayout.putConstraint(SpringLayout.WEST, labelAliasLabel, 0, SpringLayout.WEST, labelDescription);
		labelAliasLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelAliasLabel);
		
		labelIdLabel = new JLabel("org.multipage.translator.textIdentifier2");
		springLayout.putConstraint(SpringLayout.NORTH, labelIdLabel, 10, SpringLayout.SOUTH, labelDescription);
		springLayout.putConstraint(SpringLayout.WEST, labelId, 6, SpringLayout.EAST, labelIdLabel);
		labelIdLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelIdLabel);
		
		checkBoxIsStart = new JCheckBox("org.multipage.translator.textIsStartLanguage");
		springLayout.putConstraint(SpringLayout.EAST, checkBoxIsStart, 120, SpringLayout.EAST, labelAlias);
		springLayout.putConstraint(SpringLayout.WEST, labelIdLabel, 10, SpringLayout.EAST, checkBoxIsStart);
		springLayout.putConstraint(SpringLayout.WEST, checkBoxIsStart, 10, SpringLayout.EAST, labelAlias);
		springLayout.putConstraint(SpringLayout.NORTH, checkBoxIsStart, -4, SpringLayout.NORTH, labelId);
		checkBoxIsStart.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkBoxIsStart.setOpaque(false);
		add(checkBoxIsStart);
	}
	
	/**
	 * Set properties.
	 * @param id 
	 * @param image 
	 * @param isStart 
	 * @param hasFocus 
	 * @param isSelected 
	 * @param index 
	 */
	public void setProperties(String description, long id, String alias,
			BufferedImage image, boolean isStart, int index, boolean isSelected,
			boolean hasFocus) {
		
		labelDescription.setText(description);
		labelId.setText(id != 0L ? String.valueOf(id)
				: Resources.getString("org.multipage.translator.textDefaultLanguage"));
		labelAlias.setText(alias);
		
		checkBoxIsStart.setSelected(isStart);
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		if (image != null) {
			labelIcon.setIcon(new ImageIcon(image));
		}
		else {
			labelIcon.setIcon(null);
		}
		
		// Get background color.
		Color backGroundColor = id == 0L ? colorDefault : Utility.itemColor(index);
		// Set color.
		setBackground(backGroundColor);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// Call parent.
		super.paint(g);
		// Draw selection.
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelAliasLabel);
		Utility.localize(labelIdLabel);
		Utility.localize(checkBoxIsStart);
	}
}
