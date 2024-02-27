/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 30-10-2022
 *
 */
package org.multipage.gui;

import java.util.HashSet;

/**
 * Signal definitions for GUI.
 * @author vakol
 *
 */
public class GuiSignal extends Signal {

	// Load diagrams on application start up.
	public static Signal loadDiagrams = new Signal();
	
	// On diagram areas clicked.
	public static Signal onClickDiagramAreas = new Signal();
	
	// On drag diagram areas.
	public static Signal onDragDiagramAreas = new Signal();
	
	// On related areas clicked.
	public static Signal onClickRelatedAreas = new Signal();
	
	// Show properties of the areas. The set of area IDs should be appended to the propagated message: 
	// use ConditionalEvents.propagateMessage(source, Signal.showAreasProperties, HashSet<area IDs: Long>, ...).
	public static Signal showAreasProperties = new Signal();
	
	// Show relations of the areas. The set of area IDs should be appended to the propagated message: 
	// use ConditionalEvents.transmit(source, Signal.showAreasRelations, HashSet<area IDs: Long>, ...).
	public static Signal showAreasRelations = new Signal();
	
	// Display related areas.
	public static Signal displayRelatedAreas = new Signal();
	
	// Select area in the diagram. Area is determined by its ID.
	public static Signal selectDiagramArea = new Signal(
			params(
					Long.class,	// Area ID.
					Boolean.class // Select single area (false) or add area to selection (true).		
				));
	
	// Select areas in the diagram. The set of areas' IDs is sent in related info inside the message.
	public static Signal selectDiagramAreas = new Signal(
			params(
				HashSet.class // The list of area IDs.
			));
	
	// Select area with sub areas. 
	public static final Signal selectDiagramAreaWithSubareas = new Signal(
			params(
					Long.class, // Area ID.
					Boolean.class
				));
	
	// Select areas in the tree. The set of areas' IDs is sent in related info inside the message.
	public static Signal selectTreeAreas = new Signal(
			params(
				HashSet.class // The list of area IDs.
			));

	// Select all areas.
	public static Signal selectAll = new Signal();
	
	// Unselect all areas.
	public static Signal unselectAll = new Signal();
	
	// Main tab panel selection
	public static Signal mainTabChange = new Signal();
	
	// Sub panel tab change.
	public static Signal subTabChange = new Signal();
	
	// On show/hide IDs in areas diagram..
	public static Signal showOrHideIds = new Signal();
	
	// Show read only areas in areas diagram.
	public static Signal exposeReadOnlyAreas = new Signal();
	
	// Redraw GUI.
	public static Signal updateGui = new Signal();
	
	// Focus on area.
	public static Signal focusArea = new Signal();
	
	// Focus on the Basic Area.
	public static Signal focusBasicArea = new Signal();
	
	// Focus on the tab area.
	public static Signal focusTopArea = new Signal();
	
	// Focus on the home area.
	public static Signal focusHomeArea = new Signal();
	
