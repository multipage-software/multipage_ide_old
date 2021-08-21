/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.maclan.Area;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;


/**
 * @author
 *
 */
public class PropertiesPanel extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Maximized width.
	 */
	private static final int maxWidth = 300;
	
	/**
	 * Get maximized width.
	 */
	public static int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * Message panel.
	 */
	private MessagePanel message = new MessagePanel();

	/**
	 * Areas editor.
	 */
	private AreasPropertiesBase areaEditor;
	
	/**
	 * Constructor.
	 */
	public PropertiesPanel() {
		
		
		// Create area editor.
		areaEditor = newAreasProperties(true);
		
		setLayout(new BorderLayout());
				
		// Initialize.
		setNoProperties();
		
		// Set listener.
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
	}
	
	/**
	 * Create new area properties object.
	 * @param isPropertiesPanel
	 * @return
	 */
	protected AreasPropertiesBase newAreasProperties(boolean isPropertiesPanel) {
		
		return ProgramGenerator.newAreasProperties(isPropertiesPanel);
	}

	/**
	 * Edit areas.
	 */
	public void setAreas(LinkedList<Area> areas) {

		// Delegate call.
		areaEditor.setAreas(areas);
		viewAreaEditor();
	}

	/**
	 * Set no properties.
	 */
	public void setNoProperties() {
		
		// Set message.
		message.setText(Resources.getString("org.multipage.generator.textNoAreaSelected"));
		viewMessage();
	}

	/**
	 * View message.
	 */
	private void viewMessage() {

		removeAll();
		add(message, BorderLayout.CENTER);
		
		revalidate();
		Utility.repaintLater(this);
	}

	/**
	 * View area properties editor.
	 */
	private void viewAreaEditor() {
	
		removeAll();
		add(areaEditor, BorderLayout.CENTER);
		
		revalidate();
		Utility.repaintLater(this);
	}

	/**
	 * Get the properties editor.
	 * @return the areaEditor
	 */
	public AreasPropertiesBase getPropertiesEditor() {
		return areaEditor;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {

		areaEditor.dispose();
	}
}
