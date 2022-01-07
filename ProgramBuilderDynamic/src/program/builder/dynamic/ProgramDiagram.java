/**
 * 
 */
package program.builder.dynamic;

import general.gui.*;
import general.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import program.basic.*;
import program.generator.Affected;
import program.generator.GeneratorMainFrame;
import program.generator.ColorId;
import program.generator.CustomizedColors;
import program.generator.GeneralDiagram;
import program.generator.RemoveElementShape;
import program.generator.TabContainerComponent;
import program.generator.Tool;
import program.generator.ToolId;
import program.generator.ToolList;
import program.middle.*;
import program.middle.dynamic.IsNextStep;
import program.middle.dynamic.MacroElement;
import program.middle.dynamic.MiddleDynamic;
import program.middle.dynamic.Procedure;
import program.middle.dynamic.Program;
import program.middle.dynamic.Step;
import program.middle.dynamic.StepsGraph;


/**
 * @author
 * 
 */
public class ProgramDiagram extends GeneralDiagram implements TabContainerComponent {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Diagram Y space.
	 */
	private static final int diagramSpaceY = 20;
	
	/**
	 * Affected line width. 
	 */
	public static final int affectedLineWidth = 13;
	
	/**
	 * Affected intensity.
	 */
	public static final float affectedIntensity = 1.0F;

	/**
	 * Diagram margins.
	 */
	private static final double diagramMarginsPercent = 10.0;
	private static final double diagramMarginsMultiplier = (100.0 - diagramMarginsPercent) / 100.0;

	/**
	 * Connector circle.
	 */
	private static final int connectorCircle = 12;
	
	/**
	 * Connector width.
	 */
	private static final int connectorWidth = 5;

	/**
	 * Arrow parameters.
	 */
	public static final double arrowAlpha = Math.PI / 4.0;
	public static final double arrowLength = 24.0;

	/**
	 * Affect element constants.
	 */
	public static final int AFF_NEWSTEP = 0;
	public static final int AFF_REMOVESTEP = 1;
	public static final int AFF_CONNECTOR = 2;
	
	/**
	 * Program level.
	 */
	private ProgramLevel programLevel;

	/**
	 * Program.
	 */
	private Program program;
	
	/**
	 * Shown for first animationTime flag.
	 */
	private boolean firstTimeShown = true;

	/**
	 * Old element.
	 */
	private Object oldElement;

	/**
	 * Content rectangle.
	 */
	private Rectangle contentRectangle = new Rectangle();

	/**
	 * New step shape.
	 */
	private StepShape newStepShape;
	
	/**
	 * Affected elements.
	 */
	private LinkedList<AffectedDynamic> affectedElements = new LinkedList<AffectedDynamic>();
	
	/**
	 * Previous element.
	 */
	private AffectedDynamic affectedBegin;

	/**
	 * Connector visibility flag.
	 */
	private boolean connectorVisible = false;
	
	/**
	 * Constructor.
	 */
	public ProgramDiagram(Program program) {

		this.program = program;
		
		// Create first level of the diagram.
		programLevel = new ProgramLevel(this, program);

		// Cursor tool.
		toolList.add(new Tool(
				toolList, 0, ToolId.CURSOR, 
				Resources.getString("tooltipSelection"), 
				"program/generator/images/cursor.png",
				Resources.getString("textProgramCursorToolDescription")));
		// Move tool.
		toolList.add(new Tool(
				toolList, 1, ToolId.MOVE, 
				Resources.getString("tooltipMove"), 
				"program/generator/images/move.png",
				Resources.getString("textMoveToolDescription")));
		// Zoom tool.
		toolList.add(new Tool(
				toolList, 2, ToolId.ZOOM,
				Resources.getString("tooltipZoom"),
				"program/generator/images/zoom.png",
				Resources.getString("textZoomToolDescritpion")));
		// Step tool.
		toolList.add(new Tool(
				toolList, 3, ToolId.STEP, 
				Resources.getString("tooltipStep"), 
				"program/generator/images/step.png",
				Resources.getString("textStepToolDescription")));
		// Remove tool.
		toolList.add(new Tool(
				toolList, 4, ToolId.REMOVE,
				Resources.getString("tooltipRemoveStep"),
				"program/generator/images/remove_element.png",
				Resources.getString("textRemoveStepToolDescription")));
		// Connector tool.
		toolList.add(new Tool(
				toolList, 5, ToolId.CONNECTOR,
				Resources.getString("tooltipNewConnector"),
				"program/generator/images/next_step.png",
				Resources.getString("textConnectorToolDescription")));
		
		toolList.selectTool(0);
		
		// Set listeners.
		addComponentListener(new ComponentAdapter() {
			// On show panel.
			@Override
			public void componentShown(ComponentEvent e) {
				
				onShow();
			}
		});
	}
	
