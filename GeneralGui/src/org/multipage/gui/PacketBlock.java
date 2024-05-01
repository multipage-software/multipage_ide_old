package org.multipage.gui;

import java.nio.ByteBuffer;

/**
 * Block of sent bytes.
 * @author vakol
 */
public class PacketBlock extends PacketElement {
	
	/**
	 * Byte buffer.
	 */
	public ByteBuffer buffer = null;
	
	/**
	 * Number of increased bytes when reallocating the byte buffer.
	 */
	public int increaseSize = 127;
	
	/**
	 * Length of block in bytes.
	 */
	public int length = 0;
	
	/**
	 * Byte sequence that terminates the block.
	 */
	public PacketSymbol terminalSymbol = null;
	
	/**
	 * Terminal symbol byte pointer.
	 */
	public int terminalIndex = 0;
	
	/**
	 * Create block element.
	 * @param initialBufferSize - can be null
	 * @param increaseSize - can be -1
	 */
	public PacketBlock(int initialBufferSize, int increaseSize, PacketSymbol terminalSymbol, int length) {
		
		this.buffer = ByteBuffer.allocate(initialBufferSize);
		this.increaseSize = increaseSize;
		this.length = length;
		this.terminalSymbol = terminalSymbol;
		this.terminalIndex = 0;
	}
	
	/**
	 * Reset block element. 
	 */
	@Override
	public void reset() {
		
		super.reset();
		buffer.clear();
		terminalIndex = 0;
		length = 0;
	}
	
	/**
	 * String representation.
	 */
	@Override
	public String toString() {
		
		return String.format("BLOCK  %s | Bpos=%3d,Blimt=%4d,Bcapa=%4d,Bincr=%4d| len=%2d,bytes=%s,term=%s,tidx=%1d", super.toString(),
				             buffer.position(), buffer.limit(), buffer.capacity(), increaseSize, length,
				             Utility.prettyPrint(buffer.array()),
				             Utility.prettyPrint(terminalSymbol.bytes), terminalIndex);
	}
}