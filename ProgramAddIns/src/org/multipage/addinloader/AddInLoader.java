/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 15-04-2022
 *
 */

package org.multipage.addinloader;

import java.awt.Color;
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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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

		public String debug = "";

	}

	/**
	 * Version number.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Authors.
	 */
	private class Authors {
		
		
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
		setBounds(100, 100, 666, 647);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		buttonConfirm = new JButton("org.multipage.addinloader.textConfirm");
		buttonConfirm.setBounds(469, 568, 80, 25);
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
		buttonCancel.setBounds(555, 568, 80, 25);
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
		scrollPaneContainer.setBounds(5, 125, 640, 432);
		contentPane.add(scrollPaneContainer);
		
		JPanel panelWithItems = new JPanel();
		scrollPaneContainer.setViewportView(panelWithItems);
		panelWithItems.setLayout(null);
		
		panelAuthors = new JPanel();
		panelAuthors.setBounds(10, 11, 618, 93);
		panelWithItems.add(panelAuthors);
		panelAuthors.setLayout(null);
		
		JScrollPane scrollPaneCertificates = new JScrollPane();
		scrollPaneCertificates.setBorder(null);
		scrollPaneCertificates.setBounds(330, 11, 288, 82);
		scrollPaneCertificates.setPreferredSize(new Dimension(300, 70));
		panelAuthors.add(scrollPaneCertificates);
		
		JTree treeCertificates = new JTree();
		treeCertificates.setOpaque(false);
		treeCertificates.setBackground(Color.WHITE);
		treeCertificates.setBorder(new LineBorder(new Color(230, 230, 250)));
		scrollPaneCertificates.setViewportView(treeCertificates);
		
		JScrollPane scrollPaneAuthors = new JScrollPane();
		scrollPaneAuthors.setBackground(Color.WHITE);
		scrollPaneAuthors.setBorder(new LineBorder(new Color(230, 230, 250)));
		scrollPaneAuthors.setBounds(0, 11, 320, 82);
		panelAuthors.add(scrollPaneAuthors);
		scrollPaneAuthors.setPreferredSize(new Dimension(300, 70));
		
		tableAuthors = new JTable();
		tableAuthors.setBorder(null);
		scrollPaneAuthors.setViewportView(tableAuthors);
		
		labelAuthors = new JLabel("org.multipage.addinloader.textAddInAuthors");
		labelAuthors.setBounds(0, 0, 320, 12);
		panelAuthors.add(labelAuthors);
		
		labelCertificates = new JLabel("org.multipage.addinloader.textAddInCertificates");
		labelCertificates.setBounds(330, 0, 288, 12);
		panelAuthors.add(labelCertificates);
		
		JScrollPane scrollPaneMessages = new JScrollPane();
		scrollPaneMessages.setBounds(10, 328, 620, 104);
		panelWithItems.add(scrollPaneMessages);
		
		textAreaMessages = new JTextArea();
		scrollPaneMessages.setViewportView(textAreaMessages);
		
		labelPackagesAndFiles = new JLabel("org.multipage.addinloader.textAddInPackagesAndFiles");
		labelPackagesAndFiles.setBounds(10, 115, 618, 14);
		panelWithItems.add(labelPackagesAndFiles);
		
		labelMessages = new JLabel("org.multipage.addinloader.textAddInMessages");
		labelMessages.setBounds(10, 315, 618, 14);
		panelWithItems.add(labelMessages);
		
		JScrollPane scrollPanePackagesAndFiles = new JScrollPane();
		scrollPanePackagesAndFiles.setBounds(8, 130, 620, 176);
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
			JarMetaInfo metaInfo = getMetaInformation(addInJarFile);
			frame.textAreaMessages.setText(metaInfo.debug);
			
			// Load packages and files.
			frame.loadPackagesAndFiles();
			
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
	private static JarMetaInfo getMetaInformation(String addInJarFile) {
		
		// Initialize.
		JarMetaInfo metaInfo = new JarMetaInfo();
		final JarFile [] jarFile = new JarFile [] { null };
		
		try {
			// Open the jar file and preliminary verify if it was signed.
			jarFile[0] = new JarFile(addInJarFile, true);

			// Get JAR entries.
			Collections.list(jarFile[0].entries()).forEach(jarEntry -> {
				
				// Read entry content to enable following "getCodeSigners()" function to return non null array.
				InputStream inputStream = null;
				try {
					inputStream = jarFile[0].getInputStream(jarEntry);
					inputStream.readAllBytes();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					}
					catch (Exception e) {
					}
				}
			});
			
			// TODO: debug
			String [] debugText = { "" };
			
			Collections.list(jarFile[0].entries()).forEach(jarEntry -> {
				
				// Skip folders.
				if (jarEntry.isDirectory()) {
					return;
				}
				
				debugText[0] += "\n\n-----------------------------------------------\n";
				debugText[0] += jarEntry.getName();
				debugText[0] += "\n-----------------------------------------------\n";
				
				// TODO: test
				int [] s = {1};
				ByteArrayInputStream inputStream = null;
				try {
					CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
					Certificate [] certificates = jarEntry.getCertificates();
					
					byte [] bytes = certificates[0].getEncoded();
					inputStream = new ByteArrayInputStream(bytes);
					
					Certificate certificate = certificateFactory.generateCertificate(inputStream);
					String certificateType = certificate.getType();
					
					if ("X.509".equals(certificateType) && (certificate instanceof X509Certificate)) {
						
						X509Certificate x509Certificate = (X509Certificate) certificate;
						System.out.format("%s\n", x509Certificate.getSubjectX500Principal().getName());
						
						PublicKey publicKey = x509Certificate.getPublicKey();
						String algorithm = publicKey.getAlgorithm();
						
						if ("DSA".equals(algorithm) && (publicKey instanceof DSAPublicKey)) {
							
							System.out.format("%s\n", algorithm);
							DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
							
							// Verify the self-signed certificate.
							try {
								
								// Load X.509 certificate from the file.
								final String certificatePath = "C:\\from_master\\multipage_builder\\josh.cer";
								FileInputStream inputStreamCA = new FileInputStream(certificatePath);
								Certificate certificateCA = certificateFactory.generateCertificate(inputStreamCA);
								X509Certificate x509CertificateCA = (X509Certificate) certificateCA;
								System.out.println(x509CertificateCA);
								PublicKey publicKeyCA = x509CertificateCA.getPublicKey();
								DSAPublicKey dsaPublicKeyCA = (DSAPublicKey) publicKeyCA;
								
								x509Certificate.verify(dsaPublicKeyCA);
								System.out.format("%s VERIFIED\n", x509Certificate.getIssuerX500Principal().getName());
								
								// Check expiration of the certificate.
								x509Certificate.checkValidity();
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
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
				
				// Get signers.
				CodeSigner [] signers = jarEntry.getCodeSigners();
				if (signers == null) {
					return;
				}
				Arrays.stream(signers).forEachOrdered(signer -> {
					
					Timestamp timestamp = signer.getTimestamp();
					if (timestamp != null) {
						System.out.println(timestamp);
					}

					CertPath certificatePath = signer.getSignerCertPath();
					List<? extends Certificate> certificateList = certificatePath.getCertificates();
					
					int c = 1;
					String divider = "\t";
					for (Certificate certificate : certificateList) {
						
						X509Certificate x509Certificate = (X509Certificate) certificate;
						debugText[0] += divider + s[0] + '.' + c + ". " + x509Certificate.getIssuerDN().getName();
						divider = " <- ";
						
						c++;
					}
					debugText[0] += '\n';
					System.out.println("ok");
					s[0]++;
				});
			});
			
			// Get manifest.
			Manifest manifest = jarFile[0].getManifest();
			Map<String, Attributes> manifestEntries = manifest.getEntries();
			
			metaInfo.debug = debugText[0];
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			
			// Close the JAR file.
			if (jarFile != null) {
				try {
					jarFile[0].close();
				}
				catch (Exception e) {
				}
			}
		}
		
		return metaInfo;
	}
	
	/**
	 * Load packages and files.
	 */
	private void loadPackagesAndFiles() {
		
		JarFile file = null;
		try {
			// TODO: Traverse the add-in JAR file.
			file = new JarFile(addInJarFile);
			Enumeration<JarEntry> entries = file.entries();
			
			addFileOrFolder(treePackagesAndFiles, null);
			
			while (entries.hasMoreElements()) {
				
				// Get file path in JAR.
				JarEntry entry = entries.nextElement();
				String filePath = entry.getName();
				
				// Add new file or folder to the tree.
				addFileOrFolder(treePackagesAndFiles, filePath);
				
				// TODO:debug
				String messages = textAreaMessages.getText();
				messages += filePath + '\n';
				textAreaMessages.setText(messages);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (file != null) {
				try {
					file.close();
				}
				catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * Add new item to the tree.
	 * @param tree
	 * @param filePath
	 */
	private void addFileOrFolder(JTree tree, String filePath) {
		
		// Get tree model.
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = null;
		
		// Remove old and create new root node of the tree.
		if (filePath == null) {
			// Create root node.
			root = new DefaultMutableTreeNode("root");
			model.setRoot(root);
			return;
		}
		else {
			// Get root node.
			Object object = model.getRoot();
			if (!(object instanceof DefaultMutableTreeNode)) {
				return;
			}
			root = (DefaultMutableTreeNode) object;
		}
		
		// Setup current node.
		DefaultMutableTreeNode [] current = new DefaultMutableTreeNode [] {root};
		
		// Add each path element to the tree model.
		Path path = Paths.get(filePath);
		j.log("INPUT PATH %s", path.toString());
		
		path.forEach(pathElement -> {
			String pathElementName = pathElement.toString();
			
			boolean match = false;
			
			int count = current[0].getChildCount();
			for (int index = 0; index < count; index++) {
				
				Object nodeObject = current[0].getChildAt(index);
				if (!(nodeObject instanceof DefaultMutableTreeNode)) {
					continue;
				}
				
				current[0] = (DefaultMutableTreeNode) nodeObject;
				String nodeName = current.toString();
				
				if (nodeName.equals(pathElementName)) {
					match = true;
					break;
				}
			}
			
			// Create new node.
			if (!match) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(pathElementName);
				current[0].add(node);
				//j.log("%s->%s", current[0], node);
				current[0] = node;
			}
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
