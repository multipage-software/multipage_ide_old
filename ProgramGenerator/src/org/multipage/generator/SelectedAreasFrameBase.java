/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

/**
 * @author
 *
 */
public class SelectedAreasFrameBase extends JFrame {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Frame reference.
	 */
	protected static SelectedAreasFrameBase frame = ProgramGenerator.newSelectedAreasFrame();
	
	/**
	 * Update information.
	 */
	public static void updateInformation() {
		
		frame.reload();
	}

	/**
	 * List model.
	 */
	private DefaultListModel model;
	
	/**
	 * Areas properties reference.
	 */
	private AreasPropertiesBase areasPropertiesPanel;
	
	/**
	 * MessagePanel label.
	 */
	private MessagePanel panelMessage;
	
	/**
	 * Show new frame
	 */
	public static void showFrame() {
		
		frame.reload();
		frame.setVisible(true);
	}
	
	/**
	 * Components' references.
	 */
	private JLabel labelList;
	private JList list;
	private JRadioButton radioAliases;
	private JRadioButton radioDescriptions;
	private JButton buttonReload;
	private JPopupMenu popupMenu;
	private JMenuItem menuSelectArea;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JCheckBox checkCaseSensitive;
	private JCheckBox checkWholeWords;
	private JCheckBox checkExactMatch;
	private JButton buttonClose;
	private JMenuItem menuSelectAreaWithSubareas;
	private JSplitPane splitPane;
	
	/**
	 * 
	 * @param labelList
	 * @param list
	 * @param radioAliases
	 * @param radioDescriptions
	 * @param buttonReload
	 * @param popupMenu
	 * @param menuSelectArea
	 * @param labelFilter
	 * @param textFilter
	 * @param checkCaseSensitive
	 * @param checkWholeWords
	 * @param checkExactMatch
	 * @param buttonClose
	 * @param menuSelectAreaWithSubareas
	 * @param splitPane 
	 */
	protected void setComponentsReferences(
			JLabel labelList,
			JList list,
			JRadioButton radioAliases,
			JRadioButton radioDescriptions,
			JButton buttonReload,
			JPopupMenu popupMenu,
			JMenuItem menuSelectArea,
			JLabel labelFilter,
			JTextField textFilter,
			JCheckBox checkCaseSensitive,
			JCheckBox checkWholeWords,
			JCheckBox checkExactMatch,
			JButton buttonClose,
			JMenuItem menuSelectAreaWithSubareas,
			JSplitPane splitPane
			) {
		
		this.labelList = labelList;
		this.list = list;
		this.radioAliases = radioAliases;
		this.radioDescriptions = radioDescriptions;
		this.buttonReload = buttonReload;
		this.popupMenu = popupMenu;
		this.menuSelectArea = menuSelectArea;
		this.labelFilter = labelFilter;
		this.textFilter = textFilter;
		this.checkCaseSensitive = checkCaseSensitive;
		this.checkWholeWords = checkWholeWords;
		this.checkExactMatch = checkExactMatch;
		this.buttonClose = buttonClose;
		this.menuSelectAreaWithSubareas = menuSelectAreaWithSubareas;
		this.splitPane = splitPane;
	}
	
	/**
	 * Constructor.
	 */
	public SelectedAreasFrameBase() {
		
	}
	
