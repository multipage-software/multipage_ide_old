/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.util.LinkedList;

/**
 * 
 * @author
 *
 */
public interface IdentifiedTreeNode {

	/**
	 * Get identifier.
	 * @return
	 */
	long getId();

	/**
	 * Get list of children.
	 * @return
	 */
	LinkedList getChildren();
}
