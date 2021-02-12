/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;
import javax.swing.border.Border;

import org.multipage.util.*;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * @author
 *
 */
public class CssTransformPanel extends InsertPanel implements StringValueEditor {
	
	/**
	 * Components.
	 */
	private JLabel labelTransformFunctions;
	private JScrollPane scrollPane;
	private JList<CssTransform> list;
	private JToolBar toolBar;
	private JButton buttonMatrix;
	private JButton buttonMatrix3D;
	private JButton buttonTranslate;
	private JButton buttonTranslate3D;
	private JButton buttonScale;
	private JButton buttonScale3D;
	private JButton buttonRotate;
	private JButton buttonRotate3D;
	private JButton buttonSkew;
	private JButton buttonPerspective;
	private JPopupMenu popupMenu;
	private JMenuItem menuEditTransform;
	private JMenuItem menuRemoveTransform;

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
	private DefaultListModel<CssTransform> model;
	
	/**
	 * Parameters reader class.
	 */
	private interface ParametersReader {
		
		CssTransform read(String text, Obj<Integer> position);
	}

	/**
	 * Construct map.
	 */
	private static final HashMap<String, ParametersReader> parametersReaderMap = new HashMap<String, ParametersReader>();
	
	/**
	 * Static constructor.
	 */
	static {
		
		// Set map.
		setReaderMap();
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssTransformPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}
	
