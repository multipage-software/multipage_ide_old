/**
 * 
 */
package org.multipage.generator;

import com.maclan.Slot;

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
