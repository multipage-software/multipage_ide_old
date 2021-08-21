/**
 * 
 */
package org.multipage.generator;

import org.maclan.Slot;

/**
 * @author vaclav
 *
 */
public interface SlotSelectedEvent {

	/**
	 * Get the slot
	 */
	public void selected(Slot slot);
}
