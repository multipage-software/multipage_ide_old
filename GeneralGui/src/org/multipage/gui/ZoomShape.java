/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

/**
 * @author
 *
 */
public class ZoomShape implements CursorArea {

	/**
	 * Shape width.
	 */
	public static final int width = 10;
	
	/**
	 * Shape height.
	 */
	public static final int height = 160;
    
	/**
	 * Arc.
	 */
    public static final int arc = 3;
    
    /**
     * Overlap.
     */
    public static final int overlap = 7;

	/**
	 * Minimal zoom.
	 */
	private double minimal;
	
	/**
	 * Maximal zoom.
	 */
	private double maximal;
	
	/**
	 * Zoom.
	 */
	private double zoom = 1.0;
	
	/**
	 * Position on screen.
	 */
	private int x;
	private int y;

	/**
	 * Dragged flag.
	 */
	private boolean dragged = false;
	
	/**
	 * Listener.
	 */
	private ZoomListener listener = null;
	
	/**
	 * Cursor area.
	 */
	CursorAreaImpl cursorArea;

	/**
	 * Get cursor area.
	 */
	@Override
	public CursorAreaImpl getCursorArea() {

		return cursorArea;
	}

	/**
	 * Constructor.
	 */
	public ZoomShape(double min, double max, int x, int y,
			Cursor cursor, Component component) {
		
		minimal = min;
		maximal = max;
		this.x = x;
		this.y = y;
		cursorArea = new CursorAreaImpl(cursor, component,
				new CursorAreaListener() {
					@Override
					public boolean visible() {
						return isVisible();
					}
				});
	}
	
	/**
	 * Constructor
	 * @param min
	 * @param max
	 * @param cursor
	 * @param component
	 */
	public ZoomShape(double min, double max, Cursor cursor,
			Component component) {
		
		this(min, max, 0, 0, cursor, component);
	}

	/**
	 * Returns true if the shape is visible.
	 */
	public boolean isVisible() {
		
		return maximal > minimal;
	}
	
	/**
	 * Draw zoom shape.
	 */
	public void draw(Graphics2D g2) {
		
		// If the shape is not visible, exit the method.
		if (!isVisible()) {
			return;
		}
		
		int min = y + height - arc,
		    max = y + arc,
		    sliderHeight = arc * 2;
		
		// Set opacity.
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		
		Paint oldPaint = g2.getPaint();
		g2.setPaint(new GradientPaint(new Point(x + overlap, y), Color.LIGHT_GRAY, new Point(x - overlap + width, y + height), Color.WHITE));
		// Draw main part.
		g2.fillRoundRect(x + overlap, y, width, height, arc, arc);
		g2.setPaint(oldPaint);
		g2.setColor(Color.DARK_GRAY);
		g2.drawRoundRect(x + overlap, y, width, height, arc, arc);
		
		// Draw scale.
		g2.setStroke(new BasicStroke(2));
		g2.setColor(new Color(200, 200, 255));
		g2.drawLine(x, min, x + width + 2 * overlap, min);
		g2.drawLine(x, max, x + width + 2 * overlap, max);
		g2.setStroke(new BasicStroke(1));
		
		int delta = (height - 2 * arc) / 10,
		    index = 0;
		for (int ypos = max + delta; index < 9; ypos += delta) {
			g2.drawLine(x + overlap / 2, ypos, x + width + 2 * overlap - overlap / 2, ypos);
			index++;
		}
		
		double pos = (max - min) * Math.log(zoom / minimal) / Math.log(maximal / minimal) + min;
		// Draw slider.
		g2.fillRoundRect(x, (int)pos - sliderHeight / 2, width + 2 * overlap - 1, sliderHeight, arc, arc);
		g2.setColor(Color.DARK_GRAY);
		g2.drawRoundRect(x, (int)pos - sliderHeight / 2, width + 2 * overlap - 1, sliderHeight, arc, arc);
	}

	/**
	 * @return the width
	 */
	public static int getWidth() {
		return width + 2 * overlap;
	}

	/**
	 * Set position.
	 */
	public void setPosition(int x, int y) {

		this.x = x;
		this.y = y;
		
		// Set cursor area shape.
		getCursorArea().setShape(getRectangle());
	}

	/**
	 * On mouse pressed.
	 */
	public void onMousePressed(Point mouse) {
		// If the mouse is on shape.
		if (mouse.x >= x && mouse.x <= (x + getWidth()) && mouse.y >= y && mouse.y <= (y + height)) {
			// Set dragged flag
			dragged  = true;
			// Do drag.
			onMouseDragged(mouse);
		}
	}

	/**
	 * On mouse dragged.
	 */
	public void onMouseDragged(Point point) {
		
		double max = y + arc,
		       min = y + height - arc;
		
		// Compute position.
		double pos = point.y;
		if (pos > min) {
			pos = min;
		}
		if (pos < max) {
			pos = max;
		}
		
		// Compute zoom.
		zoom = Math.pow(Math.E, (pos - max) / (min - max) * Math.log(minimal / maximal) + Math.log(maximal));
		
		// Invoke listener.
		if (listener != null) {
			listener.zoomChanged();
		}
	}

	/**
	 * Get dragged flag.
	 */
	public boolean isDragged() {

		return dragged;
	}

	/**
	 * On mouse released.
	 */
	public void onMouseReleased() {

		dragged = false;
	}

	/**
	 * @param zoom the zoom to set
	 */
	public void setZoom(double zoom) {

		if (zoom < minimal) {
			zoom = minimal;
		}
		else if (zoom > maximal) {
			zoom = maximal;
		}
		this.zoom = zoom;
	}

	/**
	 * @return the zoom
	 */
	public double getZoom() {
		return zoom;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(ZoomListener listener) {
		this.listener = listener;
	}

	/**
	 * Get rectangle.
	 */
	public Rectangle getRectangle() {

		return new Rectangle(x, y, getWidth(), height);
	}

	/**
	 * Returns true if shape contains point.
	 * @param point
	 * @return
	 */
	public boolean contains(Point point) {

		return isVisible() && getRectangle().contains(point);
	}

	/**
	 * @param minimal the minimal to set
	 */
	public void setMinimal(double minimal) {
		this.minimal = minimal;
	}

	/**
	 * Gets minimal zoom.
	 * @return
	 */
	public double getMinimal() {

		return minimal;
	}

	/**
	 * Get maximal zoom.
	 * @return
	 */
	public double getMaximal() {

		return maximal;
	}

	/**
	 * @param maximal the maximal to set
	 */
	public void setMaximal(double maximal) {
		this.maximal = maximal;
	}
}
