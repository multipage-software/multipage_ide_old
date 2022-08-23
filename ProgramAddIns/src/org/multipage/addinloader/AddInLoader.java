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
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
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
	 * Version number.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constants.
	 */
	private static final String UNKNOWN = "UNKNOWN";
	
	/**
	 * REGEX pattern that parse single output line returned by "jarsigner -verify -verbose" statement.
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
	 * REGEX pattern that checks if the path name designates a META-INF file.
	 */
	private static final Pattern regexMetaInfFile = Pattern.compile(
			"META-INF(/|\\\\)(MANIFEST.MF|.+?\\.SF|.+?\\.DSA|.+?\\.RSA|SIG-.)"
			);
	
	/**
	 * REGEX pattern that can parse issuer from appropriate X.509 certificate text item.
	 */
	private static final Pattern regexIssuerFields = Pattern.compile(
			"^\\s*CN\\s*=\\s*(?<commonName>.+?)\\s*,"
			+ "\\s*OU\\s*=\\s*(?<organizationalUnit>.+?)\\s*,"
			+ "\\s*O\\s*=\\s*(?<organization>.+?)\\s*,"
			+ "\\s*L\\s*=\\s*(?<locality>.+?)\\s*,"
			+ "\\s*ST\\s*=\\s*(?<stateOrProvinceName>.+?)\\s*,"
			+ "\\s*C\\s*=\\s*(?<countryName>.+?)\\s*$"
			);
	
	/**
	 * Author that has signed files.
	 */
	private static class Author {
		
		/**
		 * Reference to code signer.
		 */
		CodeSigner codeSigner = null;
		
		/**
		 * Name of the author.
		 */
		String commonName = UNKNOWN;

		/**
		 * X509 certificate path from signer to certification authority (last item in the lit).
		 */
		List<X509Certificate> certificatePath = null;
		
		// Issuer fields taken from first X.509 certificate in certificate path. 
		private String organizationalUnit = UNKNOWN;
		private String organization = UNKNOWN;
		private String locality = UNKNOWN;
		private String stateOrProvinceName = UNKNOWN;
		private String countryName = UNKNOWN;
		
		/**
		 * Constructor.
		 * @param codeSigner
		 */
		public Author(CodeSigner codeSigner) {

			this.codeSigner = codeSigner;
		}
		
		/**
		 * This method must be called called after the object is created. It will set up additional fields of the object.
		 */
		public void postCreate() throws Exception {

			// Get full name and the certificates of the author.
			certificatePath = new LinkedList<X509Certificate>();
			CertPath certificatePath = codeSigner.getSignerCertPath();
			
			List<? extends Certificate> certificates = certificatePath.getCertificates();
			if (certificates != null && !certificates.isEmpty()) {
				
				Certificate certificate = certificates.get(0);
				if (certificate instanceof X509Certificate) {
					
					X509Certificate x509Certificate = (X509Certificate) certificate;
					
					// Parse issuer fields.
					String issuerTextLine = x509Certificate.getIssuerX500Principal().getName();
					
					Matcher matcher = regexIssuerFields.matcher(issuerTextLine);
					if (!matcher.find()) {
						AddInsUtility.throwException("org.multipage.addinloader.messageExpectingIssuerInX509Certificate");
					}
					
					// Set issuer fields.
					commonName = matcher.group("commonName");
					organizationalUnit = matcher.group("organizationalUnit");
					organization = matcher.group("organization");
					locality = matcher.group("locality");
					stateOrProvinceName = matcher.group("stateOrProvinceName");
					countryName = matcher.group("countryName");
				}
				else {
					AddInsUtility.throwException("org.multipage.addinloader.messageExpectingX509CertificateType");
				}
			}
		}
		
		/**
		 * Create hash code for this object based on code signer.
		 */
		@Override
		public int hashCode() {
			return codeSigner.hashCode();
		}
		
		/**
		 * The input object equals if it has same code signer field.
		 */
		@Override
		public boolean equals(Object object) {
			
			if (!(object instanceof Author)) {
				return false;
			}
			Author author = (Author) object;
			return author.codeSigner.equals(this.codeSigner);
		}
		
		/**
		 * Return common name of author found in X.509 certificate.
		 */
		@Override
		public String toString() {
			
			return commonName;
		}		
	}
	
	/**
	 * Certificate as a tree node.
	 */
	private static class CertificateTreeNode extends DefaultMutableTreeNode {
		
		/**
		 * Version.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * X.509 certificate object.
		 */
		public Certificate certificate = null;
		
		/**
		 * Common name of a certificate subject.
		 */
		private String commonName = UNKNOWN;
		
		/**
		 * Constructor.
		 * @param certificate
		 */
		public CertificateTreeNode(Certificate certificate) {
			
			this.certificate = certificate;
		}
		
		/**
		 * Constructor of the root node.
		 * @param rootCaption
		 */
		public CertificateTreeNode(String rootCaption) {
			
			this.commonName = rootCaption;
		}

		/**
		 * Post creation of the object.
		 */
		private void postCreate() {
			
			if (certificate instanceof X509Certificate) {
				X509Certificate x509Certificate = (X509Certificate) certificate;
				String subject = x509Certificate.getSubjectDN().getName();
				
				Matcher matcher = regexIssuerFields.matcher(subject);
				if (!matcher.find()) {
					return;
				}
				
				// Set issuer fields.
				commonName = matcher.group("commonName");
			}
		}
		
		/**
		 * Get text representation of the certificate.
		 */
		@Override
		public String toString() {
			
			return commonName;
		}
	}
	
	/**
	 * Signed file or folder as a tree node.
	 */
	private static class SignedFileTreeNode extends DefaultMutableTreeNode {
		
		/**
		 * Version.
		 */
		private static final long serialVersionUID = 1L;
		
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
		public boolean signatureVerified = false;
		public boolean listedInManifest = false;
		public boolean certificateInKeystore = false;
		public boolean isMetaInfFile = false;
		public boolean notSigned = true;

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
		 * Constructor of the root node.
		 * @param rootCaption
		 */
		public SignedFileTreeNode(String rootCaption) {
			
			this.fileName = rootCaption;
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
				boolean metaInf = signedFile.isMetaInfFile;
				
				if (leaf) {
					Icon icon = (signed ? signedFileIcon : (metaInf ? fileIcon : badFileIcon));
					renderer.setLeafIcon(icon);
				}
				else {
					Icon icon = (signed ? signedFolderIcon : badFolderIcon);
					renderer.setOpenIcon(icon);
					renderer.setClosedIcon(icon);
				}
			}
			else if (value instanceof CertificateTreeNode) {
				
				CertificateTreeNode certificateNode = (CertificateTreeNode) value;
				
				// Set up flag according to certificate.
				Icon icon = null;
				
				Certificate certificate = certificateNode.certificate;
				if (certificate != null) {
					
					PublicKey publicKey = certificate.getPublicKey();
					boolean verified = false;
					try {
						certificate.verify(publicKey);
						verified = true;
					}
					catch (Exception e) {
					}
					icon = (verified ? signedCertificateIcon : badCertificateIcon);
				}
				else {
					icon = folderIcon; 
				}
				
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
	private JLabel labelMessages;
	private JTextArea textAreaMessages;
	private JTree treePackagesAndFiles;
	private JTree treeCertificates;
	
	// GUI events.
	private PropertyChangeListener onSelectAuthorEvent = event -> onSelectAuthor();
	
	/**
	 * Set of all authors.
	 */
	private HashSet<Author> allAuthors = null;
	
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
		panelAuthors.setOpaque(false);
		panelAuthors.setBackground(SystemColor.window);
		panelAuthors.setBorder(null);
		panelAuthors.setBounds(10, 11, 618, 158);
		panelWithItems.add(panelAuthors);
		panelAuthors.setLayout(null);
		
		JScrollPane scrollPaneCertificates = new JScrollPane();
		scrollPaneCertificates.setOpaque(false);
		scrollPaneCertificates.setBounds(409, 33, 209, 124);
		scrollPaneCertificates.setPreferredSize(new Dimension(300, 70));
		panelAuthors.add(scrollPaneCertificates);
		
		treeCertificates = new JTree();
		treeCertificates.setBackground(Color.WHITE);
		scrollPaneCertificates.setViewportView(treeCertificates);
		
		JScrollPane scrollPaneAuthors = new JScrollPane();
		scrollPaneAuthors.setBackground(Color.WHITE);
		scrollPaneAuthors.setBorder(null);
		scrollPaneAuthors.setBounds(0, 11, 410, 146);
		panelAuthors.add(scrollPaneAuthors);
		scrollPaneAuthors.setPreferredSize(new Dimension(300, 70));
		
		tableAuthors = new JTable();
		tableAuthors.setFillsViewportHeight(true);
		tableAuthors.setGridColor(SystemColor.control);
		tableAuthors.setForeground(SystemColor.windowText);
		tableAuthors.setFont(new Font("Tahoma", Font.PLAIN, 13));
		tableAuthors.addPropertyChangeListener(onSelectAuthorEvent);
		tableAuthors.setBorder(null);
		scrollPaneAuthors.setViewportView(tableAuthors);
		
		labelAuthors = new JLabel("org.multipage.addinloader.textAddInAuthors");
		labelAuthors.setBorder(null);
		labelAuthors.setBounds(0, 0, 320, 12);
		panelAuthors.add(labelAuthors);
		
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

		displayCertificates(treeCertificates, null);
		
		for (int row : selectedRows) {
			
			Object value = tableAuthors.getValueAt(row, 0);
			if (!(value instanceof Author)) {
				return;
			}
			
			Author author = (Author) value;
			CodeSigner codeSigner = author.codeSigner;
			
			// Display signer certificates.
			if (treeCertificates != null) {
				CertPath certificatesPath = codeSigner.getSignerCertPath();
				displayCertificates(treeCertificates, certificatesPath);
			}
		}
		
		// Update packages and files tree.
		if (treePackagesAndFiles != null) {
			SwingUtilities.invokeLater(() -> {
				treePackagesAndFiles.updateUI();
				AddInsUtility.expandAll(treeCertificates, true);
			});
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
			frame.displayJarVerificationInfo(addInJarFile);
			
			// Show the application window.
			frame.display();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load information about the JAR file that contains add-in classes.
	 * @param - addInJarFile
	 */
	private void displayJarVerificationInfo(String addInJarFile) {
		
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
		
		final Obj<JarFile> jarFile = new Obj<JarFile>(null);
		Process process = null;
		
		try {
			// Initialize controls.
			enableGuiEvents(false);
			
			displayFileOrFolder(treePackagesAndFiles, null);
			displayNewAuthor(tableAuthors, null);
			
			enableGuiEvents(true);
			
			// Read JAR entries and check its signatures.
			
			// Traverse all JAR file entries and add corresponding file or folder nodes into the tree.
			jarFile.ref = new JarFile(addInJarFile);
			Obj<Exception> exception = new Obj<Exception>(null);
			Collections.list(jarFile.ref.entries()).forEach(jarEntry -> {
				
				InputStream inputStream = null;
				try {
					inputStream = jarFile.ref.getInputStream(jarEntry);
					inputStream.readAllBytes();
					
					displayFileOrFolder(treePackagesAndFiles, jarEntry);
					
					CodeSigner [] authors = jarEntry.getCodeSigners();
					if (authors != null) {
						for (CodeSigner author : authors) {
							displayNewAuthor(tableAuthors, author);
						}
					}
				}
				catch (Exception e) {
					if (exception.ref == null) {
						exception.ref = e;
					}
				}
				finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} 
						catch (Exception e) {
						}
					}
				}
			});
			
			// Throw possible exception.
			if (exception.ref != null) {
				throw exception.ref;
			}
			
			// Create and run the "jarsigner" verifying process.
			String javaHome = System.getProperty("java.home");
			
			String javaSignerApp = '\"' + javaHome  + File.separatorChar + "bin" + File.separatorChar + "jarsigner\"";
			ProcessBuilder processBuilder = new ProcessBuilder(javaSignerApp, "-verify", "-verbose", addInJarFile);
			
			process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			 
			// Reading the output lines and display the signature checks.
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
			if (jarFile.ref != null) {
				try {
					jarFile.ref.close();
				}
				catch (Exception e) {
				}
			}
			if (process != null) {
				process.destroyForcibly();
			}
		}
		
		// Derive verifier flags for all non leaf nodes.
		Object rootObject = treePackagesAndFiles.getModel().getRoot();
		if (rootObject instanceof SignedFileTreeNode) {
			
			SignedFileTreeNode rootNode = (SignedFileTreeNode) rootObject;
			
			boolean signaturesVerified = derivePackageFlags(rootNode);
			updateRootNodeFlags(rootNode, signaturesVerified);
		}
	}

	/**
	 * Derive flags for all packages and other non leaf nodes.
	 * @param currentNode
	 * @return true, if all children are signed, else return false.
	 */
	private boolean derivePackageFlags(TreeNode currentNode) {
		
		// Cast node to signed file.
		SignedFileTreeNode currentSignedFile = null;
		if (currentNode instanceof SignedFileTreeNode) {
			currentSignedFile = (SignedFileTreeNode) currentNode;
		}
		
		// Get child nodes.
		int count = currentNode.getChildCount();
		if (count <= 0) {
			
			// Check if the leaf node is signed.
			if (currentSignedFile != null) {
				return currentSignedFile.signatureVerified || currentSignedFile.isMetaInfFile;
			}
		}
		
		// Call current method recursively for all child nodes and check if they are signed.
		boolean childSignaturesVerified = true;
		
		for (int index = 0; index < count; index++) {

			TreeNode childNode = currentNode.getChildAt(index);
			boolean childrenSigned = derivePackageFlags(childNode);
			
			if (!childrenSigned) {
				childSignaturesVerified = false;
			}
		}
		
		// Set node flag.
		if (currentSignedFile != null) {
			currentSignedFile.signatureVerified = childSignaturesVerified;
		}
		
		return childSignaturesVerified;
	}
	
	/**
	 * Update root node flags.
	 * @param rootNode
	 * @param signaturesVerified 
	 */
	private void updateRootNodeFlags(SignedFileTreeNode rootNode, boolean signaturesVerified) {
		
		rootNode.signatureVerified = signaturesVerified;
		rootNode.fileName = AddInsUtility.getString(
				signaturesVerified ? "org.multipage.addinloader.textSignaturesVerified"
								   : "org.multipage.addinloader.textNotAllSignaturesVerified");
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
	 * Display new author.
	 * @param tableAuthors
	 * @param codeSigner
	 */
	private void displayNewAuthor(JTable tableAuthors, CodeSigner codeSigner)
		throws Exception {
		
		// Set up table model and cell renderer.
		DefaultTableModel model = null;
		if (codeSigner == null) {
			
			model = new DefaultTableModel();
			
			// Set column model.
			DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
			
			TableColumn column = new TableColumn();
			
			column.setModelIndex(0);
			String columnName = AddInsUtility.getString("org.multipage.addinloader.titleAuthorName");
			column.setHeaderValue(columnName);
			columnModel.addColumn(column);
			tableAuthors.setColumnModel(columnModel);
			model.addColumn(columnName);
			
			column.setModelIndex(1);
			columnName = AddInsUtility.getString("org.multipage.addinloader.titleAuthorRole");
			column.setHeaderValue(columnName);
			columnModel.addColumn(column);
			tableAuthors.setColumnModel(columnModel);
			model.addColumn(columnName);
			
			column.setModelIndex(2);
			columnName = AddInsUtility.getString("org.multipage.addinloader.titleAuthorLocality");
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

			tableAuthors.getTableHeader().setOpaque(false);
			tableAuthors.setModel(model);
			return;
		}
		else {
			model = (DefaultTableModel) tableAuthors.getModel();
		}
		
		// Ensure the set of authors is created.
		if (allAuthors == null) {
			allAuthors = new HashSet<Author>();
		}
		
		// Create new author.
		Author author = new Author(codeSigner);
		
		// Check if author is already inserted.
		if (allAuthors.contains(author)) {
			return;
		}
		
		// Complete creation of the author object and add it to the set of all found authors.
		author.postCreate();
		allAuthors.add(author);
		
		// Add new table item.
		model.addRow(new Object [] {
				author,
				author.organizationalUnit,
				author.locality
				});
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
		SignedFileTreeNode root = null;
		
		// Remove old and create new root node of the tree.
		if (jarEntry == null) {
			
			// Create root node.
			String rootCaption = AddInsUtility.getString("org.multipage.addinloader.textSignedFiles");
			root = new SignedFileTreeNode(rootCaption);
			model.setRoot(root);
			return;
		}
		
		// Get root node.
		Object object = model.getRoot();
		if (!(object instanceof SignedFileTreeNode)) {
			return;
		}
		root = (SignedFileTreeNode) object;
		
		// Get file path.
		String filePath = jarEntry.getName();
		
		// Get signers.
		CodeSigner [] signers = jarEntry.getCodeSigners();
		
		// Setup current node.
		Obj<SignedFileTreeNode> parent = new Obj<SignedFileTreeNode>(root);
		
		// Add each path element to the tree model.
		Path path = Paths.get(filePath);
		boolean isMetaInfFile = isMetaInfFile(path);
		
		path.forEach(pathElement -> {
			String pathElementName = pathElement.toString();

			int count = parent.ref.getChildCount();

			SignedFileTreeNode child = null;
			
			// Find matching node between child nodes of the parent node in the tree.
			boolean match = false;
			for (int index = 0; index < count; index++) {
				
				Object childNode = parent.ref.getChildAt(index);
				if (!(childNode instanceof DefaultMutableTreeNode)) {
					continue;
				}
				
				child = (SignedFileTreeNode) childNode;
				String childName = child.toString();
				
				if (childName.equals(pathElementName)) {
					match = true;
					break;
				}
			}
			
			// Create new node.
			if (!match) {
				SignedFileTreeNode childSigned = new SignedFileTreeNode(pathElementName, signers);
				childSigned.isMetaInfFile = isMetaInfFile;
				child = childSigned;
				parent.ref.add(child);
			}
			
			// Move to child node.
			if (child != null) {
				parent.ref = child;
			}
		});
	}
	
	/**
	 * Returns true, if the input path designates a META-INFO file.
	 * @param path
	 * @return
	 */
	private boolean isMetaInfFile(Path path) {
		
		String pathName = path.toString();
		Matcher matcher = regexMetaInfFile.matcher(pathName);
		
		boolean isMetaInfFile = matcher.find();
		return isMetaInfFile;
	}

	/**
	 * Parse text line taken from "jarsigner" tool output and display corresponding file or folder node
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
		CertificateTreeNode root = null;
		
		// Remove old and create new root node of the tree.
		if (certPath == null) {
			
			// Create root node.
			String rootCaption = AddInsUtility.getString("org.multipage.addinloader.textCertificates");
			root = new CertificateTreeNode(rootCaption);
			model.setRoot(root);
			return;
		}
		
		// Get root node.
		Object object = model.getRoot();
		if (!(object instanceof CertificateTreeNode)) {
			return;
		}
		root = (CertificateTreeNode) object;
		
		// Get certificates.
		List<? extends Certificate> certificates = certPath.getCertificates();
		if (certificates == null || certificates.isEmpty()) {
			return;
		}
		
		// Setup current node.
		Obj<CertificateTreeNode> parent = new Obj<CertificateTreeNode>(root);
		
		// Add each certificate to the tree model.	
		certificates.forEach(certificate -> {
			
			// Create new node.
			CertificateTreeNode child = new CertificateTreeNode(certificate);
			child.postCreate();
			parent.ref.add(child);
			
			// Move to child node.
			parent.ref = child;
		});
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
	 * On Confirm button.
	 */
	protected void onConfirm() {
		
		try {
			String destinationPackage = "org.multipage.addins";
			AddInsUtility.exportJarPackageToJarFile(addInJarFile, "", applicationPath, destinationPackage);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
