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
import java.util.regex.Matcher;
import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class CssCursorPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelCursor;
	private JComboBox comboCursor;
	private JTextField textCursor;
	private JButton buttonFindCursor;
	private JButton buttonClearCursor;

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
	 * Cursor names.
	 */
	private static final String [] cursorNames = new String [] {
			"auto",
			"default",
			"none",
			"context-menu",
			"help",
			"pointer",
			"progress",
			"wait",
			"cell",
			"crosshair",
			"text",
			"vertical-text",
			"alias",
			"copy",
			"move",
			"no-drop",
			"not-allowed",
			"e-resize",
			"n-resize",
			"ne-resize",
			"nw-resize",
			"s-resize",
			"se-resize",
			"sw-resize",
			"w-resize",
			"ew-resize",
			"ns-resize",
			"nesw-resize",
			"nwse-resize",
			"col-resize",
			"row-resize",
			"all-scroll",
			"zoom-in",
			"zoom-out",
			"grab",
			"grabbing"};
	
	/**
	 * Cursor names regex.
	 */
	private static String cursorNamesRegex = "";
	
	/**
	 * Static constructor.
	 */
	static {
		
		// Compile cursor names regex.
		boolean isFirst = true;
		for (String cursorName : cursorNames) {
			
			if (!isFirst) {
				cursorNamesRegex += '|';
			}
			
			cursorNamesRegex += cursorName;
			
			isFirst = false;
		}
	}
	
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
	 * Get resource name callback.
	 */
	private Callback getResourceName;
	
	/**
	 * Set callback.
	 * @param callback
	 */
	public void setResourceNameCallback(Callback callback) {
		
		getResourceName = callback;
	}

	// $hide<<$

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssCursorPanel(String initialString) {

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
				
		labelCursor = new JLabel("org.multipage.gui.textCssCursor");
		springLayout.putConstraint(SpringLayout.NORTH, labelCursor, 50, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelCursor, 35, SpringLayout.WEST, this);
		add(labelCursor);
		
		comboCursor = new JComboBox();
		comboCursor.setPreferredSize(new Dimension(100, 24));
		springLayout.putConstraint(SpringLayout.NORTH, comboCursor, 0, SpringLayout.NORTH, labelCursor);
		springLayout.putConstraint(SpringLayout.WEST, comboCursor, 6, SpringLayout.EAST, labelCursor);
		add(comboCursor);
		
		textCursor = new TextFieldEx();
		textCursor.setEditable(false);
		textCursor.setPreferredSize(new Dimension(6, 24));
		springLayout.putConstraint(SpringLayout.NORTH, textCursor, 0, SpringLayout.NORTH, labelCursor);
		springLayout.putConstraint(SpringLayout.WEST, textCursor, 21, SpringLayout.EAST, comboCursor);
		add(textCursor);
		textCursor.setColumns(16);
		
		buttonFindCursor = new JButton("");
		buttonFindCursor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindResource();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonFindCursor, 0, SpringLayout.NORTH, labelCursor);
		springLayout.putConstraint(SpringLayout.WEST, buttonFindCursor, 0, SpringLayout.EAST, textCursor);
		buttonFindCursor.setPreferredSize(new Dimension(24, 24));
		buttonFindCursor.setMargin(new Insets(0, 0, 0, 0));
		add(buttonFindCursor);
		
		buttonClearCursor = new JButton("");
		buttonClearCursor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClearResource();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonClearCursor, 0, SpringLayout.NORTH, labelCursor);
		springLayout.putConstraint(SpringLayout.WEST, buttonClearCursor, 0, SpringLayout.EAST, buttonFindCursor);
		buttonClearCursor.setPreferredSize(new Dimension(24, 24));
		buttonClearCursor.setMargin(new Insets(0, 0, 0, 0));
		add(buttonClearCursor);
	}

	/**
	 * On clear resource.
	 */
	protected void onClearResource() {
		
		textCursor.setText("");
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
		
		// Set resource name text control.
		textCursor.setText(imageName);
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
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonFindCursor.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
		buttonClearCursor.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Load combo boxes.
	 */
	private void loadComboBoxes() {
		
		Utility.loadItems(comboCursor, cursorNames);
		comboCursor.setSelectedItem("auto");
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String resourceName = textCursor.getText();
		String cursorName = (String) comboCursor.getSelectedItem();
		
		if (resourceName.isEmpty()) {
			return cursorName;
		}
		
		return String.format("url(\"[@URL thisArea, res=\"#%s\"]\"), %s", resourceName, cursorName);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		comboCursor.setSelectedItem("auto");

		if (initialString != null) {
			
			Obj<Integer> position = new Obj<Integer>(0);
			
			try {
				
				// Get url.
				int positionAux = position.ref;
				Obj<Matcher> matcher = new Obj<Matcher>();
				
				String text = Utility.getNextMatch(initialString, position, "\\G\\s*url\\(\"\\[@URL thisArea, res=\"#(.+)\"\\]\"\\)\\s*\\,", matcher);
				if (text != null && matcher.ref.groupCount() == 1) {
					
					String resourceName = matcher.ref.group(1);
					textCursor.setText(resourceName.trim());
				}
				else {
					position.ref = positionAux;
				}
				
				// Get cursor name.
				String regex = String.format("\\G\\s*(%s)", cursorNamesRegex);
				text = Utility.getNextMatch(initialString, position, regex, matcher);
				
				if (text != null && matcher.ref.groupCount() == 1) {
					
					String cursorName = matcher.ref.group(1);
					comboCursor.setSelectedItem(cursorName);
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

		Utility.localize(labelCursor);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssCursorBuilder");
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
		
		CssCursorPanel.bounds = bounds;
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
		
		return meansCssCursor;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
