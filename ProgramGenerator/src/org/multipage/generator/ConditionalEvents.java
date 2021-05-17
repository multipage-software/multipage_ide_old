/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */
package org.multipage.generator;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * 
 * @author vakol
 *
 */
public class ConditionalEvents {
	
	/**
	 * Enumeration of common (not fully specified) message targets.
	 */
	public static enum Target {
		
		all,
		gui,
		notGui
	}
	
	/**
	 * Log parameters.
	 */
	public static class LogParameters {
		
		// If you want to enable message LOG on STD ERR, set this flag to true.
		public boolean enable = true;
		
		// To display full log information, set this flag to true.
		public boolean full = false;
		
		// Concrete signals.
		public Signal [] concereteSignals = { Signal.updateAll };
	}
	public static LogParameters logParameters = new LogParameters();
	
	/**
	 * Coalesce time span.
	 */
	private static final long timeSpanMs = 100;
	
	/**
	 * Stop receiving unnecessary events. (Only for debugging purposes).
	 */
	private static boolean stopReceivingUnnecessary = false;
	
	/**
	 * Default message coalesce time span in milliseconds.
	 */
	private final static long minDelayMessageCoalesceMs = 25;
	
	/**
	 * Dispatch lock timeout in milliseconds. Must be greater then above coalesce time span.
	 */
	private final static long dispatchLockTimeoutMs = 250;
	
	/**
	 * Message object.
	 */
	public static class Message {
		
		// A signal for the message.
		Signal signal;

		// Source of the message.
		Object source;
		
		// Target of the message.
		Object target;
		
		// Related information sent with the message.
		Object relatedInfo;
		
		// Additional information added to the above related information. 
		Object [] additionalInfos;
		
		// Message source reflection.
		StackTraceElement reflection;
		
		// Receive time.
		Long receiveTime;
		
		// Event handler key.
		public Object key;
		
		/**
		 * Dump message.
		 */
		@Override
		public String toString() {
			String timeStamp = receiveTime != null ? Utility.formatTime(receiveTime) : "null";
			return String.format("Message 0x%08x [signal=%s, received=%s]", this.hashCode(), signal.name(), timeStamp);
		}
		
		/**
		 * Compute hash code for this message.
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((signal == null) ? 0 : signal.hashCode());
			result = prime * result + ((reflection == null) ? 0 : reflection.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((receiveTime == null) ? 0 : receiveTime.hashCode());
			return result;
		}
		
		/**
		 * Compare this message with the input message.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Message other = (Message) obj;
			if (signal != other.signal)
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
		
		/**
		 * Returns true if the source class of this message matches the parameter.
		 * @param classObject
		 * @return
		 */
		public boolean sourceClass(Class<?> classObject) {
			
			// Initialize output.
			boolean matches = false;
			
			// Check source class.
			if (source instanceof Class<?>) {
				
				Class<?> sourceClass = (Class<?>) source;
				matches = sourceClass.equals(classObject);
			}
			// Check source object.
			else if (source != null) {
				matches = source.getClass().equals(classObject);
			}
			
			return matches;
		}
		
		/**
		 * Returns true if the source object of this message matches the parameter.
		 * @param object
		 * @return
		 */
		public boolean sourceObject(Object object) {
			
			// Initialize output.
			boolean matches = false;
			
			// Check source object.
			if (source != null) {
				matches = source.equals(object);
			}
			
			return matches;
		}
		
		/**
		 * Returns true if the target class of this message matches the parameter.
		 * @param classObject
		 * @return
		 */
		public boolean targetClass(Class<AreasDiagram> classObject) {

			// Initialize output.
			boolean matches = false;
			
			// Check target class.
			if (target instanceof Class<?>) {
				
				Class<?> targetClass = (Class<?>) target;
				matches = targetClass.equals(classObject);
			}
			
			return matches;
		}
		
		/**
		 * Returns true if the target object of this message matches the parameter.
		 * @param object
		 * @return
		 */
		public boolean targetObject(Object object) {
			
			// Initialize output.
			boolean matches = false;
			
			// Check target object.
			if (target != null) {
				matches = target.equals(object);
			}
			
			return matches;
		}
		
		/**
		 * Returns true if there exists some additional information at given array index and with given type.
		 * @param index
		 * @param classObject
		 * @return
		 */
		public boolean isAdditionalInfo(int index, Class<?> classObject) {
			
			boolean infoExists = index < additionalInfos.length && additionalInfos[index].getClass().equals(classObject);
			return infoExists;
		}
		
