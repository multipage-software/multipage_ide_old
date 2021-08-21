/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package org.maclan.server;

import java.util.LinkedList;

/**
 * 
 * @author user
 *
 */
class MediasRules {
	
	LinkedList<MediaRules> mediasRules = new LinkedList<MediaRules>();

	/**
	 * Insert media rules.
	 * @param media
	 * @param selector
	 * @param property
	 * @param value
	 * @param isImportant 
	 */
	public void insert(String media, String selector, String property, String value, boolean isImportant) {
		
		// Find media rules.
		MediaRules mediaRules = null;
		
		for (MediaRules mediaRulesItem : mediasRules) {
			if (mediaRulesItem.media.equals(media)) {
				
				mediaRules = mediaRulesItem;
				break;
			}
		}
		if (mediaRules == null) {
			mediaRules = new MediaRules(media);
			mediasRules.add(mediaRules);
		}
		
		// Insert selector rules.
		mediaRules.insert(selector, property, value, isImportant);
	}
}
