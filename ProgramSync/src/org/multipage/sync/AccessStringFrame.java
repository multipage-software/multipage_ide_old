/*
 * Copyright 2010-2020 (C) Vaclav Kolarcik
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.Utility;
import javax.swing.JTextField;

/**
 * 
 * @author user
 *
 */
public class AccessStringFrame extends JFrame {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Controls.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelAreaServerUrl;
	private JComboBox<String> comboBoxUrl;
	private JLabel labelPassword;
	private JPasswordField textPassword;
	private JLabel labelUser;
	private JTextField textUser;

	/**
	 * Create the frame.
	 */
	public AccessStringFrame() {

		initComponents();
		postCreate(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		setAlwaysOnTop(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("org.multipage.sync.titleAreaServerAccess");
		setBounds(100, 100, 397, 283);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		labelAreaServerUrl = new JLabel("org.multipage.sync.textAreaServerUrl");
		springLayout.putConstraint(SpringLayout.NORTH, labelAreaServerUrl, 30, SpringLayout.NORTH, getContentPane());
		labelAreaServerUrl.setHorizontalAlignment(SwingConstants.LEFT);
		springLayout.putConstraint(SpringLayout.WEST, labelAreaServerUrl, 50, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelAreaServerUrl, -50, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelAreaServerUrl);
		
		comboBoxUrl = new JComboBox<String>();
		springLayout.putConstraint(SpringLayout.NORTH, comboBoxUrl, 3, SpringLayout.SOUTH, labelAreaServerUrl);
		springLayout.putConstraint(SpringLayout.WEST, comboBoxUrl, 50, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, comboBoxUrl, -50, SpringLayout.EAST, getContentPane());
		comboBoxUrl.setPreferredSize(new Dimension(300, 20));
		comboBoxUrl.setEditable(true);
		getContentPane().add(comboBoxUrl);
		
		labelPassword = new JLabel("org.multipage.sync.textAreaServerPassword");
		labelPassword.setHorizontalAlignment(SwingConstants.LEFT);
		springLayout.putConstraint(SpringLayout.WEST, labelPassword, 100, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, labelPassword, -100, SpringLayout.EAST, getContentPane());
		getContentPane().add(labelPassword);
		
		textPassword = new JPasswordField();
		springLayout.putConstraint(SpringLayout.NORTH, textPassword, 3, SpringLayout.SOUTH, labelPassword);
		springLayout.putConstraint(SpringLayout.WEST, textPassword, 100, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textPassword, -100, SpringLayout.EAST, getContentPane());
		getContentPane().add(textPassword);
		textPassword.setColumns(20);
		
		labelUser = new JLabel("org.multipage.sync.textAreaServerUser");
		springLayout.putConstraint(SpringLayout.NORTH, labelUser, 16, SpringLayout.SOUTH, comboBoxUrl);
		springLayout.putConstraint(SpringLayout.WEST, labelUser, 0, SpringLayout.WEST, labelPassword);
		labelUser.setHorizontalAlignment(SwingConstants.LEFT);
		getContentPane().add(labelUser);
		
		textUser = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, textUser, 100, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textUser, -100, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, labelPassword, 16, SpringLayout.SOUTH, textUser);
		springLayout.putConstraint(SpringLayout.NORTH, textUser, 0, SpringLayout.SOUTH, labelUser);
		textUser.setColumns(20);
		getContentPane().add(textUser);
	}
	
	/**
	 * Show frame.
	 * @return
	 */
	public static String showFrame()
		throws Exception {
		
		AccessStringFrame frame = new AccessStringFrame();
		frame.setVisible(true);
		
		String accessString = frame.getAccessString();
		return accessString;
	}
	
	/**
	 * Get access string.
	 * @return
	 */
	private String getAccessString() {
		
		String url = ((JTextComponent) comboBoxUrl.getEditor().getEditorComponent()).getText();
		Utility.putComboBoxItem(comboBoxUrl, url);
		
		String user = textUser.getText();
		String password = new String(textPassword.getPassword());
		
		String accessString = String.format(SyncMain.accessStringFormat, url, user, password);
		return accessString;
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
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelAreaServerUrl);
		Utility.localize(labelUser);
		Utility.localize(labelPassword);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/gui/images/main_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		
		// Initialize empty URL.
		initilalizeEmptyUrl();
	}
	
	/**
	 * Initialize empty combvo box.
	 */
	private void initilalizeEmptyUrl() {
		
		int count = comboBoxUrl.getItemCount();
		if (count <= 0) {
			comboBoxUrl.addItem("http://localhost:8080/?a");
		}
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On Cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
}
