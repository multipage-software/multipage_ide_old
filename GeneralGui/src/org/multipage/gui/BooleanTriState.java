/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class BooleanTriState {
	
	public static final BooleanTriState UNKNOWN = new BooleanTriState(null, "org.multipage.gui.textUnknownState");
	public static final BooleanTriState TRUE = new BooleanTriState(true, "org.multipage.gui.textTrueState");
	public static final BooleanTriState FALSE = new BooleanTriState(false, "org.multipage.gui.textFalseState");
	
	public Boolean value;
	private String descriptionId;
	
	public BooleanTriState(Boolean value, String descriptionId) {
		this.value = value;
		this.descriptionId = descriptionId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Resources.getString(descriptionId);
	}
}
