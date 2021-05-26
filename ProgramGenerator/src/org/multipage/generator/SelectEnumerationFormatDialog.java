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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.multipage.gui.Images;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.maclan.MiddleUtility;

/**
 * 
 * @author
 *
 */
public class SelectEnumerationFormatDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Enumeration text formats file name.
	 */
	private static final String xmlFileName = "enum_formats.xml";

	/**
	 * XML root node name.
	 */
	private static final String xmlRootName = "Formats";

	/**
	 * Format node name.
	 */
	private static final String xmlFormatNode = "Format";

	/**
	 * Output format attribute name.
	 */
	private static final String xmlOutputFromatAttribute = "Output";

	/**
	 * Input format attribute name.
	 */
	private static final String xmlInputFromatAttribute = "Input";

	/**
	 * Validation file name.
	 */
	private static final String xmlValidationFile = "/org/multipage/generator/properties/enum_formats.xsd";
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();
	
	/**
	 * Selected format.
	 */
	public static EnumerationTextFormat selectedFormat;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		selectedFormat = new EnumerationTextFormat("%s: %s;", "^([^:]+):([^;]+);?$", true);
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(selectedFormat);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		selectedFormat = Utility.readInputStreamObject(inputStream, EnumerationTextFormat.class);
	}

	/**
	 * Default table model.
	 */
	private DefaultTableModel tableModel;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelFormatsList;
	private JToolBar toolBar;
	private JScrollPane scrollPaneFormats;
	private JTable tableFormats;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static void showDialog(Component parent) {
		
		SelectEnumerationFormatDialog dialog = new SelectEnumerationFormatDialog(parent);
		dialog.setVisible(true);
		
		return;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public SelectEnumerationFormatDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textEnumerationFormatDialog");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 477, 294);
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
		
		labelFormatsList = new JLabel("org.multipage.generator.textEnumerationFormatList");
		springLayout.putConstraint(SpringLayout.NORTH, labelFormatsList, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelFormatsList, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelFormatsList);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.EAST, toolBar, -10, SpringLayout.EAST, getContentPane());
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, -6, SpringLayout.NORTH, buttonOk);
		getContentPane().add(toolBar);
		
		scrollPaneFormats = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPaneFormats, 3, SpringLayout.SOUTH, labelFormatsList);
		springLayout.putConstraint(SpringLayout.WEST, scrollPaneFormats, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPaneFormats, -3, SpringLayout.NORTH, toolBar);
		springLayout.putConstraint(SpringLayout.EAST, scrollPaneFormats, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPaneFormats);
		
		tableFormats = new JTable();
		tableFormats.setRowHeight(20);
		tableFormats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneFormats.setViewportView(tableFormats);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		
		createAndLoadTable();
		createToolBar();
		
		loadDialog();
	}

	/**
	 * Create tool bar.
	 */
	private void createToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/insert.png", this, "onAddNewFormat", "org.multipage.generator.tooltipAddNewEnumerationTextFormat");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/generator/images/remove_icon.png", this, "onRemoveFormat", "org.multipage.generator.tooltipRemoveEnumerationTextFormat");
	}
	
	/**
	 * Remove format.
	 */
	@SuppressWarnings("unused")
	private void onRemoveFormat() {
		
		// Get selected index.
		int selectedIndex = tableFormats.getSelectedRow();
		if (selectedIndex == -1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectEnumerationFormat");
			return;
		}
		
		// Get text format.
		Object object = tableModel.getValueAt(selectedIndex, 0);
		if (!(object instanceof EnumerationTextFormat)) {
			
			Utility.show(this, "org.multipage.generator.messageBadFormatClassType");
			return;
		}
		
		EnumerationTextFormat format = (EnumerationTextFormat)object;
		if (format.isDefault) {
			
			Utility.show(this, "org.multipage.generator.messageCannotRemoveDefaultEnumerationTextFormat");
			return;
		}
		
		// Ask user.
		if (!Utility.ask(this, "org.multipage.generator.messageRemoveSelectedEnumerationTextFormat")) {
			return;
		}
		
		// Remove it.
		tableModel.removeRow(selectedIndex);
	}
	
	/**
	 * Add new format.
	 */
	@SuppressWarnings("unused")
	private void onAddNewFormat() {
		
		EnumerationTextFormat format = new EnumerationTextFormat("", "", false);
		tableModel.addRow(new Object [] {format, format.input});
	}

	/**
	 * Create and load table.
	 */
	@SuppressWarnings("serial")
	private void createAndLoadTable() {
		
		final EnumerationTextFormat [] defaultFormats = {new EnumerationTextFormat("%s: %s;", "^([^:]+):([^;]+);?$", true)};
		
		// Save cell value on table focus lost.
		tableFormats.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		// Create table model.
		tableModel = new DefaultTableModel(new String [] {
				Resources.getString("org.multipage.generator.textEnumerationFormatOutput"),
				Resources.getString("org.multipage.generator.textEnumerationFormatInput")
		}, 0) {

			// Disable editing of default formats.
			@Override
			public boolean isCellEditable(int row, int column) {
				
				// Get value.
				Object value = tableModel.getValueAt(row, 0);
				if (value instanceof EnumerationTextFormat) {
					
					EnumerationTextFormat format = (EnumerationTextFormat) value;
					
					if (format.isDefault) {
						return false;
					}
				}
				
				return super.isCellEditable(row, column);
			}

			// Set value.
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				
				Object editedObject = tableModel.getValueAt(row, 0);
				if (editedObject instanceof EnumerationTextFormat) {
					
					EnumerationTextFormat format = (EnumerationTextFormat) editedObject;
					
					if (column == 0) {
						format.output = aValue.toString();
					}
					else if (column == 1) {
						format.input = aValue.toString();
						super.setValueAt(aValue, row, column);
					}
				}
			}
			
		};
		tableFormats.setModel(tableModel);
		
		// Set cell renderer.
		tableFormats.setDefaultRenderer(Object.class, new TableCellRenderer() {
			
			// Get default cell renderer.
			DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) tableFormats.getDefaultRenderer(Object.class);
			Font plainFont;
			Font boldFont;
			Color selectionColor;
			
			// Constructor.
			{
				plainFont = renderer.getFont().deriveFont(Font.PLAIN);
				boldFont = renderer.getFont().deriveFont(Font.BOLD);
				
				selectionColor = SystemColor.textHighlight;
			}
			
			// Return changed default renderer.
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				
				renderer.setFont( plainFont);
				renderer.setForeground(isSelected ? Color.WHITE : Color.BLACK);
				renderer.setBackground(isSelected ? selectionColor : Color.WHITE);
				
				// Emphasize default format.
				Object objectValue = tableModel.getValueAt(row, 0);
				if (objectValue instanceof EnumerationTextFormat) {
					EnumerationTextFormat format = (EnumerationTextFormat) objectValue;

					if (format.isDefault) {
						renderer.setFont(boldFont);
					}
				}
				
				renderer.setText(value.toString());
				
				return renderer;
			}
		});
		
		// Load default formats.
		for (EnumerationTextFormat format : defaultFormats) {
			tableModel.addRow(new Object [] {format, format.input});
		}
		
		// Try to load data from an XML file.
		loadFormatsFromXml();
		
		// Select item.
		int indexToSelect = 0;
		for (int index = 0; index < tableModel.getRowCount(); index++) {
			
			Object object = tableModel.getValueAt(index, 0);
			if (!(object instanceof EnumerationTextFormat)) {
				continue;
			}
			
			EnumerationTextFormat existingFormat = (EnumerationTextFormat) object;
			
			if (existingFormat.equals(selectedFormat)) {
				indexToSelect = index;
			}
		}
		
		tableFormats.getSelectionModel().setSelectionInterval(indexToSelect, indexToSelect);
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
		Utility.localize(labelFormatsList);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		// Save formats to XML file.
		saveFormatsToXml();
		
		saveDialog();
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Save formats to XML file.
		saveFormatsToXml();
		
		// Remember selected format.
		int selectedIndex = tableFormats.getSelectedRow();
		if (selectedIndex == -1) {
			
			Utility.show(this, "org.multipage.generator.messageSelectEnumerationFormat");
			return;
		}
		
		Object object = tableFormats.getValueAt(selectedIndex, 0);
		if (!(object instanceof EnumerationTextFormat)) {
			
			Utility.show(this, "org.multipage.generator.messageBadFormatClassType");
			return;
		}
		
		selectedFormat = (EnumerationTextFormat) object;
		
		saveDialog();
		dispose();
	}

	/**
	 * Save formats to XML file.
	 */
	private void saveFormatsToXml() {
		
	    try {
	    	boolean dataExists = false;
	    	
	    	String userDirectory = MiddleUtility.getUserDirectory();
	    	String fullFileName = null;
	    	
	    	if (!userDirectory.isEmpty()) {
	    		fullFileName = userDirectory + File.separatorChar + xmlFileName;
	    	}
	    	else {
	    		fullFileName = xmlFileName;
	    	}
	    	
			File file = new File(fullFileName);
			
	    	// If exist XML file, delete it.
			if (file.exists()) {
				file.delete();
			}
	    	
	    	// Try to create DOM document.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			
			// Insert root.
			Element root = document.createElement(xmlRootName);
			document.appendChild(root);
			
			// Do loop for all not default formats.
			int count = tableModel.getRowCount();
			for (int index = 0; index < count; index++) {
				
				Object object = tableModel.getValueAt(index, 0);
				if (!(object instanceof EnumerationTextFormat)) {
					continue;
				}
				
				EnumerationTextFormat format = (EnumerationTextFormat) object;
				
				if (!format.isDefault && !format.isEmpty()) {
					dataExists = true;
					
					// Add table name to document root element.
					Element tableElement = document.createElement(xmlFormatNode);
					
					tableElement.setAttribute(xmlOutputFromatAttribute, format.output);
					root.appendChild(tableElement);
					
					tableElement.setAttribute(xmlInputFromatAttribute, format.input);
					root.appendChild(tableElement);
				}
			}
			// If data exists, save the XML file.
			if (dataExists) {
				// Try to save XML document to file.
				Source source = new DOMSource(document);
				Result result = new StreamResult(file);
				Transformer xformer = TransformerFactory.newInstance().newTransformer();
		        xformer.transform(source, result);
			}
	    }
	    catch (Exception e) {
	    	
	    	Utility.show(this, e.getLocalizedMessage());
	    }
	}

	/**
	 * Load formats from XML.
	 */
	private void loadFormatsFromXml() {
		
    	Exception exception = null;
    	
		try {
			
			String userDirectory = MiddleUtility.getUserDirectory();
			String fullFileName = null;
			
			if (!userDirectory.isEmpty()) {
				fullFileName = userDirectory + File.separatorChar + xmlFileName;
			}
			else {
				fullFileName = xmlFileName;
			}
			
			File file = new File(fullFileName);
			if (file.exists()) {
				
				// Test if the program can read the file.
				if (!file.canRead()) {
					JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotReadEnumerationFormatsFile"));
					return;
				}
				// Try to get parser and parse file.
		    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db;
				db = dbf.newDocumentBuilder();
				// Error handler.
				db.setErrorHandler(new ErrorHandler() {
					@Override
					public void warning(SAXParseException exception) throws SAXException {
						JOptionPane.showMessageDialog(null, exception.getMessage());
					}
					@Override
					public void fatalError(SAXParseException exception) throws SAXException {						
						JOptionPane.showMessageDialog(null, exception.getMessage());
					}
					@Override
					public void error(SAXParseException exception) throws SAXException {						
						JOptionPane.showMessageDialog(null, exception.getMessage());
					}
				});
		        Document document = db.parse(file);
		        
		        // Validate XML file.
		        InputStream schemaInputStream = getClass().getResourceAsStream(xmlValidationFile);
		        if (schemaInputStream == null) {
		        	// Inform user and exit.
		        	JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotLocateEnumerationFormatValiationFile"));
		        	return;
		        }
		        
		        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		        Schema schema = factory.newSchema(new StreamSource(schemaInputStream));
		        Validator validator = schema.newValidator();
		        try {
		        	validator.validate(new DOMSource(document));
		        }
		        catch (SAXException e) {
		        	// Set message.
		        	String message = Resources.getString("org.multipage.generator.messageEnumerationFormatsValidationException")
		        						+ "\n" + e.getMessage();
		        	JOptionPane.showMessageDialog(this, message);
		        	return;
		        }
		        
		        schemaInputStream.close();
		        
		        // Get root "Formats" node.
		        Node root = document.getFirstChild();
		        if (root != null) {
		        	
		        	// Get child nodes.
		        	Node formatNode = root.getFirstChild();
		        	while (formatNode != null) {
		        		
		        		// Get "output" and "input" node attributes.
		        		NamedNodeMap attributes = formatNode.getAttributes();
		        		
		        		String outputFormat = attributes.getNamedItem(xmlOutputFromatAttribute).getNodeValue();
		        		String inputFormat = attributes.getNamedItem(xmlInputFromatAttribute).getNodeValue();
		        		
		        		// Insert new table item.
		        		tableModel.addRow(new Object [] {
		        				new EnumerationTextFormat(outputFormat, inputFormat, false),
		        				inputFormat});
		        		
		        		// Get next sibling node.
		        		formatNode = formatNode.getNextSibling();
		        	}
		        }
			}
			
		} catch (Exception e) {
			exception = e;
		}
		finally {
			if (exception != null) {
				JOptionPane.showMessageDialog(this, exception.getMessage());
			}
		}
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
