/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.multipage.basic.*;
import org.maclan.*;

import java.awt.event.*;

/**
 * 
 * @author
 *
 */
public class SelectAreaTextResources extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dark color.
	 */
	private static final Color darkColor = new Color(250, 250, 210);

	/**
	 * Output resource reference.
	 */
	private Resource resource;

	/**
	 * Area reference.
	 */
	private Area area;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel label;
	private JScrollPane scrollPane;
	private JList list;
	/**
	 * @wbp.nonvisual location=391,159
	 */
	private final JLabel labelNoResource = new JLabel("builder.textNoTextResource");
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("area");
		setBounds(100, 100, 252, 366);
		
		label = new JLabel("builder.textSelectTextResource");
		getContentPane().add(label, BorderLayout.NORTH);
		
		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onSelected(e);
			}
		});
		scrollPane.setViewportView(list);
	}

	/**
	 * On resource selected.
	 * @param e
	 */
	protected void onSelected(MouseEvent e) {
		
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			
			// Open text resource editor.
			resource = (Resource) list.getSelectedValue();
			dispose();
		}
	}

	/**
	 * Lunch dialog.
	 * @param parent
	 * @param area 
	 * @return
	 */
	public static Resource showDialog(Component parent, Area area) {

		if (area == null) {
			return null;
		}
		SelectAreaTextResources dialog = new SelectAreaTextResources(parent, area);
		dialog.setVisible(true);

		return dialog.resource;
	}
	/**
	 * Create the dialog.
	 * @param parent 
	 * @param area 
	 */
	public SelectAreaTextResources(Component parent, Area area) {

		super(Utility.findWindow(parent), ModalityType.DOCUMENT_MODAL);
		labelNoResource.setHorizontalAlignment(SwingConstants.CENTER);
		// Initialize components.
		initComponents();
		// $hide>>$
		this.area = area;
		postCreate();
		// $hide<<$
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		Utility.centerOnScreen(this);
		setTitle();
		setIcons();
		loadTextResources();
		setRenderer();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		labelNoResource.setIcon(Images.getIcon("org/multipage/generator/images/error.png"));
	}

	/**
	 * Set list renderer.
	 */
	private void setRenderer() {
		
		// Set renderer.
		list.setCellRenderer(new ListCellRenderer() {
			// Renderer.
			@SuppressWarnings("serial")
			class Renderer extends JLabel {
				// Parameters.
				private boolean isSelected;
				private boolean hasFocus;
				// Constructor.
				Renderer() {
					setIcon(Images.getIcon("org/multipage/generator/images/edit_resource.png"));
					setOpaque(true);
				}
				// Set properties.
				void setProperties(String text, int index, boolean isSelected,
						boolean hasFocus) {
					
					setText(text);
					setBackground((index % 2 == 0) ? Color.WHITE : darkColor);
					this.isSelected = isSelected;
					this.hasFocus = hasFocus;
				}
				// Paint label.
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					GraphUtility.drawSelection(g, this, isSelected, hasFocus);
				}
			}
			Renderer renderer = new Renderer();
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				renderer.setProperties(value.toString(), index, isSelected, cellHasFocus);
				return renderer;
			}
		});
	}

	/**
	 * Set title.
	 */
	private void setTitle() {
		
		setTitle(area.toString());
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(label);
		Utility.localize(labelNoResource);
	}

	/**
	 * Load text resources.
	 */
	private void loadTextResources() {

		// Load resources.
		Middle middle = ProgramBasic.getMiddle();
		Properties login = ProgramBasic.getLoginProperties();
		LinkedList<AreaResource> resources = new LinkedList<AreaResource>();
		MiddleResult result = middle.loadAreaResources(login, area, resources, null);
		
		// Report error.
		if (result.isNotOK()) {
			result.show(this);
			return;
		}
		// Load resources.
		DefaultListModel model = new DefaultListModel();
		
		for (AreaResource resource : resources) {
			if (resource.isSavedAsText()) {
				model.addElement(resource);
			}
		}
		
		if (!model.isEmpty()) {
			list.setModel(model);
		}
		else {
			getContentPane().remove(scrollPane);
			getContentPane().add(labelNoResource, BorderLayout.CENTER);
			getContentPane().remove(label);
		}
	}
}

