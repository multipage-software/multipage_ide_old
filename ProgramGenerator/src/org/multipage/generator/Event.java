/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */
package org.multipage.generator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;

/**
 * 
 * @author user
 *
 */
public enum Event {
	
	// Special event that runs forwarded user lambda function on the event thread.
	_invokeLater,
	
	// Load diagrams on application start up.
	loadDiagrams(
			ActionGroup.areaViewStateChange,
			ActionGroup.guiChange
			),
	
	// On select areas.
	selectDiagramAreas(
			ActionGroup.areaViewStateChange,
			ActionGroup.areaViewChange,
			ActionGroup.slotViewChange,
			ActionGroup.guiChange
			),
	
	// Select/unselect all areas.
	selectAll(
			ActionGroup.areaViewStateChange,
			ActionGroup.areaViewChange,
			ActionGroup.slotViewChange,
			ActionGroup.guiChange
			),
	
	// Select/unselect all areas.
	unselectAll(
			ActionGroup.areaViewStateChange,
			ActionGroup.areaViewChange,
			ActionGroup.slotViewChange,
			ActionGroup.guiChange
			),
	
	// Main tab panel selection
	mainTabChange(
			ActionGroup.areaViewChange,
			ActionGroup.slotViewChange,
			ActionGroup.guiChange
			),
	
	// Subpanel tab change.
	subTabChange(
			ActionGroup.slotViewChange
			),
	
	// Select tree area.
	selectTreeArea(
			ActionGroup.slotViewChange
			),
	
	// Select list area.
	selectListArea(
			ActionGroup.slotViewChange
			),
	
	// On show/hide IDs in areas diagram..
	showHideIds(
			ActionGroup.guiChange
			),
	
	// Show read only areas in areas diagram.
	showReadOnlyAreas(
			ActionGroup.guiChange
			),
	
	// Request update of all information.
	requestUpdateAll(
			ActionGroup.areaModelChange,
			ActionGroup.slotModelChange,
			ActionGroup.areaViewStateChange,
			ActionGroup.slotViewStateChange,
			ActionGroup.guiStateChange,
			ActionGroup.areaViewChange,
			ActionGroup.slotViewChange,
			ActionGroup.guiStateChange,
			ActionGroup.guiChange
			),
	
	// Focus on the Basic Area.
	focusBasicArea(
			ActionGroup.guiChange
			),
	
	// Focus on the tab area.
	focusTabArea(
			ActionGroup.guiChange
			),
	
	// Focus on the home area.
	focusHomeArea(
			ActionGroup.areaViewChange,
			ActionGroup.guiChange
			),
	
