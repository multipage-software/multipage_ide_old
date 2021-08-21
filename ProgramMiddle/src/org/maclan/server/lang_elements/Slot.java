/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server.lang_elements;

import org.maclan.MiddleUtility;
import org.maclan.server.AreaServer;

/**
 * @author
 *
 */
public class Slot implements BoxedObject {

	/**
	 * Middle layer server reference.
	 */
	private org.maclan.server.AreaServer server;
	
	/**
	 * Middle layer slot reference.
	 */
	org.maclan.Slot slot;
	
	/**
	 * Slot alias.
	 */
	//graalvm @HostAccess.Export
	public final String alias;
	
	/**
	 * Slot value.
	 */
	//graalvm @HostAccess.Export
	public Object value;

	/**
	 * Constructor.
	 * @param slot
	 */
	public Slot(org.maclan.server.AreaServer server, org.maclan.Slot slot) {
		
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
	//graalvm @HostAccess.Export
	public Area getArea() {
		
		return new Area(server, (org.maclan.Area) slot.getHolder());
	}
	
	/**
	 * Returns true value if this slot has a default value.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isDefault() {
		
		return slot.isDefault();
	}
	
	/**
	 * Returns false value if this slot has a default value.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isNotDefault() {
		
		return !slot.isDefault();
	}
	
	/**
	 * External provider was changed.
	 */
	//graalvm @HostAccess.Export
	public boolean isExternalChange() {
		
		return slot.isExternalChange();
	}
	
	/**
	 * Get special value.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public String getSpecialValue() {
		
		return slot.getSpecialValueNull();
	}
	
	/**
	 * Get area ID value.
	 */
	//graalvm @HostAccess.Export
	public Long getAreaIdValue() {
		
		return slot.getAreaIdValue();
	}
	
	/**
	 * Get slot type.
	 * @return
	 */
	//graalvm @HostAccess.Export
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
	//graalvm @HostAccess.Export
	public String getPath()
		throws Exception {
		
		return server.getPath(slot);
	}
	
	/**
	 * Input slot value.
	 * @return
	 */
	//graalvm @HostAccess.Export
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
	//graalvm @HostAccess.Export
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
	//graalvm @HostAccess.Export
	public void backup(String fileExtension)
		throws Exception {
		
		MiddleUtility.backup(slot, fileExtension);
	}
	
	/**
	 * Lock external source.
	 * @param readOnly
	 */
	//graalvm @HostAccess.Export
	public void lock(boolean readOnly)
		throws Exception {
		
		MiddleUtility.lock(slot, readOnly);
	}
		
	/**
	 * Revert external provider source code.
	 */
	//graalvm @HostAccess.Export
	public void revert()
		throws Exception {
		
		server.revert(slot);
	}
	
	/**
	 * Unlock input.
	 */
	//graalvm @HostAccess.Export
	public void unlockInput()
		throws Exception {
		
		long slotId = slot.getId();
		server.updateInputLock(slotId, false);
	}
	
	/**
	 * Is input lock.
	 * @return
	 */
	//graalvm @HostAccess.Export
	public boolean isInputLock()
		throws Exception {
		
		long slotId = slot.getId();
		boolean locked = server.isInputLock(slotId);
		return locked;
	}
	
	/**
	 * Unlock output.
	 */
	//graalvm @HostAccess.Export
	public void unlockOutput()
		throws Exception {
		
		long slotId = slot.getId();
		server.updateOutputLock(slotId, false);
	}
	
	/**
	 * Is output lock.
	 * @return
	 */
	//graalvm @HostAccess.Export
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
	//graalvm @HostAccess.Export
	public void watch() {
		
		try {
			server.watch(slot);
		}
		catch (Exception e) {
		}
	}
}
