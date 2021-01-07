/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import org.multipage.util.*;

import java.awt.event.*;
import java.io.*;
import java.util.function.BiConsumer;

/**
 * 
 * @author
 *
 */
public class CssKeyFrameItemDialog extends JDialog {

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
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

	/**
	 * List model.
	 */
	private DefaultListModel<String> listTimePointsModel;

	/**
	 * Table model.
	 */
	private DefaultTableModel tablePropertiesModel;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelPointsInTime;
	private JScrollPane scrollPaneTimePoints;
	private JList<String> listTimePoints;
	private JButton buttonAddTimePoint;
	private JComboBox comboFromTo;
	private JTextField textTimePoint;
	private JLabel labelTimePointUnits;
	private JPopupMenu popupMenu;
	private JMenuItem menuDelete;
	private JButton buttonDeleteTimePoint;
	private JLabel labelProperties;
	private JScrollPane scrollPaneProperties;
	private JTable tableProperties;
	private JButton buttonAddProperty;
	private JButton buttonDeleteProperty;
	private JTextField textAnimatedProperty;
	private JButton buttonFindProperty;
	private JTextField textPropertyValue;
	private JLabel labelTime;
	private JLabel labelCssProperty;
	private JLabel labelCssPropertyValue;
	private JPopupMenu popupMenuProperties;
	private JMenuItem menuDeleteProperty;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static CssKeyframe showDialog(Component parent) {
		
		CssKeyFrameItemDialog dialog = new CssKeyFrameItemDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getKeyframe();
		}
		return null;
	}

	/**
	 * Edit dialog.
	 * @param parent
	 * @param keyframe
	 */
	public static boolean editDialog(Component parent,
			CssKeyframe keyframe) {
		
		CssKeyFrameItemDialog dialog = new CssKeyFrameItemDialog(parent);
		dialog.readKeyframe(keyframe);
		
		dialog.setVisible(true);
		if (dialog.confirm) {
			
			return dialog.editKeyframe(keyframe);
		}
		return false;
	}

	/**
	 * Read keyframe.
	 * @param keyframe
	 */
	private void readKeyframe(CssKeyframe keyframe) {
		
		// Load points in time.
		for (String timePoint : keyframe.timePoints) {
			listTimePointsModel.addElement(timePoint);
		}
		
		// Load animated properties.
		keyframe.forEachProperty(new BiConsumer<String, String>() {
			@Override
			public void accept(String namesText, String value) {
				
				tablePropertiesModel.addRow(new String [] { namesText, value });
			}
		});
	}

	/**
	 * Set keyframe.
	 * @param keyframe
	 */
	private boolean editKeyframe(CssKeyframe keyframe) {
		
		if (!checkKeyframe()) {
			return false;
		}
		
		keyframe.clear();
		
		// Process time points.
		for (int index = 0; index < listTimePointsModel.size(); index++) {
			
			String timePoint = listTimePointsModel.get(index);
			keyframe.addTimePoint(timePoint);
		}
		
		// Process animated properties.
		for (int index = 0; index < tablePropertiesModel.getRowCount(); index++) {
			
			String propertyDefinition = (String) tablePropertiesModel.getValueAt(index, 0);
			String value = (String) tablePropertiesModel.getValueAt(index, 1);
			
			keyframe.addProperty(propertyDefinition, value);
		}
		
		return true;
	}

	/**
	 * Get keyframe.
	 * @return
	 */
	private CssKeyframe getKeyframe() {
		
		CssKeyframe keyframe = new CssKeyframe();
			
		if (editKeyframe(keyframe)) {
			return keyframe;
		}
		
		return null;
	}

	/**
	 * Check keyframe.
	 */
	private boolean checkKeyframe() {
		
		// Check time points.
		return checkTimePoints() && checkProperties();
	}

	/**
	 * Check properties.
	 * @return
	 */
	private boolean checkProperties() {
		
		return tablePropertiesModel.getRowCount() > 0;
	}

	/**
	 * Check time points.
	 * @return
	 */
	private boolean checkTimePoints() {
		
		return !listTimePointsModel.isEmpty();
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public CssKeyFrameItemDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		getContentPane().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onPanelClick();
			}
		});
		setTitle("org.multipage.gui.textKeyframeItemDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 548, 335);
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
		
		labelPointsInTime = new JLabel("org.multipage.gui.textPointsInTime");
		springLayout.putConstraint(SpringLayout.NORTH, labelPointsInTime, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelPointsInTime, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelPointsInTime);
		
		scrollPaneTimePoints = new JScrollPane();
		scrollPaneTimePoints.setPreferredSize(new Dimension(100, 70));
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneTimePoints, 6, SpringLayout.SOUTH, labelPointsInTime);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneTimePoints, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(scrollPaneTimePoints);
		
		listTimePoints = new JList();
		listTimePoints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneTimePoints.setViewportView(listTimePoints);
		
		popupMenu = new JPopupMenu();
		addPopup(listTimePoints, popupMenu);
		
		menuDelete = new JMenuItem("org.multipage.gui.menuDeletePointInTime");
		menuDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteTimePoint();
			}
		});
		popupMenu.add(menuDelete);
		
		buttonAddTimePoint = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonAddTimePoint, 30, SpringLayout.NORTH, getContentPane());
		buttonAddTimePoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddTimePoint();
			}
		});
		buttonAddTimePoint.setPreferredSize(new Dimension(40, 24));
		springLayout.putConstraint(SpringLayout.WEST, buttonAddTimePoint, 6, SpringLayout.EAST, scrollPaneTimePoints);
		getContentPane().add(buttonAddTimePoint);
		
		comboFromTo = new JComboBox();
		comboFromTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectFromToCombo();
			}
		});
		comboFromTo.setPreferredSize(new Dimension(80, 24));
		springLayout.putConstraint(SpringLayout.NORTH, comboFromTo, 0, SpringLayout.NORTH, buttonAddTimePoint);
		springLayout.putConstraint(SpringLayout.WEST, comboFromTo, 6, SpringLayout.EAST, buttonAddTimePoint);
		getContentPane().add(comboFromTo);
		
		textTimePoint = new TextFieldEx();
		textTimePoint.setPreferredSize(new Dimension(6, 24));
		springLayout.putConstraint(SpringLayout.NORTH, textTimePoint, 0, SpringLayout.NORTH, buttonAddTimePoint);
		springLayout.putConstraint(SpringLayout.WEST, textTimePoint, 6, SpringLayout.EAST, comboFromTo);
		getContentPane().add(textTimePoint);
		textTimePoint.setColumns(6);
		
		labelTimePointUnits = new JLabel();
		springLayout.putConstraint(SpringLayout.WEST, labelTimePointUnits, 3, SpringLayout.EAST, textTimePoint);
		labelTimePointUnits.setText("%");
		labelTimePointUnits.setPreferredSize(new Dimension(50, 24));
		springLayout.putConstraint(SpringLayout.NORTH, labelTimePointUnits, 0, SpringLayout.NORTH, textTimePoint);
		getContentPane().add(labelTimePointUnits);
		
		buttonDeleteTimePoint = new JButton("");
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneTimePoints, 0, SpringLayout.SOUTH, buttonDeleteTimePoint);
		springLayout.putConstraint(SpringLayout.NORTH, buttonDeleteTimePoint, 60, SpringLayout.NORTH, getContentPane());
		buttonDeleteTimePoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteTimePoint();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, buttonDeleteTimePoint, 6, SpringLayout.EAST, scrollPaneTimePoints);
		buttonDeleteTimePoint.setPreferredSize(new Dimension(40, 24));
		getContentPane().add(buttonDeleteTimePoint);
		
		labelProperties = new JLabel("org.multipage.gui.textAnimatedProperties");
		springLayout.putConstraint(SpringLayout.NORTH, labelProperties, 20, SpringLayout.SOUTH, scrollPaneTimePoints);
		springLayout.putConstraint(SpringLayout.WEST, labelProperties, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelProperties);
		
		scrollPaneProperties = new JScrollPane();
		scrollPaneProperties.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onTableScrollClick();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneProperties, 0, SpringLayout.EAST, comboFromTo);
		scrollPaneProperties.setPreferredSize(new Dimension(2, 120));
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneProperties, 6, SpringLayout.SOUTH, labelProperties);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneProperties, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(scrollPaneProperties);
		
		popupMenuProperties = new JPopupMenu();
		addPopup(scrollPaneProperties, popupMenuProperties);
		
		menuDeleteProperty = new JMenuItem("org.multipage.gui.textDeleteAnimatedProperty");
		menuDeleteProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteProperty();
			}
		});
		popupMenuProperties.add(menuDeleteProperty);
		
		tableProperties = new JTable();
		scrollPaneProperties.setViewportView(tableProperties);
		addPopup(tableProperties, popupMenuProperties);

		buttonAddProperty = new JButton("");
		buttonAddProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddProperty();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonAddProperty, 0, SpringLayout.NORTH, scrollPaneProperties);
		springLayout.putConstraint(SpringLayout.WEST, buttonAddProperty, 6, SpringLayout.EAST, scrollPaneProperties);
		buttonAddProperty.setPreferredSize(new Dimension(40, 24));
		getContentPane().add(buttonAddProperty);
		
		buttonDeleteProperty = new JButton("");
		buttonDeleteProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteProperty();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonDeleteProperty, 6, SpringLayout.SOUTH, buttonAddProperty);
		springLayout.putConstraint(SpringLayout.WEST, buttonDeleteProperty, 6, SpringLayout.EAST, scrollPaneProperties);
		buttonDeleteProperty.setPreferredSize(new Dimension(40, 24));
		getContentPane().add(buttonDeleteProperty);
		
		textAnimatedProperty = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAnimatedProperty, 0, SpringLayout.NORTH, scrollPaneProperties);
		springLayout.putConstraint(SpringLayout.WEST, textAnimatedProperty, 6, SpringLayout.EAST, buttonAddProperty);
		textAnimatedProperty.setPreferredSize(new Dimension(6, 24));
		textAnimatedProperty.setColumns(16);
		getContentPane().add(textAnimatedProperty);
		
		buttonFindProperty = new JButton("");
		buttonFindProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindProperty();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonFindProperty, 0, SpringLayout.NORTH, buttonAddProperty);
		springLayout.putConstraint(SpringLayout.WEST, buttonFindProperty, 0, SpringLayout.EAST, textAnimatedProperty);
		buttonFindProperty.setPreferredSize(new Dimension(24, 24));
		getContentPane().add(buttonFindProperty);
		
		textPropertyValue = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, textPropertyValue, 6, SpringLayout.EAST, buttonDeleteProperty);
		textPropertyValue.setPreferredSize(new Dimension(6, 24));
		textPropertyValue.setColumns(16);
		getContentPane().add(textPropertyValue);
		
		labelTime = new JLabel("org.multipage.gui.textTime");
		springLayout.putConstraint(SpringLayout.NORTH, labelTime, 0, SpringLayout.NORTH, labelPointsInTime);
		springLayout.putConstraint(SpringLayout.WEST, labelTime, 0, SpringLayout.WEST, textTimePoint);
		getContentPane().add(labelTime);
		
		labelCssProperty = new JLabel("org.multipage.gui.textCssProperty");
		springLayout.putConstraint(SpringLayout.WEST, labelCssProperty, 0, SpringLayout.WEST, textAnimatedProperty);
		springLayout.putConstraint(SpringLayout.SOUTH, labelCssProperty, 0, SpringLayout.SOUTH, labelProperties);
		getContentPane().add(labelCssProperty);
		
		labelCssPropertyValue = new JLabel("org.multipage.gui.textCssPropertyValue");
		springLayout.putConstraint(SpringLayout.NORTH, textPropertyValue, 6, SpringLayout.SOUTH, labelCssPropertyValue);
		springLayout.putConstraint(SpringLayout.NORTH, labelCssPropertyValue, 0, SpringLayout.NORTH, buttonDeleteProperty);
		springLayout.putConstraint(SpringLayout.WEST, labelCssPropertyValue, 6, SpringLayout.EAST, buttonDeleteProperty);
		getContentPane().add(labelCssPropertyValue);
	}

	/**
	 * On main panel click.
	 */
	protected void onPanelClick() {
		
		stopTableEditing();
	}

	/**
	 * On table scroll click.
	 */
	protected void onTableScrollClick() {
		
		stopTableEditing();
	}

	/**
	 * Delete property.
	 */
	protected void deleteProperty() {
		
		int index = tableProperties.getSelectedRow();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleCssAnimatedProperty");
			return;
		}
		
		if (!Utility.ask(this, "org.multipage.gui.messageDeleteAnimatedCssProperty")) {
			return;
		}
		
		tablePropertiesModel.removeRow(index);
	}

	/**
	 * Add property.
	 */
	protected void onAddProperty() {
		
		String propertyName = textAnimatedProperty.getText();
		String propertyValue = textPropertyValue.getText();
		
		// Check values.
		if (propertyName.isEmpty() || propertyValue.isEmpty()) {
			Utility.show(this, "org.multipage.gui.messageInsertAnimatedCssPropertyAndValue");
			return;
		}
		
		// Add table row.
		if (existsProperty(propertyName)) {
			Utility.show(this, "org.multipage.gui.messageAnimatedPropertyAlreadyExists");
			
			textAnimatedProperty.setText("");
			textPropertyValue.setText("");
			return;
		}
		
		tablePropertiesModel.addRow(new String [] { propertyName, propertyValue});
		
		// Reset input fields.
		textAnimatedProperty.setText("");
		textPropertyValue.setText("");
	}

	/**
	 * Returns true value if a property is already in the table.
	 * @param propertyName
	 * @return
	 */
	private boolean existsProperty(String propertyName) {
		
		for (int index = 0; index < tablePropertiesModel.getRowCount(); index++) {
			String foundPropertyName = (String) tablePropertiesModel.getValueAt(index, 0);
			
			if (foundPropertyName.equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * On find property.
	 */
	protected void onFindProperty() {
		
		CssProperty property = CssFindPropertyDialog.showDialog(this, CssProperty.ANIMATED);
		if (property == null) {
			return;
		}
		
		String text = property.getText();
		textAnimatedProperty.setText(text);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				textAnimatedProperty.setCaretPosition(0);
			}
		});
		
	}

	/**
	 * On delete time point.
	 */
	protected void onDeleteTimePoint() {
		
		int index = listTimePoints.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectTimePoint");
			return;
		}
		
		listTimePointsModel.remove(index);
	}

	/**
	 * On add time point.
	 */
	protected void onAddTimePoint() {
		
		String comboText = Utility.getSelectedNamedItem(comboFromTo);
		if (comboText == null) {
			comboText = "";
		}
		String editorText = textTimePoint.getText();

		// Set values.
		if (!comboText.isEmpty()) {
			listTimePointsModel.addElement(comboText);
		}
		else if (!editorText.isEmpty()) {
			
			try {
				int percentage = Integer.parseInt(editorText);
				
				if (percentage >= 0 && percentage <= 100) {
					listTimePointsModel.addElement(editorText + "%");
					
					// Reset editor and combo.
					textTimePoint.setText("");
					comboFromTo.setSelectedIndex(1);
					return;
				}
			}
			catch (Exception e) {
			}
			Utility.show(this, "org.multipage.gui.messageEnterTimePointPercentage");
		}
		else {
			Utility.show(this, "org.multipage.gui.messageEnterTimePoint");
		}
	}

	/**
	 * On select "from, to" combo.
	 */
	protected void onSelectFromToCombo() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textTimePoint.setText("");
		
		stopSettingControls();
	}

	/**
	 * Start setting controls.
	 */
	public void startSettingControls() {
		
		settingControls = true;
	}

	/**
	 * Stop setting controls.
	 */
	public void stopSettingControls() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				settingControls = false;
			}
		});
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		setToolTips();
		
		loadComboBoxes();
		initList();
		initTable();
		
		loadDialog();
		setListeners();
	}

	/**
	 * Initialize table.
	 */
	private void initTable() {
		
		// Create and set table model.
		tablePropertiesModel = new DefaultTableModel();
		tableProperties.setModel(tablePropertiesModel);
		
		// Add columns.
		tablePropertiesModel.addColumn(Resources.getString("org.multipage.gui.textCssPropertyColumn"));
		tablePropertiesModel.addColumn(Resources.getString("org.multipage.gui.textCssPropertyValueColumn"));
		
		tableProperties.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
	
	/**
	 * Stop editing table.
	 */
	private void stopTableEditing() {
		
		if (tableProperties.isEditing()) {
			tableProperties.getCellEditor().stopCellEditing();
		}
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonAddTimePoint.setToolTipText(Resources.getString("org.multipage.gui.tooltipAddTimePoint"));
		buttonDeleteTimePoint.setToolTipText(Resources.getString("org.multipage.gui.tooltipDeleteTimePoint"));
		buttonAddProperty.setToolTipText(Resources.getString("org.multipage.gui.tooltipAddKeyframeProperty"));
		buttonDeleteProperty.setToolTipText(Resources.getString("org.multipage.gui.tooltipDeleteKeyframeProperty"));
		textTimePoint.setToolTipText(Resources.getString("org.multipage.gui.textInsertTimePointPercentage"));
	}

	/**
	 * Initialize list.
	 */
	private void initList() {
		
		listTimePointsModel = new DefaultListModel<String>();
		listTimePoints.setModel(listTimePointsModel);
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		Utility.setTextChangeListener(textTimePoint, new Runnable() {
			@Override
			public void run() {
				
				if (settingControls) {
					return;
				}
				startSettingControls();
				
				comboFromTo.setSelectedIndex(0);
				
				stopSettingControls();
			}
		});
		
		// Time points edit listener.
		listTimePoints.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					editTimePoint();
				}
			}});
	}

	/**
	 * Edit selected time point.
	 */
	protected void editTimePoint() {
		
		String timePoint = listTimePoints.getSelectedValue();
		if (timePoint == null) {
			
			Utility.show(this, "org.multipage.gui.messageYouHaveToSelectTimePoint");
			return;
		}
		
		// Get new value.
		String newTimePoint = Utility.input(this, "org.multipage.gui.messageEditTimePoint", timePoint);
		if (newTimePoint == null) {
			return;
		}
		
		// Check new value.
		if (!checkTimePoint(newTimePoint)) {
			
			Utility.show(this, "org.multipage.gui.messageBadTimePointDefinition");
			return;
		}
		
		// Save new value.
		int index = listTimePoints.getSelectedIndex();
		if (index >= 0) {
			listTimePointsModel.set(index, newTimePoint);
		}
	}

	/**
	 * Check time point.
	 * @param timePoint
	 * @return
	 */
	private boolean checkTimePoint(String timePoint) {
		
		if (timePoint.equals("from") || timePoint.equals("to")) {
			return true;
		}
		
		Obj<Integer> position = new Obj<Integer>(0);
		 
		String text = Utility.getNextMatch(timePoint, position, "^\\d+%$");
		if (text == null) {
			return false;
		}
		
		String percentText = text.substring(0, position.ref - 1);
		try {
			int percent = Integer.parseInt(percentText);
			if (percent >= 0 && percent <= 100) {
				return true;
			}
		}
		catch (Exception e) {
		}
		
		return false;
	}

	/**
	 * Load combo boxes.
	 */
	private void loadComboBoxes() {
		
		Utility.loadEmptyItem(comboFromTo);
		Utility.loadNamedItems(comboFromTo, new String [][] {
				{"from", "org.multipage.gui.textCssKeyframeFrom"},
				{"to", "org.multipage.gui.textCssKeyframeTo"}
				});
		
		comboFromTo.setSelectedIndex(1);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonAddTimePoint.setIcon(Images.getIcon("org/multipage/gui/images/arrow.png"));
		menuDelete.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonDeleteTimePoint.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonAddProperty.setIcon(Images.getIcon("org/multipage/gui/images/arrow.png"));
		buttonDeleteProperty.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonFindProperty.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
		menuDeleteProperty.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelPointsInTime);
		Utility.localize(menuDelete);
		Utility.localize(labelProperties);
		Utility.localize(labelTime);
		Utility.localize(labelCssProperty);
		Utility.localize(labelCssPropertyValue);
		Utility.localize(menuDeleteProperty);
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
		
		// Check input.
		if (!checkTimePoints()) {
			Utility.show(this, "org.multipage.gui.messageYouHaveToInsertTimePoints");
			return;
		}
		
		if (!checkProperties()) {
			Utility.show(this, "org.multipage.gui.messageYouHaveToSetAnimatedProperties");
			return;
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
	
	/**
	 * Add popup menu.
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
}
