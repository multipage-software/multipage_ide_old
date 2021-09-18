/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 19-02-2021
 *
 */
package org.multipage.generator;

import java.util.HashSet;

public enum Signal implements EventCondition {
	
	// Special signal that runs user lambda function placed in a message on the message thread.
	_invokeLater,
	
	// Enables target signal.
	_enableTargetSignal,
	
	// Load diagrams on application start up.
	loadDiagrams(
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// On diagram areas clicked.
	onClickDiagramAreas(
			SignalType.areaViewStateChange,
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// On drag diagram areas.
	onDragDiagramAreas(
			SignalType.areaViewStateChange,
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// On related areas clicked.
	onClickRelatedAreas(
			SignalType.areaViewStateChange,
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// Show properties of the areas. The set of area IDs should be appended to the propagated message: 
	// use ConditionalEvents.propagateMessage(source, Signal.showAreasProperties, HashSet<area IDs: Long>, ...).
	showAreasProperties(
			SignalType.areaViewChange,
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// Show relations of the areas. The set of area IDs should be appended to the propagated message: 
	// use ConditionalEvents.transmit(source, Signal.showAreasRelations, HashSet<area IDs: Long>, ...).
	showAreasRelations(
			SignalType.areaViewChange,
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// Display related areas.
	displayRelatedAreas(
			SignalType.areaViewChange,
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// Select areas in the diagram. The set of areas' IDs is sent in related info of the message.
	selectDiagramAreas(
			SignalType.areaViewChange,
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// Select all areas.
	selectAll(
			SignalType.areaViewStateChange,
			SignalType.areaViewChange,
			SignalType.slotViewChange,
			SignalType.guiChange
			),
	
	// Unselect all areas.
	unselectAll(
			SignalType.areaViewStateChange,
			SignalType.areaViewChange,
			SignalType.slotViewChange,
			SignalType.guiChange
			),
	
	// Main tab panel selection
	mainTabChange(
			SignalType.areaViewChange,
			SignalType.slotViewChange,
			SignalType.guiChange
			),
	
	// Sub panel tab change.
	subTabChange(
			SignalType.slotViewChange
			),
	
	// On show/hide IDs in areas diagram..
	showOrHideIds(
			SignalType.guiChange
			),
	
	// Show read only areas in areas diagram.
	exposeReadOnlyAreas(
			SignalType.guiChange
			),
	
	// Request update of all information.
	updateAll(
			SignalType.areaModelChange,
			SignalType.slotModelChange,
			SignalType.areaViewStateChange,
			SignalType.slotViewStateChange,
			SignalType.guiStateChange,
			SignalType.areaViewChange,
			SignalType.slotViewChange,
			SignalType.guiStateChange,
			SignalType.guiChange
			),
	
	// Redraw GUI.
	updateGui(
			SignalType.areaViewStateChange,
			SignalType.guiChange
			),
	
	// Focus on area.
	focusArea(
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// Focus on the Basic Area.
	focusBasicArea(
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// Focus on the tab area.
	focusTabArea(
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// Focus on the home area.
	focusHomeArea(
			SignalType.areaViewChange,
			SignalType.guiChange
			),
	
	// Monitor home page in web browser.
	monitorHomePage(
			SignalType.guiChange
			),
	// Update home area.
	updateHomeArea(
			SignalType.areaModelChange,
			SignalType.areaViewChange
			),
	// Save slot.
	areaSlotSaved(
			SignalType.slotModelChange
			),
	// Reactivate GUI
	reactivateGui(
			SignalType.guiStateChange
			),
	// Display or redraw tool tip.
	displayOrRedrawToolTip(
			SignalType.guiChange
			),
	// Remove tool tip.
	removeToolTip(
			SignalType.guiChange
			),
	// Update of area sub relation.
	updateAreaSubRelation,
	// Update of area super relation.
	updateAreaSuperRelation,
	// Swaps two sibling areas.
	swapSiblingAreas,
	// Reset order of sibling areas to default order.
	resetSiblingAreasOrder,
	// Set area relation names.
	setAreaRelationNames,
	// Update subarea hidden.
	updateHiddenSubArea,
	// Updates visibility of area.
	updateAreaVisibility,
	// Update area read only flag.
	updateAreaReadOnly,
	// Update area localized flag.
	updateAreaLocalized,
	// Update area can import flag.
	updateAreaCanImport,
	// Update area project root flag.
	updateAreaIsProjectRoot,
	// Save area int database.
	saveArea,
	// Upate changes in area.
	updateAreaChanges,
	// Update area is disabled flag.
	updateAreaIsDisabled,
	// Update area inheritance.
	updateAreaInheritance,
	// Import areas.
	importToArea,
	// Update area resources.
	updateAreaResources,
	// Edit resource.
	editResource,
	// Delete resources.
	deleteResources,
	// Create new text resource.
	createTextResource,
	// Change areas properties.
	changeAreasProperties,
	// Delete localized text of an area.
	deleteAreaLocalizedText,
	// Update start resource for an area.
	updateAreaStartResource,
	// Remove area start resource.
	removeAreaStartResource,
	// Add new area.
	addArea,
	// Hide unnecessary slots.
	hideSlots,
	// Update area versions.
	updateVersions,
	// Servlet causes slots update.
	updatedSlotsWithServlet,
	// Invoked when area constructors where loaded.
	loadAreaConstructors,
	// Invoked when colors where updated.
	updateColors,
	// New enumeration type.
	newEnumeration,
	// Remove enumeration.
	removeEnumeration,
	// Update enumeration.
	updateEnumeration,
	// New enumeration value.
	newEnumerationValue,
	// Update enumeration value.
	updateEnumerationValue,
	// Remove enumeration value.
	removeEnumerationValue,
	// Update area file names.
	updateAreaFileNames,
	// When area tree was created.
	createAreasTree,
	// Update whole model.
	updateAreasModel,
	// On new basic area (database changed).
	newBasicArea,
	// Transfer area with drag and drop.
	transferToArea,
	// Import MIME types.
	importMimeTypes,
	// Load default MIME types.
	defaultMimeTypes,
	// Created new text resource.
	newTextResource,
	// On update of realted area.
	updateRelatedArea,
	// Switch database.
	switchDatabase,
	// On cancel slot editing.
	cancelSlotEditor,
	// Set slots default values.
	setSlotsDefaultValues,
	// When user slots have been removed.
	removeUserSlots,
	// Move slots.
	moveSlots,
	// Remove slots.
	removeSlots,
	// Set slots properties.
	setSlotsProperties,
	// Remove diagram.
	removeDiagram,
	// Update controls.
	updateControls,
	// A flag in a panel with areas tree changed.
	treeFlagChange;
	
	/**
	 * Unnecessary signals.
	 */
	private static final Signal [] unnecessarySignals = { displayOrRedrawToolTip, removeToolTip };
	
	/**
	 * Signal is included in the following signal types.
	 */
	private HashSet<SignalType> includedInTypes = new HashSet<SignalType>();
	
	/**
	 * Priority of the signal.
	 */
	private int priority = ConditionalEvents.MIDDLE_PRIORITY;
	
	/**
	 * Enable or disable this signal.
	 */
	private boolean enabled = true;
	
	/**
	 * Constructor of the a signal.
	 * @param signalTypes
	 */
	Signal(SignalType ... signalTypes) {
		
		for (SignalType signalType : signalTypes) {
			includedInTypes.add(signalType);
		}
	}
	
	/**
	 * Enable this signal.
	 */
	public synchronized void enable() {
		
		this.enabled = true;
	}
	
	/**
	 * Disable this signal.
	 */
	public synchronized void disable() {
		
		this.enabled = false;
	}
	
	/**
	 * Check if this signal is enabled.
	 * @return
	 */
	public boolean isEnabled() {
		
		return this.enabled;
	}
	
	/**
	 * Check for a special signals.
	 * @param event
	 * @return
	 */
	public boolean isSpecial() {
		
		return _invokeLater.equals(this) || _enableTargetSignal.equals(this);
	}
	
	/**
	 * Returns true if current signal is that of the given input type.
	 * @param signalType
	 * @return
	 */
	public boolean isOfType(SignalType signalType) {
		
		// Check the input value.
		if (signalType == null) {
			return false;
		}
		
		// Try to find the type.
		boolean isIncluded = includedInTypes.contains(signalType);
		return isIncluded;
	}
	
	/**
	 * For debugging purposes it returns true if the signal is unnecessary.
	 * @return
	 */
	boolean isUnnecessary() {
		
		for (Signal unnecessarySignal : unnecessarySignals) {
			
			if (this.equals(unnecessarySignal)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * A helper function that forms array of event conditions.
	 * The method can be used this way: EventCondition.array(A, B, ...)
	 * @param eventSignals
	 * @return
	 */
	public static EventCondition [] array(EventCondition ... eventSignals) {
		
		return eventSignals;
	}

	/**
	 * Returns true if the incoming message matches this signal.
	 */
	@Override
	public boolean matches(Message incomingMessage) {
		
		// Check if incoming message signal matches.
		Signal signal = incomingMessage.signal;
		boolean matches = this.equals(signal);
		return matches;
	}
	
	/**
	 * Set priority.
	 */
	@Override
	public void setPriority(int priority) {
		
		this.priority = priority;
	}
	
	/**
	 * Get priority.
	 */
	@Override
	public int getPriority() {
		
		return priority;
	}
	
	/**
	 * Get types.
	 */
	public HashSet<SignalType> getTypes() {
		
		return includedInTypes;
	}
}