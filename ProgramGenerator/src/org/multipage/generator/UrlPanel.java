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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.InsertPanel;
import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

import com.maclan.Area;

/**
 * 
 * @author
 *
 */
public class UrlPanel extends InsertPanel implements StringValueEditor, ExternalProviderInterface {

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
	private JLabel labelUrl;
	private JTextField textUrl;
	private JButton buttonCheckUrl;
	private JLabel labelMessage;
	private JLabel labelEncoding;
	private JComboBox comboEncoding;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public UrlPanel(String initialString) {

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
		
		labelUrl = new JLabel("org.multipage.generator.textInsertUrl");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelUrl, 28, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelUrl, 28, SpringLayout.WEST, this);
		add(labelUrl);
		
		textUrl = new TextFieldEx();
		textUrl.setText("http://");
		sl_panelMain.putConstraint(SpringLayout.NORTH, textUrl, -3, SpringLayout.NORTH, labelUrl);
		textUrl.setPreferredSize(new Dimension(6, 25));
		textUrl.setMinimumSize(new Dimension(6, 25));
		sl_panelMain.putConstraint(SpringLayout.WEST, textUrl, 6, SpringLayout.EAST, labelUrl);
		add(textUrl);
		textUrl.setColumns(25);
		
		buttonCheckUrl = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.EAST, textUrl, -3, SpringLayout.WEST, buttonCheckUrl);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonCheckUrl, 0, SpringLayout.NORTH, textUrl);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonCheckUrl, -20, SpringLayout.EAST, this);
		buttonCheckUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCheckUrl();
			}
		});
		buttonCheckUrl.setMargin(new Insets(0, 0, 0, 0));
		buttonCheckUrl.setPreferredSize(new Dimension(25, 25));
		add(buttonCheckUrl);
		
		labelMessage = new JLabel("");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelMessage, 6, SpringLayout.SOUTH, textUrl);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, labelUrl);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelMessage, -20, SpringLayout.EAST, this);
		add(labelMessage);
		
		labelEncoding = new JLabel("org.multipage.generator.textExternalProviderEncoding");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelEncoding, 140, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelEncoding, 0, SpringLayout.WEST, labelUrl);
		add(labelEncoding);
		
		comboEncoding = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboEncoding, 0, SpringLayout.NORTH, labelEncoding);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboEncoding, 6, SpringLayout.EAST, labelEncoding);
		comboEncoding.setPreferredSize(new Dimension(160, 20));
		add(comboEncoding);
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
		initializePanel();
	}
	
	/**
	 * Initialize panel
	 */
	private void initializePanel() {
		
		// If text changes, reset message
		Utility.setTextChangeListener(textUrl, () -> {
			labelMessage.setText("");
		});
	}

	/**
	 * On check URL.
	 */
	protected void onCheckUrl() {
		
		new Thread(() -> {
			
			String urlText = textUrl.getText();
			URL url;
			try {
				url = new URL(urlText);
				
				// Display waiting message
				labelMessage.setText(Resources.getString("org.multipage.generator.messageWaitingForUrlResponse"));
				
				// Try to open and close URL stream
				InputStream stream = url.openStream();
				stream.close();
				
				// Display success message
				labelMessage.setText(Resources.getString("org.multipage.generator.messageUrlSuccess"));
			}
			catch (Exception e) {
				
				// Display error message
				labelMessage.setText(String.format(
						Resources.getString("org.multipage.generator.messageUrlError"), e.getLocalizedMessage()));
			}
			
		}).start();
		
		// Create local message loop to not leave this event handler
		Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop().enter();
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		return textUrl.getText();
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			textUrl.setText(initialString);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelUrl);
		Utility.localize(labelEncoding);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonCheckUrl.setIcon(Images.getIcon("org/multipage/generator/images/check_icon.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonCheckUrl.setToolTipText(Resources.getString("org.multipage.generator.tooltipCheckUrlResponse"));
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.generator.textUrlPanel");
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
		
		UrlPanel.bounds = bounds;
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
		
		return meansUrl;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
	
	/**
	 * Set editor controls from link string.
	 */
	@Override
	public void setEditor(String link, Area area) {
		
		// Parse link string.
	}
}
