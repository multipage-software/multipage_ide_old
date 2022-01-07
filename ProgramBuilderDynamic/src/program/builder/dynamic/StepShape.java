/**
 * 
 */
package program.builder.dynamic;

import general.gui.GraphUtility;
import general.gui.Images;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import program.generator.Affected;
import program.generator.ColorId;
import program.generator.CustomizedColors;

import program.middle.dynamic.*;

/**
 * 
 * @author
 *
 */
public class StepShape implements AffectedDynamic {
	
	/**
	 * Initial width.
	 */
	public static final int initialWidth = 200;
	
	/**
	 * Initial height.
	 */
	public static final int initialHeight = 150;

	/**
	 * Condition initialHeight.
	 */
	private static final int conditionHeight = 40;

	/**
	 * Condition width.
	 */
	private static final int conditionWidth = 50;

	/**
	 * Condition stroke.
	 */
	private static final Stroke conditionStroke = new BasicStroke(3);
	
	/**
	 * Description initialHeight.
	 */
	private static final int descriptionHeight = 25;

	/**
	 * Description font.
	 */
	private static final Font descriptionFont = new Font("SansSerif", Font.BOLD, descriptionHeight - 10);

	/**
	 * Sub button size.
	 */
	private static final int subbtnSize = 16;

	/**
	 * Right and left extents.
	 */
	public static final double leftExtent = 30;
	public static final double rightExtent = 30;
	
	/**
	 * Location.
	 */
	private int x;
	private int y;
	private int width;
	private int height;
	
	/**
	 * Auxiliary flag.
	 */
	private boolean flag = false;
	
	/**
	 * Program step.
	 */
	private Step step;
	
	/**
	 * Visibility.
	 */
	private boolean visible = true;

	/**
	 * Dummy shape flag.
	 */
	private boolean dummy = false;

	/**
	 * Level number.
	 */
	private int levelNumber = 0;
	
	/**
	 * Affected top part.
	 */
	private boolean isTopAffected = false;

	/**
	 * Caller method that affects the step shape.
	 */
	private int caller = ProgramDiagram.AFF_NEWSTEP;
	
	/**
	 * Constructor.
	 * @param step
	 * @param x
	 * @param y
	 */
	public StepShape(Step step, int x, int y, int width, int height) {

		this.step = step;
		this.x = x;
		this.y = y;
		this.width = width;		
		this.height = height;
		
		// Set step user object to this shape.
		if (step != null) {
			step.setUser(this);
		}
	}

	/**
	 * Constructor.
	 * @param step
	 */
	public StepShape(Step step) {
		this(step, 0, 0, initialWidth, initialHeight);

	}

	/**
	 * Constructor
	 * @param step
	 * @param dummy
	 */
	public StepShape(Step step, boolean dummy) {
		this(step, 0, 0, initialWidth, initialHeight);
	
		this.dummy = dummy;
	}

