/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package org.maclan.server;

import java.util.LinkedList;

/**
 * Media rules.
 * @author user
 *
 */
class MediaRules {

	public String media;
	public LinkedList<SelectorRules> selectorsRules = new LinkedList<SelectorRules>();

	public MediaRules(String media) {
		
		this.media = media;
	}

	/**
	 * Insert selector rules.
	 * @param selector
	 * @param property
	 * @param value
	 * @param isImportant 
	 */
	public void insert(String selector, String property, String value, boolean isImportant) {
		
		// Find selector rules.
		SelectorRules selectorRules = null;
		
		for (SelectorRules selectorRulesItem : selectorsRules) {
			if (selectorRulesItem.selector.equals(selector)) {
				
				selectorRules = selectorRulesItem;
				break;
			}
		}
		if (selectorRules == null) {
			selectorRules = new SelectorRules(selector);
			selectorsRules.add(selectorRules);
		}
		
		// Insert property.
		selectorRules.insert(property, value, isImportant);
	}
}