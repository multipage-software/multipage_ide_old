/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator.full;

import org.multipage.generator.GeneratorMain;


/**
 * @author
 *
 */
public class GeneratorFullMain {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Use Derby middle layer.
		GeneratorMain.main("Multipage Generator Network", args, "org.maclan.postgresql", true);
	}
}
