/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.multipage.basic.*;
import org.multipage.generator.*;
import org.maclan.*;


/**
 * @author
 *
 */
public class AreaDependenciesPanelBuilder extends AreaDependenciesPanelBase {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JLabel labelAreaDependencies;
	private JScrollPane scrollPane;
	private JTable tableAreas;
	private JButton buttonUp;
	private JButton buttonDown;
	private JButton buttonDefault;
	private JRadioButton buttonSubAreas;
	private JRadioButton buttonSuperAreas;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton buttonSetRelationNames;
	private JPopupMenu popupMenu;
	private RelatedAreaPanel panelRelatedArea;

	/**
	 * Create the panel.
	 */
	public AreaDependenciesPanelBuilder() {

		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		setComponentsReferences(
				labelAreaDependencies,
				tableAreas,
				buttonUp,
				buttonDown,
				buttonDefault,
				buttonSubAreas,
				buttonSuperAreas,
				popupMenu,
				panelRelatedArea
				);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelAreaDependencies = new JLabel("org.multipage.generator.textAreaDependencies");
		springLayout.putConstraint(SpringLayout.WEST, labelAreaDependencies, 10, SpringLayout.WEST, this);
		add(labelAreaDependencies);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 7, SpringLayout.SOUTH, labelAreaDependencies);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, this);
		add(scrollPane);
		
		tableAreas = new JTable();
		tableAreas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onTableClick(e);
			}
		});
		tableAreas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(tableAreas);
		
		popupMenu = new JPopupMenu();
		addPopup(tableAreas, popupMenu);
		
		buttonUp = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonUp, 0, SpringLayout.NORTH, scrollPane);
		buttonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUp();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -6, SpringLayout.WEST, buttonUp);
		buttonUp.setPreferredSize(new Dimension(30, 30));
		springLayout.putConstraint(SpringLayout.EAST, buttonUp, -10, SpringLayout.EAST, this);
		add(buttonUp);
		
		buttonDown = new JButton("");
		buttonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDown();
			}
		});
		buttonDown.setPreferredSize(new Dimension(30, 30));
		springLayout.putConstraint(SpringLayout.NORTH, buttonDown, 6, SpringLayout.SOUTH, buttonUp);
		springLayout.putConstraint(SpringLayout.WEST, buttonDown, 0, SpringLayout.WEST, buttonUp);
		add(buttonDown);
		
		buttonDefault = new JButton("");
		buttonDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onReset();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonDefault, 6, SpringLayout.SOUTH, buttonDown);
		springLayout.putConstraint(SpringLayout.WEST, buttonDefault, 6, SpringLayout.EAST, scrollPane);
		buttonDefault.setPreferredSize(new Dimension(30, 30));
		add(buttonDefault);
		
		buttonSubAreas = new JRadioButton("org.multipage.generator.textSubAreasOrder");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaDependencies, 10, SpringLayout.SOUTH, buttonSubAreas);
		buttonSubAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAreaChange();
			}
		});
		buttonSubAreas.setSelected(true);
		buttonGroup.add(buttonSubAreas);
		springLayout.putConstraint(SpringLayout.WEST, buttonSubAreas, 0, SpringLayout.WEST, labelAreaDependencies);
		add(buttonSubAreas);
		
		buttonSuperAreas = new JRadioButton("org.multipage.generator.textSuperAreasOrder");
		buttonSuperAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAreaChange();
			}
		});
		buttonGroup.add(buttonSuperAreas);
		springLayout.putConstraint(SpringLayout.WEST, buttonSuperAreas, 6, SpringLayout.EAST, buttonSubAreas);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSuperAreas, 0, SpringLayout.SOUTH, buttonSubAreas);
		add(buttonSuperAreas);
		
		buttonSetRelationNames = new JButton("");
		buttonSetRelationNames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRelationNames();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonSetRelationNames, 6, SpringLayout.SOUTH, buttonDefault);
		springLayout.putConstraint(SpringLayout.EAST, buttonSetRelationNames, 0, SpringLayout.EAST, buttonUp);
		buttonSetRelationNames.setPreferredSize(new Dimension(30, 30));
		add(buttonSetRelationNames);
		
		panelRelatedArea = new RelatedAreaPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panelRelatedArea, 20, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, panelRelatedArea, 45, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSubAreas, 20, SpringLayout.SOUTH, panelRelatedArea);
		springLayout.putConstraint(SpringLayout.WEST, panelRelatedArea, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, panelRelatedArea, -10, SpringLayout.EAST, this);
		add(panelRelatedArea);
	}

	/**
	 * Set relation names.
	 */
	private void setRelationNames() {
		
		// Get relation name.
		String relationName = JOptionPane.showInputDialog(
				Resources.getString("builder.messageGetRelationsName"));
		if (relationName == null) {
			return;
		}
		
		boolean useSubAreas = buttonSubAreas.isSelected();
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		result = middle.login(login);
		if (result.isOK()) {
			
			long currentAreaId = currentArea.getId();
			// Set relation names.
			if (useSubAreas) {
				for (Area subArea : currentArea.getSubareas()) {
					long subAreaId = subArea.getId();
					currentArea.setSubRelationNameLight(subAreaId, relationName);
					
					result = middle.updateIsSubareaNameSub(currentAreaId, subAreaId, relationName);
					if (result.isNotOK()) {
						break;
					}
				}
			}
			else {
				for (Area superArea : currentArea.getSuperareas()) {
					long superAreaId = superArea.getId();
					currentArea.setSuperRelationNameLight(superAreaId, relationName);
					
					result = middle.updateIsSubareaNameSuper(superAreaId, currentAreaId, relationName);
					if (result.isNotOK()) {
						break;
					}
				}				
			}

			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
			
			// Update information.
			updateInformation();
			
			// Reload areas.
			loadAreas();
		}
		
		// Report error.
		if (result.isNotOK()) {
			result.show(this);
		}
	}
	
	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set icons.
	 */
	@Override
	protected void setIcons() {
		
		super.setIcons();
		
		buttonSetRelationNames.setIcon(Images.getIcon("org/multipage/generator/images/edit.png"));
	}
	
	/**
	 * Set tool tips.
	 */
	@Override
	protected void setToolTips() {
		
		super.setToolTips();
		
		buttonSetRelationNames.setToolTipText(Resources.getString("builder.tooltipSetRelationNames"));
	}
	

	/**
	 * Load areas.
	 * @param currentArea
	 */
	@SuppressWarnings("serial")
	@Override
	protected void loadAreas() {
		
		// Reload area object.
		currentArea = ProgramGenerator.getArea(currentArea.getId());
				
		
		final boolean useSubAreas = buttonSubAreas.isSelected();

		LinkedList<Long> areasIds = new LinkedList<Long>();
		
		final Middle middle = ProgramBasic.getMiddle();
		final Properties login = ProgramBasic.getLoginProperties();
		
		// Login to the database.
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			if (useSubAreas) {
				// Load subareas IDs.
				result = middle.loadAreaSubAreas(currentArea, areasIds);
				if (result.isOK()) {
					// Initialize currentArea subareas priorities.
					result = middle.initAreaSubareasPriorities(currentArea.getId(),
							areasIds);
				}
			}
			else {
				// Load superareas IDs.
				result = middle.loadAreaSuperAreas(currentArea, areasIds);
				if (result.isOK()) {
					// Initialize currentArea superarea priorities.
					result = middle.initAreaSuperareasPriorities(currentArea.getId(),
							areasIds);
				}
			}
				
			// Logout from the database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		// On error exit.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		AreasModel model = ProgramGenerator.getAreasModel();
		
		// Get areas.
		final ArrayList<Area> areas = new ArrayList<Area>();
		
		int areasCount = areasIds.size();
		for (int index = 0; index < areasCount; index++) {
			// Get area.
			Area area = model.getArea(areasIds.get(index));
			areas.add(area);
		}
		
		// Table model.
		class LocalTableModel extends AbstractTableModel {
			// Get row count.
			@Override
			public int getRowCount() {
				// Return row count.
				return areas.size();
			}
			// Get column count.
			@Override
			public int getColumnCount() {
				// Return column count.
				return 3;
			}
			// Get value.
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				// Get area.
				Area area = areas.get(rowIndex);
				// Return value.
				if (columnIndex == 0) {
					return area;
				}
				else if (columnIndex == 1) {
					if (useSubAreas) {
						return currentArea.getSubRelationName(area.getId());
					}
					else {
						return currentArea.getSuperRelationName(area.getId());
					}
				}
				else if (columnIndex == 2) {
					if (useSubAreas) {
						return currentArea.isHideSubUseSub(area.getId());
					}
					else {
						return currentArea.isHideSubUseSuper(area.getId());
					}
				}
				return null;
			}
			// Get column name.
			
			@Override
			public String getColumnName(int column) {
				// Return value.
				switch (column) {
				case 0:
					return Resources.getString("org.multipage.generator.textAreaDescription");
				case 1:
					return Resources.getString(useSubAreas ? "org.multipage.generator.textRelationNameSub" :
						"org.multipage.generator.textRelationNameSuper");
				case 2:
					return Resources.getString("builder.textRelationHideSub");
				}
				return "";
			}
			// Get column class.
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 1) {
					return String.class;
				}
				else if (columnIndex == 2) {
					return Boolean.class;
				}
				return super.getColumnClass(columnIndex);
			}
			// Get cell editable.
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				
				return columnIndex == 1 || columnIndex == 2;
			}
			// Set value.
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				
				if (columnIndex == 1 && aValue instanceof String) {
					
					// Update relation name.
					String relationName = (String) aValue;
					Area relatedArea = (Area) getValueAt(rowIndex, 0);
					long currentAreaId = currentArea.getId();
					long relatedAreaId = relatedArea.getId();
					
					MiddleResult result;
					Properties login = ProgramBasic.getLoginProperties();
					
					if (useSubAreas) {
						result = middle.updateIsSubareaNameSub(login, currentAreaId,
								relatedAreaId, relationName);
						if (result.isOK()) {
							currentArea.setSubRelationNameLight(relatedAreaId, relationName);
							updateInformation();
						}
					}
					else {
						result = middle.updateIsSubareaNameSuper(login, relatedAreaId,
								currentAreaId, relationName);
						if (result.isOK()) {
							currentArea.setSuperRelationNameLight(relatedAreaId, relationName);
							updateInformation();
						}
					}
					
					if (result.isNotOK()) {
						// Report error.
						result.show(tableAreas);
					}
				}
				else if (columnIndex == 2 && aValue instanceof Boolean) {
					
					// Update relation "hide sub areas flag".
					Boolean hideSub = (Boolean) aValue;
					Area relatedArea = (Area) getValueAt(rowIndex, 0);
					long currentAreaId = currentArea.getId();
					long relatedAreaId = relatedArea.getId();
					
					MiddleResult result;
					Properties login = ProgramBasic.getLoginProperties();
					
					// Update database.
					if (useSubAreas) {
						
						result = middle.updateIsSubareaHideSub(login, currentAreaId,
									relatedAreaId, hideSub);
						
						if (result.isOK()) {
							currentArea.setHideSubUseSub(relatedAreaId, hideSub);
							updateInformation();
						}
					}
					else {
						
						result = middle.updateIsSubareaHideSub(login, relatedAreaId,
									currentAreaId, hideSub);
						
						if (result.isOK()) {
							currentArea.setHideSubUseSuper(relatedAreaId, hideSub);
							updateInformation();
						}						
					}
					
					if (result.isNotOK()) {
						// Report error.
						result.show(tableAreas);
					}
				}
			}
		}
		
		// Get possible old selection.
		Long oldSelectedAreaId = null;
		int oldSelectedRow = tableAreas.getSelectedRow();
		if (oldSelectedRow != -1) {
			
			Area oldSelectedArea = (Area) tableAreas.getModel()
				.getValueAt(oldSelectedRow, 0);
			// Set old selected currentArea ID.
			oldSelectedAreaId = oldSelectedArea.getId();
		}
		
		// Create table model.
		LocalTableModel tableModel = new LocalTableModel();
		
		// Set table model.
		tableAreas.setModel(tableModel);
		
		if (oldSelectedAreaId != null) {
			// Select currentArea.
			for (int index = 0; index < tableModel.getRowCount(); index++) {
				// Get currentArea.
				Area area = (Area) tableModel.getValueAt(index, 0);
				if (area.getId() == oldSelectedAreaId) {
					// Select the row.
					ListSelectionModel selectionModel = tableAreas.getSelectionModel();
					if (selectionModel != null) {
						selectionModel.setSelectionInterval(index, index);
					}
					break;
				}
			}
		}
		
		// Set area name column cell renderer.
		tableAreas.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				
				if (value instanceof Area) {
					super.setValue(((Area) value).getDescriptionForDiagram());
					return;
				}
				super.setValue(value);
			}
		});
	}
}
