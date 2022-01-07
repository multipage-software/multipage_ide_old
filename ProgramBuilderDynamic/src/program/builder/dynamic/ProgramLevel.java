/**
 * 
 */
package program.builder.dynamic;

import general.util.Resources;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.JOptionPane;

import program.basic.ProgramBasic;

import program.builder.*;
import program.generator.ColorId;
import program.generator.CustomizedColors;
import program.generator.ToolList;

import program.middle.*;
import program.middle.dynamic.*;

/**
 * @author
 *
 */
public class ProgramLevel {

	/**
	 * Level margins.
	 */
	public static final int margin = 60;

	/**
	 * Diagrams x space.
	 */
	static final int diagramsXspace = 200;

	/**
	 * Program.
	 */
	private Program program;
	
	/**
	 * Macro.
	 */
	private MacroElement macroElement;

	/**
	 * Macro element shape.
	 */
	private MacroElementShape macroElementShape = new MacroElementShape();
	
	/**
	 * Diagram elements.
	 */
	private StepsDiagram diagramElements;
	
	/**
	 * Diagram sub level.
	 */
	private ProgramLevel subLevel = null;

	/**
	 * Reference to panel.
	 */
	private ProgramDiagram panel;
	
	/**
	 * Macro - micro connection.
	 */
	private MacroMicroConnection macMicConnection = new MacroMicroConnection();

	/**
	 * Previous level.
	 */
	private ProgramLevel superLevel;

	/**
	 * Parent step ID.
	 */
	private long parentStepId = 0;

	/**
	 * Parent step shape.
	 */
	private StepShape parentStepShape = null;

	/**
	 * Saved translation.
	 */
	private int savedTranslationX;
	private int savedTranslationY;

	/**
	 * Saved zoom.
	 */
	private double savedZoom = 1.0;
	
	/**
	 * Constructor.
	 */
	public ProgramLevel(ProgramDiagram panel, Program program) {

		this.panel = panel;
		this.program = program;
		this.macroElement = program;

		load();
	}
	
	/**
	 * Constructor.
	 */
	public ProgramLevel(ProgramDiagram panel, ProgramLevel previousLevel,
			Program program, Procedure procedure, long parentStepId) {

		this.panel = panel;
		this.program = program;
		this.macroElement = procedure;
		
		this.superLevel = previousLevel;
		this.parentStepId = parentStepId;

		macroElementShape.setMacroElement(procedure);
		
		load();
	}

	/**
	 * Returns true if the cursor is on the right arrow.
	 */
	public boolean isOnRightArrow(Point2D transformedMouse) {
	
		if (subLevel != null) {
			if (subLevel.isOnRightArrow(transformedMouse)) {
				return true;
			}
		}
		return getSelectedRightArrow(transformedMouse) != null;
	}

	/**
	 * Get selected right arrow.
	 */
	public StepShape getSelectedRightArrow(Point2D transformedMouse) {

		// Do loop for all steps shapes.
		for (StepShape stepShape : diagramElements.getStepShapes()) {
			if (stepShape.isRightArrow(transformedMouse)) {
				return stepShape;
			}
		}

		return null;
	}
	
	/**
	 * Returns true if the point is on the close button.
	 */
	public boolean isOnCloseButton(Point2D transformedMouse) {
		
		if (subLevel != null) {
			if (subLevel.isOnCloseButton(transformedMouse)) {
				return true;
			}
		}
		return isOnLevelCloseButton(transformedMouse);
	}

	/**
	 * Gets true if mouse is on close button.
	 */
	private boolean isOnLevelCloseButton(Point2D transformedMouse) {

		if (macroElement instanceof Procedure) {
			return macroElementShape.isOnCloseButton(transformedMouse);
		}
		return false;
	}

	/**
	 * Draw macroElement level.
	 */
	public void draw(Graphics2D g2) {

		// Draw macro element shape.
		macroElementShape.draw(g2);
		
		// Get opened step shape.
		StepShape openedStepShape = null;
		if (subLevel != null) {
			openedStepShape = subLevel.getParentStepShape();
		}
		// Draw diagram.
		diagramElements.draw(g2, openedStepShape);
		
		// Draw macro - micro connection.
		if (parentStepShape != null) {
			macMicConnection.draw(g2);
		}

		// If micro system visible, draw it.
		if (subLevel != null) {
			subLevel.draw(g2);
		}
	}

