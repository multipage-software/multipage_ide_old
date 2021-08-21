/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;


/**
 *
 */
public class AreaShapes {

	/**
	 * Affected line width.
	 */
	private static final int affectedLineWidth = 7;

	/**
	 * Minimal shape size.
	 */
	private static final double minimalShapeSize = 1.0;

	/**
	 * Free area text gape.
	 */
	private static final double freeAreaTextGapePercent = 30;

	/**
	 * Inherit sign width in percent.
	 */
	private static final int inheritSignWidthPercent = 20;

	/**
	 * Maximum font size.
	 */
	private static final int maximumFontSize = 1000;

	/**
	 * Read only lighter flag.
	 */
	public static boolean readOnlyLighter = true;

	/**
	 * Text to display.
	 */
	private static String textNumberOfSlots = null;

	/**
	 * Affected shapes.
	 */
	private static HashSet<AreaShapes> affectedShapes = new HashSet<AreaShapes>();
	
	/**
	 * Affected coordinate.
	 */
	private static AreaCoordinates affectedCoordinate;

	/**
	 * Has more info text.
	 */
	private static String textHasMoreInfo;

	/**
	 * Coordinates.
	 */
	private LinkedList<AreaCoordinates> coordinates = new LinkedList<AreaCoordinates>();
	
	/**
	 * Selected area flag.
	 */
	private boolean selected = false;
	
	/**
	 * Visibility flag.
	 */
	private boolean visible = true;
	
	/**
	 * Area node.
	 */
	private Area area;

	/**
	 * Affected opacity.
	 */
	private static float affectedIntesnity = 0.4f;

	/**
	 * Visibility of the affected shapes.
	 */
	private static boolean affectedShapesVisible = true;

	/**
	 * Ge arc size.
	 */
	private static int getArcSizePercent() {
		
		return CustomizedControls.getArcSizePercent();
	}
	
	/**
	 * Adds affected shape. 
	 * @param shapes
	 * @return 
	 */
	public static boolean addAffected(AreaShapes shapes) {
		
		// If the parameter is null remove all affected shapes
		// from the list.
		if (shapes == null) {

			affectedShapes.clear();
			affectedShapesVisible = false;
			return true;
		}
		
		// The method returns true if the shape is new.
		affectedShapesVisible = true;
		return affectedShapes.add(shapes);
	}
	
	/**
	 * Set affected shape.
	 */
	public static boolean setAffected(AreaShapes shapes) {
		
		boolean alreadyExists = affectedShapes.contains(shapes);
		affectedShapes.clear();
		
		// If the parameter is null remove all affected shapes.
		if (shapes == null) {
			affectedShapesVisible = false;
			return true;
		}
		
		affectedShapesVisible = true;
		affectedShapes.add(shapes);
		
		return alreadyExists;
	}

	/**
	 * Set affected area.
	 * @param area
	 */
	public static void setAffected(Area area) {
		
		Object user = area.getUser();
		
		if (user != null && user instanceof AreaShapes) {
			AreaShapes shapes = (AreaShapes) user;
			
			setAffected(shapes);
		}
	}
	
	/**
	 * Set affected coordinates.
	 * @param minimalCoordinate
	 */
	public static void setAffected(AreaCoordinates coordinate) {

		affectedCoordinate = coordinate;
	}

	/**
	 * Get one affected shape.
	 * @return
	 */
	public static AreaShapes getOneAffectedShape() {

		Iterator<AreaShapes> iterator = affectedShapes.iterator();
		
		// If it is no item exit the method with null value;
		if (!iterator.hasNext()) {
			return null;
		}
		
		// Get area shape.
		return iterator.next();
	}
	
	/**
	 * Get one affected area.
	 * @return
	 */
	public static Area getOneAffectedArea() {

		AreaShapes shape = getOneAffectedShape();
		if (shape == null) {
			return null;
		}
		return shape.getArea();
	}

