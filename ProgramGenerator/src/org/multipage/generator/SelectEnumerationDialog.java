/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.maclan.EnumerationObj;
import org.maclan.EnumerationValue;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class SelectEnumerationDialog extends JDialog {

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
	 * Enumerations list.
	 */
	private LinkedList<EnumerationObj> enumerations = new LinkedList<>();

	/**
	 * Selected enumeration value.
	 */
	private EnumerationValue selectedEnumerationValue;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelEnumerationName;
	private JComboBox comboEnumeration;
	private JToolBar toolBarEnumeration;
	private JLabel labelSelectValue;
	private JComboBox comboValue;
	private JMenuBar menuBar;
	private JMenu menuSettings;
	private JMenuItem menuTextFormat;

	/**
	 * Show dialog.
	 * @param parent
	 * @param inputText 
	 * @return
	 */
	public static String showDialog(Component parent, String inputText) {
		
		SelectEnumerationDialog dialog = new SelectEnumerationDialog(parent);
		dialog.selectValuesFromText(inputText);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getSelectedEnumerationValueText();
		}
		return null;
	}
	
	/**
	 * Select values from input text.
	 * @param inputText
	 */
	private void selectValuesFromText(String inputText) {
		
		// Match regex against the input text.
		String inputFormat = SelectEnumerationFormatDialog.selectedFormat.input;
		Pattern pattern = Pattern.compile(inputFormat);
		Matcher matcher = pattern.matcher(inputText);
		
		if (!matcher.matches()) {
			return;
		}
		
		if (matcher.groupCount() < 2) {
			return;
		}
		
		// Get enumeration name.
		String enumeration = matcher.group(1);
		if (enumeration == null) {
			return;
		}
		enumeration = enumeration.trim();
		
		// Get value name.
		String value = matcher.group(2);
		if (value == null) {
			return;
		}
		value = value.trim();
		
		// Select enumeration and its value.
		selectEnumerationText(enumeration);
		
		final String valueText = value;
		SwingUtilities.invokeLater(() -> { selectValueText(valueText); });
		
	}

	/**
	 * Select enumeration combo box text.
	 * @param text
	 */
	private void selectEnumerationText(String text) {
		
		int count = comboEnumeration.getItemCount();
		
		for (int index = 0; index < count; index++) {
			Object object = comboEnumeration.getItemAt(index);
			
			if (!(object instanceof EnumerationObj)) {
				continue;
			}
			
			EnumerationObj enumeration = (EnumerationObj) object;
			if (enumeration.getDescription().equals(text)) {
				
				// If found select it and exit.
				comboEnumeration.setSelectedIndex(index);
				return;
			}
		}
	}
	
	/**
	 * Select enumeration value combo box text.
	 * @param text
	 */
	private void selectValueText(String text) {
		
		int count = comboValue.getItemCount();
		
		for (int index = 0; index < count; index++) {
			Object object = comboValue.getItemAt(index);
			
			if (!(object instanceof EnumerationValue)) {
				continue;
			}
			
			EnumerationValue value = (EnumerationValue) object;
			if (value.getValue().equals(text)) {
				
				// If found select it and exit.
				comboValue.setSelectedIndex(index);
				return;
			}
		}
	}

	/**
	 * Get selected enumeration value text.
	 * @return
	 */
	private String getSelectedEnumerationValueText() {
		
		// Check the value.
		if (selectedEnumerationValue == null) {
			return null;
		}
		
		// Compile string.
		String outputFormat = SelectEnumerationFormatDialog.selectedFormat.output;
		
		return String.format(outputFormat,
				selectedEnumerationValue.getEnumeration().getDescription(),
				selectedEnumerationValue.getValue());
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public SelectEnumerationDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setMinimumSize(new Dimension(300, 300));
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuSettings = new JMenu("org.multipage.generator.menuSelectEnumerationSettings");
		menuBar.add(menuSettings);
		
		menuTextFormat = new JMenuItem("org.multipage.generator.menuSelectEnumerationTextFormat");
		menuTextFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onTextFormat();
			}
		});
		menuSettings.add(menuTextFormat);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * On select text format.
	 */
	protected void onTextFormat() {
		
		// Select format.
		SelectEnumerationFormatDialog.showDialog(this);
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textSelectEnumeration");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 450, 304);
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
		
		labelEnumerationName = new JLabel("org.multipage.generator.textSelectEnumerationName");
		springLayout.putConstraint(SpringLayout.NORTH, labelEnumerationName, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelEnumerationName, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelEnumerationName);
		
		comboEnumeration = new JComboBox();
		comboEnumeration.setFont(new Font("Tahoma", Font.PLAIN, 12));
		comboEnumeration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onSelectEnumeration();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, comboEnumeration, -10, SpringLayout.EAST, getContentPane());
		comboEnumeration.setPreferredSize(new Dimension(28, 40));
		springLayout.putConstraint(SpringLayout.NORTH, comboEnumeration, 6, SpringLayout.SOUTH, labelEnumerationName);
		springLayout.putConstraint(SpringLayout.WEST, comboEnumeration, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(comboEnumeration);
		
		toolBarEnumeration = new JToolBar();
		springLayout.putConstraint(SpringLayout.NORTH, toolBarEnumeration, 0, SpringLayout.SOUTH, comboEnumeration);
		toolBarEnumeration.setFloatable(false);
		springLayout.putConstraint(SpringLayout.WEST, toolBarEnumeration, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, toolBarEnumeration, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(toolBarEnumeration);
		
		labelSelectValue = new JLabel("org.multipage.generator.textSelectEnumerationValue");
		labelSelectValue.setVisible(false);
		springLayout.putConstraint(SpringLayout.NORTH, labelSelectValue, 28, SpringLayout.SOUTH, toolBarEnumeration);
		springLayout.putConstraint(SpringLayout.WEST, labelSelectValue, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelSelectValue);
		
		comboValue = new JComboBox();
		comboValue.setFont(new Font("Tahoma", Font.BOLD, 12));
		comboValue.setVisible(false);
		springLayout.putConstraint(SpringLayout.WEST, comboValue, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboValue, -10, SpringLayout.EAST, getContentPane());
		comboValue.setPreferredSize(new Dimension(28, 40));
		springLayout.putConstraint(SpringLayout.NORTH, comboValue, 6, SpringLayout.SOUTH, labelSelectValue);
		getContentPane().add(comboValue);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		initializeComboBox();
		loadEnumerations();
		
		loadDialog();
	}

	/**
	 * Initialize combo box.
	 */
	private void initializeComboBox() {
		
		comboValue.setRenderer(new ListCellRenderer<EnumerationValue>() {
			
			DefaultListCellRenderer  renderer = new DefaultListCellRenderer();
			
			@Override
			public Component getListCellRendererComponent(JList<? extends EnumerationValue> list,
					EnumerationValue value, int index, boolean isSelected, boolean cellHasFocus) {
				
			    if (isSelected) {
			    	renderer.setBackground(list.getSelectionBackground());
			    	renderer.setForeground(list.getSelectionForeground());
			    }
			    else {
			    	renderer.setBackground(list.getBackground());
			    	renderer.setForeground(list.getForeground());
			    }
			    
				renderer.setText(value.getValueDescriptionBuilder());
				return renderer;
			}
		});
	}

	/**
	 * Load enumerations.
	 */
	private void loadEnumerations() {
		
		// Load them from the database.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		MiddleResult result = middle.loadEnumerations(login, enumerations);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Load combo box items.
		for (EnumerationObj enumeration : enumerations) {
			
			comboEnumeration.addItem(enumeration);
		}
	}

	/**
	 * Load values.
	 * @param enumeration 
	 */
	private void loadValues(EnumerationObj enumeration) {
		
		comboValue.removeAllItems();
		
		if (enumeration == null) {
			return;
		}
		
		for (EnumerationValue value : enumeration.getValues()) {
			comboValue.addItem(value);
		}
	}

	/**
	 * On select enumeration.
	 */
	protected void onSelectEnumeration() {
		
		// Get selected enumeration.
		Object selectedObject = comboEnumeration.getSelectedItem();
		if (!(selectedObject instanceof EnumerationObj)) {
			return;
		}
		
		EnumerationObj enumeration = (EnumerationObj) selectedObject;
		
		// Show values combo box.
		if (!comboValue.isVisible()) {
			labelSelectValue.setVisible(true);
			comboValue.setVisible(true);
		}
		
		// Load values.
		loadValues(enumeration);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		menuTextFormat.setIcon(Images.getIcon("org/multipage/generator/images/text_format.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelEnumerationName);
		Utility.localize(labelSelectValue);
		Utility.localize(menuSettings);
		Utility.localize(menuTextFormat);
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
		
		// Get and check selected values.
		Object selectedObject = comboValue.getSelectedItem();
		if (!(selectedObject instanceof EnumerationValue)) {
			
			Utility.show(this, "org.multipage.generator.messageSelectEnumerationValueOrCancel");
			return;
		}
		
		selectedEnumerationValue = (EnumerationValue) selectedObject;
		
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
