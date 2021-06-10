/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 09-06-2017
 *
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.CallbackNoArg;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.Area;
import com.maclan.Middle;
import com.maclan.MiddleResult;
import com.maclan.Revision;
import com.maclan.Slot;
import com.maclan.SlotHolder;
import com.maclan.SlotType;
import com.maclan.server.ProgramServlet;

/**
 * @author 
 *
 */
public class SlotEditorHelper {
	
	/**
	 * Font.
	 */
	protected static Font fontState;
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		fontState = new Font("DialogInput", Font.PLAIN, 12);
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		Object data = inputStream.readObject();
		if (!(data instanceof Font)) {
			throw new ClassNotFoundException();
		}
		fontState = (Font) data;
		
		TextSlotEditorPanel.openHtmlEditor = inputStream.readBoolean();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		outputStream.writeObject(fontState);
		outputStream.writeBoolean(TextSlotEditorPanel.openHtmlEditor);
	}
	
	/**
	 * List of created panels
	 */
	private static LinkedList<SlotEditorHelper> helpers;
	
	/**
	 * Enable events
	 */
	private static boolean enabledEvents = true;
	
	/**
	 * Slot copy.
	 */
	public Slot editedSlot;
	
	/**
	 * Original slot reference
	 */
	public Slot originalSlot;

	/**
	 * Reduced editor reference.
	 */
	protected SlotValueEditorPanelInterface reducedEditor;
	
	/**
	 * Is ready flag.
	 */
	public boolean initialized = false;
	
	/**
	 * Editors.
	 */
	protected TextSlotEditorPanel textEditor;
	protected IntegerEditorPanel integerEditor;
	protected RealEditorPanel realEditor;
	protected BooleanEditorPanelBase booleanEditor;
	protected EnumerationEditorPanelBase enumerationEditor;
	protected ColorEditorPanel colorEditor;
	protected AreaReferenceEditorPanel areaReferenceEditor;
	protected PathPanel pathEditor;
	protected ExternalSlotEditorPanel externalSlotEditor;
	
	/**
	 * Current editor reference.
	 */
	private SlotValueEditorPanelInterface currentEditor;

	/**
	 * Is new slotCopy flag.
	 */
	public boolean isNew;
	
	/**
	 * Found attributes reference.
	 */
	public FoundAttr foundAttr;
	
	/**
	 * Auxiliary flag.
	 */
	private boolean programIsSettingSpecialValue = false;
	
	/**
	 * Reference to parent component
	 */
	protected SlotEditorGenerator editor;
	
	/**
	 * Constructor
	 * @param editor
	 */
	public SlotEditorHelper(SlotEditorGenerator editor) {
		
		this.editor = editor;
	}
	
	/**
	 * Load dialog.
	 */
	public void loadDialog() {

		textEditor.setTextFont(fontState);
	}

	/**
	 * Save dialog.
	 */
	protected void saveDialog() {
		
		fontState = textEditor.getTextFont();
	}

	/**
	 * On OK.
	 * @param editor 
	 */
	public void onOk(SlotEditorGenerator editor) {
		
		// Save slotCopy.
		if (!saveSlot(false)) {
			return;
		}

		// Update information.
		ConditionalEvents.transmit(SlotEditorHelper.this, Signal.areaSlotSaved, editedSlot);

		saveDialog();
	}
	
	/**
	 * On cancel.
	 * @param thisEditor 
	 */
	public void onCancel(SlotEditorGenerator thisEditor) {
		
		// Ask user.
		if (isChanged()) {
			if (JOptionPane.showConfirmDialog(editor.getComponent(),
					Resources.getString("org.multipage.generator.messageLoseSlotChanges"))
					!= JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		// Update information.
		long slotId = editedSlot.getId();
		ConditionalEvents.transmit(SlotEditorHelper.this, Signal.cancelSlotEditor, slotId);

		saveDialog();
	}
	
	/**
	 * Remember created panel
	 */
	public void remember() {
		
		if (helpers == null) {
			helpers = new LinkedList<SlotEditorHelper>();
		}
		helpers.add(this);
	}

	/**
	 * Get true value if an editor was reduced.
	 * @return
	 */
	protected boolean isReducedEditor() {
		
		return reducedEditor != null && !ProgramGenerator.isExtensionToBuilder();
	}

	/**
	 * Highlight found texts.
	 */
	public void highlightFound() {
		
		textEditor.highlightFound(foundAttr);
	}
	
	/**
	 * Create editors.
	 * @param useHtmlEditor 
	 */
	public void createEditors(boolean useHtmlEditor) {
		
		textEditor = new TextSlotEditorPanel(Utility.findWindow(editor.getComponent()), useHtmlEditor, editedSlot);
		integerEditor = new IntegerEditorPanel();
		realEditor = new RealEditorPanel();
		booleanEditor = ProgramGenerator.newBooleanEditorPanel();
		enumerationEditor = ProgramGenerator.newEnumerationEditorPanel();
		colorEditor = new ColorEditorPanel();
		areaReferenceEditor = new AreaReferenceEditorPanel();
		pathEditor = new PathPanel();
		externalSlotEditor = new ExternalSlotEditorPanel();
	}
	
	/**
	 * Update editor components.
	 */
	public void updateDialogSlotAndAreaName() {

		SlotHolder holder = editedSlot.getHolder();
		if (holder != null) {
			editor.getTextHolder().setText(holder.toString());
		}
		
		editor.getTextAlias().setText(editedSlot.getNameForGenerator());
	}
	
	/**
	 * Select combo type.
	 * @param type
	 */
	protected void selectComboType(SlotType type) {
		
		// Override this method or leave it unused.
	}
	
	/**
	 * Select editor.
	 * @param type
	 */
	public void selectEditor(SlotType type, boolean setDefaultLanguage) {

		if (initialized) {
			editedSlot.setValue(getValue());
		}
		
		if (type == SlotType.UNKNOWN) {
			type = SlotType.TEXT;
		}
		
		// If it is critical conversion, ask user.
		if (editedSlot.isCriticalCoversion(type)) {
			
			String message = String.format(
					Resources.getString("org.multipage.generator.messageConfirmCriticalSlotValueConversion"),
					editedSlot.getType().toString(), type.toString());
			if (JOptionPane.showConfirmDialog(editor.getComponent(), message)
					!= JOptionPane.YES_OPTION) {
				
				// Select previous type.
				selectComboType(editedSlot.getType());
				return;
			}
		}

		// Reset editor panel.
		editor.getPanelEditor().removeAll();
		// Clear the editors.
		textEditor.clear();
		integerEditor.clear();
		realEditor.clear();
		
		reducedEditor = null;
		
		editedSlot.setLocalized(false);

		switch (type) {
		case LOCALIZED_TEXT:
			editedSlot.setLocalized(true);
		case TEXT:
			currentEditor = textEditor;
			textEditor.setValueMeaning(editedSlot.getValueMeaning());
			textEditor.setValue(editedSlot.getTextValue());
			editor.getPanelEditor().add(textEditor);
			break;
		case ENUMERATION:
			currentEditor = enumerationEditor;
			enumerationEditor.setValue(editedSlot.getEnumerationValue());
			editor.getPanelEditor().add(enumerationEditor);
			reducedEditor = enumerationEditor;
			break;
		case INTEGER:
			currentEditor = integerEditor;
			integerEditor.setValue(editedSlot.getIntegerValue());
			editor.getPanelEditor().add(integerEditor);
			reducedEditor = integerEditor;
			break;
		case REAL:
			currentEditor = realEditor;
			realEditor.setValue(editedSlot.getRealValue());
			editor.getPanelEditor().add(realEditor);
			reducedEditor = realEditor;
			break;
		case BOOLEAN:
			currentEditor = booleanEditor;
			booleanEditor.setValue(editedSlot.getBooleanValue());
			editor.getPanelEditor().add(booleanEditor);
			reducedEditor = booleanEditor;
			break;
		case COLOR:
			currentEditor = colorEditor;
			colorEditor.setValue(editedSlot.getColorValue());
			editor.getPanelEditor().add(colorEditor);
			reducedEditor = colorEditor;
			break;
		case AREA_REFERENCE:
			currentEditor = areaReferenceEditor;
			areaReferenceEditor.setValue(editedSlot.getAreaValue());
			editor.getPanelEditor().add(areaReferenceEditor);
			reducedEditor = areaReferenceEditor;
			break;
		case PATH:
			currentEditor = pathEditor;
			pathEditor.setValue(editedSlot.getTextValue());
			editor.getPanelEditor().add(pathEditor);
			reducedEditor = pathEditor;
			break;
		case EXTERNAL_PROVIDER:
			currentEditor = externalSlotEditor;
			externalSlotEditor.setValue(editedSlot.getTextValue());
			editor.getPanelEditor().add(externalSlotEditor);
			reducedEditor = externalSlotEditor;
			break;
		default:
			currentEditor = null;
			break;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				editor.getComponent().validate();
				editor.getComponent().repaint();
			}
		});
	}
	
	/**
	 * Load current slot. You can override this method to load your own slot data.
	 * @return
	 */
	public Slot loadCurrentSlot() {
		
		updateHeadRevision();
		
		boolean userDefined = editedSlot.isUserDefined();
		
		// Trim alias.
		String alias = userDefined ? editor.getTextAlias().getText() : editedSlot.getAlias();
		SlotHolder holder = editedSlot.getHolder();

		// Create new slot.
		Slot newSlot = new Slot(holder, alias);
		
		long revision = editedSlot.getRevision();
		boolean localizedTextSelected = editor.getCheckLocalizedText().isSelected();
		char access = editedSlot.getAccess();
		boolean hidden = editedSlot.isHidden();
		boolean isDefault = editor.getCheckDefaultValue().isSelected();
		String name = editedSlot.getName();
		boolean preferred = isPreferred();
		String specialValue = getSpecialValueNull();
		String externalProvider = editedSlot.getExternalProvider();
		boolean readsInput = editedSlot.getReadsInput();
		boolean writesOutput = editedSlot.getWritesOutput();
		
		// Get value.
		Object value = getValue();
		String valueMeaning = getValueMeaning();
		
		newSlot.setRevision(revision);
		newSlot.setValue(value);
		newSlot.setValueMeaning(valueMeaning);
		newSlot.setLocalized(value instanceof String
			&& localizedTextSelected);
		newSlot.setAccess(access);
		newSlot.setHidden(hidden);
		newSlot.setDefault(isDefault);
		newSlot.setName(name);
		newSlot.setPreferred(preferred);
		newSlot.setUserDefined(userDefined);
		newSlot.setSpecialValue(specialValue);
		newSlot.setExternalProvider(externalProvider);
		newSlot.setReadsInput(readsInput);
		newSlot.setWritesOutput(writesOutput);
		
		return newSlot;
	}
	
	/**
	 * Returns true value if a value is changed.
	 * @return
	 */
	protected boolean isChanged() {
		
		Slot newSlot = loadCurrentSlot();
		newSlot.resetEmptyText();
		editedSlot.resetEmptyText();
		return !editedSlot.contentEquals(newSlot);
	}

	/**
	 * Get "slot is preferred" flag.
	 * @return
	 */
	protected boolean isPreferred() {
		
		return editedSlot.isPreferred() || editedSlot.isUserDefined();
	}

	/**
	 * Set editor type.
	 */
	public void setEditorType() {
		
		// Select slot type.
		SlotType type = editedSlot.getTypeUseValueMeaning();
		
		// Possibly disable localized text check box.
		if (!SlotType.isText(type)) {
			editor.getCheckLocalizedFlag().setVisible(false);
		}
		
		selectEditor(type, isNew);
	}

	/**
	 * On user localized flag.
	 */
	protected void onUserLocalizedCheck() {
		
		// Set localized text flag.
		if (editedSlot.getType().equals(SlotType.TEXT)
				|| editedSlot.getType().equals(SlotType.LOCALIZED_TEXT)) {
			
			editedSlot.setLocalized(editor.getCheckLocalizedFlag().isSelected());
		}
	}

	/**
	 * On switch on or off debugging
	 * @param buttonSelected 
	 */
	public void onToggleDebugging(boolean buttonSelected) {
		
		if (!enabledEvents) {
			return;
		}
		
		// Set flag
		Settings.setEnableDebugging(buttonSelected);
		
		// Notify other panels
		refresh((SlotEditorHelper helper) -> {
			helper.setDebugging(buttonSelected);
		});
		
		// Notify area trace frames
		AreaTraceFrame.refreshAll((AreaTraceFrame frame) -> {
			frame.setEnableDebugging(buttonSelected);
		});
	}
	
	/**
	 * Enable or disable debugging
	 * @param enabled
	 */
	private void setDebugging(boolean enabled) {
		
		JToggleButton button = editor.getToggleDebug();
		if (button != null) {
			button.setSelected(enabled);
		}
	}
	
	/**
	 * Notify other created helpers
	 * @param callback
	 */
	public static void refreshAll(Consumer<SlotEditorHelper> callback) {
		
		if (callback == null || helpers == null) {
			return;
		}
		
		enabledEvents = false;
		
		for (SlotEditorHelper helper : helpers) {
			if (helper != null) {
				callback.accept(helper);
			}
		}
		
		enabledEvents = true;
	}

	/**
	 * Notify other created helpers
	 * @param callback
	 */
	private void refresh(Consumer<SlotEditorHelper> callback) {
		
		if (callback == null || helpers == null) {
			return;
		}
		
		enabledEvents = false;
		
		for (SlotEditorHelper helper : helpers) {
			if (helper != this) {
				callback.accept(helper);
			}
		}
		
		enabledEvents = true;
	}

	/**
	 * On interpret PHP
	 */
	public void onInterpretPhp() {
		
		if (!enabledEvents) {
			return;
		}
		JCheckBox checkBox = editor.getCheckInterpretPhp();
		if (checkBox == null) {
			return;
		}
		
		final boolean selected = checkBox.isSelected();
		
		// Notify other panels
		refresh((SlotEditorHelper helper) -> {
			helper.setInterpretPhp(selected);
		});
	}
	
	/**
	 * Enable interpret PHP code
	 * @param selected
	 */
	private void setInterpretPhp(boolean selected) {
		
		JCheckBox checkBox = editor.getCheckInterpretPhp();
		if (checkBox != null) {
			checkBox.setSelected(selected);
		}
	}

	/**
	 * On render HTML pages.
	 */
	protected void onRender() {
		
		Component parentComponent = editor instanceof Component ? (Component) editor : null;
		GeneratorMainFrame.getFrame().onRender(parentComponent);
	}
	
	/**
	 * Gets current slot revision
	 * @return
	 */
	private long getCurrentRevision() {
		
		Obj<Long> revision = new Obj<Long>(0L);
		
		MiddleResult result = MiddleResult.UNKNOWN_ERROR;
		try {
			Middle middle = ProgramBasic.loginMiddle();
			result = middle.loadSlotHeadRevision(editedSlot, revision);
		}
		catch (Exception e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		if (result.isNotOK()) {
			result.show(editor.getComponent());
		}
		return revision.ref;
	}

	/**
	 * Save value.
	 * @param newRevision - if this parameter is set, make new revision of the slot
	 */
	protected boolean saveSlot(boolean newRevision) {
		
		// Get current slot
		Slot newSlot = loadCurrentSlot();
		
		Object [] cursorInfo = Utility.starWaitCursor(editor.getComponent());

		boolean localizedTextSelected = editedSlot.isLocalized();
		
		// If this is a builder application, reset empty text to null.
		if (ProgramGenerator.isExtensionToBuilder()) {
			newSlot.resetEmptyText();
		}
		
		Middle middle = ProgramBasic.getMiddle();
		
		// Database login.
		MiddleResult result = middle.login(
				ProgramBasic.getLoginProperties());
		if (result.isOK()) {
			
			// If this is a new slot, insert it.
			if (isNew) {
				result = middle.insertSlot(newSlot);
			}
			// Make new revision of slot or update slot.
			else {
				
				if (newRevision) {
					result = middle.insertSlotRevision(editedSlot, newSlot);
				}
				else {
					result = middle.updateSlot(editedSlot, newSlot, localizedTextSelected);
				}
				if (result.isOK()) {
					
					newSlot.setId(editedSlot.getId());
				}
			}

			// Database logout.
			MiddleResult logoutResult = middle.logout(result);
			if (logoutResult.isNotOK()) {
				result = logoutResult;
			}
		}
		
		// Inform about error.
		if (result.isNotOK()) {
			result.show(editor.getComponent());
			Utility.stopWaitCursor(editor.getComponent(), cursorInfo);
			return false;
		}
		
		// If new slot has been saved, change flag.
		if (isNew) {
			isNew = false;
		}
		
		editedSlot = newSlot;
		originalSlot = newSlot;
		
		// Transmit "area slot saved" signal.
		ConditionalEvents.transmit(SlotEditorHelper.this, Signal.areaSlotSaved, editedSlot);
		
		Utility.stopWaitCursor(editor.getComponent(), cursorInfo);
		
		return true;
	}
	
	/**
	 * Get value.
	 * @return
	 */
	public Object getValue() {
		
		Component [] components = editor.getPanelEditor().getComponents();
		if (components.length == 0) {
			return false;
		}
		SlotValueEditorPanelInterface valueInterface = (SlotValueEditorPanelInterface) components[0];
		Object value = valueInterface.getValue();
		
		return value;
	}
	
	/**
	 * Get value meaning.
	 * @return
	 */
	public String getValueMeaning() {
		
		Component [] components = editor.getPanelEditor().getComponents();
		if (components.length == 0) {
			return null;
		}
		SlotValueEditorPanelInterface valueInterface = (SlotValueEditorPanelInterface) components[0];
		String valueMeaning = valueInterface.getValueMeaning();
		
		return valueMeaning;
	}
	
	/**
	 * Check slot editor type.
	 */
	protected boolean checkSlotEditorType() {
		
		return true;
	}
		
	/**
	 * Update dialog
	 */
	public void updateDialog() {
		
		updateSlotValue();
		updateHeadRevision();
		updateDialogSettings();
	}
	
	/**
	 * Update dialog with a new slot object
	 * @param newSlot
	 */
	public void updateDialog(Slot newSlot) {
		
		this.editedSlot = newSlot;
		updateDialog();
	}

	/**
	 * Update dialog
	 */
	public void updateDialogSettings() {
		
		// Set slot and area name.
		updateDialogSlotAndAreaName();
		
		// Set editor type depending on slot value type.
		setEditorType();
		
		// Highlight found.
		highlightFound();
		
		// Set enumeration slot reference.
		enumerationEditor.setSlot(editedSlot);
		
		// Update default value flag.
		boolean isDefault = editedSlot.isDefault();
		editor.getCheckDefaultValue().setSelected(isDefault);
		processDefaultValue(isDefault);
		setSpecialValueEnabled(!isDefault);
		
		// Initialize special value.
		String specialValue = getEditedSlot().getSpecialValue();
		setSpecialValueControl(specialValue);
		
		if (!isDefault && !specialValue.isEmpty()) {
			processDefaultValue(true);
		}
		
		// Show/hide "inheritable" bottom label.
		if (editor.getLabelInheritable() != null) {
			editor.getLabelInheritable().setVisible(getEditedSlot().isInheritable());
		}
		
		// Do additional creation.
		boolean isUserSlot = getEditedSlot().isUserDefined();
		if (!isUserSlot) {
			editor.getCheckLocalizedFlag().setVisible(false);
		}
		
		// Set flag
		editor.getCheckLocalizedFlag().setSelected(getEditedSlot().isLocalized());
		
		// Enable editor
		editor.getTextAlias().setEditable(isUserSlot);
	}
	
	/**
	 * Update slot value.
	 */
	public void updateSlotValue() {

		Object value = editedSlot.getValue();
		if (value == null) {
			return;
		}
		
		if (!checkSlotEditorType()) {
			return;
		}
	
		// Get slot value editor.
		Component [] components = editor.getPanelEditor().getComponents();
		if (components.length != 1) {
			return;
		}
		SlotValueEditorPanelInterface editor = (SlotValueEditorPanelInterface) components[0];
		
		// Set value.
		editor.setValue(value);
	}
	
	/**
	 * On edit area.
	 */
	public void onEditArea(int tabIdentifier) {
		
		Area area = (Area) editedSlot.getHolder();
		if (area == null) {
			Utility.show(editor.getComponent(), "org.multipage.generator.messageCannotGetSlotArea");
			return;
		}
		
		// Execute area editor.
		AreaEditorFrame.showDialog(null, area, tabIdentifier);
	}
	
	/**
	 * Show slot help.
	 */
	public void onHelp() {
		
		if (editedSlot == null) {
			Utility.show(editor.getComponent(), "org.multipage.basic.messageNoDescriptionForSlot");
			return;
		}
		
		ProgramBasic.showSlotHelp(editor.getComponent(), editedSlot.getId());
	}
	
	/**
	 * On save slot.
	 */
	public void onSave() {
		
		saveSlot(false);
	}
	
	/**
	 * On commit slot changes
	 */
	public void onCommit() {
		
		// Get current slot
		Slot newSlot = loadCurrentSlot();
		
		// If the new slot is equal to original slot, save new revision
		if (newSlot.differs(originalSlot)) {
			
			long current = getCurrentRevision();
			if (Utility.askParam(editor.getComponent(), "org.multipage.generator.messageConfirmRevisionNumber", current + 1)) {
				saveSlot(true);
			}
			return;
		}
		
		// Inform user that the slot was not changed and ask them whether save new revision of the slot anyway
		if (Utility.ask(editor.getComponent(), "org.multipage.generator.messageNoSlotChangesCommitAnyway")) {
			saveSlot(true);
		}
		return;
	}
	
	/**
	 * Updates head revision number of the slot
	 */
	public void updateHeadRevision() {
		
		Obj<Long> headRevision = new Obj<Long>();
		
		// Update current revision number
		MiddleResult result = MiddleResult.UNKNOWN_ERROR;
		try {
			Middle middle = ProgramBasic.loginMiddle();
			result = middle.loadSlotHeadRevision(this.editedSlot, headRevision);
			this.editedSlot.setRevision(headRevision.ref);
		}
		catch (Exception e) {
			result = MiddleResult.sqlToResult(e);
		}
		finally {
			ProgramBasic.logoutMiddle();
		}
		if (result.isNotOK()) {
			result.show(editor.getComponent());
			return;
		}
	}
	
	/**
	 * On revision
	 */
	public void onRevision() {
		
		// Save current slot values
		Slot saved = (Slot) this.editedSlot.clone();
		
		// Get revision identifier and load appropriate slot values
		Revision revision = RevisionsDialog.showDialog(editor.getComponent(), editedSlot,
				(Revision selectedRevision) -> {
			
					// If a revision is selected in the dialog, display revised slot values
					MiddleResult result2 = MiddleResult.UNKNOWN_ERROR;
					try {
						Middle middle = ProgramBasic.loginMiddle();
						result2 = middle.loadRevisedSlot(selectedRevision, this.editedSlot);
						
						// Restore head revision number
						updateDialog();
					}
					catch (Exception e) {
						result2 = MiddleResult.sqlToResult(e);
					}
					finally {
						ProgramBasic.logoutMiddle();
					}
					if (result2.isNotOK()) {
						result2.show(editor.getComponent());
					}
				});
		
		// If cancelled, return old values
		if (revision == null) {
			updateDialog(saved);
			return;
		}
	}
	
	/**
	 * Process default value.
	 * @param isDefault
	 */
	public void processDefaultValue(boolean isDefault) {

		// Set current value editor to default state.
		if (currentEditor != null) {

			// Disable / enable component.
			if (currentEditor instanceof JComponent) {
				
				boolean isSet = false;
				if (currentEditor instanceof TextSlotEditorPanel) {
					isSet = ((TextSlotEditorPanel)currentEditor).setControlsGrayed(isDefault);
				}
				if (!isSet) {
					Utility.enableComponentTree((JComponent) currentEditor, !isDefault);
				}
			}
			
			currentEditor.setDefault(isDefault);
		}
	}
	
	/**
	 * On default value
	 */
	public void onDefaultValue() {
		
		boolean isDefault = editor.getCheckDefaultValue().isSelected();
		setSpecialValueEnabled(!isDefault);
		
		String specialValue = getSpecialValue();
		processDefaultValue(isDefault || !isDefault && !specialValue.isEmpty());
	}
	
	/**
	 * Set special value text control.
	 * @param specialValue
	 */
	public void setSpecialValueControl(String specialValue) {
		
		programIsSettingSpecialValue = true;
		editor.getTextSpecialValue().setText(specialValue);
		SwingUtilities.invokeLater(() -> {
			programIsSettingSpecialValue = false;
		});
	}
	
	/**
	 * Set listeners.
	 */
	public void setListeners() {
		
		Utility.setTextChangeListener(editor.getTextSpecialValue(), () -> {
			onSpecialValueChanged();
		});
		
		// Switch on or off interpreting of PHP code
		ProgramServlet.setInterpretPhpListener(new CallbackNoArg() {
			@Override
			public Object run() {
				JCheckBox checkBox = editor.getCheckInterpretPhp();
				return checkBox == null ? true : checkBox.isSelected();
			}
		});
	}
	
	/**
	 * Set special value enabled.
	 * @param enabled
	 */
	public void setSpecialValueEnabled(boolean enabled) {
		
		editor.getLabelSpecialValue().setEnabled(enabled);
		editor.getTextSpecialValue().setEnabled(enabled);
		editor.getButtonSpecialValue().setEnabled(enabled);
	}
	
	/**
	 * On special value changed.
	 */
	protected void onSpecialValueChanged() {
		
		if (programIsSettingSpecialValue) {
			return;
		}
		
		String specialValue = editor.getTextSpecialValue().getText();
		processDefaultValue(!specialValue.isEmpty());
	}
	
	/**
	 * On select special value.
	 */
	public void onSelectSpecialValue() {
		
		String oldValue = editor.getTextSpecialValue().getText();
		
		String newValue = SpecialValueDialog.showDialog(editor.getComponent(), oldValue);
		if (newValue == null) {
			return;
		}
		
		setSpecialValueControl(newValue);
		
		SwingUtilities.invokeLater(() -> {
			onSpecialValueChanged();
		});
	}
	
	/**
	 * Get special value.
	 * @return
	 */
	protected String getSpecialValueNull() {
		
		String specialValue = editor.getTextSpecialValue().getText();
		if (specialValue.isEmpty()) {
			return null;
		}
		
		return specialValue;
	}
	
	/**
	 * Get special value.
	 * @return
	 */
	protected String getSpecialValue() {
		
		return editor.getTextSpecialValue().getText();
	}
	
	/**
	 * On display home page.
	 */
	public void onDisplayHomePage() {
		
		ConditionalEvents.transmit(this, Signal.monitorHomePage);
	}

	/**
	 * Set title.
	 */
	public void setTitle(JFrame frame) {
		
		frame.setTitle(String.format(
				Resources.getString("org.multipage.generator.textSlotEditor"),
				editedSlot.getHolder().toString(),
				editedSlot.getAlias()));
	}

	/**
	 * Get enumeration editor.
	 * @return
	 */
	public EnumerationEditorPanelBase getEnumerationEditor() {
		
		return enumerationEditor;
	}

	/**
	 * Get edited slot.
	 * @return
	 */
	public Slot getEditedSlot() {
		
		return editedSlot;
	}
	
	/**
	 * On provider properties.
	 */
	public void onSlotProperties() {
		
		// Open slot properties dialog.
		SlotPropertiesDialog.showDialog(editor.getComponent(), editedSlot);
	}
}
