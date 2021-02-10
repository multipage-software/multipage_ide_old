/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * @author
 *
 */
public abstract class ConstructorSubObject {
	
	/**
	 * Parent constructor holder reference.
	 */
	private ConstructorHolder parentConstructorHolder;
	
	/**
	 * Changed flag.
	 */
	protected boolean changed = false;
	
	/**
	 * Set changed flag.
	 */
	public void setChanged() {
		
		changed = true;
	}

	/**
	 * Get parent constructor holder.
	 */
	public ConstructorHolder getParentConstructorHolder() {
		return parentConstructorHolder;
	}
	
	/**
	 * Get constructor group reference.
	 */
	public abstract ConstructorGroup getConstructorGroup();

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Set parent constructor holder.
	 * @param parentConstructorHolder
	 */
	public void setParentConstructorHolder(ConstructorHolder parentConstructorHolder) {
		
		this.parentConstructorHolder = parentConstructorHolder;
	}
}
