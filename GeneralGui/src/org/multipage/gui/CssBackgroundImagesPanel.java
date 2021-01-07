/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.event.ListSelectionListener;

import org.multipage.util.*;

import javax.swing.event.ListSelectionEvent;

/**
 * 
 * @author
 *
 */
public class CssBackgroundImagesPanel extends InsertPanel implements StringValueEditor {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Background layer.
	 */
	class BackgroundLayer {
		
		String imageName;
		String position;
		String size;
		String repeat;
		String attachment;
		String origin;
		String clip;
		
		/**
		 * Create new background layer.
		 * @param imageName
		 */
		public BackgroundLayer(String imageName) {
			
			this.imageName = imageName;
			position = "left top";
			size = "100% 100%";
			repeat = "no-repeat";
			attachment = "scroll";
			origin = "border-box";
			clip = "border-box";
		}

		/**
		 * Set panel controls.
		 * @param panel
		 */
		public void setControlValues(
				CssBackgroundImagesPanel panel) {
			
			startSettingControls();
			
			panel.setImagePosition(position);
			panel.setImageSize(size);
			panel.setRepeat(repeat);
			panel.setAttachment(attachment);
			panel.setOrigin(origin);
			panel.setClip(clip);
			
			stopSettingControls();
		}

		/**
		 * Read control values.
		 * @param panel
		 */
		public void getControlValues(
				CssBackgroundImagesPanel panel) {
			
			position = panel.getPosition();
			size = panel.getImageSize();
			repeat = panel.getRepeat();
			attachment = panel.getAttachment();
			origin = panel.getOrigin();
			clip = panel.getClip();
		}

