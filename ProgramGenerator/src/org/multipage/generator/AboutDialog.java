/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

import build_number.BuildNumber;

/**
 * 
 * @author
 *
 */
public class AboutDialog extends AboutDialogBase {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	private JEditorPane textArea;
	private JLabel labelPicture;
	private JButton buttonClose;
	private JLabel labelBuildNumber;

	/**
	 * Create the dialog.
	 */
	public AboutDialog(Window owner) {
		super(owner, ModalityType.APPLICATION_MODAL);	
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreation();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		setTitle("org.multipage.generator.dialogAboutTitle");
		setSize(new Dimension(559, 350));
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		textArea = new JEditorPane();
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, textArea, -60, SpringLayout.SOUTH, getContentPane());
		textArea.setContentType("text/html");
		springLayout.putConstraint(SpringLayout.EAST, textArea, -10, SpringLayout.EAST, getContentPane());
		textArea.setFont(new Font("Monospaced", Font.BOLD, 12));
		textArea.setEditable(false);
		textArea.setForeground(new Color(0, 0, 139));
		textArea.setOpaque(false);
		getContentPane().add(textArea);
		
		labelPicture = new JLabel("");
		labelPicture.setBackground(Color.LIGHT_GRAY);
		springLayout.putConstraint(SpringLayout.WEST, labelPicture, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, textArea, 30, SpringLayout.EAST, labelPicture);
		springLayout.putConstraint(SpringLayout.NORTH, labelPicture, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, labelPicture, 0, SpringLayout.SOUTH, getContentPane());
		labelPicture.setPreferredSize(new Dimension(265, 302));
		getContentPane().add(labelPicture);
		
		buttonClose = new JButton("textClose");
		springLayout.putConstraint(SpringLayout.NORTH, buttonClose, -32, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonClose);
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		buttonClose.setPreferredSize(new Dimension(80, 25));
		
		labelBuildNumber = new JLabel("build number");
		labelBuildNumber.setHorizontalAlignment(SwingConstants.TRAILING);
		springLayout.putConstraint(SpringLayout.NORTH, labelBuildNumber, 6, SpringLayout.SOUTH, textArea);
		springLayout.putConstraint(SpringLayout.WEST, labelBuildNumber, 0, SpringLayout.WEST, textArea);
		springLayout.putConstraint(SpringLayout.EAST, labelBuildNumber, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelBuildNumber);
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {

		// Set build number.
		labelBuildNumber.setText(BuildNumber.getBuildNumber());
		// Localize.
		localize();
		// Set icons.
		setIcons();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set text.
		textArea.setText(String.format(Resources.getString("org.multipage.generator.messageAbout"), BuildNumber.getVersion()));
		// Set hyperlink listener.
		setHyperlinkListener();
	}

	/**
	 * Set hyperlink listener.
	 */
	private void setHyperlinkListener() {
		
		final Desktop desktop = Desktop.getDesktop(); 
		
		textArea.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					try {
						desktop.browse(new URI(e.getURL().toString()));
					}
					catch (Exception ex) {
					}
				}  
			}
		});
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonClose);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		buttonClose.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		labelPicture.setIcon(Images.getIcon("org/multipage/generator/images/splash.png"));
	}
}
