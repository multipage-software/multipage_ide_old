/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.maclan.Area;
import org.maclan.MiddleUtility;
import org.multipage.gui.Images;
import org.multipage.gui.InsertPanel;
import org.multipage.gui.StringValueEditor;
import org.multipage.gui.TextFieldEx;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class AreaPanel extends InsertPanel implements StringValueEditor, ExternalProviderInterface {

	// $hide>>$
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Serialized dialog states.
	 */
	protected static Rectangle bounds;
	private static boolean boundsSet;
	
	/**
	 * Set default states.
	 */
	public static void setDefaultData() {
		
		bounds = new Rectangle(0, 0, 500, 330);
		boundsSet = false;
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
		boundsSet = true;
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
	 * Initial string.
	 */
	private String initialString;

	// $hide<<$
	
	/**
	 * Components.
	 */
	private JLabel labelAreaServerUrl;
	private JTextField textAreaServerUrl;
	private JButton buttonConnectProvider;
	private JLabel labelExternalProviders;
	private JScrollPane scrollPane;
	
	/**
	 * Table model
	 */
	private DefaultTreeModel treeModel;
	private JTree tree;

	/**
	 * Create the dialog.
	 * @param parentWindow 
	 */
	public AreaPanel(String initialString) {

		initComponents();
		
		// $hide>>$
		this.initialString = initialString;
		postCreate();
		// $hide<<$
	}

	/**
	 * Initialize components.
	 */
	private void initComponents() {
		
		SpringLayout sl_panelMain = new SpringLayout();
		setLayout(sl_panelMain);
		
		labelAreaServerUrl = new JLabel("org.multipage.generator.textInsertAreaServerUrl");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelAreaServerUrl, 28, SpringLayout.NORTH, this);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelAreaServerUrl, 20, SpringLayout.WEST, this);
		add(labelAreaServerUrl);
		
		textAreaServerUrl = new TextFieldEx();
		textAreaServerUrl.setText("http://");
		sl_panelMain.putConstraint(SpringLayout.NORTH, textAreaServerUrl, -3, SpringLayout.NORTH, labelAreaServerUrl);
		sl_panelMain.putConstraint(SpringLayout.WEST, textAreaServerUrl, 6, SpringLayout.EAST, labelAreaServerUrl);
		textAreaServerUrl.setPreferredSize(new Dimension(6, 25));
		textAreaServerUrl.setMinimumSize(new Dimension(6, 25));
		add(textAreaServerUrl);
		textAreaServerUrl.setColumns(25);
		
		buttonConnectProvider = new JButton("org.multipage.generator.textConnectProvider");
		sl_panelMain.putConstraint(SpringLayout.EAST, textAreaServerUrl, 0, SpringLayout.WEST, buttonConnectProvider);
		sl_panelMain.putConstraint(SpringLayout.NORTH, buttonConnectProvider, 0, SpringLayout.NORTH, textAreaServerUrl);
		sl_panelMain.putConstraint(SpringLayout.EAST, buttonConnectProvider, -20, SpringLayout.EAST, this);
		buttonConnectProvider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExternalAreaProvider();
			}
		});
		buttonConnectProvider.setMargin(new Insets(0, 0, 0, 0));
		buttonConnectProvider.setPreferredSize(new Dimension(80, 25));
		add(buttonConnectProvider);
		
		labelExternalProviders = new JLabel("org.multipage.generator.textSelectExternalProvider");
		sl_panelMain.putConstraint(SpringLayout.NORTH, labelExternalProviders, 48, SpringLayout.SOUTH, labelAreaServerUrl);
		sl_panelMain.putConstraint(SpringLayout.WEST, labelExternalProviders, 0, SpringLayout.WEST, labelAreaServerUrl);
		add(labelExternalProviders);
		
		scrollPane = new JScrollPane();
		sl_panelMain.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, labelExternalProviders);
		sl_panelMain.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, labelAreaServerUrl);
		sl_panelMain.putConstraint(SpringLayout.SOUTH, scrollPane, -20, SpringLayout.SOUTH, this);
		sl_panelMain.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, buttonConnectProvider);
		add(scrollPane);
		
		tree = new JTree();
		tree.setShowsRootHandles(true);
		scrollPane.setViewportView(tree);
	}

	/**
	 * Save dialog.
	 */
	@Override
	public void saveDialog() {
		
		
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		localize();
		setIcons();
		setToolTips();
		loadTree(null);
	}

	/**
	 * On select external area provider.
	 */
	protected void onExternalAreaProvider() {
		
		String urlText = textAreaServerUrl.getText();
		try {
			URL url = new URL(urlText);
			
			// Ask user for password. TODO: make Utility.inputPwd(...)
			String user = Utility.input(this, "Enter Area Server password:");
			String password = Utility.input(this, "Enter " + user + " password:");
			
			Obj<Exception> exception = new Obj<Exception>();
			
			// Request providers exposed from Area Server
			Hashtable<String, String []> areasProviders = MiddleUtility.requestExposedProviders(url, user, password, exception);
			loadTree(areasProviders);
			
			if (exception.ref != null) {
				throw exception.ref;
			}
		}
		catch (Exception e) {
			Utility.show(this, "org.multipage.generator.messageUrlError", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Provider node
	 */
	class ProviderNode {
		
		/**
		 * Area alias and provider alias
		 */
		String areaAlias;
		String alias;
		 
		/**
		 * Constructor
		 * @param alias
		 */
		ProviderNode(String areaAlias, String alias) {
			
			this.areaAlias = areaAlias;
			this.alias = alias;
		}
		
		/**
		 * Get text
		 */
		@Override
		public String toString() {
			
			return alias;
		}
	}
	/**
	 * Load exposed areas and providers tree
	 * @param areasProviders
	 */
	private void loadTree(Hashtable<String, String[]> areasProviders) {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		
		tree.setRootVisible(false);
		treeModel = new DefaultTreeModel(root);
		tree.setModel(treeModel);
		
		
		if (areasProviders != null) {
			areasProviders.forEach( (areaAlias, providersAliases) -> {
				
				DefaultMutableTreeNode area = new DefaultMutableTreeNode(areaAlias);
				root.add(area);
				
				for (String providerAlias : providersAliases) {
					DefaultMutableTreeNode provider = new DefaultMutableTreeNode(
															new ProviderNode(areaAlias, providerAlias));
					area.add(provider);
				}
			});
		}
		else {
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		}
		
		Utility.expandAll(tree, true);
	}

	/**
	 * Get font specification.
	 * @return
	 */
	@Override
	public String getSpecification() {
		
		// Try to get selected area node
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();;
			Object nodeObject = selectedNode.getUserObject();
			
			if (nodeObject instanceof ProviderNode) {
				
				ProviderNode provider = (ProviderNode) nodeObject;
				
				// Compile Area Server link
				String url = textAreaServerUrl.getText();
				String specification = String.format("%s,areaAlias=\"%s\",providerAlias=\"%s\"",
															url, provider.areaAlias, provider.alias);
				return specification;
			}
		}
		
		// Return empty string
		return "";
	}

	/**
	 * Set from initial string.
	 */
	private void setFromInitialString() {
		
		if (initialString != null) {
			
			textAreaServerUrl.setText(initialString);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {

		Utility.localize(labelAreaServerUrl);
		Utility.localize(labelExternalProviders);
		Utility.localize(buttonConnectProvider);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {
		
		// TODO: create new icon
		//buttonConnectProvider.setIcon(Images.getIcon("org/multipage/generator/images/connect.png"));
		
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		Icon areaIcon = Images.getIcon("org/multipage/generator/images/area_node.png");
		renderer.setOpenIcon(areaIcon);
		renderer.setClosedIcon(areaIcon);
		renderer.setLeafIcon(Images.getIcon("org/multipage/generator/images/slot.png"));
	}

	/**
	 * Set tool tips.
	 */
	private void setToolTips() {
		
		buttonConnectProvider.setToolTipText(Resources.getString("org.multipage.generator.tooltipSelectMimeType"));
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getWindowTitle()
	 */
	@Override
	public String getWindowTitle() {
		
		return Resources.getString("org.multipage.generator.textCssMimeBuilder");
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getResultText()
	 */
	@Override
	public String getResultText() {
		
		return getSpecification();
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#getContainerDialogBounds()
	 */
	@Override
	public Rectangle getContainerDialogBounds() {
		
		return bounds;
	}

	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setContainerDialogBounds(java.awt.Rectangle)
	 */
	@Override
	public void setContainerDialogBounds(Rectangle bounds) {
		
		AreaPanel.bounds = bounds;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#boundsSet()
	 */
	@Override
	public boolean isBoundsSet() {

		return boundsSet;
	}
	
	/* (non-Javadoc)
	 * @see org.multipage.gui.InsertPanel#setBoundsSet(boolean)
	 */
	@Override
	public void setBoundsSet(boolean set) {
		
		boundsSet = set;
	}

	/**
	 * Get component.
	 */
	@Override
	public Component getComponent() {
		
		return this;
	}

	/**
	 * Get string value.
	 */
	@Override
	public String getStringValue() {
		
		return getSpecification();
	}

	/**
	 * Set string value.
	 */
	@Override
	public void setStringValue(String string) {
		
		initialString = string;
		setFromInitialString();
	}

	/**
	 * Get value meaning.
	 */
	@Override
	public String getValueMeaning() {
		
		return meansArea;
	}
	
	/**
	 * Set controls grayed. If a false value is returned a high level editor grayed this panel.
	 */
	@Override
	public boolean setControlsGrayed(boolean isDefault) {
		
		return false;
	}
	
	/**
	 * Set editor controls from link string.
	 */
	@Override
	public void setEditor(String link, Area area) {
		
		// Parse link string.
	}
}
