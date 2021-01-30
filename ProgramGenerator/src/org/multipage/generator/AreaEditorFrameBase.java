/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;

import org.multipage.basic.*;
import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

/**
 * @author
 *
 */
public abstract class AreaEditorFrameBase extends JFrame {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Bounds.
	 */
	protected static Rectangle bounds;

	/**
	 * Tab component selection.
	 */
	protected static int tabSelectionState;
	
	/**
	 * Save timer delay in milliseconds.
	 */
	protected static final int saveTimerDelay = 2000;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		bounds = new Rectangle();
		tabSelectionState = 0;
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		Object data = inputStream.readObject();
		if (!(data instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) data;
		
		tabSelectionState = inputStream.readInt();
		
		AreaDependenciesPanel.selectedSubAreas = inputStream.readBoolean();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		outputStream.writeObject(bounds);
		outputStream.writeInt(tabSelectionState);
		outputStream.writeBoolean(AreaDependenciesPanel.selectedSubAreas);
	}

	/**
	 * Block description saving on change.
	 */
	protected static boolean blockDescriptionSaving = false;
	protected static boolean blockAliasSaving = false;
	protected static boolean blockFileNameExtensionSaving = false;
	protected static boolean blockFolderSaving = false;
	
	/**
	 * Edited area.
	 */
	protected Area area;

	/**
	 * Automatic save timers.
	 */
	protected Timer saveDescriptionTimer;
	protected Timer saveAliasTimer;
	protected Timer saveFileNameExtensionTimer;
	protected Timer saveFolderTimer;
	
	/**
	 * Description and alias saved flags.
	 */
	protected boolean savedDescription = true;
	protected boolean savedAlias = true;
	protected boolean savedFileNameExtension = true;
	protected boolean savedFolderName = true;

	/**
	 * Current dialog.
	 */
	protected static AreaEditorFrameBase dialog;

	/**
	 * Listeners.
	 */
	protected ActionListener homeAreaListener;
	protected ActionListener isDisabledListener;

	/**
	 * Tab content lookup table.
	 */
	protected Hashtable<Component, EditorTabActions> tabContentsTable = new Hashtable<Component, EditorTabActions>();
	
	/**
	 * Old tab content.
	 */
	protected EditorTabActions oldTabContent;
	
	/**
	 * Dependencies panel.
	 */
	protected AreaDependenciesPanelBase panelDependencies;
	
	/**
	 * Resources panel.
	 */
	protected AreaResourcesEditor panelResources;

	/**
	 * Parent component reference.
	 */
	protected Component parentComponent;