		/**
		 * Try to get additional information at given array index of given returned type.
		 * @param <T>
		 * @param index
		 * @return
		 */
		
		public <T> T getAdditionalInfo(int index) {
			
			try {
				T info = (T) additionalInfos[index];
				return info;
			}
			catch (Throwable e) {
				return null;
			}
		}
		
	}
	
	
	
	/**
	 * Message queue.
	 */
	private static LinkedList<Message> messageQueue = new LinkedList<Message>();
	
	/**
	 * All conditional event processors in the application.
	 */
	private static LinkedHashMap<EventCondition, LinkedHashMap<EventConditionPriority,
					LinkedHashMap<Object, LinkedList<EventHandle>>>> conditionalEvents = new LinkedHashMap<EventCondition, LinkedHashMap<EventConditionPriority,
																							LinkedHashMap<Object, LinkedList<EventHandle>>>>();
	
	/**
	 * Scheduled events.
	 */
	private static LinkedList<ScheduledEvent> scheduledEvents = new LinkedList<ScheduledEvent>();
	
	/**
	 * Main message dispatch thread.
	 */
	private static Thread dispatchThread;
	
	/**
	 * When this flag is set to true value, the main dispatch thread stops to dispatch the messages.
	 */
	private static boolean stopDispatchMessages = false;
	
	/**
	 * A lock used in the dispatch thread that is locked when the thread is waiting for incoming messages
	 * and unlocked when the new message arrives.
	 */
	private static Lock dispatchLock = new Lock();
	
	/**
	 * Static constructor which runs the message dispatch thread.
	 */
	static {
		
		// Create and run dispatch thread.
		dispatchThread = new Thread(() -> {
			
			dispatchThread();
			
		}, "IDE-Events-Dispatcher");
		
		dispatchThread.start();
	}
	
	/**
	 * Pop message from the queue.
	 * @return
	 */
	private static Message popMessage() {
		
		synchronized (messageQueue) {
			
			if (!messageQueue.isEmpty()) {
				
				// Pop message.
				Message message = messageQueue.removeFirst();
				return message;
			}
			return null;
		}
	}
	
	/**
	 * The message dispatch thread.
	 */
	private static void dispatchThread() {
		
		Obj<Message> incomingMessage = new Obj<Message>(null);
		
		// Enter message dispatch loop.
		while (!stopDispatchMessages) {
			
			// Try to pop single incoming message or wait.
			Boolean newMessage = null;
			do {
				
				// Pop message.
				incomingMessage.ref = popMessage();
				if (incomingMessage.ref != null) {
					break;
				}
	
				// Wait for a new message.
				newMessage = !Lock.waitFor(dispatchLock, dispatchLockTimeoutMs);
				
				// Pop new message.
				if (newMessage) {
					incomingMessage.ref = popMessage();
				}
				// Invoke old scheduled events.
				else {
					invokeScheduledEvents();
				}
			}
			while (incomingMessage.ref == null);
			
			// Get current time.
			final Long currentTime = new Date().getTime();
			incomingMessage.ref.receiveTime = currentTime;
			
			// Log incoming message.
			LoggingDialog.log(incomingMessage.ref);
				
			// Get signal.
			Signal signal = incomingMessage.ref.signal;
			
			// On special events skip the next complex rules.
			if (signal.isSpecial()) {
				invokeSpecialEvents(incomingMessage.ref);
			}
			// For all matching events...
			else {
				
				synchronized (conditionalEvents) {
					
					LinkedHashMap<EventConditionPriority, LinkedHashMap<Object, LinkedList<EventHandle>>> priorities = conditionalEvents.get(signal);
					
					if (priorities != null) {
						priorities.forEach((priority, keys) -> {
							if (keys != null) {
								keys.forEach((key, eventHandles) -> {
									
									// For debug purposes: stop receiving unnecessary events.
									if (signal.isUnnecessary() && stopReceivingUnnecessary) {
										
										// Do nothing.
									}
									else {
										
										// Save key for debugging purposes.
										incomingMessage.ref.key = key;
										
										// Schedule events.
										scheduleEvents(currentTime, eventHandles, incomingMessage.ref);
									}
								});
							}
						});
					}
				}
			}
			
			if (!scheduledEvents.isEmpty()) {
				
				// Invoke events.
				invokeScheduledEvents();
			}
		}
	}
	
