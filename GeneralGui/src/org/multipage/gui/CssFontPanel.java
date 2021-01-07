/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.multipage.util.*;

import java.io.*;
import java.util.*;
import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CssFontPanel extends InsertPanel implements StringValueEditor {

	// $hide>>$
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized dialog states.
	 */
	private static Rectangle bounds;
	private static boolean boundsSet;

	/**
	 * Panel states.
	 */
	private static String fontFamily;
	private static String fontStyle;
	private static String fontVariant;
	private static String fontWeight;
	private static String fontSize;

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 590, 365);
		boundsSet = false;
		
		fontFamily = "Arial, sans-serif";
		fontStyle = "normal";
		fontVariant = "normal";
		fontWeight = "normal";
		fontSize = "medium";
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
		
		fontFamily = inputStream.readUTF();
		fontStyle = inputStream.readUTF();
		fontVariant = inputStream.readUTF();
		fontWeight = inputStream.readUTF();
		fontSize = inputStream.readUTF();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		
		outputStream.writeUTF(fontFamily == null ? "" : fontFamily);
		outputStream.writeUTF(fontStyle == null ? "" : fontStyle);
		outputStream.writeUTF(fontVariant == null ? "" : fontVariant);
		outputStream.writeUTF(fontWeight == null ? "" : fontWeight);
		outputStream.writeUTF(fontSize == null ? "" : fontSize);
	}
	
	/**
	 * Initial string.
	 */
	private String initialString;

	/**
	 * List model.
	 */
	private DefaultListModel<String> listFontFamiliesModel;
	
	/**
	 * Components change lock. 
	 */
	private boolean componentsChangeLock = false;
	
	/**
	 * Returns true value if a lock exists.
	 */
	private boolean isChangeLock() {
		
		return componentsChangeLock;
	}
	
	/**
	 * Lock changes.
	 */
	private boolean lockChanges() {
		
		if (componentsChangeLock) {
			return false;
		}
		componentsChangeLock = true;
		return true;
	}
	
	/**
	 * Unlock changes.
	 */
	private void unlockChanges() {
		
		componentsChangeLock = false;
	}
 
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelFontFamily;
	private JScrollPane scrollPaneFontFamily;
	private JList listFontFamilies;
	private JLabel labelFontStyle;
	private JRadioButton radioStyleNormal;
	private JRadioButton radioStyleItalic;
	private JRadioButton radioStyleOblique;
	private final ButtonGroup buttonGroupStyle = new ButtonGroup();
	private JLabel labelVariant;
	private JRadioButton radioVariantNormal;
	private final ButtonGroup buttonGroupVariant = new ButtonGroup();
	private JRadioButton radioVariantSmallCaps;
	private JLabel labelSize;
	private JComboBox comboBoxSize;
	private JTextField textFontSize;
	private JComboBox comboBoxSizeUnit;
	private JLabel labelFontWeight;
	private JComboBox comboBoxWeight;
	private JToolBar toolBar;
	private JLabel labelSample;
	private JLabel labelLineHeight;
	private JComboBox comboLineHeight;
	private TextFieldEx textLineHeight;
	private JComboBox comboLineHeightUnits;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssFontPanel(String initialString) {

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
		
		labelFontFamily = new JLabel("org.multipage.gui.textFontFamily");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFontFamily, 10, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFontFamily, 10, SpringLayout.WEST, this);
		add(labelFontFamily);
		
		scrollPaneFontFamily = new JScrollPane();
		scrollPaneFontFamily.setPreferredSize(new Dimension(2, 212));
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPaneFontFamily, 3, SpringLayout.SOUTH, labelFontFamily);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPaneFontFamily, 10, SpringLayout.WEST, this);
		add(scrollPaneFontFamily);
		
		listFontFamilies = new JList();
		scrollPaneFontFamily.setViewportView(listFontFamilies);
		
		labelFontStyle = new JLabel("org.multipage.gui.textFontStyle");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFontStyle, 0, SpringLayout.NORTH, labelFontFamily);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFontStyle, 280, SpringLayout.WEST, this);
		add(labelFontStyle);
		
		radioStyleNormal = new JRadioButton("org.multipage.gui.textFontNormal");
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPaneFontFamily, -20, SpringLayout.WEST, radioStyleNormal);

		sl_panelMain.putConstraint(SpringLayout.NORTH, radioStyleNormal, 6, SpringLayout.SOUTH, labelFontStyle);
		buttonGroupStyle.add(radioStyleNormal);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioStyleNormal, 0, SpringLayout.WEST, labelFontStyle);
		add(radioStyleNormal);
		
		radioStyleItalic = new JRadioButton("org.multipage.gui.textFontItalic");
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioStyleItalic, 30, SpringLayout.NORTH, this);
		buttonGroupStyle.add(radioStyleItalic);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioStyleItalic, 6, SpringLayout.EAST, radioStyleNormal);
		add(radioStyleItalic);
		
		radioStyleOblique = new JRadioButton("org.multipage.gui.textFontOblique");
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioStyleOblique, 30, SpringLayout.NORTH, this);
		buttonGroupStyle.add(radioStyleOblique);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioStyleOblique, 6, SpringLayout.EAST, radioStyleItalic);
		add(radioStyleOblique);
		
		labelVariant = new JLabel("org.multipage.gui.textFontVariant");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelVariant, 280, SpringLayout.WEST, this);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelVariant, 6, SpringLayout.SOUTH, radioStyleNormal);
		add(labelVariant);
		
		radioVariantNormal = new JRadioButton("org.multipage.gui.textFontNormal");
		buttonGroupVariant.add(radioVariantNormal);
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioVariantNormal, 6, SpringLayout.SOUTH, labelVariant);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioVariantNormal, 0, SpringLayout.WEST, labelFontStyle);
		add(radioVariantNormal);
		
		radioVariantSmallCaps = new JRadioButton("org.multipage.gui.textFontSmallCaps");
		buttonGroupVariant.add(radioVariantSmallCaps);
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioVariantSmallCaps, 0, SpringLayout.NORTH, radioVariantNormal);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioVariantSmallCaps, 6, SpringLayout.EAST, radioVariantNormal);
		add(radioVariantSmallCaps);
		
		labelSize = new JLabel("org.multipage.gui.textFontSize");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSize, 6, SpringLayout.SOUTH, radioVariantNormal);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSize, 0, SpringLayout.WEST, labelFontStyle);
		add(labelSize);
		
		comboBoxSize = new JComboBox();

		comboBoxSize.setPreferredSize(new Dimension(150, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxSize, 6, SpringLayout.SOUTH, labelSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxSize, 0, SpringLayout.WEST, labelFontStyle);
		add(comboBoxSize);
		
		textFontSize = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.WEST, textFontSize, 16, SpringLayout.EAST, comboBoxSize);
		sl_panelMain.putConstraint(SpringLayout.NORTH, textFontSize, 0, SpringLayout.NORTH, comboBoxSize);
		add(textFontSize);
		textFontSize.setColumns(5);
		
		comboBoxSizeUnit = new JComboBox();

		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxSizeUnit, 3, SpringLayout.EAST, textFontSize);
		comboBoxSizeUnit.setPreferredSize(new Dimension(50, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxSizeUnit, 0, SpringLayout.NORTH, comboBoxSize);
		add(comboBoxSizeUnit);
		
		labelFontWeight = new JLabel("org.multipage.gui.textFontWeight");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFontWeight, 0, SpringLayout.WEST, labelFontStyle);
		add(labelFontWeight);
		
		comboBoxWeight = new JComboBox();

		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxWeight, 6, SpringLayout.SOUTH, labelFontWeight);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxWeight, 0, SpringLayout.WEST, labelFontStyle);
		comboBoxWeight.setPreferredSize(new Dimension(150, 20));
		add(comboBoxWeight);
		
		toolBar = new JToolBar();
		sl_panelMain.putConstraint(SpringLayout.NORTH, toolBar, 6, SpringLayout.SOUTH, scrollPaneFontFamily);
		sl_panelMain.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, labelFontFamily);
		toolBar.setPreferredSize(new Dimension(250, 22));
		toolBar.setFloatable(false);
		add(toolBar);
		
		labelSample = new JLabel("");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSample, 10, SpringLayout.SOUTH, toolBar);
		labelSample.setHorizontalAlignment(SwingConstants.CENTER);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSample, 10, SpringLayout.WEST, this);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, labelSample, -10, SpringLayout.SOUTH, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelSample, -10, SpringLayout.EAST, this);
		add(labelSample);
		
		labelLineHeight = new JLabel("org.multipage.gui.textLineHeight");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelLineHeight, 6, SpringLayout.SOUTH, comboBoxSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelLineHeight, 0, SpringLayout.WEST, labelFontStyle);
		add(labelLineHeight);
		
		comboLineHeight = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboLineHeight, 6, SpringLayout.SOUTH, labelLineHeight);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboLineHeight, 0, SpringLayout.WEST, labelFontStyle);
		comboLineHeight.setPreferredSize(new Dimension(150, 20));
		add(comboLineHeight);
		
		textLineHeight = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textLineHeight, 0, SpringLayout.NORTH, comboLineHeight);
		sl_panelMain.putConstraint(SpringLayout.WEST, textLineHeight, 0, SpringLayout.WEST, textFontSize);
		textLineHeight.setColumns(5);
		add(textLineHeight);
		
		comboLineHeightUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFontWeight, 6, SpringLayout.SOUTH, comboLineHeightUnits);
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboLineHeightUnits, 0, SpringLayout.NORTH, comboLineHeight);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboLineHeightUnits, 0, SpringLayout.WEST, comboBoxSizeUnit);
		comboLineHeightUnits.setPreferredSize(new Dimension(50, 20));
		add(comboLineHeightUnits);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		loadFromString(initialString);
	}

	/**
	 * Update sample.
	 */
	protected void updateSample() {
		
		Object sampleText = Resources.getString("org.multipage.gui.textThisIsTextSample");
		
		// Get selected font family.
		String fontFamily = (String) listFontFamilies.getSelectedValue();
		if (fontFamily == null) {
			fontFamily = "serif";
		}
		
		String text = String.format("<html><span style=\"" +
				"font-family: %s; " +
				"font-style: %s; " +
				"font-size: %s; " +
				"font-weight: %s; " +
				"font-variant: %s; " +
				"\">%s</span></html>",
				fontFamily,
				getFontStyle(),
				getFontSize(),
				getFontWeight(),
				getFontVariant(),
				sampleText);
		
		labelSample.setText(text);
	}

	/**
	 * Load from initial string.
	 */
	private void loadFromString(String string) {
		
		initialString = string;
		
		if (initialString != null) {
			setFromInitialString();
		}
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
		fontSize = getFontSize();
		fontWeight = getFontWeight();
		fontStyle = getFontStyle();
		fontVariant = getFontVariant();
		fontFamily = getFontFamilies();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		
		radioStyleNormal.setSelected(true);
		radioVariantNormal.setSelected(true);
		
		loadFontFamilies();
		loadSizes();
		loadWeights();
		loadLineHeights();
		
		initToolBars();

		loadDialog();
		updateSample();

		// Set listeners.
		setListeners();
	}

	/**
	 * Initialize tool bars.
	 */
	private void initToolBars() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/insert.png", this, "onAddFontName", "org.multipage.gui.tooltipAddFontName");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", this, "onRemoveFontName", "org.multipage.gui.tooltipRemoveFontName");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_backward.png", this, "onMoveUp", "org.multipage.gui.tooltipMoveFontNameUp");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_forward.png", this, "onMoveDown", "org.multipage.gui.tooltipMoveFontNameDown");
	}
	
	/**
	 * Move font name up.
	 */
	public void onMoveUp() {
		
		int index = listFontFamilies.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleFontFace");
			return;
		}
		
		// Check selection, move font up and select it.
		if (index <= 0) {
			return;
		}
		
		String currentName = listFontFamiliesModel.get(index);
		String previousName = listFontFamiliesModel.get(index - 1);
		listFontFamiliesModel.set(index, previousName);
		listFontFamiliesModel.set(index - 1, currentName);
		
		listFontFamilies.setSelectedIndex(index - 1);
		listFontFamilies.ensureIndexIsVisible(index - 1);
	}
	
	/**
	 * Move font name down.
	 */
	public void onMoveDown() {
		
		int index = listFontFamilies.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleFontFace");
			return;
		}
		
		int count = listFontFamiliesModel.getSize();
		
		// Check selection, move font down and select it.
		if (index >= count - 1) {
			return;
		}
		
		String currentName = listFontFamiliesModel.get(index);
		String nextName = listFontFamiliesModel.get(index + 1);
		listFontFamiliesModel.set(index, nextName);
		listFontFamiliesModel.set(index + 1, currentName);
		
		listFontFamilies.setSelectedIndex(index + 1);
		listFontFamilies.ensureIndexIsVisible(index + 1);
	}
	
	/**
	 * Add font name.
	 */
	public void onAddFontName() {
		
		String fontName = SelectFontNameDialog.showDialog(this);
		if (fontName == null) {
			return;
		}
		
		// Add font name to the list.
		int selectedIndex = listFontFamilies.getSelectedIndex();
		if (selectedIndex == -1) {
			
			listFontFamiliesModel.addElement(fontName);
			selectedIndex = listFontFamiliesModel.getSize() - 1;
		}
		else {
			selectedIndex++;
			listFontFamiliesModel.add(selectedIndex, fontName);
		}
		
		listFontFamilies.ensureIndexIsVisible(selectedIndex);
		listFontFamilies.setSelectedIndex(selectedIndex);
	}

	/**
	 * Remove font name.
	 */
	public void onRemoveFontName() {
		
		int index = listFontFamilies.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleFontFace");
			return;
		}
		
		// Ask user and delete.
		if (Utility.ask(this, "org.multipage.gui.messageRemoveSelectedFontFaceName",
				listFontFamilies.getSelectedValue())) {
			
			listFontFamiliesModel.remove(index);
			
			listFontFamilies.ensureIndexIsVisible(index - 1);
			listFontFamilies.setSelectedIndex(index - 1);
		}
	}

	/**
	 * Listeners.
	 */
	private void setListeners() {
		
		listFontFamilies.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				updateSample();
			}
		});
		
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSample();
			}
		};
		
		radioStyleNormal.addActionListener(listener);
		radioStyleItalic.addActionListener(listener);
		radioStyleOblique.addActionListener(listener);
		
		listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSample();
			}
		};
		
		radioVariantNormal.addActionListener(listener);
		radioVariantSmallCaps.addActionListener(listener);
		
		comboBoxSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (isChangeLock()) {
					return;
				}
				lockChanges();
				
				textFontSize.setText("");
				updateSample();
				
				unlockChanges();
			}
		});
		
		Utility.setTextChangeListener(textFontSize, new Runnable() {
			@Override
			public void run() {
				
				if (isChangeLock()) {
					return;
				}
				lockChanges();
				
				comboBoxSize.setSelectedIndex(0);
				updateSample();
				
				unlockChanges();
			}
		});
		
		comboLineHeight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (isChangeLock()) {
					return;
				}
				lockChanges();
				
				textLineHeight.setText("");
				
				unlockChanges();
			}
		});
		
		Utility.setTextChangeListener(textLineHeight, new Runnable() {
			@Override
			public void run() {
				
				if (isChangeLock()) {
					return;
				}
				lockChanges();
				
				comboLineHeight.setSelectedIndex(0);
				
				unlockChanges();
			}
		});
		
		comboBoxSizeUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSample();
			}
		});
		
		comboBoxWeight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSample();
			}
		});
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String specification =    getFontStyle() + " "
								+ getFontVariant() + " "
								+ getFontWeight() + " "
								+ getFontSize() + "/"
								+ getLineHeight() + " "
								+ getFontFamilies();
		
		return specification;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		initialString.trim();

		if (initialString != null) {
			try {
				
				// Read font style.
				Obj<Integer> position = new Obj<Integer>(0);
				int length = initialString.length();
				
				while (position.ref < length) {
					
					// Get font style.
					String word = Utility.getNextMatch(initialString, position, "\\w+");
					if (word == null) {
						break;
					}
					setFontStyle(word);
					
					// Get font variant.
					word = Utility.getNextMatch(initialString, position, "[\\w-]+");
					if (word == null) {
						break;
					}
					setFontVariant(word);
					
					// Get font weight.
					word = Utility.getNextMatch(initialString, position, "[\\wd]+");
					if (word == null) {
						break;
					}
					setFontWeight(word);
					
					// Get size/line height.
					word = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%-/]+");
					if (word == null) {
						break;
					}
					String [] words = word.split("/");
					if (words.length != 2) {
						break;
					}
					setFontSize(words[0]);
					setLineHeight(words[1]);
					
					// Get font families.
					if (position.ref >= length) {
						break;
					}
					String text = initialString.substring(position.ref);
					if (text.isEmpty()) {
						break;
					}
					setFontFamilies(text);
				}
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Get font size.
	 * @return
	 */
	private String getFontSize() {
		
		String numberText = textFontSize.getText();
		
		// Try to get number.
		Double number = null;
		try {
			number = Double.parseDouble(numberText);
		}
		catch (Exception e) {
		}
		
		if (number != null) {
			return number.toString() + comboBoxSizeUnit.getSelectedItem();
		}
		
		Object object = comboBoxSize.getSelectedItem();
		if (object instanceof NamedItem) {
			
			String textValue = ((NamedItem) object).value;
			if (!textValue.isEmpty()) {
				return textValue;
			}
		}
		
		return "medium";
	}

	/**
	 * Get line height.
	 * @return
	 */
	private String getLineHeight() {
		
		String numberText = textLineHeight.getText();
		
		// Try to get number.
		Double number = null;
		try {
			number = Double.parseDouble(numberText);
		}
		catch (Exception e) {
		}
		
		if (number != null) {
			return number.toString() + comboLineHeightUnits.getSelectedItem();
		}
		
		Object object = comboLineHeight.getSelectedItem();
		if (object instanceof NamedItem) {
			
			String textValue = ((NamedItem) object).value;
			if (!textValue.isEmpty()) {
				return textValue;
			}
		}
		
		return "normal";
	}

	/**
	 * Set font size.
	 * @param size
	 */
	private void setFontSize(String size) {
		
		for (int index = 0; index < comboBoxSize.getItemCount(); index++) {
			NamedItem item = (NamedItem) comboBoxSize.getItemAt(index);
			
			if (item.value.equals(size)) {
				
				comboBoxSize.setSelectedIndex(index);
				return;
			}
		}
		
		// Convert text to number and units.
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(size, number, unit);
		
		// Output number.
		textFontSize.setText(number.ref);
		
		// Set units combo.
		Utility.selectComboItem(comboBoxSizeUnit, unit.ref);
	}

	/**
	 * Set line height.
	 * @param height
	 */
	private void setLineHeight(String height) {
		
		for (int index = 0; index < comboLineHeight.getItemCount(); index++) {
			NamedItem item = (NamedItem) comboLineHeight.getItemAt(index);
			
			if (item.value.equals(height)) {
				
				comboLineHeight.setSelectedIndex(index);
				return;
			}
		}
		
		// Convert text to number and units.
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		Utility.convertCssStringToNumberUnit(height, number, unit);
		
		// Output number.
		textLineHeight.setText(number.ref);
		
		// Set units combo.
		Utility.selectComboItem(comboLineHeightUnits, unit.ref);
	}

	/**
	 * Get font weight.
	 * @return
	 */
	private String getFontWeight() {
		
		Object object = comboBoxWeight.getSelectedItem();
		
		if (object instanceof NamedItem) {
			return ((NamedItem) object).value;
		}
		if (object instanceof String) {
			return (String) object;
		}
		
		return "";
	}
	
	/**
	 * Set font weight.
	 * @param weight
	 */
	private void setFontWeight(String weight) {
		
		for (int index = 0; index < comboBoxWeight.getItemCount(); index++) {
			
			Object object = comboBoxWeight.getItemAt(index);
			if (object instanceof NamedItem) {
				
				if (((NamedItem) object).value.equals(weight)) {
					
					comboBoxWeight.setSelectedIndex(index);
					return;
				}
			}
			else if (object instanceof String) {
				
				if (object.equals(weight)) {
					
					comboBoxWeight.setSelectedIndex(index);
					return;
				}
			}
		}
	}

	/**
	 * Get font style.
	 * @return
	 */
	private String getFontStyle() {
		
		if (radioStyleNormal.isSelected()) {
			return "normal";
		}
		if (radioStyleItalic.isSelected()) {
			return "italic";
		}
		if (radioStyleOblique.isSelected()) {
			return "oblique";
		}
		return "";
	}
	
	/**
	 * Set font style.
	 * @param style
	 */
	private void setFontStyle(String style) {
		
		if (style.equals("normal")) {
			radioStyleNormal.setSelected(true);
		}
		else if (style.equals("italic")) {
			radioStyleItalic.setSelected(true);
		}
		else if (style.equals("oblique")) {
			radioStyleOblique.setSelected(true);
		}
	}

	/**
	 * Get font variant.
	 * @return
	 */
	private String getFontVariant() {
		
		if (radioVariantNormal.isSelected()) {
			return "normal";
		}
		if (radioVariantSmallCaps.isSelected()) {
			return "small-caps";
		}
		return "";
	}
	
	/**
	 * Set font variant.
	 * @param variant
	 */
	private void setFontVariant(String variant) {
		
		if (variant.equals("normal")) {
			radioVariantNormal.setSelected(true);
		}
		else if (variant.equals("small-caps")) {
			radioVariantSmallCaps.setSelected(true);
		}
	}

	/**
	 * Get font families.
	 * @return
	 */
	private String getFontFamilies() {
		
		Enumeration<String> fontNames = listFontFamiliesModel.elements();
		String outputText = "";
		
		boolean isFirst = true;
		
		while (fontNames.hasMoreElements()) {
			
			String fontName = fontNames.nextElement();
			if (!isFirst) {
				outputText += ',';
			}
			
			// If a family name contains whitespace, quote it.
			if (fontName.contains(" ")) {
				fontName = '\"' + fontName + '\"';
			}
			
			outputText += fontName;
			
			isFirst = false;
		}
		
		if (outputText.isEmpty()) {
			return "serif";
		}
		return outputText;
	}
	
	/**
	 * Set font families.
	 * @param families
	 */
	private void setFontFamilies(String families) {

		families = families.trim();
		listFontFamiliesModel.clear();
		
		Obj<Integer> position = new Obj<Integer>(0);
		int length = families.length();
		
		while (position.ref < length) {
			
			String text = Utility.getNextMatch(families, position, "\\G\\s*\"");
			boolean isEndQuote = false;
			if (text != null) {
				text = Utility.getNextMatch(families, position, "[^\"]*");
				isEndQuote = true;
			}
			else {
				text = Utility.getNextMatch(families, position, "[^,]*");
			}
			
			if (text == null) {
				break;
			}
			text = text.trim();
			if (text.isEmpty()) {
				break;
			}
			
			String fontName = text;
			// Add font name to the list.
			listFontFamiliesModel.addElement(fontName);
			
			if (isEndQuote) {
				text = Utility.getNextMatch(families, position, "\"");
				if (text == null) {
					break;
				}
			}
			
			text = Utility.getNextMatch(families, position, ",");
			if (text == null) {
				break;
			}
		}
	}

	/**
	 * Load font weights.
	 */
	private void loadWeights() {

		final NamedItem [] weights = {
						new NamedItem("org.multipage.gui.textNormalWeight", "normal"),
						new NamedItem("org.multipage.gui.textBoldWeight", "bold"),
						new NamedItem("org.multipage.gui.textBolderWeight", "bolder"),
						new NamedItem("org.multipage.gui.textLighterWeight", "lighter") };
		
		for (NamedItem weight : weights) {
			comboBoxWeight.addItem(weight);
		}
		
		for (int weight = 100; weight <= 900; weight += 100) {
			
			comboBoxWeight.addItem(String.valueOf(weight));
		}
	}

	/**
	 * Load line heights.
	 */
	private void loadLineHeights() {
		
		final NamedItem [] weights = {
				new NamedItem("org.multipage.gui.textNormalLineHeight", "normal")
				};
		
		Utility.loadEmptyItem(comboLineHeight);
		
		for (NamedItem weight : weights) {
			comboLineHeight.addItem(weight);
		}
		
		comboLineHeight.setSelectedIndex(1);
		
		Utility.loadCssUnits(comboLineHeightUnits);
	}

	/**
	 * Load sizes.
	 */
	private void loadSizes() {
		
		final NamedItem [] sizes = {
			new NamedItem("org.multipage.gui.textFontSizeMedium", "medium"),
			new NamedItem("org.multipage.gui.textFontSizeXxSmall", "xx-small"),
			new NamedItem("org.multipage.gui.textFontSizeXSmall", "x-small"),
			new NamedItem("org.multipage.gui.textFontSizeSmall", "small"),
			new NamedItem("org.multipage.gui.textFontSizeLarge", "large"),
			new NamedItem("org.multipage.gui.textFontSizeXLarge", "x-large"),
			new NamedItem("org.multipage.gui.textFontSizeXxLarge", "xx-large"),
			new NamedItem("org.multipage.gui.textFontSizeSmaller", "smaller"),
			new NamedItem("org.multipage.gui.textFontSizeLarger", "larger") };
		
		DefaultComboBoxModel<NamedItem> model = new DefaultComboBoxModel<NamedItem>();
		comboBoxSize.setModel(model);
		
		Utility.loadEmptyItem(comboBoxSize);
		
		for (NamedItem size : sizes) {
			model.addElement(size);
		}
		
		comboBoxSize.setSelectedIndex(1);
		
		Utility.loadCssUnits(comboBoxSizeUnit);
	}
	
	/**
	 * Load font families.
	 */
	private void loadFontFamilies() {
		
		listFontFamiliesModel = new DefaultListModel<String>(); 
		listFontFamilies.setModel(listFontFamiliesModel);
		
		// Set cell renderer.
		listFontFamilies.setCellRenderer(new ListCellRenderer<String>() {
			
			RendererJLabel renderer = new RendererJLabel();
			
			@Override
			public Component getListCellRendererComponent(
					JList<? extends String> list,
			        String value,
			        int index,
			        boolean isSelected,
			        boolean cellHasFocus) {
				
				renderer.setText("<html><span style='font-family:" + value + ";font-size:12px'>" + value + "</span></html>");
				renderer.set(isSelected, cellHasFocus, index);
				return renderer;
			}
		});
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelFontFamily);
		Utility.localize(labelFontStyle);
		Utility.localize(radioStyleNormal);
		Utility.localize(radioStyleItalic);
		Utility.localize(radioStyleOblique);
		Utility.localize(labelVariant);
		Utility.localize(radioVariantNormal);
		Utility.localize(radioVariantSmallCaps);
		Utility.localize(labelSize);
		Utility.localize(labelFontWeight);
		Utility.localize(labelLineHeight);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textFontBuilder");
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
		
		CssFontPanel.bounds = bounds;
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
	 * Get text value.
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
		
		return meansCssFont;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {

		return false;
	}
}
