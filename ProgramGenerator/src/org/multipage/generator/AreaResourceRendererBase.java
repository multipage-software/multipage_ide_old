/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.maclan.AreaResource;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public class AreaResourceRendererBase extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Is selected flag.
	 */
	private boolean isSelected = false;
	
	/**
	 * Has focus flag.
	 */
	private boolean hasFocus = false;
	
	/**
	 * Components' references.
	 */
	private JLabel labelDescription;
	private JLabel labelNamespacePath;
	private JLabel labelTextNamespace;
	private JLabel labelTextMime;
	private JLabel labelMimeType;
	private JLabel labelTextId;
	private JLabel labelId;
	private JCheckBox checkBoxVisible;
	private JCheckBox checkBoxSavedAsText;
	private JLabel labelVisible;
	private JLabel labelSaveAsText;
	private JLabel labelImage;
	
	/**
	 * Set components' references.
	 * @param labelDescription
	 * @param labelNamespacePath
	 * @param labelTextNamespace
	 * @param labelTextMime
	 * @param labelMimeType
	 * @param labelTextId
	 * @param labelId
	 * @param checkBoxVisible
	 * @param checkBoxSavedAsText
	 * @param labelVisible
	 * @param labelSaveAsText
	 * @param labelImage
	 */
	protected void setComponentsReferences(
			JLabel labelDescription,
			JLabel labelNamespacePath,
			JLabel labelTextNamespace,
			JLabel labelTextMime,
			JLabel labelMimeType,
			JLabel labelTextId,
			JLabel labelId,
			JCheckBox checkBoxVisible,
			JCheckBox checkBoxSavedAsText,
			JLabel labelVisible,
			JLabel labelSaveAsText,
			JLabel labelImage
			) {
		
		this.labelDescription = labelDescription;
		this.labelNamespacePath = labelNamespacePath;
		this.labelTextNamespace = labelTextNamespace;
		this.labelTextMime = labelTextMime;
		this.labelMimeType = labelMimeType;
		this.labelTextId = labelTextId;
		this.labelId = labelId;
		this.checkBoxVisible = checkBoxVisible;
		this.checkBoxSavedAsText = checkBoxSavedAsText;
		this.labelVisible = labelVisible;
		this.labelSaveAsText = labelSaveAsText;
		this.labelImage = labelImage;
	}

	/**
	 * Localize.
	 */
	protected void localize() {

		Utility.localize(labelTextNamespace);
		Utility.localize(labelTextMime);
		Utility.localize(labelVisible);
		Utility.localize(labelTextId);
		Utility.localize(labelSaveAsText);
	}

	/**
	 * Set properties.
	 * @param resource
	 * @param mimeType
	 * @param namespacePath
	 * @param index
	 * @param isSelected
	 * @param hasFocus
	 */
	public void setProperties(AreaResource resource, String mimeType,
			String namespacePath, int index,
			boolean isSelected, boolean hasFocus) {
		
		// Get background color.
		Color backGroundColor = Utility.itemColor(index);
		// Set color.
		setBackground(backGroundColor);
		
		String finalDescription = resource.getLocalDescription().isEmpty() ? 
				String.format("<html><b>%s</b></html>", resource.getDescription()) :
					String.format("<html><b>%s</b>&nbsp;&nbsp;&nbsp;<i>(%s)</i></html>",
							resource.getLocalDescription(), resource.getDescription());
		
		// Set dialog components.
		labelDescription.setText(finalDescription);
		labelNamespacePath.setText(namespacePath);
		labelMimeType.setText(mimeType);
		checkBoxVisible.setSelected(resource.isVisible());
		checkBoxSavedAsText.setSelected(resource.isSavedAsText());
		
		labelId.setText(String.valueOf(resource.getId()));
		
		if (resource.getImage() != null) {
			labelImage.setIcon(new ImageIcon(resource.getImage()));
		}
		else {
			labelImage.setIcon(null);
		}
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		// Paint component.
		super.paint(g);
		// Draw selection.
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}
}
