/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 14-08-2021
 *
 */

package org.maclan.server;

/**
 * 
 * @author vakol
 *
 */
public class TagsSource {
	
	/**
	 * Slot ID.
	 */
	public Long slotId = null;
	
	/**
	 * Resource ID.
	 */
	public Long resourceId = null;
	
	/**
	 * Create new source slot.
	 * @param slotId
	 * @return
	 */
	public static TagsSource newSlot(long slotId) {
		
		TagsSource source = new TagsSource();
		source.slotId = slotId;
		return source;
	}
	
	/**
	 * Create new source resource.
	 * @param resourceId
	 * @return
	 */
	public static TagsSource newResource(long resourceId) {
		
		TagsSource source = new TagsSource();
		source.resourceId = resourceId;
		return source;
	}
	
	/**
	 * Get string representation
	 */
	@Override
	public String toString() {
		
		if (slotId != null) {
			return "s" + String.valueOf(slotId);
		}
		
		if (resourceId != null) {
			return "r" + String.valueOf(resourceId);
		}
		
		return "unknown";
	}
	
	/**
	 * Set slot as a tags source.
	 * @param slotId
	 */
	public void setSlot(long slotId) {
		
		this.slotId = slotId;
	}
	
	/**
	 * Set resource as a tags source.
	 * @param resourceId
	 */
	public void setResource(long resourceId) {
		
		this.resourceId = resourceId;
	}
}
