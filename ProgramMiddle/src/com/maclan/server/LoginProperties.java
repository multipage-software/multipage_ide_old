/*
 * Copyright 2010-2017 (C) sechance
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.*;

/**
 * @author
 *
 */
public class LoginProperties extends Properties {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public LoginProperties() {
		
		put("server", "localhost");
		put("port", "5432");
		put("ssl", "false");
		put("username", "administrator");
		put("password", "1");
	}
}
