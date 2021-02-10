/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.dnd.DropTargetDropEvent;

import javax.swing.tree.TreeNode;

/**
 * @author
 *
 */
public interface JTreeDndCallback {
	
	/**
	 * On node dropped.
	 * @param droppedDndNode
	 * @param droppedNodeParent 
	 * @param transferedDndNode
	 * @param transferredNodeParent 
	 * @param e
	 */
	void onNodeDropped(DefaultMutableTreeNodeDnD droppedDndNode, TreeNode droppedNodeParent,
			DefaultMutableTreeNodeDnD transferedDndNode, TreeNode transferredNodeParent,
			DropTargetDropEvent e);
}
