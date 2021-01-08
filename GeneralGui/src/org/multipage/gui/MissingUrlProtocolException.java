/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 13-05-2020
 *
 */

package org.multipage.gui;

import java.net.MalformedURLException;

/**
 * 
 * @author user
 *
 */
public class MissingUrlProtocolException extends MalformedURLException {
	
	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param message
	 */
	public MissingUrlProtocolException(String message) {
		super(message);
	}
}
