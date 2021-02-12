/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;

import org.multipage.util.*;

import java.awt.event.*;
import java.io.*;
import java.text.Collator;
import java.util.*;

/**
 * 
 * @author
 *
 */
public class SelectFontNameDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Font types.
	 */
	private static final int GENERIC_NAME = 0;
	private static final int FAMILY_NAME = 1;
	
	/**
	 * Font name class.
	 */
	private class FontName {
		
		String name;
		int type;
		
		/**
		 * Constructor.
		 */
		FontName(String name, int type) {
			
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();
	private static LinkedList<String> familyFonts = new LinkedList<String>();

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
		familyFonts = new LinkedList<String>();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
		outputStream.writeObject(familyFonts);
	}
	
	/**
	 * Load serialized states.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void serializeData(ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		
		bounds = Utility.readInputStreamObject(inputStream, Rectangle.class);
		familyFonts = Utility.readInputStreamObject(inputStream, LinkedList.class);
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * List model.
	 */
	private DefaultListModel<FontName> model;
	
	/**
	 * Output font name.
	 */
	private String outputName;
	
	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelFontNames;
	private JScrollPane scrollPane;
	private JLabel labelNewFont;
	private JTextField textFontName;
	private JButton buttonAddFont;
	private JList<FontName> list;
	private JPopupMenu popupMenu;
	private JMenuItem menuRename;
	private JMenuItem menuRemove;

	/**
	 * Show dialog.
	 * @param parent
	 * @return
	 */
	public static String showDialog(Component parent) {
		
		SelectFontNameDialog dialog = new SelectFontNameDialog(parent);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.outputName;
		}
		return null;
	}
	
	/**
	 * Create the dialog.
	 * @param parent 
	 */
	public SelectFontNameDialog(Component parent) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.gui.textSelectFontName");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 450, 346);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		buttonCancel = new JButton("textCancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
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
		buttonOk.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonOk, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonOk, -6, SpringLayout.WEST, buttonCancel);
		getContentPane().add(buttonOk);
		
		labelFontNames = new JLabel("org.multipage.gui.textFontNames");
		springLayout.putConstraint(SpringLayout.NORTH, labelFontNames, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelFontNames, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelFontNames);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelFontNames);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		labelNewFont = new JLabel("org.multipage.gui.textInsertFont");
		springLayout.putConstraint(SpringLayout.WEST, labelNewFont, 0, SpringLayout.WEST, labelFontNames);
		springLayout.putConstraint(SpringLayout.SOUTH, labelNewFont, -55, SpringLayout.SOUTH, getContentPane());
		getContentPane().add(labelNewFont);
		
		textFontName = new TextFieldEx();
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, textFontName);
		
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		
		popupMenu = new JPopupMenu();
		addPopup(list, popupMenu);
		
		menuRename = new JMenuItem("org.multipage.gui.textRenameFontName");
		menuRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRename();
			}
		});
		popupMenu.add(menuRename);
		
		menuRemove = new JMenuItem("org.multipage.gui.textRemoveFontName");
		menuRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRemove();
			}
		});
		popupMenu.add(menuRemove);
		textFontName.setPreferredSize(new Dimension(6, 22));
		textFontName.setMinimumSize(new Dimension(6, 22));
		springLayout.putConstraint(SpringLayout.NORTH, textFontName, -3, SpringLayout.NORTH, labelNewFont);
		springLayout.putConstraint(SpringLayout.WEST, textFontName, 6, SpringLayout.EAST, labelNewFont);
		getContentPane().add(textFontName);
		textFontName.setColumns(30);
		
		buttonAddFont = new JButton("");
		buttonAddFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddFont();
			}
		});
		buttonAddFont.setMargin(new Insets(0, 0, 0, 0));
		buttonAddFont.setPreferredSize(new Dimension(22, 22));
		springLayout.putConstraint(SpringLayout.NORTH, buttonAddFont, 0, SpringLayout.NORTH, textFontName);
		springLayout.putConstraint(SpringLayout.WEST, buttonAddFont, 0, SpringLayout.EAST, textFontName);
		getContentPane().add(buttonAddFont);
	}

	/**
	 * On remove.
	 */
	protected void onRemove() {
		
		int index = list.getSelectedIndex();
		FontName fontName = list.getSelectedValue();
		
		if (fontName == null) {
			Utility.show(this, "org.multipage.gui.messageSelectSingleFontName");
			return;
		}
		
		if (fontName.type == GENERIC_NAME) {
			Utility.show(this, "org.multipage.gui.messageCannotRemoveGenericFontName");
			return;
		}
		
		// Remove item.
		if (Utility.askParam(this, "org.multipage.gui.messageRemoveSelectedFontName", fontName.name)) {
			model.remove(index);
		}
	}

	/**
	 * On rename.
	 */
	protected void onRename() {
		
		FontName fontName = list.getSelectedValue();
		if (fontName == null) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleFontName");
			return;
		}
		
		if (fontName.type == GENERIC_NAME) {
			Utility.show(this, "org.multipage.gui.messageCannotRenameGenericFontName");
			return;
		}
		
		// Get new font name.
		String name = Utility.input(this, "org.multipage.gui.messageInsertNewFontName", fontName.name);
		if (name != null && !name.isEmpty()) {
			
			fontName.name = name;
			
			scrollPane.revalidate();
			scrollPane.repaint();
		}
	}

	/**
	 * On add font.
	 */
	protected void onAddFont() {
		
		String name = textFontName.getText();
		if (name.isEmpty()) {
			Utility.show(this, "org.multipage.gui.messageInsertFontName");
			return;
		}
		
		if (existsFont(name, FAMILY_NAME)) {
			Utility.show(this, "org.multipage.gui.messageFontNameAlreadyExists");
			return;
		}
		
		addFont(name);
		
		list.ensureIndexIsVisible(model.getSize() - 1);
	}
	
	/**
	 * Add font.
	 * @param name
	 * @return
	 */
	private boolean addFont(String name) {
		
		if (name.isEmpty()) {
			return false;
		}
		
		// Add new element.
		if (!existsFont(name, FAMILY_NAME)) {
			model.addElement(new FontName(name, FAMILY_NAME));
		}

		return true;
	}

	/**
	 * Returns true value if a font already exists.
	 * @param name
	 * @param type
	 * @return
	 */
	private boolean existsFont(String name, int type) {
		
		Enumeration<FontName> fontNames = model.elements();
		while (fontNames.hasMoreElements()) {
			
			FontName fontName = fontNames.nextElement();
			if (fontName.name.equals(name) && fontName.type == type) {
				
				return true;
			}
		}
		return false;
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		localize();
		setIcons();
		setToolTips();
		
		initList();
		loadGenericNames();
		loadFamilyNames();
		
		loadDialog();
	}

	/**
	 * Initialize list.
	 */
	private void initList() {
		
		// Create and set model.
		model = new DefaultListModel<FontName>();
		list.setModel(model);
		
		// Create and set renderer.
		list.setCellRenderer(new ListCellRenderer<FontName>() {

			// Define renderer.
			@SuppressWarnings("serial")
			final RendererJLabel renderer = new RendererJLabel() {
				{
					setOpaque(true);
				}
			};
						
			@Override
			public Component getListCellRendererComponent(
					JList<? extends FontName> list, FontName value, int index,
					boolean isSelected, boolean cellHasFocus) {
				
				if (value == null) {
					return null;
				}
				
				FontName fontName = (FontName) value;
				
				// Set renderer.
				renderer.set(isSelected, cellHasFocus, index);
				
				if (fontName.type == GENERIC_NAME) {
					renderer.setForeground(Color.DARK_GRAY);
					renderer.setText("<html>[<span style='font-family:" + fontName.name + ";font-size:12px'>" + fontName.name + "</span>]</html>");
				}
				else {
					renderer.setForeground(Color.BLACK);
					renderer.setText("<html><span style='font-family:" + fontName.name + ";font-size:12px'>" + fontName.name + "</span></html>");
				}
				
				return renderer;
			}
		});
	}

	/**
	 * Load generic font names.
	 */
	private void loadGenericNames() {
		
		final String [] genericNames = { "serif", "sans-serif", "monospace", "cursive", "fantasy" };
		
		for (String genericName : genericNames) {
			
			model.addElement(new FontName(genericName, GENERIC_NAME));
		}
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonAddFont.setToolTipText(Resources.getString("org.multipage.gui.tooltipAddFontToList"));
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
		buttonAddFont.setIcon(Images.getIcon("org/multipage/gui/images/insert.png"));
		menuRename.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		menuRemove.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelFontNames);
		Utility.localize(labelNewFont);
		Utility.localize(menuRename);
		Utility.localize(menuRemove);
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
		
		// Get chosen font.
		outputName = textFontName.getText();
		if (outputName.isEmpty()) {
			
			FontName fontName = list.getSelectedValue();
			if (fontName == null) {
				
				Utility.show(this, "org.multipage.gui.messageTypeNewOrSelectListFont");
				return;
			}
			
			outputName = fontName.name;
		}
		else {
			// Try to save new list.
			addFont(outputName);
		}
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * Load dialog.
	 */
	private void loadDialog() {
		
		if (bounds.isEmpty()) {
			Utility.centerOnScreen(this);
		}
		else {
			setBounds(bounds);
		}
	}
	
	/**
	 * Save dialog.
	 */
	private void saveDialog() {
		
		saveFamilyFonts();
		
		bounds = getBounds();
	}

	/**
	 * Save family fonts.
	 */
	private void saveFamilyFonts() {
		
		familyFonts.clear();
		
		Enumeration<FontName> fontNames = model.elements();
		while (fontNames.hasMoreElements()) {
			
			FontName fontName = fontNames.nextElement();
			if (fontName.type == FAMILY_NAME) {
				
				familyFonts.add(fontName.name);
			}
		}
	}

	/**
	 * Load family font names.
	 */
	private void loadFamilyNames() {
		
		// Sort font names.
		familyFonts.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				
				return Collator.getInstance().compare(o1,o2);
			}});
		
		for (String name : familyFonts) {
			
			model.addElement(new FontName(name, FAMILY_NAME));
		}
	}

	/**
	 * On double click list.
	 */
	private void onDoubleClickList() {
		
		FontName fontName = list.getSelectedValue();
		if (fontName == null) {
			
			Utility.show(this, "org.multipage.gui.messageTypeNewOrSelectListFont");
			return;
		}
		
		outputName = fontName.name;
		
		saveDialog();
		
		confirm = true;
		dispose();
	}

	/**
	 * On click list.
	 * @param e
	 */
	protected void onClickList(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			onDoubleClickList();
		}
	}

	/**
	 * Add popup menu.
	 * @param component
	 * @param popup
	 */
	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickList(e);
			}
		});
	}
}
