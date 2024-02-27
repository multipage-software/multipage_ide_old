/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;


/**
 * @author
 *
 */
public class ElementProperties extends JPanel implements NonCyclingReceiver, Closable {

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
	 * MessagePanel panel.
	 */
	private MessagePanel message = new MessagePanel();

	/**
	 * Areas editor.
	 */
	private AreasPropertiesBase areaEditor;
	
	/**
	 * List of previous update messages.
	 */
	private LinkedList<Message> previousMessages = new LinkedList<Message>();
	
	/**
	 * Constructor.
	 */
	public ElementProperties() {
		
		areaEditor = newAreasProperties(true);
		
		setLayout(new BorderLayout());
				
		// Initialize.
		setNoArea();
		
		// Set listener.
		setListeners();
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
	 * Set listeners.
	 */
	private void setListeners() {
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
		});
		
		// Receive update signal.
		ApplicationEvents.receiver(this, UpdateSignal.updateAreasDiagram, ApplicationEvents.HIGH_PRIORITY, message -> {
			
			// Set new areas to display.
			HashSet<Long> selectedAreaIds = message.getRelatedInfo();
			if (selectedAreaIds != null && !selectedAreaIds.isEmpty()) {
				
				LinkedList<Area> areas = ProgramGenerator.getAreas(selectedAreaIds);
				setAreas(areas);
			}
			else {
				setNoArea();
			}
		});
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
	 * Set no area.
	 */
	public void setNoArea() {
		
		// Delegate call.
		areaEditor.setSaveChanges();
		
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
	 * View area editor.
	 */
	private void viewAreaEditor() {
	
		removeAll();
		add(areaEditor, BorderLayout.CENTER);
		
		revalidate();
		Utility.repaintLater(this);
	}

	/**
	 * @return the areaEditor
	 */
	public AreasPropertiesBase getAreaEditor() {
		return areaEditor;
	}
	
	/**
	 * Get list of previous update messages.
	 */
	@Override
	public LinkedList<Message> getPreviousMessages() {
		
		// TODO: <---TEST Previous messages.
		j.log("ElementProperties %s;", previousMessages);
		
		return previousMessages;
	}
	
	/**
	 * Close element properties panel.
	 */
	@Override
	public void close() {
		
		// Close area properties.
		areaEditor.close();
		// Remove listeners.
		removeListeners();
	}
	
	/**
	 * Remove listeners.
	 */
	private void removeListeners() {
		
		// Remove event receivers.
		ApplicationEvents.removeReceivers(this);
	}	
}