		/**
		 * Gets string value.
		 * @return
		 */
		@Override
		public String toString() {
			return imageName;
		}
	}
	
	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 500, 330);
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
	private DefaultListModel<BackgroundLayer> listBackgroundImagesModel;
	
	/**
	 * Old selected layer.
	 */
	private BackgroundLayer previousSelectedLayer = null;
	
	/**
	 * Setting controls flag.
	 */
	private boolean settingControls = false;

	/**
	 * Get resource name callback.
	 */
	private Callback getResourceName;
	
	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelPositionHorizontal;
	private JComboBox comboHorizontalPosition;
	private TextFieldEx textHorizontalPosition;
	private JComboBox comboHorizontalPositionUnits;
	private JLabel labelPositionVertical;
	private JComboBox comboVerticalPosition;
	private TextFieldEx textVerticalPosition;
	private JComboBox comboVerticalPositionUnits;
	private JLabel labelImageSize;
	private JComboBox comboImageSize;
	private TextFieldEx textImageSizeX;
	private JComboBox comboImageSizeUnitsX;
	private JLabel labelX;
	private TextFieldEx textImageSizeY;
	private JComboBox comboImageSizeUnitsY;
	private JLabel labelRepeat;
	private JComboBox comboRepeat;
	private JComboBox comboOrigin;
	private JLabel labelOrigin;
	private JComboBox comboClip;
	private JLabel labelClip;
	private JComboBox comboAttachment;
	private JLabel labelAttachment;
	private JLabel labelBackgroundImage;
	private JScrollPane scrollImageNames;
	private JList<BackgroundLayer> listBackgroundImages;
	private JPopupMenu popupBackgroundImages;
	private JMenuItem menuAddImage;
	private JMenuItem menuRenameImage;
	private JMenuItem menuRemoveImage;
	private JButton buttonAddImage;
	private JButton buttonRenameImage;
	private JButton buttonRemoveImage;
	private JButton buttonFindImage;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public CssBackgroundImagesPanel(String initialString) {

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
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelPositionHorizontal = new JLabel("org.multipage.gui.textCssBackgroundHorizontalPosition");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelPositionHorizontal, 100, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelPositionHorizontal, 30, SpringLayout.WEST, this);
		add(labelPositionHorizontal);
		
		comboHorizontalPosition = new JComboBox();
		comboHorizontalPosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectHorizontalPositionCombo();
			}
		});
		comboHorizontalPosition.setPreferredSize(new Dimension(100, 20));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboHorizontalPosition, 0, SpringLayout.NORTH, labelPositionHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboHorizontalPosition, 6, SpringLayout.EAST, labelPositionHorizontal);
		add(comboHorizontalPosition);
		
		textHorizontalPosition = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textHorizontalPosition, 0, SpringLayout.NORTH, labelPositionHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, textHorizontalPosition, 6, SpringLayout.EAST, comboHorizontalPosition);
		textHorizontalPosition.setColumns(5);
		add(textHorizontalPosition);
		
		comboHorizontalPositionUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboHorizontalPositionUnits, 0, SpringLayout.NORTH, labelPositionHorizontal);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboHorizontalPositionUnits, 0, SpringLayout.EAST, textHorizontalPosition);
		comboHorizontalPositionUnits.setPreferredSize(new Dimension(50, 20));
		add(comboHorizontalPositionUnits);
		
		labelPositionVertical = new JLabel("org.multipage.gui.textCssBackgroundVerticalPosition");
		sl_panelMain.putConstraint(SpringLayout.EAST, labelPositionVertical, 0, SpringLayout.EAST, labelPositionHorizontal);
		add(labelPositionVertical);
		
		comboVerticalPosition = new JComboBox();
		comboVerticalPosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectVerticalPositionCombo();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelPositionVertical, 3, SpringLayout.NORTH, comboVerticalPosition);
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboVerticalPosition, 6, SpringLayout.SOUTH, comboHorizontalPosition);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboVerticalPosition, 0, SpringLayout.WEST, comboHorizontalPosition);
		comboVerticalPosition.setPreferredSize(new Dimension(100, 20));
		add(comboVerticalPosition);
		
		textVerticalPosition = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textVerticalPosition, 6, SpringLayout.SOUTH, textHorizontalPosition);
		sl_panelMain.putConstraint(SpringLayout.WEST, textVerticalPosition, 0, SpringLayout.WEST, textHorizontalPosition);
		textVerticalPosition.setColumns(5);
		add(textVerticalPosition);
		
		comboVerticalPositionUnits = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboVerticalPositionUnits, 6, SpringLayout.SOUTH, textHorizontalPosition);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboVerticalPositionUnits, 0, SpringLayout.EAST, textVerticalPosition);
		comboVerticalPositionUnits.setPreferredSize(new Dimension(50, 20));
		add(comboVerticalPositionUnits);
		
		labelImageSize = new JLabel("org.multipage.gui.textCssBackgroundImageSize");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelImageSize, 45, SpringLayout.SOUTH, labelPositionVertical);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelImageSize, 30, SpringLayout.WEST, this);
		add(labelImageSize);
		
		comboImageSize = new JComboBox();
		comboImageSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectImageSizeCombo();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboImageSize, 0, SpringLayout.NORTH, labelImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboImageSize, 6, SpringLayout.EAST, labelImageSize);
		comboImageSize.setPreferredSize(new Dimension(100, 20));
		add(comboImageSize);
		
		textImageSizeX = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textImageSizeX, 0, SpringLayout.NORTH, labelImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, textImageSizeX, 6, SpringLayout.EAST, comboImageSize);
		textImageSizeX.setColumns(5);
		add(textImageSizeX);
		
		comboImageSizeUnitsX = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboImageSizeUnitsX, 0, SpringLayout.NORTH, labelImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboImageSizeUnitsX, 0, SpringLayout.EAST, textImageSizeX);
		comboImageSizeUnitsX.setPreferredSize(new Dimension(50, 20));
		add(comboImageSizeUnitsX);
		
		labelX = new JLabel("x");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelX, 6, SpringLayout.EAST, comboImageSizeUnitsX);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, labelX, 0, SpringLayout.SOUTH, comboImageSize);
		add(labelX);
		
		textImageSizeY = new TextFieldEx();
		sl_panelMain.putConstraint(SpringLayout.NORTH, textImageSizeY, 0, SpringLayout.NORTH, labelImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, textImageSizeY, 6, SpringLayout.EAST, labelX);
		textImageSizeY.setColumns(5);
		add(textImageSizeY);
		
		comboImageSizeUnitsY = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboImageSizeUnitsY, 0, SpringLayout.NORTH, labelImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboImageSizeUnitsY, 0, SpringLayout.EAST, textImageSizeY);
		comboImageSizeUnitsY.setPreferredSize(new Dimension(50, 20));
		add(comboImageSizeUnitsY);
		
		labelRepeat = new JLabel("org.multipage.gui.textCssBackgroundRepeat");
		sl_panelMain.putConstraint(SpringLayout.EAST, labelRepeat, 0, SpringLayout.EAST, labelImageSize);
		add(labelRepeat);
		
		comboRepeat = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelRepeat, 3, SpringLayout.NORTH, comboRepeat);
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboRepeat, 20, SpringLayout.SOUTH, comboImageSize);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboRepeat, 0, SpringLayout.WEST, comboImageSize);
		comboRepeat.setPreferredSize(new Dimension(100, 20));
		add(comboRepeat);
		
		comboOrigin = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboOrigin, 0, SpringLayout.NORTH, labelRepeat);
		comboOrigin.setPreferredSize(new Dimension(100, 20));
		add(comboOrigin);
		
		labelOrigin = new JLabel("org.multipage.gui.textCssBackgroundOrigin");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelOrigin, 50, SpringLayout.EAST, comboRepeat);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboOrigin, 6, SpringLayout.EAST, labelOrigin);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelOrigin, 0, SpringLayout.NORTH, labelRepeat);
		add(labelOrigin);
		
		comboClip = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboClip, 20, SpringLayout.SOUTH, comboRepeat);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboClip, 0, SpringLayout.WEST, comboImageSize);
		comboClip.setPreferredSize(new Dimension(100, 20));
		add(comboClip);
		
		labelClip = new JLabel("org.multipage.gui.textCssBackgroundClip");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelClip, 0, SpringLayout.NORTH, comboClip);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelClip, 0, SpringLayout.EAST, labelImageSize);
		add(labelClip);
		
		comboAttachment = new JComboBox();
		sl_panelMain.putConstraint(SpringLayout.WEST, comboAttachment, 0, SpringLayout.WEST, comboOrigin);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, comboAttachment, 0, SpringLayout.SOUTH, comboClip);
		comboAttachment.setPreferredSize(new Dimension(100, 20));
		add(comboAttachment);
		
		labelAttachment = new JLabel("org.multipage.gui.textCssBackgroundAttachment");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelAttachment, 0, SpringLayout.NORTH, comboClip);
		sl_panelMain.putConstraint(SpringLayout.EAST, labelAttachment, 0, SpringLayout.EAST, labelOrigin);
		add(labelAttachment);
		
		labelBackgroundImage = new JLabel("org.multipage.gui.BackgroundImage");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelBackgroundImage, 20, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelBackgroundImage, 30, SpringLayout.WEST, this);
		add(labelBackgroundImage);
		
		scrollImageNames = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollImageNames, 20, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollImageNames, 6, SpringLayout.EAST, labelBackgroundImage);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollImageNames, -20, SpringLayout.NORTH, labelPositionHorizontal);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollImageNames, 350, SpringLayout.EAST, labelBackgroundImage);
		add(scrollImageNames);
		
		JList list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listBackgroundImages = list;
		listBackgroundImages.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				onSelectImageName();
			}
		});
		scrollImageNames.setViewportView(listBackgroundImages);
		
		popupBackgroundImages = new JPopupMenu();
		addPopup(listBackgroundImages, popupBackgroundImages);
		
		menuAddImage = new JMenuItem("org.multipage.gui.menuAddBackgroundImage");
		menuAddImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddImage();
			}
		});
		popupBackgroundImages.add(menuAddImage);
		
		menuRenameImage = new JMenuItem("org.multipage.gui.menuRenameBackgroundImage");
		menuRenameImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRenameImage();
			}
		});
		popupBackgroundImages.add(menuRenameImage);
		
		menuRemoveImage = new JMenuItem("org.multipage.gui.menuRemoveBackGroundImage");
		menuRemoveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveImages();
			}
		});
		popupBackgroundImages.add(menuRemoveImage);
		
		buttonAddImage = new JButton("");
		buttonAddImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddImage();
			}
		});
		buttonAddImage.setMargin(new Insets(0, 0, 0, 0));
		buttonAddImage.setPreferredSize(new Dimension(24, 24));
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonAddImage, 0, SpringLayout.NORTH, labelBackgroundImage);
		add(buttonAddImage);
		
		buttonRenameImage = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonRenameImage, 3, SpringLayout.EAST, buttonAddImage);
		buttonRenameImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRenameImage();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonRenameImage, 0, SpringLayout.NORTH, labelBackgroundImage);
		buttonRenameImage.setPreferredSize(new Dimension(24, 24));
		buttonRenameImage.setMargin(new Insets(0, 0, 0, 0));
		add(buttonRenameImage);
		
		buttonRemoveImage = new JButton("");
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonRemoveImage, 3, SpringLayout.EAST, buttonRenameImage);
		buttonRemoveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemoveImages();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonRemoveImage, 0, SpringLayout.NORTH, labelBackgroundImage);
		buttonRemoveImage.setPreferredSize(new Dimension(24, 24));
		buttonRemoveImage.setMargin(new Insets(0, 0, 0, 0));
		add(buttonRemoveImage);
		
		buttonFindImage = new JButton("");
		buttonFindImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onFindResource();
			}
		});
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonAddImage, 3, SpringLayout.EAST, buttonFindImage);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonFindImage, 0, SpringLayout.NORTH, labelBackgroundImage);
		sl_panelMain.putConstraint(SpringLayout.WEST, buttonFindImage, 6, SpringLayout.EAST, scrollImageNames);
		buttonFindImage.setPreferredSize(new Dimension(24, 24));
		buttonFindImage.setMargin(new Insets(0, 0, 0, 0));
		add(buttonFindImage);
	}

	/**
	 * On find image.
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
		listBackgroundImagesModel.addElement(new BackgroundLayer(imageName));
		
		// Select new resource name.
		int count = listBackgroundImagesModel.getSize();
		listBackgroundImages.setSelectedIndex(count - 1);
	}
	
	/**
	 * Set callback.
	 * @param callback
	 */
	public void setResourceNameCallback(Callback callback) {
		
		getResourceName = callback;
	}

	/**
	 * Start setting controls.
	 */
	public void startSettingControls() {
		
		settingControls = true;
	}

	/**
	 * Stop setting controls.
	 */
	public void stopSettingControls() {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				settingControls = false;
			}
		});
	}

	/**
	 * On select image name.
	 */
	protected void onSelectImageName() {
		
		int [] selectedIndices = listBackgroundImages.getSelectedIndices();
		boolean isSelected = selectedIndices.length == 1;
		
		BackgroundLayer selectedLayer = listBackgroundImages.getSelectedValue();
		
		// Save previous selection.
		if (previousSelectedLayer != null && previousSelectedLayer != selectedLayer) {
			previousSelectedLayer.getControlValues(this);
		}
		
		// If a layer is selected, load appropriate controls' values.
		showImagePropertyControls(isSelected);
		if (isSelected) {
			
			
			if (selectedLayer != previousSelectedLayer) {
				selectedLayer.setControlValues(this);
			}
		}
		
		previousSelectedLayer = listBackgroundImages.getSelectedValue();
	}
	
	/**
	 * Save current layer.
	 */
	private void saveCurrentLayer() {
		
		int [] selectedIndices = listBackgroundImages.getSelectedIndices();
		boolean isSelected = selectedIndices.length == 1;
		
		if (!isSelected) {
			return;
		}
		
		BackgroundLayer selectedLayer = listBackgroundImages.getSelectedValue();
		selectedLayer.getControlValues(this);
	}

	/**
	 * On remove images.
	 */
	protected void onRemoveImages() {
		
		int [] selectedIndices = listBackgroundImages.getSelectedIndices();
		if (selectedIndices.length != 1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleImageName");
			return;
		}
		
		if (Utility.askParam(this, "org.multipage.gui.messageRemoveBackgroundImage", listBackgroundImagesModel.get(selectedIndices[0]))) {
			listBackgroundImagesModel.remove(selectedIndices[0]);
		}
	}

	/**
	 * On rename image.
	 */
	protected void onRenameImage() {
		
		int [] selectedIndices = listBackgroundImages.getSelectedIndices();
		if (selectedIndices.length != 1) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleImageName");
			return;
		}
		
		int indexToRename = selectedIndices[0];
		BackgroundLayer layer = listBackgroundImagesModel.get(indexToRename);
		
		String newName = Utility.input(this, "org.multipage.gui.messageRenameBackgroundImageName", layer.imageName);
		if (newName == null) {
			return;
		}
		
		layer.imageName = newName;
		scrollImageNames.revalidate();
		scrollImageNames.repaint();
	}

	/**
	 * On add image name.
	 */
	protected void onAddImage() {
		
		String imageName = Utility.input(this, "org.multipage.gui.textInsertBackgroundImageName");
		if (imageName != null) {
			
			listBackgroundImagesModel.addElement(new BackgroundLayer(imageName));
		}
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {

		listBackgroundImagesModel = new DefaultListModel<BackgroundLayer>();
		listBackgroundImages.setModel(listBackgroundImagesModel);
		
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

		showImagePropertyControls(false);
		
		localize();
		setIcons();
		setToolTips();
		
		loadUnits();
		loadEnumerations();
		
		setListeners();
		
		loadDialog();
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		Utility.setTextChangeListener(textHorizontalPosition, new Runnable() {
			@Override
			public void run() {
				onChangeHorizontalPosition();
			}
		});
		
		Utility.setTextChangeListener(textVerticalPosition, new Runnable() {
			@Override
			public void run() {
				onChangeVerticalPosition();
			}
		});
		
		Runnable sizeChanged = new Runnable() {
			@Override
			public void run() {
				onChangeImageSize();
			}
		};
		
		Utility.setTextChangeListener(textImageSizeX, sizeChanged);
		Utility.setTextChangeListener(textImageSizeY, sizeChanged);
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonAddImage.setToolTipText(Resources.getString("org.multipage.gui.tooltipAddBackgroundImage"));
		buttonRenameImage.setToolTipText(Resources.getString("org.multipage.gui.tooltipRenameBackgroundImage"));
		buttonRemoveImage.setToolTipText(Resources.getString("org.multipage.gui.tooltipRemoveBackgroundImage"));
		buttonFindImage.setToolTipText(Resources.getString("org.multipage.gui.tooltipFindBackgroundImage"));
	}

	/**
	 * Show image property controls.
	 * @param show
	 */
	private void showImagePropertyControls(boolean show) {
		
		labelAttachment.setVisible(show);
		labelClip.setVisible(show);
		labelImageSize.setVisible(show);
		labelOrigin.setVisible(show);
		labelPositionHorizontal.setVisible(show);
		labelPositionVertical.setVisible(show);
		labelRepeat.setVisible(show);
		labelX.setVisible(show);
		
		comboAttachment.setVisible(show);
		comboClip.setVisible(show);
		comboHorizontalPosition.setVisible(show);
		comboHorizontalPositionUnits.setVisible(show);
		comboImageSize.setVisible(show);
		comboImageSizeUnitsX.setVisible(show);
		comboImageSizeUnitsY.setVisible(show);
		comboOrigin.setVisible(show);
		comboRepeat.setVisible(show);
		comboVerticalPosition.setVisible(show);
		comboVerticalPositionUnits.setVisible(show);
		
		textHorizontalPosition.setVisible(show);
		textImageSizeX.setVisible(show);
		textImageSizeY.setVisible(show);
		textVerticalPosition.setVisible(show);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		menuAddImage.setIcon(Images.getIcon("org/multipage/gui/images/insert.png"));
		buttonAddImage.setIcon(Images.getIcon("org/multipage/gui/images/insert.png"));
		menuRenameImage.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		buttonRenameImage.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		menuRemoveImage.setIcon(Images.getIcon("org/multipage/gui/images/cut_icon.png"));
		buttonRemoveImage.setIcon(Images.getIcon("org/multipage/gui/images/cut_icon.png"));
		buttonFindImage.setIcon(Images.getIcon("org/multipage/gui/images/find_icon.png"));
	}

	/**
	 * Load enumerations.
	 */
	private void loadEnumerations() {
		
		final String [][] boxes = new String [][] {
				{"padding-box", "org.multipage.gui.textCssBackgroundPaddingBox"},
				{"border-box", "org.multipage.gui.textCssBackgroundBorderBox"},
				{"content-box", "org.multipage.gui.textCssBackgroundContentBox"}
		};
		
		Utility.loadNamedItems(comboHorizontalPosition, new String [][] {
				{"left", "org.multipage.gui.textCssBackgroundLeft"},
				{"right", "org.multipage.gui.textCssBackgroundRight"},
				{"center", "org.multipage.gui.textCssBackgroundCenter"}
				});
		Utility.loadNamedItems(comboVerticalPosition, new String [][] {
				{"top", "org.multipage.gui.textCssBackgroundTop"},
				{"bottom", "org.multipage.gui.textCssBackgroundBottom"},
				{"center", "org.multipage.gui.textCssBackgroundCenter"}
				});
		Utility.loadNamedItems(comboImageSize, new String [][] {
				{"auto", "org.multipage.gui.textCssBackgroundAuto"},
				{"cover", "org.multipage.gui.textCssBackgroundCover"},
				{"contain", "org.multipage.gui.textCssBackgroundContain"}
		});
		Utility.loadNamedItems(comboRepeat, new String [][] {
				{"repeat", "org.multipage.gui.textCssBackgroundRepeat2"},
				{"repeat-x", "org.multipage.gui.textCssBackgroundRepeatX"},
				{"repeat-y", "org.multipage.gui.textCssBackgroundRepeatY"},
				{"no-repeat", "org.multipage.gui.textCssBackgroundNoRepeat"}
		});
		Utility.loadNamedItems(comboOrigin, boxes);
		Utility.loadNamedItems(comboClip, boxes);
		Utility.loadNamedItems(comboAttachment, new String [][] {
				{"scroll", "org.multipage.gui.textCssBackgroundScroll"},
				{"fixed", "org.multipage.gui.textCssBackgroundFixed"},
				{"local", "org.multipage.gui.textCssBackgroundLocal"}
		});
	}

	/**
	 * Load units.
	 */
	private void loadUnits() {
		
		Utility.loadCssUnits(comboHorizontalPositionUnits);
		Utility.loadCssUnits(comboVerticalPositionUnits);
		Utility.loadCssUnits(comboImageSizeUnitsX);
		Utility.loadCssUnits(comboImageSizeUnitsY);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		saveCurrentLayer();
		
		String specification = "";
		
		// Get specification of all layers.
		Enumeration<BackgroundLayer> layers = listBackgroundImagesModel.elements();
		
		boolean isFirst = true;
		while (layers.hasMoreElements()) {
			
			BackgroundLayer layer = layers.nextElement();
			
			String layerSpecification = String.format("url(\"[@URL thisArea, res=\"#%s\"]\")", layer.imageName);
			layerSpecification += " " + layer.position;
			layerSpecification += "/" + layer.size;
			layerSpecification += " " + layer.repeat;
			layerSpecification += " " + layer.attachment;
			layerSpecification += " " + layer.origin;
			layerSpecification += " " + layer.clip;
			
			if (isFirst) {
				specification = layerSpecification;
			}
			else {
				specification += "," + layerSpecification;
			}
			
			isFirst = false;
		}
		
		return specification;
	}

	/**
	 * Get position.
	 * @return
	 */
	private String getPosition() {
		
		return Utility.getCssValueAndUnits(textHorizontalPosition,
				comboHorizontalPositionUnits, comboHorizontalPosition) + " "
				
				+ Utility.getCssValueAndUnits(textVerticalPosition,
				comboVerticalPositionUnits, comboVerticalPosition);
	}

	/**
	 * Get size.
	 * @return
	 */
	private String getImageSize() {
		
		boolean hasAuto = textImageSizeX.getText().isEmpty() ^ textImageSizeY.getText().isEmpty();

		// Use combo value.
		if (!hasAuto) {
			if (!Utility.isTextFieldNumber(textImageSizeX) || !Utility.isTextFieldNumber(textImageSizeY)) {
				
				return Utility.getSelectedNamedItem(comboImageSize);
			}
		}
		
		String result = "";
		boolean xHasAuto = textImageSizeX.getText().isEmpty();
		boolean yHasAuto = textImageSizeY.getText().isEmpty();
		
		if (!xHasAuto) {
			result += Utility.getCssValueAndUnits(textImageSizeX, comboImageSizeUnitsX);
		}
		if (!hasAuto) {
			result += " ";
		}
		if (!yHasAuto) {
			result += Utility.getCssValueAndUnits(textImageSizeY, comboImageSizeUnitsY);
		}
		
		// Otherwise use numbers and units.
		return result;
	}

	/**
	 * Get repeat.
	 * @return
	 */
	private String getRepeat() {
		
		return Utility.getSelectedNamedItem(comboRepeat);
	}

	/**
	 * Get origin.
	 * @return
	 */
	private String getOrigin() {
		
		return Utility.getSelectedNamedItem(comboOrigin);
	}

	/**
	 * Get clip.
	 * @return
	 */
	private String getClip() {
		
		return Utility.getSelectedNamedItem(comboClip);
	}

	/**
	 * Get attachment.
	 * @return
	 */
	private String getAttachment() {
		
		return Utility.getSelectedNamedItem(comboAttachment);
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		listBackgroundImagesModel.clear();
		
		if (initialString != null) {
			
			try {
				
				Obj<Integer> position = new Obj<Integer>(0);
				
				while (true) {

					// Get image name.
					String imageName = getBackgroundImageName(position);
					if (imageName == null) {
						break;
					}
					
					// Get image position. (text to first slash);
					int begin = position.ref;
					if (Utility.getNextMatch(initialString, position, "/") == null) {
						break;
					}
					String imagePosition = initialString.substring(begin, position.ref - 1);
					imagePosition = imagePosition.trim();
					
					// Get size.
					String size = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%-]+");
					if (size == null) {
						break;
					}
					if (Utility.isCssStringNumberUnit(size)) {
						
						boolean isNextNumber = isNextNonWhitespceNumber(position.ref);
						
						if (isNextNumber) {
							String sizeRemainder = Utility.getNextMatch(initialString, position, "[\\w\\d\\.%]+");
							if (sizeRemainder == null) {
								break;
							}
							size += " " + sizeRemainder;
						}
					}
					
					// Get repeat.
					String repeat = Utility.getNextMatch(initialString, position, "[\\w-]+");
					if (repeat == null) {
						break;
					}
					
					// Get attachment.
					String attachment = Utility.getNextMatch(initialString, position, "[\\w-]+");
					if (attachment == null) {
						break;
					}
					
					// Get origin.
					String origin = Utility.getNextMatch(initialString, position, "[\\w-]+");
					if (origin == null) {
						break;
					}
					
					// Get clip.
					String clip = Utility.getNextMatch(initialString, position, "[\\w-]+");
					if (clip == null) {
						break;
					}

					// Create new layer and add it to a list.
					BackgroundLayer layer = new BackgroundLayer(imageName);
					layer.position = imagePosition;
					layer.size = size;
					layer.repeat = repeat;
					layer.attachment = attachment;
					layer.origin = origin;
					layer.clip = clip;
					
					listBackgroundImagesModel.addElement(layer);
					
					// Get next comma.
					if (!isNextComma(position)) {
						break;
					}
				}
			}
			catch (Exception e) {
				
				System.out.println(e.getMessage());
			}
			
			// Select first layer.
			if (listBackgroundImagesModel.size() > 0) {
				listBackgroundImages.setSelectedIndex(0);
			}
		}
	}

	/**
	 * Returns true value if a next non whitespace character is a number.
	 * @param position
	 * @return
	 */
	private boolean isNextNonWhitespceNumber(int position) {
		
		int length = initialString.length();
		while (position < length) {
			
			char character = initialString.charAt(position++);
			if (Character.isWhitespace(character)) {
				continue;
			}
			
			return Character.isDigit(character);
		}
		return false;
	}

	/**
	 * Returns true value if a next comma exists.
	 * @param position
	 * @return
	 */
	private boolean isNextComma(Obj<Integer> position) {
		
		int shift = 0;
		int length = initialString.length();
		char character = ' ';
		
		do {
			
			int index = position.ref + shift;
			if (index >= length) {
				break;
			}
			
			character = initialString.charAt(index);
			shift++;
		}
		while (Character.isWhitespace(character));
			
		if (character == ',') {
			position.ref += shift;
			return true;
		}
		
		shift--;
		if (shift < 0) {
			shift = 0;
		}
		position.ref += shift;
		
		return false;
	}

	/**
	 * Get background image name.
	 * @param position
	 * @return
	 */
	private String getBackgroundImageName(Obj<Integer> position) {
		
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
	 * Set image position.
	 * @param string
	 */
	private void setImagePosition(String string) {
		
		String [] splitted = string.split(" ");
		
		if (splitted.length < 2) {
			return;
		}
		
		setHorizontalPosition(splitted[0]);
		setVerticalPosition(splitted[1]);
	}

	/**
	 * Set horizontal position.
	 * @param string
	 */
	private void setHorizontalPosition(String string) {

		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		// Horizontal value.
		if (Utility.convertCssStringToNumberUnit(string, number, unit)) {
			
			// Set number and unit.
			textHorizontalPosition.setText(number.ref);
			Utility.selectComboItem(comboHorizontalPositionUnits, unit.ref);
			
			// Set enumeration.
			Utility.selectComboNamedItem(comboHorizontalPosition, "left");
		}
		else {
			
			textHorizontalPosition.setText("");
			Utility.selectComboItem(comboHorizontalPositionUnits, "px");
			
			// Set enumeration.
			Utility.selectComboNamedItem(comboHorizontalPosition, string);
		}
	}
	
	/**
	 * On select horizontal position combo.
	 */
	protected void onSelectHorizontalPositionCombo() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		textHorizontalPosition.setText("");
		Utility.selectComboItem(comboHorizontalPositionUnits, "px");
		
		stopSettingControls();
	}

	/**
	 * On change horizontal position.
	 */
	protected void onChangeHorizontalPosition() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		// Set enumeration.
		Utility.selectComboNamedItem(comboHorizontalPosition, "left");
		
		stopSettingControls();
	}

	/**
	 * Set vertical position.
	 * @param string
	 */
	private void setVerticalPosition(String string) {
		
		Obj<String> number = new Obj<String>();
		Obj<String> unit = new Obj<String>();
		
		// Vertical value.
		if (Utility.convertCssStringToNumberUnit(string, number, unit)) {
			
			// Set number and unit.
			textVerticalPosition.setText(number.ref);
			Utility.selectComboItem(comboVerticalPositionUnits, unit.ref);
			
			// Set enumeration.
			Utility.selectComboNamedItem(comboVerticalPosition, "top");
		}
		else {
			
			textVerticalPosition.setText("");
			Utility.selectComboItem(comboVerticalPositionUnits, "px");
			
			// Set enumeration.
			Utility.selectComboNamedItem(comboVerticalPosition, string);
		}
	}
	
	/**
	 * On select vertical position combo.
	 */
	protected void onSelectVerticalPositionCombo() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		textVerticalPosition.setText("");
		Utility.selectComboItem(comboVerticalPositionUnits, "px");
		
		stopSettingControls();
	}

	/**
	 * On change vertical position.
	 */
	protected void onChangeVerticalPosition() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		Utility.selectComboNamedItem(comboVerticalPosition, "top");
		
		stopSettingControls();
	}

	/**
	 * Set image size.
	 * @param string
	 */
	private void setImageSize(String string) {
		
		boolean numbersOk = false;
		
		// Split string.
		String [] splitted = string.trim().split(" ");
		
		if (splitted.length >= 1) {
			
			String horizontal = splitted[0];
			
			Obj<String> number = new Obj<String>();
			Obj<String> unit = new Obj<String>();
			
			// Horizontal specification.
			if (Utility.convertCssStringToNumberUnit(horizontal, number, unit)) {
				
				textImageSizeX.setText(number.ref);
				Utility.selectComboItem(comboImageSizeUnitsX, unit.ref);
				
				if (splitted.length == 2) {
					String vertical = splitted[1];
					
					// Vertical specification.
					if (Utility.convertCssStringToNumberUnit(vertical, number, unit)) {
						
						textImageSizeY.setText(number.ref);
						Utility.selectComboItem(comboImageSizeUnitsY, unit.ref);
						
						Utility.selectComboNamedItem(comboImageSize, "auto");
						
						numbersOk = true;
					}
				}
				else {
					textImageSizeY.setText("");
					Utility.selectComboItem(comboImageSizeUnitsY, "px");
					
					Utility.selectComboNamedItem(comboImageSize, "auto");
					numbersOk = true;
				}
			}
		}
		
		// Set enumeration.
		if (!numbersOk) {
			
			textImageSizeX.setText("");
			textImageSizeY.setText("");
			Utility.selectComboItem(comboImageSizeUnitsX, "px");
			Utility.selectComboItem(comboImageSizeUnitsY, "px");
			
			Utility.selectComboNamedItem(comboImageSize, string);
		}
	}

	/**
	 * On serlect image size combo.
	 */
	protected void onSelectImageSizeCombo() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		textImageSizeX.setText("");
		textImageSizeY.setText("");
		Utility.selectComboItem(comboImageSizeUnitsX, "px");
		Utility.selectComboItem(comboImageSizeUnitsY, "px");
		
		stopSettingControls();
	}

	/**
	 * On change image size.
	 */
	protected void onChangeImageSize() {
		
		if (settingControls) {
			return;
		}
		
		startSettingControls();
		
		Utility.selectComboNamedItem(comboImageSize, "auto");
		
		stopSettingControls();
	}

	/**
	 * Set repeat.
	 * @param string
	 */
	private void setRepeat(String string) {
		
		Utility.selectComboNamedItem(comboRepeat, string);
	}

	/**
	 * Set origin.
	 * @param string
	 */
	private void setOrigin(String string) {
		
		Utility.selectComboNamedItem(comboOrigin, string);
	}

	/**
	 * Set clip.
	 * @param string
	 */
	private void setClip(String string) {
		
		Utility.selectComboNamedItem(comboClip, string);
	}

	/**
	 * Set attachment.
	 * @param string
	 */
	private void setAttachment(String string) {
		
		Utility.selectComboNamedItem(comboAttachment, string);
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelPositionHorizontal);
		Utility.localize(labelPositionVertical);
		Utility.localize(labelImageSize);
		Utility.localize(labelRepeat);
		Utility.localize(labelOrigin);
		Utility.localize(labelClip);
		Utility.localize(labelAttachment);
		Utility.localize(labelBackgroundImage);
		Utility.localize(menuAddImage);
		Utility.localize(menuRenameImage);
		Utility.localize(menuRemoveImage);
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.gui.textCssBackgroundBuilder");
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
		
		CssBackgroundImagesPanel.bounds = bounds;
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
		
		return meansCssBackground;
	}
	
	/**
	 * Component listeners.
	 * @param component
	 * @param popup
	 */
	private void addPopup(Component component, final JPopupMenu popup) {
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
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					onRenameImage();
				}
			}
		});
	}

	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
}