	/**
	 * Draw shape.
	 * @param g2
	 * @param isMacroOpened 
	 */
	public void draw(Graphics2D g2, boolean isMacroOpened) {
		
		// If the shape is not visible exit the method.
		if (!visible) {
			return;
		}
				
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
		g2.setColor(CustomizedColors.get(step.isInactive() ? ColorId.INACTIVE_OUTLINES
				: ColorId.OUTLINES_PROTECTED));
		
		// If it is a dummy shape draw line.
		if (dummy) {
			int dummyX = x + width / 2;
			g2.drawLine(dummyX, y, dummyX, y + height);
			return;
		}
		
		int yPos = y;
		int procedureHeight = height;
		
		// If a condition exist, draw it.
		if (!step.getCondition().isEmpty()) {
			
			int xPos = x + width / 2;
			yPos = y + conditionHeight;
			g2.drawLine(xPos, y, xPos, yPos);
			
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(conditionStroke);
			
			int xCond = x + (width - conditionWidth) / 2;
			int yCond = y + conditionHeight / 2;
			g2.drawLine(xCond, yCond, xCond + conditionWidth, yCond);
			
			procedureHeight -= conditionHeight;
			
			g2.setStroke(oldStroke);
		}
		
		// Fill label.
		g2.setColor(CustomizedColors.get(step.isInactive() ? ColorId.INACTIVE_OUTLINES
				: ColorId.FILLLABEL_PROTECTED));
		g2.fillRect(x, yPos, width, descriptionHeight);
		
		// Fill body.
		g2.setColor(CustomizedColors.get(step.isInactive() ? ColorId.INACTIVE_BODIES
				: ColorId.FILLBODY));
		g2.fillRect(x, yPos + descriptionHeight, width, procedureHeight - descriptionHeight);
		
		g2.setColor(CustomizedColors.get(step.isInactive() ? ColorId.INACTIVE_OUTLINES
				: ColorId.OUTLINES_PROTECTED));
		g2.drawRect(x, yPos, width, procedureHeight);
		
		int descrY = yPos + (int) descriptionHeight;
		g2.drawLine(x, descrY, x + width, descrY);
		
		// Draw description.
		Shape oldClip = g2.getClip();
		int desX = x + 2;
		int desY = yPos + 2;
		int desWidth = width - 4;
		int desHeight = (int) descriptionHeight - 4;
		g2.clipRect(desX, desY, desWidth, desHeight);
		
		String description = step.toString();
		g2.setFont(descriptionFont);
		g2.setColor(CustomizedColors.get(step.isInactive() ? ColorId.SELECTED_TEXT
				: ColorId.TEXT));
		
		FontMetrics metrics = g2.getFontMetrics();
		Rectangle2D textRect = metrics.getStringBounds(description, g2);
		int textWidth = (int) textRect.getWidth();
		int textX;
		int textY = desY + desHeight * 80 / 100;
		
		// If the text width is greater than area.
		if (textWidth > desWidth) {
			textX = desX;
		}
		else {
			textX = desX + desWidth / 2 - textWidth / 2;
		}
		
		g2.drawString(description, textX, textY);
		
		g2.setClip(oldClip);
		
		
		// If the step has a sub level, draw the sign.
		if (step.hasSubLevel()) {
			
			// Set opacity depending on whether the step is an inactive.
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					step.isInactive() ? 0.5f : 1.0f));
			
			// Get image and draw it.
			BufferedImage subLevelSign
				= Images.getImage("program/builder/dynamic/images/sublevel.png");
			
