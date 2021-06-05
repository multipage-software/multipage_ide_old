/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 24-03-2021
 *
 */
package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import javax.swing.SwingConstants;

/**
 * 
 * @author vakol
 *
 */
public class LoggingSettingsDialog extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	//$hide>>$
	
	/**
	 * Correct and error borders.
	 */
	private static final Border correctBorder = new LineBorder(Color.BLACK);
	private static final Border errorBorder = new LineBorder(Color.RED);
	
	/**
	 * Lambda callbacks.
	 */
	private Consumer<Boolean> enableGuiLambda = null;
	private Function<Integer, Boolean> setQueuesUpdateIntervalLambda = null;
	private Function<Integer, Boolean> setEventsUpdateIntervalLambda = null;
	private Function<Integer, Boolean> setQueueLimitLambda = null;
	private Function<Integer, Boolean> setEventLimitLambda = null;
	private Function<Integer, Boolean> setMessageLimitLambda = null;
	
	/**
	 * Bounds.
	 */
	private static Rectangle bounds;

	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Initialize this dialog.
	 */
	public static void showDialog(Component parent,
			Integer queuesUpdateIntervalMs, Integer eventsUpdateIntervalMs, 
			Integer queueLimit, Integer messagelimit, Integer eventlimit,
			Consumer<Boolean> enableGuiLambda,
			Function<Integer, Boolean> setQueuesUpdateIntervalLambda,
			Function<Integer, Boolean> setEventsUpdateIntervalLambda,
			Function<Integer, Boolean> setQueueLimitLambda,
			Function<Integer, Boolean> setEventLimitLambda,
			Function<Integer, Boolean> setMessageLimitLambda) {
		
		Window parentWindow = Utility.findWindow(parent);
		LoggingSettingsDialog dialog = new LoggingSettingsDialog(parentWindow);
		
		dialog.textQueuesUpdateInterval.setText(queuesUpdateIntervalMs.toString());
		dialog.textEventsUpdateInterval.setText(eventsUpdateIntervalMs.toString());
		dialog.textQueueLimit.setText(queueLimit.toString());
		dialog.textMessageLimit.setText(messagelimit.toString());
		dialog.textEventLimit.setText(eventlimit.toString());
		
		dialog.enableGuiLambda = enableGuiLambda;
		dialog.setQueuesUpdateIntervalLambda = setQueuesUpdateIntervalLambda;
		dialog.setEventsUpdateIntervalLambda = setEventsUpdateIntervalLambda;
		dialog.setQueueLimitLambda = setQueueLimitLambda;
		dialog.setMessageLimitLambda = setMessageLimitLambda;
		dialog.setEventLimitLambda = setEventLimitLambda;
		
		dialog.setVisible(true);
	}
	
	/**
	 * A flag that signalizes canceling the dialog.
	 */
	private boolean cancelled = false;

	//$hide<<$
	
	/**
	 * Components.
	 */
	private JTextField textEventsUpdateInterval;
	private JLabel labelEventsUpdateItnterval;
	private JButton buttonOk;
	private JTextField textMessageLimit;
	private JTextField textEventLimit;
	private JLabel labelMessageLimit;
	private JLabel labelEventLimit;
	private JButton buttonCancel;
	private JLabel labelQueuesUpdateItnterval;
	private JTextField textQueuesUpdateInterval;
	private JLabel labelQueuesIntervalUnits;
	private JTextField textQueueLimit;
	private JLabel labelQueueLimit;
	
	/**
	 * Create the dialog.
	 */
	public LoggingSettingsDialog(Window parentWindow) {
		super(parentWindow, ModalityType.MODELESS);
		
		initComponents();
		postCreate(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(450, 250));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		setTitle("org.multipage.generator.textLogSettingsTitle");
		setBounds(100, 100, 537, 294);
		
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonOk = new JButton("textOk");
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		getContentPane().add(buttonOk);
		
		labelEventsUpdateItnterval = new JLabel("org.multipage.generator.textLoggedEventsDisplayInterval");
		getContentPane().add(labelEventsUpdateItnterval);
		
		textEventsUpdateInterval = new JTextField();
		textEventsUpdateInterval.setHorizontalAlignment(SwingConstants.TRAILING);
		springLayout.putConstraint(SpringLayout.NORTH, textEventsUpdateInterval, 0, SpringLayout.NORTH, labelEventsUpdateItnterval);
		springLayout.putConstraint(SpringLayout.WEST, textEventsUpdateInterval, 6, SpringLayout.EAST, labelEventsUpdateItnterval);
		textEventsUpdateInterval.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onEventsUpdateIntervalChanged();
			}
		});
		getContentPane().add(textEventsUpdateInterval);
		textEventsUpdateInterval.setColumns(10);
		
		labelMessageLimit = new JLabel("org.multipage.generator.textLoggedEventsMessageLimit");
		springLayout.putConstraint(SpringLayout.NORTH, labelMessageLimit, 10, SpringLayout.SOUTH, textEventsUpdateInterval);
		springLayout.putConstraint(SpringLayout.WEST, labelEventsUpdateItnterval, 0, SpringLayout.WEST, labelMessageLimit);
		springLayout.putConstraint(SpringLayout.WEST, labelMessageLimit, 30, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelMessageLimit);
		
		textMessageLimit = new JTextField();
		textMessageLimit.setHorizontalAlignment(SwingConstants.TRAILING);
		springLayout.putConstraint(SpringLayout.WEST, textMessageLimit, 6, SpringLayout.EAST, labelMessageLimit);
		textMessageLimit.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onSetMessageLimit();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textMessageLimit, 0, SpringLayout.NORTH, labelMessageLimit);
		getContentPane().add(textMessageLimit);
		textMessageLimit.setColumns(10);
		
		labelEventLimit = new JLabel("org.multipage.generator.textLoggedEventsLimit");
		springLayout.putConstraint(SpringLayout.NORTH, labelEventLimit, 10, SpringLayout.SOUTH, textMessageLimit);
		springLayout.putConstraint(SpringLayout.WEST, labelEventLimit, 30, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelEventLimit);
		
		textEventLimit = new JTextField();
		textEventLimit.setHorizontalAlignment(SwingConstants.TRAILING);
		springLayout.putConstraint(SpringLayout.WEST, textEventLimit, 6, SpringLayout.EAST, labelEventLimit);
		textEventLimit.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onSetEventLimit();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textEventLimit, 0, SpringLayout.NORTH, labelEventLimit);
		getContentPane().add(textEventLimit);
		textEventLimit.setColumns(10);
		
		buttonCancel = new JButton("textCancel");
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		JLabel labelEventsIntervalUnits = new JLabel("ms");
		springLayout.putConstraint(SpringLayout.NORTH, labelEventsIntervalUnits, 0, SpringLayout.NORTH, labelEventsUpdateItnterval);
		springLayout.putConstraint(SpringLayout.WEST, labelEventsIntervalUnits, 6, SpringLayout.EAST, textEventsUpdateInterval);
		getContentPane().add(labelEventsIntervalUnits);
		
		labelQueuesUpdateItnterval = new JLabel("org.multipage.generator.textLoggedQueuesDisplayInterval");
		springLayout.putConstraint(SpringLayout.NORTH, labelQueuesUpdateItnterval, 30, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelQueuesUpdateItnterval, 30, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelQueuesUpdateItnterval);
		
		textQueuesUpdateInterval = new JTextField();
		textQueuesUpdateInterval.setHorizontalAlignment(SwingConstants.TRAILING);
		textQueuesUpdateInterval.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onQueuesUpdateIntervalChanged();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, textQueuesUpdateInterval, 0, SpringLayout.NORTH, labelQueuesUpdateItnterval);
		springLayout.putConstraint(SpringLayout.WEST, textQueuesUpdateInterval, 6, SpringLayout.EAST, labelQueuesUpdateItnterval);
		textQueuesUpdateInterval.setColumns(10);
		getContentPane().add(textQueuesUpdateInterval);
		
		labelQueuesIntervalUnits = new JLabel("ms");
		springLayout.putConstraint(SpringLayout.NORTH, labelQueuesIntervalUnits, 0, SpringLayout.NORTH, labelQueuesUpdateItnterval);
		springLayout.putConstraint(SpringLayout.WEST, labelQueuesIntervalUnits, 6, SpringLayout.EAST, textQueuesUpdateInterval);
		getContentPane().add(labelQueuesIntervalUnits);
		
		labelQueueLimit = new JLabel("org.multipage.generator.textLoggedQueueLimit");
		springLayout.putConstraint(SpringLayout.NORTH, labelQueueLimit, 10, SpringLayout.SOUTH, textQueuesUpdateInterval);
		springLayout.putConstraint(SpringLayout.WEST, labelQueueLimit, 0, SpringLayout.WEST, labelEventsUpdateItnterval);
		getContentPane().add(labelQueueLimit);
		
		textQueueLimit = new JTextField();
		textQueueLimit.setHorizontalAlignment(SwingConstants.TRAILING);
		textQueueLimit.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onSetQueueLimit();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, labelEventsUpdateItnterval, 10, SpringLayout.SOUTH, textQueueLimit);
		springLayout.putConstraint(SpringLayout.NORTH, textQueueLimit, 6, SpringLayout.SOUTH, textQueuesUpdateInterval);
		springLayout.putConstraint(SpringLayout.WEST, textQueueLimit, 6, SpringLayout.EAST, labelQueueLimit);
		textQueueLimit.setColumns(10);
		getContentPane().add(textQueueLimit);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		loadDialog();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelQueuesUpdateItnterval);
		Utility.localize(labelEventsUpdateItnterval);
		Utility.localize(labelQueueLimit);
		Utility.localize(labelMessageLimit);
		Utility.localize(labelEventLimit);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
			bounds = getBounds();
		}
		else {
			setBounds(bounds);
		}
		
		textEventsUpdateInterval.setBorder(new LineBorder(Color.BLACK));
		textMessageLimit.setBorder(new LineBorder(Color.BLACK));
		textEventLimit.setBorder(new LineBorder(Color.BLACK));
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}

	/**
	 * On close dialog.
	 */
	protected void onClose() {
		
		// Set flag.
		cancelled = true;
		
		// Save dialog state.
		saveDialog();
		
		// Close dialog.
		dispose();
	}

	/**
	 * On OK button.
	 */
	protected void onOk() {
		
		// If there is an input error, do not exit the dialog.
		if (isInputError()) {
			Utility.show(this, "org.multipage.generator.messageLoggedEventsSettingsError");
			return;
		}
		
		// Save dialog state.
		saveDialog();
		
		// Close dialog.
		dispose();
	}
	
	/**
	 * On queues update interval.
	 */
	protected boolean onQueuesUpdateIntervalChanged() {
		
		// Check cancel.
		if (cancelled) {
			return false;
		}
		
		// Get update interval.
		String limitText = textQueuesUpdateInterval.getText();
		
		// Set the interval.
		Integer intervalMs = null;
		try {
			intervalMs = Integer.parseInt(limitText);
		}
		catch (Exception e) {
		}
		Boolean success = setQueuesUpdateIntervalLambda.apply(intervalMs);
		if (success == null) {
			success = false;
		}
		
		// Set control border.
		textQueuesUpdateInterval.setBorder(success ? correctBorder : errorBorder);
		
		return success;
	}
	
	/**
	 * On events update interval.
	 */
	protected boolean onEventsUpdateIntervalChanged() {
		
		// Check cancel.
		if (cancelled) {
			return false;
		}
		
		// Get update interval.
		String limitText = textEventsUpdateInterval.getText();
		
		// Set the interval.
		Integer intervalMs = null;
		try {
			intervalMs = Integer.parseInt(limitText);
		}
		catch (Exception e) {
		}
		Boolean success = setEventsUpdateIntervalLambda.apply(intervalMs);
		if (success == null) {
			success = false;
		}
		
		// Set control border.
		textEventsUpdateInterval.setBorder(success ? correctBorder : errorBorder);
		
		return success;
	}
	
	/**
	 * On queue limit.
	 */
	protected boolean onSetQueueLimit() {
		
		// Check cancel.
		if (cancelled) {
			return false;
		}
		
		// Get limit.
		String limitText = textQueueLimit.getText();
		
		// Set the limit.
		Integer newLimit = null;
		try {
			newLimit = Integer.parseInt(limitText);
		}
		catch (Exception e) {
		}
		Boolean success = setQueueLimitLambda.apply(newLimit);
		if (success == null) {
			success = false;
		}
		
		// Set control border.
		textQueueLimit.setBorder(success ? correctBorder : errorBorder);
		
		return success;
	}
	
	/**
	 * On message limit.
	 */
	protected boolean onSetMessageLimit() {
		
		// Check cancel.
		if (cancelled) {
			return false;
		}
		
		// Get limit.
		String limitText = textMessageLimit.getText();
		
		// Set the limit.
		Integer newLimit = null;
		try {
			newLimit = Integer.parseInt(limitText);
		}
		catch (Exception e) {
		}
		Boolean success = setMessageLimitLambda.apply(newLimit);
		if (success == null) {
			success = false;
		}
		
		// Set control border.
		textMessageLimit.setBorder(success ? correctBorder : errorBorder);
		
		return success;
	}
	
	/**
	 * On event limit.
	 */
	protected boolean onSetEventLimit() {
		
		// Check cancel.
		if (cancelled) {
			return false;
		}
		
		// Get limit.
		String limitText = textEventLimit.getText();
		
		// Set the limit.
		Integer newLimit = null;
		try {
			newLimit = Integer.parseInt(limitText);
		}
		catch (Exception e) {
		}
		Boolean success = setEventLimitLambda.apply(newLimit);
		if (success == null) {
			success = false;
		}
		
		// Set control border.
		textEventLimit.setBorder(success ? correctBorder : errorBorder);
		
		return success;
	}
	
	/**
	 * Check if an input error exists.
	 * @return
	 */
	private boolean isInputError() {
		
		// Disable GUI messages.
		enableGuiLambda.accept(false);
		
		// Check inputs.
		boolean success = onQueuesUpdateIntervalChanged();
		if (success) {
			
			success = onEventsUpdateIntervalChanged();
			if (success) {
				
				success = onSetQueueLimit();
				if (success) {
					
					success = onSetMessageLimit();
					if (success) {
						
						success = onSetEventLimit();
					}
				}
			}
		}
	
		// Enable GUI messages.
		enableGuiLambda.accept(true);
		
		return !success;
	}
}		