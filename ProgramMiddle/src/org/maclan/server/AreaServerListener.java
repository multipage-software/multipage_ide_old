/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.LinkedList;

/**
 * @author
 *
 */
public class AreaServerListener {

	/**
	 * Returns true value if the language is rendered.
	 * @param languageId
	 * @return
	 */
	public boolean isRendered(long languageId) {
		// You can override this method.
		return true;
	}
	
	/**
	 * Get render directory.
	 * @return
	 */
	public String getRenderingTarget() {
		// You can override this method.
		return null;
	}
	
	/**
	 * On area server error.
	 * @param message
	 */
	public void onError(String message) {
		// You can override this method.
		
	}
	
	/**
	 * On updated slots.
	 * @param slotIds
	 */
	public void updatedSlots(LinkedList<Long> slotIds) {
		// You can override this method.
		
	}
}
