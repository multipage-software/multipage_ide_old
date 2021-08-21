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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.maclan.Area;
import org.maclan.AreaTreeData;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Progress2Dialog;
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;

/**
 * 
 * @author
 *
 */
public class ImportDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Use sub name state.
	 */
	private static boolean useEdgeSubNameState = false;

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		useEdgeSubNameState = false;
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeBoolean(useEdgeSubNameState);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		useEdgeSubNameState = inputStream.readBoolean();
	}

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Area reference.
	 */
	private Area area;

	/**
	 * XML file.
	 */
	private File xmlFile;
	
	/**
	 * DAT file.
	 */
	private File datFile;

	/**
	 * Area tree data.
	 */
	private AreaTreeData areaTreeData;
	
	/**
	 * Original sub name.
	 */
	private String originalSubName = "";

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonImport;
	private JButton buttonCancel;
	private JLabel labelImportInfo;
	private JScrollPane scrollPane;
	private JEditorPane editorInfo;
	private JLabel labelSubName;
	private JTextField textSubName;
	private JButton buttonClearSubName;
	private JButton buttonOriginalSubName;
	private JCheckBox checkImportLanguage;
	private JCheckBox checkImportHome;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static Long showDialog(Component parent, Area area,
			boolean askImportLanguage, boolean askImportHome) {
		
		// Find XML file.
		String [][] filters = {{"org.multipage.generator.textXmlAreaTreeData", "xml"}};
		File xmlFile = Utility.chooseFileNameToOpen(parent, filters);
		if (xmlFile == null) {
			return null;
		}

		ImportDialog dialog = new ImportDialog(Utility.findWindow(parent),
				area, xmlFile);
		
		// Set flags.
		dialog.checkImportLanguage.setSelected(askImportLanguage);
		dialog.checkImportHome.setSelected(askImportHome);
		
		dialog.setVisible(true);

		if (dialog.confirm) {
			return dialog.areaTreeData.rootAreaId;
		}
		return null;
	}

	/**
	 * Create the dialog.
	 * @param area 
	 * @param window 
	 * @param xmlFile 
	 */
	public ImportDialog(Window window, Area area, File xmlFile) {
		super(window, ModalityType.APPLICATION_MODAL);
		setResizable(false);

		this.area = area; // $hide$
		this.xmlFile = xmlFile; // $hide$
		initComponents();
		postCreation(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("org.multipage.generator.textImportDialog");
		setBounds(100, 100, 477, 288);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonImport = new JButton("org.multipage.generator.textImport");
		buttonImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onImport();
			}
		});
		buttonImport.setMargin(new Insets(0, 0, 0, 0));
		buttonImport.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonImport);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, buttonImport, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonImport, -6, SpringLayout.WEST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		labelImportInfo = new JLabel("org.multipage.generator.textImportInfo");
		springLayout.putConstraint(SpringLayout.NORTH, labelImportInfo, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelImportInfo, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelImportInfo);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 1, SpringLayout.SOUTH, labelImportInfo);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 96, SpringLayout.SOUTH, labelImportInfo);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		editorInfo = new JEditorPane();
		editorInfo.setEditable(false);
		editorInfo.setContentType("text/html");
		scrollPane.setViewportView(editorInfo);
		
		labelSubName = new JLabel("org.multipage.generator.textInsertEdgeSubName");
		springLayout.putConstraint(SpringLayout.NORTH, labelSubName, 22, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, labelSubName, 0, SpringLayout.WEST, labelImportInfo);
		getContentPane().add(labelSubName);
		
		textSubName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textSubName, 0, SpringLayout.NORTH, labelSubName);
		springLayout.putConstraint(SpringLayout.WEST, textSubName, 6, SpringLayout.EAST, labelSubName);
		getContentPane().add(textSubName);
		textSubName.setColumns(12);
		
		buttonClearSubName = new JButton("");
		buttonClearSubName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClearSubName();
			}
		});
		buttonClearSubName.setMargin(new Insets(0, 0, 0, 0));
		buttonClearSubName.setPreferredSize(new Dimension(20, 20));
		getContentPane().add(buttonClearSubName);
		
		buttonOriginalSubName = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonClearSubName, 0, SpringLayout.NORTH, buttonOriginalSubName);
		buttonOriginalSubName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOriginalSubName();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, buttonClearSubName, 0, SpringLayout.EAST, buttonOriginalSubName);
		springLayout.putConstraint(SpringLayout.WEST, buttonOriginalSubName, 0, SpringLayout.EAST, textSubName);
		springLayout.putConstraint(SpringLayout.NORTH, buttonOriginalSubName, 0, SpringLayout.NORTH, textSubName);
		buttonOriginalSubName.setPreferredSize(new Dimension(20, 20));
		buttonOriginalSubName.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOriginalSubName);
		
		checkImportLanguage = new JCheckBox("org.multipage.generator.textSetLanguage");
		springLayout.putConstraint(SpringLayout.WEST, checkImportLanguage, 20, SpringLayout.EAST, buttonClearSubName);
		getContentPane().add(checkImportLanguage);
		
		checkImportHome = new JCheckBox("org.multipage.generator.textSetHomeArea");
		springLayout.putConstraint(SpringLayout.NORTH, checkImportLanguage, 0, SpringLayout.SOUTH, checkImportHome);
		springLayout.putConstraint(SpringLayout.NORTH, checkImportHome, 0, SpringLayout.NORTH, buttonClearSubName);
		springLayout.putConstraint(SpringLayout.WEST, checkImportHome, 0, SpringLayout.WEST, checkImportLanguage);
		getContentPane().add(checkImportHome);
	}

	/**
	 * On original sub name.
	 */
	protected void onOriginalSubName() {
		
		textSubName.setText(originalSubName);
	}

	/**
	 * On clear sub name.
	 */
	protected void onClearSubName() {
		
		// Ask user and remove sub name.
		if (Utility.ask(this, "org.multipage.generator.messageDeleteEdgeSubNameText")) {
			
			textSubName.setText("");
		}
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		Utility.centerOnScreen(this);
		localize();
		setIcons();
		setToolTips();
		loadAreaTreeData();
		
		loadDialog();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonImport);
		Utility.localize(labelImportInfo);
		Utility.localize(labelSubName);
		Utility.localize(checkImportLanguage);
		Utility.localize(checkImportHome);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonImport.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonOriginalSubName.setIcon(Images.getIcon("org/multipage/generator/images/original.png"));
		buttonClearSubName.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonOriginalSubName.setToolTipText(Resources.getString("org.multipage.generator.tooltipOriginalSubName"));
		buttonClearSubName.setToolTipText(Resources.getString("org.multipage.generator.tooltipClearSubName"));
	}

	/**
	 * Load area tree data.
	 */
	private void loadAreaTreeData() {
		
		areaTreeData = new AreaTreeData();
		
		// Create and execute progress dialog.
		ProgressDialog<MiddleResult> progressDlg = new ProgressDialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textImportProgressTitle"),
				Resources.getString("org.multipage.generator.textImportingData"));
		
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				// Read XML data.
				MiddleResult result =  areaTreeData.readXmlDataFile(xmlFile, this);

				// Call after load method. (use Swing thread)
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						
						afterDialogLoaded();
					}
				});
				
				return result;
			}
		});
				
		MiddleResult result = progressDlg.getOutput();
		// On error inform user.
		if (result != null && result.isNotOK()) {
			
			result.show(this);
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					dispose();
				}
			});
		}
		else {
			
			// Show edge sub name.
			String subName = areaTreeData.rootSuperEdge.nameSub;
			if (subName == null) {
				subName = "";
			}
			
			textSubName.setText(subName);
			
			// Remember original sub name.
			originalSubName = areaTreeData.rootSuperEdge.nameSub;
		}
	}

	/**
	 * Called after dialog loaded.
	 */
	protected void afterDialogLoaded() {
		
		// Set text information.
		String message = areaTreeData.getImportMessage();
		editorInfo.setText(message);

		// Get DAT file.
		Obj<File> datFileOutput = new Obj<File>();
		MiddleResult result = getDatFileFromXmlFile(xmlFile, datFileOutput);
		
		if (result.isOK()) {

			// Set DAT file.
			datFile = datFileOutput.ref;
		}
		else {
			 result.show(this);
			 SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					
					dispose();
				}
			});
		}
	}
	
	/**
	 * Get DAT file from XML file.
	 * @param xmlFile
	 * @return
	 */
	private MiddleResult getDatFileFromXmlFile(File xmlFile, Obj<File> datFileOutput) {
		
		datFileOutput.ref = null;
		
		// If the DAT file is not required exit the method.
		if (!areaTreeData.isRequiredDatFile()) {
			return MiddleResult.OK;
		}
		
		String xmlFileName = xmlFile.getAbsolutePath();
		String name;
		if (xmlFileName.toLowerCase().endsWith(".xml")) {
			int dotPosition = xmlFileName.lastIndexOf('.');
			name = xmlFileName.substring(0, dotPosition);
		}
		else {
			name = xmlFileName;
		}
		
		File datFile = new File(name + ".dat");
		if (!datFile.exists()) {
			
			// Let user choose DAT file.
			Utility.show2(this, String.format(
					Resources.getString("org.multipage.generator.messageCannotFindDatFileChooseIt"), datFile.getAbsolutePath()));
			
			String [][] filters = {{"org.multipage.generator.textDatFiles", "dat"}};
			
			File selectedFile = Utility.chooseFileNameToOpen(this, filters);
			
			if (selectedFile == null) {

				return new MiddleResult("org.multipage.generator.messageNoDatFileSelected", null);
			}
			
			datFile = selectedFile;
		}
		
		if (!datFile.canRead()) {
			return new MiddleResult(null, String.format(
					Resources.getString("middle.messageCannotReadDatFile"), datFile.getAbsolutePath()));
		}
		
		datFileOutput.ref = datFile;
		return MiddleResult.OK;
	}

	/**
	 * On import.
	 */
	protected void onImport() {
		
		// Save dialog.
		saveDialog();
		
		// Set sub name.
		String nameSub = textSubName.getText();
		if (nameSub.isEmpty()) {
			nameSub = null;
		}
		if (areaTreeData.rootSuperEdge != null) {
			areaTreeData.rootSuperEdge.nameSub = nameSub;
		}
				
		final boolean importLanguage = checkImportLanguage.isSelected();
		
		// Get use edge sub name flag.
		final Middle middle = ProgramBasic.getMiddle();
		final Properties login = ProgramBasic.getLoginProperties();
		
		// Create and execute progress dialog.
		Progress2Dialog<MiddleResult> progressDlg = new Progress2Dialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textImportProgressTitle"),
				Resources.getString("org.multipage.generator.textImportingData"));
		
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {

				// Import data.
				MiddleResult result = areaTreeData.saveToDatabaseFile(middle, login, area,
						datFile, importLanguage, this);

				return result;
			}
		});
				
		MiddleResult result = progressDlg.getOutput();
		// On error inform user.
		if (result != null && result.isNotOK() && result != MiddleResult.CANCELLATION) {
			result.show(this);
			dispose();
			return;
		}
		
		// Set new home area.
		boolean importHome = checkImportHome.isSelected();
		
		Long homeAreaId = areaTreeData.homeAreaId;
		if (importHome && homeAreaId != null) {
			
			// Get new area ID.
			Long newHomeAreaId = areaTreeData.getNewAreaId(homeAreaId);
			if (newHomeAreaId != null) {
				
				result = middle.setStartArea(login, newHomeAreaId);
				if (result.isNotOK()) {
					
					result.show(this);
				}
			}
		}
		
		// Set new start language.		
		Long startLanguageId = areaTreeData.startLanguageId;
		if (importLanguage && startLanguageId != null) {
			
			// Get new language ID.
			Long newStartLanguageId = areaTreeData.getNewLanguageId(startLanguageId);
			if (newStartLanguageId != null) {
				
				result = middle.updateStartLanguage(login, newStartLanguageId);
				if (result.isNotOK()) {
					
					result.show(this);
				}
			}
		}
		
		// Update root area ID.
		areaTreeData.rootAreaId = areaTreeData.getNewAreaId(areaTreeData.rootAreaId);
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
	}
}
