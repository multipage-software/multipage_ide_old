/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import javax.swing.*;

import org.maclan.*;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * 
 * @author
 *
 */
public class LocalizeTextDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Editor slider.
	 */
	private static int editorDividerPosition;
	
	/**
	 * Confirm flag.
	 */
	protected boolean confirm;

	/**
	 * Text identifier.
	 */
	private long textId;
		
	/**
	 * Language ID.
	 */
	private Language language;
	
	/**
	 * Default text editor.
	 */
	private TextEditorPane editorDefaultLanguage;
	
	/**
	 * Localized text editor.
	 */
	private TextEditorPane editorLocalizedText;
	// $hide<<$
	
	/**
	 * Components.
	 */
	private JSplitPane splitPane;
	private JPanel panelDefaultText;
	private JPanel panelLocalizedText;
	private JLabel labelDefaultLanguageText;
	private JLabel labelLocalizedText;
	private JPanel panelDefault;
	private JPanel panelLocalized;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JButton buttonSave;

	/**
	 * Show dialog.
	 * @param parentWindow
	 * @param localizedText 
	 * @param defaultText 
	 * @param textId 
	 * @return
	 */
	public static boolean showDialog(Window parentWindow, long textId,
			String defaultText, String localizedText, Language language) {

		LocalizeTextDialog dialog = new LocalizeTextDialog(parentWindow);
		// Set data.
		dialog.setDialogData(textId, defaultText, localizedText,
				language);
		dialog.setVisible(true);

		return dialog.confirm;
	}

	/**
	 * Save dialog data.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {

		// Save bounds.
		outputStream.writeObject(bounds);
		// Save editor slider.
		outputStream.writeInt(editorDividerPosition);
	}

	/**
	 * Load dialog data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load bounds.
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		// Read slider editor.
		editorDividerPosition = inputStream.readInt();
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		bounds = new Rectangle();
		editorDividerPosition = -1;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param login 
	 * @param middle 
	 */
	public LocalizeTextDialog(Window parentWindow) {
		super(parentWindow, ModalityType.APPLICATION_MODAL);

		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.translator.textLocalizeTextDialog");
		setBounds(100, 100, 874, 544);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, -40, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, splitPane, -10, SpringLayout.EAST, getContentPane());
		splitPane.setResizeWeight(0.5);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(splitPane);
		
		panelDefaultText = new JPanel();
		splitPane.setLeftComponent(panelDefaultText);
		SpringLayout sl_panelDefaultText = new SpringLayout();
		panelDefaultText.setLayout(sl_panelDefaultText);
		
		labelDefaultLanguageText = new JLabel("org.multipage.translator.textDefaultLanguageText");
		labelDefaultLanguageText.setHorizontalAlignment(SwingConstants.CENTER);
		sl_panelDefaultText.putConstraint(SpringLayout.NORTH, labelDefaultLanguageText, 10, SpringLayout.NORTH, panelDefaultText);
		sl_panelDefaultText.putConstraint(SpringLayout.WEST, labelDefaultLanguageText, 0, SpringLayout.WEST, panelDefaultText);
		panelDefaultText.add(labelDefaultLanguageText);
		
		panelDefault = new JPanel();
		sl_panelDefaultText.putConstraint(SpringLayout.NORTH, panelDefault, 6, SpringLayout.SOUTH, labelDefaultLanguageText);
		sl_panelDefaultText.putConstraint(SpringLayout.SOUTH, panelDefault, 0, SpringLayout.SOUTH, panelDefaultText);
		sl_panelDefaultText.putConstraint(SpringLayout.EAST, labelDefaultLanguageText, 0, SpringLayout.EAST, panelDefault);
		sl_panelDefaultText.putConstraint(SpringLayout.WEST, panelDefault, 0, SpringLayout.WEST, panelDefaultText);
		sl_panelDefaultText.putConstraint(SpringLayout.EAST, panelDefault, 0, SpringLayout.EAST, panelDefaultText);
		panelDefaultText.add(panelDefault);
		panelDefault.setLayout(new BorderLayout(0, 0));
		
		panelLocalizedText = new JPanel();
		splitPane.setRightComponent(panelLocalizedText);
		SpringLayout sl_panelLocalizedText = new SpringLayout();
		panelLocalizedText.setLayout(sl_panelLocalizedText);
		
		labelLocalizedText = new JLabel("org.multipage.translator.textLocalizedText");
		labelLocalizedText.setHorizontalAlignment(SwingConstants.CENTER);
		sl_panelLocalizedText.putConstraint(SpringLayout.NORTH, labelLocalizedText, 10, SpringLayout.NORTH, panelLocalizedText);
		sl_panelLocalizedText.putConstraint(SpringLayout.WEST, labelLocalizedText, 0, SpringLayout.WEST, panelLocalizedText);
		panelLocalizedText.add(labelLocalizedText);
		
		panelLocalized = new JPanel();
		sl_panelLocalizedText.putConstraint(SpringLayout.NORTH, panelLocalized, 5, SpringLayout.SOUTH, labelLocalizedText);
		sl_panelLocalizedText.putConstraint(SpringLayout.SOUTH, panelLocalized, 0, SpringLayout.SOUTH, panelLocalizedText);
		sl_panelLocalizedText.putConstraint(SpringLayout.EAST, labelLocalizedText, 0, SpringLayout.EAST, panelLocalized);
		sl_panelLocalizedText.putConstraint(SpringLayout.WEST, panelLocalized, 0, SpringLayout.WEST, panelLocalizedText);
		sl_panelLocalizedText.putConstraint(SpringLayout.EAST, panelLocalized, 0, SpringLayout.EAST, panelLocalizedText);
		panelLocalizedText.add(panelLocalized);
		panelLocalized.setLayout(new BorderLayout(0, 0));
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonCancel, 7, SpringLayout.SOUTH, splitPane);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		buttonSave = new JButton("textSave");
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveData();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonSave, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonSave, -43, SpringLayout.WEST, buttonOk);
		buttonSave.setPreferredSize(new Dimension(80, 25));
		buttonSave.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonSave);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Create editors.
		createEditors();
		// Initialize dialog.
		loadDialog();
		// Localize text.
		localize();
		// Set icons.
		setIcons();
	}

	/**
	 * Create editors.
	 */
	private void createEditors() {
		
		TextEditorPane.wordWrapState = true;
		
		editorDefaultLanguage = new TextEditorPane(this, true);
		editorDefaultLanguage.setEditable(false);
		editorDefaultLanguage.selectHtmlEditor(false);
		panelDefault.add(editorDefaultLanguage);
		
		editorLocalizedText = new TextEditorPane(this, true);
		editorLocalizedText.selectHtmlEditor(false);
		panelLocalized.add(editorLocalizedText);
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		// Try to use bounds.
		if (!bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			// Center dialog.
			Utility.centerOnScreen(this);
		}
		// Try to set divider position.
		if (editorDividerPosition != -1) {
			splitPane.setDividerLocation(editorDividerPosition);
		}
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		// Get bounds.
		bounds = getBounds();
		// Get divider position.
		editorDividerPosition = splitPane.getDividerLocation();
	}

	/**
	 * Localize text.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(labelDefaultLanguageText);
		Utility.localize(labelLocalizedText);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonSave);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/translator/images/main_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/translator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/translator/images/cancel_icon.png"));
		buttonSave.setIcon(Images.getIcon("org/multipage/translator/images/save_icon.png"));
	}

	/**
	 * Set dialog data.
	 * @param textId
	 * @param defaultText
	 * @param localizedText
	 * @param language 
	 */
	protected void setDialogData(long textId, String defaultText,
			String localizedText, Language language) {

		this.textId = textId;
		this.language = language;
		editorDefaultLanguage.setText(defaultText);
		editorLocalizedText.setText(localizedText);
		labelLocalizedText.setText(language.toString() + ":");
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
	 * On OK.
	 */
	protected void onOk() {

		confirm = true;
		
		saveData();
		saveDialog();
		dispose();
	}

	/**
	 * Save data.
	 */
	private void saveData() {

		// Get text.
		String text = editorLocalizedText.getText();
		
		MiddleResult result;
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Update localized text.
		if (text.isEmpty()) {
			result = middle.removeLanguageText(login, language.id, textId);
		}
		else {
			result = middle.updateLanguageText(login, language.id, textId, text);
		}
		if (result.isNotOK()) {
			result.show(this);
		}
		else {
			// Update information.
			onUpdateInformation();
		}
	}

	/**
	 * On update information.
	 */
	protected void onUpdateInformation() {

		// Override this method.
	}
}
