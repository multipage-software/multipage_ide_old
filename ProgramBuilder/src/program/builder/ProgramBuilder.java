/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;import java.io.*;
import java.util.Properties;

import javax.swing.JFrame;

import org.multipage.basic.*;
import org.multipage.generator.*;
import org.maclan.*;

/**
 * @author
 *
 */
public class ProgramBuilder extends ProgramGenerator {
	
	/**
	 * Debug flag.
	 */
	private static final boolean debug = false;

	/**
	 * Resource location.
	 */
	private static final String resourcesLocation = "program.builder.properties.messages";
	
	/**
	 * Extensions to ProgramBuilderDynamic.
	 */
	private static ExtensionsToDynamic extensionsToDynamic;

	/**
	 * Initialize program basic layer.
	 * @param serializer 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
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
				protected void onReadState(StateInputStream inputStream)
						throws IOException, ClassNotFoundException {
					// Serialize program dictionary.
					seriliazeData(inputStream);
				}
				// On write state.
				@Override
				protected void onWriteState(StateOutputStream outputStream)
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
		ExtensionsToDynamic extensions = ProgramBuilder.getExtensionsToDynamic();
		if (extensions == null) {
			areasModel = new AreasModel();
		}
		else {
			areasModel = extensions.newAreasModel();
		}
		
		
		// Set extension of program generator to program builder.
		setExtensionsToBuilder();
		
		return true;
	}

	/**
	 * Set default data.
	 */
	protected static void setDefaultData() {

		// Default main frame data.
		BuilderMainFrame.setDefaultData();
		// Default slot editor data.
		SlotEditorFrameBuilder.setDefaultData();
		// Default enumerations editor dialog state.
		EnumerationsEditorDialog.setDefaultData();
		// Default enumeration selector dialog state.
		EnumerationValueSelectionDialog.setDefaultData();
		
		SelectVersionDialog.setDefaultData();
		ConstructorsPanel.setDefaultData();
		SlotListPanel.setDefaultData();
		NewEnumerationValueDialog.setDefaultData();
		EditEnumerationValueDialog.setDefaultData();
		AreaSourceDialog.setDefaultData();
		AskConstructorHolder.setDefaultData();
	}

	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load main frame data.
		BuilderMainFrame.serializeData(inputStream);
		// Load slot editor data.
		SlotEditorFrameBuilder.seriliazeData(inputStream);
		// Load enumerations editor dialog state.
		EnumerationsEditorDialog.serializeData(inputStream);
		// Load enumeration selector dialog state.
		EnumerationValueSelectionDialog.serializeData(inputStream);
		
