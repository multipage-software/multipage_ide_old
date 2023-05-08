/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder.light;

import program.builder.BuilderMain;

/**
 * @author
 *
 */
public class BuilderLightMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Use Derby middle layer.
		BuilderMain.main("Multipage Builder Standalone", args, "org.multipage.derby", false, null);
	}
}
