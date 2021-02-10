/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
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
