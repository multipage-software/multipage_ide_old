/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import java.awt.image.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.multipage.gui.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.LinkedList;

/**
 * 
 * @author
 *
 */
public class LoadFlagDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * List model.
	 */
	private DefaultListModel listModel;

	/**
	 * Flags directory.
	 */
	private static final String flagsDirectory= "org/multipage/translator/images/flags/";
	
	/**
	 * Image.
	 */
	private BufferedImage image;
	private JPanel panel;
	private JButton buttonCancel;
	private JButton buttonSelect;
	private JScrollPane scrollPane;
	private JList list;
	private JPanel panelTop;
	private JLabel labelFilter;
	private JTextField textFilter;

	/**
	 * Launch the dialog.
	 */
	public static BufferedImage showDialog(Window parentWindow) {

		LoadFlagDialog dialog = new LoadFlagDialog(parentWindow);
		dialog.setVisible(true);
		
		return dialog.image;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public LoadFlagDialog(Window parentWindow) {
		super(parentWindow, ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// Initialize components.
		initComponents();
		// Post creation.
		postCreate();
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.translator.textLoadFlag");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 40));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -7, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		panel.add(buttonCancel);
		
		buttonSelect = new JButton("textSelect");
		buttonSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelect();
			}
		});
		buttonSelect.setMargin(new Insets(0, 0, 0, 0));
		buttonSelect.setPreferredSize(new Dimension(80, 25));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonSelect, 0, SpringLayout.SOUTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonSelect, -6, SpringLayout.WEST, buttonCancel);
		panel.add(buttonSelect);
		
		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onMouseClick(e);
			}
		});
		scrollPane.setViewportView(list);
		
		panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(10, 30));
		panelTop.setSize(new Dimension(0, 100));
		getContentPane().add(panelTop, BorderLayout.NORTH);
		SpringLayout sl_panelTop = new SpringLayout();
		panelTop.setLayout(sl_panelTop);
		
		labelFilter = new JLabel("org.multipage.translator.textFilter");
		sl_panelTop.putConstraint(SpringLayout.WEST, labelFilter, 10, SpringLayout.WEST, panelTop);
		panelTop.add(labelFilter);
		
		textFilter = new JTextField();
		sl_panelTop.putConstraint(SpringLayout.WEST, textFilter, 6, SpringLayout.EAST, labelFilter);
		sl_panelTop.putConstraint(SpringLayout.EAST, textFilter, -6, SpringLayout.EAST, panelTop);
		sl_panelTop.putConstraint(SpringLayout.NORTH, labelFilter, 3, SpringLayout.NORTH, textFilter);
		sl_panelTop.putConstraint(SpringLayout.NORTH, textFilter, 4, SpringLayout.NORTH, panelTop);
		panelTop.add(textFilter);
		textFilter.setColumns(10);
	}

	/**
	 * On mouse click.
	 * @param e
	 */
	protected void onMouseClick(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			
			onSelect();
		}
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Localize components.
		localize();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set icons.
		setIcons();
		// Initialize list.
		initializeList();
		// Initialize filter.
		initializeFilter();
	}

	/**
	 * Initialize filter.
	 */
	private void initializeFilter() {
		
		textFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onChangeFilter();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChangeFilter();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onChangeFilter();
			}
		});
	}

	/**
	 * On change filter.
	 */
	protected void onChangeFilter() {
		
		String filterText = textFilter.getText();
		loadList(filterText);
	}

	/**
	 * Localize components
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonSelect);
		Utility.localize(buttonCancel);
		Utility.localize(labelFilter);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/translator/images/main_icon.png"));
		buttonSelect.setIcon(Images.getIcon("org/multipage/translator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/translator/images/cancel_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		image = null;
		dispose();
	}
	
	/**
	 * On select.
	 */
	protected void onSelect() {

		// Get selected image.
		Object selected = list.getSelectedValue();
		if (!(selected instanceof CountryFlag)) {
			Utility.show(this, "org.multipage.translator.messageSelectFlag");
			return;
		}
		
		CountryFlag flag = (CountryFlag) selected;
		image = flag.image;
		
		dispose();
	}

	/**
	 * Initialize list.
	 */
	private void initializeList() {
		
		// Set renderer.
		list.setCellRenderer(new ListCellRenderer() {
			// Renderer.
			private JFlagLabel renderer = new JFlagLabel();
			// Return renderer.
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				// Check value.
				if (!(value instanceof CountryFlag)) {
					return null;
				}
				// Get flag object.
				CountryFlag flag = (CountryFlag) value;
				// Set renderer properties.
				renderer.setProperties(flag.label, flag.image, index,
						isSelected, cellHasFocus);
				
				return renderer;
			}
		});
		
		// Load list.
		loadList("");
	}

	/**
	 * Load list.
	 * @param filterText
	 */
	private void loadList(String filterText) {
		
		// Create default list model.
		listModel = new DefaultListModel();
	
		// Get directory listing.
		LinkedList<String> flagFiles = new LinkedList<String>();
		
		try {
			// Get flags directory.
			URL urlListing = ClassLoader.getSystemResource(flagsDirectory + "list.txt");
			// Read all the text returned by the serverText.
		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlListing.openStream()));
	
			String imageFile;
			while ((imageFile = reader.readLine()) != null) {
			    
				flagFiles.add(imageFile);
			}
				
			reader.close();	
		}
		catch (IOException e) {
			// Report error and exit.
			Utility.show(this, "org.multipage.translator.messageErrorLoadingFlagsList");
			return;
		}
	
		// Do loop for all flag files.
		for (String flagFile : flagFiles) {
	
			if (!filterText.isEmpty()) {
				if (!Utility.matches(flagFile, filterText, false, false, false)) {
					continue;
				}
			}
			// Try to load the file.
			URL urlFile = ClassLoader.getSystemResource(flagsDirectory + flagFile);
			if (urlFile == null) {
				System.out.println("Error: cannot load flag file.");
				continue;
			}
			try {
				BufferedImage image = ImageIO.read(urlFile);
				
				// Create flag object and add it to the list.
				CountryFlag countryFlag = new CountryFlag(flagFile, image);
				listModel.addElement(countryFlag);
				
				// Set list model.
				list.setModel(listModel);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

/**
 * 
 * @author
 *
 */
class CountryFlag {
	
	/**
	 * Label.
	 */
	public String label;
	
	/**
	 * Image.
	 */
	public BufferedImage image;
	
	/**
	 * Constructor.
	 */
	public CountryFlag(String label, BufferedImage image) {
		
		this.label = label;
		this.image = image;
	}
}

/**
 * 
 * @author
 *
 */
class JFlagLabel extends JLabel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Is selected.
	 */
	private boolean isSelected;
	
	/**
	 * Has focus.
	 */
	private boolean hasFocus;
	
	/**
	 * Constructor.
	 */
	public JFlagLabel () {
		
		setOpaque(true);
		setIconTextGap(20);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// Call parent.
		super.paint(g);
		// Draw selection.
		GraphUtility.drawSelection(g, this, isSelected, hasFocus);
	}
	
	/**
	 * Set properties.
	 * @param id 
	 * @param image 
	 * @param hasFocus 
	 * @param isSelected 
	 * @param index 
	 */
	public void setProperties(String text, BufferedImage image,
			int index, boolean isSelected, boolean hasFocus) {
		
		setText(text);

		this.isSelected = isSelected;
		this.hasFocus = hasFocus;
		
		if (image != null) {
			setIcon(new ImageIcon(image));
		}
		else {
			setIcon(null);
		}
		
		// Get background color.
		Color backGroundColor = Utility.itemColor(index);
		// Set color.
		setBackground(backGroundColor);
	}
}