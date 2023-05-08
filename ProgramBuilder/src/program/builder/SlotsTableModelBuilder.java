/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.util.Resources;

import java.util.LinkedList;

import org.multipage.generator.*;
import org.maclan.*;

/**
 * 
 * @author
 *
 */
public class SlotsTableModelBuilder extends SlotsTableModel {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param holders
	 * @param foundSlots
	 * @param showOnlyFound
	 */
	public SlotsTableModelBuilder(LinkedList<? extends SlotHolder> holders,
			LinkedList<FoundSlot> foundSlots, boolean showOnlyFound, boolean showAllSlots) {
		
		super(holders, foundSlots, showOnlyFound, showAllSlots);
	}

	/* (non-Javadoc)
	 * @see org.multipage.generator.SlotsTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		
		return 6;
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
		if (slot == null) {
			return null;
		}
		
		switch (columnIndex) {
		
		case 0:
			return slot.getAccess();
		case 1:
			return slot.getAlias();
		case 2:
			if (!slot.isDefault()) {
				
				String specialValue = slot.getSpecialValueNull();
				if (specialValue != null) {
					return specialValue;
				}
				return slot.getTextValueDecorated();
			}
			return "\uFFFFdefault";
		case 3:
			return slot.getTypeUseValueMeaning().toString();
		case 4:
			if (holders.size() == 1) {
				return Resources.getString("org.multipage.generator.textThis");
			}
			return slot.getHolder().toString();
		case 5:
			return slot.getName();
		case 6:
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
			columnTextId = "org.multipage.generator.textSlotAccess";
			break;
		case 1:
			columnTextId = "org.multipage.generator.textSlotAlias";
			break;
		case 2:
			columnTextId = "org.multipage.generator.textSlotValue";
			break;
		case 3:
			columnTextId = "builder.textSlotValueType";
			break;
		case 4:
			columnTextId = "org.multipage.generator.textSlotHolder";
			break;
		case 5:
			columnTextId = "builder.textSlotName";
			break;
		default:
			return "";
		}
		
		String columnText = Resources.getString(columnTextId);
		return columnText;
	}
}