	/**
	 * Add affected element.
	 */
	public void addAffectedElement(AffectedDynamic affected) {
		
		// Add only a new element.
		if (!affectedElements.contains(affected)) {
			affectedElements.add(affected);
		}
	}

	/**
	 * On mouse exited.
	 */
	@Override
	protected void onMouseExited() {

		// If the new step tool is selected.
		if (toolList.getSelected() == ToolId.STEP) {
			setNewStepVisibility(false);
			resetAffectedElements();
		}
		// If the connector tool is selected, hide connector.
		else if (toolList.getSelected() == ToolId.CONNECTOR) {
			connectorVisible = false;
		}
		
		resetAffectedElements();
	}

	/**
	 * On mouse entered.
	 */
	@Override
	protected void onMouseEntered() {

		// If the new step tool is selected.
		if (toolList.getSelected() == ToolId.STEP) {
			setNewStepVisibility(true);
		}
		// If the connector tool is selected, show connector.
		else if (toolList.getSelected() == ToolId.CONNECTOR) {
			connectorVisible = true;
		}
	}

	protected void onShow() {

		// If it is shown first time.
		if (firstTimeShown) {

			initializeTranslation();
			// Set tool bar and overview window.
			setOverview();
			setScrollBarsLocation();
			setScrollBars();
			// Set zoom.
			setZoom();
			// Set zoom shape.
			zoomShape.setMinimal(minimumZoom);
			zoomShape.setPosition(getWidth() - ZoomShape.getWidth(), 0);

			// Set listener.
			zoomShape.setListener(new ZoomListener() {
				@Override
				protected void zoomChanged() {
					
					// Set delta.
					int width = getWidth() - ToolList.getWidth() - ZoomShape.getWidth(),
						x = ToolList.getWidth() + width / 2,
						y = getHeight() / 2;
					
					// Zoom.
					zoomDiagram(zoomShape.getZoom(), x, y);
					
					setScrollBars();
					repaint();
				}
			});
			
			firstTimeShown = false;
			
			// Center diagram.
			center();
			
			// Set diagram loaded flag.
			setLoaded();
		}
	}

	/**
	 * On horizontal scroll.
	 * @param win
	 */
	@Override
	protected void onHorizontalScroll(Rectangle2D win) {

		Rectangle window = getRectangle();

		translationx += window.getMinX() - win.getMinX();
	}

	/**
	 * On vertical scroll.
	 * @param win
	 */
	@Override
	protected void onVerticalScroll(Rectangle2D win) {
		
		Rectangle window = getRectangle();
		
		translationy += window.getMinY() - win.getMinY();
	}

