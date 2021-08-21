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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.MiddleUtility;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.Utility;
import org.multipage.util.ProgressResult;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;

/**
 * 
 * @author
 *
 */
public class GenerateAreasDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Conatianer area reference.
	 */
	private Area containerArea;
	
	/**
	 * Table model.
	 */
	private DefaultTableModel slotsTableModel;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelContainerArea;
	private JTextField textContainerArea;
	private JLabel labelTreeWidth;
	private JTextField textTreeWidth;
	private JLabel labelTreeDepth;
	private JTextField textTreeDepth;
	private JLabel labelAreasName;
	private JTextField textAreasName;
	private JCheckBox checkUseIndices;
	private JLabel labelAreasSlots;
	private JScrollPane scrollPane;
	private JTable tableSlots;
	private JPopupMenu popupMenu;
	private JMenuItem menuAddSlot;
	private JMenuItem menuDeleteSlot;
	private JCheckBox checkVisible;
	private JCheckBox checkReadOnly;
	private JCheckBox checkLocalized;
	private JCheckBox checkInherited;
	private JButton buttonCount;

	/**
	 * Launch the application.
	 */
	public static boolean showDialog(Component parent, Area area) {

		GenerateAreasDialog dialog = new GenerateAreasDialog(Utility.findWindow(parent),
				area);
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param area 
	 * @param window 
	 */
	public GenerateAreasDialog(Window window, Area area) {
		super(window, ModalityType.APPLICATION_MODAL);
		
		// Initialize components.
		initComponents();
		postCreation(area); //$hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("org.multipage.generator.textGenerateAreas");
		setBounds(100, 100, 525, 469);
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
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		labelContainerArea = new JLabel("org.multipage.generator.textContainerArea");
		springLayout.putConstraint(SpringLayout.NORTH, labelContainerArea, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelContainerArea, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelContainerArea);
		
		textContainerArea = new JTextField();
		textContainerArea.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textContainerArea, 6, SpringLayout.SOUTH, labelContainerArea);
		springLayout.putConstraint(SpringLayout.WEST, textContainerArea, 0, SpringLayout.WEST, labelContainerArea);
		springLayout.putConstraint(SpringLayout.EAST, textContainerArea, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(textContainerArea);
		textContainerArea.setColumns(10);
		
		labelTreeWidth = new JLabel("org.multipage.generator.textTreeWidth");
		springLayout.putConstraint(SpringLayout.NORTH, labelTreeWidth, 6, SpringLayout.SOUTH, textContainerArea);
		springLayout.putConstraint(SpringLayout.WEST, labelTreeWidth, 0, SpringLayout.WEST, labelContainerArea);
		getContentPane().add(labelTreeWidth);
		
		textTreeWidth = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textTreeWidth, 6, SpringLayout.SOUTH, labelTreeWidth);
		springLayout.putConstraint(SpringLayout.WEST, textTreeWidth, 0, SpringLayout.WEST, labelContainerArea);
		springLayout.putConstraint(SpringLayout.EAST, textTreeWidth, 0, SpringLayout.EAST, labelTreeWidth);
		getContentPane().add(textTreeWidth);
		textTreeWidth.setColumns(10);
		
		labelTreeDepth = new JLabel("org.multipage.generator.textTreeDepth");
		springLayout.putConstraint(SpringLayout.NORTH, labelTreeDepth, 6, SpringLayout.SOUTH, textContainerArea);
		springLayout.putConstraint(SpringLayout.WEST, labelTreeDepth, 6, SpringLayout.EAST, labelTreeWidth);
		getContentPane().add(labelTreeDepth);
		
		textTreeDepth = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textTreeDepth, 6, SpringLayout.SOUTH, labelTreeDepth);
		springLayout.putConstraint(SpringLayout.WEST, textTreeDepth, 6, SpringLayout.EAST, textTreeWidth);
		springLayout.putConstraint(SpringLayout.EAST, textTreeDepth, 0, SpringLayout.EAST, labelTreeDepth);
		getContentPane().add(textTreeDepth);
		textTreeDepth.setColumns(10);
		
		labelAreasName = new JLabel("org.multipage.generator.textAreasName");
		springLayout.putConstraint(SpringLayout.WEST, labelAreasName, 6, SpringLayout.EAST, labelTreeDepth);
		springLayout.putConstraint(SpringLayout.SOUTH, labelAreasName, 0, SpringLayout.SOUTH, labelTreeWidth);
		getContentPane().add(labelAreasName);
		
		textAreasName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textAreasName, 6, SpringLayout.SOUTH, labelAreasName);
		springLayout.putConstraint(SpringLayout.WEST, textAreasName, 6, SpringLayout.EAST, textTreeDepth);
		springLayout.putConstraint(SpringLayout.EAST, textAreasName, -104, SpringLayout.EAST, buttonCancel);
		getContentPane().add(textAreasName);
		textAreasName.setColumns(10);
		
		checkUseIndices = new JCheckBox("builder.textUseIndices");
		checkUseIndices.setSelected(true);
		springLayout.putConstraint(SpringLayout.NORTH, checkUseIndices, 26, SpringLayout.SOUTH, textContainerArea);
		springLayout.putConstraint(SpringLayout.WEST, checkUseIndices, -97, SpringLayout.EAST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, checkUseIndices, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(checkUseIndices);
		
		labelAreasSlots = new JLabel("org.multipage.generator.textAreasSlots");
		springLayout.putConstraint(SpringLayout.WEST, labelAreasSlots, 0, SpringLayout.WEST, labelContainerArea);
		getContentPane().add(labelAreasSlots);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelAreasSlots);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(scrollPane);
		
		popupMenu = new JPopupMenu();
		addPopup(scrollPane, popupMenu);
		
		menuAddSlot = new JMenuItem("org.multipage.generator.textAddSlot");
		menuAddSlot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddSlot();
			}
		});
		popupMenu.add(menuAddSlot);
		
		menuDeleteSlot = new JMenuItem("org.multipage.generator.textDeleteSlot");
		menuDeleteSlot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteSlot();
			}
		});
		popupMenu.add(menuDeleteSlot);
		
		tableSlots = new JTable();
		scrollPane.setViewportView(tableSlots);
		
		checkVisible = new JCheckBox("org.multipage.generator.textVisible");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreasSlots, 6, SpringLayout.SOUTH, checkVisible);
		springLayout.putConstraint(SpringLayout.NORTH, checkVisible, 6, SpringLayout.SOUTH, textTreeWidth);
		springLayout.putConstraint(SpringLayout.WEST, checkVisible, 0, SpringLayout.WEST, labelContainerArea);
		getContentPane().add(checkVisible);
		
		checkReadOnly = new JCheckBox("org.multipage.generator.textReadOnly");
		springLayout.putConstraint(SpringLayout.NORTH, checkReadOnly, 6, SpringLayout.SOUTH, textTreeDepth);
		springLayout.putConstraint(SpringLayout.WEST, checkReadOnly, 0, SpringLayout.WEST, labelTreeDepth);
		getContentPane().add(checkReadOnly);
		
		checkLocalized = new JCheckBox("org.multipage.generator.textLocalized");
		springLayout.putConstraint(SpringLayout.NORTH, checkLocalized, 0, SpringLayout.NORTH, checkVisible);
		springLayout.putConstraint(SpringLayout.WEST, checkLocalized, 6, SpringLayout.EAST, checkReadOnly);
		getContentPane().add(checkLocalized);
		
		checkInherited = new JCheckBox("org.multipage.generator.textInherited");
		springLayout.putConstraint(SpringLayout.NORTH, checkInherited, 6, SpringLayout.SOUTH, textAreasName);
		springLayout.putConstraint(SpringLayout.WEST, checkInherited, 6, SpringLayout.EAST, checkLocalized);
		getContentPane().add(checkInherited);
		
		buttonCount = new JButton("org.multipage.generator.textAreasSlotsCount");
		buttonCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmCount();
			}
		});
		buttonCount.setMargin(new Insets(0, 0, 0, 0));
		buttonCount.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonCount, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.WEST, buttonCount, 0, SpringLayout.WEST, labelContainerArea);
		getContentPane().add(buttonCount);
	}

	/**
	 * Post creation.
	 * @param area
	 */
	private void postCreation(Area area) {
		
		this.containerArea = area;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Utility.centerOnScreen(this);
		localize();
		setIcons();
		loadDialog();
		createTable();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelContainerArea);
		Utility.localize(labelTreeWidth);
		Utility.localize(labelTreeDepth);
		Utility.localize(labelAreasName);
		Utility.localize(checkUseIndices);
		Utility.localize(labelAreasSlots);
		Utility.localize(menuAddSlot);
		Utility.localize(menuDeleteSlot);
		Utility.localize(checkVisible);
		Utility.localize(checkReadOnly);
		Utility.localize(checkLocalized);
		Utility.localize(checkInherited);
		Utility.localize(buttonCount);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		textContainerArea.setText(containerArea.toString());
		textTreeWidth.setText("1");
		textTreeDepth.setText("1");
		textAreasName.setText("Test area");
		addPopup(tableSlots, popupMenu);
	}
	
	/**
	 * Add popup trayMenu.
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
	 * Create table.
	 */
	private void createTable() {
		
		slotsTableModel = new DefaultTableModel();
				
		slotsTableModel.addColumn(Resources.getString("org.multipage.generator.textSlotAccess"));
		slotsTableModel.addColumn(Resources.getString("org.multipage.generator.textSlotAlias"));
		slotsTableModel.addColumn(Resources.getString("org.multipage.generator.textSlotValue"));
		
		tableSlots.setModel(slotsTableModel);

		tableSlots.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel columnModel = tableSlots.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(50);
		columnModel.getColumn(1).setPreferredWidth(230);
		columnModel.getColumn(2).setPreferredWidth(230);

	}

	/**
	 * On add slot.
	 */
	protected void onAddSlot() {
		
		int slotsCount = slotsTableModel.getRowCount();
		String slotName = "slot0";
		int slotIndex = 0;
		
		// Find slot name.
		if (slotsCount > 0) {
			while (true) {
				
				boolean slotExists = false;
				
				for (int index = 0; index < slotsCount; index++) {
					String existingSlotName = (String) slotsTableModel.getValueAt(index, 1);
					if (slotName.equals(existingSlotName)) {
						slotExists = true;
						break;
					}
				}
				
				if (!slotExists) {
					break;
				}
				// Generate new slot name.
				slotIndex++;
				slotName = "slot" + slotIndex;
			}
		}
		
		Object [] rowData = {false, slotName, "value"};
		slotsTableModel.addRow(rowData);
	}

	/**
	 * On delete slot.
	 */
	protected void onDeleteSlot() {
		
		// Delete selected row.
		int [] selectedRows = tableSlots.getSelectedRows();
		if (selectedRows.length == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectTableRow");
			return;
		}
		
		while (true) {
			int row = tableSlots.getSelectedRow();
			if (row == -1) {
				break;
			}
			slotsTableModel.removeRow(row);
		}
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Ask user.
		if (!confirmCount()) {
			return;
		}
		
		// Get area properties.
		String areasName = textAreasName.getText();
		boolean visible = checkVisible.isSelected();
		boolean readOnly = checkReadOnly.isSelected();
		boolean localized = checkLocalized.isSelected();
		final boolean inherited = checkInherited.isSelected();
		final boolean useIndices = checkUseIndices.isSelected();
		
		// Create new area.
		final Area newArea = new Area(0L, areasName, visible, "", readOnly);
		newArea.setLocalized(localized);
		
		final int treeWidth = Integer.parseInt(textTreeWidth.getText());
		final int treeDepth = Integer.parseInt(textTreeDepth.getText());
		
		// Get slots properties.
		int slotCount = slotsTableModel.getRowCount();
		final Object [][] slots = new Object [slotCount][3];
		for (int index = 0; index < slotCount; index++) {
			for (int column = 0; column < 3; column++) {
				slots[index][column] = slotsTableModel.getValueAt(index, column);
			}
		}
		
		// Create progress dialog.
		ProgressDialog<MiddleResult> progressDialog = new ProgressDialog<MiddleResult>(
				this, Resources.getString("org.multipage.generator.textCreatingAreasTree"),
				Resources.getString("org.multipage.generator.textCreatingAreasTree"));
		
		ProgressResult progressResult = progressDialog.execute(new SwingWorkerHelper<MiddleResult>() {
			// Background process.
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				// Create subtree.
				Properties login = ProgramBasic.getLoginProperties();
				Middle middle = ProgramBasic.getMiddle();
				
				MiddleResult result = middle.createAreaSubtree(login, containerArea,
						treeWidth, treeDepth, useIndices, newArea, inherited, slots, this);
				
				return result;
			}
		});

		// If execution OK.
		if (progressResult == ProgressResult.OK) {
			MiddleResult result = progressDialog.getOutput();
			if (result.isNotOK()) {
				result.show(this);
			}
		}
		// If it is an execution exception, show it.
		else if (progressResult == ProgressResult.EXECUTION_EXCEPTION) {
			// Show result message.
			Utility.show2(progressDialog.getException().getLocalizedMessage());
		}
		
		// Update data.
		long areaId = containerArea.getId();
		ConditionalEvents.transmit(GenerateAreasDialog.this, Signal.createAreasTree, areaId);
		
		// Close the window.
		dispose();
	}
	
	/**
	 * Show count.
	 */
	protected boolean confirmCount() {
		
		int width = Integer.parseInt(textTreeWidth.getText());
		int depth = Integer.parseInt(textTreeDepth.getText());
		int slotsPerAreaCount = slotsTableModel.getRowCount();
		
		double areasCount = MiddleUtility.getTotalTreeAreas(width, depth);
		double slotsCount = areasCount * slotsPerAreaCount;
		
		String message = String.format(Resources.getString("org.multipage.generator.textConfirmAreaAndSlotsCount"), areasCount, slotsCount);
		
		return JOptionPane.showConfirmDialog(this, message) == JOptionPane.YES_OPTION;
	}
}
