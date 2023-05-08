/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.Utility;

import java.awt.*;

import javax.swing.*;

import org.multipage.generator.*;
import org.maclan.Resource;

/**
 * @author
 *
 */
public class NamespaceResourceRendererBuilder extends
		NamespaceResourceRendererBase {

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
	private JCheckBox checkProtected;

	/**
	 * Create the panel.
	 */
	public NamespaceResourceRendererBuilder() {
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
		setPreferredSize(new Dimension(670, 62));
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
		labelImage.setPreferredSize(new Dimension(62, 62));
		springLayout.putConstraint(SpringLayout.NORTH, labelImage, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelImage, 0, SpringLayout.SOUTH, this);
		add(labelImage);
		
		checkVisible = new JCheckBox("org.multipage.generator.textVisible");
		springLayout.putConstraint(SpringLayout.WEST, checkVisible, 10, SpringLayout.EAST, checkBoxSaveAsText);
		checkVisible.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkVisible.setOpaque(false);
		springLayout.putConstraint(SpringLayout.NORTH, checkVisible, 2, SpringLayout.SOUTH, labelTitle);
		checkVisible.setHorizontalTextPosition(SwingConstants.LEFT);
		checkVisible.setHorizontalAlignment(SwingConstants.LEFT);
		add(checkVisible);
		
		checkProtected = new JCheckBox("builder.textResourceProtectedCheck");
		springLayout.putConstraint(SpringLayout.NORTH, checkProtected, 2, SpringLayout.SOUTH, labelTitle);
		springLayout.putConstraint(SpringLayout.WEST, labelImage, 20, SpringLayout.EAST, checkProtected);
		springLayout.putConstraint(SpringLayout.WEST, checkProtected, 10, SpringLayout.EAST, checkVisible);
		checkProtected.setOpaque(false);
		checkProtected.setHorizontalTextPosition(SwingConstants.LEFT);
		checkProtected.setHorizontalAlignment(SwingConstants.LEFT);
		checkProtected.setFont(new Font("Tahoma", Font.ITALIC, 11));
		add(checkProtected);
	}

	/* (non-Javadoc)
	 * @see org.multipage.generator.NamespaceResourceRendererBase#localize()
	 */
	@Override
	protected void localize() {
		
		super.localize();
		
		Utility.localize(checkProtected);
	}

	/* (non-Javadoc)
	 * @see org.multipage.generator.NamespaceResourceRendererBase#setProperties(com.maclan.Resource, java.lang.String, int, boolean, boolean)
	 */
	@Override
	public void setProperties(Resource resource, String mimeType, int index,
			boolean isSelected, boolean hasFocus) {
		
		super.setProperties(resource, mimeType, index, isSelected, hasFocus);
		
		checkProtected.setSelected(resource.isProtected());
	}
}
