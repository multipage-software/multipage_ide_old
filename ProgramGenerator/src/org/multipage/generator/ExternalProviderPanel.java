/*
 * Copyright 2010-2019 (C) vakol
 * 
 * Created on : 10-12-2019
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.maclan.ExternalLinkParser;
import org.maclan.Slot;
import org.maclan.ExternalLinkParser.Type;
import org.multipage.gui.StringValueEditor;
import org.multipage.gui.Utility;

/**
 * 
 * @author user
 *
 */
public class ExternalProviderPanel extends JPanel {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Panel map.
	 */
	private Object [][] panelMap = null;
			
	/**
	 * Controls.
	 */
	private JRadioButton radioFile;
	private JRadioButton radioUrl;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton radioArea;
	private JPanel container;
	
	/**
	 * Panels with input controls
	 */
	private FilePanel filePanel;
	private UrlPanel urlPanel;
	private AreaPanel areaPanel;
	
	/**
	 * Current editor
	 */
	private StringValueEditor currentEditor;
	private JCheckBox checkWritesOutput;
	private JCheckBox checkReadsInput;
	
	/**
	 * Create the panel.
	 */
	public ExternalProviderPanel() {
		
		// Initialize components
		initComponents();
		// Post creation of the panel
		postCreation(); //$hide$
	}

	/**
	 * Initialize components
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		radioFile = new JRadioButton("org.multipage.generator.textFileProvider");
		radioFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePanel();
			}
		});
		buttonGroup.add(radioFile);
		springLayout.putConstraint(SpringLayout.NORTH, radioFile, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, radioFile, 10, SpringLayout.WEST, this);
		add(radioFile);
		
		radioUrl = new JRadioButton("org.multipage.generator.textUrlProvider");
		radioUrl.setEnabled(false);
		radioUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePanel();
			}
		});
		buttonGroup.add(radioUrl);
		springLayout.putConstraint(SpringLayout.NORTH, radioUrl, 0, SpringLayout.NORTH, radioFile);
		springLayout.putConstraint(SpringLayout.WEST, radioUrl, 6, SpringLayout.EAST, radioFile);
		add(radioUrl);
		
		radioArea = new JRadioButton("org.multipage.generator.textAreaProvider");
		radioArea.setEnabled(false);
		radioArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePanel();
			}
		});
		buttonGroup.add(radioArea);
		springLayout.putConstraint(SpringLayout.WEST, radioArea, 6, SpringLayout.EAST, radioUrl);
		springLayout.putConstraint(SpringLayout.SOUTH, radioArea, 0, SpringLayout.SOUTH, radioFile);
		add(radioArea);
		
		container = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, container, 0, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, container, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, container, 0, SpringLayout.EAST, this);
		add(container);
		container.setLayout(new BorderLayout(0, 0));
		
		checkWritesOutput = new JCheckBox("org.multipage.generator.textWriteOutputToExternalProvider");
		springLayout.putConstraint(SpringLayout.NORTH, container, 0, SpringLayout.SOUTH, checkWritesOutput);
		springLayout.putConstraint(SpringLayout.WEST, checkWritesOutput, 10, SpringLayout.WEST, this);
		add(checkWritesOutput);
		
		checkReadsInput = new JCheckBox("org.multipage.generator.textReadInputFromExternalProvider");
		springLayout.putConstraint(SpringLayout.NORTH, checkWritesOutput, 3, SpringLayout.SOUTH, checkReadsInput);
		springLayout.putConstraint(SpringLayout.NORTH, checkReadsInput, 3, SpringLayout.SOUTH, radioFile);
		springLayout.putConstraint(SpringLayout.WEST, checkReadsInput, 10, SpringLayout.WEST, this);
		add(checkReadsInput);
	}
	
	/**
	 * Post creation of the panel
	 */
	private void postCreation() {
		
		localize();
		
		loadDialog();
	}
	
