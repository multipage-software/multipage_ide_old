package com.maclan.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.multipage.util.Resources;

/**
 * Unary operator.
 * @author
 *
 **/
public class UnaryOperator implements ExpressionElement {

	/**
	 * Unary operators with precedence.
	 */
	private static final Object operators[][] = {
		
		  { ExpressionSolver.BOOLEAN_NEGATION, 10, "booleanNegation" },
		  { ExpressionSolver.UNARY_MINUS, 10, "unaryMinus" },
		  { ExpressionSolver.UNARY_PLUS, 10, "unaryPlus" }
	};

	/**
	 * Returns true value if the operator is an unary operator.
	 * @param operator
	 * @return
	 */
	public static boolean isOperator(int operator) {
		
		for (Object [] operatorItem : operators) {
			if ((Integer) operatorItem[0] == operator) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Get precedence.
	 * @param operators
	 * @param operator
	 * @return
	 */
	public static Integer getPrecedence(int operator) {

		for (Object [] operatorItem : operators) {
			if ((Integer) operatorItem[0] == operator) {
				return (Integer) operatorItem[1];
			}
		}
		
		return null;
	}

	/**
	 * Get method name.
	 * @return
	 */
	private String getMethodName() {
		
		for (Object [] operatorItem : operators) {
			if ((Integer) operatorItem[0] == operator) {
				return (String) operatorItem[2];
			}
		}
		
		return null;
	}
	
	/**
	 * Operator identifier.
	 */
	private int operator;

	/**
	 * Right element.
	 */
	private ExpressionElement right;
	
	/**
	 * Constructor.
	 * @param operator
	 * @param right
	 */
	public UnaryOperator(int operator, ExpressionElement right) {

		this.operator = operator;
		this.right = right;
	}

	/**
	 * Get name.
	 */
	@Override
	public String toString() {
		
		return ExpressionSolver.getOperatorName(operator);
	}

	/**
	 * Get child count.
	 */
	@Override
	public int getChildCount() {

		return 1;
	}

	/**
	 * Get child.
	 */
	@Override
	public ExpressionElement getChild(int index) {

		if (index == 0) {
			return right;
		}
		return null;
	}

	/**
	 * Get value of the unary operator.
	 */
	@Override
	public Object getValueObject(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		// Get right operand.
		Object rightOperand = right.getValueObject(identifierSolver,
				functionSolver);
		
		// Get operator method name.
		String methodName = getMethodName();

		// Call binary operator.
		// Find object's method and invoke it.
		try {
			Method method = UnaryOperator.class.getMethod(methodName, Object.class);
			Object result = method.invoke(this, rightOperand);
			return result;
						
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SecurityException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new Exception(e.getTargetException().getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			throw e;
		}
	}
	
	/**
	 * Boolean AND.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean booleanNegation(Object rightValue) throws Exception {
		
		if (!(rightValue instanceof Boolean)) {
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingBooleanValue"));
		}
		
		return ! (Boolean) rightValue;
	}
	
	/**
	 * Unary minus.
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object unaryMinus(Object rightValue) throws Exception {
		
		if (rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageUnaryMinusNullOperand"));
		}
		if (rightValue instanceof Long) {
			return - (Long) rightValue;
		}
		if (rightValue instanceof Double) {
			return - (Double) rightValue;
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageUnaryMinusBadOperand"));
	}
	
	/**
	 * Unary plus.
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object unaryPlus(Object rightValue) throws Exception {
		
		if (rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageUnaryPlusNullOperand"));
		}
		if (rightValue instanceof Long || rightValue instanceof Double) {
			return rightValue;
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageUnaryMinusBadOperand"));
	}
}