/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.Area;
import org.maclan.AreaRelation;
import org.multipage.gui.IdentifierTreePath;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreasTreePanel extends JPanel {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Expanded tree rows.
	 */
	private static IdentifierTreePath[] expandedPaths;

	/**
	 * Root area reference.
	 */
	private Area rootArea;

	/**
	 * Tree model.
	 */
	private DefaultTreeModel treeModel;
	
	/**
	 * Area listener.
	 */
	private Consumer<LinkedList<Area>> areasListener;
	
	/**
	 * Area with sub areas listener.
	 */
	private Consumer<Area> areaWithSubAreasListener;
	
	/**
	 * Purpose of diagram.
	 */
	private int localMenuHint;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JScrollPane scrollPane;
	private JTree tree;
	private JLabel labelAreasTree;
	private JToolBar toolBar;
	private JPopupMenu popupMenu;

	/**
	 * Create the panel.
	 */
	public AreasTreePanel(int localMenuHint) {

		initComponents();
		
		this.localMenuHint = localMenuHint;
		
		postCreate(); // $hide$
	}
	
	/**
	 * Constructor.
	 */
	public AreasTreePanel() {
		
		this(AreaLocalMenu.DIAGRAM);
	}

	/**
	 * Constructor.
	 * @param rootArea
	 */
	public AreasTreePanel(Area rootArea) {
		
		this.rootArea = rootArea;

		initComponents();
		postCreate(); // $hide$
	}
	
	/**
	 * Constructor.
	 * @param rootArea
	 * @param localMenuHint
	 */
	public AreasTreePanel(Area rootArea, int localMenuHint) {
		
		this.rootArea = rootArea;
		this.localMenuHint = localMenuHint;

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, this);
		add(scrollPane);
		
		tree = new JTree();
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeCollapsed(TreeExpansionEvent arg0) {
				onTreeExpandedCollapsed();
			}
			public void treeExpanded(TreeExpansionEvent arg0) {
				onTreeExpandedCollapsed();
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onAreasSelected(e);
			}
		});
		scrollPane.setViewportView(tree);
		
		popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);
		
		labelAreasTree = new JLabel("org.multipage.generator.textAreasTree");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreasTree, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelAreasTree, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.SOUTH, labelAreasTree);
		add(labelAreasTree);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, toolBar);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, this);
		toolBar.setPreferredSize(new Dimension(13, 30));
		springLayout.putConstraint(SpringLayout.EAST, toolBar, -10, SpringLayout.EAST, this);
		toolBar.setFloatable(false);
		add(toolBar);
	}
	
	/**
	 * On tree expanded or collapsed.
	 */
	protected void onTreeExpandedCollapsed() {
		
		expandedPaths = Utility.getExpandedPaths2(tree);
	}

	/**
	 * On area selected.
	 * @param e 
	 */
	protected void onAreasSelected(TreeSelectionEvent e) {
		
		if (areasListener != null) {
			LinkedList<Area> areas = getSelectedAreas();
			
			if (areas != null) {
				areasListener.accept(areas);
			}
		}		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		
		createToolBar();
		
		addAreaPopupMenu();
		
		initializeTree();
		loadAreasTree();
		
		// Expand tree items.
		if (expandedPaths != null) {
			Utility.setExpandedPaths(tree, expandedPaths);
		}
	}
	

	/**
	 * Select area with sub nodes.
	 */
	protected void selectNodeWithSubNodes() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		TreePath treePath = selectedPaths[0];
		LinkedList<TreePath> treePaths = new LinkedList<TreePath>();
		
		getSubPaths((DefaultMutableTreeNode) treePath.getLastPathComponent(), treePaths);
		
		// Select sub nodes.
		tree.setSelectionPaths(treePaths.toArray(new TreePath [0]));
		
		// Invoke listener.
		if (areaWithSubAreasListener != null) {
			Area area = getSelectedArea();
			
			if (area != null) {
				areaWithSubAreasListener.accept(area);
			}
		}
	}
	

	/**
	 * Get sun paths.
	 * @param node
	 * @param treePaths
	 */
	private void getSubPaths(DefaultMutableTreeNode node,
			LinkedList<TreePath> treePaths) {
		
		// Add this node path.
		TreeNode [] nodePath = node.getPath();
		TreePath treePath = new TreePath(nodePath);
		treePaths.add(treePath);
		
		// Do loop for all sub nodes.		
		Enumeration<? super TreeNode> childNodes = node.children();
		while (childNodes.hasMoreElements()) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodes.nextElement();
			
			// Call this method recursively.
			getSubPaths(childNode, treePaths);
		}
	}


	/**
	 * Add area popup trayMenu.
	 */
	private void addAreaPopupMenu() {
		
		final Component thisComponent = this;
		
		AreaLocalMenu localMenu = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				
				return getSelectedArea();
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}

		});
		
		localMenu.setHint(localMenuHint);
		
		JMenuItem menuSelectSubNodes = new JMenuItem(
				Resources.getString("org.multipage.generator.menuSelectSubNodes"));
		menuSelectSubNodes.setIcon(Images.getIcon("org/multipage/generator/images/select_subnodes.png"));
		menuSelectSubNodes.addActionListener(e -> {
			
			selectNodeWithSubNodes();
		});
		
		JMenuItem menuAddSubArea = new JMenuItem(
				Resources.getString("org.multipage.generator.menuAddSubArea"));
		menuAddSubArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
		menuAddSubArea.addActionListener(e -> {
			
			onAddSubArea();
		});
		
		JMenuItem menuRemoveArea = new JMenuItem(
				Resources.getString("org.multipage.generator.menuRemoveArea"));
		menuRemoveArea.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		menuRemoveArea.addActionListener(e -> {
			
			onRemoveArea();
		});
		
		int index = 0;
		popupMenu.insert(menuSelectSubNodes, index++);
		popupMenu.insert(menuAddSubArea, index++);
		popupMenu.insert(menuRemoveArea, index++);
		popupMenu.addSeparator(); index++;
		
		localMenu.addTo(this, popupMenu);
	}
	
	/**
	 * On remove area.
	 */
	protected void onRemoveArea() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Get parent area.
		TreePath path = selectedPaths[0];
		int elementsCount = path.getPathCount();
		if (elementsCount < 2) {
			Utility.show(this, "org.multipage.generator.messageCannotRemoveRootArea");
			return;
		}
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getPathComponent(elementsCount - 2);
		Area parentArea = (Area) parentNode.getUserObject();
		
		// Get selected area.
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		Area area = (Area) node.getUserObject();
		
		AreaShapes areaShapes = (AreaShapes) area.getUser();
		HashSet<AreaShapes> shapesSet = new HashSet<AreaShapes>();
		shapesSet.add(areaShapes);
		
		// Remove area.
		GeneratorMainFrame.getVisibleAreasDiagram().removeDiagramArea(shapesSet, parentArea, this);
		SwingUtilities.invokeLater(() -> {
			updateData();
		});
	}
	
	/**
	 * On add sub area.
	 */
	public void onAddSubArea() {
		
		// Get selected area.
		TreePath [] selectedPaths = tree.getSelectionPaths();
		if (selectedPaths.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		Area parentArea = (Area) node.getUserObject();
		
		// Add new area.
		Obj<Area> newArea = new Obj<Area>();
		if (GeneratorMainFrame.getVisibleAreasDiagram().addNewArea(parentArea, this, newArea, false)) {
		
			// Select and expand area item.
			if (newArea.ref != null) {
				SwingUtilities.invokeLater(() -> {
					
					AreaTreeState.addSelectedAndExpanded(tree, selectedPaths);
					updateData();
				});
			}
		}
	}

	/**
	 * Localize.
	 */
	private void localize() {
		
		Utility.localize(labelAreasTree);
	}

	/**
	 * Create tool bar.
	 */
	private void createToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/expand_icon.png", this, "onExpandTree", "org.multipage.generator.tooltipExpandTree");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/collapse_icon.png", this, "onCollapseTree", "org.multipage.generator.tooltipCollapseTree");
		if (ProgramGenerator.isExtensionToBuilder()) {
			ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png", this, "onAddSubArea", "org.multipage.generator.tooltipAddArea");
		}
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit.png", this, "onEdit", "org.multipage.generator.tooltipEditArea");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png", this, "onUpdate", "org.multipage.generator.tooltipUpdateAreasTree");
	}

	/**
	 * Initialize tree.
	 */
	private void initializeTree() {
		
		treeModel = new DefaultTreeModel(null);
		tree.setModel(treeModel);
		
		tree.setExpandsSelectedPaths(true);
		
		// Set model.
		treeModel = new DefaultTreeModel(null);
		tree.setModel(treeModel);
		
		// Set renderer.
		tree.setCellRenderer(new TreeCellRenderer() {
			
			private long homeAreaId = 0L;
			
			@SuppressWarnings("serial")
			RendererJLabel renderer = new RendererJLabel() {
				{
					homeAreaId = ProgramGenerator.getHomeArea().getId();
				}
			};
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				
				if (!(value instanceof DefaultMutableTreeNode)) {
					renderer.setText("***error***");
					renderer.set(selected, hasFocus, row);
					return renderer;
				}
				
				renderer.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
				
				Object object = ((DefaultMutableTreeNode) value).getUserObject();
				if (object instanceof Area) {
					 Area area = (Area) object;
					 
					 // Set home area icon.
					 if (area.getId() == homeAreaId) {
						 renderer.setIcon(Images.getIcon("org/multipage/generator/images/home_icon_small.png"));
					 }
				}

				renderer.setText(object instanceof Area ? ((Area) object).getDescriptionForDiagram() : object.toString());
				renderer.set(selected, hasFocus, row);
				return renderer;
			}
		});
	}

	/**
	 * Load areas tree.
	 */
	private void loadAreasTree() {
		
		// Get root area.
		if (rootArea == null) {
			rootArea = ProgramGenerator.getArea(0L);
		}
				
		// Get area tree state.
		AreaTreeState treeState = AreaTreeState.getTreeState(tree);
		
		// Create root node.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootArea);
		
		// Create nodes.
		createNodes(rootNode);

		// Set root node.
		treeModel.setRoot(rootNode);
		
		// Apply area tree state.
		AreaTreeState.applyTreeState(treeState, tree);
	}

	/**
	 * Create nodes.
	 * @param parentNode
	 * @param inheritance 
	 */
	private void createNodes(DefaultMutableTreeNode parentNode) {
		
		Object userObject = parentNode.getUserObject();
		if (!(userObject instanceof Area)) {
			return;
		}
		
		Area area = (Area) userObject;
		
		boolean isGenerator = !ProgramGenerator.isExtensionToBuilder();
		
		// Do loop for all sub areas.
		for (Area areaItem : area.getSubareas()) {
			
			// Create new node.
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(areaItem);
			// Add it to the parent node.
			parentNode.add(childNode);
			
			// If it is Generator skip hidden sub areas.
			if (isGenerator) {
				AreaRelation relation = area.getSubRelation(areaItem.getId());
				if (relation == null) {
					continue;
				}
				if (relation.isHideSub()) {
					continue;
				}
			}
			
			// Call this method recursively.
			createNodes(childNode);
		}
	}
	
	/**
	 * Get selected area.
	 * @return
	 */
	public Area getSelectedArea() {
		
		LinkedList<Area> areas = getSelectedAreas();
		if (areas.isEmpty()) {
			return null;
		}
		Area area = areas.getFirst();
		return area;
	}
	
	/**
	 * Get selected areas.
	 * @return
	 */
	public LinkedList<Area> getSelectedAreas() {
		
		// Get selected paths.
		TreePath [] paths = tree.getSelectionPaths();
		if (paths == null) {
			return null;
		}
		
		// Get areas.
		LinkedList<Area> areas = new LinkedList<Area>();
		for (TreePath path : paths) {
			
			// Get last path item.
			Object component = path.getLastPathComponent();
			if (!(component instanceof DefaultMutableTreeNode)) {
				continue;
			}
			
			Object object = ((DefaultMutableTreeNode) component).getUserObject();
			if (object instanceof Area) {
				Area area = (Area) object;
				areas.add(area);
			}
		}
		
		return areas;
	}
	
	
	/**
	 * On expand all.
	 */
	public void onExpandTree() {
		
		Utility.expandSelected(tree, true);
	}
	
	/**
	 * On collapse all.
	 */
	public void onCollapseTree() {
		
		Utility.expandSelected(tree, false);
	}

	/**
	 * Select tree area.
	 * @param area
	 */
	public void selectArea(Area area) {
		
		if (treeModel == null) {
			return;
		}
		
		// Get root node.
		Object rootObject = treeModel.getRoot();
		if (!(rootObject instanceof DefaultMutableTreeNode)) {
			return;
		}
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;
		
		// Traverse tree breadth first.
		Enumeration<? extends TreeNode> enumeration = rootNode.breadthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object userObject = node.getUserObject();
			
			if (userObject instanceof Area) {
				Area areaItem = (Area) userObject;
				
				if (areaItem.equals(area)) {
					
					// Select tree node.
					TreeNode [] nodePath = node.getPath();
					TreePath selectionPath = new TreePath(nodePath);
					tree.setSelectionPath(selectionPath);
					
					return;
				}
			}
		}
	}

	/**
	 * Update data.
	 */
	private void updateData() {
		
		if (rootArea != null) {
			rootArea = ProgramGenerator.getArea(rootArea.getId());
		}
		
		loadAreasTree();
	}

	/**
	 * On update.
	 */
	public void onUpdate() {
		
		updateData();
	}
	
	/**
	 * On add area.
	 */
	public void onAddArea() {
		
		// Get selected area.
		Area area = getSelectedArea();
		if (area == null) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Get areas diagram and add new area.
		AreasDiagram diagram = GeneratorMainFrame.getFrame().getVisibleAreasEditor().getDiagram();
		
		Area newArea = new Area();
		diagram.addNewAreaConservatively(area, newArea, this);
		
		updateData();
		
		long areaId = area.getId();
		ConditionalEvents.transmit(AreasTreePanel.this, Signal.addArea, areaId);
	}
	
	/**
	 * On edit area.
	 */
	public void onEdit() {
		
		// Get selected area.
		Area area = getSelectedArea();
		if (area == null) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Execute area editor.
		AreaEditorFrame.showDialog(null, area);
	}
	
	/**
	 * Show trayMenu.
	 * @param e
	 * @param popup 
	 */
	protected void showMenu(MouseEvent e, JPopupMenu popup) {
		
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e, popup);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e, popup);
				}
			}
		});
	}

	/**
	 * Area selection listener.
	 */
	public void setSelectionListener(Consumer<LinkedList<Area>> areasListener) {
		
		this.areasListener = areasListener;
	}
	
	/**
	 * Area with sub areas selection listener.
	 */
	public void setSelectionWithSubListener(Consumer<Area> areaListener) {
		
		this.areaWithSubAreasListener = areaListener;
	}
}
