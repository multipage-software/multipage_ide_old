/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.MiddleResult;
import org.maclan.Namespace;
import org.maclan.NamespacesModel;
import org.multipage.basic.ProgramBasic;
import org.multipage.gui.Images;
import org.multipage.gui.ToolBarKit;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class NamespaceTreePanel extends JPanel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Namespaces model.
	 */
	private NamespacesModel model = new NamespacesModel();
	
	/**
	 * Patterns tree.
	 */
	private JTree tree = new JTree();
	
	/**
	 * Listener.
	 */
	private NamespaceTreeListener treeListener = null;
	
	/**
	 * Root node.
	 */
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

	/**
	 * Constructor.
	 */
	public NamespaceTreePanel() {
		// Initialize components.
		initComponents();
		// Post creation.
		postCreate();
	}

	/**
	 * Initialize components.
	 */
	@SuppressWarnings("serial")
	private void initComponents() {

		// Create tool bar.
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		setLayout(new BorderLayout());
		
		add(new JScrollPane(tree), BorderLayout.CENTER);
		add(toolbar, BorderLayout.PAGE_END);
		
				// Create tree tool bar.
				ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/add_node.png", this, "onAddNamespace", "org.multipage.generator.tooltipAddNamespace");
		ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/rename_node.png", this, "onRenameNamespace", "org.multipage.generator.tooltipRenameNameSpace");
		ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/remove_node.png", this, "onRemoveNamespace", "org.multipage.generator.tooltipRemoveNameSpace");
		toolbar.addSeparator();
		ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/update_icon.png", this, "onUpdateTree", "org.multipage.generator.tooltipUpdateTree");
		ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/expand_icon.png", this, "onExpandTree", "org.multipage.generator.tooltipExpandTree");
		ToolBarKit.addToolBarButton(toolbar, "org/multipage/generator/images/collapse_icon.png", this, "onCollapseTree", "org.multipage.generator.tooltipCollapseTree");
		
		// Set cell renderer.
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
						row, hasFocus);
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				Object userobject = node.getUserObject();
				
				// If is class node.
				if (userobject instanceof Namespace) {
					setIcon(node.isLeaf()? Images.getIcon("org/multipage/generator/images/class_node_leaf_icon.png")
							: Images.getIcon("org/multipage/generator/images/class_node_icon.png"));
				}
 
				return this;
			}
		});
		
		// Set selection listener.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// On selection changed.
				TreePath path = e.getPath();
				if (path != null) {
					// Get last 
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					Object user = node.getUserObject();
					if (user instanceof Namespace) {
						// Call listener.
						if (treeListener != null) {
							treeListener.onNamespaceSelectedEvent((Namespace) user);
						}
					}
				}
			}
		});
	}

	/**
	 * Post creation.
	 */
	private void postCreate() {

		// Reset tree model.
		root.removeAllChildren();
		tree.setModel(new DefaultTreeModel(root));
	}
	
	/**
	 * On add new namespace node.
	 */
	public void onAddNamespace() {

		// Get selected namespace.
		Namespace namespace = getSelectedNamespace();
		if (namespace == null) {
			// Inform user.
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageSelectNamespace"));
			return;
		}

		// Get namespace name.
		String name = JOptionPane.showInputDialog(this, Resources.getString("org.multipage.generator.messageEnterNamespaceName"));
		if (name == null) {
			return;
		}
			
		if (name.isEmpty()) {
			// Inform user.
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageNamespaceNameCannotBeEmpty"));
			return;
		}

		// Create new child namespace.
		Namespace newNamespace = new Namespace(name, namespace.getId(), null);
		MiddleResult result = ProgramBasic.getMiddle().insertNamespace(
				ProgramBasic.getLoginProperties(),
				newNamespace);
		
		if (result.isNotOK()) {
			// Inform user on error.
			result.show(this);
		}
		
		// Update information.
		updateInformation();
	}
	
	/**
	 * On rename node.
	 */
	public void onRenameNamespace() {

		// Get selected node.
		DefaultMutableTreeNode node = getSelectedNamespace(null);
		if (node == null) {
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageSelectNamespace"));
			return;
		}

		// If no parent (it is root node) inform user.
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		if (parent == null) {
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotRenameRootNamespace"));
			return;
		}

		String format = Resources.getString("org.multipage.generator.messageEnterNewNameForNamespace"),
			   message = String.format(format, node.toString()),
			   name;

		// Try to get new node name.
		name = JOptionPane.showInputDialog(this,
				message,
				((Namespace) node.getUserObject()).getDescription());
		
		if (name == null) {
			return;
		}
		
		// Rename namespace.
		Namespace namespace = (Namespace)node.getUserObject();
		String oldname = namespace.getDescription();
		namespace.setDescription(name);
		
		// Try to save node.
		Properties loginProperties = ProgramBasic.getLoginProperties();
		MiddleResult result = null;

		result = ProgramBasic.getMiddle().updateNamespaceDescritpion(
				loginProperties,
				namespace);
		
		if (result == MiddleResult.OK) {
			((DefaultTreeModel)tree.getModel()).reload(node);
		}
		else {
			// Set old description.
			namespace.setDescription(oldname);
			// Inform user on error.
			result.show(this);
		}
		
		// Update.
		updateInformation();
	}
	
	/**
	 * On remove tree node.
	 */
	public void onRemoveNamespace() {

		// Get selected node.
		DefaultMutableTreeNode node = getSelectedNamespace(null);
		if (node == null) {
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageSelectNamespace"));
			return;
		}

		// If no parent (it is root node) inform user.
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		if (parent == null) {
			JOptionPane.showMessageDialog(this, Resources.getString("org.multipage.generator.messageCannotRemoveRootNamespace"));
			return;
		}
			
		Namespace namespace = (Namespace) node.getUserObject();
		String format = Resources.getString(node.getChildCount() != 0 ?
											"org.multipage.generator.messageRemoveNamespaceTree" :
											"org.multipage.generator.messageRemoveNamespace"),
		       message = String.format(format, namespace.getDescription());
		
		// Ask user if he/she wants to remove namespace.
		if (JOptionPane.showConfirmDialog(this, message) == JOptionPane.YES_OPTION) {

			// Get selection parent namespace.
			Namespace selectedNamespace = getSelectedNamespace();
			
			// Remove namespace.
			MiddleResult result = ProgramBasic.getMiddle().removeNamespaceTree(
						ProgramBasic.getLoginProperties(),
						namespace, model);
			if (result.isNotOK()) {
				// Inform user on error.
				result.show(this);
			}
			
			// Update.
			updateInformation();
			
			// Select old namespace parent.
			if (selectedNamespace != null) {
				selectNamespace(selectedNamespace.getParentNamespaceId());
				// Expand selected namespace.
				TreePath path = tree.getSelectionPath();
				if (path != null) {
					tree.expandPath(path);
				}
			}
		}
	}

	/**
	 * On update tree.
	 */
	public void onUpdateTree() {
		
		updateInformation();
	}
	
	/**
	 * On expand all.
	 */
	public void onExpandTree() {
		
		Utility.expandAll(tree, true);
	}
	
	/**
	 * On collapse all.
	 */
	public void onCollapseTree() {
		
		Utility.expandAll(tree, false);
	}
	
	/**
	 * Gets selected node.
	 * @param pathOut
	 * @return - node or null
	 */
	private DefaultMutableTreeNode getSelectedNamespace(Obj<TreePath> pathOut) {
		
		TreePath path = tree.getSelectionPath();
		
		// Set output.
		if (pathOut != null) {
			pathOut.ref = path;
		}
		
		// Get selected node.
		if (path == null) {
			return null;
		}
		// Return selected node.
		return (DefaultMutableTreeNode)path.getLastPathComponent();
	}

	/**
	 * Get selected namespace.
	 * @return
	 */
	public Namespace getSelectedNamespace() {
		
		DefaultMutableTreeNode node = getSelectedNamespace(null);
		if (node == null) {
			return null;
		}
		
		return (Namespace)node.getUserObject();
	}

	/**
	 * Loads tree.
	 */
	public void updateInformation() {

		// Get selected namespace.
		Namespace selectedNamespace = getSelectedNamespace();
		
		// Get expanded namespaces IDs.
		LinkedList<Long> expandedIds = new LinkedList<Long>();
		TreePath rootPath = tree.getPathForRow(0);
		if (rootPath != null) {
			Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(rootPath);
			if (expandedPaths != null) {
				
				// Do loop for all expanded paths.
				while (expandedPaths.hasMoreElements()) {
					// Get path.
					TreePath path = expandedPaths.nextElement();
					if (tree.isExpanded(path)) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						Object user = node.getUserObject();
						if (user instanceof Namespace) {
							
							Namespace namespace = (Namespace) user;
							expandedIds.add(namespace.getId());
						}
					}
				}
			}
		}
		
		// Load model.		
		Properties properties = ProgramBasic.getLoginProperties();
		MiddleResult result = ProgramBasic.getMiddle().loadNamespaces(
				properties, model);
		
		if (result != MiddleResult.OK) {
			result.show(this);
		}
			
		// Set tree model.
		root.removeAllChildren();
		loadTree(root, model.getRootNamespace());
		tree.setModel(new DefaultTreeModel(root));
		
		// Expand namespaces.
		for (long expandedId : expandedIds) {
			TreePath treePath = getTreePath(expandedId);
			if (treePath != null) {
				tree.expandPath(treePath);
			}
		}
		
		// Select namespace.
		selectNamespace(selectedNamespace);
		// Expand selected namespace.
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			tree.expandPath(path);
		}
	}

	/**
	 * Get namespace tree path.
	 * @param namespaceId
	 * @return
	 */
	private TreePath getTreePath(long namespaceId) {
		
		Enumeration<? super TreeNode> enumeration = root.breadthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object user = node.getUserObject();
			if (user instanceof Namespace) {
				
				Namespace namespace = (Namespace) user;
				if (namespace.getId() == namespaceId) {
					TreePath treePath = Utility.getTreePath(node);
					return treePath;
				}
			}
		}

		return null;
	}

	/**
	 * Select namespace.
	 * @param namespace
	 */
	private void selectNamespace(Namespace namespace) {

		if (namespace != null) {
			selectNamespace(namespace.getId());
		}
	}

	/**
	 * Load nodes.
	 * @param root
	 * @param rootNamespace
	 */
	private void loadTree(DefaultMutableTreeNode node, Namespace namespace) {
		
		node.setUserObject(namespace);

		LinkedList<Namespace> namespaces = model.getNamespaceChildren(namespace);
		
		// Do loop for all children.
		for (Namespace childNamespace : namespaces) {
			
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
			node.add(childNode);
			loadTree(childNode, childNamespace);
		}
	}

	/**
	 * Select root namespace.
	 */
	public void selectRoot() {
		
		selectNamespace(0L);
	}
	
	/**
	 * Select element.
	 * @param id
	 */
	public void selectNamespace(long id) {
		
		// Get tree model.
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		
		// Find element path.
		Enumeration<? super TreeNode> enumeration = root.breadthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object user = node.getUserObject();
			if (user instanceof Namespace) {
				
				Namespace namespace = (Namespace) user;
				if (namespace.getId() == id) {
					
					// Create tree path and select it.
					TreeNode [] pathNodes = model.getPathToRoot(node);
					TreePath path = new TreePath(pathNodes);
					tree.scrollPathToVisible(path);
					tree.setSelectionPath(path);
					
					// Call listener.
					if (treeListener != null) {
						treeListener.onNamespaceSelectedEvent(namespace);
					}

					break;
				}
			}
		}
	}

	/**
	 * @param treeListener the treeListener to set
	 */
	public void setTreeListener(NamespaceTreeListener treeListener) {
		this.treeListener = treeListener;
	}

	/**
	 * @return the tree
	 */
	public JTree getTree() {
		return tree;
	}

}
