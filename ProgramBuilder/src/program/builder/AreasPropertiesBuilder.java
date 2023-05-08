/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;


import org.multipage.gui.*;
import org.multipage.util.Resources;

import java.awt.*;

import javax.swing.*;

import org.multipage.generator.*;
import org.maclan.Area;

import java.awt.event.*;

/**
 * @author
 *
 */
public class AreasPropertiesBuilder extends AreasPropertiesBase {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JLabel labelAreaDescription;
	private TextFieldAutoSave textDescription;
	private JButton buttonSaveDescription;
	private JMenuBar menuBar;
	private JMenu menuArea;
	private JMenuItem menuEditResources;
	private JButton buttonDeleteDescription;
	private JSplitPane splitPane;
	private SlotListPanel panelSlotList;
	private JButton buttonEditor;
	private JLabel labelAreaAlias;
	private TextFieldAutoSave textAlias;
	private JButton buttonSaveAlias;
	private JPanel panelExtension;
	private JMenuItem menuEditDependencies;
	private JMenuItem menuEditInheritance;
	private JMenuItem menuEditStartResource;
	private JMenuItem menuEditConstructors;
	private JMenuItem menuEditHelp;
	private JMenuItem menuAreaEdit;

	/**
	 * Create the panel.
	 */
	public AreasPropertiesBuilder(boolean isPropertiesPanel) {

		this.isPropertiesPanel = isPropertiesPanel;
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		panelSlotList = ProgramGenerator.newSlotListPanel();
		splitPane.setLeftComponent(panelSlotList);
		
		menuArea.setIcon(Images.getIcon("org/multipage/generator/images/edit.png"));
		
		setComponentsReferences(
				labelAreaDescription,
				textDescription,
				buttonSaveDescription,
				menuArea,
				menuEditResources,
				buttonDeleteDescription,
				splitPane,
				panelSlotList,
				labelAreaAlias,
				textAlias,
				buttonSaveAlias,
				panelExtension,
				menuEditDependencies,
				menuAreaEdit);
		
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelAreaDescription = new JLabel("org.multipage.generator.textAreaDescription");
		springLayout.putConstraint(SpringLayout.WEST, labelAreaDescription, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, labelAreaDescription, -10, SpringLayout.EAST, this);
		add(labelAreaDescription);
		
		textDescription = new TextFieldAutoSave(AreaEditorCommonBase.description);
		springLayout.putConstraint(SpringLayout.NORTH, textDescription, 6, SpringLayout.SOUTH, labelAreaDescription);
		springLayout.putConstraint(SpringLayout.WEST, textDescription, 10, SpringLayout.WEST, this);
		add(textDescription);
		textDescription.setColumns(10);
		
		buttonSaveDescription = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, textDescription, -3, SpringLayout.WEST, buttonSaveDescription);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveDescription, 6, SpringLayout.SOUTH, labelAreaDescription);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSaveDescription, 0, SpringLayout.SOUTH, textDescription);
		buttonSaveDescription.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveDescription.setPreferredSize(new Dimension(18, 18));
		add(buttonSaveDescription);
		
		menuBar = new JMenuBar();
		menuBar.setPreferredSize(new Dimension(0, 24));
		springLayout.putConstraint(SpringLayout.EAST, menuBar, 0, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaDescription, 10, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.SOUTH, labelAreaDescription, 26, SpringLayout.SOUTH, menuBar);
		springLayout.putConstraint(SpringLayout.NORTH, menuBar, 0, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, this);
		add(menuBar);
		
		menuArea = new JMenu("org.multipage.generator.menuArea");
		menuBar.add(menuArea);
		
		menuEditResources = new JMenuItem("org.multipage.generator.menuAreaEditResources");
		menuEditResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.RESOURCES);
			}
		});
		
		menuEditInheritance = new JMenuItem("builder.menuAreaEditInheritance");
		menuEditInheritance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.INHERITANCE);
			}
		});
		
		menuAreaEdit = new JMenuItem("org.multipage.generator.menuAreaEdit");
		menuAreaEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.NOT_SPECIFIED);
			}
		});
		menuArea.add(menuAreaEdit);
		menuArea.addSeparator();
		menuArea.add(menuEditInheritance);
		menuArea.add(menuEditResources);
		
		menuEditStartResource = new JMenuItem("builder.menuAreaEditStartResource");
		menuEditStartResource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.START_RESOURCE);
			}
		});
		menuArea.add(menuEditStartResource);
		
		menuEditDependencies = new JMenuItem("org.multipage.generator.menuAreaEditDependencies");
		menuEditDependencies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.DEPENDENCIES);
			}
		});
		menuArea.add(menuEditDependencies);
		
		menuEditConstructors = new JMenuItem("builder.menuAreaEditConstructors");
		menuEditConstructors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.CONSTRUCTORS);
			}
		});
		menuArea.add(menuEditConstructors);
		
		menuEditHelp = new JMenuItem("builder.menuAreaEditHelp");
		menuEditHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditArea(AreaEditorBuilder.HELP);
			}
		});
		menuArea.add(menuEditHelp);
		
		buttonDeleteDescription = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveDescription, -3, SpringLayout.WEST, buttonDeleteDescription);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonDeleteDescription, 0, SpringLayout.SOUTH, textDescription);
		buttonDeleteDescription.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteLocalText();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonDeleteDescription, 6, SpringLayout.SOUTH, labelAreaDescription);
		buttonDeleteDescription.setPreferredSize(new Dimension(18, 18));
		buttonDeleteDescription.setMargin(new Insets(0, 0, 0, 0));
		add(buttonDeleteDescription);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, -10, SpringLayout.EAST, this);
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		buttonEditor = new JButton("");
		buttonEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditStartResource();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonDeleteDescription, -3, SpringLayout.WEST, buttonEditor);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonEditor, 0, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.NORTH, buttonEditor, 6, SpringLayout.SOUTH, labelAreaDescription);
		springLayout.putConstraint(SpringLayout.EAST, buttonEditor, -10, SpringLayout.EAST, this);
		buttonEditor.setPreferredSize(new Dimension(18, 18));
		buttonEditor.setMargin(new Insets(0, 0, 0, 0));
		add(buttonEditor);
		
		labelAreaAlias = new JLabel("org.multipage.generator.textAreaAlias2");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaAlias, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.WEST, labelAreaAlias, 10, SpringLayout.WEST, this);
		add(labelAreaAlias);
		
		textAlias = new TextFieldAutoSave(AreaEditorCommonBase.alias);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 6, SpringLayout.SOUTH, textAlias);
		
		panelExtension = new JPanel();
		splitPane.setRightComponent(panelExtension);
		panelExtension.setLayout(new BorderLayout(0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 6, SpringLayout.EAST, labelAreaAlias);
		springLayout.putConstraint(SpringLayout.EAST, textAlias, 0, SpringLayout.EAST, buttonDeleteDescription);
		add(textAlias);
		textAlias.setColumns(10);
		
		buttonSaveAlias = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveAlias, 0, SpringLayout.NORTH, textAlias);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonSaveAlias, 0, SpringLayout.SOUTH, textAlias);
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveAlias, 0, SpringLayout.EAST, labelAreaDescription);
		buttonSaveAlias.setPreferredSize(new Dimension(18, 18));
		buttonSaveAlias.setMargin(new Insets(0, 0, 0, 0));
		add(buttonSaveAlias);
		

	}
	
	/**
	 * Set icons.
	 */
	@Override
	protected void setIcons() {
		
		super.setIcons();
		
		buttonEditor.setIcon(Images.getIcon("org/multipage/generator/images/edit_text.png"));
	}
	
	/**
	 * Set tool tips.
	 */
	@Override
	protected void setToolTips() {

		super.setToolTips();
		
        buttonEditor.setToolTipText(Resources.getString("builder.tooltipEditAreaStartResource"));
	}
	
	
	/**
	 * On edit start resource.
	 */
	protected void onEditStartResource() {
		
		// Check area.
		if (areas.size() != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return;
		}
		
		// Get area and edit start resource.
		Area area = areas.getFirst();
		ProgramBuilder.editStartResource(area, false);
	}

	/* (non-Javadoc)
	 * @see org.multipage.generator.AreasPropertiesBase#localize()
	 */
	@Override
	protected void localize() {
		
		super.localize();
		
		Utility.localize(menuEditConstructors);
		Utility.localize(menuEditHelp);
		Utility.localize(menuEditInheritance);
		Utility.localize(menuEditStartResource);
	}
}
