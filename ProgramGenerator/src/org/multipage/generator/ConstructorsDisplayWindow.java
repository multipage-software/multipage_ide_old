/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import org.multipage.gui.*;

import com.maclan.*;

/**
 * @author
 *
 */
public class ConstructorsDisplayWindow extends JWindow {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Content panel.
	 */
	private DisplayPanel contentPanel;
	
	/**
	 * Area reference.
	 */
	private Area area;

	/**
	 * Constructor.
	 * @param parent 
	 */
	public ConstructorsDisplayWindow(Component parent) {
		
		super(Utility.findWindow(parent));
		
		contentPanel = new DisplayPanel();
		contentPanel.setBackground(UIManager.getColor("Panel.background"));
		setContentPane(contentPanel);
		
		contentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
	}

	/**
	 * Set area.
	 * @param area
	 */
	private void setArea(Area area) {
		
		this.area = area;
		contentPanel.area = area;
	}

	/**
	 * Show area constructors.
	 * @param area
	 * @param topLeft
	 */
	public void showw(Area area, Point topLeft) {
		
		// Set window location and visibility.
		setLocation(topLeft);
		setVisible(true);

		// If the area is not changed, exit the method.
		if (area.equals(this.area)) {
			return;
		}
		
		setArea(area);
		
		// Set window dimension.
		Dimension size = contentPanel.computeSize();
		setSize(size);	
	}

	/**
	 * Hide window.
	 */
	public void hidew() {
		
		dispose();
	}
}

/**
 * Display panel.
 * @author
 *
 */
class DisplayPanel extends JPanel {

	/**
	 * Area reference.
	 */
	public Area area;

	/**
	 * Font.
	 */
	private Font font = new Font(Font.DIALOG, Font.PLAIN, 10);
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 */
	public DisplayPanel() {
		
		// Set background color.
		setBackground(new Color(255, 255, 100));
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		if (area != null) {
			
			// Get line height.
			FontMetrics metrics = g.getFontMetrics(font);
			int lineHeight = metrics.getHeight();
			
			// Paint constructor names.
			int lineYPosition = lineHeight;
			
			for (String constructorName : area.getConstructorHoldersNames()) {
				
				// Draw name.
				g.drawString(constructorName, 3, lineYPosition);
				
				lineYPosition += lineHeight;
			}
		}
	}

	/**
	 * Compute window size.
	 * @return
	 */
	public Dimension computeSize() {
		
		if (area == null) {
			return new Dimension();
		}
		
		int linesCount = area.getConstructorHoldersCount();
		if (linesCount == 0) {
			return new Dimension();
		}
		
		Graphics graphics = getGraphics();
		FontMetrics metrics = graphics.getFontMetrics(font);
		
		// Get text height.
		int height = metrics.getHeight() * linesCount + 6;
		
		// Get maximum line size.
		int maximumLineWidth = 0;

		// Do loop for all names.
		for (String constructorHolderName : area.getConstructorHoldersNames()) {
			
			int width = metrics.stringWidth(constructorHolderName);
			
			if (width > maximumLineWidth) {
				maximumLineWidth = width;
			}
		}
		
		return new Dimension(maximumLineWidth + 10, height);
	}
}
