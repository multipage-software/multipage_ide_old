/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.AreaResourceRendererBase;
import org.multipage.generator.AreaResourcesEditor;
import org.multipage.generator.EditorTabActions;
import org.multipage.generator.ProgramGenerator;
import org.multipage.generator.TextResourceEditor;
import org.multipage.generator.VersionRenderer;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import org.maclan.Area;
import org.maclan.AreaResource;
import org.maclan.AreaSourceData;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MimeType;
import org.maclan.Resource;
import org.maclan.VersionObj;

/**
 * 
 * @author
 *
 */
public class AreaStartPanel extends JPanel implements EditorTabActions {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Resource container.
	 */
	private Area area;

	/**
	 * Resources editor.
	 */
	private AreaResourcesEditor panelResources;
	
	/**
	 * Resources model.
	 */
	private ResourcesModel resourcesModel;

	/**
	 * Do not save flag.
	 */
	private boolean loadingInformation = false;
	
	/**
	 * Area sources table model.
	 */
	private DefaultTableModel tableModelAreaSources;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelStartResourceLabel;
	private JComboBox comboBoxResources;
	private JButton buttonEdit;
	private JButton buttonRemove;
	private JLabel labelVersion;
	private JComboBox comboBoxVersions;
	private JButton buttonVersions;
	private JCheckBox checkNotLocalized;
	private JToolBar toolBarTable;
	private JScrollPane scrollPaneAreaSources;
	private JTable tableAreaSources;
	private JLabel labelAreaSources;

