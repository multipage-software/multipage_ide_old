/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 06-04-2020
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.DefaultMutableTreeNodeDnD;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.GuiSignal;
import org.multipage.gui.Images;
import org.multipage.gui.JTreeDnD;
import org.multipage.gui.JTreeDndCallback;
import org.multipage.gui.Message;
import org.multipage.gui.NonCyclingReceiver;
import org.multipage.gui.SignalGroup;
import org.multipage.gui.TabPanelComponent;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.UpdateSignal;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.Area;
import com.maclan.AreaTreeState;
import com.maclan.AreasModel;

/**
 * 
 * @author
 *
 */
public class AreasTreeEditorPanel extends JPanel implements TabPanelComponent, TabContainerComponent, NonCyclingReceiver {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * States.
	 */
	private static boolean loadSuperAreasState = false;
	private static boolean loadAliasesState = false;
	private static int selectedTabIndexState = 0;
	private static boolean inheritState = false;
	private static boolean showIdsState = false;
	private static boolean caseSensitiveState = false;
	private static boolean wholeWordsState = false;
	private static boolean exactMatchState = false;
	private static String filterState = "";
	private static String levelsState = "";
	private static Rectangle bounds = new Rectangle();
		
	/**
	 * List renderer.
	 * @author
	 *
	 */
	@SuppressWarnings("serial")
	class ItemRendererImpl extends JLabel {

		private boolean isSelected;
		private boolean cellHasFocus;
		private boolean isVisible = false;
		
		ItemRendererImpl() {
			
			setOpaque(true);
		}
		
		public void setProperties(String text, Color textColor, String subName, String superName, boolean hiddenSubareas, int index,
				boolean isSelected, boolean cellHasFocus, boolean isHomeArea) {

			setIcon(Images.getIcon(
					isHomeArea ? (isVisible ? "org/multipage/generator/images/home_icon_small.png"
							: "org/multipage/generator/images/home_icon_small_unvisible.png")
							: (isVisible ? "org/multipage/generator/images/area_node.png"
									: "org/multipage/generator/images/area_node_unvisible.png")));
			
			if (text.isEmpty()) {
				text = Resources.getString("org.multipage.generator.textUnknownAlias");
				setForeground(Color.LIGHT_GRAY);
			}
			else {
				setForeground(Color.BLACK);
			}
			
			//String outputText = "<b>" + text + "</b>";
			String outputText = text;
			String outputTextAddition = "";
			
			final boolean isBuilder = ProgramGenerator.isExtensionToBuilder();
			
			if (!subName.isEmpty()) {
				outputTextAddition += " <sup>↓</sup> <font color=gray>" + subName + "</font>";
			}
			if (!superName.isEmpty() && isBuilder) {
				outputTextAddition += " <sup>↑</sup> <font color=gray>" + superName + "</font>";
			}
			if (hiddenSubareas && isBuilder) {
				String textAux = Resources.getString("org.multipage.generator.textHasMoreInfo");
				outputTextAddition += String.format(" <font color=\"red\", style=\"font-size: 70%%\">%s</font>",
						textAux);
			}
			setText(String.format("<html>%s&nbsp;&nbsp;%s</html>", outputText, outputTextAddition));
			setForeground(textColor);
			setBackground(Utility.itemColor(index));
			this.isSelected = isSelected;
			this.cellHasFocus = cellHasFocus;
		}
		
		public void setProperties(String text, Color colorText, int index,
				boolean isSelected, boolean cellHasFocus, boolean isHomeArea) {

			setIcon(Images.getIcon(isHomeArea ? "org/multipage/generator/images/home_icon_small.png" : "org/multipage/generator/images/area_node.png"));

			if (text.isEmpty()) {
				text = Resources.getString("org.multipage.generator.textUnknownAlias");
				setForeground(Color.LIGHT_GRAY);
			}
			else {
				setForeground(Color.BLACK);
			}
			setText(text);
			setForeground(colorText);
			setBackground(Utility.itemColor(index));
			this.isSelected = isSelected;
			this.cellHasFocus = cellHasFocus;
		}
		@Override
		public void paint(Graphics g) {
			
			super.paint(g);
			GraphUtility.drawSelection(g, this, isSelected, cellHasFocus);
		}

		public void setAreaVisible(boolean isVisible) {
			
			this.isVisible = isVisible;
		}
	}
	
	/**
	 * List of selected area IDs.
	 */
	private HashSet<Long> selectedTreeAreaIds = new HashSet<Long>();
	private HashSet<Long> selectedListAreaIds = new HashSet<Long>();
	
	/**
	 * Area ID.
	 */
	private long areaId;

	/**
	 * List renderer.
	 */
	private ItemRendererImpl itemRenderer;

	/**
	 * List model.
	 */
	private DefaultListModel listModel;
	
	/**
	 * Tree model.
	 */
	private DefaultTreeModel treeModel;

	/**
	 * New area ID path.
	 */
	private Long[] newAreaIdPath;

	/**
	 * New area added.
	 */
	private Long newAreaAddedId;
	
	/**
	 * Toggle button for sub areas and super areas.
	 */
	private JToggleButton buttonSuperAreas;
	
