/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.multipage.util.*;

/**
 * @author
 *
 */
public class JTreeDnD extends JTree implements DragGestureListener, DragSourceListener, DropTargetListener {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Cursors.
	 */
	private static Cursor linkCursor;
	private static Cursor moveCursor;
	private static Cursor noDropCursor;
	
	/**
	 * Static constructor.
	 */
	static {
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		// Load cursors.
		Image image = Images.getImage("org/multipage/gui/images/copy_cursor.png");
		if (image != null) {
			
			linkCursor = toolkit.createCustomCursor(image, new Point(), "");
		}
		else {
			linkCursor = Cursor.getDefaultCursor();
		}
		
		image = Images.getImage("org/multipage/gui/images/move_cursor.png");
		if (image != null) {
			
			moveCursor = toolkit.createCustomCursor(image, new Point(), "");
		}
		else {
			moveCursor = Cursor.getDefaultCursor();
		}
		
		image = Images.getImage("org/multipage/gui/images/no_drop.png");
		if (image != null) {
			
			noDropCursor = toolkit.createCustomCursor(image, new Point(), "");
		}
		else {
			noDropCursor = Cursor.getDefaultCursor();
		}
	}
	
	/**
	 * Drag source object.
	 */
	private DragSource dragSource;
	
	/**
	 * Drop target object.
	 */
	@SuppressWarnings("unused")
	private DropTarget dropTarget;

	/**
	 * Marked DnD node.
	 */
	private DefaultMutableTreeNodeDnD markedDndNode;

	/**
	 * Marked node parent.
	 */
	private TreeNode markedNodeParent;

	/**
	 * Drag and Drop callback.
	 */
	private JTreeDndCallback dndCallback;

	/**
	 * Enable Drag and Drop flag.
	 */
	private boolean enableDragAdnDrop = true;

