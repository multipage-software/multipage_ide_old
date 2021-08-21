/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.maclan.Area;


/**
 * Area coordinates class.
 * @author
 *
 */
public class AreaCoordinates {

	/**
	 * This area percent.
	 */
	public static final double areaFreeZonePercent = 28.5;
	
	/**
	 * Get height.
	 */
	public static double getHeight(double width) {
		
		return width * (100 - areaFreeZonePercent) / 100;
	}
	/**
	 * Gets caption rectangle.
	 * @return
	 */
	public Rectangle2D getCaptionRect() {

		return new Rectangle2D.Double(x, y, width, getLabelHeight());
	}

	/**
	 * Coordinates.
	 */
	private double x;
	private double y;
	private double width;
	
	/**
	 * Area.
	 */
	private Area area;
	
	/**
	 * Parent area.
	 */
	private Area parentArea;
	
	/**
	 * Show more info flag.
	 */
	private boolean showMoreInfo;
	
	/**
	 * Is reference flag.
	 */
	private boolean isReference;
	
	/**
	 * Constructor.
	 * @param x
	 * @param y
	 * @param width
	 * @param area
	 * @param parentArea
	 * @param showMoreInfo
	 * @param isReference
	 */
	public AreaCoordinates(double x, double y, double width,
			Area area, Area parentArea, boolean showMoreInfo,
			boolean isReference) {

		this.x = x;
		this.y = y;
		this.width = width;
		this.area = area;
		this.parentArea = parentArea;
		this.showMoreInfo = showMoreInfo;
		this.isReference = isReference;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * Multiply coordinates.
	 */
	public void multiply(double multiply) {

		x *= multiply;
		y *= multiply;
		width *= multiply;
	}

	/**
	 * Get font size.
	 */
	public double getLabelFontSize() {
		// Compute font size.
		return getHeight(width) * areaFreeZonePercent / 100 * 0.4;
	}

	/**
	 * Get font size.
	 */
	public double getInfoFontSize() {
		// Compute font size.
		return getHeight(width) * 0.08;
	}
	
	/**
	 * Get description font size.
	 * @return
	 */
	public double getDescriptionFontSize() {
		// Compute description font size.
		return getLabelFontSize() * 0.2;
	}

	/**
	 * Get label height.
	 * @return
	 */
	public double getLabelHeight() {

		return getHeight() * areaFreeZonePercent / 100;
	}
	
	/**
	 * Get label rectangle.
	 * @return
	 */
	public Rectangle2D getLabel() {
		
		return new Rectangle2D.Double(x, y, width, getLabelHeight());
	}

	/**
	 * Get height.
	 * @return
	 */
	public double getHeight() {

		return getHeight(width);
	}

	/**
	 * Area free space X position.
	 * @return
	 */
	public double getFreeX() {

		return x + (100 - areaFreeZonePercent) * width / 100;
	}

	/**
	 * Area free space Y position.
	 * @return
	 */
	public double getFreeY() {

		return y + areaFreeZonePercent * getHeight() / 100;
	}

	/**
	 * Get area free space width.
	 * @return
	 */
	public double getFreeWidth() {

		return areaFreeZonePercent * width / 100;
	}

	/**
	 * Get area free space height.
	 * @return
	 */
	public double getFreeHeight() {

		return (100 - areaFreeZonePercent) * getHeight() / 100;
	}
	
	/**
	 * Get free rectangle.
	 * @return
	 */
	public Rectangle2D getFree() {
		
		return new Rectangle2D.Double(getFreeX(), getFreeY(),
				getFreeWidth(), getFreeHeight());
	}
	/**
	 * Gets area rectangle.
	 * @return
	 */
	public Rectangle2D getRectangle() {

		return new Rectangle2D.Double(x, y, width, getHeight());
	}
	
	/**
	 * Gets child area rectangle.
	 * @return
	 */
	public Rectangle2D getChildAreaRectangle() {
		
		double labelHeight = getLabelHeight();
		double freeWidth = getFreeWidth();
		return new Rectangle2D.Double(x, y + labelHeight, width - freeWidth, getHeight() - labelHeight);
	}
	
	/**
	 * @return the parentArea
	 */
	public Area getParentArea() {
		return parentArea;
	}
	
	/**
	 * Gets center of shape.
	 * @return
	 */
	public Point2D getLabelCenter() {

		return new Point2D.Double(x + width / 2.0, y + getLabelHeight() / 2.0);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		AreasDiagram diagram = GeneratorMainFrame.getFrame().getAreaDiagram();
		
		if (diagram != null) {
			return diagram.undoTransformationX(x) + ", "
					+ diagram.undoTransformationY(y) + ", "
					+ diagram.undoTransformationZoom(width) + ", "
					+ diagram.undoTransformationZoom(getHeight());
		}
		else {
			return x + ", " + y + ", " + width + ", " + getHeight();
		}
	}
	/**
	 * @return the inherits
	 */
	public boolean getInherits() {
		
		return area.inheritsFrom(parentArea);
	}
	
	/**
	 * Get help icon size.
	 * @return
	 */
	public double getHelpIconSize() {
		
		return getLabelHeight() * 0.5;
	}
	
	/**
	 * Get help icon X position.
	 * @return
	 */
	public double getHelpIconX() {
		
		double helpSize = getHelpIconSize();
		double margins = helpSize * 0.2;
		return getX() + getWidth() - helpSize - margins;
	}
	
	/**
	 * Get help icon Y position.
	 * @return
	 */
	public double getHelpIconY() {
		
		double helpSize = getHelpIconSize();
		double margins = helpSize * 0.2;
		return getY() + margins;
	}
	
	/**
	 * Returns true value if the position is over the help icon.
	 * @param point
	 * @return
	 */
	public boolean isOverHelpIcon(Point2D point) {
		
		double size = getHelpIconSize();
		Rectangle2D rectangle = new Rectangle2D.Double(getHelpIconX(), getHelpIconY(),
				size, size);
		return rectangle.contains(point);
	}
	
	/**
	 * @return the hasSubShapes
	 */
	public boolean isShowMoreInfo() {
		return showMoreInfo;
	}
	/**
	 * @return the isReference
	 */
	public boolean isRecursion() {
		return isReference;
	}
	
	/**
	 * Get shape center.
	 * @return
	 */
	public Point2D getCenter() {
		
		return new Point2D.Double(x + width / 2.0, y + getHeight() / 2.0);
	}
}