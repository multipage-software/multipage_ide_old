/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.multipage.basic.ProgramBasic;
import org.multipage.generator.GeneratorMainFrame;
import org.multipage.generator.TextResourceEditor;
import org.maclan.*;

import java.awt.event.*;
import java.io.*;

/**
 * 
 * @author
 *
 */
public class SearchTextResources extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Columns' widths.
	 */
	private static ArrayList<Integer> columnsWidthsState = new ArrayList<Integer>();
	
	/**
	 * Bounds state.
	 */
	private static Rectangle bounds = new Rectangle();
	
	/**
	 * Search flag states.
	 */
	private static boolean caseSensitiveState = false;
	private static boolean wholeWordsState = false;
	private static boolean exactMatchState = false;

	/**
	 * Load dialog state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		columnsWidthsState = (ArrayList<Integer>) inputStream.readObject();
		bounds = (Rectangle) inputStream.readObject();
		caseSensitiveState = inputStream.readBoolean();
		wholeWordsState = inputStream.readBoolean();
		exactMatchState = inputStream.readBoolean();
	}

	/**
	 * Save dialog state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(columnsWidthsState);
		outputStream.writeObject(bounds);
		outputStream.writeBoolean(caseSensitiveState);
		outputStream.writeBoolean(wholeWordsState);
		outputStream.writeBoolean(exactMatchState);
	}

	/**
	 * Areas list.
	 */
	private LinkedList<Area> areas;

	/**
	 * Results array.
	 */
	private Object[][] resultsArray;
	
	/**
	 * Table model.
	 */
	private DefaultTableModel tableResultsModel;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonClose;
	private JPanel panelMain;
	private JLabel labelSearchText;
	private TextFieldEx textSearchedString;
	private JButton buttonSearch;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWords;
	private JCheckBox checkExactMatch;
	private JLabel labelResults;
	private JScrollPane scrollPane;
	private JRadioButton radioInAreas;
	private JRadioButton radioInAllResources;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTable tableResults;
	private JLabel labelMessage;
	private JPopupMenu popupMenu;
	private JMenuItem menuOpenEditor;
	private JMenuItem menuFocusArea;

	/**
	 * Show dialog.
	 * @param parent
	 * @param areas 
	 * @param resource
	 */
	public static void showDialog(Component parent, LinkedList<Area> areas) {
		
		SearchTextResources dialog = new SearchTextResources(Utility.findWindow(parent), areas);
		
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param areas 
	 */
	public SearchTextResources(Window parentWindow, LinkedList<Area> areas) {
		super(parentWindow, ModalityType.MODELESS);
		
		initComponents();
		
		// $hide>>$
		this.areas = areas;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(450, 400));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onClose();
			}
		});
		setTitle("builder.textSearchInTextResources");
		
		setBounds(100, 100, 528, 462);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 45));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonClose = new JButton("textClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onClose();
			}
		});
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, panel);
		buttonClose.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonClose);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelSearchText = new JLabel();
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSearchText, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSearchText, 10, SpringLayout.WEST, panelMain);
		labelSearchText.setText("builder.textSearchInResourcesText");
		panelMain.add(labelSearchText);
		
		textSearchedString = new TextFieldEx();
		textSearchedString.setPreferredSize(new Dimension(6, 24));
		sl_panelMain.putConstraint(SpringLayout.NORTH, textSearchedString, 6, SpringLayout.SOUTH, labelSearchText);
		sl_panelMain.putConstraint(SpringLayout.WEST, textSearchedString, 10, SpringLayout.WEST, panelMain);
		textSearchedString.setForeground(Color.RED);
		panelMain.add(textSearchedString);
		
		buttonSearch = new JButton();
		buttonSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSearch();
			}
		});
		buttonSearch.setText("builder.textSearch");
		buttonSearch.setMargin(new Insets(0, 0, 0, 0));
		sl_panelMain.putConstraint(SpringLayout.EAST, textSearchedString, -3, SpringLayout.WEST, buttonSearch);
		buttonSearch.setPreferredSize(new Dimension(80, 24));
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonSearch, 0, SpringLayout.NORTH, textSearchedString);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonSearch, -10, SpringLayout.EAST, panelMain);
		panelMain.add(buttonSearch);
		
		checkCaseSensitive = new JCheckBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, checkCaseSensitive, 20, SpringLayout.SOUTH, textSearchedString);
		sl_panelMain.putConstraint(SpringLayout.WEST, checkCaseSensitive, 100, SpringLayout.WEST, panelMain);
		checkCaseSensitive.setText("org.multipage.generator.textCaseSensitive");
		panelMain.add(checkCaseSensitive);
		
		checkWholeWords = new JCheckBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, checkWholeWords, 6, SpringLayout.SOUTH, checkCaseSensitive);
		sl_panelMain.putConstraint(SpringLayout.WEST, checkWholeWords, 0, SpringLayout.WEST, checkCaseSensitive);
		checkWholeWords.setText("org.multipage.generator.textWholeWords");
		panelMain.add(checkWholeWords);
		
		checkExactMatch = new JCheckBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, checkExactMatch, 6, SpringLayout.SOUTH, checkWholeWords);
		sl_panelMain.putConstraint(SpringLayout.WEST, checkExactMatch, 0, SpringLayout.WEST, checkCaseSensitive);
		checkExactMatch.setText("org.multipage.generator.textExactMatch");
		panelMain.add(checkExactMatch);
		
		labelResults = new JLabel();
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelResults, 10, SpringLayout.SOUTH, checkExactMatch);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelResults, 0, SpringLayout.WEST, labelSearchText);
		labelResults.setText("builder.textSearchResults");
		panelMain.add(labelResults);
		
		scrollPane = new JScrollPane();

		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelResults);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, -15, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
		
		tableResults = new JTable();
		tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableResults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onTableClicked(e);
			}
		});
		
		popupMenu = new JPopupMenu();
		addPopup(tableResults, popupMenu);
		
		menuOpenEditor = new JMenuItem("builder.textOpenResourceEditor");
		menuOpenEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openEditorForSelected();
			}
		});
		popupMenu.add(menuOpenEditor);
		
		menuFocusArea = new JMenuItem("org.multipage.generator.textFocusArea");
		menuFocusArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFocusArea();
			}
		});
		popupMenu.add(menuFocusArea);
		scrollPane.setViewportView(tableResults);
		
		radioInAreas = new JRadioButton("builder.textInSelectedAreas");
		buttonGroup.add(radioInAreas);
		sl_panelMain.putConstraint(SpringLayout.NORTH, radioInAreas, 0, SpringLayout.NORTH, checkCaseSensitive);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioInAreas, 250, SpringLayout.WEST, panelMain);
		panelMain.add(radioInAreas);
		
		radioInAllResources = new JRadioButton("builder.textInAllTextResources");
		buttonGroup.add(radioInAllResources);
		sl_panelMain.putConstraint(SpringLayout.WEST, radioInAllResources, 0, SpringLayout.WEST, radioInAreas);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, radioInAllResources, 0, SpringLayout.SOUTH, checkWholeWords);
		panelMain.add(radioInAllResources);
		
		labelMessage = new JLabel("New label");
		sl_panelMain.putConstraint(SpringLayout.SOUTH, labelMessage, 0, SpringLayout.SOUTH, panelMain);
		labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		labelMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelMessage, 0, SpringLayout.WEST, labelSearchText);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelMessage, 0, SpringLayout.EAST, buttonSearch);
		panelMain.add(labelMessage);
	}

	/**
	 * On close.
	 */
	protected void onClose() {
		
		saveDialog();
		dispose();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		// Set window position.
		if (bounds.getWidth() * bounds.getHeight() == 0) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		
		// Set search flags.
		checkCaseSensitive.setSelected(caseSensitiveState);
		checkWholeWords.setSelected(wholeWordsState);
		checkExactMatch.setSelected(exactMatchState);
		
		// Set appearance.
		localize();
		setIcons();
		setAccelerators();
		
		// Reset message.
		labelMessage.setText(Resources.getString("builder.textEnterSearchedStringAndPressEnter"));
		
        // Load table models.
        loadTableModel();
		
		// Set radio button.
		radioInAreas.setSelected(true);
		
		// Set search button as a default button.
		getRootPane().setDefaultButton(buttonSearch);
	}

	/**
	 * Set accelerators.
	 */
	private void setAccelerators() {
		
		menuOpenEditor.setAccelerator(KeyStroke.getKeyStroke("control O"));
		menuFocusArea.setAccelerator(KeyStroke.getKeyStroke("control alt A"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(labelSearchText);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(labelResults);
		Utility.localize(buttonSearch);
		Utility.localize(radioInAreas);
		Utility.localize(radioInAllResources);
		Utility.localize(menuOpenEditor);
		Utility.localize(menuFocusArea);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonSearch.setIcon(Images.getIcon("org/multipage/generator/images/search_icon.png"));
		menuOpenEditor.setIcon(Images.getIcon("org/multipage/generator/images/edit_resource.png"));
		menuFocusArea.setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
	}
	
	/**
     * Load table models.
     */
    @SuppressWarnings("serial")
	private void loadTableModel() {

    	// Areas model.
    	resultsArray = new Object [][] {};
    	tableResultsModel = new DefaultTableModel(
    			resultsArray,
                new String [] {
    				Resources.getString("builder.textResourceId"),
                    Resources.getString("builder.textResourceDescription"),
                    Resources.getString("builder.textResourceArea"),
                    Resources.getString("builder.textResourceLocalDescription"),
                    Resources.getString("builder.textResourceFullNamespace"),
                    Resources.getString("builder.textResourceMimeType2"),
                    Resources.getString("builder.textResourceIsVisible2")
                }
            ) {
    			@Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            };
    
        tableResults.setModel(tableResultsModel);
        
        // Set columns.
        TableColumnModel columnModel = tableResults.getColumnModel();
        Enumeration<TableColumn> columns = columnModel.getColumns();
        
        // Default columns' widths.
        final int [] defaultColumnWidths = {50, 120, 120, 70, 100, 80, 30};
        int index = 0;
        
        while (columns.hasMoreElements()) {
        	TableColumn column = columns.nextElement();
        	
        	int preferedWidth;
        	try {
        		preferedWidth = columnsWidthsState.get(index);
        	}
        	catch (Exception e) {
        		preferedWidth = defaultColumnWidths[index];
        	}
        	column.setPreferredWidth(preferedWidth);
        	
        	index++;
        }
        
        // Disable columns reordering.
        tableResults.getTableHeader().setReorderingAllowed(false);
	}
    
	/**
	 * On search.
	 */
	protected void onSearch() {
		
		// Reset message.
		labelMessage.setText(Resources.getString("builder.textEmptyResult"));
		
		// Reset the table.
		tableResultsModel.setRowCount(0);
		
		// Get search properties.
		String searchedText = textSearchedString.getText();
		if (searchedText.isEmpty()) {
			return;
		}
		
		boolean caseSensitive = checkCaseSensitive.isSelected();
		boolean wholeWords = checkWholeWords.isSelected();
		boolean exactMatch = checkExactMatch.isSelected();
		
		
		LinkedList<Resource> resources = new LinkedList<Resource>();
		
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		
		// Login to the database.
		MiddleResult result = middle.login(login);
		if (result.isNotOK()) {
			result.show(this);
			return;
		}

		// Set wait cursor.
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Check search type and search resources.
		if (radioInAreas.isSelected()) {
			
			result = middle.searchAreasTextResources(searchedText, areas,
					caseSensitive, wholeWords, exactMatch, resources);
		}
		else {
			
			result = middle.searchAllTextResources(searchedText,
					caseSensitive, wholeWords, exactMatch, resources);
		}

		// If the result is correct.
		if (result.isOK()) {
			
			// If there are no results inform user and exit.
			if (!resources.isEmpty()) {
				
				// Set message.
				labelMessage.setText(String.format(
						Resources.getString("builder.textNumberOfResultsFound"), resources.size()));
				
				// Load table model.
				for (Resource resource : resources) {
					
					// Load full name space.
					Obj<String> fullNameSpace = new Obj<String>("");
					result = middle.loadNameSpacePath(resource.getParentNamespaceId(), fullNameSpace, "/");
					if (result.isNotOK()) {
						break;
					}
					
					// Trim path.
					if (fullNameSpace.ref.length() > 1) {
						if (fullNameSpace.ref.charAt(0) == '/'
								&& fullNameSpace.ref.charAt(0) == '/') {
							
							fullNameSpace.ref = fullNameSpace.ref.substring(1);
						}
					}
					
					// Load MIME type.
					Obj<MimeType> mimeType = new Obj<MimeType>();
					result = middle.loadMimeType(resource.getMimeTypeId(), mimeType);
										if (result.isNotOK()) {
						break;
					}
					
					// Add table row.
					tableResultsModel.addRow(new Object [] 
					        { 
							resource.getId(),
							resource.getDescription(),
							resource instanceof AreaResource ? ((AreaResource) resource).getArea() : "",
							resource instanceof AreaResource ? ((AreaResource) resource).getLocalDescription() : "",
							fullNameSpace.ref,
							mimeType.toString(),
							Resources.getString(resource.isVisible() ? "builder.textTrue" : "builder.textFalse")
							});
				}
			}
			else {
				Utility.show(this, "builder.messageNoTextResourcesWithGivenTextFound");
			}
		}
		
		// Set default cursor.
		setCursor(Cursor.getDefaultCursor());

		// Logout from the database.
		MiddleResult logoutResult = middle.logout(result);
		if (result.isOK()) {
			result = logoutResult;
		}
		
		// On error inform user and quit the method.
		if (result.isNotOK()) {
			result.show(this);
		}
	}
	
	/**
	 * On table clicked.
	 * @param e 
	 */
	protected void onTableClicked(MouseEvent e) {
		
		// On double click.
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			
			openEditorForSelected();
		}
	}
	
	/**
	 * Open editor for selected item.
	 */
	private void openEditorForSelected() {
		
		// Get resource ID.
		ListSelectionModel selectionModel = tableResults.getSelectionModel();
		int selectedRow = selectionModel.getMinSelectionIndex();
		
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectTableItem");
			return;
		}
		
		long resourceId = (Long) tableResultsModel.getValueAt(selectedRow, 0);
		Object column2Value =  tableResultsModel.getValueAt(selectedRow, 2);
		Area area = null;
		
		if (column2Value instanceof Area) {
			area = (Area) column2Value;
		}
		
		String areaDescription = area != null ? area.getDescriptionForced() : "";
		
		// Show text resource editor.
		String searchText = textSearchedString.getText();
		boolean isCaseSensitive = checkCaseSensitive.isSelected();
		boolean isWholeWords = checkWholeWords.isSelected();
		if (searchText.equals("*")) {
			searchText = "";
		}
		
		FoundAttr foundAttributes = new FoundAttr(searchText, isCaseSensitive, isWholeWords);
		
		// Open editor.
		TextResourceEditor.showDialog(this, resourceId, areaDescription, true, foundAttributes, false);	
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		// Save bounds.
		bounds = getBounds();
		
		// Save search flags.
		caseSensitiveState = checkCaseSensitive.isSelected();
		wholeWordsState = checkWholeWords.isSelected();
		exactMatchState = checkExactMatch.isSelected();
		
		// Save table columns' widths.
		TableColumnModel columnModel = tableResults.getColumnModel();
		Enumeration<TableColumn> e = columnModel.getColumns();
		
		columnsWidthsState.clear();
		
		while (e.hasMoreElements()) {
			
			TableColumn column = e.nextElement();
			int width = column.getWidth();
			
			columnsWidthsState.add(width);
		}
	}
	
	/**
	 * Popup trayMenu.
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
	 * On focus area.
	 */
	protected void onFocusArea() {
		
		// Get selected item.
		ListSelectionModel selectionModel = tableResults.getSelectionModel();
		int selectedRow = selectionModel.getMinSelectionIndex();
		
		if (selectedRow == -1) {
			
			Utility.show(this, "builder.messageSelectTableItem");
			return;
		}

		// Get area.
		Area area = null;
		Object column2Value =  tableResultsModel.getValueAt(selectedRow, 2);
		
		if (column2Value instanceof Area) {
			area = (Area) column2Value;
		}
		else {
			Utility.show(this, "builder.messageNoAreaAvailable");
			return;
		}

		// Focus area.
		GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusArea(area.getId());
	}
}