/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 25-10-2022
 *
 */
package org.multipage.generator;

import org.multipage.gui.Message;

/**
 * GUI messages.
 * @author vakol
 *
 */
public class GuiMessage extends Message {
	
	/**
	 * Returns true if the target class of this message matches the parameter.
	 * @param classObject
	 * @return
	 */
	public boolean targetClass(Class<AreasDiagram> classObject) {

		// Initialize output.
		boolean matches = false;
		
		// Check target class.
		if (target instanceof Class<?>) {
			
			Class<?> targetClass = (Class<?>) target;
			matches = targetClass.equals(classObject);
		}
		
		return matches;
	}
}