	/**
	 * Get first height.
	 */
	public double getFirstHeight() {

		if (coordinates.size() > 0) {
			return AreaCoordinates.getHeight(coordinates.getFirst().getWidth());
		}
		else {
			return 0.0;
		}
	}

	/**
	 * Gets coordinates.
	 * @return
	 */
	public LinkedList<AreaCoordinates> getCoordinates() {

		return coordinates;
	}

	/**
	 * Get first label height.
	 */
	public double getFirstLabelHeight() {

		if (coordinates.size() > 0) {
			return getFirstHeight() * AreaCoordinates.areaFreeZonePercent / 100;
		}
		else {
			return 0.0;
		}
	}
	
	/**
	 * Constructor.
	 * @param showMoreInfo 
	 * @param isReference 
	 */
	public AreaShapes(double x, double y, double width, Area area,
			Area parentArea, boolean showMoreInfo, boolean isReference) {
		
		// Add coordinates.
		coordinates.add(new AreaCoordinates(x, y, width, area, parentArea, showMoreInfo,
				isReference));
		this.area = area;
		
		if (textNumberOfSlots == null) {
			textNumberOfSlots = Resources.getString("org.multipage.generator.textNumberOfSlots");
		}
		
		if (textHasMoreInfo == null) {
			textHasMoreInfo = Resources.getString("org.multipage.generator.textHasMoreInfo");
		}
	}

	/**
	 * Draw area.
	 * @param transformation 
	 * @param areasDiagram 
	 * @param overview 
	 * @param pixel 
	 */
	public void draw(Graphics2D g2, AffineTransform transformation, double zoom,
			AreasDiagram areasDiagram, boolean overview) {

		// If the shapes are not visible, exit the method.
		if (!visible) {
			return;
		}
		
		boolean isReadOnly = area.isReadOnly() && readOnlyLighter;
		final boolean isBuilder = ProgramGenerator.isExtensionToBuilder();
		boolean isAreaConstructor = area.isAreaConstructor();
		
		// Set colors.
		Color selectedColor = CustomizedColors.get(ColorId.SELECTION);
		Color selectedReadOnlyColor = CustomizedColors.get(ColorId.SELECTION_PROTECTED);
		Color fillLabelColor = CustomizedColors.get(
				isReadOnly ? ColorId.FILLLABEL_PROTECTED : ColorId.FILLLABEL);
		Color freeColor = CustomizedColors.get(ColorId.FREE);
		Color outlinesColor = CustomizedColors.get(
				isReadOnly ? ColorId.OUTLINES_PROTECTED : ColorId.OUTLINES);
		Color areaNameColor = CustomizedColors.get(
				isSelected() ? ColorId.SELECTED_TEXT : (isReadOnly ? ColorId.TEXT_PROTECTED : ColorId.TEXT));
		Color decsriptionTextColor = CustomizedColors.get(ColorId.DESCRIPTIONTEXT);
		
		// Create and set alpha composite.
		float alphaValue = 1.0f;
		AlphaComposite defaultAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
		AlphaComposite transparentAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				alphaValue * 0.7f);
		AlphaComposite forcedAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		
		g2.setComposite(defaultAlphaComposite);
		// Remember old font.
		Font oldFont = g2.getFont();
		
		// Get old stroke.
		Stroke oldStroke = g2.getStroke();
		
		BasicStroke simpleStroke = new BasicStroke(isReadOnly ? 1 : 1);
		g2.setStroke(simpleStroke);

		// Area free zone in percent.
		double freePercent = AreaCoordinates.areaFreeZonePercent;
		
