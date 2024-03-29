/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server.lang_elements;

import com.maclan.MiddleUtility;
import com.maclan.server.AreaServer;

/**
 * @author
 *
 */
public class Slot implements BoxedObject {

	/**
	 * Middle layer server reference.
	 */
	private com.maclan.server.AreaServer server;
	
	/**
	 * Middle layer slot reference.
	 */
	com.maclan.Slot slot;
	
	/**
	 * Slot alias.
	 */
	public final String alias;
	
	/**
	 * Slot value.
	 */
	public Object value;

	/**
	 * Constructor.
	 * @param slot
	 */
	public Slot(com.maclan.server.AreaServer server, com.maclan.Slot slot) {
		
		this.server = server;
		this.slot = slot;
		
		try {
			server.loadSlotValue(slot);
		}
		catch (Exception e) {
		}
		
		// Set alias and value.
		this.alias = slot.getAlias();
		
		Object slotValue = slot.getSimpleValue();
			
		// Create and set localized text with ID.
		if (server.state.showLocalizedTextIds && slot.isLocalized() && slotValue instanceof String) {
			
			String textValue = (String) slotValue;
			String textId = AreaServer.getIdHtml("S", slot.getId());
			
			this.value = textId + textValue;
		}
		else {
			this.value = slotValue;
		}
	}

	/**
	 * Unbox object.
	 */
	@Override
	public Object unbox() {
		
		return slot;
	}

	/**
	 * Convert to string.
	 */
	@Override
	public String toString() {
		
		return alias;
	}
	
	/**
	 * Get slot area.
	 * @return
	 */
	public Area getArea() {
		
		return new Area(server, (com.maclan.Area) slot.getHolder());
	}
	
	/**
	 * Returns true value if this slot has a default value.
	 * @return
	 */
	public boolean isDefault() {
		
		return slot.isDefault();
	}
	
	/**
	 * Returns false value if this slot has a default value.
	 * @return
	 */
	public boolean isNotDefault() {
		
		return !slot.isDefault();
	}
	
	/**
	 * External provider was changed.
	 */
	public boolean isExternalChange() {
		
		return slot.isExternalChange();
	}
	
	/**
	 * Get special value.
	 * @return
	 */
	public String getSpecialValue() {
		
		return slot.getSpecialValueNull();
	}
	
	/**
	 * Get area ID value.
	 */
	public Long getAreaIdValue() {
		
		return slot.getAreaIdValue();
	}
	
	/**
	 * Get slot type.
	 * @return
	 */
	public String getType() {
		
		try {
			server.loadSlotValue(slot);
			String type = slot.getTypeText();
			return type;
		}
		catch (Exception e) {
			return "error";
		}
	}
	
	/**
	 * Get path.
	 * @return
	 * @throws Exception 
	 */
	public String getPath()
		throws Exception {
		
		return server.getPath(slot);
	}
	
	/**
	 * Input slot value.
	 * @return
	 */
	public String input() {
		
		String textValue = "";
		
		try {
			server.input(slot);
			
			textValue = slot.getTextValue();
			value = textValue;
		}
		catch (Exception e) {
		}
		
		return textValue;
	}
	
	/**
	 * Output slot value.
	 * @param outputText
	 * @return
	 */
	public void output(String outputText)
		throws Exception {
		
		try {
			server.output(slot, outputText);
		}
		catch (Exception e) {
		}
	}

	/**
	 * Backup external provider.
	 * @param fileExtension
	 * @return
	 */
	public void backup(String fileExtension)
		throws Exception {
		
		MiddleUtility.backup(slot, fileExtension);
	}
	
	/**
	 * Lock external source.
	 * @param readOnly
	 */
	public void lock(boolean readOnly)
		throws Exception {
		
		MiddleUtility.lock(slot, readOnly);
	}
		
	/**
	 * Revert external provider source code.
	 */
	public void revert()
		throws Exception {
		
		server.revert(slot);
	}
	
	/**
	 * Unlock input.
	 */
	public void unlockInput()
		throws Exception {
		
		long slotId = slot.getId();
		server.updateInputLock(slotId, false);
	}
	
	/**
	 * Is input lock.
	 * @return
	 */
	public boolean isInputLock()
		throws Exception {
		
		long slotId = slot.getId();
		boolean locked = server.isInputLock(slotId);
		return locked;
	}
	
	/**
	 * Unlock output.
	 */
	public void unlockOutput()
		throws Exception {
		
		long slotId = slot.getId();
		server.updateOutputLock(slotId, false);
	}
	
	/**
	 * Is output lock.
	 * @return
	 */
	public boolean isOutputLock()
		throws Exception {
		
		long slotId = slot.getId();
		boolean locked = server.isOutputLock(slotId);
		return locked;
	}
	
	/**
	 * Watch source change in external provider.
	 * @return
	 */
	public void watch() {
		
		try {
			server.watch(slot);
		}
		catch (Exception e) {
		}
	}
}
