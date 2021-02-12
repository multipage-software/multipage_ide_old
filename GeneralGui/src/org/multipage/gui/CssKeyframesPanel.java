/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.*;
import java.io.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * @author
 *
 */
public class CssKeyframesPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelKeyframes;
	private JScrollPane scrollPane;
	private JToolBar toolBar;
	private JList<CssKeyframe> list;

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 469, 218);
		boundsSet = false;
	}

	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		boundsSet = true;
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
	}

	/**
	 * Initial string.
	 */
	private String initialString;
	
	/**
	 * List model.
	 */
	private DefaultListModel<CssKeyframe> listModel;
	
	// $hide<<$

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssKeyframesPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelKeyframes = new JLabel("org.multipage.gui.textKeyframes");
		springLayout.putConstraint(SpringLayout.NORTH, labelKeyframes, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelKeyframes, 10, SpringLayout.WEST, this);
		add(labelKeyframes);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelKeyframes);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, this);
		add(scrollPane);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -3, SpringLayout.NORTH, toolBar);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, -10, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, scrollPane);
		
		list = new JList();
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onListClick(e);
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, labelKeyframes);
		add(toolBar);
	}

	/**
	 * On list click.
	 * @param e
	 */
	protected void onListClick(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			onEdit();
		}
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		setFromInitialString();
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		
		initToolbar();
		initList();
		
		loadDialog();
	}

	/**
	 * Initialize list.
	 */
	private void initList() {
		
		listModel = new DefaultListModel<CssKeyframe>();
		list.setModel(listModel);
		
		// Set renderer.
		list.setCellRenderer(new ListCellRenderer<CssKeyframe>() {

			// Renderer.
			@SuppressWarnings("serial")
			RendererCssKeyframeItem renderer = new RendererCssKeyframeItem() {

				// Get preferred size.
				@Override
				public Dimension getPreferredSize() {
					
					final Dimension dimension = new Dimension(list.getWidth(), 80);
					return dimension;
				}
			};
			
			// Get renderer callback method.
			@Override
			public Component getListCellRendererComponent(
					JList<? extends CssKeyframe> list, CssKeyframe value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (value == null) {
					return null;
				}
				
				renderer.set(value, isSelected, cellHasFocus, 0);
				return renderer;
			}
		});
	}

	/**
	 * Initialize toolbar.
	 */
	private void initToolbar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/insert.png", this, "onAdd", "org.multipage.gui.tooltipAddKeyframe");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/edit.png", this, "onEdit", "org.multipage.gui.tooltipEditKeyframe");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", this, "onDelete", "org.multipage.gui.tooltipDeleteKeyframe");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_backward.png", this, "onMoveUp", "org.multipage.gui.tooltipMoveKeyframeUp");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_forward.png", this, "onMoveDown", "org.multipage.gui.tooltipMoveKeyframeDown");
	}
	
	/**
	 * On move keyframe up.
	 */
	@SuppressWarnings("unused")
	private void onMoveUp() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleKeyframe");
			return;
		}
		
		// Check selection, move font up and select it.
		if (index <= 0) {
			return;
		}
		
		CssKeyframe current = listModel.get(index);
		CssKeyframe previous = listModel.get(index - 1);
		listModel.set(index, previous);
		listModel.set(index - 1, current);
		
		list.setSelectedIndex(index - 1);
		list.ensureIndexIsVisible(index - 1);
	}
		
	/**
	 * On move keyframe down.
	 */
	@SuppressWarnings("unused")
	private void onMoveDown() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleKeyframe");
			return;
		}
		
		int count = listModel.getSize();
		
		// Check selection, move font down and select it.
		if (index >= count - 1) {
			return;
		}
		
		CssKeyframe current = listModel.get(index);
		CssKeyframe next = listModel.get(index + 1);
		listModel.set(index, next);
		listModel.set(index + 1, current);
		
		list.setSelectedIndex(index + 1);
		list.ensureIndexIsVisible(index + 1);
	}
	
	/**
	 * On add keyframe.
	 */
	@SuppressWarnings("unused")
	private void onAdd() {
		
		CssKeyframe keyframe = CssKeyFrameItemDialog.showDialog(this);
		if (keyframe != null) {
			
			listModel.addElement(keyframe);
		}
	}
	
	/**
	 * On edit keyframe.
	 */
	private void onEdit() {
		
		CssKeyframe keyframe = list.getSelectedValue();
		if (keyframe == null) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleKeyframe");
			return;
		}
		
		if (CssKeyFrameItemDialog.editDialog(this, keyframe)) {
			list.updateUI();
		}
	}
	
	/**
	 * On delete keyframe.
	 */
	@SuppressWarnings("unused")
	private void onDelete() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleKeyframe");
			return;
		}
		
		if (!Utility.ask(this, "org.multipage.gui.messageDeleteKeyframe")) {
			return;
		}
		
		listModel.remove(index);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		// Compile keyframes specification.
		String specification = "";
		boolean isFirst = true;
		
		for (int index = 0; index < listModel.getSize(); index++) {
			
			if (!isFirst) {
				specification += "\r\n";
			}
			
			CssKeyframe keyframe = listModel.get(index);
			specification += "    " + keyframe.getSpecificationText();
			
			isFirst = false;
		}

		return specification;
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		// Initialize controls.
		listModel.removeAllElements();

		if (initialString != null) {
			
			// Parse lines.
			String [] lines = initialString.split("\r\n");
			for (String line : lines) {
				
				CssKeyframe keyframe = CssKeyframe.parse(line);
				if (keyframe != null) {
					
					listModel.addElement(keyframe);
				}
			}
		}
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelKeyframes);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssKeyframesBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getResultText()
	 */
	@Override
	public String getResultText() {
		
		return getSpecification();
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getContainerDialogBounds()
	 */
	@Override
	public Rectangle getContainerDialogBounds() {
		
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		CssKeyframesPanel.bounds = bounds;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#boundsSet()
	 */
	@Override
	public boolean isBoundsSet() {

		return boundsSet;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setBoundsSet(boolean)
	 */
	@Override
	public void setBoundsSet(boolean set) {
		
		boundsSet = set;
	}

	/**
	 * Get component.
	 * @return
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 * @return
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 * @param string
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 * @return
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssKeyframes;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
