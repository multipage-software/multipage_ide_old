/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.maclan.MimeType;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class MimeTypesEditor extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Table model.
	 */
	private MimeTableModel tableModel;

	/**
	 * Select dialog flag.
	 */
	private boolean select = false;

	/**
	 * Selected MIME type.
	 */
	private MimeType selectedMimeType;

	/**
	 * Input MIME type.
	 */
	private Object inputMimeType;

	/**
	 * Components.
	 */
	private JButton buttonClose;
	private JLabel labelMimeTypesList;
	private JScrollPane scrollPane;
	private JTable tableMimeTypes;
	private JMenuBar menuBar;
	private JMenu menuEdit;
	private JMenuItem menuEditAppend;
	private JToolBar toolBar;
	private JMenuItem menuRemoveAll;
	private JMenuItem menuRestoreDefaults;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JButton buttonSelect;

	/**
	 * Show MIME type editor.
	 * @param parent
	 * @param mimeType 
	 * @return
	 */
	public static MimeType showDialog(Component parent, Object mimeType) {
		
		MimeTypesEditor dialog = new MimeTypesEditor(parent, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.inputMimeType = mimeType;
		dialog.setVisible(true);
		return dialog.selectedMimeType;
	}

	/**
	 * On window opened.
	 */
	protected void onWindowOpened() {
		
		if (inputMimeType == null) {
			return;
		}
		selectMimeType(inputMimeType);
	}

	/**
	 * Select MIME type.
	 * @param mimeType
	 */
	private void selectMimeType(Object mimeType) {
		
		if (mimeType == null) {
			return;
		}
		
		ArrayList<MimeType> mimeTypes = tableModel.getMimeTypes();
		
		for (int index = 0; index < mimeTypes.size(); index++) {
			MimeType mimeTypeObject = mimeTypes.get(index);
			
			boolean isEqual = false;
			
			if (mimeType instanceof String) {
				isEqual = mimeTypeObject.type.equals(mimeType);
			}
			else if (mimeType instanceof MimeType) {
				isEqual = mimeTypeObject.equals(mimeType);
			}
			
			if (isEqual) {
				
				tableMimeTypes.getSelectionModel().setSelectionInterval(index, index);
				Utility.ensureRecordVisible(tableMimeTypes, index, false);
				return;
			}
		}
	}

	/**
	 * Show dialog.
	 * @param parent
	 */	
	public static void showEditor(Component parent) {
		
		MimeTypesEditor dialog = new MimeTypesEditor(parent, false);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 * @param select 
	 */
	public MimeTypesEditor(Component parent, boolean select) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				onWindowOpened();
			}
		});
		setMinimumSize(new Dimension(330, 310));
		// Initialize components.
		initComponents();
		// Post creation.
		this.select = select;
		postCreate();
	}

	/**
	 * Initializes components.
	 */
	private void initComponents() {
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 449, 326);
		setTitle("org.multipage.generator.textMimeTypesEditor");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonClose = new JButton("textClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonClose.setMargin(new Insets(2, 4, 2, 4));
		buttonClose.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonClose);
		
		labelMimeTypesList = new JLabel("org.multipage.generator.textMimeTypesList");
		springLayout.putConstraint(SpringLayout.WEST, labelMimeTypesList, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelMimeTypesList, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelMimeTypesList);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelMimeTypesList);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		tableMimeTypes = new JTable();
		scrollPane.setViewportView(tableMimeTypes);
		
		menuBar = new JMenuBar();
		springLayout.putConstraint(SpringLayout.NORTH, labelMimeTypesList, 6, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.NORTH, menuBar, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, menuBar, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(menuBar);
		
		menuEdit = new JMenu("org.multipage.generator.textEdit");
		menuBar.add(menuEdit);
		
		menuEditAppend = new JMenuItem("org.multipage.generator.textAppend");
		menuEditAppend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importMimeTypes();
			}
		});
		menuEditAppend.setPreferredSize(new Dimension(150, 22));
		menuEdit.add(menuEditAppend);
		
		menuRemoveAll = new JMenuItem("org.multipage.generator.textRemoveAll");
		menuRemoveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeAllMimes(true);
			}
		});
		menuEdit.add(menuRemoveAll);
		
		menuRestoreDefaults = new JMenuItem("org.multipage.generator.textRestoreFactoryDefaults");
		menuRestoreDefaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFactoryDefaults();
			}
		});
		menuEdit.add(menuRestoreDefaults);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.NORTH, toolBar, -36, SpringLayout.NORTH, buttonClose);
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, -6, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, toolBar);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, labelMimeTypesList);
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, buttonClose);
		getContentPane().add(toolBar);
		
		labelFilter = new JLabel("org.multipage.generator.textFilter");
		springLayout.putConstraint(SpringLayout.NORTH, labelFilter, 0, SpringLayout.NORTH, buttonClose);
		labelFilter.setVerticalTextPosition(SwingConstants.TOP);
		labelFilter.setVerticalAlignment(SwingConstants.TOP);
		springLayout.putConstraint(SpringLayout.WEST, labelFilter, 0, SpringLayout.WEST, labelMimeTypesList);
		springLayout.putConstraint(SpringLayout.EAST, labelFilter, 33, SpringLayout.WEST, labelMimeTypesList);
		getContentPane().add(labelFilter);
		
		textFilter = new TextFieldEx();
		textFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onFilterChanged();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textFilter, 6, SpringLayout.SOUTH, toolBar);
		springLayout.putConstraint(SpringLayout.WEST, textFilter, 6, SpringLayout.EAST, labelFilter);
		springLayout.putConstraint(SpringLayout.SOUTH, textFilter, 0, SpringLayout.SOUTH, buttonClose);
		getContentPane().add(textFilter);
		textFilter.setColumns(10);
		
		buttonSelect = new JButton("org.multipage.generator.textSelect");
		buttonSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelect();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textFilter, -10, SpringLayout.WEST, buttonSelect);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSelect, 0, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, buttonSelect, -6, SpringLayout.WEST, buttonClose);
		buttonSelect.setPreferredSize(new Dimension(80, 25));
		buttonSelect.setMargin(new Insets(2, 4, 2, 4));
		getContentPane().add(buttonSelect);
	}

	/**
	 * On select.
	 */
	protected void onSelect() {
		
		if (!select) {
			dispose();
			return;
		}
		
		if (tableMimeTypes.getSelectedRowCount() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleMimeType");
			return;
		}
		
		int selectedIndex = tableMimeTypes.getSelectedRow();
		selectedMimeType = (MimeType) tableMimeTypes.getValueAt(selectedIndex, -1);
		
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		// Load tool bar.
		loadToolBar();
		// Localize components.
		localize();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set icons.
		setIcons();
		// Set row sorter.
		tableMimeTypes.setAutoCreateRowSorter(true);
		// Load MIME types.
		loadMimeTypes();
		// Set filter text.
		textFilter.setText("*");
		
		if (!select) {
			buttonSelect.setPreferredSize(new Dimension());
		}
	}

	/**
	 * Load tool bar.
	 */
	private void loadToolBar() {

		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png", this,
				"onNewMime", "org.multipage.generator.tooltipNewMimeType");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/rename_node.png", this,
				"onRename", "org.multipage.generator.tooltipRenameMimeType");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_icon.png", this,
				"onDelete", "org.multipage.generator.tooltipDeleteMimeType");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png", this,
				"onUpdate", "org.multipage.generator.tooltipUpdateMimeTypes");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/select_all.png", this,
				"onSelectAll", "org.multipage.generator.tooltipSelectAllMimeTypes");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/deselect_all.png", this,
				"onUnselectAll", "org.multipage.generator.tooltipUnselectAllMimeTypes");
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(labelMimeTypesList);
		Utility.localize(menuEdit);
		Utility.localize(menuEditAppend);
		Utility.localize(menuRemoveAll);
		Utility.localize(menuRestoreDefaults);
		Utility.localize(labelFilter);
		Utility.localize(buttonSelect);
	}

	/**
	 * Sets icons.
	 */
	private void setIcons() {

		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonSelect.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
	}

	/**
	 * Load MIME types.
	 */
	private void loadMimeTypes() {

		// Reset filter.
		textFilter.setText("*");
		// Set table model.
		tableModel = new MimeTableModel();
		tableMimeTypes.setModel(tableModel);
		// Set table column.
		tableMimeTypes.getColumnModel().getColumn(0).setPreferredWidth(400);
		tableMimeTypes.getColumnModel().getColumn(1).setPreferredWidth(80);
	}

	/**
	 * Imports MIME types.
	 */
	private void importMimeTypes() {
		
		// Get import file.
		JFileChooser dialog = new JFileChooser();
		// Create filters.
		dialog.addChoosableFileFilter(new FileNameExtensionFilter(
		        Resources.getString("org.multipage.generator.textXmlFiles"), "xml"));
		// Show the dialog.
		if (dialog.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = dialog.getSelectedFile();
		// Import data.
		importMimeTypes(file);
		
		ConditionalEvents.transmit(MimeTypesEditor.this, Signal.importMimeTypes);
		
    	// Reload table.
    	loadMimeTypes();
	}

	/**
	 * Import MIMEs from given file.
	 * @param file2
	 */
	private void importMimeTypes(File file) {
		
		if (file.exists()) {
			
			// Test if program can read the file.
			if (!file.canRead()) {
				JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotReadMimeTypesFile"));
				return;
			}
			
			// Create input stream.
			InputStream xmlInputStream = null;
			
			try {
				xmlInputStream = new FileInputStream(file);
				MiddleUtility.importMimeTypes(ProgramBasic.getMiddle(), ProgramBasic.getLoginProperties(),
						xmlInputStream, this);
			}
			catch (Exception e) {
				
				Utility.show2(this, e.getMessage());
			}
			finally {
				// Close objects.
				try {
					if (xmlInputStream != null) {
						xmlInputStream.close();
					}
				}
				catch (Exception e) {
				}
			}
		}
		else {
			// Report error.
			JOptionPane.showMessageDialog(this, String.format(
					Resources.getString("org.multipage.generator.messageFileNotFound"), file));
		}
	}
	
	/**
	 * Remove all MIME records.
	 */
	protected void removeAllMimes(boolean confirmDeletion) {

		// Confirm the deletion.
		if (confirmDeletion) {
			if (JOptionPane.showConfirmDialog(this, Resources.getString("org.multipage.generator.messageConfirmDeletionOfAllMimes"))
				!= JOptionPane.OK_OPTION) {
				return;
			}
		}
		
		// Remove all MIMEs from the database.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result = middle.removeAllMimes(login);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Reload table.
		loadMimeTypes();
	}

	/**
	 * Loads factory defaults.
	 */
	protected void loadFactoryDefaults() {

		// Remove MIME types and load them from file.
		removeAllMimes(false);
		
		// Import factory MIME types.
		MiddleUtility.importFactoryMimeTypes(ProgramBasic.getMiddle(),
				ProgramBasic.getLoginProperties(), this);
		
		ConditionalEvents.transmit(MimeTypesEditor.this, Signal.defaultMimeTypes);
		
    	// Reload table.
    	loadMimeTypes();
	}

	/**
	 * On filter changed.
	 */
	protected void onFilterChanged() {

		// Create row filter.
		RowFilter<MimeTableModel, Integer> filter = new RowFilter<MimeTableModel, Integer>() {
			@Override
			public boolean include(
					javax.swing.RowFilter.Entry<? extends MimeTableModel, ? extends Integer> entry) {
				
				// Get value.
				String text = entry.getValue(0).toString() + " " + entry.getValue(1).toString();
				String searchText = textFilter.getText();
				if (searchText.isEmpty()) {
					searchText = "*";
				}
				
				return Utility.matches(text, searchText, false, false, false);
			}
		};
		// Create sorter.
		TableRowSorter<MimeTableModel> sorter = new TableRowSorter<MimeTableModel>(
				(MimeTableModel) tableMimeTypes.getModel());
		sorter.setRowFilter(filter);
		tableMimeTypes.setRowSorter(sorter);
	}
	
	/**
	 * On delete MIME type.
	 */
	public void onDelete() {
		
		// Get selected indexes.
		int [] selectedIndices = tableMimeTypes.getSelectedRows();
		
		// Inform user.
		if (selectedIndices.length == 0) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("org.multipage.generator.messageSelectMimeType"));
			return;
		}
		
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.generator.messageDeleteSelectedMimeTypes"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		boolean dependeciesExist = false;
		
		// Login to the database.
		result = middle.login(login);
		if (result.isOK()) {
			
			ArrayList<MimeType> mimeTypes = tableModel.getMimeTypes();
			LinkedList<MimeType> mimeTypesToDelete = new LinkedList<MimeType>();
		
			// Remove selected MIME types.
			for (int selectedIndex : selectedIndices) {
				
				// Get type and extension.
				String type = (String) tableMimeTypes.getValueAt(selectedIndex, 0);
				String extension = (String) tableMimeTypes.getValueAt(selectedIndex, 1);
				
				// Start sub transaction.
				result = middle.startSubTransaction();
				if (result.isNotOK()) {
					continue;
				}
				// Delete MIME type.
				result = middle.removeMime(type, extension);
				if (result.isNotOK()) {
					
					// End failed transaction.
					MiddleResult commitResult = middle.endSubTransaction(false);
					if (commitResult.isNotOK()) {
						break;
					}
					if (result == MiddleResult.OK_NOT_ALL_DEPENDENCIES_REMOVED) {
						dependeciesExist = true;
						result = MiddleResult.OK;
					}
					else {
						break;
					}
				}
				// Result is OK.
				else {
					// End transaction successfully.
					result = middle.endSubTransaction(true);
					if (result.isNotOK()) {
						break;
					}
					
					// Add MIME type to delete.
					int mimeIndex = tableMimeTypes.convertRowIndexToModel(selectedIndex);
					MimeType mimeType = mimeTypes.get(mimeIndex);
					mimeTypesToDelete.add(mimeType);
				}
			}
			
			// Logout from the database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
			if (result.isNotOK()) {
				result.show(this);
			}
			
			// Remove MIME types.
			mimeTypes.removeAll(mimeTypesToDelete);
			// Update table.
			tableModel.fireTableDataChanged();
			
			if (dependeciesExist) {
				MiddleResult.OK_NOT_ALL_DEPENDENCIES_REMOVED.show(this);
			}
		}
	}
	
	/**
	 * On new MIME type.
	 */
	public void onNewMime() {
		
		// Get type, extension and preference from user.
		Obj<String> type = new Obj<String>("");
		Obj<String> extension = new Obj<String>("");
		Obj<Boolean> preference = new Obj<Boolean>(true);
		
		if (!MimeEdit.showDialog(this, type, extension,
				preference)) {
			return;
		}
		
		// Check type.
		if (type.ref.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("org.multipage.generator.messageMimeTypeCannotBeEmpty"));
			return;
		}
		
		// Insert MIME type to the database.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		result = middle.insertMime(login, type.ref, extension.ref,
				preference.ref, true);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}

		// Get new record.
		int selectedRow = tableMimeTypes.getSelectedRow();
		int newRecordIndex;
		if (selectedRow == -1) {
			newRecordIndex = tableModel.getMimeTypes().size();
		}
		else {
			newRecordIndex = tableMimeTypes.convertRowIndexToModel(selectedRow);
		}
		
		// Insert new record.
		MimeType newMimeType = new MimeType(0L, type.ref, extension.ref,
				preference.ref);
		tableModel.getMimeTypes().add(newRecordIndex, newMimeType);
		
		tableModel.fireTableRowsInserted(newRecordIndex, newRecordIndex);
		
		// Ensure new record selected an visible.
		tableMimeTypes.getSelectionModel().setSelectionInterval(newRecordIndex, newRecordIndex);
		Utility.ensureRecordVisible(tableMimeTypes, newRecordIndex, false);
	}
	
	/**
	 * On rename MIME type.
	 */
	public void onRename() {
		
		// Get selected indexes.
		int [] selectedIndices = tableMimeTypes.getSelectedRows();
		
		// Inform user.
		if (selectedIndices.length != 1) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("org.multipage.generator.messageSelectOnlyOneMimeType"));
			return;
		}
		
		// Get type, extension and preference.
		int selectedIndex = selectedIndices[0];
		String typeText = (String) tableMimeTypes.getValueAt(selectedIndex, 0);
		String extensionText = (String) tableMimeTypes.getValueAt(selectedIndex, 1);
		boolean preference1 = (Boolean) tableMimeTypes.getValueAt(selectedIndex, 2);
		
		// Set type, extension and preference.
		Obj<String> type = new Obj<String>(typeText.trim());
		Obj<String> extension = new Obj<String>(extensionText.trim());
		Obj<Boolean> preference = new Obj<Boolean>(preference1);
		
		if (!MimeEdit.showDialog(this, type, extension, preference)) {
			return;
		}
		
		// Check type.
		if (type.ref.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("org.multipage.generator.messageMimeTypeCannotBeEmpty"));
			return;
		}
		
		// Get record index.
		int recordIndex = tableMimeTypes.convertRowIndexToModel(selectedIndex);
		
		// Update MIME type in the database and in the model.
		MimeType oldMimeType;
		
		try {
			// Get old MIME type.
			oldMimeType = tableModel.getMimeTypes().get(recordIndex);
		}
		catch (IndexOutOfBoundsException e) {
			Utility.show2(e.getLocalizedMessage());
			return;
		}
		// Get new MIME type.
		MimeType newMimeType = new MimeType(0L, type.ref, extension.ref,
				preference.ref);
		
		MiddleResult result = ProgramBasic.getMiddle().updateMime(
				ProgramBasic.getLoginProperties(), oldMimeType, newMimeType);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update record.
		oldMimeType.copy(newMimeType);
		tableModel.fireTableRowsUpdated(recordIndex, recordIndex);
	}
	
	/**
	 * On update.
	 */
	public void onUpdate() {
		
		loadMimeTypes();
	}
	
	/**
	 * On select all.
	 */
	public void onSelectAll() {
		
		tableMimeTypes.selectAll();
	}
	
	/**
	 * On unselect all.
	 */
	public void onUnselectAll() {
		
		tableMimeTypes.clearSelection();
	}
}

