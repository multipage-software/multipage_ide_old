/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 01-06-2024
 *
 */
package org.maclan.server;

import org.multipage.util.Resources;

/**
 * Debugger watch list item type.
 * @author vakol
 */
public enum DebugWatchItemType {

	/**
	 * Enumeration of watch list item types.
	 */
	tagProperty("org.maclan.server.textDebugWatchTagProperty"),
	blockVariable("org.maclan.server.textDebugWatchBlockVariable"),
	expression("org.maclan.server.textDebugWatchExpression");
	
	/**
	 * Description of the type.
	 */
	private String descriptionId = null;
	private String description = null;
	
	/**
	 * Constructor.
	 * @param descriptionId
	 */
	DebugWatchItemType(String descriptionId) {
		
		this.descriptionId = descriptionId; 
	}
	
	/**
	 * Get type name.
	 * @return
	 */
	public String getName() {
		
		String name = name();
		return name;
	}

	/**
	 * Get type description.
	 */
	@Override
	public String toString() {
		
		if (description == null) {
			description = Resources.getString(descriptionId);
		}
		return description;
	}
	
	/**
	 * Check if type name matches this enum item.
	 * @param typeName
	 * @return
	 */
	public boolean checkTypeName(String typeName) {
		
		String enumName = super.name();
		boolean matches = enumName.equals(typeName);
		return matches;
	}
}
