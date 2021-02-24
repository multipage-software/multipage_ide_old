/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */
package org.multipage.generator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.multipage.generator.EventHandle.CoalesceState;
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
	 * Coalesce time span.
	 */
	private static final long timeSpanMs = 500;

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
		 * Dump message.
		 */
		@Override
		public String toString() {
			return "Message [signal=" + signal.name() + "]";
		}
		
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
	 * Message queue.
	 */
	public static LinkedList<Message> messageQueue = new LinkedList<Message>();
	
	/**
	 * All conditional event processors in the application.
	 */
	public static LinkedHashMap<EventCondition, LinkedHashMap<EventConditionPriority,
					LinkedHashMap<Object, HashSet<EventHandle>>>> conditionalEvents = new LinkedHashMap<EventCondition, LinkedHashMap<EventConditionPriority,
																							LinkedHashMap<Object, HashSet<EventHandle>>>>();
	
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
			Obj<Message> incomingMessage = new Obj<Message>(null);
			synchronized (messageQueue) {
				
				if (!messageQueue.isEmpty()) {
					incomingMessage.ref = messageQueue.removeFirst();
				}
			}
			
			// Process the message if it exists.
			if (incomingMessage.ref != null) {
				
				Signal signal = incomingMessage.ref.signal;
				
				// On special events skip the next complex rules.
				if (signal.isSpecial()) {
					invokeSpecialEvents(incomingMessage.ref);
				}
				else {
					
					// Dispatch message to conditional events processors.
					synchronized (conditionalEvents) {
						
						LinkedHashMap<EventConditionPriority, LinkedHashMap<Object, HashSet<EventHandle>>> map1 = conditionalEvents.get(signal);
						
						if (map1 != null) {
							map1.forEach((priority, map2) -> {
								
								if (map2 != null) {
									map2.forEach((key, eventHandles) -> {
										
										invokeEvents(eventHandles, signal, incomingMessage.ref);
									});
								}
							});
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
				
				// Invoke remaining events.
				invokeRemainingEvents();
			}
		}
	}
	
	/**
	 * 
	 * @param eventHandles
	 */
	private static void invokeRemainingEvents() {
		
		conditionalEvents.forEach((signal, map1) -> {
			
			if (map1 != null) {
				map1.forEach((priority, map2) -> {
					
					if (map2 != null) {
						map2.forEach((key, eventHandles) -> {
							
							
							
						});
					}
				});
			}
		});
	}
	
	/**
	 * 
	 * @param eventHandles
	 */
	private static void invokeRemainingEvents(LinkedList<EventHandle> eventHandles) {
		// TODO Auto-generated method stub
		
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
		
		// Initialize.
		final LinkedList<Message> messages = new LinkedList<Message>();
		
		// Invoke actions on the Swing thread.
		for (EventHandle eventHandle : eventHandles) {
			
			SwingUtilities.invokeLater(() -> {
				
				// Get current time.
				final long currentTime = new Date().getTime();
				
				// Dump all overaged messages.
				eventHandle.popOveragedMessages(currentTime, messages);
				
				// Log event.
				if (enableMessageLog) {
					j.log("-----------------------------------------------------------------");
					j.log("Event: %s [Source: %s, OID %d]\t\traised    in %s", message.signal, message.source.getClass().getSimpleName(), System.identityHashCode(message.source), message.reflection);
					j.log("\t-> Action rule: matches %s %s\t\t\tprocessed in %s", eventCondition.getClass().getSimpleName(), eventCondition.name(), eventHandle.reflection);
					j.log("\tDelay: handle \"%s\" [%d]", eventHandle.identifier(), System.identityHashCode(eventHandle));
				}
				
				// Coalesce same events within given time span.
				CoalesceState state = eventHandle.getMessageCoalesceState(currentTime, message);
				
				// Check state.
				if (!CoalesceState.unknown.equals(state)) {
				
					// On coalesce start and progress.
					if (CoalesceState.start.equals(state) || CoalesceState.progress.equals(state)) {
						
						// Create new reception time.
						eventHandle.createNewReceptionRecord(currentTime, message);
					}
				}
				
				// Accept event message (invoke corresponding lambda function).
				messages.forEach(messageToUse -> {
					eventHandle.action.accept(messageToUse);
				});
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
		return registerConditionalEvent(key, eventCondition, EventConditionPriority.middle, messageLambda, defaultMessageCoalesceMs, identifier);
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
		
		synchronized (conditionalEvents) {
			
			// Create auxiliary table from the map.
			ConditionalEventsAuxTable auxiliaryTable = ConditionalEventsAuxTable.createFrom(conditionalEvents);
			
			// Create new event handle, add new table record.
			StackTraceElement reflection = null;
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				reflection = stackElements[3];
			}
			EventHandle handle = new EventHandle(message, timeSpanMs, reflection, identifier);
			auxiliaryTable.addRecord(key, eventCondition, priority, handle);
			
			// Retrieve sorted conditional events.
			conditionalEvents = auxiliaryTable.retrieveSorted();
			
			// Return key.
			return key;
		}
	}
	
	/**
	 * Unregister receivers for conditional events for given key object.
	 * @param key
	 */
	public static void removeReceivers(Object key) {
		
		// Remove conditional events for key.
		conditionalEvents.remove(key);
	}
}
