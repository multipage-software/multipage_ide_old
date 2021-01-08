/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

/**
 * @author
 *
 */
public interface SlotValueEditorPanelInterface {
	
	/**
	 * Get value.
	 */
	Object getValue();

	/**
	 * Set value.
	 * @param value
	 */
	void setValue(Object value);

	/**
	 * Set default state.
	 * @param isDefault
	 */
	void setDefault(boolean isDefault);

	/**
	 * Get value meaning.
	 * @return
	 */
	String getValueMeaning();
}
