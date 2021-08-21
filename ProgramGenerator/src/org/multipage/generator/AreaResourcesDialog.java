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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;
import javax.swing.event.TreeSelectionEvent;

import org.maclan.Area;
import org.maclan.AreaResource;
import org.maclan.Resource;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class AreaResourcesDialog extends JDialog {

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
	 * Splitter position.
	 */
	private static int splitterPosition;
	
	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		splitterPosition = -1;
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
		
		splitterPosition = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(splitterPosition);
	}

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * Areas tree panel reference.
	 */
	private AreasTreePanel areasTreePanel;
	
	/**
	 * Resources editor reference.
	 */
	private AreaResourcesEditor resourcesEditor;
	
	/**
	 * Output list reference.
	 */
	private LinkedList<Resource> selectedResources;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JSplitPane splitMain;
	private JButton buttonCancel;

	/**
	 * Show dialog.
	 * @param parent
	 * @param selectedResources 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, LinkedList<Resource> selectedResources) {
		
		selectedResources.clear();
		
		AreaResourcesDialog dialog = new AreaResourcesDialog(Utility.findWindow(parent));
		
		dialog.selectedResources = selectedResources;
		dialog.setVisible(true);
		
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public AreaResourcesDialog(Window parentWindow) {
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
		setTitle("org.multipage.generator.textAreaResourcesDialog");
		
		setBounds(100, 100, 613, 480);
		
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
			public void actionPerformed(ActionEvent arg0) {
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
		
		splitMain = new JSplitPane();
		splitMain.setResizeWeight(0.3);
		getContentPane().add(splitMain, BorderLayout.CENTER);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		createResouresEditor();
		createAreasTree();
		
		loadData();
	}

	/**
	 * Load data.
	 */
	private void loadData() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		
		if (splitterPosition != -1) {
			splitMain.setDividerLocation(splitterPosition);
		}
	}
	
	/**
	 * Create resources editor.
	 */
	private void createResouresEditor() {
		
		resourcesEditor = new AreaResourcesEditor();
		splitMain.setRightComponent(resourcesEditor);
	}

	/**
	 * Create areas tree.
	 */
	@SuppressWarnings("serial")
	private void createAreasTree() {
		
		areasTreePanel = new AreasTreePanel() {
			@Override
			protected void onAreasSelected(TreeSelectionEvent e) {
				
				// Get selected area.
				Area selectedArea = getSelectedArea();
				
				// Set resources list.
				if (resourcesEditor != null) {
					resourcesEditor.loadArea(selectedArea);
				}
			}
		};
		
		splitMain.setLeftComponent(areasTreePanel);
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
		splitterPosition = splitMain.getDividerLocation();
	}
	
	/**
	 * On OK.
	 */
	protected void onOK() {

		if (!loadSelectedResources(selectedResources)) {
			return;
		}
		
		saveDialog();
		confirm = true;
		dispose();
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
	 * Load selected resources.
	 * @param selectedResources
	 */
	private boolean loadSelectedResources(LinkedList<Resource> selectedResources) {
		
		// Get selected resources.
		java.util.List<AreaResource> resources = resourcesEditor.getSelectedResources();
		
		if (resources == null || resources.size() == 0) {
			Utility.show(this, "org.multipage.generator.messageSelectResources");
			return false;
		}
		
		selectedResources.addAll(resources);
		
		return true;
	}
}