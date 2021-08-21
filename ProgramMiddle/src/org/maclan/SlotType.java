/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.Arrays;
import java.util.List;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public enum SlotType {
	
	UNKNOWN("middle.textUnknownSlotType", "Unknown"),
	TEXT("middle.textTextSlotType", "Text"),
	LOCALIZED_TEXT("middle.textLocalizedTextSlotType", "LocalizedText"),
	INTEGER("middle.textIntegerSlotType", "Integer"),
	REAL("middle.textRealSlotType", "Real"),
	BOOLEAN("middle.textBooleanSlotType", "Boolean"), 
	ENUMERATION("middle.textEnumerationSlotType", "Enumeration"),
	COLOR("middle.textColorSlotType", "Color"), 
	AREA_REFERENCE("middle.textAreaSlotType", "AreaReference"),
	EXTERNAL_PROVIDER("middle.textExternalSlotType", "ExternalProvider"),
	PATH("middle.textPathSlotType", "Path");
	
	/**
	 * Properties.
	 */
	private String text;
	private String typeText;
	
	/**
	 * Constructor.
	 */
	SlotType(String text, String typeText) {
		
		this.text = Resources.getString(text);
		this.typeText = typeText;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

		return text;
	}

	/**
	 * Get type text.
	 * @return
	 */
	public String getTypeText() {
		
		return typeText;
	}

	/**
	 * Returns true value if the type is text.
	 * @return
	 */
	public static boolean isText(SlotType slotType) {
		
		return slotType == SlotType.TEXT || slotType == SlotType.LOCALIZED_TEXT || slotType == SlotType.PATH;
	}
	
	/**
	 * Get all slot types
	 * @return
	 */
	public static List<SlotType> getAll() {
		
		SlotType [] slotTypes =  SlotType.class.getEnumConstants();
		List<SlotType> list = Arrays.asList(slotTypes);
		return list;
	}
	
	/**
	 * Compares text
	 * @param slotType
	 * @return
	 */
	public int compareTextTo(SlotType slotType) {
		
		return this.toString().compareTo(slotType.toString());
	}
	
	/**
	 * If the slot type is unknown, the method returns false
	 * @return
	 */
	public boolean known() {
		
		return this != UNKNOWN;
	}
}
