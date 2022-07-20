/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 15-04-2022
 *
 */

package org.multipage.addinloader;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.Signer;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * Add-in loader.
 * @author vakol
 *
 */
public class AddInLoader extends JFrame {
	
	/**
	 * JAR file meta information 
	 */
	private static class JarMetaInfo {
		
		public HashMap<String, LinkedList<Signer>> signedFiles = null;
		public String debug = "";
	}

	/**
	 * Version number.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Regex pattern that parse single output line returned by "jarsigner -verify -verbose" statement.
	 */
	private static final Pattern regexVerifier = Pattern.compile(
			"^\\s*(?<verifierFlags>[smk]{1,3}+|\\?)"
			+ "\\s+(?<fileSize>\\p{Digit}+)"
			+ "\\s+(?<dayName>\\p{Alpha}+)"
			+ "\\s+(?<monthName>\\p{Alpha}+)"
			+ "\\s+(?<dayNum>\\p{Digit}+)"
			+ "\\s+(?<time>[\\p{Digit}\\:]+)"
			+ "\\s+(?<zone>[\\p{Upper}\\:]+)"
			+ "\\s+(?<year>\\p{Digit}+)"
			+ "\\s+(?<fileOrFolder>.+)$"
			);
	
	/**
	 * Signed file or folder.
	 */
	private static class SignedFileTreeNode extends DefaultMutableTreeNode {
		
		/**
		 * Version.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * List of currently selected signers.
		 */
		public static CodeSigner [] selectedSigners = null;
		
		/**
		 * Icons.
		 */
		private static ImageIcon folderIcon = null;
		private static ImageIcon signedFolderIcon = null;
		private static ImageIcon badFolderIcon = null;
		
		private static ImageIcon fileIcon = null;
		private static ImageIcon signedFileIcon = null;
		private static ImageIcon badFileIcon = null;

		private static ImageIcon signedCertificateIcon = null;
		private static ImageIcon badCertificateIcon = null;
		
		/**
		 * Load icons.
		 */
		static {
			
			// Load image icons.
			fileIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/file.png"));
			signedFileIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/file_ok.png"));
			badFileIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/file_failed.png"));
			
			folderIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/folder.png"));
			signedFolderIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/folder_ok.png"));
			badFolderIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/folder_failed.png"));
			
			signedCertificateIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/certificate_ok.png"));
			badCertificateIcon = new ImageIcon(AddInsUtility.getImage("org/multipage/addinloader/images/certificate_failed.png"));
		}

		/**
		 * File name.
		 */
		public String fileName = null;
		
		/**
		 * List of currently selected signers.
		 */
		public CodeSigner [] fileSigners = null;
		
		/**
		 * Flags found by "jarsigner -verify -verbose" statement.
		 */
		private boolean signatureVerified = false;
		private boolean listedInManifest = false;
		private boolean certificateInKeystore = false;
		private boolean notSigned = true;

		/**
		 * Constructor.
		 * @param fileName
		 * @param fileSigners
		 */
		public SignedFileTreeNode(String fileName, CodeSigner [] fileSigners) {
			
			this.fileName = fileName;
			this.fileSigners = fileSigners;
		}
		
		/**
		 * Get icon image.
		 * @param renderer 
		 * @param leaf 
		 * @param expanded 
		 * @param value 
		 * @return
		 */
		public static void setIconImage(DefaultTreeCellRenderer renderer, boolean expanded, boolean leaf, Object value) {
			
			if (value instanceof SignedFileTreeNode) {
				
				SignedFileTreeNode signedFile = (SignedFileTreeNode) value;

				// Select file or package icon.
				boolean signed = signedFile.signatureVerified;
				
				if (leaf) {
					Icon icon = (signed? signedFileIcon : badFileIcon);
					renderer.setLeafIcon(icon);
				}
				else {
					Icon icon = (signed ? signedFolderIcon : badFolderIcon);
					renderer.setOpenIcon(icon);
					renderer.setClosedIcon(icon);
				}
			}
			else if (value instanceof X509Certificate) {
				
				X509Certificate x509Certificate = (X509Certificate) value;
				
				// TODO: <---MAKE FINISH IT Set up the signed flag according to certificate authenticity.
				boolean signed = true;
				
				Icon icon = (signed ? signedCertificateIcon : badCertificateIcon);
				renderer.setLeafIcon(icon);
				renderer.setOpenIcon(icon);
				renderer.setClosedIcon(icon);
			}
		}
		
		
		/**
		 * Returns node caption.
		 */
		@Override
		public String toString() {
			
			return fileName;
		}
		
