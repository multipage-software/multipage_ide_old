/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.border.LineBorder;

import org.multipage.util.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author
 *
 */
public class CssOutlinesPanel extends InsertPanel implements StringValueEditor {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	private static String left = "0px";
	private static String bottom = "0px";
	private static String right = "0px";
	private static String top = "0px";
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 285, 260);
		boundsSet = false;
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
		boundsSet = true;		
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
	 * Initial string.
	 */
	private String initialString;
	
	// $hide<<$
	
	private JPanel panel;
	private JPanel panelTop;
	private JPanel panelLeft;
	private JPanel panelRight;
	private JLabel labelTop;
	private JTextField textTop;
	private JComboBox comboBoxTop;
	private JPanel panelBottom;
	private JTextField textBottom;
	private JLabel labelBottom;
	private JComboBox comboBoxBottom;
	private JComboBox comboBoxLeft;
	private JTextField textLeft;
	private JLabel labelLeft;
	private JTextField textRight;
	private JComboBox comboBoxRight;
	private JLabel labelRight;
	private JCheckBox checkCenter;
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssOutlinesPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		springLayout.putConstraint(SpringLayout.NORTH, panel, 20, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, panel, 20, SpringLayout.WEST, this);
		panel.setPreferredSize(new Dimension(240, 180));
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(10, 50));
		panel.add(panelTop, BorderLayout.NORTH);
		SpringLayout sl_panelTop = new SpringLayout();
		panelTop.setLayout(sl_panelTop);
		
		labelTop = new JLabel("org.multipage.gui.textOutlineTop");
		panelTop.add(labelTop);
		
		textTop = new TextFieldEx();
		sl_panelTop.putConstraint(SpringLayout.SOUTH, labelTop, -3, SpringLayout.NORTH, textTop);
		sl_panelTop.putConstraint(SpringLayout.WEST, textTop, 75, SpringLayout.WEST, panelTop);
		sl_panelTop.putConstraint(SpringLayout.WEST, labelTop, 0, SpringLayout.WEST, textTop);
		sl_panelTop.putConstraint(SpringLayout.SOUTH, textTop, 0, SpringLayout.SOUTH, panelTop);
		panelTop.add(textTop);
		textTop.setColumns(5);
		
		comboBoxTop = new JComboBox();
		sl_panelTop.putConstraint(SpringLayout.WEST, comboBoxTop, 0, SpringLayout.EAST, textTop);
		comboBoxTop.setPreferredSize(new Dimension(50, 20));
		sl_panelTop.putConstraint(SpringLayout.NORTH, comboBoxTop, 0, SpringLayout.NORTH, textTop);
		panelTop.add(comboBoxTop);
		
		panelLeft = new JPanel();
		panelLeft.setPreferredSize(new Dimension(120, 10));
		panel.add(panelLeft, BorderLayout.WEST);
		SpringLayout sl_panelLeft = new SpringLayout();
		panelLeft.setLayout(sl_panelLeft);
		
		comboBoxLeft = new JComboBox();
		sl_panelLeft.putConstraint(SpringLayout.SOUTH, comboBoxLeft, 0, SpringLayout.SOUTH, panelLeft);
		sl_panelLeft.putConstraint(SpringLayout.EAST, comboBoxLeft, -10, SpringLayout.EAST, panelLeft);
		comboBoxLeft.setPreferredSize(new Dimension(50, 20));
		panelLeft.add(comboBoxLeft);
		
		textLeft = new TextFieldEx();
		sl_panelLeft.putConstraint(SpringLayout.SOUTH, textLeft, 0, SpringLayout.SOUTH, comboBoxLeft);
		sl_panelLeft.putConstraint(SpringLayout.EAST, textLeft, 0, SpringLayout.WEST, comboBoxLeft);
		textLeft.setColumns(5);
		panelLeft.add(textLeft);
		
		labelLeft = new JLabel("org.multipage.gui.textOutlineLeft");
		sl_panelLeft.putConstraint(SpringLayout.WEST, labelLeft, 0, SpringLayout.WEST, textLeft);
		sl_panelLeft.putConstraint(SpringLayout.SOUTH, labelLeft, -3, SpringLayout.NORTH, textLeft);
		panelLeft.add(labelLeft);
		
		panelRight = new JPanel();
		panelRight.setPreferredSize(new Dimension(120, 10));
		panel.add(panelRight, BorderLayout.EAST);
		SpringLayout sl_panelRight = new SpringLayout();
		panelRight.setLayout(sl_panelRight);
		
		textRight = new TextFieldEx();
		sl_panelRight.putConstraint(SpringLayout.WEST, textRight, 10, SpringLayout.WEST, panelRight);
		sl_panelRight.putConstraint(SpringLayout.SOUTH, textRight, 0, SpringLayout.SOUTH, panelRight);
		textRight.setColumns(5);
		panelRight.add(textRight);
		
		comboBoxRight = new JComboBox();
		sl_panelRight.putConstraint(SpringLayout.WEST, comboBoxRight, 0, SpringLayout.EAST, textRight);
		sl_panelRight.putConstraint(SpringLayout.SOUTH, comboBoxRight, 0, SpringLayout.SOUTH, textRight);
		comboBoxRight.setPreferredSize(new Dimension(50, 20));
		panelRight.add(comboBoxRight);
		
		labelRight = new JLabel("org.multipage.gui.textOutlineRight");
		sl_panelRight.putConstraint(SpringLayout.WEST, labelRight, 0, SpringLayout.WEST, textRight);
		sl_panelRight.putConstraint(SpringLayout.SOUTH, labelRight, -3, SpringLayout.NORTH, textRight);
		panelRight.add(labelRight);
		
		panelBottom = new JPanel();
		panelBottom.setPreferredSize(new Dimension(10, 80));
		panel.add(panelBottom, BorderLayout.SOUTH);
		SpringLayout sl_panelBottom = new SpringLayout();
		panelBottom.setLayout(sl_panelBottom);
		
		textBottom = new TextFieldEx();
		sl_panelBottom.putConstraint(SpringLayout.WEST, textBottom, 75, SpringLayout.WEST, panelBottom);
		textBottom.setColumns(5);
		panelBottom.add(textBottom);
		
		labelBottom = new JLabel("org.multipage.gui.textOutlineBottom");
		sl_panelBottom.putConstraint(SpringLayout.NORTH, textBottom, 3, SpringLayout.SOUTH, labelBottom);
		sl_panelBottom.putConstraint(SpringLayout.NORTH, labelBottom, 20, SpringLayout.NORTH, panelBottom);
		sl_panelBottom.putConstraint(SpringLayout.WEST, labelBottom, 0, SpringLayout.WEST, textBottom);
		panelBottom.add(labelBottom);
		
		comboBoxBottom = new JComboBox();
		sl_panelBottom.putConstraint(SpringLayout.NORTH, comboBoxBottom, 0, SpringLayout.NORTH, textBottom);
		sl_panelBottom.putConstraint(SpringLayout.WEST, comboBoxBottom, 0, SpringLayout.EAST, textBottom);
		comboBoxBottom.setPreferredSize(new Dimension(50, 20));
		panelBottom.add(comboBoxBottom);
		
		checkCenter = new JCheckBox("org.multipage.gui.textCenter");
		checkCenter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCenter();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkCenter, 32, SpringLayout.SOUTH, panel);
		springLayout.putConstraint(SpringLayout.WEST, checkCenter, 110, SpringLayout.WEST, this);
		add(checkCenter);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		loadFromString(initialString);
	}
	
	/**
	 * Load from initial string.
	 */
	private void loadFromString(String string) {
		
		initialString = string;
		
		setTop(CssOutlinesPanel.top);
		setRight(CssOutlinesPanel.right);
		setBottom(CssOutlinesPanel.bottom);
		setLeft(CssOutlinesPanel.left);

		if (initialString != null) {
			setFromInitialString();
		}
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		
		loadUnits();
		
		loadDialog();
	}

	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		Utility.loadCssUnits(comboBoxTop);
		Utility.loadCssUnits(comboBoxRight);
		Utility.loadCssUnits(comboBoxBottom);
		Utility.loadCssUnits(comboBoxLeft);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		if (checkCenter.isSelected()) {
			return getTop() + " auto " + getBottom() + " auto";
		}
		return getTop() + " " + getRight() + " " + getBottom() + " " + getLeft();
	}

	/**
	 * Get left.
	 * @return
	 */
	private String getLeft() {
		
		if (checkCenter.isSelected()) {
			return "auto";
		}
		
		String value = textLeft.getText();
		String units = "";
		
		try {
			value = String.valueOf(Double.parseDouble(value));
		}
		catch (Exception e) {
			value = "0";
		}
		
		Object item = comboBoxLeft.getSelectedItem();
		if (item instanceof String) {
			
			units = (String) item;
		}

		return value + units;
	}
	
	/**
	 * Set left.
	 * @param string
	 */
	private void setLeft(String string) {
		
		if (string.equals("auto")) {
			
			textLeft.setText("0");
			Utility.selectComboItem(comboBoxRight, "px");
			
			checkCenter.setSelected(true);
			onCenter();
			
			return;
		}
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(string, number, unit);
		
		textLeft.setText(number.ref);
		Utility.selectComboItem(comboBoxLeft, unit.ref);
	}

	/**
	 * Get bottom.
	 * @return
	 */
	private String getBottom() {
		
		String value = textBottom.getText();
		String units = "";
		
		try {
			value = String.valueOf(Double.parseDouble(value));
		}
		catch (Exception e) {
			value = "0";
		}
		
		Object item = comboBoxBottom.getSelectedItem();
		if (item instanceof String) {
			
			units = (String) item;
		}

		return value + units;
	}

	/**
	 * Set bottom.
	 * @param string
	 */
	private void setBottom(String string) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(string, number, unit);
		
		textBottom.setText(number.ref);
		Utility.selectComboItem(comboBoxBottom, unit.ref);
	}

	/**
	 * Get right.
	 * @return
	 */
	private String getRight() {
		
		if (checkCenter.isSelected()) {
			return "auto";
		}

		String value = textRight.getText();
		String units = "";
		
		try {
			value = String.valueOf(Double.parseDouble(value));
		}
		catch (Exception e) {
			value = "0";
		}
		
		Object item = comboBoxRight.getSelectedItem();
		if (item instanceof String) {
			
			units = (String) item;
		}

		return value + units;
	}

	/**
	 * Set right.
	 * @param string
	 */
	private void setRight(String string) {
		
		if (string.equals("auto")) {
			
			textRight.setText("0");
			Utility.selectComboItem(comboBoxRight, "px");
			
			checkCenter.setSelected(true);
			onCenter();
			
			return;
		}
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(string, number, unit);
		
		textRight.setText(number.ref);
		Utility.selectComboItem(comboBoxRight, unit.ref);
	}

	/**
	 * Get top.
	 * @return
	 */
	private String getTop() {

		String value = textTop.getText();
		String units = "";
		
		try {
			value = String.valueOf(Double.parseDouble(value));
		}
		catch (Exception e) {
			value = "0";
		}
		
		Object item = comboBoxTop.getSelectedItem();
		if (item instanceof String) {
			
			units = (String) item;
		}
		
		return value + units;
	}

	/**
	 * Set top.
	 * @param string
	 */
	private void setTop(String string) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(string, number, unit);
		
		textTop.setText(number.ref);
		Utility.selectComboItem(comboBoxTop, unit.ref);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		Scanner scanner = new Scanner(initialString.trim());
		
		try {
			String value = scanner.next();
			setTop(value.trim());
			
			value = scanner.next();
			setRight(value.trim());
			
			value = scanner.next();
			setBottom(value.trim());
			
			value = scanner.next();
			setLeft(value.trim());
		}
		catch (Exception e) {
		}
		
	    scanner.close();
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelTop);
		Utility.localize(labelRight);
		Utility.localize(labelBottom);
		Utility.localize(labelLeft);
		Utility.localize(checkCenter);
	}

	/**
	 * On check / uncheck center flag.
	 */
	protected void onCenter() {
		
		boolean checked = checkCenter.isSelected();
		
		if (checked) {
			textLeft.setEditable(false);
			textRight.setEditable(false);
			
			textLeft.setBackground(Color.LIGHT_GRAY);
			textRight.setBackground(Color.LIGHT_GRAY);
		}
		else {
			textLeft.setEditable(true);
			textRight.setEditable(true);
			
			textLeft.setBackground(Color.WHITE);
			textRight.setBackground(Color.WHITE);
		}
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssOutlinesBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getResultText()
	 */
	@Override
	public String getResultText() {
		
		return getSpecification();
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getContainerDialogBounds()
	 */
	@Override
	public Rectangle getContainerDialogBounds() {
		
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		CssOutlinesPanel.bounds = bounds;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#boundsSet()
	 */
	@Override
	public boolean isBoundsSet() {

		return boundsSet;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setBoundsSet(boolean)
	 */
	@Override
	public void setBoundsSet(boolean set) {
		
		boundsSet = set;
	}

	/**
	 * Get component.
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get specification.
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		loadFromString(string);
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssOutlines;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
