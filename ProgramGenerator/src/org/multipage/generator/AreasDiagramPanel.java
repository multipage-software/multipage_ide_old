/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.VersionObj;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * 
 * @author
 *
 */
public class AreasDiagramPanel extends JPanel implements TabItemInterface {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Editor states.
	 */
	public static int splitPositionStateMain = 480;
	public static int splitPositionStateSecondary = 340;
	public static LinkedList<Long> selectedAreasIdsState = new LinkedList<Long>();
	
	/**
	 * List of selected area IDs in this diagram.
	 */
	private HashSet<Long> selectedAreaIds = new HashSet<Long>();
	
	/**
	 * Areas diagram.
	 */
	private AreasDiagram areasDiagram;
	
	/**
	 * Favorites model.
	 */
	private FavoritesModel favoritesModel;
	
	/**
	 * Favorites list renderer.
	 */
	private ListCellRenderer favoritesRenderer;

	/**
	 * Focused area.
	 */
	Area lastFocusedArea;

	/**
	 * Focused area index.
	 */
	int focusedAreaIndex = 0;
	
	/**
	 * Areas list model.
	 */
	private DefaultListModel<Area> listAreasModel;
	
	/**
	 * Selected area.
	 */
	private Area areaSelection = null;
	
	/**
	 * A reference to the tab label
	 */
	private TabLabel tabLabel;
	
	/**
	 * Top area ID
	 */
	@SuppressWarnings("unused")
	private Long topAreaId = 0L;

	// $hide<<$
	/**
	 * Components.
	 */
	private JSplitPane splitPane;
	private JPanel panelDiagram;
	private JPanel panelTree;
	private JScrollPane scrollPane;
	private JSplitPane splitPaneTree;
	private JScrollPane scrollPaneFavorites;
	private JList listFavorites;
	private JPopupMenu popupMenuFavorites;
	private JMenuItem menuDeleteFavorites;
	/**
	 * @wbp.nonvisual location=570,149
	 */
	private final JPanel panelFavorites = new JPanel();
	private JPanel panel;
	private JButton buttonUp;
	private JButton buttonDown;
	private JList listAreas;
	private JPanel panelAreasTop;
	private JLabel labelFavorites;
	private JPopupMenu popupMenuAreas;
	private JMenuItem menuReload;
	private JToolBar toolBarAreas;
	private JToggleButton buttonSiblings;
	private JToggleButton buttonSubAreas;
	private JToggleButton buttonSuperAreas;
	private JLabel labelAreaListDescription;
	private JToggleButton buttonHoldListType;
	private JSeparator separator;
	private Component horizontalStrut;

