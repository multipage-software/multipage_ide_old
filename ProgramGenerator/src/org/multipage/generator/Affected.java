/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Graphics2D;
import java.awt.Point;


/**
 * Affected objects interface.
 * @author
 *
 */
public interface Affected {

	/**
	 * Draw affected.
	 * @param zoom 
	 */
	public void drawAffected(Graphics2D g2, double zoom);

	/**
	 * Gets central point.
	 * @return
	 */
	public Point getLabelCenter();

	/**
	 * Returns true value if this effected element has given
	 * next element.
	 * @param endElement
	 * @return
	 */
	public boolean isNext(Affected endElement);
}
