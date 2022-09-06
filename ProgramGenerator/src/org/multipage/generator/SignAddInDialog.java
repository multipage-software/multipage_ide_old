/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 25-07-2022
 *
 */
package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.io.IOUtils;
import org.multipage.gui.Images;
import org.multipage.gui.StateInputStream;
import org.multipage.gui.StateOutputStream;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import javax.swing.JToolBar;

/**
 * 
 * @author vakol
 *
 */
public class SignAddInDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Dialog window dimensions.
	 */
	private static Rectangle bounds;
	
	/**
	 * Certificate wrapper class.
	 */
	private static class CertificateWrapper {
		
		/**
		 * Certificate reference.
		 */
		X509Certificate ref = null;
		
		/**
		 * Common name.
		 */
		String commonName = "";
		
		/**
		 * Constructor.
		 */
		CertificateWrapper(Certificate certificate) {
			
			if (!(certificate instanceof X509Certificate)) {
				return;
			}
			
			ref = (X509Certificate) certificate;
			commonName = Utility.getCommonName(ref);
		}
		
		/**
		 * Return text representation of the certificate.
		 */
		@Override
		public String toString() {
			
			return commonName;
		}
	}
	
	/**
	 * Certificate chain wrapper.
	 * @author vakol
	 *
	 */
	private static class CertificateChainWrapper {
		
		/**
		 * Certificate reference.
		 */
		X509Certificate[] ref = null;
		
		/**
		 * Common names.
		 */
		String commonNames = "";
		
		/**
		 * Certificate chain wrapper.
		 * @param certificateChain
		 */
		public CertificateChainWrapper(Certificate[] certificateChain) {
			
			if (certificateChain == null) {
				return;
			}
			
			int length = certificateChain.length;
			if (length <= 0) {
				return;
			}
			
			ref = new X509Certificate[length];
			
			String divider = "";
			int index = 0;
			
			for (Certificate certificate : certificateChain) {
				if (!(certificate instanceof X509Certificate)) {
					return;
				}
				
				ref[index] = (X509Certificate) certificate;
				commonNames += divider + Utility.getCommonName(ref[index]);
				divider = "->";
			}
		}
		
		/**
		 * Return text representation of the certificate.
		 */
		@Override
		public String toString() {
			
			return commonNames;
		}
	}

	/**
	 * Set defaults.
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
	public static void serializeData(StateInputStream inputStream)
					throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
	}
	
	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
					throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * Exposed keystore file.
	 */
	private File keystoreFile;
	
	/**
	 * GUI components.
	 */
	private TextFieldEx textAddInPath;
	private JLabel labelAddInPath;
	private JButton buttonChooseAddIn;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelAddInName;
	private TextFieldEx textAddInName;
	private JLabel labelAddInTag;
	private TextFieldEx textAddInTag;
	private JLabel labelAddInVersion;
	private TextFieldEx textAddInVersion;
	private JLabel labelAddInDescription;
	private JTextArea textAddInDescription;
	private JPanel panelAddInAttributes;
	private JTabbedPane tabbedPane;
	private JLabel labelKeyStore;
	private JScrollPane scrollPaneKeystoreContent;
	private JTable tableKeystoreContent;
	private JLabel labelCertificateAlias;
	private JComboBox<String> comboCertificate;
	private JPasswordField textPassword;
	private JLabel labelKeystorePassword;
	private JToolBar toolBarKeystoreActions;

	/**
	 * Show add-in signing dialog.
	 */
	public static void showDialog(Component parent, String addInPath) {
		
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try {
					SignAddInDialog dialog = new SignAddInDialog(parent);
					dialog.postCreate();
					dialog.setAddInPath(addInPath);
					dialog.setVisible(true);
				} 
				catch (Exception e) {
					Utility.show(parent, "org.multipage.generator.messageCannotDiplayAddInSignerDialog", e.getLocalizedMessage());
				}
			}
		});
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public SignAddInDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.DOCUMENT_MODAL);
		initComponents();
	}
	
	/**
	 * Initialize GUI components.
	 */
	private void initComponents() {
		getContentPane().setMinimumSize(new Dimension(275, 400));
		setMinimumSize(new Dimension(275, 400));
		setTitle("org.multipage.generator.titleSignSelectedAddIn");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 608, 647);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelAddInPath = new JLabel("org.multipage.generator.textAddInPath");
		springLayout.putConstraint(SpringLayout.NORTH, labelAddInPath, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelAddInPath, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelAddInPath);
		
		textAddInPath = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAddInPath, 6, SpringLayout.SOUTH, labelAddInPath);
		springLayout.putConstraint(SpringLayout.WEST, textAddInPath, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(textAddInPath);
		textAddInPath.setColumns(10);
		
		buttonChooseAddIn = new JButton("");
		buttonChooseAddIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChooseAddIn();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, textAddInPath, 0, SpringLayout.WEST, buttonChooseAddIn);
		springLayout.putConstraint(SpringLayout.NORTH, buttonChooseAddIn, 0, SpringLayout.NORTH, textAddInPath);
		buttonChooseAddIn.setPreferredSize(new Dimension(20, 20));
		buttonChooseAddIn.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.EAST, buttonChooseAddIn, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonChooseAddIn);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, 0, SpringLayout.EAST, buttonChooseAddIn);
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, 0, SpringLayout.SOUTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonOk);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 6, SpringLayout.SOUTH, textAddInPath);
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, labelAddInPath);
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, buttonChooseAddIn);
		getContentPane().add(tabbedPane);
		
		panelAddInAttributes = new JPanel();
		panelAddInAttributes.setOpaque(false);
		tabbedPane.addTab("org.multipage.generator.titleAddInAttributes", null, panelAddInAttributes, null);
		springLayout.putConstraint(SpringLayout.NORTH, panelAddInAttributes, 180, SpringLayout.SOUTH, textAddInPath);
		springLayout.putConstraint(SpringLayout.WEST, panelAddInAttributes, 0, SpringLayout.WEST, labelAddInPath);
		springLayout.putConstraint(SpringLayout.SOUTH, panelAddInAttributes, -10, SpringLayout.NORTH, buttonOk);
		springLayout.putConstraint(SpringLayout.EAST, panelAddInAttributes, 0, SpringLayout.EAST, buttonChooseAddIn);
		SpringLayout sl_panelAddInAttributes = new SpringLayout();
		panelAddInAttributes.setLayout(sl_panelAddInAttributes);
		
		labelAddInName = new JLabel("org.multipage.generator.textAddInName");
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, labelAddInName, 10, SpringLayout.NORTH, panelAddInAttributes);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, labelAddInName, 10, SpringLayout.WEST, panelAddInAttributes);
		panelAddInAttributes.add(labelAddInName);
		
		textAddInName = new TextFieldEx();
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, textAddInName, 6, SpringLayout.SOUTH, labelAddInName);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, textAddInName, 0, SpringLayout.WEST, labelAddInName);
		textAddInName.setPreferredSize(new Dimension(200, 20));
		textAddInName.setMinimumSize(new Dimension(200, 20));
		textAddInName.setColumns(25);
		panelAddInAttributes.add(textAddInName);
		
		labelAddInTag = new JLabel("org.multipage.generator.textAddInTag");
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, labelAddInTag, 6, SpringLayout.SOUTH, textAddInName);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, labelAddInTag, 10, SpringLayout.WEST, panelAddInAttributes);
		panelAddInAttributes.add(labelAddInTag);
		
		textAddInTag = new TextFieldEx();
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, textAddInTag, 6, SpringLayout.SOUTH, labelAddInTag);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, textAddInTag, 0, SpringLayout.WEST, labelAddInName);
		textAddInTag.setColumns(10);
		panelAddInAttributes.add(textAddInTag);
		
		labelAddInVersion = new JLabel("org.multipage.generator.textAddInVersion");
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, labelAddInVersion, 6, SpringLayout.SOUTH, textAddInTag);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, labelAddInVersion, 0, SpringLayout.WEST, labelAddInName);
		panelAddInAttributes.add(labelAddInVersion);
		
		textAddInVersion = new TextFieldEx();
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, textAddInVersion, 6, SpringLayout.SOUTH, labelAddInVersion);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, textAddInVersion, 0, SpringLayout.WEST, labelAddInName);
		textAddInVersion.setColumns(10);
		panelAddInAttributes.add(textAddInVersion);
		
		labelAddInDescription = new JLabel("org.multipage.generator.textAddInDescription");
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, labelAddInDescription, 6, SpringLayout.SOUTH, textAddInVersion);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, labelAddInDescription, 0, SpringLayout.WEST, labelAddInName);
		panelAddInAttributes.add(labelAddInDescription);
		
		JScrollPane scrollPaneAddInDescription = new JScrollPane();
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, scrollPaneAddInDescription, 3, SpringLayout.SOUTH, labelAddInDescription);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, scrollPaneAddInDescription, 10, SpringLayout.WEST, panelAddInAttributes);
		sl_panelAddInAttributes.putConstraint(SpringLayout.SOUTH, scrollPaneAddInDescription, -10, SpringLayout.SOUTH, panelAddInAttributes);
		sl_panelAddInAttributes.putConstraint(SpringLayout.EAST, scrollPaneAddInDescription, -10, SpringLayout.EAST, panelAddInAttributes);
		panelAddInAttributes.add(scrollPaneAddInDescription);
		
		textAddInDescription = new JTextArea();
		scrollPaneAddInDescription.setViewportView(textAddInDescription);
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, textAddInDescription, 6, SpringLayout.SOUTH, labelAddInDescription);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, textAddInDescription, 0, SpringLayout.WEST, labelAddInName);
		sl_panelAddInAttributes.putConstraint(SpringLayout.SOUTH, textAddInDescription, -10, SpringLayout.SOUTH, panelAddInAttributes);
		sl_panelAddInAttributes.putConstraint(SpringLayout.EAST, textAddInDescription, -200, SpringLayout.EAST, panelAddInAttributes);
		
		labelCertificateAlias = new JLabel("org.multipage.generator.textCertificateAlias");
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, labelCertificateAlias, 0, SpringLayout.NORTH, labelAddInName);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, labelCertificateAlias, 50, SpringLayout.EAST, textAddInName);
		panelAddInAttributes.add(labelCertificateAlias);
		
		comboCertificate = new JComboBox<String>();
		sl_panelAddInAttributes.putConstraint(SpringLayout.NORTH, comboCertificate, 4, SpringLayout.SOUTH, labelCertificateAlias);
		sl_panelAddInAttributes.putConstraint(SpringLayout.WEST, comboCertificate, 0, SpringLayout.WEST, labelCertificateAlias);
		sl_panelAddInAttributes.putConstraint(SpringLayout.EAST, comboCertificate, 0, SpringLayout.EAST, scrollPaneAddInDescription);
		panelAddInAttributes.add(comboCertificate);
		
		JPanel panelKeystore = new JPanel();
		panelKeystore.setOpaque(false);
		tabbedPane.addTab("org.multipage.generator.titleKeyStore", null, panelKeystore, null);
		SpringLayout sl_panelKeystore = new SpringLayout();
		panelKeystore.setLayout(sl_panelKeystore);
		
		labelKeyStore = new JLabel("org.multipage.generator.textKeyStoreContent");
		sl_panelKeystore.putConstraint(SpringLayout.WEST, labelKeyStore, 10, SpringLayout.WEST, panelKeystore);
		panelKeystore.add(labelKeyStore);
		
		scrollPaneKeystoreContent = new JScrollPane();
		sl_panelKeystore.putConstraint(SpringLayout.NORTH, scrollPaneKeystoreContent, 6, SpringLayout.SOUTH, labelKeyStore);
		sl_panelKeystore.putConstraint(SpringLayout.WEST, scrollPaneKeystoreContent, 10, SpringLayout.WEST, panelKeystore);
		sl_panelKeystore.putConstraint(SpringLayout.EAST, scrollPaneKeystoreContent, -10, SpringLayout.EAST, panelKeystore);
		panelKeystore.add(scrollPaneKeystoreContent);
		
		tableKeystoreContent = new JTable();
		tableKeystoreContent.setFillsViewportHeight(true);
		scrollPaneKeystoreContent.setViewportView(tableKeystoreContent);
		
		labelKeystorePassword = new JLabel("org.multipage.generator.textKeystorePassword");
		sl_panelKeystore.putConstraint(SpringLayout.NORTH, labelKeystorePassword, 30, SpringLayout.NORTH, panelKeystore);
		sl_panelKeystore.putConstraint(SpringLayout.WEST, labelKeystorePassword, 0, SpringLayout.WEST, labelKeyStore);
		panelKeystore.add(labelKeystorePassword);
		
		textPassword = new JPasswordField();
		textPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onEnterPassword(e);
			}
		});
		sl_panelKeystore.putConstraint(SpringLayout.NORTH, labelKeyStore, 20, SpringLayout.SOUTH, textPassword);
		sl_panelKeystore.putConstraint(SpringLayout.NORTH, textPassword, -3, SpringLayout.NORTH, labelKeystorePassword);
		sl_panelKeystore.putConstraint(SpringLayout.WEST, textPassword, 6, SpringLayout.EAST, labelKeystorePassword);
		sl_panelKeystore.putConstraint(SpringLayout.EAST, textPassword, -105, SpringLayout.EAST, scrollPaneKeystoreContent);
		panelKeystore.add(textPassword);
		
		toolBarKeystoreActions = new JToolBar();
		toolBarKeystoreActions.setFloatable(false);
		sl_panelKeystore.putConstraint(SpringLayout.SOUTH, scrollPaneKeystoreContent, 0, SpringLayout.NORTH, toolBarKeystoreActions);
		sl_panelKeystore.putConstraint(SpringLayout.WEST, toolBarKeystoreActions, 0, SpringLayout.WEST, scrollPaneKeystoreContent);
		sl_panelKeystore.putConstraint(SpringLayout.SOUTH, toolBarKeystoreActions, -10, SpringLayout.SOUTH, panelKeystore);
		sl_panelKeystore.putConstraint(SpringLayout.EAST, toolBarKeystoreActions, 0, SpringLayout.EAST, scrollPaneKeystoreContent);
		panelKeystore.add(toolBarKeystoreActions);
	}
	
	/**
	 * Post creation of the dialog.
	 */
	protected void postCreate() {
		
		// Set close operation that is run automatically.
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// Create tool bars.
		createToolBar();
		// Localize texts of the GUI components.
		localize();
		// Set icons used by the GUI components.
		setIcons();
		// Export keystore to temporary file.
		keystoreFile = Utility.exposeApplicationKeystore(this, "org/multipage/addinloader/properties/multipage_client.p12");
		// Load and display keystore entries.
		loadKeystoreTable(keystoreFile);
		// Load stored dialog states.
		loadDialog();
	}
	
	/**
	 * Create tool bar.
	 */
	private void createToolBar() {
		
		ToolBarKit.addToolBarButton(toolBarKeystoreActions, "org/multipage/generator/images/keypair.png", "org.multipage.generator.tooltipGenerateNewKeyPair", () -> onKeystoreGenKey());
	}

	/**
	 * On [ENTER] key typed after entering keystore password.
	 * @param e
	 */
	protected void onEnterPassword(KeyEvent e) {
		
		// Check if user has pressed [ENTER] key.
		if (!(e.getKeyCode() == KeyEvent.VK_ENTER)) {
			return;
		}
		
		// Create empty table that can display keystore entries.
		loadKeystoreTable(keystoreFile);
	}
	
	/**
	 * Generate new key pair in keystore.
	 */
	private void onKeystoreGenKey() {
		
		// Get current directory.
		String pathName = Utility.getCurrentPathName();
		File currentDirectory = new File(pathName);
				
		// Input key pair properties.
		GenKeyDialog.showDialog(this, currentDirectory, () -> textPassword.getPassword());
	}


	/**
	 * Localize texts of the GUI components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(tabbedPane);
		Utility.localize(labelAddInPath);
		Utility.localize(labelAddInName);
		Utility.localize(labelAddInTag);
		Utility.localize(labelAddInVersion);
		Utility.localize(labelAddInDescription);
		Utility.localize(labelKeystorePassword);
		Utility.localize(labelKeyStore);
		Utility.localize(labelCertificateAlias);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
	}
	
	/**
	 * Set icons used by the GUI components.
	 */
	private void setIcons() {
		
		buttonChooseAddIn.setIcon(Images.getIcon("org/multipage/generator/images/filenames_icon.png"));
	}
	
	/**
	 * Set Add-in path.
	 * @param addInPath
	 */
	protected void setAddInPath(String addInPath) {
		
		textAddInPath.setText(addInPath);
	}
	
	/**
	 * Load keystore table.
	 */
	private void loadKeystoreTable(File keystoreFile) {
		
		// Create table model.
		DefaultTableModel tableModel = new DefaultTableModel();
		
		// Disable editing of table cells.
		tableKeystoreContent.setDefaultEditor(Object.class, null);
		
		// Create column model.
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		
		TableColumn column = new TableColumn();
		
		column.setModelIndex(0);
		String columnName = Resources.getString("org.multipage.generator.titleKeystoreEntryAliasName");
		column.setHeaderValue(columnName);
		columnModel.addColumn(column);
		tableKeystoreContent.setColumnModel(columnModel);
		tableModel.addColumn(columnName);
		
		column.setModelIndex(1);
		columnName = Resources.getString("org.multipage.generator.titleKeystoreEntryCertificate");
		column.setHeaderValue(columnName);
		columnModel.addColumn(column);
		tableKeystoreContent.setColumnModel(columnModel);
		tableModel.addColumn(columnName);
		
		column.setModelIndex(2);
		columnName = Resources.getString("org.multipage.generator.titleKeystoreEntryCertificateChain");
		column.setHeaderValue(columnName);
		columnModel.addColumn(column);
		tableKeystoreContent.setColumnModel(columnModel);
		tableModel.addColumn(columnName);
		
		column.setModelIndex(3);
		columnName = Resources.getString("org.multipage.generator.titleKeystoreEntryCreationDate");
		column.setHeaderValue(columnName);
		columnModel.addColumn(column);
		tableKeystoreContent.setColumnModel(columnModel);
		tableModel.addColumn(columnName);
		
		tableKeystoreContent.getTableHeader().setOpaque(false);
		tableKeystoreContent.setModel(tableModel);
		
		// Open keystore.
		KeyStore keystore = openKeystore(keystoreFile);
		if (keystore == null) {
			return;
		}
		
		try {
			Enumeration<String> aliases = keystore.aliases();
			final Iterator<String> aliasIterator = aliases.asIterator();
			
			Obj<Boolean> isEmpty = new Obj<Boolean>(true);
			
			aliasIterator.forEachRemaining(alias -> {
				
				try {
					Date date = keystore.getCreationDate(alias);
					Certificate certificate = keystore.getCertificate(alias);
					Certificate [] certificateChain = keystore.getCertificateChain(alias);
					
					tableModel.addRow(new Object [] {
							alias,
							new CertificateWrapper(certificate),
							new CertificateChainWrapper(certificateChain),
							date
							});
					comboCertificate.addItem(alias);
					
					isEmpty.ref = false;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});	
			// Select first item in above combo box with keystore aliases.
			if (!isEmpty.ref) {
				comboCertificate.setSelectedIndex(0);
			}
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Open keystore.
	 * @param keystoreFile 
	 * @return
	 */
	private KeyStore openKeystore(File keystoreFile) {
		
		KeyStore keystore = null;
		
		try {
			// Try to open keystore.
			char [] credentialsChar = textPassword.getPassword();
			keystore = KeyStore.getInstance(keystoreFile, credentialsChar);
		}
		catch (Exception e) {
			Utility.show(this, "org.multipage.generator.messageKeystoreCredentialsError", e.getLocalizedMessage());
		}
		
		return keystore;
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
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * On choose Add-in file.
	 */
	protected void onChooseAddIn() {
		
		File addInJarFile = Utility.chooseFileToOpen(this, new String [][] {{"org.multipage.generator.textAddInJarFilesFilter", "jar"}});
		if (addInJarFile == null) {
			return;
		}
		
		// Display file path.
		textAddInPath.setText(addInJarFile.toString());
	}
	
	/**
	 * On cancel dialog.
	 */
	protected void onCancel() {
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On OK button.
	 */
	protected void onOK() {
		
		saveDialog();
		dispose();
	}
}
