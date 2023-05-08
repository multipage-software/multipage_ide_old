/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * 
 * @author
 *
 */
public class UnknownRenderer extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Components.
	 */
	private JLabel label;

	/**
	 * Create the panel.
	 */
	public UnknownRenderer() {
		// Initialize components.
		initComponents();
		// Post creation.
		postCreate();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		label = new JLabel("builder.textUnknownValue");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		add(label, BorderLayout.CENTER);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {

		Utility.localize(label);
		label.setIcon(Images.getIcon("org/multipage/generator/images/unknown.png"));
	}
}
