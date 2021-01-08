/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 18-02-2020
 *
 */
package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;

/**
 * 
 * @author user
 *
 */
public class RendererPathItem extends JPanel {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Path not specified string constant.
	 */
	private static String notSpecified = null;

	/**
	 * Is selected flag.
	 */
	private boolean isSelected;
	
	/**
	 * Has focus flag.
	 */
	private boolean hasFocus;
	
	/**
	 * Controls.
	 */
	private JLabel labelCaption;
	private JLabel labelPath;
	
	/**
	 * Set properties.
	 */
	public void set(boolean enabled, boolean isSelected, boolean hasFocus, int index, String caption, String path) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		if (enabled) {
			this.labelCaption.setText(" " + caption + " ");
			if (path == null || path.isEmpty()) {
				
				if (notSpecified == null) {
					notSpecified = Resources.getString("org.mutlipage.gui.textNotSpecified");
				}
				path = notSpecified ;
			}
			this.labelPath.setText(path);
		}
		else {
			// Empty texts.
			this.labelCaption.setText("");
			this.labelPath.setText("");
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}

	/**
	 * Create the panel.
	 */
	public RendererPathItem() {

		initComponents();
		setOpaque(false);
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(500, 20));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelCaption = new JLabel("caption");
		springLayout.putConstraint(SpringLayout.NORTH, labelCaption, 3, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelCaption, 3, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelCaption, -3, SpringLayout.SOUTH, this);
		labelCaption.setFont(new Font("Tahoma", Font.PLAIN, 11));
		labelCaption.setForeground(Color.WHITE);
		labelCaption.setOpaque(true);
		labelCaption.setBackground(Color.GRAY);
		add(labelCaption);
		
		labelPath = new JLabel("path");
		springLayout.putConstraint(SpringLayout.NORTH, labelPath, 0, SpringLayout.NORTH, labelCaption);
		springLayout.putConstraint(SpringLayout.WEST, labelPath, 6, SpringLayout.EAST, labelCaption);
		springLayout.putConstraint(SpringLayout.SOUTH, labelPath, 0, SpringLayout.SOUTH, labelCaption);
		add(labelPath);
	}

}
