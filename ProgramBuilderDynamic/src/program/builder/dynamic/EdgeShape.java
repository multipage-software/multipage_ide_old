/**
 * 
 */
package program.builder.dynamic;

import general.gui.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

import program.generator.Affected;
import program.generator.ColorId;
import program.generator.CustomizedColors;
import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class EdgeShape implements AffectedDynamic {

	/**
	 * Edge types.
	 */
	public static final int NORMAL = 0;
	public static final int START = 1;
	public static final int END = 2;
	
	/**
	 * Surrounding radius.
	 */
	private static final int radius = 40;

	/**
	 * Edge reference.
	 */
	private IsNextStep edge;
	
	/**
	 * Is start edge flag.
	 */
	private int edgeType = NORMAL;
	
	/**
	 * Dummy step shapes.
	 */
	private LinkedList<StepShape> dummyStepShapes
									= new LinkedList<StepShape>();
	
	/**
	 * Rendered shapes.
	 */
	private LinkedList<Shape> renderedShapes;
	
	/**
	 * Shape surroundings.
	 */
	private LinkedList<Shape> shapeSurroundings;
	
	/**
	 * Is true if the edge is reversed when this shape object was
	 * created.
	 */
	private boolean isReversed;
	
	/**
	 * Macro element reference.
	 */
	private MacroElement macroElement;
	
	/**
	 * Constructor.
	 * @param edge
	 * @param edgeType 
	 * @param macroElement 
	 */
	public EdgeShape(IsNextStep edge, int edgeType, MacroElement macroElement) {

		this.edge = edge;
		this.isReversed = edge.isReversed();
		this.edgeType = edgeType;
		this.macroElement = macroElement;
	}

	/**
	 * Adds new dummy step shape.
	 * @param dummyStepShape
	 */
	public void addDummyStep(StepShape dummyStepShape) {

		if (!isReversed) {
			this.dummyStepShapes.addLast(dummyStepShape);
		}
		else {
			this.dummyStepShapes.addFirst(dummyStepShape);
		}
	}

	/**
	 * Renders the edge shape.
	 */
	private void render(MacroElementShape macroElementShape) {
		
		// If the macro element shape is null, exit the method.
		if (macroElementShape == null) {
			return;
		}
		
		int dummyStepIndex = 0;
		int numberDummySteps = dummyStepShapes.size();
		
		// Create shapes list.
		renderedShapes = new LinkedList<Shape>();
		shapeSurroundings = new LinkedList<Shape>();
		
		boolean lastLoop = false;
		boolean drawStart = edgeType == START;
		boolean drawEnd = edgeType == END;
		
		// Get start.
		Step startStep = !edge.isReversed() ? edge.getStep() : edge.getNext();
		Step finalStep = !edge.isReversed() ? edge.getNext() : edge.getStep();
		StepShape startStepShape = StepShape.getStepShape(startStep);
		StepShape finalStepShape = StepShape.getStepShape(finalStep);
		StepShape endStepShape;
		
		boolean isForward = startStepShape.getLevelNumber()
							> finalStepShape.getLevelNumber();
							
		do {
			
			// Get end step.
			if (dummyStepIndex < numberDummySteps) {
				StepShape dummyStepShape = dummyStepShapes.get(dummyStepIndex);
				
				// Draw the dummy step.
				renderedShapes.add(dummyStepShape.createDummyShape());
				shapeSurroundings.add(dummyStepShape.getDummySurroundings(radius));
				endStepShape = dummyStepShape;
			}
			else {
				endStepShape = finalStepShape;
				lastLoop = true;
			}
			
			if (startStepShape != null && endStepShape != null) {
				
				double endX = endStepShape.getX() + endStepShape.getWidth() / 2;
				double endTopY = endStepShape.getY();
				
				// If it is a start edge, add starting line.
				if (drawStart) {
					
					int macroBottom = macroElementShape.getLabelBottom();
					renderedShapes.addAll(GraphUtility.createArrowShape((int) endX,
							(int) macroBottom, (int) endX, (int) endTopY,
							ProgramDiagram.arrowAlpha, ProgramDiagram.arrowLength));
					
					shapeSurroundings.add(new Rectangle((int) (endX - radius), macroBottom,
							2 * radius, (int) endTopY - macroBottom));
					
					// Reset the flag.
					drawStart = false;
				}
				else {
					
					double startX = startStepShape.getX() + startStepShape.getWidth() / 2;
					double startTopY = startStepShape.getY();
					double startBottomY = startStepShape.getY() + startStepShape.getHeight();
					double endBottomY = endStepShape.getY() + endStepShape.getHeight();
					
					// If it is an end step and the last part, add end line.
					if (drawEnd && lastLoop) {
						
						int macroEndTop = macroElementShape.getEndLabelTop();
						renderedShapes.addAll(GraphUtility.createArrowShape((int) startX,
								(int) startBottomY, (int) startX, (int) macroEndTop,
								ProgramDiagram.arrowAlpha, ProgramDiagram.arrowLength));
						
						shapeSurroundings.add(new Rectangle((int) (startX - radius),
								(int) startBottomY, 2 * radius, macroEndTop -  (int) startBottomY));
					}
					// Else draw normal line.
					else {
					
						double x1, y1, x2, y2;
						
						// If it is a forward edge...
						if (isForward) {
							// Line coordinates.
							x1 = startX;
							y1 = startBottomY;
							x2 = endX;
							y2 = endTopY;
						}
						// else it is a backward edge...
						else {
							
							boolean pointsLeft = endX < startX;
							boolean isVertical = endX == startX;
							
							y1 = startTopY;
							y2 = endBottomY;
							
							// If the start is a dummy step...
							if (startStepShape.isDummy()) {
								
								x1 = startX;
							}
							// ... else it is a normal step.
							else {
								// If the edge points to the left...
								if (pointsLeft) {
									
									x1 = startStepShape.getLeftExtended();
								}
								// ... else if it points to the right.
								else {
									
									x1 = startStepShape.getRightExtended();
								}
							}
							
							// If the end is a dummy step...
							if (endStepShape.isDummy()) {
								
								x2 = endX;
							}
							// ... else it is a normal step.
							else {
								// If the edge points to the left...
								if (pointsLeft || isVertical) {
									
									x2 = endStepShape.getRightExtended();
								}
								// ... else it points to the right.
								else {
									
									x2 = endStepShape.getLeftExtended();
								}
							}
						}
						
						// Create and add arrow shape.
						renderedShapes.addAll(GraphUtility.createArrowShape(
								(int) x1, (int) y1, (int) x2, (int) y2,
								ProgramDiagram.arrowAlpha, ProgramDiagram.arrowLength));
						
						shapeSurroundings.addAll(GraphUtility.createLineSurrounding(
								(int) x1, (int) y1, (int) x2, (int) y2, radius));
					}
				}
			}
			
			// Set new start and increment the index.
			startStepShape = endStepShape;
			dummyStepIndex++;
		}
		while (!lastLoop);
	}
	
	/**
	 * Draws edge shape.
	 * @param g2
	 */
	public void drawEdge(Graphics2D g2) {
		
		// Set edge color.
		g2.setColor(CustomizedColors.get(edge.getStep().isInactive() ?
				ColorId.INACTIVE_OUTLINES :
				(isReversed ? ColorId.REVERSEDEDGES : ColorId.OUTLINES_PROTECTED )));
		
		// Draw shapes.
		for (Shape shape : renderedShapes) {
			
			g2.draw(shape);
		}
	}

	/**
	 * Render edge shapes.
	 * @param edgeShapes
	 * @param macroElementShape
	 */
	public static void render(LinkedList<EdgeShape> edgeShapes,
			MacroElementShape macroElementShape) {

		// Render all list items.
		for (EdgeShape edgeShape : edgeShapes) {

			edgeShape.render(macroElementShape);
		}
	}

	/**
	 * Gets edge.
	 * @return
	 */
	public IsNextStep getEdge() {

		return edge;
	}

	/**
	 * Draw affected shapes.
	 * @param g2
	 * @param zoom
	 */
	@Override
	public void drawAffected(Graphics2D g2, double zoom) {
		
		int lineWidth = ProgramDiagram.affectedLineWidth;
		
		// Get old stroke.
		Stroke oldStroke = g2.getStroke();
		// Set opacity.
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f / lineWidth));
		
		// Set edge color.
		g2.setColor(CustomizedColors.get(edge.getStep().isInactive() ?
				ColorId.INACTIVE_OUTLINES :
				(isReversed ? ColorId.REVERSEDEDGES : ColorId.OUTLINES_PROTECTED )));
		
		// Draw edges.
		for (int width = 1; width < lineWidth; width++) {
			
			g2.setStroke(new BasicStroke((float) (width / zoom)));
			drawEdge(g2);
		}
		
		// Set old stroke and composite.
		g2.setStroke(oldStroke);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}

	/**
	 * Not implemented.
	 */
	@Override
	public Point getLabelCenter() {
		
		return null;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public MacroElement getMacroElement() {

		return macroElement;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public boolean isNext(Affected endElement) {

		return false;
	}

	/**
	 * Returns true if the shape contains the point.
	 * @param transformesMouse
	 * @return
	 */
	public boolean contains(Point2D transformesMouse) {

		// Do loop for all shape surroundings.
		for (Shape surrounding : shapeSurroundings) {
			
			if (surrounding.contains(transformesMouse)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @return the edgeType
	 */
	public int getEdgeType() {
		return edgeType;
	}
}
