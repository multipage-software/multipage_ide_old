/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.Area;
import org.maclan.ConstructorHolder;
import org.multipage.gui.FoundAttr;
import org.multipage.gui.Images;
import org.multipage.gui.RendererJLabel;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;

/**
 * 
 * @author
 *
 */
public class SelectConstructorDialog extends JDialog {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Filter text.
	 */
	private static String filterText = "";

	/**
	 * Is true if a constructor was selected.
	 */
	private static boolean isSelected = false;
	
	/**
	 * Reset filter text.
	 */
	public static void resetFilter() {
		
		filterText = "";
	}

	/**
	 * Returned value.
	 */
	private WizardReturned returned;
	
	/**
	 * Tree model.
	 */
	private DefaultTreeModel treeModel;
	
	/**
	 * Constructor holders reference.
	 */
	private LinkedList<ConstructorHolder> constructorHolders;

	// $hide<<$
	/**
	 * Components.
	 */
	private JPanel panelButtons;
	private JButton buttonNext;
	private JPanel panelMain;
	private JLabel labelSelectConstructor;
	private JButton buttonCancel;
	private JButton buttonSkip;
	private JScrollPane scrollPane;
	private JTree tree;
	private JLabel labelFilter;
	private JTextField textFilter;
	private JPopupMenu popupMenu;
	private JMenuItem menuShowHelp;

