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
import java.awt.Font;
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
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import org.maclan.Language;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.VersionObj;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.translator.LanguageRenderer;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
public class DisplayOnlineDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * States.
	 */
	private static Rectangle bounds;
	private static String parametersOrUrlState;
	private static long languageIdState;
	private static long versionIdState;
	private static boolean showIdsState;
	private static boolean externalBrowserState;
	private static String [] parametersOrUrlRecentState;
	
	private static String defaultUrl;
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		
		parametersOrUrlState = "";
		parametersOrUrlRecentState = new String [] { };
		languageIdState = 0L;
		versionIdState = 0L;
		showIdsState = false;
		externalBrowserState = false;
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream) 
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		
		parametersOrUrlState = inputStream.readUTF();
		parametersOrUrlRecentState = Utility.readInputStreamObject(inputStream, String [].class);
		languageIdState = inputStream.readLong();
		versionIdState = inputStream.readLong();
		showIdsState = inputStream.readBoolean();
		externalBrowserState = inputStream.readBoolean();
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
		
		outputStream.writeUTF(parametersOrUrlState);
		outputStream.writeObject(parametersOrUrlRecentState);
		outputStream.writeLong(languageIdState);
		outputStream.writeLong(versionIdState);
		outputStream.writeBoolean(showIdsState);
		outputStream.writeBoolean(externalBrowserState);
	}

	/**
	 * Confirm dialog.
	 */
	private boolean confirm = false;
	
	/**
	 * Start language ID.
	 */
	private long startLanguageId;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JPanel panelMain;
	private JLabel labelSelectLanguage;
	private JComboBox comboBoxLanguages;
	private JLabel labelSelectVersion;
	private JComboBox comboBoxVersions;
	private JPanel panel_1;
	private JCheckBox checkShowIds;
	private Component horizontalGlue;
	private Component horizontalGlue_1;
	private JLabel labelDisplayUrl;
	private JComboBox comboParametersOrUrl;
	private JCheckBox checkExternalBrowser;
	private Component horizontalStrut;
	private JButton buttonRemoveUrl;

	/**
	 * Show dialog.
	 * @param parent
	 * @param showTextIds 
	 * @param version 
	 * @param language 
	 * @param parametersOrUrl 
	 * @param externalBrowser 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, Obj<Language> language,
			Obj<VersionObj> version, Obj<Boolean> showTextIds, Obj<String> parametersOrUrl, Obj<Boolean> externalBrowser) {
		
		defaultUrl = "http://localhost:" + Settings.getHttpPortNumber();
		
		DisplayOnlineDialog dialog = new DisplayOnlineDialog(Utility.findWindow(parent));
		
		dialog.setVisible(true);
		
		// Set output.
		if (dialog.confirm) {
			language.ref = dialog.getSelectedLanguage();
			version.ref = dialog.getSelectedVersion();
			showTextIds.ref = dialog.checkShowIds.isSelected();
			parametersOrUrl.ref = Utility.getComboBoxText(dialog.comboParametersOrUrl);
			externalBrowser.ref = dialog.checkExternalBrowser.isSelected();
		}
		
		return dialog.confirm;
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public DisplayOnlineDialog(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(420, 480));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("org.multipage.generator.textDisplayAreaOnlineDialog");
		
		setBounds(100, 100, 420, 459);
		
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
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelSelectLanguage = new JLabel("org.multipage.generator.textSelectLanguage");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSelectLanguage, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelSelectLanguage);
		
		comboBoxLanguages = new JComboBox();
		comboBoxLanguages.setPreferredSize(new Dimension(28, 80));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxLanguages, 6, SpringLayout.SOUTH, labelSelectLanguage);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxLanguages, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, comboBoxLanguages, -10, SpringLayout.EAST, panelMain);
		panelMain.add(comboBoxLanguages);
		
		labelSelectVersion = new JLabel("org.multipage.generator.textSelectVersion2");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSelectVersion, 16, SpringLayout.SOUTH, comboBoxLanguages);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSelectVersion, 0, SpringLayout.WEST, labelSelectLanguage);
		panelMain.add(labelSelectVersion);
		
		comboBoxVersions = new JComboBox();
		comboBoxVersions.setPreferredSize(new Dimension(28, 80));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxVersions, 6, SpringLayout.SOUTH, labelSelectVersion);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxVersions, 0, SpringLayout.WEST, labelSelectLanguage);
		sl_panelMain.putConstraint(SpringLayout.EAST, comboBoxVersions, -10, SpringLayout.EAST, panelMain);
		panelMain.add(comboBoxVersions);
		
		panel_1 = new JPanel();
		sl_panelMain.putConstraint(SpringLayout.NORTH, panel_1, 20, SpringLayout.SOUTH, comboBoxVersions);
		panel_1.setPreferredSize(new Dimension(10, 30));
		sl_panelMain.putConstraint(SpringLayout.WEST, panel_1, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, panel_1, -10, SpringLayout.EAST, panelMain);
		panelMain.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		panel_1.add(horizontalGlue_1);
		
		checkShowIds = new JCheckBox("org.multipage.generator.textShowTextIds");
		checkShowIds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onShowIds();
			}
		});
		checkShowIds.setFont(new Font("Tahoma", Font.BOLD, 13));
		panel_1.add(checkShowIds);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setMinimumSize(new Dimension(40, 0));
		horizontalStrut.setPreferredSize(new Dimension(40, 0));
		panel_1.add(horizontalStrut);
		
		checkExternalBrowser = new JCheckBox("org.multipage.generator.textExternalBrowser");
		checkExternalBrowser.setFont(new Font("Tahoma", Font.BOLD, 13));
		panel_1.add(checkExternalBrowser);
		
		horizontalGlue = Box.createHorizontalGlue();
		panel_1.add(horizontalGlue);
		
		labelDisplayUrl = new JLabel("org.multipage.generator.textParametersOrUrl");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelDisplayUrl, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelDisplayUrl, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelDisplayUrl);
		
		comboParametersOrUrl = new JComboBox();
		comboParametersOrUrl.setEditable(true);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSelectLanguage, 16, SpringLayout.SOUTH, comboParametersOrUrl);
		comboParametersOrUrl.setFont(new Font("Tahoma", Font.BOLD, 15));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboParametersOrUrl, 6, SpringLayout.SOUTH, labelDisplayUrl);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboParametersOrUrl, 10, SpringLayout.WEST, panelMain);
		panelMain.add(comboParametersOrUrl);
		
		buttonRemoveUrl = new JButton("");
		buttonRemoveUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeParametersOrUrl();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.EAST, comboParametersOrUrl, 0, SpringLayout.WEST, buttonRemoveUrl);
		buttonRemoveUrl.setMargin(new Insets(0, 0, 0, 0));
		buttonRemoveUrl.setPreferredSize(new Dimension(24, 9));
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonRemoveUrl, 0, SpringLayout.NORTH, comboParametersOrUrl);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, buttonRemoveUrl, 0, SpringLayout.SOUTH, comboParametersOrUrl);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonRemoveUrl, -10, SpringLayout.EAST, panelMain);
		panelMain.add(buttonRemoveUrl);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		initializeLanguagesComboBox();
		loadLanguagesToCombo();
		
		initializeVersionsComboBox();
		loadVersions();
		
		loadDialog();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		setBounds(bounds);
		
		Utility.loadComboBoxItemsArray(comboParametersOrUrl, parametersOrUrlRecentState, false);
		Utility.putComboBoxItem(comboParametersOrUrl, defaultUrl);
		Utility.setComboBoxText(comboParametersOrUrl, parametersOrUrlState);
		selectLanguage(languageIdState);
		selectVersion(versionIdState);
		
		checkShowIds.setSelected(showIdsState);
		onShowIds();
		
		checkExternalBrowser.setSelected(externalBrowserState);
	}

	/**
	 * Remove combo box item.
	 */
	private void removeParametersOrUrl() {
		
		String text = Utility.getComboBoxText(comboParametersOrUrl);
		comboParametersOrUrl.removeItem(text);
		Utility.setComboBoxText(comboParametersOrUrl, "");
	}
	
	/**
	 * Select language.
	 * @param languageId
	 */
	private void selectLanguage(long languageId) {
		
		for (int index = 0; index < comboBoxLanguages.getItemCount(); index++) {
			
			Language language = (Language) comboBoxLanguages.getItemAt(index);
			if (language.id == languageId) {
				comboBoxLanguages.setSelectedIndex(index);
				break;
			}
		}
	}

	/**
	 * Select version.
	 * @param versionId
	 */
	private void selectVersion(long versionId) {
		
		for (int index = 0; index < comboBoxVersions.getItemCount(); index++) {
			
			VersionObj version = (VersionObj) comboBoxVersions.getItemAt(index);
			if (version.getId() == versionId) {
				comboBoxVersions.setSelectedIndex(index);
				break;
			}
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelDisplayUrl);
		Utility.localize(labelSelectLanguage);
		Utility.localize(labelSelectVersion);
		Utility.localize(checkShowIds);
		Utility.localize(checkExternalBrowser);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonRemoveUrl.setIcon(Images.getIcon("org/multipage/basic/images/eraser.png"));
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
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		
		parametersOrUrlState = Utility.getComboBoxText(comboParametersOrUrl);
		if (!parametersOrUrlState.isEmpty()) {
			Utility.putComboBoxItem(comboParametersOrUrl, parametersOrUrlState);
		}
		parametersOrUrlRecentState = (String []) Utility.getComboBoxItemsArray(comboParametersOrUrl);
		
		Language language = getSelectedLanguage();
		languageIdState = language != null ? language.id : 0L;
		
		VersionObj version = getSelectedVersion();
		versionIdState = version != null ? version.getId() : 0L;
		
		showIdsState = checkShowIds.isSelected();
		
		externalBrowserState = checkExternalBrowser.isSelected();
	}

	/**
	 * Get selected language.
	 * @return
	 */
	private Language getSelectedLanguage() {
		
		return (Language) comboBoxLanguages.getSelectedItem();
	}

	/**
	 * Get selected version.
	 * @return
	 */
	private VersionObj getSelectedVersion() {
		
		return (VersionObj) comboBoxVersions.getSelectedItem();
	}

	/**
	 * Initialize combo box.
	 */
	private void initializeLanguagesComboBox() {

		// Set renderer.
		comboBoxLanguages.setRenderer(new ListCellRenderer() {
			// Create renderer.
			private LanguageRenderer renderer = new LanguageRenderer();
			// Return renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				// Check value.
				if (!(value instanceof Language)) {
					return null;
				}
				Language language = (Language) value;
				
				boolean isStart = language.id == startLanguageId;

				// Set renderer properties.
				renderer.setProperties(language.description, language.id,
						language.alias, language.image, isStart, index,
						isSelected, cellHasFocus);
				return renderer;
			}
		});
	}

	/**
	 * Load languages.
	 */
	private void loadLanguagesToCombo() {
		
		// Get prerequisites.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		// Reset combo box.
		comboBoxLanguages.removeAllItems();
		
		// Load languages.
		LinkedList<Language> languages = new LinkedList<Language>();
		
		MiddleResult result;
		
		// Login to the database.
		result = middle.login(login);
		if (result.isOK()) {
			
			result = middle.loadLanguages(languages);
			if (result.isOK()) {
				
				// Load start language ID.
				Obj<Long> startLanguageId = new Obj<Long>();
				result = middle.loadStartLanguageId(startLanguageId);
				
				this.startLanguageId = startLanguageId.ref;
			}

			// Logout from the database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Load combo box.
		for (Language language : languages) {
			comboBoxLanguages.addItem(language);
		}
	}
	
	
	/**
	 * Load versions.
	 */
	private void loadVersions() {
		
		comboBoxVersions.removeAllItems();
		
		// Prepare prerequisites.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
		
		// Load data from the database.
		MiddleResult result = middle.loadVersions(login, 0L, versions);
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Populate list.
		for (VersionObj version : versions) {
			comboBoxVersions.addItem(version);
		}
	}
	
	/**
	 * Initialize version combo.
	 */
	private void initializeVersionsComboBox() {
		
		// Create and set renderer.
		comboBoxVersions.setRenderer(new ListCellRenderer<VersionObj>() {

			// Label object.
			VersionRenderer renderer = new VersionRenderer();

			// Renderer method.
			@Override
			public Component getListCellRendererComponent(
					JList<? extends VersionObj> list, VersionObj value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (value == null) {
					renderer.reset();
				}
				else {
					renderer.set(value, index, isSelected, cellHasFocus);
				}
				return renderer;
			}
		});
	}
	
	/**
	 * On show IDs.
	 */
	protected void onShowIds() {
		
		checkShowIds.setForeground(checkShowIds.isSelected() ? Color.RED : Color.BLACK);
	}
}
