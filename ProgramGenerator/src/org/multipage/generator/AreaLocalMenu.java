/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.maclan.Area;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.AreaSignal;
import org.multipage.gui.EventSource;
import org.multipage.gui.GuiSignal;
import org.multipage.gui.Images;
import org.multipage.gui.SignalGroup;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class AreaLocalMenu {

	/**
	 * Constants.
	 */
	public static final int DIAGRAM = 1;
	public static final int EDITOR = 2;
	
	/**
	 * Empty menu item padding.
	 */
	private static final Insets emptyMenuItemPadding = new Insets(0, 0, 0, 0);

	/**
	 * Listeners.
	 */
	private AreaLocalMenuListener listener = new AreaLocalMenuListener();
	
	/**
	 * Is favorites flag.
	 */
	public boolean isAddFavorites = true;

	/**
	 * Menu purpose.
	 */
	private int purpose = 0;
	
	/**
	 * Parent component.
	 */
	private Component parentComponent;
	
	/**
	 * Taken area references.
	 */
	private List<Area> takenAreas = null;
	private Area takenAreaParent = null;
	
	/**
	 * All popup menu elements. 
	 */
	public MenuElement menuTakeAreaTree;
	public MenuElement menuCopyTakenAreaTree;
	public MenuElement menuMoveTakenAreaTree;
	public MenuElement menuAddToFavoritesArea;
	public MenuElement menuCreateAreas;
	public MenuElement menuExternalSources;
	public MenuElement menuSetHomeArea;
	public MenuElement menuEditAreaSlots;
	public MenuElement menuEditStartResources;
	public MenuElement menuFile;
	public MenuElement menuExport;
	public MenuElement menuImport;
	public MenuElement menuFocusArea;
	public MenuElement menuFocusSuperArea;
	public MenuElement menuFocusNextArea;
	public MenuElement menuFocusPreviousArea;
	public MenuElement menuFocusTabTopArea;
	public MenuElement menuEditArea;
	public MenuElement menuCopyDescription;
	public MenuElement menuCopyAlias;
	public MenuElement menuAreaTrace;
	public MenuElement menuDisplayArea;
	public MenuElement menuDisplayRenderedArea;
	public MenuElement displayMenu;
	public MenuElement menuAreaHelp;
	public MenuElement menuAreaInheritedFolders;
	public MenuElement focusMenu;
	public MenuElement selectMenu;
	public MenuElement menuSelectArea;
	public MenuElement menuSelectAreaAdd;
	public MenuElement menuSelectAreaAndSuabreas;
	public MenuElement menuSelectAreaAndSuabreasAdd;
	public MenuElement menuClearSelection;
	public MenuElement menuCloneDiagram;
	
	/**
	 * Constructor.
	 * @param areaLocalMenuListener
	 */
	public AreaLocalMenu(AreaLocalMenuListener listener) {
		
		this.listener = listener;
	}

	/**
	 * Constructor.
	 * @param listener
	 * @param purpose
	 */
	public AreaLocalMenu(AreaLocalMenuListener listener, int purpose) {

		this(listener);
		
		this.purpose = purpose;
	}
	

	/**
	 * Create new menu item.
	 * @param menuTextId
	 * @param iconPath
	 * @return
	 */
	private static JMenu newMenu(String menuTextId, String iconPath) { 
		
		String menuText = Resources.getString(menuTextId);
		JMenu menu = new JMenu(menuText);
		ImageIcon icon = Images.getIcon(iconPath);
		
		menu.setIcon(icon);
		return menu;
	}
	
	/**
	 * Create new menu.
	 * @param menuTextId
	 * @param iconPath
	 * @return
	 */
	private static JMenu newMenu(String menuTextId) { 
		
		String menuText = Resources.getString(menuTextId);
		JMenu menu = new JMenu(menuText);
		
		return menu;
	}
	
	
	
	/**
	 * Create new menu item.
	 * @param menuTextId
	 * @param iconPath
	 * @param accelerator
	 * @return
	 */
	private static JMenuItem newMenuItem(String menuTextId, String iconPath, String accelerator) { 
		
		String menuText = Resources.getString(menuTextId);
		JMenuItem menuItem = new JMenuItem(menuText);
		ImageIcon icon = Images.getIcon(iconPath);
		
		menuItem.setIcon(icon);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator));
		
		return menuItem;
	}
	
	/**
	 * Create new menu item.
	 * @param menuTextId
	 * @param iconPath
	 * @return
	 */
	private static JMenuItem newMenuItem(String menuTextId, String iconPath) { 
		
		String menuText = Resources.getString(menuTextId);
		JMenuItem menuItem = new JMenuItem(menuText);
		ImageIcon icon = Images.getIcon(iconPath);
		
		menuItem.setIcon(icon);
		return menuItem;
	}
	
	/**
	 * Create new menu item.
	 * @param menuTextId
	 * @param iconPath
	 * @return
	 */
	private static JMenuItem newMenuItem(String menuTextId) { 
		
		String menuText = Resources.getString(menuTextId);
		JMenuItem menuItem = new JMenuItem(menuText);
		
		return menuItem;
	}

	/**
	 * Add items to a popup trayMenu.
	 * @param popupMenu
	 */
	public void addTo(Component parentComponent, JPopupMenu popupMenu) {
		
		this.parentComponent = parentComponent;
		addTo(popupMenu, popupMenu.getComponentCount() + 1);
	}

	/**
	 * Add items to a popup trayMenu.
	 * @param popupMenuFavorites
	 * @param start
	 */
	public void addTo(JPopupMenu popupMenu, int start) {
		
		menuFile = newMenu("org.multipage.generator.menuFile");
		menuEditArea = newMenu("org.multipage.generator.menuEditAreaResourcesList", "org/multipage/generator/images/edit.png");
		focusMenu = newMenu("org.multipage.generator.menuFocus");
		selectMenu = newMenu("org.multipage.generator.menuSelect");
		displayMenu = newMenu("org.multipage.generator.menuDisplayMenu");
		menuCreateAreas = newMenu("org.multipage.generator.menuCreateAreas");
		
		menuTakeAreaTree = newMenuItem("org.multipage.generator.menuTakeAreaTree", "org/multipage/gui/images/copy_icon.png");
		menuCopyTakenAreaTree = newMenuItem("org.multipage.generator.menuCopyTakenAreaTree", "org/multipage/gui/images/paste_icon.png");
		menuMoveTakenAreaTree = newMenuItem("org.multipage.generator.menuMoveTakenAreaTree", "org/multipage/gui/images/paste_icon.png");
		menuAddToFavoritesArea = newMenuItem("org.multipage.generator.menuAddToFavorites", "org/multipage/generator/images/favorite.png");
		menuExternalSources = newMenuItem("org.multipage.generator.menuExternalSourceCodes");
		menuSetHomeArea = newMenuItem("org.multipage.generator.menuSetHomeArea", "org/multipage/generator/images/home_page.png");
		menuEditAreaSlots = newMenuItem("org.multipage.generator.menuEditArea", "org/multipage/generator/images/list.png");
		menuEditStartResources = newMenuItem("org.multipage.generator.menuEditStartResources", "org/multipage/generator/images/start_resource.png");
		menuExport = newMenuItem("org.multipage.generator.menuFileExport", "org/multipage/generator/images/export2_icon.png");
		menuImport = newMenuItem("org.multipage.generator.menuFileImport", "org/multipage/generator/images/import2_icon.png");
		menuFocusArea = newMenuItem("org.multipage.generator.menuFocusArea", "org/multipage/generator/images/search_icon.png");
		menuFocusSuperArea = newMenuItem("org.multipage.generator.menuFocusSuperArea", "org/multipage/generator/images/search_parent.png", "control S");
		menuFocusNextArea = newMenuItem("org.multipage.generator.menuFocusNextArea", "org/multipage/generator/images/next.png");
		menuFocusPreviousArea = newMenuItem("org.multipage.generator.menuFocusPreviousArea", "org/multipage/generator/images/previous.png");
		menuFocusTabTopArea = newMenuItem("org.multipage.generator.menuFocusTabTopArea", "org/multipage/generator/images/focus_tab.png");
		menuCopyDescription = newMenuItem("org.multipage.generator.menuCopyAreaDescription", "org/multipage/gui/images/copy_icon.png");
		menuCopyAlias = newMenuItem("org.multipage.generator.menuCopyAreaAlias", "org/multipage/gui/images/copy_icon.png");
		menuAreaTrace = newMenuItem("org.multipage.generator.menuAreaTrace", "org/multipage/generator/images/area_trace.png");
		menuDisplayArea = newMenuItem("org.multipage.generator.menuDisplayOnlineArea", "org/multipage/generator/images/display.png");
		menuDisplayRenderedArea = newMenuItem("org.multipage.generator.menuDisplayRenderedArea", "org/multipage/generator/images/display_rendered.png");
		menuAreaHelp = newMenuItem("org.multipage.generator.menuAreaHelp", "org/multipage/generator/images/help_small.png");
		menuAreaInheritedFolders = newMenuItem("org.multipage.generator.menuAreaInheritedFolders", "org/multipage/generator/images/folder.png");
		menuSelectArea = newMenuItem("org.multipage.generator.menuSelectArea2", "org/multipage/generator/images/selected_area.png");
		menuSelectAreaAdd = newMenuItem("org.multipage.generator.menuSelectAreaAdd", "org/multipage/generator/images/selected_area_add.png");
		menuSelectAreaAndSuabreas = newMenuItem("org.multipage.generator.menuSelectAreaAndSubareas2", "org/multipage/generator/images/selected_subareas.png");
		menuSelectAreaAndSuabreasAdd = newMenuItem("org.multipage.generator.menuSelectAreaAndSubareasAdd", "org/multipage/generator/images/selected_subareas_add.png");
		menuClearSelection = newMenuItem("org.multipage.generator.menuClearSelection", "org/multipage/generator/images/cancel_icon.png");
		menuCloneDiagram = newMenuItem("org.multipage.generator.menuCloneAreasDiagram", "org/multipage/generator/images/clone.png");
		
		int index = start;
		
		insert(popupMenu, menuTakeAreaTree, index++);
		insert(popupMenu, menuCopyTakenAreaTree, index++);
		insert(popupMenu, menuMoveTakenAreaTree, index++);
		addSeparator(popupMenu); index++;
		if (isAddFavorites) {
			insert(popupMenu, menuAddToFavoritesArea, index++);
			addSeparator(popupMenu); index++;
		}
		if ((purpose & DIAGRAM) == 0) {
			insert(popupMenu, selectMenu, index++);
		}
		insert(displayMenu, menuDisplayArea, 0);
		insert(displayMenu, menuDisplayRenderedArea, 1);
		insert(popupMenu, menuCreateAreas, index++);
		insert(menuCreateAreas, menuExternalSources, 0);
		insert(popupMenu, menuSetHomeArea, index++);
		insert(popupMenu, menuEditArea, index++);
		insert(popupMenu, menuEditAreaSlots, index++);
		insert(popupMenu, menuEditStartResources, index++);
		addSeparator(popupMenu); index++;
		insert(popupMenu, menuSetHomeArea, index++);
		addSeparator(popupMenu); index++;
		index = insertEditResourceMenuItems(popupMenu, index);
		insert(popupMenu, focusMenu, index++);
		insert(popupMenu, displayMenu, index++);
		insert(popupMenu, menuFile, index++);
		addSeparator(popupMenu); index++;
		insert(popupMenu, menuAreaTrace, index++);
		insert(popupMenu, menuAreaInheritedFolders, index++);
		addSeparator(popupMenu); index++;
		insert(popupMenu, menuCopyDescription, index++);
		insert(popupMenu, menuCopyAlias, index++);
		addSeparator(popupMenu); index++;
		insert(popupMenu, menuAreaHelp, index++);
		addSeparator(popupMenu); index++;
		insert(popupMenu, menuCloneDiagram, index++);
		
		index = 0;
		insert(menuFile, menuImport, index++);
		insert(menuFile, menuExport, index++);
				
		index = 0;
		insert(focusMenu, menuFocusArea, index++);
		insert(focusMenu, menuFocusSuperArea, index++);
		insert(focusMenu, menuFocusNextArea, index++);
		insert(focusMenu, menuFocusPreviousArea, index++);
		index = insertFocusMenuItems((JMenu) focusMenu, index++);
		addSeparator(focusMenu); index++;
		insert(focusMenu, menuFocusTabTopArea, index++);
		
		insertEditAreaMenu((JMenu) menuEditArea);
		
		if ((purpose & DIAGRAM) == 0) {
			index = 0;
			insert(selectMenu, menuSelectArea, index++);
			insert(selectMenu, menuSelectAreaAdd, index++);
			insert(selectMenu, menuSelectAreaAndSuabreas, index++);
			insert(selectMenu, menuSelectAreaAndSuabreasAdd, index++);
			addSeparator(selectMenu); index++;
			insert(selectMenu, menuClearSelection, index++);
		}

		// Add listeners.
		addActionListener(menuTakeAreaTree, () -> takeAreaTree());
		addActionListener(menuCopyTakenAreaTree, () -> copyTakenAreaTree());
		addActionListener(menuMoveTakenAreaTree, () -> moveTakenAreaTree());
		if (isAddFavorites) {
			addActionListener(menuAddToFavoritesArea, () -> addToFavorites());
		}
		addActionListener(menuExternalSources, () -> createExternalSources());
		addActionListener(menuEditAreaSlots, () -> editArea());
		addActionListener(menuFocusArea, () -> focusArea());
		addActionListener(menuFocusSuperArea, () -> focusSuperArea());
		addActionListener(menuFocusNextArea, () -> focusNextArea());
		addActionListener(menuFocusPreviousArea, () -> focusPreviousArea());
		addActionListener(menuCopyDescription, () -> copyDescription());
		addActionListener(menuCopyAlias, () -> copyAlias());
		addActionListener(menuAreaTrace, () -> showAreaTrace());
		addActionListener(menuAreaInheritedFolders, () -> showAreaInheritedFolders());
		addActionListener(menuAreaHelp, () -> viewHelp());
		addActionListener(menuDisplayArea, () -> displayOnlineArea());
		addActionListener(menuDisplayRenderedArea, () -> displayRenderedArea());
		addActionListener(menuSelectArea, () -> selectArea());
		addActionListener(menuSelectAreaAdd, () -> selectAreaAdd());
		addActionListener(menuSelectAreaAndSuabreas, () -> selectAreaAndSubareas());
		addActionListener(menuSelectAreaAndSuabreasAdd, () -> selectAreaAndSubareasAdd());
		addActionListener(menuClearSelection, () -> clearSelection());
		addActionListener(menuCloneDiagram, () -> cloneDiagram());
		addActionListener(menuSetHomeArea, () -> setHomeArea());
		addActionListener(menuFocusTabTopArea, () -> focusTabTopArea());
		addActionListener(menuExport, () -> exportArea());
		addActionListener(menuImport, () -> importArea());
		addActionListener(menuEditStartResources, () -> editStartResource(true));
		
		// Set listeners.
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				
				// Enable / disable pasting of area tree with tray menu item.
				boolean enable = GeneratorMainFrame.getFrame().isAreaTreeDataCopy();
				
				JMenuItem menuItem = (JMenuItem) menuCopyTakenAreaTree;
				menuItem.setEnabled(enable);
				
				menuItem = (JMenuItem) menuMoveTakenAreaTree;
				menuItem.setEnabled(enable);
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}
	
	/**
	 * Adds action listener to menu element.
	 * @param menuElement
	 * @param runnable
	 */
	private void addActionListener(MenuElement menuElement, Runnable runnable) {
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
			}
		};
		
		if (menuElement instanceof JMenuItem) {
			JMenuItem menuItem = (JMenuItem) menuElement;
			menuItem.addActionListener(listener);
		}
	}
	
	/**
	 * Disable menu items.
	 * @param disableMenuItems
	 */
	public void disableMenuItems(MenuElement ...disableMenuItems) {
		
		for (MenuElement menuElementDisable : disableMenuItems) {
			
			if (menuElementDisable instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) menuElementDisable;
				menuItem.setEnabled(false);
			}
		}		
	}
	

	/**
	 * Insert separator.
	 * @param menuElement
	 */
	private void addSeparator(MenuElement menuElement) {
		
		if (menuElement instanceof JMenu) {
			JMenu menu = (JMenu) menuElement;
			menu.addSeparator();
		}
	}

	/**
	 * Insert menu item at indexed position.
	 * @param menu
	 * @param menuItem
	 * @param index
	 */
	private void insert(MenuElement menuElement, MenuElement subElement, int index) {
		
		if (menuElement instanceof JMenu) { 
			JMenu menu = (JMenu) menuElement;
			insert(menu, subElement, index);
		}
		else if (menuElement instanceof JPopupMenu) {
			JPopupMenu popupMenu = (JPopupMenu) menuElement;
			insert(popupMenu, subElement, index);
		}
	}
	
	/**
	 * Insert menu item at indexed position.
	 * @param menu
	 * @param menuItem
	 * @param index
	 */
	private void insert(JMenu menu, MenuElement menuElement, int index) {
		
		JMenuItem menuItem = (JMenuItem) menuElement;
		menu.insert(menuItem, index);
	}
	
	/**
	 * Insert popup menu item at indexed position.
	 * @param popupMenu
	 * @param menuItem
	 * @param index
	 */
	private void insert(JPopupMenu popupMenu, MenuElement menuElement, int index) {
		
		try {
			JMenuItem menuItem = (JMenuItem) menuElement;
			popupMenu.insert(menuItem, index);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Create external sources.
	 */
	protected void createExternalSources() {
		
		Area area = listener.getCurrentArea();
		CreateAreasFromSourceCode.showDialog(parentComponent, area);
		
		// Update GUI components with areas.
		Object sourceObject = parentComponent != null ? parentComponent : this;
		ApplicationEvents.transmit(EventSource.LOCAL_POPUP_MENU.user(sourceObject), SignalGroup.UPDATE_AREAS);
	}

	/**
	 * Insert edit trayMenu items.
	 * @param popupMenu 
	 * @param index
	 * @return
	 */
	protected int insertEditResourceMenuItems(JPopupMenu popupMenu, int index) {
		
		// Override this method.
		return index;
	}

	/**
	 * Insert focus trayMenu items.
	 * @param focusMenu
	 * @param index
	 * @return
	 */
	protected int insertFocusMenuItems(JMenu focusMenu, int index) {
		
		// Override this method.
		return index;
	}

	/**
	 * Insert edit area trayMenu.
	 * @param menuEditArea
	 * @return
	 */
	protected void insertEditAreaMenu(JMenu menuEditArea) {
		
		JMenuItem menuAreaEdit = createMenuItem(Resources.getString("org.multipage.generator.menuAreaEdit"));
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorFrame.NOT_SPECIFIED);
			}
		});
		menuEditArea.add(menuAreaEdit);
		menuEditArea.addSeparator();
		
		JMenuItem menuEditResources = createMenuItem(Resources.getString("org.multipage.generator.menuAreaEditResources"));
		menuEditResources.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorFrame.RESOURCES);
			}
		});
		
		menuEditArea.add(menuEditResources);
		
		JMenuItem menuEditDependencies = createMenuItem(Resources.getString("org.multipage.generator.menuAreaEditDependencies"));
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorFrame.DEPENDENCIES);
			}
		});
		menuEditArea.add(menuEditDependencies);
		
		JMenuItem menuEditConstructor = createMenuItem(Resources.getString("org.multipage.generator.menuAreaEditConstructor"));
		menuEditConstructor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorFrame.CONSTRUCTOR);
			}
		});
		menuEditArea.add(menuEditConstructor);
	}

	/**
	 * Create new menu item.
	 * @param caption
	 * @return
	 */
	private JMenuItem createMenuItem(String caption) {
		
		JMenuItem menuItem = new JMenuItem(caption);
		menuItem.setMargin(emptyMenuItemPadding);
		return menuItem;
		
	}

	/**
	 * Display rendered area.
	 */
	protected void displayRenderedArea() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		GeneratorMainFrame.displayRenderedArea(area);
	}

	/**
	 * Display online area.
	 */
	protected void displayOnlineArea() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		GeneratorMainFrame.getFrame().displayOnlineArea(area);
	}

	/**
	 * Get area. Inform user on error.
	 * @return
	 */
	protected Area getAreaInformUser() {
		
		Area area = listener.getCurrentArea();
		if (area == null) {
			Utility.show(null, "org.multipage.generator.messageSelectSingleArea");
			return null;
		}
		return area;
	}

	/**
	 * Add to favorites.
	 */
	protected void addToFavorites() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		GeneratorMainFrame.getFrame().getAreaDiagramEditor().addFavorite(area);
	}

	/**
	 * Edit area.
	 */
	protected void editArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Open area editor.
		AreasPropertiesFrame.createNew(area);
	}

	/**
	 * Focus selected area.
	 */
	protected void focusArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Focus on area.
		GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusArea(area.getId());
	}

	/**
	 * Focus tab top area.
	 */
	protected void focusTabTopArea() {
		
		// Focus on the top area.
		ApplicationEvents.transmit(this, GuiSignal.focusTopArea);
	}

	/**
	 * Focus super area.
	 */
	protected void focusSuperArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Get area shapes.
		AreaShapes shapes = (AreaShapes) area.getUser();
		// Get parent area.
		Rectangle2D diagramRect = GeneratorMainFrame.getFrame().getAreaDiagram().getRectInCoord();
		Area superArea = shapes.getVisibleParent(diagramRect);
		
		if (superArea != null) {
			// Focus on area.
			GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusAreaNear(superArea.getId());
		}
	}

	/**
	 * Focus next area.
	 */
	protected void focusNextArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Get area shapes.
		AreaShapes shapes = (AreaShapes) area.getUser();
		// Get parent area.
		Rectangle2D diagramRect = GeneratorMainFrame.getFrame().getAreaDiagram().getRectInCoord();
		Area superArea = shapes.getVisibleParent(diagramRect);
		
		if (superArea != null) {
			
			// Get next area.
			Area nextArea = area.getNextArea(superArea);
			if (nextArea != null) {
				
				// Focus on next area.
				GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusAreaNear(nextArea.getId());
			}
			else {
				Utility.show(null, "org.multipage.generator.messageIsLastArea");
			}
		}
	}

	/**
	 * Focus previous area.
	 */
	protected void focusPreviousArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Get area shapes.
		AreaShapes shapes = (AreaShapes) area.getUser();
		// Get parent area.
		Rectangle2D diagramRect = GeneratorMainFrame.getFrame().getAreaDiagram().getRectInCoord();
		Area superArea = shapes.getVisibleParent(diagramRect);
		
		if (superArea != null) {
			
			// Get previous area.
			Area previousArea = area.getPreviousArea(superArea);
			if (previousArea != null) {
				
				// Focus on previous area.
				GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusAreaNear(previousArea.getId());
			}
			else {
				Utility.show(null, "org.multipage.generator.messageThisIsFirstArea");
			}
		}
	}

	/**
	 * Edit area with area editor.
	 */
	protected void onEditArea(int tabIdentifier) {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}

		// Execute area editor.
		AreaEditorFrame.showDialog(null, area, tabIdentifier);
	}
	
	/**
	 * Edit start resource.
	 * @param inherits 
	 */
	protected void editStartResource(boolean inherits) {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		// Edit start resource.
		GeneratorMainFrame.editStartResource(area, inherits);
	}

	/**
	 * Copy area alias.
	 */
	protected void copyAlias() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		StringSelection ss = new StringSelection(area.getAlias());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	}

	/**
	 * Copy area description.
	 */
	protected void copyDescription() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		StringSelection ss = new StringSelection(area.getDescriptionForced());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	}

	/**
	 * Show trace area.
	 */
	protected void showAreaTrace() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		AreaTraceFrame.showNewFrame(area.getId());
	}

	/**
	 * Show area inherited folders.
	 */
	protected void showAreaInheritedFolders() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		AreaInheritedFoldersDialog.showDialog(GeneratorMainFrame.getFrame(), area);
	}

	/**
	 * View help.
	 */
	protected void viewHelp() {
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		GeneratorMainFrame.getFrame().findViewAreaHelp(area);
	}
	
	/**
	 * Select area.
	 */
	protected void selectArea() {

		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		Long areaId = area.getId();
		
		// Transmit "select area" signal.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramArea, areaId, true);		
		
		// Transmit "update area tree dialog".
		ApplicationEvents.transmit(EventSource.AREA_LOCAL_MENU.user(this), SignalGroup.UPDATE_TREE);
	}

	/**
	 * Add area selection.
	 */
	protected void selectAreaAdd() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		Long areaId = area.getId();

		// Transmit "select area" signal.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramArea, areaId, false);		
		
		// Transmit "update area tree dialog".
		ApplicationEvents.transmit(EventSource.AREA_LOCAL_MENU.user(this), SignalGroup.UPDATE_TREE);
	}

	/**
	 * Select area and subareas.
	 */
	protected void selectAreaAndSubareas() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		long areaId = area.getId();
		
		// Transmit "select area with sub areas" signal.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramAreaWithSubareas, areaId, true);
		
		// Transmit "update area tree dialog".
		ApplicationEvents.transmit(EventSource.AREA_LOCAL_MENU.user(this), SignalGroup.UPDATE_TREE);
	}
	
	/**
	 * Add area and subareas selection.
	 */
	protected void selectAreaAndSubareasAdd() {
			
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		long areaId = area.getId();
		
		// Transmit "add area with sub areas selection" signal.
		ApplicationEvents.transmit(this, GuiSignal.selectDiagramAreaWithSubareas, areaId, false);		
		
		// Transmit "update area tree dialog".
		ApplicationEvents.transmit(EventSource.AREA_LOCAL_MENU.user(this), SignalGroup.UPDATE_TREE);
	}

	/**
	 * Clear selection.
	 */
	protected void clearSelection() {
		
		GeneratorMainFrame.getFrame().getVisibleAreasEditor().getDiagram().removeSelection();
	}
	
	/**
	 * Clone diagram.
	 */
	protected void cloneDiagram() {
		
		// Get selected area (can be null).
		Area area = listener.getCurrentArea();
		
		GeneratorMainFrame.getFrame().cloneAreasDiagram(area);
	}

	/**
	 * Set home area.
	 */
	protected void setHomeArea() {
		
		// Get selected areas.
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		long areaId = area.getId();
		
		// Transmit the "set home area" signal. Wait for procedure completion.
		Runnable completedLamda = () -> ApplicationEvents.transmit(EventSource.AREA_LOCAL_MENU.user(this), SignalGroup.UPDATE_ALL);
		ApplicationEvents.transmit(this, AreaSignal.setHomeArea, areaId, parentComponent, completedLamda);
	}
	
	/**
	 * Take area tree.
	 */
	protected void takeAreaTree() {
		
		// Get selected area (can be null).
		takenAreas = listener.getCurrentAreas();
		takenAreaParent = listener.getCurrentParentArea();
		
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		frame.takeAreasTrees(takenAreas, takenAreaParent);
	}

	/**
	 * Copy taken area tree.
	 */
	protected void copyTakenAreaTree() {
		
		// Check taken area.
		if (takenAreas == null || takenAreas.isEmpty() || takenAreaParent == null) {
			return;
		}
		
		// Get selected area (can be null) and copy area tree.
		Area area = listener.getCurrentArea();
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		frame.copyAreaTrees(area);
		
		// Update GUI components with areas.
		Object sourceObject = parentComponent != null ? parentComponent : this;
		ApplicationEvents.transmit(EventSource.LOCAL_POPUP_MENU.user(sourceObject), SignalGroup.UPDATE_AREAS);
	}

	/**
	 * Move taken area tree.
	 */
	protected void moveTakenAreaTree() {
		
		// Check taken area.
		if (takenAreas == null || takenAreas.isEmpty() || takenAreaParent == null) {
			return;
		}
		
		// Prepare area tree for copying.
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		Area area = listener.getCurrentArea();
		Area parentArea = listener.getCurrentParentArea();
		frame.moveAreaTrees(takenAreas, parentArea, area, parentComponent);
		
		// Update GUI components with areas.
		Object sourceObject = parentComponent != null ? parentComponent : this;
		ApplicationEvents.transmit(EventSource.LOCAL_POPUP_MENU.user(sourceObject), SignalGroup.UPDATE_AREAS);
	}

	/**
	 * Export area.
	 */
	protected void exportArea() {
		
		Area area = listener.getCurrentArea();
		if (area == null) {
			return;
		}
		
		Component parent = listener.getComponent();
		if (parent == null) {
			parent = GeneratorMainFrame.getFrame();
		}
		
		GeneratorMainFrame.exportArea(area, parent);
	}

	/**
	 * Import area.
	 */
	protected void importArea() {
		
		Area area = listener.getCurrentArea();
		if (area == null) {
			return;
		}
		
		Component parent = listener.getComponent();
		if (parent == null) {
			parent = GeneratorMainFrame.getFrame();
		}
		
		Long newAreaId = GeneratorMainFrame.importArea(area, parent, true, false, false);
		
		// Callback method.
		if (newAreaId != null) {
			
			SwingUtilities.invokeLater(() -> {
				listener.onNewArea(newAreaId);
			});
		}
	}
	/**
	 * Set purpose.
	 * @param purpose
	 */
	public void setHint(int purpose) {

		this.purpose = purpose;
	}
}
