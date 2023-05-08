/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.*;

import javax.swing.*;

import org.multipage.generator.*;

/**
 * @author
 *
 */
public class BooleanEditorPanelBuilder extends BooleanEditorPanelBase {

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
	private JLabel labelSelectValue;

	/**
	 * Create the panel.
	 */
	public BooleanEditorPanelBuilder() {

		initComponents();
		// $hide>>$
		setComponentsReferences(radioTrue, radioFalse, buttonGroup, labelSelectValue);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		radioTrue = new JRadioButton("org.multipage.generator.textTrueValue");
		springLayout.putConstraint(SpringLayout.NORTH, radioTrue, 40, SpringLayout.NORTH, this);
		radioTrue.setFont(new Font("Tahoma", Font.BOLD, 11));
		radioTrue.setForeground(new Color(0, 100, 0));
		springLayout.putConstraint(SpringLayout.WEST, radioTrue, 100, SpringLayout.WEST, this);
		radioTrue.setHorizontalAlignment(SwingConstants.CENTER);
		radioTrue.setSelected(true);
		buttonGroup.add(radioTrue);
		add(radioTrue);
		
		radioFalse = new JRadioButton("org.multipage.generator.textFalseValue");
		radioFalse.setFont(new Font("Tahoma", Font.BOLD, 11));
		radioFalse.setForeground(new Color(255, 0, 0));
		springLayout.putConstraint(SpringLayout.NORTH, radioFalse, 6, SpringLayout.SOUTH, radioTrue);
		springLayout.putConstraint(SpringLayout.WEST, radioFalse, 0, SpringLayout.WEST, radioTrue);
		buttonGroup.add(radioFalse);
		add(radioFalse);
		
		labelSelectValue = new JLabel("builder.textSelectBooleanValue");
		springLayout.putConstraint(SpringLayout.NORTH, labelSelectValue, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelSelectValue, 10, SpringLayout.WEST, this);
		add(labelSelectValue);
	}
}
