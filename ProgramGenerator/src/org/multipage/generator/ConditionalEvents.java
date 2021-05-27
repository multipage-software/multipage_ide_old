/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */
package org.multipage.generator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		public boolean enable = false;
		
		// To display full log information, set this flag to true.
		public boolean full = false;
		
		// Concrete signals.
		public Signal [] concreteSignals = { Signal.updateAll };
	}
	public static LogParameters logParameters = new LogParameters();
	
	/**
	 * Coalesce time span.
	 */
	private static final long defaultCoalesceTimeMs = 500;
	
	/**
	 * Stop receiving unnecessary events. (Only for debugging purposes).
	 */
	private static boolean stopReceivingUnnecessary = true;
	
	/**
	 * Default message coalesce time span in milliseconds.
	 */
	private final static long minDelayMessageCoalesceMs = 25;
	
	/**
	 * Dispatch lock timeout in milliseconds. Must be greater then above coalesce time span.
	 */
	private final static long dispatchLockTimeoutMs = 250;
	
	/**
	 * Receiver priorities.
	 */
	public static final int HIGH_PRIORITY = 100;
	public static final int MIDDLE_PRIORITY = 50;
	public static final int LOW_PRIORITY = 10;
	
	/**
	 * Message object.
	 */
	public static class Message {
		
		// A signal for the message.
		public Signal signal;

		// Source of the message.
		public Object source;
		
		// Target of the message.
		public Object target;
		
		// Related information sent with the message.
		public Object relatedInfo;
		
		// Additional information added to the above related information. 
		public Object [] additionalInfos;
		
		// A flag that is true if related or additional info has to be renewed
		public boolean renewInfo = false;
		
		// Message source reflection.
		public StackTraceElement reflection;
		
		// Receive time.
		public Long receiveTime;
		
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
			
			// Check references.
			if (this == obj)
				return true;
			
			// Check missing value.
			if (obj == null)
				return false;
			
			// Check object types.
			if (getClass() != obj.getClass())
				return false;
			
			// Get message.
			Message other = (Message) obj;
			
			// Check signal.
			if (signal != other.signal)
				return false;
			
			// Check source.
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			
			// Check target.
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			
			// Check relatedInfo.
			if (relatedInfo == null) {
				if (other.relatedInfo != null)
					return false;
			} else if (!relatedInfo.equals(other.relatedInfo))
				return false;
			
			// Check additionalInfos.
			if (additionalInfos == null) {
				if (other.additionalInfos != null)
					return false;
			} else {
				int count = additionalInfos.length;
				int otherCount = other.additionalInfos.length;
				
				if (otherCount != count)
					return false;
				
				// Perform deep check.
				while(--count >= 0)
					if (!additionalInfos[count].equals(other.additionalInfos[count]))
						return false;
			}
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
	private static LinkedHashMap<EventCondition, LinkedHashMap<Integer,
					LinkedHashMap<Object, LinkedList<EventHandle>>>> conditionalEvents = new LinkedHashMap<EventCondition, LinkedHashMap<Integer,
																							LinkedHashMap<Object, LinkedList<EventHandle>>>>();
	
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
	 * Surviving messages needed for coalesce of the same messages in given time span.
	 * ( Expiration time -> Message )
	 */
	private static LinkedHashMap<Long, Message> survivingMessages = new LinkedHashMap<Long, Message>();
	
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
		
		// Enter incoming message dispatch loop.
		while (!stopDispatchMessages) {
			
			// Try to pop single incoming message from the message queue or wait for a new incoming message.
			Boolean newMessage = null;
			do {
				
				// Pop the message and exit the loop.
				incomingMessage.ref = popMessage();
				if (incomingMessage.ref != null) {
					break;
				}
	
				// Wait for a new message.
				newMessage = !Lock.waitFor(dispatchLock, dispatchLockTimeoutMs);
				
				// Pop the new message.
				if (newMessage) {
					incomingMessage.ref = popMessage();
				}
			}
			while (incomingMessage.ref == null);
			
			// Get current time and save it in the incoming message.
			final Long currentTime = Utility.getNow();
			incomingMessage.ref.receiveTime = currentTime;
			
			// Log incoming message.
			LoggingDialog.log(incomingMessage.ref);
			
			// Get message signal.
			Signal signal = incomingMessage.ref.signal;
			
			// Clear expired messages.
			clearExpiredMessages(currentTime);
			
			// Check if some similar message is surviving in the dedicated memory,
			// so that current incoming message have to be skipped.
			// If so, skip the invocations of event actions.
			if (!isMessageSurviving(incomingMessage.ref, currentTime)) {
				
				// Break point managed by log.
				LoggingDialog.breakPoint(signal);
				
				// For special signals perform special invocation.
				if (signal.isSpecial()) {
					invokeSpecialEvents(incomingMessage.ref);
				}
				// For all other matching events invoke theirs action lambdas.
				else {
					
					synchronized (conditionalEvents) {
						
						LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>> priorities = conditionalEvents.get(signal);
						
						if (priorities != null) {
							priorities.forEach((priority, keys) -> {
								if (keys != null) {
									keys.forEach((key, eventHandles) -> {
										
										if (!(signal.isUnnecessary() && stopReceivingUnnecessary)) {  // ... a switch for debugging purposes; this condition disables receiving of unnecessary signals
											
											// Save the message key for debugging purposes.
											incomingMessage.ref.key = key;
											
											// Invoke event actions matching the incoming message.
											// Let survive messages for a time spans defined in event handlers.
											invokeActions(currentTime, eventHandles, priority, key, incomingMessage.ref);
										}
									});
								}
							});
						}
					}
				}
			}
			else {
				j.log("COALESCED SIGNAL %s", signal.name());
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
	 * Invoke events. Pass a reference to the incoming message to input lambda function.
	 * @param eventHandles
	 * @param priority 
	 * @param key - event handler key
	 * @param message
	 */
	public static void invokeActions(long currentTime, LinkedList<EventHandle> eventHandles, Integer priority, Object key, Message message) {
		
		// Check input.
		if (eventHandles == null || message == null) {
			return;
		}
		
		// Go through input event handles and if the message survives, invoke appropriate action on the Swing thread.
		for (EventHandle eventHandle : eventHandles) {
			
			// Compute expiration time.
			long expirationTime = currentTime + eventHandle.coalesceTimeSpanMs;
			
			// Remember the priority and event handler key.
			eventHandle.priority = priority;
			eventHandle.key = key;
			
			// Let the incoming message survive until their time expiration. For coalescing purposes.
			letMessageSurvive(message, expirationTime);
			
			// Break point managed by log.
			LoggingDialog.breakPoint(message.signal);
			
			// Invoke the event on Swing thread and write log.
			SwingUtilities.invokeLater(() -> {
				
				// Invoke action.
				eventHandle.action.accept(message);
				long executionTime = Utility.getNow();
						
				// Log the event.
				LoggingDialog.log(message, eventHandle, executionTime);
				
				j.log("INVOKED %s ACTION", message.signal.name());
			});
		}
	}
	
	/**
	 * Let survive the input message till expiration time.
	 * @param message
	 * @param expirationTime
	 */
	private static void letMessageSurvive(Message message, long expirationTime) {
		
		survivingMessages.put(expirationTime, message);
	}
	
	/**
	 * Check if the message is surviving in dedicated memory.
	 * @param message
	 * @param currentTime
	 * @return
	 */
	private static boolean isMessageSurviving(Message message, Long currentTime) {

		// Try to find message that equals the input message.
		List<Entry<Long, Message>> foundEqualMessages = survivingMessages.entrySet().stream()
			.filter(item -> message.equals(item.getValue()))
			.collect(Collectors.toList());
		
		// Check surviving message.
		boolean messageSurviving = !foundEqualMessages.isEmpty();
		
		// Possibly renew message info.
		if (messageSurviving && message.renewInfo) {
			foundEqualMessages.stream().forEach(entry -> {
				
				Message survivingMessage = entry.getValue();
				
				j.log("RENEW %s -> %s", survivingMessage.relatedInfo.toString(), message.relatedInfo.toString());
				
				survivingMessage.relatedInfo = message.relatedInfo;
				survivingMessage.additionalInfos = message.additionalInfos;
			});
		}
		
		// Return the state.
		return messageSurviving;
	}
	
	/**
	 * Clear expired messages.
	 * @param currentTime
	 */
	private static void clearExpiredMessages(Long currentTime) {
		
		// Initialization.
		HashSet<Long> expirationsToRemove = new HashSet<Long>();
		
		// Find expired time keys.
		survivingMessages.entrySet().stream()
			.filter(item -> item.getKey() < currentTime)
			.forEach(item -> expirationsToRemove.add(item.getKey()));
		
		// Remove expired map items.
		expirationsToRemove.stream().forEach(exprationTime -> survivingMessages.remove(exprationTime));
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
		propagateMessage(source, Target.all, signal, false, info);
	}
	
	/**
	 * Transmit renewed signal.
	 * @param source - the source is mostly an object that calls transmit(...) method
	 * @param signal - can be Target that specifies common target group or
	 *                 it can be any other object in application
	 * @param info   - the first info object is saved as relatedInfo and additional items
	 *                 are saved in array and attached to additionalInfo field
	 */
	public static void transmitRenewed(Object source, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, Target.all, signal, true, info);
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
		propagateMessage(source, target, signal, false, info);
	}
	
	
	/**
	 * Transmit renewed signal.
	 * @param source - the source is mostly the object that calls the transmit(...) method
	 * @param target - the target can be a Target enumeration value that specifies common target group or
	 *                 it can be any other object in application
	 * @param signal - current signal
	 * @param info   - array of additional message informations, the first info object is saved as relatedInfo
	 * 				   and additional array items are saved in the additionalInfo field as an array
	 */
	public static void transmitRenewed(Object source, Object target, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, target, signal, true, info);
	}
	
	/**
	 * Propagate message. This is an internal method.
	 * @param source
	 * @param signal
	 * @param target
	 * @param info
	 */
	private static void propagateMessage(Object source, Object target, Signal signal, boolean renewInfo, Object ... info) {
		
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
			message.renewInfo = renewInfo;
			
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
	public static void invokeLater(Runnable lambdaFunction) {
		
		// Delegate the call.
		invokeLater(message -> {
			
			lambdaFunction.run();
			return null;
		});
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
		return registerConditionalEvent(key, eventCondition, MIDDLE_PRIORITY, messageLambda, defaultCoalesceTimeMs, null);
	}
		
	/**
	 * Register new action for an event group.
	 * @param key
	 * @param eventCondition
	 * @param priority
	 * @param messageLambda
	 */
	public static Object receiver(Object key, EventCondition eventCondition, int priority, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		eventCondition.setPriority(priority);
		return registerConditionalEvent(key, eventCondition, priority, messageLambda, defaultCoalesceTimeMs, null);
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
			outputKeys[index] = registerConditionalEvent(key, eventCondition, MIDDLE_PRIORITY, messageLambda, timeSpanMs, null);
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
		return registerConditionalEvent(key, eventCondition, MIDDLE_PRIORITY, messageLambda, timeSpanMs, null);
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
		return registerConditionalEvent(key, eventCondition, MIDDLE_PRIORITY, messageLambda, minDelayMessageCoalesceMs, identifier);
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
		return registerConditionalEvent(key, eventCondition, MIDDLE_PRIORITY, messageLambda, timeSpanMs, identifier);
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
	private static Object registerConditionalEvent(Object key, EventCondition eventCondition, int priority,
			Consumer<Message> message, Long timeSpanMs, String identifier) {
		
		// Get reflection info.
		Obj<StackTraceElement> reflection = new Obj<StackTraceElement>(null);
		StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
		if (stackElements.length >= 4) {
			reflection.ref = stackElements[3];
		}
		
		// A lambda function that can register conditional event.
		synchronized (conditionalEvents) {
		
			// Create auxiliary table from the map.
			ConditionalEventsAuxTable auxiliaryTable = ConditionalEventsAuxTable.createFrom(conditionalEvents);
			
			// Create new event handle, add new table record.
			EventHandle eventHandle = new EventHandle(message, timeSpanMs, reflection.ref, identifier);
			auxiliaryTable.addRecord(key, eventCondition, priority, eventHandle);
			
			// Retrieve sorted conditional events.
			conditionalEvents = auxiliaryTable.retrieveSorted();
		}
		
		// If the key is a Swing component, use automatic release of the event receiver when the component is removed.
		if (key instanceof JComponent) {
			JComponent component = (JComponent) key;
			
			component.addAncestorListener(new AncestorListener() {
				
				// Register conditional event listener.
				@Override
				public void ancestorAdded(AncestorEvent event) {
					// Nothing to do when the component is added.
				}
				
				// Release all listeners associated with the key.
				@Override
				public void ancestorRemoved(AncestorEvent event) {
					ConditionalEvents.removeReceivers(key);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					// Nothing to do when the component is moved.
				}
			});
		}
		
		// Return key.
		return key;
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