		/**
		 * Set file verify flags.
		 * @param verifierFlags
		 */
		public void setVerifierFlags(String verifierFlags) {
			
			signatureVerified = verifierFlags.contains("s");
			listedInManifest = verifierFlags.contains("m");
			certificateInKeystore = verifierFlags.contains("k");
			notSigned = verifierFlags.contains("?");
		}
	}
	
	/**
	 * Resource location.org.multipage.
	 */
	private static final String resourcesLocation = "org.multipage.addinloader.properties.messages";
	
	/**
	 * Path to add-in JAR file.
	 */
	private static String addInJarFile = "UNKNOWN";
	
	/**
	 * Application path.
	 */
	private static String applicationPath = "UNKNOWN";
	
	/**
	 * Application working directory.
	 */
	private static String applicationWorkingDirectory = "UNKNOWN";
	
	/**
	 * Application language.
	 */
	private static String language = "en";
	
	/**
	 * Application country.
	 */
	private static String country = "US";
	
		
	/**
	 * Components.
	 */
	private JPanel contentPane;
	private JTable tableAuthors;
	private JPanel panelAuthors;
	private JTextField textAddInJarFile;
	private JTextField textApplicationPath;
	private JTextField textLoaderPath;
	private JLabel labelAddInJarFile;
	private JLabel labelApplicationPath;
	private JLabel labelLoaderPath;
	private JLabel labelAuthors;
	private JButton buttonConfirm;
	private JButton buttonCancel;
	private JTextField textApplicationWorkingDirectory;
	private JLabel labelWorkingDirectory;
	private JLabel labelPackagesAndFiles;
	private JLabel labelCertificates;
	private JLabel labelMessages;
	private JTextArea textAreaMessages;
	private JTree treePackagesAndFiles;
	private JTree treeCertificates;
	
	// GUI events.
	private PropertyChangeListener onSelectAuthorEvent = event -> onSelectAuthor();
	
	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public AddInLoader() throws Exception
	{	
		// Set System L&F.
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
		// Initialize the frame components.
		initComponents();
	}
	
