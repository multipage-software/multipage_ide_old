/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 10-03-2020
 *
 */
package com.maclan.server;

/**
 * 
 * @author user
 *
 */
class AreaMediasRules {

	public long areaId;
	public MediasRules mediasRules = new MediasRules();
	
	public AreaMediasRules(long areaId) {
		
		this.areaId = areaId;
	}

	public void insert(String media, String selector, String property, String value, boolean isImportant) {
		
		mediasRules.insert(media, selector, property, value, isImportant);
	}
}
