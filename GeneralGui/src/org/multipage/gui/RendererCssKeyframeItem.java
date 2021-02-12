/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import java.awt.*;
import java.util.function.*;

import javax.swing.table.*;

import org.multipage.util.Resources;

import javax.swing.border.LineBorder;

/**
 * 
 * @author
 *
 */
public class RendererCssKeyframeItem extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelTimePoints;
	private JTable table;

	/**
	 * Create the panel.
	 */
	public RendererCssKeyframeItem() {

		initComponents();
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelTimePoints = new JLabel("time points");
		labelTimePoints.setPreferredSize(new Dimension(100, 14));
		springLayout.putConstraint(SpringLayout.NORTH, labelTimePoints, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelTimePoints, 10, SpringLayout.WEST, this);
		add(labelTimePoints);
		
		table = new JTable();
		springLayout.putConstraint(SpringLayout.WEST, table, 0, SpringLayout.EAST, labelTimePoints);
		table.setBorder(new LineBorder(Color.LIGHT_GRAY));
		springLayout.putConstraint(SpringLayout.NORTH, table, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, table, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, table, -10, SpringLayout.EAST, this);
		table.setOpaque(false);
		add(table);
	}

	/**
	 * Is selected flag.
	 */
	protected boolean isSelected;
	
	/**
	 * Has focus flag.
	 */
	protected boolean hasFocus;

	/**
	 * Table model.
	 */
	private DefaultTableModel tableModel;
	
	/**
	 * Post create.
	 */
	private void postCreate() {
		
		setOpaque(true);
		initTable();
	}

	/**
	 * Initialize table.
	 */
	private void initTable() {
		
		tableModel = new DefaultTableModel();
		table.setModel(tableModel);
		
		// Create columns.
		tableModel.addColumn(Resources.getString("org.multipage.gui.textCssPropertyColumn"));
		tableModel.addColumn(Resources.getString("org.multipage.gui.textCssPropertyValueColumn"));
		
		// Set column width.
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(1).setMaxWidth(200);
		table.getColumnModel().getColumn(1).setMinWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		
		// Set grid line colors.
		table.setGridColor(Color.LIGHT_GRAY);
	}

	/**
	 * Set properties.
	 * @param value 
	 */
	public RendererCssKeyframeItem set(CssKeyframe value, boolean isSelected, boolean hasFocus, int index) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		Color background = Utility.itemColor(index);
		setBackground(background);
		table.setBackground(background);
		
		// Set time points and properties.
		labelTimePoints.setText(value.getTimePointsText());
		
		tableModel.getDataVector().removeAllElements();
		
		value.forEachProperty(new BiConsumer<String, String>() {
			@Override
			public void accept(String namesText, String value) {
				
				// Add table row
				tableModel.addRow(new String [] { namesText, value });
			}
		});
		
		return this;
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
	 * Get preferred size.
	 */
	@Override
	public Dimension preferredSize() {
		
		return new Dimension(200, 100);
	}
}
