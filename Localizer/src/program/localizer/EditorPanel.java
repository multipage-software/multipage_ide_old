package program.localizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.multipage.gui.SearchTextDialog;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
class PropertyValue {
	
	/**
	 * Properties.
	 */
	String value;
	int index;
	
	/**
	 * Constructor.
	 */
	PropertyValue(String value, int index) {
		
		this.value = value;
		this.index = index;
	}
}

/**
 * 
 * @author
 *
 */
class Properties {

	/**
	 * Properties.
	 */
	java.util.Properties properties = new java.util.Properties();
	
	/**
	 * Get key set.
	 * @return
	 */
	public Iterable<Object> keySet() {
		
		return properties.keySet();
	}

	/**
	 * Get value.
	 * @param keyName
	 * @return
	 */
	public String getValue(String keyName) {
		
		PropertyValue propertyValue = (PropertyValue) properties.get(keyName);
		if (propertyValue == null) {
			return "";
		}
		
		return propertyValue.value;
	}

	/**
	 * Get index.
	 * @param keyName
	 * @return
	 */
	public int getIndex(String keyName) {
		
		PropertyValue propertyValue = (PropertyValue) properties.get(keyName);
		if (propertyValue == null) {
			return -1;
		}
		
		return propertyValue.index;
	}
	
	/**
	 * Put value.
	 * @param key
	 * @param value
	 */
	public void put(String key, String value, int index) {
		
		properties.put(key, new PropertyValue(value, index));
	}

	/**
	 * Set value.
	 * @param keyName
	 * @param value
	 */
	public void setValue(String keyName, String value) {
		
		PropertyValue propertyValue = (PropertyValue) properties.get(keyName);
		if (propertyValue != null) {
			propertyValue.value = value;
		}
	}

	/**
	 * Remove property.
	 * @param keyName
	 */
	public void remove(String keyName) {
		
		properties.remove(keyName);
	}
}

/**
 * 
 * @author
 *
 */
class PropertiesBundle {
	
	/**
	 * Properties.
	 */
	String filePath;
	Properties properties;
	String name;
	
	/**
	 * Constructor.
	 * @param filePath
	 * @param properties
	 */
	public PropertiesBundle(String filePath, Properties properties) {
		
		this.filePath = filePath;
		this.properties = properties;
		
		// Infer bundle name.
		name = Resources.getString("localizer.textUnknown");
		int position = filePath.lastIndexOf(File.separatorChar);
		
		int length = filePath.length();
		
		if (position != -1 && position < length) {
			String fileName = filePath.substring(position + 1);
			
			int positionAux = fileName.lastIndexOf(EditorPanel.fileNameStart);
			if (positionAux != -1) {
				
				positionAux += EditorPanel.fileNameStart.length();
				if (positionAux <= length) {
					
					int positionDot = fileName.indexOf('.', positionAux);
					if (positionDot == -1) {
						positionDot = length;
					}
					
					// Set bundle name.
					name = fileName.substring(positionAux, positionDot);
				}
			}
		}
	}

	/**
	 * Save file.
	 */
	@SuppressWarnings("unchecked")
	public void saveFile() {

		// Convert properties to list.
		Set<Entry<Object, Object>> entries = properties.properties.entrySet();
		
		// Item class.
		@SuppressWarnings("rawtypes")
		class Item implements Comparable {
			
			// Properties.
			String key;
			String value;
			Integer index;
			
			// Constructor.
			Item(String key, String value, int index) {
				this.key = key;
				this.value = value;
				this.index = index;
			}

			// Comparation.
			@Override
			public int compareTo(Object object) {
				
				Item item = (Item) object;
				return index.compareTo(item.index);
			}
			
		}
		
		// Create items list.
		LinkedList<Item> sortedList = new LinkedList<Item>();
		for (Entry<Object, Object> entry : entries) {
			
			String key = (String) entry.getKey();
			PropertyValue propertyValue = (PropertyValue) entry.getValue();
			String value = propertyValue.value;
			int index = propertyValue.index;
			
			sortedList.add(new Item(key, value, index));
		}
		
		// Sort items list.
		Collections.sort(sortedList);
		
		// Create string.
		String text = "";
		
		for (Item item : sortedList) {
			
			String propertyText = String.format("%s = %s\r\n", item.key, item.value);
			text += propertyText;
		}

		// Write string to file.
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filePath), "UTF-8"));
			writer.write(text);
		}
		catch (Exception e) {
			Utility.show2(null, e.getMessage());
		}
	
		// Close objects.
		try {
			if (writer != null) {
				writer.close();
			}
		}
		catch (IOException e) {
			Utility.show2(null, e.getMessage());
		}
	}

	/**
	 * Remove diacritics.
	 */
	public void removeDiacritics() {

		// Remove diacritics items list.
		for (Object key : properties.keySet()) {
			String keyName = (String) key;
			
			// Get value.
			String value = properties.getValue(keyName);
			value = Utility.removeDiacritics(value);
			
			// Set value.
			properties.setValue(keyName, value);
		}
	}
	
	/**
	 * Get existing file
	 * @return
	 */
	public File getFile() {
		
		File file = new File(filePath);
		if (file.exists()) {
			return file;
		}
		return null;
	}
}

