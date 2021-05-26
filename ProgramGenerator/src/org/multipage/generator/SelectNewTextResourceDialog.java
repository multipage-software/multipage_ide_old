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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

import com.maclan.MimeType;

/**
 * Option class.
 */
class Option {
	
	String labelText;
	String type;
	String extension;
	String contentText;
	
	JRadioButton radioButton;
	
	/**
	 * Constructor.
	 */
	Option(String labelText, String mimeType, String extension, String contentText) {
		
		this.labelText = labelText;
		this.type = mimeType;
		this.extension = extension;
		this.contentText = contentText;
	}
}
/**
 * 
 * @author
 *
 */
public class SelectNewTextResourceDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Options table.
	 */
	private static final Option [] options = {
		
		new Option("org.multipage.generator.textOptionEmpty", "text/plain", "txt",
				""),
		
		new Option("org.multipage.generator.textOptionHtml", "text/html", "htm",
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
				"<HTML>\n" +
				"<HEAD>\n" +
				"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=9\" />\n" + 
				"    [@TAG startArea, slot=#head]\n" +
				"</HEAD>\n" +
				"<BODY>\n" +
				"    [@TAG startArea, slot=#body]\n" +
				"</BODY>\n" +
				"</HTML>"),
				
		new Option("org.multipage.generator.textOptionJavaScript", "text/javascript", "js",
				"// JavaScript code.\n" +
				"\n"
				),
				
		new Option("org.multipage.generator.textOptionCss", "text/css", "css",
				"/* Cascading Style Sheets */ \n" +
				"\n"
				),
				
		new Option("org.multipage.generator.textOptionXml", "text/xml", "xml",
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
				"\n"
				),
				
		new Option("org.multipage.generator.textOptionX3D", "text/xml", "xml",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.2//EN\" \n" +
				"    \"http://www.web3d.org/specifications/x3d-3.2.dtd\">\n" +
 
				"<X3D profile=\"Interchange\" version=\"3.2\"\n" +
				"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"    xsd:noNamespaceSchemaLocation=\"http://www.web3d.org/specifications/x3d-3.2.xsd\">\n" +
				"<Scene>\n" +
				"\n" +
				"</Scene>\n" +
				"</X3D>")
	};
	
	/**
	 * Bounds.
	 */
	private static Rectangle bounds;

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Load data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream) 
			throws IOException, ClassNotFoundException {
		
		Object object = inputStream.readObject();
		if (!(object instanceof Rectangle)) {
			throw new ClassNotFoundException();
		}
		bounds = (Rectangle) object;
	}

	/**
	 * Save data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream) 
			throws IOException {
		
		outputStream.writeObject(bounds);
	}
		
	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (!bounds.isEmpty()) {
			setBounds(bounds);
		}
		else {
			Utility.centerOnScreen(this);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		bounds = getBounds();
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panel;
	private JButton buttonOk;
	private JButton buttonCancel;
	private JPanel panelMain;
	private JLabel label;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JPanel panelOptions;

	/**
	 * Show dialog.
	 * @param parent
	 * @param text 
	 * @param mimeType 
	 */
	public static boolean showDialog(Component parent, Obj<String> text, Obj<MimeType> mimeType) {
		
		SelectNewTextResourceDialog dialog = new SelectNewTextResourceDialog(Utility.findWindow(parent));
		
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			// Get selected option.
			Option option = dialog.getSelectedOption();
			
			text.ref = option.contentText;
			mimeType.ref = GeneratorUtilities.getMimeType(option.type, option.extension);
		}
		
		return dialog.confirm;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SelectNewTextResourceDialog(Window parentWindow) {
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
		setTitle("org.multipage.generator.textSelectNewResourceContent");
		
		setBounds(100, 100, 450, 334);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		buttonOk = new JButton("textOk");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		label = new JLabel("org.multipage.generator.textSelectNewContent");
		sl_panelMain.putConstraint(SpringLayout.NORTH, label, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, label, 10, SpringLayout.WEST, panelMain);
		panelMain.add(label);
		
		panelOptions = new JPanel();
		sl_panelMain.putConstraint(SpringLayout.NORTH, panelOptions, 6, SpringLayout.SOUTH, label);
		sl_panelMain.putConstraint(SpringLayout.WEST, panelOptions, 50, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, panelOptions, 0, SpringLayout.SOUTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, panelOptions, -10, SpringLayout.EAST, panelMain);
		panelMain.add(panelOptions);
		panelOptions.setLayout(null);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		loadDialog();
		
		localize();
		setIcons();
		
		createOptions();
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(label);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
	}

	/**
	 * Create options.
	 */
	private void createOptions() {
		
		Rectangle radioBounds = new Rectangle(5, 5, 800, 23);
		final int yIncrement = radioBounds.height + 10;
		
		boolean isFirstOption = true;
		
		// Do loop for all options.
		for (Option option : options) {
			
			// Create new radio button.
			JRadioButton radioButton = new JRadioButton(Resources.getString(option.labelText));
			option.radioButton = radioButton;
			if (isFirstOption) {
				radioButton.setSelected(true);
				isFirstOption = false;
			}
			// Add it to the group.
			buttonGroup.add(radioButton);
			// Set radio button position.
			radioButton.setBounds(radioBounds);
			// Add it to the option panel.
			panelOptions.add(radioButton);
			
			// Increment Y position.
			radioBounds.y += yIncrement;
		}
	}
	
	/**
	 * Get selected option.
	 * @return
	 */
	private Option getSelectedOption() {

		// Do loop for all options.
		for (Option option : options) {
			
			if (option.radioButton != null && option.radioButton.isSelected()) {
				return option;
			}
		}
		
		return options[0];
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		saveDialog();
		
		confirm = true;
		dispose();
	}
}
