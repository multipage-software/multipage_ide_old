/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import javax.swing.*;

import java.awt.*;
import java.util.*;

import javax.swing.Timer;
import javax.swing.event.*;

import org.multipage.basic.*;
import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

import java.awt.event.*;
import java.io.*;

/**
 * @author
 *
 */
public class AreasPropertiesBase extends JPanel {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Save timer delay in milliseconds.
	 */
	private static final int saveTimerDelay = 2000;
		
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
	
	/**
	 * A flag that indicates automatic update of the description performed by the software.
	 */
	private boolean automaticDescriptionUpdate = false;
	
	/**
	 * A flag that indicates automatic update of the alias performed by the software.
	 */
	private boolean automaticAliasUpdate = false;

	/**
	 * Automatic save timers.
	 */
	private Timer saveDescriptionTimer;
	private Timer saveAliasTimer;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelAreaDescription;
	private JTextField textDescription;
	private JButton buttonSaveDescription;
	private JMenu menuArea;
	private JMenuItem menuEditResources;
	private JButton buttonDeleteText;
	private JSplitPane splitPane;
	protected SlotListPanel panelSlotList;
	private JLabel labelAreaAlias;
	protected JTextField textAlias;
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
			JTextField textDescription,
			JButton buttonSaveArea,
			JMenu menuArea,
			JMenuItem menuEditResources,
			JButton buttonDeleteText,
			JSplitPane splitPane,
			SlotListPanel panelSlotList,
			JLabel labelAreaAlias,
			JTextField textAlias,
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
		// Set save timers.
		setSaveTimers();
		// Load dialog.
		loadDialog();
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
		
