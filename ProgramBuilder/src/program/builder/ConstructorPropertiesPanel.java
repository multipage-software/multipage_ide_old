/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.Resources;

import javax.swing.*;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.*;
import org.maclan.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.LineBorder;

/**
 * 
 * @author
 *
 */
public class ConstructorPropertiesPanel extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor area reference.
	 */
	private Area constructorArea;

	/**
	 * Name change listener.
	 */
	private Runnable nameChangeListener;

	/**
	 * Constructor holder reference.
	 */
	private ConstructorHolder constructorHolder;

	/**
	 * Constructor holder link if it exists.
	 */
	private ConstructorHolder constructorHolderLink;

	/**
	 * Disable changes flag.
	 */
	private boolean disableChanges = false;
	
	/**
	 * Slot list panel.
	 */
	private SlotListPanelBuilder slotListPanel;
	
	/**
	 * Area resources editor.
	 */
	private AreaResourcesEditor areaResourcesEditor;

	/**
	 * Root constructor group reference.
	 */
	private ConstructorGroup rootConstructorGroup;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelConstructorHolderName;
	private TextFieldEx textConstructorHolderName;
	private JCheckBox checkInheritance;
	private JLabel labelRelationSuperName;
	private TextFieldEx textSuperRelationName;
	private JLabel labelRelationSubName;
	private TextFieldEx textSubRelationName;
	private JLabel labelConstructorAreaReference;
	private TextFieldEx textArea;
	private JButton buttonSelectArea;
	private JTabbedPane tabbedPane;
	private JPanel panelProperties;
	private JPanel panelSlots;
	private JPanel panelResources;
	private JButton buttonEditArea;
	private JButton buttonUpdate;
	private JPanel panel;
	private RelatedAreaPanel panelRelatedArea;
	private JCheckBox checkAskForRelatedArea;
	private JPanel panel_1;
	private JLabel labelSubgroupAlias;
	private JTextField textSubgroupAlias;
	private JCheckBox checkInvisible;
	private JTextField textAlias;
	private JLabel labelAlias;
	private JButton buttonSaveAlias;
	private JCheckBox checkSetHome;

	/**
	 * Create the panel.
	 * @param nameChangeListener 
	 * @param rootConstructorGroup 
	 * @param rootArea 
	 */
	public ConstructorPropertiesPanel(Runnable nameChangeListener, ConstructorGroup rootConstructorGroup) {

		initComponents();
		
		 // $hide>>$
		this.rootConstructorGroup = rootConstructorGroup;
		
		postCreate();
		this.nameChangeListener = nameChangeListener;
		// $hide<<$
	}

	/**
	 * Set root area.
	 * @param rootArea
	 */
	public void setRootArea(Area rootArea) {
		
		// TODO: Set root area reference.
	}

	/**
	 * Fire name change.
	 */
	private void fireNameChangeListener() {
		
		if (nameChangeListener != null) {
			nameChangeListener.run();
		}
	}
	
	/**
	 * Initialize components.s
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelConstructorHolderName = new JLabel("builder.textConstructorName");
		springLayout.putConstraint(SpringLayout.NORTH, labelConstructorHolderName, 10, SpringLayout.NORTH, this);
		add(labelConstructorHolderName);
		
		textConstructorHolderName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.EAST, labelConstructorHolderName, -3, SpringLayout.WEST, textConstructorHolderName);
		springLayout.putConstraint(SpringLayout.NORTH, textConstructorHolderName, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, textConstructorHolderName, 104, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, textConstructorHolderName, -10, SpringLayout.EAST, this);
		textConstructorHolderName.setColumns(20);
		add(textConstructorHolderName);
		
		labelConstructorAreaReference = new JLabel("builder.textConstructorAreaReference");
		add(labelConstructorAreaReference);
		
		textArea = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 0, SpringLayout.NORTH, labelConstructorAreaReference);
		springLayout.putConstraint(SpringLayout.EAST, labelConstructorAreaReference, -3, SpringLayout.WEST, textArea);
		springLayout.putConstraint(SpringLayout.WEST, textArea, 0, SpringLayout.WEST, textConstructorHolderName);
		textArea.setEditable(false);
		textArea.setPreferredSize(new Dimension(6, 25));
		textArea.setMinimumSize(new Dimension(6, 25));
		textArea.setColumns(20);
		add(textArea);
		
		buttonSelectArea = new JButton("builder.textSelectConstructorArea");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSelectArea, 0, SpringLayout.NORTH, textArea);
		springLayout.putConstraint(SpringLayout.EAST, buttonSelectArea, -10, SpringLayout.EAST, this);
		buttonSelectArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectArea();
			}
		});
		buttonSelectArea.setMargin(new Insets(0, 0, 0, 0));
		buttonSelectArea.setPreferredSize(new Dimension(70, 25));
		add(buttonSelectArea);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 30, SpringLayout.SOUTH, textArea);
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -10, SpringLayout.EAST, this);
		add(tabbedPane);
		
		panelProperties = new JPanel();
		tabbedPane.addTab("builder.textConstructorProperties", null, panelProperties, null);
		SpringLayout sl_panelProperties = new SpringLayout();
		panelProperties.setLayout(sl_panelProperties);
		
		checkInheritance = new JCheckBox("builder.textConstructorAreaInheritance");
		sl_panelProperties.putConstraint(SpringLayout.NORTH, checkInheritance, 20, SpringLayout.NORTH, panelProperties);
		sl_panelProperties.putConstraint(SpringLayout.WEST, checkInheritance, 180, SpringLayout.WEST, panelProperties);
		springLayout.putConstraint(SpringLayout.NORTH, checkInheritance, 10, SpringLayout.NORTH, panelProperties);
		springLayout.putConstraint(SpringLayout.WEST, checkInheritance, 10, SpringLayout.WEST, panelProperties);
		panelProperties.add(checkInheritance);
		
		labelRelationSubName = new JLabel("builder.textConstructorSubName");
		sl_panelProperties.putConstraint(SpringLayout.NORTH, labelRelationSubName, 20, SpringLayout.SOUTH, checkInheritance);
		sl_panelProperties.putConstraint(SpringLayout.EAST, labelRelationSubName, 70, SpringLayout.WEST, panelProperties);
		springLayout.putConstraint(SpringLayout.NORTH, labelRelationSubName, 65, SpringLayout.SOUTH, checkInheritance);
		springLayout.putConstraint(SpringLayout.WEST, labelRelationSubName, 0, SpringLayout.WEST, checkInheritance);
		panelProperties.add(labelRelationSubName);
		
		textSubRelationName = new TextFieldEx();
		sl_panelProperties.putConstraint(SpringLayout.NORTH, textSubRelationName, -3, SpringLayout.NORTH, labelRelationSubName);
		sl_panelProperties.putConstraint(SpringLayout.WEST, textSubRelationName, 6, SpringLayout.EAST, labelRelationSubName);
		sl_panelProperties.putConstraint(SpringLayout.EAST, textSubRelationName, -10, SpringLayout.EAST, panelProperties);
		springLayout.putConstraint(SpringLayout.NORTH, textSubRelationName, 6, SpringLayout.SOUTH, checkInheritance);
		springLayout.putConstraint(SpringLayout.WEST, textSubRelationName, 0, SpringLayout.WEST, textConstructorHolderName);
		springLayout.putConstraint(SpringLayout.EAST, textSubRelationName, -10, SpringLayout.EAST, this);
		textSubRelationName.setColumns(10);
		panelProperties.add(textSubRelationName);
		
		labelRelationSuperName = new JLabel("builder.textConstructorSuperName");
		sl_panelProperties.putConstraint(SpringLayout.NORTH, labelRelationSuperName, 24, SpringLayout.SOUTH, labelRelationSubName);
		sl_panelProperties.putConstraint(SpringLayout.EAST, labelRelationSuperName, 70, SpringLayout.WEST, panelProperties);
		springLayout.putConstraint(SpringLayout.NORTH, labelRelationSuperName, 44, SpringLayout.SOUTH, textSubRelationName);
		springLayout.putConstraint(SpringLayout.WEST, labelRelationSuperName, 107, SpringLayout.WEST, panelProperties);
		panelProperties.add(labelRelationSuperName);
		
		textSuperRelationName = new TextFieldEx();
		sl_panelProperties.putConstraint(SpringLayout.NORTH, textSuperRelationName, -3, SpringLayout.NORTH, labelRelationSuperName);
		sl_panelProperties.putConstraint(SpringLayout.WEST, textSuperRelationName, 6, SpringLayout.EAST, labelRelationSubName);
		sl_panelProperties.putConstraint(SpringLayout.EAST, textSuperRelationName, -10, SpringLayout.EAST, panelProperties);
		springLayout.putConstraint(SpringLayout.NORTH, textSuperRelationName, 61, SpringLayout.SOUTH, labelRelationSuperName);
		springLayout.putConstraint(SpringLayout.WEST, textSuperRelationName, -115, SpringLayout.WEST, textConstructorHolderName);
		springLayout.putConstraint(SpringLayout.EAST, textSuperRelationName, 0, SpringLayout.EAST, panelProperties);
		panelProperties.add(textSuperRelationName);
		textSuperRelationName.setColumns(10);
		
		labelSubgroupAlias = new JLabel("builder.textConstructorSubgroupAlias");
		sl_panelProperties.putConstraint(SpringLayout.NORTH, labelSubgroupAlias, 24, SpringLayout.SOUTH, textSuperRelationName);
		sl_panelProperties.putConstraint(SpringLayout.EAST, labelSubgroupAlias, 50, SpringLayout.EAST, labelRelationSubName);
		panelProperties.add(labelSubgroupAlias);
		
		textSubgroupAlias = new TextFieldEx();
		sl_panelProperties.putConstraint(SpringLayout.NORTH, textSubgroupAlias, -3, SpringLayout.NORTH, labelSubgroupAlias);
		sl_panelProperties.putConstraint(SpringLayout.WEST, textSubgroupAlias, 6, SpringLayout.EAST, labelSubgroupAlias);
		sl_panelProperties.putConstraint(SpringLayout.EAST, textSubgroupAlias, -50, SpringLayout.EAST, textSubRelationName);
		panelProperties.add(textSubgroupAlias);
		textSubgroupAlias.setColumns(10);
		
		panelSlots = new JPanel();
		tabbedPane.addTab("builder.textEditSlots", null, panelSlots, null);
		panelSlots.setLayout(new BorderLayout(0, 0));
		
		panelResources = new JPanel();
		tabbedPane.addTab("builder.textEditResources", null, panelResources, null);
		panelResources.setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		tabbedPane.addTab("builder.textEditRelatedArea", null, panel, null);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		panelRelatedArea = new RelatedAreaPanel();
		sl_panel.putConstraint(SpringLayout.NORTH, panelRelatedArea, 47, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, panelRelatedArea, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, panelRelatedArea, 72, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, panelRelatedArea, -10, SpringLayout.EAST, panel);
		panel.add(panelRelatedArea);
		
		panel_1 = new JPanel();
		sl_panel.putConstraint(SpringLayout.NORTH, panel_1, 23, SpringLayout.SOUTH, panelRelatedArea);
		sl_panel.putConstraint(SpringLayout.WEST, panel_1, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, panel_1, -10, SpringLayout.EAST, panel);
		panel.add(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		checkAskForRelatedArea = new JCheckBox("builder.textAskForRelatedArea");
		panel_1.add(checkAskForRelatedArea);
		sl_panel.putConstraint(SpringLayout.WEST, checkAskForRelatedArea, 102, SpringLayout.WEST, panelRelatedArea);
		sl_panel.putConstraint(SpringLayout.SOUTH, checkAskForRelatedArea, -48, SpringLayout.SOUTH, panel);

		
		buttonEditArea = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonEditArea, 0, SpringLayout.NORTH, textArea);
		buttonEditArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEditConstructorArea();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonEditArea, 0, SpringLayout.WEST, buttonSelectArea);
		buttonEditArea.setPreferredSize(new Dimension(25, 25));
		buttonEditArea.setMargin(new Insets(0, 0, 0, 0));
		add(buttonEditArea);
		
		buttonUpdate = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonUpdate, 0, SpringLayout.NORTH, labelConstructorAreaReference);
		buttonUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateAreaDisplay();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textArea, 0, SpringLayout.WEST, buttonUpdate);
		springLayout.putConstraint(SpringLayout.EAST, buttonUpdate, 0, SpringLayout.WEST, buttonEditArea);
		buttonUpdate.setPreferredSize(new Dimension(25, 25));
		buttonUpdate.setMargin(new Insets(0, 0, 0, 0));
		add(buttonUpdate);
		
		checkInvisible = new JCheckBox("builder.textConstructorInvisible");
		springLayout.putConstraint(SpringLayout.NORTH, checkInvisible, 6, SpringLayout.SOUTH, textArea);
		springLayout.putConstraint(SpringLayout.WEST, checkInvisible, 0, SpringLayout.WEST, textConstructorHolderName);
		add(checkInvisible);
		
		textAlias = new TextFieldEx();
		textAlias.setPreferredSize(new Dimension(6, 22));
		textAlias.setBorder(new LineBorder(new Color(171, 173, 179)));
		springLayout.putConstraint(SpringLayout.NORTH, labelConstructorAreaReference, 3, SpringLayout.SOUTH, textAlias);
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 3, SpringLayout.SOUTH, textConstructorHolderName);
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 0, SpringLayout.WEST, textConstructorHolderName);
		add(textAlias);
		textAlias.setColumns(10);
		
		labelAlias = new JLabel("builder.textConstructorAlias");
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 0, SpringLayout.NORTH, textAlias);
		springLayout.putConstraint(SpringLayout.EAST, labelAlias, 0, SpringLayout.EAST, labelConstructorHolderName);
		add(labelAlias);
		
		buttonSaveAlias = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveAlias, 0, SpringLayout.NORTH, textAlias);
		buttonSaveAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSaveAlias();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textAlias, 0, SpringLayout.WEST, buttonSaveAlias);
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveAlias, -10, SpringLayout.EAST, this);
		buttonSaveAlias.setPreferredSize(new Dimension(22, 22));
		buttonSaveAlias.setMargin(new Insets(0, 0, 0, 0));
		add(buttonSaveAlias);
		
		checkSetHome = new JCheckBox("builder.textSetHome");
		springLayout.putConstraint(SpringLayout.NORTH, checkSetHome, 6, SpringLayout.SOUTH, textArea);
		springLayout.putConstraint(SpringLayout.WEST, checkSetHome, 6, SpringLayout.EAST, checkInvisible);
		add(checkSetHome);
	}

	/**
	 * Post create.
	 */
	private void postCreate() {
		
		createPanels();
		
		localize();
		setIcons();
		setToolTips();
		
		setListeners();
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonSaveAlias.setToolTipText(Resources.getString("builder.tooltipSaveConstructorAlias"));
	}

	/**
	 * Create panels.
	 */
	@SuppressWarnings("serial")
	private void createPanels() {
		
		slotListPanel = new SlotListPanelBuilder() {
			@Override
			protected void onChange() {
				// On change update information.
				updatePanelInformation();
			}
		};
		
		slotListPanel.setDividerPositionToMaximum();
		slotListPanel.setDoNotSaveStateOnExit();
		
		panelSlots.add(slotListPanel);
		
		areaResourcesEditor = new AreaResourcesEditor();
		panelResources.add(areaResourcesEditor);
	}

	protected void updatePanelInformation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * On save alias.
	 */
	protected void onSaveAlias() {

		if (constructorHolder != null) {
			
			String alias = textAlias.getText();
			/*if (!alias.isEmpty()) {
				
				Utility.show(this, "builder.messageEmptyConstructorAlias");
				return;
			}*/
			
			// Update constructor holder alias.
			Properties login = ProgramBasic.getLoginProperties();
			Middle middle = ProgramBasic.getMiddle();
			
			MiddleResult result = middle.updateConstructorHolderAlias(login, constructorHolder.getId(), alias);
			if (result.isNotOK()) {
				
				result.show(this);
				return;
			}
			
			constructorHolder.setAlias(alias);
			textAlias.setForeground(Color.BLACK);
			textAlias.setBorder(new LineBorder(new Color(171, 173, 179)));
		}
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
				
		// Set name listener.
		Utility.setTextChangeListener(textConstructorHolderName, new Runnable() {
			@Override
			public void run() {
				
				if (disableChanges) {
					return;
				}
				
				// Set constructor holder name and fire change listener.
				if (constructorHolder != null) {
					
					constructorHolder.setName(textConstructorHolderName.getText());
				}
				
				fireNameChangeListener();
			}
		});
		
		// Set alias listener.
		Utility.setTextChangeListener(textAlias, () -> {
			
			if (disableChanges) {
				return;
			}
			
			// Set constructor holder name and fire change listener.
			if (constructorHolder != null) {
				
				textAlias.setForeground(Color.RED);
				textAlias.setBorder(new LineBorder(Color.RED));
			}
		});
		
		// Set inheritance listener.
		checkInheritance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (disableChanges) {
					return;
				}
				
				if (constructorHolder != null) {
					constructorHolder.setInheritance(checkInheritance.isSelected());
				}
			}
		});
		
		// Set sub relation name listener.
		Utility.setTextChangeListener(textSubRelationName, new Runnable() {
			@Override
			public void run() {
				
				if (disableChanges) {
					return;
				}
				
				if (constructorHolder != null) {
					
					if (isLink()) {
						constructorHolderLink.setSubRelationName(textSubRelationName.getText());
					}
					else {
						constructorHolder.setSubRelationName(textSubRelationName.getText());
					}
				}
			}
		});
		
		// Set super relation name listener.
		Utility.setTextChangeListener(textSuperRelationName, new Runnable() {
			@Override
			public void run() {
				
				if (disableChanges) {
					return;
				}
				
				if (constructorHolder != null) {
					
					if (isLink()) {
						constructorHolderLink.setSuperRelationName(textSuperRelationName.getText());
					}
					else {
						constructorHolder.setSuperRelationName(textSuperRelationName.getText());
					}
				}
			}
		});
		
		// Set ask for related area listener.
		checkAskForRelatedArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (disableChanges) {
					return;
				}
				
				if (constructorHolder != null) {
					constructorHolder.setAskForRelatedArea(checkAskForRelatedArea.isSelected());
				}
			}
		});
		
		// Set sub group alias.
		Utility.setTextChangeListener(textSubgroupAlias, new Runnable() {
			@Override
			public void run() {
				
				if (disableChanges) {
					return;
				}
				
				if (constructorHolder != null) {
					constructorHolder.setSubGroupAliases(textSubgroupAlias.getText());
				}
			}
		});
		
		// Set invisibility flag.
		checkInvisible.addActionListener((ActionEvent e) -> {
			
			if (disableChanges) {
				return;
			}
			
			if (constructorHolder != null) {
				constructorHolder.setInvisible(checkInvisible.isSelected());
				
				fireNameChangeListener();
			}
		});
		

		// Set home flag.
		checkSetHome.addActionListener((ActionEvent e) -> {
			
			if (disableChanges) {
				return;
			}
			
			if (constructorHolder != null) {
				constructorHolder.setHome(checkSetHome.isSelected());
				
				fireNameChangeListener();
			}
		});
	}

	/**
	 * Returns true value if it is a link.
	 */
	private boolean isLink() {
		
		return constructorHolderLink != null;
	}
	
	/**
	 * Use possible link to a constructor.
	 * @return
	 */
	private ConstructorHolder usePossibleLink() {
		
		return isLink() ? constructorHolderLink : constructorHolder;
	}
	
	/**
	 * Set constructor holder.
	 * @param constructorHolder
	 */
	public void setConstructorHolder(ConstructorHolder constructorHolder) {
		
		this.constructorHolder = constructorHolder;
		this.constructorHolderLink = null;
		
		// If it is a link, use it.
		if (constructorHolder.isLinkId() && constructorHolder.isLinkObject()) {
			 
			this.constructorHolder = constructorHolder.getLinkedConstructorHolder();

			// Remember the link.
			this.constructorHolderLink = constructorHolder;
		}
		
		disableChanges = true;
		
		// Set editor components.
		textConstructorHolderName.setText(this.constructorHolder.getName());
		textAlias.setText(this.constructorHolder.getAlias());
		checkInheritance.setSelected(this.constructorHolder.isInheritance());
		textSubRelationName.setText(usePossibleLink().getSubRelationName());
		textSuperRelationName.setText(usePossibleLink().getSuperRelationName());
		textSubgroupAlias.setText(this.constructorHolder.getSubGroupAliases());
		checkAskForRelatedArea.setSelected(this.constructorHolder.isAskForRelatedArea());
		checkInvisible.setSelected(this.constructorHolder.isInvisible());
		checkSetHome.setSelected(this.constructorHolder.isSetHome());
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				disableChanges = false;
			}
		});
		
		// Set constructor area.
		setConstructorArea(this.constructorHolder.getAreaId());
		
		textAlias.setForeground(Color.BLACK);
		textAlias.setBorder(new LineBorder(new Color(171, 173, 179)));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelConstructorHolderName);
		Utility.localize(checkInheritance);
		Utility.localize(labelRelationSubName);
		Utility.localize(labelRelationSuperName);
		Utility.localize(labelConstructorAreaReference);
		Utility.localize(buttonSelectArea);
		Utility.localize(tabbedPane);
		Utility.localize(checkAskForRelatedArea);
		Utility.localize(labelSubgroupAlias);
		Utility.localize(checkInvisible);
		Utility.localize(labelAlias);
		Utility.localize(checkSetHome);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonSelectArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
		tabbedPane.setIconAt(0, Images.getIcon("org/multipage/generator/images/properties.png"));
		tabbedPane.setIconAt(1, Images.getIcon("org/multipage/generator/images/slot_icon.png"));
		tabbedPane.setIconAt(2, Images.getIcon("org/multipage/generator/images/resources_icon_gray.png"));
		tabbedPane.setIconAt(3, Images.getIcon("org/multipage/generator/images/area_related.png"));
		buttonEditArea.setIcon(Images.getIcon("org/multipage/generator/images/edit.png"));
		buttonUpdate.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		buttonSaveAlias.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
	}

	/**
	 * On select area.
	 */
	protected void onSelectArea() {
		
		Area rootArea = ProgramGenerator.getArea(0L);
		
		// Select constructor area.
		Area selectedArea = SelectSubAreaDialog.showDialog(this, rootArea, constructorArea);
		if (selectedArea == null) {
			return;
		}
		
		constructorArea = selectedArea;
		
		// Set constructor holder area ID.
		constructorHolder.setAreaId(constructorArea.getId());
		
		updateAreaDisplay();
	}

	/**
	 * Set constructor area.
	 * @param areaId
	 */
	private void setConstructorArea(long areaId) {
		
		constructorArea = ProgramBuilder.getArea(areaId);

		updateAreaDisplay();
	}

	/**
	 * Update area display.
	 */
	private void updateAreaDisplay() {
		
		// Set area text component, slot list and resource list.
		String areaText;
		LinkedList<Area> areas = new LinkedList<Area>();
		
		if (constructorArea != null) {
			
			areaText = constructorArea.getDescriptionForDiagram();
			areas.add(constructorArea);

		}
		else {
			areaText = "";
		}
		
		textArea.setText(areaText);
		
		// Set slot list.
		slotListPanel.setAreas(areas);
	
		// Set area resources' list.
		areaResourcesEditor.loadArea(constructorArea);
		
		// Set related area panel.
		panelRelatedArea.setArea(constructorArea);
	}

	/**
	 * Disable changes.
	 */
	public void disableChanges() {
	
		textConstructorHolderName.setEnabled(false);
		textAlias.setEnabled(false);
		checkInheritance.setEnabled(false);
		textSubRelationName.setEnabled(false);
		textSuperRelationName.setEnabled(false);
	}

	/**
	 * On edit constructor area.
	 */
	protected void onEditConstructorArea() {
		
		// Update constructor area.
		if (constructorArea != null) {
			constructorArea = ProgramGenerator.getArea(constructorArea.getId());
		}
		
		// Execute area editor.
		AreaEditorFrame.showDialog(null, constructorArea);
	}

	public void stopEditing() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Save dialog.
	 */
	public void saveDialog() {
		// TODO Auto-generated method stub
	}

	/**
	 * Save constructor holder.
	 */
	public void saveConstructorHolder() {
		
		if (constructorHolder == null) {
			return;
		}
		
		// Prepare prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Update constructor holder.
		MiddleResult result = middle.updateConstructorHolderProperties(login, constructorHolder);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Update linked constructor holder if it exists.
		if (isLink()) {
			
			result = middle.updateConstructorHolderProperties(login, constructorHolderLink);
			if (result.isNotOK()) {
				result.show(this);
			}
		}
	}
}
