/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 17-09-2021
 *
 */
 
package org.multipage.gui;

import java.awt.Graphics;

import javax.swing.JTextPane;

/**
 * @author vakol
 *
 */
public class RendererJTextPane extends JTextPane {

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
	public RendererJTextPane() {
		
		setOpaque(false);
	}
	
	/**
	 * Set properties.
	 */
	public RendererJTextPane set(boolean isSelected, boolean hasFocus, int index) {
		
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
