package org.multipage.gui;

/**
 * Integer number intending for sending block lengths etc.
 * @author vakol
 */
public class PacketNumber extends PacketElement {
	
	/**
	 * Value of integer element. Often length of a block.
	 */
	public int value = 0;
	
	/**
	 * Reset integer value element. 
	 */
	@Override
	public void reset() {
		
		super.reset();
		value = 0;
	}
	
	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		return String.format("NUMBER %s | val=%2d", super.toString(), value);
	}
}