	// $hide<<$

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelTransformFunctions = new JLabel("org.multipage.gui.textCssTransformFunctions");
		springLayout.putConstraint(SpringLayout.NORTH, labelTransformFunctions, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, labelTransformFunctions, 10, SpringLayout.WEST, this);
		add(labelTransformFunctions);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 7, SpringLayout.SOUTH, labelTransformFunctions);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
		add(scrollPane);
		
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onListClicked(e);
			}
		});
		scrollPane.setViewportView(list);
		
		popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);
		
		menuEditTransform = new JMenuItem("org.multipage.gui.menuEditCssTransform");
		menuEditTransform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editTransform();
			}
		});
		popupMenu.add(menuEditTransform);
		
		menuRemoveTransform = new JMenuItem("org.multipage.gui.menuRemoveCssTransform");
		menuRemoveTransform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeTransform();
			}
		});
		popupMenu.add(menuRemoveTransform);
		
		toolBar = new JToolBar();
		springLayout.putConstraint(SpringLayout.EAST, toolBar, 0, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -3, SpringLayout.NORTH, toolBar);
		toolBar.setFloatable(false);
		springLayout.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, labelTransformFunctions);
		springLayout.putConstraint(SpringLayout.SOUTH, toolBar, -10, SpringLayout.SOUTH, this);
		add(toolBar);
		
		buttonMatrix = new JButton("org.multipage.gui.textCssMatrix");
		buttonMatrix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddMatrix();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.WEST, buttonMatrix);
		buttonMatrix.setMargin(new Insets(0, 0, 0, 0));
		buttonMatrix.setPreferredSize(new Dimension(100, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonMatrix, -2, SpringLayout.NORTH, scrollPane);
		springLayout.putConstraint(SpringLayout.EAST, buttonMatrix, -10, SpringLayout.EAST, this);
		add(buttonMatrix);
		
		buttonMatrix3D = new JButton("org.multipage.gui.textCssMatrix3D");
		springLayout.putConstraint(SpringLayout.EAST, buttonMatrix3D, -10, SpringLayout.EAST, this);
		buttonMatrix3D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddMatrix3D();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonMatrix3D, 6, SpringLayout.SOUTH, buttonMatrix);
		buttonMatrix3D.setPreferredSize(new Dimension(100, 25));
		buttonMatrix3D.setMargin(new Insets(0, 0, 0, 0));
		add(buttonMatrix3D);
		
		buttonTranslate = new JButton("org.multipage.gui.textCssTranslate");
		buttonTranslate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddTranslate();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonTranslate, 6, SpringLayout.SOUTH, buttonMatrix3D);
		springLayout.putConstraint(SpringLayout.EAST, buttonTranslate, -10, SpringLayout.EAST, this);
		buttonTranslate.setPreferredSize(new Dimension(100, 25));
		buttonTranslate.setMargin(new Insets(0, 0, 0, 0));
		add(buttonTranslate);
		
		buttonTranslate3D = new JButton("org.multipage.gui.textCssTranslate3D");
		buttonTranslate3D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddTranslate3d();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonTranslate3D, 6, SpringLayout.SOUTH, buttonTranslate);
		springLayout.putConstraint(SpringLayout.EAST, buttonTranslate3D, -10, SpringLayout.EAST, this);
		buttonTranslate3D.setPreferredSize(new Dimension(100, 25));
		buttonTranslate3D.setMargin(new Insets(0, 0, 0, 0));
		add(buttonTranslate3D);
		
		buttonScale = new JButton("org.multipage.gui.textCssScale");
		buttonScale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddScale();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonScale, 6, SpringLayout.SOUTH, buttonTranslate3D);
		springLayout.putConstraint(SpringLayout.EAST, buttonScale, -10, SpringLayout.EAST, this);
		buttonScale.setPreferredSize(new Dimension(100, 25));
		buttonScale.setMargin(new Insets(0, 0, 0, 0));
		add(buttonScale);
		
		buttonScale3D = new JButton("org.multipage.gui.textCssScale3D");
		buttonScale3D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddScale3d();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonScale3D, 6, SpringLayout.SOUTH, buttonScale);
		springLayout.putConstraint(SpringLayout.EAST, buttonScale3D, -10, SpringLayout.EAST, this);
		buttonScale3D.setPreferredSize(new Dimension(100, 25));
		buttonScale3D.setMargin(new Insets(0, 0, 0, 0));
		add(buttonScale3D);
		
		buttonRotate = new JButton("org.multipage.gui.textCssRotate");
		buttonRotate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddRotate();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonRotate, 6, SpringLayout.SOUTH, buttonScale3D);
		springLayout.putConstraint(SpringLayout.EAST, buttonRotate, -10, SpringLayout.EAST, this);
		buttonRotate.setPreferredSize(new Dimension(100, 25));
		buttonRotate.setMargin(new Insets(0, 0, 0, 0));
		add(buttonRotate);
		
		buttonRotate3D = new JButton("org.multipage.gui.textCssRotate3D");
		buttonRotate3D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddRotate3d();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonRotate3D, 6, SpringLayout.SOUTH, buttonRotate);
		springLayout.putConstraint(SpringLayout.EAST, buttonRotate3D, -10, SpringLayout.EAST, this);
		buttonRotate3D.setPreferredSize(new Dimension(100, 25));
		buttonRotate3D.setMargin(new Insets(0, 0, 0, 0));
		add(buttonRotate3D);
		
		buttonSkew = new JButton("org.multipage.gui.textCssSkew");
		buttonSkew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddSkew();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonSkew, 6, SpringLayout.SOUTH, buttonRotate3D);
		springLayout.putConstraint(SpringLayout.EAST, buttonSkew, -10, SpringLayout.EAST, this);
		buttonSkew.setPreferredSize(new Dimension(100, 25));
		buttonSkew.setMargin(new Insets(0, 0, 0, 0));
		add(buttonSkew);
		
		buttonPerspective = new JButton("org.multipage.gui.textCssPerspective");
		buttonPerspective.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddPerspective();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonPerspective, 6, SpringLayout.SOUTH, buttonSkew);
		springLayout.putConstraint(SpringLayout.EAST, buttonPerspective, -10, SpringLayout.EAST, this);
		buttonPerspective.setPreferredSize(new Dimension(100, 25));
		buttonPerspective.setMargin(new Insets(0, 0, 0, 0));
		add(buttonPerspective);
	}

	/**
	 * On add perspective.
	 */
	protected void onAddPerspective() {
		
		CssTransformPerspective perspective = CssTransformPerspectiveDialog.showDialog(this);
		if (perspective == null) {
			return;
		}
		
		model.addElement(perspective);
	}

	/**
	 * On add skew.
	 */
	protected void onAddSkew() {
		
		CssTransformSkew skew = CssTransformSkewDialog.showDialog(this);
		if (skew == null) {
			return;
		}
		
		model.addElement(skew);
	}

	/**
	 * On add rotate 3d.
	 */
	protected void onAddRotate3d() {
		
		CssTransformRotate3d rotate = CssTransformRotate3dDialog.showDialog(this);
		if (rotate == null) {
			return;
		}
		
		model.addElement(rotate);
	}

	/**
	 * On add rotate.
	 */
	protected void onAddRotate() {
		
		CssTransformRotate rotate = CssTransformRotateDialog.showDialog(this);
		if (rotate == null) {
			return;
		}
		
		model.addElement(rotate);
	}

	/**
	 * On add scale 3D.
	 */
	protected void onAddScale3d() {
		
		CssTransformScale3d scale = CssTransformScale3dDialog.showDialog(this);
		if (scale == null) {
			return;
		}
		
		model.addElement(scale);
	}

	/**
	 * On add scale.
	 */
	protected void onAddScale() {
		
		CssTransformScale scale = CssTransformScaleDialog.showDialog(this);
		if (scale == null) {
			return;
		}
		
		model.addElement(scale);
	}

	/**
	 * On add translate 3d.
	 */
	protected void onAddTranslate3d() {
		
		CssTransformTranslate3d translate = CssTransformTranslate3dDialog.showDialog(this);
		if (translate == null) {
			return;
		}
		
		model.addElement(translate);
	}

	/**
	 * On add translate.
	 */
	protected void onAddTranslate() {
		
		CssTransformTranslate translate = CssTransformTranslateDialog.showDialog(this);
		if (translate == null) {
			return;
		}
		
		model.addElement(translate);
	}

	/**
	 * On list clicked.
	 * @param e
	 */
	protected void onListClicked(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			editTransform();
		}
	}

	/**
	 * Edit transform.
	 */
	private void editTransform() {
		
		CssTransform transform = list.getSelectedValue();
		if (transform == null) {
			
			Utility.show(this, "org.multipage.gui.textSelectCssTransform");
			return;
		}
		
		if (transform instanceof CssTransformMatrix) {
			
			if (CssTransformMatrixDialog.editDialog(this, (CssTransformMatrix) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformMatrix3d) {
			
			if (CssTransformMatrix3dDialog.editDialog(this, (CssTransformMatrix3d) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformTranslate) {
			
			if (CssTransformTranslateDialog.editDialog(this, (CssTransformTranslate) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformTranslate3d) {
			
			if (CssTransformTranslate3dDialog.editDialog(this, (CssTransformTranslate3d) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformScale) {
			
			if (CssTransformScaleDialog.editDialog(this, (CssTransformScale) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformScale3d) {
			
			if (CssTransformScale3dDialog.editDialog(this, (CssTransformScale3d) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformRotate) {
			
			if (CssTransformRotateDialog.editDialog(this, (CssTransformRotate) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformRotate3d) {
			
			if (CssTransformRotate3dDialog.editDialog(this, (CssTransformRotate3d) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformSkew) {
			
			if (CssTransformSkewDialog.editDialog(this, (CssTransformSkew) transform)) {
				list.updateUI();
			}
		}
		else if (transform instanceof CssTransformPerspective) {
			
			if (CssTransformPerspectiveDialog.editDialog(this, (CssTransformPerspective) transform)) {
				list.updateUI();
			}
		}
	}
	
	/**
	 * Remove transform.
	 */
	private void removeTransform() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.textSelectCssTransform");
			return;
		}
		
		if (!Utility.ask(this, "org.multipage.gui.messageRemoveCssTransformFromList")) {
			return;
		}
		
		model.remove(index);
	}

	/**
	 * On add matrix.
	 */
	protected void onAddMatrix() {
		
		CssTransformMatrix matrix = CssTransformMatrixDialog.showDialog(this);
		if (matrix == null) {
			return;
		}
		
		model.addElement(matrix);
	}

	/**
	 * On add 3D matrix.
	 */
	protected void onAddMatrix3D() {
		
		CssTransformMatrix3d matrix = CssTransformMatrix3dDialog.showDialog(this);
		if (matrix == null) {
			return;
		}
		
		model.addElement(matrix);
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
		setIcons();
		
		initToolBar();
		initList();
		
		loadDialog();
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		menuEditTransform.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		menuRemoveTransform.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Initialize list.
	 * @param <CssTranform>
	 */
	private <CssTranform> void initList() {
		
		model = new DefaultListModel<CssTransform>();
		list.setModel(model);
		
		// Set cell renderer.
		list.setCellRenderer((ListCellRenderer<? super CssTransform>) new ListCellRenderer<CssTranform>() {
			
			@SuppressWarnings("serial")
			RendererJLabel renderer = new RendererJLabel() {
				{
					setFont(new Font("Tahoma", Font.PLAIN, 20));
					Border paddingBorder = BorderFactory.createEmptyBorder(6, 10, 6, 10);
					setBorder(paddingBorder);
				}
			};
			
			@Override
			public Component getListCellRendererComponent(
					JList<? extends CssTranform> list, CssTranform value,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (value == null) {
					return null;
				}
				
				renderer.setText(value.toString());
				renderer.set(isSelected, cellHasFocus, 0);
				return renderer;
			}
		});
	}

	/**
	 * Initialize tool bar.
	 */
	private void initToolBar() {
		
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/edit.png", this, "editTransform", "org.multipage.gui.tooltipEditCssTransform");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/cancel_icon.png", this, "removeTransform", "org.multipage.gui.tooltipRemoveCssTransform");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_backward.png", this, "onMoveUp", "org.multipage.gui.tooltipMoveTransformUp");
		ToolBarKit.addToolBarButton(toolBar, "org/multipage/gui/images/move_forward.png", this, "onMoveDown", "org.multipage.gui.tooltipMoveTransformDown");

	}
	
	/**
	 * Move transform up.
	 */
	public void onMoveUp() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.textSelectCssTransform");
			return;
		}
		
		// Check selection, move transform up and select it.
		if (index <= 0) {
			return;
		}
		
		CssTransform currentTransform = model.get(index);
		CssTransform previousTransform = model.get(index - 1);
		model.set(index, previousTransform);
		model.set(index - 1, currentTransform);
		
		list.setSelectedIndex(index - 1);
		list.ensureIndexIsVisible(index - 1);
	}
	
	/**
	 * Move transform down.
	 */
	public void onMoveDown() {
		
		int index = list.getSelectedIndex();
		if (index == -1) {
			
			Utility.show(this, "org.multipage.gui.textSelectCssTransform");
			return;
		}
		
		int count = model.getSize();
		
		// Check selection, move transform down and select it.
		if (index >= count - 1) {
			return;
		}
		
		CssTransform currentTransform = model.get(index);
		CssTransform nextTransform = model.get(index + 1);
		model.set(index, nextTransform);
		model.set(index + 1, currentTransform);
		
		list.setSelectedIndex(index + 1);
		list.ensureIndexIsVisible(index + 1);
	}
	
	/**
	 * Get specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		// Get transform list.
		String specification = "";
		
		for (int index = 0; index < model.size(); index++) {
			
			if (index > 0) {
				specification += " ";
			}
			CssTransform transform = model.get(index);
			specification += transform.toString();
		}
		
		return specification;
	}

	/**
	 * Set reader map
	 */
	private static void setReaderMap() {
		
		parametersReaderMap.put("matrix", (String text, Obj<Integer> position)->readerMatrixParameters(text, position));
		parametersReaderMap.put("matrix3d", (String text, Obj<Integer> position)->readerMatrix3dParameters(text, position));
		parametersReaderMap.put("translate", (String text, Obj<Integer> position)->readerTranslateParameters(text, position));
		parametersReaderMap.put("translate3d", (String text, Obj<Integer> position)->readerTranslate3dParameters(text, position));
		parametersReaderMap.put("scale", (String text, Obj<Integer> position)->readerScaleParameters(text, position));
		parametersReaderMap.put("scale3d", (String text, Obj<Integer> position)->readerScale3dParameters(text, position));
		parametersReaderMap.put("rotate", (String text, Obj<Integer> position)->readerRotateParameters(text, position));
		parametersReaderMap.put("rotate3d", (String text, Obj<Integer> position)->readerRotate3dParameters(text, position));
		parametersReaderMap.put("skew", (String text, Obj<Integer> position)->readerSkewParameters(text, position));
		parametersReaderMap.put("perspective", (String text, Obj<Integer> position)->readerPerspectiveParameters(text, position));
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
			
		// Initialize controls.
		model.removeAllElements();

		if (initialString != null) {
			
			Obj<Integer> position = new Obj<Integer>(0);
			
			try {
				
				int length = initialString.length();
				
				// Read all transforms.
				while (position.ref < length) {
					
					// Get transform name.
					String transformName = Utility.getNextMatch(initialString, position, "\\G\\s*(matrix3d|matrix|translate3d|translate|scale3d|scale|rotate3d|rotate|skew|perspective)\\s*(?=\\()");
					if (transformName == null) {
						return;
					}
					transformName = transformName.trim();
					
					// Get and run reader.
					ParametersReader reader = parametersReaderMap.get(transformName);
					if (reader == null) {
						return;
					}
					CssTransform transform = reader.read(initialString, position);
					if (transform == null) {
						return;
					}
					model.addElement(transform);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read matrix parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerMatrixParameters(String initialString, Obj<Integer> position) {
		
		CssTransformMatrix matrix = new CssTransformMatrix();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		
		String text = Utility.getNextMatch(initialString, position,
				String.format("\\G\\s*\\(%s,%s,%s,%s,%s,%s\\)", floatRegex, floatRegex, floatRegex, floatRegex, floatRegex, floatRegex),
				matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		final int floatsCount = 6;
		
		int groupsCount = matcher.ref.groupCount();
		if (groupsCount != floatsCount) {
			return null;
		}
		
		// Convert floats.
		float [] floatNumbers = new float [floatsCount];
		
		for (int index = 0; index < groupsCount; index++) {
			
			String numberText = matcher.ref.group(index + 1);
			try {
				floatNumbers[index] = Float.parseFloat(numberText);
			}
			catch (Exception e) {
			}
		}
		
		// Set values
		matrix.a = floatNumbers[0];
		matrix.b = floatNumbers[1];
		matrix.c = floatNumbers[2];
		matrix.d = floatNumbers[3];
		matrix.tx = floatNumbers[4];
		matrix.ty = floatNumbers[5];
		
		return matrix;
	}

	/**
	 * Read matrix 3d parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerMatrix3dParameters(String initialString, Obj<Integer> position) {
		
		CssTransformMatrix3d matrix = new CssTransformMatrix3d();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		
		String text = Utility.getNextMatch(initialString, position,
				String.format("\\G\\s*\\(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\\)",
						floatRegex, floatRegex, floatRegex, floatRegex,
						floatRegex, floatRegex, floatRegex, floatRegex,
						floatRegex, floatRegex, floatRegex, floatRegex,
						floatRegex, floatRegex, floatRegex, floatRegex
						),
				matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		final int floatsCount = 16;
		
		int groupsCount = matcher.ref.groupCount();
		if (groupsCount != floatsCount) {
			return null;
		}
		
		// Convert floats.
		float [] floatNumbers = new float [floatsCount];
		
		for (int index = 0; index < groupsCount; index++) {
			
			String numberText = matcher.ref.group(index + 1);
			try {
				floatNumbers[index] = Float.parseFloat(numberText);
			}
			catch (Exception e) {
			}
		}
		
		// Set values.
		matrix.a1 = floatNumbers[0];
		matrix.b1 = floatNumbers[1];
		matrix.c1 = floatNumbers[2];
		matrix.d1 = floatNumbers[3];
		matrix.a2 = floatNumbers[4];
		matrix.b2 = floatNumbers[5];
		matrix.c2 = floatNumbers[6];
		matrix.d2 = floatNumbers[7];
		matrix.a3 = floatNumbers[8];
		matrix.b3 = floatNumbers[9];
		matrix.c3 = floatNumbers[10];
		matrix.d3 = floatNumbers[11];
		matrix.a4 = floatNumbers[12];
		matrix.b4 = floatNumbers[13];
		matrix.c4 = floatNumbers[14];
		matrix.d4 = floatNumbers[15];
		
		return matrix;
	}

	/**
	 * Read translate parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerTranslateParameters(String initialString, Obj<Integer> position) {
		
		CssTransformTranslate translate = new CssTransformTranslate();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.lengthUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s%s,%s%s\\)", floatRegex, unitsRegex, floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 4) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			translate.tx = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		translate.txUnits = matcher.ref.group(2).trim();
		
		text = matcher.ref.group(3).trim();
		try {
			translate.ty = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		translate.tyUnits = matcher.ref.group(4).trim();
		
		return translate;
	}

	/**
	 * Read translate 3d parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerTranslate3dParameters(String initialString, Obj<Integer> position) {
		
		CssTransformTranslate3d translate = new CssTransformTranslate3d();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.lengthUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s%s,%s%s,%s%s\\)", floatRegex, unitsRegex, floatRegex, unitsRegex, floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 6) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			translate.tx = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		translate.txUnits = matcher.ref.group(2).trim();
		
		text = matcher.ref.group(3).trim();
		try {
			translate.ty = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		translate.tyUnits = matcher.ref.group(4).trim();
		
		text = matcher.ref.group(5).trim();
		try {
			translate.tz = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		translate.tzUnits = matcher.ref.group(6).trim();
		
		return translate;
	}

	/**
	 * Read scale parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerScaleParameters(String initialString, Obj<Integer> position) {
		
		CssTransformScale scale = new CssTransformScale();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		
		String regex = String.format("\\G\\s*\\(%s,%s\\)", floatRegex, floatRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 2) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			scale.sx = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		text = matcher.ref.group(2).trim();
		try {
			scale.sy = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		return scale;
	}

	/**
	 * Read scale 3s parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerScale3dParameters(String initialString, Obj<Integer> position) {
		
		CssTransformScale3d scale = new CssTransformScale3d();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		
		String regex = String.format("\\G\\s*\\(%s,%s,%s\\)", floatRegex, floatRegex, floatRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 3) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			scale.sx = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		text = matcher.ref.group(2).trim();
		try {
			scale.sy = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		text = matcher.ref.group(3).trim();
		try {
			scale.sz = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		return scale;
	}

	/**
	 * Read rotate parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerRotateParameters(String initialString, Obj<Integer> position) {
		
		CssTransformRotate rotate = new CssTransformRotate();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.angleUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s%s\\)", floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 2) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			rotate.a = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		rotate.units = matcher.ref.group(2).trim();
		
		return rotate;
	}

	/**
	 * Read rotate 3s parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerRotate3dParameters(String initialString, Obj<Integer> position) {
		
		CssTransformRotate3d rotate = new CssTransformRotate3d();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.angleUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s,%s,%s,%s%s\\)", floatRegex, floatRegex, floatRegex, floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 5) {
			return null;
		}

		text = matcher.ref.group(1).trim();
		try {
			rotate.x = Float.parseFloat(text);
		}
		catch (Exception e) {
		}

		text = matcher.ref.group(2).trim();
		try {
			rotate.y = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		text = matcher.ref.group(3).trim();
		try {
			rotate.z = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		text = matcher.ref.group(4).trim();
		try {
			rotate.a = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		rotate.aUnits = matcher.ref.group(5).trim();
		
		return rotate;
	}

	/**
	 * Read skew parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerSkewParameters(String initialString, Obj<Integer> position) {
		
		CssTransformSkew skew = new CssTransformSkew();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.angleUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s%s,%s%s\\)", floatRegex, unitsRegex, floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 4) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			skew.ax = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		skew.axUnits = matcher.ref.group(2).trim();

		text = matcher.ref.group(3).trim();
		try {
			skew.ay = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		skew.ayUnits = matcher.ref.group(4).trim();
		
		return skew;
	}

	/**
	 * Read perspective parameters.
	 * @param initialString
	 * @param position
	 * @return
	 */
	private static CssTransform readerPerspectiveParameters(String initialString, Obj<Integer> position) {

		CssTransformPerspective perspective = new CssTransformPerspective();
		
		Obj<Matcher> matcher = new Obj<Matcher>();
		
		final String floatRegex = Utility.floatGroupRegex;
		final String unitsRegex = Utility.lengthUnitsRegex;
		
		String regex = String.format("\\G\\s*\\(%s%s\\)", floatRegex, unitsRegex);
		String text = Utility.getNextMatch(initialString, position, regex, matcher);
		
		if (text == null || matcher.ref == null) {
			return null;
		}
		
		if (matcher.ref.groupCount() != 2) {
			return null;
		}
		
		text = matcher.ref.group(1).trim();
		try {
			perspective.l = Float.parseFloat(text);
		}
		catch (Exception e) {
		}
		
		perspective.units = matcher.ref.group(2).trim();
		
		return perspective;
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelTransformFunctions);
		Utility.localize(buttonMatrix);
		Utility.localize(buttonMatrix3D);
		Utility.localize(buttonTranslate);
		Utility.localize(buttonTranslate3D);
		Utility.localize(buttonScale);
		Utility.localize(buttonScale3D);
		Utility.localize(buttonRotate);
		Utility.localize(buttonRotate3D);
		Utility.localize(buttonSkew);
		Utility.localize(buttonPerspective);
		Utility.localize(menuEditTransform);
		Utility.localize(menuRemoveTransform);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssTransformBuilder");
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
		
		CssTransformPanel.bounds = bounds;
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
		
		return meansCssTransform;
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
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
}
