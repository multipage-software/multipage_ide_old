/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * 
 * @author
 *
 */
public class CssSpacingPanel extends InsertPanel implements StringValueEditor {

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
	 * Components.
	 */
	private JLabel labelHorizontal;
	private TextFieldEx textHorizontal;
	private JLabel labelVertical;
	private TextFieldEx textVertical;
	private JComboBox comboHorizontalUnits;
	private JComboBox comboVerticalUnits;
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssSpacingPanel(String initialString) {

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
		
		labelHorizontal = new JLabel("org.multipage.gui.textSpacingHorizontal");
		springLayout.putConstraint(SpringLayout.NORTH, labelHorizontal, 50, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelHorizontal, 36, SpringLayout.WEST, this);
		add(labelHorizontal);
		
		textHorizontal = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textHorizontal, 0, SpringLayout.NORTH, labelHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, textHorizontal, 6, SpringLayout.EAST, labelHorizontal);
		textHorizontal.setColumns(5);
		add(textHorizontal);
		
		labelVertical = new JLabel("org.multipage.gui.textSpacingVertical");
		springLayout.putConstraint(SpringLayout.NORTH, labelVertical, 0, SpringLayout.NORTH, labelHorizontal);
		add(labelVertical);
		
		textVertical = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textVertical, 0, SpringLayout.NORTH, labelHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, textVertical, 6, SpringLayout.EAST, labelVertical);
		textVertical.setColumns(5);
		add(textVertical);
		
		comboHorizontalUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.WEST, labelVertical, 30, SpringLayout.EAST, comboHorizontalUnits);
		springLayout.putConstraint(SpringLayout.NORTH, comboHorizontalUnits, 0, SpringLayout.NORTH, labelHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, comboHorizontalUnits, 0, SpringLayout.EAST, textHorizontal);
		comboHorizontalUnits.setPreferredSize(new Dimension(50, 20));
		add(comboHorizontalUnits);
		
		comboVerticalUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboVerticalUnits, 0, SpringLayout.NORTH, labelHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, comboVerticalUnits, 0, SpringLayout.EAST, textVertical);
		comboVerticalUnits.setPreferredSize(new Dimension(50, 20));
		add(comboVerticalUnits);
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
		
		Utility.loadCssUnits(comboHorizontalUnits);
		Utility.loadCssUnits(comboVerticalUnits);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return getHorizontal() + " " + getVertical();
	}

	/**
	 * Get horizontal.
	 * @return
	 */
	private String getHorizontal() {
		
		return Utility.getCssValueAndUnits(textHorizontal, comboHorizontalUnits);
	}

	/**
	 * Get vertical.
	 * @return
	 */
	private String getVertical() {
		
		return Utility.getCssValueAndUnits(textVertical, comboVerticalUnits);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		setHorizontal("0");
		setVertical("0");

		if (initialString != null) {
			
			Scanner scanner = new Scanner(initialString.trim());
			
			try {
				// Set values.
				setHorizontal(scanner.next().trim());
				setVertical(scanner.next().trim());
			}
			catch (Exception e) {
			}
			
		    scanner.close();
		}
	}

	/**
	 * Set horizontal.
	 * @param string
	 */
	private void setHorizontal(String string) {
		
		Utility.setCssValueAndUnits(string, textHorizontal, comboHorizontalUnits);
	}

	/**
	 * Set vertical.
	 * @param string
	 */
	private void setVertical(String string) {
		
		Utility.setCssValueAndUnits(string, textVertical, comboVerticalUnits);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelHorizontal);
		Utility.localize(labelVertical);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssSpacingBuilder");
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
		
		CssSpacingPanel.bounds = bounds;
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
		
		return meansCssSpacing;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
