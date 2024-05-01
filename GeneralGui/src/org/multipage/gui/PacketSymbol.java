package org.multipage.gui;

/**
 * Symbol which is repsesented by sequence of bytes.
 * @author vakol
 */
public class PacketSymbol extends PacketElement {
	
	/**
	 * Symbol bytes.
	 */
	public byte [] bytes = {};
	
	/**
	 * Create symbol element.
	 * @param bytes
	 * @return
	 */
	public PacketSymbol(byte [] bytes) {
		
		this.bytes = bytes;
	}
	
	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		

		return String.format("SYMBOL %s | bytes=%s", super.toString(), Utility.prettyPrint(bytes));
	}
}