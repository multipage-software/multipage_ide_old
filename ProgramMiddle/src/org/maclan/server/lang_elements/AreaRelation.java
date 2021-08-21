/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

//graalvm import org.graalvm.polyglot.HostAccess;

/**
 * @author
 *
 */
public class AreaRelation implements BoxedObject {

	/**
	 * Relation middle object reference.
	 */
	org.maclan.AreaRelation relation;
	
	/**
	 * Constructor.
	 * @param relation
	 */
	public AreaRelation(org.maclan.AreaRelation relation) {
		
		this.relation = relation;
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return relation;
	}

	/**
	 * Get relation sub name.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getSubName() {
		
		return relation.getRelationNameSub();
	}
	
	/**
	 * Get relation super name.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getSuperName() {
		
		return relation.getRelationNameSuper();
	}
	
	/**
	 * Returns true value if the relation inherits.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isInheritance() {
		
		return relation.isInheritance();
	}
}
