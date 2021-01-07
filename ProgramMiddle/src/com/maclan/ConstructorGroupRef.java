/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.util.LinkedList;

import org.multipage.gui.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class ConstructorGroupRef extends ConstructorSubObject implements IdentifiedTreeNode {

	/**
	 * Reference to group.
	 */
	public ConstructorGroup ref;
	
	/**
	 * Group ID. Helper property.
	 */
	public long groupIdHelper;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return Resources.getString("middle.textConstructorGroupReference");
	}

	/**
	 * Constructor.
	 * @param constructorGroup
	 */
	public ConstructorGroupRef(ConstructorGroup constructorGroup) {
		
		ref = constructorGroup;
	}

	/**
	 * Constructor.
	 */
	public ConstructorGroupRef() {
		
	}

	/**
	 * Get constructor group.
	 */
	@Override
	public ConstructorGroup getConstructorGroup() {
		
		return ref;
	}
	
	/**
	 * Set constructor group reference.
	 * @param constructorGroup
	 */
	public void setConstructorGroupReference(ConstructorGroup constructorGroup) {
		
		ref = constructorGroup;
	}

	/**
	 * Get identifier.
	 */
	@Override
	public long getId() {
		
		if (ref != null) {
			return ref.getId();
		}
		return -1;
	}

	/**
	 * Get children.
	 */
	@Override
	public LinkedList getChildren() {
		
		// Return empty list.
		return new LinkedList();
	}
}
