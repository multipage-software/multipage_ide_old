/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class AreasPropertiesBase extends JPanel {
	
	//$hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Splitter position.
	 */
	private static int splitterPositionState = 400;
	
	/**
	 * Constants.
	 */
	public static final String description = "DESCRIPTION";
	public static final String alias = "ALIAS";
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		splitPane.setDividerLocation(splitterPositionState);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		panelSlotList.saveDialog();
		splitterPositionState = splitPane.getDividerLocation();
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load splitter position.
		splitterPositionState = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		// Save splitter position.
		outputStream.writeInt(splitterPositionState);
	}
	
	/**
	 * Is properties panel flag.
	 */
	public boolean isPropertiesPanel;

	/**
	 * Area nodes.
	 */
	protected LinkedList<Area> areas;
	
	//$hide<<$
	/**
	 * Components.
	 */
	private JLabel labelAreaDescription;
	private TextFieldAutoSave textDescription;
	private JButton buttonSaveDescription;
	private JMenu menuArea;
	private JMenuItem menuEditResources;
	private JButton buttonDeleteText;
	private JSplitPane splitPane;
	protected SlotListPanel panelSlotList;
	private JLabel labelAreaAlias;
	protected TextFieldAutoSave textAlias;
	private JButton buttonSaveAlias;
	private JPanel panelExtension;
	private JMenuItem menuEditDependencies;
	private JMenuItem menuAreaEdit;
	
	/**
	 * Set components references.
	 * @param menuEditDependencies 
	 * @param menuAreaEdit 
	 */
	protected void setComponentsReferences(
			JLabel labelAreaDescription,
			TextFieldAutoSave textDescription,
			JButton buttonSaveArea,
			JMenu menuArea,
			JMenuItem menuEditResources,
			JButton buttonDeleteText,
			JSplitPane splitPane,
			SlotListPanel panelSlotList,
			JLabel labelAreaAlias,
			TextFieldAutoSave textAlias,
			JButton buttonSaveAlias,
			JPanel panelExtension,
			JMenuItem menuEditDependencies,
			JMenuItem menuAreaEdit) {
		
		this.labelAreaDescription = labelAreaDescription;
		this.textDescription = textDescription;
		this.buttonSaveDescription = buttonSaveArea;
		this.menuArea = menuArea;
		this.menuEditResources = menuEditResources;
		this.buttonDeleteText = buttonDeleteText;
		this.splitPane = splitPane;
		this.panelSlotList = panelSlotList;
		this.labelAreaAlias = labelAreaAlias;
		this.textAlias = textAlias;
		this.buttonSaveAlias = buttonSaveAlias;
		this.panelExtension = panelExtension;
		this.menuEditDependencies = menuEditDependencies;
		this.menuAreaEdit = menuAreaEdit;
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		//$hide>>$
		// Add programs list.
		if (!postCreateExtension(this, panelExtension)) {
			splitPane.setRightComponent(null);
			splitPane.setDividerSize(0);
		}
		
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Set tool tips.
		setToolTips();
		// Set callback functions.
		setCallbacks();
		// Set listeners.
		setListeners();
		// Load dialog.
		loadDialog();
		// Reactivate GUI.
		GeneratorMainFrame.reactivateGui();
		//$hide<<$
	}
	
	/**
	 * Get areas.
	 * @param areaIds 
	 * @return
	 */
	private LinkedList<Area> getAreas() {
		
		return this.areas;
	}
	
	/**
	 * Set slot selected event
	 */
	public void setSlotSelectedEvent(SlotSelectedEvent slotSelectedEvent) {
		
		panelSlotList.setSlotSelectedEvent(slotSelectedEvent);
	}
	
	/**
	 * Post creation of dynamic.
	 * @param panel 
	 * @param areasProperties 
	 * @param panelExtension 
	 * @return
	 */
	protected boolean postCreateExtension(AreasPropertiesBase areasProperties,
			JPanel panelExtension) {
		
		return false;
	}
	
	/**
	 * Dispose.
	 */
	public void dispose() {
		
		saveDialog();
	}

	/**
	 * Set icons.
	 */
	protected void setIcons() {
		
		buttonSaveDescription.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
		buttonDeleteText.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
		buttonSaveAlias.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
	}
	
	/**
	 * Set tool tips.
	 */
	protected void setToolTips() {

        buttonSaveDescription.setToolTipText(Resources.getString("org.multipage.generator.tooltipSaveAreaDescription"));
        buttonDeleteText.setToolTipText(Resources.getString("org.multipage.generator.tooltipDeleteLocalizedText"));
        buttonSaveAlias.setToolTipText(Resources.getString("org.multipage.generator.tooltipSaveAreaAlias"));
	}

	/**
	 * Localize components.
	 */
	protected void localize() {

		Utility.localize(labelAreaDescription);
		Utility.localize(menuArea);
		Utility.localize(menuEditResources);
		Utility.localize(labelAreaAlias);
		Utility.localize(menuEditDependencies);
		Utility.localize(menuAreaEdit);
	}
	
	/**
	 * Set area.
	 */
	public void setAreas(LinkedList<Area> areas) {
		
		SwingUtilities.invokeLater(() -> {
			
			// Get selected areas' list.
			LinkedList<Area> selectedAreas;
			if (areas != null) {
				selectedAreas = Area.trim(areas);
			}
			else {
				selectedAreas = new LinkedList<Area>();
			}
			
			// Get number of selected areas.
			int areaCount = selectedAreas.size();
			
			// Remember the selected areas.
			this.areas = selectedAreas;
			
			// Try to save unsaved changes in description and/or alias.
			if (!textDescription.state.isSaved()) {
				
				Object userObject = textDescription.getUserObject();
				if (userObject instanceof Long) {
					
					textDescription.saveText();
				}
			}
			if (!textAlias.state.isSaved()) {
				
				Object userObject = textDescription.getUserObject();
				if (userObject instanceof Long) {
					
					textAlias.saveText();
				}
			}
			
			// Initialize user objects.
			textDescription.setUserObject(null);
			textAlias.setUserObject(null);
			
			// If is single area selected, enable description editing.
			if (areaCount == 1) {
				
				// Enable text boxes.
				textDescription.setEnabled(true);
				textAlias.setEnabled(true);
				
				// Get area and its ID.
				Area area = selectedAreas.getFirst();
				long areaId = area.getId();
				
				// Set user object of text boxes to this area.
				textDescription.setUserObject(areaId);
				textAlias.setUserObject(areaId);
				
				// Get description and alias. Trim the texts.
				String description = area.getDescription();
				String alias = area.getAlias();
				
				if (description == null) {
					description = "";
				}
				if (alias == null) {
					alias = "";
				}
				
				// Update description, alias.
				textDescription.setText(description);
				textAlias.setText(alias);
			}
			else if (areaCount > 1) {
				
				final String message = Resources.getString("org.multipage.generator.textMultipleAreasSelection");
				
				// Display message.
				textDescription.setMessage(message);
				textAlias.setMessage(message);
			}
			
			// Extended method.
			setAreaExtension();
			
			// Set the list of available slots
			panelSlotList.setAreas(selectedAreas);
		});
	}

	/**
	 * Set area extension.
	 */
	protected void setAreaExtension() {
		
	}

	/**
	 * Save description changes.
	 */
	public void saveDescriptionChanges() {

		// Try to save existing changes.
		textDescription.saveText();
	}
	
	/**
	 * Save alias changes.
	 */
	public void saveAliasChanges() {

		// Try to save existing changes.
		textAlias.saveText();
	}

	/**
	 * Set callback event functions.
	 */
	public void setCallbacks() {
		
		/**
		 * Save text callback event.
		 */
		Function<TextFieldAutoSave, Function<Runnable, Consumer<Runnable>>> saveTextEvent = textBox -> onSaveFinished -> onRequestUpdate -> {
			
			// Get current area.
			Object userObject = textBox.getUserObject();
			
			// If the areas reference is not set, exit the method.
			if (userObject instanceof Long) {
				
				// Try to get area.
				Long areaId = (Long) userObject;
				Area area = ProgramGenerator.getArea(areaId);
	
				// Try to save the area description or alias.
				Middle middle = ProgramBasic.getMiddle();
				MiddleResult result = MiddleResult.OK;
				Properties login = ProgramBasic.getLoginProperties();
				
				// Initialize the flag.
				boolean saving = false;
				
				// Get text box text.
				String text = null;
				
				// Update description or alias.
				if (AreasPropertiesBase.description.equals(textBox.identifier)) {
					
					if (textBox.isTextChangedByUser()) {
						
						text = textBox.getText();
						
						if (text != null) {
							result = middle.updateAreaDescription(login, area, text);
							saving = true;
						}
					}
				}
				else if (AreasPropertiesBase.alias.equals(textBox.identifier))  {
					
					if (textBox.isTextChangedByUser()) {
						
						text = textBox.getText();
						
						if (text != null) {
							result = middle.updateAreaAlias(login, area, text);
							saving = true;
						}
					}
				}
				
				// On error display a message.
				if (result != MiddleResult.OK) {
					result.show(this);
				}
				
				// On save event...
				if (saving) {
					
					// Propagate update event
					ConditionalEvents.transmit(AreasPropertiesBase.this, Signal.updateAll);
					
					// Call finished function on the event thread.
					ConditionalEvents.invokeLater(() -> onSaveFinished.run());
				}
			}
		};
		
		textDescription.saveTextLambda = saveTextEvent;
		textAlias.saveTextLambda = saveTextEvent;
		
		/**
		 * Request update.
		 */
		Runnable updateEvent = () -> {
			
			// Propagate update event
			ConditionalEvents.transmit(AreasPropertiesBase.this, Signal.updateAll);
		};
		
		textDescription.updateLambda = updateEvent;
		textAlias.updateLambda = updateEvent;
		
		/**
		 * Get genuine text callback event.
		 */
		textDescription.getGenuineTextLambda = () -> {
			
			// Get current areas.
			LinkedList<Area> areas = getAreas();
			
			// Get area description.
			Area area = areas.getFirst();		
			String text = area.getDescription();
			return text;
		};
		
		textAlias.getGenuineTextLambda = () -> {
			
			// Get current areas.
			LinkedList<Area> areas = getAreas();
			
			// Get area alias.
			Area area = areas.getFirst();		
			String text = area.getAlias();
			return text;
		};
		
		/**
		 * Reactivate GUI callback event.
		 */
		Runnable reactivateGUI = () -> {
			
			// Reactivate GUI (Error fix when Swing looses keyboard input).
			GeneratorMainFrame.reactivateGui();
		};
		
		textDescription.focusGainedLambda = reactivateGUI;
		textAlias.focusGainedLambda = reactivateGUI;
	}
	
	/**
	 * Set listeners.
	 */
	private void setListeners() {
				
		// "Model updated" event receiver.
		ConditionalEvents.receiver(AreasPropertiesBase.this, Signal.updateAll, message -> {
	    	
			// Disable the signal temporarily.
			Signal.updateAll.disable();
			
			// Update the list of areas.
			areas = ProgramGenerator.getUpdatedAreas(areas);
			
			// Set the areas.
			setAreas(areas);
			
			// Enable editing in the main frame window.
			enableEditing(true);
			
			// Enable the signal.
			SwingUtilities.invokeLater(() -> {
				Signal.updateAll.enable();
			});
		});
	}
	
	/**
	 * On edit area.
	 */
	protected void onEditArea(int tabIdentifier) {
		
		// If it is not selected exactly one area, inform user
		// and exit the method.
		if (areas.size() != 1) {
			JOptionPane.showMessageDialog(this,
					Resources.getString("org.multipage.generator.textSelectOnlyOneArea"));
			return;
		}
		
		// Execute area editor.
		AreaEditorFrame.showDialog(null, areas.getFirst(), tabIdentifier);
	}

	/**
	 * On delete local text.
	 */
	protected void onDeleteLocalText() {
		
		// Only one area must be selected.
		if (areas.size() != 1) {
			return;
		}
		
		// Ask user.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.generator.messageDeleteTextInCurrentLanguage"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		// Get area.
		Area area = areas.getFirst();
		long areaId = area.getId();
		
		Middle middle = ProgramBasic.getMiddle();
		
		// Database login.
		MiddleResult result = middle.login(ProgramBasic.getLoginProperties());
		if (result.isOK()) {
			
			Obj<Long> descriptionId = new Obj<Long>();
			// Get description ID.
			result = middle.loadAreaDescriptionId(areaId, descriptionId);
			if (result.isOK()) {
				
				// Remove local text.
				result = middle.removeCurrentLanguageText(descriptionId.ref);
			}
			
			// Database logout.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update information.
		ConditionalEvents.transmit(AreasPropertiesBase.this, Signal.deleteAreaLocalizedText, areaId);
	}
	
	/**
	 * Enable/disable editing.
	 * @param flag
	 */
	public void enableEditing(boolean flag) {
		
		// Enable/disable whole frame.
		GeneratorMainFrame.getFrame().setEnabled(flag);
	}
}
