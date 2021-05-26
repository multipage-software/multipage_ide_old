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
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

import com.maclan.Middle;
import com.maclan.MiddleResult;
import com.maclan.VersionObj;

/**
 * 
 * @author
 *
 */
public class SelectVersionDialog extends JDialog {

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
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load state.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream) 
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
	}

	/**
	 * Save state.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * List model.
	 */
	private  DefaultListModel<VersionObj> listModel;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JPanel panelMain;
	private JButton buttonCancel;
	private JLabel labelSelectVersion;
	private JScrollPane scrollPane;
	private JList list;

	/**
	 * Show dialog.
	 * @param parent
	 * @param version 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, Obj<VersionObj> version) {
		
		// Check versions.
		LinkedList<VersionObj> versions = ProgramGenerator.getAreasModel().getVersions();
		if (versions.size() == 1) {
			version.ref = versions.getFirst();
			return true;
		}
		
		SelectVersionDialog dialog = new SelectVersionDialog(Utility.findWindow(parent));
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			version.ref = dialog.getSelectedVersion();
		}
		
		return dialog.confirm;
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SelectVersionDialog(Window parentWindow) {
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
		});
		setTitle("org.multipage.generator.textSelectVersion");
		
		setBounds(100, 100, 534, 357);
		
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
		
		labelSelectVersion = new JLabel("org.multipage.generator.textSelectVersionFromList");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSelectVersion, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSelectVersion, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelSelectVersion);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelSelectVersion);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
		
		list = new JList();
		scrollPane.setViewportView(list);
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
				
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Check selection.
		if (getSelectedVersion() == null) {
			Utility.show(this, "org.multipage.generator.messagePleaseSelectVersion");
			return;
		}
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.width == 0) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
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
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		loadDialog();
		
		localize();
		setIcons();
		
		initializeList();
		loadVersions();
		
		select(0);
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSelectVersion);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
	}
	

	/**
	 * Initialize list.
	 */
	private void initializeList() {
		
		// Create and set list model.
		listModel = new DefaultListModel<VersionObj>();
		list.setModel(listModel);
		
		// Create and set renderer.
		list.setCellRenderer(new ListCellRenderer<VersionObj>() {

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
	 * Load versions.
	 */
	private void loadVersions() {
		
		listModel.clear();
		
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
			listModel.addElement(version);
		}
	}

	/**
	 * Gets selected version.
	 * @return
	 */
	private VersionObj getSelectedVersion() {
		
		if (list.getSelectedIndices().length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleVersion");
			return null;
		}
		
		return (VersionObj) list.getSelectedValue();
	}
	
	/**
	 * Select version.
	 * @param versionId
	 */
	private void select(long versionId) {
		
		for (int index = 0; index < listModel.getSize(); index++) {
			
			VersionObj version = listModel.get(index);
			if (version.getId() == versionId) {
				
				list.setSelectedIndex(index);
				break;
			}
		}
	}
}