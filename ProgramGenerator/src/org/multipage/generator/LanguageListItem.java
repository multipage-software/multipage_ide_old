/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.Language;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class LanguageListItem extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelFlag;
	private JCheckBox checkBox;
	private JLabel labelName;

	/**
	 * Create the panel.
	 * @param startLanguageId 
	 */
	public LanguageListItem() {

		initComponents();
		postCreation(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(300, 30));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelFlag = new JLabel("");
		springLayout.putConstraint(SpringLayout.NORTH, labelFlag, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelFlag, 0, SpringLayout.SOUTH, this);
		add(labelFlag);
		
		checkBox = new JCheckBox("");
		springLayout.putConstraint(SpringLayout.EAST, labelFlag, 36, SpringLayout.EAST, checkBox);
		springLayout.putConstraint(SpringLayout.WEST, checkBox, 8, SpringLayout.WEST, this);
		checkBox.setOpaque(false);
		springLayout.putConstraint(SpringLayout.WEST, labelFlag, 6, SpringLayout.EAST, checkBox);
		springLayout.putConstraint(SpringLayout.NORTH, checkBox, 4, SpringLayout.NORTH, this);
		add(checkBox);
		
		labelName = new JLabel("name");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 6, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 6, SpringLayout.EAST, labelFlag);
		labelName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		add(labelName);
	}
	
	/**
	 * Post creation.
	 * @param startLanguageId 
	 */
	private void postCreation() {
		
	}

	/**
	 * Set properties.
	 * @param language
	 * @param index
	 * @param isSelected
	 * @param cellHasFocus
	 */
	public void setProperties(Language language, int index) {

		// Set background color.
		setBackground(Utility.itemColor(index));
		
		// Set check box.
		boolean check = false;
		if (language.user instanceof Boolean) {
			check = (Boolean) language.user;
		}
		checkBox.setSelected(check);
		
		// Set language icon.
		if (language.image != null) {
			ImageIcon icon = new ImageIcon(language.image);
			labelFlag.setIcon(icon);
		}
		else {
			labelFlag.setIcon(null);
		}
		
		// Set the rest of the panel components.
		labelName.setText(language.description);
	}
}
