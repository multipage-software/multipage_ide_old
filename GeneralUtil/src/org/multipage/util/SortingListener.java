/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.util;

/**
 * @author
 *
 */
public interface SortingListener<T> {

	/**
	 * Returns -1 if object1 < object2.
	 * Returns  0 if object1 == object2.
	 * Returns  1 if object1 > object2.
	 * @param object1
	 * @param object2
	 * @return
	 */
	int compare(T object1, T object2);
}
