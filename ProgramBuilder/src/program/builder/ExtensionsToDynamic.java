/*
 * Copyright 2017 (C) multipage-software.org
 * 
 * Created on : 26-04-2017
 *
 */

package program.builder;

import org.multipage.generator.AreasProperties;
import org.multipage.generator.GeneratorMainFrame;
import org.maclan.AreasModel;
import org.maclan.Middle;
import org.multipage.gui.StateSerializer;

/**
 * 
 * @author
 *
 */
public interface ExtensionsToDynamic {

	/**
	 * Initialize dynamic level.
	 * @param language
	 * @param country
	 * @param serializer
	 * @return
	 */
	boolean initializeLevel(String language, String country,
			StateSerializer serializer);


	/**
	 * Create new main frame.
	 * @return
	 */
	GeneratorMainFrame newMainFrame();

	/**
	 * Get middle.
	 * @return
	 */
	Middle getMiddle();

	/**
	 * Create new areas model.
	 * @return
	 */
	AreasModel newAreasModel();
}
