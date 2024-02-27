/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import org.multipage.gui.*;
import org.multipage.util.Resources;

import com.maclan.*;

import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * 
 * @author
 *
 */
public class AreaInheritedFoldersDialog extends JDialog {
	
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
	 * Table model.
	 */
	private DefaultTableModel tableModel;

	// $hide<<$
	private JButton buttonClose;
	private JLabel labelArea;
	private JTextField textAreaDescription;
	private JScrollPane scrollPane;
	private JTable table;
	private JPopupMenu popupMenu;
	private JMenuItem menuCopyFolderName;

	/**
	 * Show dialog.
	 * @param parent
	 * @param area 
	 * @param resource
	 */
	public static void showDialog(Component parent, Area area) {
		
		AreaInheritedFoldersDialog dialog = new AreaInheritedFoldersDialog(Utility.findWindow(parent));
		dialog.area = area;
		
		dialog.loadLoadDialogContent();
		
		dialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public AreaInheritedFoldersDialog(Window parentWindow) {
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
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onClose();
			}
		});
		setTitle("org.multipage.generator.textAreaInheritedFoldersDialog");
		
		setBounds(100, 100, 470, 409);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setBackground(UIManager.getColor("Panel.background"));
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(scrollPane);
		
		table = new JTable();
		table.setBackground(UIManager.getColor("Panel.background"));
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setBorder(new LineBorder(new Color(192, 192, 192)));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(new Font("Tahoma", Font.PLAIN, 14));
		scrollPane.setViewportView(table);
		
		popupMenu = new JPopupMenu();
		addPopup(table, popupMenu);
		
		menuCopyFolderName = new JMenuItem("org.multipage.generator.menuCopyInheritedFolderName");
		menuCopyFolderName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onCopyFolderName();
			}
		});
		popupMenu.add(menuCopyFolderName);
		
		buttonClose = new JButton("textClose");
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonClose);
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		buttonClose.setPreferredSize(new Dimension(80, 25));
		
		labelArea = new JLabel("org.multipage.generator.textInheritedFoldersArea");
		springLayout.putConstraint(SpringLayout.NORTH, labelArea, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelArea, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelArea);
		
		textAreaDescription = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, textAreaDescription);
		springLayout.putConstraint(SpringLayout.EAST, textAreaDescription, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, textAreaDescription, 0, SpringLayout.NORTH, labelArea);
		springLayout.putConstraint(SpringLayout.WEST, textAreaDescription, 3, SpringLayout.EAST, labelArea);
		getContentPane().add(textAreaDescription);
		textAreaDescription.setEditable(false);
		textAreaDescription.setColumns(10);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
		
		initializeTable();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(labelArea);
		Utility.localize(menuCopyFolderName);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonClose.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		menuCopyFolderName.setIcon(Images.getIcon("org/multipage/gui/images/copy_icon.png"));
	}
	
	/**
	 * On close.
	 */
	protected void onClose() {
		
		dispose();
	}

	/**
	 * Load dialog content.
	 */
	private void loadLoadDialogContent() {
		
		// Set text field.
		textAreaDescription.setText(area.getDescriptionForDiagram());
		
		// Load table.
		AreasModel areasModel = ProgramGenerator.getAreasModel();
		
		for (VersionObj version : areasModel.getVersions()) {
			
			// Load inherited folder name.
			String inheritedFolder = area.getInheritedFolder(version.getId());
			
			// Add table row.
			tableModel.addRow(new String [] { version.getDescription(), inheritedFolder });
		}
	}
	
	/**
	 * Initialize table.
	 */
	@SuppressWarnings("serial")
	private void initializeTable() {
		
		// Create and set model (not editable).
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table.setModel(tableModel);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		// Add columns.
		tableModel.addColumn(Resources.getString("org.multipage.generator.textVersionDescriptionColumn"));
		tableModel.addColumn(Resources.getString("org.multipage.generator.textInheritedFolderColumn"));
	
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setPreferredWidth(80);
		column = table.getColumnModel().getColumn(1);
		column.setPreferredWidth(350);
		
		// Set row height and margin.
		table.setRowHeight(30);
		table.setRowMargin(10);
	}
	
	/**
	 * Add popum trayMenu.
	 * @param component
	 * @param popup
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	/**
	 * On copy folder name.
	 */
	protected void onCopyFolderName() {
		
		// Get selected item.
		int selectedRow = table.getSelectedRow();
		if (selectedRow == -1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectInhertitedFoldersTableRow");
			return;
		}
		
		// Get folder name and add it to the clipboard.
		String folderName = (String) tableModel.getValueAt(selectedRow, 1);
		
		StringSelection selection = new StringSelection(folderName);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}
}