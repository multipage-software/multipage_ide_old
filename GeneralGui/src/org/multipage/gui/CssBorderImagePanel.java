/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;

import org.multipage.util.Obj;
import org.multipage.util.Resources;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author
 *
 */
public class CssBorderImagePanel extends InsertPanel implements StringValueEditor {

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
		
		bounds =  new Rectangle(0, 0, 500, 500);
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
	 * Get resource name callback.
	 */
	private Callback getResourceName;
	
	/**
	 * Initial string.
	 */
	private String initialString;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JLabel labelImageName;
	private JTextField textImageName;
	private JButton buttonGetResources;
	private JComboBox comboSlice;
	private JLabel labelSlice;
	private JTextField textSliceTop;
	private JComboBox comboSliceTopUnits;
	private TextFieldEx textSliceLeft;
	private JComboBox comboSliceLeftUnits;
	private TextFieldEx textSliceRight;
	private JComboBox comboSliceRightUnits;
	private TextFieldEx textSliceBottom;
	private JComboBox comboSliceBottomUnits;
	private JLabel labelSliceTop;
	private JLabel labelSliceRight;
	private JLabel labelSliceBottom;
	private JLabel labelSliceLeft;
	private JLabel labelWidth;
	private JLabel labelWidthTop;
	private TextFieldEx textWidthTop;
	private JComboBox comboWidthTopUnits;
	private JLabel labelWidthRight;
	private TextFieldEx textWidthRight;
	private JComboBox comboWidthRightUnits;
	private TextFieldEx textWidthBottom;
	private JLabel labelWidthBottom;
	private JComboBox comboWidthBottomUnits;
	private TextFieldEx textWidthLeft;
	private JLabel labelWidthLeft;
	private JComboBox comboWidthLeftUnits;
	private JSeparator separator1;
	private JSeparator separator2;
	private JLabel labelOutset;
	private TextFieldEx textOutsetTop;
	private JLabel labelOutsetTop;
	private JComboBox comboOutsetTopUnits;
	private TextFieldEx textOutsetRight;
	private JLabel labelOutsetRight;
	private JComboBox comboOutsetRightUnits;
	private TextFieldEx textOutsetBottom;
	private JLabel labelOutsetBottom;
	private JComboBox comboOutsetBottomUnits;
	private TextFieldEx textOutsetLeft;
	private JLabel labelOutsetLeft;
	private JComboBox comboOutsetLeftUnits;
	private JSeparator separator3;
	private JLabel labelRepeatHorizontal;
	private JComboBox comboRepeatHorizontal;
	private JLabel labelRepeatVertical;
	private JComboBox comboRepeatVertical;

	/**
	 * Create the panel.
	 * @param string 
	 */
	public CssBorderImagePanel(String string) {

		initComponents();
		
		// $hide>>$
		postCreate();
		
		if (string != null) {
			initialString = string;
			setFromInitialString();
		}
		// $hide<<$
	}
	
