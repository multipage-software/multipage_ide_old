package com.maclan.expression;

/**
 * Operator element.
 * @author
 *
 */
public interface ExpressionElement {

	/**
	 * Get expression element name.
	 * @return
	 */
	String toString();

	/**
	 * Get child count.
	 * @return
	 */
	int getChildCount();

	/**
	 * Get child.
	 * @return
	 */
	ExpressionElement getChild(int index);

	/**
	 * Evaluates this expression element.
	 * @param identifierSolver
	 * @return
	 * @throws Exception
	 */
	Object getValueObject(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver)  throws Exception;
}