		SelectVersionDialog.serializeData(inputStream);
		ConstructorsPanel.serializeData(inputStream);
		SlotListPanel.serializeData(inputStream);
		NewEnumerationValueDialog.serializeData(inputStream);
		EditEnumerationValueDialog.serializeData(inputStream);
		AreaSourceDialog.serializeData(inputStream);
		AskConstructorHolder.serializeData(inputStream);
	}

	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {

		// Save main frame data.
		BuilderMainFrame.serializeData(outputStream);
		// Save slot editor data.
		SlotEditorFrameBuilder.seriliazeData(outputStream);
		// Save enumerations editor dialog state.
		EnumerationsEditorDialog.serializeData(outputStream);
		// Save enumeration selector dialog state.
		EnumerationValueSelectionDialog.serializeData(outputStream);
		
		SelectVersionDialog.serializeData(outputStream);
		ConstructorsPanel.serializeData(outputStream);
		SlotListPanel.serializeData(outputStream);
		NewEnumerationValueDialog.serializeData(outputStream);
		EditEnumerationValueDialog.serializeData(outputStream);
		AreaSourceDialog.serializeData(outputStream);
		AskConstructorHolder.serializeData(outputStream);
	}

	/**
	 * @return the areasModel
	 */
	public static AreasModel getAreasModel() {
		return areasModel;
	}

	/**
	 * @return the extensionsToDynamic
	 */
	public static ExtensionsToDynamic getExtensionsToDynamic() {
		return extensionsToDynamic;
	}

	/**
	 * @param extensionsToDynamic the extensionsToDynamic to set
	 */
	public static void setExtensionsToDynamic(ExtensionsToDynamic extensionsToDynamic) {
		ProgramBuilder.extensionsToDynamic = extensionsToDynamic;
	}

	/**
	 * @return the debug
	 */
	public static boolean isDebug() {
		return debug;
	}
	

	/**
	 * Set extensions to builder.
	 */
	private static void setExtensionsToBuilder() {
		
		ProgramGenerator.setExtensionToBuilder(new ExtensionToBuilder() {
			
			// Create new area editor object.
			@Override
			public AreaEditorFrameBase newAreaEditor(Component parentComponent, Area area) {
				
				return new AreaEditorBuilder(parentComponent, area);
			}
			
			@Override
			public AreaEditorPanelBase newAreaEditorPanel(Component parentComponent, Area area) {
				
				return new AreaEditorBuilderPanel(parentComponent, area);
			}

			// Create new areas diagram object.
			@Override
			public AreasDiagram newAreasDiagram(
					AreasDiagramPanel areasDiagramEditor) {
				
				return new AreasDiagramBuilder(areasDiagramEditor);
			}

			// Create new slot list panel.
			@Override
			public SlotListPanel newSlotListPanel() {
				
				return new SlotListPanelBuilder();
			}

			// Create slot editor.
			@Override
			public SlotEditorBaseFrame newSlotEditor(Window parentWindow, Slot slot,
					boolean isNew, boolean modal, boolean useHtmlEditor,
					FoundAttr foundAttr) {
				
				return new SlotEditorFrameBuilder(parentWindow, slot, isNew, modal, useHtmlEditor, foundAttr);
			}

			/**
			 * Create slot editor.
			 */
			@Override
			public SlotEditorBaseFrame newSlotEditor(Slot slot, boolean isNew,
					boolean useHtmlEditor, FoundAttr foundAttr,
					Callback onChangeEvent) {
				
				return new SlotEditorFrameBuilder(slot, isNew, useHtmlEditor, foundAttr, onChangeEvent);
			}
			
			// Create boolean editor.
			@Override
			public BooleanEditorPanelBase newBooleanEditorPanel() {
				
				return new BooleanEditorPanelBuilder();
			}

			// Create new enumeration editor base.
			@Override
			public EnumerationEditorPanelBase newEnumerationEditorPanel() {
				
				return new EnumerationEditorPanelBuilder();
			}

			// Create new area local trayMenu object.
			@Override
			public AreaLocalMenu newAreaLocalMenu(AreaLocalMenuListener listener) {
				
				return new AreaLocalMenuBuilder(listener);
			}
			
			// Create new area local trayMenu object for diagram.
			@Override
			public AreaLocalMenu newAreaLocalMenuForDiagram(
					AreaLocalMenuListener listener) {
				
				return new AreaLocalMenuBuilder(listener, AreaLocalMenuBuilder.DIAGRAM);
			}

			// Create areas properties object.
			@Override
			public AreasPropertiesBase newAreasProperties(
					boolean isPropertiesPanel) {
				
				return new AreasPropertiesBuilder(isPropertiesPanel);
			}

			/**
			 * Create new slot text popup trayMenu.
			 */
			@Override
			public TextPopupMenuAddIn newGeneratorTextPopupMenuAddIn(Slot slot) {
				
				return new BuilderTextPopupMenuAddIn(slot);
			}

			/**
			 * Create new about dialog.
			 */
			@Override
			public AboutDialogBase newAboutDialog(JFrame frame) {
				
				return new AboutDialogBuilder(frame);
			}

			/**
			 * Create new resource properties editor object.
			 */
			@Override
			public ResourcePropertiesEditorBase newResourcePropertiesEditor(
					Component parentComponent, Resource resource) {
				
				return new ResourcePropertiesEditorBuilder(parentComponent, resource);
			}

			/**
			 * Create namespace resource renderer object.
			 */
			@Override
			public NamespaceResourceRendererBase newNamespaceResourceRenderer() {
				
				return new NamespaceResourceRendererBuilder();
			}

			/**
			 * Create area resource renerer object.
			 */
			@Override
			public AreaResourceRendererBase newAreaResourceRenderer() {
				
				return new AreaResourceRendererBuilder();
			}
		});
	}
	
	/**
	 * Edit start resource.
	 * @param area
	 * @param inherits 
	 */
	public static void editStartResource(Area area, boolean inherits) {
		
		Component parentComponent = GeneratorMainFrame.getFrame();
		
		Obj<Long> versionId = new Obj<Long>(0L);
		
		if (inherits) {
			
			// Select version.
			Obj<VersionObj> version = new Obj<VersionObj>();
			
			if (!SelectVersionDialog.showDialog(parentComponent, version)) {
				return;
			}
			
			// Get selected version ID.
			versionId.ref = version.ref.getId();
			
			// Get inherited area.
			Area inheritedArea = ProgramGenerator.getAreasModel().getStartArea(area, versionId.ref);
			if (inheritedArea != null) {
				area = inheritedArea;
			}
		}
		
		// Load area source.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		Obj<Long> resourceId = new Obj<Long>(0L);
		
		MiddleResult result = middle.loadAreaSource(login, area.getId(), versionId.ref, resourceId);
		if (result.isNotOK()) {
			result.show(null);
			return;
		}
		
		// Load old style start resource if not loaded.
		if (resourceId.ref == null) {
			result = middle.loadContainerStartResource(login, area, resourceId, versionId, null);
			if (result.isNotOK()) {
				result.show(null);
				return;
			}
		}
		
		if (resourceId.ref == 0L) {
			Utility.show(null, "builder.messageAreaHasNoStartResource");
			return;
		}
		
		// Get saving method.
		Obj<Boolean> savedAsText = new Obj<Boolean>();
		result = middle.loadResourceSavingMethod(login, resourceId.ref, savedAsText);
		if (result.isNotOK()) {
			result.show(null);
			return;
		}

		// Edit text resource.
		TextResourceEditor.showDialog(parentComponent, resourceId.ref,
				savedAsText.ref, false);
	}
	
	/**
	 * Edit text resource.
	 * @param area
	 * @param inherits 
	 */
	public static void editTextResource(Area area, boolean inherits) {
		
		Component parentComponent = GeneratorMainFrame.getFrame();
		
		if (inherits) {
			
			// Select version.
			Obj<VersionObj> version = new Obj<VersionObj>();
			
			if (!SelectVersionDialog.showDialog(parentComponent, version)) {
				return;
			}
			
			// Get inherited area.
			Area inheritedArea = ProgramGenerator.getAreasModel().getStartArea(area, version.ref.getId());
			if (inheritedArea != null) {
				area = inheritedArea;
			}
		}
		
		Resource resource = SelectAreaTextResources.showDialog(GeneratorMainFrame.getFrame(), area);
		// Edit text resource.
		if (resource != null) {
			TextResourceEditor.showDialog(parentComponent, resource.getId(),
					true, false);
		}
	}
}
