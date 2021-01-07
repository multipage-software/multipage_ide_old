/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import org.multipage.basic.*;
import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * 
 * @author
 *
 */
public class OrderLanguagesDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog state.
	 */
	private static Rectangle bounds;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream) 
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Table model.
	 */
	private LocalTableModel model;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonClose;
	private JPanel panelMain;
	private JLabel labelOrder;
	private JScrollPane scrollPane;
	private JButton buttonDefault;
	private JButton buttonDown;
	private JButton buttonUp;
	private JTable table;

	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 */
	public static void showDialog(Component parent) {
		
		OrderLanguagesDialog dialog = new OrderLanguagesDialog(Utility.findWindow(parent));
		
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public OrderLanguagesDialog(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onClose();
			}
		});
		setTitle("org.multipage.translator.textOrderLanguagesDialog");
		
		setBounds(100, 100, 506, 300);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonClose = new JButton("textClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, panel);
		buttonClose.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonClose);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelOrder = new JLabel("org.multipage.translator.textLanguagesOrderTable");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelOrder, 6, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelOrder, 6, SpringLayout.WEST, panelMain);
		panelMain.add(labelOrder);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelOrder);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 6, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, panelMain);
		panelMain.add(scrollPane);
		
		buttonDefault = new JButton("");
		buttonDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onReset();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -6, SpringLayout.WEST, buttonDefault);
		buttonDefault.setPreferredSize(new Dimension(30, 30));
		panelMain.add(buttonDefault);
		
		buttonDown = new JButton("");
		buttonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDown();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonDown, -10, SpringLayout.EAST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonDefault, 6, SpringLayout.SOUTH, buttonDown);
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonDefault, 0, SpringLayout.WEST, buttonDown);
		buttonDown.setPreferredSize(new Dimension(30, 30));
		panelMain.add(buttonDown);
		
		buttonUp = new JButton("");
		buttonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUp();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonDown, 6, SpringLayout.SOUTH, buttonUp);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonUp, 0, SpringLayout.NORTH, scrollPane);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonUp, -10, SpringLayout.EAST, panelMain);
		buttonUp.setPreferredSize(new Dimension(30, 30));
		panelMain.add(buttonUp);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
		setToolTips();
		
		createTable();
		loadTable();
		
		loadDialog();
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

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(labelOrder);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonClose.setIcon(Images.getIcon("org/multipage/translator/images/cancel_icon.png"));
		buttonDefault.setIcon(Images.getIcon("org/multipage/translator/images/reset_order.png"));
		buttonUp.setIcon(Images.getIcon("org/multipage/translator/images/up.png"));
		buttonDown.setIcon(Images.getIcon("org/multipage/translator/images/down.png"));
	}

	/**
	 * Set tool tips.
	 */
	protected void setToolTips() {

		buttonDefault.setToolTipText(Resources.getString("org.multipage.translator.tooltipSetDefaultLanguagesOrder"));
		buttonUp.setToolTipText(Resources.getString("org.multipage.translator.tooltipShiftLanguageUp"));
		buttonDown.setToolTipText(Resources.getString("org.multipage.translator.tooltipShiftLanguageDown"));
	}

	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Create table.
	 */
	private void createTable() {

		// Create table model and assign it to the table.
		model = new LocalTableModel();
		table.setModel(model);
		table.getTableHeader().setReorderingAllowed(false);
		
		// Set flag renderer.
		table.setDefaultRenderer(BufferedImage.class, new TableCellRenderer() {
			
			// Create flag renderer.
			RendererJLabel renderer;
			{
				renderer = new RendererJLabel();
			}
			
			// Return flag renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				// Set image. 
				if (value instanceof BufferedImage) {
					BufferedImage flag = (BufferedImage) value;
					renderer.setIcon(new ImageIcon(flag));
				}
				else {
					renderer.setIcon(null);
				}
				
				// Set properties.
				renderer.set(isSelected, hasFocus, row);
				return renderer;
			}
		});
		
		// Set text renderer.
		table.setDefaultRenderer(Object.class, new TableCellRenderer() {
			
			// Create flag renderer.
			RendererJLabel renderer;
			{
				renderer = new RendererJLabel();
			}
			
			// Return flag renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				
				// Set text. 
				renderer.setText(value.toString());
				
				// Set properties.
				renderer.set(isSelected, hasFocus, row);
				return renderer;
			}
		});
	}
	
	/**
	 * Load table.
	 */
	private void loadTable() {
		
		// Reset model content.
		model.clear();
		
		// Load languages.		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Login to the database.
		result = middle.login(login);
		if (result.isOK()) {
			
			result = middle.loadLanguages(model.getLanguages());

			// Logout from the database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Update table.
		model.fireTableDataChanged();
	}
	
	/**
	 * On move language up.
	 */
	protected void onUp() {
		
		// Get selected language.
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			Utility.show(this, "org.multipage.translator.textSelectLanguageToMove");
			return;
		}
		
		// Get previous row.
		int previousRow = selectedRow - 1;
		if (previousRow < 0) {
			return;
		}
		
		// Swap priorities.
		model.swap(selectedRow, previousRow);
		
		// Save language priorities and load new table.
		savePriorities();
		loadTable();
		
		table.setRowSelectionInterval(previousRow, previousRow);
	}
	
	/**
	 * On move language down.
	 */
	protected void onDown() {
		
		// Get selected language.
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			Utility.show(this, "org.multipage.translator.textSelectLanguageToMove");
			return;
		}

		// Get next row.
		int nextRow = selectedRow + 1;
		if (nextRow >= table.getModel().getRowCount()) {
			return;
		}
		
		// Swap priorities.
		model.swap(selectedRow, nextRow);
		
		// Save language priorities and load new table.
		savePriorities();
		loadTable();
		
		table.setRowSelectionInterval(nextRow, nextRow);
	}

	/**
	 * Save language priorities.
	 */
	private void savePriorities() {
		
		// Get list of languages.
		LinkedList<Language> languages = model.getLanguages();
		
		// Save priorities.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.updateLanguagePriorities(login, languages);
		if (result.isNotOK()) {
			result.show(this);
		}
	}

	/**
	 * On reset.
	 */
	protected void onReset() {
		
		int selectedRow = table.getSelectedRow();

		// Reset language priorities.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		MiddleResult result = middle.updateLanguagePrioritiesReset(login);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Load new table.
		loadTable();
		
		if (selectedRow != -1) {
			table.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}
}

