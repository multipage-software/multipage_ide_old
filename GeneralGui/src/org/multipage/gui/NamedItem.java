/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import org.multipage.util.Resources;

/**
 * List item class.
 * @author
 *
 */
class NamedItem {
	
	public String description;
	public String value;
	
	/**
	 * Constructor.
	 * @param descriptionId
	 * @param value
	 */
	public NamedItem(String descriptionId, String value) {

		description = Resources.getString(descriptionId);
		this.value = value;
	}

	/**
	 * Constructor.
	 * @param value
	 */
	public NamedItem(String value) {
		
		description = value;
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return description;
	}
}