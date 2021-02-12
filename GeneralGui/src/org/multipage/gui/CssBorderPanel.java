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
import javax.swing.border.*;

import org.multipage.util.*;

import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CssBorderPanel extends InsertPanel implements StringValueEditor {

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
	private static Color colorState;
	private static String style;
	private static String width;


	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 400, 230);
		boundsSet = false;
		colorState = Color.BLACK;
		style = "none";
		width = "medium";
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
		colorState = Utility.readInputStreamObject(inputStream, Color.class);
		style = inputStream.readUTF();
		width = inputStream.readUTF();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(colorState);
		outputStream.writeUTF(style == null ? "" : style);
		outputStream.writeUTF(width == null ? "" : width);
	}

	/**
	 * Border color.
	 */
	private Color color;
	
	/**
	 * Initial string.
	 */
	private String initialString;
	
	// $hide<<$
	private JLabel labelBorderStyle;
	private JComboBox comboBoxStyle;
	private JLabel labelBorderWidth;
	private JComboBox comboBoxWidth;
	private JTextField textWidth;
	private JComboBox comboBoxUnits;
	private JLabel labelBorderColor;
	private JPanel panelColor;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssBorderPanel(String initialString) {

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
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelBorderStyle = new JLabel("org.multipage.gui.textBorderStyle");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelBorderStyle, 20, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelBorderStyle, 10, SpringLayout.WEST, this);
		add(labelBorderStyle);
		
		comboBoxStyle = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxStyle, 20, SpringLayout.NORTH, this);
		comboBoxStyle.setPreferredSize(new Dimension(150, 20));
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxStyle, 6, SpringLayout.EAST, labelBorderStyle);
		add(comboBoxStyle);
		
		labelBorderWidth = new JLabel("org.multipage.gui.textBorderWidth");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelBorderWidth, 26, SpringLayout.SOUTH, labelBorderStyle);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelBorderWidth, 0, SpringLayout.WEST, labelBorderStyle);
		add(labelBorderWidth);
		
		comboBoxWidth = new JComboBox();
		comboBoxWidth.setPreferredSize(new Dimension(100, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxWidth, -3, SpringLayout.NORTH, labelBorderWidth);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxWidth, 0, SpringLayout.WEST, comboBoxStyle);
		add(comboBoxWidth);
		
		textWidth = new JTextField();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textWidth, 0, SpringLayout.NORTH, comboBoxWidth);
		sl_panelMain.putConstraint(SpringLayout.WEST, textWidth, 6, SpringLayout.EAST, comboBoxWidth);
		add(textWidth);
		textWidth.setColumns(6);
		
		comboBoxUnits = new JComboBox();
		comboBoxUnits.setPreferredSize(new Dimension(50, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxUnits, 0, SpringLayout.NORTH, textWidth);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxUnits, 6, SpringLayout.EAST, textWidth);
		add(comboBoxUnits);
		
		labelBorderColor = new JLabel("org.multipage.gui.textBorderColor");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelBorderColor, 26, SpringLayout.SOUTH, labelBorderWidth);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelBorderColor, 0, SpringLayout.WEST, labelBorderStyle);
		add(labelBorderColor);
		
		panelColor = new JPanel();
		panelColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onSelectColor();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.WEST, panelColor, 6, SpringLayout.EAST, labelBorderColor);
		panelColor.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panelColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panelColor.setPreferredSize(new Dimension(80, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, panelColor, 0, SpringLayout.NORTH, labelBorderColor);
		add(panelColor);
	}

	/**
	 * On select color.
	 */
	protected void onSelectColor() {
		
		Color newColor = Utility.chooseColor(this, color);
		
		if (newColor != null) {
			color = newColor;
			
			panelColor.setBackground(color);
		}
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
		
		setBorderStyle(style);
		setBorderWidth(width);
		setBorderColor(colorState);
		
		if (initialString != null) {
			setFromInitialString();
		}
	}

	/**
	 * Set border color.
	 * @param color
	 */
	private void setBorderColor(Color color) {
		
		this.color = color;
		panelColor.setBackground(color);
	}

	/**
	 * Set border color.
	 * @param cssColor
	 */
	private void setBorderColor(String cssColor) {
		
		if (cssColor.length() != 7 && cssColor.charAt(0) != '#') {
			return;
		}
		
		int red = Integer.parseInt(cssColor.substring(1, 3), 16);
		int green = Integer.parseInt(cssColor.substring(3, 5), 16);
		int blue = Integer.parseInt(cssColor.substring(5, 7), 16);
		
		Color color = new Color(red, green, blue);
		setBorderColor(color);
	}

	/**
	 * Set border width.
	 * @param width
	 */
	private void setBorderWidth(String width) {
		
		CssBorderPanel.width = width;
		
		// Try to select width combo.
		if (Utility.selectComboNamedItem(comboBoxWidth, width)) {
			textWidth.setText("");
			return;
		}
		
		// Get width and unit.
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		Utility.convertCssStringToNumberUnit(width, number, unit);
		
		textWidth.setText(number.ref);
		
		// Select unit.
		Utility.selectComboItem(comboBoxUnits, unit.ref);
	}

	/**
	 * Set border style.
	 * @param style
	 */
	private void setBorderStyle(String style) {
		
		CssBorderPanel.style = style;
		
		// Try to select style combo.
		Utility.selectComboNamedItem(comboBoxStyle, style);
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
		style = getBorderStyle();
		width = getBorderWidth();
		
		colorState = color;
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		
		loadBorderStyles();
		loadBorderWidths();
		
		loadDialog();
	}

	/**
	 * Load border widths.
	 */
	private void loadBorderWidths() {
		
		final NamedItem [] widths = {
				new NamedItem("org.multipage.gui.textBorderWidthThin", "thin"),
				new NamedItem("org.multipage.gui.textBorderWidthMedium", "medium"),
				new NamedItem("org.multipage.gui.textBorderWidthThick", "thick")
		};
		
		for (NamedItem width : widths) {
			comboBoxWidth.addItem(width);
		}
		
		Utility.loadCssUnits(comboBoxUnits);
	}

	/**
	 * Load border styles.
	 */
	private void loadBorderStyles() {
		
		final NamedItem [] styles = {
				new NamedItem("org.multipage.gui.textBorderStyleNone", "none"),
				new NamedItem("org.multipage.gui.textBorderStyleDotted", "dotted"),
				new NamedItem("org.multipage.gui.textBorderStyleDashed", "dashed"),
				new NamedItem("org.multipage.gui.textBorderStyleSolid", "solid"),
				new NamedItem("org.multipage.gui.textBorderStyleDouble", "double"),
				new NamedItem("org.multipage.gui.textBorderStyleGroove", "groove"),
				new NamedItem("org.multipage.gui.textBorderStyleRidge", "ridge"),
				new NamedItem("org.multipage.gui.textBorderStyleInset", "inset"),
				new NamedItem("org.multipage.gui.textBorderStyleOutset", "outset")
		};
		
		for (NamedItem style : styles) {
			comboBoxStyle.addItem(style);
		}
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return getBorderStyle() + " " + getBorderWidth() + " " + getBorderColor();
	}

	/**
	 * Get border color.
	 * @return
	 */
	private String getBorderColor() {
		
		return Utility.getCssColor(color);
	}

	/**
	 * Get border width.
	 * @return
	 */
	private String getBorderWidth() {
		
		// Try to get width number.
		try {
			
			String numberText = textWidth.getText();
			if (!numberText.isEmpty()) {
				
				double number = Double.parseDouble(numberText);
				
				// Get unit.
				String unit = (String) comboBoxUnits.getSelectedItem();
				
				return String.valueOf(number) + unit;
			}
		}
		catch (Exception e) {
		}
		
		Object object = comboBoxWidth.getSelectedItem();
		if (!(object instanceof NamedItem)) {
			return "";
		}
		
		return ((NamedItem) object).value;
	}

	/**
	 * Get border style.
	 * @return
	 */
	private String getBorderStyle() {
		
		Object object = comboBoxStyle.getSelectedItem();
		if (!(object instanceof NamedItem)) {
			return "none";
		}
		
		return ((NamedItem) object).value;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		Scanner scanner = new Scanner(initialString.trim());
		
		try {
			// Get style.
			String style = scanner.next();
			if (style != null) {
				setBorderStyle(style.trim());
				
				// Get width.
				String width = scanner.next();
				if (width != null) {
					setBorderWidth(width.trim());
					
					// Get color.
					String color = scanner.nextLine();
					if (color != null) {
						setBorderColor(color.trim());
					}
				}
			}
		}
		catch (Exception e) {
		}
		
	    scanner.close();
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelBorderStyle);
		Utility.localize(labelBorderWidth);
		Utility.localize(labelBorderColor);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssBorderBuilder");
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
		
		CssBorderPanel.bounds = bounds;
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
	 * Get string value.
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
		
		return meansCssBorder;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
