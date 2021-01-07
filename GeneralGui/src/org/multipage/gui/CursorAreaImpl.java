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
public class CursorAreaImpl {
	
	/**
	 * Parent component.
	 */
	private Component component;

	/**
	 * Cursor.
	 */
	private Cursor cursor;

	/**
	 * Shape.
	 */
	private Shape shape;

	/**
	 * Listener.
	 */
	private CursorAreaListener listener;
	
	/**
	 * Constructor.
	 */
	public CursorAreaImpl(Cursor cursor, Component component,
			CursorAreaListener listener) {
		
		this.cursor = cursor;
		this.component = component;
		this.listener = listener;
	}
	
	/**
	 * It gets the object visibility.
	 */
	public boolean visible() {
		
		if (listener == null) {
			return true;
		}
		return listener.visible();
	}
	
	/**
	 * On cursor.
	 * @param mouse
	 */
	public void onCursor(Point mouse) {
		
		// If the shape doesn't exist or is not visible, exit the method.
		if (shape == null || !visible()) {
			return;
		}

		// If the area contains the point...
		if (shape.contains(mouse)) {
			// Set cursor if not already set.
			if (component.getCursor() != cursor) {
				useCursor();
			}
		}
	}
	
	/**
	 * Use cursor.
	 */
	public void useCursor() {
		
		component.setCursor(cursor);
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * @param cursor the cursor to set
	 */
	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}
}