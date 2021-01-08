/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 14-04-2020
 *
 */
package com.maclan.server;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SecondaryLoop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;

/**
 * 
 * @author user
 *
 */
public class JavaScriptDebuggerObjectDump extends JDialog {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Dialog serialized states.
	 */
	private static Rectangle bounds = new Rectangle();
	
	/**
	 * Actions.
	 * @author user
	 *
	 */
	public static enum Action {
		breakit, runit, next};
	
	/**
	 * Components.
	 */
	private JLabel labelObjectProperties;
	private JButton buttonBreak;
	private JButton buttonRun;
	private JButton buttonNext;
	private JScrollPane scrollPane;
	private JTree tree;
	
	/**
	 * Object name and value.
	 */
	private static class ObjectNameValue {
		
		/**
		 * Object.
		 */
		Object object;
		
		/**
		 * Name.
		 */
		String name;
		
		/**
		 * Value.
		 */
		String value;
		
		/**
		 * Constructor.
		 */
		public ObjectNameValue(Object object, String name, String value) {
			
			this.object = object;
			this.name = name;
			this.value = value;
		}

		/**
		 * Make string.
		 */
		@Override
		public String toString() {
			return name + " = " + value;
		}
	}
	
	/**
	 * Object name.
	 */
	private String name;
	
	/**
	 * Debugged object.
	 */
	private Object object;
	
	/**
	 * Secondary loop to invoke. It is necessary for JFrame to be modal.
	 */
	private SecondaryLoop loop;
	
	/**
	 * Continue program flag.
	 */
	private Action continueProgram = Action.breakit;
	
	/**
	 * Tree model.
	 */
	private DefaultTreeModel model;
	
	/**
	 * Tree depth.
	 */
	private int depth;
	
	/**
	 * Dumped values.
	 */
	private LinkedList<Object> values;
	
	/**
	 * Show debugged object.
	 * @param debuggedObject 
	 * @param lock 
	 */
	public static JavaScriptDebuggerObjectDump showDialog(String name, Object debuggedObject, SecondaryLoop loop) {
		
		JavaScriptDebuggerObjectDump dialog = new JavaScriptDebuggerObjectDump();
		
		dialog.name = name;
		dialog.object = debuggedObject;
		dialog.loop = loop;
		dialog.loadTree(dialog.name, dialog.object);
		
		dialog.setVisible(true);
		return dialog;
	}
	
	/**
	 * Return action.
	 * @return
	 */
	public Action getAction() {
		
		return this.continueProgram;
	}

	/**
	 * Create the dialog.
	 */
	public JavaScriptDebuggerObjectDump() {
		super(null, ModalityType.APPLICATION_MODAL);
		initComponents();
		postCreation(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onBreak();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("com.maclan.server.titleDebuggerMessage");
		setBounds(100, 100, 450, 420);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		labelObjectProperties = new JLabel("com.maclan.server.textObjectProperties");
		springLayout.putConstraint(SpringLayout.NORTH, labelObjectProperties, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, labelObjectProperties, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(labelObjectProperties);
		
		buttonBreak = new JButton("com.maclan.server.textBreak");
		buttonBreak.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onBreak();
			}
		});
		buttonBreak.setMargin(new Insets(0, 0, 0, 0));
		buttonBreak.setPreferredSize(new Dimension(80, 25));
		springLayout.putConstraint(SpringLayout.SOUTH, buttonBreak, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, buttonBreak, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(buttonBreak);
		
		buttonRun = new JButton("com.maclan.server.textRun");
		springLayout.putConstraint(SpringLayout.NORTH, buttonRun, 0, SpringLayout.NORTH, buttonBreak);
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRun();
			}
		});
		buttonRun.setPreferredSize(new Dimension(80, 25));
		buttonRun.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonRun);
		
		scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 3, SpringLayout.SOUTH, labelObjectProperties);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, buttonBreak);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, getContentPane());
		getContentPane().add(scrollPane);
		
		tree = new JTree();
		scrollPane.setViewportView(tree);
		
		buttonNext = new JButton("com.maclan.server.textNext");
		buttonNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
		springLayout.putConstraint(SpringLayout.EAST, buttonRun, -6, SpringLayout.WEST, buttonNext);
		springLayout.putConstraint(SpringLayout.NORTH, buttonNext, 0, SpringLayout.NORTH, buttonBreak);
		springLayout.putConstraint(SpringLayout.EAST, buttonNext, -6, SpringLayout.WEST, buttonBreak);
		buttonNext.setPreferredSize(new Dimension(80, 25));
		buttonNext.setMargin(new Insets(0, 0, 0, 0));
		getContentPane().add(buttonNext);
	}
	
	/**
	 * On run program.
	 */
	protected void onRun() {
		
		this.continueProgram = Action.runit;
		
		saveDialog();
		dispose();
		
		loop.exit();
	}
	
	/**
	 * On next.
	 */
	protected void onNext() {
		
		this.continueProgram = Action.next;
		
		saveDialog();
		dispose();
		
		loop.exit();
	}
	
	/**
	 * On break program.
	 */
	protected void onBreak() {
		
		this.continueProgram = Action.breakit;
		
		saveDialog();
		dispose();
		
		loop.exit();
	}

	/**
	 * Post creation of dialog.
	 */
	private void postCreation() {
		
		localize();
		setIcons();
		
		loadDialog();
	}
	
	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		setIconImage(Images.getImage("org/multipage/gui/images/resource.png"));
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(this);
		Utility.localize(labelObjectProperties);
		Utility.localize(buttonBreak);
		Utility.localize(buttonRun);
		Utility.localize(buttonNext);
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
	/**
	 * Load tree.
	 */
	private void loadTree(String name, Object object) {
		
		if (object == null) {
			object = "null";
		}
		
		// Create model.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ObjectNameValue(object, name, object.toString()));
		model = new DefaultTreeModel(root);
		tree.setModel(model);
		
		// Set icons.
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		ImageIcon icon = Images.getIcon("org/multipage/gui/images/resource.png");
		renderer.setClosedIcon(icon);
		renderer.setOpenIcon(icon);
		renderer.setLeafIcon(icon);
		
		// Load tree recursive.
		depth = 0;
		values = new LinkedList<Object>();
		loadTreeRecursive(root);
		
		// Expand root node.
		tree.expandRow(0);
	}
	
	/**
	 * Load tree nodes recursively.
	 * @param node
	 */
	private void loadTreeRecursive(DefaultMutableTreeNode node) {
		
		if (depth >= 10) {
			return;
		}
		
		Object userObject = node.getUserObject();
		if (!(userObject instanceof ObjectNameValue)) {
			return;
		}
		
		Object object = ((ObjectNameValue) userObject).object;
		if (object == null) {
			return;
		}
		
		if (values.contains(object)) {
			return;
		}
		
		values.add(object);
				
		Class<? extends Object> objectClass = object.getClass();
		Field fields[] = objectClass.getDeclaredFields();
		
		for (Field field : fields) {
			try {
				Object value = field.get(object);
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new ObjectNameValue(value, field.getName(), value.toString()));
				node.add(childNode);
				depth++;
				loadTreeRecursive(childNode);
				depth--;
			}
			catch (Exception e) {
			}
		}
	}
}
