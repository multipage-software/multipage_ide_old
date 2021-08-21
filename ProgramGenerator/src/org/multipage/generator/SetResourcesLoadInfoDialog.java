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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.maclan.ResourceConstructor;
import org.multipage.gui.Images;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class SetResourcesLoadInfoDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Returned value.
	 */
	private WizardReturned returned = WizardReturned.UNKNOWN;
	
	/**
	 * List items references.
	 */
	private LinkedList<ResourceLoadInfoPanel> listPanelsReferences = new LinkedList<ResourceLoadInfoPanel>();

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonPrevious;
	private JButton buttonCancel;
	private JButton buttonSkip;
	private JPanel panelMain;
	private JLabel labelSetResourcesInput;
	private JScrollPane scrollPane;
	private JPanel listPanel;
	private JToolBar toolBar;

	/**
	 * Show dialog.
	 * @param parent
	 * @param resources 
	 * @param resource
	 */
	public static WizardReturned showDialog(Component parent, LinkedList<ResourceConstructor> resources) {
		
		SetResourcesLoadInfoDialog dialog = new SetResourcesLoadInfoDialog(Utility.findWindow(parent));
		
		dialog.loadResources(resources);
		dialog.setVisible(true);
		
		return dialog.returned;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SetResourcesLoadInfoDialog(Window parentWindow) {
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
		setTitle("org.multipage.generator.textSetResourcesReferences");
		
		setBounds(100, 100, 450, 300);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 40));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.EAST, panel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonPrevious = new JButton("org.multipage.generator.textPrevious");
		buttonPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrevious();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonPrevious, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.EAST, buttonPrevious, -6, SpringLayout.WEST, buttonOk);
		buttonPrevious.setPreferredSize(new Dimension(80, 25));
		buttonPrevious.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonPrevious);
		
		buttonCancel = new JButton("textCancel");
		sl_panel.putConstraint(SpringLayout.NORTH, buttonCancel, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -24, SpringLayout.WEST, buttonPrevious);
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		buttonSkip = new JButton("org.multipage.generator.textSkip");
		buttonSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSkip();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, buttonSkip, 0, SpringLayout.NORTH, buttonOk);
		sl_panel.putConstraint(SpringLayout.WEST, buttonSkip, 10, SpringLayout.WEST, panel);
		buttonSkip.setPreferredSize(new Dimension(80, 25));
		buttonSkip.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonSkip);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelSetResourcesInput = new JLabel("org.multipage.generator.textSetResourcesInput");
		labelSetResourcesInput.setFont(new Font("Tahoma", Font.PLAIN, 12));
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSetResourcesInput, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSetResourcesInput, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelSetResourcesInput);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 7, SpringLayout.SOUTH, labelSetResourcesInput);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
		
		listPanel = new JPanel();
		scrollPane.setViewportView(listPanel);
		
		toolBar = new JToolBar();
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.NORTH, toolBar);
		sl_panelMain.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, panelMain);
		toolBar.setFloatable(false);
		sl_panelMain.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, labelSetResourcesInput);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.SOUTH, panelMain);
		panelMain.add(toolBar);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		
		createToolBar();
		
		localize();
		setIcons();
		
		initializeList();
		
		// If there is no builder extension, hide skip button.
		buttonSkip.setVisible(ProgramGenerator.isExtensionToBuilder());
	}

	/**
	 * Create tool bar.
	 */
	private void createToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/clear_all.png", this,
				"onClear", "org.multipage.generator.tooltipClearResourceReferences");
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(buttonPrevious);
		Utility.localize(buttonSkip);
		Utility.localize(labelSetResourcesInput);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonPrevious.setIcon(Images.getIcon("org/multipage/generator/images/previous_icon.png"));
		buttonSkip.setIcon(Images.getIcon("org/multipage/generator/images/skip.png"));
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		returned = WizardReturned.CANCEL;
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		returned = WizardReturned.OK;
		dispose();
	}

	/**
	 * On skip.
	 */
	protected void onSkip() {
		
		returned = WizardReturned.SKIP;
		dispose();
	}

	/**
	 * On previous.
	 */
	protected void onPrevious() {
		
		returned = WizardReturned.PREVIOUS;
		dispose();
	}
	
	/**
	 * Initialize list.
	 */
	private void initializeList() {
		
	}
	
	/**
	 * Load resources.
	 * @param resources
	 */
	private void loadResources(LinkedList<ResourceConstructor> resources) {
		
		// Reset references.
		listPanelsReferences.clear();
		
		// If any editable resource doesn't exist, show label.
		if (!ResourceConstructor.existEditableResource(resources)) {
			
			// Inform user.
			listPanel.setLayout(new BorderLayout());
			
			JLabel labelMessage = new JLabel(
					Resources.getString("org.multipage.generator.messageNoReources"));
			
			labelMessage.setFont(labelMessage.getFont().deriveFont(12.0f));
			
			labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
			listPanel.add(labelMessage, BorderLayout.CENTER);
			return;
		}
		
		// Set list panel layout.
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		
		// Add panels.
		int index = 0;
		for (ResourceConstructor resource : resources) {

			// If the resource is not editable, continue the loop.
			if (!resource.isEditable()) {
				continue;
			}
			
			// Add new panel.
			ResourceLoadInfoPanel panel = new ResourceLoadInfoPanel(resource, Utility.itemColor(index++));
			
			Dimension size = panel.getPreferredSize();
			size.width = Integer.MAX_VALUE;
			panel.setMaximumSize(size);
			
			listPanel.add(panel);
			
			// Add reference.
			listPanelsReferences.add(panel);
		}
	}
	
	/**
	 * On clear.
	 */
	public void onClear() {
		
		// Let user confirm.
		if (!Utility.ask(this, "org.multipage.generator.messageClearResourcesInputs")) {
			return;
		}
		
		// Clear load infos.
		for (ResourceLoadInfoPanel panel : listPanelsReferences) {
			
			panel.clear();
		}
	}
}