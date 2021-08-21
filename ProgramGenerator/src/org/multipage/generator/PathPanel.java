/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.multipage.gui.Images;
import org.multipage.gui.InsertPanel;
import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.TextPopupMenu;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class PathPanel extends InsertPanel implements StringValueEditor, SlotValueEditorPanelInterface {
	
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
	
	/**
	 * Area reference.
	 */
	private Area area;
	
	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelFolderPath;
	private TextFieldEx textFolderPath;
	private JButton buttonSelectFolderPath;
	private JScrollPane scrollPathHint;
	private JTextPane textPathHint;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public PathPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}
	
	/**
	 * Constructor.
	 */
	public PathPanel() {
		
		this("");
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelFolderPath = new JLabel("org.multipage.generator.textInsertPath");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFolderPath, 28, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFolderPath, 20, SpringLayout.WEST, this);
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
		
		scrollPathHint = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPathHint, 40, SpringLayout.SOUTH, textFolderPath);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPathHint, 0, SpringLayout.WEST, textFolderPath);
		scrollPathHint.setBorder(null);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPathHint, -20, SpringLayout.SOUTH, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPathHint, 0, SpringLayout.EAST, buttonSelectFolderPath);
		add(scrollPathHint);
		
		textPathHint = new JTextPane();
		textPathHint.setForeground(Color.LIGHT_GRAY);
		textPathHint.setEditable(false);
		textPathHint.setContentType("text/html");
		textPathHint.setBorder(null);
		textPathHint.setOpaque(false);
		scrollPathHint.setViewportView(textPathHint);
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
		setEditBoxMenu();
		loadHint();
	}
	
	/**
	 * Set area.
	 * @param area
	 */
	public void setArea(Area area) {
		
		this.area = area;
	}
	
	/**
	 * Set edit box trayMenu.
	 */
	private void setEditBoxMenu() {
		
		// Insert trayMenu item
		TextPopupMenu menu = textFolderPath.getMenu();
		menu.insertItem(0, "org.multipage.generator.menuInsertPath", null, () -> {
			onInsertPath();
		});
		menu.insertSeparator(1);
	}

	/**
	 * On select folder path.
	 */
	protected void onSelectFilePath() {
		
		String folder = Utility.chooseDirectory(this, null);
		if (folder == null) {
			return;
		}
		
		textFolderPath.setText(folder + File.separator);
	}
	
	/**
	 * On insert path.
	 */
	private void onInsertPath() {
		
		// Insert selected path.
		ProgramPaths.PathSupplier path = PathSelectionDialog.showDialog(this, this.area);
		if (path != null) {
			textFolderPath.replaceSelection(path.tag);
		}
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
	 * Load hint.
	 */
	private void loadHint() {
		
		String hint = Resources.getString("org.multipage.generator.textPathHint");
		textPathHint.setText(hint);
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
		
		PathPanel.bounds = bounds;
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
		
		return meansPath;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
	
	/**
	 * Get value.
	 */
	@Override
	public Object getValue() {
		
		// Get path text.
		String pathText = textFolderPath.getText();
		if (!pathText.isEmpty()) {
			return pathText;
		}
		return null;
	}
	
	/**
	 * Set value.
	 */
	@Override
	public void setValue(Object value) {
		
		// Set path text.
		String pathText = "";
		if (value instanceof String) {
			pathText = (String) value;
		}
		
		textFolderPath.setText(pathText);
	}
	
	/**
	 * Set default value.
	 */
	@Override
	public void setDefault(boolean isDefault) {
		
		
	}
}
