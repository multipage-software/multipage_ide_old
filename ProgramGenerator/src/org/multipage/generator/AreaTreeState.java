/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.maclan.Area;

/**
 * Class for tree state.
 */
public class AreaTreeState {

	/**
	 * Selected tree paths.
	 */
	private LinkedList<Long []> selectedAreasIdPaths = new LinkedList<Long []>();
		
	/**
	 * Expanded area IDs.
	 */
	private LinkedList<Long []> expandedAreasIdPaths = new LinkedList<Long []>();
	
	/**
	 * Add selected area ID.
	 * @param idPath
	 */
	public void addSelectedAreaIdPath(Long [] idPath) {
		
		selectedAreasIdPaths.add(idPath);
	}
	
	/**
	 * Selected paths.
	 * @param selectedPaths
	 */
	public void addSelectedPaths(TreePath[] selectedPaths) {
		
		// Add each path.
		for (TreePath treePath : selectedPaths) {
			
			Long [] idPath = getAreaIdPath(treePath);
			addSelectedAreaIdPath(idPath);
		}
	}
	
	/**
	 * Add expanded area ID.
	 * @param idPath
	 */
	public void addExpandedAreaIdPath(Long [] idPath) {
		
		expandedAreasIdPaths.add(idPath);
	}

	/**
	 * Add expanded paths.
	 * @param expandedPaths
	 */
	public void addExpandedPaths(TreePath[] expandedPaths) {
		
		// Add each path.
		for (TreePath treePath : expandedPaths) {
			
			Long [] idPath = getAreaIdPath(treePath);
			addExpandedAreaIdPath(idPath);
		}
	}

	/**
	 * Selected and expanded paths.
	 * @param tree
	 * @param selectedPaths
	 */
	public static void addSelectedAndExpanded(JTree tree, TreePath[] selectedPaths) {
		
		AreaTreeState treeState = getTreeState(tree);
		treeState.addSelectedPaths(selectedPaths);
		treeState.addExpandedPaths(selectedPaths);
	}

	/**
	 * Get node path.
	 * @param node
	 * @param areaIdPath
	 * @param index
	 * @param nodePath
	 */
	public static boolean getNodePath(DefaultMutableTreeNode node,
			Long [] areaIdPath, int index, DefaultMutableTreeNode [] nodePath) {
		
		// Check input values.
		if (node == null || areaIdPath == null || index < 0 || index >= areaIdPath.length || nodePath == null) {
			return true;
		}
		
		long areaId = areaIdPath[index];
		
		Long currentAreaId = (Long) node.getUserObject();
		Area currentArea = ProgramGenerator.getArea(currentAreaId);
		
		// If current area ID doesn't match, exit with false.
		if (currentArea.getId() != areaId) {
			return false;
		}
		
		// Set node path element.
		nodePath[index] = node;
		
		// Call this method recursively for all sub nodes.
		Enumeration nodeChildren = node.children();
		while (nodeChildren.hasMoreElements()) {
			
			DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) nodeChildren.nextElement();
			
			boolean isCorrect = getNodePath(subNode, areaIdPath, index + 1, nodePath);
			if (isCorrect) {
				return true;
			}
		}
		
		return node.getChildCount() == 0;
	}

	/**
	 * Apply tree state.
	 * @param tree
	 * @param treeState
	 */
	public static void applyTreeState(AreaTreeState treeState, JTree tree) {
		
		// Get tree model.
		TreeModel treeModelBase = tree.getModel();
		if (!(treeModelBase instanceof DefaultTreeModel)) {
			return;
		}
		
		DefaultTreeModel treeModel = (DefaultTreeModel) treeModelBase;
		
		// Get root node.
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
		if (rootNode == null) {
			return;
		}
				
		// Expand given areas.
		for (Long [] areaIdPath : treeState.expandedAreasIdPaths) {
			
			DefaultMutableTreeNode [] nodePath = new DefaultMutableTreeNode [areaIdPath.length];
			if (AreaTreeState.getNodePath(rootNode, areaIdPath, 0, nodePath)) {
				
				if (nodePath.length <= 0) {
					continue;
				}
				if (nodePath[nodePath.length - 1] == null) {
					continue;
				}
				
				// Create tree path.
				TreePath treePath = new TreePath(nodePath);
				
				// Select given tree path.
				tree.expandPath(treePath);
			}
		}
		
		LinkedList<TreePath> selectedPaths = new LinkedList<TreePath>();
		
		// Select given areas.
		for (Long [] areaIdPath : treeState.selectedAreasIdPaths) {
			
			DefaultMutableTreeNode [] nodePath = new DefaultMutableTreeNode [areaIdPath.length];
			if (AreaTreeState.getNodePath(rootNode, areaIdPath, 0, nodePath)) {
				
				if (nodePath.length <= 0) {
					continue;
				}
				if (nodePath[nodePath.length - 1] == null) {
					continue;
				}
				
				// Create tree path.
				TreePath treePath = new TreePath(nodePath);
				
				// Select given tree path.
				selectedPaths.add(treePath);
			}
		}
		
		tree.setSelectionPaths(selectedPaths.toArray(new TreePath [0]));
	}

	/**
	 * Get tree state.
	 * @param tree
	 * @return
	 */
	public static AreaTreeState getTreeState(JTree tree) {
		
		AreaTreeState treeState = new AreaTreeState();
	
		// Get expanded areas' IDs.
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
		if (rootNode != null) {
			
			// Get tree nodes.
			Enumeration<? super DefaultMutableTreeNode> allNodes = rootNode.depthFirstEnumeration();
			
			while (allNodes.hasMoreElements()) {
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.nextElement();
				
				TreeNode [] path = node.getPath();
				TreePath treePath = new TreePath(path);
				
				// If the path is expanded, add the area ID to the list.
				if (tree.isExpanded(treePath)) {
					
					Long [] idPath = getAreaIdPath(treePath);
					
					treeState.addExpandedAreaIdPath(idPath);
				}
			}
		}
		
		// Get selected areas' IDs.
		TreePath [] selectedTreePaths = tree.getSelectionPaths();
		if (selectedTreePaths != null) {
			
			for (TreePath selectedTreePath : selectedTreePaths) {
				
				Long [] idPath = getAreaIdPath(selectedTreePath);
						
				// Add selected area ID to the tree state object.
				treeState.addSelectedAreaIdPath(idPath);
			}
		}
		
		return treeState;
	}

	/**
	 * Get area ID path.
	 * @param treePath
	 * @return
	 */
	private static Long[] getAreaIdPath(TreePath treePath) {
		
		int count = treePath.getPathCount();
		
		Long [] idPath = new Long [count];
		
		for (int index = 0; index < count; index++) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getPathComponent(index);
			Long areaId = (Long) node.getUserObject();
			
			idPath[index] = areaId;
		}
		
		return idPath;
	}

	/**
	 * Clear selected list.
	 */
	public void clearSelected() {
		
		selectedAreasIdPaths.clear();
	}
}