	/**
	 * On mouse moved.
	 * 
	 * @param oldMouse
	 * @param point
	 */
	protected void onMouseMoved(Point mouse, boolean onDiagramElement) {

		if (!onDiagramElement) {
			
			// If the cursor tool is selected.
			if (toolList.getSelected() == ToolId.CURSOR) {
				
				Point2D transformedMouse = doTransformation(mouse);
				
				// If mouse is on right arrow or close button, set hand cursor.
				if (programLevel.isOnRightArrow(transformedMouse)
					|| programLevel.isOnCloseButton(transformedMouse)) {
					
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				else {
					setCursor(Cursor.getDefaultCursor());
				}
			}
			// If the add new step tool is selected.
			else if (toolList.getSelected() == ToolId.STEP) {
				onMoveNewStep(mouse);
			}
			// If the remove tool is selected.
			else if (toolList.getSelected() == ToolId.REMOVE) {
				onMoveRemoveStep(mouse);
			}
			// If the connector tool is selected.
			else if (toolList.getSelected() == ToolId.CONNECTOR) {
				onMoveConnector(mouse);
			}
		}
		else {
			// Hide new step.
			setNewStepVisibility(false);
			// Hide connector.
			connectorVisible = false;
			
			resetAffectedElements();
		}
		repaint();
	}

	/**
	 * On remove step move.
	 * @param mouse
	 */
	private void onMoveRemoveStep(Point mouse) {

		// Affect diagram element.
		affectElement(mouse, AFF_REMOVESTEP);
	}

	/**
	 * Set new step visibility.
	 * @param visible
	 */
	private void setNewStepVisibility(boolean visible) {
		
		// SHow / hide the new step shape.
		if (newStepShape != null) {
			newStepShape.setVisible(visible);
		}
	}

	/**
	 * On move new step.
	 * @param mouse 
	 */
	private void onMoveNewStep(Point mouse) {

		// Set position of the new step shape.
		if (newStepShape != null) {
			
			affectElement(mouse, AFF_NEWSTEP);
			newStepShape.setLocation(mouse);
			newStepShape.setVisible(true);
		}
	}

	/**
	 * Affect element.
	 * @param mouse
	 * @param flag 
	 */
	private void affectElement(Point mouse, int caller) {
		
		Point2D transformedMouse = doTransformation(mouse);

		// Get affected element.
		AffectedDynamic element = programLevel.getAffectedElement(transformedMouse,
				caller);
		
		// Add affected element to the list.
		if (element != null) {
			setAffectedElement(element);
		}
		// Remove affected elements.
		else {
			resetAffectedElements();
		}
	}

	/**
	 * Set one affected element.
	 * @param element
	 */
	private void setAffectedElement(AffectedDynamic element) {

		affectedElements.clear();
		affectedElements.add(element);
	}

	/**
	 * Reset affected elements.
	 */
	private void resetAffectedElements() {

		affectedElements.clear();
	}

	/**
	 * On mouse pressed.
	 * 
	 * @param e
	 */
	protected void onMousePressed(MouseEvent e) {

		boolean flagSetOverview = false;
		Point mouse = e.getPoint();
		
		// If cursor tool selected.
		if (toolList.getSelected() == ToolId.CURSOR) {
			programLevel.onMousePressed(doTransformation(mouse));
			// Set scroll bars.
			setZoom();
			setScrollBarsLocation();
			setScrollBars();
		}
		// If the new step tool is selected.
		else if (toolList.getSelected() == ToolId.STEP) {
			
			onNewStep();
		}
		// If the remove step tool is selected.
		else if (toolList.getSelected() == ToolId.REMOVE) {
			
			onRemove();
		}
		// If connect tool is selected...
		else if (toolList.getSelected() == ToolId.CONNECTOR) {
			
			onConnector();
		}

		if (flagSetOverview) {
			// Set overview.
			overview.setTranslation(getTranslatingX(), getTranslatingY());
			repaint();
		}
	}

	/**
	 * On tool selection.
	 */
	@Override
	protected void onSelectTool(ToolId toolId, ToolId oldSelection) {

		// If the new step tool is selected...
		if (toolId == ToolId.STEP) {
			
			newStepShape = new StepShape(new Step(0, 0, "#",
					Resources.getString("textNewStep"),
						false, null, false),
					0, 0, 100, 100);
			
			newStepShape.setVisible(true);
		}
		// If remove area tool selected.
		else if (toolId == ToolId.REMOVE) {
			
			removeElementShape = new RemoveElementShape();
		}
	}
	
	/**
	 * On remove.
	 */
	private void onRemove() {

		// Get affected element.
		Affected element = getFirstAffected();
		if (element == null) {
			return;
		}
		
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
		MiddleResult result;
		boolean reloadDiagram = false;
		
		// If the affected element is a step shape...
		if (element instanceof StepShape) {
			
			StepShape stepShape = (StepShape) element;
			Step step = stepShape.getStep();
			LinkedList<Step> steps = new LinkedList<Step>();
			steps.add(step);

			// Ask user.
			if (!ConfirmStepDeletion.showConfirmDialog(frame, steps)) {
				return;
			}
			
			// Close possibly opened sub levels.
			programLevel.closeStep(step);

			// Remove the step from the database.
			result = middle.removeStep(login, step);
			if (result.isNotOK()) {
				result.show(this);
				return;
			}
			
			// Set reload flag.
			reloadDiagram = true;
		}
		// If the affected element is a macro element shape.
		else if (element instanceof MacroElementShape) {
			MacroElementShape shape = (MacroElementShape) element;
			MacroElement macroElement = shape.getMacroElement();
			
			// Ask user.
			if (!ConfirmMacroContentDeletion.showConfirmDialog(frame, macroElement)) {
				return;
			}
			
			// Try to delete macro element content.
			result = middle.removeMacroElementContent(login, macroElement);
			if (result.isNotOK()) {
				result.show(this);
				return;
			}
			
			// Close sub level.
			programLevel.closeSubLevel(macroElement);
			// Set reload flag.
			reloadDiagram = true;
		}
		// If the affected element is an edge.
		else if (element instanceof EdgeShape) {
			EdgeShape shape = (EdgeShape) element;
			IsNextStep edge = shape.getEdge();
			MacroElement macroElement = edge.getMacroElement();
			int edgeType = shape.getEdgeType();
			
			if (edgeType == EdgeShape.END) {
				// Inform use.
				JOptionPane.showMessageDialog(this,
						Resources.getString("messageCannotRemoveStopEdge"));
			}
			else if (macroElement != null) {

				// Try to remove the edge.
				if (edgeType == EdgeShape.NORMAL) {
					result = middle.removeNextStepEdge(login, edge);
				}
				else if (edgeType == EdgeShape.START) {
					result = middle.updateStepIsStart(login, edge.getNext(), false);
				}
				else {
					result = MiddleResult.UNKNOWN_EDGE_TYPE;
				}
				if (result.isNotOK()) {
					result.show(this);
				}
				else {
					// Set reload flag.
					reloadDiagram = true;
				}
			}
		}
		else {
			MiddleResult.AFFECTED_ELEMENT_ERROR.show(this);
		}

		// Reload diagram.
		if (reloadDiagram) {
			// Reset tools.
			resetTools();						
			// Render program levels.
			loadLevels();
			// Set zoom.
			setZoom();
		}
	}

	/**
	 * On mouse released.
	 * 
	 * @param e
	 */
	@Override
	protected void onMouseReleased(MouseEvent e, boolean onDiagramElement) {

	}

	/**
	 * On mouse dragged.
	 * @param e
	 */
	@Override
	protected void onMouseDragged(MouseEvent e) {

	}

	/**
	 * Gets diagram rectangle.
	 */
	private Rectangle getRectangle() {
	
		return new Rectangle(ToolList.getWidth(), 0, getWidth() - ToolList.getWidth(),
				getHeight());
	}

	/**
	 * Gets rectangle.
	 * @return
	 */
	@Override
	protected Rectangle2D getWindowRectangle() {

		return getRectangle();
	}

	/**
	 * On resized.
	 */
	@Override
	protected void onResized() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {

		// Set background color.
		setBackground(CustomizedColors.get(ColorId.BACKGROUND));

		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		// Draw tool list.
		toolList.draw(g2);
		
		int width = getWidth();
		int height = getHeight();

		// Set clipping rectangle.
		Shape oldClip = g2.getClip();
		g2.setClip(ToolList.getWidth(), 0, width - ToolList.getWidth(), height);
		
		AffineTransform oldTransform = g2.getTransform();
		g2.translate(getTranslatingX(), getTranslatingY());
		g2.scale(zoom * zoomMultiplier, zoom * zoomMultiplier);
		
		// Draw diagram.
		drawDiagram(g2);
		
		// Draw affected elements.
		drawAffected(g2);
		
		g2.setTransform(oldTransform);
		g2.setClip(oldClip);
		
		// Draw connector.
		drawConnector(g2);
		
		// Draw overview button.
		overview.draw(g2);
		
		// Draw scroll bars.
		horizontalScroll.draw(g2, CustomizedColors.get(ColorId.SCROLLBARS));
		verticalScroll.draw(g2, CustomizedColors.get(ColorId.SCROLLBARS));
		
		// Draw zoom.
		if (zoomShape != null) {
			zoomShape.draw(g2);
		}
		
		// Draw new step shape.
		if (newStepShape != null) {
			newStepShape.draw(g2, false);
		}
		
		// Draw remove area shape.
		if (removeElementShape != null) {
			removeElementShape.draw(g2);
		}
	}

	/**
	 * Draw affected elements.
	 * @param g2
	 */
	private void drawAffected(Graphics2D g2) {

		// Do loop for all affected elements.
		for (Affected element : affectedElements) {
			
			element.drawAffected(g2, getZoom());
		}
	}

	/**
	 * Draw diagram.
	 */
	private void drawDiagram(Graphics2D g2) {
		
		// Draw program level
		programLevel.draw(g2);
	}

	/**
	 * On draw overview content.
	 * @param g2
	 */
	@Override
	protected void onDrawOverview(Graphics2D g2, AffineTransform transform) {

		// Draw diagram.
		drawDiagram(g2);
	}

	/**
	 * Get text.
	 * 
	 * @return
	 */
	public String getTabDescription() {

		return program.toString();
	}

	/**
	 * Returns true if program id matches.
	 * 
	 * @param programId
	 * @return
	 */
	public boolean isProgram(long programId) {

		return program.getId() == programId;
	}

	/**
	 * Initialize translation.
	 */
	private void initializeTranslation() {

		translationx = getInitialTranslationX();
		translationy = getInitialTranslationY();
	}
	
	/**
	 * Gets initial X translation.
	 */
	public int getInitialTranslationX() {
		
		// Get this panel dimension.
		Dimension panel = getSize();
		int panelLeft = ToolList.getWidth();
		int panelRight = panel.width;
		int panelWidth = panelRight - panelLeft;
		int shapeWidth = programLevel.getLevelWidth();
		
		if (shapeWidth <= panelWidth) {
			return panelLeft + panelWidth / 2;
		}
		else {
			return panelLeft + shapeWidth / 2;
		}
	}
	
	/**
	 * Gets initial Y translation.
	 */
	public int getInitialTranslationY() {
		
		return programLevel.getLevelTitleHeight() / 2 + diagramSpaceY;
	}

	/**
	 * Closes diagram.
	 */
	public void close() {

		programLevel.close();
		programLevel = null;
		
		removeDiagram();
	}

	/**
	 * @return the translationx
	 */
	public double getTranslationX() {
		
		return translationx;
	}

	/**
	 * @return the translationy
	 */
	public double getTranslationY() {
		
		return translationy;
	}

	/**
	 * Gets diagram width.
	 */
	public int getDiagramWidth() {

		return getWidth() - ToolList.getWidth();
	}

	/**
	 * Gets diagram height.
	 */
	public int getDiagramHeight() {

		return getHeight();
	}

	/**
	 * @return the diagramspacey
	 */
	public static int getDiagramSpaceY() {
		return diagramSpaceY;
	}

	/**
	 * @return the animateVectorX
	 */
	public int getAnimateVectorX() {
		
		return (int) animateVectorX;
	}

	/**
	 * @return the animateVectorY
	 */
	public int getAnimateVectorY() {
		
		return (int) animateVectorY;
	}

	/* (non-Javadoc)
	 * @see program.builder.GeneralDiagram#onToolTip()
	 */
	@Override
	protected boolean onToolTip() {

		Object element = null;
		boolean isSet = false;
		
		Point screenMouse = MouseInfo.getPointerInfo().getLocation();
		Point panelMouse = (Point) screenMouse.clone();
		SwingUtilities.convertPointFromScreen(panelMouse, this);
		
		Rectangle window = getRectangle();
		
		// If the mouse is not in window, exit this method.
		if (!window.contains(panelMouse)) {
			return false;
		}

		// If the mouse is in vertical or horizontal scroll bar
		// or overview or zoom, exit this method.
		if (horizontalScroll.contains(panelMouse)
			|| verticalScroll.contains(panelMouse)
			|| overview.contains(panelMouse)
			|| zoomShape.contains(panelMouse)) {
			
			return false;
		}

		if (tooltipWindow != null) {

			// If the mouse is on tool list, return false.
			if (panelMouse.x <= ToolList.getWidth()) {
				oldElement = null;
				return false;
			}
			
			Point2D transformedpanelMouse = doTransformation(panelMouse);
			
			Rectangle elementRectangle = new Rectangle();
			String description = null;
			
			// Get macro element or step.
			MacroElement macroElement = programLevel.getMacroElement(transformedpanelMouse,
					elementRectangle);
			
			if (macroElement != null) {
				description = macroElement.toString();
				element = macroElement;
			}
			else {
				Step step = programLevel.getStep(transformedpanelMouse, elementRectangle);
				
				if (step != null) {
					description = step.getDescription();
					element = step;
				}
			}
			
			// If it is a new element.
			if (element != null) {
				if (element != oldElement) {
					
					// Set tool tip window location.
					Point auxiliary = new Point(0, elementRectangle.y + elementRectangle.height);
					Point2D auxiliary2D = undoTransformation(auxiliary);
					auxiliary = new Point((int) auxiliary2D.getX(), (int) auxiliary2D.getY());
					SwingUtilities.convertPointToScreen(auxiliary, this);
					Point location = new Point(screenMouse.x, auxiliary.y);
					
					tooltipWindow.showw(location, description);
				}
				isSet = true;
			}
		}
		
		oldElement = element;

		return isSet;
	}

	/**
	 * Resets tool tip.
	 */
	@Override
	protected void resetToolTipHistory() {
		
		oldElement = null;
	}

	/**
	 * Gets not transformed content rectangle.
	 */
	private Rectangle getNotTransformedContentRectangle() {
		
		// If the first level doesn't exist, exit the method.
		if (programLevel == null) {
			return null;
		}
		
		Rectangle resultRectangle = null;
		ProgramLevel level = programLevel;
		
		boolean first = true;
		
		// Do loop for all levels.
		do {
			// Get given level rectangle and compute union with resulting
			// rectangle.
			Rectangle levelRectangle = level.getLevelRect();
			
			if (first) {
				resultRectangle = new Rectangle(levelRectangle);
				first = false;
			}
			else {
				resultRectangle = Utility.union(resultRectangle, levelRectangle);
			}
			
			// Get next level.
			level = level.getSublevel();
		}
		while (level != null);
		
		return resultRectangle;
	}
	
	/**
	 * Gets content rectangle.
	 */
	@Override
	protected Rectangle2D getContentRectangle() {
	
		Rectangle content = getNotTransformedContentRectangle();
		if (content == null) {
			return null;
		}
		
		// Transform content.
		Rectangle2D content2D = undoTransformation(content);
		content = new Rectangle((int) content2D.getX(), (int) content2D.getY(),
				(int) content2D.getWidth(), (int) content2D.getHeight());
		
		return content;
	}

	/**
	 * Set overview.
	 */
	public void setOverview() {

		// Get content rectangle.
		contentRectangle = getNotTransformedContentRectangle();
		// Set overview.
		overview.setDiagramRectangle(contentRectangle);
		overview.setTranslation(getTranslatingX(), getTranslatingY());
		// Set scroll bars location.
		setScrollBarsLocation();
	}
	
	/**
	 * Set zoom.
	 */
	@Override
	protected void setZoom() {
		
		// Get content rectangle.
		Rectangle content = getNotTransformedContentRectangle();
		// Get window rectangle.
		Rectangle window = getRectangle();
		
		// Test object references.
		if (content == null || window == null) {
			return;
		}
		
		double zoomX = 1.0;
		double zoomY = 1.0;
		
		// Shrink window.
		window.width *= diagramMarginsMultiplier;
		window.height *= diagramMarginsMultiplier;
		
		// If the content rectangle is greater than window rectangle...
		if (content.width > window.width) {
			zoomX = (double) window.width / (double) content.width;
		}
		if (content.height > window.height) {
			zoomY = (double) window.height / (double) content.height;
		}
		
		// Set zoom.
		if (zoomX < zoomY) {
			minimumZoom = zoomX;
		}
		else {
			minimumZoom = zoomY;
		}
		if (zoomShape != null) {
			zoomShape.setMinimal(minimumZoom);
			zoomShape.setZoom(zoom * zoomMultiplier);
		}
	}

	/**
	 * Gets zoom.
	 * @return
	 */
	public double getZoom() {

		return zoom * zoomMultiplier;
	}
	
	/**
	 * Center diagram.
	 */
	@Override
	public void center() {
		
		// Set zoom.
		setZoom();
		zoom = zoomShape.getMinimal();
		zoomShape.setZoom(zoom);
		
		// Set vertical diagram translation.
		translationy = (int) (programLevel.getLevelTitleHeight() * zoom) / 2 + diagramSpaceY;
		
		// Set horizontal diagram translation.
		Rectangle content = getNotTransformedContentRectangle();
		Rectangle2D content2D = undoTransformation(content);
		content = new Rectangle((int) content2D.getX(), (int) content2D.getY(),
				(int) content2D.getWidth(), (int) content2D.getHeight());
		int levelWidth = (int) (programLevel.getLevelWidth() * zoom);
		Rectangle window = getRectangle();
		
		translationx = window.x + window.width / 2 + levelWidth / 2 - content.width / 2;
		
		// Set overview.
		overview.setTranslation(getTranslationX(), getTranslatingY());
		overview.setScale(zoom);
		overview.setDiagramRectangle(contentRectangle);
		
		// Set scroll bars.
		setScrollBars();
		repaint();
	}

	/**
	 * Gets first affected element.
	 * @return
	 */
	private AffectedDynamic getFirstAffected() {

		if (affectedElements.isEmpty()) {
			return null;
		}
		
		return affectedElements.getFirst();
	}
	
	/**
	 * On new step.
	 */
	private void onNewStep() {

		// Get affected element.
		Affected element = getFirstAffected();
		boolean reloadDiagram = false;
		
		if (element != null) {
			
			MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
			GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
			Properties login = ProgramBasic.getLoginProperties();
			MiddleResult result;
			
			// Check the element type.
			if (element instanceof MacroElementShape) {
				
				// Try to get procedure name.
				String procedureDescription = JOptionPane.showInputDialog(this,
						Resources.getString("messageInsertProcedureName"),
						Resources.getString("textNewProcedure"));
				if (procedureDescription == null) {
					return;
				}
				
				MacroElementShape macroShape = (MacroElementShape) element;
				MacroElement macroElement = macroShape.getMacroElement();
				
				if (macroElement != null) {
					// Check the macro element type.
					if (macroElement instanceof Program
						|| macroElement instanceof Procedure) {
						
						Step newStep = new Step();
						
						// Add a new start step to the program.
						result = middle.insertStartStep(login, macroElement, procedureDescription,
								newStep);
						if (result != MiddleResult.OK) {
							result.show(this);
							return;
						}
						
						reloadDiagram = true;
					}
				}
			}
			else if (element instanceof StepShape) {
				
				Obj<String> procedureDescription = new Obj<String>();
				
				// Ask user if to create new branch.
				int confirm = ConfirmStepCreation.showConfirmDialog(
						frame, procedureDescription, true);
				// On cancel exit the method.
				if (confirm == ConfirmStepCreation.CANCEL) {
					return;
				}
				
				StepShape stepShape = (StepShape) element;
				Step step = stepShape.getStep();
				
				if (step != null) {
					
					// Create new step.
					Step newStep = new Step();
					
					// If it is a next step...
					if (!stepShape.isTopAffected()) {

						if (confirm == ConfirmStepCreation.BRANCH) {
							
							// Add new next step to the diagram.
							result = middle.insertNextBranchStep(login, step,
									procedureDescription.ref, newStep);
						}
						else {
							
							// Add new next step.
							result = middle.insertNextConnectedStep(login, step,
									procedureDescription.ref, newStep);
						}
					}
					else {
						if (confirm == ConfirmStepCreation.BRANCH) {
							
							// Add new previous step branch to the diagram.
							result = middle.insertPreviousBranchStep(login, step,
									procedureDescription.ref, newStep);
						}
						else {
							
							// Add new previous step to the diagram.
							result = middle.insertPreviousConnectedStep(login, step,
									procedureDescription.ref, newStep);
						}
					}
					
					if (result != MiddleResult.OK) {
						result.show(this);
						return;
					}

					reloadDiagram = true;
				}
			}
			// If an edge is affected...
			else if (element instanceof EdgeShape) {
				
				Obj<String> description = new Obj<String>();
				if (ConfirmStepCreation.showConfirmDialog(frame, description, false)
						== ConfirmStepCreation.CANCEL) {
					return;
				}
				
				EdgeShape edgeShape = (EdgeShape) element;
				
				// Create new step.
				Step newStep = new Step();
				
				// Add the new step to the database.
				result = middle.insertNewConnectedStep(login, edgeShape.getMacroElement(),
						edgeShape.getEdge(), newStep, description.ref);
				
				if (result != MiddleResult.OK) {
					result.show(this);
					return;
				}
				
				reloadDiagram = true;
			}
		}
		
		// Reload diagram.
		if (reloadDiagram) {
			
			// Reset tools.
			resetTools();
			// Render program levels.
			loadLevels();
			// Set zoom.
			setZoom();
		}
	}

	/**
	 * Reset tools
	 */
	private void resetTools() {

		setNewStepVisibility(false);
		newStepShape = null;
		setRemoveShapeVisibility(false);
		removeElementShape = null;
		resetAffectedElements();
		affectedBegin = null;
		toolList.selectTool(0);
	}

	/**
	 * Load levels.
	 */
	private void loadLevels() {

		// Delegate method call.
		programLevel.loadLevels();
		repaint();
	}
	
	/**
	 * On move connector.
	 * @param mouse
	 */
	private void onMoveConnector(Point mouse) {

		if (affectedBegin == null) {
			// Affect element.
			affectElement(mouse, AFF_CONNECTOR);
		}
		else {
			// Affect next element.
			affectNextElement(mouse);
		}
		connectorVisible = true;
	}

	/**
	 * On connector.
	 */
	private void onConnector() {
			
		// If the previous element is not selected...
		if (affectedBegin == null) {
			
			// Get affected step.
			AffectedDynamic affected = getFirstAffected();
			if (affected == null) {
				return;
			}
			
			affectedBegin = affected;
			
			// Clear affected elements.
			resetAffectedElements();
		}
		else {
			onAddConector();
			resetTools();
		}
		
		repaint();
	}
	
	/**
	 * On add connector.
	 */
	private void onAddConector() {
		
		boolean reloadDiagram = false;
		
		// Get affected element.
		Affected affectedEnd = getFirstAffected();
		if (affectedEnd != null) {
		
			// If the affected element is a step...
			if (affectedEnd instanceof StepShape) {
				
				MiddleDynamic middle = ProgramBuilderDynamic.getMiddle();
				MiddleResult result;
				Properties login = ProgramBasic.getLoginProperties();
				
				// Get end step.
				StepShape endShape = (StepShape) affectedEnd;
				Step endStep = endShape.getStep();
				
				// If the beginning is a macro element...
				if (affectedBegin instanceof MacroElementShape) {
					
					// Update step's start flag.
					result = middle.updateStepIsStart(login, endStep, true);
					if (result.isNotOK()) {
						result.show(this);
					}
					else {
						reloadDiagram = true;
					}
				}
				// If the beginning is a step...
				else if (affectedBegin instanceof StepShape) {
					
					StepShape beginShape = (StepShape) affectedBegin;
					Step beginStep = beginShape.getStep();
					
					if (beginStep != null) {
						
						// If the beginnings is not the same as the end...
						if (beginStep != endStep) {
								
							MacroElement macroElement = endStep.getMacroElement();
							
							// Ask if to remove existing start edge.
							boolean isStartStep = endStep.isStart();
							StepsGraph graph = new StepsGraph(macroElement, false);
							boolean hasInputs = !graph.getInputs(endStep).isEmpty();
							boolean confirmed = true;
							
							Obj<Boolean> removeStartEdge = new Obj<Boolean>(false);
							
							if (isStartStep && !hasInputs) {
								confirmed = ConfirmBeginEdgeRemoval.showConfirmDialog(removeStartEdge);
							}
							
							if (confirmed) {

								// Login.
								result = middle.login(login);
								if (result.isOK()) {

									if (removeStartEdge.ref) {
										// Remove start edge.
										result = middle.updateStepIsStart(endStep, false);
									}
									
									if (result.isOK()) {
										// Add next step edge.
										result = middle.insertNextStepEdge(beginStep, endStep);
									}
									// Logout.
									middle.logout(result);
								}
								
								// Show error.
								if (result.isNotOK()) {
									result.show(this);
								}
								else {
									reloadDiagram = true;
								}
							}
						}
					}
				}
			}
		}
			
		// Reset tools.
		resetTools();
		
		// Reload diagram.
		if (reloadDiagram) {
			// Render program levels.
			loadLevels();
			// Set zoom.
			setZoom();
		}
	}

	/**
	 * Draw connector.
	 * @param g2
	 */
	private void drawConnector(Graphics2D g2) {

		// If there is not any previous element exit the method.
		if (affectedBegin == null) {
			return;
		}
		
		// If the connector is not visible, exit the method.
		if (!connectorVisible) {
			return;
		}
		
		// Get previous element center.
		Point previousCenter = affectedBegin.getLabelCenter();
		Point2D previousCenter2D = undoTransformation(previousCenter);
		previousCenter = new Point((int) previousCenter2D.getX(), (int) previousCenter2D.getY());
		
		// Get current end.
		Point endCenter = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(endCenter, this);
		
		g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));

