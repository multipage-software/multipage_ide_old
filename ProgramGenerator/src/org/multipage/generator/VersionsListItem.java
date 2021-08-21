/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.VersionObj;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class VersionsListItem extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JCheckBox checkBox;
	private JLabel labelName;
	private JLabel labelImage;

	/**
	 * Create the panel.
	 */
	public VersionsListItem() {

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(300, 30));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		checkBox = new JCheckBox("");
		springLayout.putConstraint(SpringLayout.NORTH, checkBox, 4, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, checkBox, 8, SpringLayout.WEST, this);
		checkBox.setOpaque(false);
		add(checkBox);
		
		labelName = new JLabel("name");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 6, SpringLayout.NORTH, this);
		labelName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		add(labelName);
		
		labelImage = new JLabel("");
		springLayout.putConstraint(SpringLayout.WEST, labelName, 6, SpringLayout.EAST, labelImage);
		springLayout.putConstraint(SpringLayout.SOUTH, labelImage, 0, SpringLayout.SOUTH, this);
		labelImage.setPreferredSize(new Dimension(30, 0));
		springLayout.putConstraint(SpringLayout.NORTH, labelImage, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelImage, 6, SpringLayout.EAST, checkBox);
		add(labelImage);
	}
	
	/**
	 * Set icon.
	 */
	private void postCreate() {
		
		labelImage.setIcon(Images.getIcon("org/multipage/generator/images/version_icon.png"));
	}

	/**
	 * Set properties.
	 * @param version
	 * @param index
	 */
	public void setProperties(VersionObj version, int index) {

		// Set background color.
		setBackground(Utility.itemColor(index));
		
		// Version description.
		labelName.setText(version.getDescription());
		// Set check box.
		checkBox.setSelected((Boolean) version.getUser());
	}
}
