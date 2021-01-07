/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.*;

/**
 * @author
 *
 */
public class SetObj extends LinkedHashSet {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Get linked list.
	 * @return
	 */
	public LinkedList toList() {
		
		LinkedList list = new LinkedList();
		list.addAll(this);
		
		return list;
	}

	/**
	 * Add all items.
	 * @param collection
	 */
	public void addCollection(Collection collection) {
		
		addAll(collection);
	}
}
