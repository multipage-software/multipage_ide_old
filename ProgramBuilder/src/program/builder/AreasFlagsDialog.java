/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;

import org.maclan.Area;
import org.maclan.Middle;
import org.maclan.MiddleResult;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreasFlagsDialog extends JDialog {

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
	 * Areas list reference.
	 */
	private LinkedList<Area> areas;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonOk;
	private JButton buttonCancel;
	private JCheckBox checkEnableVisible;
	private JCheckBox checkEnableProtected;
	private JCheckBox checkEnableLocalized;
	private JCheckBox checkVisible;
	private JCheckBox checkProtected;
	private JCheckBox checkLocalized;
	private JCheckBox checkEnableCanImport;
	private JCheckBox checkEnableProjectRoot;
	private JCheckBox checkCanImport;
	private JCheckBox checkProjectRoot;

	/**
	 * Lunch dialog.
	 * @param component
	 * @return
	 */
	public static boolean showDialog(Component component, LinkedList<Area> areas) {

		AreasFlagsDialog dialog = new AreasFlagsDialog(Utility.findWindow(component), areas);
		dialog.setVisible(true);

		return dialog.confirm ;
	}
	
	/**
	 * Create the dialog.
	 * @param window 
	 * @param areas 
	 */
	public AreasFlagsDialog(Window window, LinkedList<Area> areas) {
		super(window, ModalityType.APPLICATION_MODAL);
		this.areas = areas;
		
		initComponents();
		postCreate(); // $hide$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("builder.textAreaFlags");
		setBounds(100, 100, 386, 243);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});
		buttonOk.setPreferredSize(new Dimension(80, 25));
		buttonOk.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonOk);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		springLayout.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		springLayout.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, getContentPane());
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonCancel);
		
		checkEnableVisible = new JCheckBox("builder.textEnableFlag");
		springLayout.putConstraint(SpringLayout.NORTH, checkEnableVisible, 16, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, checkEnableVisible, 94, SpringLayout.WEST, getContentPane());
		checkEnableVisible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckEnabled();
			}
		});
		getContentPane().add(checkEnableVisible);
		
		checkEnableProtected = new JCheckBox("builder.textEnableFlag");
		springLayout.putConstraint(SpringLayout.NORTH, checkEnableProtected, 6, SpringLayout.SOUTH, checkEnableVisible);
		springLayout.putConstraint(SpringLayout.WEST, checkEnableProtected, 0, SpringLayout.WEST, checkEnableVisible);
		springLayout.putConstraint(SpringLayout.SOUTH, checkEnableProtected, 29, SpringLayout.SOUTH, checkEnableVisible);
		checkEnableProtected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckEnabled();
			}
		});
		getContentPane().add(checkEnableProtected);
		
		checkEnableLocalized = new JCheckBox("builder.textEnableFlag");
		springLayout.putConstraint(SpringLayout.NORTH, checkEnableLocalized, 6, SpringLayout.SOUTH, checkEnableProtected);
		springLayout.putConstraint(SpringLayout.WEST, checkEnableLocalized, 0, SpringLayout.WEST, checkEnableProtected);
		checkEnableLocalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckEnabled();
			}
		});
		getContentPane().add(checkEnableLocalized);
		
		checkVisible = new JCheckBox("");
		springLayout.putConstraint(SpringLayout.WEST, checkVisible, 6, SpringLayout.EAST, checkEnableVisible);
		checkVisible.setSelected(true);
		springLayout.putConstraint(SpringLayout.NORTH, checkVisible, 12, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, checkVisible, 33, SpringLayout.NORTH, getContentPane());
		checkVisible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckLabels();
			}
		});
		getContentPane().add(checkVisible);
		
		checkProtected = new JCheckBox("");
		springLayout.putConstraint(SpringLayout.NORTH, checkProtected, 0, SpringLayout.NORTH, checkEnableProtected);
		springLayout.putConstraint(SpringLayout.WEST, checkProtected, 6, SpringLayout.EAST, checkEnableProtected);
		checkProtected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckLabels();
			}
		});
		getContentPane().add(checkProtected);
		
		checkLocalized = new JCheckBox("");
		springLayout.putConstraint(SpringLayout.NORTH, checkLocalized, 0, SpringLayout.NORTH, checkEnableLocalized);
		springLayout.putConstraint(SpringLayout.WEST, checkLocalized, 0, SpringLayout.WEST, checkProtected);
		checkLocalized.setSelected(true);
		checkLocalized.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckLabels();
			}
		});
		getContentPane().add(checkLocalized);
		
		checkEnableCanImport = new JCheckBox("builder.textEnableFlag");
		checkEnableCanImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckEnabled();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkEnableCanImport, 6, SpringLayout.SOUTH, checkEnableLocalized);
		springLayout.putConstraint(SpringLayout.WEST, checkEnableCanImport, 0, SpringLayout.WEST, checkEnableLocalized);
		getContentPane().add(checkEnableCanImport);
		
		checkEnableProjectRoot = new JCheckBox("builder.textEnableFlag");
		checkEnableProjectRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckEnabled();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkEnableProjectRoot, 6, SpringLayout.SOUTH, checkEnableCanImport);
		springLayout.putConstraint(SpringLayout.WEST, checkEnableProjectRoot, 0, SpringLayout.WEST, checkEnableCanImport);
		getContentPane().add(checkEnableProjectRoot);
		
		checkCanImport = new JCheckBox("");
		checkCanImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckLabels();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkCanImport, 0, SpringLayout.NORTH, checkEnableCanImport);
		springLayout.putConstraint(SpringLayout.WEST, checkCanImport, 0, SpringLayout.WEST, checkLocalized);
		getContentPane().add(checkCanImport);
		
		checkProjectRoot = new JCheckBox("");
		checkProjectRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCheckLabels();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkProjectRoot, 6, SpringLayout.SOUTH, checkEnableCanImport);
		springLayout.putConstraint(SpringLayout.WEST, checkProjectRoot, 0, SpringLayout.WEST, checkCanImport);
		getContentPane().add(checkProjectRoot);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		Utility.centerOnScreen(this);
		localize();
		setIcons();
		setCheckEnabled();
		setCheckLabels();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(checkEnableVisible);
		Utility.localize(checkEnableProtected);
		Utility.localize(checkEnableLocalized);
		Utility.localize(checkEnableCanImport);
		Utility.localize(checkEnableProjectRoot);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}
	
	/**
	 * On OK button.
	 */
	protected void onOk() {
		
		// Confirm changes.
		if (JOptionPane.showConfirmDialog(this,
				String.format(Resources.getString("builder.messageConfirmAreaFlagsChanges"), areas.size()))
				!= JOptionPane.YES_OPTION) {
			
			dispose();
			return;
		}
		
		boolean visibleEnabled = checkEnableVisible.isSelected();
		boolean readOnlyEnabled = checkEnableProtected.isSelected();
		boolean localizedEnabled = checkEnableLocalized.isSelected();
		boolean canImportEnabled = checkEnableCanImport.isSelected();
		boolean projectRootEnabled = checkEnableProjectRoot.isSelected();
		
		// If nothing selected, exit the dialog.
		if (!visibleEnabled && !readOnlyEnabled && !localizedEnabled && !canImportEnabled && !projectRootEnabled) {
			dispose();
			return;
		}
		
		boolean visible = checkVisible.isSelected();
		boolean readOnly = checkProtected.isSelected();
		boolean localized = checkLocalized.isSelected();
		
		boolean canImport = checkCanImport.isSelected();
		boolean projectRoot = checkProjectRoot.isSelected();
		
		Properties login = ProgramBasic.getLoginProperties();
		Middle middle = ProgramBasic.getMiddle();
		
		// Login to database.
		MiddleResult result = middle.login(login);
		if (result.isOK()) {
			for (Area area : areas) {
				long areaId = area.getId();
				
				// Change visible flag.
				if (visibleEnabled) {
					result = middle.updateAreaVisibility(areaId, visible);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Change read only flag.
				if (readOnlyEnabled) {
					result = middle.updateAreaReadOnly(areaId, readOnly);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Change localized flag.
				if (localizedEnabled) {
					result = middle.updateAreaLocalized(areaId, localized);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Change can import flag.
				if (canImportEnabled) {
					result = middle.updateAreaCanImport(areaId, canImport);
					if (result.isNotOK()) {
						break;
					}
				}
				
				// Change project root flag.
				if (projectRootEnabled) {
					result = middle.updateAreaProjectRoot(areaId, projectRoot);
					if (result.isNotOK()) {
						break;
					}
				}
			}
			
			// Logout from database.
			MiddleResult logoutResult = middle.logout(result);
			if (result.isOK()) {
				result = logoutResult;
			}
		}
		
		// On error inform user.
		if (result.isNotOK()) {
			result.show(this);
		}
		
		updateInformation();
		
		confirm = true;
		dispose();
	}
	
	/**
	 * Update all data.
	 */
	private void updateInformation() {
		
		// Transmit the "update all" signal.
		//ConditionalEvents.transmit(this, Signal.updateAll);
	}

	/**
	 * Set check labels.
	 */
	private void setCheckLabels() {
		
		Color green = new Color(0, 128, 0);
		Color red = Color.RED;
		
		checkVisible.setText(Resources.getString(
				checkVisible.isSelected() ? "builder.textAreaIsVisible" : "builder.textAreaNotVisible"));
		checkVisible.setForeground(checkVisible.isSelected() ? green : red);

		checkProtected.setText(Resources.getString(
				checkProtected.isSelected() ? "builder.textAreaIsProtected" : "builder.textAreaNotProtected"));
		checkProtected.setForeground(checkProtected.isSelected() ? green : red);

		checkLocalized.setText(Resources.getString(
				checkLocalized.isSelected() ? "builder.textAreaIsLocalized" : "builder.textAreaNotLocalized"));
		checkLocalized.setForeground(checkLocalized.isSelected() ? green : red);
		
		checkCanImport.setText(Resources.getString(
				checkCanImport.isSelected() ? "builder.textAreaCanImport" : "builder.textAreaCannotImport"));
		checkCanImport.setForeground(checkCanImport.isSelected() ? green : red);

		checkProjectRoot.setText(Resources.getString(
				checkProjectRoot.isSelected() ? "builder.textAreaIsProjectRoot" : "builder.textAreaIsNotProjectRoot"));
		checkProjectRoot.setForeground(checkProjectRoot.isSelected() ? green : red);
	}

	/**
	 * Set check enabled.
	 */
	protected void setCheckEnabled() {
		
		checkVisible.setEnabled(checkEnableVisible.isSelected());
		checkProtected.setEnabled(checkEnableProtected.isSelected());
		checkLocalized.setEnabled(checkEnableLocalized.isSelected());
		checkCanImport.setEnabled(checkEnableCanImport.isSelected());
		checkProjectRoot.setEnabled(checkEnableProjectRoot.isSelected());
	}
}
