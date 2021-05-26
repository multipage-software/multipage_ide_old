/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.InsertPanel;
import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class FolderPanel extends InsertPanel implements StringValueEditor {

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
		
		bounds = new Rectangle(0, 0, 500, 330);
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
	private JLabel labelFolderPath;
	private JTextField textFolderPath;
	private JButton buttonSelectFolderPath;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public FolderPanel(String initialString) {

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
		
		labelFolderPath = new JLabel("org.multipage.generator.textInsertFolderPath");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFolderPath, 28, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFolderPath, 28, SpringLayout.WEST, this);
		add(labelFolderPath);
		
		textFolderPath = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textFolderPath, -3, SpringLayout.NORTH, labelFolderPath);
		textFolderPath.setPreferredSize(new Dimension(6, 25));
		textFolderPath.setMinimumSize(new Dimension(6, 25));
		sl_panelMain.putConstraint(SpringLayout.WEST, textFolderPath, 6, SpringLayout.EAST, labelFolderPath);
		add(textFolderPath);
		textFolderPath.setColumns(25);
		
		buttonSelectFolderPath = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.EAST, textFolderPath, -3, SpringLayout.WEST, buttonSelectFolderPath);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonSelectFolderPath, 0, SpringLayout.NORTH, textFolderPath);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonSelectFolderPath, -20, SpringLayout.EAST, this);
		buttonSelectFolderPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectFilePath();
			}
		});
		buttonSelectFolderPath.setMargin(new Insets(0, 0, 0, 0));
		buttonSelectFolderPath.setPreferredSize(new Dimension(25, 25));
		add(buttonSelectFolderPath);
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
		setToolTips();
	}

	/**
	 * On select folder path.
	 */
	protected void onSelectFilePath() {
		
		String folder = Utility.chooseDirectory(this, null);
		if (folder == null) {
			return;
		}
		
		textFolderPath.setText(folder);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return textFolderPath.getText();
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			textFolderPath.setText(initialString);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelFolderPath);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonSelectFolderPath.setIcon(Images.getIcon("org/multipage/gui/images/folder.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonSelectFolderPath.setToolTipText(Resources.getString("org.multipage.generator.tooltipSelectMimeType"));
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.generator.textCssMimeBuilder");
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
		
		FolderPanel.bounds = bounds;
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
		
		return meansFile;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