	/**
	 * Load dialog.
	 */
	protected void loadDialog() {
		
		if (bounds.isEmpty()) {
			// Center dialog.
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		getTabbedPane().setSelectedIndex(tabSelectionState);
		if (tabSelectionState == 0) {
			onTabChanged();
		}
	}

	/**
	 * Save dialog.
	 */
	protected void saveDialog() {
		
		bounds = getBounds();
		tabSelectionState = getTabbedPane().getSelectedIndex();
	}

	/**
	 * Update area description.
	 */
	public void updateAreaDialog() {
		
		// Stop current saving.
		if (saveDescriptionTimer.isRunning()) {
			saveDescriptionTimer.stop();
			getTextDescription().setForeground(Color.BLACK);
		}
		if (saveAliasTimer.isRunning()) {
			saveAliasTimer.stop();
			getTextAlias().setForeground(Color.BLACK);
		}
		if (saveFolderTimer.isRunning()) {
			saveFolderTimer.stop();
			getTextFolder().setForeground(Color.BLACK);
		}
		if (saveFileNameExtensionTimer.isRunning()) {
			saveFileNameExtensionTimer.stop();
			getTextFileName().setForeground(Color.BLACK);
			getTextFileExtension().setForeground(Color.BLACK);
		}
		
		// Update area object.
		Area newAreaObject = ProgramGenerator.getAreasModel().getArea(area.getId());
		if (newAreaObject == null) {
			return;
		}
		area = newAreaObject;

		// Set description.
		blockDescriptionSaving = true;
		getTextDescription().setText(area.getDescription());
		blockDescriptionSaving = false;
		
		// Set alias.
		blockAliasSaving = true;
		getTextAlias().setText(area.getAlias());
		blockAliasSaving = false;
		
		// Set folder.
		blockFolderSaving = true;
		getTextFolder().setText(area.getFolder());
		blockFolderSaving = false;
		
		// Set file name.
		blockFileNameExtensionSaving = true;
		getTextFileName().setText(area.getFileName());
		blockFileNameExtensionSaving = false;
	}
	
	/**
	 * On tab changed.
	 */
	protected void onTabChanged() {
		
		// Save old tab content information.
		if (oldTabContent != null) {
			oldTabContent.onSavePanelInformation();
		}
		
		// Implement information loading and saving.
		Component component = getTabbedPane().getSelectedComponent();
		// Get tab content interface.
		EditorTabActions tabContent = tabContentsTable.get(component);
		if (tabContent != null) {
			
			// Invoke load information.
			tabContent.onLoadPanelInformation();
		}
		
		// Remember tab content.
		oldTabContent = tabContent;
	}
	
	/**
	 * Update dialog.
	 */
	public static void updateDialog() {
		
		if (dialog != null) {
			dialog.updateAreaDialog();
		}
	}

	/**
	 * Get tabbed pane.
	 */
	protected abstract JTabbedPane getTabbedPane();
	
	/**
	 * Get text description.
	 */
	protected abstract JTextField getTextDescription();
	
	/**
	 * Get text alias.
	 */
	protected abstract JTextField getTextAlias();
	
	/**
	 * Get file name text field.
	 */
	protected abstract JTextField getTextFileName();
	
	/**
	 * Get file extension text field.
	 */
	protected abstract JTextField getTextFileExtension();
	
	/**
	 * Get folder text field.
	 */
	protected abstract JTextField getTextFolder();
	
	/**
	 * Get identifier text field.
	 */
	protected abstract JTextField getTextIdentifier();
	
	/**
	 * Get save file button.
	 * @return
	 */
	protected abstract JButton getButtonSaveFileName();

	/**
	 * Get save description button.
	 * @return
	 */
	protected abstract JButton getButtonSaveDescription();
	
	/**
	 * Get save alias button.
	 * @return
	 */
	protected abstract JButton getButtonSaveAlias();
	
	/**
	 * Get close button.
	 * @return
	 */
	protected abstract JButton getButtonClose();
	
	/**
	 * Save folder button.
	 * @return
	 */
	protected abstract JButton getButtonSaveFolder();
	
	/**
	 * Get save button.
	 * @return
	 */
	protected abstract JButton getButtonSave();
	
	/**
	 * Get update button.
	 * @return
	 */
	protected abstract JButton getButtonUpdate();
	
	/**
	 * Get is visible check box.
	 * @return
	 */
	protected abstract JCheckBox getCheckBoxVisible();

	/**
	 * Get is start area check box.
	 * @return
	 */
	protected abstract JCheckBox getCheckBoxHomeArea();
	
	/**
	 * Get identifier label.
	 * @return
	 */
	protected abstract JLabel getLabelIdentifier();
	
	/**
	 * Get area description label.
	 * @return
	 */
	protected abstract JLabel getLabelAreaDescription();
	
	/**
	 * Get area alias label.
	 * @return
	 */
	protected abstract JLabel getLabelAreaAlias();
	
	/**
	 * Get file name label.
	 * @return
	 */
	protected abstract JLabel getLabelFileName();
	
	/**
	 * Get folder label.
	 * @return
	 */
	protected abstract JLabel getLabelFolder();
	
	/**
	 * Get file extension.
	 * @return
	 */
	protected abstract JLabel getLabelFileExtension();
	
	/**
	 * Get disabled button.
	 */
	protected abstract JCheckBox getCheckBoxIsDisabled();
	
	/**
	 * Insert tabs' contents.
	 */
	protected abstract void insertTabsContents();
	
	/**
	 * Insert tab content.
	 * @param component
	 * @param content
	 */
	protected void insertTabContent(JPanel component, Component content) {
		
		component.add(content);
		
		if (content instanceof EditorTabActions) {
			tabContentsTable.put(component, (EditorTabActions) content);
		}
	}
	
	/**
	 * Save current panel information.
	 */
	protected void saveCurrentPanelInformation() {
		
		// Get current tab component.
		Component component = getTabbedPane().getSelectedComponent();
		
		// Get tab content interface.
		EditorTabActions tabContent = tabContentsTable.get(component);
		if (tabContent != null) {
			
			// Invoke save information.
			tabContent.onSavePanelInformation();
		}
	}

	/**
	 * Load current panel information.
	 */
	protected void loadCurrentPanelInformation() {
		
		// Get current tab component.
		Component component = getTabbedPane().getSelectedComponent();
		
		// Get tab content interface.
		EditorTabActions tabContent = tabContentsTable.get(component);
		if (tabContent != null) {
			
			// Invoke load information.
			tabContent.onLoadPanelInformation();
		}
	}

	/**
	 * On update.
	 */
	public void onUpdate() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.messageWouldYouLikeToUpdateChangesLost")) {
			return;
		}
		
