/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
public class ConfirmCheckBox extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Confirmed flag.
	 */
	private boolean confirmed = false;
	private JCheckBox checkBox;

	/**
	 * Selection.
	 */
	private Obj<Boolean> selected;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Launch the dialog.
	 */
	public static boolean showConfirmDialog(JFrame parentFrame, String message,
			Obj<Boolean> selected) {

		ConfirmCheckBox dialog = new ConfirmCheckBox(parentFrame, message, selected);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);

		return dialog.confirmed;
	}

	/**
	 * Create the dialog.
	 * @param selected 
	 * @param message 
	 * @param parentFrame 
	 */
	public ConfirmCheckBox(JFrame parentFrame, String message, Obj<Boolean> selected) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.selected = selected;
		initComponents();
		postCreation(message);
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		setTitle("org.multipage.generator.textConfirmDialog");
		setBounds(100, 100, 380, 139);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 18));
		
		checkBox = new JCheckBox("");
		contentPanel.add(checkBox);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("textOk");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOk();
					}
				});
				okButton.setMargin(new Insets(2, 4, 2, 4));
				okButton.setHorizontalAlignment(SwingConstants.LEFT);
				okButton.setPreferredSize(new Dimension(80, 25));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("textCancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancel();
					}
				});
				cancelButton.setMargin(new Insets(2, 4, 2, 4));
				cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
				cancelButton.setPreferredSize(new Dimension(80, 25));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/**
	 * On cancel button.
	 */
	protected void onCancel() {

		confirmed = false;
		dispose();
	}

	/**
	 * On OK button.
	 */
	protected void onOk() {

		selected.ref = checkBox.isSelected();
		confirmed = true;
		dispose();
	}

	/**
	 * Post creation.
	 * @param message 
	 */
	private void postCreation(String message) {
		
		// Center dialog.
		Utility.centerOnScreen(this);

		// Localize dialog.
		localize();
		
		checkBox.setText(message);
		checkBox.setSelected(selected.ref);
		
		// Set icons.
		okButton.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		cancelButton.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
	}

	/**
	 * Localize dialog.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(okButton);
		Utility.localize(cancelButton);
	}
}
