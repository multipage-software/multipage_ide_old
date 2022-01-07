package program.builder.dynamic;

import general.gui.*;
import general.util.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class ConfirmStepCreation extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Return values.
	 */
	public static final int CANCEL = 0;
	public static final int BRANCH = 1;
	public static final int CONNECTED = 2;

	private final JPanel contentPanel = new JPanel();
	
	/**
	 * Procedure description.
	 */
	private String description = "";
	
	/**
	 * Confirmation value.
	 */
	private int confirmed = CANCEL;
	private JRadioButton radioBranch;
	private JLabel labelSelectMethod;
	private JLabel labelStepDesription;
	private JRadioButton radioConnected;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField textField;
	private JLabel methodImage;

	private boolean methodSelection = true;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Show dialog.
	 * @param parentFrame
	 * @param description
	 * @param methodSelection
	 * @return
	 */
	public static int showConfirmDialog(JFrame parentFrame, Obj<String> description,
			boolean methodSelection) {
		
		ConfirmStepCreation dialog = new ConfirmStepCreation(parentFrame,
				methodSelection);
		dialog.setVisible(true);
		
		description.ref = dialog.description;
		return dialog.confirmed;
	}

	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setTitle("textConfirmNewStepCreation");
		setResizable(false);
		setIconImage(Images.getImage("program/basic/images/main_icon.png"));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		setBounds(100, 100, 344, 242);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		labelSelectMethod = new JLabel("textSelectNewStepInsertMethod");
		labelSelectMethod.setBounds(10, 67, 318, 14);
		contentPanel.add(labelSelectMethod);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				{
					cancelButton = new JButton("textCancel");
					cancelButton.setMargin(new Insets(0, 4, 0, 0));
					cancelButton.setHorizontalAlignment(SwingConstants.LEADING);
					cancelButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));
					cancelButton.setPreferredSize(new Dimension(80, 25));
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCancel();
						}
					});
					okButton = new JButton("textOk");
					buttonPane.add(okButton);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onOk();
						}
					});
					okButton.setMargin(new Insets(0, 4, 0, 0));
					okButton.setHorizontalAlignment(SwingConstants.LEADING);
					okButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
					okButton.setPreferredSize(new Dimension(80, 25));
					okButton.setActionCommand("");
					getRootPane().setDefaultButton(okButton);
					cancelButton.setActionCommand("Cancel");
					buttonPane.add(cancelButton);
				}
			}
		}

		{
			radioBranch = new JRadioButton("textNewStepBranch");
			radioBranch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onRadioButton();
				}
			});
			buttonGroup.add(radioBranch);
			radioBranch.setBounds(20, 114, 200, 23);
			contentPanel.add(radioBranch);
		}
	}

	/**
	 * Create the dialog.
	 * @param parentFrame 
	 * @param methodSelection 
	 */
	public ConfirmStepCreation(JFrame parentFrame, boolean methodSelection) {
		super(parentFrame, true);
		this.methodSelection  = methodSelection;
		initComponents();

		
		radioConnected = new JRadioButton("textNewStepConnected");
		radioConnected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRadioButton();
			}
		});
		buttonGroup.add(radioConnected);
		radioConnected.setBounds(20, 88, 200, 23);
		contentPanel.add(radioConnected);
		
		labelStepDesription = new JLabel("textInsertNewStepDescription");
		labelStepDesription.setBounds(10, 11, 424, 14);
		contentPanel.add(labelStepDesription);
		
		textField = new TextFieldEx();
		textField.setBounds(10, 36, 318, 20);
		contentPanel.add(textField);
		textField.setColumns(10);
		
		methodImage = new JLabel("");
		methodImage.setBounds(226, 73, 64, 64);
		contentPanel.add(methodImage);
		
		postConstruct();
	}

	/**
	 * On radio button.
	 */
	protected void onRadioButton() {

		loadMethodImage();
	}

	/**
	 * Post construction.
	 */
	private void postConstruct() {
		
		// Localize texts.
		Utility.localize(this);
		Utility.localize(labelSelectMethod);
		Utility.localize(labelStepDesription);
		Utility.localize(radioConnected);
		Utility.localize(radioBranch);
		Utility.localize(okButton);
		Utility.localize(cancelButton);
		
		// Enable disable radio buttons.
		radioConnected.setEnabled(methodSelection);
		radioBranch.setEnabled(methodSelection);
		labelSelectMethod.setEnabled(methodSelection);
		
		// Center the dialog.
		Utility.centerOnScreen(this);
		
		// Set initial procedure description.
		textField.setText(Resources.getString("textNewProcedure"));
		textField.selectAll();
		
		radioConnected.setSelected(true);
		
		// Load method image.
		loadMethodImage();
	}

	/**
	 * Loads method image.
	 */
	private void loadMethodImage() {

		if (methodSelection) {
			
			boolean connected = radioConnected.isSelected();
			Icon icon = Images.getIcon("program/builder/dynamic/images/" +
					(connected ? "connected_step.png" : "branch_step.png"));
			methodImage.setIcon(icon);
		}
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		confirmed = radioBranch.isSelected() ? BRANCH : CONNECTED;
		description = textField.getText();
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirmed = CANCEL;
		dispose();
	}
	public JButton getOkButton() {
		return okButton;
	}
	public JButton getCancelButton() {
		return cancelButton;
	}
}
