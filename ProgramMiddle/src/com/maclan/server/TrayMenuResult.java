/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 27-04-2020
 *
 */
package com.maclan.server;

import java.util.LinkedList;

/**
 * 
 * @author user
 *
 */
public class TrayMenuResult {
	
	/**
	 * Class item.
	 */
	public static class Item {
		
		/**
		 * Name of trayMenu item.
		 */
		public String name;
		
		/**
		 * Action for trayMenu item.
		 */
		public String action;
		
		/**
		 * Constructor.
		 * @param name
		 * @param action
		 */
		public Item(String name, String action) {
			
			this.name = name;
			this.action = action;
		}
	}
	
	/**
	 * Area server result generated for Sync trayMenu.
	 */
	private LinkedList<Item> items = new LinkedList<Item>();
	
	/**
	 * Add tray trayMenu result.
	 * @param name
	 * @param action
	 */
	public void add(String name, String action) {
		
		items.add(new Item(name, action));
	}
	
	/**
	 * Get items.
	 * @return
	 */
	public LinkedList<Item> getItems() {
		
		return items;
	}
}
