/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.maclan.server.BrowserParameters;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;
import org.multipage.util.SimpleMethodRef;


/**
 * 
 * @author
 *
 */
public class BrowserParametersDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized browser parameters.
	 */
	public static BrowserParameters serializedBrowserParameters = new BrowserParameters();
	
	/**
	 * Get parameters.
	 */
	public static BrowserParameters getParameters() {
		
		return serializedBrowserParameters;
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {
		
		serializedBrowserParameters = (BrowserParameters) inputStream.readObject();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		outputStream.writeObject(serializedBrowserParameters);
	}
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelFolder;
	private TextFieldEx textFolder;
	private JLabel labelHomePage;
	private TextFieldEx textHomePage;
	private JLabel labelBrowserTitle;
	private TextFieldEx textBrowserTitle;
	private JLabel labelBrowserMessage;
	private TextFieldEx textBrowserMessage;
	private JLabel labelWindowSize;
	private JLabel labelWindowWidth;
	private TextFieldEx textWindowWidth;
	private JLabel labelMultiply;
	private JLabel labelWindowHeight;
	private TextFieldEx textWindowHeight;
	private JCheckBox checkBrowserMaximized;
	private JButton buttonDefault;
	private JLabel labelWidthError;
	private JLabel labelHeightError;
	private final JButton buttonDefaultHomePage = new JButton("");
	private JCheckBox checkCreateAutorun;
	private JLabel labelBrowserProgramName;
	private TextFieldEx textBrowserProgramName;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static void showDialog(Component parent) {

		BrowserParametersDialog dialog = new BrowserParametersDialog(Utility.findWindow(parent));
		dialog.setVisible(true);
	}

	/**
	 * Constructor.
	 * @param parentWindow
	 */
	public BrowserParametersDialog(Window parentWindow) {
		
		super(parentWindow, ModalityType.DOCUMENT_MODAL);
		
		initComponents();
		
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(486, 320));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 487, 430);
		setTitle("org.multipage.generator.textRedderBrowserParamaters");
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.WEST, buttonDefaultHomePage, -36, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonDefaultHomePage, -10, SpringLayout.EAST, getContentPane());
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
		
		labelFolder = new JLabel("org.multipage.generator.textFolder");
		springLayout.putConstraint(SpringLayout.NORTH, labelFolder, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelFolder, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelFolder);
		
		textFolder = new TextFieldEx();
		textFolder.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textFolder, 6, SpringLayout.SOUTH, labelFolder);
		springLayout.putConstraint(SpringLayout.WEST, textFolder, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textFolder, 182, SpringLayout.WEST, getContentPane());
		getContentPane().add(textFolder);
		textFolder.setColumns(10);
		
		labelHomePage = new JLabel("org.multipage.generator.textHomePage");
		springLayout.putConstraint(SpringLayout.NORTH, labelHomePage, 0, SpringLayout.NORTH, labelFolder);
		springLayout.putConstraint(SpringLayout.WEST, labelHomePage, 10, SpringLayout.EAST, textFolder);
		getContentPane().add(labelHomePage);
		
		textHomePage = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.SOUTH, buttonDefaultHomePage, -1, SpringLayout.SOUTH, textHomePage);
		springLayout.putConstraint(SpringLayout.EAST, textHomePage, 0, SpringLayout.WEST, buttonDefaultHomePage);
		springLayout.putConstraint(SpringLayout.NORTH, buttonDefaultHomePage, 0, SpringLayout.NORTH, textHomePage);
		textHomePage.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textHomePage, 6, SpringLayout.SOUTH, labelHomePage);
		springLayout.putConstraint(SpringLayout.WEST, textHomePage, 10, SpringLayout.EAST, textFolder);
		getContentPane().add(textHomePage);
		textHomePage.setColumns(10);
		
		labelBrowserTitle = new JLabel("org.multipage.generator.textBrowserTitle");
		springLayout.putConstraint(SpringLayout.NORTH, labelBrowserTitle, 6, SpringLayout.SOUTH, textFolder);
		springLayout.putConstraint(SpringLayout.WEST, labelBrowserTitle, 0, SpringLayout.WEST, labelFolder);
		getContentPane().add(labelBrowserTitle);
		
		textBrowserTitle = new TextFieldEx();
		textBrowserTitle.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textBrowserTitle, 6, SpringLayout.SOUTH, labelBrowserTitle);
		springLayout.putConstraint(SpringLayout.WEST, textBrowserTitle, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textBrowserTitle, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textBrowserTitle);
		textBrowserTitle.setColumns(10);
		
		labelBrowserMessage = new JLabel("org.multipage.generator.textDefaultBrowserMessage");
		springLayout.putConstraint(SpringLayout.NORTH, labelBrowserMessage, 6, SpringLayout.SOUTH, textBrowserTitle);
		springLayout.putConstraint(SpringLayout.WEST, labelBrowserMessage, 0, SpringLayout.WEST, labelFolder);
		getContentPane().add(labelBrowserMessage);
		
		textBrowserMessage = new TextFieldEx();
		textBrowserMessage.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textBrowserMessage, 6, SpringLayout.SOUTH, labelBrowserMessage);
		springLayout.putConstraint(SpringLayout.WEST, textBrowserMessage, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textBrowserMessage, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textBrowserMessage);
		textBrowserMessage.setColumns(10);
		
		labelWindowSize = new JLabel("org.multipage.generator.textBrowserWindowSize");
		springLayout.putConstraint(SpringLayout.NORTH, labelWindowSize, 40, SpringLayout.SOUTH, textBrowserMessage);
		springLayout.putConstraint(SpringLayout.WEST, labelWindowSize, 0, SpringLayout.WEST, labelFolder);
		getContentPane().add(labelWindowSize);
		
		labelWindowWidth = new JLabel("org.multipage.generator.textBrowserWindowWidth");
		springLayout.putConstraint(SpringLayout.NORTH, labelWindowWidth, 0, SpringLayout.NORTH, labelWindowSize);
		springLayout.putConstraint(SpringLayout.WEST, labelWindowWidth, 6, SpringLayout.EAST, labelWindowSize);
		getContentPane().add(labelWindowWidth);
		
		textWindowWidth = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWindowWidth, -3, SpringLayout.NORTH, labelWindowWidth);
		springLayout.putConstraint(SpringLayout.WEST, textWindowWidth, 6, SpringLayout.EAST, labelWindowWidth);
		springLayout.putConstraint(SpringLayout.EAST, textWindowWidth, 60, SpringLayout.EAST, labelWindowWidth);
		textWindowWidth.setPreferredSize(new Dimension(6, 22));
		getContentPane().add(textWindowWidth);
		textWindowWidth.setColumns(10);
		
		labelMultiply = new JLabel("x");
		springLayout.putConstraint(SpringLayout.NORTH, labelMultiply, 0, SpringLayout.NORTH, labelWindowSize);
		springLayout.putConstraint(SpringLayout.WEST, labelMultiply, 6, SpringLayout.EAST, textWindowWidth);
		getContentPane().add(labelMultiply);
		
		labelWindowHeight = new JLabel("org.multipage.generator.textBrowserWindowHeight");
		springLayout.putConstraint(SpringLayout.NORTH, labelWindowHeight, 0, SpringLayout.NORTH, labelWindowSize);
		springLayout.putConstraint(SpringLayout.WEST, labelWindowHeight, 6, SpringLayout.EAST, labelMultiply);
		getContentPane().add(labelWindowHeight);
		
		textWindowHeight = new TextFieldEx();
		textWindowHeight.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textWindowHeight, -3, SpringLayout.NORTH, labelWindowHeight);
		springLayout.putConstraint(SpringLayout.WEST, textWindowHeight, 6, SpringLayout.EAST, labelWindowHeight);
		springLayout.putConstraint(SpringLayout.EAST, textWindowHeight, 60, SpringLayout.EAST, labelWindowHeight);
		getContentPane().add(textWindowHeight);
		textWindowHeight.setColumns(10);
		
		checkBrowserMaximized = new JCheckBox("org.multipage.generator.textBrowserWindowMaximized");
		springLayout.putConstraint(SpringLayout.NORTH, checkBrowserMaximized, -4, SpringLayout.NORTH, labelWindowSize);
		springLayout.putConstraint(SpringLayout.WEST, checkBrowserMaximized, 16, SpringLayout.EAST, textWindowHeight);
		getContentPane().add(checkBrowserMaximized);
		
		buttonDefault = new JButton("org.multipage.generator.textDefault");
		springLayout.putConstraint(SpringLayout.NORTH, buttonDefault, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.WEST, buttonDefault, 0, SpringLayout.WEST, labelFolder);
		buttonDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRestoreDefault();
			}
		});
		buttonDefault.setPreferredSize(new Dimension(80, 25));
		buttonDefault.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonDefault);
		
		labelWidthError = new JLabel("error");
		labelWidthError.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.NORTH, labelWidthError, 0, SpringLayout.SOUTH, textWindowWidth);
		labelWidthError.setForeground(Color.RED);
		springLayout.putConstraint(SpringLayout.WEST, labelWidthError, 0, SpringLayout.WEST, textWindowWidth);
		springLayout.putConstraint(SpringLayout.EAST, labelWidthError, 0, SpringLayout.EAST, textWindowWidth);
		getContentPane().add(labelWidthError);
		
		labelHeightError = new JLabel("error");
		labelHeightError.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.NORTH, labelHeightError, 0, SpringLayout.SOUTH, textWindowHeight);
		labelHeightError.setForeground(Color.RED);
		springLayout.putConstraint(SpringLayout.WEST, labelHeightError, 0, SpringLayout.WEST, textWindowHeight);
		springLayout.putConstraint(SpringLayout.EAST, labelHeightError, 0, SpringLayout.EAST, textWindowHeight);
		getContentPane().add(labelHeightError);
		buttonDefaultHomePage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDefaultHomePage();
			}
		});
		buttonDefaultHomePage.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonDefaultHomePage);
		
		checkCreateAutorun = new JCheckBox("org.multipage.generator.textCreateAutorun");
		springLayout.putConstraint(SpringLayout.NORTH, checkCreateAutorun, 33, SpringLayout.SOUTH, labelWindowSize);
		springLayout.putConstraint(SpringLayout.WEST, checkCreateAutorun, 150, SpringLayout.WEST, labelFolder);
		getContentPane().add(checkCreateAutorun);
		
		labelBrowserProgramName = new JLabel("org.multipage.generator.textBrowserProgramName");
		springLayout.putConstraint(SpringLayout.NORTH, labelBrowserProgramName, 18, SpringLayout.SOUTH, checkCreateAutorun);
		springLayout.putConstraint(SpringLayout.WEST, labelBrowserProgramName, 0, SpringLayout.WEST, labelFolder);
		springLayout.putConstraint(SpringLayout.SOUTH, labelBrowserProgramName, 32, SpringLayout.SOUTH, checkCreateAutorun);
		getContentPane().add(labelBrowserProgramName);
		
		textBrowserProgramName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textBrowserProgramName, 6, SpringLayout.SOUTH, labelBrowserProgramName);
		springLayout.putConstraint(SpringLayout.WEST, textBrowserProgramName, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textBrowserProgramName, 232, SpringLayout.WEST, getContentPane());
		getContentPane().add(textBrowserProgramName);
		textBrowserProgramName.setColumns(10);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		Utility.centerOnScreen(this);
		
		loadDialog(serializedBrowserParameters);
		
		setListeners();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
		Utility.localize(labelFolder);
		Utility.localize(labelHomePage);
		Utility.localize(labelBrowserTitle);
		Utility.localize(labelBrowserMessage);
		Utility.localize(labelWindowSize);
		Utility.localize(labelWindowWidth);
		Utility.localize(labelWindowHeight);
		Utility.localize(checkBrowserMaximized);
		Utility.localize(buttonDefault);
		Utility.localize(checkCreateAutorun);
		Utility.localize(labelBrowserProgramName);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonDefault.setIcon(Images.getIcon("org/multipage/generator/images/default.png"));
		buttonDefaultHomePage.setIcon(Images.getIcon("org/multipage/generator/images/default.png"));
	}

	/**
	 * On default home page.
	 */
	protected void onDefaultHomePage() {
		
		if (Utility.ask(this, "org.multipage.generator.textSetDefaultHomePageName")) {
			textHomePage.setText("index.htm");
		}
	}

	/**
	 * On cancel dialog.
	 */
	protected void onCancel() {

		dispose();
	}
	
	/**
	 * On confirm dialog.
	 */
	protected void onOK() {
		
		if (areErrors()) {
			Utility.show(this, "org.multipage.generator.textThereAreErrors");
			return;
		}
		
		saveDialog();
		
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog(BrowserParameters browserParameters) {
		
		textFolder.setText(browserParameters.getFolder());
		textHomePage.setText(browserParameters.getHomePage());
		textBrowserTitle.setText(browserParameters.getTitle());
		textBrowserMessage.setText(browserParameters.getMessage());
		textWindowWidth.setText(browserParameters.getWidthText());
		textWindowHeight.setText(browserParameters.getHeightText());
		textBrowserProgramName.setText(browserParameters.getBrowserProgramName());
		
		checkBrowserMaximized.setSelected(browserParameters.isMaximized());
		checkCreateAutorun.setSelected(browserParameters.isCreateAutorun());
		
		textCheckInteger(textWindowWidth, labelWidthError);
		textCheckInteger(textWindowHeight, labelHeightError);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		serializedBrowserParameters.setFolder(textFolder.getText().trim());
		serializedBrowserParameters.setHomePage(textHomePage.getText().trim());
		serializedBrowserParameters.setTitle(textBrowserTitle.getText());
		serializedBrowserParameters.setMessage(textBrowserMessage.getText());
		serializedBrowserParameters.setBrowserProgramName(textBrowserProgramName.getText().trim());
		
		int number;
		try {
			number = Integer.parseInt(textWindowWidth.getText());
			serializedBrowserParameters.setWidth(number);
		}
		catch (Exception e) {
			Utility.show2(this, e.getMessage());
		}
		try {
			number = Integer.parseInt(textWindowHeight.getText());
			serializedBrowserParameters.setHeight(number);
		}
		catch (Exception e) {
			Utility.show2(this, e.getMessage());
		}
		
		serializedBrowserParameters.setMaximized(checkBrowserMaximized.isSelected());
		serializedBrowserParameters.setCreateAutorun(checkCreateAutorun.isSelected());
	}

	/**
	 * On restore default.
	 */
	protected void onRestoreDefault() {
		
		if (Utility.ask(this, "org.multipage.generator.textSetDefaultBrowserParameters")) {
			
			BrowserParameters browserParameters = new BrowserParameters();
			loadDialog(browserParameters);
		}
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		class _DocumentListener implements DocumentListener {
			private SimpleMethodRef method;
			public _DocumentListener(SimpleMethodRef method) {
				this.method = method;
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				method.run();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				method.run();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				method.run();
			}
		}
		
		textWindowWidth.getDocument().addDocumentListener(new _DocumentListener(new SimpleMethodRef() {
			@Override
			public void run() {
				textCheckInteger(textWindowWidth, labelWidthError);
			}}));
		
		textWindowHeight.getDocument().addDocumentListener(new _DocumentListener(new SimpleMethodRef() {
			@Override
			public void run() {
				textCheckInteger(textWindowHeight, labelHeightError);
			}}));
	}

	/**
	 * Check integer value of the text component.
	 * @param textControl 
	 * @param label 
	 */
	protected void textCheckInteger(JTextField textControl, JLabel label) {
		
		String numberText = textControl.getText();
		
		try {
			Integer.parseInt(numberText);
			label.setText("");
		}
		catch (NumberFormatException e) {
			label.setText(Resources.getString("org.multipage.generator.textError"));
		}
	}
	
	/**
	 * Check error.
	 */
	private boolean getTextAsInteger(JTextField textControl) {
		
		String numberText = textControl.getText();
		
		try {
			Integer.parseInt(numberText);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	/**
	 * Check if there are any errors.
	 */
	private boolean areErrors() {
		
		return !getTextAsInteger(textWindowWidth) || !getTextAsInteger(textWindowHeight);
	}
}