		// Update area data.
		updateAreaDialog();
		// Load current panel information.
		loadCurrentPanelInformation();
	}

	/**
	 * On save.
	 */
	protected void onSave() {
		
		// Save current tab.
		saveCurrentPanelInformation();
		
		// Update data.
		long areaId = area.getId();
		ConditionalEvents.transmit(AreaEditorFrameBase.this, Signal.saveArea, areaId);
	}

	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Save current panel information.
		saveCurrentPanelInformation();

		// Stop timers.
		saveDescriptionTimer.stop();
		saveAliasTimer.stop();
		saveFileNameExtensionTimer.stop();
		saveFolderTimer.stop();
		
		// Save description.
		if (!savedDescription) {
			saveDescription();
		}
		// Save alias.
		if (!savedAlias) {
			saveAlias();
		}
		// Save file name.
		if (!savedFileNameExtension) {
			saveFileNameExtension();
		}
		if (!savedFolderName) {
			saveFolder();
		}
		// Save dialog.
		saveDialog();
		// Update data.
		long areaId = area.getId();
		ConditionalEvents.transmit(AreaEditorFrameBase.this, Signal.saveArea, areaId);
		
		// Close the window.
		dispose();
	}
	
	/**
	 * Pre post create.
	 */
	protected void prePostCreate() {
		
		// Insert tabs' contents.
		insertTabsContents();
		// Create listeners.
		createListeners();
		// Set start area check box.
		setStartAreaCheckbox();
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		// Localize dialog.
		localize();
		// Set icons.
		setIcons();
		// Set tool tips.
		setToolTips();
		// Load area description.
		getTextDescription().setText(area.getDescription());
		// Set area alias.
		getTextAlias().setText(area.getAlias());
		// Set area file name.
		getTextFileName().setText(area.getFileName());
		// Set area file extension.
		getTextFileExtension().setText(area.getFileExtension());
		// Set area folder name.
		getTextFolder().setText(area.getFolder());
		// Set title.
		setTitle(getTitle() + " - " + area.toString());
		// Load area identifier.
		getTextIdentifier().setText(String.valueOf(area.getId()));
		// Set area disabled flag.
		if (!area.isReadOnly()) {
			getCheckBoxIsDisabled().setSelected(!area.isEnabled());
		}
		else {
			getCheckBoxIsDisabled().setEnabled(false);
			getCheckBoxIsDisabled().setSelected(false);
		}
		// Set description text field.
		setDescriptionField();
		// Set alias text field.
		setAliasField();
		// Set file name field.
		setFileNameExtensionFields();
		// Set folder name field.
		setFolderNameField();
		// Load dialog.
		loadDialog();
		// Set filename components.
		setFileNameComponents();
		
		panelResources.panelIsReady();
	}

	/**
	 * Set file name components.
	 */
	public void setFileNameComponents() {
		
		boolean enabled;
		
		if (getCheckBoxVisible() != null) {
			enabled = getCheckBoxVisible().isSelected();
		}
		else {
			enabled = area.isVisible();
		}
		
		getLabelFileName().setEnabled(enabled);
		getLabelFileExtension().setEnabled(enabled);
		getTextFileName().setEditable(enabled);
		getTextFileExtension().setEditable(enabled);
		getButtonSaveFileName().setEnabled(enabled);
	}

	/**
	 * Create listeners.
	 */
	public void createListeners() {
		
		homeAreaListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsHomeAreaAction();
			}
		};
		getCheckBoxHomeArea().addActionListener(homeAreaListener);
		
		isDisabledListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsDisabledAction();
			}
		};
		getCheckBoxIsDisabled().addActionListener(isDisabledListener);
	}
	
	/**
	 * Set tool tips.
	 */
	private void setToolTips() {

		getButtonSaveDescription().setToolTipText(
				Resources.getString("org.multipage.generator.tooltipSaveAreaDescription"));
		getButtonSaveAlias().setToolTipText(
				Resources.getString("org.multipage.generator.tooltipSaveAreaAlias"));
		getButtonSaveFileName().setToolTipText(
				Resources.getString("org.multipage.generator.tooltipSaveFileName"));
		getButtonSaveFolder().setToolTipText(
				Resources.getString("org.multipage.generator.tooltipSaveFolder"));
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
		
		getButtonSaveDescription().setIcon(
				Images.getIcon("org/multipage/generator/images/save_icon.png"));
		getButtonSaveAlias().setIcon(
				Images.getIcon("org/multipage/generator/images/save_icon.png"));
		getButtonClose().setIcon(
				Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		getButtonSave().setIcon(
				Images.getIcon("org/multipage/generator/images/save_icon.png"));
		getButtonSaveFileName().setIcon(
				Images.getIcon("org/multipage/generator/images/save_icon.png"));
		getButtonSaveFolder().setIcon(
				Images.getIcon("org/multipage/generator/images/save_icon.png"));
		getButtonUpdate().setIcon(
				Images.getIcon("org/multipage/generator/images/update_icon.png"));
	}

	
	/**
	 * Set description field.
	 */
	private void setDescriptionField() {

		// Set document listener.
		getTextDescription().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onDescriptionChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onDescriptionChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onDescriptionChanged();
			}
		});
		
		// Create timer firing one event.
		saveDescriptionTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDescription();
			}
		});
		
		saveDescriptionTimer.setRepeats(false);
	}

	/**
	 * Set alias text field.
	 */
	private void setAliasField() {

		// Set document listener.
		getTextAlias().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onAliasChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onAliasChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onAliasChanged();
			}
		});
		
		// Create timer firing one event.
		saveAliasTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAlias();
			}
		});
		
		saveAliasTimer.setRepeats(false);
	}

	/**
	 * Set file name extension fields.
	 */
	private void setFileNameExtensionFields() {
		
		// Set document listener.
		DocumentListener listener = new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				onFileNameExtensionChanged();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				onFileNameExtensionChanged();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				onFileNameExtensionChanged();
			}
		};
		getTextFileName().getDocument().addDocumentListener(listener);
		getTextFileExtension().getDocument().addDocumentListener(listener);
		
		// Create timer.
		saveFileNameExtensionTimer = new javax.swing.Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFileNameExtension();
			}
		});
		
	}

	/**
	 * Set folder name field.
	 */
	private void setFolderNameField() {
		
		// Set document listener.
		getTextFolder().getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				onFolderChanged();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				onFolderChanged();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				onFolderChanged();
			}
		});
		
		// Create timer.
		saveFolderTimer = new javax.swing.Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFolder();
			}
		});
	}

	/**
	 * On description changed.
	 */
	protected void onDescriptionChanged() {
		
		if (blockDescriptionSaving) {
			return;
		}
		
		// Set red description color.
		getTextDescription().setForeground(Color.RED);
		// Set flag.
		savedDescription = false;
		// Restart timer.
		saveDescriptionTimer.restart();
	}

	/**
	 * On alias changed.
	 */
	protected void onAliasChanged() {
		
		if (blockAliasSaving) {
			return;
		}
		
		// Set red alias color.
		getTextAlias().setForeground(Color.RED);
		// Set flag.
		savedAlias = false;
		// Restart timer.
		saveAliasTimer.restart();
	}

	/**
	 * On file name or extension changed.
	 */
	protected void onFileNameExtensionChanged() {
		
		if (blockFileNameExtensionSaving) {
			return;
		}
		
		// Set text field red.
		getTextFileName().setForeground(Color.RED);
		getTextFileExtension().setForeground(Color.RED);
		// Set flag.
		savedFileNameExtension = false;
		// Restart timer.
		saveFileNameExtensionTimer.restart();
	}

	/**
	 * On folder changed.
	 */
	protected void onFolderChanged() {
		
		if (blockFolderSaving) {
			return;
		}
		
		// Set text field red.
		getTextFolder().setForeground(Color.RED);
		// Set flag.
		savedFolderName = false;
		// Restart timer.
		saveFolderTimer.restart();
	}

	/**
	 * Localize dialog.
	 */
	protected void localize() {

		Utility.localize(this);
		Utility.localize(getTabbedPane());
		Utility.localize(getLabelIdentifier());
		Utility.localize(getLabelAreaDescription());
		Utility.localize(getButtonClose());
		Utility.localize(getCheckBoxHomeArea());
		Utility.localize(getLabelAreaAlias());
		Utility.localize(getButtonSave());
		Utility.localize(getLabelFileName());
		Utility.localize(getLabelFolder());
		Utility.localize(getButtonUpdate());
		Utility.localize(getLabelFileExtension());
		Utility.localize(getCheckBoxIsDisabled());
	}

	/**
	 * Save description.
	 */
	protected void saveDescription() {
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		// Save area.
		result = middle.updateAreaDescription(login, area, getTextDescription().getText());
		if (result.isNotOK()) {
			getTextDescription().setText("");
			result.show(this);
			return;
		}
		
		// Set text field color.
		getTextDescription().setForeground(Color.BLACK);
		// Set flag.
		savedDescription = true;
		
		updateAreaChanges();
	}

	/**
	 * Save alias.
	 */
	protected void saveAlias() {
		
		// Trim alias.
		String alias = getTextAlias().getText().trim();
		
		// Check alias uniqueness against project root.
		AreasModel model = ProgramGenerator.getAreasModel();
		if (!model.isAreaAliasUnique(alias, area.getId())) {
			
			Utility.show(this, "org.multipage.generator.messageAreaAliasAlreadyExists", alias);
			return;
		}
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		// Save area.
		result = middle.updateAreaAlias(login, area.getId(), alias);
		if (result.isNotOK()) {
			getTextAlias().setText("");
			result.show(this);
			return;
		}

		// Set text field color.
		getTextAlias().setForeground(Color.BLACK);
		// Set flag.
		savedAlias = true;
		
		updateAreaChanges();
	}

	/**
	 * Save file name and extension.
	 */
	protected void saveFileNameExtension() {

		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		result = middle.login(login);
		if (result.isOK()) {
			
			// Trim file name.
			String fileName = getTextFileName().getText().trim();
			long areaId = area.getId();
			
			// Update file name.
			result = middle.updateAreaFileName(areaId, fileName);
			if (result.isOK()) {
				
				// Trim file extension.
				String fileExtension = getTextFileExtension().getText().trim();
				
				// Update file extension.
				result = middle.updateAreaFileExtension(areaId, fileExtension);
			}
			
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			getTextFileName().setText("");
			result.show(this);
		}
		
		// Set text field color.
		getTextFileName().setForeground(Color.BLACK);
		getTextFileExtension().setForeground(Color.BLACK);
		// Set flag.
		savedFileNameExtension = true;
		
		updateAreaChanges();
	}

	/**
	 * Save folder.
	 */
	protected void saveFolder() {
		
		// Trim folder name.
		String folderName = getTextFolder().getText().trim();
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Update folder name.
		result = middle.updateAreaFolderName(login, area.getId(), folderName);
		if (result.isNotOK()) {
			getTextFolder().setText("");
			result.show(this);
		}
		
		// Set text field color.
		getTextFolder().setForeground(Color.BLACK);
		// Set flag.
		savedFolderName = true;
		
		updateAreaChanges();
	}

	/**
	 * Update area changes.
	 */
	private void updateAreaChanges() {
		
		// Update information.
		long areaId = area.getId();
		ConditionalEvents.transmit(AreaEditorFrameBase.this, Signal.updateAreaChanges, areaId);
	}

	/**
	 * On is home area action.
	 */
	protected void onIsHomeAreaAction() {
		
		getCheckBoxHomeArea().removeActionListener(homeAreaListener);

		// Get state.
		boolean isHomeArea = getCheckBoxHomeArea().isSelected();
		
		// If the global area is a home area, inform user and exit.
		if (area.getId() == 0L && !isHomeArea) {
			
			getCheckBoxHomeArea().setSelected(true);
			getCheckBoxHomeArea().addActionListener(homeAreaListener);

			Utility.show(this, "org.multipage.generator.messageCannotResetGlobalAreaStartFlag");
			return;
		}
		
		long areaId = isHomeArea ? area.getId() : 0L;
		
		getCheckBoxHomeArea().setSelected(!isHomeArea);
		
		// Inform user. Let him/her confirm the change.
		if (areaId == 0L) {
			if (JOptionPane.showConfirmDialog(this,
					Resources.getString("org.multipage.generator.messageGlobalAreaSetAsHome")) != JOptionPane.YES_OPTION) {
				
				getCheckBoxHomeArea().addActionListener(homeAreaListener);
				return;
			}
		}
		else {
			if (JOptionPane.showConfirmDialog(this,
					Resources.getString("org.multipage.generator.messageThisAreaSetAsHome")) != JOptionPane.YES_OPTION) {
				
				getCheckBoxHomeArea().addActionListener(homeAreaListener);
				return;
			}
		}
		
		// Set start area.
		MiddleResult result = ProgramBasic.getMiddle().setStartArea(
				ProgramBasic.getLoginProperties(), areaId);
		if (result.isNotOK()) {
			result.show(this);
			
			getCheckBoxHomeArea().addActionListener(homeAreaListener);
			return;
		}
		
		getCheckBoxHomeArea().setSelected(isHomeArea);
		getCheckBoxHomeArea().addActionListener(homeAreaListener);

		// Update information.
		ConditionalEvents.transmit(AreaEditorFrameBase.this, Signal.updateHomeArea, areaId);
	}

	/**
	 * On is disabled action
	 */
	protected void onIsDisabledAction() {
		
		getCheckBoxIsDisabled().removeActionListener(isDisabledListener);
		
		// Get state.
		boolean isDisabled = getCheckBoxIsDisabled().isSelected();
		
		// Save the state
		long areaId = area.getId();
		MiddleResult result = ProgramBasic.getMiddle().setAreaDisabled(
				ProgramBasic.getLoginProperties(), areaId, isDisabled);
		if (result.isNotOK()) {
			result.show(this);
			
			getCheckBoxIsDisabled().addActionListener(isDisabledListener);
			return;
		}
		getCheckBoxIsDisabled().addActionListener(isDisabledListener);

		// Update information.
		ConditionalEvents.transmit(AreaEditorFrameBase.this, Signal.updateAreaIsDisabled);
	}

	/**
	 * Set start area check box.
	 */
	protected void setStartAreaCheckbox() {
		
		Obj<Long> startAreaId = new Obj<Long>();
		
		// Load flag.
		MiddleResult result = ProgramBasic.getMiddle().loadStartAreaId(
				ProgramBasic.getLoginProperties(), startAreaId);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// If the current area is a start area, set the check box.
		getCheckBoxHomeArea().setSelected(area.getId() == startAreaId.ref);
		// A constraint: invisible area cannot be set to home area
		//getCheckBoxIsStartArea().setEnabled(area.isVisible());
	}


	/**
	 * On description enter key.
	 */
	protected void onDescriptionEnter() {

		saveDescription();
	}

	/**
	 * Select tab.
	 * @param tabIndex
	 */
	public void selectTab(final int tabIndex) {
		
		getTabbedPane().setSelectedIndex(tabIndex);
	}

	protected JCheckBox getCheckBoxIsStartArea() {
		// TODO Auto-generated method stub
		return null;
	}
}
