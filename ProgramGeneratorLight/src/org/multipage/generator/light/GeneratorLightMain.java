/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.generator.light;

import org.multipage.generator.GeneratorMain;


/**
 * @author
 *
 */
public class GeneratorLightMain {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Use Derby middle layer with extensions.
		GeneratorMain.main("Multipage Generator Standalone", args, "org.multipage.derby", false);
	}
}
