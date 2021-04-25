/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.tree.*;

/**
 * @author
 *
 */
public class DefaultMutableTreeNodeDnD extends DefaultMutableTreeNode implements Transferable, Serializable {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Data flavors list.
	 */
	public static final DataFlavor treeNodeFlavor = new DataFlavor(
			DefaultMutableTreeNodeDnD.class, DataFlavor.javaJVMLocalObjectMimeType);
	
	private static DataFlavor [] dataFlavors = { treeNodeFlavor };

	/**
	 * Node marked flag.
	 */
	private boolean marked = false;

	/**
	 * Constructor.
	 * @param userObject
	 */
	public DefaultMutableTreeNodeDnD(Object userObject) {
		
		this.setUserObject(userObject);
	}

	/**
	 * Get data.
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		
		return this;
	}

	/**
	 * Get data flavors.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		
		return dataFlavors;
	}

	/**
	 * Returns true value if a data flavor is supported.
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		
		return flavor.equals(dataFlavors[0]);
	}

	/**
	 * Returns true value if the node is transferable.
	 * @return
	 */
	public boolean isTransferable() {
		
		// Override this method.
		return true;
	}

	/**
	 * Mark the node.
	 * @param value
	 */
	public void mark(boolean value) {
		
		this.marked = value;
	}
	
	/**
	 * Gets marked flag.
	 */
	public boolean isMarked() {
		
		return marked;
	}
}
