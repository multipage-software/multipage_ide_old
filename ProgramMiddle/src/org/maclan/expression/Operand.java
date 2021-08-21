package org.maclan.expression;

import org.multipage.util.Resources;

/**
 * Operand.
 * @author
 *
 */
public class Operand implements ExpressionElement, ObjectElement {

	/**
	 * Name.
	 */
	private String name;
	
	/**
	 * Operand kind.
	 */
	private int kind;
	
	/**
	 * This object reference.
	 */
	private Object thisObject;
	
	/**
	 * Constructor.
	 * @param name
	 * @param kind 
	 */
	public Operand(String name, int kind) {
		
		// Trim string.
		if (kind == ExpressionSolver.STRING_LITERAL) {
			name = name.substring(1, name.length() - 1);
			
			// Replace escapes.
			name = name.replaceAll("\\\\n", "\n");
			name = name.replaceAll("\\\\t", "\t");
			name = name.replaceAll("\\\\b", "\b");
			name = name.replaceAll("\\\\r", "\r");
			name = name.replaceAll("\\\\f", "\f");
			name = name.replaceAll("\\\\\'", "'");
			name = name.replaceAll("\\\\\"", "\"");
			name = name.replaceAll("\\\\\\\\", "\\\\");
		}

		this.name = name;
		this.kind = kind;
	}

	/**
	 * Get name.
	 */
	@Override
	public String toString() {

		return name;
	}

	/**
	 * Get child count.
	 */
	@Override
	public int getChildCount() {

		return 0;
	}

	/**
	 * Get child.
	 */
	@Override
	public ExpressionElement getChild(int index) {

		return null;
	}

	/**
	 * Get value.
	 * @return
	 * @throws Exception 
	 */
	@Override
	public Object getValueObject(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		switch (kind) {
		
		case ExpressionSolver.NULL_LITERAL:
			return null;
		
		case ExpressionSolver.BOOLEAN_LITERAL:
			if (name.equals(ExpressionSolver.trueValueConstant)) {
				return true;
			}
			else if (name.equals(ExpressionSolver.falseValueConstant)) {
				return false;
			}
			throw new EvaluateException(this, Resources.getString("middle.messageUnexpectedBooleanValue"));
			
		case ExpressionSolver.INTEGER_LITERAL:
			return Long.parseLong(name);
			
		case ExpressionSolver.FLOATING_POINT_LITERAL:
			return Double.parseDouble(name);
			
		case ExpressionSolver.STRING_LITERAL:
			return name;
		case ExpressionSolver.IDENTIFIER:
			// If it is PI.
			if (name.equals("PI")) {
				return Math.PI;
			}
			// If it is e.
			if (name.equals("E")) {
				return Math.E;
			}
			// Get identifier value.
			return identifierSolver.getValue(thisObject, name);
			
		case ExpressionSolver.TYPE_IDENTIFIER:
			kind = ExpressionSolver.STRING_LITERAL;
			return name;
		}

		throw new EvaluateException(this, Resources.getString("middle.messageUnknownValueType"));
	}

	/**
	 * @param thisObject the thisObject to set
	 */
	@Override
	public void setThisObject(Object thisObject) {
		this.thisObject = thisObject;
	}

	/**
	 * Get kind.
	 * @return
	 */
	public int getKind() {
		
		return kind;
	}

	/**
	 * Get name.
	 * @return
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Get object identifier name.
	 * @param element
	 * @return
	 */
	public static String getObjectIdentifierName(ExpressionElement element) {
		
		// If the element is an identifier, return its name.
		if (element instanceof Operand) {
			
			Operand operand = (Operand) element;
			if (operand.kind == ExpressionSolver.IDENTIFIER) {
				
				return operand.name;
			}
			
			return null;
		}
		
		// If the element is a member access operator...
		if (element instanceof BinaryOperator) {
			
			BinaryOperator operator = (BinaryOperator) element;
			if (operator.getOperator() == ExpressionSolver.MEMBER_ACCESS) {
				
				// Get left part. (Call this method recursively.)
				ExpressionElement leftElement = operator.getLeft();
				String leftName = getObjectIdentifierName(leftElement);
				
				// Get right part name.
				String rightName = null;
				ExpressionElement rightElement = operator.getRight();
				
				if (rightElement instanceof Operand) {
					Operand rightOperand = (Operand) rightElement;
					
					if (rightOperand.kind == ExpressionSolver.IDENTIFIER) {
						rightName = rightOperand.name;
					}
				}
				
				// If right name is null, exit the method.
				if (rightName == null) {
					return null;
				}

				// Return partial name.
				return leftName + '.' + rightName;
			}
		}
		
		return null;
	}
}