package org.multipage.gui;

/**
 * Base class for packet elements.
 * @author vakol
 */
public abstract class PacketElement {
	
	/**
	 * Packet element byte index..
	 */
	public int index = 0;
	
	/**
	 * Flag that indicates that this object is either empty or ready for using (not partially loaded).
	 */
	public boolean isCompact = true;
	
	/**
	 * Reset packet element. 
	 */
	public void reset() {
		
		index = 0;
		isCompact = true;
	}
	
	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		return String.format("idx=%2d,compact=%4b", index, isCompact);
	}
}