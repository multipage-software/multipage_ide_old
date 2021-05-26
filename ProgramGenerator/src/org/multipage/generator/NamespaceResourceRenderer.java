/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

/**
 * 
 * @author
 *
 */
public class NamespaceResourceRenderer extends NamespaceResourceRendererBase {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelTitle;
	private JLabel labelMimeTypeLabel;
	private JLabel labelMimeType;
	private JLabel labelResourceIdLabel;
	private JLabel labelResourceId;
	private JCheckBox checkBoxSaveAsText;
	private JLabel labelImage;
	private JCheckBox checkVisible;

	/**
	 * Create the panel.
	 */
	public NamespaceResourceRenderer() {
		// Initialize components.
		initComponents();
		// $hide>>$
		// Localize components.
		setComponentsReferences(
				labelTitle,
				labelMimeTypeLabel,
				labelMimeType,
				labelResourceIdLabel,
				labelResourceId,
				checkBoxSaveAsText,
				labelImage,
				checkVisible
				);
		localize();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setPreferredSize(new Dimension(579, 62));
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelTitle = new JLabel("resource description");
		labelTitle.setFont(new Font("Tahoma", Font.BOLD, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelTitle, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelTitle, 10, SpringLayout.WEST, this);
		add(labelTitle);
		
		labelMimeTypeLabel = new JLabel("org.multipage.generator.textMimeType");
		springLayout.putConstraint(SpringLayout.NORTH, labelMimeTypeLabel, 6, SpringLayout.SOUTH, labelTitle);
		springLayout.putConstraint(SpringLayout.WEST, labelMimeTypeLabel, 20, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelMimeTypeLabel, 60, SpringLayout.WEST, this);
		labelMimeTypeLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelMimeTypeLabel);
		
		labelMimeType = new JLabel("mime type");
		springLayout.putConstraint(SpringLayout.NORTH, labelMimeType, 6, SpringLayout.SOUTH, labelTitle);
		springLayout.putConstraint(SpringLayout.WEST, labelMimeType, 6, SpringLayout.EAST, labelMimeTypeLabel);
		labelMimeType.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelMimeType);
		
		labelResourceIdLabel = new JLabel("org.multipage.generator.textResourceId2");
		springLayout.putConstraint(SpringLayout.WEST, labelResourceIdLabel, 170, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelMimeType, -10, SpringLayout.WEST, labelResourceIdLabel);
		springLayout.putConstraint(SpringLayout.NORTH, labelResourceIdLabel, 6, SpringLayout.SOUTH, labelTitle);
		labelResourceIdLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelResourceIdLabel);
		
		labelResourceId = new JLabel("ID");
		springLayout.putConstraint(SpringLayout.NORTH, labelResourceId, 6, SpringLayout.SOUTH, labelTitle);
		springLayout.putConstraint(SpringLayout.WEST, labelResourceId, 6, SpringLayout.EAST, labelResourceIdLabel);
		springLayout.putConstraint(SpringLayout.EAST, labelResourceId, 50, SpringLayout.EAST, labelResourceIdLabel);
		labelResourceId.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(labelResourceId);
		
		checkBoxSaveAsText = new JCheckBox("org.multipage.generator.textSavedAsText");
		springLayout.putConstraint(SpringLayout.NORTH, checkBoxSaveAsText, 2, SpringLayout.SOUTH, labelTitle);
		springLayout.putConstraint(SpringLayout.WEST, checkBoxSaveAsText, 3, SpringLayout.EAST, labelResourceId);
		springLayout.putConstraint(SpringLayout.EAST, labelTitle, 0, SpringLayout.EAST, checkBoxSaveAsText);
		checkBoxSaveAsText.setVerticalAlignment(SwingConstants.TOP);
		checkBoxSaveAsText.setVerticalTextPosition(SwingConstants.TOP);
		checkBoxSaveAsText.setOpaque(false);
		checkBoxSaveAsText.setMargin(new Insets(2, 0, 2, 2));
		checkBoxSaveAsText.setHorizontalTextPosition(SwingConstants.LEFT);
		checkBoxSaveAsText.setHorizontalAlignment(SwingConstants.LEFT);
		checkBoxSaveAsText.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(checkBoxSaveAsText);
		
		labelImage = new JLabel("");
		springLayout.putConstraint(SpringLayout.NORTH, labelImage, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelImage, 0, SpringLayout.SOUTH, this);
		add(labelImage);
		
		checkVisible = new JCheckBox("org.multipage.generator.textResourceVisibleCheck");
		springLayout.putConstraint(SpringLayout.WEST, labelImage, 20, SpringLayout.EAST, checkVisible);
		springLayout.putConstraint(SpringLayout.EAST, labelImage, 86, SpringLayout.EAST, checkVisible);
		springLayout.putConstraint(SpringLayout.WEST, checkVisible, 10, SpringLayout.EAST, checkBoxSaveAsText);
		checkVisible.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkVisible.setOpaque(false);
		springLayout.putConstraint(SpringLayout.NORTH, checkVisible, 2, SpringLayout.SOUTH, labelTitle);
		checkVisible.setHorizontalTextPosition(SwingConstants.LEFT);
		checkVisible.setHorizontalAlignment(SwingConstants.LEFT);
		add(checkVisible);
	}
}
