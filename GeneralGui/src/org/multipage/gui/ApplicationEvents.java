/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 18-06-2017
 *
 */

package org.multipage.gui;

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

import org.multipage.util.Lock;
import org.multipage.util.Obj;

/**
 * Class for events that can be used in this application.
 * @author vakol
 *
 */
public class ApplicationEvents {
	
	/**
	 * Receiver priorities.
	 */
	public static final int HIGH_PRIORITY = 100;
	public static final int MIDDLE_PRIORITY = 50;
	public static final int LOW_PRIORITY = 10;
	
	/**
	 * Message targets.
	 */
	public static enum Target {
		
		all,
		gui,
		notGui
	}
	
	/**
	 * Parameters for event log.
	 */
	public static class LogParameters {
		
		// If you want to enable message log set this flag to true.
		public boolean enable = false;
		
		// To display full log information set this flag to true.
		public boolean full = false;
		
		// List of signals to log.
		public Signal [] loggedSignals = { };
	}
	public static LogParameters logParameters = new LogParameters();
	
	/**
	 * Message coalescing time in milliseconds.
	 */
	private static final long defaultCoalesceTimeMs = 500;
	
	/**
	 * Flag that stops receiving unnecessary events. (Only for debugging purposes).
	 */
	private static boolean stopReceivingUnnecessary = false;
	
	/**
	 * Message coalesce delay.
	 */
	private final static long minDelayMessageCoalesceMs = 25;
	
	/**
	 * Dispatch lock timeout in milliseconds. Must be greater then above coalesce time span.
	 */
	private final static long dispatchLockTimeoutMs = 250;
	
	/**
	 * Message renewal time in milliseconds.
	 */
	private static long messageRenewalIntervalMs = dispatchLockTimeoutMs;
	
	/**
	 * Receiver latency.
	 */
	private static final int DEFAULT_REPEAT_LATENCY_MS = 1000;
	
	/**
	 * Message queue.
	 */
	private static LinkedList<Message> messageQueue = new LinkedList<Message>();
	
	/**
	 * Map of all registered receivers in the application.
	 */
	private static LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>>
		receivers = new LinkedHashMap<ApplicationEvent, LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>>>();
	
	/**
	 * Main dispatch thread.
	 */
	private static Thread dispatchThread;
	
	/**
	 * If this flag is set to true the above thread stops dispatching messages.
	 */
	private static boolean stopDispatchMessages = false;
	
	/**
	 * Lock used in the dispatch thread. When locked the thread waits for incoming messages
	 * and when unlocked a new message arrives.
	 */
	private static Lock dispatchLock = new Lock();
	
	/**
	 * Surviving messages for coalescing of same messages within a period of time.
	 * ( maps Expiration Time to Message )
	 */
	private static LinkedHashMap<Long, Message> survivingMessages = new LinkedHashMap<Long, Message>();
	
	/**
	 * Static constructor. It starts message dispatch thread.
	 */
	static {
		
		// Create and run dispatch thread.
		dispatchThread = new Thread(() -> {
			
			dispatchThread();
			
		}, "IDE-Events-Dispatcher");
		
		dispatchThread.start();
	}
	
	/**
	 * Pop message from the message queue.
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
	 * Push message into message queue.
	 * @param message
	 */
	private static void pushMessage(Message message) {
		
		synchronized (messageQueue) {
			
			// Append message to the end of the queue.
			messageQueue.add(message);
		}
	}

	/**
	 * Create queue snapshot.
	 * @return
	 */
	public static LinkedList<Message> getQueueSnapshot() {
		
		synchronized (messageQueue) {
			
			if (messageQueue.isEmpty()) {
				return null;
			}
			
			// Make message queue clone.
			LinkedList<Message> queueSnapshot = new LinkedList<Message>();
			queueSnapshot.addAll(messageQueue);
			
			return queueSnapshot;
		}
	}
	
