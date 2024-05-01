/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 18-11-2023
 *
 */
package org.multipage.util;

/**
 * All closable object can inherit from this interface.
 */
public interface Closable {

	/**
	 * Close object.
	 */
	void close();
}
