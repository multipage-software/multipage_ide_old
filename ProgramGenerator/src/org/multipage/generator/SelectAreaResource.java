/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.Area;
import com.maclan.VersionObj;

/**
 * @author
 *
 */
public class SelectAreaResource extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Area reference.
	 */
	private Area area;

	/**
	 * Start area reference.
	 */
	private Area startArea;
	
	/**
	 * Old text.
	 */
	public static String oldText = "";

	/**
	 * List modelAreas.
	 */
	private DefaultListModel modelAreas;

	/**
	 * Slots list model.
	 */
	private DefaultListModel modelResources;

	/**
	 * Do not update listAreas flag.
	 */
	private boolean doNotUpdateList = false;

	/**
	 * Output values.
	 */
	private Object areaText;
	private String resourceText;

	// $hide<<$
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JLabel labelAreaAlias;
	private JTextField textAreaAlias;
	private JScrollPane scrollPaneAreas;
	private JList listAreas;
	private JScrollPane scrollPaneResources;
	private JList listResources;
	private JSplitPane splitPane;

	/**
	 * SHow dialog.
	 * @param parent
	 * @param area 
	 * @return
	 */
	public static Object [] showDialog(Window parent, Area area) {
		
		SelectAreaResource dialog = new SelectAreaResource(parent, area);
		dialog.setVisible(true);
		
		if (!dialog.confirm ) {
			return null;
		}
		Object [] result = {dialog.areaText, dialog.resourceText};
		return result;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 * @param area 
	 */
	public SelectAreaResource(Window parent, Area area) {
		super(parent, ModalityType.DOCUMENT_MODAL);

		// Initialize components.
		initComponents();
		// $hide>>$
		this.area = area;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		setTitle("org.multipage.generator.textSelectAreaResource");
		setBounds(100, 100, 585, 409);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -96, SpringLayout.EAST, getContentPane());
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		labelAreaAlias = new JLabel("org.multipage.generator.textAreaAliasFilter");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaAlias, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelAreaAlias, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelAreaAlias);
		
		textAreaAlias = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textAreaAlias, 6, SpringLayout.SOUTH, labelAreaAlias);
		springLayout.putConstraint(SpringLayout.WEST, textAreaAlias, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textAreaAlias, -10, SpringLayout.EAST, getContentPane());
		textAreaAlias.setColumns(10);
		getContentPane().add(textAreaAlias);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -6, SpringLayout.NORTH, buttonOk);
		splitPane.setResizeWeight(0.5);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 6, SpringLayout.SOUTH, textAreaAlias);
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, labelAreaAlias);
		springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(splitPane);
		
		scrollPaneAreas = new JScrollPane();
		splitPane.setLeftComponent(scrollPaneAreas);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneAreas, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneAreas, 0, SpringLayout.WEST, labelAreaAlias);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneAreas, -258, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneAreas, -153, SpringLayout.EAST, textAreaAlias);
		
		listAreas = new JList();
		listAreas.setForeground(new Color(0, 128, 0));
		listAreas.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				onSelectList();
			}
		});
		scrollPaneAreas.setViewportView(listAreas);
		
		scrollPaneResources = new JScrollPane();
		splitPane.setRightComponent(scrollPaneResources);
		
		listResources = new JList();
		listResources.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					onOk();
				}
			}
		});
		listResources.setForeground(new Color(255, 0, 0));
		scrollPaneResources.setViewportView(listResources);
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Get selected area and resource.
		areaText = (String) listAreas.getSelectedValue();
		if (areaText.equals(Resources.getString("org.multipage.generator.textThisArea"))) {
			areaText = (Integer) 1;
		}
		else if (areaText.equals(Resources.getString("org.multipage.generator.textStartArea"))) {
			areaText = (Integer) 2;
		}
		resourceText = (String) listResources.getSelectedValue();
		if (resourceText == null) {
			Utility.show(this, "org.multipage.generator.messageSelectAreaAndSlot");
			return;
		}
		
		confirm = true;
		dispose();
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		// Center dialog.
		Utility.centerOnScreen(this);
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Load start area.
		loadStartArea();
		// Create and update listAreas.
		createAreasList();
		updateAreasList();
		// Create resources list.
		createResourcesList();
		// Set text listeners.
		setTextListener();
		// Set old text.
		textAreaAlias.setText(oldText);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
		Utility.localize(labelAreaAlias);
	}

	/**
	 * Create listAreas.
	 */
	private void createAreasList() {
		
		modelAreas = new DefaultListModel();
		listAreas.setModel(modelAreas);
	}
	
	/**
	 * Create resources list.
	 */
	private void createResourcesList() {
		
		modelResources = new DefaultListModel();
		listResources.setModel(modelResources);
	}
	
	/**
	 * Set text listener.
	 */
	private void setTextListener() {
		
		textAreaAlias.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onTextChanged();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onTextChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onTextChanged();
			}
		});
	}

	/**
	 * On text changed.
	 */
	protected void onTextChanged() {

		if (!doNotUpdateList) {
			oldText = textAreaAlias.getText();
		}
		updateAreasList();
	}
	
	/**
	 * Update listAreas.
	 */
	private void updateAreasList() {
		
		if (doNotUpdateList) {
			return;
		}
		
		// Reset modelAreas.
		modelAreas.clear();
		// Get areas.
		LinkedList<Area> areas = ProgramGenerator.getAreasModel().getAreas();
		// Get inserted text.
		String text = textAreaAlias.getText();
		
		// Load area aliases.
		LinkedList<String> aliases = new LinkedList<String>();
		for (Area area : areas) {
			String alias = area.getAlias();
			if (!alias.isEmpty()) {
				
				if (!text.isEmpty() && !alias.startsWith(text)) {
					continue;
				}
				if (area.getResourceNamesCount() == 0) {
					continue;
				}
				aliases.add(alias);
			}
		}
		// Sort aliases.
		Collections.sort(aliases);
		
		// Add to modelAreas.
		for (String alias : aliases) {
			modelAreas.addElement(alias);
		}
		
		// Insert start area, if it exists.
		if (startArea != null) {
			modelAreas.add(0,  Resources.getString("org.multipage.generator.textStartArea"));
		}
		
		// Insert this area at the begin..
		if (area != null) {
			modelAreas.add(0, Resources.getString("org.multipage.generator.textThisArea"));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					listAreas.setSelectedIndex(0);
				}
			});
		}
	}
	
	/**
	 * On select list.
	 */
	protected void onSelectList() {
		
		// Get area alias.
		String areaAlias = (String) listAreas.getSelectedValue();
		// Update resources list.
		updateResourcesList(areaAlias);
	}
	
	/**
	 * Update resources list.
	 * @param areaAlias 
	 */
	private void updateResourcesList(String areaAlias) {
		
		// Reset list.
		modelResources.clear();
		if (areaAlias == null) {
			return;
		}
		Area area;
		// Get area.
		if (areaAlias.equals(Resources.getString("org.multipage.generator.textThisArea"))) {
			area = this.area;
		}
		else if (areaAlias.equals(Resources.getString("org.multipage.generator.textStartArea"))) {
			area = startArea;
		}
		else {
			area = ProgramGenerator.getAreasModel().getArea(areaAlias);
		}
		if (area == null) {
			return;
		}
		
		// Load resource names.
		LinkedList<String> resourceNames = new LinkedList<String>();
		resourceNames.addAll(area.getResourceNames());
		// Sort the list.
		Collections.sort(resourceNames);
		
		// Insert to model.
		for (String resourceName : resourceNames) {
			modelResources.addElement(resourceName);
		}
	}

	/**
	 * Load start area.
	 */
	private void loadStartArea() {
		
		// Select version.
		Obj<VersionObj> version = new Obj<VersionObj>();
		if (!SelectVersionDialog.showDialog(null, version)) {
			return;
		}
		
		startArea = ProgramGenerator.getAreasModel().getStartArea(area, version.ref.getId());
	}
}
