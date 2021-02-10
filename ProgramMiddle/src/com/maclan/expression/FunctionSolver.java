/**
 * 
 */
package com.maclan.expression;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class FunctionSolver {

	/**
	 * Get identifier value.
	 * @param thisObject 
	 * @param name
	 * @return
	 */
	public Object getValue(Object thisObject, String name, Object [] parameters) throws Exception {

		throw new Exception(String.format(
				Resources.getString("middle.messageUnknownFunction"), name, parameters.length));
	}
}