/**
 * 
 * @author
 *
 */
class PropertiesBundles {
	
	/**
	 * Properties.
	 */
	LinkedList<PropertiesBundle> propertiesBundles = new LinkedList<PropertiesBundle>();
	
	/**
	 * Keys list.
	 */
	LinkedList<String> keysList = new LinkedList<String>();

	/**
	 * Add property bundle.
	 * @param filePath
	 * @param properties
	 */
	public void add(String filePath, Properties properties) {
		
		propertiesBundles.add(new PropertiesBundle(filePath, properties));
	}

	/**
	 * Get bundle names.
	 * @return
	 */
	public LinkedList<String> getNames() {
		
		LinkedList<String> names = new LinkedList<String>();
		
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			
			names.add(propertiesBundle.name);
		}
		
		return names;
	}

	/**
	 * Initialize key list.
	 */
	public void initKeyList() {
		
		keysList.clear();
		
		// Do loop for all properties' bundles.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			for (Object key : propertiesBundle.properties.keySet()) {
				
				// If it is a new object, add it to the list.
				if (!keysList.contains(key)) {
					keysList.add((String) key);
				}
			}
		}
	}

	/**
	 * Get keys count.
	 * @return
	 */
	public int getKeysCount() {
		
		return keysList.size();
	}

	/**
	 * Get key name.
	 * @param index
	 * @return
	 */
	public String getKeyName(int index) {
		
		return keysList.get(index);
	}

	/**
	 * Get value.
	 * @param keyName
	 * @param propertiesBundleIndex
	 * @return
	 */
	public String getValue(String keyName, int propertiesBundleIndex) {

		// Check index.
		if (!(propertiesBundleIndex >= 0 && propertiesBundleIndex < propertiesBundles.size())) {
			return "";
		}
		
		// Get properties bundle.
		PropertiesBundle propertiesBundle = propertiesBundles.get(propertiesBundleIndex);
		String value = propertiesBundle.properties.getValue(keyName);
		
		return value;
	}

	/**
	 * 
	 * @param keyName
	 * @return
	 */
	public int getIndex(String keyName) {
		
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return -1;
		}
		
		return propertiesBundle.properties.getIndex(keyName);
	}

	/**
	 * Get file path name.
	 * @param languageDescriptor
	 * @return
	 */
	public String getFilePathName(String languageDescriptor) {
		
		String fileName = "messages_" + languageDescriptor + ".properties";
		
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return fileName;
		}
		
		String primaryFilePath = propertiesBundle.filePath;
		
		// Get path.
		int position = primaryFilePath.lastIndexOf(File.separatorChar);
		if (position == -1) {
			return fileName;
		}
		
		String path = primaryFilePath.substring(0, position);
		
		return path + File.separatorChar + fileName;
	}

	/**
	 * Get file path.
	 * @param name
	 * @return
	 */
	public String getFilePath(String name) {
		
		// Do loop for all bundles.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			if (propertiesBundle.name.equals(name)) {
				return propertiesBundle.filePath;
			}
		}
		
		return null;
	}

	/**
	 * Remove bundle.
	 * @param name
	 */
	public void removeBundle(String name) {
		
		// Do loop for all bundles.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			if (propertiesBundle.name.equals(name)) {
				
				propertiesBundles.remove(propertiesBundle);
				return;
			}
		}
	}

	/**
	 * Get bundles count.
	 * @return
	 */
	public int size() {
		
		return propertiesBundles.size();
	}

	/**
	 * Get bundle name.
	 * @param index
	 * @return
	 */
	public String getName(int index) {
		
		PropertiesBundle propertiesBundle = propertiesBundles.get(index);
		if (propertiesBundle == null) {
			return "";
		}
		return propertiesBundle.name;
	}

	/**
	 * Set primary value.
	 * @param keyName
	 * @param value
	 */
	public void setPrimaryValue(String keyName, String value) {
		
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return;
		}
		
		// Set value.
		propertiesBundle.properties.setValue(keyName, value);
	}

	/**
	 * Set value.
	 * @param name
	 * @param keyName
	 * @param value
	 * @param keyIndex 
	 */
	public void setValue(String name, String keyName, String value, int keyIndex) {
		
		// Find properties bundle and set the value.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			
			if (propertiesBundle.name.equals(name)) {
				
				if (!value.isEmpty()) {
					propertiesBundle.properties.put(keyName, value, keyIndex);
				}
				else {
					propertiesBundle.properties.remove(keyName);
				}
				return;
			}
		}
	}

	/**
	 * Get primary key index.
	 * @param keyName
	 * @return
	 */
	public int getPrimaryKeyIndex(String keyName) {
		
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return 0;
		}
		
		return propertiesBundle.properties.getIndex(keyName);
	}

	/**
	 * Remove property.
	 * @param keyName
	 */
	public void removeProperty(String keyName) {
		
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			propertiesBundle.properties.remove(keyName);
		}
		
		initKeyList();
	}

	/**
	 * Add new property.
	 * @param keyName
	 * @param index
	 */
	public void addNew(String keyName, int index) {
		
		// Get primary bundle.
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return;
		}

		propertiesBundle.properties.put(keyName, "", index);
	}

	/**
	 * Returns true value if the key name already exists.
	 * @param keyName
	 * @return
	 */
	public boolean exists(String keyName) {
		
		for (String keyItem : keysList) {
			if (keyItem.equals(keyName)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get last index.
	 * @return
	 */
	public int getLastIndex() {
		
		// Get primary bundle.
		PropertiesBundle propertiesBundle = propertiesBundles.get(0);
		if (propertiesBundle == null) {
			return 0;
		}
		
		int lastIndex = 0;
		
		java.util.Properties properties = propertiesBundle.properties.properties;
		for (Object value : properties.values()) {
			
			PropertyValue propertyValue = (PropertyValue) value;
			int index = propertyValue.index;
			
			if (index > lastIndex) {
				lastIndex = index;
			}
		}
		
		return lastIndex + 1;
	}

	/**
	 * Save files.
	 */
	public void saveFiles() {
		
		// Do loop for all bundles.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			
			propertiesBundle.saveFile();
		}
	}

	/**
	 * Remove diacritics.
	 */
	public void removeDiacritics() {
		
		// Do loop for all properties bundles.
		for (PropertiesBundle propertiesBundle : propertiesBundles) {
			
			propertiesBundle.removeDiacritics();
		}
	}
	
	/**
	 * Gets list of files
	 * @return
	 */
	public ArrayList<File> getFiles() {
		
		ArrayList<File> files = new ArrayList<File>();
		
		for (PropertiesBundle bundle : propertiesBundles) {
			
			File file = bundle.getFile();
			if (file != null) {
				files.add(file);
			}
		}
		return files;
	}
}

/**
 * 
 * @author
 *
 */
public class EditorPanel extends JPanel {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * File name start.
	 */
	static String fileNameStart = "messages_";
	
	/**
	 * Properties bundles.
	 */
	private PropertiesBundles propertiesBundles = new PropertiesBundles();

	/**
	 * Columns reference.
	 */
	private EditorColumns columns;

	/**
	 * Current table column listener.
	 */
	private TableColumnModelListener currentTableColumnListener;

	/**
	 * Table model.
	 */
	private AbstractTableModel tableModel;
	
	/**
	 * Editor name
	 */
	private String name = "";
	
	/**
	 * The last row found by user.
	 */
	private Integer lastFoundRow = null;

	// $hide<<$
	/**
	 * Components.
	 */
	private JScrollPane scrollPane;
	private JTable table;
	private JPopupMenu popupMenu;
	private JMenuItem menuNewLanguage;
	private JMenuItem menuRemoveLanguage;
	private JMenuItem menuAddProperty;
	private JMenuItem menuEditProperty;
	private JMenuItem menuRemoveProperty;
	private JMenuItem menuFindProperty;

	/**
	 * Create the panel.
	 * @param name 
	 */
	public EditorPanel(String name) {

		initComponents();
		
		this.name = name;
		postCreate(); // $hide$
	}
	
	/**
	 * Add popup menu.
	 * @param component
	 * @param popup
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		popupMenu = new JPopupMenu();
		addPopup(scrollPane, popupMenu);
		
		table = new JTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onTableClicked(e);
			}
		});
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
		addPopup(table, popupMenu);
		
		menuAddProperty = new JMenuItem("localizer.menuAddProperty");
		menuAddProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAdd();
			}
		});
		popupMenu.add(menuAddProperty);
		
		menuEditProperty = new JMenuItem("localizer.menuEditProperty");
		menuEditProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onEdit();
			}
		});
		popupMenu.add(menuEditProperty);
		
		menuRemoveProperty = new JMenuItem("localizer.menuRemoveProperty");
		menuRemoveProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRemove();
			}
		});
		popupMenu.add(menuRemoveProperty);
		popupMenu.addSeparator();
		
		menuFindProperty = new JMenuItem("localizer.menuFindProperty");
		menuFindProperty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindProperty();
			}
		});
		popupMenu.add(menuFindProperty);
		popupMenu.addSeparator();
		
		menuNewLanguage = new JMenuItem("localizer.menuNewLanguage");
		menuNewLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNewLanguage();
			}
		});
		popupMenu.add(menuNewLanguage);
		
		menuRemoveLanguage = new JMenuItem("localizer.menuRemoveLanguage");
		menuRemoveLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRemoveLanguage();
			}
		});
		popupMenu.add(menuRemoveLanguage);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
	}

	/**
	 * Localize.
	 */
	private void localize() {

		Utility.localize(menuNewLanguage);
		Utility.localize(menuRemoveLanguage);
		Utility.localize(menuAddProperty);
		Utility.localize(menuEditProperty);
		Utility.localize(menuRemoveProperty);
		Utility.localize(menuFindProperty);
	}

	/**
	 * Load editor.
	 * @param filePath 
	 * @param columns 
	 */
	public PropertiesBundles load(String filePath, EditorColumns columns) {
		
		// Set columns reference.
		this.columns = columns;
		
		// Get path.
		int position = filePath.lastIndexOf(File.separatorChar);
		String path = "";
		
		if (position != -1) {
			path = filePath.substring(0, position);
		}
		
		File pathObject = new File(path);
		final String primaryFileName = new File(filePath).getName();
		
		// Load array of files.
		File [] restFileNames = pathObject.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File directory, String name) {
				
				// Check file name.
				return name.startsWith(fileNameStart) && !name.equalsIgnoreCase(primaryFileName);
			}
		});
		
		// Trim the list
		if (restFileNames == null) {
			restFileNames = new File [] {};
		}

		// Parse primary file content.
		Properties primaryProperties = parseFileContent(filePath);
		propertiesBundles.add(filePath, primaryProperties);
		
		// Parse rest of files contents.
		for (File file : restFileNames) {
			
			if (file == null) {
				continue;
			}
			
			String filePathAux = file.getPath();
			if (!file.exists()) {
				continue;
			}
			// Parse file.
			Properties properties = parseFileContent(filePathAux);
			propertiesBundles.add(filePathAux, properties);
		}
		
		// Initialize keys list.
		propertiesBundles.initKeyList();
		
		// Load table.
		loadTable();
		
		return propertiesBundles;
	}

	/**
	 * Parse line.
	 * @param line
	 * @param value
	 * @return
	 */
	private String parseLine(String line, Obj<String> value) {
		
		int position = line.indexOf('=');
		if (position == -1) {
			
			value.ref = "";
			return line;
		}
		
		int length = line.length();
		
		// Get key.
		String key = line.substring(0, position);
		
		// Get value.
		String valueText = "";
		if (position + 1 <= length) {
			valueText = line.substring(position + 1, length);
		}
		
		// Return key and value.
		value.ref = valueText.trim();
		return key.trim();
	}

	/**
	 * Load file content.
	 * @param fileName
	 * @return
	 */
	private Properties parseFileContent(String fileName) {
		
		Properties fileItems = new Properties();
		
		FileInputStream inputStream = null;
		BufferedReader reader = null;
		try {
			// Create input stream reader.
			inputStream = new FileInputStream(fileName);
			InputStreamReader streamReader = new InputStreamReader(inputStream, "UTF-8");
			reader = new BufferedReader(streamReader);
			
			// Parse properties.
			int index = 0;
			
			Obj<String> value = new Obj<String>();
			while (true) {
				
				// Read line.
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				
				// Parse line.
				String key = parseLine(line, value);
				
				fileItems.put(key, value.ref, index);
				index++;
			}
		}
		catch (Exception e) {

			// Inform user about an error.
			Utility.show2(this, e.getMessage());
		}
		
		// Close objects.
		try {
			if (reader != null) {
				reader.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}
		catch (IOException e) {
		}
		
		return fileItems;
	}

	/**
	 * Load table.
	 * @param columnsDescription 
	 */
	@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
	private void loadTable() {
		
		// Create table model.
		LinkedList<String> columnsList = propertiesBundles.getNames();
		columnsList.addFirst(Resources.getString("localizer.textKeyName"));
		columnsList.addFirst(Resources.getString("localizer.textIndex"));
		
		final String [] columns = columnsList.toArray(new String [0]);
		
		// Set sorter.
		table.setAutoCreateRowSorter(true);
		
		// Set model.
		tableModel = new AbstractTableModel() {

			@Override
			public int getColumnCount() {
				return columns.length;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				
				if (columnIndex == 0) {
					return Integer.class;
				}
				return String.class;
			}
			
			@Override
			public String getColumnName(int index) {
				// Return column name.
				return columns[index];
			}
		
			@Override
			public int getRowCount() {
				return propertiesBundles.getKeysCount();
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {

				String keyName = propertiesBundles.getKeyName(rowIndex);
				
				// Get index.
				if (columnIndex == 0) {
					return propertiesBundles.getIndex(keyName);
				}
				// Get key name.
				if (columnIndex == 1) {
					return keyName;
				}
				
				// Get primary value.
				if (columnIndex >= 2) {
					return propertiesBundles.getValue(keyName, columnIndex - 2);
				}
				return "";
			}
		};
		
		table.setModel(tableModel);
		
		// Set column widths.
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		if (this.columns != null) {
			final TableColumnModel columnModel = table.getColumnModel();

			// Remove current listener.
			if (currentTableColumnListener != null) {
				columnModel.removeColumnModelListener(currentTableColumnListener);
			}
			
			columnModel.getColumn(0).setPreferredWidth(this.columns.getColumn0());
			columnModel.getColumn(1).setPreferredWidth(this.columns.getColumn1());
			
			// Set other columns.
			int width = this.columns.getOtherColumns();
			for (int index = 2; index < columnModel.getColumnCount(); index++) {
				
				columnModel.getColumn(index).setPreferredWidth(width);
			}
			
			final EditorColumns thisColumns = this.columns;
			
			// Set columns listener.		
			currentTableColumnListener = new TableColumnModelListener() {
				@Override
				public void columnMarginChanged(ChangeEvent e) {

					// Save column widths.
					thisColumns.setColumn0(columnModel.getColumn(0).getWidth());
					thisColumns.setColumn1(columnModel.getColumn(1).getWidth());
					thisColumns.setOtherColumns(columnModel.getColumn(2).getWidth());
				}
				@Override
				public void columnSelectionChanged(ListSelectionEvent e) {
				}
				@Override
				public void columnRemoved(TableColumnModelEvent e) {
				}
				@Override
				public void columnMoved(TableColumnModelEvent e) {
				}
				@Override
				public void columnAdded(TableColumnModelEvent e) {
				}
			};
			
			columnModel.addColumnModelListener(currentTableColumnListener);
		}
		
		// Sort table.
		DefaultRowSorter sorter = ((DefaultRowSorter)table.getRowSorter());
    	ArrayList list = new ArrayList();
    	list.add( new RowSorter.SortKey(0, SortOrder.ASCENDING));
    	sorter.setSortKeys(list);
    	sorter.sort();
	}

	/**
	 * Update table.
	 */
	public void updateTable() {
		
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
	}

	/**
	 * On new language.
	 */
	protected void onNewLanguage() {
		
		// Get language descriptor.
		String languageDescriptor = Utility.input(this, "localizer.textInsertNewLanguageDescriptor");
		if (languageDescriptor == null) {
			return;
		}
		
		String filePathName = propertiesBundles.getFilePathName(languageDescriptor);
				
		// Create new file.
		if (!Utility.askParam(this, "localizer.messageCreateNewFile", filePathName)) {
			return;
		}
		
		File newFile = new File(filePathName);
		try {
			newFile.createNewFile();
		}
		catch (IOException e) {
			Utility.show2(this, e.getMessage());
		}
		
		// Create new bundle.
		Properties properties = new Properties();
		propertiesBundles.add(filePathName, properties);
		
		// Load table.
		loadTable();
	}
	
	/**
	 * On remove language.
	 */
	@SuppressWarnings("unchecked")
	protected void onRemoveLanguage() {
		
		// Select languages to remove.
		Obj<String> selectedName = new Obj<String>();
		LinkedList<String> names = (LinkedList<String>) propertiesBundles.getNames().clone();
		names.removeFirst();
		
		if (!SelectNameDlg.showDialog(this, names, selectedName) || selectedName.ref == null) {
			return;
		}
		
		// Remove selected file.
		String filePath = propertiesBundles.getFilePath(selectedName.ref);
		if (filePath == null) {
			Utility.show(this, "localizer.messageUnknownLanguageName");
			return;
		}
		
		if (!Utility.askParam(this, "localizer.messageRemoveFile", filePath)) {
			return;
		}
		
		File file = new File(filePath);
		file.delete();
		
		// Remove bundle.
		propertiesBundles.removeBundle(selectedName.ref);
		
		// Load table.
		loadTable();
	}

	/**
	 * Get selected index.
	 * @return
	 */
	private Integer getSelectedIndex() {
		
		int row = table.getSelectedRow();
		if (row == -1) {
			
			Utility.show(this, "localizer.messageSelectProperty");
			return null;
		}
		
		int index = table.convertRowIndexToModel(row);
		return index;
	}

	/**
	 * On edit.
	 */
	protected void onEdit() {
		
		// Get selected index.
		Integer selected = getSelectedIndex();
		if (selected == null) {
			return;
		}
		
		// Get key name.
		String keyName = propertiesBundles.getKeyName(selected);
		if (keyName == null) {
			Utility.show(this, "localizer.messageUnknowKeyName");
			return;
		}
		
		// Edit property with given key.
		if (!PropertyEditor.showDialog(this, keyName, propertiesBundles, this)) {
			return;
		}
		
		// Save files.
		saveFiles();
		
		// Update table model.
		updateTableModel();
	}
	
	/**
	 * Update table model.
	 */
	private void updateTableModel() {
		
		int selectedRow = table.getSelectedRow();

		AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
		tableModel.fireTableDataChanged();
		
		// Select item.
		try {
			table.setRowSelectionInterval(selectedRow, selectedRow);
		}
		catch (Exception e) {
		}
	}

	/**
	 * On table clicked.
	 * @param e 
	 */
	protected void onTableClicked(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			onEdit();
		}
	}
	
	/**
	 * On remove property.
	 */
	protected void onRemove() {
		
		// Get selected property index.
		Integer selected = getSelectedIndex();
		if (selected == null) {
			return;
		}
		
		// Get key name.
		String keyName = propertiesBundles.getKeyName(selected);
		if (keyName == null) {
			Utility.show(this, "localizer.messageUnknowKeyName");
			return;
		}
		
		// Confirm deletion.
		if (!Utility.askParam(this, "localizer.messageRemoveProperty", keyName)) {
			return;
		}
		
		// Remove property.
		propertiesBundles.removeProperty(keyName);
		
		// Update table model.
		updateTableModel();
	}

	/**
	 * On add property.
	 */
	protected void onAdd() {
		
		// Get key name.
		String keyName = Utility.input(this, "localizer.messageInsertKeyName");
		if (keyName == null) {
			return;
		}
		
		// Check if the key name already exists.
		int row = getPropertyRow(keyName);
		if (row >= 0) {
			
			selectTableRow(row);
			
			Utility.show(this, "localizer.messageKeyAlreadyExists");
			return;
		}
		
		// Get last index.
		int lastIndex = propertiesBundles.getLastIndex();
		
		// Insert new key name.
		propertiesBundles.addNew(keyName, lastIndex);
		propertiesBundles.initKeyList();
		
		// Update table model.
		updateTableModel();
		
		Rectangle rect = table.getCellRect(tableModel.getRowCount() - 1, 0, true);
		table.scrollRectToVisible(rect);
	}

	/**
	 * Save files.
	 */
	public void saveFiles() {
		
		// Do loop for all bundles.
		propertiesBundles.saveFiles();
	}

	/**
	 * Remove diacritics.
	 */
	public void removeDiacritics() {
		
		// Delegate call.
		propertiesBundles.removeDiacritics();
		
		// Update table model.
		updateTableModel();
	}

	/**
	 * On find property.
	 */
	protected void onFindProperty() {
		
		// Open search dialog.
		final Obj<SearchTextDialog> searchTextDialog = new Obj<SearchTextDialog>();
		searchTextDialog.ref = SearchTextDialog.showDialog(this, "localizer.textSearchPropertyDialog", true,
				
				// On OK.
				parameters -> {
					
					// Start from selected property or from the beginning.
					Integer startRow = table.getSelectedRow();
					if (startRow == -1) {
						startRow = 0;
						lastFoundRow = null;
					}
					if (startRow.equals(lastFoundRow)) {
						
						if (parameters.isForward()) {
							startRow++;
						}
						else {
							startRow--;
						}
					}
					
					// Search in table.
					boolean forward = parameters.isForward();
					int rows = table.getRowCount();
					int columns = table.getColumnCount();
					
					// Do loop forward or backward.
					for (int row = startRow; forward ? row < rows : row >= 0; row = forward ? row + 1 : row - 1) {
						
						// Get row text.
						String rowText = "";
						for (int column = 0; column < columns; column++) {
							rowText += (column > 0 ? " " : "")  + table.getValueAt(row, column).toString();
						}
						
						// If a text is found, select the row and exit method.
						if (Utility.find(rowText, parameters)) {
							
							selectTableRow(row);
							
							lastFoundRow = row;
							return;
						}
					}
					
					// If not found, inform user about it.
					Utility.show(this, "localizer.messageNoMatchingPropertyFound");
				},
				
				// On Cancel
				() -> {
					
					// Realease dialog object.
					searchTextDialog.ref.dispose();
					searchTextDialog.ref = null;
				});
	}
	
	/**
	 * Select table row
	 * @param row
	 */
	public void selectTableRow(int row) {
		
		table.getSelectionModel().setSelectionInterval(row, row);
		table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
	}
	
	/**
	 * Returns true if the property already exists
	 * @param propertyName
	 * @return
	 */
	public int getPropertyRow(String propertyName) {
		
		int rows = table.getRowCount();
		
		// Do loop forward or backward.
		for (int row = 0; row < rows; row++) {
			
			// Get property text.
			String propertyText = table.getValueAt(row, 1).toString();
			
			// If property text matches, return true value.
			if (propertyText.contentEquals(propertyName)) {
				return row;
			}
		}
		
		// Property not found
		return -1;
	}
	
	/**
	 * Gets list of files
	 * @return
	 */
	public ArrayList<File> getListOfFiles() {
		
		ArrayList<File> list = propertiesBundles.getFiles();
		return list;
	}
	
	/**
	 * Gets tab name
	 * @return
	 */
	public String getTabName() {
		
		return name;
	}
}
