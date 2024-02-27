/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.*;

import com.maclan.*;

import java.awt.*;
import java.util.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author
 *
 */
public class AreasPropertiesFrame extends JFrame {

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
	 * Set default state.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Read state.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
	}

	/**
	 * Write state.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Used frames.
	 */
	private static LinkedList<AreasPropertiesFrame> frames =
		new LinkedList<AreasPropertiesFrame>();
	
	/**
	 * Update frames.
	 */
	public static void updateInformation() {

		// Update area properties frames.
		for (AreasPropertiesFrame frame : frames) {
			frame.updateFrameInformation();
		}
	}

	/**
	 * Editor.
	 */
	private AreasPropertiesBase editor;

	/**
	 * Area.
	 */
	private Area area;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;

	/**
	 * Display new frame.
	 * @param area
	 */
	public static void displayNew(Area area) {
		
		AreasPropertiesFrame frame = new AreasPropertiesFrame(area);
		frames.add(frame);
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 * @param area 
	 */
	public AreasPropertiesFrame(Area area) {
		setAlwaysOnTop(true);
		
		this.area = area;
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		postCreation();
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
		setBounds(100, 100, 359, 523);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 0, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, getContentPane());
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
	}

	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		createEditor();
		setTitle();
		setIcons();
		loadDialog();
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
	}
	
	/**
	 * Create editor.
	 */
	private void createEditor() {
		
		editor = newAreasProperties(false);
		panel.add(editor);
		
		LinkedList<Area> areas = new LinkedList<Area>();
		areas.add(area);
		editor.setAreas(areas);
	}
	
	/**
	 * Create new areas properties object.
	 * @param isPropertiesPanel
	 * @return
	 */
	protected AreasPropertiesBase newAreasProperties(boolean isPropertiesPanel) {
		
		return ProgramGenerator.newAreasProperties(isPropertiesPanel);
	}

	/**
	 * Set title.
	 */
	private void setTitle() {
		
		setTitle(String.format(
				Resources.getString("org.multipage.generator.textEditArea"), area));
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
	}
	
	/**
	 * Update information.
	 */
	private void updateFrameInformation() {
		
		editor.updateEditor();
	}

	/**
	 * On close window.
	 */
	protected void onClose() {
		
		saveDialog();
		frames.remove(this);
		dispose();
	}
}