	// Monitor home page in web browser.
	public static Signal monitorHomePage = new Signal();
	// Update home area.
	public static Signal updateHomeArea = new Signal();
	// Save slot.
	public static Signal areaSlotSaved = new Signal();
	// Reactivate GUI
	public static Signal reactivateGui = new Signal();
	// Display or redraw tool tip.
	public static Signal displayOrRedrawToolTip = new Signal();
	// Remove tool tip.
	public static Signal removeToolTip = new Signal();
	// Update of area sub relation.
	public static Signal updateAreaSubRelation = new Signal();
	// Update of area super relation.
	public static Signal updateAreaSuperRelation = new Signal();
	// Swaps two sibling areas.
	public static Signal swapSiblingAreas = new Signal();
	// Reset order of sibling areas to default order.
	public static Signal resetSiblingAreasOrder = new Signal();
	// Set area relation names.
	public static Signal setAreaRelationNames = new Signal();
	// Update sub area hidden.
	public static Signal updateHiddenSubArea = new Signal();
	// Updates visibility of area.
	public static Signal updateAreaVisibility = new Signal();
	// Update area read only flag.
	public static Signal updateAreaReadOnly = new Signal();
	// Update area localized flag.
	public static Signal updateAreaLocalized = new Signal();
	// Update area can import flag.
	public static Signal updateAreaCanImport = new Signal();
	// Update area project root flag.
	public static Signal updateAreaIsProjectRoot = new Signal();
	// Save area into the database.
	public static Signal saveArea = new Signal();
	// Update changes in area.
	public static Signal updateAreaChanges = new Signal();
	// Update area is disabled flag.
	public static Signal updateAreaIsDisabled = new Signal();
	// Update area inheritance.
	public static Signal updateAreaInheritance = new Signal();
	// Import areas.
	public static Signal importToArea = new Signal();
	// Update area resources.
	public static Signal updateAreaResources = new Signal();
	// Edit resource.
	public static Signal editResource = new Signal();
	// Delete resources.
	public static Signal deleteResources = new Signal();
	// Create new text resource.
	public static Signal createTextResource = new Signal();
	// Change areas properties.
	public static Signal changeAreasProperties = new Signal();
	// Delete localized text of an area.
	public static Signal deleteAreaLocalizedText = new Signal();
	// Update start resource for an area.
	public static Signal updateAreaStartResource = new Signal();
	// Remove area start resource.
	public static Signal removeAreaStartResource = new Signal();
	// Add new area.
	public static Signal addArea = new Signal();
	// Hide unnecessary slots.
	public static Signal hideSlots = new Signal();
	// Update area versions.
	public static Signal updateVersions = new Signal();
	// Servlet causes slots update.
	public static Signal updatedSlotsWithServlet = new Signal();
	// Invoked when area constructors where loaded.
	public static Signal loadAreaConstructors = new Signal();
	// Invoked when colors where updated.
	public static Signal updateColors = new Signal();
	// New enumeration type.
	public static Signal newEnumeration = new Signal();
	// Remove enumeration.
	public static Signal removeEnumeration = new Signal();
	// Update enumeration.
	public static Signal updateEnumeration = new Signal();
	// New enumeration value.
	public static Signal newEnumerationValue = new Signal();
	// Update enumeration value.
	public static Signal updateEnumerationValue = new Signal();
	// Remove enumeration value.
	public static Signal removeEnumerationValue = new Signal();
	// Update area file names.
	public static Signal updateAreaFileNames = new Signal();
	// When area tree was created.
	public static Signal createAreasTree = new Signal();
	// On new basic area (database changed).
	public static Signal newBasicArea = new Signal();
	// Transfer area with drag and drop.
	public static Signal transferToArea = new Signal();
	// Import MIME types.
	public static Signal importMimeTypes = new Signal();
	// Load default MIME types.
	public static Signal defaultMimeTypes = new Signal();
	// Created new text resource.
	public static Signal newTextResource = new Signal();
	// On update of related area.
	public static Signal updateRelatedArea = new Signal();	
	// On cancel slot editing.
	public static Signal cancelSlotEditor = new Signal();
	// Set slots default values.
	public static Signal setSlotsDefaultValues = new Signal();
	// When user slots have been removed.
	public static Signal removeUserSlots = new Signal();
	// Move slots.
	public static Signal moveSlots = new Signal();
	// Remove slots.
	public static Signal removeSlots = new Signal();
	// Set slots properties.
	public static Signal setSlotsProperties = new Signal();
	// Remove diagram.
	public static Signal removeDiagram = new Signal();
	// Update controls.
	public static Signal updateControls = new Signal();
	// A flag in a panel with areas tree changed.
	public static Signal treeFlagChange = new Signal();

	/**
	 * Static constructor.
	 */
	static {
		// Unnecessary signals in static constructor.
		addUnnecessary(GuiSignal.displayOrRedrawToolTip, GuiSignal.removeToolTip);
		
		// Describe signals.
		reflectSignals(GuiSignal.class);
	}
	
	/**
	 * Check if an input object equals this signal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof GuiSignal) {
			
			Signal signal = (GuiSignal) obj;
			boolean isSame = this.name.equals(signal.name);
			
			return isSame;
		}
		return false;
	}
}