	/**
	 * Set callback.
	 * @param callback
	 */
	public void setResourceNameCallback(Callback callback) {
		
		getResourceName = callback;
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		SpringLayout springLayout = new SpringLayout();
		setLayout(springLayout);
		
		labelImageName = new JLabel("org.multipage.gui.textBorderImageName");
		springLayout.putConstraint(SpringLayout.WEST, labelImageName, 10, SpringLayout.WEST, this);
		labelImageName.setFont(new Font("Tahoma", Font.BOLD, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelImageName, 41, SpringLayout.NORTH, this);
		add(labelImageName);
		
		textImageName = new TextFieldEx();
		textImageName.setPreferredSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textImageName, -3, SpringLayout.NORTH, labelImageName);
		springLayout.putConstraint(SpringLayout.WEST, textImageName, 6, SpringLayout.EAST, labelImageName);
		add(textImageName);
		textImageName.setColumns(20);
		
		buttonGetResources = new JButton("");
		springLayout.putConstraint(SpringLayout.WEST, buttonGetResources, 3, SpringLayout.EAST, textImageName);
		buttonGetResources.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindResource();
			}
		});
		buttonGetResources.setMargin(new Insets(0, 0, 0, 0));
		buttonGetResources.setPreferredSize(new Dimension(22, 22));
		springLayout.putConstraint(SpringLayout.NORTH, buttonGetResources, 0, SpringLayout.NORTH, textImageName);
		add(buttonGetResources);
		
		comboSlice = new JComboBox();
		comboSlice.setPreferredSize(new Dimension(100, 20));
		add(comboSlice);
		
		labelSlice = new JLabel("org.multipage.gui.textSlice");
		labelSlice.setFont(new Font("Tahoma", Font.BOLD, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelSlice, 33, SpringLayout.SOUTH, labelImageName);
		springLayout.putConstraint(SpringLayout.NORTH, comboSlice, -3, SpringLayout.NORTH, labelSlice);
		springLayout.putConstraint(SpringLayout.WEST, comboSlice, 6, SpringLayout.EAST, labelSlice);
		springLayout.putConstraint(SpringLayout.WEST, labelSlice, 10, SpringLayout.WEST, this);
		add(labelSlice);
		
		textSliceTop = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textSliceTop, -1, SpringLayout.NORTH, comboSlice);
		textSliceTop.setPreferredSize(new Dimension(6, 22));
		add(textSliceTop);
		textSliceTop.setColumns(6);
		
		comboSliceTopUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboSliceTopUnits, 0, SpringLayout.NORTH, textSliceTop);
		springLayout.putConstraint(SpringLayout.WEST, comboSliceTopUnits, 0, SpringLayout.EAST, textSliceTop);
		add(comboSliceTopUnits);
		
		textSliceLeft = new TextFieldEx();
		textSliceLeft.setPreferredSize(new Dimension(6, 22));
		textSliceLeft.setColumns(6);
		add(textSliceLeft);
		
		comboSliceLeftUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboSliceLeftUnits, 0, SpringLayout.NORTH, textSliceLeft);
		springLayout.putConstraint(SpringLayout.WEST, comboSliceLeftUnits, 0, SpringLayout.EAST, textSliceLeft);
		add(comboSliceLeftUnits);
		
		textSliceRight = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textSliceRight, 0, SpringLayout.NORTH, textSliceTop);
		springLayout.putConstraint(SpringLayout.WEST, textSliceLeft, 0, SpringLayout.WEST, textSliceRight);
		textSliceRight.setPreferredSize(new Dimension(6, 22));
		textSliceRight.setColumns(6);
		add(textSliceRight);
		
		comboSliceRightUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboSliceRightUnits, 0, SpringLayout.NORTH, textSliceRight);
		springLayout.putConstraint(SpringLayout.WEST, comboSliceRightUnits, 0, SpringLayout.EAST, textSliceRight);
		add(comboSliceRightUnits);
		
		textSliceBottom = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textSliceLeft, 0, SpringLayout.NORTH, textSliceBottom);
		springLayout.putConstraint(SpringLayout.NORTH, textSliceBottom, 31, SpringLayout.SOUTH, textSliceTop);
		springLayout.putConstraint(SpringLayout.WEST, textSliceBottom, 0, SpringLayout.WEST, textSliceTop);
		textSliceBottom.setPreferredSize(new Dimension(6, 22));
		textSliceBottom.setColumns(6);
		add(textSliceBottom);
		
		comboSliceBottomUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboSliceBottomUnits, 0, SpringLayout.NORTH, textSliceBottom);
		springLayout.putConstraint(SpringLayout.WEST, comboSliceBottomUnits, 0, SpringLayout.WEST, comboSliceTopUnits);
		add(comboSliceBottomUnits);
		
		labelSliceTop = new JLabel("org.multipage.gui.textTop");
		springLayout.putConstraint(SpringLayout.WEST, labelSliceTop, 30, SpringLayout.EAST, comboSlice);
		springLayout.putConstraint(SpringLayout.WEST, textSliceTop, 6, SpringLayout.EAST, labelSliceTop);
		springLayout.putConstraint(SpringLayout.NORTH, labelSliceTop, 3, SpringLayout.NORTH, comboSlice);
		add(labelSliceTop);
		
		labelSliceRight = new JLabel("org.multipage.gui.textRight");
		springLayout.putConstraint(SpringLayout.WEST, labelSliceRight, 20, SpringLayout.EAST, comboSliceTopUnits);
		springLayout.putConstraint(SpringLayout.WEST, textSliceRight, 6, SpringLayout.EAST, labelSliceRight);
		springLayout.putConstraint(SpringLayout.NORTH, labelSliceRight, 3, SpringLayout.NORTH, comboSlice);
		add(labelSliceRight);
		
		labelSliceBottom = new JLabel("org.multipage.gui.textBottom");
		springLayout.putConstraint(SpringLayout.NORTH, labelSliceBottom, 4, SpringLayout.NORTH, textSliceBottom);
		springLayout.putConstraint(SpringLayout.EAST, labelSliceBottom, -6, SpringLayout.WEST, textSliceBottom);
		add(labelSliceBottom);
		
		labelSliceLeft = new JLabel("org.multipage.gui.textLeft");
		springLayout.putConstraint(SpringLayout.NORTH, labelSliceLeft, 0, SpringLayout.NORTH, textSliceLeft);
		springLayout.putConstraint(SpringLayout.EAST, labelSliceLeft, -6, SpringLayout.WEST, textSliceLeft);
		add(labelSliceLeft);
		
		labelWidth = new JLabel("org.multipage.gui.textBorderImageWidth");
		labelWidth.setFont(new Font("Tahoma", Font.BOLD, 11));
		springLayout.putConstraint(SpringLayout.EAST, labelWidth, 0, SpringLayout.EAST, comboSlice);
		add(labelWidth);
		
		labelWidthTop = new JLabel("org.multipage.gui.textTop");
		springLayout.putConstraint(SpringLayout.NORTH, labelWidthTop, 0, SpringLayout.NORTH, labelWidth);
		springLayout.putConstraint(SpringLayout.WEST, labelWidthTop, 0, SpringLayout.WEST, labelSliceTop);
		add(labelWidthTop);
		
		textWidthTop = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWidthTop, 0, SpringLayout.NORTH, labelWidthTop);
		springLayout.putConstraint(SpringLayout.WEST, textWidthTop, 0, SpringLayout.WEST, textSliceTop);
		textWidthTop.setPreferredSize(new Dimension(6, 22));
		textWidthTop.setColumns(6);
		add(textWidthTop);
		
		comboWidthTopUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboWidthTopUnits, 0, SpringLayout.NORTH, textWidthTop);
		springLayout.putConstraint(SpringLayout.WEST, comboWidthTopUnits, 0, SpringLayout.EAST, textWidthTop);
		add(comboWidthTopUnits);
		
		labelWidthRight = new JLabel("org.multipage.gui.textRight");
		springLayout.putConstraint(SpringLayout.NORTH, labelWidthRight, 0, SpringLayout.NORTH, labelWidth);
		springLayout.putConstraint(SpringLayout.WEST, labelWidthRight, 0, SpringLayout.WEST, labelSliceRight);
		add(labelWidthRight);
		
		textWidthRight = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWidthRight, 0, SpringLayout.NORTH, labelWidth);
		springLayout.putConstraint(SpringLayout.WEST, textWidthRight, 0, SpringLayout.WEST, textSliceLeft);
		textWidthRight.setPreferredSize(new Dimension(6, 22));
		textWidthRight.setColumns(6);
		add(textWidthRight);
		
		comboWidthRightUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboWidthRightUnits, 0, SpringLayout.NORTH, textWidthRight);
		springLayout.putConstraint(SpringLayout.WEST, comboWidthRightUnits, 0, SpringLayout.WEST, comboSliceLeftUnits);
		add(comboWidthRightUnits);
		
		textWidthBottom = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWidthBottom, 25, SpringLayout.SOUTH, textWidthTop);
		springLayout.putConstraint(SpringLayout.WEST, textWidthBottom, 0, SpringLayout.WEST, textSliceTop);
		textWidthBottom.setPreferredSize(new Dimension(6, 22));
		textWidthBottom.setColumns(6);
		add(textWidthBottom);
		
		labelWidthBottom = new JLabel("org.multipage.gui.textBottom");
		springLayout.putConstraint(SpringLayout.NORTH, labelWidthBottom, 0, SpringLayout.NORTH, textWidthBottom);
		springLayout.putConstraint(SpringLayout.EAST, labelWidthBottom, 0, SpringLayout.EAST, labelSliceTop);
		add(labelWidthBottom);
		
		comboWidthBottomUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboWidthBottomUnits, 0, SpringLayout.NORTH, textWidthBottom);
		springLayout.putConstraint(SpringLayout.WEST, comboWidthBottomUnits, 0, SpringLayout.WEST, comboSliceTopUnits);
		add(comboWidthBottomUnits);
		
		textWidthLeft = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textWidthLeft, 0, SpringLayout.NORTH, textWidthBottom);
		springLayout.putConstraint(SpringLayout.WEST, textWidthLeft, 0, SpringLayout.WEST, textSliceLeft);
		textWidthLeft.setPreferredSize(new Dimension(6, 22));
		textWidthLeft.setColumns(6);
		add(textWidthLeft);
		
		labelWidthLeft = new JLabel("org.multipage.gui.textLeft");
		springLayout.putConstraint(SpringLayout.NORTH, labelWidthLeft, 0, SpringLayout.NORTH, textWidthBottom);
		springLayout.putConstraint(SpringLayout.EAST, labelWidthLeft, 0, SpringLayout.EAST, labelSliceRight);
		add(labelWidthLeft);
		
		comboWidthLeftUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboWidthLeftUnits, 0, SpringLayout.NORTH, textWidthLeft);
		springLayout.putConstraint(SpringLayout.WEST, comboWidthLeftUnits, 0, SpringLayout.WEST, comboSliceLeftUnits);
		add(comboWidthLeftUnits);
		
		separator1 = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, labelWidth, 20, SpringLayout.SOUTH, separator1);
		springLayout.putConstraint(SpringLayout.NORTH, separator1, 20, SpringLayout.SOUTH, labelSliceBottom);
		springLayout.putConstraint(SpringLayout.WEST, separator1, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, separator1, -10, SpringLayout.EAST, this);
		add(separator1);
		
		separator2 = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, separator2, 20, SpringLayout.SOUTH, textWidthBottom);
		springLayout.putConstraint(SpringLayout.WEST, separator2, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, separator2, -10, SpringLayout.EAST, this);
		add(separator2);
		
		labelOutset = new JLabel("org.multipage.gui.textBorderImageOutset");
		labelOutset.setFont(new Font("Tahoma", Font.BOLD, 11));
		springLayout.putConstraint(SpringLayout.NORTH, labelOutset, 20, SpringLayout.SOUTH, separator2);
		springLayout.putConstraint(SpringLayout.EAST, labelOutset, 0, SpringLayout.EAST, comboSlice);
		add(labelOutset);
		
		textOutsetTop = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textOutsetTop, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.WEST, textOutsetTop, 0, SpringLayout.WEST, textSliceTop);
		textOutsetTop.setPreferredSize(new Dimension(6, 22));
		textOutsetTop.setColumns(6);
		add(textOutsetTop);
		
		labelOutsetTop = new JLabel("org.multipage.gui.textTop");
		springLayout.putConstraint(SpringLayout.NORTH, labelOutsetTop, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.EAST, labelOutsetTop, 0, SpringLayout.EAST, labelSliceTop);
		add(labelOutsetTop);
		
		comboOutsetTopUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboOutsetTopUnits, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.WEST, comboOutsetTopUnits, 0, SpringLayout.WEST, comboSliceTopUnits);
		add(comboOutsetTopUnits);
		
		textOutsetRight = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textOutsetRight, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.WEST, textOutsetRight, 0, SpringLayout.WEST, textSliceLeft);
		textOutsetRight.setPreferredSize(new Dimension(6, 22));
		textOutsetRight.setColumns(6);
		add(textOutsetRight);
		
		labelOutsetRight = new JLabel("org.multipage.gui.textRight");
		springLayout.putConstraint(SpringLayout.NORTH, labelOutsetRight, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.EAST, labelOutsetRight, 0, SpringLayout.EAST, labelSliceRight);
		add(labelOutsetRight);
		
		comboOutsetRightUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboOutsetRightUnits, 0, SpringLayout.NORTH, labelOutset);
		springLayout.putConstraint(SpringLayout.WEST, comboOutsetRightUnits, 0, SpringLayout.WEST, comboSliceLeftUnits);
		add(comboOutsetRightUnits);
		
		textOutsetBottom = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textOutsetBottom, 26, SpringLayout.SOUTH, textOutsetTop);
		springLayout.putConstraint(SpringLayout.WEST, textOutsetBottom, 0, SpringLayout.WEST, textSliceTop);
		textOutsetBottom.setPreferredSize(new Dimension(6, 22));
		textOutsetBottom.setColumns(6);
		add(textOutsetBottom);
		
		labelOutsetBottom = new JLabel("org.multipage.gui.textBottom");
		springLayout.putConstraint(SpringLayout.NORTH, labelOutsetBottom, 0, SpringLayout.NORTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.EAST, labelOutsetBottom, 0, SpringLayout.EAST, labelSliceTop);
		add(labelOutsetBottom);
		
		comboOutsetBottomUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboOutsetBottomUnits, 0, SpringLayout.NORTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.WEST, comboOutsetBottomUnits, 0, SpringLayout.WEST, comboSliceTopUnits);
		add(comboOutsetBottomUnits);
		
		textOutsetLeft = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textOutsetLeft, 0, SpringLayout.NORTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.WEST, textOutsetLeft, 0, SpringLayout.WEST, textSliceLeft);
		textOutsetLeft.setPreferredSize(new Dimension(6, 22));
		textOutsetLeft.setColumns(6);
		add(textOutsetLeft);
		
		labelOutsetLeft = new JLabel("org.multipage.gui.textLeft");
		springLayout.putConstraint(SpringLayout.NORTH, labelOutsetLeft, 0, SpringLayout.NORTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.EAST, labelOutsetLeft, 0, SpringLayout.EAST, labelSliceRight);
		add(labelOutsetLeft);
		
		comboOutsetLeftUnits = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboOutsetLeftUnits, 0, SpringLayout.NORTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.WEST, comboOutsetLeftUnits, 0, SpringLayout.WEST, comboSliceLeftUnits);
		add(comboOutsetLeftUnits);
		
		separator3 = new JSeparator();
		springLayout.putConstraint(SpringLayout.NORTH, separator3, 20, SpringLayout.SOUTH, textOutsetBottom);
		springLayout.putConstraint(SpringLayout.WEST, separator3, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, separator3, -10, SpringLayout.EAST, this);
		add(separator3);
		
		labelRepeatHorizontal = new JLabel("org.multipage.gui.textBorderImageRepeatHorizontal");
		springLayout.putConstraint(SpringLayout.NORTH, labelRepeatHorizontal, 20, SpringLayout.SOUTH, separator3);
		springLayout.putConstraint(SpringLayout.WEST, labelRepeatHorizontal, 0, SpringLayout.WEST, labelSlice);
		labelRepeatHorizontal.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(labelRepeatHorizontal);
		
		comboRepeatHorizontal = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboRepeatHorizontal, 0, SpringLayout.NORTH, labelRepeatHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, comboRepeatHorizontal, 6, SpringLayout.EAST, labelRepeatHorizontal);
		comboRepeatHorizontal.setPreferredSize(new Dimension(100, 20));
		add(comboRepeatHorizontal);
		
		labelRepeatVertical = new JLabel("org.multipage.gui.textBorderImageRepeatVertical");
		springLayout.putConstraint(SpringLayout.NORTH, labelRepeatVertical, 0, SpringLayout.NORTH, labelRepeatHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, labelRepeatVertical, 20, SpringLayout.EAST, comboRepeatHorizontal);
		labelRepeatVertical.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(labelRepeatVertical);
		
		comboRepeatVertical = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboRepeatVertical, 0, SpringLayout.NORTH, comboRepeatHorizontal);
		springLayout.putConstraint(SpringLayout.WEST, comboRepeatVertical, 6, SpringLayout.EAST, labelRepeatVertical);
		comboRepeatVertical.setPreferredSize(new Dimension(100, 20));
		add(comboRepeatVertical);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		
		loadEnumerations();
		loadUnits();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(labelImageName);
		Utility.localize(labelSlice);
		Utility.localize(labelSliceBottom);
		Utility.localize(labelSliceLeft);
		Utility.localize(labelSliceRight);
		Utility.localize(labelSliceTop);
		Utility.localize(labelWidth);
		Utility.localize(labelWidthBottom);
		Utility.localize(labelWidthLeft);
		Utility.localize(labelWidthRight);
		Utility.localize(labelWidthTop);
		Utility.localize(labelOutset);
		Utility.localize(labelOutsetBottom);
		Utility.localize(labelOutsetLeft);
		Utility.localize(labelOutsetRight);
		Utility.localize(labelOutsetTop);
		Utility.localize(labelRepeatHorizontal);
		Utility.localize(labelRepeatVertical);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonGetResources.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
	}

	/**
	 * Load enumerations.
	 */
	private void loadEnumerations() {
		
		Utility.loadEmptyItem(comboSlice);
		Utility.loadNamedItems(comboSlice, new String [][] {
				{"fill", "org.multipage.gui.textCssBorderImageSliceFill"}
				});
		
		Utility.loadNamedItems(comboRepeatVertical, new String [][] {
				{"stretch", "org.multipage.gui.textCssBorderImageStretch"},
				{"repeat", "org.multipage.gui.textCssBorderImageRepeat"},
				{"round", "org.multipage.gui.textCssBorderImageRound"},
				{"space", "org.multipage.gui.textCssBorderImageSpace"}
				});
		
		Utility.loadNamedItems(comboRepeatHorizontal, new String [][] {
				{"stretch", "org.multipage.gui.textCssBorderImageStretch"},
				{"repeat", "org.multipage.gui.textCssBorderImageRepeat"},
				{"round", "org.multipage.gui.textCssBorderImageRound"},
				{"space", "org.multipage.gui.textCssBorderImageSpace"}
				});
	}
	
	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		comboSliceTopUnits.addItem(" ");
		comboSliceBottomUnits.addItem(" ");
		comboSliceLeftUnits.addItem(" ");
		comboSliceRightUnits.addItem(" ");
		
		comboSliceTopUnits.addItem("%");
		comboSliceBottomUnits.addItem("%");
		comboSliceLeftUnits.addItem("%");
		comboSliceRightUnits.addItem("%");
		
		Utility.loadCssUnits(comboWidthTopUnits);
		Utility.loadCssUnits(comboWidthRightUnits);
		Utility.loadCssUnits(comboWidthBottomUnits);
		Utility.loadCssUnits(comboWidthLeftUnits);
		
		Utility.loadCssUnits(comboOutsetTopUnits);
		Utility.loadCssUnits(comboOutsetRightUnits);
		Utility.loadCssUnits(comboOutsetBottomUnits);
		Utility.loadCssUnits(comboOutsetLeftUnits);
	}

	/**
	 * On find resource.
	 */
	protected void onFindResource() {
		
		if (getResourceName == null) {
			
			Utility.show(this, "org.multipage.gui.messageNoResourcesAssociated");
			return;
		}
		
		// Use callback to obtain resource name.
		Object outputValue = getResourceName.run(null);
		if (!(outputValue instanceof String)) {
			return;
		}
		
		String imageName = (String) outputValue;
		
		// Set image name text control.
		textImageName.setText(imageName);
	}

	/**
	 * Get specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		String imageName = textImageName.getText();
		if (imageName.isEmpty()) {
			return "none 100% 1 0s stretch";
		}
		
		String specification = String.format("url(\"[@URL thisArea, res=\"#%s\"]\")", imageName);
		
		specification += " " + Utility.getCssValueAndUnits(textSliceTop, comboSliceTopUnits, "100%");
		specification += " " + Utility.getCssValueAndUnits(textSliceRight, comboSliceRightUnits, "100%");
		specification += " " + Utility.getCssValueAndUnits(textSliceBottom, comboSliceBottomUnits, "100%");
		specification += " " + Utility.getCssValueAndUnits(textSliceLeft, comboSliceLeftUnits, "100%");
		specification += " " + Utility.getSelectedNamedItem(comboSlice);
		
		specification += "/" + Utility.getCssValueAndUnits(textWidthTop, comboWidthTopUnits, "auto");
		specification += " " + Utility.getCssValueAndUnits(textWidthRight, comboWidthRightUnits, "auto");
		specification += " " + Utility.getCssValueAndUnits(textWidthBottom, comboWidthBottomUnits, "auto");
		specification += " " + Utility.getCssValueAndUnits(textWidthLeft, comboWidthLeftUnits, "auto");
		
		specification += "/" + Utility.getCssValueAndUnits(textOutsetTop, comboOutsetTopUnits, "0");
		specification += " " + Utility.getCssValueAndUnits(textOutsetRight, comboOutsetRightUnits, "0");
		specification += " " + Utility.getCssValueAndUnits(textOutsetBottom, comboOutsetBottomUnits, "0");
		specification += " " + Utility.getCssValueAndUnits(textOutsetLeft, comboOutsetLeftUnits, "0");
		
		specification += " " + Utility.getSelectedNamedItem(comboRepeatHorizontal);
		specification += " " + Utility.getSelectedNamedItem(comboRepeatVertical);
		
		return specification;
	}

	/**
	 * Set components from initial string.
	 */
	private void setFromInitialString() {
		
		clear();
		
		if (initialString != null) {
			
			try {
				
				Obj<Integer> position = new Obj<Integer>(0);

				// Get image name.
				String imageName = getImageName(position);
				if (imageName == null) {
					return;
				}
				
				textImageName.setText(imageName);
				
				// Parse slice definition.
				if (!parseSliceTextSetControls(position)) {
					return;
				}
				
				// Get next backslash.
				String nextMatch = Utility.getNextMatch(initialString, position, "/");
				if (!nextMatch.equals("/")) {
					return;
				}
				
				// Parse widths.
				if (!parseWidthTextSetControls(position)) {
					return;
				}
				
				// Get next backslash.
				nextMatch = Utility.getNextMatch(initialString, position, "/");
				if (!nextMatch.equals("/")) {
					return;
				}
				
				// Parse widths.
				if (!parseOutsetTextSetControls(position)) {
					return;
				}
				
				// Parse repeat.
				parseRepeatTextSetControls(position);
			}
			catch (Exception e) {
				
			}
		}
	}

	/**
	 * Parse repeat text and set controls.
	 * @param position
	 */
	private boolean parseRepeatTextSetControls(Obj<Integer> position) {

		// Enumerate controls.
		final JComboBox [] comboBoxes = {comboRepeatHorizontal, comboRepeatVertical};
		
		for (int index = 0; index < 2; index++) {
			
			// Get repeat value.
			String repeatValue = Utility.getNextMatch(initialString, position, "[\\w]+");
			if (!checkRepeatValue(repeatValue)) {
				return false;
			}
			
			Utility.selectComboNamedItem(comboBoxes[index], repeatValue);
		}
		
		return true;
	}

	/**
	 * Check repeat value.
	 * @param repeatValue
	 * @return
	 */
	private boolean checkRepeatValue(String repeatValue) {
		
		final String [] repeatValues = {"stretch", "repeat", "round", "space"};
		
		// Try to find repeat value.
		for (String acceptedValue : repeatValues) {
			
			if (repeatValue.equals(acceptedValue)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Parse outset text and set controls.
	 * @param position
	 * @return
	 */
	private boolean parseOutsetTextSetControls(Obj<Integer> position) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		// Enumerate outset controls.
		final JTextField [] textFields = {textOutsetTop, textOutsetRight, textOutsetBottom, textOutsetLeft};
		final JComboBox [] comboBoxes = {comboOutsetTopUnits, comboOutsetRightUnits, comboOutsetBottomUnits, comboOutsetLeftUnits};
		
		for (int index = 0; index < 4; index++) {
			
			// Get outset value.
			String numberUnits = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%-]+");
			if (numberUnits == null) {
				return false;
			}
			
			// Convert string to number and units.
			if (!Utility.convertCssStringToNumberUnit(numberUnits, number, unit)) {
				return false;
			}
			
			// Set appropriate controls.
			textFields[index].setText(number.ref);
			Utility.selectComboItem(comboBoxes[index], unit.ref);
		}
		
		return true;
	}

	/**
	 * Parse widths and set controls.
	 * @param position
	 * @return
	 */
	private boolean parseWidthTextSetControls(Obj<Integer> position) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		// Enumerate width controls.
		final JTextField [] textFields = {textWidthTop, textWidthRight, textWidthBottom, textWidthLeft};
		final JComboBox [] comboBoxes = {comboWidthTopUnits, comboWidthRightUnits, comboWidthBottomUnits, comboWidthLeftUnits};
		
		for (int index = 0; index < 4; index++) {
			
			// Try to get auto flag.
			int savedPosition = position.ref;
			String nextMatch = Utility.getNextMatch(initialString, position, "[\\w]+");
			if (nextMatch.equals("auto")) {
				
				textFields[index].setText("");
				Utility.selectComboItem(comboBoxes[index], "");
				continue;
			}
			else {
				position.ref = savedPosition;
			}
					
			// Get width value.
			String numberUnits = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%-]+");
			if (numberUnits == null) {
				return false;
			}
			
			// Convert string to number and units.
			if (!Utility.convertCssStringToNumberUnit(numberUnits, number, unit)) {
				return false;
			}
			
			// Set appropriate controls.
			textFields[index].setText(number.ref);
			Utility.selectComboItem(comboBoxes[index], unit.ref);
		}

		return true;
	}

	/**
	 * Parse slice text and set controls.
	 * @param position
	 * @return
	 */
	private boolean parseSliceTextSetControls(Obj<Integer> position) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		// Enumerate slice controls.
		final JTextField [] textFields = {textSliceTop, textSliceRight, textSliceBottom, textSliceLeft};
		final JComboBox [] comboBoxes = {comboSliceTopUnits, comboSliceRightUnits, comboSliceBottomUnits, comboSliceLeftUnits};
		
		for (int index = 0; index < 4; index++) {
			
			// Get number and units.
			String numberUnits = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%-]+");
			if (numberUnits == null) {
				return false;
			}
			
			// Convert string to number and units.
			if (!Utility.convertCssStringToNumberUnit(numberUnits, number, unit)) {
				return false;
			}
			
			// Set appropriate controls.
			textFields[index].setText(number.ref);
			Utility.selectComboItem(comboBoxes[index], unit.ref);
		}
		
		// Get fill flag.
		int savedPosition = position.ref;
		String nextMatch = Utility.getNextMatch(initialString, position, "[\\w]+");
		if (nextMatch.equals("fill")) {
			
			Utility.selectComboNamedItem(comboSlice, "fill");
		}
		else {
			position.ref = savedPosition;
		}
		
		return true;
	}

	/**
	 * Get image name.
	 * @param position
	 * @return
	 */
	private String getImageName(Obj<Integer> position) {
		
		// Get next match.
		String url = Utility.getNextMatch(initialString, position, "url");
		if (url == null) {
			return null;
		}
		
		// Get opening parenthesis.
		String leftParenthesis = Utility.getNextMatch(initialString, position, "\\(");
		if (leftParenthesis == null) {
			return null;
		}
		
		String imageName = null;
		
		// Get name start.
		String nameStart = Utility.getNextMatch(initialString, position, "res=\"#");
		if (nameStart != null) {
			
			// Get image name.
			imageName = Utility.getNextMatch(initialString, position, "[^\\\"]*");
		}
		
		// Get closing parenthesis.
		String rightParenthesis = Utility.getNextMatch(initialString, position, "\\)");
		if (rightParenthesis == null) {
			return null;
		}
		
		return imageName;
	}

	/**
	 * Clear components.
	 */
	private void clear() {
		
		textImageName.setText("");
		
		Utility.selectFirst(comboSlice);
		textSliceTop.setText("");
		Utility.selectFirst(comboSliceTopUnits);
		textSliceRight.setText("");
		Utility.selectFirst(comboSliceRightUnits);
		textSliceBottom.setText("");
		Utility.selectFirst(comboSliceBottomUnits);
		textSliceLeft.setText("");
		Utility.selectFirst(comboSliceLeftUnits);
		
		textWidthTop.setText("");
		Utility.selectFirst(comboWidthTopUnits);
		textWidthRight.setText("");
		Utility.selectFirst(comboWidthRightUnits);
		textWidthBottom.setText("");
		Utility.selectFirst(comboWidthBottomUnits);
		textWidthLeft.setText("");
		Utility.selectFirst(comboWidthLeftUnits);
		
		textOutsetTop.setText("");
		Utility.selectFirst(comboOutsetTopUnits);
		textOutsetRight.setText("");
		Utility.selectFirst(comboOutsetRightUnits);
		textOutsetBottom.setText("");
		Utility.selectFirst(comboOutsetBottomUnits);
		textOutsetLeft.setText("");
		Utility.selectFirst(comboOutsetLeftUnits);
		
		Utility.selectFirst(comboRepeatHorizontal);
		Utility.selectFirst(comboRepeatVertical);
	}

	/**
	 * Return this component.
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansCssBorderImage;
	}

	/**
	 * Get result text.
	 */
	@Override
	public String getResultText() {
		
		return getStringValue();
	}

	/**
	 * Get dialog bounds.
	 */
	@Override
	public Rectangle getContainerDialogBounds() {

		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.BorderImageBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		CssBorderImagePanel.bounds = bounds;
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
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
