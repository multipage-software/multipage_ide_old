/*
 * Copyright 2020 (C) Vaclav Kolarcik
 * 
 * Created on : 06-04-2020
 *
 */

package program.builder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.AreaEditorCommonBase;
import org.multipage.generator.AreaEditorPanelBase;
import org.multipage.generator.AreaResourcesEditor;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;

import org.maclan.Area;
import org.maclan.MiddleResult;

/**
 * 
 * @author
 *
 */
public class AreaEditorBuilderPanel extends AreaEditorPanelBase {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L; 

	/**
	 * Tab identifiers.
	 */
	public static final int NOT_SPECIFIED = -1;
	public static final int INHERITANCE = 0;
	public static final int RESOURCES = 1;
	public static final int START_RESOURCE = 2;
	public static final int DEPENDENCIES = 3;
	public static final int CONSTRUCTORS = 4;
	public static final int HELP = 5;

	/**
	 * Area help editor panel
	 */
	private AreaHelpEditor panelAreaHelpEditor;
	
	/**
	 * Constructors panel.
	 */
	private ConstructorsPanel panelConstructors;
	
	/**
	 * Inheritance panel.
	 */
	private AreaInheritancePanel panelInheritance;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel resourcesPane;
	private JPanel bottomPanel;
	private JButton buttonClose;
	private Component horizontalGlue;
	private JPanel panel;
	private JLabel labelAreaDescription;
	private TextFieldAutoSave textDescription;
	private JLabel labelIdentifier;
	private TextFieldEx textIdentifier;
	private JTabbedPane tabbedPane;
	private JButton buttonSaveDescription;
	private AreaStartPanel panelStart;
	private JCheckBox checkBoxIsStartArea;
	private JCheckBox checkBoxVisible;
	private JLabel labelAreaAlias;
	private TextFieldAutoSave textAlias;
	private JButton buttonSaveAlias;
	private JPanel panelStartAux;

	private JPanel panelDependenciesAux;

	private JPanel panelResourcesAux;
	private JCheckBox checkReadOnly;
	private JPanel panelHelpAux;
	private JButton buttonSave;
	private Component horizontalStrut;
	private JCheckBox checkLocalized;
	private JLabel labelFileName;
	private TextFieldAutoSave textFileName;
	private JButton buttonSaveFileName;
	private TextFieldAutoSave textFolder;
	private JLabel labelFolder;
	private JButton buttonSaveFolder;
	private JPanel panelInheritanceAux;
	private Component horizontalStrut_1;
	private JButton buttonUpdate;
	private JPanel panelConstructorsAux;
	private TextFieldAutoSave textFileExtension;
	private JLabel labelFileExtension;
	private JCheckBox checkCanImport;
	private JCheckBox checkProjectRoot;
	private JCheckBox checkBoxIsDisabled;

