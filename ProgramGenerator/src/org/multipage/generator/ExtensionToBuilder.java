/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JFrame;

import org.maclan.Area;
import org.maclan.Resource;
import org.maclan.Slot;
import org.multipage.gui.Callback;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.TextPopupMenuAddIn;

/**
 * @author
 *
 */
public interface ExtensionToBuilder {

	/**
	 * Create new area editor object.
	 * @param parentComponent
	 * @param area
	 * @return
	 */
	AreaEditorFrameBase newAreaEditor(Component parentComponent, Area area);
	
	/**
	 * Create new area editor panel.
	 * @param parentComponent
	 * @param area
	 * @return
	 */
	AreaEditorPanelBase newAreaEditorPanel(Component parentComponent, Area area);
	
	/**
	 * Create new areas diagram editor.
	 * @param areasDiagramEditor
	 * @return
	 */
	AreasDiagram newAreasDiagram(AreasDiagramPanel areasDiagramEditor);

	/**
	 * Create new slot list panel.
	 * @return
	 */
	SlotListPanel newSlotListPanel();

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
	SlotEditorBaseFrame newSlotEditor(Window parentWindow, Slot slot, boolean isNew,
			boolean modal, boolean useHtmlEditor, FoundAttr foundAttr);

	/**
	 * Create slot editor.
	 * @param slot
	 * @param isNew
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @param onChangeEvent
	 * @return
	 */
	SlotEditorBaseFrame newSlotEditor(Slot slot, boolean isNew, boolean useHtmlEditor,
			FoundAttr foundAttr, Callback onChangeEvent);
	
	/**
	 * Create new boolean editor panel.
	 * @return
	 */
	BooleanEditorPanelBase newBooleanEditorPanel();

	/**
	 * Create new enumeration panel.
	 * @return
	 */
	EnumerationEditorPanelBase newEnumerationEditorPanel();

	/**
	 * Create new area local trayMenu object.
	 * @param listener
	 * @return 
	 */
	AreaLocalMenu newAreaLocalMenu(AreaLocalMenuListener listener);
	
	/**
	 * Create new area local trayMenu object for diagram.
	 * @param listener
	 * @return 
	 */
	AreaLocalMenu newAreaLocalMenuForDiagram(AreaLocalMenuListener listener);
	
	/**
	 * Create new areas properties panel.
	 * @param isPropertiesPanel
	 * @return
	 */
	AreasPropertiesBase newAreasProperties(boolean isPropertiesPanel);

	/**
	 * Create new slot text popup trayMenu.
	 * @param slot
	 * @return
	 */
	TextPopupMenuAddIn newGeneratorTextPopupMenuAddIn(Slot slot);

	/**
	 * Create about dialog.
	 * @param frame
	 * @return
	 */
	AboutDialogBase newAboutDialog(JFrame frame);

	/**
	 * Create new resource properties editor object.
	 * @param parentComponent
	 * @param resource
	 * @return
	 */
	ResourcePropertiesEditorBase newResourcePropertiesEditor(
			Component parentComponent, Resource resource);

	/**
	 * Create namespace resource renderer object.
	 * @return
	 */
	NamespaceResourceRendererBase newNamespaceResourceRenderer();

	/**
	 * Create area resource object.
	 * @return
	 */
	AreaResourceRendererBase newAreaResourceRenderer();
}
