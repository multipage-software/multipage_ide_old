/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * @author
 *
 */
public class HorizontalScroll extends ScrollShape {

	/**
	 * Height.
	 */
	private static final int height = 20;
	
	/**
	 * Get height.
	 * @return
	 */
	public static int getHeight() {

		return height;
	}

	/**
	 * Location.
	 */
	private int x1;
	private int y1;
	private int x2;

	/**
	 * Constructor.
	 * @param cursor
	 */
	public HorizontalScroll(Cursor cursor, Component component) {
		super(cursor, component);
	}

	/**
	 * Set location.
	 */
	public void setLocation(int x1, int y1, int x2) {

		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		
		// Set cursor area.
		getCursorArea().setShape(
				new Rectangle(x1, y1, x2 - x1, getHeight()));
	}

	/**
	 * Draw horizontal scroll.
	 */
	public void draw(Graphics2D g2, Color color) {

		if (isVisible()) {
			int width = x2 - x1;
			
			// Get image.
			BufferedImage leftImage = Images.getImage("org/multipage/gui/images/left.png");
			BufferedImage rightImage = Images.getImage("org/multipage/gui/images/right.png");
			
			g2.setColor(color);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			// Draw outlines.
			g2.drawRect(x1, y1, width - 1, height - 1);
			// Draw right and left buttons.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.drawImage(leftImage, null, x1 + 2, y1 + 2);
			g2.drawImage(rightImage, null, x2 - 18, y1 + 2);
			// Draw lines.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2.drawLine(x1 + 20, y1, x1 + 20, y1 + height);
			g2.drawLine(x2 - 20, y1, x2 - 20, y1 + height);
			// Fill rectangle.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			g2.fillRect(x1, y1, width, height);
			
			
			int size = sliderSize(),
			    start = sliderStart();
			// Draw and fill slider.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2.drawRect(start, y1, size, y1 + height);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			g2.fillRect(start, y1, size, y1 + height);
		}
	}

	/**
	 * Return slider start.
	 * @return
	 */
	private int sliderStart() {
		
		int width = x2 - x1;
		int inner = width - 2 * 20;
		int start = x1 + 20 + (int)(inner * (win.getX() - content.getX()) / content.getWidth());
		int size = sliderSize();
		int end = start + size;
		
		// If the end of the slide exceeds boundaries set new start.
		if (end > x2 - 20) {
			start = x2 - 20 - size;
		}
		
		return start;
	}
	
	/**
	 * Get slider end.
	 */
	private int sliderEnd() {
		
		return sliderStart() + sliderSize();
	}

	/**
	 * Return slider size.
	 * @return
	 */
	private int sliderSize() {
		
		int width = x2 - x1;
		int inner = width - 2 * 20;
		
		int size = (int)(inner * win.getWidth() / content.getWidth());
		if (size < minimumSliderSize) {
			size = minimumSliderSize;;
		}
		return size;
	}

	/**
	 * Returns true if scroll bar contains point.
	 * @param point
	 * @return
	 */
	public boolean contains(Point point) {

		if (isVisible()) {
			Rectangle rect = new Rectangle(x1, y1, x2 - x1, height);
			return rect.contains(point);
		}
		return false;
	}
	
	/**
	 * Is visible.
	 */
	@Override
	public boolean isVisible() {
		
		boolean visible = false;
		
		if (win != null && content != null) {
			visible = content.getMinX() < win.getMinX() || content.getMaxX() > win.getMaxX();
		}
		return visible;
	}

	/**
	 * On mouse pressed.
	 * @param mouse
	 */
	public void onMousePressed(Point mouse) {
		
		// If the mouse is outside the rectangle, exit the method.
		if (!contains(mouse)) {
			return;
		}

		final Rectangle2D winRect = win.getBounds2D();
		final double step = win.getWidth() * stepPercent / 100;
		
		// If right part affected.
		if (mouse.getX() >= sliderEnd()) {
			
			// Create and schedule timer.
			timer = new javax.swing.Timer(0, null);
			timer.setDelay((int) repeatMs);
			
			// Set action command.
			timer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					double newX;
					newX = win.getX() + step;
					if (newX + win.getWidth() > content.getX() + content.getWidth()) {
						newX = content.getX() + content.getWidth() - win.getWidth();
					}
					win.setRect(newX, 0, winRect.getWidth(), 1);
					invokeOnScroll();
				}
			});

			// Start timer.
			timer.start();
		}
		// If left part affected.
		else if (mouse.getX() <= sliderStart()) {
		
			// Create and schedule timer.
			timer = new javax.swing.Timer(0, null);
			timer.setDelay((int) repeatMs);
			
			// Set action command.
			timer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					double newX;
					newX = win.getX() - step;
					if (newX < content.getX()) {
						newX = content.getX();
					}
					win.setRect(newX, 0, winRect.getWidth(), 1);
					invokeOnScroll();
				}
			});
			
			// Start timer.
			timer.start();
		}
		// If slider affected.
		else {
			// Set pressed position.
			pressedPosition = new Point(mouse);
		}
	}

	/**
	 * On mouse dragged.
	 * @param e
	 */
	public void onMouseDragged(MouseEvent e) {

		int width = x2 - x1;
		int inner = width - 2 * 20;
		
		delta = (e.getPoint().x - pressedPosition.getX()) * content.getWidth() / inner;
		
		double newX = win.getX() + delta;
		if (newX < content.getX()) {
			newX = content.getX();
		}
		if (newX + win.getWidth() > content.getX() + content.getWidth()) {
			newX = content.getX() + content.getWidth() - win.getWidth();
		}
		win.setRect(newX, 0, win.getWidth(), 1);
		// Invoke listener.
		invokeOnScroll();
		
		pressedPosition = e.getPoint();
		delta = 0.0;
	}

	/**
	 * Get rectangle.
	 * @return
	 */
	public Rectangle getRect() {

		return new Rectangle(x1, y1, x2 - x1, height);
	}
}
