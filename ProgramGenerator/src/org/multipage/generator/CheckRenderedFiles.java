/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.maclan.Area;
import org.maclan.AreasModel;
import org.maclan.VersionObj;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class CheckRenderedFiles extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog bounds.
	 */
	private static Rectangle bounds = new Rectangle(100, 100, 450, 300);

	/**
	 * Load parameters.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		 Object object = inputStream.readObject();
		 if (object instanceof Rectangle) {
			 bounds = (Rectangle) object;
		 }
		 else {
			 throw new ClassNotFoundException();
		 }
	}

	/**
	 * Save parameters.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Load dialog.
	 */
	public void loadDialog() {
		
		setBounds(bounds);
	}
	
	/**
	 * Save dialog.
	 */
	public void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * Areas reference.
	 */
	private LinkedList<Area> areas;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JPanel panelMain;
	private JLabel labelAmbiguousPages;
	private JScrollPane scrollPane;
	private JButton buttonReload;
	private JButton buttonCancel;

	/**
	 * Show dialog.
	 * @param parent
	 * @param areas 
	 * @param resource
	 */
	public static void showDialog(Component parent, LinkedList<Area> areas) {
		
		CheckRenderedFiles dialog = new CheckRenderedFiles(Utility.findWindow(parent), false, areas);
		
		dialog.buttonCancel.setVisible(false);
		dialog.setVisible(true);
	}

	/**
	 * Show modal dialog.
	 * @param parent
	 * @param areas
	 */
	public static boolean showDialogModal(Component parent, LinkedList<Area> areas) {
		
		CheckRenderedFiles dialog = new CheckRenderedFiles(Utility.findWindow(parent), true, areas);
		
		dialog.buttonCancel.setVisible(true);
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param modal 
	 * @param areas 
	 */
	public CheckRenderedFiles(Window parentWindow, boolean modal, LinkedList<Area> areas) {
		super(parentWindow, modal ? ModalityType.DOCUMENT_MODAL : ModalityType.MODELESS);

		initComponents();
		
		checkAreas(areas); // $hide$
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(300, 200));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("org.multipage.generator.textCheckRenderedFiles");
		
		setBounds(100, 100, 447, 297);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.EAST, panel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonReload = new JButton("org.multipage.generator.textReload");
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onReload();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonReload, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.WEST, buttonReload, 10, SpringLayout.WEST, panel);
		buttonReload.setPreferredSize(new Dimension(80, 25));
		buttonReload.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonReload);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, 0, SpringLayout.SOUTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -6, SpringLayout.WEST, buttonOk);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelAmbiguousPages = new JLabel("org.multipage.generator.textAmbiguousPages");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelAmbiguousPages, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelAmbiguousPages, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelAmbiguousPages);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 7, SpringLayout.SOUTH, labelAmbiguousPages);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, -7, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		confirm = true;
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
				
		confirm = false;
		
		saveDialog();
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
				
		loadDialog();
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(labelAmbiguousPages);
		Utility.localize(buttonReload);
		Utility.localize(buttonCancel);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonReload.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * Check areas.
	 * @param areas
	 */
	private void checkAreas(LinkedList<Area> areas) {
		
		// Save reference.
		this.areas = areas;
		
		// Create panel.
		JPanel listPanel = new JPanel();
		scrollPane.setViewportView(listPanel);
		
		// Get list of ambiguous file names.
		LinkedList<AmbiguousFileName> ambiguousFileNames = new LinkedList<AmbiguousFileName>();
		
		AreasModel model = ProgramGenerator.getAreasModel();
		LinkedList<VersionObj> versions = model.getVersions();
		
		getAmbiguousFileNames(areas, versions, ambiguousFileNames);

		int count = ambiguousFileNames.size();
		
		if (count == 0) {
			// Inform user.
			listPanel.setLayout(new BorderLayout());
			
			JLabel labelMessage = new JLabel(
					Resources.getString("org.multipage.generator.messageNoAmbiguousFileNamesFound"));
			
			labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
			listPanel.add(labelMessage, BorderLayout.CENTER);
			return;
		}

		// Set list panel layout.
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		
		// Add panels.
		for (int index = 0; index < count; index++) {
			
			AmbiguousFileName fileName = ambiguousFileNames.get(index);
			
			// Add new panel.
			JPanel panel = new AmbiguousRenderedFilePanel(fileName);
			
			Dimension size = panel.getPreferredSize();
			size.width = Integer.MAX_VALUE;
			panel.setMaximumSize(size);
			
			panel.setBackground(Utility.itemColor(index));
			listPanel.add(panel);
		}
	}

	/**
	 * Get ambiguous file names.
	 * @param areas
	 * @param ambiguousFileNames
	 */
	public static void getAmbiguousFileNames(
			LinkedList<Area> areas, LinkedList<VersionObj> versions,
			LinkedList<AmbiguousFileName> ambiguousFileNames) {
		
		ambiguousFileNames.clear();
		
		AreasModel model = ProgramGenerator.getAreasModel();
		
		LinkedList<AmbiguousFileName> fileNames = new LinkedList<AmbiguousFileName>();
		
		for (Area area : areas) {
			
			if (!area.isVisible()) {
				continue;
			}
			
			for (VersionObj version : versions) {
				
				// Get area full file name.
				String fullFileName = model.getAreaFullFileName(area, version);
				if (fullFileName == null) {
					continue;
				}
				
				// Add area full file name.
				addFileName(fileNames, fullFileName, area, version);
			}
		}
		
		// Get ambiguous file names.
		for (AmbiguousFileName fileName : fileNames) {
			
			if (fileName.isAmbiguous()) {
				ambiguousFileNames.add(fileName);
			}
		}
	}

	/**
	 * Add file name.
	 * @param fileNamesList
	 * @param area
	 * @param version 
	 */
	private static void addFileName(LinkedList<AmbiguousFileName> fileNamesList,
			String fullFileName, Area area, VersionObj version) {
		
		AmbiguousFileName foundFileName = null;
		
		// Try to found existing file name.
		for (AmbiguousFileName fileName : fileNamesList) {
			
			if (fileName.fileName.equalsIgnoreCase(fullFileName)) {
				
				foundFileName = fileName;
				break;
			}
		}
		
		if (foundFileName == null) {
			foundFileName = new AmbiguousFileName(fullFileName);
			fileNamesList.add(foundFileName);
		}
		
		// Add new item.
		foundFileName.addItem(area, version);
	}
	
	/**
	 * On reload.
	 */
	protected void onReload() {
		
		// Get model.
		AreasModel model = ProgramGenerator.getAreasModel();
		
		// Update areas' references.
		model.updateAreas(areas);
		
		// Check new areas.
		checkAreas(areas);
	}
}