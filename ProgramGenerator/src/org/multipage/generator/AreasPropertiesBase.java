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
	public AreasPropertiesBase() {
	}


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
	 * Decline text change events.
	 */
	public static boolean declineTextChangeEvents = false;
	
	/**
	 * Is properties panel flag.
	 */
	public boolean isPropertiesPanel;

	/**
	 * Area nodes.
	 */
	protected LinkedList<Area> areas;

	/**
	 * Automatic save timers.
	 */
	private Timer saveDescriptionTimer;
	private Timer saveAliasTimer;

	/**
	 * Description and alias focus.
	 */
	private boolean isDescriptionFocus = false;
	private boolean isAliasFocus = false;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelAreaDescription;
	private JTextField textDescription;
	private JButton buttonSaveArea;
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
		this.buttonSaveArea = buttonSaveArea;
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
		
		// Set text editor listeners.
		setTextEditorListeners();
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Post initialize.
		postInitialize();
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
	private void setTextEditorListeners() {
		
		textDescription.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				isDescriptionFocus = true;
			}
			@Override
			public void focusLost(FocusEvent e) {
				isDescriptionFocus = false;
			}
		});
		textAlias.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				isAliasFocus  = true;
			}
			@Override
			public void focusLost(FocusEvent e) {
				isAliasFocus = false;
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
		
		buttonSaveArea.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
		buttonDeleteText.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
		buttonSaveAlias.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
	}

	/**
	 * Post initialize.
	 */
	private void postInitialize() {

		// Set tool tips.
		setToolTips();

        // Listen for changes in the text field.
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

        // Listen for changes in the text field.
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
    }

	/**
	 * Set tool tips.
	 */
	protected void setToolTips() {

        buttonSaveArea.setToolTipText(Resources.getString("org.multipage.generator.tooltipSaveAreaDescription"));
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
		
		if (areas == null) {
			areas = new LinkedList<Area>();
		}
		
		// Try to save existing changes.
		if (!declineTextChangeEvents) {
			saveDescription();
			saveAlias();
		}
		
		// If areas has not been changed, exit function.
		if (Utility.contentEquals(this.areas, areas)) {
			return;
		}
		
		int areaCount = areas.size();
		
		this.areas = areas;
		
		// If is single area selected, enable description editing.
		if (areaCount == 1) {
			Area area = areas.getFirst();
			
			textDescription.setForeground(Color.BLACK);
			textDescription.setEnabled(true);
			textDescription.setText(area.getDescription());

			textAlias.setForeground(Color.BLACK);
			textAlias.setEnabled(true);
			
			textAlias.setText(area.getAlias());
		}
		else if (areaCount > 1) {
			final Color grayedColor = Color.LIGHT_GRAY;
			final String message = Resources.getString("org.multipage.generator.textMultipleAreasSelection");
			
			textDescription.setForeground(grayedColor);
			textDescription.setEnabled(false);
			textDescription.setText(message);
			textAlias.setEnabled(false);
			textAlias.setForeground(grayedColor);
			textAlias.setText(message);
		}
		
		// Extended method.
		setAreaExtension();

		panelSlotList.setAreas(areas);
		
		// Invoke callback function.
		onSetAreas(this.areas);
		
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
	 * Set area description.
	 * @param description
	 */
	public void setAreaDescription(String description) {
		
		if (areas == null) {
			return;
		}
		Area area = null;
		try {
			area = areas.getFirst();
		}
		catch (Exception e) {
		}
		if (area != null) {
			area.setDescription(description);
		}

		textDescription.setText(description);
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
		if (isAreaDescriptionChanged()) {
			
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
				textDescription.setText("");
				result.show(this);
			}

			textDescription.setForeground(Color.BLACK);
			boolean oldFocus = isDescriptionFocus;
			int oldCaret = textDescription.getCaretPosition();
			
			// Decline change text events.
			AreasProperties.declineTextChangeEvents = true;
			long areaId = area.getId();
			Event.propagate(AreasPropertiesBase.this, Event.updateAreaDescription, areaId);
			
			// Set focus.
			if (oldFocus) {
				textDescription.requestFocus();
				try {
					textDescription.setCaretPosition(oldCaret);
				}
				catch (IllegalArgumentException e) {
				}
			}
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
		if (isAreaAliasChanged()) {
			
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
				textAlias.setText("");
				result.show(this);
			}
			else {
				area.setAlias(alias);
			}
			
			textAlias.setForeground(Color.BLACK);
			
			boolean oldFocus = isAliasFocus;
			int oldCaret = textAlias.getCaretPosition();
			
			// Decline change text events.
			AreasProperties.declineTextChangeEvents = true;
			Event.propagate(AreasPropertiesBase.this, Event.saveAreaAlias, areaId);
			
			// Set focus.
			if (oldFocus) {
				textAlias.requestFocus();
				textAlias.setCaretPosition(oldCaret);
			}
		}
	}

	/**
	 * Returns true value if the area description changes.
	 * @return
	 */
	private boolean isAreaDescriptionChanged() {
		
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
	private boolean isAreaAliasChanged() {

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
		
		// Create timer firing one event.
		saveAliasTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAlias();
			}
		});
		
		saveDescriptionTimer.setRepeats(false);
		saveAliasTimer.setRepeats(false);
	}

	/**
	 * On change description.
	 */
	protected void onChangeDescription() {
		
		if (declineTextChangeEvents) {
			return;
		}

		Color color;
	
		// If the current area description is not equal to loaded area
		// description set red text color.
		if (isAreaDescriptionChanged()) {
			color = Color.RED;
		
			// Start save timer.
			saveDescriptionTimer.restart();
		}
		else {
			color = Color.black;
		}
		
		textDescription.setForeground(color);
	}

	/**
	 * On change alias.
	 */
	protected void onChangeAlias() {
		
		if (declineTextChangeEvents) {
			return;
		}

		Color color;
	
		// If the current area alias is not equal to loaded area
		// alias set red text color.
		if (isAreaAliasChanged()) {
			color = Color.RED;
		
			// Start save timer.
			saveAliasTimer.restart();
		}
		else {
			color = Color.black;
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
		
		// Get srea.
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
	 * On description enter.
	 */
	protected void onDescriptionEnter() {

		saveDescription();
	}
	
	/**
	 * On alias enter.
	 */
	protected void onAliasEnter() {
		
		saveAlias();
	}
}