	// Monitor home page in web browser.
	monitorHomePage(
			ActionGroup.guiChange
			),
	// Update home area.
	updateHomeArea(
			ActionGroup.areaModelChange,
			ActionGroup.areaViewChange
			),
	// Save slot.
	saveSlot(
			ActionGroup.slotModelChange,
			ActionGroup.slotViewChange
			),
	// Indicates updates areas model
	modelUpdated(
			ActionGroup.areaModelChange
			),
	// Reactivate GUI
	reactivateGui(
			ActionGroup.guiStateChange
			),
	// Update of area sub relation.
	updateAreaSubRelation,
	// Update of area super relation.
	updateAreaSuperRelation,
	// Swaps two sibling areas.
	swapSiblingAreas,
	// Reset order of sibling areas to default order.
	resetSiblingAreasOrder,
	// Set area relation names.
	setAreaRelationNames,
	// Update subarea hidden.
	updateHiddenSubArea,
	// Updates visibility of area.
	updateAreaVisibility,
	// Update area read only flag.
	updateAreaReadOnly,
	// Update area localized flag.
	updateAreaLocalized,
	// Update area can import flag.
	updateAreaCanImport,
	// Update area project root flag.
	updateAreaIsProjectRoot,
	// Save area int database.
	saveArea,
	// Upate changes in area.
	updateAreaChanges,
	// Update area is disabled flag.
	updateAreaIsDisabled,
	// Update area inheritance.
	updateAreaInheritance,
	// Import areas.
	importToArea,
	// Update area resources.
	updateAreaResources,
	// Edit resource.
	editResource,
	// Delete resources.
	deleteResources,
	// Create new text resource.
	createTextResource,
	// Change reas properties.
	changeAreasProperties,
	// Delete locaized text of an area.
	deleteAreaLocalizedText,
	// Update start resource for an area.
	updateAreaStartResource,
	// Remove area start resource.
	removeAreaStartResource,
	// Add new area.
	addArea,
	// Hide unnecessary slots.
	hideSlots,
	// Update area versions.
	updateVersions,
	// Servlet causes slots update.
	updatedSlotsWithServlet,
	// Invoked when area constructors where loaded.
	loadAreaConstructors,
	// Invoked when colors where updated.
	updateColors,
	// New enumeration type.
	newEnumeration,
	// Remove enumeration.
	removeEnumeration,
	// Update enumeration.
	updateEnumeration,
	// New enumeration value.
	newEnumerationValue,
	// Update enumeration value.
	updateEnumerationValue,
	// Remove enumeration value.
	removeEnumerationValue,
	// Update area file names.
	updateAreaFileNames,
	// When area tree was created.
	createAreasTree,
	// Update whole model.
	updateAreasModel,
	// On new basic area (database changed).
	newBasicArea,
	// Transfer area with drag and drop.
	transferToArea,
	// Import MIME types.
	importMimeTypes,
	// Load default MIME types.
	defaultMimeTypes,
	// Created new text resource.
	newTextResource,
	// On update of realted area.
	updateRelatedArea,
	// Switch database.
	switchDatabase,
	// On cancel slot editing.
	cancelSlotEditor,
	// Set slots default values.
	setSlotsDefaultValues,
	// When user slots have been removed.
	removeUserSlots,
	// Move slots.
	moveSlots,
	// Remove slots.
	removeSlots,
	// Set slots properties.
	setSlotsProperties,
	// Tooltip timer.
	tooltipTimer,
	// Remove diagram.
	removeDiagram,
	// Update controls.
	updateControls,
	// A flag in a panel with areas tree changed.
	treeFlagChange;
	
	/**
	 * Common event target
	 */
	public static enum Target {
		
		all,
		gui,
		notGui
	}
		
	/**
	 * Included in groups.
	 */
	private HashSet<ActionGroup> includedInGroups = new HashSet<ActionGroup>();
	
	/**
	 * Enable event LOG on STD ERR.
	 */
	private static boolean enableLog = false;
	
	/**
	 * Default similar events time span in milliseconds.
	 */
	private final static long defaultTimeSpanMs = 250;


	
	/**
	 * Event data object.
	 */
	public static class ActionData {
		
		// Event.
		Event event;
		
		// Source of the event.
		Object source;
		
		// Targets of the event.
		Object target;
		
		// Related information.
		Object relatedInfo;
		
		// Auxiliary information.
		Object additionalInfo;
		
		// Event reflection.
		StackTraceElement reflection;
		
		/**
		 * Check is this event is among input event list.
		 * @param data
		 * @param events
		 * @return
		 */
		public boolean foundFor(Event ... events) {
			
			for (Event event : events) {
				if (this.event.equals(event)) {
					return true;
				}
			}
			return false;
		}
		
		// Compute hash code.
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((event == null) ? 0 : event.hashCode());
			result = prime * result + ((reflection == null) ? 0 : reflection.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			return result;
		}
		
