/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.maclan.MiddleUtility;
import org.maclan.server.DebugListener;
import org.maclan.server.ProgramServlet;
import org.multipage.addinloader.AddInLoader;
import org.multipage.addins.ProgramAddIns;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.CallbackNoArg;
import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
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
	 * Show tab constants.
	 */
	public static final int NOT_SET = -1;
	public static final int GENERAL = 0;
	public static final int ADD_INS = 1;
	
	/**
	 * Settings dialog singleton.
	 */
	private static Settings dialog = null;
	
	/**
	 * Settings.
	 */
	private static String resourcesRenderFolder;
	private static long maximumTextResourceSize;
	private static int extractedCharacters;
	private static boolean isRemovePartiallyRenderedPages;
	private static int httpPortNumber;
	private static boolean commonResourceFileNames;
	private static boolean enableDebugging = false;
	private static double animationDuration;
	
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
		
		// Set servlet listener that can check if debugging is enabled.
		ProgramServlet.setDebuggingEnabledListener(() -> enableDebugging);
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
		animationDuration = 3.0;
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
		textPortNumber.setText("8080");
		//MiddleUtility.webInterfaceDir = "";
		//MiddleUtility.databaseAccess = "";
		commonResourceFileNames = false;
		sliderAnimationDuration.setValue((int) animationDuration);
	}
	
	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
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
		setEnableDebugging(false);  // This command sets a listener for servlet
		animationDuration = inputStream.readDouble();
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
					throws IOException {

		// Write maximum text resource size.
		outputStream.writeUTF(resourcesRenderFolder);
		outputStream.writeLong(maximumTextResourceSize);
		outputStream.writeInt(extractedCharacters);
		outputStream.writeBoolean(isRemovePartiallyRenderedPages);
		outputStream.writeInt(httpPortNumber);
		outputStream.writeBoolean(commonResourceFileNames);
		outputStream.writeBoolean(enableDebugging);
		outputStream.writeDouble(animationDuration);
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		setDefaults();
	}

	/**
	 * Dialog components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JButton buttonDefaults;
	private JTabbedPane tabbedPane;
	private JLabel labelWebInterfaceDirectory;
	private TextFieldEx textWebInterfaceDirectory;
	private JLabel labelPhpEngineDirectory;
	private TextFieldEx textPhpEngineDirectory;
	private JButton buttonPhpEngine;
	private JLabel labelHttpPortNumer;
	private TextFieldEx textPortNumber;
	private JLabel labelResourcesRenderFolder;
	private TextFieldEx textResourcesRenderFolder;
	private JCheckBox checkCommonResourceFileNames;
	private JLabel labelAnimationDuration;
	private JSlider sliderAnimationDuration;
	private JSeparator separator;
	private JLabel labelMaxSizeOfTextResource;
	private TextFieldEx textMaxTextResourceSize;
	private JLabel labelBytes;
	private JLabel labelIndexExtractLength;
	private TextFieldEx textExtractCharacters;
	private JLabel labelCharacters;
	private JCheckBox checkRemovePartiallyRenderedPages;
	private JLabel labelDatabaseAccess;
	private TextFieldEx textDatabaseAccess;
	private JButton buttonDatabaseAccess;
	private JButton buttonWebInterface;
	private JButton buttonResourcesFolder;
	private JToolBar toolBar;
	private JSeparator separator_1;
	private JSeparator separator_2;

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
		sliderAnimationDuration.setValue((int) animationDuration);
		
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
			MiddleUtility.saveServerProperties();
			Utility.show(this, "org.multipage.generator.messageRestartApplication");
		}
		
		// Get current value of animation duration.
		animationDuration = (int) sliderAnimationDuration.getValue();
		
		return true;
	}

	/**
	 * Launch the dialog.
	 * @param parent
	 */
	public static void showDialog(Component parent) {

		showDialog(parent, Settings.NOT_SET);
	}
	
	/**
	 * Launch the dialog.
	 * @param parent
	 * @param selectTab
	 */
	public static void showDialog(Component parent, int selectTab) {
		
		// Show the dialog.
		if (dialog == null) {
			dialog = new Settings(parent);
		}
		
		dialog.selectTab(selectTab);
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public Settings(Component parent) {
		super(Utility.findWindow(parent), ModalityType.MODELESS);
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
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.generator.textSettings");
		setBounds(100, 100, 726, 690);
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
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.NORTH, buttonOk);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -10, SpringLayout.EAST, getContentPane());
		tabbedPane.setPreferredSize(new Dimension(5, 600));
		tabbedPane.setSize(new Dimension(0, 600));
		getContentPane().add(tabbedPane);
		
		JPanel panelGeneral = new JPanel();
		panelGeneral.setBackground(Color.WHITE);
		tabbedPane.addTab("org.multipage.generator.textGeneralSettings", null, panelGeneral, null);
		SpringLayout sl_panelGeneral = new SpringLayout();
		panelGeneral.setLayout(sl_panelGeneral);
		
		labelDatabaseAccess = new JLabel("org.multipage.generator.textDatabaseAccess");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelDatabaseAccess, 10, SpringLayout.NORTH, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelDatabaseAccess, 10, SpringLayout.WEST, panelGeneral);
		panelGeneral.add(labelDatabaseAccess);
		
		textDatabaseAccess = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textDatabaseAccess, 6, SpringLayout.SOUTH, labelDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textDatabaseAccess, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(textDatabaseAccess);
		
		buttonDatabaseAccess = new JButton("");
		buttonDatabaseAccess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDatabaseAccess();
			}
		});
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textDatabaseAccess, 0, SpringLayout.WEST, buttonDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, buttonDatabaseAccess, 0, SpringLayout.NORTH, textDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, buttonDatabaseAccess, -10, SpringLayout.EAST, panelGeneral);
		buttonDatabaseAccess.setPreferredSize(new Dimension(20, 20));
		panelGeneral.add(buttonDatabaseAccess);
		
		labelWebInterfaceDirectory = new JLabel("org.multipage.generator.textWebInterfaceDirectory");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelWebInterfaceDirectory, 15, SpringLayout.SOUTH, textDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelWebInterfaceDirectory, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelWebInterfaceDirectory);
		
		textWebInterfaceDirectory = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textWebInterfaceDirectory, 6, SpringLayout.SOUTH, labelWebInterfaceDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textWebInterfaceDirectory, 0, SpringLayout.WEST, labelDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textWebInterfaceDirectory, 0, SpringLayout.EAST, textDatabaseAccess);
		panelGeneral.add(textWebInterfaceDirectory);
		
		buttonWebInterface = new JButton("");
		buttonWebInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onWebInterface();
			}
		});
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, buttonWebInterface, 6, SpringLayout.SOUTH, labelWebInterfaceDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, buttonWebInterface, 0, SpringLayout.EAST, buttonDatabaseAccess);
		buttonWebInterface.setPreferredSize(new Dimension(20, 20));
		panelGeneral.add(buttonWebInterface);
		
		labelPhpEngineDirectory = new JLabel("org.multipage.generator.textPhpEngineDirectory");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelPhpEngineDirectory, 19, SpringLayout.SOUTH, textWebInterfaceDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelPhpEngineDirectory, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelPhpEngineDirectory);
		
		textPhpEngineDirectory = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textPhpEngineDirectory, 6, SpringLayout.SOUTH, labelPhpEngineDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textPhpEngineDirectory, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(textPhpEngineDirectory);
		
		buttonPhpEngine = new JButton("");
		buttonPhpEngine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPhpEngine();
			}
		});
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textPhpEngineDirectory, 0, SpringLayout.WEST, buttonPhpEngine);
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, buttonPhpEngine, 0, SpringLayout.NORTH, textPhpEngineDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, buttonPhpEngine, 0, SpringLayout.WEST, buttonDatabaseAccess);
		buttonPhpEngine.setPreferredSize(new Dimension(20, 20));
		panelGeneral.add(buttonPhpEngine);
		
		labelHttpPortNumer = new JLabel("org.multipage.generator.textHttpPortNumber");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelHttpPortNumer, 23, SpringLayout.SOUTH, textPhpEngineDirectory);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelHttpPortNumer, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelHttpPortNumer);
		
		textPortNumber = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textPortNumber, 6, SpringLayout.SOUTH, labelHttpPortNumer);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textPortNumber, 0, SpringLayout.WEST, labelDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textPortNumber, 50, SpringLayout.WEST, labelHttpPortNumer);
		textPortNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		panelGeneral.add(textPortNumber);
		
		labelResourcesRenderFolder = new JLabel("org.multipage.generator.textResourcesRenderFolder");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelResourcesRenderFolder, 16, SpringLayout.SOUTH, textPortNumber);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelResourcesRenderFolder, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelResourcesRenderFolder);
		
		textResourcesRenderFolder = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textResourcesRenderFolder, 6, SpringLayout.SOUTH, labelResourcesRenderFolder);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textResourcesRenderFolder, 10, SpringLayout.WEST, panelGeneral);
		panelGeneral.add(textResourcesRenderFolder);
		
		checkCommonResourceFileNames = new JCheckBox("org.multipage.generator.textCommonResources");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, checkCommonResourceFileNames, 10, SpringLayout.SOUTH, textResourcesRenderFolder);
		checkCommonResourceFileNames.setBackground(Color.WHITE);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, checkCommonResourceFileNames, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(checkCommonResourceFileNames);
		
		labelAnimationDuration = new JLabel("org.multipage.generator.textAnimationDelta");
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelAnimationDuration, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelAnimationDuration);
		
		sliderAnimationDuration = new JSlider();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, sliderAnimationDuration, 6, SpringLayout.SOUTH, labelAnimationDuration);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, sliderAnimationDuration, 10, SpringLayout.WEST, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, sliderAnimationDuration, 0, SpringLayout.EAST, buttonDatabaseAccess);
		sliderAnimationDuration.setBackground(Color.WHITE);
		sliderAnimationDuration.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onAnimationDurationChange();
			}
		});
		sliderAnimationDuration.setValue(3);
		sliderAnimationDuration.setSnapToTicks(true);
		sliderAnimationDuration.setPaintTicks(true);
		sliderAnimationDuration.setPaintLabels(true);
		sliderAnimationDuration.setMinorTickSpacing(1);
		sliderAnimationDuration.setMinimum(1);
		sliderAnimationDuration.setMaximum(5);
		sliderAnimationDuration.setMajorTickSpacing(1);
		panelGeneral.add(sliderAnimationDuration);
		
		separator = new JSeparator();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelAnimationDuration, 17, SpringLayout.SOUTH, separator);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, separator, 10, SpringLayout.WEST, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, separator, 0, SpringLayout.EAST, buttonDatabaseAccess);
		panelGeneral.add(separator);
		
		labelMaxSizeOfTextResource = new JLabel("org.multipage.generator.textMaximumSizeOfTextResource");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelMaxSizeOfTextResource, 10, SpringLayout.SOUTH, checkCommonResourceFileNames);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelMaxSizeOfTextResource, 10, SpringLayout.WEST, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, labelMaxSizeOfTextResource, -10, SpringLayout.EAST, panelGeneral);
		panelGeneral.add(labelMaxSizeOfTextResource);
		
		textMaxTextResourceSize = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textMaxTextResourceSize, 6, SpringLayout.SOUTH, labelMaxSizeOfTextResource);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textMaxTextResourceSize, 10, SpringLayout.WEST, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textMaxTextResourceSize, 100, SpringLayout.WEST, panelGeneral);
		textMaxTextResourceSize.setHorizontalAlignment(SwingConstants.RIGHT);
		panelGeneral.add(textMaxTextResourceSize);
		
		labelBytes = new JLabel("org.multipage.generator.textBytes");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelBytes, 6, SpringLayout.SOUTH, labelMaxSizeOfTextResource);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelBytes, 6, SpringLayout.EAST, textMaxTextResourceSize);
		panelGeneral.add(labelBytes);
		
		labelIndexExtractLength = new JLabel("org.multipage.generator.textIndexFilePageExtractLength");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelIndexExtractLength, 10, SpringLayout.SOUTH, textMaxTextResourceSize);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelIndexExtractLength, 0, SpringLayout.WEST, labelDatabaseAccess);
		panelGeneral.add(labelIndexExtractLength);
		
		textExtractCharacters = new TextFieldEx();
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, textExtractCharacters, 6, SpringLayout.SOUTH, labelIndexExtractLength);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, textExtractCharacters, 0, SpringLayout.WEST, labelDatabaseAccess);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textExtractCharacters, 100, SpringLayout.WEST, panelGeneral);
		textExtractCharacters.setHorizontalAlignment(SwingConstants.RIGHT);
		panelGeneral.add(textExtractCharacters);
		
		labelCharacters = new JLabel("org.multipage.generator.textCharacters");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, labelCharacters, 6, SpringLayout.SOUTH, labelIndexExtractLength);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, labelCharacters, 6, SpringLayout.EAST, textExtractCharacters);
		panelGeneral.add(labelCharacters);
		
		checkRemovePartiallyRenderedPages = new JCheckBox("org.multipage.generator.textRemovePartiallyRenderedPages");
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, checkRemovePartiallyRenderedPages, 10, SpringLayout.SOUTH, textExtractCharacters);
		sl_panelGeneral.putConstraint(SpringLayout.WEST, checkRemovePartiallyRenderedPages, 10, SpringLayout.WEST, panelGeneral);
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, separator, 16, SpringLayout.SOUTH, checkRemovePartiallyRenderedPages);
		checkRemovePartiallyRenderedPages.setBackground(Color.WHITE);
		panelGeneral.add(checkRemovePartiallyRenderedPages);
		
		buttonResourcesFolder = new JButton("");
		buttonResourcesFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onResourcesFolder();
			}
		});
		sl_panelGeneral.putConstraint(SpringLayout.EAST, textResourcesRenderFolder, 0, SpringLayout.WEST, buttonResourcesFolder);
		sl_panelGeneral.putConstraint(SpringLayout.NORTH, buttonResourcesFolder, 6, SpringLayout.SOUTH, labelResourcesRenderFolder);
		sl_panelGeneral.putConstraint(SpringLayout.EAST, buttonResourcesFolder, 0, SpringLayout.EAST, buttonDatabaseAccess);
		buttonResourcesFolder.setPreferredSize(new Dimension(20, 20));
		panelGeneral.add(buttonResourcesFolder);
		
		JPanel panelAddIn = new JPanel();
		panelAddIn.setBorder(null);
		tabbedPane.addTab("org.multipage.generator.textAddIns", null, panelAddIn, null);
		panelAddIn.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		panelAddIn.add(scrollPane);
		
		JList listAddIns = new JList();
		listAddIns.setBorder(null);
		scrollPane.setViewportView(listAddIns);
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panelAddIn.add(toolBar, BorderLayout.NORTH);
		
		JButton buttonLoadAddIn = new JButton("org.multipage.generator.menuLoadAddIn");
		buttonLoadAddIn.setMargin(new Insets(2, 2, 2, 2));
		buttonLoadAddIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLoadAddIn();
			}
		});
		toolBar.add(buttonLoadAddIn);
		
		JButton buttonRemoveAddIn = new JButton("org.multipage.generator.menuRemoveAddIn");
		buttonRemoveAddIn.setMargin(new Insets(2, 2, 2, 2));
		buttonRemoveAddIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveAddIn();
			}
		});
		
		separator_1 = new JSeparator();
		separator_1.setMaximumSize(new Dimension(3, 32767));
		separator_1.setPreferredSize(new Dimension(0, 0));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_1);
		toolBar.add(buttonRemoveAddIn);
		
		separator_2 = new JSeparator();
		separator_2.setMaximumSize(new Dimension(3, 32767));
		separator_2.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_2);
		
		JButton buttonSignAddIn = new JButton("org.multipage.generator.menuAddInSigner");
		buttonSignAddIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSignAddIn();
			}
		});
		buttonSignAddIn.setMargin(new Insets(2, 2, 2, 2));
		toolBar.add(buttonSignAddIn);
	}
	
	/**
	 * O change of the animation duration.
	 */
	protected void onAnimationDurationChange() {
		
		// Get current value of animation duration.
		boolean isAdjusting = sliderAnimationDuration.getValueIsAdjusting();
		if (isAdjusting) {
			
			animationDuration = (int) sliderAnimationDuration.getValue();
		}
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
		Utility.localize(tabbedPane);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(toolBar);
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
		Utility.localize(labelAnimationDuration);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonWebInterface.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonDatabaseAccess.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonPhpEngine.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonResourcesFolder.setIcon(Images.getIcon("org/multipage/generator/images/folder.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * Select tab.
	 * @param selectTab
	 */
	private void selectTab(int selectTab) {
		
		int tabCount = tabbedPane.getTabCount();
		
		if (selectTab >= 0 && selectTab < tabCount) {
			tabbedPane.setSelectedIndex(selectTab);
		}
	}
	
	/**
	 * Get animation duration.
	 * @return
	 */
	public static double getAnimationDuration() {
		
		return animationDuration;
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
	 * On resources folder.
	 */
	private void onResourcesFolder() {
		
		String resourcesFolder = Utility.chooseDirectory2(this, "org.multipage.generator.titleResourcesDirectory");
		if (resourcesFolder == null) {
			return;
		}
		
		textResourcesRenderFolder.setText(resourcesFolder);
	}
	
	/**
	 * On load add-in.
	 */
	protected void onLoadAddIn() {
		
		// Get file name.
		String [][] filters = {{"org.multipage.generator.textAddInJarFilesFilter", "jar"}};
		
		File addInJarFile = Utility.chooseFileToOpen(this, filters);
		if (addInJarFile == null) {
			return;
		}
		
		// Get loader package.
		Class<?> loaderClass = AddInLoader.class;
		String className = loaderClass.getSimpleName();
		String jarFileName = className + ".jar";
		Package thePackage = loaderClass.getPackage();
		
		try {
			// Create unique temporary folder for saving temporary JAR file.
			Path temporaryPath = Files.createTempDirectory("ProgramGenerator_");
			
			// Create temporary JAR file.
			File temporaryJarFile = Paths.get(temporaryPath.toString(), jarFileName).toFile();
			
			// Find out if this application is zipped in JAR file.
			boolean isApplicationZipped = Utility.isApplicationZipped();
			
			// Get path to ProgramAddins project.
			String applicationPath = Utility.getApplicationPath(ProgramAddIns.class);
			
			String workingDirectory = "";
			
			// On JAR file.
			if (isApplicationZipped) {
				
				// Import JAR package classes to output JAR file.
				Utility.importJarPackageToJarFile(applicationPath, thePackage, temporaryJarFile, thePackage);
				
				workingDirectory = Paths.get(applicationPath).getParent().toString();
			}
			// On a directory with classes.
			else {
				
				// Import directory classes to output JAR file.
				Utility.importDirectoryClassesToJarFile(applicationPath, thePackage, temporaryJarFile);
			}
			
			// Try to run the Add-In loader.
			if (temporaryJarFile != null) {
				
				Utility.runExecutableJar(temporaryPath.toString(), temporaryJarFile.toString(), new String [] {
											"addInJarFile=" + addInJarFile.toString(),
											"applicationFile=" +  applicationPath,
											"applicationWorkingDirectory=" + workingDirectory});
				
				// Terminate the application.
				ConditionalEvents.transmit(this, Signal.terminate);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * On remove add-in.
	 * 
	 */
	protected void onRemoveAddIn() {
		
	}
	
	/**
	 * On sign Add-in classes placed in Add-in JAR file.
	 */
	protected void onSignAddIn() {
		
		// Find add-in JAR file on disk.
		File addInJarFile = Utility.chooseFileToOpen(this, new String [][] {{"org.multipage.generator.textAddInJarFilesFilter", "jar"}});
		if (addInJarFile == null) {
			return;
		}
		
		SignAddInDialog.showDialog(this, addInJarFile.toString());
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
	public static boolean partiallyGeneratedRemoved() {
		
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
