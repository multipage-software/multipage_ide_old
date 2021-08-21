/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.*;

import org.maclan.*;

/**
 * 
 * @author
 *
 */
public class LanguagesBlockDescriptor extends BlockDescriptor {

	/**
	 * Languages list.
	 */
	@SuppressWarnings("unused")
	private LinkedList<Language> languages;
	
	/**
	 * Index.
	 */
	@SuppressWarnings("unused")
	private long index;
	
	/**
	 * Constructor.
	 * @param languages
	 */
	public LanguagesBlockDescriptor(LinkedList<Language> languages) {

		this.languages = languages;
		index = 1;
	}

	/**
	 * Set index.
	 * @param index
	 */
	public void setIndex(int index) {

		this.index = index;
	}
}