		// Compare with input object.
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ActionData other = (ActionData) obj;
			if (event != other.event)
				return false;
			if (reflection == null) {
				if (other.reflection != null)
					return false;
			} else if (!reflection.getFileName().equals(other.reflection.getFileName()))
				return false;
			 else if (reflection.getLineNumber() != other.reflection.getLineNumber())
					return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			return true;
		}
	}
	
	/**
	 * ActionGroup handle.
	 */
	private static class ActionHandle {
		
		// ActionGroup lambda function.
		Consumer<ActionData> action;
		
		// Time span in milliseconds.
		Long timeSpanMs;
		
		// Delay start. Maps event data hash code to delay timeout variable.
		Hashtable<ActionData, Obj<Long>> delayStarts;
		
		// A stack trace. ActionGroup reflection.
		StackTraceElement reflection;
		
		// Indentifier of an action. (For debug purposes, it can be removed.)
		String identifier;
		
		// Constructor.
		ActionHandle(Consumer<ActionData> action, Long timeSpanMs, StackTraceElement reflection, String identifier) {
			
			this.action = action;
			this.timeSpanMs = timeSpanMs;
			this.delayStarts = new Hashtable<ActionData, Obj<Long>>();
			this.reflection = reflection;
			this.identifier = identifier;
		}
		
		// Trin identifier string.
		String identifier() {
			return identifier != null ? identifier : "";
		}
		
		// Get delay start.
		public Obj<Long> getDelayStart(ActionData eventData) {
			
			for (Map.Entry<ActionData, Obj<Long>> entry : delayStarts.entrySet()) {
				
				ActionData eventDataKey = entry.getKey();
				if (eventDataKey.equals(eventData)) {
					return entry.getValue();
				}
			}
			return null;
		}
		
		// Store new delay start.
		public void putDelayStart(ActionData eventData, Obj<Long> delayStart) {
			
			Obj<Long> timeSpanMs = new Obj<Long>();
			delayStarts.put(eventData, timeSpanMs);
		}
		
		// Remove xiststing delay start.
		public void removeDelayStart(ActionData eventData) {
			
			for (Map.Entry<ActionData, Obj<Long>> entry : delayStarts.entrySet()) {
				
				ActionData eventDataKey = entry.getKey();
				if (eventDataKey.equals(eventData)) {
					
					delayStarts.remove(eventDataKey);
					return;
				}
			}
		}
	}
	
	/**
	 * Event queue.
	 */
	public static LinkedList<ActionData> queue = new LinkedList<ActionData>();
	
	/**
	 * Event rules. (With keys as groups and actions related to them.)
	 */
	public static LinkedHashMap<ActionGroup, HashMap<Object, HashSet<ActionHandle>>> rules = new LinkedHashMap<ActionGroup, HashMap<Object, HashSet<ActionHandle>>>();
	
	/**
	 * Main event dispatch thread.
	 */
	private static Thread mainThread;
	
	/**
	 * When this flag is set to true value, the main thread stops dispatching events.
	 */
	private static boolean stopMainThread = false;
	
	/**
	 * Main dispatching thread lock that wait for incomming event.
	 */
	private static Lock dispatchLock = new Lock();
	
	/**
	 * Static constructor which runs main event thread.
	 */
	static {
		
		mainThread = new Thread(() -> {
			
			while (!stopMainThread) {
				
				ActionData actionData = null;
				
				// Dispatch event.
				synchronized (queue) {
					
					if (!queue.isEmpty()) {
						actionData = queue.removeFirst();
					}
				}
				
				if (actionData != null) {
					
					Event event = actionData.event;
					
					// On special events skip the rules.
					if (isSpecial(event)) {
						invokeSpecialAction(actionData);
					}
					else {
						
						// Dispatch actions using rules.
						synchronized (rules) {
							
							for (Map.Entry<ActionGroup, HashMap<Object, HashSet<ActionHandle>>> entry : rules.entrySet()) {
								
								ActionGroup actionGroup = entry.getKey();
								Boolean isInActionGroup = event.isInActionGroup(actionGroup);
								if (isInActionGroup != null && isInActionGroup) {
									
									HashMap<Object, HashSet<ActionHandle>> actionsMap = entry.getValue();
									invokeActions(actionGroup, actionsMap, actionData);
								}
							}
						}
					}
				}
				
				boolean moreEvents;
				synchronized (queue) {
					moreEvents = queue.isEmpty();
				}
				
				// Enter idle state.
				if (!moreEvents) {
					boolean test = Lock.waitFor(dispatchLock, 250);
					if (test) {
						System.out.format("Event dispatcher timeout is %b\n", test);
					}
				}
			}
			
		}, "IDE-Events-Dispatcher");
		
		mainThread.start();
	}
	
	/**
	 * Constructor
	 * @param guichange
	 */
	Event(ActionGroup ... actionGroups) {
		
		for (ActionGroup actionGroup : actionGroups) {
			includedInGroups.add(actionGroup);
		}
	}
	
	/**
	 * Check for a special event.
	 * @param event
	 * @return
	 */
	private static boolean isSpecial(Event event) {
		
		return Event._invokeLater.equals(event);
	}

	/**
	 * Invoke special action.
	 * @param actionData
	 */
	private static void invokeSpecialAction(ActionData actionData) {
		
		// On invoke later a lambda function.
		SwingUtilities.invokeLater(() -> {
			
			try {
				if (Event._invokeLater.equals(actionData.event) && actionData.target instanceof Function) {
					
					// Retrieve lambda function reference and run the lambda function.
					Function<ActionData, Exception> lambdaFunction = (Function<ActionData, Exception>) actionData.target;
					Exception exception = lambdaFunction.apply(actionData);
					
					// Throw possible exception (for future debugging and other purposes).
					if (exception != null) {
						throw exception;
					}
				}
			}
			catch (Exception e) {
				
				// Print stack trace for the special event.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Stop the main thread.
	 */
	public static void stopDispatching() {
		
		// Release objects.
		synchronized (queue) {
			queue.clear();
			rules.clear();
		}
		
		// Stop main thread.
		if (mainThread != null) {
			Lock.notify(dispatchLock);
			stopMainThread = true;
		}
	}
	
	/**
	 * Returns true value if the event is in input group.
	 * @param actionGroup
	 * @return - on error returns null value
	 */
	public Boolean isInActionGroup(ActionGroup actionGroup) {
		
		Boolean included = includedInGroups == null ? null : includedInGroups.contains(actionGroup);
		
		if (included == null) {
			return false;
		}
		
		return included;
	}
	
	/**
	 * Invoke actions.
	 * @param actionGroup 
	 * @param actionsMap
	 * @param data
	 */
	public static void invokeActions(ActionGroup actionGroup, HashMap<Object, HashSet<ActionHandle>> actionsMap, ActionData data) {
		
		if (actionsMap == null) {
			return;
		}
		
		// Run new thread for group of actions (grouped by a key).
		for (Map.Entry<Object, HashSet<ActionHandle>> entry : actionsMap.entrySet()) {
				
			HashSet<ActionHandle> actionHandles = entry.getValue();
			if (actionHandles != null) {
				
				// Invoke actions in AWT thread.
				for (ActionHandle handle : actionHandles) {
					
					SwingUtilities.invokeLater(() -> {
						
						// Coalesce similar events in timespan.
						if (enableLog) {
							System.err.format("Event %s [%s, OID %d]\n\traised at [%s]\n", data.event, data.source.getClass().getSimpleName(), System.identityHashCode(data.source), data.reflection);
							System.err.format("\t-> ActionGroup %s in [%s]\n", actionGroup.name(), handle.reflection);
							System.err.format("Delayed for handle \"%s\" [%d] \n", handle.identifier(), System.identityHashCode(handle));
							System.err.format("-----------------------------------------------------------------\n");
							if ("test".equals(handle.identifier)) {
								System.err.print("");
							}
						}
						if (Event.delay(data, handle)) {
							return;
						}
						handle.action.accept(data);
					});
				}
			}
		}
	}
	
	/**
	 * Propagate event.
	 * @param source - the source is an object that calls propagate(...) method
	 * @param event  - can be Target that specifies common target group or
	 *                 it can be any other object in application
	 * @param info   - the first info object is saved as relatedInfo and additional items
	 *                 are saved in array and attached to additionalInfo field
	 */
	public static void propagate(Object source, Event event, Object ... info) {
		
		// Delegate the call.
		doPropagate(source, Target.all, event, info);
	}
	
	/**
	 * Propagate event.
	 * @param source - the source is an object that calls propagate(...) method
	 * @param target - can be Target that specifies common target group or
	 *                 it can be any other object in application
	 * @param event  - current event
	 * @param info   - the first info object is saved as relatedInfo and additional items
	 *                 are saved in array and attached to additionalInfo field
	 */
	public static void propagate(Object source, Object target, Event event, Object ... info) {
		
		// Delegate the call.
		doPropagate(source, target, event, info);
	}
	
	/**
	 * Propagate event.
	 * @param source
	 * @param event
	 * @param target
	 * @param info
	 */
	private static void doPropagate(Object source, Object target, Event event, Object ... info) {
		
		// Add event to the queue.
		synchronized (queue) {
			
			Event.ActionData data = new Event.ActionData();
			
			data.source = source;
			data.target = target;
			data.event = event;
			
			if (info instanceof Object []) {
				int count = info.length;
				if (count >= 1) {
					data.relatedInfo = info[0];
				}
				if (count >= 2) {
					data.additionalInfo = Arrays.copyOfRange(info, 1, count);
				}
			}
			
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				data.reflection = stackElements[3];
			}
			
			queue.add(data);
			
			Lock.notify(dispatchLock);
		}
	}

	/**
	 * Invoke lambda function later on the event thread
	 * @param labdaFunction
	 */
	public static void invokeLater(Function<ActionData, Exception> labdaFunction) {
		
		// Add event to the queue.
		synchronized (queue) {
			
			// Create special _invokeLater event and put it into the event queue.
			Event.ActionData data = new Event.ActionData();
			
			data.source = Event.class;
			data.target = labdaFunction;
			data.event = Event._invokeLater;
			
			queue.add(data);
			
			Lock.notify(dispatchLock);
		}
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for event group
	 * @param actionGroup
	 * @param action
	 * @return - input key for action group
	 */
	public static Object receiver(Object key, ActionGroup actionGroup, Consumer<Event.ActionData> action) {
		
		final long timeSpanMs = 500;
		
		// Delegate the call.
		return doAdd(key, actionGroup, action, timeSpanMs, null);
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for event group
	 * @param actionGroup
	 * @param action
	 * @param timeSpanMs
	 * @return a key for action group
	 */
	public static Object add(Object key, ActionGroup actionGroup, Consumer<Event.ActionData> action, Long timeSpanMs) {
		
		// Delegate the call.
		return doAdd(key, actionGroup, action, timeSpanMs, null);
	}

	/**
	 * Register new action for an event group.
	 * @param key - a key for event group
	 * @param actionGroup
	 * @param action
	 * @param timeSpanMs
	 * @param identifier
	 * @return - input key for action group
	 */
	public static Object add(Object key, ActionGroup actionGroup, Consumer<ActionData> action, String identifier) {
		
		// Delegate the call.
		return doAdd(key, actionGroup, action, defaultTimeSpanMs, identifier);
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for event group
	 * @param actionGroup
	 * @param action
	 * @param timeSpanMs
	 * @param identifier
	 * @return - input key for action group
	 */
	public static Object add(Object key, ActionGroup actionGroup, Consumer<ActionData> action, Long timeSpanMs, String identifier) {
		
		// Delegate the call.
		return doAdd(key, actionGroup, action, timeSpanMs, identifier);
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for event group
	 * @param actionGroup
	 * @param action
	 * @param timeSpanMs
	 * @param identifier 
	 * @return a key for action group
	 */
	private static Object doAdd(Object key, ActionGroup actionGroup, Consumer<Event.ActionData> action, Long timeSpanMs, String identifier) {
		
		// Try to get existing actions or create a new set.
		synchronized (rules) {
			
			// Get actions
			HashMap<Object, HashSet<ActionHandle>> actionsMap = rules.get(actionGroup);
			if (actionsMap != null) {
				rules.remove(actionGroup);
			}
			else {
				actionsMap = new HashMap<Object, HashSet<ActionHandle>>();
			}
			
			rules.put(actionGroup, actionsMap);
			
			// Sort rules depending on its priority, determined by a position in enumeration.
			rules = Utility.sort(rules, (Object key1, Object key2) -> {
				
				ActionGroup eventGroup1 = (ActionGroup) key1;
				ActionGroup eventGroup2 = (ActionGroup) key2;
				
				int delta = eventGroup1.ordinal() - eventGroup2.ordinal();
				return delta;
			});
			
			// Get actions depending on the input key.
			HashSet<ActionHandle> actions = actionsMap.get(key);
			if (actions == null) {
				actions = new HashSet<ActionHandle>();
				actionsMap.put(key, actions);
			}
			
			// Add new action.
			StackTraceElement reflection = null;
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				reflection = stackElements[3];
			}
			ActionHandle handle = new ActionHandle(action, timeSpanMs, reflection, identifier);
			actions.add(handle);
			return key;
		}
	}
	
	/**
	 * Helper function that can filter event.
	 * @param lambda
	 * @return - true if this event is OK.
	 */
	public static boolean passes(BooleanSupplier lambda) {
		
		boolean result = lambda.getAsBoolean();
		return result;
	}
	
	/**
	 * Unregister actions for given key.
	 * @param key
	 */
	public static void remove(Object key) {
		
		// Remove actions for all groups.
		for (Map.Entry<ActionGroup, HashMap<Object, HashSet<ActionHandle>>> entry : rules.entrySet()) {
			
			HashMap<Object, HashSet<ActionHandle>> actionsMap = entry.getValue();
			
			// Remove actions for given key.
			actionsMap.remove(key);
		}
	}
	
	/**
	 * Delay between two same events. Some events can be skipped.
	 * @param actionHandle
	 * @return - if the event shold be skipped, returns true value
	 */
	public static boolean delay(ActionData eventData, ActionHandle actionHandle) {
		
		// Get time span in milliseconds.
		Long timeSpanMs = actionHandle.timeSpanMs;
		
		// Check input value.
		if (timeSpanMs == null || timeSpanMs < 100 || timeSpanMs > 10000) {
			return true;
		}
		
		boolean skipCurrentEvent;
		
		// Get delay start for the event or create new one.
		Obj<Long> delayStart = actionHandle.getDelayStart(eventData);
		if (delayStart == null) {
			delayStart = new Obj<Long>();
			actionHandle.putDelayStart(eventData, delayStart);
		}
		
		// Try to compute current delay.
		long currentTime = new Date().getTime();
		Long delay = delayStart.ref != null ? currentTime - delayStart.ref : null;
		
		boolean delayInProgress = delay == null ? false : delay < timeSpanMs;
		
		// Set delay.
		if (delayInProgress) {
			if (enableLog) {
				System.err.format("-------------------\n\t---- Event skipped, delay in progress\n-------------------\n");
			}
			skipCurrentEvent = true;
		}
		else {
			if (delayStart.ref == null) {
				delayStart.ref = currentTime;
				if (enableLog) {
					System.err.format("-------------------\n\t|--> Event delay started\n-------------------\n");
				}
				
			}
			else {
				
				actionHandle.removeDelayStart(eventData);
				if (enableLog) {
					System.err.format("-------------------\n\t---| Event delay stopped in %dms\n-------------------\n", delay);
				}
			}
			skipCurrentEvent = false;
		}
		
		return skipCurrentEvent;
	}
	
	/**
	 * Check event source class.
	 * @param data
	 * @param event
	 * @param source
	 * @return
	 */
	public static boolean sourceClass(ActionData data, Event event, Class sourceClass) {
		
		return event.equals(data.event) && sourceClass.equals(data.source.getClass());
	}
	
	/**
	 * Check event source object.
	 * @param data
	 * @param event
	 * @param sourceObject
	 * @return
	 */
	public static boolean sourceObject(ActionData data, Event event, Object sourceObject) {
		
		return event.equals(data.event) && sourceObject.equals(data.source);
	}
	
	/**
	 * Check target class for the event.
	 * @param data
	 * @param areasDiagram
	 * @return
	 */
	public static boolean targetClass(ActionData data, Class targetClass) {
		
		return targetClass.equals(data.target);
	}
	
	/**
	 * Check target object for the event.
	 * @param data
	 * @param areasDiagram
	 * @return
	 */
	public static boolean targetObject(ActionData data, Object targetObject) {
		
		return targetObject.equals(data.target);
	}
}