	/**
	 * Localize controls
	 */
	private void localize() {
		
		Utility.localize(radioFile);
		Utility.localize(radioUrl);
		Utility.localize(radioArea);
		Utility.localize(checkReadsInput);
		Utility.localize(checkWritesOutput);
	}
	
	/**
	 * Update panel depending on radio buttons
	 */
	private void updatePanel() {
		
		container.remove(filePanel);
		container.remove(urlPanel);
		container.remove(areaPanel);
		currentEditor = null;
		
		if (radioFile.isSelected()) {
			container.add(filePanel, BorderLayout.CENTER);
			currentEditor = filePanel;
		}
		else if (radioUrl.isSelected()) {
			container.add(urlPanel, BorderLayout.CENTER);
			currentEditor = urlPanel;
		}
		else if (radioArea.isSelected()) {
			container.add(areaPanel, BorderLayout.CENTER);
			currentEditor = areaPanel;
		}
		
		container.updateUI();
	}
	
	/**
	 * Load dialog
	 */
	private void loadDialog() {
		
		// Create panels.
		filePanel = new FilePanel("");
		urlPanel = new UrlPanel("");
		areaPanel = new AreaPanel("");
		
		// Create panel map.
		panelMap = new Object [][] {{ ExternalLinkParser.Type.FILE, radioFile, filePanel },
									{ ExternalLinkParser.Type.URL,  radioUrl, urlPanel },
									{ ExternalLinkParser.Type.AREA, radioArea, areaPanel }};
		
		loadEditor(Type.FILE, null, null);
									
		updatePanel();
	}

	/**
	 * Get external provider link
	 * @return
	 */
	String getExternalProviderLink() {
		
		if (currentEditor == null) {
			return "";
		}
		
		String link = currentEditor.getSpecification();
		if (link.isEmpty()) {
			return "";
		}
		
		String valueMeaning = getValueMeaning();
		link = valueMeaning + ";" + link;
		
		return link;
	}
	
	/**
	 * Get reads input flag
	 */
	boolean getReadsInput() {
		
		return checkReadsInput.isSelected();
	}
	
	/**
	 * Get writes output flag
	 */
	boolean getWritesOutput() {
		
		return checkWritesOutput.isSelected();
	}
	
	/**
	 * Gets type of the external link
	 * @return
	 */
	String getValueMeaning() {
		
		if (currentEditor == null) {
			return "";
		}
		
		for (Object [] item : panelMap) {
			if (item[2].equals(currentEditor)) {
				
				ExternalLinkParser.Type type = (ExternalLinkParser.Type) item[0];
				return type.alias;
			}
		}
		
		return "";
	}
	
	/**
	 * Select panel.
	 * @param type
	 * @param link 
	 * @param area 
	 */
	private void loadEditor(Type type, String link, Area area) {
		
		// Do loop for all panels.
		for (Object [] item : panelMap) {
			if (item[0].equals(type)) {
				
				JRadioButton radio = (JRadioButton)  item[1];
				radio.setSelected(true);
				
				if (link != null) {
					
					// Set external provider access string.
					ExternalProviderInterface handler = (ExternalProviderInterface) item[2];
					handler.setEditor(link, area);
				}
				
				break;
			}
		}
	}
	
	/**
	 * Load controls content from slot.
	 * @param slot
	 */
	public void loadFromSlot(Slot slot) {
		
		// Set area.
		Area area = (Area) slot.getHolder();
		
		// Get external provider link.
		String link = slot.getExternalProvider();
		ExternalLinkParser.Type type = ExternalLinkParser.getType(link);
		
		// Load editor depending on type.
		loadEditor(type, link, area);
		
		// Set slot reads flag.
		boolean readsInput = slot.getReadsInput();
		checkReadsInput.setSelected(readsInput);
		
		// Set slot writes flag.
		boolean writesOutput = slot.getWritesOutput();
		checkWritesOutput.setSelected(writesOutput);
	}
}
