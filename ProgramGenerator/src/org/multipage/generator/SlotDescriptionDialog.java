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
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.EditorPaneEx;
import org.multipage.gui.Images;
import org.multipage.gui.TextEditorPane;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

import com.maclan.Middle;
import com.maclan.MiddleResult;
import com.maclan.Slot;

/**
 * 
 * @author
 *
 */
public class SlotDescriptionDialog extends JDialog {

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
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	

	/**
	 * Slot reference.
	 */
	private Slot slot;

	/**
	 * Editor reference.
	 */
	private TextEditorPane editor;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JPanel panelMain;
	private JEditorPane textSlotInfo;
	private JPanel panelContainer;
	private JButton buttonUnlink;

	/**
	 * Show dialog.
	 * @param parent
	 * @param slot 
	 * @param resource
	 */
	public static void showDialog(Component parent, Slot slot) {
		
		SlotDescriptionDialog dialog = new SlotDescriptionDialog(Utility.findWindow(parent));
		dialog.setSlot(slot);
		
		dialog.setVisible(true);
	}
	
	/**
	 * Set slot
	 * @param slot
	 */
	private void setSlot(Slot slot) {
		
		this.slot = slot;
		
		textSlotInfo.setText(slot.getSlotInfo());
		
		loadSlotDescription(slot);
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SlotDescriptionDialog(Window parentWindow) {
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
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				onWindowOpened();
			}
		});
		setTitle("org.multipage.generator.textSlotDescriptionDialog");
		
		setBounds(100, 100, 450, 370);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 35));
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
		
		buttonUnlink = new JButton("org.multipage.generator.textUnlink");
		sl_panel.putConstraint(SpringLayout.NORTH, buttonUnlink, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.EAST, buttonUnlink, -6, SpringLayout.WEST, buttonOk);
		buttonUnlink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onUnlink();
			}
		});
		buttonUnlink.setPreferredSize(new Dimension(80, 25));
		buttonUnlink.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonUnlink);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		textSlotInfo = new EditorPaneEx();
		textSlotInfo.setBackground(new Color(240, 240, 240));
		textSlotInfo.setContentType("text/html");
		sl_panelMain.putConstraint(SpringLayout.EAST, textSlotInfo, -10, SpringLayout.EAST, panelMain);
		textSlotInfo.setEditable(false);
		sl_panelMain.putConstraint(SpringLayout.NORTH, textSlotInfo, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, textSlotInfo, 10, SpringLayout.WEST, panelMain);
		panelMain.add(textSlotInfo);
		
		panelContainer = new JPanel();
		sl_panelMain.putConstraint(SpringLayout.NORTH, panelContainer, 6, SpringLayout.SOUTH, textSlotInfo);
		sl_panelMain.putConstraint(SpringLayout.WEST, panelContainer, 6, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, panelContainer, 0, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, panelContainer, -6, SpringLayout.EAST, panelMain);
		panelMain.add(panelContainer);
		panelContainer.setLayout(new BorderLayout(0, 0));
	}

	/**
	 * If the 
	 */
	protected void onWindowOpened() {
		
		// If the slot description is not an orphan, inform user.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		Obj<Boolean> isOrphan = new Obj<Boolean>();
		
		MiddleResult result = middle.loadSlotDescriptionIsOrphan(login, this.slot.getId(), isOrphan);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		if (!isOrphan.ref) {
			Utility.show(this, "org.multipage.generator.messageThisSlotDescriptionIsUsedInOtherSlots");
		}
		else {
			buttonUnlink.setEnabled(false);
		}
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		createEditor();
		
		localize();
		setIcons();
		loadDialog();
	}

	/**
	 * Create editor.
	 */
	private void createEditor() {
		
		editor = new TextEditorPane(this, true);
		editor.selectHtmlEditor(true);
		
		panelContainer.add(editor);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonUnlink);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonUnlink.setIcon(Images.getIcon("org/multipage/generator/images/unlink.png"));
	}

	/**
	 * Load slot description.
	 */
	protected void loadSlotDescription(Slot slot) {

		// Load description.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		Obj<String> description = new Obj<String>("");
		
		MiddleResult result = middle.loadSlotDescription(login, slot.getId(), description);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Display description.
		editor.setText(description.ref);
		
		// Set initial edit focus.
		editor.grabFocusText();
	}

	/**
	 * Save slot description.
	 * @param slotId
	 * @param description
	 */
	private void saveSlotDescription(long slotId, String description) {
		
		// Save slot description.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();

		MiddleResult result = middle.updateSlotDescription(login, slotId, description);
		if (result.isNotOK()) {
			result.show(this);
		}
	}

	/**
	 * On unlink description.
	 */
	protected void onUnlink() {
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.textUnlinkSlotDescription")) {
			return;
		}
		
		saveSlotDescription(slot.getId(), null);
		
		Utility.show(this, "org.multipage.generator.messageSlotDescriptionHasBeenUnlinked");
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * On cancel.
	 */
	private void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Save slot description and exit.
		String description = editor.getText();
		saveSlotDescription(slot.getId(), description);
		
		saveDialog();
		dispose();
	}
}