	/**
	 * Create the frame.
	 * @param parentComponent 
	 * @param area 
	 */
	public AreaEditorBuilderPanel(Component parentComponent, Area area) {
		super(parentComponent, area);
		
		// Initialize components.
		initComponents();
		// $hide>>$
		this.area = area;
		this.parentComponent = parentComponent;
		// Post creation.
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		this.getWindowLambda.get().setMinimumSize(new Dimension(530, 360));
		this.getWindowLambda.get().setBounds(100, 100, 764, 622);
		setLayout(new BorderLayout(0, 0));
		resourcesPane = new JPanel();
		resourcesPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(resourcesPane);
		resourcesPane.setLayout(new BorderLayout(0, 0));
		
		bottomPanel = new JPanel();
		resourcesPane.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		horizontalGlue = Box.createHorizontalGlue();
		bottomPanel.add(horizontalGlue);
		
		buttonClose = new JButton("org.multipage.generator.textSaveAndClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		
		buttonSave = new JButton("org.multipage.generator.textSave");
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSave();
			}
		});
		
		buttonUpdate = new JButton("org.multipage.generator.textUpdate");
		buttonUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUpdate();
			}
		});
		buttonUpdate.setPreferredSize(new Dimension(80, 25));
		buttonUpdate.setMargin(new Insets(2, 4, 2, 4));
		bottomPanel.add(buttonUpdate);
		
		horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setPreferredSize(new Dimension(40, 0));
		bottomPanel.add(horizontalStrut_1);
		buttonSave.setPreferredSize(new Dimension(80, 25));
		buttonSave.setMargin(new Insets(2, 4, 2, 4));
		bottomPanel.add(buttonSave);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		bottomPanel.add(horizontalStrut);
		buttonClose.setPreferredSize(new Dimension(110, 25));
		buttonClose.setMargin(new Insets(2, 4, 2, 4));
		bottomPanel.add(buttonClose);
		
		panel = new JPanel();
		resourcesPane.add(panel, BorderLayout.CENTER);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		labelAreaDescription = new JLabel("org.multipage.generator.textAreaDescription");
		sl_panel.putConstraint(SpringLayout.WEST, labelAreaDescription, 0, SpringLayout.WEST, panel);
		panel.add(labelAreaDescription);
		
		textDescription = new TextFieldAutoSave(AreaEditorCommonBase.description);
		sl_panel.putConstraint(SpringLayout.NORTH, labelAreaDescription, 0, SpringLayout.NORTH, textDescription);
		textDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onDescriptionEnter();
				}
			}
		});
		sl_panel.putConstraint(SpringLayout.WEST, textDescription, 6, SpringLayout.EAST, labelAreaDescription);
		panel.add(textDescription);
		textDescription.setColumns(10);
		
		labelIdentifier = new JLabel("org.multipage.generator.textIdentifier");
		sl_panel.putConstraint(SpringLayout.EAST, labelIdentifier, 0, SpringLayout.EAST, labelAreaDescription);
		panel.add(labelIdentifier);
		
		textIdentifier = new TextFieldEx();
		sl_panel.putConstraint(SpringLayout.NORTH, textDescription, 16, SpringLayout.SOUTH, textIdentifier);
		sl_panel.putConstraint(SpringLayout.NORTH, textIdentifier, 7, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, textIdentifier, 6, SpringLayout.EAST, labelIdentifier);
		sl_panel.putConstraint(SpringLayout.EAST, textIdentifier, 100, SpringLayout.EAST, labelIdentifier);
		sl_panel.putConstraint(SpringLayout.NORTH, labelIdentifier, 3, SpringLayout.NORTH, textIdentifier);
		textIdentifier.setEditable(false);
		panel.add(textIdentifier);
		textIdentifier.setColumns(10);
		
		tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		sl_panel.putConstraint(SpringLayout.NORTH, tabbedPane, 193, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, panel);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onTabChanged();
			}
		});
		tabbedPane.setOpaque(true);
		panel.add(tabbedPane);
		
		panelInheritanceAux = new JPanel();
		tabbedPane.addTab("builder.textInheritanceTab", null, panelInheritanceAux, null);
		panelInheritanceAux.setLayout(new BorderLayout(0, 0));
		
		panelResourcesAux = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textAreaResourcesTab", null, panelResourcesAux, null);
		panelResourcesAux.setLayout(new BorderLayout(0, 0));

		panelStartAux = new JPanel();
		tabbedPane.addTab("builder.textAreaStart", null, panelStartAux, null);
		panelStartAux.setLayout(new BorderLayout(0, 0));
		
		buttonSaveDescription = new JButton("");
		sl_panel.putConstraint(SpringLayout.EAST, textDescription, -3, SpringLayout.WEST, buttonSaveDescription);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSaveDescription, 0, SpringLayout.NORTH, textDescription);
		sl_panel.putConstraint(SpringLayout.EAST, buttonSaveDescription, -10, SpringLayout.EAST, panel);
		buttonSaveDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDescription();
			}
		});
		buttonSaveDescription.setIconTextGap(0);
		buttonSaveDescription.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveDescription.setPreferredSize(new Dimension(20, 20));
		panel.add(buttonSaveDescription);
		
		checkBoxIsStartArea = new JCheckBox("org.multipage.generator.textIsHomeArea");
		sl_panel.putConstraint(SpringLayout.WEST, checkBoxIsStartArea, 86, SpringLayout.WEST, panel);
		checkBoxIsStartArea.setPreferredSize(new Dimension(80, 23));
		sl_panel.putConstraint(SpringLayout.SOUTH, checkBoxIsStartArea, -16, SpringLayout.NORTH, tabbedPane);

		panel.add(checkBoxIsStartArea);
		
		panelDependenciesAux = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textAreaDependenciesTab", null, panelDependenciesAux, null);
		panelDependenciesAux.setLayout(new BorderLayout(0, 0));
		
		panelConstructorsAux = new JPanel();
		tabbedPane.addTab("builder.textConstructors", null, panelConstructorsAux, null);
		panelConstructorsAux.setLayout(new BorderLayout(0, 0));
		
		panelHelpAux = new JPanel();
		tabbedPane.addTab("builder.textHelpTab", null, panelHelpAux, null);
		panelHelpAux.setLayout(new BorderLayout(0, 0));

		checkBoxVisible = new JCheckBox("builder.textIsVisible");
		checkBoxVisible.setPreferredSize(new Dimension(80, 23));
		sl_panel.putConstraint(SpringLayout.WEST, checkBoxVisible, 6, SpringLayout.EAST, checkBoxIsStartArea);
		sl_panel.putConstraint(SpringLayout.SOUTH, checkBoxVisible, -16, SpringLayout.NORTH, tabbedPane);
		checkBoxVisible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsVisibleAction();
			}
		});
		panel.add(checkBoxVisible);
		
		labelAreaAlias = new JLabel("org.multipage.generator.textAreaAlias");
		panel.add(labelAreaAlias);
		
		textAlias = new TextFieldAutoSave(AreaEditorCommonBase.alias);
		sl_panel.putConstraint(SpringLayout.WEST, textAlias, 6, SpringLayout.EAST, labelAreaDescription);
		sl_panel.putConstraint(SpringLayout.NORTH, labelAreaAlias, 0, SpringLayout.NORTH, textAlias);
		sl_panel.putConstraint(SpringLayout.NORTH, textAlias, 66, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, textDescription, -3, SpringLayout.NORTH, textAlias);
		sl_panel.putConstraint(SpringLayout.EAST, labelAreaAlias, -6, SpringLayout.WEST, textAlias);
		sl_panel.putConstraint(SpringLayout.EAST, textAlias, -23, SpringLayout.EAST, buttonSaveDescription);
		panel.add(textAlias);
		textAlias.setColumns(10);
		
		buttonSaveAlias = new JButton("");
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSaveAlias, 0, SpringLayout.NORTH, textAlias);
		sl_panel.putConstraint(SpringLayout.WEST, buttonSaveAlias, 0, SpringLayout.WEST, buttonSaveDescription);
		buttonSaveAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAlias();
			}
		});
		buttonSaveAlias.setPreferredSize(new Dimension(20, 20));
		buttonSaveAlias.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveAlias.setIconTextGap(0);
		panel.add(buttonSaveAlias);
		
		checkReadOnly = new JCheckBox("org.multipage.generator.textReadOnly");
		checkReadOnly.setPreferredSize(new Dimension(80, 23));
		sl_panel.putConstraint(SpringLayout.WEST, checkReadOnly, 6, SpringLayout.EAST, checkBoxVisible);
		sl_panel.putConstraint(SpringLayout.SOUTH, checkReadOnly, -16, SpringLayout.NORTH, tabbedPane);
		checkReadOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onIsReadOnlyAction();
			}
		});
		panel.add(checkReadOnly);
		
		checkLocalized = new JCheckBox("org.multipage.generator.textLocalized");
		checkLocalized.setPreferredSize(new Dimension(80, 23));
		sl_panel.putConstraint(SpringLayout.SOUTH, checkLocalized, -16, SpringLayout.NORTH, tabbedPane);
		checkLocalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLocalizedAction();
			}
		});
		sl_panel.putConstraint(SpringLayout.WEST, checkLocalized, 6, SpringLayout.EAST, checkReadOnly);
		panel.add(checkLocalized);
		
		labelFileName = new JLabel("org.multipage.generator.textAreaFileName");
		labelFileName.setHorizontalAlignment(SwingConstants.RIGHT);
		sl_panel.putConstraint(SpringLayout.WEST, labelFileName, 0, SpringLayout.WEST, labelAreaDescription);
		sl_panel.putConstraint(SpringLayout.EAST, labelFileName, 0, SpringLayout.EAST, labelAreaDescription);
		panel.add(labelFileName);
		
		textFileName = new TextFieldAutoSave(AreaEditorCommonBase.fileName);
		sl_panel.putConstraint(SpringLayout.WEST, textFileName, 0, SpringLayout.WEST, textDescription);
		textFileName.setColumns(10);
		panel.add(textFileName);
		
		buttonSaveFileName = new JButton("");

		sl_panel.putConstraint(SpringLayout.WEST, buttonSaveFileName, 0, SpringLayout.WEST, buttonSaveDescription);
		buttonSaveFileName.setPreferredSize(new Dimension(20, 20));
		buttonSaveFileName.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveFileName.setIconTextGap(0);
		panel.add(buttonSaveFileName);
		
		textFolder = new TextFieldAutoSave(AreaEditorCommonBase.folder);
		sl_panel.putConstraint(SpringLayout.NORTH, textFileName, 3, SpringLayout.SOUTH, textFolder);
		sl_panel.putConstraint(SpringLayout.NORTH, textFolder, 3, SpringLayout.SOUTH, textAlias);
		textFolder.setColumns(10);
		panel.add(textFolder);
		
		labelFolder = new JLabel("org.multipage.generator.textAreasFolder");
		sl_panel.putConstraint(SpringLayout.EAST, labelFolder, 0, SpringLayout.EAST, labelAreaAlias);
		sl_panel.putConstraint(SpringLayout.WEST, textFolder, 6, SpringLayout.EAST, labelFolder);
		sl_panel.putConstraint(SpringLayout.NORTH, labelFileName, 9, SpringLayout.SOUTH, labelFolder);
		sl_panel.putConstraint(SpringLayout.NORTH, labelFolder, 0, SpringLayout.NORTH, textFolder);
		sl_panel.putConstraint(SpringLayout.WEST, labelFolder, 0, SpringLayout.WEST, labelAreaDescription);
		labelFolder.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(labelFolder);
		
		buttonSaveFolder = new JButton("");
		sl_panel.putConstraint(SpringLayout.EAST, textFolder, -3, SpringLayout.WEST, buttonSaveFolder);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSaveFileName, 3, SpringLayout.SOUTH, buttonSaveFolder);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSaveFolder, 0, SpringLayout.NORTH, textFolder);
		sl_panel.putConstraint(SpringLayout.WEST, buttonSaveFolder, 0, SpringLayout.WEST, buttonSaveDescription);
		buttonSaveFolder.setPreferredSize(new Dimension(20, 20));
		buttonSaveFolder.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveFolder.setIconTextGap(0);
		panel.add(buttonSaveFolder);
		
		textFileExtension = new TextFieldAutoSave(AreaEditorCommonBase.fileExtension);
		sl_panel.putConstraint(SpringLayout.NORTH, textFileExtension, 0, SpringLayout.NORTH, labelFileName);
		sl_panel.putConstraint(SpringLayout.EAST, textFileExtension, 0, SpringLayout.EAST, textDescription);
		textFileExtension.setColumns(10);
		panel.add(textFileExtension);
		
		labelFileExtension = new JLabel("org.multipage.generator.textAreaFileExtension");
		sl_panel.putConstraint(SpringLayout.EAST, textFileName, -6, SpringLayout.WEST, labelFileExtension);
		sl_panel.putConstraint(SpringLayout.NORTH, labelFileExtension, 0, SpringLayout.NORTH, labelFileName);
		sl_panel.putConstraint(SpringLayout.EAST, labelFileExtension, -6, SpringLayout.WEST, textFileExtension);
		panel.add(labelFileExtension);
		
		checkCanImport = new JCheckBox("builder.textCanImport");
		checkCanImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCanImportAction();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, checkCanImport, 0, SpringLayout.NORTH, checkBoxIsStartArea);
		sl_panel.putConstraint(SpringLayout.WEST, checkCanImport, 6, SpringLayout.EAST, checkLocalized);
		checkCanImport.setPreferredSize(new Dimension(80, 23));
		panel.add(checkCanImport);
		
		checkProjectRoot = new JCheckBox("builder.textProjectRoot");
		checkProjectRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onProjectRootAction();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, checkProjectRoot, 0, SpringLayout.NORTH, checkBoxIsStartArea);
		sl_panel.putConstraint(SpringLayout.WEST, checkProjectRoot, 6, SpringLayout.EAST, checkCanImport);
		checkProjectRoot.setPreferredSize(new Dimension(90, 23));
		panel.add(checkProjectRoot);
		
		checkBoxIsDisabled = new JCheckBox("org.multipage.generator.textDisable");
		sl_panel.putConstraint(SpringLayout.NORTH, checkBoxIsDisabled, 0, SpringLayout.NORTH, labelIdentifier);
		sl_panel.putConstraint(SpringLayout.EAST, checkBoxIsDisabled, 0, SpringLayout.EAST, buttonSaveDescription);
		panel.add(checkBoxIsDisabled);
	}
	
	/**
	 * On close.
	 */
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Insert tabs' contents.
	 */
	protected void insertTabsContents() {
			
		// Area inheritance editor.
		panelInheritance = new AreaInheritancePanel();
		panelInheritance.setArea(area);
		insertTabContent(panelInheritanceAux, panelInheritance);
		
		// Area resources panel.
		panelResources = new AreaResourcesEditor();
		panelResources.setArea(area);
		insertTabContent(panelResourcesAux, panelResources);
		
		// Area start resource panel.
		panelStart = new AreaStartPanel();
		panelStart.setReferences(area, panelResources);
		insertTabContent(panelStartAux, panelStart);
		
		// Area dependencies panel.
		panelDependencies = new AreaDependenciesPanelBuilder();
		panelDependencies.setArea(area);
		insertTabContent(panelDependenciesAux, panelDependencies);

		// Area help editor.
		panelAreaHelpEditor = new AreaHelpEditor();
		panelAreaHelpEditor.setArea(area);
		insertTabContent(panelHelpAux, panelAreaHelpEditor);
		
		// Area constructors editor.
		panelConstructors = new ConstructorsPanel();
		panelConstructors.setArea(area);
		insertTabContent(panelConstructorsAux, panelConstructors);
	}

	/**
	 * Post creation.
	 */
	@Override
	protected void postCreate() {
		
		// Set visible check box.
		checkBoxVisible.setSelected(area.isVisible());
		// Set read only check box.
		checkReadOnly.setSelected(area.isReadOnly());
		// Set localized check box.
		checkLocalized.setSelected(area.isLocalized());
		
		checkCanImport.setSelected(area.canImport());
		checkProjectRoot.setSelected(area.isProjectRoot());
		
		// Call super class method.
		super.postCreate();
	}


	/**
	 * Localize dialog.
	 */
	@Override
	protected void localize() {

		super.localize();
		
		Utility.localize(checkLocalized);
		Utility.localize(checkReadOnly);
		Utility.localize(checkBoxVisible);
		Utility.localize(checkCanImport);
		Utility.localize(checkProjectRoot);
	}

	/**
	 * On visible flag change.
	 */
	protected void onIsVisibleAction() {

		// Get state.
		boolean visible = checkBoxVisible.isSelected();
		
		// Update area visibility.
		MiddleResult result = ProgramBasic.getMiddle().updateAreaVisibility(
				ProgramBasic.getLoginProperties(), area.getId(),
				visible);
		if (result.isNotOK()) {
			result.show(this.getWindowLambda.get());
			return;
		}

		// Update information.
		updateInformation();
		
		// Set file name components.
		setFileNameComponents();
	}

	/**
	 * On read only flag changed.
	 */
	protected void onIsReadOnlyAction() {

		// Get state.
		boolean readOnly = checkReadOnly.isSelected();
		
		// Update area read only flag.
		MiddleResult result = ProgramBasic.getMiddle().updateAreaReadOnly(
				ProgramBasic.getLoginProperties(), area.getId(),
				readOnly);
		if (result.isNotOK()) {
			result.show(this.getWindowLambda.get());
			return;
		}

		// Update information.
		updateInformation();
	}

	/**
	 * On localized flag changed.
	 */
	protected void onLocalizedAction() {
		
		// Get state.
		boolean localized = checkLocalized.isSelected();
		
		// Update localized flag.
		MiddleResult result = ProgramBasic.getMiddle().updateAreaLocalized(
				ProgramBasic.getLoginProperties(), area.getId(),
				localized);
		
		if (result.isNotOK()) {
			result.show(this.getWindowLambda.get());
			return;
		}

		// Update information.
		updateInformation();
	}

	/**
	 * On can import flag changed.
	 */
	protected void onCanImportAction() {
		
		// Get state.
		boolean canImport = checkCanImport.isSelected();
		
		// Update can import flag.
		MiddleResult result = ProgramBasic.getMiddle().updateAreaCanImport(
				ProgramBasic.getLoginProperties(), area.getId(),
				canImport);
		
		if (result.isNotOK()) {
			result.show(this.getWindowLambda.get());
			return;
		}

		// Update information.
		updateInformation();
	}

	/**
	 * On project root flag changed.
	 */
	protected void onProjectRootAction() {
		
		// Get state.
		boolean projectRoot = checkProjectRoot.isSelected();
		
		// Update project root flag.
		MiddleResult result = ProgramBasic.getMiddle().updateAreaProjectRoot(
				ProgramBasic.getLoginProperties(), area.getId(),
				projectRoot);
		
		if (result.isNotOK()) {
			result.show(this.getWindowLambda.get());
			return;
		}

		// Update information.
		updateInformation();
	}

	private void updateInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Get tabbed pane.
	 */
	@Override
	protected JTabbedPane getTabbedPane() {
		
		return tabbedPane;
	}

	/**
	 * Get text description.
	 */
	@Override
	protected TextFieldAutoSave getTextDescription() {
		
		return textDescription;
	}
	

	@Override
	protected TextFieldAutoSave getTextAlias() {
		
		return textAlias;
	}

	@Override
	protected TextFieldAutoSave getTextFileName() {
		
		return textFileName;
	}

	@Override
	protected TextFieldAutoSave getTextFolder() {
		
		return textFolder;
	}

	@Override
	protected TextFieldEx getTextIdentifier() {
		
		return textIdentifier;
	}

	@Override
	protected JButton getButtonSaveFileName() {
		
		return buttonSaveFileName;
	}

	@Override
	protected JButton getButtonSaveDescription() {
		
		return buttonSaveDescription;
	}

	@Override
	protected JButton getButtonSaveAlias() {
		
		return buttonSaveAlias;
	}

	@Override
	protected JButton getButtonClose() {
		
		return buttonClose;
	}

	@Override
	protected JButton getButtonSaveFolder() {
		
		return buttonSaveFolder;
	}

	@Override
	protected JButton getButtonSave() {
		
		return buttonSave;
	}

	@Override
	protected JButton getButtonUpdate() {
		
		return buttonUpdate;
	}

	@Override
	protected JCheckBox getCheckBoxVisible() {
		
		return checkBoxVisible;
	}

	@Override
	protected JCheckBox getCheckBoxIsStartArea() {
		
		return checkBoxIsStartArea;
	}

	@Override
	protected JLabel getLabelIdentifier() {
		
		return labelIdentifier;
	}

	@Override
	protected JLabel getLabelAreaDescription() {
		
		return labelAreaDescription;
	}

	@Override
	protected JLabel getLabelAreaAlias() {
		
		return labelAreaAlias;
	}

	@Override
	protected JLabel getLabelFileName() {
		
		return labelFileName;
	}

	@Override
	protected JLabel getLabelFolder() {
		
		return labelFolder;
	}

	@Override
	protected TextFieldAutoSave getTextFileExtension() {
		
		return textFileExtension;
	}

	@Override
	protected JLabel getLabelFileExtension() {
		
		return labelFileExtension;
	}

	@Override
	protected JCheckBox getCheckBoxIsDisabled() {
		return checkBoxIsDisabled;
	}

	@Override
	protected JCheckBox getCheckBoxHomeArea() {
		// TODO Auto-generated method stub
		return null;
	}
}