		// Get diagram rectangle in current coordinates.
		Rectangle2D diagram = areasDiagram.getRectInCoord();
		
		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			
			// Compute parameters.
			boolean inherits = coord.getInherits();
			String description = area.getDescriptionForDiagram();
			double height = coord.getHeight();
			double labelEndSpace = coord.getWidth() * 0.01;
			double houseSize = 0;
			double startSize = 0;
			double houseBorderSize = 0;
			double startBorderSize = 0;
			double labelHeight = coord.getLabelHeight();
			double width = coord.getWidth();
			double labelStartSpace = coord.getWidth() * 0.01;
			double inheritSignWidth = inherits && isBuilder ? (inheritSignWidthPercent * width / 100) : labelStartSpace;
			boolean isHomeArea = ProgramGenerator.getAreasModel().isHomeArea(area);
			boolean isStartResource = area.isStartArea();
			boolean isHelp = area.isHelp();
			boolean isLabelLeftIcon;
			double arcWidth = width * getArcSizePercent() / 100;
			double arcHeight = width * getArcSizePercent() / 100;
			
			// Set is icon flag.
			if (isBuilder) {
				isLabelLeftIcon = isHomeArea || isStartResource || inherits;
			}
			else {
				isLabelLeftIcon = isHomeArea;
			}
			
			// If the shape has children.
			if (coord.isShowMoreInfo() && ProgramGenerator.isExtensionToBuilder()) {
				
				int infoFontSizeInt = GraphUtility.getSizeTransform(transformation,
						coord.getInfoFontSize());
				
				if (infoFontSizeInt < maximumFontSize) {
					
					Font descriptionFont = new Font("Dialog", Font.BOLD, infoFontSizeInt);
					
					// Get metrics from the graphics.
					FontMetrics metrics = g2.getFontMetrics(descriptionFont);
					// Get the height of a line of text.
					double textHeight = 0;//metrics.getHeight() / transformation.getScaleY();
					// Get the advance of my text.
					double textWidth = metrics.stringWidth(textHasMoreInfo) / transformation.getScaleX();
					
					Rectangle2D childPart = coord.getChildAreaRectangle();
					
					double x = childPart.getX() + (childPart.getWidth() - textWidth) / 2.0;
					double y = childPart.getY() + (childPart.getHeight() - textHeight) / 2.0;
					
					g2.setColor(selectedColor);
					g2.setFont(descriptionFont);

					GraphUtility.drawStringTransform(g2, transformation, textHasMoreInfo, x, y);
				}
			}
			
			// Compute label rectangle.
			Rectangle2D label = coord.getLabel();
			// Compute free area.
			Rectangle2D free = coord.getFree();
			
			boolean drawFree = true;
			boolean drawLabel = true;
			
			if (!overview) {
				drawFree = Utility.isIntersection(diagram, free);
				drawLabel = Utility.isIntersection(diagram, label);
			}
			