		Stroke oldStroke = g2.getStroke();
		BasicStroke linesStroke = new BasicStroke(connectorWidth, BasicStroke.CAP_ROUND,
				BasicStroke.CAP_ROUND);
		g2.setStroke(linesStroke);
		
		// Draw connection.
		g2.drawLine(previousCenter.x, previousCenter.y, endCenter.x, endCenter.y);
		
		
		// If the distance of the points is greater than arrow length
		// draw the arrow.
		double distance = Math.sqrt(Math.pow(previousCenter.x - endCenter.x, 2)
				                  + Math.pow(previousCenter.y - endCenter.y, 2));
		boolean drawArrow = distance > arrowLength;
		Point pointA = null;
		Point pointB = null;
		
		if (drawArrow) {
			// Compute arrow parameters.
			pointA = new Point();
			pointB = new Point();
			
			GraphUtility.computeArrow(previousCenter, endCenter, arrowAlpha, arrowLength,
					pointA, pointB);
					
			// Draw arrow.
			g2.drawLine(endCenter.x, endCenter.y, pointA.x, pointA.y);
			g2.drawLine(endCenter.x, endCenter.y, pointB.x, pointB.y);
		}
		
		// Draw line centers.
		g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND));
		g2.setColor(CustomizedColors.get(ColorId.BACKGROUND));
		g2.drawLine(previousCenter.x, previousCenter.y, endCenter.x, endCenter.y);
		
		if (drawArrow) {
			g2.drawLine(pointA.x, pointA.y, endCenter.x, endCenter.y);
			g2.drawLine(pointB.x, pointB.y, endCenter.x, endCenter.y);
		}
		
		g2.setStroke(oldStroke);
		
		g2.setColor(CustomizedColors.get(ColorId.OUTLINES_PROTECTED));
		
		// Draw connector beginning.
		g2.fillOval(previousCenter.x - connectorCircle / 2,
				previousCenter.y - connectorCircle / 2, connectorCircle,
				connectorCircle);
		
		// Draw connector end.
		g2.fillOval(endCenter.x - connectorCircle / 2,
				endCenter.y - connectorCircle / 2, connectorCircle,
				connectorCircle);
	}

	/**
	 * Affect next element.
	 * @param mouse
	 */
	private void affectNextElement(Point mouse) {
		
		resetAffectedElements();

		// Get affected element.
		Point2D transformedMouse = doTransformation(mouse);
		AffectedDynamic endElement = programLevel.getAffectedElement(
				transformedMouse, AFF_CONNECTOR);
		if (!(endElement instanceof StepShape)) {
			return;
		}
		
		// If the affected element is the previous element exit the
		// method.
		if (endElement == affectedBegin) {
			return;
		}
		
		// If the previous element and the end element are not parts of
		// the same macro element exit the method.
		MacroElement previousMacroElement = affectedBegin.getMacroElement();
		MacroElement endMacroElement = endElement.getMacroElement();
		
		if (previousMacroElement != endMacroElement) {
			return;
		}
		
		// If the previous element already has a next element, exit
		// the method.
		if (affectedBegin.isNext(endElement)) {
			return;
		}
		
		// If the beginning element is a macro element and the next step
		// is already a start step, exit the method.
		if (affectedBegin instanceof MacroElementShape
				&& endElement instanceof StepShape) {

			MacroElementShape macroShape = (MacroElementShape) affectedBegin;
			MacroElement beginMacro = macroShape.getMacroElement();
			StepShape stepShape = (StepShape) endElement;
			Step step = stepShape.getStep();
			
			if (beginMacro == null || step == null) {
				return;
			}
			
			if (step.isStart()) {
				return;
			}
		}
		
		setAffectedElement(endElement);
	}

	/* (non-Javadoc)
	 * @see program.builder.GeneralDiagram#onReloadDiagram()
	 */
	@Override
	protected void onReloadDiagram() {

		// Reload program levels.
		loadLevels();
	}

	/**
	 * On escape.
	 */
	@Override
	protected void onEscape() {
		
	}

	@Override
	public String getTabDecsription() {

		return program.toString();
	}

	@Override
	protected void onPopupMenu(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
