/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */
package org.multipage.generator;

import javax.swing.*;
import org.multipage.gui.*;
import org.multipage.util.Obj;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Cloned diagram dialog.
 * @author user
 *
 */
public class ClonedDiagramDialog extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serialized window type.
	 */
	private static ContentOfTab.Type windowTypeState;

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		windowTypeState = ContentOfTab.Type.treeView;
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(windowTypeState);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(StateInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		windowTypeState = Utility.readInputStreamObject(inputStream, ContentOfTab.Type.class);
	}
	
	/**
	 * Dialog confirmed flag.
	 */
	private boolean confirmed = false;
	
	/**
	 * Controls.
	 */
	private JLabel labelName;
	private TextFieldEx textCaption;
	private JRadioButton radioDiagram;
	private JRadioButton radioAreaTree;
	private JButton buttonCancel;
	private JButton buttonOk;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Show dialog.
	 * @param parent
	 * @param caption
	 * @return
	 */
	public static String showDialog(Component parent, String caption, Obj<ContentOfTab.Type> type) {
		
		ClonedDiagramDialog dialog = new ClonedDiagramDialog(Utility.findWindow(parent));
		dialog.setProperties(caption);
		dialog.setVisible(true);
		
		if (dialog.confirmed) {
			type.ref = dialog.getCloneType();
			String captionResult = dialog.getCaption();
			return captionResult;
		}
		return null;
	}
	
	/**
	 * Create the dialog.
	 * @param window 
	 */
	public ClonedDiagramDialog(Window window) {
		super(window, ModalityType.DOCUMENT_MODAL);
		
		// Initialize components.
		initComponents();
		
		// Post creation.
		postCreate(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("org.multipage.generator.messageGetAreasEditorCloneName");
		setBounds(100, 100, 309, 224);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelName = new JLabel("org.multipage.generator.textNewTabCaption");
		springLayout.putConstraint(SpringLayout.NORTH, labelName, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelName, 16, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelName);
		
		textCaption = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.NORTH, textCaption, 6, SpringLayout.SOUTH, labelName);
		springLayout.putConstraint(SpringLayout.WEST, textCaption, 0, SpringLayout.WEST, labelName);
		springLayout.putConstraint(SpringLayout.EAST, textCaption, -16, SpringLayout.EAST, getContentPane());
		getContentPane().add(textCaption);
		textCaption.setColumns(10);
		
		radioDiagram = new JRadioButton("org.multipage.generator.textCloneDiagram");
		buttonGroup.add(radioDiagram);
		springLayout.putConstraint(SpringLayout.NORTH, radioDiagram, 16, SpringLayout.SOUTH, textCaption);
		springLayout.putConstraint(SpringLayout.WEST, radioDiagram, 70, SpringLayout.WEST, getContentPane());
		getContentPane().add(radioDiagram);
		
		radioAreaTree = new JRadioButton("org.multipage.generator.textCloneTree");
		buttonGroup.add(radioAreaTree);
		springLayout.putConstraint(SpringLayout.NORTH, radioAreaTree, 6, SpringLayout.SOUTH, radioDiagram);
		springLayout.putConstraint(SpringLayout.WEST, radioAreaTree, 0, SpringLayout.WEST, radioDiagram);
		getContentPane().add(radioAreaTree);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -16, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -16, SpringLayout.EAST, getContentPane());
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		getContentPane().add(buttonCancel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -10, SpringLayout.WEST, buttonCancel);
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
	}
	
	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		localize();
		setIcons();
		initSelection();
		
		Utility.centerOnScreen(this);
		
		loadDialog();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		switch (windowTypeState) {
		
		case diagram:
			radioDiagram.setSelected(true);
			break;
			
		case treeView:
		default:
			radioAreaTree.setSelected(true);
		}
	}

	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		windowTypeState = getCloneType();
	}
	
	/**
	 * Initialize selection.
	 */
	private void initSelection() {
		
		radioDiagram.setSelected(true);
	}
	
	/**
	 * Set dialog properties.
	 * @param caption
	 */
	private void setProperties(String caption) {
		
		textCaption.setText(caption);
	}
	
	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelName);
		Utility.localize(radioDiagram);
		Utility.localize(radioAreaTree);
		Utility.localize(buttonCancel);
		Utility.localize(buttonOk);
	}
		
	/**
	 * Set components icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}
	
	/**
	 * Get clone type.
	 * @return
	 */
	private ContentOfTab.Type getCloneType() {
		
		if (radioDiagram.isSelected()) {
			return ContentOfTab.Type.diagram;
		}
		else if (radioAreaTree.isSelected()) {
			return ContentOfTab.Type.treeView;
		}
		return ContentOfTab.Type.unknown;
	}
	
	/**
	 * Get caption.
	 * @return
	 */
	private String getCaption() {
		
		String caption = textCaption.getText();
		return caption;
	}
	
	/**
	 * On OK button.
	 */
	protected void onOK() {
		
		confirmed = true;
		dispose();
		
		saveDialog();
	}
	
	/**
	 * On cancel button.
	 */
	protected void onCancel() {
		
		dispose();
		
		saveDialog();
	}
}
