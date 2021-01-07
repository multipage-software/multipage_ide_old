/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.Properties;

/**
 * Token.
 */
class Token {

	/**
	 * Token type.
	 */
	int type;
	
	/**
	 * Token start.
	 */
	int start;
	
	/**
	 * Token end.
	 */
	int end;
	
	/**
	 * Properties.
	 */
	Properties properties = new Properties();
	
	/**
	 * Constructor.
	 * @param type
	 */
	public Token(int type) {

		this.type = type;
	}

	/**
	 * Constructor.
	 * @param type
	 * @param start
	 * @param end
	 */
	public Token(int type, int start, int end) {
		
		this.type = type;
		this.start = start;
		this.end = end;
	}

	/**
	 * Constructor.
	 * @param type
	 * @param start
	 * @param end
	 * @param properties 
	 * @param ref 
	 */
	public Token(int type, int start, int end, Properties properties) {

		this(type, start, end);

		this.properties = properties;
	}
}