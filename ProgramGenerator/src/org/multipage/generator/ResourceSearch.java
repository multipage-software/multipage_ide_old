/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.maclan.MimeType;
import org.maclan.Resource;
import org.multipage.gui.BooleanTriState;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;


/**
 * 
 * @author
 *
 */
public class ResourceSearch extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog object.
	 */
	private static ResourceSearch staticDialog;

	/**
	 * List panel reference.
	 */
	private static SearchableResourcesList listPanel;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelName;
	private TextFieldEx textName;
	private JLabel labelMime;
	private JComboBox comboBoxMime;
	private JLabel labelIdentifier;
	private TextFieldEx textIdentifier;
	private JComboBox comboSavedAsText;
	private JComboBox comboIsVisible;
	private JSeparator separator;
	private JLabel labelSettings;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWord;
	private JRadioButton buttonForward;
	private JRadioButton buttonBackward;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JButton buttonFindNext;
	private JSeparator separatorHorizontal;
	private final JButton buttonReloadMime = new JButton("");
	private JLabel labelSavedAsText;
	private JLabel labelIsVisible;
	private JLabel labelMessage;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static void showDialog(SearchableResourcesList listPanel) {

		ResourceSearch.listPanel = listPanel;
		
		if (staticDialog == null) {
			staticDialog = new ResourceSearch(listPanel.getWindow());
		}
		staticDialog.loadMimeTypes();
		staticDialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public ResourceSearch(Window parent) {
		super(parent, ModalityType.MODELESS);

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		setResizable(false);
		setTitle("org.multipage.generator.textResourceSearch");
		setBounds(100, 100, 387, 303);
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.WEST, buttonReloadMime, 165, SpringLayout.WEST, getContentPane());
		getContentPane().setLayout(springLayout);
		
		labelName = new JLabel("org.multipage.generator.textResourceName");
		springLayout.putConstraint(SpringLayout.EAST, buttonReloadMime, 0, SpringLayout.EAST, labelName);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelName);
		
		textName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textName, 6, SpringLayout.SOUTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textName, 0, SpringLayout.WEST, labelName);
		springLayout.putConstraint(SpringLayout.EAST, textName, 0, SpringLayout.EAST, labelName);
		getContentPane().add(textName);
		
		labelMime = new JLabel("org.multipage.generator.textResourceMimeType");
		springLayout.putConstraint(SpringLayout.NORTH, labelMime, 6, SpringLayout.SOUTH, textName);
		springLayout.putConstraint(SpringLayout.NORTH, buttonReloadMime, 6, SpringLayout.SOUTH, labelMime);
		springLayout.putConstraint(SpringLayout.WEST, labelMime, 0, SpringLayout.WEST, labelName);
		getContentPane().add(labelMime);
		
		comboBoxMime = new JComboBox();
		springLayout.putConstraint(SpringLayout.SOUTH, buttonReloadMime, 0, SpringLayout.SOUTH, comboBoxMime);
		springLayout.putConstraint(SpringLayout.NORTH, comboBoxMime, 6, SpringLayout.SOUTH, labelMime);
		springLayout.putConstraint(SpringLayout.WEST, comboBoxMime, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboBoxMime, -4, SpringLayout.WEST, buttonReloadMime);
		comboBoxMime.setPreferredSize(new Dimension(180, 20));
		getContentPane().add(comboBoxMime);
		
		labelIdentifier = new JLabel("org.multipage.generator.textResourceIdentifier");
		springLayout.putConstraint(SpringLayout.NORTH, labelIdentifier, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelIdentifier, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelIdentifier);
		
		textIdentifier = new TextFieldEx();
		textIdentifier.setBackground(new Color(255, 215, 0));
		textIdentifier.setForeground(Color.RED);
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 6, SpringLayout.SOUTH, textIdentifier);
		springLayout.putConstraint(SpringLayout.EAST, textIdentifier, 0, SpringLayout.EAST, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textIdentifier, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, textIdentifier, 6, SpringLayout.SOUTH, labelIdentifier);
		getContentPane().add(textIdentifier);
		
		comboSavedAsText = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, comboSavedAsText, 10, SpringLayout.WEST, getContentPane());
		comboSavedAsText.setMaximumSize(new Dimension(32767, 22));
		getContentPane().add(comboSavedAsText);
		
		comboIsVisible = new JComboBox();
		springLayout.putConstraint(SpringLayout.EAST, comboSavedAsText, -31, SpringLayout.WEST, comboIsVisible);
		springLayout.putConstraint(SpringLayout.NORTH, comboIsVisible, 0, SpringLayout.NORTH, comboSavedAsText);
		comboIsVisible.setMaximumSize(new Dimension(32767, 22));
		getContentPane().add(comboIsVisible);
		
		separator = new JSeparator();
		springLayout.putConstraint(SpringLayout.EAST, comboIsVisible, -12, SpringLayout.WEST, separator);
		springLayout.putConstraint(SpringLayout.EAST, labelMime, -10, SpringLayout.WEST, separator);
		springLayout.putConstraint(SpringLayout.EAST, labelName, -6, SpringLayout.WEST, separator);
		springLayout.putConstraint(SpringLayout.EAST, labelIdentifier, -10, SpringLayout.WEST, separator);
		springLayout.putConstraint(SpringLayout.WEST, separator, 191, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, separator, -45, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, separator, 10, SpringLayout.NORTH, getContentPane());
		separator.setOrientation(SwingConstants.VERTICAL);
		getContentPane().add(separator);
		
		labelSettings = new JLabel("org.multipage.generator.textSearchResourceNameParamaters");
		springLayout.putConstraint(SpringLayout.NORTH, labelSettings, 30, SpringLayout.NORTH, getContentPane());
		labelSettings.setForeground(new Color(0, 128, 128));
		springLayout.putConstraint(SpringLayout.WEST, labelSettings, 6, SpringLayout.EAST, separator);
		springLayout.putConstraint(SpringLayout.EAST, labelSettings, 178, SpringLayout.EAST, separator);
		getContentPane().add(labelSettings);
		
		checkCaseSensitive = new JCheckBox("org.multipage.generator.textCaseSensitive");
		checkCaseSensitive.setForeground(new Color(0, 128, 128));
		springLayout.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 16, SpringLayout.SOUTH, labelSettings);
		springLayout.putConstraint(SpringLayout.WEST, checkCaseSensitive, 26, SpringLayout.EAST, separator);
		springLayout.putConstraint(SpringLayout.EAST, checkCaseSensitive, 0, SpringLayout.EAST, labelSettings);
		getContentPane().add(checkCaseSensitive);
		
		checkWholeWord = new JCheckBox("org.multipage.generator.textWholeWords");
		springLayout.putConstraint(SpringLayout.NORTH, checkWholeWord, 15, SpringLayout.SOUTH, checkCaseSensitive);
		springLayout.putConstraint(SpringLayout.WEST, checkWholeWord, 0, SpringLayout.WEST, checkCaseSensitive);
		checkWholeWord.setForeground(new Color(0, 128, 128));
		getContentPane().add(checkWholeWord);
		
		buttonForward = new JRadioButton("org.multipage.generator.textSearchForward");
		buttonGroup.add(buttonForward);
		buttonForward.setForeground(new Color(0, 128, 128));
		springLayout.putConstraint(SpringLayout.WEST, buttonForward, 0, SpringLayout.WEST, checkCaseSensitive);
		getContentPane().add(buttonForward);
		
		buttonBackward = new JRadioButton("org.multipage.generator.textSearchBackward");
		springLayout.putConstraint(SpringLayout.NORTH, buttonBackward, 3, SpringLayout.SOUTH, buttonForward);
		buttonGroup.add(buttonBackward);
		buttonBackward.setForeground(new Color(0, 128, 128));
		springLayout.putConstraint(SpringLayout.WEST, buttonBackward, 0, SpringLayout.WEST, checkCaseSensitive);
		getContentPane().add(buttonBackward);
		
		buttonFindNext = new JButton("org.multipage.generator.textFindNext");
		buttonFindNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindNext();
			}
		});
		buttonFindNext.setMargin(new Insets(0, 0, 0, 0));
		buttonFindNext.setPreferredSize(new Dimension(85, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonFindNext, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonFindNext, 0, SpringLayout.EAST, labelSettings);
		getContentPane().add(buttonFindNext);
		
		separatorHorizontal = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, separatorHorizontal, 26, SpringLayout.SOUTH, checkWholeWord);
		springLayout.putConstraint(SpringLayout.NORTH, buttonForward, 23, SpringLayout.SOUTH, separatorHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, separatorHorizontal, 0, SpringLayout.EAST, separator);
		springLayout.putConstraint(SpringLayout.EAST, separatorHorizontal, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(separatorHorizontal);
		buttonReloadMime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onReloadMime();
			}
		});
		buttonReloadMime.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonReloadMime);
		
		labelSavedAsText = new JLabel("org.multipage.generator.textResourceSavedAsText");
		springLayout.putConstraint(SpringLayout.NORTH, comboSavedAsText, 6, SpringLayout.SOUTH, labelSavedAsText);
		springLayout.putConstraint(SpringLayout.NORTH, labelSavedAsText, 6, SpringLayout.SOUTH, comboBoxMime);
		springLayout.putConstraint(SpringLayout.WEST, labelSavedAsText, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelSavedAsText, 100, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSavedAsText);
		
		labelIsVisible = new JLabel("org.multipage.generator.textResourceIsVisible");
		springLayout.putConstraint(SpringLayout.WEST, comboIsVisible, 0, SpringLayout.WEST, labelIsVisible);
		springLayout.putConstraint(SpringLayout.NORTH, labelIsVisible, 6, SpringLayout.SOUTH, comboBoxMime);
		springLayout.putConstraint(SpringLayout.WEST, labelIsVisible, 110, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelIsVisible, -10, SpringLayout.WEST, separator);
		getContentPane().add(labelIsVisible);
		
		labelMessage = new JLabel("");
		springLayout.putConstraint(SpringLayout.WEST, labelMessage, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelMessage, -101, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, labelMessage, -10, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(labelMessage);
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{textName, textIdentifier, comboBoxMime, comboSavedAsText, comboIsVisible, checkCaseSensitive, checkWholeWord, buttonForward, buttonBackward, buttonFindNext, getContentPane(), labelName, labelMime, labelIdentifier, separator, labelSettings, separatorHorizontal, buttonReloadMime, labelSavedAsText, labelIsVisible, labelMessage}));
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		localize();
		setIcons();
		
		initIdentifierTextBox();
		initComboBoxes();
		addKeyMaps();
		
		buttonForward.setSelected(true);
	}

	/**
	 * Add key maps.
	 */
	private void addKeyMaps() {
	
		getRootPane().setDefaultButton(buttonFindNext);
	}

	/**
	 * Initialize comboboxes.
	 */
	private void initComboBoxes() {
		
		comboSavedAsText.addItem(BooleanTriState.UNKNOWN);
		comboSavedAsText.addItem(BooleanTriState.TRUE);
		comboSavedAsText.addItem(BooleanTriState.FALSE);
		
		comboIsVisible.addItem(BooleanTriState.UNKNOWN);
		comboIsVisible.addItem(BooleanTriState.TRUE);
		comboIsVisible.addItem(BooleanTriState.FALSE);
	}

	/**
	 * Initialize identifier text box.
	 */
	private void initIdentifierTextBox() {
		
		textIdentifier.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onIdentiferChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onIdentiferChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onIdentiferChanged();
			}
		});
	}

	/**
	 * On identifier changed.
	 */
	protected void onIdentiferChanged() {
		
		Long identifier = getIdentifier();
		if (textIdentifier.getText().isEmpty()) {
			identifier = 0L;
		}
		
		textIdentifier.setForeground(identifier != null ? Color.BLACK : Color.RED);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelName);
		Utility.localize(labelMime);
		Utility.localize(labelIdentifier);
		Utility.localize(labelSavedAsText);
		Utility.localize(labelIsVisible);
		Utility.localize(labelSettings);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWord);
		Utility.localize(buttonForward);
		Utility.localize(buttonBackward);
		Utility.localize(buttonFindNext);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonFindNext.setIcon(Images.getIcon("org/multipage/generator/images/search_icon.png"));
		buttonReloadMime.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
	}
	
	/**
	 * On close.
	 */
	protected void onClose() {
		
		dispose();
		staticDialog = null;
	}

	/**
	 * Load MIME types.
	 */
	private void loadMimeTypes() {
		
		if (listPanel == null) {
			return;
		}
		
		LinkedList<MimeType> mimeTypesReference = listPanel.getMimeTypes();
		if (mimeTypesReference == null) {
			return;
		}
		
		// Clone and sort the list.
		LinkedList<MimeType> mimeTypes = (LinkedList<MimeType>) mimeTypesReference.clone();
		Collections.sort(mimeTypes, new Comparator<MimeType>() {
			@Override
			public int compare(MimeType mimeType1, MimeType mimeType2) {
				return mimeType1.toString().compareTo(mimeType2.toString());
			}});
		
		// Load MIME types to the combo box.
		comboBoxMime.removeAllItems();
		
		// Add default item.
		comboBoxMime.addItem("");

		for (MimeType mimeType : mimeTypes) {
			comboBoxMime.addItem(mimeType);
		}
	}

	/**
	 * Get resource name.
	 * @return
	 */
	private String getResourceName() {
		
		String name = textName.getText();
		if (name == null) {
			return null;
		}
		if (name.isEmpty()) {
			return null;
		}
		
		return name;
	}

	/**
	 * On reload MIME.
	 */
	protected void onReloadMime() {
		
		loadMimeTypes();
	}

	/**
	 * Update dialog.
	 */
	public static void update() {
		
		if (staticDialog != null) {
			staticDialog.loadMimeTypes();
		}
	}

	/**
	 * Get selected MIME type.
	 * @return
	 */
	private MimeType getSelectedMimeType() {
		
		Object item = comboBoxMime.getSelectedItem();
		if (!(item instanceof MimeType)) {
			return null;
		}
		
		return (MimeType) item;
	}

	/**
	 * Get identifier.
	 * @return
	 */
	private Long getIdentifier() {
		
		String idText = textIdentifier.getText();
		Long identifier = null;
		
		try {
			identifier = Long.parseLong(idText);
		}
		catch (NumberFormatException e) {
		}
		
		return identifier;
	}

	/**
	 * Get saved as text.
	 * @return
	 */
	private Boolean getSavedAsText() {
		
		Object item = comboSavedAsText.getSelectedItem();
		if (!(item instanceof BooleanTriState)) {
			return null;
		}
		
		BooleanTriState state = (BooleanTriState) item;
		return state.value;
	}

	/**
	 * Get is visible.
	 * @return
	 */
	private Boolean getIsVisible() {
		
		Object item = comboIsVisible.getSelectedItem();
		if (!(item instanceof BooleanTriState)) {
			return null;
		}
		
		BooleanTriState state = (BooleanTriState) item;
		return state.value;
	}

	/**
	 * On find next.
	 */
	protected void onFindNext() {
		
		if (listPanel == null) {
			return;
		}
		
		// Get first selected item.
		boolean forward = buttonForward.isSelected();

		JList list = listPanel.getList();
		ListModel model = list.getModel();
		int resourcesCount = model.getSize();
		
		if (resourcesCount == 0) {
			Utility.show(this, "org.multipage.generator.messageThereAreNoResourcesInList");
			return;
		}
		
		int selectedIndex = list.getSelectedIndex();
		int lastIndex = resourcesCount - 1;
		int nextIndex;
		
		if (selectedIndex == -1) {
			nextIndex = forward ? 0 : lastIndex;
		}
		else {
			if (forward) {
				nextIndex = selectedIndex < lastIndex ? selectedIndex + 1 : lastIndex;
			}
			else {
				nextIndex = selectedIndex > 0 ? selectedIndex - 1 : 0;
			}
		}
		
		// Get settings.
		String name = getResourceName();
		MimeType mimeType = getSelectedMimeType();
		Long identifier = getIdentifier();
		Boolean savedAsText = getSavedAsText();
		Boolean isVisible = getIsVisible();
		boolean isCaseSensitive = checkCaseSensitive.isSelected();
		boolean isWholeWord = checkWholeWord.isSelected();
		
		Resource foundResource = null;
		
		// Search for next match.
		for (int currentIndex = nextIndex;
		     currentIndex >= 0 && currentIndex <= lastIndex;
		     currentIndex = forward ? currentIndex + 1 : currentIndex - 1) {
			
			// Get resource reference.
			Object element = model.getElementAt(currentIndex);
			if (!(element instanceof Resource)) {
				continue;
			}
			Resource resource = (Resource) element;
			
			if (identifier != null) {
				if (identifier == resource.getId()) {
					// Select this resource.
					list.setSelectedIndex(currentIndex);
					list.ensureIndexIsVisible(currentIndex);
					
					foundResource = resource;
					break;
				}
			}
			
			// Check properties.
			if (mimeType != null) {
				if (mimeType.id != resource.getMimeTypeId()) {
					continue;
				}
			}
			if (savedAsText != null) {
				if (savedAsText != resource.isSavedAsText()) {
					continue;
				}
			}
			if (isVisible != null) {
				if (isVisible != resource.isVisible()) {
					continue;
				}
			}
			if (name == null && identifier == null) {
				
				// Select this resource.
				list.setSelectedIndex(currentIndex);
				list.ensureIndexIsVisible(currentIndex);
				
				foundResource = resource;
				break;
			}
			
			if (name != null) {
				// Search in resource description.
				String resourceName = resource.getDescription();
				FoundAttr foundAttr = new FoundAttr(name, isCaseSensitive, isWholeWord);
				
				if (Utility.find(resourceName, foundAttr)) {
					
					// Select this resource.
					list.setSelectedIndex(currentIndex);
					list.ensureIndexIsVisible(currentIndex);
					
					foundResource = resource;
					break;
				}
			}
		}
		
		// If not found inform user.
		if (foundResource == null) {
			labelMessage.setText(Resources.getString("org.multipage.generator.textResourceNotFound"));
		}
		else {
			labelMessage.setText(foundResource.getDescription());
		}
	}
}
