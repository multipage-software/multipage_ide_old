/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.Obj;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * 
 * @author
 *
 */
public class AskConstructorHolder extends JDialog {

	// $hide>>$
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
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton radioNewName;
	private JRadioButton radioLink;
	private JTextField textName;
	private JLabel labelName;

	/**
	 * Show dialog.
	 * @param parent
	 * @param name 
	 * @param isLink 
	 * @param defaultName 
	 * @return
	 */
	public static boolean showDialog(Component parent, Obj<Boolean> isLink,
			Obj<String> name, String defaultName) {
		
		AskConstructorHolder dialog = new AskConstructorHolder(parent);
		
		dialog.textName.setText(defaultName);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			isLink.ref = dialog.radioLink.isSelected();
			if (!isLink.ref) {
				name.ref = dialog.textName.getText();
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public AskConstructorHolder(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setMinimumSize(new Dimension(450, 255));

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("builder.messageInsertConstructorName");
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
		setBounds(100, 100, 450, 255);
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
		
		radioNewName = new JRadioButton("builder.textNewConstructor");
		springLayout.putConstraint(SpringLayout.NORTH, radioNewName, 43, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, radioNewName, 78, SpringLayout.WEST, getContentPane());
		radioNewName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setComponentsState();
			}
		});
		radioNewName.setSelected(true);
		buttonGroup.add(radioNewName);
		getContentPane().add(radioNewName);
		
		textName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.EAST, textName, -60, SpringLayout.EAST, getContentPane());
		getContentPane().add(textName);
		textName.setColumns(10);
		
		labelName = new JLabel("builder.textInsertNewConstructorName");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 5, SpringLayout.SOUTH, radioNewName);
		springLayout.putConstraint(SpringLayout.WEST, labelName, 70, SpringLayout.WEST, radioNewName);
		springLayout.putConstraint(SpringLayout.NORTH, textName, 1, SpringLayout.SOUTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textName, 0, SpringLayout.WEST, labelName);
		getContentPane().add(labelName);
		
		radioLink = new JRadioButton("builder.textConstructorLink");
		springLayout.putConstraint(SpringLayout.NORTH, radioLink, 20, SpringLayout.SOUTH, textName);
		springLayout.putConstraint(SpringLayout.WEST, radioLink, 0, SpringLayout.WEST, radioNewName);
		getContentPane().add(radioLink);
		radioLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setComponentsState();
			}
		});
		buttonGroup.add(radioLink);
	}
	
	/**
	 * On window opened.
	 */
	protected void onWindowOpened() {
		
		textName.grabFocus();
		textName.selectAll();
	}

	/**
	 * Set components state.
	 */
	protected void setComponentsState() {
		
		boolean isNewName = radioNewName.isSelected();
		labelName.setEnabled(isNewName);
		textName.setEnabled(isNewName);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		setComponentsState();
		
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
		Utility.localize(radioNewName);
		Utility.localize(radioLink);
		Utility.localize(labelName);
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
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
}
