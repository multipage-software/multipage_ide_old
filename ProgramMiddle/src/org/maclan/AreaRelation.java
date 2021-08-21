/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.LinkedList;

/**
 * @author
 *
 */
public class AreaRelation {

	/**
	 * Relation name - sub.
	 */
	private String relationNameSub;
	
	/**
	 * Relation name - super.
	 */
	private String relationNameSuper;

	/**
	 * Inheritance.
	 */
	private boolean inheritance;

	/**
	 * Hide sub areas flag.
	 */
	private boolean hideSub;
	
	/**
	 * Is reference flag.
	 */
	private boolean reference;

	/**
	 * Constructor.
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub 
	 * @param reference 
	 */
	public AreaRelation(boolean inheritance, String relationNameSub,
			String relationNameSuper, boolean hideSub, boolean reference) {
		
		this.inheritance = inheritance;
		this.relationNameSub = relationNameSub;
		this.relationNameSuper = relationNameSuper;
		this.hideSub = hideSub;
		this.reference = reference;
	}

	/**
	 * @return the inheritance
	 */
	public boolean isInheritance() {
		
		return inheritance;
	}

	/**
	 * Get relation name.
	 * @return
	 */
	public String getRelationNameSub() {
		
		if (relationNameSub == null) {
			return "";
		}
		return relationNameSub;
	}

	/**
	 * Get relation name.
	 * @return
	 */
	public String getRelationNameSuper() {
		
		if (relationNameSuper == null) {
			return "";
		}
		return relationNameSuper;
	}

	/**
	 * Set inheritance.
	 * @param inheritance
	 */
	public void setInheritance(boolean inheritance) {
		
		this.inheritance = inheritance;
	}

	/**
	 * Set relation name sub.
	 * @param relationName
	 */
	public void setRelationNameSub(String relationName) {
		
		this.relationNameSub = relationName;
	}

	/**
	 * Set relation name super.
	 * @param relationName
	 */
	public void setRelationNameSuper(String relationName) {
		
		this.relationNameSuper = relationName;
	}

	/**
	 * Returns true value if to hide subareas.
	 * @return
	 */
	public boolean isHideSub() {
		
		return hideSub;
	}

	/**
	 * Set relation flag.
	 * @param hideSub
	 */
	public void setHideSub(boolean hideSub) {
		
		this.hideSub = hideSub;
	}

	/**
	 * @return the reference
	 */
	public boolean isRecursion() {
		return reference;
	}

	/**
	 * Return true value if the list contains given relation.
	 * @param relations
	 * @param relation
	 * @return
	 */
	public static boolean containsRelation(
			LinkedList<AreaRelation> relations,
			AreaRelation relation) {
		
		for (AreaRelation foundRelation : relations) {
			
			if (foundRelation.equalsRelation(relation)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true value if the relation equals to this relation.
	 * @param relation
	 * @return
	 */
	public boolean equalsRelation(AreaRelation relation) {
		
		return inheritance == relation.inheritance
				&& hideSub == relation.hideSub
				&& relationNameSub == relation.relationNameSub
				&& relationNameSuper == relation.relationNameSuper;
	}
}
