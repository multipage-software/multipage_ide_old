/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import org.multipage.generator.*;
import org.maclan.*;

/**
 * @author
 *
 */
public class SlotEditorFrameBuilder extends SlotEditorBaseFrame implements SlotEditorGenerator {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		SlotEditorBaseFrame.setDefaultData();
	}
	
	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		SlotEditorBaseFrame.seriliazeData(inputStream);
	}
	
	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(StateOutputStream outputStream)
		throws IOException {

		SlotEditorBaseFrame.seriliazeData(outputStream);
	}
	
	/**
	 * Reference to this frame.
	 */
	private SlotEditorFrameBuilder thisFrame = this;
	
	/**
	 * Types combo.
	 */
	private SlotTypeCombo typesCombo;

	/**
	 * Access combo.
	 */
	private AccessComboBox accessCombo;
	
	/**
	 * This check box does nothing.
	 */
	private JCheckBox checkNoOperation = new JCheckBox();

	// $hide<<$
	/**
	 * Dialog components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelSlotHolder;
	private JTextField textHolder;
	private JLabel labelAlias;
	private JTextField textAlias;
	private JPanel panelEditor;
	private JButton buttonSave;
	
	private JPanel panelTypes;
	private JLabel labelValueType;
	private JLabel labelAccess;
	private JPanel panelAccess;
	private JCheckBox checkCanHidden;
	private JCheckBox checkDefaultValue;
	private JTextField textName;
	private JLabel labelName;
	private JCheckBox checkPreferred;
	private JCheckBox checkUserDefined;
	private JButton buttonHelp;
	private JLabel labelSpecialValue;
	private TextFieldEx textSpecialValue;
	private JButton buttonSpecialValue;
	private JMenuBar menuBar;
	private JMenu menuArea;
	private JMenuItem menuAreaEdit;
	private JMenuItem menuEditResources;
	private JMenuItem menuEditDependencies;
	private JMenuItem menuEditInheritance;
	private JMenuItem menuEditStartResource;
	private JMenuItem menuEditConstructors;
	private JMenuItem menuEditHelp;

	/**
	 * Editor helper object
	 */
	protected SlotEditorHelper helper = createCustomizedHelper();
	private JMenu menuSlot;
	private JMenuItem menuSlotProperties;
	
	/**
	 * Expose dialog components. Use SlotEditor for Generator interface.
	 */
	@Override
	public Component getComponent() {
		return this;
	}
	@Override
	public JTextField getTextAlias() {
		return textAlias;
	}
	@Override
	public JCheckBox getCheckDefaultValue() {
		return checkDefaultValue;
	}
	@Override
	public TextFieldEx getTextSpecialValue() {
		return textSpecialValue;
	}
	@Override
	public JCheckBox getCheckLocalizedFlag() {
		return checkNoOperation;
	}
	@Override
	public JTextField getTextHolder() {
		return textHolder;
	}
	@Override
	public Container getPanelEditor() {
		return panelEditor;
	}
	@Override
	public JCheckBox getCheckLocalizedText() {
		return checkNoOperation;
	}
	@Override
	public JLabel getLabelSpecialValue() {
		return labelSpecialValue;
	}
	@Override
	public JButton getButtonSpecialValue() {
		return buttonSpecialValue;
	}
	/**
	 * Lunch the dialog.
	 * @param parentWindow
	 * @param slot
	 * @param isNew
	 * @param modal
	 * @param foundAttr
	 */
	public static void showDialogSimple(Window parentWindow, Slot slot,
			boolean isNew, boolean modal, FoundAttr foundAttr) {

		if (showExisting(slot)) {
			return;
		}
		
		SlotEditorFrameBuilder dialog = new SlotEditorFrameBuilder(parentWindow, slot, isNew, modal, false, foundAttr);
		dialog.setVisible(true);		
	}
	
	/**
	 * Create customized helper.
	 * @param editor
	 * @return
	 */
	private SlotEditorHelper createCustomizedHelper() {
		
		final SlotEditorBaseFrame thisFrame = this;
		
		return new SlotEditorHelper(this) {
			
			/**
			 * On OK button.
			 */
			@Override
			public void onOk(SlotEditorGenerator editor) {
				
				super.onOk(editor);
				dispose();
				createdSlotEditors.remove(thisFrame);
			}
			
			/**
			 * On Cancel button.
			 */
			@Override
			public void onCancel(SlotEditorGenerator editor) {
				
				super.onCancel(editor);
				dispose();
				createdSlotEditors.remove(thisFrame);
			}

			/**
			 * Load dialog.
			 */
			@Override
			public void loadDialog() {
				
				if (bounds.isEmpty()) {
					Utility.centerOnScreen(thisFrame);
					bounds = thisFrame.getBounds();
				}
				else {
					thisFrame.setBounds(bounds);
				}
				super.loadDialog();
			}

			/**
			 * Save dialog.
			 */
			@Override
			protected void saveDialog() {
				
				super.saveDialog();
				bounds = thisFrame.getBounds();
			}

			/**
			 * Load current slot. The function load slots for saveSlot() method
			 * that saves slot data into database.
			 */
			@Override
			public Slot loadCurrentSlot() {
				
				return loadCurrentBuilderSlot();
			}
		};
	}

	/**
	 * Create the dialog.
	 * @param isNew 
	 * @param isNew 
	 * @param modal 
	 * @param useHtmlEditor 
	 * @param foundAttr 
	 * @wbp.parser.constructor
	 */
	public SlotEditorFrameBuilder(Window parentWindow, Slot slot, boolean isNew,
			boolean modal, boolean useHtmlEditor, FoundAttr foundAttr) {

		createdSlotEditors.add(this);
		
		helper.isNew = isNew;
		helper.foundAttr = foundAttr;
		
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate(slot, useHtmlEditor);
		// $hide<<$
	}

	/**
	 * Post create.
	 */
	protected void postCreate(Slot slot, boolean useHtmlEditor) {
		
		// Make copy of slot object
		helper.editedSlot = (Slot) slot.clone();
		
		// Localize components, set icons and tool tips.
		localize();
		setIcons();
		// Create editors.
		helper.createEditors(useHtmlEditor);
		// Set components data.
		updateEditorComponents();
		helper.updateSlotValue();
		// Highlight found.
		helper.highlightFound();
		// Set key bindings.
		setKeyBindings();
		// Set enumeration slot reference.
		helper.getEnumerationEditor().setSlot(helper.getEditedSlot());

		// Comboboxes.
		setTypeAccessHidden();
		
		// Set editor type depending on slot value type.
		helper.setEditorType();
		if (!checkSlotEditorType()) {
			SwingUtilities.invokeLater(()->{ dispose(); });
		}
		
		// Update default value flag.
		boolean isDefault = helper.getEditedSlot().isDefault();
		checkDefaultValue.setSelected(isDefault);
		helper.processDefaultValue(isDefault);
		helper.setSpecialValueEnabled(!isDefault);
		
		// Initialize special value.
		String specialValue = helper.getEditedSlot().getSpecialValue();
		helper.setSpecialValueControl(specialValue);
		
		if (!isDefault && !specialValue.isEmpty()) {
			helper.processDefaultValue(true);
		}
		
		helper.setListeners();
		
		// Load dialog.
		helper.loadDialog();
		
		helper.initialized = true;
	}
	
	/**
	 * Set key bindings.
	 */
	@SuppressWarnings("serial")
	protected void setKeyBindings() {
		
		panelEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save slot");
		panelEditor.getActionMap().put("save slot", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Call on save method.
				helper.onSave();
			}
		});
	}

	/**
	 * Create slot editor.
	 */
	public SlotEditorFrameBuilder(Slot slot, boolean isNew,
			boolean useHtmlEditor, FoundAttr foundAttr, Callback onChangeEvent) {
		
		this(null, slot, isNew, false, useHtmlEditor, foundAttr);
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(400, 300));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				helper.onCancel(thisFrame);
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("textSlotEditor");
		setBounds(100, 100, 699, 575);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onCancel(thisFrame);
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onOk(thisFrame);
			}
		});
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		labelSlotHolder = new JLabel("org.multipage.generator.textSlotHolder");
		springLayout.putConstraint(SpringLayout.EAST, labelSlotHolder, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(labelSlotHolder);
		
		textHolder = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, labelSlotHolder, 0, SpringLayout.WEST, textHolder);
		springLayout.putConstraint(SpringLayout.SOUTH, labelSlotHolder, -6, SpringLayout.NORTH, textHolder);
		springLayout.putConstraint(SpringLayout.NORTH, textHolder, 30, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textHolder, 0, SpringLayout.EAST, buttonCancel);
		textHolder.setEditable(false);
		getContentPane().add(textHolder);
		textHolder.setColumns(15);
		
		labelAlias = new JLabel("org.multipage.generator.textSlotAlias");
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 10, SpringLayout.NORTH, getContentPane());
		getContentPane().add(labelAlias);
		
		textAlias = new TextFieldEx();
		textAlias.setMinimumSize(new Dimension(160, 20));
		springLayout.putConstraint(SpringLayout.WEST, labelAlias, 0, SpringLayout.WEST, textAlias);
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 30, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(textAlias);
		textAlias.setColumns(20);
		
		panelTypes = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, panelTypes, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panelTypes, 160, SpringLayout.WEST, getContentPane());
		getContentPane().add(panelTypes);
		panelTypes.setLayout(new BorderLayout(0, 0));
		
		labelValueType = new JLabel("builder.textValueType");
		springLayout.putConstraint(SpringLayout.WEST, labelValueType, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, panelTypes, 2, SpringLayout.SOUTH, labelValueType);
		springLayout.putConstraint(SpringLayout.SOUTH, panelTypes, 29, SpringLayout.SOUTH, labelValueType);
		springLayout.putConstraint(SpringLayout.NORTH, labelValueType, 6, SpringLayout.SOUTH, textAlias);
		getContentPane().add(labelValueType);
		
		panelEditor = new JPanel();
		springLayout.putConstraint(SpringLayout.SOUTH, panelEditor, -6, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.WEST, panelEditor, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panelEditor, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(panelEditor);
		panelEditor.setLayout(new BorderLayout(0, 0));
		
		buttonSave = new JButton("textSave");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSave, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonSave, -54, SpringLayout.WEST, buttonOk);
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onSave();
			}
		});
		buttonSave.setPreferredSize(new Dimension(80, 25));
		buttonSave.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonSave);
		
		labelAccess = new JLabel("builder.textAccessType");
		springLayout.putConstraint(SpringLayout.NORTH, labelAccess, 6, SpringLayout.SOUTH, textAlias);
		getContentPane().add(labelAccess);
		
		panelAccess = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, labelAccess, 0, SpringLayout.WEST, panelAccess);
		springLayout.putConstraint(SpringLayout.NORTH, panelAccess, 2, SpringLayout.SOUTH, labelAccess);
		springLayout.putConstraint(SpringLayout.WEST, panelAccess, 6, SpringLayout.EAST, panelTypes);
		springLayout.putConstraint(SpringLayout.SOUTH, panelAccess, 29, SpringLayout.SOUTH, labelAccess);
		springLayout.putConstraint(SpringLayout.EAST, panelAccess, 160, SpringLayout.EAST, panelTypes);
		getContentPane().add(panelAccess);
		panelAccess.setLayout(new BorderLayout(0, 0));
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{textAlias, panelEditor, buttonOk, buttonCancel, getContentPane(), labelSlotHolder, textHolder, labelAlias, panelTypes, labelValueType}));
		getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{panelEditor, buttonOk, buttonCancel, labelSlotHolder, textHolder, labelAlias, panelTypes, labelValueType, textAlias}));
		
		checkCanHidden = new JCheckBox("builder.textIsSlotProtected");
		checkCanHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onProtectedChange();
			}
		});
		getContentPane().add(checkCanHidden);
		
		checkDefaultValue = new JCheckBox("org.multipage.generator.textSlotDefaultValue");
		checkDefaultValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onDefaultValue();
			}
		});
		getContentPane().add(checkDefaultValue);
		
		textName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textName, 0, SpringLayout.NORTH, textHolder);
		springLayout.putConstraint(SpringLayout.WEST, textName, 6, SpringLayout.EAST, textAlias);
		springLayout.putConstraint(SpringLayout.EAST, textName, -6, SpringLayout.WEST, textHolder);
		getContentPane().add(textName);
		textName.setColumns(10);
		
		labelName = new JLabel("builder.textSlotName");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 0, SpringLayout.NORTH, labelAlias);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 0, SpringLayout.WEST, textName);
		getContentPane().add(labelName);
		
		checkPreferred = new JCheckBox("builder.textSlotPreferred");
		springLayout.putConstraint(SpringLayout.NORTH, checkPreferred, 6, SpringLayout.SOUTH, textName);
		springLayout.putConstraint(SpringLayout.NORTH, checkCanHidden, 0, SpringLayout.SOUTH, checkPreferred);
		springLayout.putConstraint(SpringLayout.WEST, checkCanHidden, 0, SpringLayout.WEST, checkPreferred);
		springLayout.putConstraint(SpringLayout.WEST, checkPreferred, 30, SpringLayout.EAST, panelAccess);
		getContentPane().add(checkPreferred);
		
		checkUserDefined = new JCheckBox("builder.textSlotUserDefined");
		checkUserDefined.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUserDefined();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, checkUserDefined, 120, SpringLayout.EAST, panelAccess);
		springLayout.putConstraint(SpringLayout.NORTH, checkDefaultValue, 0, SpringLayout.SOUTH, checkUserDefined);
		springLayout.putConstraint(SpringLayout.WEST, checkDefaultValue, 0, SpringLayout.WEST, checkUserDefined);
		springLayout.putConstraint(SpringLayout.NORTH, checkUserDefined, 6, SpringLayout.SOUTH, textName);
		getContentPane().add(checkUserDefined);
		
		buttonHelp = new JButton("");
		buttonHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onHelp();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonHelp, 15, SpringLayout.SOUTH, textHolder);
		buttonHelp.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.EAST, buttonHelp, -10, SpringLayout.EAST, getContentPane());
		buttonHelp.setPreferredSize(new Dimension(25, 25));
		getContentPane().add(buttonHelp);
		
		labelSpecialValue = new JLabel("org.multipage.generator.textSlotSpecialValue");
		springLayout.putConstraint(SpringLayout.NORTH, labelSpecialValue, 12, SpringLayout.SOUTH, panelAccess);
		springLayout.putConstraint(SpringLayout.WEST, labelSpecialValue, 0, SpringLayout.WEST, labelValueType);
		getContentPane().add(labelSpecialValue);
		
		textSpecialValue = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, panelEditor, 10, SpringLayout.SOUTH, textSpecialValue);
		springLayout.putConstraint(SpringLayout.NORTH, textSpecialValue, 6, SpringLayout.SOUTH, checkCanHidden);
		springLayout.putConstraint(SpringLayout.WEST, textSpecialValue, 3, SpringLayout.EAST, labelSpecialValue);
		textSpecialValue.setMinimumSize(new Dimension(160, 20));
		textSpecialValue.setColumns(10);
		getContentPane().add(textSpecialValue);
		
		buttonSpecialValue = new JButton("");
		buttonSpecialValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onSelectSpecialValue();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textSpecialValue, 0, SpringLayout.WEST, buttonSpecialValue);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSpecialValue, 0, SpringLayout.NORTH, textSpecialValue);
		springLayout.putConstraint(SpringLayout.WEST, buttonSpecialValue, 0, SpringLayout.EAST, panelAccess);
		buttonSpecialValue.setPreferredSize(new Dimension(20, 20));
		buttonSpecialValue.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonSpecialValue);
		
		menuBar = new JMenuBar();
		springLayout.putConstraint(SpringLayout.NORTH, menuBar, 12, SpringLayout.SOUTH, panelAccess);
		springLayout.putConstraint(SpringLayout.EAST, menuBar, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(menuBar);
		
		menuArea = new JMenu("org.multipage.generator.menuArea");
		menuBar.add(menuArea);
		
		menuAreaEdit = new JMenuItem("org.multipage.generator.menuAreaEdit");
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.NOT_SPECIFIED);
			}
		});
		menuArea.add(menuAreaEdit);
		
		menuArea.addSeparator();
		
		menuEditInheritance = new JMenuItem("builder.menuAreaEditInheritance");
		menuEditInheritance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.INHERITANCE);
			}
		});
		menuArea.add(menuEditInheritance);
		
		menuEditResources = new JMenuItem("org.multipage.generator.menuAreaEditResources");
		menuEditResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.RESOURCES);
			}
		});
		menuArea.add(menuEditResources);
		
		menuEditStartResource = new JMenuItem("builder.menuAreaEditStartResource");
		menuEditStartResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.START_RESOURCE);
			}
		});
		menuArea.add(menuEditStartResource);
		
		menuEditDependencies = new JMenuItem("org.multipage.generator.menuAreaEditDependencies");
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.DEPENDENCIES);
			}
		});
		menuArea.add(menuEditDependencies);
		
		menuEditConstructors = new JMenuItem("builder.menuAreaEditConstructors");
		menuEditConstructors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.CONSTRUCTORS);
			}
		});
		menuArea.add(menuEditConstructors);
		
		menuEditHelp = new JMenuItem("builder.menuAreaEditHelp");
		menuEditHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorBuilder.HELP);
			}
		});
		menuArea.add(menuEditHelp);
		
		menuSlot = new JMenu("org.multipage.generator.menuSlot");
		menuBar.add(menuSlot);
		
		menuSlotProperties = new JMenuItem("org.multipage.generator.menuSlotProperties");
		menuSlotProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onSlotProperties();
			}
		});
		menuSlot.add(menuSlotProperties);
	}
	
	/**
	 * On protected flag change.
	 */
	protected void onProtectedChange() {
		
		boolean isProtected = checkCanHidden.isSelected();
		if (isProtected) {
			
			checkUserDefined.setSelected(false);
			checkPreferred.setSelected(false);
		}
		
		checkUserDefined.setEnabled(!isProtected);
		checkPreferred.setEnabled(!isProtected);
	}

	/**
	 * On check user defined flag.
	 */
	protected void onUserDefined() {
		
		if (checkUserDefined.isSelected()) {
			checkPreferred.setSelected(true);
			checkPreferred.setEnabled(false);
		}
		else {
			checkPreferred.setEnabled(true);
		}
	}

	/**
	 * Update editor components.
	 */
	protected void updateEditorComponents() {

		helper.updateDialogSlotAndAreaName();
		
		textName.setText(helper.getEditedSlot().getName());
		textAlias.setText(helper.getEditedSlot().getAlias());
		
		boolean userDefined = helper.getEditedSlot().isUserDefined();
		boolean isPreferred = helper.getEditedSlot().isPreferred();
		boolean isProtected = helper.getEditedSlot().isHidden();
		
		checkUserDefined.setSelected(userDefined);
		
		if (!userDefined) {
			checkPreferred.setSelected(isPreferred);
		}
		else {
			helper.getEditedSlot().setPreferred(true);
			checkPreferred.setSelected(true);
			checkPreferred.setEnabled(false);
		}
		
		if (isProtected) {
			
			checkUserDefined.setSelected(false);
			checkPreferred.setSelected(false);
			
			checkUserDefined.setEnabled(false);
			checkPreferred.setEnabled(false);
		}
	}
	
	/**
	 * Set type, access, hidden.
	 */
	private void setTypeAccessHidden() {
		
		setTypesCombo();
		// Set access combo.
		setAccessCombo();
		// Set hidden flag.
		setHiddenFlag();
	}

	/**
	 * Set hidden flag.
	 */
	private void setHiddenFlag() {
		
		checkCanHidden.setSelected(helper.getEditedSlot().isHidden());
	}

	/**
	 * Localize components.
	 */
	protected void localize() {
		
		// Common
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonSave);
		Utility.localize(labelSlotHolder);
		Utility.localize(labelAlias);
		Utility.localize(checkDefaultValue);
		Utility.localize(labelSpecialValue);
		Utility.localize(menuArea);
		Utility.localize(menuAreaEdit);
		Utility.localize(menuEditResources);
		Utility.localize(menuEditDependencies);
		Utility.localize(menuSlot);
		Utility.localize(menuSlotProperties);
		
		// Only Builder
		Utility.localize(labelValueType);
		Utility.localize(labelAccess);
		Utility.localize(checkCanHidden);
		Utility.localize(labelName);
		Utility.localize(checkPreferred);
		Utility.localize(checkUserDefined);
		Utility.localize(menuEditInheritance);
		Utility.localize(menuEditStartResource);
		Utility.localize(menuEditConstructors);
		Utility.localize(menuEditHelp);
	}

	
	/**
	 * Set icons.
	 */
	protected void setIcons() {

		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonSave.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
		buttonHelp.setIcon(Images.getIcon("org/multipage/generator/images/help_small.png"));
		buttonSpecialValue.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
		menuArea.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
	}
	/**
	 * Set types combo.
	 */
	private void setTypesCombo() {

		typesCombo = new SlotTypeCombo();
		
		// Select slot type.
		SlotType type = helper.getEditedSlot().getTypeUseValueMeaning();
		if (type == SlotType.UNKNOWN) {
			type = SlotType.TEXT;
		}
		typesCombo.setSelected(type);
		helper.selectEditor(type, helper.isNew);
		
		panelTypes.add(typesCombo);
		
		typesCombo.addActionListener(new ActionListener() {
			// On action.
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get selected type.
				SlotType type = typesCombo.getSelected();
				helper.selectEditor(type, true);
			}
		});
	}

	/**
	 * Set access combo.
	 */
	private void setAccessCombo() {
		
		// Create and attach access combo box.
		accessCombo = new AccessComboBox();
		panelAccess.add(accessCombo);
		// Select access.
		accessCombo.selectItem(helper.getEditedSlot().getAccess());
	}

	/**
	 * Get helper.
	 */
	@Override
	public SlotEditorHelper getHelper() {
		
		return helper;
	}
	
	/**
	 * Loads current slot.
	 * @return
	 */
	protected Slot loadCurrentBuilderSlot() {
		
		// Trim alias.
		String alias = getAlias();
		Slot editedSlot = helper.getEditedSlot();
		SlotHolder holder = editedSlot.getHolder();

		// Create new slot.
		Slot newSlot = new Slot(holder, alias);
		
		char access = getAccess();
		boolean hidden = isHidden();
		boolean isDefault = getCheckDefaultValue().isSelected();
		String name = getSlotName();
		boolean preferred = isPreferred();
		boolean userDefined = isUserDefined();
		String specialValue = getSpecialValue();
		String externalProvider = editedSlot.getExternalProvider();
		boolean readsInput = editedSlot.getReadsInput();
		boolean writesOutput = editedSlot.getWritesOutput();
		
		// Get value.
		Object value = helper.getValue();
		String valueMeaning = helper.getValueMeaning();
		
		newSlot.setValue(value);
		newSlot.setValueMeaning(valueMeaning);
		newSlot.setLocalized(value instanceof String && isLocalizedText());
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
	 * Gets true value if localized text is selected.
	 * @return
	 */
	private boolean isLocalizedText() {
		
		return typesCombo.getSelected() == SlotType.LOCALIZED_TEXT;
	}

	/**
	 * Get hidden.
	 * @return
	 */
	private boolean isHidden() {
		
		return checkCanHidden.isSelected();
	}

	/**
	 * Get access.
	 * @return
	 */
	private char getAccess() {
		
		return accessCombo.getSelectedAccess();
	}

	/**
	 * Get slot name.
	 * @return
	 */
	private String getSlotName() {
		
		return textName.getText();
	}

	/**
	 * Get alias.
	 * @return
	 */
	private String getAlias() {
		
		return textAlias.getText().trim();
	}

	/**
	 * Get "user defined slot" flag.
	 * @return
	 */
	private boolean isUserDefined() {
		
		return checkUserDefined.isSelected();
	}

	/**
	 * Get "slot is preferred" flag.
	 * @return
	 */
	private boolean isPreferred() {
		
		return checkPreferred.isSelected() || isUserDefined();
	}
	
	/**
	 * Get special value.
	 * @return
	 */
	private String getSpecialValue() {
		
		return textSpecialValue.getText();
	}

	/**
	 * Check slot editor type.
	 */
	private boolean checkSlotEditorType() {
		
		// Check slot and editor type.
		if (helper.getEditedSlot().getValue() != null && helper.getEditedSlot().getType() != typesCombo.getSelected()) {
			Utility.show(this, "builder.messageSlotAndEditorTypeConflict");
			return false;
		}
		
		return true;
	}

	@Override
	public JToggleButton getToggleDebug() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JCheckBox getCheckInterpretPhp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JLabel getLabelInheritable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
