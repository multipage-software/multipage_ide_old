/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.maclan.Area;
import org.maclan.AreaResource;
import org.maclan.AreasModel;
import org.maclan.ConstructorGroup;
import org.maclan.ConstructorHolder;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.ResourceConstructor;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Progress2Dialog;
import org.multipage.gui.Utility;
import org.multipage.gui.ZoomListener;
import org.multipage.gui.ZoomShape;
import org.multipage.util.Obj;
import org.multipage.util.ProgressResult;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;
import org.multipage.util.j;

/**
 * @author
 *
 */
public class AreasDiagram extends GeneralDiagram implements TabItemInterface {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * New area width.
	 */
	private static final double newAreaWidth = 100;

	/**
	 * Space between zoom shape and vertical scroll.
	 */
	protected static final int zoomScrollSpace = 10;

	/**
	 * Minimum drag.
	 */
	private static final int minimumDrag = 5;

	/**
	 * Connector width.
	 */
	private static final float connectorWidth = 3.0f;

	/**
	 * Connector circle size.
	 */
	private static final int connectorCircle = 12;
	
	/**
	 * Sub area border percent.
	 */
	private static final double subareaBorderPercent = 10;

	/**
	 * Tool tip vertical shift.
	 */
	private static final int tooltipVerticalShift = 20;
	
	/**
	 * Constructors display window horizontal shift.
	 */
	private static final int constructorsDisplayHorizontalShift = 101;

	/**
	 * Focused area shape width.
	 */
	public static int focusAreaShapeWidth = 600;

	/**
	 * Focused area shape position.
	 */
	private static final int focusAreaShapeLocation = 30;
	
	/**
	 * Top area ID
	 */
	private Long topAreaId = 0L;
	
