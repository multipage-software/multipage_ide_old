/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.maclan.Area;
import org.multipage.gui.ToolTipWindow;
import org.multipage.gui.Utility;

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
	private DisplayPanel displayPanel;
	
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
		
		JPanel displayPanelWrapper = new JPanel();
		displayPanelWrapper.setBorder(new LineBorder(Color.BLACK));
		
		setContentPane(displayPanelWrapper);
		
		displayPanel = new DisplayPanel();
		displayPanel.setBackground(UIManager.getColor("Panel.background"));
		displayPanel.setBorder(new EmptyBorder(1, 3, 1, 3));	// Label padding: top, left, bottom, right.
		
		displayPanelWrapper.setLayout(new BorderLayout());
		displayPanelWrapper.add(displayPanel, BorderLayout.CENTER);
	}

	/**
	 * Set area.
	 * @param area
	 */
	private void setArea(Area area) {
		
		this.area = area;
		displayPanel.area = area;
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
		Dimension size = displayPanel.computeSize();
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
	 * Fonts.
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
			
			// Get line height of the caption.
			FontMetrics metrics = g.getFontMetrics(ToolTipWindow.font);
			int lineHeight = metrics.getHeight();
			
			// Paint area description and alias and constructor names.
			int lineYPosition = lineHeight;
			
			// Draw area name.
			g.setFont(ToolTipWindow.font);
			g.drawString(area.getDescription(), 3, lineYPosition);
			
			// Get common line height of the item.
			metrics = g.getFontMetrics(font);
			lineHeight = metrics.getHeight();
			
			lineYPosition += lineHeight;
			
			for (String listItem : area.getConstructorNameList()) {
				
				// Draw name.
				g.setFont(font);
				g.drawString(listItem, 3, lineYPosition);
				
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
		
		int itemCount = area.getConstructorHoldersCount() + 1;
		if (itemCount == 0) {
			return new Dimension();
		}
		
		Graphics g = getGraphics();

		// Get line height of the caption.
		FontMetrics metrics = g.getFontMetrics(ToolTipWindow.font);
		
		// Initialize maximum line width with caption width.
		int maximumLineWidth = metrics.stringWidth(area.getDescription());
		
		// Get caption height.
		int listHeight = metrics.getHeight();
		
		// Get line height of the texts.
		metrics = g.getFontMetrics(font);
		int itemHeight = metrics.getHeight();
		
		// Add text heights.
		listHeight += itemHeight * itemCount;
		
		// Do loop for all names.
		for (String listItem : area.getConstructorNameList()) {
			
			int width = metrics.stringWidth(listItem);
			
			if (width > maximumLineWidth) {
				maximumLineWidth = width;
			}
		}
		
		return new Dimension(maximumLineWidth + 10, listHeight);
	}
}
