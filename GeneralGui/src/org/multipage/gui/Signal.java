/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 19-02-2021
 *
 */
package org.multipage.gui;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Signal definitions.
 * @author vakol
 *
 */
public class Signal implements ApplicationEvent {
	
	// Special signal that runs user lambda function placed in a message on the message thread.
	public static Signal _invokeLater = new Signal();
	
	// Enables target signal.
	public static Signal _enableTargetSignal = new Signal();
	
	// TODO: <---MAKE finish the definition of the "terminate" signal
	public static Signal terminate = new Signal();
	
	// Enable or disable debugging.
	public static Signal debugging = new Signal();
	
	public static Signal stepLog = new Signal();
	
	public static Signal runLogging = new Signal();
	
	public static Signal breakLogging = new Signal();
	
	// Switch database.
	public static Signal switchDatabase = new Signal();
	
	/**
	 * Helper function for assignment of signal types.
	 * @param signalTypes
	 * @return
	 */
	protected static SignalGroup [] groups(SignalGroup ... signalTypes) {
		
		return signalTypes;
	}
	
	/**
	 * Helper function for assignment of info types.
	 * @param infoTypes
	 * @return
	 */
	protected static Class [] params(Class ... infoTypes) {
		
		return infoTypes;
	}
	
	/**
	 * Unnecessary signals.
	 */
	protected static final LinkedList<Signal> unnecessarySignals = new LinkedList<Signal>();
	
	/**
	 * Descriptive name of the signal.
	 */
	public String name = "unknown";
	
	/**
	 * Ordinal number of the signal.
	 */
	public int ordinal = -1;
	
	/**
	 * List of all signals.
	 */
	public static LinkedList<Signal> allSignals = new LinkedList<Signal>();

	/**
	 * Signal is included in the following signal types.
	 */
	private HashSet<SignalGroup> includedInGroups = new HashSet<SignalGroup>();
	
	/**
	 * Enable or disable this signal.
	 */
	private boolean enabled = true;
	
	/**
	 * Types of additional information attached to messages of this signal.
	 */
	public Class [] infoTypes = null;
	
	/**
	 * Next ordinal number of the signal. Incremented in loadDescriptiveNames(...).
	 */
	private static int nextOrdinal = 1;
	
	/**
	 * Static constructor.
	 */
	static {
		// Unnecessary signals in static constructor.
		addUnnecessary(/* Add them as parameters. */);
		
		// Describe signals.
		reflectSignals(Signal.class);
	}
	
	/**
	 * Get array of checked infos.
	 * @param infos
	 * @param infoTypes
	 * @return
	 */
	public Object [] getCheckedInfos(Object [] infos) {
		
		// If they are no types return input array.
		if (infoTypes == null) {
			return infos;
		}
		
		final int infoCount = infos.length;
		
		// Create checked info parameter array.
		Object [] checkedInfos = new Object[infoCount];
		
		// Check info type and reset those that do not match.
		int typeCount = infoTypes.length;
		for (int index = 0; (index < infoCount) && (index < typeCount); index++) {
			
			// Initialization.
			Object infoOject = infos[index];
			checkedInfos[index] = infoOject;
			
			// If the info object is null do not check its type.
			if (infoOject == null) {
				continue;
			}

			// Get the "must have class" for the info object.
			Class<?> mustHaveType = infoTypes[index];
			 
			// If type is provided and it doesn't match reset the info value.
			Class<?> infoType = infoOject.getClass();
			if (!infoType.equals(mustHaveType)) {
				checkedInfos[index] = null;
			}
		}
		return checkedInfos;
	}
	
	/**
	 * Describe signals in the input class using reflection
	 * @param theClass
	 */
	public static void reflectSignals(Class<? extends Signal> theClass) {
		
		Field [] fields = theClass.getFields();
		for (Field field : fields) {
			
			String fieldName = field.getName();
			try {
				Object fieldValue = field.get(fieldName);
				if (fieldValue instanceof Signal) {
					
					Signal signal = (Signal) fieldValue;
					
					// Set signal name and ordinal number.
					signal.name = fieldName;
					signal.ordinal= nextOrdinal;
					
					allSignals.add(signal);
					
					nextOrdinal++;
				}
			}
			catch (Exception e) {
			}
		}
	}
	
