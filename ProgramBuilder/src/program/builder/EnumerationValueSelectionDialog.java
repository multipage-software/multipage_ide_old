/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;

import javax.swing.*;

import org.maclan.*;

import java.awt.event.*;
import java.io.*;

/**
 * 
 * @author
 *
 */
public class EnumerationValueSelectionDialog extends JDialog {

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
	public static void serializeData(StateInputStream inputStream) 
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
	public static void serializeData(StateOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Enumeration editor panel.
	 */
	private EnumerationEditorPanelBuilder editorPanel;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonCancel;
	private JButton buttonOk;

	/**
	 * Show dialog.
	 * @param parent
	 * @param enumerationValue 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, Obj<EnumerationValue> enumerationValue) {
		
		// Create dialog and set enumeration value.
		EnumerationValueSelectionDialog dialog = new EnumerationValueSelectionDialog(Utility.findWindow(parent));
		if (enumerationValue.ref != null) {
			dialog.editorPanel.setEnumerationValue(enumerationValue.ref);
		}
		
		dialog.setVisible(true);
		
		// Get enumeration value.
		if (dialog.confirm) {
			enumerationValue.ref = dialog.editorPanel.getEnumerationValue();
		}
		
		return dialog.confirm;
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public EnumerationValueSelectionDialog(Window parentWindow) {
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
		setMinimumSize(new Dimension(500, 210));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("builder.textEnumerationValueSelectionDialog");
		
		setBounds(100, 100, 500, 210);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonOk);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		loadDialog();
		
		editorPanel = new EnumerationEditorPanelBuilder();
		getContentPane().add(editorPanel, BorderLayout.CENTER);
		
		localize();
		setIcons();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.width == 0) {
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
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
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
		saveDialog();
		dispose();
	}
}