/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

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
	public void addSelectedAreaId(Long [] idPath) {
		
		selectedAreasIdPaths.add(idPath);
	}

	/**
	 * Add expanded area ID.
	 * @param idPath
	 */
	public void addExpandedAreaId(Long [] idPath) {
		
		expandedAreasIdPaths.add(idPath);
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
		
		if (index >= areaIdPath.length) {
			return true;
		}
		
		long areaId = areaIdPath[index];
		
		Area currentArea = (Area) node.getUserObject();
		
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
					
					treeState.addExpandedAreaId(idPath);
				}
			}
		}
		
		// Get selected areas' IDs.
		TreePath [] selectedTreePaths = tree.getSelectionPaths();
		if (selectedTreePaths != null) {
			
			for (TreePath selectedTreePath : selectedTreePaths) {
				
				Long [] idPath = getAreaIdPath(selectedTreePath);
						
				// Add selected area ID to the tree state object.
				treeState.addSelectedAreaId(idPath);
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
			Area area = (Area) node.getUserObject();
			
			idPath[index] = area.getId();
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