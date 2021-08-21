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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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

import org.maclan.MiddleUtility;
import org.multipage.gui.Images;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * @author
 *
 */
class TableId {
	
	static final TableId DEFAULT = new TableId(false, "org.multipage.generator.textDefaultColorTable");
	static final TableId RED_BLUE = new TableId(false, "org.multipage.generator.textWhiteBlueRedTable");
	static final TableId GREEN = new TableId(false, "org.multipage.generator.textGreenTable");
	
	/**
	 * Is user defined flag.
	 */
	private boolean isUser;
	
	/**
	 * Table name string.
	 */
	private String nameString;

	/**
	 * Constructor.
	 */
	public TableId(boolean isUser, String nameString) {

		this.isUser = isUser;
		this.nameString = nameString;
	}

	/**
	 * To string.
	 */
	@Override
	public String toString() {
		
		if (isUser) {
			return nameString;
		}
		else {
			return Resources.getString(nameString);
		}
	}

	/**
	 * @return the isUser
	 */
	public boolean isUser() {
		return isUser;
	}
}

/**
 * @author
 *
 */
public class CustomizedColors extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Color column width.
	 */
	private static final int colorColumnWidth = 100;

	/**
	 * Colors file name.
	 */
	private static final String xmlFileName = "colors.xml";

	/**
	 * XML root node name.
	 */
	private static final String xmlRootName = "ColorTemplates";

	/**
	 * XML color node name.
	 */
	private static final String xmlColorNode = "Color";

	/**
	 * XML color table node.
	 */
	private static final String xmlTableNode = "Template";

	/**
	 * XML identifier attribute name.
	 */
	private static final String xmlIdAttribute = "id";

	/**
	 * XML value attribute name.
	 */
	private static final String xmlValueAttribute = "value";

	/**
	 * Validation file.
	 */
	private static final String xmlValidationFile = "/org/multipage/generator/properties/colors.xsd";

	/**
	 * Color tables.
	 */
	static Hashtable<TableId, Hashtable<ColorId, Obj<Color>>> colorTables = new Hashtable<TableId, Hashtable<ColorId, Obj<Color>>>();
	
	/**
	 * Current color table.
	 */
	static Hashtable<ColorId, Obj<Color>> currentColorTable = new Hashtable<ColorId, Obj<Color>>();
	
	/**
	 * Current color table id
	 */
	static TableId currentColorTableId = TableId.DEFAULT;

	/**
	 * Template state.
	 */
	private static String templateState;

	/**
	 * Static constructor.
	 */
	static {

		loadDefaultTables();
		
		loadCurrentTable();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(ObjectInputStream inputStream)
		throws IOException, ClassNotFoundException {

		templateState = inputStream.readUTF();
	}
	
	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		outputStream.writeUTF(templateState);
	}
	
	/**
	 * Set default.
	 */
	public static void setDefaultData() {

		templateState = "";
	}
	
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		// Try to select template by name.
		setCurrentTableByName(templateState);
		// Load current table.
		loadCurrentTable();
	}
		
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		// Get selected template name.
		templateState = currentColorTableId.toString();
		// Save data to XML file.
		saveDataToXml();
	}

	/**
	 * Load current table.
	 */
	private static void loadCurrentTable() {

		Hashtable<ColorId, Obj<Color>> sourceTable = colorTables.get(currentColorTableId);
		
		// Copy source table to current table.
		currentColorTable.clear();
		for (ColorId colorId : sourceTable.keySet()) {
			// Get color.
			Obj<Color> color = sourceTable.get(colorId);
			// Create new color.
			Obj<Color> newColor = new Obj<Color>();
			newColor.ref = new Color(color.ref.getRGB());
			// Add to current table.
			currentColorTable.put(colorId, newColor);
		}
	}

	/**
	 * Add color.
	 */
	private static void addColor(Hashtable<ColorId, Obj<Color>> table, ColorId colorId, Color color) {
		
		Obj<Color> colorRef = new Obj<Color>();
		colorRef.ref = color;
		
		table.put(colorId, colorRef);
	}
	
	/**
	 * Load default tables.
	 */
	private static void loadDefaultTables() {
		
		// Create tables.
		Hashtable<ColorId, Obj<Color>> default_table = new Hashtable<ColorId, Obj<Color>>();
		colorTables.put(TableId.DEFAULT, default_table);
		// Set colors.
		addColor(default_table, ColorId.INACTIVE_OUTLINES, new Color(0xff808080));
		addColor(default_table, ColorId.TEXT_PROTECTED, new Color(0xff999999));
		addColor(default_table, ColorId.SELECTION_PROTECTED, new Color(0xff009999));
		addColor(default_table, ColorId.SELECTED_TEXT, new Color(0xffffffff));
		addColor(default_table, ColorId.BACKGROUNDTEXT, new Color(0xff000000));
		addColor(default_table, ColorId.TOOLBACKGROUND, new Color(0xffffffff));
		addColor(default_table, ColorId.REVERSEDEDGES, new Color(0xff006699));
		addColor(default_table, ColorId.OVERVIEWBACKGROUND, new Color(0xff646464));
		addColor(default_table, ColorId.FREE, new Color(0xff666666));
		addColor(default_table, ColorId.OUTLINES, new Color(0xff000000));
		addColor(default_table, ColorId.FILLLABEL, new Color(0xff408c8c));
		addColor(default_table, ColorId.SELECTION, new Color(0xff009999));
		addColor(default_table, ColorId.INACTIVE_BODIES, new Color(0xffffffff));
		addColor(default_table, ColorId.OUTLINES_PROTECTED, new Color(0xff000000));
		addColor(default_table, ColorId.FILLBODY, new Color(0xff999999));
		addColor(default_table, ColorId.BACKGROUND, new Color(0xff000000));
		addColor(default_table, ColorId.SCROLLBARS, new Color(0xff007575));
		addColor(default_table, ColorId.FILLLABEL_PROTECTED, new Color(0xff666666));
		addColor(default_table, ColorId.TOOLLISTBACKGROUND, new Color(0xffc0c0c0));
		addColor(default_table, ColorId.SCRIPT_COMMAND_HIGHLIGHT, new Color(0xff999999));
		addColor(default_table, ColorId.DESCRIPTIONTEXT, new Color(0xff808080));
		addColor(default_table, ColorId.TEXT, new Color(0xff000000));
	}
    
    /**
     * Get color.
     */
    public static Color get(ColorId colorId) {
    	
    	Obj<Color> color = currentColorTable.get(colorId);
    	if (color == null) {
    		return Color.BLACK;
    	}
    	if (color.ref == null) {
    		return Color.BLACK;
    	}
    	return color.ref;
    }

    /**
     * Table model.
     */
    private ColorTableModel tableModel = new ColorTableModel();
    
    /**
     * Table renderer.
     */
    private ColorTableRenderer tableRenderer = new ColorTableRenderer();
    
    /**
     * Table cell editor.
     */
    private ColorCellEditor tableEditor;
    
	/**
     * Dialog components.
     */
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton cloneButton;
    private javax.swing.JTable table;
    private javax.swing.JComboBox templatesCombo;
    
    /**
     * Dirty flag.
     */
    private boolean dirty = false;
	
    /**
     * Initialize components.
     */
    private void initComponents() {

        javax.swing.JLabel labelColorTable = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        templatesCombo = new javax.swing.JComboBox();
        cloneButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setResizable(false);

        labelColorTable.setText(Resources.getString("org.multipage.generator.textColorTable"));
        table.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(table);

        jLabel1.setText(Resources.getString("org.multipage.generator.textColorScheme"));

        cloneButton.setText(Resources.getString("org.multipage.generator.textCloneColors"));

        removeButton.setText(Resources.getString("org.multipage.generator.textRemoveColors"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                    .addComponent(labelColorTable)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(templatesCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cloneButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(templatesCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cloneButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(labelColorTable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }
    
    /**
     * Constructor.
     */
    public CustomizedColors(java.awt.Frame parent) {
    	
        super(parent, false);
        initComponents();
        
        // Load user color tables.
        loadDataFromXml();
        
        // Create table editor.
        tableEditor = new ColorCellEditor(this);
        
        // Set description and icon.
        setTitle(Resources.getString("org.multipage.generator.textCustomizeColorsDialog"));
        setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
        
        // Center dialog.
        Dimension dimension = getSize();
        Dimension screen = getToolkit().getScreenSize();
        setLocation((screen.width - dimension.width) / 2, (screen.height - dimension.height) / 2);
         
        tableModel.loadColorTable();

        // Set table model, renderer and cell editor.
        table.setModel(tableModel);
        table.setDefaultRenderer(Color.class, tableRenderer);
        table.setDefaultEditor(Color.class, tableEditor);
        // Set color column width.
        TableColumn column = table.getColumnModel().getColumn(1);
        column.setMaxWidth(colorColumnWidth);
      
        // Set combo box listener.
        templatesCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// If a content is dirty, save it.
				saveIfDirty(false);
				// Update selection.
				currentColorTableId = (TableId) templatesCombo.getSelectedItem();
				loadCurrentTable();
				tableModel.loadColorTable();
				GeneratorMainFrame.getFrame().repaint();
			}
		});
        // Set button listeners.
        cloneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Save color data.
				saveIfDirty(true);
			}
		});
        removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Remove current template.
				removeCurrent();
			}
		});
        // Dialog listeners.
        addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Save data.
				saveIfDirty(false);
			}
			@Override
			public void windowOpened(WindowEvent e) {
		        // Update combo box.
				updateComboBox();
			}
		});
        // Load dialog.
        loadDialog();
    }

    /**
     * Set current color table by name.
     * @param template
     */
    protected void setCurrentTableByName(String tableName) {

    	// Get table identifier.
    	for (TableId tableId : colorTables.keySet()) {
    		
    		if (tableId.toString().compareTo(tableName) == 0) {
    			currentColorTableId = tableId;
    			break;
    		}
    	}
	}

	/**
     * Load user color tables from XML.
     */
    private void loadDataFromXml() {

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
				
				// Test if program can read file.
				if (!file.canRead()) {
					JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotReadColorTablesFile"));
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
		        	JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotLocateColorValiationFile"));
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
		        	String message = Resources.getString("org.multipage.generator.messageColorValidationException")
		        						+ "\n" + e.getMessage();
		        	JOptionPane.showMessageDialog(this, message);
		        	return;
		        }
		        
		        schemaInputStream.close();
		        
		        Node root = document.getFirstChild();
		        Node tableNode = root.getFirstChild();
		        while (tableNode != null) {
		        	String tableName = tableNode.getAttributes().getNamedItem(xmlIdAttribute).getNodeValue();
		        	// Create new user color table and add it to list.
		        	Hashtable<ColorId, Obj<Color>> hashTable = new Hashtable<ColorId, Obj<Color>>();
		        	TableId tableId = new TableId(true, tableName);
		        	colorTables.put(tableId, hashTable);
		        	// Set colors.
		        	Node colorNode = tableNode.getFirstChild();
		        	while (colorNode != null) {
		        		String colorIdStr = colorNode.getAttributes().getNamedItem(xmlIdAttribute).getNodeValue();
		        		String colorValueStr = colorNode.getAttributes().getNamedItem(xmlValueAttribute).getNodeValue();
		        		ColorId colorId = ColorId.getColorId(colorIdStr);
		        		// If color identifier is null, skip it.
		        		if (colorId == null) {
		        			colorNode = colorNode.getNextSibling();
		        			continue;
		        		}
		        		Obj<Color> color = new Obj<Color>();
		        		int rgb = Long.valueOf(colorValueStr.toLowerCase(), 16).intValue();
		        		color.ref = new Color(rgb);
		        		hashTable.put(colorId, color);
		        		
		        		colorNode = colorNode.getNextSibling();
		        	}
		        	tableNode = tableNode.getNextSibling();
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
	 * Save data to XML file.
	 */
	private void saveDataToXml() {
	
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
			
			// Do loop for all color table keys.
			for (TableId tableId : colorTables.keySet()) {
				if (tableId.isUser()) {
					
					dataExists = true;
					
					String tableName = tableId.toString();
					// Add table name to document root element.
					Element tableElement = document.createElement(xmlTableNode);
					tableElement.setAttribute(xmlIdAttribute, tableName);
					root.appendChild(tableElement);
					// Get color table and save it.
					Hashtable<ColorId, Obj<Color>> colorTable = colorTables.get(tableId);
					for (ColorId colorId : colorTable.keySet()) {
						String colorStrId = colorId.getColorId();
						Obj<Color> color = colorTable.get(colorId);
						String colorStr = null;
						colorStr = String.format("%x", color.ref.getRGB());
						// Create color element.
						Element colorElement = document.createElement(xmlColorNode);
						tableElement.appendChild(colorElement);
						colorElement.setAttribute(xmlIdAttribute, colorStrId);
						colorElement.setAttribute(xmlValueAttribute, colorStr);
					}
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
	    	e.printStackTrace();
	    }
	}

	/**
     * Remove current color table (template).
     */
    protected void removeCurrent() {

    	// Inform user.
    	String messageFormat = Resources.getString("org.multipage.generator.messageCannotRemoveDefaultColorTable");
    	String message = String.format(messageFormat, currentColorTableId.toString());
    	if (!currentColorTableId.isUser()) {
    		JOptionPane.showMessageDialog(this, message);
    		return;
    	}
    	messageFormat = Resources.getString("org.multipage.generator.messageRemoveColorTemplate");
    	message = String.format(messageFormat, currentColorTableId.toString());
    	int answer = JOptionPane.showConfirmDialog(this, message);
    	if (answer != JOptionPane.YES_OPTION) {
    		return;
    	}
    	
    	// Remove current color table.
    	colorTables.remove(currentColorTableId);
    	currentColorTableId = TableId.RED_BLUE;
    	updateComboBox();
    	
		// Save data to XML file.
		saveDataToXml();
	}

	/**
     * Save color table if a content is dirty.
     */
    public void saveIfDirty(boolean saveAs) {

    	if (dirty || saveAs) {
    		   		    		
    		// If current color table is not user, ask user.
    		if (!currentColorTableId.isUser() || saveAs) {
    			
    			if (!saveAs) {
	    			int answer = JOptionPane.showConfirmDialog(this, Resources.getString("org.multipage.generator.messageSaveColorTableChanges"));
	    			if (answer != JOptionPane.YES_OPTION) {
	    				dirty = false;
	    				return;
	    			}
    			}
    			// Get template name.
    			String templateName = JOptionPane.showInputDialog(this, Resources.getString("org.multipage.generator.messageInsertTempateName"));
    			if (templateName == null) {
    				return;
    			}
    			if (templateName.isEmpty()) {
    				JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageTemplateNameCannotBeEmpty"));
    				return;
    			}
    			if (existTemplateName(templateName)) {
    				JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageTemplateNameExist"));
    				return;
    			}
    			// Create new table.
    			Hashtable<ColorId, Obj<Color>> table = new Hashtable<ColorId, Obj<Color>>();
    			// Save data.
    			saveDataToTable(table);
    			// Add table to templates.
    			currentColorTableId = new TableId(true, templateName);
    			colorTables.put(currentColorTableId, table);
     			// Save data to XML file.
    			saveDataToXml();
    			
    			updateComboBox();
    		}
    		// If current control table is a user table.
    		else {
    			// Save color table.
    			Hashtable<ColorId, Obj<Color>> table = colorTables.get(currentColorTableId);
    			table.clear();
    			// Save data.
    			saveDataToTable(table);
    			// Save data to XML file.
    			saveDataToXml();
    		}
    		dirty = false;
    	}
	}

    /**
     * Save data to table.
     * @param table2
     */
    private void saveDataToTable(Hashtable<ColorId, Obj<Color>> table) {

    	// Do loop for all table entries.
		for (ColorTableEntry row : tableModel.getColorTable()) {
			ColorId colorId = row.getId();
			Obj<Color> color = row.getColor();
			// Add table item.
			table.put(colorId, color);
		}
	}

	/**
     * Return true if a tempate name already exist.
     * @param templateName 
     * @return
     */
    private boolean existTemplateName(String templateName) {

    	// Do loop for all table identifiers.
    	for (TableId key : colorTables.keySet()) {
    		if (key.toString().compareTo(templateName) == 0) {
    			return true;
    		}
    	}
    	
		return false;
	}

	/**
     * Update combo box.
     */
	private void updateComboBox() {

		LinkedList<TableId> keys = new LinkedList<TableId>();
		for (TableId tableId : colorTables.keySet()) {
			keys.add(tableId);
		}
		// Sort table names.
		Collections.sort(keys, new Comparator<TableId> () {
			@Override
			public int compare(TableId o1, TableId o2) {
				// Compare table names.
				return o1.toString().compareTo(o2.toString());
			}
		});
		// Set model and select item.
        templatesCombo.setModel(new DefaultComboBoxModel(keys.toArray()));
        templatesCombo.setSelectedItem(currentColorTableId);
	}

	/**
     * Set dirty flag.
     * @param dirty
     */
	public void setDirty(boolean dirty) {

		this.dirty = dirty;
	}

	/**
	 * Dispose dialog.
	 */
	public void disposeDialog() {

		saveDialog();
	}
}

/**
 * Color table entry class.
 */
class ColorTableEntry {
	
	/**
	 * Color id.
	 */
	public ColorId id;
	
	/**
	 * Color.
	 */
	public Obj<Color> color;

	/**
	 * Constructor.
	 */
	public ColorTableEntry(ColorId id, Obj<Color> color) {

		this.id = id;
		this.color = color;
	}

	/**
	 * @return the id
	 */
	public ColorId getId() {
		return id;
	}

	/**
	 * @return the color
	 */
	public Obj<Color> getColor() {
		return color;
	}
}

/**
 * 
 * @author
 *
 */
class ColorTableModel extends AbstractTableModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Color table.
	 */
	private ArrayList<ColorTableEntry> colorTable = new ArrayList<ColorTableEntry>();

	/**
	 * Load current color table.
	 */
	public void loadColorTable() {
		
		// Load color table.
		colorTable.clear();
		for (ColorId colorId : CustomizedColors.currentColorTable.keySet()) {
			Obj<Color> color = CustomizedColors.currentColorTable.get(colorId);
			colorTable.add(new ColorTableEntry(colorId, color));
		}
		
		// Sort color table.
		Collections.sort(colorTable, new Comparator<ColorTableEntry>() {
			@Override
			public int compare(ColorTableEntry o1, ColorTableEntry o2) {
				return o1.id.getColorText().compareTo(o2.id.getColorText());
			}
		});
		
		// Fire table changed event.
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * On set value.
	 */
	@Override
	public void setValueAt(Object aValue, int row, int column) {

		ColorTableEntry colorDef = colorTable.get(row);
		colorDef.color.ref = (Color) aValue;
		
		GeneratorMainFrame.getFrame().repaint();
		
        fireTableCellUpdated(row, column);
	}

	/**
	 * On get column count.
	 */
	@Override
	public int getColumnCount() {

		return 2;
	}

	/**
	 * On get row count.
	 */
	@Override
	public int getRowCount() {

		return colorTable.size();
	}

	/**
	 * On get value at given position.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		if (columnIndex == 0) {
			return colorTable.get(rowIndex).id.getColorText();
		}
		else {
			return colorTable.get(rowIndex).color;
		}
	}

	/**
	 * On get column class.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		
		if (columnIndex == 1) {
			return Color.class;
		}
		return super.getColumnClass(columnIndex);
	}

	/**
	 * On get column name.
	 */
	@Override
	public String getColumnName(int column) {

		if (column == 0) {
			return Resources.getString("org.multipage.generator.textColorName");
		}
		else {
			return Resources.getString("org.multipage.generator.textColor");
		}
	}

	/**
	 * On is cell editable.
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {

		return columnIndex == 1;
	}

	/**
	 * Get color table.
	 * @return the colorTable
	 */
	public ArrayList<ColorTableEntry> getColorTable() {
		return colorTable;
	}
}

/**
 * 
 * @author
 *
 */
class ColorCellEditor extends AbstractCellEditor implements TableCellEditor {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Color cell button.
	 */
	private JButton editButton = new JButton();
	
	/**
	 * Color dialog.
	 */
	private JDialog colorDialog;
	
	/**
	 * Color chooser.
	 */
	private JColorChooser colorChooser = new JColorChooser();
	
	/**
	 * Current color.
	 */
	private Color currentColor;

	/**
	 * Customized colors dialog.
	 */
	private CustomizedColors dialog;
	
	/**
	 * Constructor.
	 */
	ColorCellEditor(CustomizedColors dialog) {
		
		this.dialog = dialog;
		
		// Create color dialog.
		colorDialog = JColorChooser.createDialog(
				editButton,
				Resources.getString("org.multipage.gui.textColorChooserDialog"),
				true,
				colorChooser,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Set current color.
						currentColor = colorChooser.getColor();
					}
				},
				null);
	
		// Set button listener.
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onButton();
			}
		});
	}

	/**
	 * On button pressed.
	 */
	protected void onButton() {
		
		Color oldColor = currentColor;
		
		// Show color chooser.
        editButton.setBackground(currentColor);
        colorChooser.setColor(currentColor);
		colorDialog.setVisible(true);
        //Make the renderer reappear.
        fireEditingStopped();
        
        // If a color changes, set dirty flag.
        dialog.setDirty(!oldColor.equals(currentColor));
	}

	/**
	 * Get editor color.
	 */
	@Override
	public Object getCellEditorValue() {

		return currentColor;
	}

	/**
	 * On get editor component (button).
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		currentColor = ((Obj<Color>) value).ref;
		editButton.setBackground(currentColor);
		return editButton;
	}
}

/**
 * 
 * @author
 *
 */
class ColorTableRenderer implements TableCellRenderer {
	
	/**
	 * Label component.
	 */
	private JLabel label = new JLabel();
	
	/**
	 * Borders
	 */
    private Border unselectedBorder = null;
    private Border selectedBorder = null;

	/**
	 * Constructor.
	 */
	ColorTableRenderer() {
		
		label.setOpaque(true);
	}

	/**
	 * Callback.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

        if (isSelected) {
            if (selectedBorder == null) {
                selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                          table.getSelectionBackground());
            }
            label.setBorder(selectedBorder);
        } else {
            if (unselectedBorder == null) {
                unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                          table.getBackground());
            }
            label.setBorder(unselectedBorder);
        }

		label.setBackground(((Obj<Color>) value).ref);
		return label;
	}
}
