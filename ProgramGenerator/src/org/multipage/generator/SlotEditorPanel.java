/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import org.maclan.Slot;
import org.multipage.gui.Callback;
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
public class SlotEditorPanel extends JPanel implements SlotEditorGenerator {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Editor helper object
	 */
	protected SlotEditorHelper helper = createHelper();
	
	/**
	 * Creates helper. You can override this method to customize your helper.
	 * @return
	 */
	public SlotEditorHelper createHelper() {
		
		// You can override this code.
		return new SlotEditorHelper(this);
	}
	
	/**
	 * Gets reference to helper.
	 */
	public SlotEditorHelper getHelper() {
		
		return helper;
	}
	
	/**
	 * Reference to this editor.
	 */
	public SlotEditorPanel thisEditor = this;

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
	private JPanel panelAux;
	private JCheckBox checkDefaultValue;
	private Component horizontalGlue;
	private JButton buttonHelp;
	private JLabel labelSpecialValue;
	private Component horizontalGlue_1;
	private TextFieldEx textSpecialValue;
	private JButton buttonSpecialValue;
	private JMenuBar menuBar;
	private JMenu menuArea;
	private JMenu menuSlot;
	private JMenuItem menuAreaEdit;
	private JMenuItem menuEditResources;
	private JMenuItem menuEditDependencies;
	private JMenuItem menuSlotProperties;
	private JCheckBox checkLocalizedFlag;
	private JButton buttonDisplay;
	private Component horizontalStrut_1;
	private JLabel labelInheritable;
	private JToggleButton toggleDebug;
	private Component horizontalStrut_2;
	private Component horizontalStrut_3;
	private JButton buttonRender;
	private Component horizontalStrut_A;
	private JCheckBox checkInterpretPhp;
	private JButton buttonRevision;
	private JButton buttonCommit;
	private JButton buttonExpose;
	
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
		return checkLocalizedFlag;
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
		return checkLocalizedFlag;
	}
	@Override
	public JLabel getLabelSpecialValue() {
		return labelSpecialValue;
	}
	@Override
	public JButton getButtonSpecialValue() {
		return buttonSpecialValue;
	}
	@Override
	public JToggleButton getToggleDebug() {
		return toggleDebug;
	}
	@Override
	public JCheckBox getCheckInterpretPhp() {
		return checkInterpretPhp;
	}
	@Override
	public JLabel getLabelInheritable() {
		return labelInheritable;
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
	public SlotEditorPanel(Window parentWindow, Slot slot, boolean isNew,
			boolean modal, boolean useHtmlEditor, FoundAttr foundAttr) {
				
		this.helper.isNew = isNew;
		this.helper.foundAttr = foundAttr;
		
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
		
		// Original slot reference
		helper.originalSlot = slot;
		
		// Make copy of slot object
		helper.editedSlot = (Slot) slot.clone();
		
		// Localize components, set icons and tool tips.
		localize();
		setIcons();
		setToolTips();
		setKeyBindings();
		
		// Create editors.
		helper.createEditors(useHtmlEditor);
		
		// Set listeners
		helper.setListeners();
		
		// Do additional creation.
		boolean isUserSlot = getEditedSlot().isUserDefined();
		if (isUserSlot) {
			Utility.localize(checkLocalizedFlag);
		}
		
		// Enable slot properties trayMenu.
		slotPropertiesMenu();
		
		// Update dialog
		updateDialog();
		
		// Set debug toggle button
		getToggleDebug().setSelected(Settings.getEnableDebugging());

		// Load dialog.
		helper.loadDialog();
		
		// Set flag
		helper.initialized = true;
		
		// Remember the panel
		helper.remember();
	}

	/**
	 * Enable/disable slot properties trayMenu.
	 */
	private void slotPropertiesMenu() {
		
		menuSlot.setVisible(helper.getEditedSlot().isExternalProvider());
	}

	/**
	 * Update dialog
	 */
	private void updateDialog() {
		
		// Set slot and area name.
		helper.updateDialogSettings();
	}

	/**
	 * Localize components.
	 */
	protected void localize() {

		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonSave);
		Utility.localize(buttonCommit);
		Utility.localize(buttonRevision);
		Utility.localize(labelSlotHolder);
		Utility.localize(labelAlias);
		Utility.localize(checkDefaultValue);
		Utility.localize(labelSpecialValue);
		Utility.localize(menuArea);
		Utility.localize(menuSlot);
		Utility.localize(menuAreaEdit);
		Utility.localize(menuEditResources);
		Utility.localize(menuEditDependencies);
		Utility.localize(menuSlotProperties);
		Utility.localize(checkInterpretPhp);
		if (labelInheritable != null) {
			Utility.localize(labelInheritable);
		}
		Utility.localize(buttonExpose);
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
		menuSlot.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		buttonRender.setIcon(Images.getIcon("org/multipage/generator/images/render.png"));
		buttonDisplay.setIcon(Images.getIcon("org/multipage/generator/images/display_home_page.png"));
		toggleDebug.setIcon(Images.getIcon("org/multipage/generator/images/debug.png"));
		buttonRevision.setIcon(Images.getIcon("org/multipage/generator/images/revision.png"));
		buttonCommit.setIcon(Images.getIcon("org/multipage/generator/images/commit.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonRender.setToolTipText(Resources.getString("org.multipage.generator.tooltipRenderHtmlPages"));
		buttonDisplay.setToolTipText(Resources.getString("org.multipage.generator.tooltipDisplayHomePage"));
		toggleDebug.setToolTipText(Resources.getString("org.multipage.generator.tooltipEnableDisplaySourceCode"));
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
	 * Get slot copy
	 * @return
	 */
	protected Slot getEditedSlot() {
		
		return helper.getEditedSlot();
	}
	
	/**
	 * Constructor.
	 * @param slot
	 * @param isNew
	 * @param useHtmlEditor
	 * @param foundAttr
	 * @param onChangeEvent
	 */
	public SlotEditorPanel(Slot slot, boolean isNew, boolean useHtmlEditor, FoundAttr foundAttr,
			Callback onChangeEvent) {
		
		this(null, slot, isNew, false, useHtmlEditor, foundAttr);
	}
	
	/**
	 * Get panel
	 * @return
	 */
	private JPanel getContentPane() {
		
		return this;
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(450, 430));
		setBounds(100, 100, 601, 470);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onCancel(thisEditor);
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
				helper.onOk(thisEditor);
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
		springLayout.putConstraint(SpringLayout.WEST, textHolder, -240, SpringLayout.EAST, getContentPane());
		textHolder.setEditable(false);
		getContentPane().add(textHolder);
		textHolder.setColumns(10);
		
		labelAlias = new JLabel("org.multipage.generator.textSlotAlias2");
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelAlias, -6, SpringLayout.WEST, labelSlotHolder);
		getContentPane().add(labelAlias);
		
		textAlias = new TextFieldEx();
		textAlias.setFont(new Font("Tahoma", Font.BOLD, 11));
		textAlias.setMinimumSize(new Dimension(160, 20));
		springLayout.putConstraint(SpringLayout.WEST, labelAlias, 0, SpringLayout.WEST, textAlias);
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 30, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textAlias, -6, SpringLayout.WEST, textHolder);
		getContentPane().add(textAlias);
		textAlias.setColumns(10);
		
		panelEditor = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, panelEditor, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panelEditor, -6, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, panelEditor, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(panelEditor);
		panelEditor.setLayout(new BorderLayout(0, 0));
		
		buttonSave = new JButton("textSave");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSave, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonSave, -30, SpringLayout.WEST, buttonOk);
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onSave();
			}
		});
		buttonSave.setPreferredSize(new Dimension(80, 25));
		buttonSave.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonSave);
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{textAlias, panelEditor, buttonOk, buttonCancel, getContentPane(), labelSlotHolder, textHolder, labelAlias}));
		getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{panelEditor, buttonOk, buttonCancel, labelSlotHolder, textHolder, labelAlias, textAlias}));
		
		menuBar = new JMenuBar();
		springLayout.putConstraint(SpringLayout.NORTH, menuBar, 15, SpringLayout.SOUTH, textHolder);
		springLayout.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, labelAlias);
		getContentPane().add(menuBar);
		
		menuArea = new JMenu("org.multipage.generator.menuArea");
		menuBar.add(menuArea);
		
		menuAreaEdit = new JMenuItem("org.multipage.generator.menuAreaEdit");
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				helper.onEditArea(AreaEditorFrame.NOT_SPECIFIED);
			}
		});
		menuArea.add(menuAreaEdit);
		
		menuArea.addSeparator();
		
		menuEditResources = new JMenuItem("org.multipage.generator.menuAreaEditResources");
		menuEditResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorFrame.RESOURCES);
			}
		});
		menuArea.add(menuEditResources);
		
		menuEditDependencies = new JMenuItem("org.multipage.generator.menuAreaEditDependencies");
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onEditArea(AreaEditorFrame.DEPENDENCIES);
			}
		});
		menuArea.add(menuEditDependencies);
			
		menuSlot = new JMenu("org.multipage.generator.menuSlot");
		menuBar.add(menuSlot);
		
		menuSlotProperties = new JMenuItem("org.multipage.generator.menuSlotProperties");
		menuSlotProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				helper.onSlotProperties();
			}
		});
		menuSlot.add(menuSlotProperties);
		
		panelAux = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, panelAux, 3, SpringLayout.EAST, menuBar);
		springLayout.putConstraint(SpringLayout.NORTH, panelEditor, 6, SpringLayout.SOUTH, panelAux);
		springLayout.putConstraint(SpringLayout.NORTH, panelAux, 10, SpringLayout.SOUTH, textHolder);
		springLayout.putConstraint(SpringLayout.EAST, panelAux, 0, SpringLayout.EAST, labelAlias);
		panelAux.setPreferredSize(new Dimension(10, 28));
		getContentPane().add(panelAux);
		panelAux.setLayout(new BoxLayout(panelAux, BoxLayout.X_AXIS));
		
		horizontalGlue = Box.createHorizontalGlue();
		panelAux.add(horizontalGlue);
		
		checkDefaultValue = new JCheckBox("org.multipage.generator.textSlotDefaultValue");
		checkDefaultValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				helper.onDefaultValue();
			}
		});
		checkDefaultValue.setFont(new Font("Tahoma", Font.PLAIN, 13));
		panelAux.add(checkDefaultValue);
		
		horizontalStrut_A = Box.createHorizontalStrut(20);
		panelAux.add(horizontalStrut_A);
		
		checkInterpretPhp = new JCheckBox("org.multipage.generator.textInterpretPhp");
		checkInterpretPhp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onInterpretPhp();
			}
		});
		checkInterpretPhp.setSelected(true);
		panelAux.add(checkInterpretPhp);
		
		Component horizontalStrut_B = Box.createHorizontalStrut(10);
		panelAux.add(horizontalStrut_B);
		
		checkLocalizedFlag = new JCheckBox("org.multipage.generator.textUserTextLocalized");
		checkLocalizedFlag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onUserLocalizedCheck();
			}
		});
		panelAux.add(checkLocalizedFlag);
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		panelAux.add(horizontalGlue_1);
		
		buttonHelp = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, textHolder, -3, SpringLayout.WEST, buttonHelp);
		buttonHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onHelp();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonHelp, 3, SpringLayout.SOUTH, labelSlotHolder);
		springLayout.putConstraint(SpringLayout.EAST, buttonHelp, 0, SpringLayout.EAST, buttonCancel);
		buttonHelp.setPreferredSize(new Dimension(25, 25));
		buttonHelp.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonHelp);
		
		labelSpecialValue = new JLabel("org.multipage.generator.textSlotSpecialValue");
		springLayout.putConstraint(SpringLayout.NORTH, labelSpecialValue, 6, SpringLayout.SOUTH, textHolder);
		springLayout.putConstraint(SpringLayout.WEST, labelSpecialValue, 0, SpringLayout.WEST, labelSlotHolder);
		getContentPane().add(labelSpecialValue);
		
		textSpecialValue = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.SOUTH, panelAux, 0, SpringLayout.SOUTH, textSpecialValue);
		
		buttonDisplay = new JButton("");
		buttonDisplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onDisplayHomePage();
			}
		});
		
		toggleDebug = new JToggleButton("");
		toggleDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onToggleDebugging(toggleDebug.isSelected());
			}
		});
		toggleDebug.setBorder(UIManager.getBorder("CheckBox.border"));
		toggleDebug.setMargin(new Insets(0, 0, 0, 0));
		panelAux.add(toggleDebug);
		
		horizontalStrut_2 = Box.createHorizontalStrut(3);
		horizontalStrut_2.setPreferredSize(new Dimension(3, 0));
		panelAux.add(horizontalStrut_2);
		
		buttonRender = new JButton("");
		buttonRender.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onRender();
			}
		});
		buttonRender.setMargin(new Insets(0, 0, 0, 0));
		panelAux.add(buttonRender);
		
		horizontalStrut_3 = Box.createHorizontalStrut(3);
		horizontalStrut_3.setPreferredSize(new Dimension(3, 0));
		panelAux.add(horizontalStrut_3);
		buttonDisplay.setMargin(new Insets(0, 0, 0, 0));
		panelAux.add(buttonDisplay);
		
		horizontalStrut_1 = Box.createHorizontalStrut(0);
		panelAux.add(horizontalStrut_1);
		springLayout.putConstraint(SpringLayout.NORTH, textSpecialValue, 3, SpringLayout.SOUTH, labelSpecialValue);
		springLayout.putConstraint(SpringLayout.WEST, textSpecialValue, 0, SpringLayout.WEST, labelSlotHolder);
		textSpecialValue.setColumns(10);
		getContentPane().add(textSpecialValue);
		
		buttonSpecialValue = new JButton("");
		buttonSpecialValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onSelectSpecialValue();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textSpecialValue, -3, SpringLayout.WEST, buttonSpecialValue);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSpecialValue, -3, SpringLayout.NORTH, textSpecialValue);
		springLayout.putConstraint(SpringLayout.EAST, buttonSpecialValue, 0, SpringLayout.EAST, buttonCancel);
		buttonSpecialValue.setPreferredSize(new Dimension(25, 25));
		buttonSpecialValue.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonSpecialValue);
		{
			labelInheritable = new JLabel("org.multipage.generator.textUserProviderInheritable");
			springLayout.putConstraint(SpringLayout.NORTH, labelInheritable, 6, SpringLayout.NORTH, buttonCancel);
			springLayout.putConstraint(SpringLayout.WEST, labelInheritable, 10, SpringLayout.WEST, this);
			getContentPane().add(labelInheritable);
		}
		
		buttonRevision = new JButton("org.multipage.generator.textRevisions");
		springLayout.putConstraint(SpringLayout.EAST, buttonRevision, -6, SpringLayout.WEST, buttonSave);
		buttonRevision.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, buttonRevision, 0, SpringLayout.NORTH, buttonCancel);
		buttonRevision.setPreferredSize(new Dimension(80, 25));
		buttonRevision.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onRevision();
			}
		});
		add(buttonRevision);
		
		buttonCommit = new JButton("org.multipage.generator.textCommit");
		springLayout.putConstraint(SpringLayout.EAST, buttonCommit, -6, SpringLayout.WEST, buttonRevision);
		buttonCommit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helper.onCommit();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonCommit, 0, SpringLayout.NORTH, buttonCancel);
		buttonCommit.setPreferredSize(new Dimension(80, 25));
		buttonCommit.setMargin(new Insets(0, 0, 0, 0));
		add(buttonCommit);
		
		buttonExpose = new JButton("org.multipage.generator.textExpose");
		buttonExpose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExpose();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonExpose, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonExpose, -30, SpringLayout.WEST, buttonCommit);
		buttonExpose.setPreferredSize(new Dimension(80, 25));
		buttonExpose.setMargin(new Insets(0, 0, 0, 0));
		add(buttonExpose);
	}
	
	/**
	 * On expose component
	 */
	protected void onExpose() {
		
		// TODO: expose component
		
	}
}
