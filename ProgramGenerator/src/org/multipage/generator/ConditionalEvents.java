/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */
package org.multipage.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.multipage.util.Lock;
import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * 
 * @author user
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
	 * If you want to enable message LOG on STD ERR, set this flag to true.
	 */
	private static boolean enableMessageLog = false;
	
	/**
	 * Default message coalesce time span in milliseconds.
	 */
	private final static long defaultMessageCoalesceMs = 250;
	
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
		
		/**
		 * Check if the current message is triggered by some of input messages.
		 * @param messages
		 * @return
		 */
		// TODO
		public boolean triggeredBy(ConditionalEvents ... messages) {
			
			for (ConditionalEvents message : messages) {
				if (this.signal.equals(message)) {
					return true;
				}
			}
			return false;
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
	 * Event handle.
	 */
	private static class EventHandle {
		
		/**
		 * An action is a lambda function. It consumes the message if it is not coalesced in a time span.
		 */
		Consumer<Message> action;
		
		/**
		 * Time span in milliseconds for coalescing the same messages. They do not trigger the action.
		 */
		Long timeSpanMs;
		
		/**
		 * Table of messages' receive moments.
		 */
		Hashtable<Object, Obj<Long>> messagesReceiveMoments;
		
		/**
		 * Reflection of the signal receiver.
		 */
		StackTraceElement reflection;
		
		/**
		 *  Identifier of this event handle (only for debugging purposes; this property can be removed from
		 *  the class code along with the debugging code).
		 */
		String identifier;
		
		/**
		 * Constructor.
		 * @param action
		 * @param timeSpanMs
		 * @param reflection
		 * @param identifier
		 */
		EventHandle(Consumer<Message> action, Long timeSpanMs, StackTraceElement reflection, String identifier) {
			
			this.action = action;
			this.timeSpanMs = timeSpanMs;
			this.messagesReceiveMoments = new Hashtable<Object, Obj<Long>>();
			this.reflection = reflection;
			this.identifier = identifier;
		}
		
		/**
		 * Trim the identifier string.
		 * @return
		 */
		String identifier() {
			return identifier != null ? identifier : "";
		}
		
		/**
		 * Get stored receive moment for the input message.
		 * @param message
		 * @return
		 */
		public Long getStoredReceiveMoment(Message message) {
			
			// Get signal associated with the message and retrieve appropriate receive moment.
			Signal signal = message.signal;
			Obj<Long> receiveMoment = messagesReceiveMoments.get(signal);
			
			// Return value.
			if (receiveMoment == null) {
				return null;
			}
			
			return receiveMoment.ref;
		}
		
		/**
		 * Store new receive moment for the input message.
		 * @param message
		 * @param receiveMoment 
		 */
		public void storeNewReceiveMoment(Message message, long receiveMoment) {
			
			// Get message signal and wrap receive moment with an object.
			Signal signal = message.signal;
			Obj<Long> receiveMomentObject = new Obj<Long>(receiveMoment);
			
			// Save the receive moment.
			messagesReceiveMoments.put(signal, receiveMomentObject);
		}
		
		/**
		 * Remove stored receive moment for the input message.
		 * @param message
		 */
		public void removeStoredReceiveMoment(Message message) {
			
			// Get message signal.
			Signal signal = message.signal;
			
			// Remove assigned message receive moment.
			messagesReceiveMoments.remove(signal);
		}
	}
	
	/**
	 * Message queue.
	 */
	public static LinkedList<Message> messageQueue = new LinkedList<Message>();
	
	/**
	 * All conditional event processors in the application.
	 */
	public static LinkedHashMap<Object, LinkedHashMap<EventCondition, HashSet<EventHandle>>> conditionalEvents = new LinkedHashMap<Object, LinkedHashMap<EventCondition, HashSet<EventHandle>>>();
	
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
	 * The message dispatch thread.
	 */
	private static void dispatchThread() {
		
		// Enter message dispatch loop.
		while (!stopDispatchMessages) {
			
			// Try to pull single incoming message.
			Message incomingMessage = null;
			synchronized (messageQueue) {
				
				if (!messageQueue.isEmpty()) {
					incomingMessage = messageQueue.removeFirst();
				}
			}
			
			// Process the message if it exists.
			if (incomingMessage != null) {
				
				Signal signal = incomingMessage.signal;
				
				// On special events skip the next complex rules.
				if (signal.isSpecial()) {
					invokeSpecialEvents(incomingMessage);
				}
				else {
					
					// Dispatch message to conditional events processors.
					synchronized (conditionalEvents) {
						
						for (LinkedHashMap<EventCondition, HashSet<EventHandle>> conditionalEventsForKey : conditionalEvents.values()) {
							for (Map.Entry<EventCondition, HashSet<EventHandle>> mapEntry : conditionalEventsForKey.entrySet()) {
								
								// Check if event condition matches.
								EventCondition eventCondition = mapEntry.getKey();
								HashSet<EventHandle> eventHandles = mapEntry.getValue();
								boolean conditionMatches = eventCondition.matches(incomingMessage);
								if (conditionMatches) {
									
									// If so invoke appropriate event.
									invokeEvents(eventHandles, eventCondition, incomingMessage);
								}
							}
						}
					}
				}
			}
			
			// Check if there are more events in the queue.
			boolean moreEvents;
			synchronized (messageQueue) {
				moreEvents = messageQueue.isEmpty();
			}
			
			// If not, enter the idle state for 250 ms.
			if (!moreEvents) {
				Lock.waitFor(dispatchLock, 250);
				
				// Update lock.
				dispatchLock = new Lock();
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
			}
			catch (Exception e) {
				
				// Print stack trace for the special event when an exception has been raised.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Stop the main thread.
	 */
	public static void stopDispatching() {
		
		// Release objects.
		synchronized (messageQueue) {
			messageQueue.clear();
			conditionalEvents.clear();
		}
		
		// Stop main thread.
		if (dispatchThread != null) {
			Lock.notify(dispatchLock);
			stopDispatchMessages = true;
		}
	}
	
	/**
	 * Invoke events. Pass the reference to incoming message to the event lambda function.
	 * @param eventHandles
	 * @param eventCondition
	 * @param message
	 */
	public static void invokeEvents(HashSet<EventHandle> eventHandles, EventCondition eventCondition, Message message) {
		
		// Check input.
		if (eventHandles == null) {
			return;
		}
		
		// Invoke actions on the Swing thread.
		for (EventHandle eventHandle : eventHandles) {
			
			SwingUtilities.invokeLater(() -> {
				
				// Log event.
				if (enableMessageLog) {
					j.log("-----------------------------------------------------------------");
					j.log("Event: %s [Source: %s, OID %d]\t\traised    in %s", message.signal, message.source.getClass().getSimpleName(), System.identityHashCode(message.source), message.reflection);
					j.log("\t-> Action rule: matches %s %s\t\t\tprocessed in %s", eventCondition.getClass().getSimpleName(), eventCondition.name(), eventHandle.reflection);
					j.log("\tDelay: handle \"%s\" [%d]", eventHandle.identifier(), System.identityHashCode(eventHandle));
				}
				
				// Coalesce same events within given time span.
				if (ConditionalEvents.coalesceMessage(eventHandle, message)) {
					return;
				}
				
				// Invoke the event action.
				eventHandle.action.accept(message);
			});
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
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventCondition - either Signal or SignalType
	 * @param action
	 * @return - input key for action group
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> action) {
		
		final long timeSpanMs = 500;
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, action, timeSpanMs, null);
	}
	
	/**
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventConditions
	 * @param action
	 * @return - keys for action condition
	 */
	public static Object [] receiver(Object key, EventCondition [] eventConditions, Consumer<Message> action) {
		
		final long timeSpanMs = 500;
		
		// Initialization.
		int count = eventConditions.length;
		Object [] outputKeys = new Object[count];
		
		// Add action rules.
		for (int index = 0; index < count; index++) {
			
			EventCondition actionCondition = eventConditions[index];
			outputKeys[index] = registerConditionalEvent(key, actionCondition, action, timeSpanMs, null);
		}
		
		return outputKeys;
	}
	
	/**
	 * 
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param action
	 * @param timeSpanMs
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> action, Long timeSpanMs) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, action, timeSpanMs, null);
	}

	/**
	 * Register new conditional event, the receiver of messages.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param action
	 * @param timeSpanMs
	 * @param identifier
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> action, String identifier) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, action, defaultMessageCoalesceMs, identifier);
	}
	
	/**
	 * Register new action for an event group.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param action
	 * @param timeSpanMs
	 * @param identifier
	 * @return - a key for action condition
	 */
	public static Object receiver(Object key, EventCondition eventCondition, Consumer<Message> action, Long timeSpanMs, String identifier) {
		
		// Delegate the call.
		return registerConditionalEvent(key, eventCondition, action, timeSpanMs, identifier);
	}
	
	/**
	 * Register new conditional event for given condition.
	 * @param key - a key for conditional event
	 * @param eventCondition
	 * @param message
	 * @param timeSpanMs
	 * @param identifier 
	 * @return a key for action group
	 */
	private static Object registerConditionalEvent(Object key, EventCondition eventCondition, Consumer<Message> message, Long timeSpanMs, String identifier) {
		
		synchronized (conditionalEvents) {
			
			// Get event handles.
			LinkedHashMap<EventCondition, HashSet<EventHandle>> conditionalEventsForKey = conditionalEvents.get(key);
			if (conditionalEventsForKey != null) {
				conditionalEvents.remove(eventCondition);
			}
			else {
				conditionalEventsForKey = new LinkedHashMap<EventCondition, HashSet<EventHandle>>();
			}
			
			// Set the conditional event.
			conditionalEvents.put(key, conditionalEventsForKey);
			
			// Get conditional events depending on the event condition.
			HashSet<EventHandle> eventHandles = conditionalEventsForKey.get(eventCondition);
			if (eventHandles == null) {
				
				// Create new handles if they do not exist.
				eventHandles = new HashSet<EventHandle>();
				conditionalEventsForKey.put(eventCondition, eventHandles);
				
				// Sort conditional event depending on the priority of events' conditions Priorities are determined by ordinal().
				conditionalEventsForKey = sort(conditionalEventsForKey, (EventCondition eventCondition1, EventCondition eventCondition2) -> {
					
					// Compare event conditions' priorities.
					int delta = eventCondition1.ordinal() - eventCondition2.ordinal();
					return delta;
				});
			}
			
			// Add new conditional event into the list depending on the priority of condition.
			StackTraceElement reflection = null;
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				reflection = stackElements[3];
			}
			EventHandle eventHandle = new EventHandle(message, timeSpanMs, reflection, identifier);
			eventHandles.add(eventHandle);
			
			return key;
		}
	}
	
	/**
	 * Sort collection.
	 * @param collection
	 * @param comparator
	 */
	private static LinkedHashMap sort(LinkedHashMap<EventCondition, HashSet<EventHandle>> collection,
			BiFunction<EventCondition, EventCondition, Integer> comparator) {
		
		List<Map.Entry<EventCondition, HashSet<EventHandle>>> entries = new ArrayList(collection.entrySet());
		
		Collections.sort(entries, new Comparator<Map.Entry<EventCondition, HashSet<EventHandle>>>() {
		    @Override
		    public int compare(Map.Entry<EventCondition, HashSet<EventHandle>> left, Map.Entry<EventCondition, HashSet<EventHandle>> right) {
		        return left.getKey().ordinal() - right.getKey().ordinal();
		    }
		});
		
		LinkedHashMap resultCollection = new LinkedHashMap();
		for (Map.Entry entry : entries) {
			resultCollection.put(entry.getKey(), entry.getValue());
		}
		
		return resultCollection;
	}
	
	
	/**
	 * Unregister receivers for conditional events for given key object.
	 * @param key
	 */
	public static void removeReceivers(Object key) {
		
		// Remove conditional events for key.
		conditionalEvents.remove(key);
	}
	
	/**
	 * Coalesce multiple incoming messages with the same signal. The time span is defined by the event handle.
	 * Some events thus can be skipped with no action.
	 * @param eventHandle
	 * @param message
	 * @return - if the message has to be skipped, returns true value
	 */
	public static boolean coalesceMessage(EventHandle eventHandle, Message message) {
		
		// Get coalescing time span in milliseconds.
		Long timeSpanMs = eventHandle.timeSpanMs;
		
		// Check and trim the value of time span.
		if (timeSpanMs == null || timeSpanMs < 100 || timeSpanMs > 10000) {
			return true;
		}
		
		// Get current receive moment.
		long currentTime = new Date().getTime();
		
		// Get stared receive moment for the input message or store new one for it in the event handle.
		Long messageReceiveMoment = eventHandle.getStoredReceiveMoment(message);
		if (messageReceiveMoment == null) {
			eventHandle.storeNewReceiveMoment(message, currentTime);
		}
		
		// Try to compute current delay between this message and first received message both associated with the same signal.
		Long delay = messageReceiveMoment != null ? currentTime - messageReceiveMoment : null;
		
		// Check if the coalescing of the messages is in progress.
		boolean coalescingInProgress = delay == null ? false : delay < timeSpanMs;
		
		// Remove unused message receive moment.
		if (!coalescingInProgress && messageReceiveMoment != null) {
			eventHandle.removeStoredReceiveMoment(message);
		}
		
		return coalescingInProgress;
	}
}
