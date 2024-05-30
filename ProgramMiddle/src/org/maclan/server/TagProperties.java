/*
 * Copyright 2010-2024 (C) vakol
 * 
 * Created on : 30-05-2017
 *
 */
package org.maclan.server;

import java.util.Properties;

/**
 * Area Server macro language tag properties.
 * @author vakol
 */
public class TagProperties extends Properties {

	/**
	 * Default version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Value and computed value of the property.
	 */
	private static final class ExtendedValue {
		
		/**
		 * Property value.
		 */
		public Object value = null;
		
		/**
		 * Computed value.
		 */
		public Object computedValue = null;
	}
	
	/**
	 * Put property computed value.
	 * @param key
	 * @param computedValue
	 */
    public synchronized Object putComputed(Object key, Object computedvalue) {
    	
    	// Get property value.
    	Object propertyValue = super.get(key);
    	boolean isExtended = (propertyValue instanceof ExtendedValue);
    	
    	ExtendedValue extendedValue = null; 
    	if (isExtended) {
    		extendedValue = (ExtendedValue) propertyValue;
    	}
    	else {
    		extendedValue = new ExtendedValue();
    		extendedValue.value = propertyValue;
    	}
    	
    	extendedValue.computedValue = computedvalue;
        return super.put(key, extendedValue);
    }
    
    /**
     * Get computed value.
     */
    public Object getComputed(Object key) {
        
    	Object valueObject = super.get(key);
    	if (!(valueObject instanceof ExtendedValue)) {
    		return null;
    	}
    	
    	ExtendedValue extendedValue = (ExtendedValue) valueObject;
    	return extendedValue.computedValue;
    }
    
    /**
     * Get value.
     */
    @Override
    public Object get(Object key) {
        
    	Object valueObject = super.get(key);
    	if (!(valueObject instanceof ExtendedValue)) {
    		return valueObject;
    	}
    	
    	ExtendedValue extendedValue = (ExtendedValue) valueObject;
    	return extendedValue.value;
    }
}
