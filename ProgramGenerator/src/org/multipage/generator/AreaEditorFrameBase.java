/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 09-04-2021
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;

import org.maclan.Area;
import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public abstract class AreaEditorFrameBase extends AreaEditorCommonBase {

	/**
	 * Version.
	 */
	protected static final long serialVersionUID = 1L;
	
	/**
	 * Frame object.
	 */
	private JFrame frame = new JFrame();
		
	/**
	 * Constructor.
	 * @param parentComponent
	 * @param area
	 */
	public AreaEditorFrameBase(Component parentComponent, Area area) {
		super(parentComponent, area);
		
		// Set lambda functions that are used in the base class methods.
		getWindowLambda = () -> {
			return Utility.findWindow(frame);
		};
		
		getTitleLambda = () -> {
			return frame.getTitle();
		};
		
		setTitleLambda = title -> {
			frame.setTitle(title);
		};
		
		setIconImageLambda = icon -> {
			frame.setIconImage(icon);
		};
		
		getBoundsLambda = () -> {
			return frame.getBounds();
		};
		
		setBoundsLambda = bounds -> {
			frame.setBounds(bounds);
		};
		
		disposeLambda = () -> {
			frame.dispose();
		};
	}
	
	/**
	 * Set the frame visible.
	 * @param flag
	 */
	public void setVisible(boolean flag) {
		
		// Delegate the call.
		frame.setVisible(flag);
	}
	
	/**
	 * Set content panel of the frame.
	 * @param contentPane
	 */
	protected void setContentPane(Container contentPane) {
		
		// Delegate the call.
		frame.setContentPane(contentPane);
	}
	
	/**
	 * Set boundaries of the frame.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected void setBounds(int x, int y, int width, int height) {
		
		// Delegate the call.
		frame.setBounds(x, y, width, height);
		
	}
	
	/**
	 * Set default close operation of the frame.
	 * @param operation
	 */
	protected void setDefaultCloseOperation(int operation) {
		
		// Delegate the call.
		frame.setDefaultCloseOperation(operation);
	}
	
	/**
	 * Add window listener to the frame.
	 * @param windowAdapter
	 */
	protected void addWindowListener(WindowAdapter windowAdapter) {
		
		// Delegate the call.
		frame.addWindowListener(windowAdapter);
	}
	
	/**
	 * Set minimum frame size.
	 * @param dimension
	 */
	protected void setMinimumSize(Dimension dimension) {
		
		// Delegate the call.
		frame.setMinimumSize(dimension);
	}
}