/**
 * Table model.
 * @author
 *
 */
class LocalTableModel extends AbstractTableModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Columns definition.
	 */
	private String [] columns = {
			"org.multipage.translator.textIdentifier2",
			"org.multipage.translator.textFlag",
			"org.multipage.translator.textLanguageDescription",
			"org.multipage.translator.textLanguageAlias"};
	
	/**
	 * Languages list.
	 */
	private LinkedList<Language> languages = new LinkedList<Language>();
	
	/**
	 * Get column count.
	 */
	@Override
	public int getColumnCount() {
		
		return columns.length;
	}

	/**
	 * Swap rows.
	 * @param row1
	 * @param row2
	 */
	public void swap(int row1, int row2) {
		
		// Check input values.
		int count = languages.size();
		
		if (row1 < 0 || row2 < 0 || row1 >= count || row2 >= count) {
			return;
		}
		
		// Swap languages.
		Language language1 = languages.get(row1);
		Language language2 = languages.get(row2);
		
		languages.set(row1, language2);
		languages.set(row2, language1);
		
		// Update table.
		fireTableDataChanged();
	}

	/**
	 * Get column name.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		
		if (columnIndex < 0 || columnIndex >= columns.length) {
			return "*error*";
		}
		
		return Resources.getString(columns[columnIndex]);
	}

	/**
	 * Get languages list.
	 * @return
	 */
	public LinkedList<Language> getLanguages() {
		
		return languages;
	}

	/**
	 * Clear data.
	 */
	public void clear() {
		
		languages.clear();
		fireTableDataChanged();
	}

	/**
	 * Get row count.
	 */
	@Override
	public int getRowCount() {
		
		return languages.size();
	}

	/**
	 * Get value of given cell.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		if (rowIndex >= languages.size()) {
			return "*error*";
		}
		
		// Get language.
		Language language = languages.get(rowIndex);
		
		switch (columnIndex) {
			case 0:
				return language.id;
			case 1:
				return language.image;
			case 2:
				return language.description;
			case 3:
				return language.alias;
		}
		return "*error*";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		if (columnIndex == 1) {
			return BufferedImage.class;
		}
		return super.getColumnClass(columnIndex);
	}
}