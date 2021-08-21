/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.*;

/**
 * @author
 *
 */
public abstract class SlotHolder {
	
	/**
	 * Slots.
	 */
	protected LinkedList<Slot> slots = new LinkedList<Slot>();
	
	/**
	 * Slots loaded flag.
	 */
	private boolean slotsLoaded = false;

	/**
	 * Get ID.
	 * @return
	 */
	public abstract long getId();

	/**
	 * Clear slots.
	 */
	public void clearSlots() {

		slots.clear();
	}

	/**
	 * Add slot.
	 * @param slot
	 */
	public void addSlot(Slot slot) {
		
		slot.setHolder(this);
		slots.add(slot);
	}

	/**
	 * Get slots list.
	 * @return
	 */
	public LinkedList<Slot> getSlots() {

		return slots;
	}

	/**
	 * Get string.
	 */
	public abstract String toString();

	/**
	 * @param slotsLoaded the slotsLoaded to set
	 */
	public void setSlotsLoaded(boolean slotsLoaded) {
		this.slotsLoaded = slotsLoaded;
	}

	/**
	 * @return the slotsLoaded
	 */
	public boolean areSlotsLoaded() {
		return slotsLoaded;
	}

	/**
	 * Returns true value if the slots have equal name.
	 * @param object
	 * @return
	 */
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof SlotHolder)) {
			return false;
		}
		SlotHolder holder = (SlotHolder) object;
		return holder.getId() == getId();
	}

	/**
	 * .
	 * @return
	 */
	public String getDescriptionForced() {
		
		// Nothing to do.
		return "";
	}
}