		// Set text boxes content change
        textDescription.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChangeDescription();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeDescription();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeDescription();
			}
        });

        textAlias.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChangeAlias();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeAlias();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeAlias();
			}
        });
        
        // Event receivers
        Event.receiver(this, EventGroup.areaModelChange, action -> {
        	
        	// On areas model updated
        	if (action.foundFor(Event.modelUpdated)) {
        		
        		// Unlock description and alias text boxes
        		lockDescription(false);
        		lockAlias(false);
        		
        		// Consider the changes of description and alias
        		onChangeDescription();
        		onChangeAlias();
        	}
        });
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
	 * Set description text box.
	 * @param text
	 */
	private void setDescription(String text) {
		
		// Set flag
		automaticDescriptionUpdate = true;
		
		// Set text box
		textDescription.setText(text);
	}
	
	/**
	 * Set alias text box.
	 * @param text
	 */
	private void setAlias(String text) {
		
		// Set flag
		automaticAliasUpdate = true;
		
		// Set text box
		textAlias.setText(text);
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
			saveDescription();
			saveAlias();
			
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
					setDescription(area.getDescription());
					setAlias(area.getAlias());
				}
				
				else if (areaCount > 1) {
					
					final String message = Resources.getString("org.multipage.generator.textMultipleAreasSelection");
					
					// Set messages
					setDescription(message);
					setAlias(message);
				}
				
				// Lock/unlock text boxes
				lockDescription(lockTextBoxes);
				lockAlias(lockTextBoxes);
				
				// Extended method.
				setAreaExtension();
				
				// Set the list of available slots
				panelSlotList.setAreas(selectedAreas);
				
				// Invoke callback function.
				onSetAreas(this.areas);
			}
			
			// Lock/unlock text boxes
			lockDescription(lockTextBoxes);
			lockAlias(lockTextBoxes);
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
	public void setSaveChanges() {

		// Try to save existing changes.
		saveDescription();
		saveAlias();
	}
	
	/**
	 * Lock description text box input
	 */
	private void lockDescription(boolean lock) {
		
		j.log("LOCK DESCRIPTION = %b", lock);
		j.printStackTrace("LOCK DESCRIPTION");
		
		// Enable/disable the text box
		textDescription.setEnabled(!lock);
		textDescription.setBackground(Color.WHITE);
		
		// Enable/disable the buttons
		buttonSaveDescription.setEnabled(!lock);
		buttonDeleteText.setEnabled(!lock);
	}
	
	/**
	 * Lock alias text box input
	 * @param lock
	 */
	private void lockAlias(boolean lock) {
		
		j.log("LOCK ALIAS = %b", lock);
		j.printStackTrace("LOCK ALIAS");
		
		// Enable/disable the text box
		textAlias.setEnabled(!lock);
		textAlias.setBackground(Color.WHITE);
		
		// Enable/disable the button
		buttonSaveAlias.setEnabled(!lock);
	}

	/**
	 * Save description.
	 */
	public void saveDescription() {
		
		// If the areas reference is not set exit the method.
		if (areas == null) {
			return;
		}
		
		// Only one area must be selected.
		if (areas.size() != 1) {
			return;
		}

		// If the description changes...
		if (isAreaDescriptionChangedByUser()) {
			
			j.log("TEXT SAVE description");
			
			// Try to save the area description.
			Middle middle = ProgramBasic.getMiddle();
			MiddleResult result;
			Properties login = ProgramBasic.getLoginProperties();
			Area area = areas.getFirst();
			String description = textDescription.getText();
			
			// Check area reference.
			if (area == null) {
				return;
			}
			
			result = middle.updateAreaDescription(login, area, description);
			if (result != MiddleResult.OK) {
				setDescription("");
				result.show(this);
			}
			
			// Remove highlight
			textDescription.setForeground(Color.BLACK);

			// Lock the text box
			lockDescription(true);

			// Propagate update event
			Event.propagate(AreasPropertiesBase.this, Event.requestUpdateAll);
		}
	}

	/**
	 * Save alias.
	 */
	public void saveAlias() {
		
		// If the areas reference is not set exit the method.
		if (areas == null) {
			return;
		}
		
		// Only one area must be selected.
		if (areas.size() != 1) {
			return;
		}

		// If the description changes...
		if (isAreaAliasChangedByUser()) {
			
			j.log("TEXT SAVE alias");
			
			// Get new area alias.
			Area area;
			try {
				area = areas.getFirst();
			}
			catch (Exception e) {
				return;
			}
			
			long areaId = area.getId();
			
			String alias = textAlias.getText();

			// Check alias uniqueness against project root.
			AreasModel model = ProgramGenerator.getAreasModel();
			if (!model.isAreaAliasUnique(alias, areaId)) {
				
				Utility.show(this, "org.multipage.generator.messageAreaAliasAlreadyExists", alias);
				return;
			}
			
			// Try to save the area description.
			Middle middle = ProgramBasic.getMiddle();
			MiddleResult result;
			Properties login = ProgramBasic.getLoginProperties();
			
			result = middle.updateAreaAlias(login, areaId, alias);
			if (result != MiddleResult.OK) {
				setAlias("");
				result.show(this);
			}
			else {
				area.setAlias(alias);
			}
			
			// Remove highlight
			textAlias.setForeground(Color.BLACK);
			
			// Lock the text box
			lockAlias(true);

			// Decline change text events.
			Event.propagate(AreasPropertiesBase.this, Event.requestUpdateAll, areaId);
		}
	}

	/**
	 * Returns true value if the area description changes.
	 * @return
	 */
	private boolean isAreaDescriptionChangedByUser() {
		
		// Check the flag
		if (automaticDescriptionUpdate) {
			return false;
		}
		
		// Find out description changes
		try {
			Area area = areas.getFirst();		
			String text = textDescription.getText();
			
			return text.compareTo(area.getDescription()) != 0;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns true value if the area alias changes.
	 * @return
	 */
	private boolean isAreaAliasChangedByUser() {
		
		// Check the flag
		if (automaticAliasUpdate) {
			return false;
		}
		
		// Find out alias changes
		try {
			Area area = areas.getFirst();
			String text = textAlias.getText();
		
			return text.compareTo(area.getAlias()) != 0;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Set save timer.
	 */
	private void setSaveTimers() {

		// Create timer firing one event.
		saveDescriptionTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDescription();
			}
		});
				
		saveDescriptionTimer.setInitialDelay(saveTimerDelay);
		saveDescriptionTimer.setRepeats(false);
		
		// Create timer firing one event.
		saveAliasTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAlias();
			}
		});
		
		saveAliasTimer.setInitialDelay(saveTimerDelay);
		saveAliasTimer.setRepeats(false);
		
	}

	/**
	 * On change description.
	 */
	protected void onChangeDescription() {
		
		j.printStackTrace("CHANGE DESCRIPTION");
		
		Color color;
	
		// If the current area description is not equal to loaded area
		// description set red text color.
		if (isAreaDescriptionChangedByUser()) {
			color = Color.RED;
		
			// Start save timer.
			saveDescriptionTimer.restart();
		}
		else {
			color = Color.black;
			
			// End of automatic sequence
			automaticDescriptionUpdate = false;
		}
		
		textDescription.setForeground(color);
	}

	/**
	 * On change alias.
	 */
	protected void onChangeAlias() {
		
		j.printStackTrace("CHANGE ALIAS");
		
		Color color;
	
		// If the current area alias is not equal to loaded area
		// alias set red text color.
		if (isAreaAliasChangedByUser()) {
			color = Color.RED;
		
			// Start save timer.
			saveAliasTimer.restart();
		}
		else {
			color = Color.black;
			
			// End of automatic sequence
			automaticAliasUpdate = false;
		}
		
		textAlias.setForeground(color);
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

	/**
	 * On description key input.
	 */
	protected void onDescriptionKey() {
		
		j.printStackTrace("DESCRIPTION KEY");
		
		// Reset the flag
		automaticDescriptionUpdate = false;
	}
	
	/**
	 * On alias key input.
	 */
	protected void onAliasKey() {
		
		j.printStackTrace("ALIAS KEY");
		
		// Reset the flag
		automaticAliasUpdate = false;
	}
}
