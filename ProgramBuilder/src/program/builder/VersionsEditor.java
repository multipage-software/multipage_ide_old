/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;

import java.awt.*;

import javax.swing.*;

import org.multipage.basic.*;
import org.multipage.generator.GeneratorTranslatorDialog;
import org.multipage.generator.GeneratorMainFrame;
import org.multipage.generator.VersionRenderer;
import org.maclan.*;

import java.awt.event.*;
import java.io.IOException;
import org.multipage.gui.StateInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

/**
 * 
 * @author
 *
 */
public class VersionsEditor extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog boundary.
	 */
	public static Rectangle bounds;

	/**
	 * Load properties.
	 * @param inputStream
	 * @throws ClassNotFoundException 
	 */
	public static void serializeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		// Get dialog bounds.
		bounds = (Rectangle) inputStream.readObject();
	}

	/**
	 * Save properties.
	 * @param outputStream
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {
		
		// Save dialog bounds.
		outputStream.writeObject(bounds);
	}

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
	private JPanel panelComponents;
	private JLabel labelVersionsList;
	private JScrollPane scrollPane;
	private JToolBar toolBar;
	private JList list;
	private JButton buttonTranslator;

	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 */
	public static void showDialog(Component parent) {
		
		VersionsEditor dialog = new VersionsEditor(Utility.findWindow(parent));
		
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public VersionsEditor(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds != null) {
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
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("builder.textVersionsEditor");
		
		setBounds(100, 100, 450, 344);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 43));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOK();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.EAST, panel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonTranslator = new JButton("builder.textTranslator");
		buttonTranslator.setIconTextGap(6);
		buttonTranslator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onTranslator();
			}
		});
		buttonTranslator.setHorizontalAlignment(SwingConstants.LEFT);
		sl_panel.putConstraint(SpringLayout.WEST, buttonTranslator, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonTranslator, 0, SpringLayout.SOUTH, buttonOk);
		buttonTranslator.setPreferredSize(new Dimension(100, 30));
		buttonTranslator.setMargin(new Insets(0, 3, 0, 0));
		panel.add(buttonTranslator);
		
		panelComponents = new JPanel();
		getContentPane().add(panelComponents, BorderLayout.CENTER);
		SpringLayout sl_panelComponents = new SpringLayout();
		panelComponents.setLayout(sl_panelComponents);
		
		labelVersionsList = new JLabel("builder.textVersionsList");
		sl_panelComponents.putConstraint(SpringLayout.NORTH, labelVersionsList, 10, SpringLayout.NORTH, panelComponents);
		sl_panelComponents.putConstraint(SpringLayout.WEST, labelVersionsList, 10, SpringLayout.WEST, panelComponents);
		panelComponents.add(labelVersionsList);
		
		scrollPane = new JScrollPane();
		sl_panelComponents.putConstraint(SpringLayout.NORTH, scrollPane, 9, SpringLayout.SOUTH, labelVersionsList);
		sl_panelComponents.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelComponents);
		sl_panelComponents.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelComponents);
		panelComponents.add(scrollPane);
		
		toolBar = new JToolBar();
		sl_panelComponents.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, toolBar);
		
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				onListClick(event);
			}
		});
		scrollPane.setViewportView(list);
		toolBar.setFloatable(false);
		sl_panelComponents.putConstraint(SpringLayout.EAST, toolBar, -10, SpringLayout.EAST, panelComponents);
		toolBar.setPreferredSize(new Dimension(13, 30));
		sl_panelComponents.putConstraint(SpringLayout.WEST, toolBar, 10, SpringLayout.WEST, panelComponents);
		sl_panelComponents.putConstraint(SpringLayout.SOUTH, toolBar, -10, SpringLayout.SOUTH, panelComponents);
		panelComponents.add(toolBar);
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		dispose();
	}

	/**
	 * On list click.
	 * @param event 
	 */
	protected void onListClick(MouseEvent event) {
		
		if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
			onEditVersion();
		}
	}

	/**
	 * On OK.
	 */
	protected void onOK() {
		
		saveDialog();
		
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		
		createToolBar();
		localize();
		setIcons();
		
		initializeList();
		loadVersions();
		
		loadDialog();
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
	 * Create tool bar.
	 */
	private void createToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/add_item_icon.png", this, "onAddVersion",
				"builder.tooltipAddVersion");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/edit.png", this, "onEditVersion",
				"builder.tooltipEditVersion");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_icon.png", this, "onRemoveVersion",
				"builder.tooltipRemoveVersion");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/update_icon.png", this, "onUpdateVersions",
				"builder.tooltipUpdateVersions");
		toolBar.addSeparator();
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/select_all.png", this, "onSelectAllVersions",
				"org.multipage.generator.tooltipSelectAllVersions");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/deselect_all.png", this, "onUnselectAllVersions",
				"org.multipage.generator.tooltipUnselectAllVersions");
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(labelVersionsList);
		Utility.localize(buttonTranslator);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonTranslator.setIcon(Images.getIcon("org/multipage/generator/images/translator.png"));
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

	/**
	 * On add version.
	 */
	public void onAddVersion() {
		
		// Get new version object.
		VersionObj version = new VersionObj();
		
		if (!VersionPropertiesDialog.showNewDialog(this, version)) {
			return;
		}
		
		// Prepare prerequisites.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Insert data.
		MiddleResult result = middle.insertVersion(login, version);
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Load versions.
		loadVersions();
		
		// Select version.
		select(version.getId());
		
		// Ensure selection visible.
		Utility.ensureSelectedItemVisible(list);
	}

	/**
	 * On edit version.
	 */
	public void onEditVersion() {
		
		VersionObj version = getSelectedVersion();
		if (version == null) {
			return;
		}
		
		// Edit version.
		if (!VersionPropertiesDialog.showEditDialog(this, version)) {
			return;
		}
		
		// Prepare prerequisites.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Update version.
		MiddleResult result = middle.updateVersion(login, version);
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Load versions.
		loadVersions();
		
		// Select version.
		select(version.getId());
	}
	
	/**
	 * On remove version.
	 */
	public void onRemoveVersion() {
		
		List<VersionObj> versions = list.getSelectedValuesList();
		if (versions.isEmpty()) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleVersion");
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "builder.messageDeleteSelectedVersions")) {
			return;
		}
		
		// Prepare prerequisites.
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Try to login to database.
		MiddleResult result = middle.login(login);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
			
		// Do for all selected versions.
		for (VersionObj version : versions) {
		
			// Cannot delete default version.
			if (version.getId() == 0L) {
				Utility.show(this, "builder.messageCannotDeleteDefaultVersion");
				continue;
			}

			// Update version.
			result = middle.removeVersion(version.getId());
			
			// On error inform user.
			if (result.isNotOK()) {
				break;
			}
		}
		
		// Inform user about an error.
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Logout from database.
		MiddleResult logoutResult = middle.logout(result);
		if (logoutResult.isNotOK()) {
			logoutResult.show(this);
		}
		
		// Load versions.
		loadVersions();
	}
	
	/**
	 * Update versions.
	 */
	public void onUpdateVersions() {
		
		loadVersions();
	}
	
	/**
	 * Select all versions.
	 */
	public void onSelectAllVersions() {
		
		list.setSelectionInterval(0, listModel.getSize() - 1);
	}
	
	/**
	 * Unselect all versions.
	 */
	public void onUnselectAllVersions() {
		
		list.clearSelection();
	}
	
	/**
	 * On translator.
	 */
	protected void onTranslator() {
		
		// Show dialog.
		GeneratorTranslatorDialog.showDialog(GeneratorMainFrame.getFrame(), new LinkedList<Area>());
	}
}