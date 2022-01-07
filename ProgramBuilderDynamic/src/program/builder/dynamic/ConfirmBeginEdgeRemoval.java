package program.builder.dynamic;

import general.gui.Images;
import general.gui.Utility;
import general.util.*;

import java.awt.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 
 * @author
 *
 */
public class ConfirmBeginEdgeRemoval extends JDialog {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Confirm value.
	 */
	private boolean confirm = false;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox removeStartEdge;

	/**
	 * Remove edge.
	 */
	private boolean removeEdge;

	/**
	 * Show the dialog.
	 */
	public static boolean showConfirmDialog(Obj<Boolean> removeStartEdge) {

		ConfirmBeginEdgeRemoval dialog = new ConfirmBeginEdgeRemoval();
		dialog.setVisible(true);
		removeStartEdge.ref = dialog.removeEdge;
		
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 */
	public ConfirmBeginEdgeRemoval() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		initComponents();
		postInitialization();
	}

	private void initComponents() {
		setTitle("textConfirmBeginEdgeRemoval");
		setBounds(100, 100, 339, 169);
		getContentPane().setLayout(null);
		
		removeStartEdge = new JCheckBox("textRemoveStartEdge");
		removeStartEdge.setHorizontalAlignment(SwingConstants.CENTER);
		removeStartEdge.setBounds(0, 38, 323, 23);
		getContentPane().add(removeStartEdge);
		{
			okButton = new JButton("textOk");
			okButton.setBounds(80, 88, 80, 25);
			getContentPane().add(okButton);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onOk();
				}
			});
			okButton.setHorizontalAlignment(SwingConstants.LEFT);
			okButton.setMargin(new Insets(2, 2, 2, 2));
			okButton.setPreferredSize(new Dimension(80, 25));
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		{
			cancelButton = new JButton("textCancel");
			cancelButton.setBounds(170, 88, 80, 25);
			getContentPane().add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onCancel();
				}
			});
			cancelButton.setMargin(new Insets(2, 2, 2, 2));
			cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
			cancelButton.setPreferredSize(new Dimension(80, 25));
			cancelButton.setActionCommand("Cancel");
		}
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {

		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {

		confirm = true;
		removeEdge = removeStartEdge.isSelected();
		dispose();
	}

	/**
	 * After initialization.
	 */
	private void postInitialization() {

		// Set dialog icon.
		setIconImage(Images.getImage("program/basic/images/main_icon.png"));
		// Set icons.
		okButton.setIcon(Images.getIcon("general/gui/images/ok_icon.png"));
		cancelButton.setIcon(Images.getIcon("general/gui/images/cancel_icon.png"));
		// Center dialog.
		Utility.centerOnScreen(this);
		// Localize components.
		Utility.localize(this);
		Utility.localize(okButton);
		Utility.localize(cancelButton);
		Utility.localize(removeStartEdge);
		// Select the check box.
		removeStartEdge.setSelected(true);
	}
}
