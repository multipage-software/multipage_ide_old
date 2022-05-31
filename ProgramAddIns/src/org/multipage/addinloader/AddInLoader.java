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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

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
	private JTextField textLabelPath;
	private JLabel labelAddInJarFile;
	private JLabel labelApplicationPath;
	private JLabel labelLoaderPath;
	private JLabel labelAuthors;
	private JButton buttonConfirm;
	private JButton buttonCancel;
	
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
					
					// Display loader application path.
					String thisLoaderPath = AddInsUtility.getApplicationPath(AddInLoader.class);
					textLabelPath.setText(thisLoaderPath);
					
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
		scrollPaneContainer.setBounds(5, 95, 640, 467);
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
		treeCertificates.setBorder(null);
		scrollPaneCertificates.setViewportView(treeCertificates);
		
		JScrollPane scrollPaneAuthors = new JScrollPane();
		scrollPaneAuthors.setBackground(Color.WHITE);
		scrollPaneAuthors.setBorder(null);
		scrollPaneAuthors.setBounds(0, 11, 320, 82);
		panelAuthors.add(scrollPaneAuthors);
		scrollPaneAuthors.setPreferredSize(new Dimension(300, 70));
		
		tableAuthors = new JTable();
		tableAuthors.setBorder(null);
		scrollPaneAuthors.setViewportView(tableAuthors);
		
		labelAuthors = new JLabel("org.multipage.addinloader.textAddInAuthors");
		labelAuthors.setBounds(0, 0, 618, 12);
		panelAuthors.add(labelAuthors);
		
		labelAddInJarFile = new JLabel("org.multipage.addinloader.textAddInJarPath");
		labelAddInJarFile.setBounds(10, 10, 94, 14);
		contentPane.add(labelAddInJarFile);
		
		textAddInJarFile = new JTextField();
		textAddInJarFile.setEditable(false);
		textAddInJarFile.setBounds(102, 8, 533, 20);
		contentPane.add(textAddInJarFile);
		textAddInJarFile.setColumns(10);
		
		labelApplicationPath = new JLabel("org.multipage.addinloader.textMultipagePath");
		labelApplicationPath.setBounds(10, 38, 94, 14);
		contentPane.add(labelApplicationPath);
		
		textApplicationPath = new JTextField();
		textApplicationPath.setEditable(false);
		textApplicationPath.setBounds(102, 36, 533, 20);
		contentPane.add(textApplicationPath);
		textApplicationPath.setColumns(10);
		
		labelLoaderPath = new JLabel("org.multipage.addinloader.textLoaderPath");
		labelLoaderPath.setBounds(10, 65, 94, 14);
		contentPane.add(labelLoaderPath);
		
		textLabelPath = new JTextField();
		textLabelPath.setEditable(false);
		textLabelPath.setBounds(102, 64, 533, 20);
		contentPane.add(textLabelPath);
		textLabelPath.setColumns(10);
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
			
			// Show the application window.
			frame.display();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Process application arguments.
	 * @param args
	 */
	protected void processArguments(String[] args) {
		
		// Get Add-in JAR file.
		addInJarFile = getArgumentValue(args, "addInJarFile");
		
		// Get application path.
		applicationPath = getArgumentValue(args, "applicationAddInsPath");
		
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
		AddInsUtility.localize(labelLoaderPath);
		AddInsUtility.localize(labelAuthors);
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
		
		// Close the frame.
		dispose();
	}
}