	/**
	 * Create the panel.
	 */
	public AreasDiagramPanel() {
		panelFavorites.setLayout(new BorderLayout(0, 0));

		initComponents();
		// $hide>>$
		postCreation();
		// $hide<<$
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		splitPane.setDividerLocation(splitPositionStateMain);
		splitPaneTree.setDividerLocation(splitPositionStateSecondary);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		// Save state.
		splitPositionStateMain = splitPane.getDividerLocation();
		splitPositionStateSecondary = splitPaneTree.getDividerLocation();
		saveFavorites();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {

		setLayout(new BorderLayout(0, 0));
		
		splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(1.0);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		panelDiagram = new JPanel();
		splitPane.setLeftComponent(panelDiagram);
		panelDiagram.setLayout(new BorderLayout(0, 0));
		
		splitPaneTree = new JSplitPane();
		splitPaneTree.setContinuousLayout(true);
		splitPaneTree.setOneTouchExpandable(true);
		splitPane.setRightComponent(splitPaneTree);
		
		panelTree = new JPanel();
		splitPaneTree.setLeftComponent(panelTree);
		panelTree.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panelTree.add(scrollPane, BorderLayout.CENTER);
		
		listAreas = new JList();
		listAreas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					
					int clicks = e.getClickCount();
					if (clicks == 1) {
						onClickArea();
					}
					else if (clicks == 2) {
						onDoubleClickArea();
					}
				}
			}
		});
		scrollPane.setViewportView(listAreas);
		
		popupMenuAreas = new JPopupMenu();
		addPopup(listAreas, popupMenuAreas);
		
		menuReload = new JMenuItem("org.multipage.generator.menuReloadAreas");
		menuReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				hideRelatedAreas();
			}
		});
		popupMenuAreas.add(menuReload);

		popupMenuAreas.addSeparator();
		
		popupMenuAreas.addSeparator();
		
		panelAreasTop = new JPanel();
		scrollPane.setColumnHeaderView(panelAreasTop);
		panelAreasTop.setLayout(new BorderLayout(0, 0));
		
		toolBarAreas = new JToolBar();
		toolBarAreas.setFloatable(false);
		panelAreasTop.add(toolBarAreas, BorderLayout.NORTH);
		
		buttonSiblings = new JToggleButton("");
		buttonSiblings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAreaListButton(e);
			}
		});
		buttonSiblings.setPreferredSize(new Dimension(16, 16));
		toolBarAreas.add(buttonSiblings);
		
		buttonSubAreas = new JToggleButton("");
		buttonSubAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAreaListButton(e);
			}
		});
		buttonSubAreas.setPreferredSize(new Dimension(16, 16));
		toolBarAreas.add(buttonSubAreas);
		
		buttonSuperAreas = new JToggleButton("");
		buttonSuperAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAreaListButton(e);
			}
		});
		buttonSuperAreas.setPreferredSize(new Dimension(16, 16));
		toolBarAreas.add(buttonSuperAreas);
		
		separator = new JSeparator();
		separator.setMaximumSize(new Dimension(6, 32767));
		separator.setOrientation(SwingConstants.VERTICAL);
		toolBarAreas.add(separator);
		
		buttonHoldListType = new JToggleButton("org.multipage.generator.textHoldListType");
		buttonHoldListType.setHorizontalAlignment(SwingConstants.LEFT);
		toolBarAreas.add(buttonHoldListType);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		toolBarAreas.add(horizontalStrut);
		
		labelAreaListDescription = new JLabel("");
		labelAreaListDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		toolBarAreas.add(labelAreaListDescription);
		
		scrollPaneFavorites = new JScrollPane();
		panelFavorites.add(scrollPaneFavorites);
		splitPaneTree.setRightComponent(panelFavorites);
		
		listFavorites = new JList();
		listFavorites.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (e.getClickCount() == 1) {
						onClickFavorite();
					}
					else if (e.getClickCount() == 2) {
						onDoubleClickFavorite();
					}
				}
			}
		});
		scrollPaneFavorites.setViewportView(listFavorites);
		
		popupMenuFavorites = new JPopupMenu();
		addPopup(listFavorites, popupMenuFavorites);
		
		menuDeleteFavorites = new JMenuItem("org.multipage.generator.menuDeleteFavorites");
		menuDeleteFavorites.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteFavorites();
			}
		});
		popupMenuFavorites.add(menuDeleteFavorites);
		popupMenuFavorites.addSeparator();
		
		labelFavorites = new JLabel("org.multipage.generator.textFavoritesLabel");
		scrollPaneFavorites.setColumnHeaderView(labelFavorites);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(35, 10));
		panelFavorites.add(panel, BorderLayout.EAST);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonUp = new JButton("");
		buttonUp.setToolTipText("org.multipage.generator.tooltipMoveFavoriteUp");
		buttonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveFavoriteUp();
			}
		});
		buttonUp.setPreferredSize(new Dimension(25, 25));
		sl_panel.putConstraint(SpringLayout.NORTH, buttonUp, 5, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, buttonUp, 5, SpringLayout.WEST, panel);
		panel.add(buttonUp);
		
		buttonDown = new JButton("");
		buttonDown.setToolTipText("org.multipage.generator.tooltipMoveFavoriteDown");
		buttonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveFavoriteDown();
			}
		});
		buttonDown.setPreferredSize(new Dimension(25, 25));
		sl_panel.putConstraint(SpringLayout.NORTH, buttonDown, 6, SpringLayout.SOUTH, buttonUp);
		sl_panel.putConstraint(SpringLayout.WEST, buttonDown, 0, SpringLayout.WEST, buttonUp);
		panel.add(buttonDown);
	}
	
	/**
	 * On click on favorite.
	 */
	protected void onClickFavorite() {
		
		listAreas.clearSelection();
		
		// Set new area selection.
		Object value = listFavorites.getSelectedValue();
		if (value instanceof Long) {
			Long areaId = (Long) value;
			
			selectedAreaIds.clear();
			selectedAreaIds.add(areaId);
		}
	}
	
	/**
	 * On double click on favorite.
	 */
	protected void onDoubleClickFavorite() {
		
		// Get selected areas.
		Object [] selectedObjects = listFavorites.getSelectedValues();
		if (selectedObjects.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		if (selectedObjects[0] instanceof Long) {
			long areaId = (Long) selectedObjects[0];
		
			// Focus area.
			focusAreaNear(areaId);
		}
	}
	
	/**
	 * On click area.
	 */
	protected void onClickArea() {
		
		// Clear selection in favorites.
		listFavorites.clearSelection();
		
		// Set new area selection.
		Object value = listAreas.getSelectedValue();
		if (value instanceof Area) {
			Area area = (Area) value;
			
			selectedAreaIds.clear();
			selectedAreaIds.add(area.getId());
			
			// Transmit "on related areas clicked" signal.
			ConditionalEvents.transmit(AreasDiagramPanel.this, Signal.onClickRelatedAreas, selectedAreaIds);
		}
	}
	
	/**
	 * On double click area.
	 */
	protected void onDoubleClickArea() {
		
		// Get selected areas.
		List<Area> selectedAreas = listAreas.getSelectedValuesList();
		if (selectedAreas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		areaSelection = selectedAreas.get(0);
		long areaId = areaSelection.getId();
		
		// Repaint GUI.
		repaint();
		
		// Focus area.
		focusAreaNear(areaId);
		
		// Transmit "update areas" signal.
		ConditionalEvents.transmit(AreasDiagramPanel.this, Signal.displayRelatedAreas, areaId);
	}

	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		loadDialog();
		localize();
		setIcons();
		setToolTips();
		setAreasDiagram();
		setTree();
		setFavorites();
		setListeners();
		createPopupMenus();
		createAreasList();
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonSiblings.setToolTipText(Resources.getString("org.multipage.generator.tooltipAreaSiblings"));
		buttonSubAreas.setToolTipText(Resources.getString("org.multipage.generator.tooltipAreaSubAreas"));
		buttonSuperAreas.setToolTipText(Resources.getString("org.multipage.generator.tooltipAreaSuperAreas"));
		buttonHoldListType.setToolTipText(Resources.getString("org.multipage.generator.tooltipHoldAreasListType"));
	}

	/**
	 * Create popup menus.
	 */
	private void createPopupMenus() {

		// Favorites popup.
		final Component thisComponent = this;
		
		AreaLocalMenu areaLocalMenuFavorites = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected items.
				Object [] selected = listFavorites.getSelectedValues();
				if (selected.length != 1) {
					return null;
				}
				return ProgramGenerator.getAreasModel().getArea((Long) selected[0]);
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		areaLocalMenuFavorites.isAddFavorites = false;
		areaLocalMenuFavorites.addTo(this, popupMenuFavorites);
		
		AreaLocalMenu areaLocalMenuAreas = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {

				// Get selected item.
				return (Area) listAreas.getSelectedValue();
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		areaLocalMenuAreas.addTo(this, popupMenuAreas);
	}

	/**
	 * Set areas diagram.
	 */
	private void setAreasDiagram() {
		
		// Create new areas diagram.
		areasDiagram = ProgramGenerator.newAreasDiagram(this);
		panelDiagram.add(areasDiagram);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		menuDeleteFavorites.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
		buttonUp.setIcon(Images.getIcon("org/multipage/generator/images/up.png"));
		buttonDown.setIcon(Images.getIcon("org/multipage/generator/images/down.png"));
		menuReload.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		buttonSiblings.setIcon(Images.getIcon("org/multipage/generator/images/siblings_small.png"));
		buttonSubAreas.setIcon(Images.getIcon("org/multipage/generator/images/subareas_small.png"));
		buttonSuperAreas.setIcon(Images.getIcon("org/multipage/generator/images/superareas_small.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(menuDeleteFavorites);
		Utility.localizeTooltip(buttonUp);
		Utility.localizeTooltip(buttonDown);
		Utility.localize(labelFavorites);
		Utility.localize(menuReload);
		Utility.localize(buttonHoldListType);
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// "On click related areas" event receiver.
		ConditionalEvents.receiver(this, Signal.onClickRelatedAreas, message -> {
			
			// Get selected areas.
			HashSet<Long> selectedAreaIds = getSelectedAreaIds();
			
			j.log("TRANSMITTED 5 showAreasProperties %s", selectedAreaIds.toString());
			// Propagate "show areas properties" event.
			ConditionalEvents.transmit(AreasDiagramPanel.this, Signal.showAreasProperties, selectedAreaIds);
			// Propagate "select diagram areas" event.
			ConditionalEvents.transmit(AreasDiagramPanel.this, Signal.selectDiagramAreas, selectedAreaIds);
		});
		
		// "Show areas' relations" event receiver.
		ConditionalEvents.receiver(this, Signal.showAreasRelations, action -> {
				
			if (action.relatedInfo instanceof HashSet<?>) {
				
				selectedAreaIds = (HashSet<Long>) action.relatedInfo;
				displayRelatedAreasForSet(selectedAreaIds);
			}
		});
		
		// "Select all" event receiver.
		ConditionalEvents.receiver(this, Signal.selectAll, action -> {
			
			if (AreasDiagramPanel.this.isShowing()) {
				
				selectedAreaIds = ProgramGenerator.getAllAreaIds();
				displayRelatedAreasForSet(selectedAreaIds);
			}
		});
		
		// "Unselect all" event receiver.
		ConditionalEvents.receiver(this, Signal.unselectAll, action -> {
			
			if (AreasDiagramPanel.this.isShowing()) {
				
				selectedAreaIds = new HashSet<Long>();
				displayRelatedAreasForSet(selectedAreaIds);
			}
		});
		
		// "Focus home area" event receiver.
		ConditionalEvents.receiver(this, Signal.focusHomeArea, action -> {
			
			if (AreasDiagramPanel.this.isShowing()) {
				focusHomeArea();
			}
		});
		
		// "Focus tab area" event receiver.
		ConditionalEvents.receiver(this, Signal.focusTabArea, action -> {
			
			if (AreasDiagramPanel.this.isShowing()) {
				
				Long tabAreaId = action.relatedInfo instanceof Long ? (Long) action.relatedInfo : 0L;
				focusAreaNear(tabAreaId);
			}
		});
		
		// "Display related areas" event receiver.
		ConditionalEvents.receiver(this, Signal.displayRelatedAreas, action -> {
			
			if (AreasDiagramPanel.this.isShowing() && action.relatedInfo instanceof Long) {
				
				// Pull area ID.
				long areaId = (Long) action.relatedInfo;
				// Display related areas.
				displayRelatedAreas(areaId);
			}
		});
		
		// Add receiver for the "show or hide" event.
		ConditionalEvents.receiver(this, Signal.showOrHideIds, message -> {
			
			// Reload and repaint the GUI.
			reload();
			repaint();
		});
	}
	
	/**
	 * Remove listeners.
	 * @return
	 */
	private void removeListeners() {
		
		ConditionalEvents.removeReceivers(this);
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		
		saveDialog();
		removeListeners();
		areasDiagram.dispose();
		areasDiagram.removeDiagram();
	}

	/**
	 * Initialize.
	 */
	public void init() {
		
		areasDiagram.init();
		loadFavorites();
	}

	/**
	 * Initialize diagram editor.
	 * @param currentAreasEditor 
	 */
	public void initDiagramEditor(AreasDiagramPanel currentAreasEditor) {
		
		splitPane.setDividerLocation(currentAreasEditor.splitPane.getDividerLocation());
		splitPaneTree.setDividerLocation(currentAreasEditor.splitPaneTree.getDividerLocation());
		
		areasDiagram.init();
	}

	/**
	 * Load favorites.
	 */
	private void loadFavorites() {

		FavoritesModel.setAreasIds(selectedAreasIdsState);
		favoritesModel.update();
		listFavorites.updateUI();
	}

	/**
	 * Save favorites.
	 */
	private void saveFavorites() {
		
		selectedAreasIdsState = favoritesModel.getAreasIds();
	}

	/**
	 * Get diagram.
	 * @return
	 */
	public AreasDiagram getDiagram() {
		
		return areasDiagram;
	}
	
	/**
	 * Get selected areas.
	 * @return
	 */
	public LinkedList<Area> getSelectedAreas() {

		return areasDiagram.getSelectedAreas();
	}

	/**
	 * Get selected and enabled areas.
	 * @return
	 */
	public LinkedList<Area> getSelectedAndEnabledAreas() {

		LinkedList<Area> areas = areasDiagram.getSelectedAreas();
		LinkedList<Area> result = new LinkedList<Area>();
		
		for (Area area : areas) {
			if (area.isEnabled()) {
				result.add(area);
			}
		}
		return result;
	}
	
	/**
	 * Set tree.
	 */
	private void setTree() {

	}

	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	/**
	 * Add favorite area.
	 * @param area
	 */
	public void addFavorite(Area area) {
		
		favoritesModel.addNew(area.getId());
		listFavorites.updateUI();
	}

	/**
	 * Set favorites.
	 */
	private void setFavorites() {
		
		// Create and set model.
		favoritesModel = new FavoritesModel(this);
		listFavorites.setModel(favoritesModel);
		// Create and set renderer.
		favoritesRenderer = new FavoritesRenderer();
		listFavorites.setCellRenderer(favoritesRenderer);
	}

	/**
	 * Delete favorites.
	 */
	protected void deleteFavorites() {
		
		// Get selected areas.
		Object [] selectedObjects = listFavorites.getSelectedValues();
		if (selectedObjects.length < 1) {
			Utility.show(this, "org.multipage.generator.messageSelectFavorites");
			return;
		}
		
		// Remove selected areas.
		for (Object selectedObject : selectedObjects) {
			if (selectedObject instanceof Long) {
				
				long areaId = (Long) selectedObject;
				favoritesModel.removeArea(areaId);
			}
		}
		
		listFavorites.updateUI();
	}

	/**
	 * Focus area.
	 * @param area
	 */
	public void focusArea(long areaId) {
		
		// Get area object.
		Area area = ProgramGenerator.getAreasModel().getArea(areaId);
		if (area == null) {
			return;
		}
		
		// Get area shapes.
		Object userObject = area.getUser();
		if (!(userObject instanceof AreaShapes)) {
			return;
		}
		
		if (!area.equals(lastFocusedArea)) {
			focusedAreaIndex = 0;
		}
		
		AreaShapes shapes = (AreaShapes) userObject;
		LinkedList<AreaCoordinates> coordinatesList = shapes.getCoordinates();
		if (focusedAreaIndex >= coordinatesList.size()) {
			focusedAreaIndex = 0;
		}
		
		// Focus on area shape.
		AreaCoordinates coordinates = coordinatesList.get(focusedAreaIndex);
		areasDiagram.focus(coordinates, area);
		
		lastFocusedArea = area;
		focusedAreaIndex++;		
	}

	/**
	 * Focus global area.
	 */
	public void focusGlobalArea() {
		
		focusArea(0L);
	}
	
	/**
	 * Focus near area.
	 * @param areaId
	 */
	public void focusAreaNear(long areaId) {
		
		// Get area object.
		Area area = ProgramGenerator.getAreasModel().getArea(areaId);
		if (area == null) {
			return;
		}
		
		// Get diagram coordinates.
		Rectangle2D diagramRectangle = areasDiagram.getRectInCoord();
		
		// Get area shapes.
		Object userObject = area.getUser();
		if (!(userObject instanceof AreaShapes)) {
			return;
		}
		AreaShapes shapes = (AreaShapes) userObject;
		LinkedList<AreaCoordinates> coordinatesList = shapes.getCoordinates();
		if (coordinatesList.isEmpty()) {
			return;
		}

		// Find nearest area coordinates.
		double maximumIntersectedWidth = -1.0;
		double maximumAngle = -1.0;
		
		double diagramHeight = diagramRectangle.getHeight();
		Point2D diagramCenter = new Point2D.Double(diagramRectangle.getCenterX(), diagramRectangle.getCenterY());
		
		LinkedList<AreaCoordinates> largestIntersected = new LinkedList<AreaCoordinates>();
		LinkedList<AreaCoordinates> closerNotIntersected = new LinkedList<AreaCoordinates>();
		
		// Find largest intersected coordinates and all near not intersected coordinates.
		for (AreaCoordinates coordinates : coordinatesList) {
			
			Rectangle2D areaRectangle = coordinates.getRectangle();
			if (Utility.isIntersection(areaRectangle, diagramRectangle)) {
				
				if (coordinates.getWidth() > maximumIntersectedWidth) {
					maximumIntersectedWidth = coordinates.getWidth();
					
					largestIntersected.clear();
					largestIntersected.add(coordinates);
				}
				else if (coordinates.getWidth() == maximumIntersectedWidth) {
					largestIntersected.add(coordinates);
				}
			}
			else {
				
				// Compute distance.
				Point2D shapeCenter = coordinates.getCenter();
				
				double distance = shapeCenter.distance(diagramCenter);
				double angle = distance == 0.0 ? Math.PI / 2.0 : Math.atan(diagramHeight / distance);
				
				if (angle > maximumAngle) {
					maximumAngle = angle;
					
					closerNotIntersected.clear();
					closerNotIntersected.add(coordinates);
				}
				else if (angle == maximumAngle) {
					closerNotIntersected.add(coordinates);
				}
			}
		}
		
		// If single intersected coordinates exist, focus it.
		if (largestIntersected.size() == 1) {
			
			AreaCoordinates coordinates = largestIntersected.getFirst();
			areasDiagram.focus(coordinates, area);
			return;
		}
		
		// If exist intersected coordinates, get coordinates near the diagram top left corner.
		if (!largestIntersected.isEmpty()) {
			
			Point2D diagramLeftTop = new Point2D.Double(diagramRectangle.getX(), diagramRectangle.getY());
			
			double maximumDistance = -1.0;
			AreaCoordinates nearestCoordinates = null;
			
			for (AreaCoordinates coordinates : largestIntersected) {
				
				// Get coordinates center.
				Point2D shapeCenter = coordinates.getCenter();
				double distance = shapeCenter.distance(diagramLeftTop);
				
				// Update maximum value.
				if (maximumDistance > distance) {
					maximumDistance = distance;
					nearestCoordinates = coordinates;
				}
			}
			
			if (nearestCoordinates != null) {
				areasDiagram.focus(nearestCoordinates, area);
				return;
			}
			
			// ... just to be sure.
			areasDiagram.focus(largestIntersected.getFirst(), area);
			return;
		}
		
		// Get first closer not intersected coordinates.
		double maximumDistance = -1.0;
		AreaCoordinates closerCoordinates = null;
		
		for (AreaCoordinates coordinates : closerNotIntersected) {
			
			// Compute distance.
			Point2D shapeCenter = coordinates.getCenter();
			double distance = shapeCenter.distance(diagramCenter);
			
			if (distance > maximumDistance) {
				
				maximumDistance = distance;
				closerCoordinates = coordinates;
			}
		}
		
		// Focus area.
		if (closerCoordinates != null) {
			areasDiagram.focus(closerCoordinates, area);
			return;
		}
		
		// For sure ...
		if (!closerNotIntersected.isEmpty()) {
			areasDiagram.focus(closerNotIntersected.getFirst(), area);
			return;
		}
		areasDiagram.focus(coordinatesList.getFirst(), area);
		return;
	}

	/**
	 * Move selected favorites up.
	 */
	protected void moveFavoriteUp() {
		
		// Get selected favorites.
		Object [] selectedObjects = listFavorites.getSelectedValues();
		if (selectedObjects.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleFavoriteArea");
			return;
		}
		
		// Get favorite area and move it up.
		Object selectedObject = selectedObjects[0];
		if (selectedObject instanceof Long) {
			
			long areaId = (Long) selectedObject;
			// Move selected object up.
			favoritesModel.moveUp(areaId, listFavorites);
		}
	}

	/**
	 * Move selected favorites down.
	 */
	protected void moveFavoriteDown() {
		
		// Get selected favorites.
		Object [] selectedObjects = listFavorites.getSelectedValues();
		if (selectedObjects.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleFavoriteArea");
			return;
		}
		
		// Get favorite area and move it up.
		Object selectedObject = selectedObjects[0];
		if (selectedObject instanceof Long) {
			
			long areaId = (Long) selectedObject;
			// Move selected object down.
			favoritesModel.moveDown(areaId, listFavorites);
		}
	}

	/**
	 * Focus on home area.
	 */
	public void focusHomeArea() {
		
		AreasModel model = ProgramGenerator.getAreasModel();
		focusArea(model.getHomeAreaId());
	}

	/**
	 * Select area.
	 * @param areaId
	 * @param selected
	 * @param affectSubareas
	 */
	public void select(long areaId, boolean selected, boolean affectSubareas) {
		
		areasDiagram.removeSelection();
		areasDiagram.select(areaId, selected, affectSubareas);
	}
	
	/**
	 * Select siblings button.
	 */
	private void select(JToggleButton button) {
		
		final JToggleButton [] buttonList = { buttonSiblings, buttonSubAreas, buttonSuperAreas };
		
		// Select the input button and unselect the others.
		for (JToggleButton existingButton : buttonList) {
			
			existingButton.setSelected(existingButton.equals(button));
		}
	}
	
	/**
	 * Display related areas.
	 * @param areaIds
	 */
	private void displayRelatedAreasForSet(HashSet<Long> areaIds) {
		
		if (areaIds.size() != 1) {
			hideRelatedAreas();
			return;
		}
		
		Long areaId = areaIds.iterator().next();
		displayRelatedAreas(areaId);
	}

	/**
	 * Hide related areas.
	 */
	private void hideRelatedAreas() {
		
		displayRelatedAreas(null);
	}
	
	/**
	 * Display related areas.
	 */
	private void displayRelatedAreas(Long areaId) {
		
		if (areaId != null) {
			areaSelection = ProgramGenerator.getArea(areaId);
		}
		else {
			areaSelection = null;
		}
		
		if (!buttonHoldListType.isSelected()) {
			
			// On loose list type.
			resetAreaListButtonsAndDescription();
			loadAreaSiblings();
			return;
		}

		// On hold list type.
		if (buttonSiblings.isSelected()) {
			
			resetAreaListButtonsAndDescription();
			loadAreaSiblings();
		}
		else if (buttonSubAreas.isSelected()) {
			
			resetAreaListButtonsAndDescription();
			loadAreaSubAreas();
		}
		else if (buttonSuperAreas.isSelected()) {
			
			resetAreaListButtonsAndDescription();
			loadAreaSuperAreas();
		}
		else {
			resetAreaListButtonsAndDescription();
			loadAreaSiblings();
		}
	}

	/**
	 * Create areas list.
	 */
	private void createAreasList() {
		
		listAreasModel = new DefaultListModel<Area>();
		listAreas.setCellRenderer(newListSiblingsRenderer());
		listAreas.setModel(listAreasModel);
		
		resetAreaListButtonsAndDescription();
	}

	/**
	 * Load siblings.
	 */
	private void loadAreaSiblings() {
		
		// Remember current selection.
		int [] selectedIndices = listAreas.getSelectedIndices();
		
		buttonSiblings.setSelected(true);

		listAreasModel.clear();
		
		if (areaSelection == null) {
			return;
		}

		// Get area shapes.
		Object userObject = areaSelection.getUser();
		if (!(userObject instanceof AreaShapes)) {
			return;
		}
		
		AreaShapes shapes = (AreaShapes) userObject;
		// Get parent area.
		Rectangle2D diagramRect = areasDiagram.getRectInCoord();
		Area superArea = shapes.getVisibleParent(diagramRect);
		
		if (superArea != null) {
			
			// Add siblings list.
			int selectedIndex = 0;
			int index = 0;
			
			LinkedList<Area> siblings = superArea.getSubareas();
			
			for (Area subArea : siblings) {
				
				listAreasModel.addElement(subArea);
				
				if (subArea.equals(areaSelection)) {
					selectedIndex = index;
				}
				index++;
			}
			
			int siblingsCount = siblings.size();
			
			// Show smartly the selected area.
			if (selectedIndex - 1 > 0) {
				listAreas.ensureIndexIsVisible(selectedIndex - 1);
			}
			
			if (selectedIndex + 1 < siblingsCount) {
				listAreas.ensureIndexIsVisible(selectedIndex + 1);
			}
			
			listAreas.ensureIndexIsVisible(selectedIndex);
		}
		else {
			// Load global area.
			listAreasModel.addElement(areaSelection);
		}
		
		// Set description.
		labelAreaListDescription.setText(Resources.getString("org.multipage.generator.textAreaSiblings"));
		
		// Restore selection.
		listAreas.setSelectedIndices(selectedIndices);
	}

	/**
	 * Load sub areas.
	 */
	private void loadAreaSubAreas() {
		
		buttonSubAreas.setSelected(true);
		buttonHoldListType.setEnabled(true);

		listAreasModel.clear();
		
		if (areaSelection == null) {
			return;
		}
		
		for (Area area : areaSelection.getSubareas()) {
			
			listAreasModel.addElement(area);
		}
		
		// Set description.
		labelAreaListDescription.setText(Resources.getString("org.multipage.generator.textAreaSubAreas"));
	}

	/**
	 * Load super areas.
	 */
	private void loadAreaSuperAreas() {
		
		buttonSuperAreas.setSelected(true);
		buttonHoldListType.setEnabled(true);
		
		listAreasModel.clear();
		
		if (areaSelection == null) {
			return;
		}
		
		for (Area area : areaSelection.getSuperareas()) {
			
			listAreasModel.addElement(area);
		}
		
		// Set description.
		labelAreaListDescription.setText(Resources.getString("org.multipage.generator.textAreaSuperAreas"));
	}
	
	/**
	 * Reset area list buttons and description.
	 */
	private void resetAreaListButtonsAndDescription() {
				
		// Reset buttons.
		buttonSiblings.setSelected(false);
		buttonSubAreas.setSelected(false);
		buttonSuperAreas.setSelected(false);
		
		buttonHoldListType.setEnabled(false);
		
		labelAreaListDescription.setText("");
	}
	
	/**
	 * On area list button.
	 * @param event
	 */
	protected void onAreaListButton(ActionEvent event) {

		// Reset buttons.
		resetAreaListButtonsAndDescription();

		// Get event source.
		Object source = event.getSource();
		if (!(source instanceof JToggleButton)) {
			return;
		}
		
		JToggleButton sourceButton = (JToggleButton) source;

		// Do appropriate action.
		if (sourceButton.equals(buttonSiblings)) {
			loadAreaSiblings();
			return;
		}
		if (sourceButton.equals(buttonSubAreas)) {
			loadAreaSubAreas();
			return;
		}
		if (sourceButton.equals(buttonSuperAreas)) {
			loadAreaSuperAreas();
			return;
		}
	}

	/**
	 * Create new siblings renderer.
	 * @return
	 */
	@SuppressWarnings("serial")
	private ListCellRenderer newListSiblingsRenderer() {
		
		class Renderer extends JLabel {
			// Fields.
			private boolean isSelected;
			private boolean hasFocus;
			// Constructor.
			public Renderer() {
				setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
			}
			// Set label.
			public void set(Area area, boolean isSelected, boolean hasFocus) {
				this.isSelected = isSelected;
				this.hasFocus = hasFocus;
				setText(area.getDescriptionForDiagram());
				setForeground(area.equals(areaSelection) ? Color.red : Color.black);
			}
			// Reset label.
			public void reset() {
				isSelected = hasFocus = false;
				setText("");
				setForeground(Color.black);
			}
			// Paint label.
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				GraphUtility.drawSelection(g, this, isSelected, hasFocus);
			}
		}
		
		// Create and return renderer.
		return new DefaultListCellRenderer() {
			Renderer renderer = new Renderer();
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
				if (value instanceof Area) {
					renderer.set((Area) value, isSelected, cellHasFocus);
				}
				else {
					renderer.reset();
				}
				return renderer;
			}
		};
	}

	/**
	 * Focus start area.
	 * @param areaId
	 * @param versionId 
	 */
	public void focusStartArea(long areaId, long versionId) {
		
		AreasModel model = ProgramGenerator.getAreasModel();
		Area area = model.getArea(areaId);
		VersionObj version = model.getVersion(versionId);
		
		// Check parameters.
		if (area == null || version == null) {
			Utility.show(this, "org.multipage.generator.textFocusStartAreaBadParameter");
			return;
		}
		
		Area startArea = model.getStartArea(area, versionId);
		
		// If not found, inform user.
		if (startArea == null) {
			Utility.show(this, "org.multipage.generator.messageStartAreaNotFound", area.toString(), version.toString());
			return;
		}
		
		// Focus start area.
		focusAreaNear(startArea.getId());
	}

	/**
	 * Copy area tree.
	 */
	public void copyAreaTree() {
		
	}
	
	/**
	 * On tab panel change event.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
		// Call this method for diagram panel.
		areasDiagram.onTabPanelChange(e, selectedIndex);
		
		// Propagate event.
		ConditionalEvents.transmit(AreasDiagramPanel.this, Signal.mainTabChange, selectedIndex);
	}
	
	/**
	 * Before tab panel removed.
	 */
	@Override
	public void beforeTabPanelRemoved() {
		
		// Call the same method for diagram.
		areasDiagram.beforeTabPanelRemoved();
		
		// Remove listeners.
		removeListeners();
	}
	
	/**
	 * Get selected area IDs.
	 * @return
	 */
	public HashSet<Long> getSelectedAreaIds() {
		
		return this.selectedAreaIds;
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
		
		// Try to get inner area diagram, set and return the state object.
		AreasDiagram areasDiagram = this.getDiagram();
		if (areasDiagram == null) {
			return null;
		}
		TabState tabState = areasDiagram.getTabState();
		
		// Set title and return the state object
		tabState.title = tabLabel.getDescription();
		return tabState;
	}
	
	/**
	 * Set reference to a tab label
	 */
	@Override
	public void setTabLabel(TabLabel tabLabel) {
		
		this.tabLabel = tabLabel;
	}
	
	/**
	 * Set top area ID
	 */
	@Override
	public void setAreaId(Long topAreaId) {
		
		this.topAreaId = topAreaId;
		
		// Try to get inner area diagram and set top area ID.
		AreasDiagram areasDiagram = this.getDiagram();
		if (areasDiagram == null) {
			return;
		}
		
		areasDiagram.setAreaId(topAreaId);
	}
}

/**
 * Favorites list renderer.
 * @author
 *
 */
class FavoritesRenderer implements ListCellRenderer {
	
	/**
	 * Asterisk.
	 */
	private static final Icon asteriskIcon;
	
	/**
	 * Static constructor.
	 */
	static {
		
		asteriskIcon = Images.getIcon("org/multipage/generator/images/favorite.png");
	}
		
	/**
	 * Label.
	 */
	private class FavoriteLabel extends JLabel {

		/**
		 * Version.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Flags.
		 */
		private boolean isSelected;
		private boolean hasFocus;

		/**
		 * Constructor.
		 */
		public FavoriteLabel() {
			
			setIcon(asteriskIcon);
		}

		/**
		 * Reset properties.
		 */
		public void resetProperties() {
			
			setText("");
			setForeground(Color.BLACK);
			this.isSelected = false;
			this.hasFocus = false;
		}

		/**
		 * Set properties.
		 * @param areaId
		 * @param hasFocus 
		 * @param isSelected 
		 */
		public void setProperties(long areaId, boolean isSelected, boolean hasFocus) {
			
			// Get area.
			AreasModel model = ProgramGenerator.getAreasModel();
			Area area = model.getArea(areaId);
			
			if (area == null) {
				resetProperties();
				return;
			}
			setText(area.getDescriptionForDiagram());
			
			// Area selection.
			Color color = Color.BLACK;
			Object user = area.getUser();
			if (user instanceof AreaShapes) {
				AreaShapes shapes = (AreaShapes) user;
				
				if (shapes.isSelected()) {
					color = Color.RED;
				}
			}
			setForeground(color);
			
			this.isSelected = isSelected;
			this.hasFocus = hasFocus;
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			
			super.paint(g);
			GraphUtility.drawSelection(g, this, isSelected, hasFocus);
		}
	}

	/**
	 * Label.
	 */
	private FavoriteLabel label;

	/**
	 * Constructor.
	 */
	public FavoritesRenderer() {
		
		label = new FavoriteLabel();
	}
	
	/**
	 * Return renderer component.
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		// Check value type.
		if (!(value instanceof Long)) {
			label.resetProperties();
		}
		else {
			long areaId = (Long) value;
			
			label.setProperties(areaId, isSelected, cellHasFocus);
		}
		return label;
	}
}

/**
 * Favorites model.
 * @author
 *
 */
class FavoritesModel extends AbstractListModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Areas' IDs list.
	 */
	private static LinkedList<Long> favoriteAreasIds = new LinkedList<Long>();
	
	/**
	 * Editor reference.
	 */
	private AreasDiagramPanel editor;
	
	/**
	 * Constructor.
	 */
	public FavoritesModel(AreasDiagramPanel editor) {
		
		this.editor = editor;
	}

	/**
	 * Update favorites model. Remove non existing areas.
	 */
	public void update() {
		
		LinkedList<Long> itemsToRemove = new LinkedList<Long>();
		AreasModel model = ProgramGenerator.getAreasModel();
		
		Long lastFocusedAreaId = null;
		if (editor.lastFocusedArea != null) {
			lastFocusedAreaId = editor.lastFocusedArea.getId();
		}
		
		for (long areaId : favoriteAreasIds) {
			if (!model.existsArea(areaId)) {
				itemsToRemove.add(areaId);
				
				// Remove last focused area from editor.
				if (lastFocusedAreaId != null && areaId == lastFocusedAreaId) {
					editor.lastFocusedArea = null;
					editor.focusedAreaIndex = 0;
				}
			}
		}
		
		favoriteAreasIds.removeAll(itemsToRemove);
	}

	/**
	 * Move area up.
	 * @param areaId
	 * @param list 
	 */
	public void moveUp(long areaId, JList list) {
		
		// Get area ID index.
		int index = favoriteAreasIds.indexOf(areaId);
		// Swap areas.
		if (index > 0) {
			swap(index, index - 1);
		
			list.setSelectedIndex(index - 1);
			list.updateUI();
		}
	}

	/**
	 * Move area down.
	 * @param areaId
	 * @param list 
	 */
	public void moveDown(long areaId, JList list) {
		
		// Get area index and areas count.
		int index = favoriteAreasIds.indexOf(areaId);
		int count = favoriteAreasIds.size();
		// Swap areas.
		if (index < count - 1) {
			swap(index, index + 1);
			
			list.setSelectedIndex(index + 1);
			list.updateUI();
		}
	}

	/**
	 * Swap areas.
	 * @param index1
	 * @param index2
	 */
	private void swap(int index1, int index2) {
		
		long areaId1 = favoriteAreasIds.get(index1);
		long areaId2 = favoriteAreasIds.get(index2);
		favoriteAreasIds.set(index1, areaId2);
		favoriteAreasIds.set(index2, areaId1);
	}

	/**
	 * Remove area.
	 * @param area
	 */
	public void removeArea(long areaId) {
		
		favoriteAreasIds.remove(areaId);
	}

	/**
	 * Add new area.
	 * @param areaId
	 */
	public void addNew(long areaId) {
		
		if (!favoriteAreasIds.contains(areaId)) {
			favoriteAreasIds.add(areaId);
		}
	}

	/**
	 * Get size.
	 */
	@Override
	public int getSize() {
		
		return favoriteAreasIds.size();
	}

	/**
	 * Get area.
	 */
	@Override
	public Object getElementAt(int index) {
		
		return favoriteAreasIds.get(index);
	}

	/**
	 * @return the areas
	 */
	public LinkedList<Long> getAreasIds() {
		
		return favoriteAreasIds;
	}
	
	/**
	 * Set areas' IDS.
	 * @param areasIds
	 */
	public static void setAreasIds(LinkedList<Long> areasIds) {
		
		favoriteAreasIds = areasIds;
	}
}
