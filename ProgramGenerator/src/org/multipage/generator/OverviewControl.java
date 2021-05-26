/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.multipage.gui.CursorArea;
import org.multipage.gui.CursorAreaImpl;
import org.multipage.gui.CursorAreaListener;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;


/**
 * @author
 *
 */
public class OverviewControl implements CursorArea {

	/**
	 * Overview window size.
	 */
	private static int windowSize = 150;
	
	/**
	 * Minimal view size.
	 */
	private static final int minimalViewSize = 6;

	/**
	 * Window visible flag.
	 */
	private static boolean winVisible;
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Read visibility flag.
		winVisible = inputStream.readBoolean();	
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		// Write visibility flag.
		outputStream.writeBoolean(winVisible);
	}

	/**
	 * Set default.
	 */
	public static void setDefaultData() {

		winVisible = true;
	}

	/**
	 * Window cursor area.
	 */
	private CursorAreaImpl windowCursorArea;
	
	/**
	 * Button cursor area.
	 */
	private CursorAreaImpl buttonCursorArea;

	/**
	 * Get cursor area.
	 */
	@Override
	public CursorAreaImpl getCursorArea() {

		return windowCursorArea;
	}

	/**
	 * Button cursor area
	 */
	public CursorAreaImpl getButtonCursorArea() {

		return buttonCursorArea;
	}

	/**
	 * Location.
	 */
	private Point location = new Point();
	
	/**
	 * Size.
	 */
	private Dimension size = new Dimension();
	
	/**
	 * Current viewport rectangle.
	 */
	private Rectangle2D viewport = new Rectangle2D.Double();

	/**
	 * Current diagram rectangle.
	 */
	private Rectangle2D diagramNotMovedNotScaled = new Rectangle2D.Double();
	
	/**
	 * Listener.
	 */
	private OverviewAdapter listener;

	/**
	 * Translation.
	 */
	private double translationX = 0.0;
	private double translationY = 0.0;

	/**
	 * Scale.
	 */
	private double scale = 1.0;

	/**
	 * Origin.
	 */
	private Point2D origin = new Point2D.Double();

	/**
	 * Overview viewport.
	 */
	private Rectangle2D overviewViewport = new Rectangle2D.Double();

	/**
	 * Diagram scale.
	 */
	private double diagramScale = 1.0;

	/**
	 * Visibility flag.
	 */
	private boolean flagVisible = false;

	/**
	 * Right space flag.
	 */
	private boolean isRightSpace = true;

	/**
	 * Bottom space flag.
	 */
	private boolean isBottomSpace = false;

	/**
	 * Diagram.
	 */
	private GeneralDiagram diagram;

	/**
	 * Constructor.
	 * @param diagram
	 */
	public OverviewControl(GeneralDiagram diagram) {

		this.diagram = diagram;
		
		windowCursorArea = new CursorAreaImpl(
				Images.loadCursor("org/multipage/generator/images/overview_cursor.png",
						new Point(16, 16)), diagram,
						new CursorAreaListener() {
							@Override
							public boolean visible() {
								return flagVisible && winVisible;
							}
						});
		buttonCursorArea = new CursorAreaImpl(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), diagram,
				new CursorAreaListener() {
					@Override
					public boolean visible() {
						return flagVisible;
					}
				});
	}

	/**
	 * Set size.
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height) {

		size.setSize(width, height);
		setCursorArea();
	}

	/**
	 * @return the size
	 */
	public Dimension getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Dimension size) {
		this.size = size;
		setCursorArea();
	}

	/**
	 * Returns true if it is visible.
	 */
	public boolean isVisible() {
		
		return flagVisible;
	}
	
	/**
	 * Returns true if the button contains point.
	 * @param point
	 * @return
	 */
	public boolean contains(Point point) {

		Rectangle button = getButtonRectangle();
		Rectangle window = getWindow();
		
		return isVisible() && (button.contains(point) || isOpened() && window.contains(point));
	}

	/**
	 * Draw button.
	 */
	public void draw(Graphics2D g2) {
		
		// If it is visible, draw it.
		if (isVisible()) {
			
			g2.setColor(CustomizedColors.get(ColorId.SCROLLBARS));
			
			// Fill rectangle.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			g2.fillRect(location.x,
					    location.y,
					    size.width,
					    size.height);
			
			// Draw rectangle.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g2.drawRect(location.x,
					    location.y,
					    size.width,
					    size.height);
			
			// Draw image.
			BufferedImage image = Images.getImage("org/multipage/generator/images/overview.png");
			if (image != null) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				g2.drawImage(image,
						     location.x,
						     location.y,
						     location.x + size.width,
						     location.y + size.height,
						     0, 0,
						     image.getWidth(),
						     image.getHeight(),
						     null);
			}
			
			// If window is visible, draw it.
			if (winVisible) {
				
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
				
				int x = location.x + (isRightSpace ? 0 : size.width) - windowSize;
				int y = location.y + (isBottomSpace ? 0 : size.height) - windowSize;
				int width = windowSize - (isRightSpace ? 0 : 1);
				int height = windowSize - (isBottomSpace ? 0 : 1);
				
				// Fill area.
				g2.setColor(CustomizedColors.get(ColorId.BACKGROUND));
				g2.fillRect(x, y, width, height);
				
				// Draw content.
				if (listener != null && scale != 0.0) {
					
					// Clip rectangle.
					Shape oldClip = g2.getClip();
					g2.clipRect(x, y, width, height);
					
					// Set translation and scale.
					AffineTransform transform = new AffineTransform();
					transform.translate(x - origin.getX() + 1, y - origin.getY() + 1);
					transform.scale(scale, scale);
					
					transform.translate(translationX, translationY);
					transform.scale(diagramScale, diagramScale);
					
					// Draw diagram content.
					listener.onDrawContent(g2, transform);
					
					// Create auxiliary viewport.
					double viewX = overviewViewport.getX();
					double viewY = overviewViewport.getY();
					double viewWidth = overviewViewport.getWidth();
					double viewHeight = overviewViewport.getHeight();
					
					if (viewWidth < minimalViewSize) {
						viewWidth = minimalViewSize;
						viewX -= viewWidth / 2;
					}
					if (viewHeight < minimalViewSize) {
						viewHeight = minimalViewSize;
						viewY -= viewHeight / 2;
					}
					Rectangle2D overviewViewportAux = new Rectangle2D.Double(
							viewX, viewY, viewWidth, viewHeight);
					
					// Fill viewport complement.
					g2.setColor(CustomizedColors.get(ColorId.OVERVIEWBACKGROUND));
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					Area withoutViewport = new Area(new Rectangle(x, y, width, height));
					withoutViewport.subtract(new Area(overviewViewportAux));
					g2.fill(withoutViewport);

					// Draw viewport.
					g2.setColor(Color.BLACK);
					g2.draw(new Rectangle2D.Double(overviewViewportAux.getX(), overviewViewportAux.getY(),
							overviewViewportAux.getWidth(), overviewViewportAux.getHeight()));
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
					
					// Set old clip.
					g2.setClip(oldClip);
				}
				
				// Draw area border.
				g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));
				g2.drawRect(x, y, width, height);
			}
		}
	}

	/**
	 * On mouse action.
	 * @param mouse 
	 */
	public void onMouseAction(Point mouse) {
		
		// If the overview is not visible, exit the method.
		if (!flagVisible) {
			return;
		}

		Rectangle button = new Rectangle();
		button.setLocation(location);
		button.setSize(size);
		
		Rectangle window = getWindow();
		
		if (button.contains(mouse)) {
			// Toggle overview window visibility.
			winVisible = !winVisible;
		}
		// If the window is visible and the mouse is inside the window...
		else if (winVisible && window.contains(mouse)) {
			setViewport(mouse.x, mouse.y);
		}
	}
	
	/**
	 * Gets window rectangle.
	 * @return
	 */
	private Rectangle getWindow() {
		
		return new Rectangle(
			location.x + (isRightSpace ? 0 : size.width) - windowSize,
			location.y + (isBottomSpace ? 0 : size.height) - windowSize,
			windowSize - (isRightSpace ? 0 : 1),
			windowSize - (isBottomSpace ? 0 : 1));
	}

	/**
	 * Sets viewport.
	 */
	private void setViewport(double x, double y) {

		double overviewCenterX = overviewViewport.getX() + overviewViewport.getWidth() / 2;
		double overviewCenterY = overviewViewport.getY() + overviewViewport.getHeight() / 2;
		double vectorX = (overviewCenterX - x) / scale;
		double vectorY = (overviewCenterY - y) / scale;
		
		// Animate diagram translation.
		diagram.animateTranslationAndScaleRelative(vectorX, vectorY, 1.0, true, false);
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(OverviewAdapter listener) {
		this.listener = listener;
	}

	/**
	 * Sets location using right and bottom coordinates.
	 * @param right
	 * @param bottom
	 */
	public void setRightBottomLocation(int right, int bottom) {

		location.x = right - (int) size.getWidth();
		location.y = bottom - (int) size.getHeight();
		setCursorArea();
	}

	/**
	 * Sets cursor area.
	 */
	private void setCursorArea() {

		windowCursorArea.setShape(getWindow());
		
		buttonCursorArea.setShape(new Rectangle(
				location, size));
	}

	/**
	 * Sets viewport rectangle.
	 * @param rectangle
	 */
	public void setViewportRectangle(Rectangle rectangle) {

		// Set viewport and compute overview parameters.
		viewport = rectangle;
		computeOverviewParameters();
	}

	/**
	 * Sets diagramNotMovedNotScaled rectangle.
	 * @param rectangle
	 */
	public void setDiagramRectangle(Rectangle2D rectangle) {

		// Set diagramNotMovedNotScaled and compute overview parameters.
		diagramNotMovedNotScaled = rectangle;
		computeOverviewParameters();
	}
	
	/**
	 * Set translation.
	 */
	public void setTranslation(double x, double y) {
		
		translationX = x;
		translationY = y;
		computeOverviewParameters();
	}
	
	/**
	 * Set scale.
	 */
	public void setScale(double diagramScale) {
		
		this.diagramScale = diagramScale;
		computeOverviewParameters();
	}
	
	/**
	 * Compute overview parameters.
	 */
	private void computeOverviewParameters() {
		
		Rectangle2D.Double diagram = new Rectangle2D.Double(
				diagramNotMovedNotScaled.getX(),
				diagramNotMovedNotScaled.getY(),
				diagramNotMovedNotScaled.getWidth(),
				diagramNotMovedNotScaled.getHeight());
		// Scale diagram.
		diagram.x *= diagramScale;
		diagram.y *= diagramScale;
		diagram.width *= diagramScale;
		diagram.height *= diagramScale;
		// Move diagram.
		diagram.x += translationX;
		diagram.y += translationY;
		Rectangle2D wholeRect = Utility.union(diagram, viewport);
		
		// If the width is longer than height.
		if (wholeRect.getWidth() >= wholeRect.getHeight()) {
			
			if (wholeRect.getWidth() != 0) {
				scale = (double) (windowSize - 2) / wholeRect.getWidth();
				origin = new Point2D.Double(wholeRect.getX() * scale,
						wholeRect.getY() * scale - (windowSize - 2 - wholeRect.getHeight() * scale) / 2 );
			}
		}
		else {
			
			if (wholeRect.getHeight() != 0) {
				scale = (double) (windowSize - 2) / wholeRect.getHeight();
				
				origin = new Point2D.Double(wholeRect.getX() * scale - (windowSize - 2 - wholeRect.getWidth() * scale) / 2,
						wholeRect.getY() * scale);
			}
		}
		
		// Set viewport rectangle.
		double x = location.x + (isRightSpace ? 0 : size.width) + 1 - windowSize;
		double y = location.y + (isBottomSpace ? 0 : size.height) + 1 - windowSize;
		overviewViewport = new Rectangle2D.Double(viewport.getX() * scale + x - origin.getX(),
				viewport.getY() * scale + y - origin.getY(),
				viewport.getWidth() * scale,
				viewport.getHeight() * scale);
	}

	/**
	 * Sets right and bottom spaces.
	 */
	public void setRightBottomSpace(boolean right, boolean bottom) {
		
		this.isRightSpace = right;
		this.isBottomSpace = bottom;
		
		computeOverviewParameters();
		
		setCursorArea();
	}
	
	/**
	 * Sets visibility flag.
	 */
	public void setVisible(boolean visible) {
		
		flagVisible = visible;
	}
	
	/**
	 * Returns true if the overview window is opened.
	 */
	public static boolean isOpened() {
		
		return winVisible;
	}

	/**
	 * Gets rectangle.
	 * @return
	 */
	public Rectangle getRectangle() {

		return new Rectangle(location.x + (isRightSpace ? 0 : size.width) - windowSize,
				location.y + (isBottomSpace ? 0 : size.height) - windowSize,
				windowSize, windowSize);
	}

	/**
	 * Gets button rectangle.
	 * @return
	 */
	public Rectangle getButtonRectangle() {

		return new Rectangle(location.x, location.y, size.width, size.height);
	}

	/**
	 * Get overview zoom.
	 * @return
	 */
	public double getZoom() {

		return scale;
	}
}
