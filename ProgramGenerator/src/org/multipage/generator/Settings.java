/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.maclan.MiddleUtility;
import org.maclan.server.DebugListener;
import org.maclan.server.ProgramServlet;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.CallbackNoArg;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class Settings extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Settings.
	 */
	private static String resourcesRenderFolder;
	private static long maximumTextResourceSize;
	private static int extractedCharacters;
	private static boolean isRemovePartiallyRenderedPages;
	private static int httpPortNumber;
	private static boolean commonResourceFileNames;
	private static boolean enableDebugging;
	
	/**
	 * Set maximum text resource size.
	 */
	public static void setMaximumTextResSize(long size) {

		maximumTextResourceSize = size;
	}
	
	/**
	 * Get maximum text resource size.
	 */
	public static long getMaximumTextResSize() {
		
		return maximumTextResourceSize;
	}
	
	/**
	 * Static constructor.
	 */
	static {
		
		setDefaults();
	}
	
	/**
	 * Enable / disable debugging
	 * @param enable
	 */
	public static void setEnableDebugging(boolean enable) {
		
		enableDebugging = enable;
		
		// Switch on or off debugging of code
		DebugListener.setDebugPhpListener(new CallbackNoArg() {
			@Override
			public Object run() {
				return enableDebugging;
			}
		});
		
		// Enable @META tags in the area server.
		ProgramServlet.enableMetaTags(enable);
	}
	
	/**
	 * Returns true if debugging is enabled or false if not
	 * @return
	 */
	public static boolean getEnableDebugging() {
		
		return enableDebugging;
	}
	
	/**
	 * Set defaults.
	 */
	protected static void setDefaults() {
		
		resourcesRenderFolder = "";
		maximumTextResourceSize = 1048576;
		extractedCharacters = 100;
		isRemovePartiallyRenderedPages = false;
		httpPortNumber = 8080;
		commonResourceFileNames = false;
		enableDebugging = false;
	}

	/**
	 * On set defaults.
	 */
	protected void onDefaults() {

		if (!Utility.ask(this, "org.multipage.generator.messageRestoreDeafultSettings")) {
			return;
		}
		
		// Set controls.
		textResourcesRenderFolder.setText("");
		textMaxTextResourceSize.setText("1048576");
		textExtractCharacters.setText("100");
		textPortNumber.setText("80");
		//MiddleUtility.webInterfaceDir = "";
		//MiddleUtility.databaseAccess = "";
		commonResourceFileNames = false;
	}

	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
					throws IOException, ClassNotFoundException {
		
		// Read maximum text resource size.
		resourcesRenderFolder = inputStream.readUTF();
		maximumTextResourceSize = inputStream.readLong();
		extractedCharacters = inputStream.readInt();
		isRemovePartiallyRenderedPages = inputStream.readBoolean();
		httpPortNumber = inputStream.readInt();
		//MiddleUtility.webInterfaceDir = inputStream.readUTF();
		//MiddleUtility.databaseAccess = inputStream.readUTF();
		commonResourceFileNames = inputStream.readBoolean();
		enableDebugging = inputStream.readBoolean();
		setEnableDebugging(enableDebugging);  // This command sets a listener for servlet
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
					throws IOException {

		// Write maximum text resource size.
		outputStream.writeUTF(resourcesRenderFolder);
		outputStream.writeLong(maximumTextResourceSize);
		outputStream.writeInt(extractedCharacters);
		outputStream.writeBoolean(isRemovePartiallyRenderedPages);
		outputStream.writeInt(httpPortNumber);
		outputStream.writeBoolean(commonResourceFileNames);
		outputStream.writeBoolean(enableDebugging);
	}

	/**
	 * Set deafult data.
	 */
	public static void setDefaultData() {

		setDefaults();
	}

	/**
	 * Dialog components.
	 */
	private JButton buttonCancel;
	private JLabel labelMaxSizeOfTextResource;
	private TextFieldEx textMaxTextResourceSize;
	private JButton buttonOk;
	private JLabel labelBytes;
	private JButton buttonDefaults;
	private JLabel labelIndexExtractLength;
	private TextFieldEx textExtractCharacters;
	private JLabel labelCharacters;
	private JCheckBox checkRemovePartiallyRenderedPages;
	private JLabel labelHttpPortNumer;
	private JSeparator separator;
	private TextFieldEx textPortNumber;
	private TextFieldEx textWebInterfaceDirectory;
	private JLabel labelWebInterfaceDirectory;
	private JCheckBox checkCommonResourceFileNames;
	private JLabel labelResourcesRenderFolder;
	private TextFieldEx textResourcesRenderFolder;
	private JButton buttonWebInterface;
	private JLabel labelDatabaseAccess;
	private TextFieldEx textDatabaseAccess;
	private JButton buttonDatabaseAccess;
	private JLabel labelPhpEngineDirectory;
	private TextFieldEx textPhpEngineDirectory;
	private JButton buttonPhpEngine;

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		textResourcesRenderFolder.setText(resourcesRenderFolder);
		textMaxTextResourceSize.setText(String.valueOf(maximumTextResourceSize));
		textExtractCharacters.setText(String.valueOf(extractedCharacters));
		checkRemovePartiallyRenderedPages.setSelected(isRemovePartiallyRenderedPages);
		textPortNumber.setText(String.valueOf(httpPortNumber));
		textDatabaseAccess.setText(MiddleUtility.getDatabaseAccess());
		textPhpEngineDirectory.setText(MiddleUtility.getPhpDirectory());
		checkCommonResourceFileNames.setSelected(commonResourceFileNames);
		
		try {
			textWebInterfaceDirectory.setText(MiddleUtility.getWebInterfaceDirectory());
		}
		catch (Exception e) {
		}
	}

	/**
	 * Save dialog.
	 */
	private boolean saveDialog() {
						
		final long textResourceMaximum = 10 * 1048576;
		
		long number;
		String text;
		String canonicalPath;
		
		boolean restartRequired = false;
		
		// Save resources render folder.
		resourcesRenderFolder = textResourcesRenderFolder.getText();
		
		// Check maximum text resource size.
		text = textMaxTextResourceSize.getText();
		
		// Convert text to long value.
		try {
			number = Long.parseLong(text);
			if (number < 1 || number > textResourceMaximum) {
				Utility.show2(this, String.format(
						Resources.getString("org.multipage.generator.messageMaximumTextResourceSizeOutOfLimits"),
						1, textResourceMaximum));
				return false;
			}
		}
		catch (NumberFormatException e) {
			Utility.show(this, "org.multipage.generator.messageMaximumTextResourceFormatError");
			return false;
		}
				
		// Set value.
		setMaximumTextResSize(number);
		
		// Save number of extracted characters.
		int intNumber;
		text = textExtractCharacters.getText();
		try {
			intNumber = Integer.parseInt(text);
		}
		catch (NumberFormatException e) {
			Utility.show(this, "org.multipage.generator.messageExtractedCharactersFormatError");
			return false;
		}

		// Set value.
		setExtractedCharacters(intNumber);
		
		isRemovePartiallyRenderedPages = checkRemovePartiallyRenderedPages.isSelected();
		
		// Save port.
		int intPortNumber;
		text = textPortNumber.getText();
		try {
			intPortNumber = Integer.parseInt(text);
		}
		catch (NumberFormatException e) {
			Utility.show(this, "org.multipage.generator.messagePortNumberFormatError");
			return false;
		}
		
		if (!restartRequired) {
			restartRequired = (intPortNumber != httpPortNumber);
		}
		
		// Set value.
		httpPortNumber = intPortNumber;
		
		// Save web directory.
		boolean isOk = false;
		text = textWebInterfaceDirectory.getText();
		if (text.isEmpty()) {
			text = MiddleUtility.getDefaultWebInterfaceDirectory();
		}
		try {
			Path path = Paths.get(text);
			if (Files.exists(path)) {
				File dir = new File(path.toString());
				canonicalPath = dir.getCanonicalPath();
				if (!restartRequired) {
					restartRequired = (!canonicalPath.contentEquals(MiddleUtility.getWebInterfaceDirectory()));
				}
				MiddleUtility.setWebInterfaceDirectory(canonicalPath);
				isOk = true;
			}
		}
		catch (Exception e) {
		}
		if (!isOk && Utility.showConfirm(this, "org.multipage.generator.messageWebInterfaceDirectoryNotFound")) {
			MiddleUtility.setWebInterfaceDirectory(text);
		}
		
		// Save database access string.
		isOk = false;
		text = textDatabaseAccess.getText();
		if (text.isEmpty()) {
			text = MiddleUtility.getUserDirectory();
		}
		try {
			Path path = Paths.get(text);
			if (Files.exists(path)) {
				File dir = new File(path.toString());
				canonicalPath = dir.getCanonicalPath();
				boolean updateRequired = (!canonicalPath.contentEquals(MiddleUtility.getDatabaseAccess()));
				
				String newDirectory = dir.getCanonicalPath();
				MiddleUtility.setDatabaseAccess(newDirectory);
					
				if (updateRequired) {
					ProgramBasic.updateDatabaseAccess(newDirectory);
					ConditionalEvents.transmit(Settings.this, Signal.switchDatabase, newDirectory);
				}
				
				isOk = true;
			}
		}
		catch (Exception e) {
		}
		if (!isOk && Utility.showConfirm(this, "org.multipage.generator.messageDatabaseDirectoryNotFound")) {
			MiddleUtility.setDatabaseAccess(text);
		}
		
		// Save PHP directory string.
		isOk = false;
		text = textPhpEngineDirectory.getText();
		try {
			Path path = Paths.get(text);
			if (Files.exists(path)) {
				File dir = new File(path.toString());
				canonicalPath = dir.getCanonicalPath();
				if (!restartRequired) {
					restartRequired = (!canonicalPath.contentEquals(MiddleUtility.getPhpDirectory()));
				}
				MiddleUtility.setPhpDirectory(dir.getCanonicalPath());
				isOk = true;
			}
		}
		catch (Exception e) {
		}
		if (!isOk && Utility.showConfirm(this, "org.multipage.generator.messageDatabaseDirectoryNotFound")) {
			MiddleUtility.setDatabaseAccess(text);
		}
		
		commonResourceFileNames = checkCommonResourceFileNames.isSelected();
		
		// If application must restart, inform user.
		if (restartRequired) {
			MiddleUtility.saveServersProperties();
			Utility.show(this, "org.multipage.generator.messageRestartApplication");
		}
		
		return true;
	}

	/**
	 * Launch the dialog.
	 */
	public static void showDialog(JFrame parentFrame) {

		Settings dialog = new Settings(parentFrame);
		dialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 * @param parentFrame 
	 */
	public Settings(JFrame parentFrame) {
		super(parentFrame, true);
		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreation();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(330, 520));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.generator.textSettings");
		setBounds(100, 100, 330, 510);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMinimumSize(new Dimension(80, 25));
		buttonCancel.setMaximumSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		labelMaxSizeOfTextResource = new JLabel("org.multipage.generator.textMaximumSizeOfTextResource");
		springLayout.putConstraint(SpringLayout.WEST, labelMaxSizeOfTextResource, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelMaxSizeOfTextResource, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelMaxSizeOfTextResource);
		
		textMaxTextResourceSize = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textMaxTextResourceSize, 3, SpringLayout.SOUTH, labelMaxSizeOfTextResource);
		springLayout.putConstraint(SpringLayout.WEST, textMaxTextResourceSize, 10, SpringLayout.WEST, getContentPane());
		textMaxTextResourceSize.setHorizontalAlignment(SwingConstants.RIGHT);
		getContentPane().add(textMaxTextResourceSize);
		textMaxTextResourceSize.setColumns(15);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMinimumSize(new Dimension(80, 25));
		buttonOk.setMaximumSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelBytes = new JLabel("org.multipage.generator.textBytes");
		springLayout.putConstraint(SpringLayout.NORTH, labelBytes, 0, SpringLayout.NORTH, textMaxTextResourceSize);
		springLayout.putConstraint(SpringLayout.WEST, labelBytes, 6, SpringLayout.EAST, textMaxTextResourceSize);
		springLayout.putConstraint(SpringLayout.SOUTH, labelBytes, 0, SpringLayout.SOUTH, textMaxTextResourceSize);
		getContentPane().add(labelBytes);
		
		buttonDefaults = new JButton("org.multipage.generator.textRestoreDefaults");
		buttonDefaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDefaults();
			}
		});
		springLayout.putConstraint(SpringLayout.WEST, buttonDefaults, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, buttonDefaults, -10, SpringLayout.SOUTH, getContentPane());
		buttonDefaults.setPreferredSize(new Dimension(80, 25));
		buttonDefaults.setMinimumSize(new Dimension(80, 25));
		buttonDefaults.setMaximumSize(new Dimension(80, 25));
		buttonDefaults.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonDefaults);
		
		labelIndexExtractLength = new JLabel("org.multipage.generator.textIndexFilePageExtractLength");
		springLayout.putConstraint(SpringLayout.NORTH, labelIndexExtractLength, 10, SpringLayout.SOUTH, textMaxTextResourceSize);
		springLayout.putConstraint(SpringLayout.WEST, labelIndexExtractLength, 0, SpringLayout.WEST, labelMaxSizeOfTextResource);
		getContentPane().add(labelIndexExtractLength);
		
		textExtractCharacters = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textExtractCharacters, 3, SpringLayout.SOUTH, labelIndexExtractLength);
		springLayout.putConstraint(SpringLayout.WEST, textExtractCharacters, 10, SpringLayout.WEST, getContentPane());
		textExtractCharacters.setHorizontalAlignment(SwingConstants.RIGHT);
		springLayout.putConstraint(SpringLayout.EAST, textExtractCharacters, 0, SpringLayout.EAST, textMaxTextResourceSize);
		getContentPane().add(textExtractCharacters);
		textExtractCharacters.setColumns(10);
		
		labelCharacters = new JLabel("org.multipage.generator.textCharacters");
		springLayout.putConstraint(SpringLayout.NORTH, labelCharacters, 6, SpringLayout.SOUTH, labelIndexExtractLength);
		springLayout.putConstraint(SpringLayout.WEST, labelCharacters, 0, SpringLayout.WEST, labelBytes);
		getContentPane().add(labelCharacters);
		
		checkRemovePartiallyRenderedPages = new JCheckBox("org.multipage.generator.textRemovePartiallyRenderedPages");
		springLayout.putConstraint(SpringLayout.NORTH, checkRemovePartiallyRenderedPages, 10, SpringLayout.SOUTH, textExtractCharacters);
		springLayout.putConstraint(SpringLayout.WEST, checkRemovePartiallyRenderedPages, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(checkRemovePartiallyRenderedPages);
		
		labelHttpPortNumer = new JLabel("org.multipage.generator.textHttpPortNumber");
		springLayout.putConstraint(SpringLayout.WEST, labelHttpPortNumer, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelHttpPortNumer);
		
		separator = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, labelMaxSizeOfTextResource, 20, SpringLayout.SOUTH, separator);
		springLayout.putConstraint(SpringLayout.EAST, separator, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, separator, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(separator);
		
		textPortNumber = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textPortNumber, 3, SpringLayout.SOUTH, labelHttpPortNumer);
		springLayout.putConstraint(SpringLayout.WEST, textPortNumber, 10, SpringLayout.WEST, getContentPane());
		textPortNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		textPortNumber.setColumns(5);
		getContentPane().add(textPortNumber);
		
		labelWebInterfaceDirectory = new JLabel("org.multipage.generator.textWebInterfaceDirectory");
		springLayout.putConstraint(SpringLayout.WEST, labelWebInterfaceDirectory, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelWebInterfaceDirectory);
		
		textWebInterfaceDirectory = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWebInterfaceDirectory, 3, SpringLayout.SOUTH, labelWebInterfaceDirectory);
		springLayout.putConstraint(SpringLayout.WEST, textWebInterfaceDirectory, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(textWebInterfaceDirectory);
		textWebInterfaceDirectory.setColumns(10);
		
		checkCommonResourceFileNames = new JCheckBox("org.multipage.generator.textCommonResources");
		springLayout.putConstraint(SpringLayout.WEST, checkCommonResourceFileNames, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, separator, 20, SpringLayout.SOUTH, checkCommonResourceFileNames);
		getContentPane().add(checkCommonResourceFileNames);
		
		labelResourcesRenderFolder = new JLabel("org.multipage.generator.textResourcesRenderFolder");
		springLayout.putConstraint(SpringLayout.NORTH, labelResourcesRenderFolder, 10, SpringLayout.SOUTH, textPortNumber);
		springLayout.putConstraint(SpringLayout.WEST, labelResourcesRenderFolder, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelResourcesRenderFolder);
		
		textResourcesRenderFolder = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textResourcesRenderFolder, 3, SpringLayout.SOUTH, labelResourcesRenderFolder);
		springLayout.putConstraint(SpringLayout.EAST, textResourcesRenderFolder, -10, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, checkCommonResourceFileNames, 6, SpringLayout.SOUTH, textResourcesRenderFolder);
		springLayout.putConstraint(SpringLayout.WEST, textResourcesRenderFolder, 10, SpringLayout.WEST, getContentPane());
		textResourcesRenderFolder.setColumns(10);
		getContentPane().add(textResourcesRenderFolder);
		
		buttonWebInterface = new JButton("");
		springLayout.putConstraint(SpringLayout.NORTH, buttonWebInterface, 0, SpringLayout.NORTH, textWebInterfaceDirectory);
		springLayout.putConstraint(SpringLayout.EAST, buttonWebInterface, -6, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textWebInterfaceDirectory, 0, SpringLayout.WEST, buttonWebInterface);
		buttonWebInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onWebInterface();
			}
		});
		buttonWebInterface.setPreferredSize(new Dimension(20, 20));
		getContentPane().add(buttonWebInterface);
		
		labelDatabaseAccess = new JLabel("org.multipage.generator.textDatabaseAccess");
		springLayout.putConstraint(SpringLayout.NORTH, labelDatabaseAccess, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelDatabaseAccess, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelDatabaseAccess);
		
		textDatabaseAccess = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelWebInterfaceDirectory, 10, SpringLayout.SOUTH, textDatabaseAccess);
		springLayout.putConstraint(SpringLayout.NORTH, textDatabaseAccess, 3, SpringLayout.SOUTH, labelDatabaseAccess);
		springLayout.putConstraint(SpringLayout.WEST, textDatabaseAccess, 10, SpringLayout.WEST, getContentPane());
		textDatabaseAccess.setColumns(10);
		getContentPane().add(textDatabaseAccess);
		
		buttonDatabaseAccess = new JButton("");
		buttonDatabaseAccess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDatabaseAccess();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textDatabaseAccess, 0, SpringLayout.WEST, buttonDatabaseAccess);
		springLayout.putConstraint(SpringLayout.NORTH, buttonDatabaseAccess, 0, SpringLayout.NORTH, textDatabaseAccess);
		springLayout.putConstraint(SpringLayout.EAST, buttonDatabaseAccess, -6, SpringLayout.EAST, getContentPane());
		buttonDatabaseAccess.setPreferredSize(new Dimension(20, 20));
		getContentPane().add(buttonDatabaseAccess);
		
		labelPhpEngineDirectory = new JLabel("org.multipage.generator.textPhpEngineDirectory");
		springLayout.putConstraint(SpringLayout.NORTH, labelPhpEngineDirectory, 10, SpringLayout.SOUTH, textWebInterfaceDirectory);
		springLayout.putConstraint(SpringLayout.WEST, labelPhpEngineDirectory, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelPhpEngineDirectory);
		
		textPhpEngineDirectory = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, labelHttpPortNumer, 10, SpringLayout.SOUTH, textPhpEngineDirectory);
		springLayout.putConstraint(SpringLayout.NORTH, textPhpEngineDirectory, 3, SpringLayout.SOUTH, labelPhpEngineDirectory);
		springLayout.putConstraint(SpringLayout.WEST, textPhpEngineDirectory, 10, SpringLayout.WEST, getContentPane());
		textPhpEngineDirectory.setColumns(10);
		getContentPane().add(textPhpEngineDirectory);
		
		buttonPhpEngine = new JButton("");
		buttonPhpEngine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPhpEngine();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textPhpEngineDirectory, 0, SpringLayout.WEST, buttonPhpEngine);
		springLayout.putConstraint(SpringLayout.NORTH, buttonPhpEngine, 0, SpringLayout.NORTH, textPhpEngineDirectory);
		springLayout.putConstraint(SpringLayout.EAST, buttonPhpEngine, -6, SpringLayout.EAST, getContentPane());
		buttonPhpEngine.setPreferredSize(new Dimension(20, 20));
		getContentPane().add(buttonPhpEngine);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Localize component.
		localize();
		// Set icons.
		setIcons();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Load values.
		loadDialog();
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelMaxSizeOfTextResource);
		Utility.localize(labelBytes);
		Utility.localize(buttonDefaults);
		Utility.localize(labelIndexExtractLength);
		Utility.localize(labelCharacters);
		Utility.localize(checkRemovePartiallyRenderedPages);
		Utility.localize(labelHttpPortNumer);
		Utility.localize(labelWebInterfaceDirectory);
		Utility.localize(checkCommonResourceFileNames);
		Utility.localize(labelResourcesRenderFolder);
		Utility.localize(labelDatabaseAccess);
		Utility.localize(labelPhpEngineDirectory);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonWebInterface.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonDatabaseAccess.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonPhpEngine.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * On web interface localization button event.
	 */
	protected void onWebInterface() {
		
		String webInterfaceDirectory = Utility.chooseDirectory2(this, "org.multipage.generator.titleWebInterfaceDirectory");
		if (webInterfaceDirectory == null) {
			return;
		}
		
		textWebInterfaceDirectory.setText(webInterfaceDirectory);
	}
	
	/**
	 * On database access button event.
	 */
	protected void onDatabaseAccess() {
		
		// On Derby database.
		if ("org.multipage.derby".equals(MiddleUtility.getPathToMiddle())) {
			
			// Select directory.
			String databaseDirectory = Utility.chooseDirectory2(this, "org.multipage.generator.titleDatabaseDirectory");
			if (databaseDirectory == null) {
				return;
			}
			
			textDatabaseAccess.setText(databaseDirectory);
		}
		// On PostgreSQL database.
		else if ("org.maclan.postgresql".equals(MiddleUtility.getPathToMiddle())) {
			
			
		}
	}
	
	/**
	 * On PHP engine.
	 */
	protected void onPhpEngine() {
		
		String phpDirectory = Utility.chooseDirectory2(this, "org.multipage.generator.titlePhpDirectory");
		if (phpDirectory == null) {
			return;
		}
		
		textPhpEngineDirectory.setText(phpDirectory);
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {

		// Save settings.
		if (saveDialog()) {
		
			dispose();
		}
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		dispose();
	}
	
	/**
	 * @return
	 */
	public static String getResourcesRenderFolder() {
		return resourcesRenderFolder;
	}
	
	/**
	 * @return the extractedCharacters
	 */
	public static int getExtractedCharacters() {
		return extractedCharacters;
	}

	/**
	 * @param extractedCharacters the extractedCharacters to set
	 */
	public static void setExtractedCharacters(int extractedCharacters) {
		Settings.extractedCharacters = extractedCharacters;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isRemovePartiallyGenerated() {
		
		return isRemovePartiallyRenderedPages;
	}

	/**
	 * Get port number.
	 * @return
	 */
	public static int getHttpPortNumber() {
		
		return httpPortNumber;
	}
	
	/**
	 * Get common resource file names.
	 * @return
	 */
	public static boolean getCommonResourceFileNamesFlag() {
		
		return commonResourceFileNames;
	}
}
