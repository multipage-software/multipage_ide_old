/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 09-04-2021
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * @author
 *
 */
public abstract class AreaEditorCommonBase {
	
	/**
	 * Text box identifiers.
	 */
	public static final String description = "DESCRIPTION";
	public static final String alias = "ALIAS";
	public static final String folder = "FOLDER";
	public static final String fileName = "FILENAME";
	public static final String fileExtension = "FILEEXTENSION";
	
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
	 * Edited area.
	 */
	protected Area area;
	
	/**
	 * Current dialog.
	 */
	protected static AreaEditorPanelBase dialog;

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
	 * Constructors panel.
	 */
	protected AreaConstructorPanel panelConstructor;

	/**
	 * Parent component reference.
	 */
	protected Component parentComponent;
	
	/**
	 * Dispose lambda function.
	 */
	protected Runnable disposeLambda;
	
	/**
	 * Get title lambda function.
	 */
	protected Supplier<String> getTitleLambda;
	
	/**
	 * Set title lambda function.
	 */
	protected Consumer<String> setTitleLambda;
	
	/**
	 * Set icon lambda function.
	 */
	protected Consumer<BufferedImage> setIconImageLambda;
	
	/**
	 * Get the window; lambda function.
	 */
	protected Supplier<Window> getWindowLambda;
	
	/**
	 * Set frame boundaries; lambda function.
	 */
	protected Consumer<Rectangle> setBoundsLambda;
	
	/**
	 * Get frame boundaries; lambda function.
	 */
	protected Supplier<Rectangle> getBoundsLambda;
	
	/**
	 * Constructor.
	 */
	public AreaEditorCommonBase(Component parentComponent, Area area) {
		
		// Remember editor area.
		this.area = area;
		this.parentComponent = parentComponent;
	}
	
