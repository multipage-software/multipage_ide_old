/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder.full;

import program.builder.BuilderMain;

/**
 * @author
 *
 */
public class BuilderFullMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Use PostgreSQL middle layer.
		BuilderMain.main("Multipage Builder Network", args, "com.maclan.postgresql", true, null);
	}
}
