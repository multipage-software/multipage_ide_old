/**
 * 
 */
package program.builder.dynamic;

import general.gui.GraphUtility;
import general.gui.Images;

import java.awt.*;
import java.awt.geom.*;

import program.generator.Affected;
import program.generator.ColorId;
import program.generator.CustomizedColors;

import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class MacroElementShape implements AffectedDynamic {
	
	/**
	 * Label labelHeight.
	 */
	private static final int labelHeight = 30;
	
	/**
	 * Font labelHeight in percent.
	 */
	private static final int fontHeightPercent = 80;

	/**
	 * Left position;
	 */
	private int left;
	
	/**
	 * Top position.
	 */
	private int top;

	/**
	 * Width.
	 */
	private int width = StepShape.initialWidth;
	
	/**
	 * Height of the canvas.
	 */
	private int height = StepShape.initialHeight;
	
	/**
	 * Macro element reference.
	 */
	private MacroElement macroElement = null;

	
	/**
	 * Set left position.
	 */
	public void setLeft(int left) {

		this.left = left;
	}
	
	/**
	 * Sets shape width.
	 */
	public void setWidth(int width) {

		this.width = width;
	}

	/**
	 * Gets label height.
	 * @return
	 */
	public int getLabelHeight() {

		return labelHeight;
	}
	
	/**
	 * Get font labelHeight.
	 */
	private int getFontHeight() {
		
		return labelHeight * fontHeightPercent / 100;
	}

	/**
	 * Get width.
	 * @return
	 */
	public int getWidth() {

		return width;
	}

	/**
	 * Draw shape.
	 */
	public void draw(Graphics2D g2) {
		
		if (macroElement != null) {
			
			String description = macroElement.toString();
			
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
	
			// Create font.
			int fontHeight = (int) getFontHeight();
			Font font = new Font("SansSerif", Font.PLAIN, fontHeight);
			Font oldFont = g2.getFont();
			g2.setFont(font);
			FontMetrics metrics = g2.getFontMetrics();
			Rectangle2D descRect = metrics.getStringBounds(description, g2);
					
			// Fill start label area.
			g2.setColor(CustomizedColors.get(ColorId.FILLLABEL_PROTECTED));
			g2.fillRect(left, top, width, labelHeight);
			
			// Draw start label outline.
			g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));
			g2.drawRect(left, top, width, labelHeight);
			
			int closeIconWidth = 0;
			
			// If it is a sub level, draw close icons.
			if (macroElement instanceof Procedure) {
				
				Image closeIcon = Images.getImage("program/generator/images/remove_icon.png");
				if (closeIcon != null) {
					
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
					g2.drawImage(closeIcon, left, top, null);
					
					closeIconWidth = closeIcon.getWidth(null);
					
					g2.drawImage(closeIcon, left + width - closeIconWidth, top, null);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
				}
			}
			
			int descWidth = (int) descRect.getWidth();
			int baseLine = top + labelHeight / 2 + fontHeight / 2 - metrics.getDescent() / 2;
			int descStart;
			
			if (descWidth > width - 4 - 2 * closeIconWidth) {
				descStart = left + 2 + closeIconWidth;
				descWidth = width - 4 - 2 * closeIconWidth;
			}
			else {
				descStart = left + 2 + (width - descWidth) / 2;
			}
			
			// Draw text.
			Rectangle oldClip = g2.getClipBounds();
			g2.clipRect(descStart, top, descWidth, labelHeight);
			g2.setColor(CustomizedColors.get(ColorId.TEXT));
			g2.drawString(description, descStart, baseLine);
			g2.setClip((int) oldClip.getX(), (int) oldClip.getY(), (int) oldClip.getWidth(), (int) oldClip.getHeight());
			
			// Fill end label area.
			int canvasBottom = top + labelHeight + height;
			g2.setColor(CustomizedColors.get(ColorId.FILLLABEL_PROTECTED));
			g2.fillRect(left, canvasBottom, width, labelHeight);
			
			// Draw end label outline.
			g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));
			g2.drawRect(left, canvasBottom, width, labelHeight);
			
			// Set old font.
			g2.setFont(oldFont);
		}
	}

	/**
	 * Get label rectangle.
	 */
	public Rectangle getLabelRect() {

		Rectangle returnedRectangle = new Rectangle(left, top,
				width, labelHeight);
		return returnedRectangle;
	}

	/**
	 * Set top position.
	 */
	public void setTop(int top) {

		this.top = top;
	}

	/**
	 * Get label bottom.
	 */
	public int getLabelBottom() {

		return top + labelHeight;
	}

	/**
	 * Gets right position.
	 */
	public int getRight() {

		return left + width;
	}

	/**
	 * Sets macro element reference.
	 */
	public void setMacroElement(MacroElement macroElement) {

		this.macroElement = macroElement;
		macroElement.setUser(this);
	}

	/**
	 * Return true if the mouse point is on close button.
	 */
	public boolean isOnCloseButton(Point2D transformedMouse) {
		
		Rectangle leftRectangle = new Rectangle(left, top, 16, 16);
		Rectangle rightRectangle = new Rectangle(left + width - 16, top, 16, 16);
		
		return leftRectangle.contains(transformedMouse) || rightRectangle.contains(transformedMouse);
	}

	/**
	 * Returns true if the mouse is on this shape label.
	 * @param transformesMouse
	 * @return
	 */
	public boolean labelContains(Point2D transformesMouse) {
		
		Rectangle rectangle = new Rectangle(left, top, width, labelHeight);

		return rectangle.contains(transformesMouse);
	}

	/**
	 * Gets left location.
	 * @return
	 */
	public int getLeft() {

		return left;
	}

	/**
	 * Gets top location.
	 * @return
	 */
	public int getTop() {

		return top;
	}

	/**
	 * Draw affected rectangle.
	 */
	@Override
	public void drawAffected(Graphics2D g2, double zoom) {
		
		int top = getTop();
		
		Color color = CustomizedColors.get(ColorId.OUTLINES_PROTECTED);
		int lineWidth = (int) (ProgramDiagram.affectedLineWidth / zoom);

		// Draw gradient rectangle.
		GraphUtility.drawGradientRectangle(g2, left, top, width, labelHeight,
				lineWidth, color,
				ProgramDiagram.affectedIntensity);
	}

	/**
	 * Gets macro element.
	 * @return
	 */
	@Override
	public MacroElement getMacroElement() {

		return macroElement;
	}

	/**
	 * Gets label central point.
	 */
	@Override
	public Point getLabelCenter() {

		return new Point(left + width / 2, top + labelHeight / 2);
	}

	/**
	 * Returns true value if the macro element has the affected step.
	 */
	@Override
	public boolean isNext(Affected affectedElement) {

		// If the affected element is not a step exit the method.
		if (!(affectedElement instanceof StepShape)) {
			return false;
		}
		
		StepShape stepShape = (StepShape) affectedElement;
		Step step = stepShape.getStep();
		if (step == null || macroElement == null) {
			return false;
		}
		
		// Check if it is a sub step.
		return step.isStart();
	}

	/**
	 * Set dimension.
	 * @param width
	 * @param height
	 */
	public void setDimension(int width, int height) {

		this.width = width;
		this.height = height;
	}

	/**
	 * Get end label top.
	 * @return
	 */
	public int getEndLabelTop() {

		return top + labelHeight + height;
	}

	/**
	 * Gets rectangle.
	 * @return
	 */
	public Rectangle getRectangle() {

		return new Rectangle(left, top, width, height + 2 * labelHeight);
	}

	/**
	 * Gets height.
	 * @return
	 */
	public int getHeight() {

		return height + 2 * labelHeight;
	}

	/**
	 * Gets shape.
	 * @param macroElement
	 * @return
	 */
	public static MacroElementShape getShape(MacroElement macroElement) {

		Object user = macroElement.getUser();
		
		if (user instanceof MacroElementShape) {
			return (MacroElementShape) user;
		}
		return null;
	}
}
