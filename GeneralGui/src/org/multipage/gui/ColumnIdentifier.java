/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class ColumnIdentifier {

	/**
	 * Column name.
	 */
	private String columnName;
	
	/**
	 * Set column identifier.
	 * @param columnNameTextIdentifier
	 */
	public ColumnIdentifier(String columnNameTextIdentifier) {
		
		columnName = Resources.getString(columnNameTextIdentifier);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return columnName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		return ((String) obj).equals(columnName);
	}
}