			g2.drawImage(subLevelSign, x + width / 2 - 50, descrY + 10, 100,
					procedureHeight - descriptionHeight - 20, null);
		}
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		
		// Draw sub button.
		int subX = x + width - subbtnSize;
		int subY = descrY + 2;
		BufferedImage arrow;
		if (isMacroOpened) {
			arrow = Images.getImage("program/builder/dynamic/images/left_arrow.png");
		}
		else {
			arrow = Images.getImage("program/builder/dynamic/images/right_arrow.png");
		}
		g2.drawImage(arrow, subX, subY, subbtnSize, subbtnSize, null);
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the initialHeight
	 */
	public int getHeight() {
		return initialHeight;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set vertical size.
	 * @param width2
	 */
	public void setVertSize(int newWidth) {

		x = x + (width - newWidth) / 2;
		width = newWidth;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	/**
	 * @return the flag
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * Returns true if mouse on right arrow.
	 */
	public boolean isRightArrow(Point2D transformedMouse) {
		
		if (dummy) {
			return false;
		}

		Rectangle rect = new Rectangle(x + width - subbtnSize,
				                       y + (step.getCondition().isEmpty() ? 0 : conditionHeight) + descriptionHeight + 2,
				                       subbtnSize,
				                       subbtnSize);
		return rect.contains(transformedMouse);
	}

	/**
	 * Gets X center.
	 * @return
	 */
	public int getXcenter() {

		return x + width / 2;
	}

	/**
	 * Get y center.
	 */
	public int getYcenter() {
		
		int yStart;
		int shapeHeight;
		
		if (step.getCondition().isEmpty()) {
			yStart = y;
			shapeHeight = initialHeight;
		}
		else {
			yStart = y + conditionHeight;
			shapeHeight = initialHeight - conditionHeight;
		}
		
		return yStart + shapeHeight / 2;
	}

	/**
	 * Get shape rectangle.
	 */
	public Rectangle getRect() {

		return new Rectangle(x, y, width, getHeight());
	}

	/**
	 * Moves shape vertically.
	 */
	public void moveVerticaly(int moveY) {

		y += moveY;
	}

	/**
	 * Get body y start position.
	 */
	public int getBodyYstart() {

		if (step.getCondition().isEmpty()) {
			return y;
		}
		else {
			return y + conditionHeight;
		}
	}

	/**
	 * @return the step
	 */
	public Step getStep() {
		return step;
	}

	/**
	 * Gets true if the mouse is on label.
	 * @param transformedpanelMouse
	 * @return
	 */
	public boolean labelContains(Point2D transformedpanelMouse) {

		if (dummy) {
			return false;
		}
		
		Rectangle rectangle = new Rectangle(x,
				isCondition() ? y + conditionHeight : y,
				width, descriptionHeight);
		return rectangle.contains(transformedpanelMouse);
	}

	/**
	 * Returns true if the step has a condition.
	 * @return
	 */
	private boolean isCondition() {

		return !step.getCondition().isEmpty();
	}

	/**
	 * Gets description height.
	 * @return
	 */
	public static int getDescriptionHeight() {

		return descriptionHeight;
	}

	/**
	 * Set location.
	 * @param mouse
	 */
	public void setLocation(Point leftTop) {

		x = (int) leftTop.getX();
		y = (int) leftTop.getY();
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Draws affected rectangle.
	 */
	@Override
	public void drawAffected(Graphics2D g2, double zoom) {

		if (dummy) {
			return;
		}
		
		boolean isSplit = caller == ProgramDiagram.AFF_NEWSTEP;
		
		Color color = CustomizedColors.get(step.isInactive() ? ColorId.INACTIVE_OUTLINES
				: ColorId.OUTLINES_PROTECTED);
		int lineWidth = (int) (ProgramDiagram.affectedLineWidth / zoom);
		
		int halfHeight = isSplit ? height / 2 : height;
		int yPosition = isSplit ? (isTopAffected ? y : y + halfHeight) : y;

		// Draw gradient rectangle.
		GraphUtility.drawGradientRectangle(g2, x, yPosition, width, halfHeight,
				lineWidth, color,
				ProgramDiagram.affectedIntensity);
		
		if (isSplit) {
			
			int centerX = x + width / 2;
			
			// Get old and set new stroke.
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(new BasicStroke((float) (6.0 / zoom),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			
	
			// Draw arrow.
			if (isTopAffected) {
				GraphUtility.drawArrow(g2, centerX, y, centerX, y - 60,
						ProgramDiagram.arrowAlpha, ProgramDiagram.arrowLength);
			}
			else {
				int bottom = getBottom();
				GraphUtility.drawArrow(g2, centerX, bottom, centerX, bottom + 60,
						ProgramDiagram.arrowAlpha, ProgramDiagram.arrowLength);
			}
			
			// Set original stroke.
			g2.setStroke(oldStroke);
		}
	}

	/**
	 * Returns true value if the step shape contains a point.
	 * @param transformesMouse
	 * @param caller 
	 * @return
	 */
	public boolean contains(Point2D transformesMouse, int caller) {

		if (dummy) {
			return false;
		}
		
		this.caller  = caller;
		
		Rectangle topRectangle = getTopRect();
		if (topRectangle.contains(transformesMouse)) {
			
			// Set flag and return true value.
			isTopAffected = true;
			return true;
		}
		
		Rectangle bottomRectangle = getBottomRect();
		if (bottomRectangle.contains(transformesMouse)) {
			
			// Reset flag and return true value.
			isTopAffected = false;
			return true;
		}
		
		return false;
	}

	/**
	 * Gets top rectangle.
	 * @return
	 */
	private Rectangle getTopRect() {

		return new Rectangle(x, y, width, height / 2);
	}

	/**
	 * Gets bottom rectangle.
	 * @return
	 */
	private Rectangle getBottomRect() {

		int halfHeight = height / 2;
		
		return new Rectangle(x, y + halfHeight, width, halfHeight);
	}

	/**
	 * Get central point.
	 */
	@Override
	public Point getLabelCenter() {

		return new Point(x + width / 2, getYcenter());
	}

	/**
	 * Gets macro element.
	 */
	@Override
	public MacroElement getMacroElement() {

		if (step != null) {
			return step.getMacroElement();
		}
		
		return null;
	}

	/**
	 * Returns true value if the step has the next element.
	 */
	@Override
	public boolean isNext(Affected endElement) {

		// If the end element is not a step, exit the method.
		if (!(endElement instanceof StepShape)) {
			return false;
		}
		
		StepShape endShape = (StepShape) endElement;
		Step thisStep = getStep();
		Step endStep = endShape.getStep();
		
		// Check steps.
		if (thisStep == null || endStep == null) {
			return false;
		}
		
		// If the edge already exists, exit the method.
		MacroElement macroElement = thisStep.getMacroElement();
		if (macroElement == null) {
			return false;
		}
		
		IsNextStep edge = macroElement.getEdge(thisStep, endStep);

		return edge != null;
	}

	/**
	 * Gets bottom of the shape.
	 * @return
	 */
	public int getBottom() {

		return y + getHeight();
	}

	/**
	 * Sets default dimension.
	 */
	public void setDefaultDimesion() {

		this.width = initialWidth;
		this.height = initialHeight;
	}

	/**
	 * Set this shape as dummy.
	 */
	public void setDummy() {

		dummy  = true;
	}

	/**
	 * @return the dummy
	 */
	public boolean isDummy() {
		return dummy;
	}

	/**
	 * Gets left extended position.
	 * @return
	 */
	public double getLeftExtended() {

		return x - leftExtent;
	}

	/**
	 * Get right extended position.
	 * @return
	 */
	public double getRightExtended() {

		return x + width + rightExtent;
	}

	/**
	 * Gets list of steps.
	 * @param stepShapes
	 * @return
	 */
	public static ArrayList<Step> getSteps(ArrayList<StepShape> stepShapes) {

		ArrayList<Step> steps = new ArrayList<Step>();
		
		for (StepShape stepShape : stepShapes) {
			steps.add(stepShape.getStep());
		}
		return steps;
	}


	/**
	 * Get step shape.
	 */
	public static StepShape getStepShape(Step step) {

		Object user = step.getUser();
		if (user instanceof StepShape) {
			return (StepShape) user;
		}
		
		return null;
	}

	/**
	 * Sets level number.
	 * @param levelNumber
	 */
	public void setLevelNumber(int levelNumber) {

		this.levelNumber = levelNumber;
	}

	/**
	 * Gets level number.
	 * @return
	 */
	public int getLevelNumber() {

		return levelNumber;
	}

	/**
	 * Increments level.
	 * @param increment
	 */
	private void incrementLevels(int increment) {

		this.levelNumber += increment;
	}

	/**
	 * Increment step shapes levels.
	 * @param steps
	 * @param increment
	 */
	public static void incrementLevels(LinkedList<Step> steps, int increment) {

		// Do loop for existing step shapes.
		for (Step step : steps) {
			StepShape stepShape = getStepShape(step);
			if (stepShape != null) {
				
				stepShape.incrementLevels(increment);
			}
		}
	}

	/**
	 * Move step shape to the right and down.
	 * @param rightShift
	 * @param downShift
	 */
	public void move(int rightShift, int downShift) {

		x += rightShift;
		y += downShift;
	}

	/**
	 * Sets step.
	 * @param step
	 */
	public void setStep(Step step) {

		this.step = step;
	}

	/**
	 * Set dummy.
	 * @param dummy2
	 */
	public void setDummy(boolean dummy) {

		this.dummy = dummy;
	}

	/**
	 * Creates dummy shape.
	 * @return
	 */
	public Shape createDummyShape() {

		int x = getXcenter();
		
		return new Line2D.Double(x, y, x, getBottom());
	}

	/**
	 * Gets dummy shape surroundings.
	 * @return
	 */
	public Rectangle getDummySurroundings(int radius) {

		int x = getXcenter();
		
		return new Rectangle(x - radius, y, 2 * radius, getHeight());
	}

	/**
	 * @return the isTopAffected
	 */
	public boolean isTopAffected() {
		return isTopAffected;
	}
}
