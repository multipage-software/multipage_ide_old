/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.util.LinkedList;

import org.maclan.Slot;
import org.multipage.gui.FoundAttr;


/**
 * @author
 *
 */
public class FoundSlot {
	
	/**
	 * Slot reference.
	 */
	private Slot slot;
	
	/**
	 * Found attr.
	 */
	private FoundAttr foundAttr;

	/**
	 * Constructor.
	 * @param slot
	 * @param searchText
	 * @param isCaseSensitive
	 * @param isWholeWordsButton
	 * @param isExactMatch
	 */
	public FoundSlot(Slot slot, String searchText, boolean isCaseSensitive,
			boolean isWholeWordsButton) {
		
		this.slot = slot;
		this.foundAttr = new FoundAttr(searchText, isCaseSensitive, isWholeWordsButton);
	}

	/**
	 * Constructor.
	 * @param slot
	 * @param foundAttr
	 */
	public FoundSlot(Slot slot, FoundAttr foundAttr) {
		
		this.slot = slot;
		this.foundAttr = foundAttr;
	}

	/**
	 * Returns true value if the slot is found.
	 * @param slot2
	 * @return
	 */
	public static boolean isSlotFound(LinkedList<FoundSlot> foundSlots, Slot slot) {
		
		// Do loop for all slots.
		for (FoundSlot foundSlot : foundSlots) {
			if (foundSlot.slot.equals(slot)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns found attributes.
	 * @param foundSlots
	 * @param slot
	 * @return
	 */
	public static FoundAttr getFoundAtt(LinkedList<FoundSlot> foundSlots,
			Slot slot) {
		
		if (foundSlots == null) {
			return null;
		}
		
		// Do loop for all slots.
		for (FoundSlot foundSlot : foundSlots) {
			if (foundSlot.slot.equals(slot)) {
				return foundSlot.foundAttr;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FoundSlot [slot=" + slot + ", foundAttr=" + foundAttr + "]";
	}
}
