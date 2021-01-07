/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.*;

/**
 * @author
 *
 */
public class StatusBar extends JPanel {
	
	/**
	 * Status bar height.
	 */
	protected static final int height = 25;
	
	/**
	 * Font size.
	 */
	protected static final int fontSize = 12;
	
	/**
	 * Font.
	 */
	protected static Font font = new Font("SansSerif", Font.PLAIN, fontSize);

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public StatusBar() {
		
		setPreferredSize(new Dimension(0, height));
		setBackground(new Color(220, 220, 220));
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		int width = getWidth();
		
		// Draw top line.
		g.setColor(Color.BLACK);
		g.drawLine(0, 0, width, 0);
	}
}