	/**
	 * Load macroElement data.
	 */
	private void load() {

		Properties properties = ProgramBasic.getLoginProperties();
		MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
		MiddleResult result;
		boolean isFirstLevel = macroElement instanceof Program;
		
		// Get parent step shape.
		if (!isFirstLevel) {
			parentStepShape = superLevel.diagramElements.getStepShape(parentStepId);
			if (parentStepShape == null) {
				JOptionPane.showMessageDialog(panel,
						Resources.getString("messageCannotGetPreviousLevelOpenedStepShape"));
				return;
			}
		}

		// Clear macro element.
		macroElement.removeAll();
		
		// Load macro element description.
		result = middle.loadMacroElementDescription(properties, macroElement);
		if (result.isNotOK()) {
			// Inform user and exit the method.
			result.show(panel);
			return;
		}
		
		// Load macro element steps.
		result = middle.loadMacroElementLevel(properties, macroElement);
		if (result != MiddleResult.OK) {
			// Inform user.
			result.show(panel);
			return;
		}
		
		// Create diagram elements.
		diagramElements = new StepsDiagram(macroElement, true);
		
		// Render diagram elements.
		diagramElements.render();

		// Render level macro element.
		// Get diagram rectangle.
		Rectangle diagramRect = diagramElements.getLevelStepsRect();
		if (diagramRect == null) {
			int diagramHeight = StepShape.initialHeight + 2 * StepsDiagram.spaceY;
			
			diagramRect = new Rectangle(0, 0, StepShape.initialWidth,
					diagramHeight);
		}
		
		// Color the inactive steps.
		diagramElements.colorInactive();
		
		// If it is the first level...
		if (isFirstLevel) {
			
			// Set macro element dimension.
			macroElementShape.setDimension(diagramRect.width,
					diagramRect.height);
			
			// Set left shift.
			int rightShift = - macroElementShape.getWidth() / 2;
			int downShift = macroElementShape.getLabelHeight();
			
			macroElementShape.setMacroElement(macroElement);
			macroElementShape.setLeft(rightShift);
			
			// Move diagram steps and edges.
			diagramElements.move(rightShift, downShift);
		}
		// If it is a sub level...
		else {
			
			// Set macro element dimension.
			macroElementShape.setDimension(diagramRect.width + 2 * margin,
					diagramRect.height);
			
			int parentShapeCenter = parentStepShape.getYcenter();
			int macroLevelHeight = macroElementShape.getHeight();
			int macroLevelLeft = superLevel.getLevelRight();
			int macroLevelTop = parentShapeCenter - macroLevelHeight / 2;
			
			// Set top minimum.
			if (macroLevelTop < 0) {
				macroLevelTop = 0;
			}
			
			// Set macro shape position.
			macroElementShape.setLeft(macroLevelLeft);
			macroElementShape.setTop(macroLevelTop);
			
			// Move diagram steps and edges.
			diagramElements.move(macroLevelLeft + margin,
					macroLevelTop + macroElementShape.getLabelHeight());

			// Set macro - micro connection.
			macMicConnection.setTrapeze(parentStepShape,
					macroElementShape.getRectangle());

		}
	}

	/**
	 * Load program levels.
	 */
	public void loadLevels() {

		load();
		
		if (subLevel != null) {
			subLevel.loadLevels();
		}
	}
	
	/**
	 * Gets level width.
	 */
	public int getLevelWidth() {
		return macroElementShape.getWidth();
	}

	/**
	 * Gets level height
	 * @return
	 */
	public int getLevelTitleHeight() {
		return macroElementShape.getLabelHeight();
	}

	/**
	 * Get level rectangle.
	 */
	public Rectangle getLevelRect() {
		
		return macroElementShape.getRectangle();
	}

