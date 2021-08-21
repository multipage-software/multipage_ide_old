/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import org.maclan.Area;
import org.maclan.AreaReference;
import org.maclan.Slot;
import org.maclan.SlotHolder;
import org.maclan.SlotType;
import org.multipage.gui.StringValueEditor;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class SlotsTableModel extends AbstractTableModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Slot holders.
	 */
	protected LinkedList<? extends SlotHolder> holders;

	/**
	 * Slot table items array.
	 */
	protected ArrayList<Slot> slots = new ArrayList<Slot>();

	/**
	 * Constructor.
	 * @param holders
	 */
	public SlotsTableModel(LinkedList<? extends SlotHolder> holders,
			LinkedList<FoundSlot> foundSlots, boolean showOnlyFound, boolean showAllSlots) {

		setList(holders, foundSlots, showOnlyFound, showAllSlots);
	}

	/**
	 * Set list.
	 * @param holders
	 * @param foundSlots 
	 * @param showOnlyFound - TODO
	 * @param showAllSlots 
	 */
	public void setList(LinkedList<? extends SlotHolder> holders,
			LinkedList<FoundSlot> foundSlots, boolean showOnlyFound, boolean showAllSlots) {

		this.holders = holders;
		
		// Clear items.
		slots.clear();
		
		// List of found slots.
		ArrayList<Slot> highlightedSlots = new ArrayList<Slot>();
		
		// List of other slots.
		ArrayList<Slot> otherSlots = new ArrayList<Slot>();
		
		// Load table items.
		for (SlotHolder holder : holders) {
			
			// Do loop for all slots.
			for (Slot slot : holder.getSlots()) {
				
				if (!showAllSlots && slot.isUserDefined()) {
					continue;
				}
				
				// Load possible area value object.
				AreaReference areaValue = slot.getAreaValue();
				if (areaValue != null && !areaValue.existsAreaObject()) {
					
					long areaValueId = areaValue.areaId;
					Area area = ProgramGenerator.getArea(areaValueId);
					
					areaValue.setAreaObject(area);
				}
				
				// Add slot to array.
				if (FoundSlot.isSlotFound(foundSlots, slot)) {
					highlightedSlots.add(slot);
				}
				else {
					otherSlots.add(slot);
				}
			}
		}
		
		// Add highlighted slots to the beginning of the array.
		slots.addAll(highlightedSlots);
		
		// If not only found slots should be displayed, add all other slots.
		if (!showOnlyFound) {
			slots.addAll(otherSlots);
		}
	}

	/**
	 * Remove items.
	 * @param items
	 */
	public void removeAll(LinkedList<Slot> slots) {

		this.slots.removeAll(slots);
		fireTableDataChanged();
	}

	/**
	 * Get item.
	 * @param index
	 * @return
	 */
	public Slot get(int index) 
		throws IndexOutOfBoundsException {

		if (index < 0 || index >= slots.size()) {
			return null;
		}
		return slots.get(index);
	}

	/**
	 * Add item.
	 * @param slot
	 */
	public void add(Slot slot) {

		slots.add(slot);
		fireTableDataChanged();
	}

	/**
	 * Return row count.
	 */
	@Override
	public int getRowCount() {

		return slots.size();
	}

	/**
	 * Return column count.
	 */
	@Override
	public int getColumnCount() {

		return 4;
	}

	/**
	 * Return value.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		if (rowIndex < 0 || rowIndex >= slots.size()) {
			return null;
		}

		Slot slot = slots.get(rowIndex);
		
		switch (columnIndex) {
		
		case 0:
			return slot.getAlias();
		case 1:
			if (slot.isDefault()) {
				return "\uFFFFdefault";
			}
			
			String specialValue = slot.getSpecialValueNull();
			if (specialValue != null) {
				return specialValue;
			}
			
			String valueMeaning = slot.getValueMeaning();
			if (valueMeaning != null && valueMeaning.charAt(0) == 'c' 
					&& !valueMeaning.equals(StringValueEditor.meansColor)
					&& !valueMeaning.equals(StringValueEditor.meansCssNumber)
					&& !valueMeaning.equals(StringValueEditor.meansCssTextLine)) {
					
				return ".....";
			}
			
			if (slot.getType() == SlotType.LOCALIZED_TEXT) {
				return String.format("<html>%s</html>", slot.getTextValue());
			}
			
			return slot.getTextValue();
		case 2:
			if (holders.size() == 1) {
				return Resources.getString("org.multipage.generator.textThis");
			}
			return slot.getHolder().toString();
		case 3:
			return slot.getNameForGenerator();
		case 4:
			return slot;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {

		String columnTextId;
		
		switch (column) {
		
		case 0:
			columnTextId = "org.multipage.generator.textSlotAlias";
			break;
		case 1:
			columnTextId = "org.multipage.generator.textSlotValue";
			break;
		case 2:
			columnTextId = "org.multipage.generator.textSlotHolder";
			break;
		case 3:
			columnTextId = "org.multipage.generator.textSlotAlias2";
			break;
		default:
			return "";
		}
		
		return Resources.getString(columnTextId);
	}

	/**
	 * @return the slots
	 */
	public ArrayList<Slot> getSlots() {
		return slots;
	}
}