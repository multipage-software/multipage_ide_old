/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.translator;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * 
 * @author
 *
 */
public class LanguageEditor extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Identifier.
	 */
	private long id;

	/**
	 * Description.
	 */
	private String description;

	/**
	 * Alias.
	 */
	private String alias;
	
	/**
	 * Icon.
	 */
	private BufferedImage image;
	
	/**
	 * Is start language flag.
	 */
	private boolean isSart;

	/**
	 * Parent window.
	 */
	private Window parentWindow;

	// $hide<<$
	/**
	 * Confirmed flag.
	 */
	private boolean confirmed = false;
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelDescription;
	private JTextField textDescription;
	private JLabel labelAlias;
	private JTextField textAlias;
	private JLabel labelId;
	private JTextField textIdentifier;
	private JLabel labelIcon;
	private JButton buttonLoadImage;
	private JButton buttonLoadFromFile;
	private JButton buttonResetImage;
	private JCheckBox checkBoxStartLanguage;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public LanguageEditor(Window parentWindow) {
		super(parentWindow, ModalityType.APPLICATION_MODAL);
		// Initialize components.
		initComponents();
		// $hide>>$
		// Post creation.
		this.parentWindow = parentWindow;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setMinimumSize(new Dimension(80, 25));
		buttonCancel.setMaximumSize(new Dimension(80, 25));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setMinimumSize(new Dimension(80, 25));
		buttonOk.setMaximumSize(new Dimension(80, 25));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelDescription = new JLabel("org.multipage.translator.textLanguageDescription");
		springLayout.putConstraint(SpringLayout.NORTH, labelDescription, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelDescription, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelDescription);
		
		textDescription = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textDescription, 6, SpringLayout.SOUTH, labelDescription);
		springLayout.putConstraint(SpringLayout.WEST, textDescription, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textDescription, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(textDescription);
		textDescription.setColumns(10);
		setTitle("org.multipage.translator.textLanguageEditor");
		setBounds(100, 100, 396, 216);
		
		labelAlias = new JLabel("org.multipage.translator.textLanguageAlias");
		springLayout.putConstraint(SpringLayout.NORTH, labelAlias, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.WEST, labelAlias, 0, SpringLayout.WEST, labelDescription);
		getContentPane().add(labelAlias);
		
		textAlias = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textAlias, 6, SpringLayout.SOUTH, labelAlias);
		springLayout.putConstraint(SpringLayout.WEST, textAlias, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textAlias, 159, SpringLayout.WEST, getContentPane());
		getContentPane().add(textAlias);
		textAlias.setColumns(10);
		
		labelId = new JLabel("org.multipage.translator.textLanguageId");
		springLayout.putConstraint(SpringLayout.NORTH, labelId, 6, SpringLayout.SOUTH, textDescription);
		getContentPane().add(labelId);
		
		textIdentifier = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.WEST, labelId, 0, SpringLayout.WEST, textIdentifier);
		springLayout.putConstraint(SpringLayout.EAST, textIdentifier, 100, SpringLayout.EAST, textAlias);
		textIdentifier.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.NORTH, textIdentifier, 0, SpringLayout.NORTH, textAlias);
		springLayout.putConstraint(SpringLayout.WEST, textIdentifier, 6, SpringLayout.EAST, textAlias);
		getContentPane().add(textIdentifier);
		textIdentifier.setColumns(10);
		
		labelIcon = new JLabel("");
		labelIcon.setHorizontalAlignment(SwingConstants.CENTER);
		springLayout.putConstraint(SpringLayout.WEST, labelIcon, 281, SpringLayout.WEST, getContentPane());
		labelIcon.setMaximumSize(new Dimension(32, 32));
		springLayout.putConstraint(SpringLayout.NORTH, labelIcon, 6, SpringLayout.SOUTH, textDescription);
		springLayout.putConstraint(SpringLayout.SOUTH, labelIcon, -23, SpringLayout.NORTH, buttonCancel);
		getContentPane().add(labelIcon);
		
		buttonLoadImage = new JButton("");
		springLayout.putConstraint(SpringLayout.EAST, labelIcon, -2, SpringLayout.WEST, buttonLoadImage);
		buttonLoadImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLoadIcon();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonLoadImage, 10, SpringLayout.SOUTH, textDescription);
		buttonLoadImage.setMinimumSize(new Dimension(25, 25));
		buttonLoadImage.setMaximumSize(new Dimension(25, 25));
		buttonLoadImage.setMargin(new Insets(2, 0, 0, 0));
		buttonLoadImage.setPreferredSize(new Dimension(25, 25));
		springLayout.putConstraint(SpringLayout.WEST, buttonLoadImage, 353, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonLoadImage, 0, SpringLayout.EAST, buttonCancel);
		getContentPane().add(buttonLoadImage);
		
		buttonLoadFromFile = new JButton("");
		springLayout.putConstraint(SpringLayout.WEST, buttonLoadFromFile, 0, SpringLayout.WEST, buttonLoadImage);
		springLayout.putConstraint(SpringLayout.EAST, buttonLoadFromFile, 0, SpringLayout.EAST, buttonLoadImage);
		buttonLoadFromFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onLoadIconFromFile();
			}
		});
		buttonLoadFromFile.setMinimumSize(new Dimension(25, 25));
		buttonLoadFromFile.setMaximumSize(new Dimension(25, 25));
		buttonLoadFromFile.setMargin(new Insets(2, 0, 0, 0));
		buttonLoadFromFile.setPreferredSize(new Dimension(25, 25));
		springLayout.putConstraint(SpringLayout.NORTH, buttonLoadFromFile, 6, SpringLayout.SOUTH, buttonLoadImage);
		getContentPane().add(buttonLoadFromFile);
		
		buttonResetImage = new JButton("");
		springLayout.putConstraint(SpringLayout.WEST, buttonResetImage, 0, SpringLayout.WEST, buttonLoadImage);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonResetImage, -6, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonResetImage, 0, SpringLayout.EAST, buttonCancel);
		buttonResetImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onResetIcon();
			}
		});
		buttonResetImage.setMinimumSize(new Dimension(25, 25));
		buttonResetImage.setMaximumSize(new Dimension(25, 25));
		buttonResetImage.setMargin(new Insets(0, 0, 0, 0));
		buttonResetImage.setPreferredSize(new Dimension(25, 25));
		getContentPane().add(buttonResetImage);
		
		checkBoxStartLanguage = new JCheckBox("org.multipage.translator.textStartLanguage");
		checkBoxStartLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onStartCheckBox();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkBoxStartLanguage, 6, SpringLayout.SOUTH, textAlias);
		springLayout.putConstraint(SpringLayout.WEST, checkBoxStartLanguage, 10, SpringLayout.WEST, textAlias);
		getContentPane().add(checkBoxStartLanguage);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		// Localize dialog components.
		localize();
		// Center dialog.
		Utility.centerOnScreen(this);
		// Set icons.
		setIcons();
		// Initialize description.
		textDescription.setText(Resources.getString("org.multipage.translator.textNewLanguage"));
		textDescription.selectAll();
		// Initialize identifier.
		textIdentifier.setText(Resources.getString("org.multipage.translator.textUnknown"));
		textIdentifier.setEditable(false);
		// Initialize is start checkbox.
		checkBoxStartLanguage.setSelected(isSart);
	}

	/**
	 * Localize dialog components.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
		Utility.localize(labelDescription);
		Utility.localize(labelAlias);
		Utility.localize(labelId);
		Utility.localize(checkBoxStartLanguage);
	}

	/**
	 * Get confirmed flag.
	 * @return
	 */
	public boolean isConfirmed() {

		return confirmed;
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		setIconImage(Images.getImage("org/multipage/translator/images/main_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/translator/images/cancel_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/translator/images/ok_icon.png"));
		labelIcon.setIcon(Images.getIcon("org/multipage/translator/images/unknown.png"));
		buttonLoadFromFile.setIcon(Images.getIcon("org/multipage/translator/images/open.png"));
		buttonLoadImage.setIcon(Images.getIcon("org/multipage/translator/images/load_icon.png"));
		buttonResetImage.setIcon(Images.getIcon("org/multipage/translator/images/unknown_icon.png"));
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirmed = false;
		dispose();
	}
	
	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Check description and alias.
		description = textDescription.getText();
		alias = textAlias.getText();
		
		if (description.isEmpty()) {
			Utility.show(this, "org.multipage.translator.messageDescriptionCannotBeEmpty");
			return;
		}
		
		if (alias.isEmpty()) {
			Utility.show(this, "org.multipage.translator.messageAliasCannotBeEmpty");
			return;
		}
		
		confirmed = true;
		dispose();
	}

	/**
	 * Set alias.
	 * @param alias
	 */
	public void setAlias(String alias) {

		textAlias.setText(alias);
	}

	/**
	 * Get description.
	 * @return
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * Get alias.
	 * @return
	 */
	public String getAlias() {

		return alias;
	}

	/**
	 * Load icon.
	 */
	protected void onLoadIconFromFile() {
		
		// Load icon from disk.
		image = TranslatorUtilities.loadImageFromDisk(this);
		if (image == null) {
			return;
		}
		// Set icon.
		labelIcon.setIcon(new ImageIcon(image));
	}

	/**
	 * Reset icon.
	 */
	protected void onResetIcon() {

		// Ask user.
		if (JOptionPane.showConfirmDialog(this,
				Resources.getString("org.multipage.translator.messageRemoveLanguageIcon"))
				!= JOptionPane.YES_OPTION) {
			return;
		}
		
		image = null;
		labelIcon.setIcon(Images.getIcon("org/multipage/translator/images/unknown.png"));
	}

	/**
	 * Get image.
	 * @return
	 */
	public BufferedImage getImage() {

		return image;
	}

	/**
	 * On load icon.
	 */
	protected void onLoadIcon() {
		
		image = LoadFlagDialog.showDialog(parentWindow);
		if (image == null) {
			return;
		}
		// Set icon.
		labelIcon.setIcon(new ImageIcon(image));
	}

	/**
	 * Set description.
	 * @param text
	 */
	public void setDescription(String text) {

		textDescription.setText(text);
	}

	/**
	 * Set ID.
	 * @param id
	 */
	public void setId(long id) {

		this.id = id;
		textIdentifier.setText(String.valueOf(id));
	}

	/**
	 * Set image.
	 * @param image
	 */
	public void setImage(BufferedImage image) {

		this.image = image;
		if (image != null) {
			labelIcon.setIcon(new ImageIcon(image));
		}
	}

	/**
	 * On start check box.
	 */
	protected void onStartCheckBox() {

		boolean isStart = checkBoxStartLanguage.isSelected();
		
		// If it is default language and the start flag
		// should be removed, inform user and exit.
		if (id == 0L && !isStart) {
			checkBoxStartLanguage.setSelected(true);
			Utility.show(this, "org.multipage.translator.textCannotResetDefaultLanguageStartFlag");
			return;
		}
		
		this.isSart = isStart;
	}

	/**
	 * @return the isSart
	 */
	public boolean isSart() {
		return isSart;
	}

	/**
	 * @param isSart the isSart to set
	 */
	public void setSart(boolean isSart) {
		this.isSart = isSart;
		checkBoxStartLanguage.setSelected(isSart);
	}
}
