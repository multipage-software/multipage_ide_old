/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;


/**
 * A private subclass of the default highlight painter
 * @author
 *
 */
class FindHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
	
	/**
	 * Constructor.
	 * @param color
	 */
    public FindHighlightPainter(Color color) {
        super(color);
    }

	/* (non-Javadoc)
	 * @see javax.swing.text.DefaultHighlighter.DefaultHighlightPainter#paint(java.awt.Graphics, int, int, java.awt.Shape, javax.swing.text.JTextComponent)
	 */
	@Override
	public void paint(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c) {
		// Set transparency.
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		super.paint(g, offs0, offs1, bounds, c);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.DefaultHighlighter.DefaultHighlightPainter#paintLayer(java.awt.Graphics, int, int, java.awt.Shape, javax.swing.text.JTextComponent, javax.swing.text.View)
	 */
	@Override
	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c, View view) {
		// Set transparency.
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
		Shape shape = super.paintLayer(g, offs0, offs1, bounds, c, view);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		return shape;
	}
}