	/**
	 * Display the frame window.
	 */
	public void display() {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					// Display arguments.
					textAddInJarFile.setText(addInJarFile);
					textApplicationPath.setText(applicationPath);
					textApplicationWorkingDirectory.setText(applicationWorkingDirectory);
					
					// Display loader application path.
					String thisLoaderPath = AddInsUtility.getApplicationPath(AddInLoader.class);
					textLoaderPath.setText(thisLoaderPath);
					
					// Show the frame window.
					//$hide>>$
					postCreate(); 
					centerOnScreen(AddInLoader.this);
					setAlwaysOnTop(true);
					//$hide<<$
					setVisible(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
		setResizable(false);
		setTitle("org.multipage.addinloader.titleMain");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 666, 733);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		buttonConfirm = new JButton("org.multipage.addinloader.textConfirm");
		buttonConfirm.setBounds(465, 658, 80, 25);
		buttonConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onConfirm();
			}
		});
		contentPane.setLayout(null);
		contentPane.add(buttonConfirm);
		buttonConfirm.setMargin(new Insets(0, 0, 0, 0));
		buttonConfirm.setPreferredSize(new Dimension(80, 25));
		
		buttonCancel = new JButton("org.multipage.addinloader.textCancel");
		buttonCancel.setBounds(555, 658, 80, 25);
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		contentPane.add(buttonCancel);
		
		JScrollPane scrollPaneContainer = new JScrollPane();
		scrollPaneContainer.setBorder(null);
		scrollPaneContainer.setBounds(5, 125, 640, 522);
		contentPane.add(scrollPaneContainer);
		
		JPanel panelWithItems = new JPanel();
		panelWithItems.setBorder(null);
		scrollPaneContainer.setViewportView(panelWithItems);
		panelWithItems.setLayout(null);
		
		panelAuthors = new JPanel();
		panelAuthors.setBorder(null);
		panelAuthors.setBounds(10, 11, 618, 158);
		panelWithItems.add(panelAuthors);
		panelAuthors.setLayout(null);
		
		JScrollPane scrollPaneCertificates = new JScrollPane();
		scrollPaneCertificates.setBorder(new LineBorder(new Color(230, 230, 250)));
		scrollPaneCertificates.setBounds(330, 22, 288, 136);
		scrollPaneCertificates.setPreferredSize(new Dimension(300, 70));
		panelAuthors.add(scrollPaneCertificates);
		
		treeCertificates = new JTree();
		treeCertificates.setOpaque(false);
		treeCertificates.setBackground(Color.WHITE);
		treeCertificates.setBorder(null);
		scrollPaneCertificates.setViewportView(treeCertificates);
		
		JScrollPane scrollPaneAuthors = new JScrollPane();
		scrollPaneAuthors.setBackground(Color.WHITE);
		scrollPaneAuthors.setBorder(new LineBorder(new Color(230, 230, 250)));
		scrollPaneAuthors.setBounds(0, 11, 320, 146);
		panelAuthors.add(scrollPaneAuthors);
		scrollPaneAuthors.setPreferredSize(new Dimension(300, 70));
		
		tableAuthors = new JTable();
		tableAuthors.addPropertyChangeListener(onSelectAuthorEvent);
		tableAuthors.setBorder(null);
		scrollPaneAuthors.setViewportView(tableAuthors);
		
		labelAuthors = new JLabel("org.multipage.addinloader.textAddInAuthors");
		labelAuthors.setBounds(0, 0, 320, 12);
		panelAuthors.add(labelAuthors);
		
		labelCertificates = new JLabel("org.multipage.addinloader.textAddInCertificates");
		labelCertificates.setBounds(330, 11, 288, 12);
		panelAuthors.add(labelCertificates);
		
		JScrollPane scrollPaneMessages = new JScrollPane();
		scrollPaneMessages.setBounds(10, 396, 620, 115);
		panelWithItems.add(scrollPaneMessages);
		
		textAreaMessages = new JTextArea();
		scrollPaneMessages.setViewportView(textAreaMessages);
		
		labelPackagesAndFiles = new JLabel("org.multipage.addinloader.textAddInPackagesAndFiles");
		labelPackagesAndFiles.setBounds(10, 180, 618, 14);
		panelWithItems.add(labelPackagesAndFiles);
		
		labelMessages = new JLabel("org.multipage.addinloader.textAddInMessages");
		labelMessages.setBounds(10, 382, 618, 14);
		panelWithItems.add(labelMessages);
		
		JScrollPane scrollPanePackagesAndFiles = new JScrollPane();
		scrollPanePackagesAndFiles.setBounds(8, 195, 620, 176);
		panelWithItems.add(scrollPanePackagesAndFiles);
		
		treePackagesAndFiles = new JTree();
		scrollPanePackagesAndFiles.setViewportView(treePackagesAndFiles);
		
		labelAddInJarFile = new JLabel("org.multipage.addinloader.textAddInJarPath");
		labelAddInJarFile.setBounds(10, 10, 94, 14);
		contentPane.add(labelAddInJarFile);
		
		textAddInJarFile = new JTextField();
		textAddInJarFile.setEditable(false);
		textAddInJarFile.setBounds(102, 8, 533, 20);
		contentPane.add(textAddInJarFile);
		textAddInJarFile.setColumns(10);
		
		labelApplicationPath = new JLabel("org.multipage.addinloader.textMultipagePath");
		labelApplicationPath.setBounds(10, 36, 94, 14);
		contentPane.add(labelApplicationPath);
		
		textApplicationPath = new JTextField();
		textApplicationPath.setEditable(false);
		textApplicationPath.setBounds(102, 36, 533, 20);
		contentPane.add(textApplicationPath);
		textApplicationPath.setColumns(10);
		
		labelLoaderPath = new JLabel("org.multipage.addinloader.textLoaderPath");
		labelLoaderPath.setBounds(10, 92, 94, 14);
		contentPane.add(labelLoaderPath);
		
		textLoaderPath = new JTextField();
		textLoaderPath.setEditable(false);
		textLoaderPath.setBounds(102, 92, 533, 20);
		contentPane.add(textLoaderPath);
		textLoaderPath.setColumns(10);
		
		textApplicationWorkingDirectory = new JTextField();
		textApplicationWorkingDirectory.setEditable(false);
		textApplicationWorkingDirectory.setColumns(10);
		textApplicationWorkingDirectory.setBounds(102, 64, 533, 20);
		contentPane.add(textApplicationWorkingDirectory);
		
		labelWorkingDirectory = new JLabel("org.multipage.addinloader.textMainWorkingDirectory");
		labelWorkingDirectory.setBounds(10, 64, 94, 14);
		contentPane.add(labelWorkingDirectory);
	}
	
	/**
	 * On select author.
	 */
	protected void onSelectAuthor() {
		
		// Get selected authors that have signed JAR entries.
		int [] selectedRows = tableAuthors.getSelectedRows();
		int count = selectedRows.length;
		
		// Get selected items of the table.
		SignedFileTreeNode.selectedSigners = new CodeSigner [count];
		
		displayCertificates(treeCertificates, null);
		
		for (int index = 0; index < count; index++) {
			
			Object value = tableAuthors.getValueAt(index, 0);
			CodeSigner signer = (CodeSigner) value;
			SignedFileTreeNode.selectedSigners[index] = signer;
			
			// Display signer certificates.
			if (treeCertificates != null) {
				CertPath certificatesPath = signer.getSignerCertPath();
				displayCertificates(treeCertificates, certificatesPath);
			}
		}
		
		// Update packages and files tree.
		if (treePackagesAndFiles != null) {
			SwingUtilities.invokeLater(() -> treePackagesAndFiles.updateUI());
		}
	}
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			// Create the main application window.
			AddInLoader frame = new AddInLoader();
			
			// Use application arguments.
			frame.processArguments(args);
			
			// Set language and country.
			AddInsUtility.setLanguageAndCountry(language, country);
			
			// Load resources file.
			if (!AddInsUtility.loadResource(resourcesLocation)) {
				return;
			}
			
			// Load Add-in JAR file meta information.
			JarMetaInfo metaInfo = frame.readJarInformation(addInJarFile);
			
			// TODO: debug
			frame.textAreaMessages.setText(metaInfo.debug);
			
			// Show the application window.
			frame.display();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load information about the JAR file that contains add-in functions.
	 * @param - addInJarFile
	 */
	private JarMetaInfo readJarInformation(String addInJarFile) {
		
		// Initialize controls.
		JarMetaInfo metaInfo = new JarMetaInfo();
		
		// Set certificate, package and file renderer.
		TreeCellRenderer cellRenderer = new TreeCellRenderer() {
			
			DefaultTreeCellRenderer innerRenderer = new DefaultTreeCellRenderer();
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				SignedFileTreeNode.setIconImage(innerRenderer, expanded, leaf, value);
				return innerRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		};
		
		treeCertificates.setCellRenderer(cellRenderer);
		treePackagesAndFiles.setCellRenderer(cellRenderer);
		
		enableGuiEvents(false);
		displayNewAuthor(tableAuthors, null);
		displayFileOrFolder(treePackagesAndFiles, null);
		enableGuiEvents(true);
		
		JarFile jarFile = null;
		Process process = null;
		
		try {
			// Traverse all JAR file entries and add corresponding file or folder nodes into the tree.
			jarFile = new JarFile(addInJarFile);
			Collections.list(jarFile.entries()).forEach(jarEntry -> {
				displayFileOrFolder(treePackagesAndFiles, jarEntry);
			});
			
			// Create and run the "jarsigner" verifying process.
			String javaHome = System.getProperty("java.home");
			
			String javaSignerApp = '\"' + javaHome  + File.separatorChar + "bin" + File.separatorChar + "jarsigner\"";
			ProcessBuilder processBuilder = new ProcessBuilder(javaSignerApp, "-verify", "-verbose", addInJarFile);
			
			process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			 
			// Reading the output.
			String line = null;
		    while ((line = reader.readLine()) != null) {
		    	checkFileOrFolder(treePackagesAndFiles, line);
		    }
		    
		    // Expand all tree nodes.
		    AddInsUtility.expandAll(treePackagesAndFiles, true);
		}
		catch (Exception e) {
			AddInsUtility.show(this, "org.multipage.addinloader.messageJarSignerException", e.getLocalizedMessage());
		}
		finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				}
				catch (Exception e) {
				}
			}
			if (process != null) {
				process.destroyForcibly();
			}
		}
		
		// Create a set for all code signers.
		HashSet<CodeSigner> codeSigners = new HashSet<CodeSigner>();
		
			
		
		return metaInfo;
	}
	
	/**
	 * Enable/disable GUI events.
	 * @param enable
	 */
	private void enableGuiEvents(boolean enable) {
		
		tableAuthors.removePropertyChangeListener(onSelectAuthorEvent);
		if (enable) {
			tableAuthors.addPropertyChangeListener(onSelectAuthorEvent);
		}
	}

	/**
	 * TODO <---Display new author.
	 * @param tableAuthors
	 * @param signer
	 */
	private void displayNewAuthor(JTable tableAuthors, CodeSigner signer) {
		
		// Set up table model and cell renderer.
		DefaultTableModel model = null;
		if (signer == null) {
			
			model = new DefaultTableModel();
			
			// Set column model.
			DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
			
			TableColumn column = new TableColumn();
			
			column.setModelIndex(0);
			String columnName = AddInsUtility.getString("org.multipage.addinloader.titleAuthor");
			column.setHeaderValue(columnName);
			columnModel.addColumn(column);
			tableAuthors.setColumnModel(columnModel);
			model.addColumn(columnName);
			
			column.setModelIndex(1);
			columnName = "certificatePath";
			column.setHeaderValue(columnName);
			columnModel.addColumn(column);
			tableAuthors.setColumnModel(columnModel);
			model.addColumn(columnName);
			
			// Set author selection event.
			tableAuthors.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					onSelectAuthor();
				}
			});

			tableAuthors.setModel(model);
			return;
		}
		else {
			model = (DefaultTableModel) tableAuthors.getModel();
		}
		
		String author = "UNKNOWN";
		
		// Get certificates of the signer.
		CertPath certificatePath = signer.getSignerCertPath();
		List<? extends Certificate> certificates = certificatePath.getCertificates();
		if (certificates != null && !certificates.isEmpty()) {
			
			Certificate certificate = certificates.get(0);
			if (certificate instanceof X509Certificate) {
				
				X509Certificate x509Certificate = (X509Certificate) certificate;
				author = x509Certificate.getIssuerX500Principal().getName();
			}
		}
		
		// If the author (the signer) is not already displayed, add them to the table.
		int rowCount = model.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			
			// Get name of author from the table.
			try {
				Object cellValue = model.getValueAt(row, 0);
				if (!(cellValue instanceof String)) {
					continue;
				}
			
				// If the author already exists, exit this method.
				String tableAuthor = (String) cellValue;
				if (author.equals(tableAuthor)) {
					return;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Add new row.
		model.addRow(new Object [] {signer, author, certificatePath});
	}

	/**
	 * Add new item to the tree.
	 * @param tree
	 * @param jarEntry
	 */
	private void displayFileOrFolder(JTree tree, JarEntry jarEntry) {
		
		// Check input value.
		if (tree == null) {
			return;
		}
		
		// Get tree model.
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = null;
		
		// Remove old and create new root node of the tree.
		if (jarEntry == null) {
			
			// Create root node.
			String rootCaption = AddInsUtility.getString("org.multipage.addinloader.textSignedFiles");
			root = new DefaultMutableTreeNode(rootCaption);
			model.setRoot(root);
			return;
		}
		
		// Get root node.
		Object object = model.getRoot();
		if (!(object instanceof DefaultMutableTreeNode)) {
			return;
		}
		root = (DefaultMutableTreeNode) object;
		
		// Get file path.
		String filePath = jarEntry.getName();
		
		// Get signers.
		CodeSigner [] signers = jarEntry.getCodeSigners();
		
		// Setup current node.
		Obj<DefaultMutableTreeNode> parent = new Obj<DefaultMutableTreeNode>(root);
		
		// Add each path element to the tree model.
		Path path = Paths.get(filePath);
		
		path.forEach(pathElement -> {
			String pathElementName = pathElement.toString();

			int count = parent.ref.getChildCount();

			DefaultMutableTreeNode child = null;
			
			// Find matching node between child nodes of the parent node in the tree.
			boolean match = false;
			for (int index = 0; index < count; index++) {
				
				Object childNode = parent.ref.getChildAt(index);
				if (!(childNode instanceof DefaultMutableTreeNode)) {
					continue;
				}
				
				child = (DefaultMutableTreeNode) childNode;
				String childName = child.toString();
				
				if (childName.equals(pathElementName)) {
					match = true;
					break;
				}
			}
			
			// Create new node.
			if (!match) {
				child = new SignedFileTreeNode(pathElementName, signers);
				parent.ref.add(child);
			}
			
			// Move to child node.
			if (child != null) {
				parent.ref = child;
			}
		});
	}
	
	/**
	 * TODO: <---MAKE Parse text line taken from "jarsigner" tool output and display corresponding file or folder node
	 * check in input tree. 
	 * @param tree
	 * @param jarVerifierOutputLine
	 */
	private void checkFileOrFolder(JTree tree, String jarVerifierOutputLine) {
		
		// Extract file or folder path from the input line.
		Matcher matcher = regexVerifier.matcher(jarVerifierOutputLine);
		if (!matcher.find()) {
			return;
		}
		
		// Get file or folder path.
		String fileOrFolder = matcher.group("fileOrFolder");
		Path path = Paths.get(fileOrFolder);
		
		// Get verify result.
		String verifierFlags = matcher.group("verifierFlags");
		
		// Check matching node in the tree.
		Object rootObject = tree.getModel().getRoot();
		if (!(rootObject instanceof DefaultMutableTreeNode)) {
			return;
		}
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject; 
		Obj<DefaultMutableTreeNode> currentNode = new Obj<DefaultMutableTreeNode>(rootNode);
		
		Obj<Boolean> found = new Obj<Boolean>(true);
		path.forEach(pathElement -> {
			
			String pathElementName = pathElement.toString();
			
			int count = currentNode.ref.getChildCount();
			for (int index = 0; index < count; index++) {
				
				TreeNode childNode = currentNode.ref.getChildAt(index);
				if (!(childNode instanceof SignedFileTreeNode)) {
					return;
				}
				
				SignedFileTreeNode child = (SignedFileTreeNode) childNode;
				String childName = child.toString();
				
				if (pathElementName.equals(childName)) {
					currentNode.ref = child;
					return;
				}
			}
			
			found.ref = false;
		});
		
		// If the file or folder was not found...
		if (!found.ref) {
			// TODO: debug
			j.log("NOT FOUND [%s] %s", verifierFlags, fileOrFolder);
			return;
		}
		
		// Check tree node type.
		if (!(currentNode.ref instanceof SignedFileTreeNode)) {
			return;
		}
		
		// Set flags for the signed file node.
		SignedFileTreeNode signedFileNode = (SignedFileTreeNode) currentNode.ref;
		signedFileNode.setVerifierFlags(verifierFlags);
	}

	/**
	 * Add new item to the tree of certificates.
	 * @param tree
	 * @param certPath
	 */
	private void displayCertificates(JTree tree, CertPath certPath) {
		
		// Check input value.
		if (tree == null) {
			return;
		}
		
		// Get tree model.
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = null;
		
		// Remove old and create new root node of the tree.
		if (certPath == null) {
			
			// Create root node.
			String rootCaption = AddInsUtility.getString("org.multipage.addinloader.textCertificates");
			root = new DefaultMutableTreeNode(rootCaption);
			model.setRoot(root);
			return;
		}
		
		// Get root node.
		Object object = model.getRoot();
		if (!(object instanceof DefaultMutableTreeNode)) {
			return;
		}
		root = (DefaultMutableTreeNode) object;
		
		// Get certificates.
		List<? extends Certificate> certificates = certPath.getCertificates();
		if (certificates == null || certificates.isEmpty()) {
			return;
		}
		
		// Setup current node.
		Obj<DefaultMutableTreeNode> parent = new Obj<DefaultMutableTreeNode>(root);
		
		// TODO: debug
		j.log("--------------------");
		j.log("NEW CERTIFICATE PATH, len=%d", certificates.size());
		j.log("--------------------");
		
		// Add each certificate to the tree model.	
		certificates.forEach(certificate -> {
			
			// Create new node.
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(certificate);
			parent.ref.add(child);
			
			// TODO: debug
			j.log("NEW NODE %s -> %s", dump(parent.ref), dump(child));
			
			// Move to child node.
			parent.ref = child;
		});
	}
	
	// TODO: remove it
	private String dump(DefaultMutableTreeNode node) {
		
		Object userObject = node.getUserObject();
		if (userObject instanceof X509Certificate) {
			X509Certificate certificate = (X509Certificate) userObject;
			return certificate.getIssuerDN().getName();
		}
		return node.toString();
	}
	
	/**
	 * Process application arguments.
	 * @param args
	 */
	protected void processArguments(String[] args) {
		
		// Get Add-in JAR file.
		addInJarFile = getArgumentValue(args, "addInJarFile");
		
		// Get application path.
		applicationPath = getArgumentValue(args, "applicationFile");
		
		// Get application working directory.
		applicationWorkingDirectory = getArgumentValue(args, "applicationWorkingDirectory");
		
		// Get language.
		language = getArgumentValue(args, "language");
		
		// Get country.
		country = getArgumentValue(args, "country");
	}
	
	/**
	 * Get argument value.
	 * @param args
	 * @param argName
	 * @return
	 */
	private String getArgumentValue(String[] args, String argName) {
		
		// Get named argument value.
		final Pattern regex = Pattern.compile("^(.+?)\\s*=\\s*(.+?)$");
		final int count = args.length;
		
		for (int index = 0; index < count; index++) {
			
			String argument = args[index];
			Matcher matcher = regex.matcher(argument);
			
			boolean matches = matcher.matches();
			int groupCount = matcher.groupCount();
			
			if (!(matches &&  groupCount == 2)) {
				continue;
			}
			
			String name = matcher.group(1);
			if (name.equalsIgnoreCase(argName)) {
				String value = matcher.group(2);
				return value;
			}
		}
		
		// Return empty string if nothing was found.
		return "";
	}

	/**
	 * Center window on the screen.
	 * @param window
	 */
	public static void centerOnScreen(Window window) {

		// Get window width and height.
		int width = window.getWidth();
		int height = window.getHeight();

		// Get screen dimensions and set window location.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((screenSize.width - width) / 2,
				(screenSize.height - height) / 2);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Initialize the list view.
		initializeList();
		// Display Add-In certificates.
		displayAddInCertificates();
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		AddInsUtility.localize(this);
		AddInsUtility.localize(labelAddInJarFile);
		AddInsUtility.localize(labelApplicationPath);
		AddInsUtility.localize(labelWorkingDirectory);
		AddInsUtility.localize(labelLoaderPath);
		AddInsUtility.localize(labelAuthors);
		AddInsUtility.localize(labelCertificates);
		AddInsUtility.localize(labelPackagesAndFiles);
		AddInsUtility.localize(labelMessages);
		AddInsUtility.localize(buttonConfirm);
		AddInsUtility.localize(buttonCancel);

	}

	/**
	 * Set icons for components.
	 */
	private void setIcons() {
		
		// Set main icon.
		Image icon = AddInsUtility.getImage("org/multipage/addinloader/images/main_icon.png");
		setIconImage(icon);
	}
	
	/**
	 * Initialize the list view.
	 */
	private void initializeList() {
		
		// Create list of authors.
		
		
	}
	
	/**
	 * Display Add-In certificates.
	 */
	private void displayAddInCertificates() {
		
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * On Confirm button.
	 */
	protected void onConfirm() {
		
		try {
			
			// Try to load Add-In into the application JAR file or folder.
		}
		catch (Exception e) {
			
		}
	}
	
	/**
	 * On Cancel button.
	 */
	protected void onCancel() {
		
		try {
			// Run main application.
			AddInsUtility.runExecutableJar(applicationWorkingDirectory, applicationPath);
			// Close the frame.
			dispose();
		}
		catch (Exception e) {
			AddInsUtility.show(this, "org.multipage.addinloader.messageCannotStartMainApplication", e.getLocalizedMessage());
		}
	}
}
