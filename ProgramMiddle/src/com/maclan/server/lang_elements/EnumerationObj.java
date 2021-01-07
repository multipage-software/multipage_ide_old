/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server.lang_elements;

import java.util.LinkedList;

//graalvm import org.graalvm.polyglot.HostAccess;

/**
 * @author
 *
 */
public class EnumerationObj implements BoxedObject {

	/**
	 * Middle object reference.
	 */
	com.maclan.EnumerationObj enumeration;
	
	/**
	 * Public fields.
	 */
	//graalvm @HostAccess.Export
	public final long id;
	
	/**
	 * Constructor.
	 * @param enumeration
	 */
	public EnumerationObj(com.maclan.EnumerationObj enumeration) {
		
		this.enumeration = enumeration;
		
		this.id = enumeration.getId();
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return enumeration;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return String.format("[Enumeration object id = %d]", id);
	}
	
	/**
	 * Get enumeration description.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getDescription() {
		
		return enumeration.getDescription();
	}
	
	/**
	 * Get list of enumeration values.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public LinkedList<EnumerationValue> getValues() {
		
		LinkedList<com.maclan.EnumerationValue> middleValues = enumeration.getValues();
		LinkedList<EnumerationValue> values = new LinkedList<EnumerationValue>();
		
		for (com.maclan.EnumerationValue middleValue : middleValues) {
			values.add(new EnumerationValue(middleValue));
		}
		
		return values;
	}
}
