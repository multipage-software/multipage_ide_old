/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Graphics;

import javax.swing.JLabel;

/**
 * @author
 *
 */
public class RendererJLabel extends JLabel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Is selected flag.
	 */
	protected boolean isSelected;
	
	/**
	 * Has focus flag.
	 */
	protected boolean hasFocus;
	
	/**
	 * Constructor.
	 */
	public RendererJLabel() {
		
		setOpaque(false);
	}
	
	/**
	 * Set properties.
	 */
	public RendererJLabel set(boolean isSelected, boolean hasFocus, int index) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		setBackground(Utility.itemColor(index));
		
		return this;
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
