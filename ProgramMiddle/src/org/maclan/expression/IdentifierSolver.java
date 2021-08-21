/**
 * 
 */
package org.maclan.expression;

import org.maclan.server.AreaServer;
import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class IdentifierSolver {
	
	/**
	 * Classes constants.
	 */
	private static final String classConstantMath = "math";
	private static final String classConstantText = "text";
	
	/**
	 * Get identifier value.
	 * @param thisObject 
	 * @param name
	 * @return
	 */
	public Object getValue(Object thisObject, String name) throws Exception {
		
		// If this object is null.
		if (thisObject == null) {
			if (name.equals(classConstantMath)) {
				return Math.class;
			}
			if (name.equals(classConstantText)) {
				return String.class;
			}
		}

		throw new Exception(String.format(
				Resources.getString("middle.messageUnknownIdentifier"), name));
	}

	/**
	 * Get area server.
	 * @return
	 */
	public AreaServer getAreaServer() {
		
		return null;
	}
}
