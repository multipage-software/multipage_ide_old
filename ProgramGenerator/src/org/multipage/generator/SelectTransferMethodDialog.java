/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class SelectTransferMethodDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returned values.
	 */
	public static final int CANCELLED = -1;
	public static final int BEFORE = 1;
	public static final int AFTER = 2;
	public static final int SUBAREA = 3;
	
	/**
	 * Dialog serialized states.
	 */
	private static Point location = new Point();
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		location = new Point();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(location);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		location = Utility.readInputStreamObject(inputStream, Point.class);
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * "Only as sub area" flag.
	 */
	private boolean onlyAsSubarea = false;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelQuestion;
	private JRadioButton radioBefore;
	private JRadioButton radioAfter;
	private JRadioButton radioSubarea;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Show dialog.
	 * @param parent
	 * @param action 
	 * @param onlyAsSubarea 
	 * @return
	 */
	public static int showDialog(Component parent, int action, boolean onlyAsSubarea) {
		
		SelectTransferMethodDialog dialog = new SelectTransferMethodDialog(parent, action, onlyAsSubarea);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			// Get selected method.
			return dialog.getSelectedMethod();
		}
		return CANCELLED;
	}
	
	/**
	 * Get selected method.
	 * @return
	 */
	private int getSelectedMethod() {
		
		if (radioBefore.isSelected()) {
			return BEFORE;
		}
		if (radioAfter.isSelected()) {
			return AFTER;
		}
		return SUBAREA;
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 * @param action 
	 * @param onlyAsSubarea 
	 */
	public SelectTransferMethodDialog(Component parent, int action, boolean onlyAsSubarea) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setTitle("title");
		setResizable(false);

		initComponents();
		
		// $hide>>$
		setAction(action, onlyAsSubarea);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 314, 227);
		SpringLayout springLayout = new SpringLayout();
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
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelQuestion = new JLabel("question");
		springLayout.putConstraint(SpringLayout.NORTH, labelQuestion, 25, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelQuestion, 33, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelQuestion);
		
		radioBefore = new JRadioButton("org.multipage.generator.textBeforeTargetArea");
		buttonGroup.add(radioBefore);
		springLayout.putConstraint(SpringLayout.NORTH, radioBefore, 16, SpringLayout.SOUTH, labelQuestion);
		springLayout.putConstraint(SpringLayout.WEST, radioBefore, 65, SpringLayout.WEST, getContentPane());
		getContentPane().add(radioBefore);
		
		radioAfter = new JRadioButton("org.multipage.generator.textAfterTargetArea");
		buttonGroup.add(radioAfter);
		springLayout.putConstraint(SpringLayout.NORTH, radioAfter, 6, SpringLayout.SOUTH, radioBefore);
		springLayout.putConstraint(SpringLayout.WEST, radioAfter, 0, SpringLayout.WEST, radioBefore);
		getContentPane().add(radioAfter);
		
		radioSubarea = new JRadioButton("org.multipage.generator.textSubTargetArea");
		buttonGroup.add(radioSubarea);
		springLayout.putConstraint(SpringLayout.NORTH, radioSubarea, 6, SpringLayout.SOUTH, radioAfter);
		springLayout.putConstraint(SpringLayout.WEST, radioSubarea, 0, SpringLayout.WEST, radioBefore);
		getContentPane().add(radioSubarea);
	}

	/**
	 * Set action. 1 - link, 2 - move.
	 * @param onlyAsSubarea 
	 */
	private void setAction(int action, boolean onlyAsSubarea) {
		
		// Check action.
		if (!(action == 1 || action == 2)) {
			Utility.show(this, "org.multipage.generator.messageUnknownTransferAreaAction");
			
			onCancel();
			return;
		}
		
		// Set component texts.
		setTitle(action == 1 ? "org.multipage.generator.textSelectAreaLinkMethod" : "org.multipage.generator.textSelectAreaMoveMethod");
		labelQuestion.setText(action == 1 ? "org.multipage.generator.textWhereToLinkArea" : "org.multipage.generator.textWhereToMoveArea");
		
		this.onlyAsSubarea = onlyAsSubarea;
		
		// Leave only sub area option.
		if (onlyAsSubarea) {
			
			radioBefore.setEnabled(false);
			radioAfter.setEnabled(false);
		}
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		loadDialog();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelQuestion);
		Utility.localize(radioBefore);
		Utility.localize(radioAfter);
		Utility.localize(radioSubarea);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (location.x == 0 && location.y == 0) {
			Utility.centerOnScreen(this);
		}
		else {
			setLocation(location);
		}
		
		// Initialize radio buttons.
		if (!onlyAsSubarea) {
			radioBefore.setSelected(true);
		}
		else {
			radioSubarea.setSelected(true);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		location = getLocation();
	}
}
