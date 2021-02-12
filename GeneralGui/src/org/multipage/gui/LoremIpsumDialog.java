/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;

import org.multipage.util.Resources;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 
 * @author
 *
 */
public class LoremIpsumDialog extends JDialog {
	
	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Maximum paragraphs.
	 */
	private static final int MAXIMUM_PARAGRAPHS = 50;
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * Lorem Ipsum generator object.
	 */
	private LoremIpsum loremIpsum;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelParagraphs;
	private JTextField textParagraphs;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JLabel labelError;
	private final JCheckBox checkUseP = new JCheckBox("org.multipage.gui.textLoremIpsumUseP");

	/**
	 * Create the dialog.
	 */
	public LoremIpsumDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.gui.textLoremIpsumDialog");
		setBounds(100, 100, 310, 323);
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.WEST, checkUseP, 49, SpringLayout.WEST, getContentPane());
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		labelParagraphs = new JLabel("org.multipage.gui.textLoremIpsumParagraphs");
		springLayout.putConstraint(SpringLayout.NORTH, labelParagraphs, 22, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelParagraphs, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelParagraphs);
		
		textParagraphs = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textParagraphs, 0, SpringLayout.NORTH, labelParagraphs);
		springLayout.putConstraint(SpringLayout.WEST, textParagraphs, 6, SpringLayout.EAST, labelParagraphs);
		getContentPane().add(textParagraphs);
		textParagraphs.setColumns(10);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 82, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, checkUseP, -6, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		labelError = new JLabel("text");
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, labelError);
		springLayout.putConstraint(SpringLayout.NORTH, labelError, 235, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelError, 10, SpringLayout.WEST, getContentPane());
		labelError.setForeground(Color.RED);
		getContentPane().add(labelError);
		checkUseP.setSelected(true);
		checkUseP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onParagraphsChange();
			}
		});
		getContentPane().add(checkUseP);
	}

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static String showDialog(Component parent) {
		
		LoremIpsumDialog dialog = new LoremIpsumDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			return dialog.getLoremIpsum();
		}
		
		return null;
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);
		localize();
		setIcons();
		
		initLoremIpsum();
	}

	/**
	 * Initialize.
	 */
	private void initLoremIpsum() {
		
		loremIpsum = new LoremIpsum();
		
		textParagraphs.setText("2");
		
		onParagraphsChange();
		
		Utility.setTextChangeListener(textParagraphs, new Runnable() {
			@Override
			public void run() {
				
				onParagraphsChange();
			}
		});
	}

	/**
	 * On paragraphs change.
	 */
	private void onParagraphsChange() {
		
		// Get number of paragraphs
		String paragraphsString = textParagraphs.getText();
		Integer paragraphsNumber = null;
		
		try {
			paragraphsNumber = Integer.parseInt(paragraphsString);
		}
		catch (Exception e) {
		}
		
		if (paragraphsNumber != null && paragraphsNumber >= 1 && paragraphsNumber <= MAXIMUM_PARAGRAPHS) {
			
			// Get Lorem Ipsum text.
			String text = loremIpsum.getParagraphs(paragraphsNumber, checkUseP.isSelected());
			textPane.setText(text);
			
			labelError.setText("");
		}
		else {
			textPane.setText("");
			labelError.setText(String.format(Resources.getString("org.multipage.gui.textLoremIpsumParagraphsNumberError"), MAXIMUM_PARAGRAPHS));
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
		Utility.localize(labelParagraphs);
		Utility.localize(checkUseP);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOK() {
		
		confirm = true;
		dispose();
	}

	/**
	 * Get Lorem Ipsum.
	 * @return
	 */
	private String getLoremIpsum() {
		
		return textPane.getText();
	}
}
