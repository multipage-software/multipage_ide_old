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
public class CssBoxShadowPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelHorizontal;
	private TextFieldEx textHorizontal;
	private JComboBox comboHorizontalUnits;
	private JLabel labelVertical;
	private TextFieldEx textVertical;
	private JComboBox comboVerticalUnits;
	private TextFieldEx textBlur;
	private JLabel labelBlur;
	private JComboBox comboBlurUnits;
	private TextFieldEx textSpread;
	private JComboBox comboSpreadUnits;
	private JLabel labelSpread;
	private JPanel panelColor;
	private JLabel labelColor;
	private JCheckBox checkInset;

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
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 500, 260);
		boundsSet = false;
		colorState = Color.BLACK;
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
	}

	/**
	 * Initial string.
	 */
	private String initialString;
	
	/**
	 * Color.
	 */
	private Color color;
	
	// $hide<<$

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssBoxShadowPanel(String initialString) {

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
		
		labelHorizontal = new JLabel("org.multipage.gui.textShadowHorizontalPosition");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelHorizontal, 43, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelHorizontal, 20, SpringLayout.WEST, this);
		add(labelHorizontal);
		
		textHorizontal = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textHorizontal, 0, SpringLayout.NORTH, labelHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, textHorizontal, 6, SpringLayout.EAST, labelHorizontal);
		textHorizontal.setColumns(5);
		add(textHorizontal);
		
		comboHorizontalUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboHorizontalUnits, 0, SpringLayout.NORTH, labelHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboHorizontalUnits, 0, SpringLayout.EAST, textHorizontal);
		comboHorizontalUnits.setPreferredSize(new Dimension(50, 20));
		add(comboHorizontalUnits);
		
		labelVertical = new JLabel("org.multipage.gui.textShadowVerticalPosition");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelVertical, 0, SpringLayout.NORTH, labelHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelVertical, 16, SpringLayout.EAST, comboHorizontalUnits);
		add(labelVertical);
		
		textVertical = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textVertical, 0, SpringLayout.NORTH, labelHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, textVertical, 6, SpringLayout.EAST, labelVertical);
		textVertical.setColumns(5);
		add(textVertical);
		
		comboVerticalUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboVerticalUnits, 0, SpringLayout.NORTH, labelHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboVerticalUnits, 0, SpringLayout.EAST, textVertical);
		comboVerticalUnits.setPreferredSize(new Dimension(50, 20));
		add(comboVerticalUnits);
		
		textBlur = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textBlur, 24, SpringLayout.SOUTH, textHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, textBlur, 0, SpringLayout.WEST, textHorizontal);
		textBlur.setColumns(5);
		add(textBlur);
		
		labelBlur = new JLabel("org.multipage.gui.textShadowBlur");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelBlur, 0, SpringLayout.NORTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelBlur, 0, SpringLayout.EAST, labelHorizontal);
		add(labelBlur);
		
		comboBlurUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBlurUnits, 0, SpringLayout.NORTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBlurUnits, 0, SpringLayout.WEST, comboHorizontalUnits);
		comboBlurUnits.setPreferredSize(new Dimension(50, 20));
		add(comboBlurUnits);
		
		textSpread = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textSpread, 0, SpringLayout.NORTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.WEST, textSpread, 0, SpringLayout.WEST, textVertical);
		textSpread.setColumns(5);
		add(textSpread);
		
		comboSpreadUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboSpreadUnits, 0, SpringLayout.NORTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboSpreadUnits, 0, SpringLayout.WEST, comboVerticalUnits);
		comboSpreadUnits.setPreferredSize(new Dimension(50, 20));
		add(comboSpreadUnits);
		
		labelSpread = new JLabel("org.multipage.gui.textShadowSpread");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSpread, 0, SpringLayout.NORTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelSpread, 0, SpringLayout.EAST, labelVertical);
		add(labelSpread);
		
		panelColor = new JPanel();
		panelColor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				onSelectColor();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.EAST, panelColor, 0, SpringLayout.EAST, comboHorizontalUnits);
		panelColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		sl_panelMain.putConstraint(SpringLayout.NORTH, panelColor, 24, SpringLayout.SOUTH, textBlur);
		sl_panelMain.putConstraint(SpringLayout.WEST, panelColor, 0, SpringLayout.WEST, textHorizontal);
		panelColor.setPreferredSize(new Dimension(80, 20));
		panelColor.setBorder(new LineBorder(Color.LIGHT_GRAY));
		add(panelColor);
		
		labelColor = new JLabel("org.multipage.gui.textShadowColor");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelColor, 30, SpringLayout.SOUTH, labelBlur);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelColor, 0, SpringLayout.EAST, labelHorizontal);
		add(labelColor);
		
		checkInset = new JCheckBox("org.multipage.gui.textShadowInset");
		checkInset.setIconTextGap(6);
		sl_panelMain.putConstraint(SpringLayout.NORTH, checkInset, 0, SpringLayout.NORTH, panelColor);
		sl_panelMain.putConstraint(SpringLayout.WEST, checkInset, 10, SpringLayout.WEST, labelVertical);
		add(checkInset);
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

		setFromInitialString();
	}

	/**
	 * Set color.
	 * @param color
	 */
	private void setColor(Color color) {
		
		this.color = color;
		
		panelColor.setBackground(color);
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
		colorState = color;
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
		
		Utility.loadCssUnits(comboHorizontalUnits);
		Utility.loadCssUnits(comboVerticalUnits);
		Utility.loadCssUnits(comboBlurUnits);
		Utility.loadCssUnits(comboSpreadUnits);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String inset = getInset();
		if (!inset.isEmpty()) {
			inset = " " + inset;
		}
		return getHorizontal()  + " " + getVertical() + " " + getBlur() + " " + getSpread() + " " + getColor() + inset;
	}

	/**
	 * Get inset.
	 * @return
	 */
	private String getInset() {
		
		return checkInset.isSelected() ? "inset" : "";
	}

	/**
	 * Get color.
	 * @return
	 */
	private String getColor() {
		
		return Utility.getCssColor(color);
	}

	/**
	 * Get spread.
	 * @return
	 */
	private String getSpread() {
		
		return Utility.getCssValueAndUnits(textSpread, comboSpreadUnits);
	}

	/**
	 * Get blur.
	 * @return
	 */
	private String getBlur() {
		
		return Utility.getCssValueAndUnits(textBlur, comboBlurUnits);
	}

	/**
	 * Get vertical position.
	 * @return
	 */
	private String getVertical() {
		
		return Utility.getCssValueAndUnits(textVertical, comboVerticalUnits);
	}

	/**
	 * Get horizontal position.
	 * @return
	 */
	private String getHorizontal() {
		
		return Utility.getCssValueAndUnits(textHorizontal, comboHorizontalUnits);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		setColor(colorState);
		setHorizontal("0px");
		setVertical("0px");
		setBlur("0px");
		setSpread("0px");
		setInset(false);

		if (initialString != null) {
		
			Scanner scanner = new Scanner(initialString.trim());
			
			try {
				// Set values.
				setHorizontal(scanner.next().trim());
				setVertical(scanner.next().trim());
				setBlur(scanner.next().trim());
				setSpread(scanner.next().trim());
				setColor(scanner.next().trim());

				String inset = scanner.nextLine().trim();
				if (inset.equals("inset")) {
					setInset(true);
				}
			}
			catch (Exception e) {
			}
			
		    scanner.close();
		}
	}

	/**
	 * Set horizontal position.
	 * @param string
	 */
	private void setHorizontal(String string) {
		
		Utility.setCssValueAndUnits(string, textHorizontal, comboHorizontalUnits);
	}

	/**
	 * Set horizontal position.
	 * @param string
	 */
	private void setVertical(String string) {
		
		Utility.setCssValueAndUnits(string, textVertical, comboVerticalUnits);
	}

	/**
	 * Set blur.
	 * @param string
	 */
	private void setBlur(String string) {
		
		Utility.setCssValueAndUnits(string, textBlur, comboBlurUnits);
	}

	/**
	 * Set spread.
	 * @param string
	 */
	private void setSpread(String string) {
		
		Utility.setCssValueAndUnits(string, textSpread, comboSpreadUnits);
	}

	/**
	 * Set color.
	 * @param string
	 */
	private void setColor(String string) {
		
		setColor(Utility.getColorFromCss(string));
	}

	/**
	 * Set inset shadow.
	 * @param set
	 */
	private void setInset(boolean set) {
		
		checkInset.setSelected(set);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelHorizontal);
		Utility.localize(labelVertical);
		Utility.localize(labelBlur);
		Utility.localize(labelSpread);
		Utility.localize(labelColor);
		Utility.localize(checkInset);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssShadowBuilder");
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
		
		CssBoxShadowPanel.bounds = bounds;
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
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssBoxShadow;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
