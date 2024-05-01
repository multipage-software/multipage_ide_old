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
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.maclan.Area;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.GuiSignal;
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
		
		initComponents();
		postCreate();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {

		// Create area editor.
		areaEditor = newAreasProperties(true);
		setLayout(new BorderLayout());
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Initialize.
		setNoProperties();
		setListeners();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// Set listener.
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
		
		// Receive the "display area properties" messages.
		ApplicationEvents.receiver(this, GuiSignal.displayAreaProperties, message -> {
			
			HashSet<Long> selectedAreaIds = message.getRelatedInfo();
			
			if (!selectedAreaIds.isEmpty()) {
				setAreas(selectedAreaIds);
			}
			else {
				setNoProperties();
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
	 * Set areas.
	 * @param selectedAreaIds
	 */
	public void setAreas(HashSet<Long> selectedAreaIds) {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		selectedAreaIds.forEach(areaId -> {
			
			Area area = ProgramGenerator.getArea(areaId);
			if (area == null) {
				return;
			}
			
			areas.add(area);
		});
		
		// Delegate call.
		setAreas(areas);
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

		ApplicationEvents.removeReceivers(this);
		areaEditor.dispose();
	}
}