			if (drawLabel) {

				// If the width or height of shape is less than minimal size,
				// do not draw the shape.
				if (width * zoom < minimalShapeSize || height * zoom < minimalShapeSize) {
					continue;
				}
				
				// Draw label background and flags.
				g2.setColor(selected ? (isReadOnly ? selectedReadOnlyColor :  selectedColor) : fillLabelColor);
				GraphUtility.fillLabelRectTransform(g2, transformation, coord.getX(), coord.getY(),
						coord.getWidth(), labelHeight, arcWidth, arcHeight);

				// If it is home area, draw house.
				if (isHomeArea) {
					
					houseBorderSize = height * 0.07;
					houseSize = height * freePercent / 100 - 2 * houseBorderSize;
					g2.setComposite(transparentAlphaComposite);
					
					g2.setColor(areaNameColor);
					
					// Draw house.
					GraphUtility.drawHouseTransform(g2, transformation,
							coord.getX() + houseBorderSize, coord.getY() + houseBorderSize,
							houseSize, houseSize, 7.5);
					
					g2.setComposite(defaultAlphaComposite);
				}
				// Draw start resource.
				if (isStartResource && isBuilder) {
					
					// Get image.
					BufferedImage start = Images.getImage("org/multipage/generator/images/start.png");
					if (start != null) {
						startBorderSize = height * 1 / 100;
						startSize = height * freePercent / 100 - 2 * startBorderSize;
						g2.setComposite(transparentAlphaComposite);
						// Draw start icon.
						GraphUtility.drawImageTransform(g2, transformation, start,
								coord.getX() + houseSize + 2 * houseBorderSize + startBorderSize,
								coord.getY() + startBorderSize, startSize, startSize);
						g2.setComposite(defaultAlphaComposite);
					}
				}
				// Draw inheritance sign.
				if (inherits && isBuilder) {

					g2.setColor(areaNameColor);
					double hMargin = labelHeight * 0.25;
					double x = coord.getX() + inheritSignWidth / 2 + (isHomeArea ? inheritSignWidth : 0) + (isStartResource ? inheritSignWidth : 0);
					double y1 = coord.getY() + hMargin;
					double y2 = y1 + labelHeight - 2 * hMargin;
					double length = 0.3 * height * freePercent / 100;
					double stroke = width * 0.015;
					
					GraphUtility.drawArrowTransform(g2, transformation, stroke, x, y2, x, y1, Math.PI / 2, length);
					g2.setStroke(simpleStroke);
				}
				// Draw help button.
				if (isHelp) {
					
					double helpSize = coord.getHelpIconSize();
					double x = coord.getHelpIconX();
					double y = coord.getHelpIconY();

					g2.setComposite(forcedAlphaComposite);
					GraphUtility.drawImageTransform(g2, transformation, Images.getImage("org/multipage/generator/images/help_icon.png"),
							x, y, helpSize, helpSize);
					g2.setComposite(defaultAlphaComposite);
					
				}
			}
			
			Shape oldClip = g2.getClip();

			// Draw free area.
			double freeX = free.getX(),
		       freeY = free.getY(),
		       freeWidth = free.getWidth(),
		       freeHeight = free.getHeight();
			
			if (drawFree) {

				g2.setColor(freeColor);
				GraphUtility.fillFreeRectTransform(g2, transformation, freeX, freeY, freeWidth, freeHeight, arcWidth, arcHeight);
			}
			
			// Draw label text and help icon.
			if (drawLabel) {
				double labelWidth;
				if (isHelp) {
					labelWidth = coord.getHelpIconX() - coord.getX() - labelEndSpace;
				}
				else {
					labelWidth = coord.getWidth() - labelEndSpace;
				}
				
				g2.setColor(areaNameColor);
				GraphUtility.clipRectTransform(g2, transformation, coord.getX(), coord.getY(), labelWidth, height * freePercent / 100);
				
				if (!description.isEmpty()) {
					
					// Get font size.
					double fontSize = coord.getLabelFontSize();
					int fontSizeInt = GraphUtility.getSizeTransform(transformation, fontSize);
					
					// Draw label.
					if (fontSizeInt < maximumFontSize) {

						Font labelFont = new Font("Dialog", Font.PLAIN, fontSizeInt);
					    AttributedString as = new AttributedString(description);
					    as.addAttribute(TextAttribute.FONT, labelFont);
					    
					    // If the area is not visible strike the text through.
					    if (!area.isVisible()) {
					    	as.addAttribute(TextAttribute.STRIKETHROUGH,
					    			TextAttribute.STRIKETHROUGH_ON, 0, description.length());
					    }
					    
						GraphUtility.drawStringTransform(g2, transformation, as.getIterator(),
								coord.getX() + houseSize + 2 * houseBorderSize + startSize + 2 * startBorderSize + inheritSignWidth
								+ (!isLabelLeftIcon ? width * 0.07 : 0),
								freeY - (height * freePercent / 100 - coord.getLabelFontSize()) / 2);
					}
				}
			}

			g2.setClip(oldClip);
			
