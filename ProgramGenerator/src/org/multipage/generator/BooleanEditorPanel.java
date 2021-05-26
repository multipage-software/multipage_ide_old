/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/**
 * 
 * @author
 *
 */
public class BooleanEditorPanel extends BooleanEditorPanelBase {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JRadioButton radioTrue;
	private JRadioButton radioFalse;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private Component horizontalGlue;
	private Component horizontalGlue_1;
	private Component horizontalStrut;

	/**
	 * Create the panel.
	 */
	public BooleanEditorPanel() {

		initComponents();
		// $hide>>$
		setComponentsReferences(radioTrue, radioFalse, buttonGroup, null);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);
		
		radioTrue = new JRadioButton("org.multipage.generator.textTrueValue");
		radioTrue.setFont(new Font("Tahoma", Font.BOLD, 11));
		radioTrue.setForeground(new Color(0, 100, 0));
		radioTrue.setHorizontalAlignment(SwingConstants.CENTER);
		radioTrue.setSelected(true);
		buttonGroup.add(radioTrue);
		add(radioTrue);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(30, 0));
		add(horizontalStrut);
		
		radioFalse = new JRadioButton("org.multipage.generator.textFalseValue");
		radioFalse.setFont(new Font("Tahoma", Font.BOLD, 11));
		radioFalse.setForeground(new Color(255, 0, 0));
		buttonGroup.add(radioFalse);
		add(radioFalse);
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		add(horizontalGlue_1);
	}
}