	/**
	 * Invoke special event.
	 * @param message
	 */
	private static void invokeSpecialEvents(Message message) {
		
		// Now only on "invoke later" a lambda function sent along with the input message.
		SwingUtilities.invokeLater(() -> {
			
			try {
				// Check the "invoke later" signal and the message target type that must be some lambda function.
				if (Signal._invokeLater.equals(message.signal) && message.target instanceof Function) {
					
					// Retrieve lambda function reference and run the lambda function.
					Function<Message, Exception> lambdaFunction = (Function<Message, Exception>) message.target;
					Exception exception = lambdaFunction.apply(message);
					
					// Throw possible exception (for future debugging and other purposes).
					if (exception != null) {
						throw exception;
					}
				}
				// Check the "enable target signal" and enable the target signal.
				else if (Signal._enableTargetSignal.equals(message.signal) && message.target instanceof Signal) {
					
					// Retrieve the signal that should be enabled.
					Signal signalToEnable = (Signal) message.target;
					// Enable the signal.
					signalToEnable.enable();
				}
			}
			catch (Exception e) {
				
				// Print stack trace for the special event when an exception has been raised.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Invoke events. Pass the reference to incoming message to the event lambda function.
	 * @param eventHandles
	 * @param message
	 */
	public static void scheduleEvents(long currentTime, LinkedList<EventHandle> eventHandles, Message message) {
		
		// Check input.
		if (eventHandles == null) {
			return;
		}
		
		// Schedule events.
		for (EventHandle eventHandle : eventHandles) {
			
			// Try to get already scheduled and updated event for an event handle and input message.
			ScheduledEvent scheduledEvent = getUpdatedScheduledEvent(eventHandle, message);
			if (scheduledEvent == null) {
				
				// Schedule new event.
				scheduledEvent = new ScheduledEvent(currentTime, message, eventHandle);
				scheduledEvents.add(scheduledEvent);
				
				// Log scheduled event.
				LoggingDialog.log(scheduledEvent, false);
			}
		}
	}

	/**
	 * Returns coalesced scheduled event for a handle signaled by input signal.
	 * @param eventHandle
	 * @param message
	 * @return 
	 */
	private static ScheduledEvent getUpdatedScheduledEvent(EventHandle eventHandle, Message message) {
		
		// If the event handle doesn't coalesce messages, return null.
		if (eventHandle.coalesceTimeSpanMs == null || eventHandle.coalesceTimeSpanMs <= 0L) {
			return null;
		}
		
		// Get events for same a signaled handle.
		LinkedList<ScheduledEvent> similarEvents = new LinkedList<ScheduledEvent>();
		for (ScheduledEvent scheduledEvent : scheduledEvents) {
			
			if (scheduledEvent.eventHandle.equals(eventHandle) && scheduledEvent.message.signal.equals(message.signal)) {
				similarEvents.add(scheduledEvent);
			}
		}
		
		// Check the resulting list.
		if (similarEvents.isEmpty()) {
			return null;
		}
		
		// Get first similar scheduled event and rewrite it with new message.
		ScheduledEvent scheduledEvent = similarEvents.get(0);
		scheduledEvent.message = message;
		scheduledEvent.executionTime = message.receiveTime + eventHandle.coalesceTimeSpanMs;
		
		// Remove subsequent events from the list of scheduled events
		// (not the first event, which has been updated above).
		similarEvents.remove(0);
		scheduledEvents.removeAll(similarEvents);
			
		// Return updated event.
		return scheduledEvent;
	}

	/**
	 * Invoke remaining events.
	 */
	private static void invokeScheduledEvents() {
		
		// Get current time.
		final long currentTime = new Date().getTime();
		
		// Initialize.
		final LinkedList<ScheduledEvent> processedEvents = new LinkedList<ScheduledEvent>();
		
		// Invoke actions on the Swing thread.
		for (ScheduledEvent scheduledEvent : scheduledEvents) {
			
			// Check if the event timeout has elapsed.
			if (currentTime < scheduledEvent.executionTime) {
				break;
			}
			
			// Invoke the scheduled event action.
			SwingUtilities.invokeLater(() -> {
				scheduledEvent.eventHandle.action.accept(scheduledEvent.message);
				
				// Log the event.
				logEvent(scheduledEvent);
				LoggingDialog.log(scheduledEvent, true);
			});
			
			// Remember processed events.
			processedEvents.add(scheduledEvent);
		}
		
		// Clear schedule.
		scheduledEvents.removeAll(processedEvents);
	}
	
	/**
	 * Log event.
	 * @param message
	 * @param eventCondition
	 * @param eventHandle
	 */
	private static void logEvent(ScheduledEvent scheduledEvent) {
		
		if (!logParameters.enable) {
			return;
		}
		
		Message message = scheduledEvent.message;
		
		if (Signal.displayOrRedrawToolTip.equals(message.signal)
				|| Signal.removeToolTip.equals(message.signal)) {
			return;
		}
		
		Signal signal = message.signal;
		
		int concereteSignalsCount = logParameters.concereteSignals.length;
		boolean logThisSignal = concereteSignalsCount == 0;
		if (concereteSignalsCount > 0) {
			
			for (Signal concreteSignal : logParameters.concereteSignals) {
				if (signal.equals(concreteSignal)) {
					
					logThisSignal = true;
					break;
				}
			}
		}
		
		if (!logThisSignal) {
			return;
		}
		
		EventHandle eventHandle = scheduledEvent.eventHandle;
		
		// Full log information.
		if (logParameters.full) {
			
			String receivedTimeString = Utility.formatTime(message.receiveTime);
			String scheduledTimeString = Utility.formatTime(scheduledEvent.executionTime);
			
			j.log("-----------------------------------------------------------------");
			j.log("Event: %s [Source: %s, OID %d]\t\ttransmitted with %s in %s", message.signal, message.source.getClass().getSimpleName(), System.identityHashCode(message.source), receivedTimeString, message.reflection);
			j.log("\t-> Action was scheduled for %s and processed in %s", scheduledTimeString, eventHandle.reflection);
		}
		// Simplified log information.
		else {
			j.log("-----------------------------------------------------------------");
			//j.log("%s\t\t\t%s\t%s", message.signal, message.reflection, eventHandle.reflection);
			j.log("%s[0x%08x]\t\t\t%s", message.signal, message.key.hashCode(), eventHandle.reflection);
		}
	}
	
	/**
	 * Stop the main thread.
	 */
	public static void stopDispatching() {
		
		// Release objects.
		synchronized (messageQueue) {
			messageQueue.clear();
			
			synchronized (conditionalEvents) {
				conditionalEvents.clear();
			}
		}
		
		// Stop main thread.
		if (dispatchThread != null) {
			Lock.notify(dispatchLock);
			stopDispatchMessages = true;
		}
	}

	/**
	 * Transmit signal.
	 * @param source - the source is mostly an object that calls transmit(...) method
	 * @param signal - can be Target that specifies common target group or
	 *                 it can be any other object in application
	 * @param info   - the first info object is saved as relatedInfo and additional items
	 *                 are saved in array and attached to additionalInfo field
	 */
	public static void transmit(Object source, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, Target.all, signal, info);
	}
	
	/**
	 * Transmit signal.
	 * @param source - the source is mostly the object that calls the transmit(...) method
	 * @param target - the target can be a Target enumeration value that specifies common target group or
	 *                 it can be any other object in application
	 * @param signal - current signal
	 * @param info   - array of additional message informations, the first info object is saved as relatedInfo
	 * 				   and additional array items are saved in the additionalInfo field as an array
	 */
	public static void transmit(Object source, Object target, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, target, signal, info);
	}
	
	/**
	 * Propagate message. This is an internal method.
	 * @param source
	 * @param signal
	 * @param target
	 * @param info
	 */
	private static void propagateMessage(Object source, Object target, Signal signal, Object ... info) {
		
		// Check if the signal is enabled.
		if (!signal.isEnabled()) {
			return;
		}
		
		// Filter unnecessary signals (only for debugging purposes)
		if (stopReceivingUnnecessary && signal.isUnnecessary()) {
			return;
		}
		
		// Add new message to the message queue and unlock the message dispatch thread.
		synchronized (messageQueue) {
			
			Message message = new Message();
			
			message.source = source;
			message.target = target;
			message.signal = signal;
			
			if (info instanceof Object []) {
				int count = info.length;
				if (count >= 1) {
					message.relatedInfo = info[0];
				}
				if (count >= 2) {
					message.additionalInfos = Arrays.copyOfRange(info, 1, count);
				}
			}
			
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				message.reflection = stackElements[3];
			}
			
			messageQueue.add(message);
			
			Lock.notify(dispatchLock);
		}
	}

	/**
	 * Invoke lambda function later on the message dispatch thread.
	 * @param lambdaFunction
	 */
	public static void invokeLater(Function<Message, Exception> lambdaFunction) {
		
		// Create special message with _invokeLater signal and put it into the message queue.
		// Then unlock the message dispatch thread.
		synchronized (messageQueue) {
			
			
			ConditionalEvents.Message message = new ConditionalEvents.Message();
			
			message.source = ConditionalEvents.class;
			message.target = lambdaFunction;
			message.signal = Signal._invokeLater;
			
			messageQueue.add(message);
			
			Lock.notify(dispatchLock);
		}
	}
	
	/**
	 * Disable signal.
	 * @param signal
	 */
	public static void disableSignal(Signal signal) {
		
		signal.disable();
	}
	
	/**
	 * Enable "enable signal" message.
	 * @param signalToEnable
	 */
	public static void enableSignal(Signal signalToEnable) {
		
		// Create special message with _enableSelectedSignal and put it into the message queue.
		// Then unlock the message dispatch thread.
		synchronized (messageQueue) {
			
			ConditionalEvents.Message message = new ConditionalEvents.Message();
			
			message.source = ConditionalEvents.class;
			message.target = signalToEnable;
			message.signal = Signal._enableTargetSignal;
			
			messageQueue.add(message);
			
			Lock.notify(dispatchLock);
		}
	}

	/**
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventCondition - either Signal or SignalType
	 * @param messageLambda
	 * @return - input key for action group
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, timeSpanMs, null);
	}
		
	/**
	 * Register new action for an event group.
	 * @param key
	 * @param eventCondition
	 * @param priority
	 * @param messageLambda
	 */
	public static Object receiver(Object key, EventCondition eventCondition, EventConditionPriority priority, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		eventCondition.setPriority(priority);
		return registerConditionalEvent(key, eventCondition, priority, messageLambda, timeSpanMs, null);
	}
	
	/**
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventConditions
	 * @param messageLambda
	 * @return - keys for action condition
	 */
	public static Object [] receiver(Object key, EventCondition [] eventConditions, Consumer<Message> messageLambda) {
		
		final long timeSpanMs = 500;
		
		// Initialization.
		int count = eventConditions.length;
		Object [] outputKeys = new Object[count];
		
		// Add action rules.
		for (int index = 0; index < count; index++) {
			
			EventCondition eventCondition = eventConditions[index];
			outputKeys[index] = registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, timeSpanMs, null);
		}
		
		return outputKeys;
	}
	
