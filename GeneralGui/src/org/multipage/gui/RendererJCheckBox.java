/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Graphics;

import javax.swing.JCheckBox;

/**
 * @author
 *
 */
public class RendererJCheckBox extends JCheckBox {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Is selected flag.
	 */
	private boolean isSelected;
	
	/**
	 * Has focus flag.
	 */
	private boolean hasFocus;
	
	/**
	 * Constructor.
	 */
	public RendererJCheckBox() {
		
		setOpaque(false);
	}
	
	/**
	 * Set properties.
	 */
	public void set(boolean isSelected, boolean hasFocus, int index) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		setBackground(Utility.itemColor(index));
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}
}