	/**
	 * Create the panel.
	 */
	public AreaStartPanel() {
		// Initialize components
		initComponents();
		// Post creation.
		// $$hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelStartResourceLabel = new JLabel("builder.textStartResource");
		springLayout.putConstraint(SpringLayout.WEST, labelStartResourceLabel, 10, SpringLayout.WEST, this);
		add(labelStartResourceLabel);
		
		comboBoxResources = new JComboBox();
		springLayout.putConstraint(SpringLayout.SOUTH, labelStartResourceLabel, -6, SpringLayout.NORTH, comboBoxResources);
		springLayout.putConstraint(SpringLayout.WEST, comboBoxResources, 0, SpringLayout.WEST, labelStartResourceLabel);
		springLayout.putConstraint(SpringLayout.EAST, comboBoxResources, -10, SpringLayout.EAST, this);
		comboBoxResources.setPreferredSize(new Dimension(28, 70));
		comboBoxResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onComboAction(e);
			}
		});
		add(comboBoxResources);
		
		buttonEdit = new JButton("org.multipage.generator.textEdit");
		springLayout.putConstraint(SpringLayout.SOUTH, comboBoxResources, -3, SpringLayout.NORTH, buttonEdit);
		springLayout.putConstraint(SpringLayout.WEST, buttonEdit, 0, SpringLayout.WEST, labelStartResourceLabel);
		buttonEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEdit();
			}
		});
		buttonEdit.setPreferredSize(new Dimension(80, 25));
		buttonEdit.setMargin(new Insets(0, 0, 0, 0));
		buttonEdit.setMaximumSize(new Dimension(80, 25));
		buttonEdit.setMinimumSize(new Dimension(80, 25));
		add(buttonEdit);
		
		buttonRemove = new JButton("builder.textRemoveStartResource");
		springLayout.putConstraint(SpringLayout.WEST, buttonRemove, 6, SpringLayout.EAST, buttonEdit);
		buttonRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemove();
			}
		});
		buttonRemove.setMargin(new Insets(0, 0, 0, 0));
		buttonRemove.setPreferredSize(new Dimension(80, 25));
		add(buttonRemove);
		
		labelVersion = new JLabel("builder.textSelectVersion");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonRemove, -3, SpringLayout.NORTH, labelVersion);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonEdit, -3, SpringLayout.NORTH, labelVersion);
		springLayout.putConstraint(SpringLayout.WEST, labelVersion, 0, SpringLayout.WEST, labelStartResourceLabel);
		add(labelVersion);
		
		comboBoxVersions = new JComboBox();
		springLayout.putConstraint(SpringLayout.EAST, comboBoxVersions, -10, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, labelVersion, -3, SpringLayout.NORTH, comboBoxVersions);
		springLayout.putConstraint(SpringLayout.WEST, comboBoxVersions, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, comboBoxVersions, -10, SpringLayout.SOUTH, this);
		comboBoxVersions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onComboAction(e);
			}
		});
		comboBoxVersions.setPreferredSize(new Dimension(280, 60));
		add(comboBoxVersions);
		
		buttonVersions = new JButton("builder.textOpenVersionsEditor");
		springLayout.putConstraint(SpringLayout.EAST, buttonVersions, -10, SpringLayout.EAST, this);
		buttonVersions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onVersionsEditor();
			}
		});
		buttonVersions.setMargin(new Insets(0, 0, 0, 0));
		buttonVersions.setPreferredSize(new Dimension(80, 25));
		add(buttonVersions);
		
		checkNotLocalized = new JCheckBox("builder.textStartResourceNotLocalized");
		springLayout.putConstraint(SpringLayout.WEST, checkNotLocalized, 6, SpringLayout.EAST, buttonRemove);
		springLayout.putConstraint(SpringLayout.SOUTH, checkNotLocalized, -3, SpringLayout.NORTH, labelVersion);
		checkNotLocalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNotLocalizedAction();
			}
		});
		checkNotLocalized.setPreferredSize(new Dimension(211, 25));
		add(checkNotLocalized);
		
		toolBarTable = new JToolBar();
		springLayout.putConstraint(SpringLayout.NORTH, buttonVersions, 0, SpringLayout.NORTH, toolBarTable);
		springLayout.putConstraint(SpringLayout.WEST, toolBarTable, 0, SpringLayout.WEST, labelStartResourceLabel);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBarTable, -22, SpringLayout.NORTH, labelStartResourceLabel);
		springLayout.putConstraint(SpringLayout.EAST, toolBarTable, -300, SpringLayout.EAST, this);
		toolBarTable.setFloatable(false);
		add(toolBarTable);
		
		scrollPaneAreaSources = new JScrollPane();
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneAreaSources, 0, SpringLayout.WEST, labelStartResourceLabel);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneAreaSources, -3, SpringLayout.NORTH, toolBarTable);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneAreaSources, -10, SpringLayout.EAST, this);
		add(scrollPaneAreaSources);
		
		tableAreaSources = new JTable();
		tableAreaSources.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onAreaSourcesClick(e);
			}
		});
		tableAreaSources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneAreaSources.setViewportView(tableAreaSources);
		
		labelAreaSources = new JLabel("builder.textAreaSourcesLabel");
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneAreaSources, 3, SpringLayout.SOUTH, labelAreaSources);
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaSources, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelAreaSources, 0, SpringLayout.WEST, labelStartResourceLabel);
		add(labelAreaSources);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Localize components.
		localize();
		// Initialize components.
		initializeComboBoxes();
		initializeToolBar();
		initializeTable();
		// Set icons.
		setIcons();
	}

	/**
	 * Initialize table.
	 */
	@SuppressWarnings("serial")
	private void initializeTable() {
		
		final Component thisComponent = this;
			
		// Create table model.
		tableModelAreaSources = new DefaultTableModel(new String [] {
				Resources.getString("builder.textResourceColumn"),
				Resources.getString("builder.textVersionColumn"),
				Resources.getString("builder.textNotLocalizedColumn")
		}, 0) {

			// Editable cells.
			@Override
			public boolean isCellEditable(int row, int column) {
				
				return column == 2;
			}

			// Get column class.
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				
				if (columnIndex == 2) {
					return Boolean.class;
				}
				return super.getColumnClass(columnIndex);
			}

			// Set cell value.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				
				// Save "not localized flag" in database table.
				if (column == 2 && aValue instanceof Boolean && row >= 0 && row < getRowCount()) {
					
					boolean notLocalized = (Boolean) aValue;
					AreaResource areaResource = null;
					VersionObj version = null;
					
					Object columnObject = getValueAt(row, 0);
					if (columnObject instanceof AreaResource) {
						areaResource = (AreaResource) columnObject;
					}
					columnObject = getValueAt(row, 1);
					if (columnObject instanceof VersionObj) {
						version = (VersionObj) columnObject;
					}
					
					if (areaResource == null || version == null) {
						Utility.show(thisComponent, "builder.messageUnknownTableValueFound");
					}
					else {
						// Update database.
						updateDatabaseAreaSourceNotLocalized(areaResource.getId(), version.getId(), notLocalized);
					}
				}
				
				super.setValueAt(aValue, row, column);
			}
		};
		
		tableAreaSources.setModel(tableModelAreaSources);
		
		// Set row height.
		tableAreaSources.setRowHeight(28);
		
		// Set columns.
		TableColumnModel columnModel = tableAreaSources.getColumnModel();
		columnModel.getColumn(2).setMaxWidth(100);
		
		// Set cell renderer.
		columnModel.getColumn(0).setCellRenderer(new TableCellRenderer() {
			
			final RendererJLabel renderer = new RendererJLabel();
			{
				// Set renderer left gap.
				renderer.setBorder(new EmptyBorder(0, 10, 0, 0));
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				if (value instanceof AreaResource) {
					 AreaResource areaResource = (AreaResource) value;
					 MimeType mimeType = panelResources.getMime(areaResource.getMimeTypeId());
					 
					 renderer.setText(String.format("<html><b>%s</b>&nbsp;&nbsp;&nbsp;(<i>%s</i>)&nbsp;&nbsp;&nbsp;id:%d</html>", areaResource.getDescription(), mimeType.type, areaResource.getId()));
				}
				else {
					renderer.setText("");
				}
				
				renderer.set(isSelected, hasFocus, row);
		        return renderer;
			}
		});
		
		// Set cell renderer.
		columnModel.getColumn(1).setCellRenderer(new TableCellRenderer() {
			
			final RendererJLabel renderer = new RendererJLabel();
			{
				renderer.setIcon(Images.getIcon("org/multipage/generator/images/version_icon.png"));
				renderer.setBorder(new EmptyBorder(0, 10, 0, 0));
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				renderer.setText(value.toString());
				renderer.set(isSelected, hasFocus, row);
		        return renderer;
			}
		});
	}

	/**
	 * Update not localized flag in the database.
	 * @param resourceId
	 * @param versionId
	 * @param notLocalized
	 */
	protected void updateDatabaseAreaSourceNotLocalized(long resourceId, long versionId,
			boolean notLocalized) {
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.updateAreaSourceNotLocalized(login, area.getId(), resourceId, versionId, notLocalized);
		if (result.isNotOK()) {
			result.show(this);
		}
	}

	/**
	 * Load area sources table.
	 */
	private void loadAreaSourcesTable() {
		
		// Clear table.
		tableModelAreaSources.getDataVector().removeAllElements();
		
		// Load area sources from the database.
		LinkedList<AreaSourceData> areaSourcesData = new LinkedList<AreaSourceData>();
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.loadAreaSources(login, area.getId(), areaSourcesData);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Insert table items.
		for (AreaSourceData areaSourceData : areaSourcesData) {
			
			insertAreaSourceTableItem(areaSourceData.resourceId, areaSourceData.versionId, areaSourceData.notLocalized);
		}
	}

	/**
	 * On new area source.
	 */
	@SuppressWarnings("unused")
	private void onAddAreaSource() {
		
		// Get selected items.
		Obj<AreaResource> areaResource = new Obj<AreaResource>();
		Obj<VersionObj> version = new Obj<VersionObj>();
		Obj<Boolean> notLocalized = new Obj<Boolean>();
		
		if (!AreaSourceDialog.insertDialog(this, panelResources, areaResource, version, notLocalized)) {
			return;
		}
		
		// Add new item.
		addAreaSource(areaResource.ref, version.ref, notLocalized.ref);
	}
	
	/**
	 * On edit area source.
	 */
	private void onEditAreaSource() {
		
		// Output objects.
		Obj<AreaResource> areaResource = new Obj<AreaResource>();
		Obj<VersionObj> version = new Obj<VersionObj>();
		Obj<Boolean> notLocalized = new Obj<Boolean>();
		
		// Get selected table item.
		if (!getSelectedAreaSource(areaResource, version, notLocalized)) {
			return;
		}
		
		if (!AreaSourceDialog.editDialog(this, panelResources, areaResource, version, notLocalized)) {
			return;
		}
		
		// Edit selected item.
		editSelectedAreaSource(areaResource.ref, version.ref, notLocalized.ref);
	}
	
	/**
	 * On update area sources.
	 */
	protected void onUpdateAreaSources() {
		
		loadAreaSourcesTable();
	}
	
	/**
	 * On area source table click.
	 * @param event 
	 */
	protected void onAreaSourcesClick(MouseEvent event) {
		
		if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			onEditAreaSource();
		}
	}

	/**
	 * Get selected area source.
	 * @param areaResource
	 * @param version
	 * @param notLocalized
	 */
	private boolean getSelectedAreaSource(Obj<AreaResource> areaResource,
			Obj<VersionObj> version, Obj<Boolean> notLocalized) {
		
		// Get selected row.
		int selectedRow = tableAreaSources.getSelectedRow();
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectSingleAreaSource");
			return false;
		}
		
		areaResource.ref = (AreaResource) tableModelAreaSources.getValueAt(selectedRow, 0);
		version.ref = (VersionObj) tableModelAreaSources.getValueAt(selectedRow, 1);
		notLocalized.ref = (Boolean) tableModelAreaSources.getValueAt(selectedRow, 2);
		
		return true;
	}

	/**
	 * Put area source into table.
	 * @param resourceId
	 * @param versionId
	 * @param notLocalized
	 */
	private void insertAreaSourceTableItem(long resourceId, long versionId,
			boolean notLocalized) {
		
		// Get objects' references.
		AreaResource areaResource = getAreaResourceFromId(resourceId);
		if (areaResource == null) {
			Utility.show(this, "builder.messageAreaResourceNotLoaded");
			return;
		}
		
		VersionObj version = ProgramGenerator.getAreasModel().getVersion(versionId);
		if (version == null) {
			Utility.show(this, "builder.messageAreaVersionNotLoaded");
			return;
		}
		
		// Add new table row.
		tableModelAreaSources.addRow(new Object [] { areaResource, version, notLocalized});
	}

	/**
	 * Get area resource.
	 * @return
	 */
	private AreaResource getAreaResourceFromId(long resourceId) {
		
		int count = comboBoxResources.getItemCount();
		
		for (int index = 0; index < count; index++) {
			Object item = comboBoxResources.getItemAt(index);
			if (item instanceof AreaResource) {
				
				AreaResource areaResource = (AreaResource) item;
				if (areaResource.getId() == resourceId) {
					return areaResource;
				}
			}
		}
		return null;
	}

	/**
	 * Add area source.
	 * @param areaResource
	 * @param version
	 * @param notLocalized
	 */
	private void addAreaSource(AreaResource areaResource, VersionObj version, boolean notLocalized) {
		
		// Try to insert new item into the database.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.insertAreaSource(login, area.getId(), areaResource.getId(), version.getId(), notLocalized);
		if (result.isOK()) {
			
			// Add new table row.
			tableModelAreaSources.addRow(new Object [] { areaResource, version, notLocalized});
		}
		else if (result == MiddleResult.ELEMENT_ALREADY_EXISTS) {
			Utility.show(this, "builder.messageAreaSourceAlreadyExists");
		}
		else {
			result.show(this);
		}
	}

	/**
	 * Edit selected area source.
	 * @param areaResource
	 * @param version
	 * @param notLocalized
	 */
	private void editSelectedAreaSource(AreaResource areaResource, VersionObj version,
			boolean notLocalized) {
		
		// Get selected row.
		int selectedRow = tableAreaSources.getSelectedRow();
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectSingleAreaSource");
			return;
		}
		
		// Try to edit item in the database.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		long areaId = area.getId();
		
		// Get old values.
		AreaResource oldAreaResource = (AreaResource) tableModelAreaSources.getValueAt(selectedRow, 0);
		VersionObj oldVersion = (VersionObj) tableModelAreaSources.getValueAt(selectedRow, 1);
		
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			// We must delete old item and insert new item because a table key is removed.
			result = middle.deleteAreaSource(areaId, oldAreaResource.getId(), oldVersion.getId());
			
			if (result.isOK()) {
				result = middle.insertAreaSource(areaId, areaResource.getId(), version.getId(), notLocalized);
			}
			
			// Logout from database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				
				result = logoutResult;
			}
		}
		
		// Update table item.
		if (result.isOK()) {
			
			tableModelAreaSources.setValueAt(areaResource, selectedRow, 0);
			tableModelAreaSources.setValueAt(version, selectedRow, 1);
			tableModelAreaSources.setValueAt(notLocalized, selectedRow, 2);
		}
		else {
			result.show(this);
		}
	}

	/**
	 * On remove area source.
	 */
	@SuppressWarnings("unused")
	private void onRemoveAreaSource() {
		
		// Get selected table item.
		int selectedRow = tableAreaSources.getSelectedRow();
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectSingleAreaSource");
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "builder.messageDeleteAreaSource")) {
			return;
		}
		
		// Get objects.
		AreaResource areaResource = null;
		VersionObj version = null;
		
		Object columnObject = tableModelAreaSources.getValueAt(selectedRow, 0);
		if (columnObject instanceof AreaResource) {
			areaResource = (AreaResource) columnObject;
		}
		
		columnObject = tableModelAreaSources.getValueAt(selectedRow, 1);
		if (columnObject instanceof VersionObj) {
			version = (VersionObj) columnObject;
		}
		
		// On error inform user.
		if (areaResource == null || version == null) {
			Utility.show(this, "builder.messageUnknownTableValueFound");
			return;
		}
		
		// Try to delete selected item from the database.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.deleteAreaSource(login, area.getId(), areaResource.getId(), version.getId());
		if (!result.isOK()) {
			result.show(this);
			return;
		}
		
		// Remove table row.
		tableModelAreaSources.removeRow(selectedRow);
	}
	
	/**
	 * On edit source text.
	 */
	@SuppressWarnings("unused")
	private void onEditResourceText() {
		
		// Get selected table row.
		// Get selected table item.
		int selectedRow = tableAreaSources.getSelectedRow();
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectSingleAreaSource");
			return;
		}
		
		// Get area resource object.
		Object columnObject = tableModelAreaSources.getValueAt(selectedRow, 0);
		if (!(columnObject instanceof AreaResource)) {
			Utility.show(this, "builder.messageUnknownTableValueFound");
			return;
		}
		AreaResource areaResource = (AreaResource) columnObject;
		
		TextResourceEditor.showDialog(this,
				areaResource.getId(), areaResource.isSavedAsText(), true);
	}
	
	/**
	 * Initialize tool bar.
	 */
	private void initializeToolBar() {
		
		ToolBarKit.addToolBarButton(toolBarTable, "org/multipage/generator/images/add_item_icon.png", this, "onAddAreaSource", "builder.tooltipAddAreaSource");
		ToolBarKit.addToolBarButton(toolBarTable, "org/multipage/generator/images/edit.png", this, "onEditAreaSource", "builder.tooltipEditAreaSource");
		ToolBarKit.addToolBarButton(toolBarTable, "org/multipage/generator/images/edit_resource.png", this, "onEditResourceText", "builder.tooltipEditResourceText");
		ToolBarKit.addToolBarButton(toolBarTable, "org/multipage/generator/images/remove_icon.png", this, "onRemoveAreaSource", "builder.tooltipRemoveAreaSource");
		ToolBarKit.addToolBarButton(toolBarTable, "org/multipage/generator/images/update_icon.png", this, "onUpdateAreaSources", "builder.tooltipUpdateAreaSources");

	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		buttonEdit.setIcon(Images.getIcon("org/multipage/generator/images/edit_text.png"));
		buttonRemove.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
		buttonVersions.setIcon(Images.getIcon("org/multipage/generator/images/version_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelStartResourceLabel);
		Utility.localize(buttonEdit);
		Utility.localize(buttonRemove);
		Utility.localize(labelVersion);
		Utility.localize(buttonVersions);
		Utility.localize(checkNotLocalized);
		Utility.localize(labelAreaSources);
	}

	/**
	 * Set area reference.
	 * @param area
	 * @param panelResources 
	 */
	public void setReferences(Area area,
			AreaResourcesEditor panelResources) {
		
		this.area = area;
		this.panelResources = panelResources;
		
		// Load resources panel.
		panelResources.onLoadPanelInformation();
		
		// Set model.
		resourcesModel = new ResourcesModel(panelResources);
		comboBoxResources.setModel(resourcesModel);
	}

	/**
	 * Initialize combobox.
	 */
	private void initializeComboBoxes() {

		// Set renderer.
		comboBoxResources.setRenderer(new ListCellRenderer() {
			// Renderer object.
			private AreaResourceRendererBase renderer = ProgramGenerator.newAreaResourceRenderer();
			private UnknownRenderer unknown = new UnknownRenderer();
			// Return renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {

				// Check the object.
				if (!(value instanceof AreaResource)) {
					return unknown;
				}
				// Get resource.
				AreaResource resource = (AreaResource) value;
				MimeType mimeType = panelResources.getMime(resource.getMimeTypeId());
				String namespace = panelResources.getNamespacePath(resource.getParentNamespaceId());
				// Set renderer.
				renderer.setProperties(resource, mimeType.type, namespace, index,
						isSelected, cellHasFocus);
				return renderer;
			}
		});
		
		// Create and set renderer.
		comboBoxVersions.setRenderer(new ListCellRenderer<VersionObj>() {

			// Label object.
			VersionRenderer renderer = new VersionRenderer();

			// Renderer method.
			@Override
			public Component getListCellRendererComponent(
					JList<? extends VersionObj> list, VersionObj value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				// Propagate enable state.
				renderer.setEnabledComponents(comboBoxVersions.isEnabled());
				
				if (value == null) {
					renderer.reset();
				}
				else {
					renderer.set(value, index, isSelected, cellHasFocus);
				}
				return renderer;
			}
		});
	}

	/**
	 * Load start resource.
	 */
	private void loadPanelInformation() {

		if (area == null) {
			return;
		}
		
		// Reload area object.
		area = ProgramGenerator.getArea(area.getId());
		
		// Do not safe while loading.
		loadingInformation = true;
		
		// Update model.
		if (resourcesModel != null) {
			// Set flag.
			resourcesModel.update();
		}
		
		// Load versions.
		loadVersions();
		
		// Prepare prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		Obj<Long> resourceId = new Obj<Long>(0L);
		Obj<Long> versionId = new Obj<Long>(0L);
		Obj<Boolean> startResourceNotLocalized = new Obj<Boolean>(false);
		MiddleResult result;
		
		// Load start resource.
		result = middle.loadContainerStartResource(login, area, resourceId, versionId, startResourceNotLocalized);
		if (result.isNotOK()) {
			result.show(this);
			
			// Enable saving.
			loadingInformation = false;
			return;
		}
		
		// Select resource in the combo box.
		comboBoxResources.setSelectedItem(null);
		enableVersions(false);
		disableNotLocalized();
		
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxResources.getModel();
		for (int index = 0; index < model.getSize(); index++) {
			Object item = model.getElementAt(index);
			if (item instanceof AreaResource) {
				
				AreaResource resource = (AreaResource) item;
				if (resource.getId() == resourceId.ref) {

					// Select start resource.
					comboBoxResources.setSelectedItem(resource);
					
					// Enable versions combo box.
					enableVersions(true);
					// Select version.
					selectVersion(versionId.ref);

					// Set "start resource not localized" flag.
					checkNotLocalized.setSelected(startResourceNotLocalized.ref);
					checkNotLocalized.setEnabled(true);

					break;
				}
			}
		}
		
		// Load area sources table.
		loadAreaSourcesTable();
		
		// Enable saving.
		loadingInformation = false;
	}

	/**
	 * Enable "start resource not localized" flag.
	 */
	private void disableNotLocalized() {
		
		checkNotLocalized.setEnabled(false);
		checkNotLocalized.setSelected(false);
	}

	/**
	 * Select version.
	 * @param versionId
	 */
	private void selectVersion(long versionId) {
		
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxVersions.getModel();
		
		for (int index = 0; index < model.getSize(); index++) {
			VersionObj version = (VersionObj) model.getElementAt(index);
			
			if (version.getId() == versionId) {
				comboBoxVersions.setSelectedItem(version);
				return;
			}
		}
	}

	/**
	 * Enable versions.
	 * @param enable
	 */
	private void enableVersions(boolean enable) {
		
		labelVersion.setEnabled(enable);
		comboBoxVersions.setEnabled(enable);
	}

	/**
	 * Combo box action.
	 */
	protected void onComboAction(ActionEvent e) {

		if (e.getModifiers() == 0) {
			return;
		}
		
		// If the dialog is loading information, do not execute this action.
		if (loadingInformation) {
			return;
		}

		// Save information.
		savePanelInformation();

		// Enable versions.
		enableVersions(true);
		
		// Enable "start resource not localized" flag.
		checkNotLocalized.setEnabled(true);
		
		// If it is a resources combo box action, reset flag.
		if (e.getSource() == comboBoxResources) {
			checkNotLocalized.setSelected(false);
		}
	}
	
	/**
	 * Save information.
	 */
	private void savePanelInformation() {
			
		if (loadingInformation) {
			return;
		}
		
		// Get selected resource.
		Resource resource = (Resource) comboBoxResources.getSelectedItem();
		VersionObj version = (VersionObj) comboBoxVersions.getSelectedItem();
		
		if (resource == null || version == null) {
			return;
		}
		
		// Get "start resource not localized" flag.
		boolean startResourceNotLocalized = checkNotLocalized.isSelected();
		
		// Update data.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		result = middle.updateStartResource(login, area, resource, version,
				startResourceNotLocalized);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update diagram.
		updateInformation();
	}

	/**
	 * On "start resource not localized" check box action.
	 */
	protected void onNotLocalizedAction() {
		
		// If the dialog is loading information, do not execute this action.
		if (loadingInformation) {
			return;
		}
		
		// Save information.
		savePanelInformation();
	}

	/**
	 * On edit resource.
	 */
	protected void onEdit() {
		
		// Get selected resource.
		Object selected = comboBoxResources.getSelectedItem();
		if (!(selected instanceof Resource)) {
			Utility.show(this, "builder.messageSelectStartResource");
			return;
		}
		
		Resource resource = (Resource) selected;
		
		TextResourceEditor.showDialog(this,
				resource.getId(), resource.isSavedAsText(), true);
	}

	/**
	 * Remove start resource.
	 */
	protected void onRemove() {

		// Reset start resource.
		MiddleResult result = ProgramBasic.getMiddle().resetStartResource(
				ProgramBasic.getLoginProperties(), area);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		// Reset combo boxes.
		comboBoxResources.setSelectedIndex(-1);
		selectVersion(0L);
		
		// Disable versions.
		enableVersions(false);
		// Disable "start resource not localized" check box.
		disableNotLocalized();
		
		// Update diagram.
		updateInformation();
	}

	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * On versions editor.
	 */
	protected void onVersionsEditor() {
		
		VersionsEditor.showDialog(this);
		
		loadVersions();
	}

	/**
	 * Load versions.
	 */
	private void loadVersions() {
		
		comboBoxVersions.removeAllItems();
		
		// Prepare prerequisites.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Load versions.
		LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
		MiddleResult result = middle.loadVersions(login, 0L, versions);
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Load versions.
		for (VersionObj version : versions) {
			comboBoxVersions.addItem(version);
		}
		
		// Select default version.
		selectVersion(0L);
	}

	/**
	 * On load panel information.
	 */
	public void onLoadPanelInformation() {

		// Update information.
		loadPanelInformation();
	}

	/**
	 * On save panel information.
	 */
	public void onSavePanelInformation() {
		
		// Save start resource.
		savePanelInformation();
	}
}

/**
 * 
 * @author
 *
 */
class ResourcesModel extends DefaultComboBoxModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Resources panel.
	 */
	private AreaResourcesEditor panelResources;
	
	/**
	 * Constructor.
	 * @param panelResources
	 */
	public ResourcesModel(AreaResourcesEditor panelResources) {

		this.panelResources = panelResources;
	}
	
	/**
	 * Get list size.
	 */
	@Override
	public int getSize() {

		return panelResources.getResourcesCount();
	}
	
	/**
	 * Get list element.
	 */
	@Override
	public Object getElementAt(int index) {

		return panelResources.getResourceFromIndex(index);
	}
	
	/**
	 * Update.
	 */
	public void update() {
		
		fireContentsChanged(this, 0, panelResources.getResourcesCount() - 1);
	}
}
