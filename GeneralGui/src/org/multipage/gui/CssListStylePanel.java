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
import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CssListStylePanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelType;
	private JComboBox comboType;
	private JTextField textText;
	private JLabel labelText;
	private JComboBox comboPosition;
	private JLabel labelPosition;
	private TextFieldEx textImageName;
	private JLabel labelImage;
	private JButton buttonGetResources;

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
	 * Get resource name callback.
	 */
	private Callback getResourceName;
	
	/**
	 * Initial string.
	 */
	private String initialString;
	
	/**
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

	// $hide<<$

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssListStylePanel(String initialString) {

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
		
		labelType = new JLabel("org.multipage.gui.textListStyleType");
		springLayout.putConstraint(SpringLayout.NORTH, labelType, 42, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelType, 40, SpringLayout.WEST, this);
		add(labelType);
		
		comboType = new JComboBox();
		comboType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onComboType();
			}
		});
		comboType.setPreferredSize(new Dimension(120, 20));
		springLayout.putConstraint(SpringLayout.NORTH, comboType, 0, SpringLayout.NORTH, labelType);
		springLayout.putConstraint(SpringLayout.WEST, comboType, 6, SpringLayout.EAST, labelType);
		add(comboType);
		
		textText = new TextFieldEx();
		textText.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textText, 0, SpringLayout.NORTH, comboType);
		add(textText);
		textText.setColumns(15);
		
		labelText = new JLabel("org.multipage.gui.textStyleText");
		springLayout.putConstraint(SpringLayout.WEST, labelText, 43, SpringLayout.EAST, comboType);
		springLayout.putConstraint(SpringLayout.WEST, textText, 6, SpringLayout.EAST, labelText);
		springLayout.putConstraint(SpringLayout.NORTH, labelText, 0, SpringLayout.NORTH, labelType);
		add(labelText);
		
		comboPosition = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboPosition, 22, SpringLayout.SOUTH, comboType);
		springLayout.putConstraint(SpringLayout.EAST, comboPosition, 0, SpringLayout.EAST, comboType);
		comboPosition.setPreferredSize(new Dimension(120, 20));
		add(comboPosition);
		
		labelPosition = new JLabel("org.multipage.gui.textListStylePosition");
		springLayout.putConstraint(SpringLayout.NORTH, labelPosition, 0, SpringLayout.NORTH, comboPosition);
		springLayout.putConstraint(SpringLayout.EAST, labelPosition, 0, SpringLayout.EAST, labelType);
		add(labelPosition);
		
		textImageName = new TextFieldEx();
		textImageName.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textImageName, 0, SpringLayout.NORTH, comboPosition);
		springLayout.putConstraint(SpringLayout.WEST, textImageName, 0, SpringLayout.WEST, textText);
		textImageName.setColumns(15);
		add(textImageName);
		
		labelImage = new JLabel("org.multipage.gui.textStyleImage");
		springLayout.putConstraint(SpringLayout.NORTH, labelImage, 0, SpringLayout.NORTH, comboPosition);
		springLayout.putConstraint(SpringLayout.EAST, labelImage, 0, SpringLayout.EAST, labelText);
		add(labelImage);
		
		buttonGetResources = new JButton("");
		buttonGetResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindResource();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonGetResources, 0, SpringLayout.NORTH, comboPosition);
		springLayout.putConstraint(SpringLayout.WEST, buttonGetResources, 0, SpringLayout.EAST, textImageName);
		buttonGetResources.setPreferredSize(new Dimension(22, 22));
		buttonGetResources.setMargin(new Insets(0, 0, 0, 0));
		add(buttonGetResources);
	}
	
	/**
	 * On type selection.
	 */
	protected void onComboType() {
		
		if (settingControls) {
			return;
		}
		startSettingControls();
		
		textText.setText("");
		
		stopSettingControls();
	}

	/**
	 * Set callback.
	 * @param callback
	 */
	public void setResourceNameCallback(Callback callback) {
		
		getResourceName = callback;
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
	 * On find resource.
	 */
	protected void onFindResource() {
		
		if (getResourceName == null) {
			
			Utility.show(this, "org.multipage.gui.messageNoResourcesAssociated");
			return;
		}
		
		// Use callback to obtain resource name.
		Object outputValue = getResourceName.run(null);
		if (!(outputValue instanceof String)) {
			return;
		}
		
		String imageName = (String) outputValue;
		
		// Set image name text control.
		textImageName.setText(imageName);
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
		setIcons();
		
		loadComboBoxes();
		
		loadDialog();
		setListeners();
	}
	
	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		Utility.setTextChangeListener(textText, new Runnable() {
			@Override
			public void run() {
				
				if (settingControls) {
					return;
				}
				startSettingControls();
				
				comboType.setSelectedIndex(0);
				
				stopSettingControls();
			}
		});
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonGetResources.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
	}

	/**
	 * Load combo boxes.
	 */
	private void loadComboBoxes() {
		
		Utility.loadEmptyItem(comboType);
		Utility.loadNamedItems(comboType, new String [][] {
				{"none", "org.multipage.gui.textCssStyleNone"},
				{"disc", "org.multipage.gui.textCssStyleDisk"},
				{"circle", "org.multipage.gui.textCssStyleCirkle"},
				{"square", "org.multipage.gui.textCssStyleSquare"},
				{"decimal", "org.multipage.gui.textCssStyleDecimal"},
				{"lower-roman", "org.multipage.gui.textCssStyleLowerRoman"},
				{"upper-roman", "org.multipage.gui.textCssStyleUpperRoman"},
				{"lower-alpha", "org.multipage.gui.textCssStyleLowerAlpha"},
				{"upper-alpha", "org.multipage.gui.textCssStyleUpperAlpha"},
				{"lower-greek", "org.multipage.gui.textCssStyleLowerGreek"},
				{"armenian", "org.multipage.gui.textCssStyleArmenian"},
				{"georgian", "org.multipage.gui.textCssStyleGeorgian"},
				{"decimal-leading-zero", "org.multipage.gui.textCssStyleDecimalLeadingZero"}
				});
		
		Utility.loadNamedItems(comboPosition, new String [][] {
				{"outside", "org.multipage.gui.textCssStyleOutside"},
				{"inside", "org.multipage.gui.textCssStyleInside"}
				});
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return getType() + " " + getPosition() + " " + getImage();
	}

	/**
	 * Get type.
	 * @return
	 */
	private String getType() {
		
		String type = textText.getText();
		if (!type.isEmpty()) {
			
			type = type.replace("\"", "\\\"");
			return "\"" + type + "\"";
		}
		
		type = Utility.getSelectedNamedItem(comboType);
		if (type.isEmpty()) {
			return "disc";
		}
		
		return type;
	}

	/**
	 * Get position.
	 * @return
	 */
	private String getPosition() {
		
		String position = Utility.getSelectedNamedItem(comboPosition);
		if (position.isEmpty()) {
			return "outside";
		}
		return position;
	}

	/**
	 * Get image.
	 * @return
	 */
	private String getImage() {
		
		String imageName = textImageName.getText();
		if (imageName.isEmpty()) {
			return "none";
		}
		
		return String.format("url(\"[@URL thisArea, res=\"#%s\"]\")", imageName);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		setType("disc");
		setPosition("outside");
		setImage("none");

		if (initialString != null) {
						
			try {
				Obj<Integer> position = new Obj<Integer>(0);

				// Get type.
				String text = Utility.getNextMatch(initialString, position, "^\\s*\"(([^\\\\\"]|\\\\\"|\\\\(?!\"))*)\"");
				if (text == null) {
					text = Utility.getNextMatch(initialString, position, "\\s*\\S+");
					if (text == null) {
						return;
					}
				}
				else {
					text = text.replace("\\\"", "\"");
				}
				setType(text.trim());
				
				// Get position.
				text = Utility.getNextMatch(initialString, position, "\\s*\\S+");
				if (text == null) {
					return;
				}
				setPosition(text.trim());
				
				// Get image.
				try {
					text = initialString.substring(position.ref);
					setImage(text.trim());
				}
				catch (Exception e) {
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Set type.
	 * @param string
	 */
	private void setType(String string) {
		
		if (!Utility.selectComboNamedItem(comboType, string)) {
			if (string.length() >= 2 && string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"') {
				
				textText.setText(string.substring(1, string.length() - 1));
				return;
			}
		}
		textText.setText("");
	}

	/**
	 * Set position.
	 * @param string
	 */
	private void setPosition(String string) {
		
		Utility.selectComboNamedItem(comboPosition, string);
	}

	/**
	 * Set image.
	 * @param string
	 */
	private void setImage(String string) {
		
		String imageName = getImageName(string);
		if (imageName == null) {
			imageName = "";
		}
		textImageName.setText(imageName);
	}

	/**
	 * Get image name.
	 * @param position
	 * @return
	 */
	private String getImageName(String string) {
		
		Obj<Integer> position = new Obj<Integer>(0);
		
		// Get next match.
		String url = Utility.getNextMatch(string, position, "url");
		if (url == null) {
			return null;
		}
		
		// Get opening parenthesis.
		String leftParenthesis = Utility.getNextMatch(string, position, "\\(");
		if (leftParenthesis == null) {
			return null;
		}
		
		String imageName = null;
		
		// Get name start.
		String nameStart = Utility.getNextMatch(string, position, "res=\"#");
		if (nameStart != null) {
			
			// Get image name.
			imageName = Utility.getNextMatch(string, position, "[^\\\"]*");
		}
		
		// Get closing parenthesis.
		String rightParenthesis = Utility.getNextMatch(string, position, "\\)");
		if (rightParenthesis == null) {
			return null;
		}
		
		return imageName;
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelType);
		Utility.localize(labelText);
		Utility.localize(labelPosition);
		Utility.localize(labelImage);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssListStyleBuilder");
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
		
		CssListStylePanel.bounds = bounds;
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
		
		return meansCssListStyle;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
