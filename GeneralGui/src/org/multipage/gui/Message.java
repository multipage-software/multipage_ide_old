/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 28-05-2021
 *
 */

package org.multipage.gui;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * Message object.
 */
public class Message {
	
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
	
	// A flag that is true when the message has to be renewed.
	public boolean renew = false;
	
	// Message source reflection.
	public StackTraceElement reflection;
	
	// Transmit time.
	public Long transmitTime;
	
	// Receive time.
	public Long receiveTime;
	
	// Reference to the object that receives the message.
	public Object receiverObject;
	
	/**
	 * Dump message.
	 */
	@Override
	public String toString() {
		String timeStamp = receiveTime != null ? Utility.formatTime(receiveTime) : "null";
		String info = relatedInfo != null ? ", info=" + relatedInfo.toString() : "";
		return String.format("Message 0x%08x [signal=%s, received=%s%s]", this.hashCode(), signal.name(), timeStamp, info);
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
		// Perform deep check.
		} else if (!Utility.equalsDeep(relatedInfo, other.relatedInfo))
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
			
			// Perform deep check on each item.
			while(--count >= 0)
				if (!Utility.equalsDeep(additionalInfos[count], other.additionalInfos[count]))
					return false;
		}
		return true;
	}
	
	/**
	 * Check if this message coalesces with the input object.
	 * @param obj
	 * @param renew 
	 * @return
	 */
	public boolean coalesces(Object obj, boolean renew) {
		
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
		
		// Check target.
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		
		if (!renew) {
			// Check relatedInfo.
			if (relatedInfo == null) {
				if (other.relatedInfo != null)
					return false;
			// Perform deep check.
			} else if (!Utility.equalsDeep(relatedInfo, other.relatedInfo))
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
				
				// Perform deep check on each item.
				while(--count >= 0)
					if (!Utility.equalsDeep(additionalInfos[count], other.additionalInfos[count]))
						return false;
			}
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
	 * Try to get related information of given returned type.
	 * @return
	 */
	public <T> T getRelatedInfo() {
				
		try {
			
			if (relatedInfo == null) {
				return null;
			}
			
			@SuppressWarnings("unchecked")
			T info = (T) relatedInfo;
			return info;
		}
		catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * Try to get additional information at given array index of given returned type.
	 * @param <T>
	 * @param index
	 * @return
	 */
	public <T> T getAdditionalInfo(int index) {
		
		try {
			
			int length = additionalInfos.length;
			if (index >= length || additionalInfos[index] == null) {
				return null;
			}
			
			@SuppressWarnings("unchecked")
			T info = (T) additionalInfos[index];
			return info;
		}
		catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * Check if the message invokes itself and creates infinite loop. To perform this check inherit receiver from NonCyclicReceiver.
	 * @param updatedModule
	 * @param subsequentMessageTimeoutMs
	 * @param previousMessageLambda
	 * @return
	 */
	public boolean isCyclic(NonCyclingReceiver updatedModule, int subsequentMessageTimeoutMs, Function<Message, Boolean> previousMessageLambda) {
		
		// Check input.
		if (updatedModule == null || signal == null) {
			return false;
		}
		
		// Initialization.
		LinkedList<Message> previousMessages = updatedModule.getPreviousMessages();
		LinkedList<Message> cyclicMessages = new LinkedList<Message>();
		LinkedList<Message> receiverMessages = new LinkedList<Message>();
		
		// Check if previous messages imply infinite loop of messages.
		for (Message previousMessage : previousMessages) {
			
			// Check if message signal matches.
			if (signal != previousMessage.signal) {
				continue;
			}
			
			// Initialize flag value.
			boolean isRepeated = false;
			
			// Check if target of previous message matches the target of this message.
			Object previousTarget = previousMessage.target;
			
			if (previousTarget.equals(target)) {
				receiverMessages.add(previousMessage);
				
				// Check if this message happens before timeout.
				isRepeated = isInvokedBeforeTimeout(previousMessage, subsequentMessageTimeoutMs);
			}
			
			// Use callback to examine if the message is repeated.
			if (previousMessageLambda != null) {
				
				boolean matches = previousMessageLambda.apply(previousMessage);
				if (matches) {
					isRepeated = true;
				}
			}
			
			// If the message is repeated, remove it from the list.
			if (isRepeated) {
				cyclicMessages.add(previousMessage);
				previousMessages.remove(previousMessage);
			}
		}
		
		// Set output value;
		boolean isCyclic = !cyclicMessages.isEmpty();
		
		// Remove found messages from the list.
		if (isCyclic) {
			previousMessages.removeAll(cyclicMessages);
		}
		
		// Remove all receiver messages.
		previousMessages.removeAll(receiverMessages);
		
		// Add the new message to the list of previous messages.
		previousMessages.add(this);
		
		// If this action has been invoked by user do not set the output value to cyclic.
		if (source instanceof EventSource) {
			EventSource updateSource = (EventSource) source;
			
			if (updateSource.isUserAction) {
				isCyclic = false;
			}
		}
		
		return isCyclic;
	}
	
	/**
	 * Check if the message invokes itself and creates infinite loop.
	 * @return
	 */
	public boolean isCyclic() {
		
		final int DEFAULT_SUBSEQUENT_MESASGE_TIMEOUT_MS = 1000;
		
		if (!(receiverObject instanceof NonCyclingReceiver)) {
			return false;
		}
		
		NonCyclingReceiver nonCyclicreceiver = (NonCyclingReceiver) receiverObject;

		// Delegate this call with default timeout.
		return isCyclic(nonCyclicreceiver, DEFAULT_SUBSEQUENT_MESASGE_TIMEOUT_MS, null);
	}

	/**
	 * Check if the message invokes itself and creates infinite loop.
	 * @param updatedModule
	 * @param previousMessageLambda
	 * @return
	 */
	public boolean isCyclic(NonCyclingReceiver updatedModule, Function<Message, Boolean> previousMessageLambda) {
		
		final int DEFAULT_SUBSEQUENT_MESASGE_TIMEOUT_MS = 1000;
		
		// Delegate this call with default timeout.
		return isCyclic(updatedModule, DEFAULT_SUBSEQUENT_MESASGE_TIMEOUT_MS, previousMessageLambda);
	}
	
	/**
	 * Check if this message has been processed before previous messages timeout ellapsed.
	 * @param previousMessage
	 * @param timeoutMs
	 * @return
	 */
	private boolean isInvokedBeforeTimeout(Message previousMessage, int timeoutMs) {
		
		long lastChanceTime = previousMessage.receiveTime + timeoutMs;
		boolean isBefore = (receiveTime <= lastChanceTime);
		return isBefore;
	}
}