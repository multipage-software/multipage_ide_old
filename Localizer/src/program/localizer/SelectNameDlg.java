package program.localizer;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.*;

import org.multipage.gui.*;
import org.multipage.util.Obj;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 
 * @author
 *
 */
public class SelectNameDlg extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JPanel panelMain;
	private JLabel labelSelectLanguageName;
	private JComboBox<String> comboBoxNames;
	private JButton buttonCancel;

	/**
	 * Show dialog.
	 * @param parent
	 * @param selectedName 
	 * @param names 
	 * @param resource
	 */
	public static boolean showDialog(Component parent, LinkedList<String> names, Obj<String> selectedName) {
		
		SelectNameDlg dialog = new SelectNameDlg(Utility.findWindow(parent));
		
		dialog.loadNames(names);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			selectedName.ref = dialog.getSelectedName();
		}
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SelectNameDlg(Window parentWindow) {
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
		setTitle("localizer.textSelectLanguageNameDialog");
		
		setBounds(100, 100, 284, 161);
		
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
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		sl_panel.putConstraint(SpringLayout.SOUTH, buttonCancel, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, buttonOk, 0, SpringLayout.NORTH, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		sl_panel.putConstraint(SpringLayout.EAST, buttonCancel, -10, SpringLayout.EAST, panel);
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panel.add(buttonCancel);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelSelectLanguageName = new JLabel("localizer.textSelectLanguageName");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSelectLanguageName, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSelectLanguageName, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelSelectLanguageName);
		
		comboBoxNames = new JComboBox<String>();
		comboBoxNames.setPreferredSize(new Dimension(28, 25));
		sl_panelMain.putConstraint(SpringLayout.NORTH, comboBoxNames, 6, SpringLayout.SOUTH, labelSelectLanguageName);
		sl_panelMain.putConstraint(SpringLayout.WEST, comboBoxNames, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, comboBoxNames, -10, SpringLayout.EAST, panelMain);
		panelMain.add(comboBoxNames);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		Utility.centerOnScreen(this);
		
		localize();
		setIcons();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelSelectLanguageName);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Load names.
	 * @param names
	 */
	private void loadNames(LinkedList<String> names) {
		
		// Do loop for all names.
		for (String name : names) {
			comboBoxNames.addItem(name);
		}
	}
	
	/**
	 * Get selected name.
	 * @return
	 */
	private String getSelectedName() {
		
		return (String) comboBoxNames.getSelectedItem();
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
}