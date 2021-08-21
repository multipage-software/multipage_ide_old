/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.Slot;
import org.maclan.SlotHolder;
import org.maclan.SlotType;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Callback;
import org.multipage.gui.EditorPaneEx;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Images;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * 
 * @author vakol
 *
 */
public class SlotListPanel extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Splitter position.
	 */
	private static int splitterPositionFromEnd;

	/**
	 * An array of table column positions.
	 */
	private static int [] tableColumnPositions;
	
	/**
	 * Column widths.
	 */
	private static int [] columnWidthsState;
	
	/**
	 * Show preferred slots.
	 */
	private static boolean showUserSlots = false;

	/**
	 * Clipboard.
	 */
	private static LinkedList<Slot> clipBoard =
		new LinkedList<Slot>();

	/**
	 * Get slot clipboard.
	 */
	public static LinkedList<Slot> getSlotClipboard() {
		
		return clipBoard;
	}

	/**
	 * Set slot clipboard.
	 * @param _slotClipboard
	 */
	public static void setSlotClipboard(LinkedList<Slot> _slotClipboard) {
		
		clipBoard = _slotClipboard;
	}

	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		splitterPositionFromEnd = -1;
		
		if (ProgramGenerator.isExtensionToBuilder()) {
			
			columnWidthsState = new int [] {50, 150, 100, 100, 70, 150};
			tableColumnPositions = new int [] {0, 1, 2, 3, 4, 5};
		}
		else {
			columnWidthsState = new int [] {128, 128, 100, 128};
			tableColumnPositions = new int [] {0, 1, 2, 3};
		}
		
		showUserSlots = false;
	}

	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		splitterPositionFromEnd = inputStream.readInt();
		columnWidthsState = Utility.readInputStreamObject(inputStream, int [].class);
		tableColumnPositions = Utility.readInputStreamObject(inputStream, int [].class);
		showUserSlots = inputStream.readBoolean();
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeInt(splitterPositionFromEnd);
		outputStream.writeObject(columnWidthsState);
		outputStream.writeObject(tableColumnPositions);
		outputStream.writeBoolean(showUserSlots);
	}

	/**
	 * Holders list.
	 */
	protected LinkedList<Area> areas =
		new LinkedList<Area>();
	
	/**
	 * Table model.
	 */
	protected SlotsTableModel tableModel;
	
	/**
	 * Found slots.
	 */
	protected LinkedList<FoundSlot> foundSlots = new LinkedList<FoundSlot>();
	
	/**
	 * Show only found slots button.
	 */
	protected JToggleButton buttonShowOnlyFound;
	
	/**
	 * Show only preferred slots button.
	 */
	protected JToggleButton buttonShowUserSlots;
	/**
	 * Popup trayMenu.
	 */
	protected JPopupMenu popupMenu;

	/**
	 * Do not save state on exit flag.
	 */
	protected boolean doNotSaveStateOnExit = false;
	
	/**
	 * Set divider position to maximum.
	 */
	protected boolean setDividerPositionToMaximum = false;
	
	/**
	 * Search direction.
	 */
	protected boolean searchDirectionForward = true;
	
	/**
	 * Table properties set flag.
	 */
	private boolean isTablePropertiesReady = false;

	/**
	 * Searched in values flag.
	 */
	private boolean searchedInValues = false;

	/**
	 * Key sequence timer.
	 */
	private Timer keySequenceResetTimer;

	/**
	 * Key sequence.
	 */
	private String keySequence = "";

	/**
	 * Do not update slot description flag.
	 */
	private boolean doNotUpdateSlotDescription = false;

	/***
	 * Load slot description timer.
	 */
	private Timer loadDescriptionTimer;
	
	/**
	 * Slot selected callback
	 */
	private SlotSelectedEvent slotSelectedEvent;

	/**
	 * Menu items.
	 */
	protected JSeparator separator1;
	protected JSeparator separator2;
	protected JMenuItem menuMoveSlots;
	protected JMenuItem menuUseSlots;
	protected JMenuItem menuCopySlots;
	protected JMenuItem menuFocusArea;
	protected JMenuItem menuSetDefaultNormal;
	protected JMenuItem menuClearSearch;
	
	/**
	 * Search dialog object.
	 */
	private SearchSlotDialog searchDialog;
	
	/**
	 * Use database to load slots.
	 */
	private boolean useDatabase = true;
	
	/**
	 * Enable editing slots.
	 */
	private boolean slotEditingEnabled = true;
	
	/**
	 * Save column widths.
	 */
	private boolean saveColumnWidths = true;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelSlots;
	protected JTable tableSlots;
	protected JSplitPane splitPane;
	private JScrollPane scrollPaneDescription;
	protected JEditorPane textDescription;
	private JPanel panelTop;
	protected JScrollPane scrollPane;
	protected JToolBar toolBar;
	
	/**
	 * Create the panel.
	 */
	public SlotListPanel() {

		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelSlots = new JLabel("org.multipage.generator.textSlots");
		springLayout.putConstraint(SpringLayout.NORTH, labelSlots, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelSlots, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelSlots, 0, SpringLayout.EAST, this);
		add(labelSlots);
		
		splitPane = new JSplitPane();
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(true);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.SOUTH, labelSlots);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, this);
		splitPane.setResizeWeight(0.8);
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, labelSlots);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		scrollPaneDescription = new JScrollPane();
		scrollPaneDescription.setBorder(null);
		splitPane.setRightComponent(scrollPaneDescription);
		
		textDescription = new EditorPaneEx();
		textDescription.setBorder(null);
		textDescription.setContentType("text/html");
		textDescription.setFont(new Font("Arial", Font.PLAIN, 12));
		textDescription.setBackground(SystemColor.control);
		textDescription.setEditable(false);
		scrollPaneDescription.setViewportView(textDescription);
		
		panelTop = new JPanel();
		panelTop.setBorder(null);
		splitPane.setLeftComponent(panelTop);
		SpringLayout sl_panelTop = new SpringLayout();
		panelTop.setLayout(sl_panelTop);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		sl_panelTop.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, panelTop);
		sl_panelTop.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, panelTop);
		sl_panelTop.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, panelTop);
		panelTop.add(scrollPane);
		
		tableSlots = new JTable();
		tableSlots.setBorder(null);
		scrollPane.setViewportView(tableSlots);
		tableSlots.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		toolBar = new JToolBar();
		sl_panelTop.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, toolBar);
		sl_panelTop.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, panelTop);
		sl_panelTop.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, panelTop);
		sl_panelTop.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, panelTop);
		toolBar.setFloatable(false);
		panelTop.add(toolBar);
		tableSlots.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 2) {
					
					if (slotEditingEnabled) {
						onEdit();
					}
				}
				
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 1) {
					fireSlotSelected();
				}
			}
		});
	}
	
	/**
	 * Set slot selected event
	 */
	public void setSlotSelectedEvent(SlotSelectedEvent slotSelectedEvent) {
		
		this.slotSelectedEvent = slotSelectedEvent;
	}
	
	/**
	 * Fire slot selected event
	 */
	protected void fireSlotSelected() {
		
		if (slotSelectedEvent == null) {
			return;
		}
		
		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length != 1) {
			
			return;
		}
		
		// Get selected slot and edit it.
		Slot slot = (Slot) tableModel.get(selectedRows[0]);
		slotSelectedEvent.selected(slot);
	}

	/**
	 * Create popup menu.
	 */
	protected void createMenu() {
		
		popupMenu = new JPopupMenu();
		addPopup(scrollPane, popupMenu);
		addPopup(tableSlots, popupMenu);
		
		menuUseSlots = new JMenuItem("org.multipage.generator.textUseSlots");
		menuUseSlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useSlots();
			}
		});
		menuUseSlots.setPreferredSize(new Dimension(200, 22));
		popupMenu.add(menuUseSlots);
		
		separator1 = new JSeparator();
		popupMenu.add(separator1);
		
		menuCopySlots = new JMenuItem("org.multipage.generator.textCopySlots");
		menuCopySlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveSlots(true);
			}
		});
		popupMenu.add(menuCopySlots);
		
		menuMoveSlots = new JMenuItem("org.multipage.generator.textMoveSlots");
		menuMoveSlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveSlots(false);
			}
		});
		popupMenu.add(menuMoveSlots);

		separator2 = new JSeparator();
		popupMenu.add(separator2);
		
		menuFocusArea = new JMenuItem("org.multipage.generator.textFocusArea");
		menuFocusArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				focusSelectedArea();
			}
		});
		popupMenu.add(menuFocusArea);
		
		// Set default values menu item.
		menuSetDefaultNormal = new JMenuItem("org.multipage.generator.textSetDefaultNormalSlotValues");
		menuSetDefaultNormal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaultNormalValues();
			}
		});
		popupMenu.add(menuSetDefaultNormal);
		
		// Clear search results.
		menuClearSearch = new JMenuItem("org.multipage.generator.textClearAreaSlotSearchResults");
		menuClearSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSearch();
			}
		});
		popupMenu.add(menuClearSearch);
	}

	/**
	 * Set slot default values.
	 */
	protected void setDefaultNormalValues() {
		
		// Get selected slots.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length < 1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSlots");
			return;
		}
		
		Obj<Boolean> isDefault = new Obj<Boolean>();
		
		// Set default values input dialog.
		if (!SetSlotValuesDialog.showDialog(this, isDefault)) {
			return;
		}
		
		// Login to the database.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.login(login);
		if (result.isNotOK()) {
			
			result.show(this);
			return;
		}
		
		// Set slots' properties.
		for (int index : selectedRows) {
			
			Slot slot = (Slot) tableModel.get(index);
			
			// Update slot default value.
			result = middle.updateSlotIsDefault(slot.getHolder().getId(), slot.getAlias(), isDefault.ref);
			if (result.isNotOK()) {
				break;
			}
		}
		
		// Logout from the database.
		MiddleResult logoutResult = middle.logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
		
		// Show error.
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Update information.
		ConditionalEvents.transmit(SlotListPanel.this, Signal.setSlotsDefaultValues);
	}

	/**
	 * Clear search results.
	 */
	protected void clearSearch() {
		
		// Escape search mode.
		escapeFoundSlotsMode();
		
		// Load slots.
		loadSlots();
	}

	/**
	 * Get selected rows.
	 * @return
	 */
	protected int[] getSelectedRows() {
		
		int [] selectedRows = tableSlots.getSelectedRows();
		selectedRows = convertViewRowsToModel(selectedRows);
		
		return selectedRows;
	}

	/**
	 * Focus selected area.
	 */
	protected void focusSelectedArea() {

		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
			return;
		}
		
		List<Slot> slots = tableModel.getSlots();
		// Check.
		if (slots.size() == 0) {
			return;
		}
		
		Slot slot = (Slot) slots.get(selectedRows[0]);
		GeneratorMainFrame.getFrame().getAreaDiagramEditor().focusAreaNear(slot.getHolder().getId());
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {

		// Create trayMenu.
		createMenu();
		// Initialize tool bar.
		createToolBar();
		buttonShowUserSlots.setSelected(showUserSlots);
		// Initialize key strokes.
		initKeyStrokes();
		// Localize components.
		localize();
		// Initialize table.
		initTable();
		// Set icons.
		setIcons();
		// Load dialog.
		loadDialog();
		// Initialize divider listener.
		initDividerListener();
		// Set key listener.
		initKeyListener();
		initColumnListener();
		// Enable web links in description.
		Utility.enableWebLinks(this, textDescription);
		// Initialize timer
		initLoadDescriptionTimer();
		// Set listener
		setListeners();
	}
	
	/**
	 * Set listener
	 */
	private void setListeners() {
		
		// Create "update all" request event.
		ConditionalEvents.receiver(SlotListPanel.this, Signal.updateAll, message -> {
			
			// Disable the signal temporarily.
			Signal.updateAll.disable();
			
			// Update slot list
			update();
			
			// Enable the signal.
			SwingUtilities.invokeLater(() -> {
				Signal.updateAll.enable();
			});
		});
		
		// Create "area slot saved" event listener.
		ConditionalEvents.receiver(SlotListPanel.this, Signal.areaSlotSaved, message -> {
			
			// Get slot.
			Slot slot = message.getRelatedInfo();
			if (slot == null) {
				return;
			}
			
			// Check slot area.
			SlotHolder slotHolder = slot.getHolder();
			if (!(slotHolder instanceof Area)) {
				return;
			}
			
			Area slotArea = (Area) slotHolder;
			if (SlotListPanel.this.areas.contains(slotArea)) {
			
				// Reload the slot table view.
				update();
			}
		});
	}

	/**
	 * Set column change listener.
	 */
	private void initColumnListener() {
		
		tableSlots.getTableHeader().setReorderingAllowed(true);
		
		tableSlots.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
			}
			@Override
			public void columnRemoved(TableColumnModelEvent e) {
			}
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				
				// Get table column position.
				tableColumnPositions = getTableColumnPositions();
			}
			@Override
			public void columnMarginChanged(ChangeEvent e) {
				
				// Set column widths.
				if (isTablePropertiesReady) {
					getTableColumnWidths(columnWidthsState);
				}
			}
			@Override
			public void columnAdded(TableColumnModelEvent e) {
			}
		});
	}

	/**
	 * Load dialog.
	 */
	protected void loadDialog() {
		
		// Resize listener.
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				
				super.componentResized(e);
				
				// Set splitter position.
				int maximumSplitterPosition = splitPane.getMaximumDividerLocation();
				
				if (setDividerPositionToMaximum) {
					
					splitPane.setDividerLocation(maximumSplitterPosition);
					splitterPositionFromEnd = 0;
					return;
				}
				
				if (splitterPositionFromEnd == -1) {
					splitterPositionFromEnd = maximumSplitterPosition - splitPane.getDividerLocation();
				}
				else {

					int splitterPosition = maximumSplitterPosition - splitterPositionFromEnd;
					splitPane.setDividerLocation(splitterPosition);
				}
			}
		});
		
		
		isTablePropertiesReady = true;
		
		// Set column widths.
		setTableColumnWidths(columnWidthsState);
		
		// Move column positions.
		// The input values area model indices positioned on view.
		if (tableColumnPositions != null) {
			moveTableColumns(tableColumnPositions);
		}
	}
	
	/**
	 * Hide help panel.
	 */
	public void hideHelpPanel() {
		
		SwingUtilities.invokeLater(() -> {
			splitPane.setDividerSize(0);
			splitPane.getRightComponent().setVisible(false);
		});
	}
	
	/**
	 * Disable slots editor.
	 */
	public void doNotEditSlots() {
		
		slotEditingEnabled = false;
		toolBar.setPreferredSize(new Dimension(0, 0));
		toolBar.setVisible(false);
	}
	
	/**
	 * Do not preserve column widths.
	 */
	public void doNotPreserveColumns() {
		
		saveColumnWidths = false;
	}
	
	/**
	 * Save dialog.
	 */
	public void saveDialog() {
		
		// Stop time.
		if (keySequenceResetTimer != null) {
			keySequenceResetTimer.stop();
		}
		
		if (saveColumnWidths) {
			// Get table column position.
			tableColumnPositions = getTableColumnPositions();
			// Set column widths.
			getTableColumnWidths(columnWidthsState);
		}
	}
		
	/**
	 * Get table column positions. Each array item contains column model index
	 * on given view position.
	 * @return
	 */
	protected int [] getTableColumnPositions() {
		
		int columnCount = tableSlots.getColumnCount();
		int [] columnModelIndices = new int [columnCount];
		
		for (int viewIndex = 0; viewIndex < columnCount; viewIndex++) {
			
			columnModelIndices[viewIndex] = tableSlots.convertColumnIndexToModel(viewIndex);
		}
		
		return columnModelIndices;
	}
	
	/**
	 * Move table columns.
	 * @param columnModelIndices
	 */
	protected void moveTableColumns(int[] columnModelIndices) {
		
		TableColumnModel columnModel = tableSlots.getColumnModel();
		int columnCount = tableSlots.getColumnCount();
		
		// Set view positions of columns.
		for (int newViewIndex = 0; newViewIndex < columnModelIndices.length; newViewIndex++) {
			
			int modelIndex = columnModelIndices[newViewIndex];
			if (modelIndex < 0 || modelIndex >= columnCount) {
				continue;
			}
			
			int columnViewIndex = tableSlots.convertColumnIndexToView(modelIndex);
			
			// Move column to new column index.
			columnModel.moveColumn(columnViewIndex, newViewIndex);
		}
	}

	/**
	 * Initialize divider listener.
	 */
	private void initDividerListener() {

		splitPane.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent arg0) {
			}
			@Override
			public void ancestorMoved(AncestorEvent arg0) {
			}
			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				
				if (doNotSaveStateOnExit) {
					return;
				}
				
				// Save splitter position.
				int maximumSplitterPosition = splitPane.getMaximumDividerLocation();
				splitterPositionFromEnd = maximumSplitterPosition - splitPane.getDividerLocation();
			}});
	}

	/**
	 * Initialize key strokes.
	 */
	@SuppressWarnings("serial")
	private void initKeyStrokes() {
		
		tableSlots.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "removeHighlights");
		tableSlots.getActionMap().put("removeHighlights", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {

				escapeFoundSlotsMode();
				
				// Load slots.
				loadSlots();
			}}
		);
		
		tableSlots.getInputMap().put(KeyStroke.getKeyStroke("control F"), "find");
		tableSlots.getActionMap().put("find", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {

				onSearch();
			}}
		);
	}

	/**
	 * Initialize key listener.
	 */
	private void initKeyListener() {
		
		// Initialize key sequence reset timer.
		keySequenceResetTimer = new Timer(1200, (e) -> {
			
			keySequence = "";
		});
		keySequenceResetTimer.setRepeats(false);
		
		// Set key listener.
		tableSlots.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
				char key = e.getKeyChar();
				
				// A Key with ALT sequentially selects slot with given start character.
				if (e.isAltDown()) {
					selectSlotWithCharacter(key);
				}
				else {
					// Otherwise find start character sequence.
					keySequence += Character.toUpperCase(key);
					
					// Select slots and restart timer.
					selectSlotWithCharacterSequence(keySequence);
					
					// Start sequence reset.
					keySequenceResetTimer.restart();
				}
			}
		});
	}

	/**
	 * Select slot with start character sequence.
	 * @param key
	 */
	protected void selectSlotWithCharacterSequence(String keySequence) {
		
		int modelColumnIndex = getSearchableColumnModelIndex();
		int size = tableModel.getRowCount();
		
		for (int viewRowIndex = 0; viewRowIndex < size; viewRowIndex++) {
			
			int modelRowIndex = tableSlots.convertRowIndexToModel(viewRowIndex);
			
			Object columnValue = tableModel.getValueAt(modelRowIndex, modelColumnIndex);
			if (columnValue == null) {
				continue;
			}
			
			// Check table cell text value.
			String textValue = columnValue.toString().toUpperCase();
			
			if (!textValue.isEmpty() && textValue.startsWith(keySequence)) {
				
				tableSlots.getSelectionModel().setSelectionInterval(viewRowIndex, viewRowIndex);
				Utility.ensureRecordVisible(tableSlots, viewRowIndex, true);
				return;
			}
		}
		
		return;
	}

	/**
	 * Select slot with given character.
	 * @param key
	 */
	protected void selectSlotWithCharacter(char key) {
		
		// Select slot that start with given character. Begin from current selection.
		int currentViewRowIndex = tableSlots.getSelectedRow();
		if (currentViewRowIndex == -1) {
			
			currentViewRowIndex = 0;
			searchDirectionForward = true;
		}
		
		// Search forward.
		if (searchDirectionForward) {
			
			if (!searchForward(currentViewRowIndex, key)) {
				searchDirectionForward = false;
			}
		}
		
		// Search backward.
		if (!searchDirectionForward) {
			
			if (!searchBackward(currentViewRowIndex, key)) {
				
				searchDirectionForward = true;
				searchForward(currentViewRowIndex, key);
			}
		}
	}

	/**
	 * Get searchable column index.
	 * @return
	 */
	private int getSearchableColumnModelIndex() {
		
		// Get view 0 model index.
		int modelColumnIndex = tableSlots.convertColumnIndexToModel(0);
		
		// Trim column index for Builder. Do not use access values column.
		if (ProgramGenerator.isExtensionToBuilder() && modelColumnIndex == 0) {
			
			// Get view 1 model index.
			modelColumnIndex = tableSlots.convertColumnIndexToModel(1);
		}

		return modelColumnIndex;
	}

	/**
	 * Search forward.
	 * @param currentViewRowIndex
	 * @param key
	 * @return
	 */
	private boolean searchForward(int currentViewRowIndex, char key) {
		
		key = Character.toUpperCase(key);
		
		int modelColumnIndex = getSearchableColumnModelIndex();
		int size = tableModel.getRowCount();
		
		for (int viewRowIndex = currentViewRowIndex + 1; viewRowIndex < size; viewRowIndex++) {
			
			int modelRowIndex = tableSlots.convertRowIndexToModel(viewRowIndex);
			
			Object columnValue = tableModel.getValueAt(modelRowIndex, modelColumnIndex);
			if (columnValue == null) {
				continue;
			}
			
			// Check table cell text value.
			String textValue = columnValue.toString();
			
			if (!textValue.isEmpty() && Character.toUpperCase(textValue.charAt(0)) == key) {
				
				tableSlots.getSelectionModel().setSelectionInterval(viewRowIndex, viewRowIndex);
				Utility.ensureRecordVisible(tableSlots, viewRowIndex, true);
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Search backward.
	 * @param currentViewRowIndex
	 * @param key
	 */
	private boolean searchBackward(int currentViewRowIndex, char key) {
		
		key = Character.toUpperCase(key);
		
		int modelColumnIndex = getSearchableColumnModelIndex();
		
		for (int viewRowIndex = currentViewRowIndex - 1; viewRowIndex >= 0; viewRowIndex--) {
			
			int modelRowIndex = tableSlots.convertRowIndexToModel(viewRowIndex);
						
			Object columnValue = tableModel.getValueAt(modelRowIndex, modelColumnIndex);
			if (columnValue == null) {
				continue;
			}
			
			// Check table cell text value.
			String textValue = columnValue.toString();
			
			if (!textValue.isEmpty() && Character.toUpperCase(textValue.charAt(0)) == key) {
				
				tableSlots.getSelectionModel().setSelectionInterval(viewRowIndex, viewRowIndex);
				Utility.ensureRecordVisible(tableSlots, viewRowIndex, true);
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Escape found slots mode.
	 */
	private void escapeFoundSlotsMode() {
		
		searchDirectionForward = true;
		
		foundSlots.clear();
		buttonShowOnlyFound.setSelected(false);
	}
	
	/**
	 * Localize components.
	 */
	protected void localize() {

		Utility.localize(labelSlots);
		Utility.localize(menuUseSlots);
		Utility.localize(menuMoveSlots);
		Utility.localize(menuCopySlots);
		Utility.localize(menuFocusArea);
		Utility.localize(menuSetDefaultNormal);
		Utility.localize(menuClearSearch);
	}

	/**
	 * Set icons.
	 */
	protected void setIcons() {
		
		menuUseSlots.setIcon(Images.getIcon("org/multipage/gui/images/paste_icon.png"));
		menuMoveSlots.setIcon(Images.getIcon("org/multipage/generator/images/move_icon.png"));
		menuCopySlots.setIcon(Images.getIcon("org/multipage/gui/images/copy_icon.png"));
		menuFocusArea.setIcon(Images.getIcon("org/multipage/gui/images/search_icon.png"));
		menuSetDefaultNormal.setIcon(Images.getIcon("org/multipage/generator/images/default_value.png"));
		menuClearSearch.setIcon(Images.getIcon("org/multipage/generator/images/clear_search.png"));
	}

	/**
	 * Initialize tool bars.
	 */
	protected void createToolBar() {

        // Add tool bar buttons.
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit_full.png", this, "onEditFull", "org.multipage.generator.tooltipEditSlot");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit_simple.png", this, "onEditSimple", "org.multipage.generator.tooltipSimpleEditSlot");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png", this, "onNewUserSlot", "org.multipage.generator.tooltipNewSlot");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_node.png", this, "onRemoveUserSlot", "org.multipage.generator.tooltipRemoveSlot");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png", this, "onUpdate", "org.multipage.generator.tooltipUpdateSlots");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/search_icon.png", this, "onSearch", "org.multipage.generator.tooltipSearchInSlots");
        buttonShowOnlyFound = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/display_search.png", this, "onClickShowFound", "org.multipage.generator.tooltipShowFoundSlots");
        toolBar.addSeparator();
        buttonShowUserSlots = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/preferred.png", this, "onShowUser", "org.multipage.generator.tooltipShowUserSlots");
        toolBar.addSeparator();
        ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/help_small.png", this, "onEditSlotDescription", "org.multipage.generator.tooltipEditUserSlotDescription");
	}
	
	/**
	 * On edit slot description.
	 */
	public void onEditSlotDescription() {

		editSlotDescription();
	}
	
	/**
	 * Edit slot description.
	 */
	protected void editSlotDescription() {
		
		// Get selected table items.
		int [] selectedRows = getSelectedRows();
		// If single slot not selected, inform user and exit the method.
		if (selectedRows.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
			return;
		}
		
		// Get selected slot and edit it.
		Slot slot = (Slot) tableModel.get(selectedRows[0]);
		
		// Check user slot in Generator.
		if (!ProgramGenerator.isExtensionToBuilder() && !slot.isUserDefined()) {
			
			Utility.show(this, "org.multipage.generator.messageSlotHelpForUserDefined");
			return;
		}
		
		// Edit slot description.
		SlotDescriptionDialog.showDialog(this, slot);
		
		// Load slots.
		loadSlots();
	}
	
	/**
	 * On new user slot.
	 */
	@SuppressWarnings("unused")
	private void onNewUserSlot() {
		
		// If there is more than one holder, inform user and exit.
		if (areas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageMoreThanOneHolderSelected");
			return;
		}
		
		Area area = areas.getFirst();
		
		// Cannot add user slot to area without constructor
		// TODO: temporary removed
		/*if (!area.isAreaConstructor()) {
			Utility.show(this, "org.multipage.generator.messageCannotAddSlotToArea");
			return;
		}*/
		
		// Get new user slot type and alias.
		Obj<String> slotAlias = new Obj<String>();
		Obj<SlotType> slotType = new Obj<SlotType>();
		Obj<Boolean> isInheritable = new Obj<Boolean>();
		
		if (!UserSlotInput.showDialog(this, slotAlias, slotType, isInheritable)) {
			return;
		}
		
		// Check if the slot alias already exists.
		if (existsSlotAlias(slotAlias.ref)) {
			Utility.show(this, "org.multipage.generator.messageSlotNameAlreadyExists");
			return;
		}

		// Create new slot.
		Slot slot = new Slot(area);
		
		// Set slot alias and set the slot as a localized text slot. Also set it as user defined and preferred.
		slot.setAlias(slotAlias.ref);
		if (SlotType.isText(slotType.ref))
			slot.setLocalizedTextValue("");
		slot.setLocalized(slotType.ref);
		slot.setUserDefined(true);
		slot.setPreferred(true);
		slot.setValueMeaning(slotType.ref);
		
		// Set inheritance.
		slot.setAccess(isInheritable.ref ? Slot.publicAccess : Slot.privateAccess);
		
		// If the slot should have an external provider, find it.
		if (SlotType.EXTERNAL_PROVIDER.equals(slotType.ref)) {
			
			if (!ExternalProviderDialog.showDialog(this, slot)) {
				return;
			}
		}
		
		// Edit slot data.
		SlotEditorFrame editor = new SlotEditorFrame(slot, true, true, null, new Callback() {
			@Override
			public Object run(Object parameter) {
				
				onChange();
				
				if (!(parameter instanceof Slot)) {
					return null;
				}
				
				Slot slot = (Slot) parameter;
				
				SwingUtilities.invokeLater(() -> { ensureSlotVisible(slot.getId()); });
				return null;
			}
		});

		editor.setVisible(true);
	}

	/**
	 * Return true value if first area has a slot with given alias.
	 * @param slotAlias
	 * @return
	 */
	private boolean existsSlotAlias(String slotAlias) {

		if (areas.size() != 1) {
			return false;
		}
		
		Area area = areas.getFirst();
		
		return area.getSlot(slotAlias) != null;
	}

	/**
	 * On remove user slot.
	 */
	@SuppressWarnings("unused")
	private void onRemoveUserSlot() {
		
		// Get selected table items.
		int [] selectedRows = getSelectedRows();
		// If nothing selected, inform user and exit the method.
		if (selectedRows.length == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectUserSlots");
			return;
		}
		
		// Confirm deletion.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.generator.messageDeleteSelectedUserSlots"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		// Items to delete.
		LinkedList<Slot> slotsToDelete = new LinkedList<Slot>();
		
		// Database login.
		Middle middle = ProgramBasic.getMiddle();
		MiddleResult result = middle.login(ProgramBasic.getLoginProperties());
		if (result.isOK()) {
			
			// Do loop for all selected indices.
			for (int rowIndex : selectedRows) {
				
				// Get slot and its holder and remove the slot.
				Slot slot = tableModel.get(rowIndex);
				
				if (!slot.isUserDefined()) {
					continue;
				}
				
				result = middle.removeSlot(slot);
				if (result.isNotOK()) {
					break;
				}
				
				slotsToDelete.add(slot);
			}
			
			// Database logout.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		// On error inform user and exit the method.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Check list and inform user.
		if (slotsToDelete.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageNoUserSlotsDeleted");
			return;
		}
		
		// Remove selected items.
		tableModel.removeAll(slotsToDelete);
		
		onChange();
		
		// Update information.
		ConditionalEvents.transmit(SlotListPanel.this, Signal.removeUserSlots);
	}

	/**
	 * On click show found.
	 */
	public void onClickShowFound() {
		
		if (foundSlots.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageNoSlotsMarkedFound");
			
			escapeFoundSlotsMode();
			return;
		}
		
		onShowFound();
	}
	
	/**
	 * Initialize table.
	 */
	protected void initTable() {

		tableModel = new SlotsTableModel(areas, foundSlots, buttonShowOnlyFound.isSelected(), !buttonShowUserSlots.isSelected());
		tableSlots.setModel(tableModel);
		
		tableSlots.setAutoCreateRowSorter(true);
        DefaultRowSorter sorter = ((DefaultRowSorter) tableSlots.getRowSorter());
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
		
		// Set renderer.
		setTableRenderer();
		
		// Set selection listener.
		ListSelectionModel listSelectionModel = tableSlots.getSelectionModel();
		
		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {

				// Load slot description.
				loadSlotDescription();
			}
		};
		listSelectionModel.addListSelectionListener(listSelectionListener);
	}

	/**
	 * Set table renderer.
	 */
	protected void setTableRenderer() {
		
		// Create cell renderer.
		TableCellRenderer cellRenderer = new TableCellRenderer() {
			// Renderer.
			SlotCellRenderer renderer = new SlotCellRenderer();
			// Get renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				// Convert column index.
				column = tableSlots.convertColumnIndexToModel(column);
				// Convert row index.
				row = tableSlots.convertRowIndexToModel(row);
								
				Slot slot = tableModel.get(row);
				renderer.setSlotCell(slot, column, value, isSelected, hasFocus,
						FoundSlot.isSlotFound(foundSlots, slot), false);

				return renderer;
			}
		};
		
		// Set renderers.
		TableColumnModel columnModel = tableSlots.getColumnModel();
		for (int index = 0; index < columnModel.getColumnCount(); index++) {
			TableColumn column = columnModel.getColumn(index);
			column.setCellRenderer(cellRenderer);
		}
	}

	/**
	 * Get table column width sum.
	 * @return
	 */
	protected int getColumnWidthSum() {
		
		if (tableSlots == null) {
			return 0;
		}
		
		TableColumnModel columnModel = tableSlots.getColumnModel();
		int count = columnModel.getColumnCount();
		int sum = 0;
		
		for (int index = 0; index < count; index++) {
			sum += columnModel.getColumn(index).getPreferredWidth();
		}

		return sum;
	}

	/**
	 * Load slots.
	 * @param areas
	 */
	public void loadSlots() {

		// Load slots.
		if (useDatabase) {
			loadSlotsFromDatabase();
		}
		
		// Get selection.
		LinkedList<Slot> oldSelectedSlots = new LinkedList<Slot>();
		for (int selectedRow : getSelectedRows()) {
			
			oldSelectedSlots.add(tableModel.get(selectedRow));
		}
		
		// Set table.
		doNotUpdateSlotDescription = true;
		tableSlots.removeAll();
		tableSlots.clearSelection();
		tableModel.setList(areas, foundSlots, buttonShowOnlyFound.isSelected(), !buttonShowUserSlots.isSelected());
		tableModel.fireTableDataChanged();
		
		// Set selection.
		int rowCount = tableSlots.getRowCount();
		
		for (int row = 0; row < rowCount; row++) {
			Slot slot = tableModel.get(row);
			
			for (Slot oldSlot : oldSelectedSlots) {
				if (slot.equals(oldSlot)) {
					
					int viewRow = tableSlots.convertRowIndexToView(row);
					tableSlots.getSelectionModel().addSelectionInterval(viewRow, viewRow);
				}
			}
		}
		
		// Load slot description.
		SwingUtilities.invokeLater(() -> {
			
			doNotUpdateSlotDescription = false;
			loadSlotDescription();
		});
	}

	/**
	 * Initialize load description timer.
	 */
	private void initLoadDescriptionTimer() {
		
		loadDescriptionTimer = new Timer(100, (e) -> { loadSlotDescriptionFunction(); });
		
		loadDescriptionTimer.setRepeats(false);
		loadDescriptionTimer.setCoalesce(true);
	}

	/**
	 * Load slot description.
	 */
	protected void loadSlotDescription() {
		
		if (doNotUpdateSlotDescription) {
			return;
		}
		
		if (loadDescriptionTimer != null) {
			loadDescriptionTimer.restart();
		}
	}

	/**
	 * Load slot description function.
	 */
	private void loadSlotDescriptionFunction() {
		
		// If a single slot is selected, show slot description.
		int selectionCount = tableSlots.getSelectedRowCount();
				
		final String messageFormat = "<html><i>%s</i></html>";
		
		//boolean highlightDescriptionBackground = false;
		
		if (selectionCount == 1) {
			
			// Get selected slot.
			int [] selectedRows = getSelectedRows();
			if (selectedRows.length != 1) {
				
				Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
				return;
			}
			
			// Get selected slot and its description.
			Slot slot = (Slot) tableModel.get(selectedRows[0]);						
			long descriptionSlotId = slot.getId();

			// Load description.
			Middle middle = ProgramBasic.getMiddle();
			Properties login = ProgramBasic.getLoginProperties();
			
			Obj<String> description = new Obj<String>("");
			
			MiddleResult result = middle.loadSlotDescription(login, descriptionSlotId, description);
			if (result.isNotOK()) {
				result.show(this);
			}
			
			// Display description.
			if (!description.ref.isEmpty()) {
				textDescription.setText(String.format("<html>%s</html>", description.ref));
				
				//highlightDescriptionBackground = true;
			}
			else {
				textDescription.setText(String.format(messageFormat,
						Resources.getString("org.multipage.generator.messageNoSlotInformation")));
			}
		}
		else if (selectionCount > 1) {
			textDescription.setText(String.format(messageFormat,
					Resources.getString("org.multipage.generator.messageMulitpleSlotSelection")));
		}
		else {
			textDescription.setText(String.format(messageFormat,
					Resources.getString("org.multipage.generator.messageNoSlotSelected")));
		}
		
		// Reset scroll bar position.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				Utility.resetScrollBarPosition(scrollPaneDescription);
			}
		});
		
		// Set description background.
		//textDescription.setBackground(highlightDescriptionBackground ? new Color(255, 255, 204) : UIManager.getColor("Panel.background"));
	}

	/**
	 * Load slots from database.
	 */
	protected void loadSlotsFromDatabase() {
		
		// Clear area slots.
		Area.clearSlots(areas);
		
		// Load area slots.
		MiddleResult result = ProgramBasic.getMiddle().loadAreasSlots(
				ProgramBasic.getLoginProperties(), areas, false, true);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
	}

	/**
	 * On change slots.
	 */
	protected void onChange() {
		
		// Transmit the "update all" signal.
		ConditionalEvents.transmit(SlotListPanel.this, Signal.updateAll);
	}
	
	/**
	 * On update.
	 */
	public void onUpdate() {
		
		// Reset found slots.
		update();
	}
	
	/**
	 * Update the list.
	 */
	public void update() {
		
		// Reset found slots.
		escapeFoundSlotsMode();
		
		// Load slots.
		loadSlots();
	}	

	/**
	 * Select all items.
	 */
	public void onShowFound() {

		tableModel.setList(areas, foundSlots, buttonShowOnlyFound.isSelected(), buttonShowUserSlots.isSelected());
		
		// Load slots.
		loadSlots();
	}
	
	/**
	 * On show all slots.
	 */
	public void onShowUser() {
		
		showUserSlots = buttonShowUserSlots.isSelected();
		
		// Load slots.
		loadSlots();
		
		// Ensure selected slot visibility.
		SwingUtilities.invokeLater(() -> {
			
			int viewRowIndex = tableSlots.getSelectedRow();
			if (viewRowIndex != -1) {
				Utility.ensureRecordVisible(tableSlots, viewRowIndex, true);
			}
		});
	}
	
	/**
	 * Edit slot.
	 */
	public void onEdit() {
		
		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length != 1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
			return;
		}
		
		// Get selected slot and edit it.
		Slot slot = (Slot) tableModel.get(selectedRows[0]);
		FoundAttr foundAttr = FoundSlot.getFoundAtt(searchedInValues ? foundSlots : null, slot);
		boolean showSimple = false;
		if (slot.isLocalized()) {
			try {
				// Edit slot data.
				SlotEditorFrame.showDialog(slot, false, true, foundAttr, new Callback() {
					@Override
					public Object run(Object parameter) {
						onChange();
						return null;
					}
				});
			}
			catch (Exception e) {
				// Inform user.
				Utility.show(this, "org.multipage.generator.messageCannotEditHtmlUsingSimpleEditor");
				showSimple = true;
			}
		}
		else {
			showSimple = true;
		}
		if (showSimple) {
			
			// Show simple editor.
			SlotEditorFrame.showDialog(slot, false, false, foundAttr, new Callback() {
				@Override
				public Object run(Object parameter) {
					onChange();
					return null;
				}
			});
		}
		
		onChange();
	}
	
	/**
	 * Convert view row indices to model indices.
	 * @param viewRows
	 * @return
	 */
	private int[] convertViewRowsToModel(int[] viewRows) {
		
		int rowsCount = viewRows.length;
		
		int [] modelRows = new int [rowsCount];
		
		for (int index = 0; index < rowsCount; index++) {
			modelRows[index] = tableSlots.convertRowIndexToModel(viewRows[index]);
		}
		
		return modelRows;
	}

	/**
	 * Edit slot.
	 */
	public void onEditFull() {
		
		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length != 1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
			return;
		}
		
		// Get selected slot and edit it.
		Slot slot = (Slot) tableModel.get(selectedRows[0]);
		FoundAttr foundAttr = FoundSlot.getFoundAtt(foundSlots, slot);
		
		try {
			SlotEditorFrame.showDialog(slot, false, true, foundAttr, new Callback() {
				@Override
				public Object run(Object parameter) {
					onChange();
					return null;
				}
			});
		}
		catch (Exception e) {
			
			// Inform user and show simple editor.
			Utility.show(this, "org.multipage.generator.messageCannotEditHtmlUsingSimpleEditor");
			
			// Show simple editor.
			SlotEditorFrame.showDialog(slot, false, false, foundAttr, new Callback() {
				@Override
				public Object run(Object parameter) {
					onChange();
					return null;
				}
			});
		}
		
		onChange();
	}
	
	/**
	 * Edit slot (simple).
	 */
	public void onEditSimple() {
		
		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length != 1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleSlot");
			return;
		}
		
		// Get selected slot and edit it.
		Slot slot = (Slot) tableModel.get(selectedRows[0]);
		FoundAttr foundAttr = FoundSlot.getFoundAtt(foundSlots, slot);
		SlotEditorFrame.showDialog(slot, false, false, foundAttr, new Callback() {
			@Override
			public Object run(Object parameter) {
				onChange();
				return null;
			}
		});
		onChange();
	}
	
	/**
	 * Set areas.
	 * @param areas
	 */
	public void setAreas(LinkedList<Area> areas) {

		this.areas = areas;
		
		j.log("SELECTED AREAS %s", areas.toString());
		
		// Load slots.
		loadSlots();
		
		// Try to update slot search info.
		updateSearch();
	}
	
	/**
	 * Set single area.
	 * @param area
	 */
	public void setArea(Area area) {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		areas.add(area);
		
		setAreas(areas);
	}
	
	/**
	 * Set whether to use database when loading slots for areas.
	 * @param use
	 */
	public void setUseDatabase(boolean use) {
		
		this.useDatabase = use;
	}

	/**
	 * Set table columns.
	 * @param columnWidths
	 */
	protected void setTableColumnWidths(int[] columnWidths) {

		int length = columnWidths.length;
		TableColumnModel columnModel = tableSlots.getColumnModel();
		int columnCount = columnModel.getColumnCount();
		
		for (int index = 0; index < length && index < columnCount; index++) {
			// Get column.
			TableColumn column = columnModel.getColumn(index);
			// Set preferred width.
			column.setPreferredWidth(columnWidths[index]);
		}
	}
	
	/**
	 * Set column widths.
	 * @param columnWidths
	 */
	public void setTableColumnWidths(Integer[] columnWidths) {
		
		int length = columnWidths.length;
		TableColumnModel columnModel = tableSlots.getColumnModel();
		int columnCount = columnModel.getColumnCount();
		
		for (int index = 0; index < length && index < columnCount; index++) {
			// Get column.
			TableColumn column = columnModel.getColumn(index);
			// Set preferred width.
			column.setPreferredWidth(columnWidths[index]);
		}
	}
	
	/**
	 * Get table columns.
	 * @param columnWidths
	 */
	public void getTableColumnWidths(int[] columnWidths) {

		int length = columnWidths.length;
		TableColumnModel columnModel = tableSlots.getColumnModel();
		int columnCount = columnModel.getColumnCount();
		
		for (int modelIndex = 0; modelIndex < length && modelIndex < columnCount; modelIndex++) {
			
			int viewIndex = tableSlots.convertColumnIndexToView(modelIndex);
			
			// Get column.
			TableColumn column = columnModel.getColumn(viewIndex);
			// Set preferred width.
			columnWidths[modelIndex] = column.getPreferredWidth();
		}
	}
	
	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	protected void addPopup(Component component, final JPopupMenu popup) {
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
				if (!slotEditingEnabled) {
					return;
				}
				enableMenu();
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	/**
	 * On search in slots.
	 */
	public void onSearch() {
		
		// Show search dialog.
		if (searchDialog == null) {
			searchDialog = SearchSlotDialog.createDialog("org.multipage.generator.textSearchInSlots", this);
		}
		
		// Show dialog.
		searchDialog.showModal();
				
		// Update search.
		int resultCount = updateSearch();
		
		// Get reset flag and reset found slots.
		boolean reset = searchDialog.getResetFlag();
		if (reset) {
			escapeFoundSlotsMode();
			return;
		}

		// Display messages.
		if (searchDialog.showCount()) {
			Utility.show(this, "org.multipage.generator.textNumberOfFoundSlots", resultCount);
		}
		else if (resultCount == 0) {
			Utility.show(this, "org.multipage.generator.textNoSlotsFound", resultCount);
		}
	}
	
	/**
	 * Update search.
	 * @return number of found providers
	 */
	private int updateSearch() {
		
		int resultCount = 0;
		
		// Check dialog.
		if (searchDialog == null) {
			return resultCount;
		}
		
		searchedInValues = searchDialog.getSearchInValues();
		
		foundSlots.clear();
		buttonShowOnlyFound.setSelected(false);
		loadSlots();
		
		if (searchDialog.isConfirmed()) {
			FoundAttr foundAttr = searchDialog.getFoundAttr();
			boolean searchInValues = searchDialog.isSearchInValues();
			boolean searchInDescriptions = searchDialog.isSearchInDescriptions();
			
			boolean isFirstExceptionShown = false;
			
			// Do loop for all holders.
			for (SlotHolder holder : areas) {
				// Do loop for all slots.
				for (Slot slot : holder.getSlots()) {
					
					String text = null;
					
					if (searchInValues) {
						Object value = slot.getValue();
						if (value != null) {
							text = value.toString();
						}
						else {
							continue;
						}
					}
					else if (searchInDescriptions) {
						try {
							text = slot.loadDescription(ProgramBasic.getLoginProperties(), ProgramBasic.getMiddle());
						}
						catch (Exception e) {
							
							if (!isFirstExceptionShown) {
								Utility.show2(this, e.getLocalizedMessage());
							}
							isFirstExceptionShown = true;
							continue;
						}
					}
					else {
						// Depends on application type.
						text = ProgramGenerator.isExtensionToBuilder() ? slot.getAliasWithId() : slot.getNameForGenerator() + (!foundAttr.isWholeWords ? " " + slot.getAlias() : "");
					}
					
					// If the text is found.
					if (Utility.find(text, foundAttr)) {
						
						// Add to found slots.
						foundSlots.add(new FoundSlot(slot, foundAttr));
						resultCount++;
					}
				}
			}
			
			// Show found slots.
			if (resultCount > 0) {
				
				SwingUtilities.invokeLater(() -> {
					
					buttonShowOnlyFound.setSelected(true);
					onShowFound();
				});
			}
		}		
		// Redraw the table.
		tableSlots.updateUI();
		
		// Return number of results.
		return resultCount;
	}
	
	/**
	 * Set do not save state on exit flag.
	 */
	public void setDoNotSaveStateOnExit() {
		
		doNotSaveStateOnExit = true;
	}

	/**
	 * Find slot in list and set it visible.
	 * @param slotId
	 */
	protected void ensureSlotVisible(Long slotId) {
		
		if (slotId == null) {
			return;
		}

		int rowCount = tableModel.getRowCount();
		
		// Do loop for all slots.
		for (int modelRowIndex = 0; modelRowIndex < rowCount; modelRowIndex++) {
			
			Slot slot = tableModel.get(modelRowIndex);
			if (slot == null) {
				continue;
			}
			
			if (slot.getId() == slotId) {
				
				int viewRowIndex = tableSlots.convertRowIndexToView(modelRowIndex);
				
				tableSlots.getSelectionModel().setSelectionInterval(viewRowIndex, viewRowIndex);
				Utility.ensureRecordVisible(tableSlots, viewRowIndex, true);
				
				return;
			}
		}
	}

	/**
	 * Returns true value if a single holder is selected. The method
	 * informs user.
	 * @return
	 */
	private boolean isSingleHolder() {
		
		if (areas.size() != 1) {
			Class<?> holderClass = areas.get(0).getClass();
			String message;
			if (holderClass.equals(Area.class)) {
				message = "org.multipage.generator.messageSelectSingleArea";
			}
			else {
				message = "org.multipage.generator.messageSelectSingleHolder";
			}
			Utility.show(this, message);
			return false;
		}

		return true;
	}

	/**
	 * Returns true value if the slots has not the same holder.
	 * @param holder 
	 * @param clipBoard2
	 * @return
	 */
	private boolean isAnotherHolder(LinkedList<Slot> slots, SlotHolder holder) {
		
		if (holder == null) {
			return true;
		}
		
		for (Slot slot : clipBoard) {
			
			SlotHolder slotHolder = slot.getHolder();
			if (slotHolder == null) {
				continue;
			}
			
			if (slotHolder.getId() == holder.getId()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Enable trayMenu.
	 * @param popup
	 */
	private void enableMenu() {
		
		boolean enable = false;
		
		if (areas.size() == 1 && !clipBoard.isEmpty()) {
			SlotHolder holder = areas.get(0);
			if (isAnotherHolder(clipBoard, holder)) {
				enable = true;
			}
		}
		
		// Enable / disable trayMenu items.
		menuMoveSlots.setEnabled(enable);
		menuCopySlots.setEnabled(enable);
	}
	
	/**
	 * Delete slots.
	 * @param middle 
	 * @param slots
	 */
	private MiddleResult deleteSlots(Middle middle, List<Slot> slots) {
		
		for (Slot slot : slots) {
			MiddleResult result = middle.removeSlot(slot);
			if (result.isNotOK()) {
				return result;
			}
		}
		
		return MiddleResult.OK;
	}

	/**
	 * Copy slots.
	 */
	protected void useSlots() {
		
		// Get selected objects.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectSlots");
			return;
		}
		
		List<Slot> slots = tableModel.getSlots();
		// Check.
		if (slots.size() == 0) {
			return;
		}
		
		clipBoard.clear();
		
		// Use selected slots.
		for (int selectedRow : selectedRows) {
			Slot slot = (Slot) slots.get(selectedRow);
			clipBoard.add(slot);
		}
	}

	/**
	 * Move slots.
	 * @param copy 
	 */
	protected void moveSlots(boolean copy) {
		
		// Must be a single holder.
		if (!isSingleHolder()) {
			return;
		}
		
		// Clip board must not be empty.
		if (clipBoard.size() == 0) {
			Utility.show(this, "org.multipage.generator.messageClipboardIsEmpty");
			return;
		}
		
		SlotHolder holder = areas.get(0);
		
		// Must be another holder.
		if (!isAnotherHolder(clipBoard, holder)) {
			Utility.show(this, "org.multipage.generator.messageCopyOrMoveSlotsToAnotherPlace");
			return;
		}

		// Check slot existence.
		List<Slot> tableSlots = tableModel.getSlots();
		List<Slot> slotsToMoveOrCopy = new LinkedList<Slot>();
		List<Slot> slotsToDelete = new LinkedList<Slot>();
		
		// Confirm paste.
		if (!SelectSlotsOverride.showDialog(GeneratorMainFrame.getFrame(),
				tableSlots, clipBoard, slotsToDelete, slotsToMoveOrCopy)) {
			return;
		}
		
		// If nothing to move, exit the method.
		if (slotsToMoveOrCopy.isEmpty()) {
			return;
		}
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			// Delete slots.
			result = deleteSlots(middle, slotsToDelete);
			if (result.isOK()) {
				
				if (copy) {
					
					// Remove description references.
					if (ProgramGenerator.isExtensionToBuilder()
							&& Utility.ask(this, "org.multipage.generator.textRemoveSlotDescriptionLinks")) {
						
						Slot.removeDescriptions(slotsToMoveOrCopy);
					}
					
					// Copy slots.
					result = middle.insertSlotsHolder(slotsToMoveOrCopy, holder);
				}
				else {
					// Move slots.
					result = middle.updateSlotsHolder(slotsToMoveOrCopy, holder);
					// Clear clipboard.
					clipBoard.clear();
				}
			}
			
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Update data.
		onChange();
	}
}