			// Draw number of slots.
			if (drawFree) {
				
				double descrSize = coord.getDescriptionFontSize();
				int descrSizeInt = GraphUtility.getSizeTransform(transformation, descrSize);
				
				if (descrSizeInt < maximumFontSize) {

					g2.setColor(decsriptionTextColor);
					oldClip = g2.getClip();
					
					Font descriptionFont = new Font("Dialog", Font.PLAIN, descrSizeInt);
					g2.setFont(descriptionFont);
					GraphUtility.clipRectTransform(g2, transformation, freeX, freeY, freeWidth, freeHeight);
					GraphUtility.drawStringTransform(g2, transformation,
							textNumberOfSlots + " " + String.valueOf(area.getSlotAliasesCount()),
							freeX + 2 * descrSize, freeY + 3 * descrSize);
					
					// Draw list of slots.
					double yTextPosition = freeY + 4 * descrSize
						+ freeAreaTextGapePercent * descrSize / 100.0;
					double xTextPosition = freeX + 2 * descrSize;
					
					int slotIndex = 0;
					
					for (String slotName : isBuilder ? area.getSlotAliases() : area.getSlotNames()) {
						
						// Draw maximum of 17 slots.
						if (slotIndex > 16) {
							break;
						}
		
						// Draw the text.
						GraphUtility.drawStringTransform(g2, transformation, slotName, xTextPosition, yTextPosition);
						
						yTextPosition += descrSize * 1.5;
						
						slotIndex++;
					}
					
					g2.setClip(oldClip);
				}
			}
			
			// Draw boundaries.
			boolean drawBoundary = true;
			if (!overview) {
				drawBoundary = Utility.isBoundaryIntersection(diagram, coord.getRectangle());
			}
			
