/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.maclan.MiddleResult;
import org.maclan.Resource;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class ResourceAreasDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Bounds.
	 */
	private static Rectangle bounds;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Read data.
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
	}

	/**
	 * Write data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
	
	/**
	 * List model.
	 */
	private DefaultListModel<Area> listModel;
	
	/**
	 * Close resources flag.
	 */
	private boolean closeResources = false;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonClose;
	private JPanel panelMain;
	private JLabel labelResource;
	private JScrollPane scrollPane;
	private JList list;
	private JPopupMenu popupMenu;
	private JButton buttonCloseResources;


	/**
	 * Show dialog.
	 * @param parent
	 * @param resource 
	 * @param resource
	 */
	public static void showDialog(Component parent, Resource resource, Obj<Boolean> closeResources) {
		
		ResourceAreasDialog dialog = new ResourceAreasDialog(Utility.findWindow(parent),
				closeResources == null);
		
		dialog.loadResourceAreas(resource);
		
		dialog.setVisible(true);
		
		if (closeResources != null) {
			closeResources.ref = dialog.closeResources;
		}
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param hideCloseResources 
	 */
	public ResourceAreasDialog(Window parentWindow, boolean hideCloseResources) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		buttonCloseResources.setVisible(!hideCloseResources);
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onClose();
			}
		});
		setTitle("org.multipage.generator.textResourceAreasList");
		
		setBounds(100, 100, 450, 337);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
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
		
		buttonCloseResources = new JButton("org.multipage.generator.textCloseResources");
		buttonCloseResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCloseResources();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCloseResources, 0, SpringLayout.SOUTH, buttonClose);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCloseResources, -6, SpringLayout.WEST, buttonClose);
		buttonCloseResources.setPreferredSize(new Dimension(85, 25));
		buttonCloseResources.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCloseResources);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelResource = new JLabel("resource");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelResource, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelResource, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelResource);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelResource);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
		
		list = new JList();
		scrollPane.setViewportView(list);
		
		popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);
	}

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
	 * On close.
	 */
	protected void onClose() {
		
		saveDialog();
		dispose();
	}

	/**
	 * Close resources.
	 */
	protected void onCloseResources() {
		
		closeResources = true;
		
		onClose();
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		loadDialog();

		localize();
		setIcons();
		
		initTable();
		initPopupMenu();
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
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(buttonCloseResources);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonCloseResources.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Initialize list.
	 */
	private void initTable() {
		
		// Create table model.
		listModel = new DefaultListModel<Area>();
		list.setModel(listModel);
		
		// Set cell renderer.
		list.setCellRenderer(new ListCellRenderer<Area>() {
			// Renderer.
			@SuppressWarnings("serial")
			RendererJLabel renderer = new RendererJLabel() {
				{
					setIcon(Images.getIcon("org/multipage/generator/images/area_node.png"));
				}
			};
			// Overridden method.
			@Override
			public Component getListCellRendererComponent(
					JList<? extends Area> list, Area area, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				renderer.setText(area.getDescriptionForDiagram());
				renderer.set(isSelected, cellHasFocus, index);
				
				return renderer;
			}
		});
		
		// On double click.
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onDblClick(e);
			}
		});
	}

	/**
	 * Load resource areas.
	 * @param resource
	 */
	private void loadResourceAreas(Resource resource) {
		
		// Set label text.
		String labelText = String.format(
				Resources.getString("org.multipage.generator.textResourceAreasLabel"), resource.getDescription());
		labelResource.setText(labelText);
		
		// Load resource areas' IDs.
		LinkedList<Long> areasIds = new LinkedList<>();
		
		MiddleResult result = ProgramBasic.getMiddle().loadResourceAreasIds(
				ProgramBasic.getLoginProperties(), resource.getId(), areasIds);
		
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		
		// Get areas list.
		LinkedList<Area> areas = ProgramGenerator.getAreasModel().getAreas(areasIds);
		Collections.sort(areas, new Comparator<Area>() {
			@Override
			public int compare(Area area1, Area area2) {
				// Compare area descriptions.
				return area1.getDescription().compareTo(area2.getDescription());
			}
		});
		
		// Load areas.
		listModel.clear();
		
		for (Area area : areas) {
			listModel.addElement(area);
		}
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
		
		// Get selected list item.
		Object item = list.getSelectedValue();
		if (item instanceof Area) {
			return (Area) item;
		}
		
		return null;
	}
	
	/**
	 * On double click.
	 * @param e
	 */
	protected void onDblClick(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			
			// Focus selected area.
			Area area = getSelectedArea();
			if (area != null) {
				
				GeneratorMainFrame.getFrame().getVisibleAreasEditor().focusArea(area.getId());
			}
		}
	}

}