	/**
	 * Constructor of signal.
	 */
	public Signal() {
		
		// Delegate method call to constructor with implementation.
		this(null, null);
	}
	
	/**
	 * Constructor of signal.
	 * @param signalTypes
	 */
	public Signal(SignalGroup [] signalTypes) {
		
		// Delegate method call to constructor with implementation.
		this(signalTypes, null);		
	}
	
	/**
	 * Constructor of signal.
	 * @param infoTypes
	 */
	public Signal(Class [] infoTypes) {
		
		// Delegate method call to constructor with implementation.
		this(null, infoTypes);
	}
	
	/**
	 * Constructor of signal.
	 * @param signalTypes
	 * @param infoTypes
	 */
	public Signal(SignalGroup [] signalTypes, Class [] infoTypes) {
		
		if (signalTypes != null) {
			for (SignalGroup signalType : signalTypes) {
				includedInGroups.add(signalType);
				signalType.addSignal(this);
			}
		}
		
		this.infoTypes = infoTypes;
	}
	
	/**
	 * Enable this signal.
	 */
	public synchronized void enable() {
		
		this.enabled = true;
	}
	
	/**
	 * Disable this signal.
	 */
	public synchronized void disable() {
		
		this.enabled = false;
	}
	
	/**
	 * Check if this signal is enabled.
	 * @return
	 */
	public boolean isEnabled() {
		
		return this.enabled;
	}
	
	/**
	 * Check for a special signals.
	 * @param event
	 * @return
	 */
	public boolean isSpecial() {
		
		return _invokeLater.equals(this) || _enableTargetSignal.equals(this);
	}
	
	/**
	 * Returns true if current signal is in input group.
	 * @param signalGroup
	 * @return
	 */
	public boolean isInGroup(SignalGroup signalGroup) {
		
		// Check the input value.
		if (signalGroup == null) {
			return false;
		}
		
		// Try to find the group.
		boolean isIncluded = includedInGroups.contains(signalGroup);
		return isIncluded;
	}
	
	/**
	 * For debugging purposes it returns true if the signal is unnecessary.
	 * @return
	 */
	public boolean isUnnecessary() {
		
		for (Signal unnecessarySignal : unnecessarySignals) {
			
			if (this.equals(unnecessarySignal)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add unnecessary signals.
	 * @param signals
	 */
	public static void addUnnecessary(Signal ... signals) {
		
		for (Signal signal : signals) {
			unnecessarySignals.add(signal);
		}
	}
	
	/**
	 * A helper function that forms array of event conditions.
	 * The method can be used this way: EventCondition.array(A, B, ...)
	 * @param eventSignals
	 * @return
	 */
	public static ApplicationEvent [] array(ApplicationEvent ... eventSignals) {
		
		return eventSignals;
	}

	/**
	 * Returns true if the incoming message matches this signal.
	 */
	@Override
	public boolean matches(Message incomingMessage) {
		
		// Check if incoming message signal matches.
		Signal signal = incomingMessage.signal;
		boolean matches = this.equals(signal);
		return matches;
	}
	
	/**
	 * Get types.
	 */
	public HashSet<SignalGroup> getTypes() {
		
		return includedInGroups;
	}
	
	/**
	 * Return all defined signals.
	 * @return
	 */
	public static LinkedList<Signal> definedSignals() {
		
		return allSignals;
	}
	
	/**
	 * Get field name.
	 */
	@Override
	public String name() {
		
		return name;
	}
	
	/**
	 * Get ordinal number.
	 */
	@Override
	public int ordinal() {
		
		return ordinal;
	}

	/**
	 * Check if an input object equals this signal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Signal) {
			
			Signal signal = (Signal) obj;
			boolean isSame = this.name.equals(signal.name);
			
			return isSame;
		}
		return false;
	}
	
	/**
	 * Return signal name.
	 */
	@Override
	public String toString() {
		return name;
	}
}