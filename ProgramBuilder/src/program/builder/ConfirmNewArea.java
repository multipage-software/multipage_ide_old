/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.Images;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import org.maclan.Area;

/**
 * 
 * @author
 *
 */
public class ConfirmNewArea extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Confirmation flag.
	 */
	private boolean confirmed = false;

	/**
	 * Area reference.
	 */
	private Area area;

	/**
	 * Inheritance.
	 */
	private Obj<Boolean> inheritance;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private final JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JButton cancelButton;
	private JLabel labelDescription;
	private JTextField textDescription;
	private JCheckBox checkBoxInherit;
	private JPanel buttonPane;
	private JLabel labelAlias;
	private JTextField textAlias;
	private JCheckBox checkBoxVisible;
	private JLabel labelRelationNameSub;
	private JTextField textRelationNameSub;
	private JLabel labelRelationNameSuper;
	private JTextField textRelationNameSuper;
	private JCheckBox checkReadOnly;
	private JCheckBox checkLocalized;
	private JLabel labelFileName;
	private JTextField textFileName;
	private JLabel labelFolder;
	private JTextField textFolder;
	private JCheckBox checkCanImport;
	private JCheckBox checkProjectRoot;

	/**
	 * Launch the dialog.
	 * @param relationNameSub 
	 */
	public static boolean showConfirmDialog(Component parent, Area area,
			Obj<Boolean> inheritance, Obj<String> relationNameSub,
			Obj<String> relationNameSuper) {

		ConfirmNewArea dialog = new ConfirmNewArea(parent);
		dialog.area = area;
		dialog.inheritance = inheritance;
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		if (dialog.confirmed) {
			relationNameSub.ref = dialog.textRelationNameSub.getText();
			relationNameSuper.ref = dialog.textRelationNameSuper.getText();
		}
		
		return dialog.confirmed;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public ConfirmNewArea(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("builder.textInsertAreaDescription");

		initComponents();
		
		// $hide>>$
		// Set icon
		setIconImage(Images.getImage("org/multipage/generator/images/main_icon.png"));
		
		// Localize dialog.
		localize();
		
		// Center the dialog.
		Utility.centerOnScreen(this);
		
		postCreate();
		// $hide<<$
	}
	
	/**
	 * Last creation step.
	 */
	private void postCreate() {
		
		// Initialize text field.
		textDescription.setText(Resources.getString("org.multipage.generator.textNewArea"));
		textDescription.selectAll();
		
		// Set icons.
		okButton.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		cancelButton.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		
		// Set file name components.
		setFileNameComponents();
	}

	/**
	 * Set file name components.
	 */
	private void setFileNameComponents() {
		
		boolean enabled = checkBoxVisible.isSelected();
		
		labelFileName.setEnabled(enabled);
		textFileName.setEnabled(enabled);
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setBounds(100, 100, 470, 464);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		labelDescription = new JLabel("builder.textInsertNewAreaDescriptionLabel");
		labelDescription.setBounds(10, 11, 350, 14);
		contentPanel.add(labelDescription);
		
		textDescription = new TextFieldEx();
		textDescription.setBounds(10, 36, 444, 20);
		contentPanel.add(textDescription);
		textDescription.setColumns(10);
		{
			checkBoxInherit = new JCheckBox("org.multipage.generator.textInheritFromSuperArea");
			checkBoxInherit.setSelected(true);
			checkBoxInherit.setBounds(10, 134, 172, 23);
			contentPanel.add(checkBoxInherit);
		}
		
		labelAlias = new JLabel("builder.textNewAreaAlias");
		labelAlias.setBounds(10, 67, 371, 14);
		contentPanel.add(labelAlias);
		
		textAlias = new JTextField();
		textAlias.setBounds(10, 92, 444, 20);
		contentPanel.add(textAlias);
		textAlias.setColumns(10);
		
		checkBoxVisible = new JCheckBox("builder.textNewAreaVisible");
		checkBoxVisible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangeAreaVisible();
			}
		});
		checkBoxVisible.setSelected(true);
		checkBoxVisible.setBounds(184, 134, 78, 23);
		contentPanel.add(checkBoxVisible);
		
		labelRelationNameSub = new JLabel("org.multipage.generator.textRelationNameSub");
		labelRelationNameSub.setBounds(10, 196, 371, 14);
		contentPanel.add(labelRelationNameSub);
		
		textRelationNameSub = new JTextField();
		textRelationNameSub.setBounds(10, 210, 444, 20);
		contentPanel.add(textRelationNameSub);
		textRelationNameSub.setColumns(10);
		
		labelRelationNameSuper = new JLabel("org.multipage.generator.textRelationNameSuper");
		labelRelationNameSuper.setBounds(10, 241, 371, 14);
		contentPanel.add(labelRelationNameSuper);
		
		textRelationNameSuper = new JTextField();
		textRelationNameSuper.setColumns(10);
		textRelationNameSuper.setBounds(10, 256, 444, 20);
		contentPanel.add(textRelationNameSuper);
		
		checkReadOnly = new JCheckBox("org.multipage.generator.textReadOnly");
		checkReadOnly.setBounds(264, 134, 85, 23);
		contentPanel.add(checkReadOnly);
		
		checkLocalized = new JCheckBox("org.multipage.generator.textLocalized");
		checkLocalized.setSelected(true);
		checkLocalized.setBounds(351, 134, 103, 23);
		contentPanel.add(checkLocalized);
		
		labelFileName = new JLabel("org.multipage.generator.textAreaFileName");
		labelFileName.setBounds(10, 287, 444, 14);
		contentPanel.add(labelFileName);
		
		textFileName = new JTextField();
		textFileName.setColumns(10);
		textFileName.setBounds(10, 302, 444, 20);
		contentPanel.add(textFileName);
		
		labelFolder = new JLabel("org.multipage.generator.textAreasFolder");
		labelFolder.setBounds(10, 333, 444, 14);
		contentPanel.add(labelFolder);
		
		textFolder = new JTextField();
		textFolder.setColumns(10);
		textFolder.setBounds(10, 348, 444, 20);
		contentPanel.add(textFolder);
		
		checkCanImport = new JCheckBox("builder.textCanImport");
		checkCanImport.setBounds(184, 160, 78, 23);
		contentPanel.add(checkCanImport);
		
		checkProjectRoot = new JCheckBox("builder.textProjectRoot");
		checkProjectRoot.setBounds(264, 160, 85, 23);
		contentPanel.add(checkProjectRoot);
		contentPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{textDescription, checkBoxInherit, labelDescription}));
		{
			buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("textOk");
				okButton.setMargin(new Insets(2, 4, 2, 4));
				okButton.setHorizontalAlignment(SwingConstants.LEFT);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOk();
					}
				});
				okButton.setPreferredSize(new Dimension(80, 25));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("textCancel");
				cancelButton.setMargin(new Insets(2, 4, 2, 4));
				cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancel();
					}
				});
				cancelButton.setPreferredSize(new Dimension(80, 25));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{textDescription, textAlias, checkBoxInherit, checkBoxVisible, okButton, cancelButton}));
	}
	
	/**
	 * On change visible area check box.
	 */
	protected void onChangeAreaVisible() {
		
		setFileNameComponents();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {

		confirmed = true;
		
		// Set output.
		String text = textDescription.getText();
		area.setDescription(text);
		area.setAlias(textAlias.getText());
		area.setVisible(checkBoxVisible.isSelected());
		inheritance.ref = checkBoxInherit.isSelected();
		area.setReadOnly(checkReadOnly.isSelected());
		area.setLocalized(checkLocalized.isSelected());
		area.setFileName(textFileName.getText());
		area.setFolder(textFolder.getText());
		area.setCanImport(checkCanImport.isSelected());
		area.setProjectRoot(checkProjectRoot.isSelected());
		
		dispose();
	}

	protected void onCancel() {

		confirmed = false;
		dispose();
	}

	/**
	 * Localize dialog.
	 */
	private void localize() {

		Utility.localize(this);
		Utility.localize(okButton);
		Utility.localize(cancelButton);
		Utility.localize(labelDescription);
		Utility.localize(checkBoxInherit);
		Utility.localize(labelAlias);
		Utility.localize(checkBoxVisible);
		Utility.localize(labelRelationNameSub);
		Utility.localize(labelRelationNameSuper);
		Utility.localize(checkReadOnly);
		Utility.localize(checkLocalized);
		Utility.localize(labelFileName);
		Utility.localize(labelFolder);
		Utility.localize(checkCanImport);
		Utility.localize(checkProjectRoot);
	}
}
