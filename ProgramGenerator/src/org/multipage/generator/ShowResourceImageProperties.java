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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.Middle;
import com.maclan.MiddleResult;
import com.maclan.Resource;

/**
 * 
 * @author
 *
 */
public class ShowResourceImageProperties extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Resource reference.
	 */
	private Resource resource;

	/**
	 * Image reference.
	 */
	private BufferedImage image;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonClose;
	private JPanel panel_1;
	private JLabel labelWidthHeight;
	private TextFieldEx textWidthHeight;
	private JLabel labelFileSize;
	private TextFieldEx textFileSize;
	private JLabel labelSavedAsText;
	private TextFieldEx textSavedAsText;
	private JLabel labelBitsPerPixel;
	private TextFieldEx textBitsPerPixel;
	private JLabel labelHasAlpha;
	private TextFieldEx textHasAlpha;
	private JButton buttonShowContent;

	/**
	 * Show dialog.
	 * @param parent
	 * @param resource
	 */
	public static void showDialog(Component parent, Resource resource) {
		
		ShowResourceImageProperties dialog = new ShowResourceImageProperties(
				Utility.findWindow(parent), resource);
		
		dialog.setVisible(true);
	}
	
	/**
	 * Create the dialog.
	 * @param parentWindow 
	 * @param resource 
	 */
	public ShowResourceImageProperties(Window parentWindow, Resource resource) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		this.resource = resource;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("org.multipage.generator.textResourceDataProperties");
		
		setBounds(100, 100, 450, 357);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonClose = new JButton("textClose");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonClose.setMargin(new Insets(0, 0, 0, 0));
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonClose, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonClose, -10, SpringLayout.EAST, panel);
		buttonClose.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonClose);
		
		panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{57, 151, 0, 170, 0};
		gbl_panel_1.rowHeights = new int[] {0, 30, 30, 30, 30, 30, 0, 0, 30};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		labelSavedAsText = new JLabel("org.multipage.generator.textSavedAsText");
		GridBagConstraints gbc_labelSavedAsText = new GridBagConstraints();
		gbc_labelSavedAsText.anchor = GridBagConstraints.LINE_START;
		gbc_labelSavedAsText.insets = new Insets(0, 0, 5, 5);
		gbc_labelSavedAsText.gridx = 1;
		gbc_labelSavedAsText.gridy = 1;
		panel_1.add(labelSavedAsText, gbc_labelSavedAsText);
		
		textSavedAsText = new TextFieldEx();
		textSavedAsText.setPreferredSize(new Dimension(100, 20));
		textSavedAsText.setEditable(false);
		textSavedAsText.setBorder(null);
		GridBagConstraints gbc_textSavedAsText = new GridBagConstraints();
		gbc_textSavedAsText.insets = new Insets(0, 0, 5, 0);
		gbc_textSavedAsText.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSavedAsText.gridx = 3;
		gbc_textSavedAsText.gridy = 1;
		panel_1.add(textSavedAsText, gbc_textSavedAsText);
		
		labelFileSize = new JLabel("org.multipage.generator.textResourceFileSize");
		GridBagConstraints gbc_labelFileSize = new GridBagConstraints();
		gbc_labelFileSize.anchor = GridBagConstraints.LINE_START;
		gbc_labelFileSize.insets = new Insets(0, 0, 5, 5);
		gbc_labelFileSize.gridx = 1;
		gbc_labelFileSize.gridy = 2;
		panel_1.add(labelFileSize, gbc_labelFileSize);
		
		textFileSize = new TextFieldEx();
		textFileSize.setPreferredSize(new Dimension(100, 20));
		textFileSize.setEditable(false);
		textFileSize.setBorder(null);
		GridBagConstraints gbc_textFileSize = new GridBagConstraints();
		gbc_textFileSize.insets = new Insets(0, 0, 5, 0);
		gbc_textFileSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFileSize.gridx = 3;
		gbc_textFileSize.gridy = 2;
		panel_1.add(textFileSize, gbc_textFileSize);
		
		labelWidthHeight = new JLabel("org.multipage.generator.textImageWidthHeightText");
		GridBagConstraints gbc_labelWidthHeight = new GridBagConstraints();
		gbc_labelWidthHeight.anchor = GridBagConstraints.LINE_START;
		gbc_labelWidthHeight.insets = new Insets(0, 0, 5, 5);
		gbc_labelWidthHeight.gridx = 1;
		gbc_labelWidthHeight.gridy = 3;
		panel_1.add(labelWidthHeight, gbc_labelWidthHeight);
		
		textWidthHeight = new TextFieldEx();
		textWidthHeight.setBorder(null);
		textWidthHeight.setPreferredSize(new Dimension(100, 20));
		textWidthHeight.setEditable(false);
		GridBagConstraints gbc_textWidthHeight = new GridBagConstraints();
		gbc_textWidthHeight.fill = GridBagConstraints.HORIZONTAL;
		gbc_textWidthHeight.anchor = GridBagConstraints.LINE_START;
		gbc_textWidthHeight.insets = new Insets(0, 0, 5, 0);
		gbc_textWidthHeight.gridx = 3;
		gbc_textWidthHeight.gridy = 3;
		panel_1.add(textWidthHeight, gbc_textWidthHeight);
		
		labelBitsPerPixel = new JLabel("org.multipage.generator.textBitsPerPixel");
		GridBagConstraints gbc_labelBitsPerPixel = new GridBagConstraints();
		gbc_labelBitsPerPixel.anchor = GridBagConstraints.LINE_START;
		gbc_labelBitsPerPixel.insets = new Insets(0, 0, 5, 5);
		gbc_labelBitsPerPixel.gridx = 1;
		gbc_labelBitsPerPixel.gridy = 4;
		panel_1.add(labelBitsPerPixel, gbc_labelBitsPerPixel);
		
		textBitsPerPixel = new TextFieldEx();
		textBitsPerPixel.setPreferredSize(new Dimension(100, 20));
		textBitsPerPixel.setEditable(false);
		textBitsPerPixel.setBorder(null);
		GridBagConstraints gbc_textBitsPerPixel = new GridBagConstraints();
		gbc_textBitsPerPixel.insets = new Insets(0, 0, 5, 0);
		gbc_textBitsPerPixel.fill = GridBagConstraints.HORIZONTAL;
		gbc_textBitsPerPixel.gridx = 3;
		gbc_textBitsPerPixel.gridy = 4;
		panel_1.add(textBitsPerPixel, gbc_textBitsPerPixel);
		
		labelHasAlpha = new JLabel("org.multipage.generator.textHasAlphaChannel");
		GridBagConstraints gbc_labelHasAlpha = new GridBagConstraints();
		gbc_labelHasAlpha.anchor = GridBagConstraints.LINE_START;
		gbc_labelHasAlpha.insets = new Insets(0, 0, 5, 5);
		gbc_labelHasAlpha.gridx = 1;
		gbc_labelHasAlpha.gridy = 5;
		panel_1.add(labelHasAlpha, gbc_labelHasAlpha);
		
		textHasAlpha = new TextFieldEx();
		textHasAlpha.setPreferredSize(new Dimension(100, 20));
		textHasAlpha.setEditable(false);
		textHasAlpha.setBorder(null);
		GridBagConstraints gbc_textHasAlpha = new GridBagConstraints();
		gbc_textHasAlpha.insets = new Insets(0, 0, 5, 0);
		gbc_textHasAlpha.fill = GridBagConstraints.HORIZONTAL;
		gbc_textHasAlpha.gridx = 3;
		gbc_textHasAlpha.gridy = 5;
		panel_1.add(textHasAlpha, gbc_textHasAlpha);
		
		buttonShowContent = new JButton("org.multipage.generator.textShowContent");
		buttonShowContent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onShowContent();
			}
		});
		buttonShowContent.setMargin(new Insets(0, 0, 0, 0));
		buttonShowContent.setPreferredSize(new Dimension(120, 25));
		GridBagConstraints gbc_buttonShowContent = new GridBagConstraints();
		gbc_buttonShowContent.gridwidth = 4;
		gbc_buttonShowContent.gridx = 0;
		gbc_buttonShowContent.gridy = 7;
		panel_1.add(buttonShowContent, gbc_buttonShowContent);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
		
		loadProperties();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonClose);
		Utility.localize(labelWidthHeight);
		Utility.localize(labelFileSize);
		Utility.localize(labelSavedAsText);
		Utility.localize(labelBitsPerPixel);
		Utility.localize(labelHasAlpha);
		Utility.localize(buttonShowContent);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonClose.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonShowContent.setIcon(Images.getIcon("org/multipage/generator/images/show_content.png"));
	}
	
	/**
	 * Load properties.
	 */
	private void loadProperties() {
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		MiddleResult result;
		
		// Try to load resource image.
		Obj<BufferedImage> image = new Obj<BufferedImage>();
		
		result = middle.loadResourceFullImage(login, resource.getId(), image);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		// Show saved as text.
		textSavedAsText.setText(Resources.getString(
				resource.isSavedAsText() ? "org.multipage.gui.textTrueState" : "org.multipage.gui.textFalseState"));
		
		this.image = image.ref;
		
		if (image.ref != null) {
			
			// Show image dimensions.
			String text = String.valueOf(image.ref.getWidth()) + " x " + String.valueOf(image.ref.getHeight());
			textWidthHeight.setText(text);
			
			ColorModel colorModel = image.ref.getColorModel();
			// Show pixel size.
			textBitsPerPixel.setText(String.valueOf(colorModel.getPixelSize()));
			
			// Show alpha channel.
			textHasAlpha.setText(Resources.getString(
					colorModel.hasAlpha() ? "org.multipage.gui.textTrueState" : "org.multipage.gui.textFalseState"));
		}
		
		// Show file length.

		Obj<Long> fileLength = new Obj<Long>(0L);
		result = middle.loadResourceDataLength(login, resource.getId(), fileLength);
		if (result.isNotOK()) {
			result.show(this);
		}
		
		textFileSize.setText(String.valueOf(fileLength.ref) + " Bytes");
	}
	
	/**
	 * On show content
	 */
	protected void onShowContent() {
		
		if (image != null) {
			
			ShowResourceContent.showDialog(this, resource, image);
		}
		else {
			// Edit the resource.
			TextResourceEditor.showDialog(this,
					resource.getId(), resource.isSavedAsText(), true);
		}
	}
}
