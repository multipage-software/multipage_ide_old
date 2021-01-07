/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * 
 * @author
 *
 */
public class Progress2Dialog<TOutput> extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Worker thread.
	 */
	private SwingWorkerHelper<TOutput> swingWorker;

	/**
	 * Worker thread result state.
	 */
	protected ProgressResult resultState = ProgressResult.NONE;

	/**
	 * Components.
	 */
	private JLabel labelMessage;
	private JProgressBar progressBar1;
	private JLabel labelNote;
	private JButton buttonCancel;
	private JPanel panel;
	private Component horizontalGlue1;
	private Component horizontalGlue2;
	private JCheckBox checkKill;
	private JProgressBar progressBar2;

	/**
	 * Create the dialog.
	 * @param title 
	 * @param message 
	 */
	public Progress2Dialog(Component parent, String title, String message) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setTitle(title);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				onWindowOpened();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);

		// Initialize components.
		initComponents();
		// Post creation.
		// $hide>>$
		postCreation(message);
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(280, 230));
		setBounds(100, 100, 342, 243);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelMessage = new JLabel("message");
		springLayout.putConstraint(SpringLayout.WEST, labelMessage, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelMessage, -10, SpringLayout.EAST, getContentPane());
		labelMessage.setFont(new Font("Tahoma", Font.BOLD, 13));
		springLayout.putConstraint(SpringLayout.NORTH, labelMessage, 20, SpringLayout.NORTH, getContentPane());
		labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		labelMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		getContentPane().add(labelMessage);
		
		progressBar1 = new JProgressBar();
		springLayout.putConstraint(SpringLayout.NORTH, progressBar1, 20, SpringLayout.SOUTH, labelMessage);
		springLayout.putConstraint(SpringLayout.WEST, progressBar1, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, progressBar1, 52, SpringLayout.NORTH, labelMessage);
		springLayout.putConstraint(SpringLayout.EAST, progressBar1, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(progressBar1);
		
		labelNote = new JLabel("note");
		springLayout.putConstraint(SpringLayout.WEST, labelNote, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelNote, 0, SpringLayout.EAST, labelMessage);
		labelNote.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(labelNote);
		
		panel = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, labelMessage);
		springLayout.putConstraint(SpringLayout.SOUTH, panel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, labelMessage);
		getContentPane().add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		horizontalGlue1 = Box.createHorizontalGlue();
		panel.add(horizontalGlue1);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMinimumSize(new Dimension(80, 25));
		buttonCancel.setMaximumSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonCancel);
		springLayout.putConstraint(SpringLayout.WEST, buttonCancel, 78, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		
		horizontalGlue2 = Box.createHorizontalGlue();
		panel.add(horizontalGlue2);
		
		checkKill = new JCheckBox("org.multipage.gui.textKillOperation");
		springLayout.putConstraint(SpringLayout.NORTH, checkKill, 6, SpringLayout.SOUTH, labelNote);
		springLayout.putConstraint(SpringLayout.WEST, checkKill, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, checkKill, 0, SpringLayout.EAST, labelMessage);
		checkKill.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(checkKill);
		
		progressBar2 = new JProgressBar();
		springLayout.putConstraint(SpringLayout.NORTH, labelNote, 15, SpringLayout.SOUTH, progressBar2);
		springLayout.putConstraint(SpringLayout.SOUTH, progressBar2, 22, SpringLayout.SOUTH, progressBar1);
		springLayout.putConstraint(SpringLayout.NORTH, progressBar2, 6, SpringLayout.SOUTH, progressBar1);
		springLayout.putConstraint(SpringLayout.WEST, progressBar2, 0, SpringLayout.WEST, labelMessage);
		springLayout.putConstraint(SpringLayout.EAST, progressBar2, 0, SpringLayout.EAST, labelMessage);
		getContentPane().add(progressBar2);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		// Cancel worker thread.
		if (swingWorker != null) {
			
			// If to kill the thread...
			if (checkKill.isSelected()) {
				// Ask user.
				if (JOptionPane.showConfirmDialog(this,
						Resources.getString("org.multipage.gui.messageShouldKillThread"))
						== JOptionPane.YES_OPTION) {
					
					// Cancel the thread.
					swingWorker.cancel(true);
				}
			}
			else {
				swingWorker.scheduleCancel();
			}
		}
	}

	/**
	 * Post creation.
	 * @param message 
	 */
	private void postCreation(String message) {
		
		// Set message.
		labelMessage.setText(message);
		// Center dialog.
		Utility.centerOnScreen(this);
		// Localize.
		localize();
		// Set icons.
		setIcons();
		// Initialize progress.
		onProgressValue(0);
	}

	/**
	 * Localize.
	 */
	private void localize() {

		Utility.localize(buttonCancel);
		Utility.localize(checkKill);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/gui/images/progress.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * On progress value.
	 * @param progressValue
	 */
	protected void onProgressValue(int progressValue) {

		// Set progress bar.
		progressBar1.setValue(progressValue);
		// Set note.
		labelNote.setText(String.format(
				Resources.getString("org.multipage.gui.textProgressNote"), progressValue));
	}

	/**
	 * On progress 2 value.
	 * @param progressValue
	 */
	protected void onProgress2Value(int progress2Value) {

		// Set progress bar.
		progressBar2.setValue(progress2Value);
	}

	/**
	 * Execute the dialog with given thread.
	 */
	public ProgressResult execute(SwingWorkerHelper<TOutput> swingWorker) {

		// Set worker thread.
		this.swingWorker = swingWorker;
		
		// Show the dialog.
		setVisible(true);
		
		// Return result.
		return resultState;
	}

	/**
	 * On window opened.
	 */
	protected void onWindowOpened() {
		
		// Execute the worker thread.
		if (swingWorker != null) {
			
			// Set property change listener.
			this.swingWorker.addPropertyChangeListener(new PropertyChangeListener() {
				// On property change.
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					// If it is a "progress" property.
					if (evt.getPropertyName().equals("progress")) {
						
						// Get progress value.
						int progressValue = (Integer) evt.getNewValue();
						// Set progress bar.
						onProgressValue(progressValue);
					}
					else if (evt.getPropertyName().equals("progress2")) {
						
						// Get progress value.
						int progress2Value = (Integer) evt.getNewValue();
						// Set progress bar.
						onProgress2Value(progress2Value);
					}
				}
			});
			
			// Listen for result to close the dialog.
			swingWorker.addResultChangeListener(new PropertyChangeListener() {
				// Result changes.
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					// Check property name.
					if (evt.getPropertyName().equals(SwingWorkerHelper.resultPropertyName)) {
						// Set result state.
						resultState = (ProgressResult) evt.getNewValue();
						// Close dialog.
						dispose();
					}
				}
			});
			swingWorker.execute();
		}
		else {
			resultState = ProgressResult.NONE;
			return;
		}
	}

	/**
	 * Get output.
	 * @return
	 */
	public TOutput getOutput() {

		if (swingWorker != null) {
			return swingWorker.getOutput();
		}
		else {
			return null;
		}
	}

	/**
	 * Gets exception.
	 * @return
	 */
	public Exception getException() {

		if (swingWorker != null) {
			return swingWorker.getException();
		}
		else {
			return null;
		}
	}
}