	/**
	 * Load dialog.
	 */
	protected void loadDialog() {
		
		if (bounds.isEmpty()) {
			// Center dialog.
			Utility.centerOnScreen(getWindowLambda.get());
		}
		else {
			setBoundsLambda.accept(bounds);
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
		
		bounds = getBoundsLambda.get();
		tabSelectionState = getTabbedPane().getSelectedIndex();
	}

	/**
	 * Update area description.
	 */
	public void updateAreaDialog() {
		
		// Update area object.
		Area newAreaObject = ProgramGenerator.getArea(area.getId());
		if (newAreaObject == null) {
			return;
		}
		area = newAreaObject;

		// Set description.
		getTextDescription().setText(area.getDescription());
		getTextDescription().setUserObject(area.getId());
		
		// Set alias.
		getTextAlias().setText(area.getAlias());
		getTextAlias().setUserObject(area.getId());
		
		// Set folder.
		getTextFolder().setText(area.getFolder());
		getTextFolder().setUserObject(area.getId());
		
		// Set file name.
		getTextFileName().setText(area.getFileName());
		getTextFileName().setUserObject(area.getId());
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
	protected abstract TextFieldAutoSave getTextDescription();
	
	/**
	 * Get text alias.
	 */
	protected abstract TextFieldAutoSave getTextAlias();
	
	/**
	 * Get file name text field.
	 */
	protected abstract TextFieldAutoSave getTextFileName();
	
	/**
	 * Get file extension text field.
	 */
	protected abstract TextFieldAutoSave getTextFileExtension();
	
	/**
	 * Get folder text field.
	 */
	protected abstract TextFieldAutoSave getTextFolder();
	
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
	 * Save form data.
	 */
	protected void saveData() {
		
		// Save description.
		saveDescription();
		// Save alias.
		saveAlias();
		// Save folder name.
		saveFolder();
		// Save file name.
		saveFileName();
		// Save file extension.
		saveFileExtension();
	}
	
	/**
	 * On update.
	 */
	public void onUpdate() {
		
		// Ask user.
		if (!Utility.ask(getWindowLambda.get(), "org.multipage.generator.messageWouldYouLikeToUpdateChangesLost")) {
			return;
		}
		
		// Update area data.
		updateAreaDialog();
	}

	/**
	 * On save.
	 */
	protected void onSave() {
		
		// Save current panel input.
		saveData();
		
		// Transmit "update all" request.
		ConditionalEvents.transmit(this, Signal.updateAll);
	}

	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Save current panel input.
		saveData();
		
		// Transmit "update all" request.
		ConditionalEvents.transmit(this, Signal.updateAll);

		// Save dialog.
		saveDialog();
		
		// Close the window.
		dispose();
	}
	
	/**
	 * Dispose form.
	 */
	private void dispose() {
		
		if (disposeLambda != null) {
			disposeLambda.run();
		}
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		// Insert tabs' contents.
		insertTabsContents();
		// Create listeners.
		createListeners();
		// Set start area check box.
		setStartAreaCheckbox();
		// Localize dialog.
		localize();
		// Set icons.
		setIcons();
		// Set tool tips.
		setToolTips();
		// Set callback functions
		setCallbacks();
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
		// Load dialog.
		loadDialog();
		// Set filename components.
		setFileNameComponents();
		
		panelResources.panelIsReady();
	}
	
	/**
	 * Get title.
	 * @return
	 */
	private String getTitle() {

		if (getTitleLambda != null) {
			String title = getTitleLambda.get();
			return title;
		}
		return "";
	}
	
	/**
	 * Set title.
	 * @param title
	 */
	public void setTitle(String title) {
		
		if (setTitleLambda != null) {
			setTitleLambda.accept(title);
		}
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
	 * Set icon of the window.
	 * @param image
	 */
	protected void setIconImage(BufferedImage image) {
		
		if (setIconImageLambda != null) {
			setIconImageLambda.accept(image);
		}
	}

	/**
	 * Localize dialog.
	 */
	protected void localize() {
		
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
		
		// Delegate the call.
		String description = getTextDescription().getText().trim();
		saveDescription(area, description);
	}
	
	/**
	 * Save description.
	 * @param area 
	 * @param description
	 */
	protected void saveDescription(Area area, String description) {
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		// Save area description.
		MiddleResult result = middle.updateAreaDescription(login, area, description);
		if (result.isNotOK()) {
			
			result.show(getWindowLambda.get());
			getTextDescription().setText("");
			return;
		}
		
		j.log("SAVED DESCRIPTION %s", description);
	}
	
	/**
	 * Save alias.
	 */
	protected void saveAlias() {
		
		// Delegate the call.
		String alias = getTextAlias().getText().trim();
		saveAlias(area, alias);
	}
	
	/**
	 * Save alias.
	 * @param area 
	 * @param alias
	 */
	protected void saveAlias(Area area, String alias) {
		
		// Check alias uniqueness against project root.
		AreasModel model = ProgramGenerator.getAreasModel();
		if (!model.isAreaAliasUnique(alias, area.getId())) {
			
			Utility.show(getWindowLambda.get(), "org.multipage.generator.messageAreaAliasAlreadyExists", alias);
			return;
		}
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Get area ID.
		long areaId = area.getId();

		// Save area.
		result = middle.updateAreaAlias(login, areaId, alias);
		if (result.isNotOK()) {
			
			result.show(getWindowLambda.get());
			getTextAlias().setText("");
			return;
		}
	}
	
	/**
	 * Save folder.
	 */
	protected void saveFolder() {
		
		// Delegate the call.
		String folder = getTextFolder().getText().trim();
		saveFolder(area, folder);
	}
	
	/**
	 * Save folder.
	 * @param area 
	 * @param folder
	 */
	protected void saveFolder(Area area, String folder) {
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Get area ID.
		long areaId = area.getId();
		
		// Update folder name.
		MiddleResult result = middle.updateAreaFolderName(login, areaId, folder);
		if (result.isNotOK()) {
			
			getTextFolder().setText("");
			result.show(getWindowLambda.get());
			return;
		}
	}
	
	/**
	 * Save file name.
	 */
	protected void saveFileName() {
		
		// Delegate the call.
		String fileName = getTextFileName().getText().trim();
		saveFileName(area, fileName);
	}
	
	/**
	 * Save file name.
	 * @param area 
	 * @param fileName
	 */
	protected void saveFileName(Area area, String fileName) {
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			// Get area ID.
			long areaId = area.getId();
			
			// Update file name.
			result = middle.updateAreaFileName(areaId, fileName);
			
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			
			getTextFileName().setText("");
			result.show(getWindowLambda.get());
			return;
		}
	}
	
	/**
	 * Save file extension.
	 */
	protected void saveFileExtension() {
		
		// Delegate the call.
		String fileExtension = getTextFileExtension().getText().trim();
		saveFileExtension(area, fileExtension);
	}
	
	/**
	 * Save file extension.
	 * @param area 
	 * @param fileExtension
	 */
	protected void saveFileExtension(Area area, String fileExtension) {
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			// Get area ID.
			long areaId = area.getId();
			
			// Update file extension.
			result = middle.updateAreaFileExtension(areaId, fileExtension);
			
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			
			getTextFileExtension().setText("");
			result.show(getWindowLambda.get());
			return;
		}
	}

	
	/**
	 * Set callback event functions.
	 */
	public void setCallbacks() {
		
		/**
		 * Save text lambda.
		 */
		Function<TextFieldAutoSave, Function<Runnable, Consumer<Runnable>>> saveTextEvent = textBox -> onSaveFinished -> onRequestUpdate -> {
			
			// Get current area.
			Object userObject = textBox.getUserObject();
			
			// If the areas reference is not set, exit the method.
			if (userObject instanceof Long) {
				
				// Get text of the text box.
				String text = textBox.getText();
				if (text == null) {
					return;
				}
				
				// Get associated area.
				Long areaId = (Long) userObject;
				Area area = ProgramGenerator.getArea(areaId);
				
				// Update required record.
				if (description.equals(textBox.identifier)) {
					saveDescription(area, text);
				}
				else if (alias.equals(textBox.identifier)) {
					saveAlias(area, text);
				}
				else if (folder.equals(textBox.identifier)) {
					saveFolder(area, text);
				}
				else if (fileName.equals(textBox.identifier)) {
					saveFileName(area, text);
				}
				else if (fileExtension.equals(textBox.identifier)) {
					saveFileExtension(area, text);
				}
				else {
					return;
				}
				
				// Call finished function.
				onSaveFinished.run();
				
				// Call request update function.
				onRequestUpdate.run();
			}
		};
		
		// Set lambda functions.
		getTextDescription().saveTextLambda = saveTextEvent;
		getTextAlias().saveTextLambda = saveTextEvent;
		getTextFolder().saveTextLambda = saveTextEvent;
		getTextFileName().saveTextLambda = saveTextEvent;
		getTextFileExtension().saveTextLambda = saveTextEvent;
		
		/**
		 * Request update.
		 */
		Runnable updateEvent = () -> {
			
			// Disable editing.
			enableEditing(false);
			
			// Propagate update event
			//ConditionalEvents.transmit(AreaEditorCommonBase.this, Signal.updateAll);
		};
		
		// Set lambda functions.
		getTextDescription().updateLambda = updateEvent;
		getTextAlias().updateLambda = updateEvent;
		getTextFolder().updateLambda = updateEvent;
		getTextFileName().updateLambda = updateEvent;
		getTextFileExtension().updateLambda = updateEvent;
		
		/**
		 * Get genuine text callback event.
		 */
		getTextDescription().getGenuineTextLambda = () -> {
			
			// Get area description.	
			String text = area.getDescription();
			if (text == null) {
				return "";
			}
			return text;
		};
		
		getTextAlias().getGenuineTextLambda = () -> {
			
			// Get area alias.
			String text = area.getAlias();
			if (text == null) {
				return "";
			}
			return text;
		};
		
		getTextFolder().getGenuineTextLambda = () -> {
			
			// Get area folder.
			String text = area.getFolder();
			if (text == null) {
				return "";
			}
			return text;
		};

		getTextFileName().getGenuineTextLambda = () -> {
			
			// Get area folder.
			String text = area.getFileName();
			if (text == null) {
				return "";
			}
			return text;
		};
		
		getTextFileExtension().getGenuineTextLambda = () -> {
			
			// Get area folder.
			String text = area.getFileExtension();
			if (text == null) {
				return "";
			}
			return text;
		};
	}
	
