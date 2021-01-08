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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextCaret;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;

import com.maclan.Area;
import com.maclan.AreasModel;
import com.maclan.Middle;
import com.maclan.MiddleResult;

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
	 * Class constants.
	 */
	public static final int areaDescription = 0;
	public static final int areaAlias = 1;
	
	/**
	 * Splitter position.
	 */
	private static int splitterPositionState = 400;
	
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
		// Set the listeners.
		setListeners();
		// Set callback functions.
		setCallbacks();
		// Load dialog.
		loadDialog();
		// Reactivate GUI.
		GeneratorMainFrame.reactivateGui();
		//$hide<<$
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
	 * Set text editor listeners.
	 */
	private void setListeners() {
        
        // Event receivers
        Event.receiver(this, ActionGroup.areaModelChange, action -> {
        	
        	// On areas model updated
        	if (action.foundFor(Event.modelUpdated)) {
        		
        		// Set automatic text update.
        		setAutomaticTextUpdate();
        		
        		// Consider the changes of description and alias
        		onChangeTexts();
        		
        		// Unlock description and alias text boxes
        		lockInputs(false);
        	}
        });
    }
	
	/**
	 * Set automatic text update.
	 */
	private void setAutomaticTextUpdate() {
		
		textDescription.automaticUpdate = true;
		textAlias.automaticUpdate = true;		
	}

	/**
	 * On change description or alias.
	 */
	private void onChangeTexts() {
		
		textDescription.onChangeText();
		textAlias.onChangeText();
	}

	/**
	 * Lock inputs.
	 * @param lock
	 */
	private void lockInputs(boolean lock) {
		
		// Unlock description and alias text boxes
    	textDescription.lockInput(lock);
    	textAlias.lockInput(lock);
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
	 * Update editor.
	 */
	public void updateEditor() {
		
		if (areas == null) {
			return;
		}
		
		// Reload areas.
		LinkedList<Area> newAreas = new LinkedList<Area>();
		AreasModel model = ProgramGenerator.getAreasModel();
		
		for (Area area : areas) {
			Area newArea = model.getArea(area.getId());
			if (newArea != null) {
				newAreas.add(newArea);
			}
		}
		
		areas = newAreas;
		setAreas(areas);
	}
	
	/**
	 * Set area.
	 */
	public void setAreas(LinkedList<Area> areas) {
		
		SwingUtilities.invokeLater(() -> {
			
			// Trim the input list
			LinkedList<Area> selectedAreas;
			if (areas != null) {
				selectedAreas = areas;
			}
			else {
				selectedAreas = new LinkedList<Area>();
			}
			
			// Try to save current changes.
			saveChanges();
			
			// Get number of areas and set the flag
			int areaCount = selectedAreas.size();
			boolean isSingleArea = areaCount == 1;
			
			// Set the lock flag
			boolean lockTextBoxes = !isSingleArea;
			
			// If areas has been changed, display the changes.
			if (!Utility.contentEquals(this.areas, selectedAreas)) {
				
				// Remember the areas
				this.areas = selectedAreas;
				
				// If is single area selected, enable description editing.
				if (isSingleArea) {
					Area area = selectedAreas.getFirst();
					
					// Set texts
					textDescription.setText(area.getDescription());
					textAlias.setText(area.getAlias());
				}
				
				else if (areaCount > 1) {
					
					final String message = Resources.getString("org.multipage.generator.textMultipleAreasSelection");
					
					// Set messages
					textDescription.setText(message);
					textAlias.setText(message);
				}
				
				// Lock/unlock text boxes
				lockInputs(lockTextBoxes);
				
				// Extended method.
				setAreaExtension();
				
				// Set the list of available slots
				panelSlotList.setAreas(selectedAreas);
				
				// Invoke callback function.
				onSetAreas(this.areas);
			}
			
			// Lock/unlock text boxes
			lockInputs(lockTextBoxes);
		});
	}
	
	/**
	 * Set areas callback method.
	 * @param areas
	 */
	protected void onSetAreas(LinkedList<Area> areas) {
		
		// Override this method.
		
	}

	/**
	 * Set area extension.
	 */
	protected void setAreaExtension() {
		
	}

	/**
	 * Save changes.
	 */
	public void saveChanges() {

		// Try to save existing changes.
		textDescription.saveText();
		textAlias.saveText();
	}

	/**
	 * Set callback event functions.
	 */
	public void setCallbacks() {
		
		/**
		 * Save text callback event.
		 */
		BiConsumer<String, Consumer<String>> saveTextEvent = (description, setTextEvent) -> {
			
			j.log("TEXT SAVE description");
			
			// If the areas reference is not set exit the method.
			if (areas == null) {
				return;
			}
			
			// Only one area must be selected.
			if (areas.size() != 1) {
				return;
			}
			
			// Try to save the area description.
			Middle middle = ProgramBasic.getMiddle();
			MiddleResult result;
			Properties login = ProgramBasic.getLoginProperties();
			Area area = areas.getFirst();
			
			// Check area reference.
			if (area == null) {
				return;
			}
			
			// Update description.
			result = middle.updateAreaDescription(login, area, description);
			if (result != MiddleResult.OK) {
				
				// On error reset description and display message.
				setTextEvent.accept("");
				result.show(this);
			}
			
			// Propagate update event
			Event.propagate(AreasPropertiesBase.this, Event.requestUpdateAll);
		};
		
		textDescription.saveTextEvent = saveTextEvent;
		textAlias.saveTextEvent = saveTextEvent;
		
		/**
		 * Enable/disable callback events.
		 */
		textDescription.enableEvent = lock -> {
			
			// Enable or disable  buttons.
			buttonSaveDescription.setEnabled(!lock);
			buttonDeleteText.setEnabled(!lock);
		};
		
		textAlias.enableEvent = lock -> {
			
			// Enable or disable  buttons.
			buttonSaveAlias.setEnabled(!lock);
		};
		
		/**
		 * Get genuine text callback event.
		 */
		textDescription.getGenuineTextEvent = () -> {
			
			// Get area description.
			Area area = areas.getFirst();		
			String text = area.getDescription();
			
			return text;
		};
		
		textAlias.getGenuineTextEvent = () -> {
			
			// Get area alias.
			Area area = areas.getFirst();		
			String text = area.getAlias();
			
			return text;
		};
		
		/**
		 * Reactivate GUI callback event.
		 */
		Runnable reactivateGUI = () -> {
			
			// Reactivate GUI. (Error fix, Swing looses keyboard)
			GeneratorMainFrame.reactivateGui();
			
			j.log("GUI REACTIVATED");
		};
		
		textDescription.focusGained = reactivateGUI;
		textAlias.focusGained = reactivateGUI;
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
		AreaEditor.showDialog(null, areas.getFirst(), tabIdentifier);
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
		Event.propagate(AreasPropertiesBase.this, Event.deleteAreaLocalizedText, areaId);
	}
}
