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
import java.awt.event.*;
import java.net.*;

import javax.swing.event.*;

import org.multipage.generator.*;

/**
 * @author
 *
 */
public class AboutDialogBuilder extends AboutDialogBase {


	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	private JEditorPane textArea;
	private JLabel labelPicture;
	private JButton buttonClose;

	/**
	 * Create the dialog.
	 */
	public AboutDialogBuilder(Window owner) {
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
		setTitle("builder.dialogAboutTitle");
		setSize(new Dimension(559, 350));
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		textArea = new JEditorPane();
		textArea.setContentType("text/html");
		springLayout.putConstraint(SpringLayout.EAST, textArea, -10, SpringLayout.EAST, getContentPane());
		textArea.setFont(new Font("Monospaced", Font.BOLD, 12));
		textArea.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 20, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, textArea, -40, SpringLayout.SOUTH, getContentPane());
		textArea.setForeground(new Color(0, 0, 139));
		textArea.setOpaque(false);
		getContentPane().add(textArea);
		
		labelPicture = new JLabel("");
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

		// Localize.
		localize();
		// Set icons.
		setIcons();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set text.
		textArea.setText(Resources.getString("builder.messageAbout"));
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
		labelPicture.setIcon(Images.getIcon("splash/splash.png"));
	}
}
