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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author
 *
 */
public class CssPerspectiveOriginPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelPositionX;
	private JLabel labelPositionY;
	private JComboBox comboPositionX;
	private JComboBox comboPositionY;
	private JTextField textPositionX;
	private JTextField textPositionY;
	private JComboBox comboPositionXUnits;
	private JComboBox comboPositionYUnits;

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
	
	/**
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

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

	// $hide<<$

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssPerspectiveOriginPanel(String initialString) {

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
				
		labelPositionX = new JLabel("org.multipage.gui.textPerspectiveOriginPositionX");
		springLayout.putConstraint(SpringLayout.NORTH, labelPositionX, 50, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelPositionX, 35, SpringLayout.WEST, this);
		add(labelPositionX);
		
		labelPositionY = new JLabel("org.multipage.gui.textPerspectiveOriginPositionY");
		springLayout.putConstraint(SpringLayout.NORTH, labelPositionY, 25, SpringLayout.SOUTH, labelPositionX);
		springLayout.putConstraint(SpringLayout.EAST, labelPositionY, 0, SpringLayout.EAST, labelPositionX);
		add(labelPositionY);
		
		comboPositionX = new JComboBox();
		comboPositionX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPositionXComboChange();
			}
		});
		comboPositionX.setPreferredSize(new Dimension(100, 20));
		springLayout.putConstraint(SpringLayout.NORTH, comboPositionX, 0, SpringLayout.NORTH, labelPositionX);
		springLayout.putConstraint(SpringLayout.WEST, comboPositionX, 6, SpringLayout.EAST, labelPositionX);
		add(comboPositionX);
		
		comboPositionY = new JComboBox();
		comboPositionY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPositionYComboChange();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, comboPositionY, 0, SpringLayout.NORTH, labelPositionY);
		springLayout.putConstraint(SpringLayout.EAST, comboPositionY, 0, SpringLayout.EAST, comboPositionX);
		comboPositionY.setPreferredSize(new Dimension(100, 20));
		add(comboPositionY);
		
		textPositionX = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textPositionX, 0, SpringLayout.NORTH, labelPositionX);
		springLayout.putConstraint(SpringLayout.WEST, textPositionX, 23, SpringLayout.EAST, comboPositionX);
		add(textPositionX);
		textPositionX.setColumns(10);
		
		textPositionY = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textPositionY, 0, SpringLayout.NORTH, labelPositionY);
		springLayout.putConstraint(SpringLayout.WEST, textPositionY, 0, SpringLayout.WEST, textPositionX);
		textPositionY.setColumns(10);
		add(textPositionY);
		
		comboPositionXUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboPositionXUnits, 0, SpringLayout.NORTH, labelPositionX);
		springLayout.putConstraint(SpringLayout.WEST, comboPositionXUnits, 0, SpringLayout.EAST, textPositionX);
		comboPositionXUnits.setPreferredSize(new Dimension(50, 20));
		add(comboPositionXUnits);
		
		comboPositionYUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboPositionYUnits, 0, SpringLayout.NORTH, labelPositionY);
		springLayout.putConstraint(SpringLayout.WEST, comboPositionYUnits, 0, SpringLayout.EAST, textPositionY);
		comboPositionYUnits.setPreferredSize(new Dimension(50, 20));
		add(comboPositionYUnits);
	}

	/**
	 * On position X combo change.
	 */
	protected void onPositionXComboChange() {
		
		if (comboPositionX.getSelectedIndex() == 0) {
			return;
		}
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textPositionX.setText("");
		comboPositionXUnits.setSelectedIndex(0);
		
		stopSettingControls();
	}

	/**
	 * On position Y combo change.
	 */
	protected void onPositionYComboChange() {
		
		if (comboPositionY.getSelectedIndex() == 0) {
			return;
		}
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textPositionY.setText("");
		comboPositionYUnits.setSelectedIndex(0);
		
		stopSettingControls();
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
		loadComboBoxes();
		
		loadDialog();
		
		setListeners();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		Utility.setTextChangeListener(textPositionX, new Runnable() {
			@Override
			public void run() {
				
				if (settingControls) {
					return;
				}
				startSettingControls();
				
				comboPositionX.setSelectedIndex(0);
				
				stopSettingControls();
			}
		});
		
		Utility.setTextChangeListener(textPositionY, new Runnable() {
			@Override
			public void run() {
				
				if (settingControls) {
					return;
				}
				startSettingControls();
				
				comboPositionY.setSelectedIndex(0);
				
				stopSettingControls();
			}
		});
	}

	/**
	 * Load combo boxes.
	 */
	private void loadComboBoxes() {
		
		Utility.loadEmptyItem(comboPositionX);
		Utility.loadNamedItems(comboPositionX, new String [][] {
				{"left", "org.multipage.gui.textPerspectiveOriginLeft"},
				{"center", "org.multipage.gui.textPerspectiveOriginCenter"},
				{"right", "org.multipage.gui.textPerspectiveOriginRight"}
		});
		Utility.selectComboNamedItem(comboPositionX, "center");
		
		Utility.loadEmptyItem(comboPositionY);
		Utility.loadNamedItems(comboPositionY, new String [][] {
				{"top", "org.multipage.gui.textPerspectiveOriginTop"},
				{"center", "org.multipage.gui.textPerspectiveOriginCenter"},
				{"bottom", "org.multipage.gui.textPerspectiveOriginBottom"}
		});
		Utility.selectComboNamedItem(comboPositionY, "center");
	}

	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		Utility.loadCssUnits(comboPositionXUnits);
		Utility.loadCssUnits(comboPositionYUnits);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return getPositionX() + " " + getPositionY();
	}

	/**
	 * Get position X.
	 * @return
	 */
	private String getPositionX() {

		return Utility.getCssValueAndUnits(textPositionX, comboPositionXUnits, comboPositionX, "center");
	}

	/**
	 * Get position Y.
	 * @return
	 */
	private String getPositionY() {
		
		return Utility.getCssValueAndUnits(textPositionY, comboPositionYUnits, comboPositionY, "center");
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		Utility.selectComboNamedItem(comboPositionX, "center");
		textPositionX.setText("");
		comboPositionXUnits.setSelectedIndex(0);
		
		Utility.selectComboNamedItem(comboPositionY, "center");
		textPositionY.setText("");
		comboPositionYUnits.setSelectedIndex(0);

		if (initialString != null) {
			
			Obj<Integer> position = new Obj<Integer>(0);
			
			try {
				
				// Set X position.
				int positionAux = position.ref;
				String text = Utility.getNextMatch(initialString, position, "\\G\\s*(left|center|right)\\s+");
				if (text != null) {
					Utility.selectComboNamedItem(comboPositionX, text.trim());
				}
				else {
					position.ref = positionAux;
					text = Utility.getNextMatch(initialString, position, "\\G\\s*\\S+\\s+");
					if (text == null) {
						return;
					}
					
					Utility.setCssValueAndUnits(text.trim(), textPositionX, comboPositionXUnits, "0", "px");
					comboPositionX.setSelectedIndex(0);
				}
				
				// Set Y position.
				positionAux = position.ref;
				text = Utility.getNextMatch(initialString, position, "\\G\\s*(top|center|bottom)\\s*");
				if (text != null) {
					Utility.selectComboNamedItem(comboPositionY, text.trim());
				}
				else {
					position.ref = positionAux;
					text = Utility.getNextMatch(initialString, position, "\\G\\s*\\S+\\s*");
					if (text == null) {
						return;
					}
					
					Utility.setCssValueAndUnits(text.trim(), textPositionY, comboPositionYUnits, "0", "px");
					comboPositionY.setSelectedIndex(0);
				}
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelPositionX);
		Utility.localize(labelPositionY);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssPerspectiveOriginBuilder");
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
		
		CssPerspectiveOriginPanel.bounds = bounds;
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
		
		return meansCssPerspectiveOrigin;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
