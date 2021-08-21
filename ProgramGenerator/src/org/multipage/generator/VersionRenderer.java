/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.VersionObj;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class VersionRenderer extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Parameters.
	 */
	private boolean isSelected;
	private boolean cellHasFocus;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelIdText;
	private JLabel labelId;
	private JLabel labelDescription;
	private JLabel labelAliasText;
	private JLabel labelAlias;

	/**
	 * Create the panel.
	 */
	public VersionRenderer() {

		initComponents();
		
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(371, 62));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelIdText = new JLabel("org.multipage.generator.textVersionId");
		springLayout.putConstraint(SpringLayout.WEST, labelIdText, 51, SpringLayout.WEST, this);
		labelIdText.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelIdText);
		
		labelId = new JLabel("id");
		springLayout.putConstraint(SpringLayout.NORTH, labelId, 0, SpringLayout.NORTH, labelIdText);
		springLayout.putConstraint(SpringLayout.WEST, labelId, 6, SpringLayout.EAST, labelIdText);
		springLayout.putConstraint(SpringLayout.EAST, labelId, 65, SpringLayout.EAST, labelIdText);
		labelId.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelId);
		
		labelDescription = new JLabel("description");
		labelDescription.setIconTextGap(10);
		springLayout.putConstraint(SpringLayout.NORTH, labelIdText, 6, SpringLayout.SOUTH, labelDescription);
		springLayout.putConstraint(SpringLayout.NORTH, labelDescription, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelDescription, 10, SpringLayout.WEST, this);
		labelDescription.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(labelDescription);
		
		labelAliasText = new JLabel("org.multipage.generator.textVersionAlias");
		labelAliasText.setFont(new Font("Tahoma", Font.ITALIC, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelAliasText, 0, SpringLayout.NORTH, labelIdText);
		springLayout.putConstraint(SpringLayout.WEST, labelAliasText, 6, SpringLayout.EAST, labelId);
		add(labelAliasText);
		
		labelAlias = new JLabel("alias");
		labelAlias.setPreferredSize(new Dimension(70, 14));
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 0, SpringLayout.NORTH, labelIdText);
		labelAlias.setFont(new Font("Tahoma", Font.ITALIC, 11));
		springLayout.putConstraint(SpringLayout.WEST, labelAlias, 6, SpringLayout.EAST, labelAliasText);
		add(labelAlias);
	}
	
	/**
	 * Post create.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		labelDescription.setIcon(Images.getIcon("org/multipage/generator/images/version.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelIdText);
		Utility.localize(labelAliasText);
	}

	/* Paint method
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		// Paint component.
		super.paint(g);
		// Draw selection.
		GraphUtility.drawSelection(g, this, isSelected, cellHasFocus);
	}
	
	/**
	 * Reset properties.
	 */
	public void reset() {
		
		isSelected = false;
		cellHasFocus = false;
		
		// Background color.
		setBackground(Utility.itemColor(0));
		
		// Reset parameters.
		labelId.setText("");
		labelDescription.setText("");
		labelAlias.setText("");
	}

	/**
	 * Set properties.
	 * @param version
	 * @param index
	 * @param isSelected
	 * @param cellHasFocus
	 */
	public void set(VersionObj version, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		this.isSelected = isSelected;
		this.cellHasFocus = cellHasFocus;
		
		// Background color.
		setBackground(Utility.itemColor(index));
		
		// Show version parameters.
		labelId.setText(String.valueOf(version.getId()));
		labelDescription.setText(version.getDescription());
		labelAlias.setText(version.getAlias());
	}

	/**
	 * Enable / disable components.
	 * @param enable
	 */
	public void setEnabledComponents(boolean enable) {
		
		labelIdText.setEnabled(enable);
		labelId.setEnabled(enable);
		labelDescription.setEnabled(enable);
		labelAliasText.setEnabled(enable);
		labelAlias.setEnabled(enable);
	}
}
