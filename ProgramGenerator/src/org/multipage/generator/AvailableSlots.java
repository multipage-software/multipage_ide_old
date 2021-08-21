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
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.maclan.Slot;
import org.maclan.SlotHolder;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.GraphUtility;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author
 *
 */
public class AvailableSlots extends JDialog {
	
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
	 * Text pane.
	 */
	private JTextComponent textPane;
	
	/**
	 * Model.
	 */
	private DefaultListModel model;
	
	/**
	 * Direct user slots flag.
	 */
	private boolean onlyDirectUserSlots = false;
	
	/**
	 * Slot reference.
	 */
	private Slot slot;

	// $hide<<$
	/**
	 * Components.
	 */
	private JScrollPane scrollPane;
	private JList list;

	/**
	 * Show dialog
	 * @param textPane
	 * @param onlyDirectUserSlots 
	 * @param slot 
	 * @return
	 */
	public static void showDialog(JTextComponent textPane, boolean onlyDirectUserSlots, Slot slot) {

		AvailableSlots dialog = new AvailableSlots(textPane, onlyDirectUserSlots, slot);
		dialog.setVisible(true);
	}
	
	/**
	 * Create the frame.
	 * @param textPane 
	 * @param onlyDirectUserSlots 
	 * @param slot 
	 */
	public AvailableSlots(JTextComponent textPane, boolean onlyDirectUserSlots, Slot slot) {
		super(Utility.findWindow(textPane), ModalityType.DOCUMENT_MODAL);
		this.textPane = textPane;
		this.slot = slot;
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		this.onlyDirectUserSlots = onlyDirectUserSlots;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.generator.textInheritedSlots");
		getContentPane().setBackground(UIManager.getColor("scrollbar"));
		setBounds(100, 100, 250, 316);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, getContentPane());
		scrollPane.setBackground(Color.WHITE);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		list = new JList();
		list.setBackground(new Color(250, 250, 210));
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					slotSelected();
				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		setPosition();
		setList();
		loadSlots();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		 setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
	}

	/**
	 * Set position.
	 */
	private void setPosition() {
		
		Point mouse = MouseInfo.getPointerInfo().getLocation();

		// Get window width and height.
		int width = getWidth();
		int height = getHeight();
		
		mouse.x -= width / 2;
		mouse.y -= height / 2;
		
		// Get screen dimensions and set window location.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		if (mouse.x + width > screenSize.width) {
			mouse.x = screenSize.width - width;
		}
		
		if (mouse.y + height > screenSize.height) {
			mouse.y = screenSize.height - height;
		}
		
		if (mouse.x < 0) {
			mouse.x = 0;
		}
		
		if (mouse.y < 0) {
			mouse.y = 0;
		}
		
		setLocation(mouse);
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		Color oldColor = g.getColor();
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.setColor(oldColor);
	}

	/**
	 * Set list.
	 */
	private void setList() {
		
		// Set model.
		model = new DefaultListModel();
		list.setModel(model);
		
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
					setIcon(Images.getIcon("org/multipage/generator/images/slot.png"));
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
	 * Load available slots.
	 */
	private void loadSlots() {
		
		// Clear list.
		model.clear();

		Area area = null;
		
		if (slot != null) {
			// Get slot area.
			SlotHolder holder = slot.getHolder();
			if (holder instanceof Area) {
				area = (Area) holder;
			}
		}
		else {
			// Get selected areas.
			LinkedList<Area> areas = GeneratorMainFrame.getFrame().getSelectedAreas();
			if (!areas.isEmpty()) {
				area = areas.getFirst();
			}
		}
							
		if (area != null) {
			Middle middle = ProgramBasic.getMiddle();
			
			// Database login.
			MiddleResult result = middle.login(ProgramBasic.getLoginProperties());
			if (result.isOK()) {
				
				// Get area slots aliases.
				LinkedList<String> slotsAliases = new LinkedList<String>();
				
				if (!onlyDirectUserSlots) {
					result = middle.loadSlotsInheritedAliases(area.getId(), slotsAliases, false);
				}
				else {
					result = middle.loadSlotsAliasesUser(area.getId(), slotsAliases);
				}
				
				if (result.isOK()) {
					
					// Sort the list.
					Collections.sort(slotsAliases);

					// Do loop for all slots aliases.
					for (String slotAlias : slotsAliases) {
						
						model.addElement(slotAlias);
					}
				}
				
				// Logout from database.
				MiddleResult logoutResult = middle.logout(result);
				if (result.isOK()) {
					result = logoutResult;
				}
			}

			// On error inform user.
			if (result.isNotOK()) {
				result.show(GeneratorMainFrame.getFrame());
			}
		}
	}
	
	/**
	 * On slot selected.
	 */
	protected void slotSelected() {
		
		String slotAlias = list.getSelectedValue().toString();
		if (slotAlias != null) {

			textPane.replaceSelection(String.format("[@TAG %s]", slotAlias));
		}
		
		dispose();
	}
}
