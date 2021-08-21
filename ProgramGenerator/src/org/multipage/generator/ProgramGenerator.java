/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JFrame;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.Resource;
import org.maclan.Slot;
import org.maclan.server.TextRenderer;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Callback;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.SerializeStateAdapter;
import org.multipage.gui.StateSerializer;
import org.multipage.gui.TextPopupMenuAddIn;
import org.multipage.util.Resources;

import build_number.BuildNumber;

/**
 * @author
 *
 */
public class ProgramGenerator {
	
	/**
	 * Debug flag.
	 */
	private static final boolean debug = true;
	
	/**
	 * AreasModel.
	 */
	protected static AreasModel areasModel;
	
	/**
	 * Resource location.
	 */
	protected static String resourcesLocation = "org.multipage.generator.properties.messages";

	/**
	 * Extension to builder.
	 */
	private static ExtensionToBuilder extensionToBuilder = null;
	
	
	private static ExtensionToBuilder extensionToDynamic;
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * Get state serializer
	 * @return
	 */
	public static StateSerializer getSerializer() {
		
		return serializer;
	}
	
	/**
	 * Initialize program basic layer.
	 * @param serializer 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
		// Remember the serializer
		ProgramGenerator.serializer = serializer;
		
		// Set local identifiers.
		Resources.setLanguageAndCountry(language, country);
		
		// Load resources file.
		if (!Resources.loadResource(resourcesLocation)) {
			return false;
		}

		// Add state serializer.
		if (serializer != null) {
			serializer.add(new SerializeStateAdapter() {
				// On read state.
				@Override
				protected void onReadState(ObjectInputStream inputStream)
						throws IOException, ClassNotFoundException {
					// Serialize program dictionary.
					seriliazeData(inputStream);
				}
				// On write state.
				@Override
				protected void onWriteState(ObjectOutputStream outputStream)
						throws IOException {
					// Serialize program dictionary.
					serializeData(outputStream);
				}
				// On set default state.
				@Override
				protected void onSetDefaultState() {
					// Set default data.
					setDefaultData();
				}
			});
		}

		// Create areas model.
		areasModel = new AreasModel();
		
		return true;
	}
	

	/**
	 * Set default data.
	 */
	protected static void setDefaultData() {
		
		GeneratorMainFrame.setDefaultData();
		Settings.setDefaultData();
		GeneratorUtilities.setDefaultData();
		AreasDiagram.setDefaultData();
		SplitProperties.setDefaultData();
		CustomizedControls.setDefaultData();
		CustomizedColors.setDefaultData();
		OverviewControl.setDefaultData();
		TextResourceEditor.setDefaultData();
		AreaEditorFrameBase.setDefaultData();
		ResourcesEditorDialog.setDefaultData();
		AreasProperties.setDefaultData();
		TextRenderer.setDefaultData();
		SelectNewTextResourceDialog.setDefaultData();
		ResourceAreasDialog.setDefaultData();
		SearchAreaDialog.setDefaultData();
		DisplayOnlineDialog.setDefaultData();
		AreasPropertiesFrame.setDefaultData();
		SlotListPanel.setDefaultData();
		AreaResourcesDialog.setDefaultData();
		SelectSubAreaDialog.setDefaultData();
		SelectVersionDialog.setDefaultData();
		SelectSuperAreaDialog.setDefaultData();
		CssMimePanel.setDefaultData();
		SpecialValueDialog.setDefaultData();
		EnumerationEditorPanel.setDefaultData();
		SelectEnumerationDialog.setDefaultData();
		SelectEnumerationFormatDialog.setDefaultData();
		ImportDialog.setDefaultData();
		SelectTransferMethodDialog.setDefaultData();
		SlotDescriptionDialog.setDefaultData();
		UserSlotInput.setDefaultData();
		SearchSlotDialog.setDefaultData();
		SlotEditorHelper.setDefaultData();
		SlotEditorFrame.setDefaultData();
		DebugViewer.setDefaultData();
		RevisionsDialog.setDefaultData();
		ExternalProviderDialog.setDefaultData();
		RevertExternalProvidersDialog.setDefaultData();
		SlotPropertiesDialog.setDefaultData();
		PathSelectionDialog.setDefaultData();
		CreateAreasFromSourceCode.setDefaultData();
		ClonedDiagramDialog.setDefaultData();
		LoggingDialog.setDefaultData();
		LoggingSettingsDialog.setDefaultData();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		GeneratorMainFrame.serializeData(inputStream);
		Settings.serializeData(inputStream);
		GeneratorUtilities.serializeData(inputStream);
		AreasDiagram.seriliazeData(inputStream);
		SplitProperties.seriliazeData(inputStream);
		CustomizedControls.seriliazeData(inputStream);
		CustomizedColors.seriliazeData(inputStream);
		OverviewControl.seriliazeData(inputStream);
		TextResourceEditor.seriliazeData(inputStream);
		AreaEditorFrameBase.seriliazeData(inputStream);
		ResourcesEditorDialog.seriliazeData(inputStream);
		AreasProperties.seriliazeData(inputStream);
		TextRenderer.serializeData(inputStream);
		RenderDialog.serializeData(inputStream);
		BrowserParametersDialog.serializeData(inputStream);
		CheckRenderedFiles.serializeData(inputStream);
		ConfirmAreasConnect.serializeData(inputStream);
		AreaTraceFrame.serializeData(inputStream);
		AreaHelpViewer.serializeData(inputStream);
		SelectNewTextResourceDialog.serializeData(inputStream);
		ResourceAreasDialog.serializeData(inputStream);
		SearchAreaDialog.serializeData(inputStream);
		DisplayOnlineDialog.serializeData(inputStream);
		AreasPropertiesFrame.serializeData(inputStream);
		SlotListPanel.serializeData(inputStream);
		AreaResourcesDialog.serializeData(inputStream);
		SelectSubAreaDialog.serializeData(inputStream);
		SelectVersionDialog.serializeData(inputStream);
		SelectSuperAreaDialog.serializeData(inputStream);
		CssMimePanel.serializeData(inputStream);
		SpecialValueDialog.serializeData(inputStream);
		EnumerationEditorPanel.serializeData(inputStream);
		SelectEnumerationDialog.serializeData(inputStream);
		SelectEnumerationFormatDialog.serializeData(inputStream);
		ImportDialog.serializeData(inputStream);
		SelectTransferMethodDialog.serializeData(inputStream);
		SlotDescriptionDialog.serializeData(inputStream);
		UserSlotInput.serializeData(inputStream);
		SearchSlotDialog.serializeData(inputStream);
		SlotEditorHelper.seriliazeData(inputStream);
		SlotEditorFrame.seriliazeData(inputStream);
		DebugViewer.serializeData(inputStream);
		RevisionsDialog.serializeData(inputStream);
		ExternalProviderDialog.serializeData(inputStream);
		RevertExternalProvidersDialog.serializeData(inputStream);
		SlotPropertiesDialog.serializeData(inputStream);
		PathSelectionDialog.serializeData(inputStream);
		CreateAreasFromSourceCode.serializeData(inputStream);
		ClonedDiagramDialog.serializeData(inputStream);
		LoggingDialog.serializeData(inputStream);
		LoggingSettingsDialog.serializeData(inputStream);
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		GeneratorMainFrame.serializeData(outputStream);
		Settings.serializeData(outputStream);
		GeneratorUtilities.serializeData(outputStream);
		AreasDiagram.serializeData(outputStream);
		SplitProperties.serializeData(outputStream);
		CustomizedControls.serializeData(outputStream);
		CustomizedColors.serializeData(outputStream);
		OverviewControl.serializeData(outputStream);
		TextResourceEditor.seriliazeData(outputStream);
		AreaEditorFrameBase.seriliazeData(outputStream);
		ResourcesEditorDialog.seriliazeData(outputStream);
		AreasProperties.seriliazeData(outputStream);
		TextRenderer.serializeData(outputStream);
		RenderDialog.serializeData(outputStream);
		BrowserParametersDialog.serializeData(outputStream);
		CheckRenderedFiles.serializeData(outputStream);
		ConfirmAreasConnect.serializeData(outputStream);
		AreaTraceFrame.serializeData(outputStream);
		AreaHelpViewer.serializeData(outputStream);
		SelectNewTextResourceDialog.serializeData(outputStream);
		ResourceAreasDialog.serializeData(outputStream);
		SearchAreaDialog.serializeData(outputStream);
		DisplayOnlineDialog.serializeData(outputStream);
		AreasPropertiesFrame.serializeData(outputStream);
		SlotListPanel.serializeData(outputStream);
		AreaResourcesDialog.serializeData(outputStream);
		SelectSubAreaDialog.serializeData(outputStream);
		SelectVersionDialog.serializeData(outputStream);
		SelectSuperAreaDialog.serializeData(outputStream);
		CssMimePanel.serializeData(outputStream);
		SpecialValueDialog.serializeData(outputStream);
		EnumerationEditorPanel.serializeData(outputStream);
		SelectEnumerationDialog.serializeData(outputStream);
		SelectEnumerationFormatDialog.serializeData(outputStream);
		ImportDialog.serializeData(outputStream);
		SelectTransferMethodDialog.serializeData(outputStream);
		SlotDescriptionDialog.serializeData(outputStream);
		UserSlotInput.serializeData(outputStream);
		SearchSlotDialog.serializeData(outputStream);
		SlotEditorHelper.seriliazeData(outputStream);
		SlotEditorFrame.seriliazeData(outputStream);
		DebugViewer.seriliazeData(outputStream);
		RevisionsDialog.serializeData(outputStream);
		ExternalProviderDialog.serializeData(outputStream);
		RevertExternalProvidersDialog.serializeData(outputStream);
		SlotPropertiesDialog.serializeData(outputStream);
		PathSelectionDialog.serializeData(outputStream);
		CreateAreasFromSourceCode.serializeData(outputStream);
		ClonedDiagramDialog.serializeData(outputStream);
		LoggingDialog.serializeData(outputStream);
		LoggingSettingsDialog.serializeData(outputStream);
	}
	
