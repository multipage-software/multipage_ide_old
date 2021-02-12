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

/**
 * 
 * @author
 *
 */
public class CssClipPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JPanel panel;
	private JLabel labelTop;
	private TextFieldEx textTop;
	private JComboBox comboTopUnits;
	private TextFieldEx textBottom;
	private JLabel labelBottom;
	private JComboBox comboBottomUnits;
	private JLabel labelRight;
	private TextFieldEx textRight;
	private JComboBox comboRightUnits;
	private TextFieldEx textLeft;
	private JLabel labelLeft;
	private JComboBox comboLeftUnits;

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
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 469, 218);
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

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssClipPanel(String initialString) {

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
		panel.setPreferredSize(new Dimension(420, 140));
		springLayout.putConstraint(SpringLayout.NORTH, panel, 30, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, panel, 24, SpringLayout.WEST, this);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		labelTop = new JLabel("org.multipage.gui.textCssClipTop");
		sl_panel.putConstraint(SpringLayout.NORTH, labelTop, 30, SpringLayout.NORTH, panel);
		labelTop.setHorizontalAlignment(SwingConstants.TRAILING);
		labelTop.setPreferredSize(new Dimension(80, 14));
		sl_panel.putConstraint(SpringLayout.WEST, labelTop, 10, SpringLayout.WEST, panel);
		panel.add(labelTop);
		
		textTop = new TextFieldEx();
		sl_panel.putConstraint(SpringLayout.NORTH, textTop, 0, SpringLayout.NORTH, labelTop);
		sl_panel.putConstraint(SpringLayout.WEST, textTop, 6, SpringLayout.EAST, labelTop);
		textTop.setColumns(5);
		panel.add(textTop);
		
		comboTopUnits = new JComboBox();
		sl_panel.putConstraint(SpringLayout.NORTH, comboTopUnits, 0, SpringLayout.NORTH, labelTop);
		sl_panel.putConstraint(SpringLayout.WEST, comboTopUnits, 0, SpringLayout.EAST, textTop);
		comboTopUnits.setPreferredSize(new Dimension(50, 20));
		panel.add(comboTopUnits);
		
		textBottom = new TextFieldEx();
		sl_panel.putConstraint(SpringLayout.NORTH, textBottom, 31, SpringLayout.SOUTH, textTop);
		sl_panel.putConstraint(SpringLayout.WEST, textBottom, 0, SpringLayout.WEST, textTop);
		textBottom.setColumns(5);
		panel.add(textBottom);
		
		labelBottom = new JLabel("org.multipage.gui.textCssClipBottom");
		labelBottom.setHorizontalAlignment(SwingConstants.TRAILING);
		labelBottom.setPreferredSize(new Dimension(80, 14));
		sl_panel.putConstraint(SpringLayout.NORTH, labelBottom, 0, SpringLayout.NORTH, textBottom);
		sl_panel.putConstraint(SpringLayout.EAST, labelBottom, 0, SpringLayout.EAST, labelTop);
		panel.add(labelBottom);
		
		comboBottomUnits = new JComboBox();
		sl_panel.putConstraint(SpringLayout.NORTH, comboBottomUnits, 0, SpringLayout.NORTH, textBottom);
		sl_panel.putConstraint(SpringLayout.WEST, comboBottomUnits, 0, SpringLayout.WEST, comboTopUnits);
		comboBottomUnits.setPreferredSize(new Dimension(50, 20));
		panel.add(comboBottomUnits);
		
		labelRight = new JLabel("org.multipage.gui.textCssClipRight");
		sl_panel.putConstraint(SpringLayout.WEST, labelRight, 20, SpringLayout.EAST, comboTopUnits);
		labelRight.setHorizontalAlignment(SwingConstants.TRAILING);
		labelRight.setPreferredSize(new Dimension(80, 14));
		sl_panel.putConstraint(SpringLayout.NORTH, labelRight, 0, SpringLayout.NORTH, labelTop);
		panel.add(labelRight);
		
		textRight = new TextFieldEx();
		sl_panel.putConstraint(SpringLayout.NORTH, textRight, 0, SpringLayout.NORTH, labelTop);
		sl_panel.putConstraint(SpringLayout.WEST, textRight, 6, SpringLayout.EAST, labelRight);
		textRight.setColumns(5);
		panel.add(textRight);
		
		comboRightUnits = new JComboBox();
		sl_panel.putConstraint(SpringLayout.NORTH, comboRightUnits, 0, SpringLayout.NORTH, labelTop);
		sl_panel.putConstraint(SpringLayout.WEST, comboRightUnits, 0, SpringLayout.EAST, textRight);
		comboRightUnits.setPreferredSize(new Dimension(50, 20));
		panel.add(comboRightUnits);
		
		textLeft = new TextFieldEx();
		sl_panel.putConstraint(SpringLayout.NORTH, textLeft, 0, SpringLayout.NORTH, textBottom);
		sl_panel.putConstraint(SpringLayout.WEST, textLeft, 0, SpringLayout.WEST, textRight);
		textLeft.setColumns(5);
		panel.add(textLeft);
		
		labelLeft = new JLabel("org.multipage.gui.textCssClipLeft");
		labelLeft.setHorizontalAlignment(SwingConstants.TRAILING);
		labelLeft.setPreferredSize(new Dimension(80, 14));
		sl_panel.putConstraint(SpringLayout.NORTH, labelLeft, 0, SpringLayout.NORTH, textBottom);
		sl_panel.putConstraint(SpringLayout.EAST, labelLeft, 0, SpringLayout.EAST, labelRight);
		panel.add(labelLeft);
		
		comboLeftUnits = new JComboBox();
		sl_panel.putConstraint(SpringLayout.NORTH, comboLeftUnits, 0, SpringLayout.NORTH, textBottom);
		sl_panel.putConstraint(SpringLayout.WEST, comboLeftUnits, 0, SpringLayout.WEST, comboRightUnits);
		comboLeftUnits.setPreferredSize(new Dimension(50, 20));
		panel.add(comboLeftUnits);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		setFromInitialString();
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
		
		Utility.loadCssUnits(comboBottomUnits);
		Utility.loadCssUnits(comboLeftUnits);
		Utility.loadCssUnits(comboTopUnits);
		Utility.loadCssUnits(comboRightUnits);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return "rect(" + getTop() + ", " + getRight() + ", " + getBottom() + ", " + getLeft() + ")";
	}

	/**
	 * Get top.
	 * @return
	 */
	private String getTop() {
		
		return Utility.getCssValueAndUnits(textTop, comboTopUnits);
	}

	/**
	 * Get right.
	 * @return
	 */
	private String getRight() {
		
		return Utility.getCssValueAndUnits(textRight, comboRightUnits);
	}

	/**
	 * Get bottom.
	 * @return
	 */
	private String getBottom() {
		
		return Utility.getCssValueAndUnits(textBottom, comboBottomUnits);
	}

	/**
	 * Get left.
	 * @return
	 */
	private String getLeft() {
		
		return Utility.getCssValueAndUnits(textLeft, comboLeftUnits);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		setTop("0px");
		setRight("0px");
		setBottom("0px");
		setLeft("0px");

		if (initialString != null) {
		
			String trimmedString = initialString.replaceAll("rect\\s*\\(", "");
			trimmedString = trimmedString.replaceAll(",", "");
			trimmedString = trimmedString.replaceAll("\\)", "");
			
			Scanner scanner = new Scanner(trimmedString.trim());
			
			try {
				// Set values.
				setTop(scanner.next().trim());
				setRight(scanner.next().trim());
				setBottom(scanner.next().trim());
				setLeft(scanner.next().trim());
			}
			catch (Exception e) {
			}
			
		    scanner.close();
		}
	}

	/**
	 * Set top.
	 * @param string
	 */
	private void setTop(String string) {
		
		Utility.setCssValueAndUnits(string, textTop, comboTopUnits);
	}

	/**
	 * Set right.
	 * @param string
	 */
	private void setRight(String string) {
		
		Utility.setCssValueAndUnits(string, textRight, comboRightUnits);
	}

	/**
	 * Set bottom.
	 * @param string
	 */
	private void setBottom(String string) {
		
		Utility.setCssValueAndUnits(string, textBottom, comboBottomUnits);
	}

	/**
	 * Set left.
	 * @param string
	 */
	private void setLeft(String string) {
		
		Utility.setCssValueAndUnits(string, textLeft, comboLeftUnits);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelBottom);
		Utility.localize(labelLeft);
		Utility.localize(labelTop);
		Utility.localize(labelRight);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssClipBuilder");
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
		
		CssClipPanel.bounds = bounds;
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
	 * @return
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 * @return
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 * @param string
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 * @return
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssClip;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
