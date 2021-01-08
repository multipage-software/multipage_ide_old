/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

import javax.swing.GroupLayout.*;
import javax.swing.LayoutStyle.*;
import java.awt.*;
import java.io.*;


/**
 * @author
 *
 */
public class SearchAreaDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bounds.
	 */
	private static Rectangle bounds;
	
	/**
	 * Columns' widths.
	 */
	private static int [] columnsWidths;
	
	/**
	 * Column count.
	 */
	private static final int columnCount = 5;
	
	/**
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		
		columnsWidths = new int [] {250, 100, 30, 30, 250};
	}

	/**
	 * Load state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
		
		
		object = inputStream.readObject();
		if (!(object instanceof int [])) {
			throw new ClassNotFoundException();
		}
		columnsWidths = (int []) object;
		if (columnsWidths.length != columnCount) {
			throw new ClassNotFoundException();
		}
	}

	/**
	 * Save state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(columnsWidths);
	}

	/**
	 * Table models.
	 */
	private DefaultTableModel areasTableModel;
    private javax.swing.JCheckBox caseSensitive;
    private javax.swing.JButton clearButton;
    private javax.swing.JCheckBox exactMatch;
    private javax.swing.JButton globalAreaButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelSearchString;
    private javax.swing.JButton reloadButton;
    private javax.swing.JTable resultsTable;
    private javax.swing.JLabel searchResultsLabel;
    private javax.swing.JTextField searchStringText;
    private javax.swing.JCheckBox wholeWordsButton;

    /**
     * AreasModel reference.
     */
	private AreasModel model;

	/**
	 * Results tables.
	 */
	private Object[][] areasTable;
	private JPopupMenu popupMenu;

	/**
	 * Add popup trayMenu.
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
	 * Initialize components. Created with NetBeans.
	 */
    private void initComponents() {

        labelSearchString = new javax.swing.JLabel();
        searchStringText = new TextFieldEx();
        searchResultsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        caseSensitive = new javax.swing.JCheckBox();
        wholeWordsButton = new javax.swing.JCheckBox();
        reloadButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        exactMatch = new javax.swing.JCheckBox();
        globalAreaButton = new javax.swing.JButton();

        setTitle("org.multipage.generator.textSearchDialogTitle");

        labelSearchString.setText("org.multipage.generator.textSearchStringLabel");

        searchStringText.setForeground(new java.awt.Color(255, 0, 0));

        searchResultsLabel.setText("org.multipage.generator.textSearchResults");
        
        popupMenu = new JPopupMenu();
        addPopup(resultsTable, popupMenu);

        resultsTable.setModel(areasTableModel);
        resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(resultsTable);

        caseSensitive.setText("org.multipage.generator.textCaseSensitive");

        wholeWordsButton.setText("org.multipage.generator.textWholeWords");

        exactMatch.setText("org.multipage.generator.textExactMatch");

        globalAreaButton.setText("org.multipage.generator.textGlobalArea");
        globalAreaButton.setMargin(new Insets(0, 0, 0, 0));
        globalAreaButton.setPreferredSize(new java.awt.Dimension(80, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
        				.addComponent(labelSearchString, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(reloadButton, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
        					.addGap(2)
        					.addComponent(clearButton, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(searchStringText, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(140)
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(exactMatch)
        						.addComponent(caseSensitive)
        						.addComponent(wholeWordsButton)))
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(searchResultsLabel)
        					.addPreferredGap(ComponentPlacement.RELATED, 249, Short.MAX_VALUE)
        					.addComponent(globalAreaButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(labelSearchString)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
        				.addComponent(searchStringText, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        				.addComponent(clearButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        				.addComponent(reloadButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
        			.addGap(32)
        			.addComponent(caseSensitive)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(wholeWordsButton)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(exactMatch)
        					.addGap(5)
        					.addComponent(globalAreaButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        				.addComponent(searchResultsLabel))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        			.addContainerGap())
        );
        layout.linkSize(SwingConstants.VERTICAL, new Component[] {searchStringText, reloadButton, clearButton});
        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {reloadButton, clearButton});
        getContentPane().setLayout(layout);

        pack();
        
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent arg0) {
        		onClose();
        	}
        });
    }

    /**
     * Localize components texts.
     */
	private void localize() {

		Utility.localize(this);
		Utility.localize(labelSearchString);
		Utility.localize(searchResultsLabel);
		Utility.localize(caseSensitive);
		Utility.localize(wholeWordsButton);
		Utility.localize(exactMatch);
		Utility.localize(globalAreaButton);
	}
	
	/**
	 * Constructor.
	 * @param parent
	 */
    public SearchAreaDialog(java.awt.Frame parent, AreasModel model) {
        super(parent, false);

		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));

        this.model = model;
        
        // Load table models.
        loadTableModels();
        
        initComponents();
        // Set initial focus.
        searchStringText.requestFocusInWindow();
        // Set component texts.
        localize();
        // Load icons.
        setcons();
        // Add listeners.
        setListeners();

        initializeTable();
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Initialize popup trayMenu.
        initPopupMenu();
        
        // Load dialog state.
        loadDialog();
    }

    /**
     * Initialize popup trayMenu.
     */
    private void initPopupMenu() {
    	
    	final Component thisComponent = this;
		
		AreaLocalMenu areaMenu = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get selected area.
				return getSelectedArea();
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		areaMenu.addTo(this, popupMenu);
	}

    /**
     * Get selected area.
     * @return
     */
	protected Area getSelectedArea() {
		
		int selectedRow = resultsTable.getSelectedRow();
		
		if (selectedRow == -1) {
			return null;
		}
		return (Area) areasTableModel.getValueAt(selectedRow, 0);
	}

	/**
     * On close.
     */
	protected void onClose() {
		
		saveDialog();
		
		dispose();
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
		
		// Set column widths.
		TableColumnModel columnModel = resultsTable.getColumnModel();
		
		for (int index = 0; index < columnCount; index++) {
			
			columnsWidths[index] = columnModel.getColumn(index).getWidth();
		}
	}
    /**
     * Load icons.
     */
    private void setcons() {

		reloadButton.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		clearButton.setIcon(Images.getIcon("org/multipage/generator/images/remove_icon.png"));
		globalAreaButton.setIcon(Images.getIcon("org/multipage/generator/images/center.png"));
	}

	/**
     * Load table models.
     */
    @SuppressWarnings("serial")
	private void loadTableModels() {

    	// Areas model.
    	areasTable = new Object [][] {};
    	areasTableModel = new DefaultTableModel(
                areasTable,
                new String [] {
                    Resources.getString("org.multipage.generator.textSearchResultAreas"),
                    Resources.getString("org.multipage.generator.textSerachResultAreaAliases"),
                    Resources.getString("org.multipage.generator.textSearchResultId"),
                    Resources.getString("org.multipage.generator.textSearchResultNumSlots"),
                    Resources.getString("org.multipage.generator.textSearchResultParentArea")
                }
            ) {
    			@Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            };
	}

	/**
     * Add listeners.
     */
    public void setListeners() {
        searchStringText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onInputChanged();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {			
				onInputChanged();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {			
				onInputChanged();
			}
	    });
        caseSensitive.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
            	onInputChanged();
            }
        });
        wholeWordsButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
            	onInputChanged();
            }
        });
        exactMatch.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
            	onInputChanged();
            }
        });
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
				// Invoke method.
            	if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            		onSelectionChanged(resultsTable.getSelectedRow());
            	}
            }
        });
        reloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onInputChanged();
			}
        });
        clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Reset search string and invoke change method.
				searchStringText.setText("");
				onInputChanged();
			}
		});
        globalAreaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Set focus on global area.
				GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusGlobalArea();
			}
		});
    }

	/**
     * On selection changed.
     * @param selectedRow
     */
	protected void onSelectionChanged(int selectedRow) {
		
		// If no selection, exit the method.
		if (selectedRow == -1) {
			return;
		}
		
		// Get main frame.
		GeneratorMainFrame frame = GeneratorMainFrame.getFrame();
		
		// Get selected area and shapes.
		AreaCoordinatesTableItem coordinatesItem = (AreaCoordinatesTableItem) areasTableModel.getValueAt(selectedRow, 4);
		Area area = (Area) areasTableModel.getValueAt(selectedRow, 0);
		
		// Focus coordinates.
		AreasDiagram diagram = frame.getVisibleAreasEditor().getDiagram();
		diagram.focus(coordinatesItem.coordinate, area);
	}

	/**
     * On input changed.
     * @param evt
     */
    private void onInputChanged() {
    	
    	// Invoke the method content asynchronously.
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
		
		    	// Get search text and load table.
		 		String searchText = searchStringText.getText();
			    loadAreasTable(searchText);
			}
    	});
    }

    /**
     * Load areas table.
     * @param searchText 
     */
	private void loadAreasTable(String searchText) {
		
		// Reset the table
		areasTableModel.setRowCount(0);
		
		// Do loop for all area shapes in the model.
		for (Area area : model.getAreas()) {
			
			String areaAlias = area.getAlias();
			
			// If the area description contains the search string.
			if (Utility.matches(area.getDescription(), area.getDescriptionForDiagram(),
					searchText, caseSensitive.isSelected(),
					wholeWordsButton.isSelected(), exactMatch.isSelected())
				
				|| Utility.matches(areaAlias, areaAlias,
						searchText, caseSensitive.isSelected(),
						wholeWordsButton.isSelected(), exactMatch.isSelected())) {
				
				Object user = area.getUser();
				if (user != null && user instanceof AreaShapes) {
					
					AreaShapes shapes = (AreaShapes) user;
					
					// Do loop for all shape coordinates.
					for (AreaCoordinates coordinate : shapes.getCoordinates()) {
						
						areasTableModel.addRow(new Object [] 
						        { area, area.getAlias(), area.getId(), area.getSlotAliasesCount(),
								new AreaCoordinatesTableItem(coordinate) });
					}
				}
			}
		}
	}

	/**
     * Sets model and renderer.
     */
	@SuppressWarnings("serial")
	private void initializeTable() {

		resultsTable.setModel(areasTableModel);
		// Set column widths.
		TableColumnModel columnModel = resultsTable.getColumnModel();
		
		for (int index = 0; index < columnCount; index++) {
			
			columnModel.getColumn(index).setPreferredWidth(columnsWidths[index]);
		}
		
		// Set area cell renderer.
		columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				
				if (value instanceof Area) {
					super.setValue(((Area) value).getDescriptionForDiagram());
					return;
				}
				super.setValue(value);
			}
		});
	}
}

/**
 * 
 * @author
 *
 */
class AreaCoordinatesTableItem  {
	
	AreaCoordinates coordinate;

	/**
	 * Constructor.
	 * @param coordinate
	 */
	public AreaCoordinatesTableItem(AreaCoordinates coordinate) {
		
		this.coordinate = coordinate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (coordinate != null) {
			
			Area parentArea = coordinate.getParentArea();
			
			if (parentArea != null) {
				return parentArea.getDescriptionForDiagram();
			}
		}
		
		return "";
	}
}