	/**
	 * Toggle areas' descriptions and aliases.
	 */
	private JToggleButton buttonAliases;
	
	/**
	 * Button show ID.
	 */
	private JToggleButton buttonShowIds;
	
	/**
	 * List of previous update messages which is used to determine infinite message loops.
	 */
	private LinkedList<Message> previousMessages = new LinkedList<Message>();
	
	/**
	 * Inheritance for super areas.
	 */
	private JCheckBox checkInherits = new JCheckBox("org.multipage.generator.textInherits");
	private JTabbedPane tabbedPane;
	private JPanel panelList;
	private JPanel panelTree;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWords;
	private JCheckBox checkExactMatch;
	private JScrollPane scrollList;
	private JList list;
	private JScrollPane scrollTree;
	private JTreeDnD tree;
	private JLabel labelLevels;
	private JTextField textLevels;
	private JLabel labelFoundAreasCount;
	private JToolBar toolBarTree;
	private JPopupMenu popupMenuList;
	private JPopupMenu popupMenuTree;
	private JMenuItem menuSelectSubNodes;
	private JMenuItem menuAddSubArea;
	private JMenuItem menuRemoveArea;
	
	/**
	 * Create the frame.
	 * @param areaId 
	 */
	public AreasTreeEditorPanel(long areaId) {
		setMinimumSize(new Dimension(400, 350));

		// Initialize components.
		initComponents();
		// $hide>>$
		this.areaId = areaId;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {

		setBounds(100, 100, 693, 540);
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onTreeListTabChange();
			}
		});
		add(tabbedPane);
		
		panelTree = new JPanel();
		tabbedPane.addTab("tree", null, panelTree, null);
		SpringLayout sl_panelTree = new SpringLayout();
		panelTree.setLayout(sl_panelTree);
		
		scrollTree = new JScrollPane();
		scrollTree.setBorder(null);
		sl_panelTree.putConstraint(SpringLayout.NORTH, scrollTree, 0, SpringLayout.NORTH, panelTree);
		sl_panelTree.putConstraint(SpringLayout.WEST, scrollTree, 0, SpringLayout.WEST, panelTree);
		sl_panelTree.putConstraint(SpringLayout.EAST, scrollTree, 0, SpringLayout.EAST, panelTree);
		panelTree.add(scrollTree);
		
		tree = new JTreeDnD();
		scrollTree.setViewportView(tree);
		
		popupMenuTree = new JPopupMenu();
		addPopup(tree, popupMenuTree);
		
		menuSelectSubNodes = new JMenuItem("org.multipage.generator.menuSelectSubNodes");
		menuSelectSubNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNodeWithSubNodes();
			}
		});
		popupMenuTree.add(menuSelectSubNodes);
		
		menuAddSubArea = new JMenuItem("org.multipage.generator.menuAddSubArea");
		menuAddSubArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddSubArea();
			}
		});
		popupMenuTree.add(menuAddSubArea);
		
		menuRemoveArea = new JMenuItem("org.multipage.generator.menuRemoveArea");
		menuRemoveArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveArea();
			}
		});
		popupMenuTree.add(menuRemoveArea);
		
		popupMenuTree.addSeparator();
		
		toolBarTree = new JToolBar();
		sl_panelTree.putConstraint(SpringLayout.SOUTH, scrollTree, 0, SpringLayout.NORTH, toolBarTree);
		sl_panelTree.putConstraint(SpringLayout.WEST, toolBarTree, 0, SpringLayout.WEST, panelTree);
		sl_panelTree.putConstraint(SpringLayout.SOUTH, toolBarTree, 0, SpringLayout.SOUTH, panelTree);
		toolBarTree.setFloatable(false);
		panelTree.add(toolBarTree);
		
		panelList = new JPanel();
		panelList.setBackground(Color.WHITE);
		tabbedPane.addTab("org.multipage.generator.textListAreas", null, panelList, null);
		SpringLayout sl_panelList = new SpringLayout();
		panelList.setLayout(sl_panelList);
		
		labelFilter = new JLabel("org.multipage.generator.textFilter");
		sl_panelList.putConstraint(SpringLayout.NORTH, labelFilter, 10, SpringLayout.NORTH, panelList);
		sl_panelList.putConstraint(SpringLayout.WEST, labelFilter, 10, SpringLayout.WEST, panelList);
		panelList.add(labelFilter);
		
		textFilter = new JTextField();
		sl_panelList.putConstraint(SpringLayout.NORTH, textFilter, 8, SpringLayout.NORTH, panelList);
		sl_panelList.putConstraint(SpringLayout.WEST, textFilter, 6, SpringLayout.EAST, labelFilter);
		sl_panelList.putConstraint(SpringLayout.EAST, textFilter, 197, SpringLayout.EAST, labelFilter);
		panelList.add(textFilter);
		textFilter.setColumns(10);
		
		checkCaseSensitive = new JCheckBox("org.multipage.generator.textCaseSensitive");
		sl_panelList.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 7, SpringLayout.NORTH, panelList);
		checkCaseSensitive.setBackground(Color.WHITE);
		checkCaseSensitive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Reload panel.
				reload();
			}
		});
		panelList.add(checkCaseSensitive);
		
		checkWholeWords = new JCheckBox("org.multipage.generator.textWholeWords");
		checkWholeWords.setBackground(Color.WHITE);
		sl_panelList.putConstraint(SpringLayout.NORTH, checkWholeWords, 0, SpringLayout.NORTH, checkCaseSensitive);
		checkWholeWords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Reload panel.
				reload();
			}
		});
		sl_panelList.putConstraint(SpringLayout.WEST, checkWholeWords, 6, SpringLayout.EAST, checkCaseSensitive);
		panelList.add(checkWholeWords);
		
		checkExactMatch = new JCheckBox("org.multipage.generator.textExactMatch");
		checkExactMatch.setBackground(Color.WHITE);
		sl_panelList.putConstraint(SpringLayout.NORTH, checkExactMatch, 0, SpringLayout.NORTH, checkCaseSensitive);
		checkExactMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Reload panel.
				reload();
			}
		});
		sl_panelList.putConstraint(SpringLayout.WEST, checkExactMatch, 6, SpringLayout.EAST, checkWholeWords);
		panelList.add(checkExactMatch);
		
		scrollList = new JScrollPane();
		scrollList.setBorder(null);
		sl_panelList.putConstraint(SpringLayout.NORTH, scrollList, 0, SpringLayout.SOUTH, checkCaseSensitive);
		sl_panelList.putConstraint(SpringLayout.WEST, scrollList, 0, SpringLayout.WEST, panelList);
		sl_panelList.putConstraint(SpringLayout.EAST, scrollList, 0, SpringLayout.EAST, panelList);
		panelList.add(scrollList);
		
		list = new JList();
		scrollList.setViewportView(list);
		
		popupMenuList = new JPopupMenu();
		addPopup(list, popupMenuList);
		
		labelLevels = new JLabel("org.multipage.generator.textLevels");
		sl_panelList.putConstraint(SpringLayout.NORTH, labelLevels, 0, SpringLayout.NORTH, labelFilter);
		sl_panelList.putConstraint(SpringLayout.WEST, labelLevels, 6, SpringLayout.EAST, textFilter);
		panelList.add(labelLevels);
		
		textLevels = new JTextField();
		sl_panelList.putConstraint(SpringLayout.NORTH, textLevels, 0, SpringLayout.NORTH, textFilter);
		sl_panelList.putConstraint(SpringLayout.WEST, checkCaseSensitive, 30, SpringLayout.EAST, textLevels);
		sl_panelList.putConstraint(SpringLayout.WEST, textLevels, 6, SpringLayout.EAST, labelLevels);
		sl_panelList.putConstraint(SpringLayout.EAST, textLevels, 51, SpringLayout.EAST, labelLevels);
		textLevels.setColumns(10);
		panelList.add(textLevels);
		
		labelFoundAreasCount = new JLabel("org.multipage.generator.textFoundAreasCount");
		sl_panelList.putConstraint(SpringLayout.SOUTH, scrollList, 0, SpringLayout.NORTH, labelFoundAreasCount);
		sl_panelList.putConstraint(SpringLayout.WEST, labelFoundAreasCount, 0, SpringLayout.WEST, panelList);
		sl_panelList.putConstraint(SpringLayout.SOUTH, labelFoundAreasCount, 0, SpringLayout.SOUTH, panelList);
		panelList.add(labelFoundAreasCount);
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
		GeneratorMainFrame.getVisibleAreasDiagram().removeArea(shapesSet, parentArea, this);
		
		reload();
	}

	/**
	 * On add sub area.
	 */
	protected void onAddSubArea() {
		
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
					
					selectAndExpandNewArea(newArea.ref.getId(), true);
				});
			}
		}
	}

	/**
	 * Select and expand new area tree item.
	 * @param newAreaId
	 * @param select 
	 */
	private void selectAndExpandNewArea(Long newAreaId, boolean select) {
		
		// Save expand state.
		TreePath selectedPath = tree.getSelectionPath();
		if (selectedPath != null) {
			
			int count = selectedPath.getPathCount();
			newAreaIdPath = new Long [count];
			
			for (int index = 0; index < count; index++) {
				
				DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) selectedPath.getPathComponent(index);
				Object object = mutableNode.getUserObject();
				if (object instanceof Area) {
					
					Area area = (Area) object;
					newAreaIdPath[index] = area.getId();
				}
			}
		}
		
		newAreaAddedId = select ? newAreaId : null;
		reload();
	}

	/**
	 * Select area with sub nodes.
	 */
	protected void selectNodeWithSubNodes() {
		
		// Get selected path.
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
	 * Returns true value if the IDs have to be visible.
	 * @return
	 */
	private boolean showIds() {
		
		boolean showIds = buttonShowIds.isSelected();
		return showIds;
	}
	
	/**
	 * Post create.
	 */
	private void postCreate() {
		
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Create tool bar.
		createToolBars();
		// Load dialog.
		loadDialog();
		// Create tree.
		createTree();
		// Create list.
		createList();
		// Create popup menus.
		createPopupMenus();
		// Set listeners.
		setListeners();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		buttonAliases.setSelected(loadAliasesState);
		buttonSuperAreas.setSelected(loadSuperAreasState);
		tabbedPane.setSelectedIndex(selectedTabIndexState);
		checkInherits.setSelected(inheritState);
		buttonShowIds.setSelected(showIdsState);
		checkCaseSensitive.setSelected(caseSensitiveState);
		checkWholeWords.setSelected(wholeWordsState);
		checkExactMatch.setSelected(exactMatchState);
		textFilter.setText(filterState);
		textLevels.setText(levelsState);
		
		if (bounds.isEmpty()) {
			// Center dialog.
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
	}

	/**
	 * Create popup menus.
	 */
	private void createPopupMenus() {
		
		// Create new area trayMenu.
		final Component thisComponent = this;
		
		// Create new area trayMenu.
		AreaLocalMenu areaMenuList = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				Object [] selected = list.getSelectedValues();
				if (selected.length != 1) {
					return null;
				}
				Area area = (Area) selected[0];
				return ProgramGenerator.getAreasModel().getArea(area.getId());
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		AreaLocalMenu areaMenuTree = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				TreePath [] selectedPaths = tree.getSelectionPaths();
				if (selectedPaths.length != 1) {
					return null;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent();
				Area area = (Area) node.getUserObject();
				return ProgramGenerator.getAreasModel().getArea(area.getId());
			}

			@Override
			public Area getCurrentParentArea() {
				
				// Get selected area and its parent.
				TreePath [] selectedPaths = tree.getSelectionPaths();
				if (selectedPaths.length != 1) {
					return null;
				}
				
				TreePath selectedPath = selectedPaths[0];
				int elementsCount = selectedPath.getPathCount();
				
				if (elementsCount < 2) {
					return null;
				}

				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedPath.getPathComponent(elementsCount - 2);
				Area parentArea = (Area) parentNode.getUserObject();
				return ProgramGenerator.getAreasModel().getArea(parentArea.getId());
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}

			@Override
			public void onNewArea(Long newAreaId) {
				
				if (newAreaId == null) {
					return;
				}

				// Select new area (imported).
				GeneratorMainFrame.getVisibleAreasDiagram().removeSelection();
				GeneratorMainFrame.getVisibleAreasDiagram().select(newAreaId, true, false);
				
				selectAndExpandNewArea(newAreaId, false);
			}
		});
		// Add new popup menu items.
		areaMenuTree.addTo(this, popupMenuTree);
		areaMenuTree.disableMenuItems(
				// List of disabled menu items.
				areaMenuTree.menuAddToFavoritesArea,
				areaMenuTree.menuFocusSuperArea,
				areaMenuTree.menuFocusNextArea,
				areaMenuTree.menuFocusPreviousArea,
				areaMenuTree.menuFocusTabTopArea
			);
		areaMenuList.addTo(this, popupMenuList);
	}

	/**
	 * Create tool bar.
	 */
	private void createToolBars() {
		
		// Area tree tool bar.
		ToolBarKit.addToolBarButton(toolBarTree, "org/multipage/generator/images/expand_icon.png", this, "onExpandTree", "org.multipage.generator.tooltipExpandTree");
		ToolBarKit.addToolBarButton(toolBarTree, "org/multipage/generator/images/collapse_icon.png", this, "onCollapseTree", "org.multipage.generator.tooltipCollapseTree");
		
		// Add tool bar controls.
		buttonSuperAreas = ToolBarKit.addToggleButton(toolBarTree, "org/multipage/generator/images/superareas.png", this, "onToggleSubSuper", "org.multipage.generator.tooltipToggleSubSuperAreas");
		buttonAliases = ToolBarKit.addToggleButton(toolBarTree, "org/multipage/generator/images/description_alias.png", this, "onToggleDescriptionsAliases", "org.multipage.generator.tooltipToggleDescriptionsAliases");
		buttonShowIds = ToolBarKit.addToggleButton(toolBarTree, "org/multipage/generator/images/show_hide_id.png", this, "onToggleShowIds", "org.multipage.generator.tooltipToggleShowIds");
		toolBarTree.add(checkInherits);
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
	 * On toggle sub and super areas.
	 */
	public void onToggleSubSuper() {
		
		// Reload panel.
		reload();
	}
	
	/**
	 * On toggle descriptions and aliases of areas.
	 */
	public void onToggleDescriptionsAliases() {
		
		// Reload panel.
		reload();
	}
	
	/**
	 * On toggle IDs of areas.
	 */
	public void onToggleShowIds() {
		
		// Reload panel.
		reload();
	}
	
	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		DocumentListener listener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				reload();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				reload();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				reload();
			}
		};
		
		textFilter.getDocument().addDocumentListener(listener);
		textLevels.getDocument().addDocumentListener(listener);
		
		// On tree item selection.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		    	
		    	if (!isVisible()) {
		    		return;
		    	}
		    	
		    	onSelectedTreeItem();
		    	
		    	// Propagate event.
		    	// TODO: <---REFACTOR EVENTS
		    	//Event.propagate(AreasTreeEditorPanel.this, Event.selectTreeArea, selectedTreeAreaIds);
		    }
		});
		
		// Set list selection listener.
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
		    	
				if (!isVisible()) {
		    		return;
		    	}
				
				onSelectedListItem();
				
		    	// Propagate event.
				// TODO: <---REFACTOR EVENTS
		    	//Event.propagate(AreasTreeEditorPanel.this, Event.selectListArea, selectedListAreaIds);
			}
		});
		
		// TODO: <---COPY to new version
		// Receive "update areas" messages.
		ApplicationEvents.receiver(this, SignalGroup.create(UpdateSignal.updateAreasModel, UpdateSignal.updateAreasTreeEditor), message -> {
			
			reload();
			tree.updateUI();
		});
		
		// Add area view state event listener.
		// TODO: <---REFACTOR EVENTS
		/*Event.receiver(this, ActionGroup.areaViewStateChange, (data) -> {
			
			boolean isShowing = AreasTreeEditorPanel.this.isShowing();
			if (isShowing) {
				
				if (Event.is(data, Event.selectAll)) {
					
					setAllSelection(true);
				}
				else if (Event.is(data, Event.unselectAll)) {
					setAllSelection(false);
				}
			}
		});
		
		// Add area view event listener.
		Event.receiver(this, ActionGroup.areaViewChange, (data) -> {
			
			reload();
		});*/
	}
	
	/**
	 * Removes attached listeners.
	 */
	private void removeListeners() {
		
		// Remove event listener.
		ApplicationEvents.removeReceivers(this);
	}
	
	/**
	 * On tree item selection.
	 */
	private void onSelectedTreeItem() {
		
		selectedTreeAreaIds.clear();
		
		// Get selected areas.
    	LinkedList<Area> areas = new LinkedList<Area>();
    	TreePath [] paths = tree.getSelectionPaths();
    	
    	if (paths != null) {
    	
	    	// Do loop for all paths and avoid duplicate areas.
	    	for (TreePath path : paths) {
	    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
	    		
	    		Area area = (Area) node.getUserObject();
	    		long areaId = area.getId();
	    		
	    		// If the area is already in list, continue loop.
	    		boolean isNewArea = true;
	    		
	    		for (Area item : areas) {
	    			if (item.getId() == areaId) {
	    				isNewArea = false;
	    			}
	    		}
	    		
	    		if (isNewArea) {
	    			areas.add(area);
	    			selectedTreeAreaIds.add(areaId);
	    		}
	    	}
    	}
    	
    	// Transmit area selection signal.
    	ApplicationEvents.transmit(this, GuiSignal.selectTreeAreas, selectedTreeAreaIds);
	}

	/**
	 * On list item selection.
	 */
	private void onSelectedListItem() {
		
		selectedListAreaIds.clear();
		
		// Get selected areas.
		for (Object item : list.getSelectedValuesList()) {
			if (item instanceof Area) {
				Area area = (Area) item;
				selectedListAreaIds.add(area.getId());
			}
		}
		
    	// Transmit area selection signal.
    	ApplicationEvents.transmit(this, GuiSignal.selectDiagramAreas, selectedListAreaIds);
	}
	
	/**
	 * Get selected area IDs depending on current (panel tree or list).
	 * @return
	 */
	public HashSet<Long> getSelectedAreaIds() {
		
		int tab = tabbedPane.getSelectedIndex();
		if (tab == 1) {
			return selectedListAreaIds;
		}
		return selectedTreeAreaIds;
	}
	
	/**
	 * Create tree.
	 */
	private void createTree() {

		tree.setExpandsSelectedPaths(true);
		
		// Set model.
		treeModel = new DefaultTreeModel(null);
		tree.setModel(treeModel);
		// Set renderer.
		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {

				itemRenderer.setForeground(Color.BLACK);
				
				if (!(value instanceof DefaultMutableTreeNode)) {
					itemRenderer.setProperties("#renderer error#", Color.BLACK, 0, selected, hasFocus, false);
					return itemRenderer;
				}
				
				// Get tree nodes.
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
				DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) treeNode.getParent();
				
				// Get DnD mark.
				boolean dndMark = false;
				if (value instanceof DefaultMutableTreeNodeDnD) {
					
					DefaultMutableTreeNodeDnD dndNode = (DefaultMutableTreeNodeDnD) value;
					dndMark = dndNode.isMarked();
				}
				selected = selected || dndMark;
				
				// Get area.
				Area area = (Area) treeNode.getUserObject();
				boolean isHomeArea = ProgramGenerator.getAreasModel().isHomeArea(area);
				boolean isVisible = area.isVisible();
				boolean isDisabled = !area.isEnabled();
				
				itemRenderer.setAreaVisible(isVisible);
				
				// Get area text.
				boolean showIds = showIds();
				String alias = area.getAlias(showIds);
				boolean isEmptyAlias = alias == null;
				boolean isDescription = !buttonAliases.isSelected();
				if (isEmptyAlias) {
					long areaId = area.getId();
					alias = (showIds ? String.format("[%d] ", areaId) : "")
							+ Resources.getString("org.multipage.generator.textUnknownAlias");
				}
				
				String text = isDescription ? area.getDescriptionForced(showIds) : alias;
				Color colorText = (!isDescription && isEmptyAlias ? Color.LIGHT_GRAY : Color.BLACK);
						
				// Get sub relation names.
				if (parentTreeNode != null) {
					Area parentArea = (Area) parentTreeNode.getUserObject();
	
					String subName = parentArea.getSubRelationName(area.getId());
					String superName = area.getSuperRelationName(parentArea.getId());
					boolean hiddenSubareas = parentArea.isSubareasHidden(area);
					
					itemRenderer.setProperties(text, colorText, subName, superName, hiddenSubareas, 0, selected, hasFocus, isHomeArea);
				}
				else {
					itemRenderer.setProperties(text, colorText, 0, selected, hasFocus, isHomeArea);
				}
				
				// If the area is disabled, gray its name
				if (isDisabled) {
					itemRenderer.setForeground(Color.LIGHT_GRAY);
				}
				
				Object userObject = area.getUser();
				if (userObject instanceof AreaShapes) {
					
					boolean isSelected = ((AreaShapes) userObject).isSelected();
					if (isSelected) {
						
						itemRenderer.setForeground(Color.RED);
					}
				}
				return itemRenderer;
			}
		});
		
		final Component thisComponent = this;
		
		// Set Drag and Drop callback.
		tree.setDragAndDropCallback(new JTreeDndCallback() {

			@Override
			public void onNodeDropped(
					DefaultMutableTreeNodeDnD droppedDndNode,
					TreeNode droppedNodeParent,
					DefaultMutableTreeNodeDnD transferedDndNode,
					TreeNode transferredNodeParent,
					DropTargetDropEvent e) {
				
				// Get transferred area, target area, parent areas and action number and do an action.
				Object transferredObject = transferedDndNode.getUserObject();
				Object droppedObject = droppedDndNode.getUserObject();
				
				if (!(transferredObject instanceof Area && droppedObject instanceof Area)) {
					e.rejectDrop();
					return;
				}
				
				Area transferredParentArea = null;
				if (transferredNodeParent instanceof DefaultMutableTreeNode) {
					
					DefaultMutableTreeNode transferredMutableNodeParent = (DefaultMutableTreeNode) transferredNodeParent;
					Object userObject = transferredMutableNodeParent.getUserObject();
					
					if (userObject instanceof Area) {
						transferredParentArea = (Area) userObject;
					}
				}
				
				Area droppedParentArea = null;
				if (droppedNodeParent instanceof DefaultMutableTreeNode) {
					
					DefaultMutableTreeNode droppedMutableNodeParent = (DefaultMutableTreeNode) droppedNodeParent;
					Object userObject = droppedMutableNodeParent.getUserObject();
					
					if (userObject instanceof Area) {
						droppedParentArea = (Area) userObject;
					}
				}
				
				int action = e.getDropAction();
				
				GeneratorMainFrame.transferArea(
						(Area)transferredObject, transferredParentArea,
						(Area)droppedObject, droppedParentArea,
						action, thisComponent);
				
				reload();
			}
		});
	}

	/**
	 * Create list.
	 */
	private void createList() {
		
		// Set renderer.
		itemRenderer = new ItemRendererImpl();
		list.setCellRenderer(new ListCellRenderer() {
			// Get list renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				itemRenderer.setForeground(Color.BLACK);
				
				if (!(value instanceof Area)) {
					return null;
				}
				Area area = (Area) value;
				
				// Get area text.
				boolean showIds = showIds();
				String alias = area.getAlias(showIds);
				boolean isEmptyAlias = alias == null;
				boolean isDescription = !buttonAliases.isSelected();
				boolean isHomeArea = ProgramGenerator.getAreasModel().isHomeArea(area);
				
				if (isEmptyAlias) {
					long areaId = area.getId();
					alias = (showIds ? String.format("[%d] ", areaId) : "")
							+ Resources.getString("org.multipage.generator.textUnknownAlias");
				}
				
				String text  = isDescription ? area.getDescriptionForced(showIds()) : alias;
				Color colorText = (!isDescription && isEmptyAlias ? Color.LIGHT_GRAY : Color.BLACK);
				
				itemRenderer.setProperties(text, colorText, index, isSelected, cellHasFocus, isHomeArea);
				
				Object userObject = area.getUser();
				if (userObject instanceof AreaShapes) {
					
					boolean isAreaSelected = ((AreaShapes) userObject).isSelected();
					if (isAreaSelected) {
						
						itemRenderer.setForeground(Color.RED);
					}
				}
				return itemRenderer;
			}
		});
		// Set model.
		listModel = new DefaultListModel();
		list.setModel(listModel);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		tabbedPane.setIconAt(1, Images.getIcon("org/multipage/generator/images/list.png"));
		menuSelectSubNodes.setIcon(Images.getIcon("org/multipage/generator/images/select_subnodes.png"));
		menuAddSubArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
		menuRemoveArea.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(checkInherits);
		Utility.localize(labelLevels);
		Utility.localize(tabbedPane);
		Utility.localize(labelFilter);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(menuSelectSubNodes);
		Utility.localize(menuAddSubArea);
		Utility.localize(menuRemoveArea);
	}

	/**
	 * On close window.
	 */
	protected void onCloseWindow() {
		
		saveDialog();
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		loadAliasesState = buttonAliases.isSelected();
		loadSuperAreasState = buttonSuperAreas.isSelected();
		selectedTabIndexState = tabbedPane.getSelectedIndex();
		inheritState = checkInherits.isSelected();
		showIdsState = buttonShowIds.isSelected();
		caseSensitiveState = checkCaseSensitive.isSelected();
		wholeWordsState = checkWholeWords.isSelected();
		exactMatchState = checkExactMatch.isSelected();
		filterState = textFilter.getText();
		levelsState = textLevels.getText();
		bounds = getBounds();
	}

	/**
	 * Update panel.
	 */
	@Override
	public void reload() {
		
		SwingUtilities.invokeLater(() -> {
			
			// Set tab icon and text.
			boolean isSubareas = !buttonSuperAreas.isSelected();
			String iconPath = "org/multipage/generator/images/" + (isSubareas ? "subareas" 
					: "superareas") + ".png";
			
			tabbedPane.setIconAt(0, Images.getIcon(iconPath));
			tabbedPane.setTitleAt(0, Resources.getString(
					isSubareas ? "org.multipage.generator.textSubAreasTree" : "org.multipage.generator.textSuperAreasTree"));
			
			// Get selected tab.
			boolean isList = tabbedPane.getSelectedIndex() == 1;
			// Get selected text type.
			boolean isDescription = !buttonAliases.isSelected();
			// Get inheritance.
			boolean inheritance = checkInherits.isSelected();
			// Get number of levels.
			String levelsText = textLevels.getText();
			int levels = 0;
			try {
				levels = Integer.parseInt(levelsText);
			}
			catch (Exception e) {
			}
			// Get filter.
			String filterText = textFilter.getText();
			boolean caseSensitive = checkCaseSensitive.isSelected();
			boolean wholeWord = checkWholeWords.isSelected();
			boolean exactMatch = checkExactMatch.isSelected();
	
			// Set inheritance and Drag and Drop.
			checkInherits.setEnabled(!isSubareas);
			tree.enableDragAndDrop(isSubareas);
		
			// Update tree.
			if (!isList) {
				
				// Get tree state.
				AreaTreeState treeState = AreaTreeState.getTreeState(tree);
				if (newAreaIdPath != null) {
					
					treeState.addExpandedAreaId(newAreaIdPath);
					
					if (newAreaAddedId != null) {
						
						int count = newAreaIdPath.length;
						Long [] path = new Long[count + 1];
						
						for (int index = 0; index < count; index++) {
							path[index] = newAreaIdPath[index];
						}
						path[count] = newAreaAddedId;
						treeState.clearSelected();
						treeState.addSelectedAreaId(path);
						
						newAreaAddedId = null;
					}
					
					newAreaIdPath = null;
				}
				
				// Load tree.
				updateTreeModel(treeModel, areaId, isSubareas, inheritance);
							
				// Apply tree state.
				AreaTreeState.applyTreeState(treeState, tree);
				
				// Expand tree root.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Utility.expandTop(tree, true);
					}
				});
			}
			else {
				// Get current selected items.
				int [] selectedIndices = list.getSelectedIndices();
				// Update list.
				listModel.clear();
				// Get areas.
				AreasModel areasModel = ProgramGenerator.getAreasModel();
				LinkedList<Area> areas = isSubareas ? areasModel.getAreaAndSubAreas(areaId, levels) :
					areasModel.getAreaAndSuperAreas(areaId, levels, inheritance);
				LinkedList<Area> areasSorted = new LinkedList<Area>();
				
				// Load texts.
				for (Area areaItem : areas) {
					
					String alias = areaItem.getAlias();
					String description = areaItem.getDescriptionForced(showIds());
					
					String text = isDescription ? description : alias;
					if (!text.isEmpty()) {
						if (!filterText.isEmpty() && !Utility.matches(text, filterText,
								caseSensitive, wholeWord, exactMatch)) {
							continue;
						}
						areasSorted.add(areaItem);
					}
				}
				
				// Sort texts.
				class AreasComparator implements Comparator<Area> {
					
					boolean isAliases;
					
					public AreasComparator(boolean isAliases) {
						this.isAliases = isAliases;
					}
	
					@Override
					public int compare(Area area1, Area area2) {
						
						String area1Text;
						String area2Text;
						
						if (isAliases) {
							area1Text = area1.getAlias();
							area2Text = area2.getAlias();
						}
						else {
							area1Text = area1.getDescriptionForced(showIds());
							area2Text = area2.getDescriptionForced(showIds());
						}
						return area1Text.compareTo(area2Text);
					}
				}
				
				Collections.sort(areasSorted, new AreasComparator(!isDescription));
				
				// Load list.
				for (Area areaSorted : areasSorted) {
					listModel.addElement(areaSorted);
				}
				
				// Set areas count.
				labelFoundAreasCount.setText(String.format(
						Resources.getString("org.multipage.generator.textFoundAreasCount"), areasSorted.size()));
				
				// Restore selection.
				list.setSelectedIndices(selectedIndices);
			}
		});
	}
	
	/**
	 * Update tree model.
	 * @param treeModel
	 * @param isSubareas
	 * @param inheritance
	 */
	private void updateTreeModel(DefaultTreeModel treeModel, Long rootAreaId, boolean isSubareas,
			boolean inheritance) {
		
		if (rootAreaId == null) {
			// Clear tree model.
			treeModel.setRoot(null);
			return;
		}
		
		// Get root area.
		Area rootArea = ProgramGenerator.getArea(rootAreaId);
		if (rootArea == null) {
			treeModel.setRoot(null);
			return;
		}
		
		// Create root node.
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNodeDnD(rootArea);
		
		// Create nodes.
		createNodes(rootNode, isSubareas, inheritance);
		
		// Set root node.
		treeModel.setRoot(rootNode);
	}

	/**
	 * Create nodes.
	 * @param parentNode
	 * @param inheritance 
	 */
	private void createNodes(DefaultMutableTreeNode parentNode, boolean isSubareas, boolean inheritance) {
		
		Object userObject = parentNode.getUserObject();
		if (!(userObject instanceof Area)) {
			return;
		}
		
		Area area = (Area) userObject;
		
		// Do loop for all sub or super areas.
		LinkedList<Area> areas = null;
		
		if (isSubareas) {
			
			// If the area is disabled, hide its sub areas
			if (!area.isEnabled()) {
				return;
			}
			
			areas = area.getSubareas();
		}
		else {
			if (!inheritance) {
				areas = area.getSuperareas();
			}
			else {
				areas = area.getInheritsFrom();
			}
		}
		
		for (Area areaItem : areas) {
			
			// Create new node.
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNodeDnD(areaItem);
			// Add it to the parent node.
			parentNode.add(childNode);

			if (isSubareas) {
				// If area item sub areas are hidden, continue the loop.
				if (area.isSubareasHidden(areaItem)) {
					continue;
				}
			}
			
			// Call this method recursively.
			createNodes(childNode, isSubareas, inheritance);
		}
	}

	/**
	 * Set all selected.
	 * @param select
	 */
	private void setAllSelected(boolean select) {
		
		int tabIndex = tabbedPane.getSelectedIndex();
		
		if (select) {
			if (tabIndex == 0) {
				tree.addSelectionRow(0);
				selectNodeWithSubNodes();
			}
			else if (tabIndex == 1) {
				int itemCount = listModel.getSize();
				list.setSelectionInterval(0, itemCount - 1);
			}
		}
		else {
			if (tabIndex == 0) {
				tree.clearSelection();
			}
			else if (tabIndex == 1) {
				list.clearSelection();
			}
		}
	}
	
	/**
	 * Show trayMenu.
	 * @param e
	 * @param popup 
	 */
	protected void showMenu(MouseEvent e, JPopupMenu popup) {
		
		if (popup.equals(popupMenuTree)) {
			
			boolean isSubAreas = !buttonSuperAreas.isSelected();
			
			menuAddSubArea.setEnabled(isSubAreas);
			menuRemoveArea.setEnabled(isSubAreas);
		}
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Add popup window.
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
	 * Redraw information.
	 */
	public static void redrawInformation() {
		
		Utility.traverseUI((component) -> {
			
			if (component instanceof AreasTreeEditorPanel) {
				AreasTreeEditorPanel traceFrame = (AreasTreeEditorPanel) component;
				
				traceFrame.tree.repaint();
				traceFrame.list.repaint();
			}
			return true;
		});
	}
	
	/**
	 * On tab switch.
	 */
	protected void onTreeListTabChange() {
		
		if (!isVisible()) {
			return;
		}
		
		reload();
		
		int index = tabbedPane.getSelectedIndex();
		
		// Transmit tree/list tab change.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramAreas, index == 0 ? selectedTreeAreaIds : selectedListAreaIds);
	}

	/**
	 * On tab panel change event.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
		if (!isVisible()) {
			return;
		}
		
		reload();
		
		int index = tabbedPane.getSelectedIndex();
		
		// TODO: <---COPY to new version
		// Transmit main tab change.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramAreas, index == 0 ? selectedTreeAreaIds : selectedListAreaIds);
	}
	
	/**
	 * No tab text.
	 */
	@Override
	public String getTabDescription() {
		
		return "";
	}
	
	/**
	 * Before tab panel removed.
	 */
	@Override
	public void beforeTabPanelRemoved() {
		
		// Remove listeners.
		removeListeners();
	}
	
	/**
	 * Get list of previous messages.
	 */
	@Override
	public LinkedList<Message> getPreviousMessages() {
		
		return previousMessages;
	}
	
	/**
	 * Called when the tree editor needs to recreate its contents.
	 */
	@Override
	public void recreateContent() {
		
	}
}
