/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 01-06-2024
 *
 */
package org.maclan.server;

/**
 * Item of debug watch list.
 * @author vakol
 */
public class DebugWatchItem {

	/**
	 * Item name.
	 */
	private String name = null;
	
	/**
	 * Item type.
	 */
	private DebugWatchItemType type = null;
	
	/**
	 * Item full name.
	 */
	private String fullName = null;
	
	/**
	 * Text rep[resentation of the item value.
	 */
	private String value = null;

	/**
	 * Value type.
	 */
	private String valueType = null;
	
	/**
	 * Constructor.
	 * @param name
	 * @param type
	 */
	public DebugWatchItem(String name, DebugWatchItemType type) {
		
		this.name = name;
		this.type = type;
	}

	/**
	 * Constructor.
	 * @param type
	 * @param name
	 * @param fullName
	 * @param value
	 * @param valueType
	 */
	public DebugWatchItem(DebugWatchItemType type, String name, String fullName, String value, String valueType) {
		
		this.name = name;
		this.type = type;
		this.fullName = fullName;
		this.value = value;
		this.valueType = valueType;
	}

	/**
	 * Get watch item name.
	 * @return
	 */
	public String getName() {
		
		return name;
	}
	
	/**
	 * Get watch item full name.
	 * @return
	 */
	public String getFullName() {
		
		return fullName;
	}
	
	/**
	 * Get watch item type.
	 * @return
	 */
	public DebugWatchItemType getType() {
		
		return type;
	}
	
	/**
	 * Get type name.
	 * @return
	 */
	public String getTypeName() {
		
		if (type == null) {
			return "unknown";
		}
		String typeName = type.name();
		return typeName;
	}
	
	/**
	 * Get watch item value.
	 * @return
	 */
	public String getValue() {
		
		return value;
	}
	
	/**
	 * Get watch item value type.
	 * @return
	 */
	public String getValueType() {
		
		return valueType;
	}

	/**
	 * Returns true value if the name and property type matches.
	 * @param name
	 * @param type
	 * @return
	 */
	public boolean matches(String name, DebugWatchItemType type) {
		
		if (name == null || type == null) {
			return false;
		}
		
		boolean matches = name.equals(this.name) && type.equals(this.type);
		return matches;
	}
	
	/**
	 * Get text representation of the watch item.
	 */
	@Override
	public String toString() {
		
		if (fullName != null && !fullName.isEmpty()) {
			return fullName;
		}
		return name;
	}
}
