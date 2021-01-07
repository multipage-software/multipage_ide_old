/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * 
 * @author
 *
 */
public class CssFindPropertyDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();

	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle();
	}

	/**
	 * Save serialized states.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(ObjectOutputStream outputStream)
			throws IOException {
		
		outputStream.writeObject(bounds);
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
	}
	
	/**
	 * Confirm flag.
	 */
	private boolean confirm = false;
	
	/**
	 * List model.
	 */
	private DefaultListModel listModel;
	
	/**
	 * Timers.
	 */
	private Timer timer;

	/**
	 * Loading controls content flag.
	 */
	private boolean loading;

	/**
	 * CSS properties type.
	 */
	private char type;

	// $hide<<$
	/**
	 * Components.
	 */
	private JButton buttonCancel;
	private JButton buttonOk;
	private JLabel labelList;
	private JScrollPane scrollPane;
	private JList list;
	private JLabel labelSearch;
	private JTextField textSearch;
	private JCheckBox checkWholeWord;

	/**
	 * Show dialog.
	 * @param parent
	 * @param animated2 
	 * @return
	 */
	public static CssProperty showDialog(Component parent, char type) {
		
		CssFindPropertyDialog dialog = new CssFindPropertyDialog(parent, type);
		dialog.setVisible(true);
		
		if (dialog.confirm) {
			
			return dialog.getPropertyName();
		}
		return null;
	}
	
	/**
	 * Get property name.
	 * @return
	 */
	private CssProperty getPropertyName() {
		
		return (CssProperty) list.getSelectedValue();
	}

	/**
	 * Create the dialog.
	 * @param parent 
	 * @param type 
	 */
	public CssFindPropertyDialog(Component parent, char type) {
		super(Utility.findWindow(parent), ModalityType.APPLICATION_MODAL);

		initComponents();
		
		// $hide>>$
		this.type = type;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setTitle("org.multipage.gui.textFindCssProperty");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		setBounds(100, 100, 450, 362);
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
		
		labelList = new JLabel("org.multipage.gui.textCssPropertiesList");
		springLayout.putConstraint(SpringLayout.NORTH, labelList, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelList, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelList);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 7, SpringLayout.SOUTH, labelList);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -100, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickList(e);
			}
		});
		scrollPane.setViewportView(list);
		
		labelSearch = new JLabel("org.multipage.gui.textSearchCssProperty");
		springLayout.putConstraint(SpringLayout.NORTH, labelSearch, 6, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, labelSearch, 0, SpringLayout.WEST, labelList);
		getContentPane().add(labelSearch);
		
		textSearch = new JTextField();
		textSearch.setPreferredSize(new Dimension(2, 24));
		springLayout.putConstraint(SpringLayout.NORTH, textSearch, 6, SpringLayout.SOUTH, scrollPane);
		springLayout.putConstraint(SpringLayout.WEST, textSearch, 6, SpringLayout.EAST, labelSearch);
		getContentPane().add(textSearch);
		textSearch.setColumns(18);
		
		checkWholeWord = new JCheckBox("org.multipage.gui.textWholeWord");
		checkWholeWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onWholeWord();
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, checkWholeWord, 6, SpringLayout.SOUTH, textSearch);
		springLayout.putConstraint(SpringLayout.WEST, checkWholeWord, 10, SpringLayout.WEST, textSearch);
		getContentPane().add(checkWholeWord);
	}

	/**
	 * On click list.
	 * @param e
	 */
	protected void onClickList(MouseEvent e) {
		
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			onOk();
		}
	}

	/**
	 * On whole word.
	 */
	protected void onWholeWord() {
		
		loadList();
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		loading = true;
		
		localize();
		setIcons();
		
		initList();
		
		loadDialog();
		
		setListeners();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loading = false;
				loadList();
			}
		});
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		timer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				loadList();
			}
		});
		timer.setRepeats(false);
		
		Utility.setTextChangeListener(textSearch, new Runnable() {
			@Override
			public void run() {
				
				if (!timer.isRunning()) {
					timer.start();
				}
			}
		});
	}

	/**
	 * Initialize list.
	 */
	private void initList() {
		
		listModel = new DefaultListModel();
		list.setModel(listModel);
		
		// Initialize list item renderer.
		list.setCellRenderer(new ListCellRenderer<CssProperty>() {

			// Renderer object.
			@SuppressWarnings("serial")
			RendererJLabel renderer = new RendererJLabel() {
				{
					setBorder(new EmptyBorder(3, 6, 3, 6));
				}
			};

			// Set renderer.
			@Override
			public Component getListCellRendererComponent(
					JList<? extends CssProperty> list, CssProperty property,
					int index, boolean isSelected, boolean cellHasFocus) {
				
				if (property == null) {
					return null;
				}
				
				String propertyText = property.getHtmlText();
				renderer.setText(propertyText);
				
				renderer.set(isSelected, cellHasFocus, index);
				
				return renderer;
			}
		});
	}

	/**
	 * Load list content.
	 */
	private void loadList() {
		
		if (loading) {
			return;
		}
		
		listModel.clear();
		
		LinkedList<CssProperty> animatedProperties = CssProperty.getProperties(type);
		
		// Get filter string.
		String filterText = textSearch.getText();
		boolean wholeWords = checkWholeWord.isSelected();
		
		FoundAttr findAttributes = new FoundAttr(filterText, false, wholeWords);
		filterText = filterText.trim();
		
		boolean showAll = filterText.isEmpty();
		
		// Load list items.
		animatedProperties.forEach(new Consumer<CssProperty>() {
			@Override
			public void accept(CssProperty property) {
				
				String primalName = property.getPrimalName();
				
				if (Utility.find(primalName, findAttributes) || showAll) {
					listModel.addElement(property);
				}
			}
		});
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonOk.setIcon(Images.getIcon("org/multipage/gui/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/gui/images/cancel_icon.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonOk);
		Utility.localize(buttonCancel);
		Utility.localize(labelList);
		Utility.localize(labelSearch);
		Utility.localize(checkWholeWord);
	}
	
	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		saveDialog();
		
		timer.stop();
		
		confirm = false;
		dispose();
	}

	/**
	 * On OK.
	 */
	protected void onOk() {
		
		// Get selected item.
		CssProperty property = (CssProperty) list.getSelectedValue();
		if (property == null) {
			
			Utility.show(this, "org.multipage.gui.messageSelectSingleCssProperty");
			return;
		}
		
		saveDialog();
		
		timer.stop();
		
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
		
		bounds = getBounds();
	}
}