	/**
	 * Get application title.
	 * @return
	 */
	public static String getApplicationTitle() {
		
		return String.format(Resources.getString("org.multipage.generator.textMainFrameCaption"), BuildNumber.getVersion(), 
				ProgramGenerator.class.getSuperclass().getName().equals("GeneratorFullMain") ? "Network" : "Standalone");
	}

	/**
	 * @return the areasModel
	 */
	public static AreasModel getAreasModel() {
		return areasModel;
	}

	/**
	 * Get debug flag.
	 * @return
	 */
	public static boolean isDebug() {
		
		return debug;
	}

	/**
	 * Set extension to builder.
	 * @param extensionToBuilder the extensionToBuilder to set
	 */
	public static void setExtensionToBuilder(ExtensionToBuilder extensionToBuilder) {
		ProgramGenerator.extensionToBuilder = extensionToBuilder;
	}

	/**
	 * Set extension to dynamic.
	 * @param extensionToDynamic the extensionToBuilder to set
	 */
	public static void getExtensionToDynamic(ExtensionToBuilder extensionToDynamic) {
		
		if (!isExtensionToDynamic()) {
			ProgramGenerator.extensionToDynamic = extensionToDynamic;
		}
		else {
			
		}
	}
	
	/**
	 * Returns true value, if an extension to builder exists.
	 * @return
	 */
	public static boolean isExtensionToBuilder() {
		
		return extensionToBuilder != null;
	}
	