	/**
	 * Show dialog.
	 * @param parent
	 * @param constructorHolders 
	 * @param selectedConstructorHolder 
	 * @param resource
	 */
	public static WizardReturned showDialog(Component parent, LinkedList<ConstructorHolder> constructorHolders,
			Obj<ConstructorHolder> selectedConstructorHolder) {
		
		SelectConstructorDialog dialog = new SelectConstructorDialog(Utility.findWindow(parent));
		
		dialog.constructorHolders = constructorHolders;
		dialog.loadConstructorHolderTree();
		
		// Set selected constructor holder.
		if (!isSelected && selectedConstructorHolder.ref != null) {
			isSelected = dialog.setSelectedConstructorHolder(selectedConstructorHolder.ref);
		}
		
		if (!isSelected && constructorHolders.size() == 1) {
			dialog.tree.setSelectionRow(0);
		}
		
		dialog.setVisible(true);
		
		// Get selected constructor holder.
		if (dialog.returned == WizardReturned.NEXT) {
			selectedConstructorHolder.ref = dialog.getSelectedConstructorHolder();
		}
		
		return dialog.returned;
	}

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public SelectConstructorDialog(Window parentWindow) {
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
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				onCancel();
			}
		});
		setTitle("org.multipage.generator.textAreaConstructorsList");
		
		setBounds(100, 100, 450, 300);
		
		panelButtons = new JPanel();
		panelButtons.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(panelButtons, BorderLayout.SOUTH);
		SpringLayout sl_panelButtons = new SpringLayout();
		panelButtons.setLayout(sl_panelButtons);
		
		buttonNext = new JButton("org.multipage.generator.textNext");
		sl_panelButtons.putConstraint(SpringLayout.SOUTH, buttonNext, -10, SpringLayout.SOUTH, panelButtons);
		sl_panelButtons.putConstraint(SpringLayout.EAST, buttonNext, -10, SpringLayout.EAST, panelButtons);
		buttonNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNext();
			}
		});
		buttonNext.setMargin(new Insets(0, 0, 0, 0));
		buttonNext.setPreferredSize(new Dimension(80, 25));
		panelButtons.add(buttonNext);
		
		buttonCancel = new JButton("textCancel");
		sl_panelButtons.putConstraint(SpringLayout.NORTH, buttonCancel, 0, SpringLayout.NORTH, buttonNext);
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
		buttonCancel.setPreferredSize(new Dimension(80, 25));
		buttonCancel.setMargin(new Insets(0, 0, 0, 0));
		panelButtons.add(buttonCancel);
		
		buttonSkip = new JButton("org.multipage.generator.textSkip");
		sl_panelButtons.putConstraint(SpringLayout.WEST, buttonCancel, 69, SpringLayout.EAST, buttonSkip);
		sl_panelButtons.putConstraint(SpringLayout.WEST, buttonSkip, 10, SpringLayout.WEST, panelButtons);
		sl_panelButtons.putConstraint(SpringLayout.SOUTH, buttonSkip, -10, SpringLayout.SOUTH, panelButtons);
		buttonSkip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSkip();
			}
		});
		buttonSkip.setPreferredSize(new Dimension(80, 25));
		buttonSkip.setMargin(new Insets(0, 0, 0, 0));
		panelButtons.add(buttonSkip);
		
		panelMain = new JPanel();
		getContentPane().add(panelMain, BorderLayout.CENTER);
		SpringLayout sl_panelMain = new SpringLayout();
		panelMain.setLayout(sl_panelMain);
		
		labelSelectConstructor = new JLabel("org.multipage.generator.textSelectConstructor");
		labelSelectConstructor.setFont(new Font("Tahoma", Font.PLAIN, 12));
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelSelectConstructor, 10, SpringLayout.NORTH, panelMain);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelSelectConstructor, 10, SpringLayout.WEST, panelMain);
		panelMain.add(labelSelectConstructor);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 3, SpringLayout.SOUTH, labelSelectConstructor);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panelMain);
		panelMain.add(scrollPane);
		
		tree = new JTree();
		scrollPane.setViewportView(tree);
		
		popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);
		
		menuShowHelp = new JMenuItem("org.multipage.generator.menuShowConstructorHelp");
		menuShowHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onShowHelp();
			}
		});
		popupMenu.add(menuShowHelp);
		
		labelFilter = new JLabel("org.multipage.generator.textInsertContructorFilter");
		sl_panelMain.putConstraint(SpringLayout.WEST, labelFilter, 6, SpringLayout.WEST, labelSelectConstructor);
		panelMain.add(labelFilter);
		
		textFilter = new JTextField();
		sl_panelMain.putConstraint(SpringLayout.WEST, textFilter, 3, SpringLayout.EAST, labelFilter);
		sl_panelMain.putConstraint(SpringLayout.EAST, textFilter, -10, SpringLayout.EAST, panelMain);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, -3, SpringLayout.NORTH, textFilter);
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelFilter, 3, SpringLayout.NORTH, textFilter);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, textFilter, 0, SpringLayout.SOUTH, panelMain);
		panelMain.add(textFilter);
		textFilter.setColumns(30);
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {
				
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		Utility.centerOnScreen(this);

		localize();
		setIcons();
			
		// Set initial filter text.
		textFilter.setText(filterText);
		
		initializeTree();
		initializeFilter();

		// If there is no builder extension, hide skip button.
		buttonSkip.setVisible(ProgramGenerator.isExtensionToBuilder());
		
		SwingUtilities.invokeLater(() -> {
			textFilter.grabFocus();
		});
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(buttonNext);
		Utility.localize(labelSelectConstructor);
		Utility.localize(buttonCancel);
		Utility.localize(buttonSkip);
		Utility.localize(labelFilter);
		Utility.localize(menuShowHelp);
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		buttonNext.setIcon(Images.getIcon("org/multipage/generator/images/ok_icon.png"));
		buttonCancel.setIcon(Images.getIcon("org/multipage/generator/images/cancel_icon.png"));
		buttonSkip.setIcon(Images.getIcon("org/multipage/generator/images/skip.png"));
		menuShowHelp.setIcon(Images.getIcon("org/multipage/generator/images/help_small.png"));
	}

	/**
	 * Initialize tree.
	 */
	private void initializeTree() {
		
		// Set tree model.
		treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		tree.setModel(treeModel);
		
		tree.setRowHeight(18);
		
		tree.setRootVisible(true);
		
		// Set renderer.
		tree.setCellRenderer(new TreeCellRenderer() {
			
			RendererJLabel renderer = new RendererJLabel();
			
			final int fontSize = 11;
			
			Font folderFont = new Font("Tahoma", Font.PLAIN, fontSize);
			Font constructorFont = new Font("Tahoma", Font.PLAIN, fontSize);

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				
				if (!(value instanceof DefaultMutableTreeNode)) {
					return null;
				}
				
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
				Object userObject = treeNode.getUserObject();
				renderer.setForeground(Color.GRAY);
				
				if (userObject instanceof String) {
					String folderName = (String) userObject;
					
					renderer.setText(folderName);
					renderer.setIcon(Images.getIcon("org/multipage/generator/images/folder_empty_icon.png"));
					renderer.setFont(folderFont);
				}
				else if (userObject instanceof ConstructorHolder) {
					ConstructorHolder constructorHolder = (ConstructorHolder) userObject;
					
					renderer.setForeground(Color.BLACK);
					renderer.setText(constructorHolder.getPathLastName());
					renderer.setIcon(Images.getIcon("org/multipage/generator/images/constructor.png"));
					renderer.setFont(constructorFont);
				}
				else {
					renderer.setText("");
					renderer.setIcon(Images.getIcon("org/multipage/generator/images/tree_root.png"));
					renderer.setFont(constructorFont);
				}
				
				renderer.set(selected, hasFocus, 0);
				return renderer;
			}
		});
	}

	/**
	 * Initialize filter.
	 */
	private void initializeFilter() {

		// Set timer
		final Timer timer = new Timer(500, (e) -> {
			
			// Reload constructors.
			if (isVisible()) {
				loadConstructorHolderTree();
			}
		});
		
		// Set timer properties.
		timer.setCoalesce(true);
		timer.setRepeats(false);
		
		// Text change listener.
		Utility.setTextChangeListener(textFilter, () -> {
			timer.start();
		});
		
		// ESC key listener.
		KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					textFilter.setText("");
				}
			}
		};
		textFilter.addKeyListener(keyAdapter);
		
		// Stop timer on exit.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {

				timer.stop();
			}
		});
	}

	/**
	 * Load constructor holders.
	 */
	private void loadConstructorHolderTree() {
		
		// Clear old data.
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
		rootNode.removeAllChildren();
		treeModel.reload();
		
		// Get filter text.
		String filterText = textFilter.getText();
		boolean filterIsOn = !filterText.isEmpty();
		FoundAttr foundAttr = new FoundAttr(filterText, false, false);
				
		// Load constructor holders.
		for (ConstructorHolder constructorHolder : constructorHolders) {
						
			if (constructorHolder.isInvisible()) {
				continue;
			}
			
			// Filter constructor name.
			if (filterIsOn) {
				String constructorName = constructorHolder.getPathLastName();
				
				if (!Utility.find(constructorName, foundAttr)) {
					continue;
				}
			}
			
			DefaultMutableTreeNode node = addNewConstructorNode(rootNode, constructorHolder);
			
			if (constructorHolder.selectIt()) {
				
				// Select node after moving it up
				SwingUtilities.invokeLater(() -> {
					
					TreeNode[] nodes = treeModel.getPathToRoot(node);
					TreePath treePath = new TreePath(nodes);
					
					tree.setSelectionPath(treePath);
					tree.scrollPathToVisible(treePath);
				});
							
				isSelected = true;
			}
			
			constructorHolder.removeAsterisk();
		}
		
		// Expand root node.
		Utility.expandAll(tree, true);
	}
	
	/**
	 * Add new constructor holder node.
	 * @param constructorHolder
	 */
	private DefaultMutableTreeNode addNewConstructorNode(DefaultMutableTreeNode rootNode, ConstructorHolder constructorHolder) {
		
		// Parse constructor path.
		String constructorName = constructorHolder.getName();
		String [] constructorPath = constructorName.split("/");
		
		// Create tree node.
		for (int index = 0; index < constructorPath.length - 1; index++) {
			
			String pathItem = constructorPath[index];
			
			DefaultMutableTreeNode childNode = getChildFolderTreeNode(rootNode, pathItem);
			
			if (childNode == null) {
				childNode = new DefaultMutableTreeNode(pathItem);
				rootNode.add(childNode);
			}

			rootNode = childNode;
		}
		
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(constructorHolder);
		rootNode.add(childNode);
		
		return childNode;
	}

	/**
	 * Get child folder node or null.
	 * @param rootNode 
	 * @param childFolderName
	 * @return
	 */
	private DefaultMutableTreeNode getChildFolderTreeNode(
			DefaultMutableTreeNode rootNode, String childFolderName) {
		
		Enumeration children = rootNode.children();
		
		// Do loop for all root node children.
		while (children.hasMoreElements()) {
			
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
			if (childNode == null) {
				return null;
			}
			
			Object userObject = childNode.getUserObject();
			if (userObject instanceof String) {
				
				String folderName = (String) userObject;
				if (folderName.equals(childFolderName)) {
					
					return childNode;
				}
			}
		}
		return null;
	}

	/**
	 * Get selected constructor holder.
	 * @return
	 */
	private ConstructorHolder getSelectedConstructorHolder() {
		
		// Get selected items.
		if (tree.getSelectionCount() != 1) {
			
			return null;
		}
		
		TreePath path = tree.getSelectionPath();
		Object lastPathItem = path.getLastPathComponent();
		
		if (!(lastPathItem instanceof DefaultMutableTreeNode)) {
			return null;
		}
		
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathItem;
		
		Object userObject = treeNode.getUserObject();
		if (!(userObject instanceof ConstructorHolder)) {
			
			Utility.show(this, "org.multipage.generator.messageSelectSingleConstructor");
			return null;
		}
		
		return (ConstructorHolder) userObject;
	}
	
	/**
	 * Set selected constructor holder.
	 * @param constructorHolder 
	 */
	private boolean setSelectedConstructorHolder(ConstructorHolder constructorHolder) {
		
		if (constructorHolder == null) {
			return false;
		}
		
		// Parse constructor holder name.
		String constructorName = constructorHolder.getName();
		String [] constructorPath = constructorName.split("/");
		int index = 0;
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
		LinkedList pathObjects = new LinkedList();
		pathObjects.add(rootNode);
		
		while (index < constructorPath.length) {
			
			boolean isFolder = index < constructorPath.length - 1;
			if (isFolder) {
				
				DefaultMutableTreeNode childNode = getChildFolderTreeNode(rootNode, constructorPath[index]);
				if (childNode == null) {
					return false;
				}
				
				Object userObject = childNode.getUserObject();
				if (!(userObject instanceof String)) {
					return false;
				}
				
				String folderName = (String) userObject;
				if (!folderName.equals(constructorPath[index])) {
					return false;
				}
				
				pathObjects.add(childNode);
				rootNode = childNode;
			}
			else {
				
				Enumeration children = rootNode.children();
				while (children.hasMoreElements()) {
					
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
					Object userObject = childNode.getUserObject();
					
					if (userObject instanceof ConstructorHolder) {
						
						ConstructorHolder foundConstructorHolder = (ConstructorHolder) userObject;
						if (foundConstructorHolder.getName().equals(constructorHolder.getName())) {
							
							pathObjects.add(childNode);
							
							// Select tree node.
							Object [] pathArray = pathObjects.toArray();
							TreePath treePath = new TreePath(pathArray);
							
							tree.setSelectionPath(treePath);
							return true;
						}
					}
				}
			}
			index++;
		}
		
		return false;
	}

	/**
	 * On cancel.
	 */
	protected void onCancel() {
		
		filterText = textFilter.getText();
		
		returned = WizardReturned.CANCEL;
		dispose();
	}
	

	/**
	 * On next.
	 */
	protected void onNext() {
		
		// An item must be selected.
		ConstructorHolder selectedConstructorHolder = getSelectedConstructorHolder();
		if (selectedConstructorHolder == null) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleConstructor");
			return;
		}
		
		filterText = textFilter.getText();
		
		returned = WizardReturned.NEXT;
		dispose();
	}
	
	/**
	 * On skip.
	 */
	protected void onSkip() {
		
		filterText = textFilter.getText();
		
		returned = WizardReturned.SKIP;
		dispose();
	}

	/**
	 * On show help.
	 */
	protected void onShowHelp() {
		
		// An item must be selected.
		ConstructorHolder selectedConstructorHolder = getSelectedConstructorHolder();
		if (selectedConstructorHolder == null) {
			Utility.show(this, "org.multipage.generator.messageSelectSingleConstructor");
			return;
		}
		
		LinkedList<Area> list = new LinkedList<Area>();
		
		Area area = ProgramGenerator.getArea(selectedConstructorHolder.getAreaId());
		if (area == null) {
			return;
		}
		list.add(area);
		
		// Show help viewer.
		AreaHelpViewer.showDialog(this, list);
	}

	/**
	 * Add popup trayMenu.
	 * @param component
	 * @param popup
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
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
		});
	}
}
