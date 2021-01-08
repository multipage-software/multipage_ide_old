/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package com.maclan.server;

import java.util.LinkedList;

/**
 * 
 * @author user
 *
 */
public class AreasMediasRules {

	LinkedList<AreaMediasRules> areasMediasRules = new LinkedList<AreaMediasRules>();
	
	/**
	 * Insert new CSS rule.
	 * @param area
	 * @param media
	 * @param selector
	 * @param property
	 * @param value
	 * @param isImportant 
	 */
	public void insert(long areaId, String media, String selector, String property, String value, boolean isImportant) {
		
		// Find area medias rules.
		AreaMediasRules areaMediasRules = null;
		
		for (AreaMediasRules areaMediasRulesItem : areasMediasRules) {
			if (areaMediasRulesItem.areaId == areaId) {
				
				areaMediasRules = areaMediasRulesItem;
				break;
			}
		}
		
		if (areaMediasRules == null) {
			areaMediasRules = new AreaMediasRules(areaId);
			areasMediasRules.add(areaMediasRules);
		}
		
		areaMediasRules.insert(media, selector, property, value, isImportant);
	}
	
	/**
	 * Clear rules.
	 */
	public void clear() {
		
		areasMediasRules.clear();
	}	
}
