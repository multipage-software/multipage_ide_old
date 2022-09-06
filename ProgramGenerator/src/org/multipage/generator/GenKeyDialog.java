/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 01-09-2022
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.multipage.addinloader.AddInsUtility;
import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

import net.miginfocom.swing.MigLayout;

/**
 * Class for the GenKeyDialog. Use the showDialog(...) method to make this dialog visible.
 * @author vakol
 *
 */
public class GenKeyDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Frame window boundaries.
	 */
	private static Rectangle bounds = null;
	
	/**
	 * Path to current directory.
	 */
	private File currentDirectory = null;
	
	/**
	 * Gets keystore password.
	 */
	private Supplier<char[]> passwordLambda = null;
	
	/**
	 * Frame controls.
	 */
	private JButton buttonOk;
	private JButton buttonCancel;
	private JLabel labelCommonName;
	private JLabel labelOrganization;
	private JLabel labelOrganizationalUnit;
	private JLabel labelLocality;
	private JLabel labelState;
	private JLabel labelCountry;
	private JLabel labelKayPairAlias;
	private TextFieldEx textCommonName;
	private TextFieldEx textOrganization;
	private TextFieldEx textOrganizationalUnit;
	private TextFieldEx textLocality;
	private TextFieldEx textState;
	private TextFieldEx textCountry;
	private TextFieldEx textKeyPairAlias;

	
	//$hide>>$
	/**
	 * Frame object fields.
	 */
	
	// TODO Herein add new frame object fields.

	//$hide<<$
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
	}
	
	/**
	 * Read states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}

	/**
	 * Write states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Show the dialog.
	 * @param parent
	 * @param keystoreFile
	 * @param currentDirectory
	 */
	public static void showDialog(Component parent, File currentDirectory, Supplier<char []> passwordLambda) {
		
		// Create a new frame object and make it visible.
		GenKeyDialog dialog = new GenKeyDialog(parent);
		
		dialog.currentDirectory = currentDirectory;
		dialog.passwordLambda = passwordLambda;
		
		dialog.setVisible(true);
		return;
	}
	
	/**
	 * Create the frame.
	 * @param parent 
	 */
	public GenKeyDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		postCreate(); //$hide$
	}

	/**
	 * Initialize frame components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(400, 300));
		setBounds(new Rectangle(0, 0, 400, 400));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		setTitle("org.multipage.titleGenKeyDialog");
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
		
		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel, 205, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, buttonCancel);
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(panel);
		panel.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][]"));
		
		labelCommonName = new JLabel("org.multipage.generator.textCommonName");
		panel.add(labelCommonName, "cell 0 0,alignx trailing");
		
		textCommonName = new TextFieldEx();
		panel.add(textCommonName, "cell 1 0,growx");
		textCommonName.setColumns(10);
		
		labelOrganization = new JLabel("org.multipage.generator.textOrganization");
		panel.add(labelOrganization, "cell 0 1,alignx trailing");
		
		textOrganization = new TextFieldEx();
		panel.add(textOrganization, "cell 1 1,growx");
		textOrganization.setColumns(10);
		
		labelOrganizationalUnit = new JLabel("org.multipage.generator.textOrganizationalUnit");
		panel.add(labelOrganizationalUnit, "cell 0 2,alignx trailing");
		
		textOrganizationalUnit = new TextFieldEx();
		panel.add(textOrganizationalUnit, "cell 1 2,growx");
		textOrganizationalUnit.setColumns(10);
		
		labelLocality = new JLabel("org.multipage.generator.textLocality");
		panel.add(labelLocality, "cell 0 3,alignx trailing");
		
		textLocality = new TextFieldEx();
		panel.add(textLocality, "cell 1 3,growx");
		textLocality.setColumns(10);
		
		labelState = new JLabel("org.multipage.generator.textState");
		panel.add(labelState, "cell 0 4,alignx trailing");
		
		textState = new TextFieldEx();
		panel.add(textState, "cell 1 4,growx");
		textState.setColumns(10);
		
		labelCountry = new JLabel("org.multipage.generator.textCountry");
		panel.add(labelCountry, "cell 0 5,alignx trailing");
		
		textCountry = new TextFieldEx();
		panel.add(textCountry, "cell 1 5,growx");
		textCountry.setColumns(10);
		
		labelKayPairAlias = new JLabel("org.multipage.generator.textKeystoreAlias");
		panel.add(labelKayPairAlias, "cell 0 7,alignx trailing");
		
		textKeyPairAlias = new TextFieldEx();
		panel.add(textKeyPairAlias, "cell 1 7,growx");
		textKeyPairAlias.setColumns(10);
	}

	/**
	 * Post creation of the frame controls.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		// TODO Add post creation function that initialize the dialog.
		loadDialog();
	}
	
	/**
	 * Localize texts of the frame controls.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelCommonName);
		Utility.localize(labelOrganization);
		Utility.localize(labelOrganizationalUnit);
		Utility.localize(labelLocality);
		Utility.localize(labelState);
		Utility.localize(labelCountry);
		Utility.localize(labelKayPairAlias);
	}
	
	/**
	 * Set frame icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/gui/images/main.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}
	
	/**
	 * The frame confirmed by the user click on the [OK] button.
	 */
	protected void onOk() {
		
		Obj<File> updatedKeystoreFile = new Obj<File>();
		String resultText = generateNewKeyPair(updatedKeystoreFile);
		if (resultText == null) {
			return;
		}
		Utility.show2(this, resultText);
		
		saveDialog();
		dispose();
		
		try {
			// Import keystore file. Application will be restarted.
		    AddInsUtility.importUpdatedKeystore(updatedKeystoreFile.ref, true);
		}
		catch (Exception e) {
			Utility.show2(this, e.getLocalizedMessage());
		}
	}
	
	/**
	 * The frame has been canceled with the [Cancel] or the [X] button.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * Generate new key pair.
	 */
	private String generateNewKeyPair(Obj<File> keystoreFile) {
		
		// Check prerequisites.
		if (currentDirectory == null || !currentDirectory.isDirectory() || passwordLambda == null) {
			return null;
		}
		
		// Expose keystore file.
		keystoreFile.ref = Utility.exposeApplicationKeystore(this, "org/multipage/addinloader/properties/multipage_client.p12");
		if (keystoreFile.ref == null || !keystoreFile.ref.isFile()) {
			return null;
		}
		
		// Generate new key pair.
		String keystoreName = keystoreFile.ref.getAbsolutePath();
		String alias = textKeyPairAlias.getText();
		char [] password = passwordLambda.get();
		String distinguishedName = getDistinguishedName();
		
		String resultText = null;
		try {
			resultText = Utility.runJavaTool(currentDirectory.toString(),
					"keytool",
					new String [] {
						"-genkeypair",
						"-keystore", keystoreName,
						"-alias", alias,
						"-keyalg", "RSA",
						"-dname", distinguishedName,
						"-validity", "365",
						"-storepass", new String(password)
						},
						30000, TimeUnit.MILLISECONDS
					);
			return resultText;
		}
		catch (Exception e) {
			resultText = e.getLocalizedMessage();
		}
		
		// Return keystore file name.
		return keystoreName;
	}
	
	/**
	 * Compile distinguished name for X.509 certificate from input dialog values.
	 * @return
	 */
	private String getDistinguishedName() {
		
		String dname = "";
		String text = textCountry.getText();
		if (!text.isEmpty())  {
			dname += "C=" + text + ", ";
		}
		text = textState.getText();
		if (!text.isEmpty())  {
			dname += "ST=" + text + ", ";
		}
		text = textLocality.getText();
		if (!text.isEmpty())  {
			dname += "L=" + text + ", ";
		}
		text = textOrganization.getText();
		if (!text.isEmpty())  {
			dname += "O=" + text + ", ";
		}
		text = textOrganizationalUnit.getText();
		if (!text.isEmpty())  {
			dname += "OU=" + text + ", ";
		}
		text = textCommonName.getText();
		if (!text.isEmpty())  {
			dname += "CN=" + text;
		}
		return dname;
	}

	/**
	 * Load and set initial state of the frame window.
	 */
	private void loadDialog() {
		
		// Set dialog window boundaries.
		if (bounds != null && !bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
		
		// TODO Load additional states.
		
	}
	
	/**
	 * Save current state of the frame window.
	 */
	private void saveDialog() {
		
		// Save current dialog window boundaries.
		bounds = getBounds();
		
		// TODO Save additional states.
		
	}
}