	/**
	 * Constructor.
	 */
	public JTreeDnD() {
		
		// Create drag source, drop target and drag gesture recognizer.
		dragSource = new DragSource();
		
		dropTarget = new DropTarget(this, this);
		
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE | DnDConstants.ACTION_LINK,
		        this);
	}

	/**
	 * Clear all marked nodes.
	 */
	private void clearAllMarked() {
		
		markedDndNode = null;
		
		TreeModel model = getModel();
		if (model == null) {
			return;
		}
		
		// Traverse the tree model and remove marks.
		Object node = model.getRoot();
		if (node == null) {
			return;
		}
		
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(node);
		
		while(!queue.isEmpty()) {
			
			node = queue.removeFirst();
			if (node instanceof DefaultMutableTreeNodeDnD) {
				
				DefaultMutableTreeNodeDnD dndNode = (DefaultMutableTreeNodeDnD) node;
				dndNode.mark(false);
			}
			
			int count = model.getChildCount(node);
			for (int index = 0; index < count; index++) {
				
				Object childNode = model.getChild(node, index);
				if (childNode != null) {
					queue.addLast(childNode);
				}
			}
		}
		
		updateUI();
	}

	/**
	 * Enable / disable Drag and Drop.
	 * @param enable
	 */
	public void enableDragAndDrop(boolean enable) {
		
		enableDragAdnDrop = enable;
	}

	/**
	 * On drag gesture recognized.
	 */
	@Override
	public void dragGestureRecognized(DragGestureEvent e) {
		
		if (!enableDragAdnDrop) {
			return;
		}
		
		// Get selected transferable tree node and start drag.
		Obj<Transferable> transferable = new Obj<Transferable>();
		Obj<TreeNode> parentNode = new Obj<TreeNode>();
		
		getSelectedTransferableNode(transferable, parentNode);
		if (transferable.ref == null || parentNode.ref == null) {
			return;
		}
		
		try {
			dragSource.startDrag(e, selectCursor(e.getDragAction()), transferable.ref, this);
		}
		catch (Exception exception) {
			
		}
	}

	/**
	 * Get transferable node object.
	 * @param transferable
	 * @param parentNode 
	 * @return
	 */
	private void getSelectedTransferableNode(Obj<Transferable> transferable,
			Obj<TreeNode> parentNode) {
		
		// Get selected node and if it is transferable, return it.
		TreePath path = getSelectionPath();
		if (path == null) {
			return;
		}
		
		Object node = path.getLastPathComponent();
		if (!(node instanceof DefaultMutableTreeNodeDnD)) {
			return;
		}
		
		DefaultMutableTreeNodeDnD dndNode = (DefaultMutableTreeNodeDnD) node;
		if (!dndNode.isTransferable()) {
			return;
		}
		
		transferable.ref = dndNode;
		parentNode.ref = dndNode.getParent();
	}

	/**
	 * Select cursor depending on action.
	 * @param dragAction
	 * @return
	 */
	private Cursor selectCursor(int dragAction) {
		
		switch (dragAction) {
			case 2:
				return moveCursor;
			case 1:
				return linkCursor;
			default:
				return noDropCursor;
		}
	}

	/**
	 * On exit tree view.
	 * @param arg0
	 */
	@Override
	public void dragExit(DragSourceEvent e) {
		
		clearAllMarked();
	}

	/**
	 * On drag over.
	 */
	@Override
	public void dragOver(DragSourceDragEvent e) {
		
		// Process action, set cursor and clear marked nodes.
		clearAllMarked();
		
		int action = e.getTargetActions();
		
		setCursor(selectCursor(action));

		if (!(action == 1 || action == 2)) {
			return;
		}

		// Mark node or clear or all marks.
		Point location = e.getLocation();
		if (location == null) {
			return;
		}
		
		SwingUtilities.convertPointFromScreen(location, this);

		TreePath path = getPathForLocation(location.x, location.y);
		if (path == null) {
			return;
		}

		Object node = path.getLastPathComponent();
		if (!(node instanceof DefaultMutableTreeNodeDnD)) {
			return;
		}
		
		DefaultMutableTreeNodeDnD dndNode = (DefaultMutableTreeNodeDnD) node;
		dndNode.mark(true);
		
		markedDndNode = dndNode;
		
		// Get marked node parent.
		markedNodeParent = null;
		int nodeCount = path.getPathCount();
		
		if (nodeCount > 1) {
			Object nodeObject = path.getPathComponent(nodeCount - 2);
			
			if (nodeObject instanceof TreeNode) {
				markedNodeParent = (TreeNode) nodeObject;
			}
		}
		
		// Update component view.
		updateUI();
	}

	/**
	 * On drop.
	 * @param e
	 */
	@Override
	public void drop(DropTargetDropEvent e) {
		
		// Get transferable node and its parent.
		Obj<Transferable> transferable = new Obj<Transferable>();
		Obj<TreeNode> transferredNodeParent = new Obj<TreeNode>();
		
		getSelectedTransferableNode(transferable, transferredNodeParent);
		
		if (transferable.ref == null) {
			
			e.rejectDrop();
			return;
		}
		
		Object data = null;
		try {
			data = transferable.ref.getTransferData(DefaultMutableTreeNodeDnD.treeNodeFlavor);
		}
		catch (Exception exception) {
			
			e.rejectDrop();
			return;
		}
		
		if (!(data instanceof DefaultMutableTreeNodeDnD)) {
			
			e.rejectDrop();
			return;
		}
		
		DefaultMutableTreeNodeDnD transferedDndNode = (DefaultMutableTreeNodeDnD) data;
		
		// Get marked node.
		DefaultMutableTreeNodeDnD droppedDndNode = markedDndNode;
		if (droppedDndNode == null) {
			
			e.rejectDrop();
			return;
		}
		
		// Get dropped node parent.
		TreeNode droppedNodeParent = markedNodeParent;
		
		// Call callback.
		if (dndCallback != null) {
			dndCallback.onNodeDropped(droppedDndNode, droppedNodeParent,
					transferedDndNode, transferredNodeParent.ref, e);
		}
	}

	/**
	 * Set Drag and Drop callback.
	 * @param dndCallback
	 */
	public void setDragAndDropCallback(JTreeDndCallback dndCallback) {
		
		this.dndCallback = dndCallback;
	}

	/**
	 * On drop end.
	 */
	@Override
	public void dragDropEnd(DragSourceDropEvent e) {
		
		clearAllMarked();
		
		setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void dragEnter(DragSourceDragEvent e) {
		
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent e) {
		
	}

	@Override
	public void dragEnter(DropTargetDragEvent e) {
		
	}

	@Override
	public void dragExit(DropTargetEvent e) {
		
	}

	@Override
	public void dragOver(DropTargetDragEvent e) {
		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent e) {
		
	}
}