	/**
	 * Enable/disable editing.
	 * @param flag
	 */
	public void enableEditing(boolean flag) {
		
		// Enable/disable whole frame.
		GeneratorMainFrame.getFrame().setEnabled(flag);
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

			Utility.show(getWindowLambda.get(), "org.multipage.generator.messageCannotResetGlobalAreaStartFlag");
			return;
		}
		
		long areaId = isHomeArea ? area.getId() : 0L;
		
		getCheckBoxHomeArea().setSelected(!isHomeArea);
		
		// Inform user. Let him/her confirm the change.
		if (areaId == 0L) {
			if (JOptionPane.showConfirmDialog(getWindowLambda.get(),
					Resources.getString("org.multipage.generator.messageGlobalAreaSetAsHome")) != JOptionPane.YES_OPTION) {
				
				getCheckBoxHomeArea().addActionListener(homeAreaListener);
				return;
			}
		}
		else {
			if (JOptionPane.showConfirmDialog(getWindowLambda.get(),
					Resources.getString("org.multipage.generator.messageThisAreaSetAsHome")) != JOptionPane.YES_OPTION) {
				
				getCheckBoxHomeArea().addActionListener(homeAreaListener);
				return;
			}
		}
		
		// Set start area.
		MiddleResult result = ProgramBasic.getMiddle().setStartArea(
				ProgramBasic.getLoginProperties(), areaId);
		if (result.isNotOK()) {
			result.show(getWindowLambda.get());
			
			getCheckBoxHomeArea().addActionListener(homeAreaListener);
			return;
		}
		
		getCheckBoxHomeArea().setSelected(isHomeArea);
		getCheckBoxHomeArea().addActionListener(homeAreaListener);

		// Update information.
		ConditionalEvents.transmit(this, Signal.updateHomeArea, areaId);
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
			result.show(getWindowLambda.get());
			
			getCheckBoxIsDisabled().addActionListener(isDisabledListener);
			return;
		}
		getCheckBoxIsDisabled().addActionListener(isDisabledListener);

		// Update information.
		ConditionalEvents.transmit(this, Signal.updateAreaIsDisabled, areaId);
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
			result.show(getWindowLambda.get());
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
		
		return null;
	}
}