	/**
	 * Message dispatch thread.
	 */
	private static void dispatchThread() {
		
		Obj<Message> incomingMessage = new Obj<Message>(null);
		
		// Enter message dispatch loop.
		while (!stopDispatchMessages) {
			
			// Try to pop single message from the queue or just wait for a new message.
			do {
				
				// Log message queue snapshot.
				LoggingCallback.addMessageQueueSnapshot(getQueueSnapshot(), Utility.getNow());
				
				// Pop the message and exit waiting loop.
				incomingMessage.ref = popMessage();
				if (incomingMessage.ref != null) {
					break;
				}
	
				// Wait for a new message.
				Lock.waitFor(dispatchLock, dispatchLockTimeoutMs);
			}
			while (incomingMessage.ref == null);
			
			// Get current time and save this value in message member.
			final Long currentTime = Utility.getNow();
			incomingMessage.ref.receiveTime = currentTime;
			
			// Log incoming message.
			LoggingCallback.log(incomingMessage.ref);
			
			// Get message signal.
			Signal signal = incomingMessage.ref.signal;
			
			// Clear expired messages.
			clearExpiredMessages(currentTime);
			
			// If similar message is surviving in the dedicated memory,
			// current message shell be skipped.
			if (!isMessageSurviving(incomingMessage.ref, currentTime)) {
				
				// Break point managed by log system.
				LoggingCallback.breakPoint(signal);
				
				// For special signals perform special method invocations.
				if (signal.isSpecial()) {
					invokeSpecialEvents(incomingMessage.ref);
				}
				// For all other matching receivers invoke theirs actions.
				else {
					synchronized (receivers) {
						
						// Filter the receivers using incoming signal.
						LinkedHashMap<Integer, LinkedHashMap<Object, LinkedList<EventHandle>>> priorities = receivers.get(signal);
						if (priorities != null) {
							
							// For each stored receiver priority retrieve appropriate receiver objects.
							priorities.forEach((priority, receiverObjects) -> {
								if (receiverObjects != null) {
									
									// Invoke actions for event handles with above priority.
									receiverObjects.forEach((receiverObject, eventHandles) -> {
										
										if (!(signal.isUnnecessary() && stopReceivingUnnecessary)) {  // ... this switch is only for debugging purposes; the condition disables receiving of unnecessary signals
											
											// Remember the message's receiver.
											incomingMessage.ref.receiverObject = receiverObject;
											
											// Invoke event actions matching the incoming message.
											// Let survive messages for a time spans defined in event handler.
											invokeActions(currentTime, eventHandles, priority, receiverObject, incomingMessage.ref);
										}
									});
								}
							});
						}
					}
				}
			}
			else {
				
				// If the message should be renewed schedule it again.
				if (incomingMessage.ref.renew) {
					
					// Wait for a while, then push message back into message queue with renewal flag cleared
					// and with receive time updated. Release message dispatch lock.
					try {
						Thread.sleep(messageRenewalIntervalMs);
						
						incomingMessage.ref.receiveTime = currentTime;
						incomingMessage.ref.renew = false;
						
						pushMessage(incomingMessage.ref);
						
						Lock.notify(dispatchLock);
					}
					catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	/**
	 * Invoke special events.
	 * @param message
	 */
	private static void invokeSpecialEvents(Message message) {
		
		SwingUtilities.invokeLater(() -> {
			
			try {
				// Check the signal and the target function.
				if (Signal._invokeLater.equals(message.signal) && message.target instanceof Function) {
					
					// Retrieve lambda function and run it.
					Function<Message, Exception> lambdaFunction = (Function<Message, Exception>) message.target;
					Exception exception = lambdaFunction.apply(message);
					
					// Throw exception.
					if (exception != null) {
						throw exception;
					}
				}
				// Check enable target signal.
				else if (Signal._enableTargetSignal.equals(message.signal) && message.target instanceof Signal) {
					
					// Get signal that will be enabled.
					Signal signalToEnable = (Signal) message.target;
					// Enable the signal.
					signalToEnable.enable();
				}
			}
			catch (Exception e) {
				
				// Print stack trace for special event.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Invoke events. Pass messages to target lambda functions.
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
		
		// Go through event handles and invoke appropriate action.
		for (EventHandle eventHandle : eventHandles) {
			
			// Compute message expiration time.
			long expirationTime = currentTime + eventHandle.coalesceTimeSpanMs;
			
			// Remember message priority and event handler key.
			eventHandle.priority = priority;
			eventHandle.key = key;
			
			// For coalescing purposes let incoming message survive in dedicated memory until expiration time.
			letMessageSurvive(message, expirationTime);
			
			// Break point managed by log.
			LoggingCallback.breakPoint(message.signal);
			
			// Invoke event on Swing thread and write to log.
			SwingUtilities.invokeLater(() -> {
				
				// Invoke action.
				eventHandle.action.accept(message);
				long executionTime = Utility.getNow();
						
				// Log event.
				LoggingCallback.log(message, eventHandle, executionTime);
			});
		}
	}
	
	/**
	 * Let survive message in memory dedicated for coalescing of same messages.
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
		
		// Get renewal flag.
		boolean renew = message.renew;

		// Try to find message that equals input message.
		List<Entry<Long, Message>> foundEqualMessages = survivingMessages.entrySet().stream()
			.filter(item -> message.coalesces(item.getValue(), renew))
			.collect(Collectors.toList());
		
		// Check survived message.
		boolean messageSurviving = !foundEqualMessages.isEmpty();
		
		// Return state.
		return messageSurviving;
	}
	
	/**
	 * Clear expired messages.
	 * @param currentTime
	 */
	private static void clearExpiredMessages(Long currentTime) {
		
		// Initialization.
		HashSet<Long> expirationsToRemove = new HashSet<Long>();
		
		// Find expired time points.
		survivingMessages.entrySet().stream()
			.filter(item -> item.getKey() < currentTime)
			.forEach(item -> expirationsToRemove.add(item.getKey()));
		
		// Remove expired map items.
		expirationsToRemove.stream().forEach(exprationTime -> survivingMessages.remove(exprationTime));
	}
	
	/**
	 * Stop main thread.
	 */
	public static void stopDispatching() {
		
		// Release objects.
		synchronized (messageQueue) {
			messageQueue.clear();
			
			synchronized (receivers) {
				receivers.clear();
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
	 * @param source - source is mostly object that calls the transmit() method
	 * @param signal - transmitted signal
	 * @param info   - first message info is stored in relatedInfo member, additional
	 *                 infos are stored in additionalInfo array
	 */
	public static void transmit(Object source, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, Target.all, signal, false, info);
	}
	
	/**
	 * Transmit renewed signal.
	 * @param source - source is mostly object that calls the transmit() method
	 * @param signal - transmitted signal
	 * @param info   - first message info is stored in relatedInfo member, additional
	 *                 infos are stored in additionalInfo array
	 */
	public static void transmitRenewed(Object source, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, Target.all, signal, true, info);
	}
	
	/**
	 * Transmit signal.
	 * @param source - source is mostly object that calls the transmit() method
	 * @param target - can be Target class object that specifies common target group or
	 *                 it can be any other object used in application
	 * @param signal - transmitted signal
	 * @param info   - first message info is stored in relatedInfo member, additional
	 *                 infos are stored in additionalInfo array
	 */
	public static void transmit(Object source, Object target, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, target, signal, false, info);
	}
	
	
	/**
	 * Transmit renewed signal.
	 * @param source - source is mostly object that calls the transmit() method
	 * @param target - can be Target class object that specifies common target group or
	 *                 it can be any other object used in application
	 * @param signal - transmitted signal
	 * @param info   - first message info is stored in relatedInfo member, additional
	 *                 infos are stored in additionalInfo array
	 */
	public static void transmitRenewed(Object source, Object target, Signal signal, Object ... info) {
		
		// Delegate the call.
		propagateMessage(source, target, signal, true, info);
	}
	
	/**
	 * Entry point that performs signals transmission.
	 * @param source - source of signals
	 * @param signalGroup - signal groups
	 * @param additionalGoupsAndInfos - can contain additional signal group list an can be followed by info list
	 * 					                that will be transmitted inside each signal message.  
	 * 				   					The first info is saved as relatedInfo field and additional items
	 *                 					are saved in an array named additionalInfo inside message. Theirs values 
	 *                 					are checked against class types included in signal object.
	 */
	public static void transmit(EventSource source, SignalGroup signalGroup, Object ... additionalGoupsAndInfos) {
		
		// Check group signals.
		if (signalGroup.signals == null) {
			return;
		}
		
		// Get list of signals.
		LinkedList<Signal> signals = new LinkedList<Signal>();
		signals.addAll(signalGroup.signals);
		
		int count = additionalGoupsAndInfos.length;
		int index = 0;
		for (; index < count; index++) {
			
			Object item = additionalGoupsAndInfos[index];
			if (item instanceof SignalGroup) {
				
				SignalGroup additionalSignalGroup = (SignalGroup) item;
				signals.addAll(additionalSignalGroup.signals);
			}
			else {
				break;
			}
		}
		
		// Get array of infos.
		Object [] infos = new Object[count - index];
		for (int infoIndex = 0; index < count; index++, infoIndex++) {
			infos[infoIndex] = additionalGoupsAndInfos[infoIndex];
		}
		
		// Transmit all enabled signals.
		for (Signal signal : signals) {
			
			// Get checked infos.
			Object [] checkedInfos = signal.getCheckedInfos(infos);
			
			// Transmit current signal.
			ApplicationEvents.transmit(source, signal, checkedInfos);
		}
	}
	
	/**
	 * Propagate message. This is internal method.
	 * @param source
	 * @param signal
	 * @param target
	 * @param info
	 */
	private static void propagateMessage(Object source, Object target, Signal signal, boolean renew, Object ... info) {
		
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
			
			long currentTime = Utility.getNow();
			
			Message message = new Message();
			
			message.source = source;
			message.target = target;
			message.signal = signal;
			message.receiveTime = currentTime;
			
			if (info instanceof Object []) {
				int count = info.length;
				if (count >= 1) {
					message.relatedInfo = info[0];
				}
				if (count >= 2) {
					message.additionalInfos = Arrays.copyOfRange(info, 1, count);
				}
			}
			message.renew = renew;
			
			StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
			if (stackElements.length >= 4) {
				message.reflection = stackElements[3];
			}
			
			messageQueue.add(message);
			Lock.notify(dispatchLock);
		}
	}
	
	/**
	 * Invoke lambda function later on the message dispatch thread using Runnable type.
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
			
			
			Message message = new Message();
			
			message.source = ApplicationEvents.class;
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
	 * Post  "enable signal" message.
	 * @param signalToEnable
	 */
	public static void enableSignal(Signal signalToEnable) {
		
		// Creates special message with _enableSelectedSignal and put it into the message queue.
		// Then unlocks the message dispatch thread.
		synchronized (messageQueue) {
			
			Message message = new Message();
			
			message.source = ApplicationEvents.class;
			message.target = signalToEnable;
			message.signal = Signal._enableTargetSignal;
			
			messageQueue.add(message);
			
			Lock.notify(dispatchLock);
		}
	}

	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventCondition - condition that must be met when the message is received
	 * @param messageLambda
	 * @return 
	 */
	public static Object receiver(Object receiverObject, ApplicationEvent eventCondition, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, MIDDLE_PRIORITY, messageLambda, defaultCoalesceTimeMs, null);
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param repeatLatencyMs
	 * @param eventCondition - condition that must be met when the message is received
	 * @param messageLambda
	 * @return 
	 */
	public static Object receiver(Object receiverObject, int repeatLatencyMs, ApplicationEvent eventCondition, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, repeatLatencyMs, eventCondition, MIDDLE_PRIORITY, messageLambda, defaultCoalesceTimeMs, null);
	}
		
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventCondition
	 * @param priority
	 * @param messageLambda
	 */
	public static Object receiver(Object receiverObject, ApplicationEvent eventCondition, int priority, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, priority, messageLambda, defaultCoalesceTimeMs, null);
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param repeatLatencyMs
	 * @param eventCondition
	 * @param priority
	 * @param messageLambda
	 */
	public static Object receiver(Object receiverObject, int repeatLatencyMs, ApplicationEvent eventCondition, int priority, Consumer<Message> messageLambda) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, priority, messageLambda, defaultCoalesceTimeMs, null);
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventConditions
	 * @param messageLambda
	 * @return
	 */
	public static Object [] receiver(Object receiverObject, ApplicationEvent [] eventConditions, Consumer<Message> messageLambda) {
		
		final long timeSpanMs = 500;
		
		// Initialization.
		int count = eventConditions.length;
		Object [] outputKeys = new Object[count];
		
		// Add action rules.
		for (int index = 0; index < count; index++) {
			
			ApplicationEvent eventCondition = eventConditions[index];
			outputKeys[index] = registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, MIDDLE_PRIORITY, messageLambda, timeSpanMs, null);
		}
		
		return outputKeys;
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventCondition
	 * @param messageLambda
	 * @param messageCoalesceMs
	 * @return
	 */
	public static Object receiver(Object receiverObject, ApplicationEvent eventCondition, Consumer<Message> messageLambda, Long messageCoalesceMs) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, MIDDLE_PRIORITY, messageLambda, messageCoalesceMs, null);
	}

	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventCondition
	 * @param messageLambda
	 * @param coalesceTimeSpanMs
	 * @param identifier
	 * @return
	 */
	public static Object receiver(Object receiverObject, ApplicationEvent eventCondition, Consumer<Message> messageLambda, String identifier) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, MIDDLE_PRIORITY, messageLambda, minDelayMessageCoalesceMs, identifier);
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param eventCondition
	 * @param messageLambda
	 * @param messageCoalesceMs
	 * @param identifier
	 * @return
	 */
	public static Object receiver(Object receiverObject, ApplicationEvent eventCondition, Consumer<Message> messageLambda, Long messageCoalesceMs, String identifier) {
		
		// Delegate the call.
		return registerReceiver(receiverObject, DEFAULT_REPEAT_LATENCY_MS, eventCondition, MIDDLE_PRIORITY, messageLambda, messageCoalesceMs, identifier);
	}
	
	/**
	 * Register message receiver.
	 * @param receiverObject - object that receives messages
	 * @param repeatLatencyMs - latency value that can avoid infinite message cycles
	 * @param eventCondition
	 * @param priority 
	 * @param messageLambda
	 * @param timeSpanMs
	 * @param identifier 
	 * @return 
	 */
	private static Object registerReceiver(Object receiverObject, int repeatLatencyMs, ApplicationEvent eventCondition, int priority,
			Consumer<Message> messageLambda, Long timeSpanMs, String identifier) {
		
		// Get this call reflection.
		Obj<StackTraceElement> reflection = new Obj<StackTraceElement>(null);
		StackTraceElement stackElements [] = Thread.currentThread().getStackTrace();
		if (stackElements.length >= 4) {
			reflection.ref = stackElements[3];
		}
		
		synchronized (receivers) {
		
			// Create sorting table for receivers.
			ReceiversSortingTable receiversSotingTable = ReceiversSortingTable.createFrom(receivers);
			
			// Create new event handle and add it to sorting table.
			EventHandle eventHandle = new EventHandle(messageLambda, priority, timeSpanMs, reflection.ref, identifier);
			receiversSotingTable.addReceiver(receiverObject, eventCondition, priority, eventHandle);
			
			// Retrieve sorted list of receivers.
			receivers = receiversSotingTable.retrieveSortedReceivers();
		}
		
		// If the key object is a Swing component automatically release receiver if component was removed.
		if (receiverObject instanceof JComponent) {
			JComponent component = (JComponent) receiverObject;
			
			component.addAncestorListener(new AncestorListener() {
				
				// Register conditional event listener.
				@Override
				public void ancestorAdded(AncestorEvent event) {
					// Nothing to do when the component is added.
				}
				
				// When component is removed release all listeners associated with key object.
				@Override
				public void ancestorRemoved(AncestorEvent event) {
					ApplicationEvents.removeReceivers(receiverObject);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					// Nothing to do when the component is moved.
				}
			});
		}
		
		// Return key object.
		return receiverObject;
	}
	
	/**
	 * Unregister receivers using key object.
	 * @param key
	 */
	public static void removeReceivers(Object key) {
		
		synchronized (receivers) {
			
			// Remove receivers for key.
			receivers.remove(key);
		}
	}
}
