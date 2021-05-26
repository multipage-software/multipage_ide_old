/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 19-02-2021
 *
 */
package org.multipage.generator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author vakol
 *
 */
public class ConditionalEventsAuxTable {
	
	/**
	 * Record.
	 */
	private static class Record {
		
		/**
		 * Key.
		 */
		public Object key = null;
		
		/**
		 * Condition.
		 */
		public EventCondition condition = null;
		
		/**
		 * Priority.
		 */
		public Integer priority = null;
		
		/**
		 * Set of event handles.
		 */
		public HashSet<EventHandle> handles = null;
	}
	
	/**
	 * Table.
	 */
	private LinkedList<Record> table = new LinkedList<Record>();
	
	/**
	 * Create auxiliary table for conditional events.
	 * @param conditionalEvents
	 * @return
	 */
	public static ConditionalEventsAuxTable createFrom(
			LinkedHashMap<EventCondition, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> conditionalEvents) {
		
		// Create, set and return new singleton object.
		ConditionalEventsAuxTable conditionalEventsTable = new ConditionalEventsAuxTable();
		
		// Create new table of conditional events.
		conditionalEvents.forEach((condition, map1) -> {
			map1.forEach((priority, map2) -> {
				map2.forEach((key, handles) -> {
					conditionalEventsTable.addRecords(key, condition, priority, handles);
				});
			});
		});
		
		return conditionalEventsTable;
	}
	
	/**
	 * Add new records.
	 * @param key
	 * @param condition
	 * @param priority
	 * @param handles
	 */
	private void addRecords(Object key, EventCondition condition, Integer priority,
			LinkedList<EventHandle> handles) {
		
		// Delegate the call.
		handles.forEach(handle -> {
			addRecord(key, condition, priority, handle);
		});
	}

	/**
	 * Add new table item.
	 * @param key
	 * @param eventCondition
	 * @param priority
	 * @param handle
	 */
	public void addRecord(Object key, EventCondition eventCondition, Integer priority, EventHandle handle) {
		
		// Create new record.
		Record record = new Record();
		record.key = key;
		record.condition = eventCondition;
		record.priority = priority;
		
		if (record.handles == null) {
			record.handles = new HashSet<EventHandle>();
		}
		
		record.handles.add(handle);
		
		table.add(record);
	}
	
	/**
	 * Sort and retrieve conditional events.
	 * @return
	 */
	public LinkedHashMap<EventCondition, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> retrieveSorted() {
		
		// Sort table records.
		Collections.sort(table, new Comparator () {

			@Override
			public int compare(Object o1, Object o2) {

				Record r1 = (Record) o1;
				Record r2 = (Record) o2;
				
				int delta = r1.condition.ordinal() - r2.condition.ordinal();
				if (delta == 0) {
					delta = r1.priority - r2.priority;
				}
				// Records with higher priority are on the table bottom.
				return -delta;
			}
		});
		
		// Create new conditional events.
		LinkedHashMap<EventCondition, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> conditionalEvents
			= new LinkedHashMap<EventCondition, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>>();
		
		// Add sorted records of the table.
		table.forEach(record -> {
			
			// Map 1
			LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>> map1 = conditionalEvents.get(record.condition);
			if (map1 == null) {
				map1 = new LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>();
				conditionalEvents.put(record.condition, map1);
			}
			
			// Map 2
			LinkedHashMap<Object, LinkedList<EventHandle>> map2 = map1.get(record.priority);
			if (map2 == null) {
				map2 = new LinkedHashMap<Object, LinkedList<EventHandle>>();
				map1.put(record.priority, map2);
			}
			
			// Handles
			LinkedList<EventHandle> handles = map2.get(record.key);
			if (handles == null) {
				handles = new LinkedList<EventHandle>();
				map2.put(record.key, handles);
			}
			handles.addAll(record.handles);
		});
		
		return conditionalEvents;
	}
	
}
