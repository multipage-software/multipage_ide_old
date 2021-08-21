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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Images;
import org.multipage.gui.TextEditorPane;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class TextResourceEditor extends JFrame {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Font.
	 */
	private static Font fontState;
	
	/**
	 * Created editors.
	 */
	private static LinkedList<TextResourceEditor> createdTextEditors = new LinkedList<TextResourceEditor>();
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			// Center the dialog.
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		
		editor.setTextFont(fontState);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		fontState = editor.getTextFont();
	}

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {

		bounds = new Rectangle();
		fontState = new Font("DialogInput", Font.PLAIN, 12);
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		Object data = inputStream.readObject();
		if (!(data instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) data;
		
		data = inputStream.readObject();
		if (!(data instanceof Font)) {
			throw new ClassNotFoundException();
		}
		fontState = (Font) data;
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void seriliazeData(ObjectOutputStream outputStream)
		throws IOException {

		outputStream.writeObject(bounds);
		outputStream.writeObject(fontState);
	}

	/**
	 * Resource ID.
	 */
	private long resourceId;

	/**
	 * Safe text.
	 */
	private String safeText = null;
	
	/**
	 * TextResourceEditor.
	 */
	private TextEditorPane editor;

	/**
	 * Menu add in.
	 */
	private GeneratorTextPopupMenuAddIn popupMenuAddIn;

	// $hide<<$
	/**
	 * Dialog components.
	 */
	private JButton buttonClose;
	private JButton buttonSave;
	private JPanel editorPanel;
	private JButton buttonSaveAndClose;

	/**
	 * Dispose not visible editors.
	 */
	private static void disposeNotVisibleEditors() {
		
		LinkedList<TextResourceEditor> editorsToRemove = new LinkedList<TextResourceEditor>();
		
		// Do loop for all created slot editors.
		for (TextResourceEditor slotEditor : createdTextEditors) {
			
			if (!slotEditor.isVisible()) {
				
				slotEditor.dispose();
				editorsToRemove.add(slotEditor);
			}
		}
		
		// Remove editors from list.
		createdTextEditors.removeAll(editorsToRemove);
	}

	/**
	 * Show existing dialog.
	 * @param resourceId
	 * @return
	 */
	private static boolean showExisting(long resourceId) {
		
		// Dispose not visible editors.
		disposeNotVisibleEditors();
		
		// Do loop for all created editors.
		for (TextResourceEditor textEditor : createdTextEditors) {
						
			if (textEditor.resourceId == resourceId) {
				
				textEditor.setVisible(true);
				textEditor.setExtendedState(NORMAL);
				textEditor.toFront();
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Launch the dialog.
	 * @param resource 
	 */
	public static void showDialog(Component component, long resource,
			boolean isSavedAsText, boolean modal) {
		
		showDialog(component, resource, "", isSavedAsText, null, modal);
	}

	/**
	 * Launch dialog.
	 * @param component
	 * @param resourceId
	 * @param isSavedAsText
	 * @param foundAttributes
	 * @param modal
	 */
	public static void showDialog(Component component, long resourceId, String areaDescription,
			boolean isSavedAsText, FoundAttr foundAttributes, boolean modal) {
		
		if (showExisting(resourceId)) {
			return;
		}
		
		Window parentWindow = Utility.findWindow(component);

		// If the resource is not set as text, inform user and exit
		// the method.
		if (!isSavedAsText) {
			Utility.show(parentWindow, "org.multipage.generator.messageResourceNotSavedAsText");
			return;
		}
		
		TextResourceEditor dialog = new TextResourceEditor(null,
				resourceId, areaDescription, modal);
		dialog.setFoundAttributes(foundAttributes);
		
		dialog.setVisible(true);
	}

	/**
	 * Create the dialog.
	 * @param resource 
	 * @param parentWindow 
	 * @param areaDescription 
	 * @param modal 
	 */
	public TextResourceEditor(Window parentWindow, long resourceId,
			String areaDescription, boolean modal) {
		
		createdTextEditors.add(this);
		
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreate(resourceId, areaDescription);
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
				onClose();
			}
		});
		setTitle("org.multipage.generator.textEditor");
		setBounds(100, 100, 800, 600);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonClose = new JButton("textClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		buttonClose.setMaximumSize(new Dimension(80, 25));
		buttonClose.setMinimumSize(new Dimension(80, 25));
		buttonClose.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonClose);
		
		buttonSave = new JButton("textSave");
		springLayout.putConstraint(SpringLayout.NORTH, buttonSave, 0, SpringLayout.NORTH, buttonClose);
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSave();
			}
		});
		buttonSave.setPreferredSize(new Dimension(80, 25));
		buttonSave.setMargin(new Insets(0, 0, 0, 0));
		buttonSave.setMaximumSize(new Dimension(80, 25));
		buttonSave.setMinimumSize(new Dimension(80, 25));
		getContentPane().add(buttonSave);
		
		editorPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, editorPanel, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, editorPanel, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, editorPanel, -6, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, editorPanel, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(editorPanel);
		editorPanel.setLayout(new BorderLayout(0, 0));
		
		buttonSaveAndClose = new JButton("org.multipage.generator.textSaveAndClose");
		springLayout.putConstraint(SpringLayout.EAST, buttonSave, -135, SpringLayout.WEST, buttonSaveAndClose);
		buttonSaveAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSaveAndClose();
			}
		});
		buttonSaveAndClose.setMargin(new Insets(0, 0, 0, 0));
		buttonSaveAndClose.setPreferredSize(new Dimension(120, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonSaveAndClose, 0, SpringLayout.NORTH, buttonClose);
		springLayout.putConstraint(SpringLayout.EAST, buttonSaveAndClose, -6, SpringLayout.WEST, buttonClose);
		getContentPane().add(buttonSaveAndClose);
	}

	/**
	 * On close.
	 */
	protected void onClose() {
		
		close();
	}
	
	/**
	 * Close dialog.
	 */
	private void close() {
		
		// Process possible new content.
		processNewContent();
		// Save dialog data.
		saveDialog();
		// Dispose window.
		dispose();
		
		createdTextEditors.remove(this);
	}

	/**
	 * Post creation.
	 * @param areaDescription 
	 * @param resource 
	 */
	private void postCreate(long resourceId, String areaDescription) {
		
		this.resourceId = resourceId;
		// Create editor.
		createEditor();
		// Localize.
		localize();
		// Set icons.
		setIcons();
		// Set title.
		setTitle2(areaDescription);
		// Initialize tool bar.
		intializeToolBar();
		// Load resource data.
		load();
		// Set editor listeners.
		setEditorListeners();
		// Load dialog data.
		loadDialog();
		// Add builder popup trayMenu add-in.
		popupMenuAddIn = new GeneratorTextPopupMenuAddIn();
		editor.addPopupMenusPlain(popupMenuAddIn);
	}

	/**
	 * Set title.
	 * @param areaDescription 
	 */
	private void setTitle2(String areaDescription) {
		
		// Load resource name.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		Obj<String> name = new Obj<String>();
		Obj<String> type = new Obj<String>();
		
		MiddleResult result = middle.loadResourceName(login, resourceId,
				name, type);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Set dialog title.
		String title = getTitle() + "-" + name.ref + " (" + type.ref + ")"
				+ (!areaDescription.isEmpty() ? " of area \"" + areaDescription + "\"" : "");
		setTitle(title);
	}

	/**
	 * Create editor.
	 */
	private void createEditor() {

		editor = new TextEditorPane(this, false);
		editor.setExtractBody(false);
		editorPanel.add(editor);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(buttonSave);
		Utility.localize(buttonSaveAndClose);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonSave.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
		buttonSaveAndClose.setIcon(Images.getIcon("org/multipage/generator/images/save_icon.png"));
	}

	/**
	 * Load resource data.
	 */
	private void load() {

		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		Obj<String> text = new Obj<String>();
		
		// Load resource text.
		result = middle.loadResourceTextToString(login, resourceId, text);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Get old scroll position.
		Point oldScrollPosition = editor.getScrollPosition();
		// Set safe text.
		safeText = text.ref;
		// Set editor text.
		editor.setText(text.ref);
		
		// Scroll to the old position.
		editor.scrollToPosition(oldScrollPosition);
	}
	
	/**
	 * On save.
	 */
	private void onSave() {
		
		save();
	}

	/**
	 * On save and close.
	 */
	protected void onSaveAndClose() {

		save();
		close();
	}

	/**
	 * Save.
	 */
	protected boolean save() {

		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		MiddleResult result;
		
		// Get text.
		String text = editor.getText();
		
		// Save resource text.
		result = middle.updateResourceText(login, resourceId, text);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Set safe text.
		safeText = text;
		
		return result.isOK();
	}

	/**
	 * Initialize tool bar.
	 */
	private void intializeToolBar() {

	}

	/**
	 * On before change listener.
	 * @param currentLanguageId
	 * @param oldLanguageId
	 */
	protected void onLanguageBeforeChange(long currentLanguageId,
			long oldLanguageId) {
		
		// Process new content.
		processNewContent();
	}

	/**
	 * On language change.
	 * @param currentLanguageId
	 * @param oldLanguageId
	 */
	protected void onLanguageAfterChanged(long currentLanguageId, long oldLanguageId) {

		load();
	}
	
	/**
	 * Process possible new content. 
	 */
	private void processNewContent() {
		
		if (safeText == null) {
			return;
		}
		
		// Get current content.
		String currentContent = editor.getText();
		// If the content is not changed, do nothing.
		if (currentContent.equals(safeText)) {
			return;
		}
		
		// Ask user if to save the content.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.generator.messageEditorContentChangedSaveIt"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		// Save the content.
		save();
	}

	/**
	 * Set editor listeners.
	 */
	private void setEditorListeners() {

		editor.getTextPane().addKeyListener(new KeyAdapter() {
			// On key pressed.
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
					// Save data.
					save();
				}
			}
		});
	}
	
	/**
	 * Set found attributes
	 * @param foundAttributes
	 */
	private void setFoundAttributes(FoundAttr foundAttributes) {
		
		if (foundAttributes != null) {
			editor.highlightFound(foundAttributes);
		}
	}
}
