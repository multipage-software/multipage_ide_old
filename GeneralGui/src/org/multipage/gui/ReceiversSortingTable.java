/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 19-02-2021
 *
 */
package org.multipage.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author vakol
 *
 */
public class ReceiversSortingTable {
	
	/**
	 * Record.
	 */
	private static class ReceiverRecord {
		
		/**
		 * Key.
		 */
		public Object receiverObject = null;
		
		/**
		 * Condition.
		 */
		public ApplicationEvent condition = null;
		
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
	 * List of all receivers.
	 */
	private LinkedList<ReceiverRecord> receivers = new LinkedList<ReceiverRecord>();
	
	/**
	 * Create sorting table for conditional events.
	 * @param conditionalEvents
	 * @return
	 */
	public static ReceiversSortingTable createFrom(LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> conditionalEvents) {
		
		// Create, set and return new singleton object.
		ReceiversSortingTable conditionalEventsTable = new ReceiversSortingTable();
		
		// Create new list of conditional events.
		conditionalEvents.forEach((condition, map1) -> {
			map1.forEach((priority, map2) -> {
				map2.forEach((receiverObject, handles) -> {
					conditionalEventsTable.addReceivers(receiverObject, condition, priority, handles);
				});
			});
		});
		
		return conditionalEventsTable;
	}
	
	/**
	 * Add new receivers to the list of all receivers.
	 * @param receiverObject
	 * @param condition
	 * @param priority
	 * @param handles
	 */
	private void addReceivers(Object receiverObject, ApplicationEvent condition, Integer priority,
			LinkedList<EventHandle> handles) {
		
		// Delegate this call for all input handles to "addRecord(...)".
		handles.forEach(handle -> {
			addReceiver(receiverObject, condition, priority, handle);
		});
	}

	/**
	 * Add new receiver to the list of all receivers.
	 * @param receiverObject
	 * @param eventCondition
	 * @param priority
	 * @param handle
	 */
	public void addReceiver(Object receiverObject, ApplicationEvent eventCondition, Integer priority,
			EventHandle handle) {
		
		// Create new receiver.
		ReceiverRecord receiverRecord = new ReceiverRecord();
		receiverRecord.receiverObject = receiverObject;
		receiverRecord.condition = eventCondition;
		receiverRecord.priority = priority;
		
		if (receiverRecord.handles == null) {
			receiverRecord.handles = new HashSet<EventHandle>();
		}
		
		receiverRecord.handles.add(handle);
		
		receivers.add(receiverRecord);
	}
	
	/**
	 * Sort and retrieve conditional events.
	 * @return
	 */
	public LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> retrieveSortedReceivers() {
		
		// Sort table records.
		Collections.sort(receivers, new Comparator<ReceiverRecord> () {

			@Override
			public int compare(ReceiverRecord r1, ReceiverRecord r2) {
				
				int delta = r1.condition.ordinal() - r2.condition.ordinal();
				if (delta == 0) {
					delta = r1.priority - r2.priority;
				}
				// Records with higher priority are on the table bottom.
				return -delta;
			}
		});
		
		// Create output list of sorted receivers.
		LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>> sortedReceivers =
			new LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>>();
		
		// Load the list of sorted receivers.
		receivers.forEach(record -> {
			
			// Maps conditions.
			LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>> map1 = sortedReceivers.get(record.condition);
			if (map1 == null) {
				map1 = new LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>();
				sortedReceivers.put(record.condition, map1);
			}
			
			// Maps priorities.
			LinkedHashMap<Object, LinkedList<EventHandle>> map2 = map1.get(record.priority);
			if (map2 == null) {
				map2 = new LinkedHashMap<Object, LinkedList<EventHandle>>();
				map1.put(record.priority, map2);
			}
			
			// Adds event handles
			LinkedList<EventHandle> handles = map2.get(record.receiverObject);
			if (handles == null) {
				handles = new LinkedList<EventHandle>();
				map2.put(record.receiverObject, handles);
			}
			handles.addAll(record.handles);
		});
		
		return sortedReceivers;
	}
}
