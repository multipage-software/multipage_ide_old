/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.EditorTabActions;
import org.multipage.generator.ProgramGenerator;
import org.multipage.gui.CheckBoxList;
import org.multipage.gui.CheckBoxListManager;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;

/**
 * 
 * @author
 *
 */
public class AreaInheritancePanel extends JPanel implements EditorTabActions {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Edited area reference.
	 */
	protected Area area;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelSuperAreas;
	private JScrollPane scrollPane;

	/**
	 * Create the panel.
	 */
	public AreaInheritancePanel() {

		initComponents();
		
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelSuperAreas = new JLabel("builder.textSuperAreas");
		springLayout.putConstraint(SpringLayout.NORTH, labelSuperAreas, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelSuperAreas, 10, SpringLayout.WEST, this);
		add(labelSuperAreas);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelSuperAreas);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, this);
		add(scrollPane);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelSuperAreas);
	}

	/**
	 * Set area reference.
	 * @param area
	 */
	public void setArea(Area area) {
		
		this.area = area;
	}

	/**
	 * Load inheritance.
	 */
	private void loadInheritance() {
		
		// Reload area object.
		area = ProgramGenerator.getArea(area.getId());
				

		// Connect inheritance list.
		CheckBoxList<Area> listInheritance = new CheckBoxList<Area>();
		scrollPane.setViewportView(listInheritance);
		
		listInheritance.setContentManager(new CheckBoxListManager<Area>() {
			
			// Loads items.
			@Override
			protected boolean loadItem(int index, Obj<Area> object,
					Obj<String> text, Obj<Boolean> selected) {
				
				LinkedList<Area> superAreas = area.getSuperareas();
				
				// If the index is out of bounds, return false value.
				if (index >= superAreas.size()) {
					return false;
				}
				
				Area superArea = superAreas.get(index);
				
				// Set object, text and inheritance.
				object.ref = superArea;
				text.ref = superArea.getDescriptionForDiagram();
				selected.ref = area.inheritsFrom(superArea);
				
				return true;
			}
			
			// Processes change.
			@Override
			protected boolean processChange(Area inheritArea, boolean selected) {

				// Save inheritance.
				return saveInheritance(inheritArea, selected);
			}
		});
	}
	
	/**
	 * Save inheritance.
	 * @param inherits 
	 */
	private boolean saveInheritance(Area superArea, boolean inherits) {
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Save sub area edges.
		result = middle.updateIsSubAreaEdge(login, superArea.getId(),
					area.getId(), inherits);
		if (result.isNotOK()) {
			result.show(this);
			return false;
		}

		// Set inheritance.
		area.setInheritanceLight(superArea.getId(), inherits);
		
		// Update information.
		updateInformation();
		
		return true;
	}
	
	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * On load panel information.
	 */
	public void onLoadPanelInformation() {

		// Load inheritance.
		loadInheritance();
	}

	/**
	 * On save panel information.
	 */
	public void onSavePanelInformation() {

	}
}
