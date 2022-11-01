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
public class Signal implements EventCondition {
	
	// Special signal that runs user lambda function placed in a message on the message thread.
	public static Signal _invokeLater = new Signal();
	
	// Enables target signal.
	public static Signal _enableTargetSignal = new Signal();
	
	// Update areas' model for the GUI.
	public static Signal updateAreasModel = new Signal(
			SignalType.guiChangeByUser
			);

	// Update areas' properties.
	public static Signal updateAreasProperties = new Signal();
	
	// TODO: <---MAKE finish the definition of the "terminate" signal
	public static Signal terminate = new Signal();
	
	// Enable or disable debugging.
	public static Signal debugging = new Signal(
			SignalType.serverStateChange
			);
	
	public static Signal stepLog = new Signal(
			SignalType.guiStateChange,
			SignalType.guiChange
			);
	
	public static Signal runLogging = new Signal(
			SignalType.guiStateChange,
			SignalType.guiChange
			);
	
	public static Signal breakLogging = new Signal(
			SignalType.guiStateChange,
			SignalType.guiChange
			);
	
	// Switch database.
	public static Signal switchDatabase = new Signal();
	
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
	private HashSet<SignalType> includedInTypes = new HashSet<SignalType>();
	
	/**
	 * Enable or disable this signal.
	 */
	private boolean enabled = true;
	
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
		describeSignals(Signal.class);
	}
	
	/**
	 * Describe signals in input class.
	 * @param theClass
	 */
	public static void describeSignals(Class<? extends Signal> theClass) {
		
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
	 * Constructor of the a signal.
	 * @param signalTypes
	 */
	public Signal(SignalType ... signalTypes) {
		
		// Set list of types.
		for (SignalType signalType : signalTypes) {
			includedInTypes.add(signalType);
		}
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
	 * Returns true if current signal is that of the given input type.
	 * @param signalType
	 * @return
	 */
	public boolean isOfType(SignalType signalType) {
		
		// Check the input value.
		if (signalType == null) {
			return false;
		}
		
		// Try to find the type.
		boolean isIncluded = includedInTypes.contains(signalType);
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
	public static EventCondition [] array(EventCondition ... eventSignals) {
		
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
	public HashSet<SignalType> getTypes() {
		
		return includedInTypes;
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
}