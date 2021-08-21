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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class SelectSubAreaDialog extends JDialog {

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
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
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
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Root area reference.
	 */
	private Area rootArea;

	/**
	 * Areas tree panel.
	 */
	private AreasTreePanel treePanel;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;

	/**
	 * Show dialog.
	 * @param parent
	 * @param selectedArea 
	 * @param resource
	 */
	public static Area showDialog(Component parent, Area rootArea, Area selectedArea) {
		
		// Update area.
		if (rootArea != null) {
			rootArea = ProgramGenerator.getArea(rootArea.getId());
		}
		
		SelectSubAreaDialog dialog = new SelectSubAreaDialog(Utility.findWindow(parent),
				rootArea);
		
		dialog.selectArea(selectedArea);
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			return dialog.getSelectedArea();
		}
		return null;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param rootArea 
	 */
	public SelectSubAreaDialog(Window parentWindow, Area rootArea) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		this.rootArea = rootArea;
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
		setTitle("org.multipage.generator.textSelectSubAreaDialog");
		
		setBounds(100, 100, 464, 357);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
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
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		createPanels();
		
		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Create panels.
	 */
	private void createPanels() {
		
		treePanel = new AreasTreePanel(rootArea);
		
		getContentPane().add(treePanel);
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
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
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
	protected void onCancel() {
		
		confirm = false;
		
		saveDialog();
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOK() {
		
		confirm = true;
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Get selected area.
	 * @return
	 */
	private Area getSelectedArea() {
		
		return treePanel.getSelectedArea();
	}
	
	/**
	 * Select area.
	 * @param area
	 */
	private void selectArea(Area area) {
		
		treePanel.selectArea(area);
	}
}