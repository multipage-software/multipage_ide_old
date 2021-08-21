/**
 * 
 */
package org.maclan.expression;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class EvaluateException extends Exception {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public EvaluateException(ExpressionElement element, String message) {
		super(String.format(Resources.getString("middle.messageEvaluateException"),
				element.toString(), message));
	}
}
