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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;

import org.maclan.Area;
import org.maclan.AreaRelation;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class SelectSuperAreaDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * Area reference.
	 */
	private Area area;

	/**
	 * Table model.
	 */
	private DefaultTableModel tableModel;

	/**
	 * Selected super area.
	 */
	private Area superArea;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelSelect;
	private JScrollPane scrollPane;
	private JTable table;

	/**
	 * Show dialog.
	 * @param parent
	 * @param area 
	 * @return
	 */
	public static Area showDialog(Component parent, Area area) {
		
		SelectSuperAreaDialog dialog = new SelectSuperAreaDialog(parent, area);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.superArea;
		}
		return null;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 * @param area 
	 */
	public SelectSuperAreaDialog(Component parent, Area area) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		this.area = area;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textSelectSuperArea");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 450, 300);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelSelect = new JLabel("org.multipage.generator.textLabelSelectSuperArea");
		springLayout.putConstraint(SpringLayout.NORTH, labelSelect, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelSelect, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSelect);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.SOUTH, labelSelect);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 194, SpringLayout.SOUTH, labelSelect);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		initTable();
		
		loadDialog();
	}

	/**
	 * Initialize table.
	 */
	@SuppressWarnings("serial")
	private void initTable() {
		
		// Create model.
		tableModel = new DefaultTableModel() {

			@Override
			public boolean isCellEditable(int row, int column) {
				
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				
				if (columnIndex == 2 || columnIndex == 3) {
					return Boolean.class;
				}
				
				return super.getColumnClass(columnIndex);
			}
			
		};
		table.setModel(tableModel);
		
		// Add columns.
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnID"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnName"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnInherits"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnHides"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnNameSub"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textColumnNameSuper"));
				
		long areaId = area.getId();
		
		// Add rows.
		for (Area superArea : area.getSuperareas()) {
			
			AreaRelation relation = superArea.getSubRelation(areaId);
			
			tableModel.addRow(new Object [] {
					superArea.getId(),
					superArea.getDescriptionForced(false),
					relation.isInheritance(),
					relation.isHideSub(),
					relation.getRelationNameSub(),
					relation.getRelationNameSuper()
					});
		}
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSelect);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Get selected row.
		int selectedIndex = table.getSelectedRow();
		if (selectedIndex == -1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSuperArea");
			return;
		}
		
		// Set area.
		Long superAreaId = (Long) tableModel.getValueAt(selectedIndex, 0);
		if (superAreaId != null) {
			
			superArea = area.getSuperarea(superAreaId);
		}
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
