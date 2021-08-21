/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 06-04-2020
 *
 */

package org.multipage.generator;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.maclan.Area;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldAutoSave;
import org.multipage.gui.TextFieldEx;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreaEditorPanel extends AreaEditorPanelBase {

	// $hide>>$
	/**
	 * Tab identifiers.
	 */
	public static final int NOT_SPECIFIED = -1;
	public static final int RESOURCES = 0;
	public static final int DEPENDENCIES = 1;

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
	private JTextField textIdentifier;
	private JTabbedPane tabbedPane;
	private JButton buttonSaveDescription;
	private JCheckBox checkBoxIsStartArea;
	private JLabel labelAreaAlias;
	private TextFieldAutoSave textAlias;
	private JButton buttonSaveAlias;
	private JPanel panelDependenciesAux;
	private JPanel panelResourcesAux;
	private JButton buttonSave;
	private Component horizontalStrut;
	private JLabel labelFileName;
	private TextFieldAutoSave textFileName;
	private JButton buttonSaveFileName;
	private TextFieldAutoSave textFolder;
	private JLabel labelFolder;
	private JButton buttonSaveFolder;
	private Component horizontalStrut_1;
	private JButton buttonUpdate;
	private TextFieldAutoSave textFileExtension;
	private JLabel labelFileExtension;
	private JCheckBox checkBoxIsDisabled;
	private JButton buttonDisplay;

	/**
	 * Show window.
	 * @param object
	 * @param first
	 * @param tabIdentifier
	 */
	public static void showDialog(JFrame parentFrame, Area area, int tabIdentifier) {
		
		if (dialog == null) {
			
			dialog = ProgramGenerator.newAreaEditorPanel(parentFrame, area);
			
			if (tabIdentifier != -1) {
				dialog.selectTab(tabIdentifier);
			}
 			
			dialog.setVisible(true);
			dialog = null;
		}
	}

	/**
	 * Show window.
	 */
	public static void showDialog(Component parentComponent, Area area) {
		
		if (dialog == null) {
			
			dialog = ProgramGenerator.newAreaEditorPanel(parentComponent, area);
			dialog.setVisible(true);
			dialog = null;
		}
	}

	/**
	 * Create the frame.
	 * @param parentComponent
	 * @param area 
	 */
	public AreaEditorPanel(Component parentComponent, Area area) {
		super(parentComponent, area);
		
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
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
		
		panelResourcesAux = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textAreaResourcesTab", null, panelResourcesAux, null);
		panelResourcesAux.setLayout(new BorderLayout(0, 0));
		
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
		sl_panel.putConstraint(SpringLayout.WEST, checkBoxIsStartArea, 106, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, checkBoxIsStartArea, -16, SpringLayout.NORTH, tabbedPane);

		panel.add(checkBoxIsStartArea);
		
		panelDependenciesAux = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textAreaDependenciesTab", null, panelDependenciesAux, null);
		panelDependenciesAux.setLayout(new BorderLayout(0, 0));
		
		JPanel panelConstructorsAux = new JPanel();
		tabbedPane.addTab("org.multipage.generator.textAreaConstructorsTab", null, panelConstructorsAux, null);
		
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
		
		labelFileName = new JLabel("org.multipage.generator.textAreaFileName2");
		labelFileName.setHorizontalAlignment(SwingConstants.RIGHT);
		sl_panel.putConstraint(SpringLayout.WEST, labelFileName, 0, SpringLayout.WEST, labelAreaDescription);
		sl_panel.putConstraint(SpringLayout.EAST, labelFileName, 0, SpringLayout.EAST, labelAreaDescription);
		panel.add(labelFileName);
		
		textFileName = new TextFieldAutoSave(AreaEditorCommonBase.fileName);
		sl_panel.putConstraint(SpringLayout.WEST, textFileName, 0, SpringLayout.WEST, textDescription);
		textFileName.setColumns(10);
		panel.add(textFileName);
		
		buttonSaveFileName = new JButton("");
		buttonSaveFileName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFileName();
				saveFileExtension();
			}
		});
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
		sl_panel.putConstraint(SpringLayout.EAST, labelFolder, -6, SpringLayout.WEST, textAlias);
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
		
		textFileExtension = new TextFieldAutoSave("FILEEXTENSION");
		sl_panel.putConstraint(SpringLayout.NORTH, textFileExtension, 0, SpringLayout.NORTH, labelFileName);
		sl_panel.putConstraint(SpringLayout.EAST, textFileExtension, 0, SpringLayout.EAST, textDescription);
		textFileExtension.setColumns(10);
		panel.add(textFileExtension);
		
		labelFileExtension = new JLabel("org.multipage.generator.textAreaFileExtension");
		sl_panel.putConstraint(SpringLayout.EAST, textFileName, -6, SpringLayout.WEST, labelFileExtension);
		sl_panel.putConstraint(SpringLayout.NORTH, labelFileExtension, 0, SpringLayout.NORTH, labelFileName);
		sl_panel.putConstraint(SpringLayout.EAST, labelFileExtension, -6, SpringLayout.WEST, textFileExtension);
		panel.add(labelFileExtension);
		
		checkBoxIsDisabled = new JCheckBox("org.multipage.generator.textDisable");
		sl_panel.putConstraint(SpringLayout.NORTH, checkBoxIsDisabled, 0, SpringLayout.NORTH, checkBoxIsStartArea);
		sl_panel.putConstraint(SpringLayout.WEST, checkBoxIsDisabled, 6, SpringLayout.EAST, checkBoxIsStartArea);
		panel.add(checkBoxIsDisabled);
		
		buttonDisplay = new JButton("");
		sl_panel.putConstraint(SpringLayout.EAST, buttonDisplay, -10, SpringLayout.EAST, textDescription);
		buttonDisplay.setSize(new Dimension(20, 20));
		sl_panel.putConstraint(SpringLayout.NORTH, buttonDisplay, 0, SpringLayout.NORTH, labelIdentifier);
		buttonDisplay.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonDisplay);
	}

	/**
	 * Insert tabs' contents.
	 */
	protected void insertTabsContents() {
		
		// Area resources panel.
		panelResources = new AreaResourcesEditor();
		panelResources.setArea(area);
		insertTabContent(panelResourcesAux, panelResources);

		// Area dependencies panel.
		panelDependencies = new AreaDependenciesPanel();
		panelDependencies.setArea(area);
		insertTabContent(panelDependenciesAux, panelDependencies);
	}
	
	/**
	 * Initialize display button.
	 */
	private void initDisplayButton() {
		
		// Set icon, tool tip and listener.
		buttonDisplay.setIcon(Images.getIcon("org/multipage/generator/images/display_home_page.png"));
		buttonDisplay.setToolTipText(Resources.getString("org.multipage.generator.tooltipDisplayHomePage"));
		
		buttonDisplay.addActionListener((e) -> {
			onDisplayHomePage();
		});
	}
	
	/**
	 * On display home page.
	 */
	private void onDisplayHomePage() {
		
		ConditionalEvents.transmit(this, Signal.monitorHomePage);
	}
	
	/**
	 * Post creation.
	 */
	@Override
	protected void postCreate() {
		
		// Call super class method.
		super.postCreate();
		
		// If it is a protected area disable alias.
		if (area.isProtected()) {
			textAlias.setEnabled(false);
		}
		
		// Initialize display button.
		initDisplayButton();
	}

	/**
	 * Get tabbed pane.
	 */
	@Override
	protected JTabbedPane getTabbedPane() {
		
		return tabbedPane;
	}


	/**
	 * Get text of the area identifier.
	 */
	@Override
	protected JTextField getTextIdentifier() {
		
		return textIdentifier;
	}
	
	/**
	 * Get text of the area description.
	 */
	@Override
	protected TextFieldAutoSave getTextDescription() {
		
		return textDescription;
	}
	
	/**
	 * Get text of the area alias.
	 */
	@Override
	protected TextFieldAutoSave getTextAlias() {
		
		return textAlias;
	}
	
	/**
	 * Get text of the area folder.
	 */
	@Override
	protected TextFieldAutoSave getTextFolder() {
		
		return textFolder;
	}

	/**
	 * Get text of the area file name.
	 */
	@Override
	protected TextFieldAutoSave getTextFileName() {
		
		return textFileName;
	}
	
	/**
	 * Get text of the area file extension.
	 */
	@Override
	protected TextFieldAutoSave getTextFileExtension() {
		
		return textFileExtension;
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
		
		return null;
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
	protected JLabel getLabelFileExtension() {
		
		return labelFileExtension;
	}

	@Override
	protected JCheckBox getCheckBoxIsDisabled() {
		
		return checkBoxIsDisabled;
	}

	@Override
	protected JCheckBox getCheckBoxHomeArea() {
		
		return null;
	}
}
