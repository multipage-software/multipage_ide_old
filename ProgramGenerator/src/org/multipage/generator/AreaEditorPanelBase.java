/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 08-04-2021
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.maclan.Area;
import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public abstract class AreaEditorPanelBase extends AreaEditorCommonBase {
	
	/**
	 * Panel object.
	 */
	private JPanel panel = new JPanel();
	
	/**
	 * Constructor.
	 * @param parentComponent
	 * @param area
	 */
	public AreaEditorPanelBase(Component parentComponent, Area area) {
		super(parentComponent, area);
		
		// Set lambda functions that are used in the base class methods.
		getWindowLambda = () -> {
			return Utility.findWindow(panel);
		};
		
		getTitleLambda = null;
		
		setTitleLambda = null;
		
		setIconImageLambda = null;
		
		getBoundsLambda = () -> {
			return panel.getBounds();
		};
		
		setBoundsLambda = bounds -> {
			panel.setBounds(bounds);
		};
		
		disposeLambda = null;
	}
	
	/**
	 * Set the panel visible.
	 * @param flag
	 */
	public void setVisible(boolean flag) {
		
		// Delegate the call.
		panel.setVisible(flag);
	}
	
	/**
	 * Set layout of the panel.
	 * @param layoutManager
	 */
	protected void setLayout(LayoutManager layoutManager) {
		
		// Delegate the call.
		panel.setLayout(layoutManager);
	}
	
	/**
	 * Add new component to the panel.
	 * @param component
	 */
	protected void add(Component component) {
		
		// Delegate the call.
		panel.add(component);
	}
}