	/**
	 * Returns true value, if an extension to dynamic exists.
	 * @return
	 */
	public static boolean isExtensionToDynamic() {
		
		return extensionToDynamic != null;
	}
	
	/**
	 * Create area editor object.
	 * @param parentComponent
	 * @param area
	 * @return
	 */
	public static AreaEditorFrameBase newAreaEditor(Component parentComponent, Area area) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreaEditor(parentComponent, area);
		}
		
		return new AreaEditorFrame(parentComponent, area);
	}
	
	/**
	 * Create area editor object.
	 * @param parentComponent
	 * @param area
	 * @return
	 */
	public static AreaEditorPanelBase newAreaEditorPanel(Component parentComponent, Area area) {

		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreaEditorPanel(parentComponent, area);
		}
		
		return new AreaEditorPanel(parentComponent, area);
	}

	/**
	 * New areas diagram.
	 * @param areasDiagramEditor
	 * @return
	 */
	public static AreasDiagram newAreasDiagram(AreasDiagramPanel areasDiagramEditor) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreasDiagram(areasDiagramEditor);
		}
		
		return new AreasDiagram(areasDiagramEditor);
	}

	/**
	 * Create new slot list panel.
	 * @return
	 */
	public static SlotListPanel newSlotListPanel() {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newSlotListPanel();
		}
		
		return new SlotListPanel();
	}

	/**
	 * Create new slot editor object.
	 * @param parentWindow
	 * @param slot
	 * @param isNew
	 * @param modal
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @return
	 */
	public static SlotEditorBaseFrame newSlotEditor(
			Window parentWindow, Slot slot, boolean isNew, boolean modal,
			boolean useHtmlEditor, FoundAttr foundAttr) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newSlotEditor(parentWindow, slot, isNew, modal, useHtmlEditor, foundAttr);
		}
		
		return new SlotEditorFrame(parentWindow, slot, isNew, modal, useHtmlEditor, foundAttr);
	}

	/**
	 * Create new slot editor.
	 * @param slot
	 * @param isNew
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @param onChangeEvent
	 * @return
	 */
	public static SlotEditorBaseFrame newSlotEditor(Slot slot, boolean isNew,
			boolean useHtmlEditor, FoundAttr foundAttr, Callback onChangeEvent) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newSlotEditor(slot, isNew, useHtmlEditor, foundAttr, onChangeEvent);
		}
		
		return new SlotEditorFrame(slot, isNew, useHtmlEditor, foundAttr, onChangeEvent);
	}

	/**
	 * Create new boolean editor object.
	 * @return
	 */
	public static BooleanEditorPanelBase newBooleanEditorPanel() {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newBooleanEditorPanel();
		}
		
		return new BooleanEditorPanel();
	}

	/**
	 * Create new enumeration editor object.
	 * @return
	 */
	public static EnumerationEditorPanelBase newEnumerationEditorPanel() {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newEnumerationEditorPanel();
		}
		
		return new EnumerationEditorPanel();
	}

	/**
	 * Create new area loacal trayMenu object.
	 * @param listener
	 * @return
	 */
	public static AreaLocalMenu newAreaLocalMenu(
			AreaLocalMenuListener listener) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreaLocalMenu(listener);
		}
		
		return new AreaLocalMenu(listener);
	}

	/**
	 * Create new area local trayMenu object for diagram.
	 * @param listener
	 * @return
	 */
	public static AreaLocalMenu newAreaLocalMenuForDiagram(
			AreaLocalMenuListener listener) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreaLocalMenuForDiagram(listener);
		}
		
		return new AreaLocalMenu(listener, AreaLocalMenu.DIAGRAM);
	}

	/**
	 * Create new areas properties object.
	 * @param isPropertiesPanel
	 * @return
	 */
	public static AreasPropertiesBase newAreasProperties(boolean isPropertiesPanel) {

		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreasProperties(isPropertiesPanel);
		}
		
		return new AreasProperties(isPropertiesPanel);
	}

	/**
	 * Create slot text popup trayMenu.
	 * @param slot
	 * @return
	 */
	public static TextPopupMenuAddIn newGeneratorTextPopupMenuAddIn(Slot slot) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newGeneratorTextPopupMenuAddIn(slot);
		}
		
		return new GeneratorTextPopupMenuAddIn(slot);
	}

	/**
	 * Create new about dialog.
	 * @param frame
	 * @return
	 */
	public static AboutDialogBase newAboutDialog(JFrame frame) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newAboutDialog(frame);
		}
		
		return new AboutDialog(frame);
	}

	/**
	 * Get area object.
	 * @param id
	 * @return
	 */
	public static Area getArea(long id) {
		
		return areasModel.getArea(id);
	}
	
	/**
	 * Update ara.
	 * @param area
	 * @return
	 */
	public static Area updateArea(Area area) {
		
		long id = area.getId();
		return areasModel.getArea(id);
	}

	/**
	 * Get home area.
	 * @return
	 */
	public static Area getHomeArea() {
		
		return areasModel.getHomeArea();
	}

	/**
	 * Create resource properties dialog object.
	 * @param parentComponent
	 * @param resource
	 * @return
	 */
	public static ResourcePropertiesEditorBase newResourcePropertiesEditor(
			Component parentComponent, Resource resource) {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newResourcePropertiesEditor(parentComponent,
					resource);
		}
		
		return new ResourcePropertiesEditor(parentComponent, resource);
	}

	/**
	 * Create new namespace renerer object.
	 * @return
	 */
	public static NamespaceResourceRendererBase newNamespaceResourceRenderer() {
		
		if (extensionToBuilder != null) {
			return extensionToBuilder.newNamespaceResourceRenderer();
		}
		
		return new NamespaceResourceRenderer();
	}

	/**
	 * Create area resource renderer object.
	 * @return
	 */
	public static AreaResourceRendererBase newAreaResourceRenderer() {

		if (extensionToBuilder != null) {
			return extensionToBuilder.newAreaResourceRenderer();
		}
		
		return new AreaResourceRenderer();
	}

	/**
	 * Get external slots in input areas.
	 * @param areas
	 * @return
	 */
	public static LinkedList<Slot> getExternalSlots(LinkedList<Area> areas)
		throws Exception {
		
		LinkedList<Slot> externalSlots = new LinkedList<Slot>();
		Exception exception = null;
		
		// Get list of external providers in selected areas.
		try {
			
			Middle middle = ProgramBasic.loginMiddle();
			
			LinkedList<Slot> slots = new LinkedList<Slot>();
			for (Area area : areas) {
				
				// Load slots.
				MiddleResult result = middle.loadAreaExternalSlots(area, slots);
				result.throwPossibleException();
				
				// Append them to result list.
				externalSlots.addAll(slots);
			}
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			// Logout from middle layer.
			ProgramBasic.logoutMiddle();
		}
		
		// Throw exception.
		if (exception != null) {
			throw exception;
		}
		
		// Return result list.
		return externalSlots;
	}
	
	/**
	 * Reload areas model.
	 */
	public static MiddleResult reloadModel() {
		
		// Get model object.
		AreasModel model = ProgramGenerator.getAreasModel();
		synchronized (model) {
		
			// Get login information.
			Properties properties = ProgramBasic.getLoginProperties();
			
			// Get hidden slots flag.
			boolean loadHiddenSlots = ProgramGenerator.isExtensionToBuilder() ? true : false;
			
			// Load areas model from database.
			MiddleResult result = ProgramBasic.getMiddle().loadAreasModel(properties, model, loadHiddenSlots);
			
			return result;
		}
	}

	/**
	 * Get all area IDs.
	 * @return
	 */
	public static HashSet<Long> getAllAreaIds() {
		
		HashSet<Long> areaIds = new HashSet<Long>();
		
		synchronized (areasModel) {
			
			for (Area area : areasModel.getAreas()) {
				
				long areaId = area.getId();
				areaIds.add(areaId);
			}
		}
		return areaIds;
	}
	
	/**
	 * Return updated list of areas.
	 * @param areas
	 * @return
	 */
	public static LinkedList<Area> getUpdatedAreas(LinkedList<Area> areas) {
		
		LinkedList<Area> updatedAreas = new LinkedList<Area>();
		
		// Update each area.
		if (areas != null) {
			for (Area area : areas) {
				
				long areaId = area.getId();
				Area updatedArea = getArea(areaId);
				
				updatedAreas.add(updatedArea);
			}
		}
		
		return updatedAreas;
	}
	
	/**
	 * Get areas with given IDs.
	 * @param areaIds
	 * @return
	 */
	public static LinkedList<Area> getAreas(HashSet<Long> areaIds) {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		
		// Get list of areas with given ID.
		for (Long areaId : areaIds) {
			if (areaId != null) {
				
				Area area = getArea(areaId);
				areas.add(area);
			}
		}
		return areas;
	}
	
	/**
	 * Get model identifier for debugging purposes.
	 * @return
	 */
	public static String getModelIdentifier() {
		
		if (areasModel == null) {
			return "unknown";
		}
		return areasModel.getTimeStamp();
	}
}
