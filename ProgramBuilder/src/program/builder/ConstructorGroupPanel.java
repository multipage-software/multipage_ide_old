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
import org.multipage.generator.ProgramGenerator;
import org.multipage.generator.SelectSubAreaDialog;
import org.maclan.*;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;

/**
 * @author
 *
 */
public class ConstructorGroupPanel extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor group.
	 */
	private ConstructorGroup constructorGroup;
	
	/**
	 * List model.
	 */
	private DefaultListModel model;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelConstructorList;
	private JScrollPane scrollPane;
	private JList list;
	private JLabel labelExtensionArea;
	private JTextField textExtensionArea;
	private JButton buttonClearArea;
	private JButton buttonSelectArea;
	private JLabel labelGroupAlias;
	private JTextField textGroupAlias;

	/**
	 * Constructor.
	 */
	public ConstructorGroupPanel() {
		
		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelConstructorList = new JLabel("builder.textListOfGroupConstructors");
		springLayout.putConstraint(SpringLayout.WEST, labelConstructorList, 10, SpringLayout.WEST, this);
		add(labelConstructorList);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelConstructorList);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, this);
		add(scrollPane);
		
		list = new JList();
		scrollPane.setViewportView(list);
		
		labelExtensionArea = new JLabel("builder.textExtensionAreaLink");
		springLayout.putConstraint(SpringLayout.NORTH, labelExtensionArea, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelExtensionArea, 10, SpringLayout.WEST, this);
		add(labelExtensionArea);
		
		textExtensionArea = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textExtensionArea, 6, SpringLayout.SOUTH, labelExtensionArea);
		springLayout.putConstraint(SpringLayout.WEST, textExtensionArea, 10, SpringLayout.WEST, this);
		textExtensionArea.setPreferredSize(new Dimension(6, 25));
		textExtensionArea.setEditable(false);
		add(textExtensionArea);
		textExtensionArea.setColumns(10);
		
		buttonClearArea = new JButton("");
		buttonClearArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClearExtensionArea();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonClearArea, -10, SpringLayout.EAST, this);
		buttonClearArea.setPreferredSize(new Dimension(25, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonClearArea, 30, SpringLayout.NORTH, this);
		add(buttonClearArea);
		
		buttonSelectArea = new JButton("");
		buttonSelectArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onSelectExtensionArea();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textExtensionArea, 0, SpringLayout.WEST, buttonSelectArea);
		springLayout.putConstraint(SpringLayout.NORTH, buttonSelectArea, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, buttonSelectArea, 0, SpringLayout.WEST, buttonClearArea);
		buttonSelectArea.setPreferredSize(new Dimension(25, 25));
		add(buttonSelectArea);
		
		labelGroupAlias = new JLabel("builder.textConstructorGroupAlias");
		springLayout.putConstraint(SpringLayout.NORTH, labelGroupAlias, 20, SpringLayout.SOUTH, textExtensionArea);
		springLayout.putConstraint(SpringLayout.NORTH, labelConstructorList, 30, SpringLayout.SOUTH, labelGroupAlias);
		springLayout.putConstraint(SpringLayout.WEST, labelGroupAlias, 0, SpringLayout.WEST, labelConstructorList);
		add(labelGroupAlias);
		
		textGroupAlias = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textGroupAlias, -3, SpringLayout.NORTH, labelGroupAlias);
		textGroupAlias.setPreferredSize(new Dimension(6, 25));
		springLayout.putConstraint(SpringLayout.WEST, textGroupAlias, 6, SpringLayout.EAST, labelGroupAlias);
		springLayout.putConstraint(SpringLayout.EAST, textGroupAlias, 0, SpringLayout.EAST, scrollPane);
		add(textGroupAlias);
		textGroupAlias.setColumns(10);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		setToolTips();
		initializeTable();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelConstructorList);
		Utility.localize(labelExtensionArea);
		Utility.localize(labelGroupAlias);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonSelectArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
		buttonClearArea.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonSelectArea.setToolTipText(Resources.getString("builder.tooltipSelectExtensionArea"));
		buttonClearArea.setToolTipText(Resources.getString("builder.tooltipClearExtensionArea"));
	}

	/**
	 * Set constructor group.
	 * @param constructorGroup
	 */
	public void setConstructorGroup(ConstructorGroup constructorGroup) {
		
		this.constructorGroup = constructorGroup;
		
		loadList();
		loadExtensionArea();
		
		textGroupAlias.setText(constructorGroup.getAlias());
	}

	/**
	 * Initialize table.
	 */
	private void initializeTable() {
		
		model = new DefaultListModel<ConstructorHolder>();
		list.setModel(model);
	}

	/**
	 * Load list.
	 */
	private void loadList() {
		
		model.clear(); 
		
		for (ConstructorHolder constructorHolder : constructorGroup.getConstructorHolders()) {
			
			if (constructorHolder.isInvisible()) {
				continue;
			}
			
			ConstructorHolder linkedConstructorHolder = constructorHolder.getLinkedConstructorHolder();
			
			model.addElement(linkedConstructorHolder != null ? 
					linkedConstructorHolder : constructorHolder);
		}
	}
	
	/**
	 * Set root area.
	 */
	public void setRootArea(Area rootArea) {
		
		// TODO: Set area reference.
	}

	/**
	 * Load extension area.
	 */
	private void loadExtensionArea() {
		
		// Reset text field.
		textExtensionArea.setText("");
		
		if (constructorGroup == null) {
			return;
		}
		
		Long extensionAreaId = constructorGroup.getExtensionAreaId();
		if (extensionAreaId != null) {
			
			Area extensionArea = ProgramGenerator.getArea(extensionAreaId);
			if (extensionArea != null) {
				
				// Set text field.
				textExtensionArea.setText(extensionArea.getDescriptionForDiagram());
			}
		}
	}
	
	/**
	 * Select extension area.
	 */
	protected void onSelectExtensionArea() {

		// Get root area.
		Area rootArea = ProgramGenerator.getArea(0L);
		
		// Get extension area.
		Area extensionArea = rootArea;
		Long extensionAreaId = constructorGroup.getExtensionAreaId();
		if (extensionAreaId != null) {
			
			Area foundExtensionArea = ProgramGenerator.getArea(extensionAreaId);
			if (foundExtensionArea != null) {
				extensionArea = foundExtensionArea;
			}
		}
		
		// Select constructor area.
		Area selectedArea = SelectSubAreaDialog.showDialog(this, rootArea, extensionArea);
		if (selectedArea == null) {
			return;
		}
		
		// Set constructor group extension area ID and save it.
		constructorGroup.setExtensionAreaId(selectedArea.getId());
		save();
		loadExtensionArea();
	}

	/**
	 * On clear extension area.
	 */
	protected void onClearExtensionArea() {

		// Reset constructor group extension area ID and save it.
		constructorGroup.setExtensionAreaId(null);
		save();
		loadExtensionArea();
	}

	/**
	 * Save constructor group.
	 */
	private void save() {
		
		if (constructorGroup == null) {
			return;
		}
		
		// Get constructor group extension area ID and alias and save it.
		Long extensionAreaId = constructorGroup.getExtensionAreaId();
		
		String alias = textGroupAlias.getText();
		constructorGroup.setAlias(alias);
		alias = constructorGroup.getAliasNull();
		
		// Prepare prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			
			long constructorGroupId = constructorGroup.getId();
		
			result = middle.updateConstructorGroupExtension(
					constructorGroupId, extensionAreaId);
			
			if (result.isOK()) {
				
				result = middle.updateConstructorGroupAlias(
					constructorGroupId, alias);
			}
		}
		
		MiddleResult logoutResult = middle.logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
			
		if (result.isNotOK()) {
			result.show(this);
		}
	}

	/**
	 * Save constructor group.
	 */
	public void saveConstructorGroup() {
		
		save();
	}
}
