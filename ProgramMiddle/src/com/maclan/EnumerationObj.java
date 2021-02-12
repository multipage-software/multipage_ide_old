/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.util.*;

/**
 * @author
 *
 */
public class EnumerationObj {

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Description.
	 */
	private String description = "";
	
	/**
	 * Values.
	 */
	private LinkedList<EnumerationValue> values = new LinkedList<EnumerationValue>();

	/**
	 * Constructor.
	 * @param id
	 * @param description
	 */
	public EnumerationObj(long id, String description) {
		
		this.id = id;
		this.description = description;
	}

	/**
	 * Get identifier.
	 * @return
	 */
	public long getId() {
		
		return id;
	}

	/**
	 * Set values.
	 * @param valuesPar
	 */
	public void insertValues(LinkedList<EnumerationValue> valuesPar) {
		
		values.clear();
		
		for (EnumerationValue value : valuesPar) {
			
			value.setEnumeration(this);
			values.add(value);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return description;
	}

	/**
	 * Show description with IDs.
	 * @return
	 */
	public String getDescriptionWithIds() {
		
		return String.format("[%d] %s", id, description);
	}

	/**
	 * Get description.
	 * @return
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Get values.
	 * @return
	 */
	public LinkedList<EnumerationValue> getValues() {
		
		return values;
	}

	/**
	 * Get enumeration value.
	 * @param enumerationValueId
	 * @return
	 */
	public EnumerationValue getValue(long enumerationValueId) {
		
		for (EnumerationValue enumerationValue : values) {
			if (enumerationValue.getId() == enumerationValueId) {
				return enumerationValue;
			}
		}
		return null;
	}

	/**
	 * Insert value.
	 * @param enumerationValueId
	 * @param enumerationValueText
	 */
	public void insertValue(long enumerationValueId, String enumerationValueText) {
		
		// Try to find existing value.
		for (EnumerationValue value : values) {
			
			if (value.getId() == enumerationValueId) {
				
				value.setValue(enumerationValueText);
				return;
			}
		}
		
		// If not found, create new and add it to the list.
		EnumerationValue value = new EnumerationValue();
		values.add(value);
		
		value.setEnumeration(this);
		value.setId(enumerationValueId);
		value.setValue(enumerationValueText);
	}

	/**
	 * Get value.
	 * @param value
	 * @return
	 */
	public EnumerationValue getValue(String value) {
		
		// Do loop for all values.
		for (EnumerationValue enumerationValue : values) {
			
			if (enumerationValue.getValue().equals(value)) {
				return enumerationValue;
			}
		}
		
		return null;
	}
}