/**
 * 
 * @author
 *
 */
class MimeTableModel extends AbstractTableModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Array of MIME types.
	 */
	private ArrayList<MimeType> mimeTypes;

	/**
	 * Constructor.
	 */
	public MimeTableModel() {

		// Get MIME types.
		mimeTypes = ProgramGenerator.getAreasModel().getMimeTypes();
	}

	/**
	 * Get row count.
	 */
	@Override
	public int getRowCount() {

		return mimeTypes.size();
	}

	/**
	 * Get columns count.
	 */
	@Override
	public int getColumnCount() {

		return 3;
	}

	/**
	 * Get value.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		MimeType mimeType = null;
		
		// Get MIME type.
		try {
			mimeType = mimeTypes.get(rowIndex);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// Return type and extension.
		switch (columnIndex) {
		case -1:
			return mimeType;
		case 0:
			return mimeType.type;
		case 1:
			return mimeType.extension;
		case 2:
			return mimeType.preference;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {

		String columnText = "";
		
		switch (column) {
		case 0:
			columnText = "org.multipage.generator.textMimeTypeColumn";
			break;
		case 1:
			columnText = "org.multipage.generator.textMimeExtension";
			break;
		case 2:
			columnText = "org.multipage.generator.textMimePreference";
			break;
		}
		return Resources.getString(columnText);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		switch (columnIndex) {
		case 0:
		case 1:
			return String.class;
		case 2:
			return Boolean.class;
		default:
			return Object.class;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex,
			int columnIndex) {

		MimeType oldMimeType = null;
		
		// Get old MIME type.
		try {
			oldMimeType = mimeTypes.get(rowIndex);
		}
		catch (IndexOutOfBoundsException e) {
			Utility.show2(e.getLocalizedMessage());
			return;
		}
		
		// Get new MIME type.
		MimeType newMimeType = oldMimeType.clone();
		switch (columnIndex) {
		case 0:
			newMimeType.type = (String) aValue;
			break;
		case 1:
			newMimeType.extension = (String) aValue;
			break;
		case 2:
			newMimeType.preference = (Boolean) aValue;
		}
		
		// Save changes in database.
		MiddleResult result = ProgramBasic.getMiddle().updateMime(
				ProgramBasic.getLoginProperties(), 
				oldMimeType, newMimeType);
		if (result.isNotOK()) {
			result.show(null);
			return;
		}
		
		// Set new MIME type.
		oldMimeType.copy(newMimeType);

        fireTableCellUpdated(rowIndex, columnIndex);
	}

	/**
	 * @return the mimeTypes
	 */
	public ArrayList<MimeType> getMimeTypes() {
		return mimeTypes;
	}
}