	/**
	 * 
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param messageLambda
	 * @param timeSpanMs
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> messageLambda, Long timeSpanMs) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, timeSpanMs, null);
	}

	/**
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param messageLambda
	 * @param coalesceTimeSpanMs
	 * @param identifier
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> messageLambda, String identifier) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, minDelayMessageCoalesceMs, identifier);
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param messageLambda
	 * @param timeSpanMs
	 * @param identifier
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> messageLambda, Long timeSpanMs, String identifier) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, timeSpanMs, identifier);
	}
	
	/**
	 * Register new conditional event for given condition.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param priority 
	 * @param message
	 * @param timeSpanMs
	 * @param identifier 
	 * @return a key for action group
	 */
	private static Object registerConditionalEvent(Object key, EventCondition eventCondition, EventConditionPriority priority,
			Consumer<Message> message, Long timeSpanMs, String identifier) {
		
		// Get reflection info.
		Obj<StackTraceElement> reflection = new Obj<StackTraceElement>(null);
		StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
		if (stackElements.length >= 4) {
			reflection.ref = stackElements[3];
		}
		
		// Lambda function that can register the conditional event.
		Supplier<Object> registerLambda = () -> {
			synchronized (conditionalEvents) {
			
				// Create auxiliary table from the map.
				ConditionalEventsAuxTable auxiliaryTable = ConditionalEventsAuxTable.createFrom(conditionalEvents);
				
				// Create new event handle, add new table record.
				EventHandle handle = new EventHandle(message, timeSpanMs, reflection.ref, identifier);
				auxiliaryTable.addRecord(key, eventCondition, priority, handle);
				
				// Retrieve sorted conditional events.
				conditionalEvents = auxiliaryTable.retrieveSorted();
				
				// Return key.
				return key;
			}
		};
		
		// If the key is a Swing component, use automatic release.
		if (key instanceof JComponent) {
			JComponent component = (JComponent) key;
			
			component.addAncestorListener(new AncestorListener() {
				
				// Register conditional event listener.
				@Override
				public void ancestorAdded(AncestorEvent event) {
					registerLambda.get();
				}
				
				// Release all the listeners.
				@Override
				public void ancestorRemoved(AncestorEvent event) {
					ConditionalEvents.removeReceivers(key);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					// Nothing to do when the component is moved.
				}
			});
			return key;
		}
		else {
			return registerLambda.get();
		}
	}
	
	/**
	 * Unregister receivers for conditional events for given key object.
	 * @param key
	 */
	public static void removeReceivers(Object key) {
		
		synchronized (conditionalEvents) {
			
			// Remove conditional events for key.
			conditionalEvents.remove(key);
		}
	}
}