	/**
	 * Dialog states.
	 */
	private static double translationxState;
	private static double translationyState;
	private static double zoomState;
	private static boolean setDefaultStateFlagState;

	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Read states.
		translationxState = inputStream.readDouble();
		translationyState = inputStream.readDouble();
		zoomState = inputStream.readDouble();
		// Set default values flag.
		setDefaultStateFlagState = false;
		AreaShapes.readOnlyLighter = inputStream.readBoolean();
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		// Write states.
		outputStream.writeDouble(translationxState);
		outputStream.writeDouble(translationyState);
		outputStream.writeDouble(zoomState);
		outputStream.writeBoolean(AreaShapes.readOnlyLighter);
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		setDefaultStateFlagState = true;
	}

	/**
	 * Load dialog.
	 */
	public void loadDialog() {
		
		// Read states.
		translationx = translationxState;
		translationy = translationyState;
		zoom = zoomState;
		// Set default values flag.
		setDefaultStateFlag = setDefaultStateFlagState;
	}
	
	/**
	 * Save dialog.
	 */
	public void saveDialog() {
		
		translationxState = translationx;
		translationyState = translationy;
		zoomState = zoom;
	}
	
	/**
	 * Selection rectangle.
	 */
	private Rectangle selectionRectangle = new Rectangle();

	/**
	 * True if default state to load.
	 */
	private boolean setDefaultStateFlag = true;
	
	/**
	 * It is true if an area is preselected.
	 */
	private Point preselected;
	
	/**
	 * New area shape.
	 */
	private AreaShapes newAreaShape;

	/**
	 * Area to be connected.
	 */
	private AreaShapes areaShapeConnected;
	
	/**
	 * Popup trayMenu.
	 */
	private JPopupMenu popupMenu;

	/**
	 * Display constructors window.
	 */
	private ConstructorsDisplayWindow displayConstructorsWindow;

	/**
	 * Not animate next focus flag.
	 */
	private boolean notAnimateNextFocus = false;

	/**
	 * Area trayMenu.
	 */
	private AreaLocalMenu areaMenu;

	/**
	 * A reference to last selected area coordinates.
	 */
	private AreaCoordinates lastSelectedAreaCoordinates;
	
	/**
	 * A flag that signalized invoked selectArea() method.
	 */
	private boolean selectAreaInvoked = false;
	
	/**
	 * If this flag is true, the subareas area currently selected.
	 */
	public boolean affectSubareas = false;
	
	/**
	 * A reference to parent editor panel.
	 */
	@SuppressWarnings("unused")
	private AreasDiagramPanel parentEditor;
	
	/**
	 * Constructor.
	 * @param parent 
	 */
	public AreasDiagram(AreasDiagramPanel parentEditor) {
		
		// Remember reference to the parent editor.
		this.parentEditor = parentEditor;
		
		// Create components.
		createComponents();
		// Load dialog.
		loadDialog();
		
		// Cursor tool.
		toolList.add(new Tool(
				toolList, 0, ToolId.CURSOR, 
				Resources.getString("org.multipage.generator.tooltipSelection"), 
				"org/multipage/generator/images/cursor.png",
				Resources.getString("org.multipage.generator.textAreaCursorToolDescription")));
		// Move tool.
		toolList.add(new Tool(
				toolList, 1, ToolId.MOVE, 
				Resources.getString("org.multipage.generator.tooltipMove"), 
				"org/multipage/generator/images/move.png",
				Resources.getString("org.multipage.generator.textMoveToolDescription")));
		// Zoom tool.
		toolList.add(new Tool(
				toolList, 2, ToolId.ZOOM,
				Resources.getString("org.multipage.generator.tooltipZoom"),
				"org/multipage/generator/images/zoom.png",
				Resources.getString("org.multipage.generator.textZoomToolDescritpion")));
		// Area tool.
		toolList.add(new Tool(
				toolList, 3, ToolId.AREA,
				Resources.getString("org.multipage.generator.tooltipNewArea"),
				"org/multipage/generator/images/area.png",
				Resources.getString("org.multipage.generator.textAreaToolDescription")));
		// Remove tool.
		toolList.add(new Tool(
				toolList, 4, ToolId.REMOVE,
				Resources.getString("org.multipage.generator.tooltipRemoveArea"),
				"org/multipage/generator/images/remove_element.png",
				Resources.getString("org.multipage.generator.textRemoveAreaDescription")));	
		// Connector tool.
		toolList.add(new Tool(
				toolList, 5, ToolId.CONNECTOR,
				Resources.getString("org.multipage.generator.tooltipNewConnector"),
				"org/multipage/generator/images/connector.png",
				Resources.getString("org.multipage.generator.textConnectorToolDescription")));
		
		// Set transfer handler.
		setTransferHandle();
		
		// Initialize short cuts.
		createShortCuts();
		
		// Set listeners.
		setListeners();
	}
	
	/**
	 * Set listeners.
	 * @return
	 */
	private void setListeners() {
		
		// Add redraw event listener.
		ConditionalEvents.receiver(this, Signal.array(Signal.loadDiagrams, Signal.updateAll), message -> {
			
			// Disable the signal temporarily.
			Signal.updateAll.disable();
			
			// Reload and repaint the diagram.
			reload(false, false);
			setOverview();
			repaint();
			
			// Get selected area IDs.
			HashSet<Long> selectedIds = getSelectedAreaIds();
			
			j.log("TRANSMITTED 4 showAreasProperties %s", selectedIds.toString());
			// Propagate the "show areas' properties" signal.
			ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasProperties, selectedIds);
			// Propagate the "show areas' relations" signal.
			ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasRelations, selectedIds);
			
			// Enable the signal.
			SwingUtilities.invokeLater(() -> {
				Signal.updateAll.enable();
			});
		});
		
		// Add receiver for the "click areas in diagram" event.
		ConditionalEvents.receiver(this, Signal.onClickDiagramAreas, message -> {
			
			// Check the source diagram, clicked graph point and if CTRL key has been pressed.
			if (AreasDiagram.this.equals(message.source) && message.relatedInfo instanceof Point2D && message.isAdditionalInfo(0, Boolean.class)) {
				
				// Pull graph point and CTRL flag from the event action.
				Point2D graphPoint = (Point2D) message.relatedInfo;
				boolean ctrlKeyPressed = message.getAdditionalInfo(0);
				
				// Select area and sub areas in this diagram. The diagram must be repainted.
				select(graphPoint, !ctrlKeyPressed);
				repaint();
				
				// Get selected area IDs.
				HashSet<Long> selectedIds = getSelectedAreaIds();
				
				j.log("TRANSMITTED 2 showAreasProperties %s", selectedIds.toString());
				// Propagate the "show areas' properties" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasProperties, selectedIds);
				// Propagate the "show areas' relations" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasRelations, selectedIds);
			}
		});
		
		// Add receiver for the "drag areas in diagram" event.
		ConditionalEvents.receiver(this, Signal.onDragDiagramAreas, message -> {
			
			// Check the source diagram.
			if (AreasDiagram.this.equals(message.source)) {
				
				// Get selected area IDs.
				HashSet<Long> selectedIds = getSelectedAreaIds();
				
				j.log("TRANSMITTED 1 showAreasProperties %s", selectedIds.toString());
				// Propagate the "show areas' properties" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasProperties, selectedIds);
				// Propagate the "show areas' relations" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasRelations, selectedIds);
			}
		});
		
		// Listen for "select diagram areas" event.
		ConditionalEvents.receiver(this, Signal.selectDiagramAreas, message -> {
			
			if (message.relatedInfo instanceof HashSet<?>) {
				
				// Pull set of area IDs.
				HashSet<Long> selectedIds = (HashSet<Long>) message.relatedInfo;
				// Remove selection.
				removeSelection();
				// Select the areas.
				selectAreas(selectedIds);
				// Set overview and repaint the GUI.
				setOverview();
				repaint();
			}
		});
		
		// Add area selection receiver.
		ConditionalEvents.receiver(this, Signal.array(Signal.selectAll), message -> {
			if (AreasDiagram.this.isShowing()) {
				
				// Select all.
				setAllSelection(true);
				// Set overview and repaint the GUI.
				setOverview();
				repaint();
			}
		});
			
		// Add unselect all receiver.
		ConditionalEvents.receiver(this, Signal.unselectAll, message -> {
											 
			if (AreasDiagram.this.isShowing()) {
				
				// Unselect all.
				setAllSelection(false);
				// Set overview and repaint the GUI.
				setOverview();
				repaint();
			}
		});
		
		// "Update all request" event receiver.
		ConditionalEvents.receiver(this, Signal.updateAll, ConditionalEvents.MIDDLE_PRIORITY, message -> {
			
			// Render the areas diagram.
			renderDiagram();
		});
		
		// Add focus event receiver.
		ConditionalEvents.receiver(this, Signal.focusBasicArea, message -> {
			
			// Focus currently visible Basic Area.
			if (AreasDiagram.this.isShowing()) {
				focusBasicArea();
			}
		});
		
		// Add receiver for the "expose read only areas" event.
		ConditionalEvents.receiver(this, Signal.exposeReadOnlyAreas, message -> {
			
			// Set overview and repaint the GUI.
			setOverview();
			repaint();
		});
		
		// Add non-coalescing receiver (with time span equal to 0L) for the "display or redraw tool tip" event.
		ConditionalEvents.receiver(this, Signal.displayOrRedrawToolTip, message -> {
			
			if (message.sourceObject(this)) {
				// Display appropriate tool tip only if constructor window is hidden.
				boolean isSet = displayConstructorsWindow.isVisible() ? false : onToolTip();
				// or hide the tool tip window.
				if (!isSet) {
					tooltipWindow.hidew();
				}
			}
		},
		0L);
		
		// Add receiver for the "remove toll tip" event. Event priority is decreased with relation to "display or redraw tool tip" event.
		ConditionalEvents.receiver(this, Signal.removeToolTip, ConditionalEvents.LOW_PRIORITY, message -> {
			
			// Hide tool tip window.
			tooltipWindow.hidew();
		});
		
		// Add receiver for the "show or hide" event.
		ConditionalEvents.receiver(this, Signal.showOrHideIds, message -> {
			
			// Set overview and repaint the GUI.
			setOverview();
			repaint();
		});
		
		// Add receiver for the "on tab change" event.
		ConditionalEvents.receiver(this, Signal.mainTabChange, message -> {
			
			if (AreasDiagram.this.isShowing()) {
				
				// Get selected area IDs.
				HashSet<Long> selectedIds = getSelectedAreaIds();
				
				// Remove selection.
				removeSelection();
				
				// Selected area IDs.
				for (long areaId : selectedIds) {
					
					Area area = ProgramGenerator.getArea(areaId);
					selectRecursive(area, true, false);
				}
				
				j.log("TRANSMITTED 3 showAreasProperties %s", selectedIds.toString());
				// Propagate the "show areas' properties" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasProperties, selectedIds);
				// Propagate the "show areas' relations" signal.
				ConditionalEvents.transmit(AreasDiagram.this, Signal.showAreasRelations, selectedIds);
			}
		});
	}
	
	/**
	 * Remove listeners.
	 */
	private void removeListeners() {
		
		ConditionalEvents.removeReceivers(this);
	}
	
	/**
	 * Create short cuts.
	 */
	@SuppressWarnings("serial")
	private void createShortCuts() {
		
		// Focus super area: CTRL + S key.
		Action focusSuperAreaAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (areaMenu != null) {
					areaMenu.focusSuperArea();
				}
			}};
			
		KeyStroke focusSuperAreaKey = KeyStroke.getKeyStroke("control S");
		getInputMap().put(focusSuperAreaKey, "focusSuperArea");
		getActionMap().put("focusSuperArea", focusSuperAreaAction);
	}

	/**
	 * Set transfer handle.
	 */
	protected void setTransferHandle() {
		
	}

	/**
	 * Create components.
	 */
	private void createComponents() {
		
		createPopupMenu();
		
		// Create constructor display window.
		displayConstructorsWindow = new ConstructorsDisplayWindow(this);
	}

	/**
	 * Create popup trayMenu.
	 */
	private void createPopupMenu() {
		
		// Create trayMenu.
		popupMenu = new JPopupMenu();
		// Create area local trayMenu.
		final Component thisComponent = this;
		
		areaMenu = ProgramGenerator.newAreaLocalMenuForDiagram(new AreaLocalMenuListener() {
			
			// Get current area.
			@Override
			protected Area getCurrentArea() {
				// Get selected areas.
				LinkedList<Area> areas = getSelectedAreas();
				if (areas.size() != 1) {
					return null;
				}
				return areas.getFirst();
			}

			// Get current parent area.
			@Override
			public Area getCurrentParentArea() {
				
				// Try to find selected area shape super area.
				AreaCoordinates lastSelectedCoordinates = getLastSelectedAreaCoordinates();
				if (lastSelectedCoordinates != null) {
					
					Area parentArea = lastSelectedCoordinates.getParentArea();
					return parentArea;
				}
				
				return null;
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		areaMenu.addTo(this, popupMenu);
	}

	/**
	 * Returns true value is area connection is in progress.
	 * @return
	 */
	public boolean isAreaConnection() {
		
		return areaShapeConnected != null;
	}

	/**
	 * On popup trayMenu.
	 */
	@Override
	protected void onPopupMenu(MouseEvent e) {
		
		popupMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * On enter this window.
	 */
	@Override
	protected void onMouseEntered() {

		// If the area tool is selected, show the new area.
		if (toolList.getSelected() == ToolId.AREA
			|| toolList.getSelected() == ToolId.CONNECTOR) {
			
			setNewAreaVisible(true);
		}
		
		// Transmit "display or redraw tool tip" signal.
		ConditionalEvents.transmit(this, Signal.displayOrRedrawToolTip);
	}

	/**
	 * On exit this window.
	 */
	@Override
	protected void onMouseExited() {

		// If the area tool is selected, hide the new area.
		if (toolList.getSelected() == ToolId.AREA) {
			setNewAreaVisible(false);
		}
		// If the connector tool is selected.
		else if (toolList.getSelected() == ToolId.CONNECTOR) {
			setNewAreaVisible(false);
		}
		
		AreaShapes.resetAffected();
		
		// Hide constructors' names.
		hideConstructorsDisplay();
		
		// Transmit "remove tool tip" signal.
		ConditionalEvents.transmit(this, Signal.removeToolTip);
	}

	/**
	 * On vertical scroll.
	 * @param win
	 */
	@Override
	protected void onVerticalScroll(Rectangle2D win) {
		
		translationy = - win.getY() * zoom;
	}

	/**
	 * On horizontal scroll.
	 * @param win
	 */
	@Override
	protected void onHorizontalScroll(Rectangle2D win) {

		translationx = ToolList.getWidth() - win.getX() * zoom;
	}
	
	/**
	 * On resized.
	 */
	@Override
	protected void onResized() {

	}

	/**
	 * On mouse moved.
	 */
	@Override
	protected void onMouseMoved(Point mouse, boolean onDiagramControl) {

		boolean constructorsMustBeDisplayed = false;
		
		if (!onDiagramControl) {
			
			// If the new area tool is selected.
			if (toolList.getSelected() == ToolId.AREA) {
				// Set new area shape location.
				if (newAreaShape != null) {
					newAreaShape.setFirstLocation(mouse);
					newAreaShape.setVisible(true);
				}
				// Affect area.
				if (!animationTimer.isRunning()) {
					
					boolean isGenerator = !ProgramGenerator.isExtensionToBuilder();
					boolean isAffected = affectArea(mouse, false, isGenerator);
					
					// If an area is affected, display its constructors.
					if (isAffected) {
						
						// Get affected area.
						Area affectedArea = AreaShapes.getOneAffectedArea();
						
						// Display affected area constructors.
						displayConstructors(affectedArea);
						constructorsMustBeDisplayed = true;
					}
				}
				repaint();
			}
			// If the remove area tool is selected.
			else if (toolList.getSelected() == ToolId.REMOVE) {

				// Affect area.
				affectArea(mouse, true, false);
				repaint();
			}
			// If the connector tool is selected.
			else if (toolList.getSelected() == ToolId.CONNECTOR) {
				
				onConnectorMove(mouse);
				repaint();
			}
		}
		else {
			boolean repaint = newAreaShape != null && newAreaShape.isVisible();
			// Hide new area.
			setNewAreaVisible(false);
			
			if (!animationTimer.isRunning()) {
				AreaShapes.resetAffected();
			}
			
			if (repaint) {
				repaint();
			}
		}
		
		// Close constructors display.
		if (!constructorsMustBeDisplayed) {
			hideConstructorsDisplay();
		}
		
		// Transmit "display or redraw tool tip" signal.
		ConditionalEvents.transmit(this, Signal.displayOrRedrawToolTip);
	}

	/**
	 * Sets new area visibility.
	 * @param visible
	 */
	private void setNewAreaVisible(boolean visible) {

		if (newAreaShape != null) {
			newAreaShape.setVisible(visible);
		}
	}

	/**
	 * On connector move.
	 * @param mouse
	 */
	private void onConnectorMove(Point mouse) {
		
		// Affect area.
		affectArea(mouse, false, false);
		
		if (newAreaShape != null) {
			
			// Set the new area location.
			newAreaShape.setFirstLocation(mouse);
			newAreaShape.setVisible(true);

			// If the affected area exists reset it if it exists
			// a circle in the diagram.
			Area affectedArea = AreaShapes.getOneAffectedArea();
			
			if (affectedArea != null) {
				
				Area connectedArea = areaShapeConnected.getArea();
				//AreaShapes.setAffectedAreaVisible(!alreadyExists(affectedArea, connectedArea));
				AreaShapes.setAffectedAreaVisible(!existsCircle(affectedArea, connectedArea));
			}
		}
	}

	/**
	 * Affect area.
	 * @param mouse
	 * @return 
	 */
	public boolean affectArea(Point mouse, boolean deletion, boolean onlyWithConstrcutor) {
		
		// Get transformed mouse position.
		Point2D transformedMouse = doTransformation(mouse);
		
		double minimalSurface = Double.MAX_VALUE;
		AreaShapes minimalShapes = null;
		AreaCoordinates minimalCoordinate = null;

		// Get the smallest affected area.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			
			// If  the area has no constructor, continue the loop.
			if (onlyWithConstrcutor && !area.isAreaConstructor()) {
				continue;
			}
			
			// Get area shapes.
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shapes = (AreaShapes) user;
				
				// Get affected rectangle.
				Obj<AreaCoordinates> affectedCoordinates = new Obj<AreaCoordinates>();
				
				if (shapes.getMinimalAffectedRect(transformedMouse, affectedCoordinates)) {
					
					Rectangle2D affectedRectangle = affectedCoordinates.ref.getRectangle();
					
					// Compute rectangle surface and if it is smaller than minimum set
					// the minimal surface and shapes object reference.
					double surface = affectedRectangle.getWidth()
										* affectedRectangle.getHeight();

					if (surface < minimalSurface) {
						minimalSurface = surface;
						minimalShapes = shapes;
						minimalCoordinate = affectedCoordinates.ref;
					}
				}
			}
		}

		if (minimalShapes != null) {
			
			// If the area is selected and the deletion is requested,
			// affect all selected shapes.
			if (minimalShapes.isSelected() && deletion) {
				AreaShapes.affectAllSelected(ProgramGenerator.getAreasModel());
			}
			// If the affected area is not selected.
			else {
				// Set affected shapes.
				AreaShapes.setAffected(minimalShapes);
			}
			
			AreaShapes.setAffected(minimalCoordinate);
			return true;
		}
		else {
			AreaShapes.resetAffected();
		}
		
		return false;
	}

	/**
	 * On mouse dragged.
	 */
	@Override
	protected void onMouseDragged(MouseEvent e) {
			
		// If cursor selected.
		if (toolList.getSelected() == ToolId.CURSOR) {
			if (mousePosPressed != null) {
				
				setSelectionArea(mousePosPressed, e.getPoint());
				repaint();
			}
		}
		else if (toolList.getSelected() == ToolId.CONNECTOR) {
			onConnectorMove(e.getPoint());
		}
	}

	/**
	 * Set selection area.
	 * @param mousePosPressed2
	 * @param point
	 */
	private void setSelectionArea(Point point1, Point point2) {

		int vectorx = point2.x - point1.x,
		    vectory = point2.y - point1.y;
		
		if (vectorx > 0) {
			selectionRectangle.x = point1.x;
		}
		else {
			selectionRectangle.x = point2.x;
		}
		if (vectory > 0) {
			selectionRectangle.y = point1.y;
		}
		else {
			selectionRectangle.y = point2.y;
		}
		selectionRectangle.width = Math.abs(vectorx);
		selectionRectangle.height = Math.abs(vectory);
		
		// Set rectangle using current areas.
		Rectangle2D rectangle = new Rectangle2D.Double(
				(selectionRectangle.x - translationx) / zoom,
				(selectionRectangle.y - translationy) / zoom,
				selectionRectangle.width / zoom,
				selectionRectangle.height / zoom);

		// If the area is not small...
		if (!isSelectionRectSmall()) {
			// Remove selection.
			removeSelection();
			// Select affected areas.
			selectAffectedAreas(ProgramGenerator.getAreasModel().getRootArea(), rectangle);
		}
	}

	/**
	 * Remove selection area.
	 */
	private void removeSelectionRectangle() {

		selectionRectangle.width = 0;
		selectionRectangle.height = 0;
	}
	
	/**
	 * Selection rectangle is small.
	 */
	private boolean isSelectionRectSmall() {
		
		return selectionRectangle.height < minimumDrag && selectionRectangle.height < minimumDrag;
	}

	/**
	 * On mouse signalReleased.
	 */
	@Override
	protected void onMouseReleased(MouseEvent e, boolean onDiagramControl) {

		// Check button. BUTTON2 opens popup trayMenu.
		if (e.getButton() == MouseEvent.BUTTON2) {
			return;
		}
		
		if (!onDiagramControl) {
			
			// If cursor tool selected.
			if (toolList.getSelected() == ToolId.CURSOR && preselected != null) {
				
				if (isSelectionRectSmall()) { // For Drag and Drop purposes.
					
					// Convert mouse position to graph coordinates.
					Point2D graphPoint = doTransformation(preselected);
					
					// Get CTRL key pressed.
					boolean ctrlKeyPressed = e.isControlDown();
					
					// Propagate "on diagram areas clicked" event.
					ConditionalEvents.transmit(AreasDiagram.this, Signal.onClickDiagramAreas, graphPoint, ctrlKeyPressed);
				}
				else {
					
					// Propagate "on diagram areas drag end" event.
					ConditionalEvents.transmit(AreasDiagram.this, Signal.onDragDiagramAreas);
				}
			}
		}
		
		preselected = null;
		removeSelectionRectangle(); // The Drag and Drop rectangle.
		
		repaint();
	}

	/**
	 * On mouse pressed.
	 */
	@Override
	protected void onMousePressed(MouseEvent e) {

		// Check button. BUTTON2 uses popup trayMenu.
		if (e.getButton() == MouseEvent.BUTTON2) {
			return;
		}
		
		boolean flagSetOverview = false;
		boolean clearAffectedArea = true;
		Point mouse = e.getPoint();
		int button = e.getButton();
		
		// Get selected diagram tool.
		ToolId selectedToolId = toolList.getSelected();
		
		// If cursor tool selected.
		if (selectedToolId == ToolId.CURSOR) {
			
			Area area = getHelpArea(mouse);
			if (area != null) {
				onHelp(area);
			}
			else if (button == MouseEvent.BUTTON1) {
				preselected = mouse;
			}
		}
		// If area tool selected.
		else if (selectedToolId == ToolId.AREA) {
			
			// Hide constructors' names.
			hideConstructorsDisplay();
			
			// Hide new area.
			newAreaShape.setVisible(false);
			repaint();
			
			// Try to add new area.
			if (!addNewArea()) {
				// Show new area.
				newAreaShape.setVisible(true);
				clearAffectedArea = false;
			}
			else {
				// Dispose new area.
				newAreaShape = null;			
				// Select cursor tool.
				toolList.selectTool(0);
			}
			repaint();
		}
		// If the remove area tool is selected.
		else if (selectedToolId == ToolId.REMOVE) {
			
			// Remove area.
			removeArea();
			// Dispose new area.
			removeElementShape = null;
			// Select cursor tool.
			toolList.selectTool(0);
			repaint();
		}
		// If the connector tool is selected.
		else if (selectedToolId == ToolId.CONNECTOR) {
			
			clearAffectedArea = onConnectorSelection(e);
		}
		
		boolean flagRepaint = false;
		
		// Clear affected area.
		if (clearAffectedArea) {
			AreaShapes.resetAffected();
			flagRepaint = true;
		}
		
		if (flagSetOverview) {
			// Set overview.
			overview.setTranslation(getTranslatingX(), getTranslatingY());
			flagRepaint = true;
		}
		
		if (flagRepaint) {
			repaint();
		}
	}

	/**
	 * On help.
	 * @param mouse
	 */
	private void onHelp(Area area) {
		
		LinkedList<Area> list = new LinkedList<Area>();
		list.add(area);
		
		// Show help viewer.
		AreaHelpViewer.showDialog(this, list);
	}

	/**
	 * Is over help icon.
	 * @param mouse
	 * @return
	 */
	private Area getHelpArea(Point mouse) {

		// Do transformation.
		Point2D point = doTransformation(mouse);
		// Do loop for all areas.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			if (area.isHelp()) {
				Object user = area.getUser();
				if (user instanceof AreaShapes) {
					for (AreaCoordinates coord : ((AreaShapes) user).getCoordinates()) {
						
						// If the mouse coordinates are over the help icon...
						if (coord.isOverHelpIcon(point)) {
							return area;
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * On connector selection.
	 * @param e
	 * @return 
	 */
	private boolean onConnectorSelection(MouseEvent e) {

		if (areaShapeConnected == null) {
			
			// Get affected area shapes.
			areaShapeConnected = AreaShapes.getOneAffectedShape();
			if (areaShapeConnected != null) {
				Area affectedArea = areaShapeConnected.getArea();
				// If the area is global, inform user and do nothing.
				if (affectedArea.getId() == 0) {
					JOptionPane.showMessageDialog(this,
							Resources.getString("org.multipage.generator.messageCannotConnectGlobalArea"));
					// Select first tool.
					toolList.selectTool(0);
					// Reset connected area.
					areaShapeConnected = null;
					// Remove affected area and reset cursor.
					AreaShapes.resetAffected();
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					repaint();
				}
				else {
					// Show new area.
					newAreaShape = new AreaShapes(0, 0, 100,
							affectedArea, null, false, false);
					// Set the new area location.
					newAreaShape.setFirstLocation(e.getPoint());
					repaint();
				}
			}
			
			return true;
		}
		else {
			if (e.getClickCount() == 2) {
				// If the affected area is visible.
				if (AreaShapes.isAffectedAreaVisible()) {
					
					// Get affected area and connected area.
					Area parentArea = AreaShapes.getOneAffectedArea();
					Area subArea = areaShapeConnected.getArea();
					
					if (parentArea != null && subArea != null) {
						// Connect areas.
						connectAreas(parentArea, subArea);
					}
				}
				
				// Select first tool.
				toolList.selectTool(0);
				// Reset new area and connected area.
				newAreaShape = null;
				areaShapeConnected = null;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				repaint();
				
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * Ask user for new sub area edge parameters.
	 * @param parentArea
	 * @param subArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub
	 * @param parentComponent
	 * @return
	 */
	public static boolean askNewSubAreaEdge(Area parentArea, Area subArea,
			Obj<Boolean> inheritance, Obj<String> relationNameSub,
			Obj<String> relationNameSuper, Obj<Boolean> hideSub,
			Component parentComponent) {
		
		Middle middle = ProgramBasic.getMiddle();
		Properties loginProperties = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		// Load parent constructor tree.
		ConstructorGroup constructorGroup = new ConstructorGroup();
		result = middle.loadConstructorGroupForNewArea(loginProperties, parentArea.getId(), constructorGroup);
		if (result.isNotOK()) {
			
			result.show(parentComponent);
			return false;
		}
		
		// Declare selected constructor holder.
		ConstructorHolder existingConstructorHolder = null;
		
		// Get sub area constructor ID.
		Long subAreaConstructorHolderId = subArea.getConstructorHolderId();
		if (subAreaConstructorHolderId != null) {
			
			// Get parent constructor holder that matches the sub area constructor ID.
			existingConstructorHolder = constructorGroup.getConstructorHolder(subAreaConstructorHolderId);
		}
		
		// If there is no existing constructor holder...
		if (existingConstructorHolder == null) {
			// Ask user.
			if (!ConfirmAreasConnect.showConfirmDialog(Utility.findWindow(parentComponent),
					inheritance, relationNameSub, relationNameSuper, hideSub)) {
				return false;
			}
		}
		else {
			// Show confirm dialog.
			if (!Utility.askParam(parentComponent, "org.multipage.generator.messageConfirmConstructorOfArea", existingConstructorHolder.getName())) {
				return false;
			}
			
			// Set values from constructor holder.
			inheritance.ref = existingConstructorHolder.isInheritance();
			relationNameSub.ref = existingConstructorHolder.getSubRelationName();
			relationNameSuper.ref = existingConstructorHolder.getSuperRelationName();
			hideSub.ref = false;
		}
		
		return true;
	}

	/**
	 * Connect areas.
	 * @param parentArea
	 * @param subArea
	 */
	private void connectAreas(Area parentArea, Area subArea) {
		
		// Check if a cycle exists in the new areas diagram.
		if (existsCircle(parentArea, subArea)) {
			return;
		}
		
		// Ask user for new sub area edge.
		Obj<Boolean> inheritance = new Obj<Boolean>(true);
		Obj<String> relationNameSub = new Obj<String>();
		Obj<String> relationNameSuper = new Obj<String>();
		Obj<Boolean> hideSub = new Obj<Boolean>(true);
		
		boolean confirmed = askNewSubAreaEdge(parentArea, subArea, inheritance, relationNameSub, relationNameSuper, hideSub,
				GeneratorMainFrame.getFrame());
		if (!confirmed) {
			return;
		}
		
		// Connect parent area with existing sub area.
		Middle middle = ProgramBasic.getMiddle();
		Properties loginProperties = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.connectSimplyAreas(loginProperties, parentArea, subArea,
				inheritance.ref, relationNameSub.ref, relationNameSuper.ref, hideSub.ref,
				false);
		if (result != MiddleResult.OK) {
			result.show(this);
		}
		
		// Reload diagram.
		updateInformation();
	}

	/**
	 * Get selected areas.
	 * @return
	 */
	public LinkedList<Area> getSelectedAreas() {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		
		// Do loop for all areas.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shapes = (AreaShapes) user;
				
				// If the area is selected, add it to list.
				if (shapes.isSelected()) {
					areas.add(area);
				}
			}
			else if (user instanceof Boolean) {
				// If the area is selected, add it to list.
				if ((Boolean) user) {
					areas.add(area);
				}
			}
		}

		return areas;
	}
	
	/**
	 * Get selected area IDs.
	 * @return
	 */
	public HashSet<Long> getSelectedAreaIds() {
		
		HashSet<Long> areaIds = new HashSet<Long>();
		
		// Do loop for all areas.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			
			long areaId = area.getId();
			Object user = area.getUser();
			
			if (user instanceof AreaShapes) {
				AreaShapes shapes = (AreaShapes) user;
				
				// If the area is selected, add it to list.
				if (shapes.isSelected()) {
					areaIds.add(areaId);
				}
			}
			else if (user instanceof Boolean) {
				
				// If the area is selected, add it to list.
				if ((Boolean) user) {
					areaIds.add(areaId);
				}
			}
		}

		return areaIds;
	}

	/**
	 * Update information.
	 * @param source 
	 */
	public void updateInformation() {
		
		// Get selected areas.
		LinkedList<Area> selectedAreas = getSelectedAreas();
		// Reload diagram.
		reload(false, false);
		// Select areas.
		selectAreas(selectedAreas);
		// Sets overview.
		setOverview();
	}
	
	/**
	 * Render areas diagram.
	 */
	public static void renderDiagram() {
		
		// Get model object.
		AreasModel model = ProgramGenerator.getAreasModel();

		// Get root area height.
		int rootAreaHeight = (int) AreaCoordinates.getHeight((double) focusAreaShapeWidth);
		
		// Clear areas' user data with renedering and make new renedering of areas.
		model.clearAreasUserData();
		render(0, -(double) focusAreaShapeWidth / 2, -rootAreaHeight / 2, (double) focusAreaShapeWidth, null);
	}

	/**
	 * Reload areas.
	 */
	public void reload(boolean firstPass, boolean initTransformation) {

		renderDiagram();
		
		// Set diagram origin.
		if (initTransformation) {
			translationy = getHeight() / 2;
			translationx = ToolList.getWidth() + (getWidth() - ToolList.getWidth()) / 2;
		}
		
		// Update connected area shape.
		updateConnectedAreaShape();

		// Get minimal size.
		double size = getMinimalSize();
		// Compute ration.
		double ratio = (double) focusAreaShapeWidth / size;
		
		// Set old maximum.
		oldMaximumZoom = maximumZoom;
		
		// Set initial zoom.
		maximumZoom = ratio;
		if (zoomShape != null) {
			zoomShape.setMaximal(maximumZoom);
		}
		
		// Set zoom.
		if (initTransformation) {
			zoom = 1.0;
		}
		
		reduceZoom();
		
		// Set zoom shape.
		if (zoomShape != null) {
			zoomShape.setZoom(zoom);
		}
		
		setScrollBars();
		
		// Repaint diagram.
		repaint();
	}

	/**
	 * Update connected area shape.
	 */
	private void updateConnectedAreaShape() {
		
		if (areaShapeConnected != null) {
			
			Area area = areaShapeConnected.getArea();
			if (area != null) {
				
				area = ProgramGenerator.getAreasModel().getArea(area.getId());
				areaShapeConnected = (AreaShapes) area.getUser();
			}
		}
	}
	
	/**
	 * Initialization.
	 */
	public void init() {
		
		reload(true, setDefaultStateFlag);
		
		// Set zoom shape.
		zoomShape.setMinimal(minimumZoom);
		zoomShape.setPosition(getWidth() - ZoomShape.getWidth(), 0);
		zoomShape.setZoom(zoom);
		
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

		// Select cursor tool.
		toolList.selectTool(0);
		
		// Set overview window.
		setOverview();

		repaint();

		// Set diagram loaded flag.
		setLoaded();
		
		// Save focus.
		if (!setDefaultStateFlag) {
			saveFocus(getTranslatingX(), getTranslatingY(), getZoom());
		}
		
		// Focus global area first time.
		if (setDefaultStateFlag) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					focusBasicArea();
				}
			});
		}
	}
	
	/**
	 * Returns true value if the root area fills the whole diagram.
	 * @param transformation 
	 */
	public boolean isRootAreaFillDiagram() {
		
		// Get global area rectangle.
		Rectangle2D areaRectangle = getGlobalAreaSize();
		if (areaRectangle != null) {
			
			// Get diagram rectangle.
			Rectangle2D diagramRectangle = getRectInCoord();
			boolean isDiagramInsideGlobalArea = areaRectangle.contains(diagramRectangle);
			
			return isDiagramInsideGlobalArea;
		}
		
		return false;
	}
	
	/**
	 * On paint.
	 */
	@Override
	public void paint(Graphics g) {

		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
		// Set scale and translate.
		AffineTransform transformation = new AffineTransform();
		transformation.translate(translationx + deltax,
							translationy + deltay);
		double actualZoom = zoom * zoomMultiplier;
		transformation.scale(actualZoom, actualZoom);
		
		// Draw background.
		boolean drawText = !isRootAreaFillDiagram();
		drawBackground(g2, drawText);

		// Draw tool list.
		toolList.draw(g2);
		
		g2.setClip(ToolList.getWidth(), 0, getWidth() - ToolList.getWidth(), getHeight());

		// Draw root area body.
		AreaShapes.drawRootBody(g2, transformation);

		// Draw diagram.
		drawDiagram(g2, transformation, actualZoom, false);

		// Draw affected shapes.
		AreaShapes.drawAffectedShapes(g2, transformation, actualZoom);

		// Draw new area shape.
		if (newAreaShape != null) {
			newAreaShape.draw(g2, null, 1.0, this, true);
			
			// Draw possible connection.
			drawConnection(g2);
		}
		
		// Draw remove area shape.
		if (removeElementShape != null) {
			removeElementShape.draw(g2);
		}
		
		// Draw zoom.
		if (zoomShape != null) {
			zoomShape.draw(g2);
		}
		
		// Draw selection rectangle.
		if (preselected != null) {
			if (selectionRectangle.width + selectionRectangle.height != 0) {
				g2.setColor(CustomizedColors.get(ColorId.SELECTION));
				g2.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
				g2.fillRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
			}
		}
		
		// Draw horizontal and vertical scroll.
		g2.setClip(0, 0, getWidth(), getHeight());
		horizontalScroll.draw(g2, CustomizedColors.get(ColorId.SCROLLBARS));
		verticalScroll.draw(g2, CustomizedColors.get(ColorId.SCROLLBARS));
		
		// Draw overview button.
		overview.draw(g2);
	}

	/**
	 * Draw background.
	 * @param g2 
	 * @param drawText 
	 */
	private void drawBackground(Graphics2D g2, boolean drawText) {
		
		// Fill background.
		g2.setColor(CustomizedColors.get(ColorId.BACKGROUND));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		Rectangle panelRect = getVisibleRect();
		g2.fill(panelRect);
		
		final String text = Resources.getString("org.multipage.generator.textBackgroundText").trim();
		
		// Draw background text.
		if (drawText && !text.isEmpty()) {
			g2.setColor(CustomizedColors.get(ColorId.BACKGROUNDTEXT));
			
			FontMetrics fontMetrics = g2.getFontMetrics();
			int fontHeight = fontMetrics.getHeight();
			int stringWidth = fontMetrics.stringWidth(text);
			
			int xSpan = 80 * stringWidth / 46;
			int ySpan = 30 * fontHeight / 14;
	
			for (int x = 0; x < panelRect.width; x += xSpan) {
				
				int index = 0;
				for (int y = 0; y < panelRect.height; y += ySpan) {
					
					g2.drawString(text, x + (index % 2 == 0 ? 0 : xSpan / 2), y);
					
					index++;
				}
			}
		}
	}

	/**
	 * Draw connection.
	 * @param g2
	 */
	private void drawConnection(Graphics2D g2) {

		// If the area shape to connect with doesn't exist exit the method.
		if (areaShapeConnected == null || newAreaShape == null) {
			return;
		}
		if (!newAreaShape.isVisible()) {
			return;
		}
		
		// Get end point.
		Point2D endPoint2D = newAreaShape.firstLabelCenter();
		Point2D endPoint = new Point2D.Double(endPoint2D.getX(), endPoint2D.getY());
		
		// Get area read only flag value.
		Area connectedArea = areaShapeConnected.getArea();
		boolean isReadOnly = connectedArea.isReadOnly() && AreaShapes.readOnlyLighter;
		
		AlphaComposite halfOpaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		AlphaComposite opaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
		g2.setColor(CustomizedColors.get(isReadOnly ? ColorId.OUTLINES_PROTECTED : ColorId.OUTLINES));
		
		BasicStroke linesStroke = new BasicStroke(connectorWidth, BasicStroke.CAP_ROUND,
													BasicStroke.CAP_ROUND);
		BasicStroke circlesStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
													BasicStroke.CAP_ROUND);
		
		// Draw connections between new area and affected areas.
		for (AreaCoordinates coordinates : areaShapeConnected.getCoordinates()) {
			
			// The beginning point must be converted using current transformation.
			Point2D beginPoint2D = coordinates.getLabelCenter();
			Point2D beginPoint = undoTransformation(beginPoint2D);

			// Draw lines.
			g2.setStroke(linesStroke);
			g2.setComposite(halfOpaque);
			g2.draw(new Line2D.Double(beginPoint.getX(), beginPoint.getY(),
					endPoint.getX(), endPoint.getY()));
			
			// Draw circles.
			g2.setStroke(circlesStroke);
			g2.setComposite(opaque);
			g2.fill(new Ellipse2D.Double(beginPoint.getX() - connectorCircle / 2,
					beginPoint.getY() - connectorCircle / 2, connectorCircle, connectorCircle));
			g2.fill(new Ellipse2D.Double(endPoint.getX() - connectorCircle / 2,
					endPoint.getY() - connectorCircle / 2, connectorCircle, connectorCircle));
		}
	}

	/**
	 * On draw overview content.
	 * @param g2
	 */
	@Override
	protected void onDrawOverview(Graphics2D g2, AffineTransform transform) {

		// Draw root area body.
		AreaShapes.drawRootBody(g2, transform);
		// Draw graph.
		drawDiagram(g2, transform, zoom * overview.getZoom(), true);
	}

	/**
	 * Center diagram.
	 */
	@Override
	public void center() {
		
		focusBasicArea();
	}
	
	/**
	 * Get diagram rectangle in current coordinates.
	 * @return
	 */
	public Rectangle2D getRectInCoord() {
		
		// Compute current zoom.
		double currentZoom = zoom * zoomMultiplier;
		
		// Compute graph coordinates.
		double x = (ToolList.getWidth() - translationx - deltax) / currentZoom,
		       y = - (translationy + deltay) / currentZoom,
		       width = (getWidth() - ToolList.getWidth()) / currentZoom,
		       height = getHeight() / currentZoom;
		    
		return new Rectangle2D.Double(x, y, width, height);
	}

	/**
	 * Gets rectangle.
	 * @return
	 */
	@Override
	protected Rectangle2D getWindowRectangle() {

		return getRectInCoord();
	}

	/**
	 * Gets content rectangle.
	 * @return
	 */
	@Override
	protected Rectangle2D getContentRectangle() {

		return getGlobalAreaSize();
	}
	
	/**
	 * Render graph.
	 */
	public static void render(long id, double x, double y, double width, Area parentArea) {
	
		// Get area.
		Area area = ProgramGenerator.getAreasModel().getArea(id);
		
		if (area != null) {
			
			// Show / hide sub areas.
			boolean hideSubAreas = false;
			
			if (parentArea != null) {
				hideSubAreas = area.isHideSubUseSuper(parentArea.getId());
			}
			
			// Get child areas.
			LinkedList<Area> children = ProgramGenerator.getAreasModel().getAreaChildren(id);
			
			// Get children count.
			int count = children.size();
			
			// Has sub shapes.
			boolean showMoreInfo = count > 0 && hideSubAreas;
			
			// Is connection from super area reference.
			boolean isReference;
			
			if (parentArea != null) {
				isReference = parentArea.isRecursionUseSub(id);
			}
			else {
				isReference = false;
			}
			
			// Set area shape.
			Object user = area.getUser();
			if (user == null) {
				
				area.setUser(new AreaShapes(x, y, width, area, parentArea, showMoreInfo,
						isReference));
			}
			else {
				AreaShapes shape = (AreaShapes) user;
				shape.add(x, y, width, area, parentArea, showMoreInfo, isReference);
			}
			
			if (count > 0 && !hideSubAreas && !isReference) {
				
				// Compute matrix size for children.
				double sqrt = Math.sqrt((double) count);
				int xcount = (int) Math.ceil(sqrt);
				
				// Compute size of child areas.
				double height = AreaCoordinates.getHeight(width),
					   cHeight = height * AreaCoordinates.areaFreeZonePercent / 100,
				       rWidth = width * AreaCoordinates.areaFreeZonePercent / 100,
				       lWidth = width - rWidth,
				       saXSpace = lWidth / xcount * subareaBorderPercent / 100,
				       saWidth = lWidth / xcount - saXSpace - saXSpace / xcount,
				       saHeight = AreaCoordinates.getHeight(saWidth),
				       saYSpace = AreaCoordinates.getHeight(saXSpace);
				
				int xindex = 0,
				    yindex = 0;
				
				final boolean isExtensionToBuilder = ProgramGenerator.isExtensionToBuilder();
				
				// Render sub areas.
				for (Area subarea : children) {
					
					if (!isExtensionToBuilder && area.isSubareasHidden(subarea)) {
						continue;
					}
					
					double newX = x + saXSpace + xindex * (saWidth + saXSpace),
					       newY = y + saYSpace + cHeight + yindex * (saHeight + saYSpace),
					       newSize = saWidth;
					
					render(subarea.getId(), newX, newY, newSize, area);
					
					// Set position in matrix.
					xindex ++;
					if (xindex >= xcount) {
						yindex++;
						xindex = 0;
					}
				}
			}
		}
	}

	/**
	 * Draw graph.
	 * @param transformation 
	 * @param overview 
	 */
	public void drawDiagram(Graphics2D g2, AffineTransform transformation,
			double actualZoom, boolean overview) {
	
		// Draw all nodes.
		AreasModel model = ProgramGenerator.getAreasModel();
		synchronized (model) {
			
			for (Area area : model.getAreas()) {
				
				Object user = area.getUser();
				if (user instanceof AreaShapes) {
					AreaShapes shapes = (AreaShapes) user;
					shapes.draw(g2, transformation, actualZoom, this, overview);
				}		
			}
		}
	}

	/**
	 * Get minimal size.
	 */
	public double getMinimalSize() {
		
		double minimal = 0.0;
		
		// Do loop for nodes.
		for (Area node : ProgramGenerator.getAreasModel().getAreas()) {
			Object user = node.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				double current = shape.getMinimalSize();
				if (minimal == 0.0) {
					minimal = current;
				}
				if (current < minimal) {
					minimal = current;
				}
			}
		}
		return minimal;
	}

	/**
	 * Multiple graph.
	 */
	public void multiple(double multiple) {

		// Loop for all nodes.
		for (Area node : ProgramGenerator.getAreasModel().getAreas()) {
			Object user = node.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				shape.multiply(multiple);
			}
		}
	}

	/**
	 * Try to select area and its sub areas. The method doesn't repaint the GUI.
	 */
	public void select(Point2D graphPoint, boolean reset) {

		lastSelectedAreaCoordinates = null;
		
		Area affectedArea = null;
		boolean captionHit = false;
		
		// Find matching area.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				
				AreaCoordinates areaCoordinates = shape.isCaptionHit(graphPoint);
				if (areaCoordinates != null) {
					
					// Set selected area.
					lastSelectedAreaCoordinates = areaCoordinates;
					
					affectedArea = area;
					captionHit = true;
					break;
				}
				
				areaCoordinates = shape.isAreaFreeHit(graphPoint);
				if (areaCoordinates != null) {
					
					// Set selected area.
					lastSelectedAreaCoordinates = areaCoordinates;
					
					affectedArea = area;
					captionHit = false;
					break;
				} 
			}
		}
		
		// Select area and possibly sub areas (depending on captionHit flag).
		if (affectedArea != null) {
			boolean select = true;
			Object user = affectedArea.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shapes = (AreaShapes) user;
				
				select = !shapes.isSelected();
				
				// Remove selection.
				if (reset) {
					select(ProgramGenerator.getAreasModel().getArea(0), false, true);
				}
			}
			select(affectedArea, select, captionHit);
		}
		else {
			// Unselect all.
			select(ProgramGenerator.getAreasModel().getRootArea(), false, true);
		}
	}
	
	/**
	 * Select area and its sub areas.
	 * @param area
	 * @param selected
	 * @param affectSubareas 
	 */
	public void select(Area area, boolean selected, boolean affectSubareas) {
		
		this.affectSubareas = affectSubareas;
		
		selectRecursive(area, selected, affectSubareas);
	}
	
	/**
	 * Select area and its sub areas (recursion).
	 * @param area
	 * @param selected
	 * @param affectSubareas 
	 */
	private void selectRecursive(Area area, boolean selected, boolean affectSubareas) {
		
		if (area != null) {
			
			long areaId = area.getId();
			
			// Select area.
			AreaShapes.select(area, selected);

			// Select all sub areas.
			LinkedList<Area> list = ProgramGenerator.getAreasModel().getAreaChildren(areaId);
			
			if (affectSubareas) {
				for (Area child : list) {
					
					long childId = child.getId();
					
					// Do not select hidden area shapes.
					if (area.isHideSubUseSub(childId) && selected) {
						
						AreaShapes.select(child, selected);
						continue;
					}
					
					selectRecursive(child, selected, true);
				}
			}
		}
	}

	/**
	 * Select area.
	 * @param id
	 * @param selected
	 * @param affectSubareas
	 */
	public void select(long id, boolean selected, boolean affectSubareas) {

		// Get area.
		Area area = ProgramGenerator.getAreasModel().getArea(id);
		select(area, selected, affectSubareas);
	}

	/**
	 * Set affected area.
	 * @param area
	 * @param rectangle
	 */
	public void selectAffectedAreas(Area area, Rectangle2D rectangle) {

		if (area != null) {
			// Get area shape.
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				if (shape.isInside(rectangle)) {
					select(area, true, true);
				}
				// If this area not contained, check sub areas.
				else {
					for (Area child : ProgramGenerator.getAreasModel().getAreaChildren(area.getId())) {
						selectAffectedAreas(child, rectangle);
					}
				}
			}
		}
	}

	/**
	 * Removes selection.
	 */
	public void removeSelection() {

		select(ProgramGenerator.getAreasModel().getRootArea(), false, true);
	}

	/**
	 * Select area.
	 * @param areaId
	 */
	public void selectArea(long areaId, boolean reset) {
		
		// Run this method only once.
		if (selectAreaInvoked) {
			return;
		}
		
		if (reset) {
			removeSelection();
		}
		
		Area area = ProgramGenerator.getAreasModel().getArea(areaId);
		
		selectAreaInvoked = true;
		select(area, true, false);
		
		// Sets overview.
		setOverview();
		// Update area trace dialogs.
		AreaTraceFrame.updateInformation();
		selectAreaInvoked = false;
	}

	/**
	 * Select area with subareas.
	 * @param areaId
	 */
	public void selectAreaWithSubareas(long areaId, boolean reset) {
		
		if (reset) {
			removeSelection();
		}
		
		Area area = ProgramGenerator.getAreasModel().getArea(areaId);
		select(area, true, true);
		updateInformation();
	}
	
	/**
	 * Get size of global area.
	 * @return
	 */
	public Rectangle2D getGlobalAreaSize() {

		Area node = ProgramGenerator.getAreasModel().getRootArea();
		if (node != null) {
			Object user = node.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				return shape.getFirstRectangle();
			}
		}
		return null;
	}

	/**
	 * Set overview.
	 */
	public void setOverview() {

		Area root = ProgramGenerator.getAreasModel().getRootArea();
		if (root != null) {
			Object user = root.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shape = (AreaShapes) user;
				
				Rectangle2D resultRectangle = new Rectangle2D.Double(shape.getFirstX(),
						shape.getFirstY(),
						shape.getFirstWidth(),
						shape.getFirstHeight());
				// Set overview.
				overview.setDiagramRectangle(resultRectangle);
			}
		}

		// Set overview.
		overview.setScale(zoom);
		overview.setTranslation(getTranslatingX(), getTranslatingY());
	}

	/**
	 * On tool tip.
	 */
	@Override
	protected boolean onToolTip() {
		
		boolean isSet = false;
		
		// Get mouse position and convert it to current window coordinates.
		Point screenMouse = MouseInfo.getPointerInfo().getLocation();
		Point windowMouse = (Point) screenMouse.clone();
		SwingUtilities.convertPointFromScreen(windowMouse, this);
		
		Rectangle window = getRectangle();

		// If the mouse is not in the window, exit this method.
		if (!window.contains(windowMouse)) {
			return false;
		}
		
		// If the mouse is in vertical or horizontal scroll bar
		// or overview or the zoom, exit this method.
		if (horizontalScroll.contains(windowMouse)
			|| verticalScroll.contains(windowMouse)
			|| overview.contains(windowMouse)
			|| zoomShape.contains(windowMouse)) {
			
			return false;
		}
		
		if (tooltipWindow != null) {
			// Do loop for all areas.
			for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
				Object user = area.getUser();
				if (user instanceof AreaShapes) {
					AreaShapes shapes = (AreaShapes) user;
					// Do loop for all shapes.
					for (AreaCoordinates coordinate : shapes.getCoordinates())
					{
						// Get area caption rectangle and do transformation.
						Rectangle2D areaCaptionRect2D = coordinate.getCaptionRect();
						Rectangle2D areaCaptionRect = undoTransformation(areaCaptionRect2D);
						
						// If the caption contains mouse, show tool tip.
						if (areaCaptionRect.contains(windowMouse)) {
							
							screenMouse.y += tooltipVerticalShift;
							String toolTipMessage = String.format(
									Resources.getString("org.multipage.generator.textAreaToolTip"), area.toString(),
									area.getAlias(), area.getSlotAliasesCount());
							tooltipWindow.showw(screenMouse, toolTipMessage);
	
							isSet = true;
						}
					}
				}
			}
		}
		return isSet;
	}

	/**
	 * Gets diagram window rectangle.
	 * @return
	 */
	private Rectangle getRectangle() {

		return new Rectangle(ToolList.getWidth(), 0, getWidth() - ToolList.getWidth(),
				getHeight());
	}
	
	/**
	 * Add new area.
	 * @return
	 */
	protected boolean addNewArea() {
		
		// Get affected area.
		Area parentArea = AreaShapes.getOneAffectedArea();
		return addNewArea(parentArea, this, null, true);
	}
	
	/**
	 * Add new area to existing parent area.
	 */
	@SuppressWarnings("incomplete-switch")
	public boolean addNewArea(Area parentArea, Component parentComponent, Obj<Area> newArea,
			boolean selectAndFocus) {
		
		// Check affected area.
		if (parentArea == null) {
			JOptionPane.showMessageDialog(parentComponent,
					Resources.getString("org.multipage.generator.messageNoAreaAffected"));
			return false;
		}
		
		// If the area is hidden, exit the method.
		Area parentParentArea = AreaShapes.getAffectedParentArea();
		boolean isHidden = parentParentArea != null ? parentParentArea.isHideSubUseSub(parentArea.getId()) : false;
		
		if (isHidden) {
			
			Utility.show(parentComponent, "org.multipage.generator.messageCannotAddAreaToHiddenAreas");
			return false;
		}
		
		// Create new area
		final Area newAreaAdded = new Area(0, "", true, "");
		
		if (newArea != null) {
			newArea.ref = newAreaAdded;
		}
		
		// Get prerequisites.
		final Middle middle = ProgramBasic.getMiddle();
		final Properties login = ProgramBasic.getLoginProperties();
		
		
		// Load constructor group for new area.
		ConstructorGroup constructorGroup = new ConstructorGroup();
		MiddleResult result = middle.loadConstructorGroupForNewArea(login, parentArea.getId(), constructorGroup);
		if (result.isNotOK()) {
			
			result.show(parentComponent);
			// Do not exit the method.
		}
		
		boolean addAreaConservatively = true;
		
		// Initialize success flag.
		boolean success = false;
		
		// If the constructor group is not empty, run wizard.
		if (!constructorGroup.isEmpty()) {
			
			addAreaConservatively = false;
			
			// Define wizard graph nodes.
			final int SELECT_CONSTRUCTOR_HOLDER = 0;
			final int GET_AREA_NAME_FILE_AND_FOLDER = 1;
			final int SET_RELATED_AREA = 2;
			final int SET_RESOURCES_REFERENCES = 3;
			final int EXIT_WIZARD = 4;
			
			// Set initial wizard state.
			int wizardState = SELECT_CONSTRUCTOR_HOLDER;
			
			// Current selected constructor holder.
			Obj<ConstructorHolder> selectedConstructorHolder = new Obj<ConstructorHolder>();
			
			// Area file and folder.
			Obj<String> areaDescription = new Obj<String>("");
			Obj<String> subName = new Obj<String>("");
			Obj<String> areaFile = new Obj<String>("");
			Obj<String> areaFolder = new Obj<String>("");
			
			// Constructor area, related area and area resources.
			Area selectedConstructorArea = null;
			Obj<Area> selectedRelatedArea = new Obj<Area>();
			LinkedList<AreaResource> areaResources = null;
			final Obj<LinkedList<ResourceConstructor>> resourceConstructors = new Obj<LinkedList<ResourceConstructor>>(null);
			
			boolean existEditableResource = false;
			boolean isAskForRelatedArea = false;
			
			// Reset wizard states.
			SelectConstructorDialog.resetFilter();
			
			// Do wizard loop.
			while (wizardState != EXIT_WIZARD) {

				// Returned value.
				WizardReturned returned = WizardReturned.UNKNOWN;
			
				// On select constructor holder.
				if (wizardState == SELECT_CONSTRUCTOR_HOLDER) {
	
					// Select constructor.
					returned = SelectConstructorDialog.showDialog(parentComponent,
							constructorGroup.getConstructorHolders(), selectedConstructorHolder);
					
					// On next step...
					if (returned == WizardReturned.NEXT) {
						
						// Set wait cursor because loading resource list may be time consuming.
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						
						// Get selected constructor area.
						long selectedConstructorAreaId = selectedConstructorHolder.ref.getAreaId();
						selectedConstructorArea = ProgramGenerator.getArea(selectedConstructorAreaId);
						
						if (selectedConstructorArea == null) {
							Utility.show(parentComponent, "org.multipage.generator.messageCannotFindConstructorArea");
							break;
						}
						
						// Set related area.
						selectedRelatedArea.ref = selectedConstructorArea.getRelatedArea();
						
						// Load constructor area resource list.
						areaResources = new LinkedList<AreaResource>();
						result = middle.loadAreaResources(login, selectedConstructorArea, areaResources, null);
						if (result.isNotOK()) {
							
							result.show(parentComponent);
							break;
						}

						// Get resource constructors list.
						resourceConstructors.ref = new LinkedList<ResourceConstructor>();
						for (AreaResource areaResource : areaResources) {
							
							// Create resource constructor and add it to the list.
							ResourceConstructor resourceConstructor = new ResourceConstructor(areaResource);
							
							if (areaResource.getDescription().equals("empty")) {
								resourceConstructor.setEditable(true);
							}
							else {
								resourceConstructor.setEditable(false);
								// Set link to resource.
								resourceConstructor.setLoadInfo(areaResource);
							}
							
							resourceConstructors.ref.add(resourceConstructor);
						}
						
						// Set flag.
						isAskForRelatedArea = selectedConstructorHolder.ref.isAskForRelatedArea();
						existEditableResource = ResourceConstructor.existEditableResource(resourceConstructors.ref);

						// Set new state.
						wizardState = GET_AREA_NAME_FILE_AND_FOLDER;
						
						// Restore cursor.
						setCursor(Cursor.getDefaultCursor());
					}
				}
				// On area file and folder input.
				else if (wizardState == GET_AREA_NAME_FILE_AND_FOLDER) {

					areaDescription.ref = selectedConstructorArea.getDescription();
					boolean isLastDialog = !existEditableResource && !isAskForRelatedArea;

					subName.ref = selectedConstructorHolder.ref.getSubRelationName();
					
					// Display area dialog.
					returned = AreaNameFileFolderDialog.showDialog(parentComponent, parentArea, isLastDialog,
							areaDescription, subName, areaFile, areaFolder);
					
					switch (returned) {
					case PREVIOUS:
						wizardState = SELECT_CONSTRUCTOR_HOLDER;
						break;
					case NEXT:
						wizardState = isLastDialog ? EXIT_WIZARD
								: (isAskForRelatedArea ? SET_RELATED_AREA : SET_RESOURCES_REFERENCES);
						break;
					}
				}
				// On set related area.
				else if (wizardState == SET_RELATED_AREA) {
					
					Area relatedArea = selectedConstructorArea.getRelatedArea();
					boolean isLastDialog = !existEditableResource;

					if (isAskForRelatedArea) {
						// Ask for related area.
						returned = RelatedAreaConstructorDialog.showDialog(parentComponent, isLastDialog, 
							relatedArea, selectedRelatedArea);
						
						switch (returned) {
						case PREVIOUS:
							wizardState = GET_AREA_NAME_FILE_AND_FOLDER;
							break;
						case NEXT:
							wizardState = isLastDialog ? EXIT_WIZARD : SET_RESOURCES_REFERENCES;
							break;
						}
					}
					else {
						wizardState = EXIT_WIZARD;
					}
				}
				// On set resources references.
				else if (wizardState == SET_RESOURCES_REFERENCES) {

					if (ResourceConstructor.existEditableResource(resourceConstructors.ref)) {
						returned = SetResourcesLoadInfoDialog.showDialog(parentComponent, resourceConstructors.ref);
						
						switch (returned) {
						case PREVIOUS:
							wizardState = isAskForRelatedArea ? SET_RELATED_AREA : GET_AREA_NAME_FILE_AND_FOLDER;
							break;
						case OK:
							wizardState = EXIT_WIZARD;
							break;
						}
					}
					else {
						wizardState = EXIT_WIZARD;
					}
				}
				
				// Exit method on cancel.
				if (returned == WizardReturned.CANCEL) {
					return false;
				}
				
				// Skip wizard.
				if (returned == WizardReturned.SKIP) {
					
					addAreaConservatively = true;
					break;
				}
			}
			
			if (!addAreaConservatively) {
				
				// Trim properties.
				if (subName.ref.isEmpty()) {
					subName.ref = null;
				}
				if (areaFile.ref.isEmpty()) {
					areaFile.ref = null;
				}
				if (areaFolder.ref.isEmpty()) {
					areaFolder.ref = null;
				}
				
				// Check constructor area.
				if (selectedConstructorArea == null) {
					Utility.show(parentComponent, "org.multipage.generator.messageConstructorAreaNotFound");
					return false;
				}
				
				// Try to insert new area.
				newAreaAdded.setDescription(areaDescription.ref);
				newAreaAdded.setFileName(areaFile.ref);
				newAreaAdded.setFolder(areaFolder.ref);
				newAreaAdded.setLocalized(selectedConstructorArea.isLocalized());
				newAreaAdded.setVisible(selectedConstructorArea.isVisible());
				newAreaAdded.setReadOnly(false);
				newAreaAdded.setRelatedArea(selectedRelatedArea.ref);
				newAreaAdded.setFileExtension(selectedConstructorArea.getFileExtension());
				newAreaAdded.setCanImport(selectedConstructorArea.canImport());
				newAreaAdded.setProjectRoot(selectedConstructorArea.isProjectRoot());
				
				// Try to add new area.
				result = middle.insertArea(
							login,
							parentArea, newAreaAdded, selectedConstructorHolder.ref.isInheritance(),
							subName.ref,
							selectedConstructorHolder.ref.getSuperRelationName());

				// On error inform user.
				if (result != MiddleResult.OK) {
					result.show(parentComponent);
					return false;
				}
				
				// If the new area is visible, set it as a home area.
				if (selectedConstructorHolder.ref.isSetHome()) {
					
					result = middle.setStartArea(login, newAreaAdded.getId());
					
					// On error inform user.
					if (result.isNotOK()) {
						result.show(parentComponent);
					}
				}
				
				// Set sub area constructor holder ID.
				if (selectedConstructorHolder.ref != null) {
					
					long subConstructorHolderId = selectedConstructorHolder.ref.getId();
				
					// Update new area constructor holder ID.
					result = middle.updateAreaConstructorHolder(login, newAreaAdded.getId(),
							subConstructorHolderId);
					
					// On error inform user.
					if (result != MiddleResult.OK) {
						result.show(parentComponent);
						return false;
					}
				}
				
				// Load constructor area slots.
				result = middle.loadAreaSlots(login, selectedConstructorArea);
				if (result != MiddleResult.OK) {
					result.show(parentComponent);
					return false;
				}
				
				// Save constructor area slots.
				result = middle.insertAreaSlots(login, newAreaAdded, selectedConstructorArea.getSlots());
				
				if (result != MiddleResult.OK) {
					result.show(parentComponent);
					return false;
				}
				
				// Get resources list reference.				
				if (!resourceConstructors.ref.isEmpty()) {
				
					// Import constructor resources.
					final Progress2Dialog<MiddleResult> dialog = new Progress2Dialog<MiddleResult>(parentComponent, 
							Resources.getString("org.multipage.generator.textResourceLoadProgress"),
							Resources.getString("org.multipage.generator.textLoadingResourceFromFile"));
					
					// Execute progress dialog.
					ProgressResult progressResult = dialog.execute(new SwingWorkerHelper<MiddleResult>() {
						@Override
						protected MiddleResult doBackgroundProcess()
								throws Exception {
							
							// Get resources count.
							int resourcesCount = resourceConstructors.ref.size();
							
							// Set main progress bar step.
							double progressBarStep = 100.0 / (double) resourcesCount;
							double progressBarValue = progressBarStep;
							
							// Load resources.
							for (ResourceConstructor constructorResource : resourceConstructors.ref) {
								
								// Set main progress bar.
								setProgressBar((int) Math.ceil(progressBarValue));
								progressBarValue += progressBarStep;
								
								// Create new resource.
								AreaResource resource = new AreaResource();
								resource.setLocalDescription(constructorResource.getDescription());
								resource.setVisible(constructorResource.getVisibility());
								
								// Get load info.
								ResourceConstructor.LoadInfo loadInfo = constructorResource.getLoadInfo();
								
								MiddleResult result = MiddleResult.UNKNOWN_ERROR;
								
								if (loadInfo instanceof ResourceConstructor.LinkLoadInfo) {
									
									// On resource link.
									ResourceConstructor.LinkLoadInfo linkLoadInfo = (ResourceConstructor.LinkLoadInfo) loadInfo;
									
									// Insert resource link.
									result = middle.insertResourceLinkToArea(login, newAreaAdded.getId(),
											linkLoadInfo.resource.getId(), constructorResource.getDescription());
								}
								else if (loadInfo instanceof ResourceConstructor.FileLoadInfo) {
									
									// On resource file.
									ResourceConstructor.FileLoadInfo fileLoadInfo = (ResourceConstructor.FileLoadInfo) loadInfo;
									File file = fileLoadInfo.file;

									// Get saving method.
									Obj<Boolean> saveAsText = new Obj<Boolean>(constructorResource.isSaveAsText());
									Obj<String> encoding = new Obj<String>("UTF8");
									if (saveAsText.ref) {
										
										SelectResourceSavingMethod.showDialog(dialog, file, saveAsText, encoding);
									}
									// Insert file.
									result = middle.insertResourceFileToArea(login, newAreaAdded.getId(),
											file, saveAsText.ref, encoding.ref, constructorResource.getDescription(),
											constructorResource.getVisibility(), this);
								}
								else {
									
									// On empty resource.
									
									// Insert empty area resource.
									result = middle.insertResourceEmptyToArea(login, newAreaAdded.getId(), constructorResource.isSaveAsText(),
											constructorResource.getDescription(), false);
								}
								
								// On error inform user and exit.
								if (result != MiddleResult.OK) {
									return result;
								}
							}
							return MiddleResult.OK;
						}
					});
					
					// Create possible area sources.
					if (selectedConstructorArea.isStartArea()) {
						
						long newAreaId = newAreaAdded.getId();
						
						// Create area sources.
						result = middle.insertAreaSources(login, newAreaId, selectedConstructorArea.getAreaSourcesCollection());
						if (result.isNotOK()) {
							result.show(parentComponent);
						}
						
						// If an old style start resource exists, set constructed area start resource.
						long constructorAreaId = selectedConstructorArea.getId();
						
						result = middle.updateAreaStartResourceFromConstructorArea(login, newAreaId, constructorAreaId);
						if (result.isNotOK()) {
							result.show(parentComponent);
						}
					}
					
					// Process progress result.
					if (progressResult == ProgressResult.OK) {
						
						// On middle layer error inform user.
						result = dialog.getOutput();
						if (result.isNotOK()) {
							result.show(parentComponent);
						}
					}
				}
				
				// Reload diagram.
				updateInformation();
				
				if (selectAndFocus) {
					// Select the new area.
					selectArea(newAreaAdded.getId(), true);
				}
			}
			
			// Set success flag.
			success = true;
		}
		
		// Add new area conservatively.
		if (addAreaConservatively) {
		
			success = addNewAreaConservatively(parentArea, newAreaAdded, parentComponent);
		}
		
		// Focus parent area.
		if (selectAndFocus) {
			GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusAreaNear(parentArea.getId());
		}
		
		// Propagate update all event.
		ConditionalEvents.transmit(this, Signal.updateAll);
		
		return success;
	}

	/**
	 * Add new area conservatively.
	 * @param parentComponent 
	 * @return
	 */
	protected boolean addNewAreaConservatively(Area parentArea, Area newArea, Component parentComponent) {
		
		// Override this method.
		
		// Inform user.
		Utility.show(parentComponent, "org.multipage.generator.messageCannotAddNewAreaToThisArea", parentArea.getDescriptionForDiagram());
		return false;
	}
	
	/**
	 * Remove existing area.
	 */
	private void removeArea() {
		
		// Hide remove area shape.
		setRemoveShapeVisibility(false);
		repaint();
		
		HashSet<AreaShapes> affectedAreaShapes = AreaShapes.getAffectedAreaShapes();
		Area affectedParentArea = AreaShapes.getAffectedParentArea();
		
		removeDiagramArea(affectedAreaShapes, affectedParentArea, this);
		
		// Show remove area shape.
		setRemoveShapeVisibility(true);
		repaint();
		
	}
	
	/**
	 * Remove existing diagram area.
	 */
	public void removeDiagramArea(HashSet<AreaShapes> affectedAreaShapes, Area affectedParentArea, Component parentComponent) {
		
		// Areas deletion dialog.
		AreasDeletionDialog dialog = newAreasDeletionDialog(affectedAreaShapes,
				affectedParentArea, parentComponent);
		dialog.setVisible(true);
		
		// Reload area diagram.
		updateInformation();
		
		// Propagate update all event.
		ConditionalEvents.transmit(this, Signal.updateAll);
	}

	/**
	 * Create new areas deletion dialog.
	 * @param topAreas
	 * @param parentArea
	 * @param parentComponent 
	 */
	protected AreasDeletionDialog newAreasDeletionDialog(HashSet topAreas,
			Area parentArea, Component parentComponent) {
		
		return new AreasDeletionDialog(parentComponent, topAreas, parentArea);
	}
	
	/**
	 * Returns true if a circle exists in the new diagram.
	 * @param parentArea
	 * @param referencedArea
	 * @return
	 */
	public static boolean existsCircle(Area parentArea, Area referencedArea) {
		
		// If the referenced area is the global area return true.
		if (referencedArea.getId() == 0) {
			return true;
		}

		// If the referenced area is the same as parent area, return true value.
		if (referencedArea.equals(parentArea)) {
			return true;
		}

		// If the referenced area is already in the parent area
		// exit the method with true.
		if (parentArea.existsSubarea(referencedArea)) {
			return true;
		}
		
		// Add the reference temporarily to the list of sub areas of the
		// parent area  and check if a cycle exists.
		parentArea.addSubarea(referencedArea, false, null, null, false, false);
		
		boolean existsCircle = ProgramGenerator.getAreasModel().existsCircleInAreas();
		
		// Remove sub area.
		parentArea.removeSubarea(referencedArea);
				
		return existsCircle;
	}

	/**
	 * Focus to coordinates.
	 * @param coordinates
	 * @param area 
	 */
	public void focus(AreaCoordinates coordinates, Area area) {
		
		// If to affect the area...
		if (area != null) {
			AreaShapes.setAffected(area);
		}
		
		// Compute new zoom and possibly reduce it.
		double newZoom = (double) focusAreaShapeWidth / coordinates.getWidth();
		newZoom = reduceZoom(newZoom);
		
		if (isDiagramPositionChanged) {
			saveFocus(getTranslatingX(), getTranslatingY(), getZoom());
			isDiagramPositionChanged = false;
		}
		
		double newTranslationX = focusAreaShapeLocation + ToolList.getWidth()
				- newZoom * coordinates.getX();
		double newTranslationY = focusAreaShapeLocation - newZoom * coordinates.getY();
		
		// Animation flag.
		boolean animate = false;
		
		if (!isAnimationRunning()) {
			
			final double animatedChangeTreshold = 0.01; 

			// Do not focus if the position change is too small.
			
			// Compute relative zoom change.
			double zoomRelativeChange = Math.abs(1.0 - newZoom / zoom);
			
			if (zoomRelativeChange > animatedChangeTreshold) {
				animate = true;
			}
			
			// Compute relative translation.
			double translXRelativeChange = Math.abs(newTranslationX / newZoom - translationx / zoom);
			double translYRelativeChange = Math.abs(newTranslationY / newZoom - translationy / zoom);
			
			if (translXRelativeChange > animatedChangeTreshold) {
				animate = true;
			}
			if (translYRelativeChange > animatedChangeTreshold) {
				animate = true;
			}
		}
		else {
			animate = true;
		}
		
		// Process not animate next focus flag.
		if (notAnimateNextFocus) {
			animate = false;
			notAnimateNextFocus = false;
		}

		// Save coordinates.
		saveFocus(newTranslationX, newTranslationY, newZoom);
		if (animate) {
	
			// Animate.
			animate(newTranslationX, newTranslationY, newZoom);
		}
		else {
			// Set new values (do not animate).
			finishAnimation();
			
			zoom = newZoom;
			translationx = newTranslationX;
			translationy = newTranslationY;
			
			repaint();
		}
	}

	/**
	 * On escape.
	 */
	@Override
	protected void onEscape() {
		
		// Hide constructors' names.
		hideConstructorsDisplay();
	}
	
	/**
	 * Escape diagram modes.
	 */
	public void escapeDiagramModes() {
		
		// Reset affected area shapes.
		AreaShapes.resetAffected();
		newAreaShape = null;
		removeElementShape = null;
		areaShapeConnected = null;
		mousePosPressed = null;
		translationx += deltax;
		translationy += deltay;
		deltax = deltay = 0;
		preselected = null;
		removeSelectionRectangle();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		toolList.selectTool(0);
	}

	/**
	 * Select all.
	 * @param select 
	 */
	public void setAllSelection(boolean select) {

		Area globalArea = ProgramGenerator.getAreasModel().getRootArea();
		select(globalArea, select, true);
	}

	/**
	 * Focus global area.
	 */
	public void focusBasicArea() {

		Area global = ProgramGenerator.getAreasModel().getRootArea();
		if (global != null) {
			
			Object user = global.getUser();
			if (user != null && user instanceof AreaShapes) {
			
				AreaShapes shapes = (AreaShapes) user;
				AreaCoordinates coordinates = shapes.getFirstCoordinates();
				
				if (coordinates != null) {
					
					// Animate focus.
					focus(coordinates, global);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see program.builder.GeneralDiagram#onSelectTool(program.builder.ToolId)
	 */
	@Override
	protected void onSelectTool(ToolId toolId, ToolId oldSelection) {
		
		// If area tool selected.
		if (toolId == ToolId.AREA) {
			newAreaShape = new AreaShapes(0, 0, newAreaWidth,
					new Area(0, Resources.getString("org.multipage.generator.textNewArea"), true, "", false),
					null, false, false);
			newAreaShape.setVisible(false);
		}
		// If the area tool is not selected, release new area shape.
		else {
			newAreaShape = null;
		}
		// If remove area tool selected.
		if (toolId == ToolId.REMOVE) {
			removeElementShape = new RemoveElementShape();
			removeElementShape.setVisible(false);
		}
		else {
			removeElementShape = null;
		}
		// If the connector tool is selected.
		if (toolId == ToolId.CONNECTOR) {

		}
		else {
			if (toolId != ToolId.AREA) {
				newAreaShape = null;
			}
			areaShapeConnected = null;
		}
		
		// If the connector tool was selected...
		if (oldSelection == ToolId.CONNECTOR) {
			
			if (toolId == ToolId.CONNECTOR) {
				onEscape();
			}
		}
	}

	/**
	 * Set selected areas.
	 * @param areas
	 */
	private void selectAreas(LinkedList<Area> areas) {

		// Do loop for given areas.
		for (Area area : areas) {
			// Get loaded area.
			Area loadedArea = ProgramGenerator.getArea(area.getId());
			select(loadedArea, true, false);
		}
	}
	
	/**
	 * Set selected areas.
	 * @param selectedAreaIds
	 */
	private void selectAreas(HashSet<Long> selectedAreaIds) {
		
		selectedAreaIds.forEach(areaId -> {
			
			Area area = ProgramGenerator.getArea(areaId);
			select(area, true, false);
		});
	}

	/**
	 * On set zoom.
	 */
	@Override
	protected void setZoom() {
		
		zoomShape.setZoom(zoom * zoomMultiplier);
	}

	/**
	 * Dispose dialog.
	 */
	public void dispose() {

		saveDialog();
	}

	/**
	 * Reset diagram.
	 */
	public void resetDiagramPosition() {
		
		Area global = ProgramGenerator.getAreasModel().getRootArea();
		if (global != null) {
			
			Object user = global.getUser();
			if (user != null && user instanceof AreaShapes) {
			
				AreaShapes shapes = (AreaShapes) user;
				AreaCoordinates coordinates = shapes.getFirstCoordinates();
				
				if (coordinates != null) {
					
					// Compute new zoom and possibly reduce it.
					double newZoom = (double) focusAreaShapeWidth / coordinates.getWidth();
					zoom = reduceZoom(newZoom);
					
					translationx = focusAreaShapeLocation + ToolList.getWidth()
							- zoom * coordinates.getX();
					translationy = focusAreaShapeLocation - zoom * coordinates.getY();
					
				}
			}
		}
	}

	/**
	 * Get biggest visible area.
	 * @return
	 */
	public Area getBiggestVisibleArea() {

		Rectangle2D windowRectangle = getWindowRectangle();
		
		double maximumSurface = 0.0;
		Area foundArea = null;
		
		// Do loop for all areas' shapes.
		for (Area area : ProgramGenerator.getAreasModel().getAreas()) {
			
			// Get shapes.
			AreaShapes shapes = (AreaShapes) area.getUser();
			if (shapes != null) {
				
				double surface = shapes.getBiggestInsideSurface(windowRectangle);
				if (surface > maximumSurface) {
					
					maximumSurface = surface;
					foundArea = area;
				}
			}
		}
		
		return foundArea;
	}

	/**
	 * Close constructors display.
	 */
	private void hideConstructorsDisplay() {
		
		displayConstructorsWindow.hidew();
	}

	/**
	 * Display area constructors.
	 * @param area
	 */
	private void displayConstructors(Area area) {
		
		// Show area constructors.
		Point screenMouse = MouseInfo.getPointerInfo().getLocation();
		screenMouse.x += constructorsDisplayHorizontalShift;
		displayConstructorsWindow.showw(area, screenMouse);
	}

	/**
	 * Set not animate next focus flag.
	 */
	public void setNotAnimateNextFocus() {
		
		notAnimateNextFocus = true;
	}

	/**
	 * Get last selected area coordinates.
	 */
	public AreaCoordinates getLastSelectedAreaCoordinates() {
		
		return lastSelectedAreaCoordinates;
	}

	/**
	 * Returns true value if the area contains a sub area.
	 * @param area
	 * @param subArea
	 * @return
	 */
	public static boolean containsSubarea(Area area, Area subArea) {
		
		LinkedList<Area> queue = new LinkedList<Area>();
		queue.add(area);
		
		while (!queue.isEmpty()) {
			
			Area foundArea = queue.removeFirst();
			if (foundArea.equals(subArea)) {
				
				return true;
			}
			
			queue.addAll(foundArea.getSubareas());
		}
		
		return false;
	}

	@Override
	protected void resetToolTipHistory() {
		
	}
	
	/**
	 * On tab panel changed.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
		
	}
	
	/**
	 * Called before tab removed.
	 */
	@Override
	public void beforeTabPanelRemoved() {
		
		// Call GeneralDiagram class method.
		super.beforeRemoved();
		
		// Remove listeners.
		removeListeners();
	}

	/**
	 * No tab text.
	 */
	@Override
	public String getTabDescription() {
		
		return "";
	}

	/**
	 * Update panel.
	 */
	@Override
	public void reload() {
		
	}

	/**
	 * Get tab state
	 */
	@Override
	public TabState getTabState() {
		
		// Create new tab state
		AreasDiagramTabState tabState = new AreasDiagramTabState();
		
		// Set the area ID
		tabState.areaId = topAreaId;
		
		// Get diagram coordinates and return the state object
		tabState.translationx = getTranslatingX();
		tabState.translationy = getTranslatingY();
		tabState.zoom = getZoom();
		
		return tabState;
	}
	
	/**
	 * Set reference to a tab label
	 */
	@Override
	public void setTabLabel(TabLabel tabLabel) {
		
	}
	
	/**
	 * Set top area ID
	 */
	@Override
	public void setAreaId(Long topAreaId) {
		
		this.topAreaId = topAreaId;
	}

	public void focus(Area area) {
		// TODO Auto-generated method stub
		
	}
}