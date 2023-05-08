/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.multipage.gui.*;
import org.multipage.util.*;
import org.multipage.basic.*;
import org.multipage.generator.*;
import org.maclan.*;

/**
 * @author
 *
 */
public class SlotListPanelBuilder extends SlotListPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Show hidden slots flag.
	 */
	public static boolean showHiddenSlots = false;

	/**
	 * Menu items.
	 */
	private JMenuItem menuSetSelectedSlots;
	protected JMenuItem menuEditDescription;
	private JMenuItem menuCopyTextValue;

	/**
	 * Constructor.
	 */
	public SlotListPanelBuilder() {
		
		postCreate();
	}

	/**
	 * Load slots from database.
	 */
	@Override
	protected void loadSlotsFromDatabase() {
		
		MiddleResult result = ProgramBasic.getMiddle().loadAreasSlots(
				ProgramBasic.getLoginProperties(), areas, showHiddenSlots, true);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.generator.SlotListPanel#postCreate()
	 */
	@Override
	protected void postCreate() {
		
		// Call superclass method.
		super.postCreate();
	}

	/**
	 * Initialize tool bars.
	 */
	@Override
	protected void createToolBar() {

        // Add tool bar buttons.
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png", this, "onNew", "builder.tooltipNewSlot");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/rename_node.png", this, "onEditFull", "org.multipage.generator.tooltipEditSlot");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit_simple.png", this, "onEditSimple", "org.multipage.generator.tooltipSimpleEditSlot");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_node.png", this, "onRemove", "builder.tooltipRemoveSlot");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png", this, "onUpdate", "org.multipage.generator.tooltipUpdateSlots");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/select_all.png", this, "onSelectAll", "builder.tooltipSelectAllSlots");
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/deselect_all.png", this, "onUnselectAll", "builder.tooltipUnselectAllSlots");
        toolBar.addSeparator();
        ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/search_icon.png", this, "onSearch", "org.multipage.generator.tooltipSearchInSlots");
        toolBar.addSeparator();
        buttonShowUserSlots = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/preferred.png", this, "onShowUser", "org.multipage.generator.tooltipShowUserSlots");
        buttonShowOnlyFound = ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/selected.png", this, "onClickShowFound", "org.multipage.generator.tooltipShowFoundSlots");
        toolBar.addSeparator();
        ToolBarKit.addToggleButton(toolBar, "org/multipage/generator/images/help_small.png", this, "onEditSlotDescription", "builder.tooltipEditSlotDescription");
	}
	
	/**
	 * Localize components.
	 */
	@Override
	protected void localize() {
		
		menuSetDefaultNormal = new JMenuItem(); // Is used in superclass.
		
		Utility.localize(menuSetSelectedSlots);
		Utility.localize(menuEditDescription);
		Utility.localize(menuCopyTextValue);

	}

	/**
	 * Set icons.
	 */
	@Override
	protected void setIcons() {
		
		menuSetSelectedSlots.setIcon(Images.getIcon("org/multipage/gui/images/properties.png"));
		menuEditDescription.setIcon(Images.getIcon("org/multipage/generator/images/help_small.png"));
		menuCopyTextValue.setIcon(Images.getIcon("org/multipage/generator/images/copy_value.png"));
	}

	/**
	 * On new slot.
	 */
	public void onNew() {
		
		// If there is more than one holder, inform user and exit.
		if (areas.size() != 1) {
			Utility.show(this, "builder.messageMoreThanOneHolderSelected");
			return;
		}

		// Create new slot.
		Slot slot = new Slot(areas.getFirst());
		
		// Edit slot data.
		SlotEditorFrameBuilder editor = new SlotEditorFrameBuilder(slot, true, true, null, new Callback() {
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
	 * On remove slot.
	 */
	public void onRemove() {
		
		// Get selected table items.
		int [] selectedRows = getSelectedRows();
		// If nothing selected, inform user and exit the method.
		if (selectedRows.length == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectSlots");
			return;
		}
		
		// Confirm deletion.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("builder.messageDeleteSelectedSlots"))
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
		
		// Remove selected items.
		tableModel.removeAll(slotsToDelete);
		
		onChange();
		
		// Update information.
		updateInformation();
	}

	/**
	 * Select all items.
	 */
	public void onSelectAll() {
		
		tableSlots.setRowSelectionInterval(0, tableSlots.getRowCount() - 1);
	}
	
	/**
	 * Unselect all items.
	 */
	public void onUnselectAll() {
		
		tableSlots.clearSelection();
	}

	/**
	 * Create trayMenu.
	 */
	@Override
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
		
		menuCopyTextValue = new JMenuItem("org.multipage.generator.textCopySlotTextValue");
		menuCopyTextValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copySlotTextValue();
			}
		});
		popupMenu.add(menuCopyTextValue);
		popupMenu.addSeparator();
		
		menuSetSelectedSlots = new JMenuItem("builder.textSetSelectedSlots");
		menuSetSelectedSlots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSetSlotsProperties();
			}
		});
		popupMenu.add(menuSetSelectedSlots);
		
		popupMenu.addSeparator();
		
		menuEditDescription = new JMenuItem("org.multipage.generator.menuSlotDescription");
		menuEditDescription.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				editSlotDescription();
			}
		});
		popupMenu.add(menuEditDescription);
		
	}

	/**
	 * Copy slot text value.
	 */
	protected void copySlotTextValue() {
		
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
		String textValue = slot.getTextValue();
		
		// Copy value to clipboard.
		Clipboard clipboard = Toolkit.getDefaultToolkit ().getSystemClipboard();
		StringSelection stringSelection = new StringSelection (textValue);
		clipboard.setContents(stringSelection, null);
	}

	/**
	 * On set slots' properties.
	 */
	protected void onSetSlotsProperties() {
		
		// Get selected slots.
		int [] selectedRows = getSelectedRows();
		if (selectedRows.length < 1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSlots");
			return;
		}
		
		// Get slot properties.
		Obj<Character> access = new Obj<Character>();
		Obj<Boolean> hidden = new Obj<Boolean>();
		Obj<Boolean> isDefault = new Obj<Boolean>();
		Obj<Boolean> isPreferred = new Obj<Boolean>();
		
		if (!SlotPropertiesDialog.showDialog(this, access, hidden, isDefault, isPreferred)) {
			return;
		}
		
		if (access.ref == null && hidden.ref == null 
				&& isDefault.ref == null && isPreferred.ref == null) {
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
			
			if (access.ref != null) {
				// Update slot access.
				result = middle.updateSlotAccess(slot.getHolder().getId(), slot.getAlias(),
						access.ref.toString());
				if (result.isNotOK()) {
					break;
				}
			}
			
			if (hidden.ref != null) {
				// Update slot hidden flag.
				result = middle.updateSlotHidden(slot.getHolder().getId(), slot.getAlias(), hidden.ref);
				if (result.isNotOK()) {
					break;
				}
			}
			
			if (isDefault.ref != null) {
				// Update slot default value.
				result = middle.updateSlotIsDefault(slot.getHolder().getId(), slot.getAlias(), isDefault.ref);
				if (result.isNotOK()) {
					break;
				}
			}
			
			if (isPreferred.ref != null) {
				// Update slot is preferred.
				result = middle.updateSlotIsPreferred(slot.getHolder().getId(), slot.getAlias(), isPreferred.ref);
				if (result.isNotOK()) {
					break;
				}
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
		updateInformation();
	}

	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Initialize table.
	 */
	@Override
	protected void initTable() {

		tableModel = new SlotsTableModelBuilder(areas, foundSlots, buttonShowOnlyFound.isSelected(), !buttonShowUserSlots.isSelected());
		tableSlots.setModel(tableModel);
		
		tableSlots.setAutoCreateRowSorter(true);
        DefaultRowSorter sorter = ((DefaultRowSorter) tableSlots.getRowSorter());
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        
		// Set renderer.
		setTableRenderer();
		
		// Set selection listener.
		ListSelectionModel selectionModel = tableSlots.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {

				// Load slot description.
				loadSlotDescription();
			}
		});
	}

	/**
	 * Set table renderer.
	 */
	@Override
	protected void setTableRenderer() {
		
		// Access renderer.
		TableColumn column1 = tableSlots.getColumnModel().getColumn(0);
		column1.setCellRenderer(new TableCellRenderer() {
			// Renderer.
			AccessRenderer renderer = new AccessRenderer();
			// Get renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				if (!(value instanceof Character)) {
					return null;
				}
				
				row = tableSlots.convertRowIndexToModel(row);
				
				Slot slot = tableModel.get(row);
				char access = (Character) value;
				renderer.setProperties(access, isSelected, hasFocus,
						FoundSlot.isSlotFound(foundSlots, slot));
				return renderer;
			}
		});
		
		// Create cell renderer.
		TableCellRenderer cellRenderer = new TableCellRenderer() {
			// Renderer.
			SlotCellRenderer renderer = new SlotCellRenderer();
			// Get renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				// Convert index.
				column = tableSlots.convertColumnIndexToModel(column);
				row = tableSlots.convertRowIndexToModel(row);
				
				Slot slot = tableModel.get(row);
				renderer.setSlotCell(slot, column, value, isSelected, hasFocus, 
						FoundSlot.isSlotFound(foundSlots, slot), true);

				return renderer;
			}
		};
		
		// Set renderers.
		TableColumnModel columnModel = tableSlots.getColumnModel();
		for (int index = 1; index < columnModel.getColumnCount(); index++) {
			TableColumn column = columnModel.getColumn(index);
			column.setCellRenderer(cellRenderer);
		}
	}

	/**
	 * On edit slot description.
	 */
	public void onEditSlotDescription() {
		
		editSlotDescription();
	}

	/**
	 * Set divider position to maximum.
	 */
	public void setDividerPositionToMaximum() {

		setDividerPositionToMaximum  = true;
	}
}


/**
 * Access renderer.
 * @author
 *
 */
class AccessRenderer extends JLabel {
	
	/**
	 * Highlight color.
	 */
	private static final Color highlightColor = new Color(255, 100, 100);
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Icons.
	 */
	private static final Icon publicIcon;
	private static final Icon privateIcon;
	
	/**
	 * Constructor.
	 */
	static {
		
		publicIcon = Images.getIcon("org/multipage/generator/images/public.png");
		privateIcon = Images.getIcon("org/multipage/generator/images/private.png");
	}

	/**
	 * States.
	 */
	private boolean isSelected;
	private boolean hasFocus;

	/**
	 * Constructor.
	 */
	public AccessRenderer() {
		
		setOpaque(true);
	}
	
	/**
	 * Set properties.
	 * @param access
	 * @param hasFocus 
	 * @param isSelected 
	 */
	public void setProperties(char access, boolean isSelected, boolean hasFocus,
			boolean isFound) {
		
		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		if (access == Slot.publicAccess) {
			setIcon(publicIcon);
		}
		else if (access == Slot.privateAccess) {
			setIcon(privateIcon);
		}
		else {
			setIcon(null);
		}
		setBackground(isFound ? highlightColor : Color.WHITE);
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
