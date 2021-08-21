/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.maclan.Area;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextPopupMenu;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreaHelpViewer extends JFrame {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Window states.
	 */
	public static Rectangle bounds = new Rectangle(0, 0, 0, 0);
	public static int dividerLocation = 56;

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (object instanceof Rectangle) {
			bounds = (Rectangle) object;
		}
		else {
			throw new ClassNotFoundException();
		}
		dividerLocation = inputStream.readInt();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeInt(dividerLocation);
	}

	// $hide<<$
	/**
	 * Components.
	 */
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JList listAreas;
	private JScrollPane scrollPaneAreas;
	private JSplitPane splitPane;
	
	/**
	 * Lunch dialog.
	 * @param component
	 * @param foundAreas 
	 * @return
	 */
	public static void showDialog(Component component, LinkedList<Area> foundAreas) {

		Window parentWindow = Utility.findWindow(component);
		AreaHelpViewer dialog = new AreaHelpViewer(parentWindow, foundAreas);
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param foundAreas 
	 */
	public AreaHelpViewer(Window parentWindow, LinkedList<Area> foundAreas) {

		// Initialize components.
		initComponents();
		// $hide>>$
		postCreate(foundAreas);
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("org.multipage.generator.textInfo");
		setBounds(100, 100, 276, 402);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		scrollPaneAreas = new JScrollPane();
		splitPane.setLeftComponent(scrollPaneAreas);
		scrollPaneAreas.setPreferredSize(new Dimension(2, 54));
		
		listAreas = new JList();
		listAreas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneAreas.setViewportView(listAreas);
		listAreas.setForeground(Color.BLACK);
		listAreas.setOpaque(true);
		listAreas.setBackground(Color.WHITE);
		listAreas.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setBackground(Color.WHITE);
		textPane.setContentType("text/html;charset=UTF-8");
		scrollPane.setViewportView(textPane);
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
		
		if (bounds.width == 0 && bounds.height == 0) {
			// Center dialog.
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
		
		splitPane.setDividerLocation(dividerLocation);
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
		
		dividerLocation = splitPane.getDividerLocation();
	}
	
	/**
	 * Post creation.
	 * @param foundAreas 
	 */
	private void postCreate(LinkedList<Area> foundAreas) {

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Load areas list.
		loadAreasList(foundAreas);
		// Load text.
		loadText(foundAreas.getFirst());
		// Set icon.
		setIconImage(Images.getImage("org/multipage/basic/images/main_icon.png"));
		// Load dialog.
		loadDialog();
		// Set popup trayMenu.
		new TextPopupMenu(textPane);
		
		// Select last area.
		int size = listAreas.getModel().getSize();
		if (size > 0) {
			listAreas.setSelectedIndex(size -1);
		}
	}

	/**
	 * Load areas list.
	 * @param foundAreas
	 */
	@SuppressWarnings("serial")
	private void loadAreasList(LinkedList<Area> foundAreas) {
		
		final DefaultListModel<Area> model = new DefaultListModel<Area>();
		listAreas.setModel(model);
		
		listAreas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				// On list selection.
				// Get selected area.
				int selectedIndex = listAreas.getSelectedIndex();
				Area selectedArea = model.get(selectedIndex);
				
				// Load text.
				loadText(selectedArea);
			}
		});
		
		for (Area foundArea : foundAreas) {
			model.addElement(foundArea);
		}
		
		// Set list renderer.
		listAreas.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				
				if (value instanceof Area) {
					value = ((Area) value).getDescriptionForDiagram();
				}
				
				return super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
		});
	}

	/**
	 * Load text.
	 * @param area
	 */
	private void loadText(Area area) {
		
		// Set title and label.
		String title = String.format(
				Resources.getString("org.multipage.generator.textInfo"),
				area.getDescriptionForced());
		setTitle(title);
		
		// Set content.
		Obj<String> helpText = new Obj<String>();
		MiddleResult result = ProgramBasic.getMiddle().loadHelp(
				ProgramBasic.getLoginProperties(), area, helpText);
		if (result.isNotOK()) {
			helpText.ref = result.getMessage();
		}
		
		textPane.setText(helpText.ref);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scrollPane.getVerticalScrollBar().setValue(0);
			}
		});
	}
}
