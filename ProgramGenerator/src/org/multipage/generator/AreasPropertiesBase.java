/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.HashSet;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.EventSource;
import org.multipage.gui.GuiSignal;
import org.multipage.gui.Images;
import org.multipage.gui.SignalGroup;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.UpdateSignal;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class AreasPropertiesBase extends JPanel {
	public AreasPropertiesBase() {
	}
	
	//$hide>>$
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
	public static void seriliazeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load splitter position.
		splitterPositionState = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(StateOutputStream outputStream)
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
	 * Automatic save timers.
	 */
	private Timer saveDescriptionTimer;
	private Timer saveAliasTimer;
	
	/**
	 * Description and alias focus.
	 */
	private boolean isDescriptionFocus = false;
	private boolean isAliasFocus = false;
	
	/**
	 * Edit boxes' listeners.
	 */
	private DocumentListener descriptionListener;
	private DocumentListener aliasListener;
	
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
		// Set save timers.
		setSaveTimers();
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
	 * Set text editor focus listeners.
	 */
	private void setFocusListeners() {
		
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
	 * Save description.
	 */
	public void saveDescription() {
		
		// If the areas reference is not set exit the method.
		if (areas != null && areas.size() == 1) {
			
			// Disable edit box change events.
			disableTextInputEvents(textDescription, descriptionListener);
			
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
				
				long areaId = area.getId();
				
				// Transmit the update signal.
				HashSet<Long> selectedAreas = new HashSet<Long>();
				selectedAreas.add(areaId);
				ApplicationEvents.transmit(EventSource.AREA_EDITOR.user(this), SignalGroup.UPDATE_ALL, selectedAreas);
				
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
			
			// Enable edit box change events.
			enableTextInputEvents(textDescription, descriptionListener);
		}
	}

	/**
	 * Save alias.
	 */
	public void saveAlias() {

		// Single area must be selected.
		if (areas != null && areas.size() == 1) {
			
			// Disable edit box change events.
			disableTextInputEvents(textAlias, aliasListener);			
			
			// If the alias changes...
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
				
				// Transmit the update signal.
				HashSet<Long> selectedAreas = new HashSet<Long>();
				selectedAreas.add(areaId);
				ApplicationEvents.transmit(EventSource.AREA_EDITOR.user(this), SignalGroup.UPDATE_ALL, selectedAreas);
				
				// Set focus.
				if (oldFocus) {
					textAlias.requestFocus();
					textAlias.setCaretPosition(oldCaret);
				}
			}
			
			// Enable edit box change events.
			enableTextInputEvents(textAlias, aliasListener);				
		}
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
					
					// Call finished function on the event thread.
					ApplicationEvents.invokeLater(() -> onSaveFinished.run());
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
			// TODO: finish it
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
	
		// Set text editor listeners.
		setFocusListeners();
		
		// Wrap description and alias change events.
		descriptionListener = createTextListener(() -> onChangeDescription());
		aliasListener = createTextListener(() -> onChangeAlias());
		
		// Enable the input events on appropriate text boxes.
		enableTextInputEvents(textDescription, descriptionListener);
		enableTextInputEvents(textAlias, aliasListener);
		
		// TODO: <---MAKE Release the receiver when leaving the dialog.
		
		// "Model updated" event receiver.
		ApplicationEvents.receiver(this, UpdateSignal.updateAreasProperties, message -> {
			
			// Update the list of areas.
			areas = ProgramGenerator.getUpdatedAreas(areas);
			
			// Set the areas.
			setAreas(areas);
			
			// Enable editing in the main frame window.
			enableEditing(true);
		});
	}
	
	/**
	 * Wrap text input event.
	 * @return
	 */
	private DocumentListener createTextListener(Runnable textInputEvent) {
		
		DocumentListener listener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				textInputEvent.run();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				textInputEvent.run();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				textInputEvent.run();
			}
        };
        return listener;
	}
	
	/**
	 * Enable text box input events.
	 * @param textField
	 * @param listener
	 */
	private void enableTextInputEvents(JTextField textField, DocumentListener listener) {
		
		// Listen for changes in the text field.
		Document document = textField.getDocument();
		document.removeDocumentListener(listener);
		document.addDocumentListener(listener);
	}

	/**
	 * Disable text box input events.
	 * @param textField
	 * @param listener
	 */
	private void disableTextInputEvents(JTextField textField, DocumentListener listener) {
		
		// Disable listener of changes in the text field.
		Document document = textField.getDocument();
		document.removeDocumentListener(listener);
	}
	
	/**
	 * Set save timer.
	 */
	private void setSaveTimers() {

		// Create timer firing one event.
		saveDescriptionTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDescriptionChanges();
			}
		});
		
		// Create timer firing one event.
		saveAliasTimer = new Timer(saveTimerDelay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveAliasChanges();
			}
		});
		
		saveDescriptionTimer.setRepeats(false);
		saveAliasTimer.setRepeats(false);
	}
	
	/**
	 * On change description.
	 */
	protected void onChangeDescription() {
		
		Color color;
		
		// If the current area description is not equal to loaded area
		// description set text color to red.
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
