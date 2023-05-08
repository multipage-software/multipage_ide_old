/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;

import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.multipage.gui.Images;
import org.multipage.gui.ToolBarKit;
import org.multipage.util.Resources;
import org.multipage.generator.*;
import org.maclan.Area;

/**
 * @author
 *
 */
public class BuilderMainFrame extends GeneratorMainFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		// Load show hidden slots button state.
		SlotListPanelBuilder.showHiddenSlots = inputStream.readBoolean();
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {
		
		// Save show hidden slots button state.
		outputStream.writeBoolean(SlotListPanelBuilder.showHiddenSlots);
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		SlotListPanelBuilder.showHiddenSlots = false;
	}
	
	/**
	 * Show id toggle button.
	 */
	private JToggleButton showHiddenSlotsButton;
	
	/**
	 * Constructor.
	 */
	public BuilderMainFrame() {
	
		super();
		setTitle(Resources.getString("builder.textMainFrameCaption"));
	}
	
	/**
	 * Create tool bar..
	 */
	@Override
	protected void addHideSlotsButton(JToolBar toolBar) {
		
		toolBar.addSeparator();
		showHiddenSlotsButton = ToolBarKit.addToggleButton(toolBar, "program/builder/images/show_hide_slots.png", this, "onHideSlots", "builder.tooltipShowHideSlots");
	}
	
	/**
	 * On hide slots.
	 */
	public void onHideSlots() {
		
		SlotListPanelBuilder.showHiddenSlots = showHiddenSlotsButton.isSelected();
		updateInformation();
	}
	
	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Load dialog.
	 */
	@Override
	protected void loadDialog() {
		
		super.loadDialog();
		
		// Set show hidden slots button state.
		showHiddenSlotsButton.setSelected(SlotListPanelBuilder.showHiddenSlots);
	}
	
	/**
	 * Save dialog.
	 */
	@Override
	protected void saveDialog() {
		
		super.saveDialog();
		
		// Save show hidden slots button state.
		SlotListPanelBuilder.showHiddenSlots = showHiddenSlotsButton.isSelected();
	}
	
	/*
	 * Add edit enumerations trayMenu item.
	 */
	@Override
	protected void addEditEnumerationsMenuItem(JMenu menu) {
		
		JMenuItem editEnumerations = new JMenuItem(Resources.getString("builder.menuEditEnumerations"));
			editEnumerations.setAccelerator(KeyStroke.getKeyStroke("control alt E"));
			editEnumerations.setIcon(Images.getIcon("org/multipage/generator/images/enumerations.png"));
			
		menu.add(editEnumerations);
	
		editEnumerations.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onEnumerations();
			}
		});
	}
	
	/**
	 * On enumerations.
	 */
	protected void onEnumerations() {
		
		EnumerationsEditorDialog.showDialog(this);
	}
	
	/**
	 * Add edit versions trayMenu item.
	 * @param trayMenu
	 */
	@Override
	protected void addEditVersionsMenuItem(JMenuItem menu) {
			
		JMenuItem versions = new JMenuItem(Resources.getString("builder.menuEditVersions"));
			versions.setAccelerator(KeyStroke.getKeyStroke("control V"));
			versions.setIcon(Images.getIcon("org/multipage/generator/images/version_icon.png"));
			
		menu.add(versions);
			
		versions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onVersions();
			}
		});
	}

	/**
	 * On versions editor.
	 */
	protected void onVersions() {
		
		VersionsEditor.showDialog(this);
		
		updateInformation();
	}
	

	/**
	 * Add search in text resources trayMenu item.
	 * @param trayMenu
	 */
	@Override
	protected void addSearchInTextResourcesMenuItem(JMenu menu) {
		
		JMenuItem toolsSearchInTextResources = new JMenuItem(Resources.getString("builder.menuToolsSearchInTextResources"));
			toolsSearchInTextResources.setAccelerator(KeyStroke.getKeyStroke("control alt T"));
			toolsSearchInTextResources.setIcon(Images.getIcon("org/multipage/generator/images/search_resources.png"));
		
		menu.add(toolsSearchInTextResources);
		
		toolsSearchInTextResources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSearchInTextResources();
			}
		});
	}

	/**
	 * Search in text resources.
	 */
	protected void onSearchInTextResources() {
		
		// Get selected areas.
		LinkedList<Area> areas = getSelectedAreas();
		// Show dialog.
		SearchTextResources.showDialog(this, areas);
	}
}
