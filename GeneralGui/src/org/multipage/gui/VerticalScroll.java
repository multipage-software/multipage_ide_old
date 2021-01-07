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
public class VerticalScroll extends ScrollShape {

	/**
	 * Width.
	 */
	private static final int width = 20;

	/**
	 * Get width.
	 * @return
	 */
	public static int getWidth() {

		return width;
	}
	
	/**
	 * Location.
	 */
	private int x1;
	private int y1;
	private int y2;

	/**
	 * Constructor.
	 * @param cursor
	 */
	public VerticalScroll(Cursor cursor, Component component) {
		super(cursor, component);
	}

	/**
	 * Set location.
	 */
	public void setLocation(int x1, int y1, int y2) {

		this.x1 = x1;
		this.y1 = y1;
		this.y2 = y2;
		
		// Set cursor area.
		getCursorArea().setShape(
				new Rectangle(x1, y1, getWidth(), y2 - y1));
	}

	/**
	 * Draw horizontal scroll.
	 */
	public void draw(Graphics2D g2, Color color) {
		
		if (isVisible()) {

			int height = y2 - y1;
			
			BufferedImage topImage = Images.getImage("org/multipage/gui/images/top.png");
			BufferedImage bottomImage = Images.getImage("org/multipage/gui/images/bottom.png");

			g2.setColor(color);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			// Draw outlines.
			g2.drawRect(x1, y1, width - 1, height - 1);
			// Draw top and bottom buttons.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.drawImage(topImage, null, x1 + 2, y1 + 2);
			g2.drawImage(bottomImage, null, x1 + 2, y2 - 18);
			// Draw lines.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2.drawLine(x1, y1 + 20, x1 + width, y1 + 20);
			g2.drawLine(x1, y2 - 20, x1 + width, y2 - 20);
			// Fill rectangle.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			g2.fillRect(x1, y1, width, height);
			
			int size = sliderSize(),
			    start = sliderStart();
			// Draw and fill slider.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2.drawRect(x1, start, x1 + width, size);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			g2.fillRect(x1, start, x1 + width, size);
		}
	}

	/**
	 * Return slider start.
	 * @return
	 */
	private int sliderStart() {
		
		int height = y2 - y1;
		int inner = height - 2 * 20;
		int start = y1 + 20 + (int)(inner * (win.getY() - content.getY()) / content.getHeight());
		int size = sliderSize();
		int end = start + size;
		
		// If the end of the slide exceed boundaries set new start.
		if (end > y2 - 20) {
			start = y2 - 20 - size;
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
		
		int height = y2 - y1;
		int inner = height - 2 * 20;
		
		int size = (int)(inner * win.getHeight() / content.getHeight());
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
			Rectangle rect = new Rectangle(x1, y1, width, y2 - y1);
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
			visible = content.getMinY() < win.getMinY() || content.getMaxY() > win.getMaxY();
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
		final double step = win.getHeight() * stepPercent / 100;
		
		// If bottom part affected.
		if (mouse.getY() >= sliderEnd()) {
			
			// Create and schedule timer.
			timer = new javax.swing.Timer(0, null);
			timer.setDelay((int) repeatMs);
			
			// Set action command.
			timer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					double newY;
					newY = win.getY() + step;
					if (newY + win.getHeight() > content.getY() + content.getHeight()) {
						newY = content.getY() + content.getHeight() - win.getHeight();
					}
					win.setRect(0, newY, 1, winRect.getHeight());
					invokeOnScroll();
				}
			});

			// Start timer.
			timer.start();
		}
		// If top button affected.
		else if (mouse.getY() <= sliderStart()) {
			
			// Create and schedule timer.
			timer = new javax.swing.Timer(0, null);
			timer.setDelay((int) repeatMs);
			
			// Set action command.
			timer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					double newY;
					newY = win.getY() - step;
					if (newY < content.getY()) {
						newY = content.getY();
					}
					win.setRect(0, newY, 1, winRect.getHeight());
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
	public void onMouseGragged(MouseEvent e) {

		int height = y2 - y1;
		int inner = height - 2 * 20;
		
		delta = (e.getPoint().y - pressedPosition.getY()) * content.getHeight() / inner;
		
		double newY = win.getY() + delta;
		if (newY < content.getY()) {
			newY = content.getY();
		}
		if (newY + win.getHeight() > content.getY() + content.getHeight()) {
			newY = content.getY() + content.getHeight() - win.getHeight();
		}
		win.setRect(0, newY, 1, win.getHeight());
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

		return new Rectangle(x1, y1, width, y2 - y1);
	}
}

