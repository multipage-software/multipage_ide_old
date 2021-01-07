/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

/**
 * @author
 *
 */
public class FoundAttr {

	/**
	 * Attributes.
	 */
	public String searchText;
	public boolean isCaseSensitive;
	public boolean isWholeWords;

	/**
	 * Constructor.
	 * @param searchText
	 * @param isCaseSensitive
	 * @param isWholeWords
	 * @param isExactMatch
	 */
	public FoundAttr(String searchText, boolean isCaseSensitive,
			boolean isWholeWords) {
		
		this.searchText = searchText;
		this.isCaseSensitive = isCaseSensitive;
		this.isWholeWords = isWholeWords;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FoundAttr [searchText=" + searchText + ", isCaseSensitive="
				+ isCaseSensitive + ", isWholeWords=" + isWholeWords + "]";
	}
}
