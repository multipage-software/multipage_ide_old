package org.multipage.gui;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Packet object that consists from packet elements.
 * @author vakol
 */
public class Packet {
	
	/**
	 * Packet parts.
	 */
	public LinkedList<PacketElement> packetParts = new LinkedList<PacketElement>();
	
	/**
	 * Auxiliary properties which can be set to help create packet content.
	 */
	public HashMap<Integer, Object> userProperties = new HashMap<Integer, Object>();

	/**
	 * Packet exception raised while reading the packet.
	 */
	public Exception packetException = null;
	
	/**
	 * Constructor.
	 * @param packetReader
	 */
	public Packet() {
	}

	/**
	 * Reset handler.
	 */
	public void reset() {
		
		userProperties.clear();
	}
	
	/**
	 * Throw packet reading exception.
	 */
	public void throwException() 
			throws Exception {
		
		if (packetException == null) {
			return;
		}
		throw packetException;
	}
}