	/**
	 * Get area and on error inform user.
	 * @return
	 */
	private Area getAreaInformUser() {
		
		// Get single selected area.
		Object [] selected = list.getSelectedValues();
		if (selected.length != 1) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleArea");
			return null;
		}
		return (Area) selected[0];
	}

	/**
	 * Focus area.
	 */
	protected void focusArea() {

		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}
		
		long areaId = area.getId();
		AreasDiagramEditor editor = GeneratorMainFrame.getFrame().getAreaDiagramEditor();
		// Focus area.
		editor.focusArea(areaId);
	}

	/**
	 * Select area.
	 */
	protected void selectArea() {

		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}

		// Select area.
		AreasDiagram diagram = GeneratorMainFrame.getFrame().getAreaDiagram();
		diagram.selectArea(area.getId(), true);
	}

	/**
	 * Select area with subareas.
	 */
	protected void selectAreaWithSubareas() {
		
		Area area = getAreaInformUser();
		if (area == null) {
			return;
		}

		// Select area.
		AreasDiagram diagram = GeneratorMainFrame.getFrame().getAreaDiagram();
		diagram.selectAreaWithSubareas(area.getId(), true);
	}

	/**
	 * Post creation.
	 */
	protected void postCreate() {
		
		// $hide>>$
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		// Create and set areas properties panel.
		areasPropertiesPanel = ProgramGenerator.newAreasProperties(true);
		panelMessage = new MessagePanel();
		
		setNoAreasSelectedMessage();
		
		// Set listeners.
		setListeners();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Create list.
		createList();
		// Localize components.
		localize();
		// Set icons.
		setIcons();
		// Set popup trayMenu.
		setPopupMenu();
		// $hide<<$
	}

	/**
	 * Set no areas selected message.
	 */
	private void setNoAreasSelectedMessage() {
		
		panelMessage.setText(Resources.getString("org.multipage.generator.textNoAreaSelected"));
		splitPane.setRightComponent(panelMessage);
	}

	/**
	 * Set popup trayMenu.
	 */
	private void setPopupMenu() {
		
		final Component thisComponent = this;
		
		AreaLocalMenu areaLocalPopup = ProgramGenerator.newAreaLocalMenu(new AreaLocalMenuListener() {
			@Override
			protected Area getCurrentArea() {
				// Get single selected area.
				Object [] selected = list.getSelectedValues();
				if (selected.length != 1) {
					return null;
				}
				return (Area) selected[0];
			}

			@Override
			public Component getComponent() {
				// Get this component.
				return thisComponent;
			}
		});
		
		areaLocalPopup.addTo(this, popupMenu);
	}

	/**
	 * Set icons.
	 */
	protected void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		buttonReload.setIcon(Images.getIcon("org/multipage/generator/images/update_icon.png"));
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		
		menuSelectArea.setIcon(Images.getIcon("org/multipage/generator/images/selected_area.png"));
		menuSelectAreaWithSubareas.setIcon(Images.getIcon("org/multipage/generator/images/selected_subareas.png"));
	}

	/**
	 * Localize components.
	 */
	protected void localize() {

		Utility.localize(this);
		Utility.localize(labelList);
		Utility.localize(radioAliases);
		Utility.localize(radioDescriptions);
		Utility.localize(buttonReload);
		Utility.localize(menuSelectArea);
		Utility.localize(labelFilter);
		Utility.localize(checkCaseSensitive);
		Utility.localize(checkWholeWords);
		Utility.localize(checkExactMatch);
		Utility.localize(buttonClose);
		Utility.localize(menuSelectAreaWithSubareas);
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// Set listener.
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				LinkedList<Area> areas = getAreas();
				if (areas.isEmpty()) {
					return;
				}
				
		    	int location = splitPane.getDividerLocation();
		    	
		    	// Set areas properties panel.
		    	areasPropertiesPanel.setAreas(areas);
		    	splitPane.setRightComponent(areasPropertiesPanel);
		    	
		    	splitPane.setDividerLocation(location);
		    	
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					onListDoubleClick();
				}
			}
		});
		
		// Set filter listener.
		textFilter.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				onFilterChange();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				onFilterChange();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				onFilterChange();
			}			
			private void onFilterChange() {
				reload();
			}
		});
		
		// Set radio listener.
		ActionListener radioListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				reload();
			}
		};
		radioDescriptions.addActionListener(radioListener);
		radioAliases.addActionListener(radioListener);
	}
	
	/**
	 * Get selected areas.
	 * @return
	 */
	protected LinkedList<Area> getAreas() {
		
		// Get selected areas.
		Object [] selected = list.getSelectedValues();
		LinkedList<Area> areas = new LinkedList<Area>();
		
		for (Object object : selected) {
			areas.add((Area) object);
		}
		return areas;
	}

	/**
	 * Create list.
	 */
	private void createList() {
		
		// Create model.
		model = new DefaultListModel();
		list.setModel(model);
		
		// Create renderer.
		@SuppressWarnings("serial")
		class Renderer extends JLabel {

			private boolean isSelected;
			private boolean cellHasFocus;

			Renderer() {
				setOpaque(true);
				setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
			}
			
			public void setProperties(String text, int index,
					boolean isSelected, boolean cellHasFocus) {

				setText(text);
				setBackground(Utility.itemColor(index));
				this.isSelected = isSelected;
				this.cellHasFocus = cellHasFocus;
			}

			@Override
			public void paint(Graphics g) {
				
				super.paint(g);
				GraphUtility.drawSelection(g, this, isSelected, cellHasFocus);
			}
		}
		
		list.setCellRenderer(new ListCellRenderer() {
			
			private Renderer renderer = new Renderer();
			
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (!(value instanceof Area)) {
					return null;
				}
				Area area = (Area) value;
				String text  = radioAliases.isSelected() ? area.getAlias()
						: area.getDescriptionForDiagram();
				if (text.isEmpty()) {
					text = Resources.getString("org.multipage.generator.textUnknownDescription");
				}
				
				renderer.setProperties(text, index, isSelected, cellHasFocus);
				return renderer;
			}
		});
	}

	/**
	 * Reload area.
	 * @param areas2
	 */
	protected void reload() {
		
		int dividerLocation = splitPane.getDividerLocation();
		
		// Reset panel.
		areasPropertiesPanel.setAreas(null);
		setNoAreasSelectedMessage();
		
		// Get area.
		int [] selectedIndices = list.getSelectedIndices();
		
		model.clear();
		
		// Get selected areas.
		LinkedList<Area> areas = GeneratorMainFrame.getFrame().getAreaDiagram().getSelectedAreas();
		
		boolean isAliases = radioAliases.isSelected();
		LinkedList<Area> areasSorted = new LinkedList<Area>();
		
		String filterText = textFilter.getText();
		
		// Load texts.
		for (Area areaItem : areas) {
			
			String alias = areaItem.getAlias();
			String description = areaItem.getDescriptionForced();
			
			String text = isAliases ? alias : description;
			if (!filterText.isEmpty() && !Utility.matches(text, filterText,
					checkCaseSensitive.isSelected(), checkWholeWords.isSelected(),
					checkExactMatch.isSelected())) {
				continue;
			}
			areasSorted.add(areaItem);

		}
		
		// Sort texts.
		class AreasComparator implements Comparator<Area> {
			
			boolean isAliases;
			
			public AreasComparator(boolean isAliases) {
				this.isAliases = isAliases;
			}

			@Override
			public int compare(Area area1, Area area2) {
				
				String area1Text;
				String area2Text;
				
				if (isAliases) {
					area1Text = area1.getAlias();
					area2Text = area2.getAlias();
				}
				else {
					area1Text = area1.getDescriptionForced();
					area2Text = area2.getDescriptionForced();
				}
				return area1Text.compareTo(area2Text);
			}
		}
		
		Collections.sort(areasSorted, new AreasComparator(isAliases));
		
		// Load list.
		for (Area areaSorted : areasSorted) {
			model.addElement(areaSorted);
		}
		
		// Select area.
		if (selectedIndices != null) {
			list.setSelectedIndices(selectedIndices);
		}
		
		// Restore divider location.
		splitPane.setDividerLocation(dividerLocation);
	}

	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	protected static void addPopup(Component component, final JPopupMenu popup) {
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
	 * On list double click.
	 */
	protected void onListDoubleClick() {

		focusArea();
	}
}