			// Draw boundary.
			if (drawBoundary) {
				final float dash[] = { 1.0f };
				final float strokeWidth = 1.0f;

				g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
				        BasicStroke.JOIN_MITER, 20.0f, dash, 0.0f));
				
				g2.setColor(selected ? (isReadOnly ? selectedReadOnlyColor : selectedColor) : outlinesColor);
				
				GraphUtility.drawRoundRectTransform(g2, transformation, coord.getX(), coord.getY(),
						coord.getWidth(), height, arcWidth, arcHeight);
				
				g2.setColor(decsriptionTextColor);
				
				GraphUtility.drawLineTransform(g2, transformation, coord.getX() + coord.getWidth() - freeWidth, coord.getY() + labelHeight,
						coord.getX() + coord.getWidth(), coord.getY() + labelHeight);
				
				
				// Draw reference.
				if (coord.isRecursion()) {
					
					// Draw recursion.
					g2.setColor(selected ? (isReadOnly ? selectedReadOnlyColor : selectedColor) : fillLabelColor);
					double centralAreaWidth = coord.getWidth() - free.getWidth();
					double centralAreaHeight = height - label.getHeight();
					double marginVertical = centralAreaWidth * 0.35;
					double marginHorizontal = centralAreaHeight * 0.3;
					double x1 = coord.getX() + marginVertical;
					double y1 = coord.getY() + label.getHeight() + marginHorizontal;
					double x2 = coord.getX() + centralAreaWidth - marginVertical;
					double y2 = coord.getY() + height - marginHorizontal;
					
					GraphUtility.drawRecursionTransform(g2, transformation, x1, y1, x2, y2);
				}
			}
			
			// Draw if it has a constructor.
			if (drawBoundary && isAreaConstructor && !overview) {

				final AlphaComposite constructorAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

				g2.setStroke(simpleStroke);
				
				g2.setComposite(constructorAlphaComposite);
				g2.setColor(selected ? (isReadOnly ? selectedReadOnlyColor : selectedColor) : outlinesColor);
				
				GraphUtility.drawRoundRectTransform(g2, transformation, coord.getX(), coord.getY(),
						coord.getWidth(), height, arcWidth, arcHeight);
			}
		}
		
		// Set old stroke.
		g2.setStroke(oldStroke);
		
		// Set old font and dispose existing font.
		g2.setFont(oldFont);
	}

	/**
	 * Draw affected shapes.
	 * @param transformation 
	 */
	public static void drawAffectedShapes(Graphics2D g2, AffineTransform transformation, double zoom) {
		
		// If there area no affected shapes, exit the method.
		if (affectedShapes == null) {
			return;
		}

		// If the affected shapes are not visible exit the method.
		if (!affectedShapesVisible) {
			return;
		}

		// Draw gradient rectangle for all affected shapes and coordinates.
		for (AreaShapes shape : affectedShapes) {
			
			// Get area read only flag value.
			Area area = shape.getArea();
			boolean isReadOnly = area.isReadOnly() && AreaShapes.readOnlyLighter;
			
			for (AreaCoordinates coord : shape.getCoordinates()) {
				
				ColorId colorId = shape.isSelected() ? ColorId.SELECTION :
					(isReadOnly ? ColorId.OUTLINES_PROTECTED : ColorId.OUTLINES);
				
				GraphUtility.drawGradientRectangleTransf(g2, transformation,
						coord.getX(), coord.getY(), coord.getWidth(), coord.getHeight(),
						affectedLineWidth / zoom, CustomizedColors.get(colorId),
						affectedIntesnity);
			}
		}
	}

	/**
	 * Get minimal size.
	 * @return
	 */
	public double getMinimalSize() {

		double minimal = 0.0;
		
		// Find minimal width.
		for (AreaCoordinates coord : coordinates) {
			double width = coord.getWidth();
			if (minimal == 0.0) {
				minimal = width;
			}
			else if (width < minimal) {
				minimal = width;
			}
		}
		return minimal;
	}

	/**
	 * Multiply.
	 */
	public void multiply(double multiply) {
		
		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			coord.multiply(multiply);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String text = "AreaShapes ";
		
		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			text += "[x=" + coord.getX() + ", y=" + coord.getY() + ", width=" + coord.getWidth() +  ", height=" + coord.getHeight() + "]";
		}
		return text;
	}

	/**
	 * Returns coordinates if a caption is hit.
	 * @param point
	 * @return
	 */
	public AreaCoordinates isCaptionHit(Point2D point) {

		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			Rectangle2D rect = new Rectangle2D.Double(coord.getX(),
					                                  coord.getY(),
					                                  coord.getWidth(),
					                                  AreaCoordinates.getHeight(coord.getWidth()) * AreaCoordinates.areaFreeZonePercent / 100);
		
			if (rect.contains(point)) {
				return coord;
			}
		}
		return null;
	}

	/**
	 * Is area free hit.
	 * @param point
	 * @return
	 */
	public AreaCoordinates isAreaFreeHit(Point2D point) {
		
		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			Rectangle2D rect = new Rectangle2D.Double(coord.getFreeX(),
					                                  coord.getFreeY(),
					                                  coord.getFreeWidth(),
					                                  coord.getFreeHeight());
		
			if (rect.contains(point)) {
				return coord;
			}
		}
		return null;
	}

	/**
	 * Select area.
	 * @param b
	 */
	public void select(boolean selected) {

		this.selected = selected;
	}
	
	/**
	 * Is selected.
	 */
	public boolean isSelected() {
		
		return selected;
	}

	/**
	 * Returns true if whole shape is inside rectangle.
	 * @param rectangle
	 * @return
	 */
	public boolean isInside(Rectangle2D rectangle) {

		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			if (rectangle.contains(coord.getX(),
					               coord.getY(),
					               coord.getWidth(),
					               coord.getHeight())) {
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get first rectangle.
	 * @return
	 */
	public Rectangle2D getFirstRectangle() {

		AreaCoordinates coord = coordinates.getFirst();
		
		if (coord != null) {
			return new Rectangle2D.Double(coord.getX(),
										  coord.getY(),
										  coord.getWidth(),
										  coord.getHeight());
		}
		else {
			return null;
		}
	}

	/**
	 * Get first shape x position.
	 */
	public double getFirstX() {
		
		AreaCoordinates coord = coordinates.getFirst();
		
		if (coord != null) {
			return coord.getX();
		}
		else {
			return 0.0;
		}
	}

	/**
	 * Get first shape y position.
	 */
	public double getFirstY() {
		
		AreaCoordinates coord = coordinates.getFirst();
		
		if (coord != null) {
			return coord.getY();
		}
		else {
			return 0.0;
		}
	}

	/**
	 * Get first width.
	 */
	public double getFirstWidth() {
		
		AreaCoordinates coord = coordinates.getFirst();
		
		if (coord != null) {
			return coord.getWidth();
		}
		else {
			return 0.0;
		}
	}

	/**
	 * Add new shape coordinates.
	 * @param inherits 
	 */
	public void add(double x, double y, double width, Area area, Area parentArea,
			boolean hasSubShapes, boolean isReference) {

		// Add coordinates.
		coordinates.add(new AreaCoordinates(x, y, width, area, parentArea,
				hasSubShapes, isReference));
	}

	/**
	 * Sets visibility flag.
	 * @param visible
	 */
	public void setVisible(boolean visible) {

		this.visible = visible;
	}

	/**
	 * Returns true if the shapes are visible.
	 * @return
	 */
	public boolean isVisible() {

		return visible;
	}

	/**
	 * Set first shape location.
	 * @param point
	 */
	public void setFirstLocation(Point point) {

		AreaCoordinates coord = coordinates.getFirst();
		
		coord.setX(point.x);
		coord.setY(point.y);
	}

	/**
	 * Get minimal affected rectangle.
	 * @param transformedMouse
	 * @return
	 */
	public boolean getMinimalAffectedRect(Point2D transformedMouse, Obj<AreaCoordinates> outputCoordinate) {

		outputCoordinate.ref = null;
		
		double surface = Double.MAX_VALUE;
		
		// Do loop for all area coordinates.
		for (AreaCoordinates coordinate : coordinates) {
			Rectangle2D rectangle = coordinate.getRectangle();
			
			// If the point is inside the rectangle, return the rectangle.
			if (rectangle.contains(transformedMouse)) {
				
				double currentSurface = rectangle.getWidth() * rectangle.getHeight();
				
				if (currentSurface <= surface) {
					outputCoordinate.ref = coordinate;
					
					currentSurface = surface;
				}
			}
		}
		
		return outputCoordinate.ref != null;
	}

	/**
	 * Reset affected.
	 */
	public static void resetAffected() {

		affectedShapes.clear();
		affectedCoordinate = null;
		affectedShapesVisible = false;
	}

	/**
	 * Gets affected parent area.
	 * @return
	 */
	public static Area getAffectedParentArea() {

		if (affectedCoordinate != null) {
			return affectedCoordinate.getParentArea();
		}
		return null;
	}

	/**
	 * Set visibility of the affected area shapes.
	 * @param visible
	 */
	public static void setAffectedAreaVisible(boolean visible) {

		affectedShapesVisible = visible;
	}
	
	/**
	 * Gets area.
	 * @return
	 */
	public Area getArea() {

		return area;
	}

	/**
	 * Gets center of first shape.
	 * @return
	 */
	public Point2D firstLabelCenter() {
		
		AreaCoordinates coordinate = coordinates.getFirst();
		
		if (coordinate != null) {
			return coordinate.getLabelCenter();
		}
		return new Point2D.Double();
	}

	/**
	 * If the affected area is visible the method returns true.
	 * @return
	 */
	public static boolean isAffectedAreaVisible() {

		return affectedShapesVisible;
	}

	/**
	 * Affect all selected.
	 * @param model 
	 */
	public static void affectAllSelected(AreasModel model) {

		// Clear the list.
		affectedShapes.clear();
		affectedShapesVisible = false;
		
		// Do loop for all selected areas.
		for (Area area : model.getAreas()) {
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				
				if (shape.isSelected()) {
					
					// Add selected shape to the list of affected shapes.
					affectedShapes.add(shape);
					affectedShapesVisible = true;
				}
			}
		}

	}

	/**
	 * Gets affected area shapes.
	 * @return
	 */
	public static HashSet<AreaShapes> getAffectedAreaShapes() {

		return affectedShapes;
	}

	/**
	 * Gets visibility.
	 * @return
	 */
	public static boolean getAffectedAreaVisible() {

		return affectedShapesVisible;
	}

	/**
	 * Get first coordinates
	 * @return
	 */
	public AreaCoordinates getFirstCoordinates() {

		return coordinates.getFirst();
	}

	/**
	 * Get visible parent.
	 * @param diagramRect
	 * @return
	 */
	public Area getVisibleParent(Rectangle2D diagramRect) {
		
		// Do loop for all parents.
		for (Area parentArea : area.getSuperareas()) {
			
			Object user = parentArea.getUser();
			if (!(user instanceof AreaShapes)) {
				continue;
			}
			
			AreaShapes shapes = (AreaShapes) user;

			LinkedList<AreaCoordinates> superCoordinates = shapes.getCoordinates();
			
			// Do loop for all coordinates.
			for (AreaCoordinates coord : superCoordinates) {
				
				if (Utility.isIntersection(diagramRect, coord.getRectangle())) {
					return parentArea;
				}
			}
		}
		
		return null;
	}

	/**
	 * Draw root area body.
	 * @param g2
	 * @param transform 
	 */
	public static void drawRootBody(Graphics2D g2, AffineTransform transform) {
		
		float alphaValue = 1.0f;
		AlphaComposite defaultAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
		g2.setComposite(defaultAlphaComposite);
	
		Area root = ProgramGenerator.getAreasModel().getRootArea();
		if (root != null) {
			Object user = root.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				
				if (!shape.getCoordinates().isEmpty()) {
					AreaCoordinates coord = shape.getCoordinates().getFirst();
					
					g2.setColor(CustomizedColors.get(ColorId.FILLBODY));
					
					double x = coord.getX();
					double y = coord.getY() + coord.getLabelHeight();
					double width = coord.getWidth() - coord.getFreeWidth();
					double height = coord.getHeight() - coord.getLabelHeight();
					double arcWidth = coord.getWidth() * getArcSizePercent() / 100;
					double arcHeight = coord.getWidth() * getArcSizePercent() / 100;
					
					GraphUtility.fillRootRectTransform(g2, transform, x, y, width, height, arcWidth, arcHeight);
				}
			}
		}
	}

	/**
	 * Get biggest shape inside rectangle surface.
	 * @param rectangle
	 * @return
	 */
	public double getBiggestInsideSurface(Rectangle2D rectangle) {
		
		double maximumSurface = 0.0;
		
		double rx = rectangle.getX();
		double ry = rectangle.getY();
		double rwidth = rectangle.getWidth();
		double rheight = rectangle.getHeight();
		
		// Do loop for all coordinates.
		for (AreaCoordinates coord : coordinates) {
			
			double x = coord.getX();
			double y =  coord.getY();
			double width = coord.getWidth();
			double height = coord.getLabelHeight();
			
			if (rectangle.intersects(x, y, width, height)) {

				// Get intersection width and height.
				double intersectionWidth = Utility.getIntersectionLength(x, width, rx, rwidth);
				double intersectionHeight = Utility.getIntersectionLength(y, height, ry, rheight);
				
				// Coordinates are inside given rectangle.
				double surface = intersectionWidth * intersectionHeight;
				
				if (surface > maximumSurface) {
					maximumSurface = surface;
				}
			}
		}
		
		return maximumSurface;
	}

	/**
	 * Select area.
	 * @param area
	 */
	public static void select(Area area, boolean selected) {
		
		Object user = area.getUser();
		
		if (user instanceof AreaShapes) {
			AreaShapes shape = (AreaShapes) user;
			shape.select(selected);
		}
		else if (user instanceof Boolean || user == null) {
			area.setUser((Boolean) selected);
		}
	}
}