	/**
	 * On mouse pressed.
	 */
	public boolean onMousePressed(Point2D point2d) {
		
		// If sub level exists, delegate event to it.
		if (subLevel != null) {
			
			// If close button is affected...
			if (subLevel.isOnLevelCloseButton(point2d)) {
				
				// Close sub level.
				subLevel.close();
				subLevel = null;
				
				// Save new diagram rectangle.
				panel.setOverview();
				
				return true;
			}
			if (subLevel.onMousePressed(point2d)) {
				return true;
			}
		}
		
		// If sub button is affected...
		StepShape stepShape = getSelectedRightArrow(point2d);
		if (stepShape != null) {
			
			// Open new diagram level.
			openSubProcedures(stepShape);
			
			// Save new diagram rectangle.
			panel.setOverview();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Open sub procedures.
	 */
	private void openSubProcedures(StepShape stepShape) {
		
		Step step = stepShape.getStep();
		long procedureId = step.getProcedureId();
		Procedure procedure = new Procedure(0L, procedureId, step.getDescription(), false);
		boolean openSubLevel;
		
		// If the sub level already exists, close it else create a new sub diagram.
		if (subLevel != null) {
			
			StepShape openedStepShape = subLevel.getParentStepShape();
			openSubLevel = stepShape != openedStepShape;
			
			subLevel.close(!openSubLevel);
			subLevel = null;
		}
		else {
			openSubLevel = true;
		}
		
		if (openSubLevel) {
			
			// Get step ID.
			long stepId = step.getId();
			
			// Create sub level.
			subLevel = new ProgramLevel(
								panel,
								this,
								program,
								procedure,
								stepId);
			
			// Save current translation and zoom.
			subLevel.saveCurrentTranslationAndZoom();
			
			// Animate translation.
			Rectangle subLevelRect = subLevel.getLevelRect();
			int windowWidth = panel.getDiagramWidth();
			int windowHeight = panel.getDiagramHeight();
			double zoom = panel.getZoom();
			
			double focusX, focusY = 0;

			// If the sub level width is less than window width...
			if (subLevelRect.getWidth() * zoom <= windowWidth) {
				focusX = ToolList.getWidth() - zoom * subLevelRect.getCenterX()
								+ windowWidth / 2;
			}
			else {
				focusX = ToolList.getWidth() - zoom * subLevelRect.getX();
			}
			
			// If the top or bottom of the sub level is not visible...
			Rectangle2D transformedSubLevel = panel.undoTransformation(subLevelRect);
			boolean isInside = transformedSubLevel.getY() >= 0
								&& transformedSubLevel.getMaxY() <= windowHeight;
			if (isInside) {
				focusY = panel.getTranslationY();
			}
			else {
				focusY = - subLevelRect.getY() * zoom;
			}
			
			// Animate translation.
			panel.animateTranslationAndScaleAbsolute(focusX, focusY, zoom);
		}
	}

	/**
	 * Gets level right.
	 * @return
	 */
	private int getLevelRight() {
		
		if (macroElement instanceof Program) {
			return macroElementShape.getWidth() / 2 + diagramsXspace;
		}
		else {
			return macroElementShape.getRight() + diagramsXspace;
		}
	}

	/**
	 * Gets parent step shape.
	 */
	private StepShape getParentStepShape() {

		return parentStepShape;
	}

	/**
	 * Close program level.
	 */
	public void close() {
		
		close(true);
	}
	
	/**
	 * Close program level.
	 * @param animate 
	 */
	public void close(boolean animate) {

		if (macroElement instanceof Procedure) {
			
			// Animate translation.
			panel.animateTranslationAndScaleRelative(
					getSavedTranslationX() - panel.getTranslationX(),
					getSavedTranslationY() - panel.getTranslationY(),
					getSavedZoom() / panel.getZoom(),
					animate, true);
		}

		// Close sub level.
		if (subLevel != null) {
			
			subLevel.close();
			subLevel = null;
		}
		
		// Get model.
		AreasModelDynamic model = (AreasModelDynamic) ProgramBuilder.getAreasModel();
		
		// Remove all unused steps and edges.
		model.removeStepsAndEdges(program, macroElement);
		
		diagramElements.clear();
	}

	/**
	 * Gets saved zoom.
	 * @return
	 */
	private double getSavedZoom() {

		return savedZoom;
	}

	/**
	 * Saves current translation.
	 */
	public void saveCurrentTranslationAndZoom() {

		savedTranslationX = (int) (panel.getTranslationX() + panel.getAnimateVectorX());
		savedTranslationY = (int) (panel.getTranslationY() + panel.getAnimateVectorY());
		savedZoom = panel.getZoom();
	}

	/**
	 * @return the savedTranslationX
	 */
	public int getSavedTranslationX() {
		return savedTranslationX;
	}

	/**
	 * @return the savedTranslationY
	 */
	public int getSavedTranslationY() {
		return savedTranslationY;
	}

	/**
	 * Gets macro element.
	 * @param rectangle 
	 * @param transformedpanelMouse
	 * @return
	 */
	public MacroElement getMacroElement(Point2D transformedpanelMouse, Rectangle rectangle) {

		if (macroElementShape.labelContains(transformedpanelMouse)) {
			
			rectangle.setLocation(macroElementShape.getLeft(),
					macroElementShape.getTop());
			rectangle.setSize(macroElementShape.getWidth(),
					macroElementShape.getLabelHeight());
			
			return macroElement;
		}
		
		if (subLevel != null) {
			return subLevel.getMacroElement(transformedpanelMouse, rectangle);
		}
		
		return null;
	}

	/**
	 * Gets step.
	 * @param transformedpanelMouse
	 * @param rectangle
	 * @return
	 */
	public Step getStep(Point2D transformedpanelMouse, Rectangle rectangle) {

		// Do loop for all step shapes.
		for (StepShape stepShape : diagramElements.getStepShapes()) {
			if (stepShape.labelContains(transformedpanelMouse)) {
				
				rectangle.setLocation(stepShape.getX(),
						stepShape.getBodyYstart());
				rectangle.setSize(stepShape.getWidth(),
						StepShape.getDescriptionHeight());
				
				return stepShape.getStep();
			}
		}
		
		if (subLevel != null) {
			return subLevel.getStep(transformedpanelMouse, rectangle);
		}
		
		return null;
	}

	/**
	 * Gets sub level.
	 * @return
	 */
	public ProgramLevel getSublevel() {

		return subLevel;
	}

	/**
	 * Gets affected element.
	 * @param transformesMouse
	 * @param caller 
	 * @return
	 */
	public AffectedDynamic getAffectedElement(Point2D transformesMouse, int caller) {

		// If the macro element is affected return it.
		if (macroElementShape.labelContains(transformesMouse)) {
			return macroElementShape;
		}
		
		// If the diagram element is affected return it.
		AffectedDynamic affectedShape = diagramElements.getAffectedShape(transformesMouse, caller);
		if (affectedShape != null) {
			return affectedShape;
		}
		
		// Check if a sub level element is affected.
		if (subLevel != null) {
			return subLevel.getAffectedElement(transformesMouse, caller);
		}
		
		return null;
	}

	/**
	 * Close step.
	 * @param step
	 */
	public void closeStep(Step step) {
		
		// If the sub level doesn't exist exit the method.
		if (subLevel == null) {
			return;
		}
		
		// Get sub level macro element.
		MacroElement macroElement = subLevel.macroElement;
		
		// If the macro element is not a procedure exit the method.
		if (!(macroElement instanceof Procedure)) {
			return;
		}
		
		// Get procedure id.
		Procedure procedure = (Procedure) macroElement;
		long procedureId = procedure.getId();
		
		// If the procedure ID matches the step procedure ID
		// close the level.
		if (procedureId == step.getProcedureId()) {
			subLevel.close();
			subLevel = null;
			return;
		}
		
		// Call the method recursively.
		subLevel.closeStep(step);
	}

	/**
	 * Close sub level
	 * @param macroElemenToCloseSublevel
	 */
	public void closeSubLevel(MacroElement macroElemenToCloseSublevel) {

		if (macroElement == macroElemenToCloseSublevel) {
			if (subLevel != null) {
				subLevel.close();
				subLevel = null;
			}
			return;
		}
		
		// Try to close sub level.
		if (subLevel != null) {
			subLevel.closeSubLevel(macroElemenToCloseSublevel);
		}
	}
}

/**
 * 
 * @author
 *
 */
class MacroMicroConnection {
	
	/**
	 * Trapeze.
	 */
	private Polygon trapeze = new Polygon();
	
	/**
	 * Micro system rectangle.
	 */
	private Rectangle microRect = new Rectangle();

	/**
	 * Set trapeze.
	 */
	public void setTrapeze(StepShape parentStepShape, Rectangle levelRect) {

		// Set micro system rectangle.
		microRect.setBounds(levelRect);
		
		int ax1 = 0;
		int ay1 = 0;
		int ay2 = 0;
		int bx1 = 0;
		int by1 = 0;
		int by2 = 0;
		
		// Set coordinates.
		ax1 = parentStepShape.getX() + parentStepShape.getWidth();
		ay1 = parentStepShape.getBodyYstart();
		ay2 = parentStepShape.getY() + parentStepShape.getHeight();
		
		bx1 = (int) levelRect.getX();
		by1 = (int) levelRect.getY();
		by2 = by1 + (int) levelRect.getHeight();
		
		// Set trapeze.
		trapeze.reset();
		trapeze.addPoint(ax1, ay1);
		trapeze.addPoint(bx1, by1);
		trapeze.addPoint(bx1, by2);
		trapeze.addPoint(ax1, ay2);
		trapeze.addPoint(ax1, ay1);
	}

	/**
	 * Draw trapeze.
	 */
	public void draw(Graphics2D g2) {

		// Draw trapeze.
		g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
		g2.fillPolygon(trapeze);
		
		// Draw microsystem rectangle.
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g2.drawRect(microRect.x, microRect.y, microRect.width, microRect.height);
	}
}
