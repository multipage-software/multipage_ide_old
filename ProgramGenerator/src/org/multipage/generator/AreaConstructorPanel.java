/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 09-06-2021
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.maclan.ConstructorHolder;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author vakol
 *
 */
public class AreaConstructorPanel extends JPanel {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor reference.
	 */
	private ConstructorHolder constructor = null;
	
	/**
	 * ID of current area.
	 */
	private Long areaId = null;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelConstructorName;
	private JTextField textConstructorName;
	private SlotListPanel panelSlots;
	private JTextField textConstructorAreaId;
	
	/**
	 * Create the panel.
	 */
	public AreaConstructorPanel() {

		initComponents();
		postCreate(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.SOUTH);
		
		JPanel panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(10, 30));
		add(panelTop, BorderLayout.NORTH);
		SpringLayout sl_panelTop = new SpringLayout();
		panelTop.setLayout(sl_panelTop);
		
		labelConstructorName = new JLabel("org.multipage.generator.textConstructorPathName");
		sl_panelTop.putConstraint(SpringLayout.NORTH, labelConstructorName, 6, SpringLayout.NORTH, panelTop);
		sl_panelTop.putConstraint(SpringLayout.WEST, labelConstructorName, 10, SpringLayout.WEST, panelTop);
		panelTop.add(labelConstructorName);
		
		textConstructorName = new TextFieldEx();
		sl_panelTop.putConstraint(SpringLayout.EAST, textConstructorName, -6, SpringLayout.EAST, panelTop);
		textConstructorName.setEditable(false);
		sl_panelTop.putConstraint(SpringLayout.NORTH, textConstructorName, 6, SpringLayout.NORTH, panelTop);
		panelTop.add(textConstructorName);
		textConstructorName.setColumns(10);
		
		textConstructorAreaId = new TextFieldEx();
		sl_panelTop.putConstraint(SpringLayout.WEST, textConstructorName, 3, SpringLayout.EAST, textConstructorAreaId);
		sl_panelTop.putConstraint(SpringLayout.WEST, textConstructorAreaId, 6, SpringLayout.EAST, labelConstructorName);
		textConstructorAreaId.setEditable(false);
		sl_panelTop.putConstraint(SpringLayout.NORTH, textConstructorAreaId, 0, SpringLayout.NORTH, labelConstructorName);
		panelTop.add(textConstructorAreaId);
		textConstructorAreaId.setColumns(8);
		//$hide>>$
		panelSlots = new SlotListPanel();
		add(panelSlots, BorderLayout.CENTER);
		//$hide<<$
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelConstructorName);
	}

	/**
	 * 
	 * @param area
	 */
	public void setArea(Area area) {
		
		// Free old values.
		this.areaId = null;
		this.constructor = null;
		
		// Check input value.
		if (area == null) {
			return;
		}
		
		// Initialization.
		this.areaId = area.getId();
		Obj<Long> constructorId = new Obj<Long>();
		ConstructorHolder constructor = new ConstructorHolder();
		
		// Load area constructor.
		try {
			Middle middle = ProgramBasic.loginMiddle();
			
			MiddleResult result = middle.loadAreaConstructor(areaId, constructorId);
			result.throwPossibleException();
			
			if (constructorId.ref != null) {
				
				result = middle.loadConstructorHolder(constructorId.ref, constructor);
				result.throwPossibleException();
				
				// Set the constructor.
				this.constructor = constructor;
			}
		}
		catch (Exception e) {
			
			Utility.show2(this, e.getLocalizedMessage());
			return;
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		
		// Load controls.
		loadControls();
	}
	
	/**
	 * Load dialog controls.
	 */
	private void loadControls() {
		
		// Display constructor name and associated area ID.
		textConstructorName.setText(getConstructorName());
		textConstructorAreaId.setText(getConstrcutorAreaId());
		
		// Get constructor area.
		Area constructorArea = null;
		if (this.constructor != null) {
			
			long areaId = this.constructor.getAreaId();
			constructorArea = ProgramGenerator.getArea(areaId);
		}
		
		// Check if constructor area exists.
		if (constructorArea == null) {
			return;
		}
		
		// Load slot values.
		try {
			Middle middle = ProgramBasic.loginMiddle();
			MiddleResult result = middle.loadSlots(constructorArea, true);
			result.throwPossibleException();
		}
		catch (Exception e) {
			Utility.show2(this, e.getLocalizedMessage());
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		
		// Load the slot list view.
		panelSlots.setArea(constructorArea);
	}
	
	/**
	 * Gets constructor name.
	 * @return
	 */
	private String getConstructorName() {
		
		String name = this.constructor == null ? "" : this.constructor.getNameText();
		return name;
	}
	
	/**
	 * Get constructor area ID.
	 * @return
	 */
	private String getConstrcutorAreaId() {
		
		// Check constructor.
		if (this.constructor == null) {
			return "";
		}
		
		long areaId = this.constructor.getAreaId();
		return Long.toString(areaId);
	}

}
