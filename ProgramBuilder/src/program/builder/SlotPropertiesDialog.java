/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.gui.*;
import org.multipage.util.Obj;

import java.awt.*;

import javax.swing.*;

import org.maclan.Slot;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 
 * @author
 *
 */
public class SlotPropertiesDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	/**
	 * Access combo box.
	 */
	private AccessComboBox accessCombo;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private final JPanel panelMain = new JPanel();
	private JPanel panelAccess;
	private JCheckBox checkHidden;
	private JCheckBox checkEnableAccess;
	private JCheckBox checkEnableHidden;
	private JButton buttonCancel;
	private JCheckBox checkEnableDefault;
	private JCheckBox checkDefault;
	private JCheckBox checkEnablePreferred;
	private JCheckBox checkPreferred;

	/**
	 * Show dialog.
	 * @param parent
	 * @param hidden 
	 * @param access 
	 * @param isPreferred 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, Obj<Character> access,
			Obj<Boolean> hidden, Obj<Boolean> isDefault, Obj<Boolean> isPreferred) {
		
		access.ref = null;
		hidden.ref = null;
		isDefault.ref = null;
		isPreferred.ref = null;
		
		SlotPropertiesDialog dialog = new SlotPropertiesDialog(Utility.findWindow(parent));
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			access.ref = dialog.getAccessValue();
			hidden.ref = dialog.getHiddenValue();
			isDefault.ref = dialog.getDefaultValue();
			isPreferred.ref = dialog.getPreferredValue();
		}
		
		return dialog.confirm;
	}

	/**
	 * Get access value.
	 * @return
	 */
	private Character getAccessValue() {
		
		if (checkEnableAccess.isSelected()) {
			return accessCombo.getSelectedAccess();
		}
		
		return null;
	}

	/**
	 * Get hidden value.
	 * @return
	 */
	private Boolean getHiddenValue() {
		
		if (checkEnableHidden.isSelected()) {
			return checkHidden.isSelected();
		}
		
		return null;
	}
	
	/**
	 * Get default value.
	 * @return
	 */
	private Boolean getDefaultValue() {
		
		if (checkEnableDefault.isSelected()) {
			return checkDefault.isSelected();
		}
		
		return null;
	}
	
	/**
	 * Get preferred value.
	 * @return
	 */
	private Boolean getPreferredValue() {
		
		if (checkEnablePreferred.isSelected()) {
			return checkPreferred.isSelected();
		}
		
		return null;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SlotPropertiesDialog(Window parentWindow) {
		super(parentWindow, ModalityType.DOCUMENT_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("builder.textSlotPropertiesDialog");
		
		setBounds(100, 100, 289, 393);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		buttonOk.setPreferredSize(new Dimension(80, 25));
		panel.add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		getContentPane().add(panelMain, BorderLayout.CENTER);
		panelMain.setLayout(null);
		
		panelAccess = new JPanel();
		panelAccess.setBounds(80, 43, 149, 23);
		panelMain.add(panelAccess);
		
		checkHidden = new JCheckBox("builder.textSlotCanBeHidden");
		checkHidden.setBounds(80, 112, 163, 23);
		panelAccess.setLayout(new BorderLayout(0, 0));
		panelMain.add(checkHidden);
		
		checkEnableAccess = new JCheckBox("builder.textSetSlotAccesType");
		checkEnableAccess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangedSetAccess();
			}
		});
		checkEnableAccess.setForeground(Color.GRAY);
		checkEnableAccess.setBounds(25, 14, 167, 23);
		checkEnableAccess.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkEnableAccess.setHorizontalTextPosition(SwingConstants.LEADING);
		panelMain.add(checkEnableAccess);
		
		checkEnableHidden = new JCheckBox("builder.textSetSlotHiddenFlag");
		checkEnableHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangedSetHidden();
			}
		});
		checkEnableHidden.setForeground(Color.GRAY);
		checkEnableHidden.setBounds(25, 83, 169, 23);
		checkEnableHidden.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkEnableHidden.setHorizontalTextPosition(SwingConstants.LEADING);
		panelMain.add(checkEnableHidden);
		
		checkEnableDefault = new JCheckBox("builder.textSetSlotDefaultValue");
		checkEnableDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangeSetDefault();
			}
		});
		checkEnableDefault.setHorizontalTextPosition(SwingConstants.LEADING);
		checkEnableDefault.setForeground(Color.GRAY);
		checkEnableDefault.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkEnableDefault.setBounds(25, 147, 169, 23);
		panelMain.add(checkEnableDefault);
		
		checkDefault = new JCheckBox("builder.textSlotIsDefault");
		checkDefault.setBounds(80, 184, 163, 23);
		panelMain.add(checkDefault);
		
		checkEnablePreferred = new JCheckBox("builder.textSetSlotPreferredFlag");
		checkEnablePreferred.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onChangeSetPreferred();
			}
		});
		checkEnablePreferred.setHorizontalTextPosition(SwingConstants.LEADING);
		checkEnablePreferred.setForeground(Color.GRAY);
		checkEnablePreferred.setFont(new Font("Tahoma", Font.ITALIC, 11));
		checkEnablePreferred.setBounds(25, 220, 169, 23);
		panelMain.add(checkEnablePreferred);
		
		checkPreferred = new JCheckBox("builder.textSlotIsPreferred");
		checkPreferred.setBounds(80, 260, 163, 23);
		panelMain.add(checkPreferred);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
		setAccessCombo();
		setDialogInitialState();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(checkHidden);
		Utility.localize(checkEnableAccess);
		Utility.localize(checkEnableHidden);
		Utility.localize(buttonCancel);
		Utility.localize(checkEnableDefault);
		Utility.localize(checkDefault);
		Utility.localize(checkEnablePreferred);
		Utility.localize(checkPreferred);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Set access combo.
	 */
	private void setAccessCombo() {
		
		// Create and attach access combo box.
		accessCombo = new AccessComboBox();
		panelAccess.add(accessCombo);
		// Select access.
		accessCombo.selectItem(Slot.privateAccess);
	}

	/**
	 * Set dialog initial state.
	 */
	private void setDialogInitialState() {
		
		accessCombo.setEnabled(false);
		checkHidden.setEnabled(false);
		checkDefault.setEnabled(false);
		checkPreferred.setEnabled(false);
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		confirm = true;
		
		dispose();
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		confirm = false;
		
		dispose();
	}

	/**
	 * On changed set access.
	 */
	protected void onChangedSetAccess() {
		
		accessCombo.setEnabled(checkEnableAccess.isSelected());
	}

	/**
	 * On changed set hidden.
	 */
	protected void onChangedSetHidden() {
		
		checkHidden.setEnabled(checkEnableHidden.isSelected());
	}
	
	/**
	 * On change set default.
	 */
	protected void onChangeSetDefault() {
		
		checkDefault.setEnabled(checkEnableDefault.isSelected());
	}

	/**
	 * On change preferred enabled.
	 */
	protected void onChangeSetPreferred() {
		
		checkPreferred.setEnabled(checkEnablePreferred.isSelected());
	}
}