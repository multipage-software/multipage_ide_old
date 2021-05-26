/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

import com.maclan.Area;
import com.maclan.AreasModel;

/**
 * @author
 *
 */
public class SelectAreaDialog extends JDialog {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Old text.
	 */
	public static String oldText = "";
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * List model.
	 */
	private DefaultListModel model;

	/**
	 * Do not update listAreas flag.
	 */
	private boolean doNotUpdateList = false;

	/**
	 * Current area reference.
	 */
	private Area area;

	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelAreaAlias;
	private JTextField textAreaAlias;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelFoundAreaAliases;
	private JScrollPane scrollPaneAreas;
	private JList listAreas;

	/**
	 * Show dialog.
	 * @param parent
	 * @param area 
	 * @return
	 */
	public static String showDialog(Component parent, Area area) {
		
		SelectAreaDialog dialog = new SelectAreaDialog(parent, area);
		dialog.setVisible(true);
		
		if (!dialog.confirm) {
			return null;
		}
		String alias = dialog.textAreaAlias.getText();
		return alias;
	}

	/**
	 * Constructor.
	 * @param parent 
	 * @param area 
	 */
	public SelectAreaDialog(Component parent, Area area) {
		super(Utility.findWindow(parent), ModalityType.DOCUMENT_MODAL);
		
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
		setSize(new Dimension(461, 436));
		setTitle("org.multipage.generator.textSelectAreaAlias");
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelAreaAlias = new JLabel("org.multipage.generator.textAreaAliasFilter");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaAlias, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelAreaAlias, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelAreaAlias);
		
		textAreaAlias = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textAreaAlias, 6, SpringLayout.SOUTH, labelAreaAlias);
		springLayout.putConstraint(SpringLayout.WEST, textAreaAlias, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textAreaAlias, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textAreaAlias);
		textAreaAlias.setColumns(10);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, textAreaAlias);
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelFoundAreaAliases = new JLabel("org.multipage.generator.textFoundAreaAliases");
		springLayout.putConstraint(SpringLayout.NORTH, labelFoundAreaAliases, 6, SpringLayout.SOUTH, textAreaAlias);
		springLayout.putConstraint(SpringLayout.WEST, labelFoundAreaAliases, 0, SpringLayout.WEST, labelAreaAlias);
		getContentPane().add(labelFoundAreaAliases);
		
		scrollPaneAreas = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneAreas, 6, SpringLayout.SOUTH, labelFoundAreaAliases);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneAreas, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneAreas, -6, SpringLayout.NORTH, buttonOk);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneAreas, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPaneAreas);
		
		listAreas = new JList();
		listAreas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					onListDoubleCLick();
				}
			}
		});
		listAreas.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				onListSelection();
			}
		});
		scrollPaneAreas.setViewportView(listAreas);
	}

	/**
	 * On listAreas double click.
	 */
	protected void onListDoubleCLick() {

		onOk();
	}

	/**
	 * on OK.
	 */
	protected void onOk() {
		
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
		// Create and update listAreas.
		createList();
		updateList();
		// Set text listeners.
		setTextListener();
		// Set old text.
		textAreaAlias.setText(oldText);
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
		updateList();
	}
	
	/**
	 * On listAreas selection.
	 */
	protected void onListSelection() {
		
		doNotUpdateList = true;
		textAreaAlias.setText((String) listAreas.getSelectedValue());
		doNotUpdateList = false;
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelAreaAlias);
		Utility.localize(labelFoundAreaAliases);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
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
	 * Create listAreas.
	 */
	private void createList() {
		
		// Create and set model.
		model = new DefaultListModel();
		listAreas.setModel(model);
	}

	/**
	 * Update listAreas.
	 */
	private void updateList() {
		
		if (doNotUpdateList) {
			return;
		}
		
		// Reset model.
		model.clear();
		
		// Get areas.
		AreasModel areasModel = ProgramGenerator.getAreasModel();
		if (area == null) {
			area = areasModel.getHomeArea();
		}
		
		LinkedList<Area> areas = areasModel.getProjectAreas(area);
		
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
				aliases.add(alias);
			}
		}
		// Sort aliases.
		Collections.sort(aliases);
		
		// Add to model.
		for (String alias : aliases) {
			model.addElement(alias);
		}
	}
}
