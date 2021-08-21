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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.CancellationException;

import javax.swing.JButton;
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
import org.multipage.gui.ProgressDialog;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;
import org.multipage.util.SwingWorkerHelper;

/**
 * 
 * @author
 *
 */
public class ExportDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Export folder.
	 */
	public static String exportFolder = "";

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Area reference.
	 */
	private Area area;
	
	/**
	 * Parent area reference.
	 */
	private Area parentArea;
	
	/**
	 * Area tree data.
	 */
	private AreaTreeData areaTreeData;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonExport;
	private JButton buttonCancel;
	private JLabel labelExportInfo;
	private JScrollPane scrollPane;
	private JEditorPane editorInfo;
	private JLabel labelExportFolder;
	private JTextField textFolder;
	private JButton buttonFolder;
	private JLabel labelExportTreeName;
	private JTextField textFileName;

	/**
	 * Show dialog.
	 * @param parent
	 * @param parentArea 
	 * @return
	 */
	public static boolean showDialog(Component parent, Area area, Area parentArea) {

		ExportDialog dialog = new ExportDialog(Utility.findWindow(parent), area, parentArea);
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param area 
	 * @param window 
	 * @param parentArea 
	 */
	public ExportDialog(Window window, Area area, Area parentArea) {
		super(window, ModalityType.DOCUMENT_MODAL);

		this.area = area; // $hide$
		this.parentArea = parentArea; // $hide$
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
		setTitle("org.multipage.generator.textExportDialog");
		setBounds(100, 100, 450, 321);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonExport = new JButton("org.multipage.generator.textExport");
		buttonExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExport();
			}
		});
		buttonExport.setMargin(new Insets(0, 0, 0, 0));
		buttonExport.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonExport);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, buttonExport, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonExport, -6, SpringLayout.WEST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		labelExportInfo = new JLabel("org.multipage.generator.textExportInfo");
		springLayout.putConstraint(SpringLayout.NORTH, labelExportInfo, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelExportInfo, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelExportInfo);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 1, SpringLayout.SOUTH, labelExportInfo);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 96, SpringLayout.SOUTH, labelExportInfo);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 424, SpringLayout.WEST, getContentPane());
		getContentPane().add(scrollPane);
		
		editorInfo = new JEditorPane();
		editorInfo.setEditable(false);
		editorInfo.setContentType("text/html");
		scrollPane.setViewportView(editorInfo);
		
		labelExportFolder = new JLabel("org.multipage.generator.textExportFolder");
		springLayout.putConstraint(SpringLayout.NORTH, labelExportFolder, 6, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, labelExportFolder, 0, SpringLayout.WEST, labelExportInfo);
		getContentPane().add(labelExportFolder);
		
		textFolder = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textFolder, 6, SpringLayout.SOUTH, labelExportFolder);
		springLayout.putConstraint(SpringLayout.WEST, textFolder, 10, SpringLayout.WEST, getContentPane());
		textFolder.setEditable(false);
		getContentPane().add(textFolder);
		textFolder.setColumns(10);
		
		buttonFolder = new JButton("");
		buttonFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onFolder();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonFolder, 0, SpringLayout.NORTH, textFolder);
		springLayout.putConstraint(SpringLayout.EAST, textFolder, -6, SpringLayout.WEST, buttonFolder);
		springLayout.putConstraint(SpringLayout.EAST, buttonFolder, 0, SpringLayout.EAST, buttonCancel);
		buttonFolder.setPreferredSize(new Dimension(20, 20));
		getContentPane().add(buttonFolder);
		
		labelExportTreeName = new JLabel("org.multipage.generator.textExportTreeName");
		springLayout.putConstraint(SpringLayout.NORTH, labelExportTreeName, 6, SpringLayout.SOUTH, textFolder);
		springLayout.putConstraint(SpringLayout.WEST, labelExportTreeName, 0, SpringLayout.WEST, labelExportInfo);
		getContentPane().add(labelExportTreeName);
		
		textFileName = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, textFileName, 6, SpringLayout.SOUTH, labelExportTreeName);
		springLayout.putConstraint(SpringLayout.WEST, textFileName, 0, SpringLayout.WEST, labelExportInfo);
		springLayout.putConstraint(SpringLayout.EAST, textFileName, 0, SpringLayout.EAST, textFolder);
		textFileName.setColumns(10);
		getContentPane().add(textFileName);
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
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
		loadDialog();
		loadDataToExport();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonExport);
		Utility.localize(labelExportInfo);
		Utility.localize(labelExportFolder);
		Utility.localize(labelExportTreeName);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonExport.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonFolder.setIcon(Images.getIcon("org/multipage/generator/images/open.png"));
	}
	
	/**
	 * Returns true value if the folder exists.
	 * @param folder
	 * @return
	 */
	private boolean existsFolder(String folder) {
		
		return new File(folder).exists();
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		setFolderField(exportFolder);
		textFileName.setText(Utility.convertToFileName(area.getDescriptionForced(false)));
	}

	/**
	 * Set taget field.
	 * @param folder
	 */
	private void setFolderField(String folder) {
		
		if (!existsFolder(folder)) {
			folder = Resources.getString("org.multipage.generator.textFolderDoesntExist");
			textFolder.setForeground(Color.RED);
			exportFolder = "";
		}
		else {
			textFolder.setForeground(Color.BLACK);
			exportFolder = folder;
		}
		
		textFolder.setText(folder);
	}
	
	/**
	 * On set folder.
	 */
	protected void onFolder() {
		
		// Get rendering target.
		String folder = Utility.chooseDirectory(this, Resources.getString("org.multipage.generator.textSelectExportFolder"));
		if (folder == null) {
			return;
		}
		
		// Set target field.
		setFolderField(folder);
	}

	/**
	 * Load data to export.
	 */
	private void loadDataToExport() {
		
		// Create and execute progress dialog.
		ProgressDialog<MiddleResult> progressDlg = new ProgressDialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textExportProgressTitle"),
				Resources.getString("org.multipage.generator.textExportLoadingData"));
		
		// Load area tree data.
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				Middle middle = ProgramBasic.getMiddle();
				Properties login = ProgramBasic.getLoginProperties();
				areaTreeData = new AreaTreeData();
				
				Long parentAreaId = parentArea != null ? parentArea.getId() : null;
				
				MiddleResult result = middle.loadAreaTreeData(login, area.getId(), parentAreaId,
						areaTreeData, this);
				return result;
			}
		});
		
		boolean exitDialog = false;
		
		MiddleResult result = progressDlg.getOutput();
		// On cancel exit.
		if (result == null) {
			exitDialog = true;
		}
		// On error exit.
		else if (result.isNotOK()) {
			result.show(this);
			exitDialog = true;
		}
		if (exitDialog) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					dispose();
				}
			});

			return;
		}
		// Set message information.
		String message = areaTreeData.getExportMessage();
		editorInfo.setText(message);
	}

	/**
	 * On export.
	 */
	protected void onExport() {
		
		final String folder = textFolder.getText();
		final String fileName = Utility.convertToFileName(textFileName.getText());
		
		// Check folder.
		if (!existsFolder(folder)) {
			Utility.show(this, "org.multipage.generator.messageExportFolderDoesntExist");
			return;
		}
		
		// Check if files already exist.
		File datFile = new File(folder + File.separator + fileName + ".dat");
		File xmlFile = new File(folder + File.separator + fileName + ".xml");
		
		if (datFile.exists() || xmlFile.exists()) {
			
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String lastModified = format.format(xmlFile.lastModified());
			
			if (!Utility.ask(this, "org.multipage.generator.messageExportFileExistsOverwriteIt", fileName, lastModified)) {
				return;
			}
		}
		
		// Create and execute progress dialog.
		ProgressDialog<MiddleResult> progressDlg = new ProgressDialog<MiddleResult>(this,
				Resources.getString("org.multipage.generator.textExportProgressTitle"),
				Resources.getString("org.multipage.generator.textExportingData"));
		
		progressDlg.execute(new SwingWorkerHelper<MiddleResult>() {
			@Override
			protected MiddleResult doBackgroundProcess() throws Exception {
				
				Middle middle = ProgramBasic.getMiddle();
				Properties login = ProgramBasic.getLoginProperties();
				
				// Export data.
				MiddleResult result = areaTreeData.export(middle, login, folder, fileName, this);
				if (result == MiddleResult.CANCELLATION) {
					throw new CancellationException();
				}

				return result;
			}
		});
				
		MiddleResult result = progressDlg.getOutput();
		// On error inform user.
		if (result != null && result.isNotOK()) {
			result.show(this);
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				dispose();
			}
		});
	}
}
