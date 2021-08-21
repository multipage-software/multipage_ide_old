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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class RelatedAreaConstructorDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returned value.
	 */
	private static WizardReturned returned;
	
	/**
	 * Selected area reference.
	 */
	private Obj<Area> selectedRelatedArea;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonSkip;
	private JButton buttonNext;
	private JButton buttonPrevious;
	private JButton buttonCancel;
	private JPanel panelMain;
	private JLabel labelRelatedArea;
	private TextFieldEx textRelatedArea;
	private JButton buttonSelectRelatedArea;
	private JLabel labelMessage;

	/**
	 * Show dialog.
	 * @param parent
	 * @param selectedRelatedArea 
	 * @param relatedArea 
	 * @param ask 
	 * @param previousReturned 
	 * @param resource
	 * @return 
	 */
	public static WizardReturned showDialog(Component parent, boolean isLastDialog,
			Area relatedArea, Obj<Area> selectedRelatedArea) {
		
		RelatedAreaConstructorDialog dialog = new RelatedAreaConstructorDialog(
				Utility.findWindow(parent), isLastDialog, selectedRelatedArea);
		
		dialog.setVisible(true);
		
		return returned;
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param isLastDialog
	 * @param selectedRelatedArea 
	 */
	public RelatedAreaConstructorDialog(Window parentWindow, boolean isLastDialog,
			Obj<Area> selectedRelatedArea) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		this.selectedRelatedArea = selectedRelatedArea;
		postCreate(isLastDialog);
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
		setTitle("org.multipage.generator.textRelatedAreaConstructorDialog");
		
		setBounds(100, 100, 450, 300);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonSkip = new JButton("org.multipage.generator.textSkip");
		buttonSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onSkip();
			}
		});
		sl_panel.putConstraint(SpringLayout.WEST, buttonSkip, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonSkip, -10, SpringLayout.SOUTH, panel);
		buttonSkip.setPreferredSize(new Dimension(80, 25));
		buttonSkip.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonSkip);
		
		buttonNext = new JButton("org.multipage.generator.textNext");
		buttonNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonNext, 0, SpringLayout.SOUTH, buttonSkip);
		sl_panel.putConstraint(SpringLayout.EAST, buttonNext, -10, SpringLayout.EAST, panel);
		buttonNext.setPreferredSize(new Dimension(80, 25));
		buttonNext.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonNext);
		
		buttonPrevious = new JButton("org.multipage.generator.textPrevious");
		buttonPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrevious();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonPrevious, 0, SpringLayout.SOUTH, buttonSkip);
		sl_panel.putConstraint(SpringLayout.EAST, buttonPrevious, -6, SpringLayout.WEST, buttonNext);
		buttonPrevious.setPreferredSize(new Dimension(80, 25));
		buttonPrevious.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonPrevious);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, 0, SpringLayout.SOUTH, buttonSkip);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -24, SpringLayout.WEST, buttonPrevious);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelRelatedArea = new JLabel("org.multipage.generator.textRelatedArea");
		labelRelatedArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
		sl_panelMain.putConstraint(SpringLayout.WEST, labelRelatedArea, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelRelatedArea);
		
		textRelatedArea = new TextFieldEx();
		textRelatedArea.setFont(new Font("Tahoma", Font.BOLD, 14));
		sl_panelMain.putConstraint(SpringLayout.NORTH, textRelatedArea, 6, SpringLayout.SOUTH, labelRelatedArea);
		sl_panelMain.putConstraint(SpringLayout.WEST, textRelatedArea, 0, SpringLayout.WEST, labelRelatedArea);
		textRelatedArea.setPreferredSize(new Dimension(6, 25));
		textRelatedArea.setEditable(false);
		textRelatedArea.setColumns(10);
		panelMain.add(textRelatedArea);
		
		buttonSelectRelatedArea = new JButton("");
		buttonSelectRelatedArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onSelectRelatedArea();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonSelectRelatedArea, 0, SpringLayout.NORTH, textRelatedArea);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonSelectRelatedArea, -10, SpringLayout.EAST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, textRelatedArea, 0, SpringLayout.WEST, buttonSelectRelatedArea);
		buttonSelectRelatedArea.setPreferredSize(new Dimension(25, 25));
		buttonSelectRelatedArea.setMargin(new Insets(0, 0, 0, 0));
		panelMain.add(buttonSelectRelatedArea);
		
		labelMessage = new JLabel("org.multipage.generator.textSelectRelatedArea");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelRelatedArea, 15, SpringLayout.SOUTH, labelMessage);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelMessage, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, labelRelatedArea);
		labelMessage.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelMain.add(labelMessage);
	}
	
	/**
	 * Post creation.
	 * @param isLastDialog 
	 */
	private void postCreate(boolean isLastDialog) {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Utility.centerOnScreen(this);
		
		localize(isLastDialog);
		setIcons();
		setToolTips();
		
		loadRelatedAreaDescription();
	}

	/**
	 * Localize components.
	 * @param isLastDialog 
	 */
	private void localize(boolean isLastDialog) {
		
		Utility.localize(this);
		buttonNext.setText(Resources.getString(isLastDialog ? "textOk" : "org.multipage.generator.textNext"));
		Utility.localize(buttonCancel);
		Utility.localize(buttonPrevious);
		Utility.localize(buttonSkip);
		Utility.localize(labelRelatedArea);
		Utility.localize(labelMessage);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonNext.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonPrevious.setIcon(Images.getIcon("org/multipage/generator/images/previous_icon.png"));
		buttonSkip.setIcon(Images.getIcon("org/multipage/generator/images/skip.png"));
		buttonSelectRelatedArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
	}

	/**
	 * Set tool tips.
	 */
	protected void setToolTips() {

		buttonSelectRelatedArea.setToolTipText(Resources.getString("org.multipage.generator.tooltipSelectRelatedArea"));
	}

	/**
	 * Load related area description.
	 */
	private void loadRelatedAreaDescription() {
		
		if (selectedRelatedArea.ref != null) {
			textRelatedArea.setText(selectedRelatedArea.ref.getDescriptionForced());
		}
	}

	/**
	 * On skip.
	 */
	protected void onSkip() {
		
		returned = WizardReturned.SKIP;
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		returned = WizardReturned.CANCEL;
		dispose();
	}
	
	/**
	 * On Next.
	 */
	protected void onNext() {
		
		returned = WizardReturned.NEXT;
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
	 * On select related area.
	 */
	protected void onSelectRelatedArea() {
		
		// Select area.
		Area rootArea = ProgramGenerator.getArea(0);
		
		Area relatedArea = SelectSubAreaDialog.showDialog(this, rootArea, selectedRelatedArea.ref);
		if (relatedArea == null) {
			return;
		}
		
		// Set new selected area.
		selectedRelatedArea.ref = relatedArea;
		textRelatedArea.setText(relatedArea.getDescriptionForced());
	}
}