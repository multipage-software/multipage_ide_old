/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.util.*;

/**
 * @author
 *
 */
public class GraphUtility {
	
	/**
	 * Colors.
	 */
	private static final Color colorHighlight = new Color(20, 20, 20);
	
	/**
	 * Gradient rectangle intensity multiplier.
	 */
	private static final float gradientRectangleIntensityMultiplier = 1.0f;
	
	/**
	 * Image for true value.
	 */
	private static BufferedImage trueImage;

	/**
	 * Image for false value.
	 */
	private static BufferedImage falseImage;
	
	/**
	 * Default texture paint.
	 */
	private static TexturePaint defaultTexturePaint;

	/**
	 * Static constructor.
	 */
	static {
		
		// Set images.
		BufferedImage image = Images.getImage("org/multipage/gui/images/default_texture.png");
		if (image != null) {
			defaultTexturePaint = new TexturePaint(image, new Rectangle(0, 0, 5, 5));
		}
		
		trueImage = Images.getImage("org/multipage/gui/images/true.png");
		falseImage = Images.getImage("org/multipage/gui/images/false.png");
	}

	/**
	 * Draw gradient rectangle.
	 * @param affectedOpacity 
	 */
	public static void drawGradientRectangle(Graphics2D g2, int x, int y,
			int width, int height, int lineSize, Color color, float intensity) {
		
		int halfSize = lineSize / 2;
		
		// Draw rectangle.
		g2.setColor(color);
		g2.drawRect(x, y, width, height);
		
		// Set background color.
		Color backgroundColor = new Color(color.getRed() / 255,
				color.getGreen() / 255,
				color.getBlue() / 255,
				0.0f);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				gradientRectangleIntensityMultiplier * (float) intensity));
		
		// Get old paint.
		Paint oldPaint = g2.getPaint();
		
		Polygon polygon;
		
		// Left border.
		// Create new gradient paint.
		g2.setPaint(new GradientPaint(x - halfSize, 0, backgroundColor, x, 0,
				color, true));
		
		polygon = new Polygon();
		
		polygon.addPoint(x - halfSize, y - halfSize);
		polygon.addPoint(x + halfSize, y + halfSize);
		polygon.addPoint(x + halfSize, y + height - halfSize);
		polygon.addPoint(x - halfSize, y + height + halfSize);
		
		// Fill polygon.
		g2.fill(polygon);
		
		// Top border.
		// Create new gradient paint.
		g2.setPaint(new GradientPaint(0, y - halfSize, backgroundColor, 0, y,
				color, true));
		
		polygon = new Polygon();

		polygon.addPoint(x - halfSize, y - halfSize);
		polygon.addPoint(x + width + halfSize, y - halfSize);
		polygon.addPoint(x + width - halfSize, y + halfSize);
		polygon.addPoint(x + halfSize, y + halfSize);
		
		// Fill polygon.
		g2.fill(polygon);
		
		// Right border.
		// Create new gradient paint.
		g2.setPaint(new GradientPaint(x + width - halfSize, 0, backgroundColor,
				x + width, 0, color, true));
		
		polygon = new Polygon();
		
		polygon.addPoint(x + width - halfSize, y + halfSize);
		polygon.addPoint(x + width + halfSize, y - halfSize);
		polygon.addPoint(x + width + halfSize, y + height + halfSize);
		polygon.addPoint(x + width - halfSize, y + height - halfSize);
		
		// Fill polygon.
		g2.fill(polygon);
		
		// Bottom border.
		// Create new gradient paint.
		g2.setPaint(new GradientPaint(0, y + height - halfSize, backgroundColor,
				0, y + height, color, true));
		
		polygon = new Polygon();
		
		polygon.addPoint(x + halfSize, y + height - halfSize);
		polygon.addPoint(x + width - halfSize, y + height - halfSize);
		polygon.addPoint(x + width + halfSize, y + height + halfSize);
		polygon.addPoint(x - halfSize, y + height + halfSize);
		
		// Fill polygon.
		g2.fill(polygon);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

		// Set old paint.
		g2.setPaint(oldPaint);
	}

	/**
	 * Compute arrow.
	 * @param begin
	 * @param end
	 * @param arrowAlpha
	 * @param arrowLength
	 * @param pointA
	 * @param pointB
	 */
	public static void computeArrow(Point begin, Point end,
			double arrowAlpha, double arrowLength, Point pointA, Point pointB) {

		double alpha;
		
		// Compute angle.
		alpha = Math.atan((double) (end.y - begin.y)
				/ (double) (end.x - begin.x));
		
		double gamma = alpha - arrowAlpha / 2.0;
		double delta = alpha + arrowAlpha / 2.0;
		
		double dAX = arrowLength * Math.cos(gamma);
		double dAY = arrowLength * Math.sin(gamma);
		double dBX = arrowLength * Math.cos(delta);
		double dBY = arrowLength * Math.sin(delta);
		
		// Set arrow points.
		if (end.x >= begin.x) {
			pointA.x = end.x - (int) dAX;
			pointA.y = end.y - (int) dAY;
			pointB.x = end.x - (int) dBX;
			pointB.y = end.y - (int) dBY;
		}
		else {
			pointA.x = end.x + (int) dAX;
			pointA.y = end.y + (int) dAY;
			pointB.x = end.x + (int) dBX;
			pointB.y = end.y + (int) dBY;
		}
	}

	/**
	 * Create arrow shape.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param alpha
	 * @param length
	 * @return
	 */
	public static LinkedList<Shape> createArrowShape(int x1, int y1, int x2, int y2,
			double alpha, double length) {
		
		LinkedList<Shape> arrowShape = new LinkedList<Shape>();
		
		// Compute arrow points.
		Point pointA = new Point();
		Point pointB = new Point();
		
		computeArrow(new Point(x1, y1), new Point(x2, y2), alpha, length, pointA, pointB);
		
		// Create lines.
		arrowShape.add(new Line2D.Double(x1, y1, x2, y2));
		arrowShape.add(new Line2D.Double(x2, y2, pointA.x, pointA.y));
		arrowShape.add(new Line2D.Double(x2, y2, pointB.x, pointB.y));
		
		return arrowShape;
	}
	
	/**
	 * Draw arrow.
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param alpha 
	 * @param length 
	 */
	public static void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2,
			double alpha, double length) {
		
		// Get arrow shape.
		LinkedList<Shape> arrowShape = createArrowShape(x1, y1, x2, y2, alpha, length);
		
		// Draw it.
		for (Shape shape : arrowShape) {
			g2.draw(shape);
		}
	}

	/**
	 * Gets list of line surroundings.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param radius 
	 * @return
	 */
	public static LinkedList<Shape> createLineSurrounding(int x1,
			int y1, int x2, int y2, double radius) {
		
		// Create list.
		LinkedList<Shape> shapes = new LinkedList<Shape>();

		double dx = x2 - x1;
		double dy = y2 - y1;
		double d = Math.sqrt(dx * dx + dy * dy);
		double alpha = Math.asin(dx / d);
		
		if (dy < 0) {
			alpha = Math.PI - alpha;
		}
		
		int dxa = (int) (radius * Math.cos(alpha));
		int dya = (int) (radius * Math.sin(alpha));
		
		// Create polygon.
		Polygon polygon = new Polygon();
		// Add points.
		polygon.addPoint(x1 - dxa, y1 + dya);
		polygon.addPoint(x1 + dxa, y1 - dya);
		polygon.addPoint(x2 + dxa, y2 - dya);
		polygon.addPoint(x2 - dxa, y2 + dya);
		// Add polygon to the shapes list.
		shapes.add(polygon);
		
		double diameter = 2 * radius;
		
		// Create circle 1 and 2. Add them to the output list.
		Ellipse2D circle1 = new Ellipse2D.Double(x1 - radius, y1 - radius,
				diameter, diameter);
		Ellipse2D circle2 = new Ellipse2D.Double(x2 - radius, y2 - radius,
				diameter, diameter);
		shapes.add(circle1);
		shapes.add(circle2);
		
		return shapes;
	}
	
	/**
	 * Draw selection.
	 * @param g
	 * @param component
	 * @param isSelected
	 * @param hasFocus
	 */
	public static void drawDefaultValue(Graphics g, Component component) {
		
		Graphics2D g2 = (Graphics2D) g;

		g2.setPaint(defaultTexturePaint);
		
		Rectangle bounds = component.getBounds();
		int right = (int) bounds.getWidth();
		int bottom = (int) bounds.getHeight();
		
		g2.fillRect(0, 0, right, bottom);
	}

	/**
	 * Draw boolean value.
	 * @param g
	 * @param component
	 * @param booleanValue
	 */
	public static void drawBooleanValue(Graphics g,
			Component component, boolean booleanValue) {
		
		Graphics2D g2 = (Graphics2D) g;

		Rectangle bounds = component.getBounds();
		int height = (int) bounds.getHeight();
		
		final int imageSize = 13;
		final int leftPadding = 1;
		
		g2.drawImage(booleanValue ? trueImage : falseImage, leftPadding,
				(height - imageSize) / 2, null);
	}

	/**
	 * Draw selection.
	 * @param g
	 * @param component
	 * @param isSelected
	 * @param hasFocus
	 */
	public static void drawSelection(Graphics g, Component component,
			boolean isSelected, boolean hasFocus) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		// If is selected.
		if (isSelected) {

			// Get properties.
			Composite oldComposite = g2.getComposite();
			Color oldColor = g2.getColor();
	
			// Set opacity.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			// Draw rectangle.
			g2.setColor(colorHighlight);
			
			// Draw rectangle.
			Dimension dimension = component.getSize();
			Rectangle rectangle = new Rectangle(dimension);
			g2.fill(rectangle);
			
			// If has focus.
			if (hasFocus) {
				
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
				rectangle.setSize(dimension.width - 1, dimension.height - 1);
				g2.draw(rectangle);
			}
			
			// Set old properties.
			g2.setComposite(oldComposite);
			g2.setColor(oldColor);
		}
	}

	/**
	 * Fill rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void fillRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		
		g2.fillRect(xInt, yInt, widthInt, heightInt);
	}

	/**
	 * Fill label rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public static void fillLabelRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height, double arcWidth, double arcHeight) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			Point2D arcWidthHeight = transformation.deltaTransform(
					new Point2D.Double(arcWidth, arcHeight), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
			arcWidth = arcWidthHeight.getX();
			arcHeight = arcWidthHeight.getY();
		}
		
		Path2D path = new Path2D.Double();
		path.moveTo(x, y + height);
		path.append(new Arc2D.Double(x, y, arcWidth, arcHeight, 180, -90, Arc2D.OPEN), true);
		path.lineTo(x + width - arcWidth / 2, y);
		path.append(new Arc2D.Double(x + width - arcWidth, y, arcWidth, arcHeight, 90, -90, Arc2D.OPEN), true);
		path.lineTo(x + width, y + height);
		
		g2.fill(path);
	}

	/**
	 * Draw free rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public static void fillFreeRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y,
			double width, double height, double arcWidth,
			double arcHeight) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			Point2D arcWidthHeight = transformation.deltaTransform(
					new Point2D.Double(arcWidth, arcHeight), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
			arcWidth = arcWidthHeight.getX();
			arcHeight = arcWidthHeight.getY();
		}
		
		Path2D path = new Path2D.Double();
		path.append(new Arc2D.Double(x + width - arcWidth, y + height - arcHeight, arcWidth, arcHeight, 0, -90, Arc2D.OPEN), true);
		path.lineTo(x, y + height);
		path.lineTo(x, y);
		path.lineTo(x + width, y);
		
		g2.fill(path);
	}

	/**
	 * Fill root rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param arcWidth
	 * @param arcHeight
	 */
	public static void fillRootRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height, double arcWidth, double arcHeight) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			Point2D arcWidthHeight = transformation.deltaTransform(
					new Point2D.Double(arcWidth, arcHeight), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
			arcWidth = arcWidthHeight.getX();
			arcHeight = arcWidthHeight.getY();
		}
		
		Path2D path = new Path2D.Double();
		path.append(new Arc2D.Double(x, y + height - arcHeight, arcWidth, arcHeight, -90, -90, Arc2D.OPEN), true);
		path.lineTo(x, y);
		path.lineTo(x + width, y);
		path.lineTo(x + width, y + height);
		
		g2.fill(path);
	}

	/**
	 * Draw rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		
		g2.drawRect(xInt, yInt, widthInt, heightInt);
	}

	/**
	 * Draw round rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawRoundRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height, double arcWidth, double arcHeight) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			Point2D arcWidthHeight = transformation.deltaTransform(
					new Point2D.Double(arcWidth, arcHeight), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
			arcWidth = arcWidthHeight.getX();
			arcHeight = arcWidthHeight.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		int arcWidthInt = (int) arcWidth;
		int arcHeightInt = (int) arcHeight;
		
		g2.drawRoundRect(xInt, yInt, widthInt, heightInt, arcWidthInt, arcHeightInt);
	}

	/**
	 * Draw recursion.
	 * @param g2
	 * @param transformation
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static void drawRecursionTransform(Graphics2D g2,
			AffineTransform transformation, double x1, double y1, double x2,
			double y2) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D point1 = transformation.transform(
					new Point2D.Double(x1, y1), null);
			Point2D point2 = transformation.transform(
					new Point2D.Double(x2, y2), null);
			
			x1 = point1.getX();
			y1 = point1.getY();
			x2 = point2.getX();
			y2 = point2.getY();
		}
		
		int x1Int = (int) x1;
		int y1Int = (int) y1;
		int x2Int = (int) x2;
		int y2Int = (int) y2;
		int widthInt = x2Int - x1Int;
		
		float widthFloat = (float) widthInt;
		
		// Compute line strength.
		float lineStrength = widthFloat * 0.18f;
		int lineStrengthInt = (int) lineStrength;
		if (lineStrengthInt == 0) {
			lineStrength = 1;
		}
		
		// Compute corner size.
		float cornerSize = widthFloat * 0.4f;
		int cornerSizeInt = (int) cornerSize;
		
		// Set arrow size.
		float arrowWidth = widthFloat * 0.5f;
		int arrowWidthInt = (int) arrowWidth;
		float arrowHeight = arrowWidth * 1.25f;
		int arrowHeightInt = (int) arrowHeight;
		
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(lineStrengthInt, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		// Draw symbol.
		g2.drawLine(x1Int, y1Int + arrowHeightInt / 2, x1Int, y2Int - cornerSizeInt);
		g2.drawArc(x1Int, y2Int - cornerSizeInt * 2, cornerSizeInt * 2, cornerSizeInt * 2, 180, 90);
		g2.drawLine(x1Int + cornerSizeInt, y2Int, x2Int - cornerSizeInt, y2Int);
		g2.drawArc(x2Int - cornerSizeInt * 2, y2Int - cornerSizeInt * 2, cornerSizeInt * 2, cornerSizeInt * 2, 270, 90);
		g2.drawLine(x2Int, y2Int - cornerSizeInt, x2Int, y1Int);
		g2.drawLine(x2Int, y1Int, x2Int - arrowWidthInt / 2, y1Int + arrowHeightInt / 2);
		g2.drawLine(x2Int, y1Int, x2Int + arrowWidthInt / 2, y1Int + arrowHeightInt / 2);
		
		g2.setStroke(oldStroke);
	}

	/**
	 * Draw line.
	 * @param g2
	 * @param transformation
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public static void drawLineTransform(Graphics2D g2,
			AffineTransform transformation, double x1, double y1,
			double x2, double y2) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D point1 = transformation.transform(
					new Point2D.Double(x1, y1), null);
			Point2D point2 = transformation.transform(
					new Point2D.Double(x2, y2), null);
			
			x1 = point1.getX();
			y1 = point1.getY();
			x2 = point2.getX();
			y2 = point2.getY();
		}
		
		int x1Int = (int) x1;
		int y1Int = (int) y1;
		int x2Int = (int) x2;
		int y2Int = (int) y2;
		
		g2.drawLine(x1Int, y1Int, x2Int, y2Int);
	}

	/**
	 * Clip rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void clipRectTransform(Graphics2D g2,
			AffineTransform transformation, double x, double y, double width,
			double height) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		
		g2.clipRect(xInt, yInt, widthInt, heightInt);
	}

	/**
	 * Draw image.
	 * @param g2
	 * @param transformation
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawImageTransform(Graphics2D g2,
			AffineTransform transformation, BufferedImage image, double x,
			double y, double width, double height) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		
		g2.drawImage(image, xInt, yInt, widthInt, heightInt, null);
	}
	
	/**
	 * Draw house icon.
	 * @param g2
	 * @param transformation
	 * @param d
	 * @param e
	 * @param houseSize
	 * @param houseSize2
	 */
	public static void drawHouseTransform(Graphics2D g2, AffineTransform transformation,
			double x, double y, double width, double height, double strokePercetntWidth) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();
		}
		
		g2.setStroke(new BasicStroke((float) (width * strokePercetntWidth / 100.0), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		
		double roofHeight = height * 0.45;
		double brickworkWidth = width * 0.67;
		double brickworkHeight = height - roofHeight;
		double brickworkLRSpace = (width - brickworkWidth) / 2.0;
		double brickworkTop = height - brickworkHeight;
		
		Path2D house = new Path2D.Double();
		house.moveTo(x + brickworkLRSpace, y + roofHeight);
		house.lineTo(x, y + roofHeight);
		house.lineTo(x + width / 2.0, y);
		house.lineTo(x + width, y + roofHeight);
		house.lineTo(x + width - brickworkLRSpace, y + roofHeight);
		house.lineTo(x + width - brickworkLRSpace, y + height);
		house.lineTo(x + brickworkLRSpace, y + height);
		house.lineTo(x + brickworkLRSpace, y + brickworkTop);
		
		g2.draw(house);
	}
	
	/**
	 * Draw arrow.
	 * @param g2
	 * @param transformation
	 * @param stroke
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param alpha
	 * @param length
	 */
	public static void drawArrowTransform(Graphics2D g2,
			AffineTransform transformation, double stroke, double x1, double y1,
			double x2, double y2, double alpha, double length) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D xY = transformation.transform(
					new Point2D.Double(x1, y1), null);

			x1 = xY.getX();
			y1 = xY.getY();
			
			xY =  transformation.transform(
					new Point2D.Double(x2, y2), null);

			x2 = xY.getX();
			y2 = xY.getY();
			
			Point2D sizeVector = transformation.deltaTransform(
					new Point2D.Double(stroke, 0), null);

			stroke = sizeVector.getX();
			
			sizeVector = transformation.deltaTransform(
					new Point2D.Double(length, 0), null);

			length = sizeVector.getX();
		}
		
		int strokeInt = (int) stroke;
		int x1Int = (int) x1;
		int y1Int = (int) y1;
		int x2Int = (int) x2;
		int y2Int = (int) y2;
		int lengthInt = (int) length;
		
		g2.setStroke(new BasicStroke(strokeInt, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		drawArrow(g2, x1Int, y1Int, x2Int, y2Int, alpha, lengthInt);
	}

	/**
	 * Draw string.
	 * @param g2
	 * @param transformation
	 * @param iterator
	 * @param x
	 * @param y
	 */
	public static void drawStringTransform(Graphics2D g2,
			AffineTransform transformation,
			AttributedCharacterIterator iterator, double x, double y) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);

			x = leftTop.getX();
			y = leftTop.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		
		g2.drawString(iterator, xInt, yInt);
	}

	/**
	 * Draw string.
	 * @param g2
	 * @param transformation
	 * @param text
	 * @param x
	 * @param y
	 */
	public static void drawStringTransform(Graphics2D g2,
			AffineTransform transformation, String text, double x, double y) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);

			x = leftTop.getX();
			y = leftTop.getY();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		
		g2.drawString(text, xInt, yInt);
	}

	/**
	 * Get size.
	 * @param transformation
	 * @param size
	 * @return
	 */
	public static int getSizeTransform(AffineTransform transformation,
			double size) {
		
		// Perform transformation.
		if (transformation != null) {
			Point2D sizeVector = transformation.deltaTransform(
					new Point2D.Double(size, 0), null);

			size = sizeVector.getX();
		}
		
		int sizeInt = (int) size;
		
		return sizeInt;
	}
	
	/**
	 * Draw gradient rectangle.
	 * @param g2
	 * @param transformation
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param lineSize
	 * @param color
	 * @param intensity
	 */
	public static void drawGradientRectangleTransf(Graphics2D g2,
			AffineTransform transformation, double x, double y,
			double width, double height, double lineSize, Color color,
			float intensity) {
		
		// Perform transformation.
		if (transformation != null) {
			
			Point2D leftTop = transformation.transform(
					new Point2D.Double(x, y), null);
			Point2D widthHeight = transformation.deltaTransform(
					new Point2D.Double(width, height), null);
			
			x = leftTop.getX();
			y = leftTop.getY();
			width = widthHeight.getX();
			height = widthHeight.getY();

			Point2D sizeVector = transformation.deltaTransform(
					new Point2D.Double(lineSize, 0), null);

			lineSize = sizeVector.getX();
		}
		
		int xInt = (int) x;
		int yInt = (int) y;
		int widthInt = (int) width;
		int heightInt = (int) height;
		int lineSizeInt = (int) lineSize;
		
		drawGradientRectangle(g2, xInt, yInt,
			widthInt, heightInt, lineSizeInt, color, intensity);
	}

}
