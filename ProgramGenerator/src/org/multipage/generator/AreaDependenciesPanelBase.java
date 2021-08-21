/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import org.maclan.Area;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class AreaDependenciesPanelBase extends JPanel implements EditorTabActions {
	public AreaDependenciesPanelBase() {
	}

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Panel state.
	 */
	public static boolean selectedSubAreas = true;
	
	/**
	 * Area reference.
	 */
	protected Area currentArea;
	
	/**
	 * Components' references.
	 */
	private JLabel labelAreaDependencies;
	private JTable tableAreas;
	private JButton buttonUp;
	private JButton buttonDown;
	private JButton buttonDefault;
	private JRadioButton buttonSubAreas;
	private JRadioButton buttonSuperAreas;
	private JPopupMenu popupMenu;
	private RelatedAreaPanel panelRelatedArea;

	/**
	 * Set components' references.
	 * @param labelAreaDependencies
	 * @param tableAreas
	 * @param buttonUp
	 * @param buttonDown
	 * @param buttonDefault
	 * @param buttonSubAreas
	 * @param buttonSuperAreas
	 * @param labelRelatedArea 
	 * @param buttonClearRelatedArea 
	 * @param buttonSelectRelatedArea 
	 * @param buttonUpdateRelatedArea 
	 * @param textRelatedArea 
	 * @param buttonSetRelationNames
	 */
	protected void setComponentsReferences(
			JLabel labelAreaDependencies,
			JTable tableAreas,
			JButton buttonUp,
			JButton buttonDown,
			JButton buttonDefault,
			JRadioButton buttonSubAreas,
			JRadioButton buttonSuperAreas,
			JPopupMenu popupMenu,
			RelatedAreaPanel panelRelatedArea
			) {
		
		 this.labelAreaDependencies = labelAreaDependencies;
		 this.tableAreas = tableAreas;
		 this.buttonUp = buttonUp;
		 this.buttonDown = buttonDown;
		 this.buttonDefault = buttonDefault;
		 this.buttonSubAreas = buttonSubAreas;
		 this.buttonSuperAreas = buttonSuperAreas;
		 this.popupMenu = popupMenu;
		 this.panelRelatedArea = panelRelatedArea;
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {

		// Set table property.
		tableAreas.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Set tool tips.
		setToolTips();
		// Select button.
		buttonSubAreas.setSelected(selectedSubAreas);
		buttonSuperAreas.setSelected(!selectedSubAreas);
		// Create popup trayMenu.
		createPopupMenu();
	}

	/**
	 * Create popup trayMenu.
	 */
	private void createPopupMenu() {
		
		final Component thisComponent = this;
		
		AreaLocalMenu menu = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				return getSelectedArea();
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		menu.addTo(this, popupMenu);
	}

	/**
	 * Get selected area.
	 * @return
	 */
	protected Area getSelectedArea() {
		
		int row = tableAreas.getSelectedRow();
		if (row == -1) {
			Utility.show(this, "org.multipage.generator.messageSelectArea");
			return null;
		}
		
		return (Area) tableAreas.getValueAt(row, 0);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelAreaDependencies);
		Utility.localize(buttonSubAreas);
		Utility.localize(buttonSuperAreas);
	}
	
	/**
	 * Set currentArea.
	 * @param currentArea
	 */
	public void setArea(Area area) {

		this.currentArea = area;
		
		panelRelatedArea.setArea(area);
		
		// Load related area.
		panelRelatedArea.loadRelatedArea();
	}

	/**
	 * On currentArea change.
	 */
	protected void onAreaChange() {

		selectedSubAreas = buttonSubAreas.isSelected();
		
		// Load areas.
		loadAreas();
	}

	/**
	 * Load areas.
	 * @param currentArea
	 */
	protected void loadAreas() {
		
		// Override the method.
	}

	/**
	 * Swap areas priority
	 * @param row1
	 * @param row2
	 */
	private void swapAreaPriorities(int row1, int row2) {
		
		boolean useSubAreas = buttonSubAreas.isSelected();

		// Get area1 and area2.
		Area area1 = (Area) tableAreas.getModel().getValueAt(row1, 0);
		Area area2 = (Area) tableAreas.getModel().getValueAt(row2, 0);
		
		// Swap sub areas priorities.
		MiddleResult result;
		if (useSubAreas) {
			result = ProgramBasic.getMiddle().swapAreaSubAreasPriorities(
					ProgramBasic.getLoginProperties(), currentArea,
					area1, area2);
		}
		else {
			result = ProgramBasic.getMiddle().swapAreaSuperAreasPriorities(
					ProgramBasic.getLoginProperties(), currentArea,
					area1, area2);
		}
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update information.
		ConditionalEvents.transmit(AreaDependenciesPanelBase.this, Signal.swapSiblingAreas, area1.getId(), area2.getId());
		onAreaChange();
	}

	/**
	 * On currentArea up.
	 */
	protected void onUp() {
		
		// Get selected currentArea.
		int selectedRow = tableAreas.getSelectedRow();
		if (selectedRow == -1) {
			Utility.show(this, "org.multipage.generator.textSelectAreaPriority");
			return;
		}
		
		// Get previous row.
		int previousRow = selectedRow - 1;
		if (previousRow < 0) {
			return;
		}
		
		// Swap priorities.
		swapAreaPriorities(selectedRow, previousRow);
	}

	/**
	 * On currentArea down.
	 */
	protected void onDown() {
		
		// Get selected currentArea.
		int selectedRow = tableAreas.getSelectedRow();
		if (selectedRow == -1) {
			Utility.show(this, "org.multipage.generator.textSelectAreaPriority");
			return;
		}

		// Get next row.
		int nextRow = selectedRow + 1;
		if (nextRow >= tableAreas.getModel().getRowCount()) {
			return;
		}
		
		// Swap priorities.
		swapAreaPriorities(selectedRow, nextRow);
	}

	/**
	 * On reset siblings order.
	 */
	protected void onReset() {
		
		long currentAreaId = currentArea.getId();

		// Reset sub areas priorities.
		MiddleResult result = ProgramBasic.getMiddle().resetSubAreasPriorities(
				ProgramBasic.getLoginProperties(), currentAreaId);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update information.
		ConditionalEvents.transmit(AreaDependenciesPanelBase.this, Signal.resetSiblingAreasOrder, currentAreaId);
		onAreaChange();
	}

	/**
	 * Set icons.
	 */
	protected void setIcons() {

		buttonDefault.setIcon(Images.getIcon("org/multipage/generator/images/reset_order.png"));
		buttonUp.setIcon(Images.getIcon("org/multipage/generator/images/up.png"));
		buttonDown.setIcon(Images.getIcon("org/multipage/generator/images/down.png"));
	}

	/**
	 * Set tool tips.
	 */
	protected void setToolTips() {

		buttonDefault.setToolTipText(Resources.getString("org.multipage.generator.tooltipSetDefaultOrder"));
		buttonUp.setToolTipText(Resources.getString("org.multipage.generator.tooltipShiftAreaUp"));
		buttonDown.setToolTipText(Resources.getString("org.multipage.generator.tooltipShiftAreaDown"));
	}

	/**
	 * On close dialog.
	 */
	public boolean close() {
		
		return !tableAreas.isEditing();
	}

	/**
	 * On load panel information.
	 */
	@Override
	public void onLoadPanelInformation() {

		// Invoke currentArea change method.
		onAreaChange();
		
		// Load related area.
		panelRelatedArea.loadRelatedArea();
	}

	/**
	 * On save panel information.
	 */
	@Override
	public void onSavePanelInformation() {
		
	}
	
	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	protected static void addPopup(Component component, final JPopupMenu popup) {
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
	 * On table mouse click.
	 * @param e
	 */
	protected void onTableClick(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			
			// Focus selected area.
			Area area = getSelectedArea();
			if (area != null) {
				
				GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusArea(area.getId());
			}
		}
	}
}
