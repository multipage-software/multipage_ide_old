package org.maclan.expression;

import java.lang.reflect.*;

import org.maclan.Slot;
import org.maclan.server.AreaServer;
import org.multipage.util.*;

/**
 * Binary operator.
 * @author
 *
 **/
public class BinaryOperator implements ExpressionElement {
	
	/**
	 * Binary operators with precedence.
	 */
	private static final Object operators[][] = {
		
		  { ExpressionSolver.PROCEDURE_PARAMETER, 1, "procedureParameter" },
		  { ExpressionSolver.BOOLEAN_OR, 2, "booleanOr" },
		  { ExpressionSolver.BOOLEAN_AND, 3, "booleanAnd" },
		  { ExpressionSolver.BOOLEAN_XOR, 4, "booleanXor" },
		  { ExpressionSolver.NOT_EQUAL, 5, "notEqual" },
		  { ExpressionSolver.EQUAL, 5, "equal" },
		  { ExpressionSolver.IS_OPRATOR, 5, "isOperator" },
		  { ExpressionSolver.GREATER, 6, "greater" },
		  { ExpressionSolver.GREATER_EQUAL, 6, "greaterEqual" },
		  { ExpressionSolver.LESS, 6, "less" },
		  { ExpressionSolver.LESS_EQUAL, 6, "lessEqual" },
		  { ExpressionSolver.PLUS, 7, "addition" },
		  { ExpressionSolver.MINUS, 7, "subtraction" },
		  { ExpressionSolver.MULTIPLICATION, 8, "multiplication" },
		  { ExpressionSolver.DIVISION, 8, "division" },
		  { ExpressionSolver.AS_OPERATOR, 9, "asOperator" },
		  { ExpressionSolver.REMAINDER, 10, "remainder" },
		  { ExpressionSolver.MEMBER_ACCESS, 12, "" }
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
	 * Left element.
	 */
	private ExpressionElement left;
	
	/**
	 * Right element.
	 */
	private ExpressionElement right;

	/**
	 * Area server reference.
	 */
	private AreaServer server;
	
	/**
	 * Constructor.
	 */
	public BinaryOperator(int operator, ExpressionElement left,
			ExpressionElement right) {
		
		this.operator = operator;
		this.left = left;
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

		return 2;
	}

	/**
	 * Get child.
	 */
	@Override
	public ExpressionElement getChild(int index) {
		
		switch (index) {
		case 0:
			return left;
		case 1:
			return right;
		}

		return null;
	}
	
	/**
	 * Evaluate operator.
	 */
	@Override
	public Object getValueObject(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		// Check operands.
		if (left == null || right == null) {
			return null;
		}

		// If it is a member access.
		if (operator == ExpressionSolver.MEMBER_ACCESS) {
			return memberAccess(identifierSolver, functionSolver);
		}
		
		// Process left operand.
		Object leftOperand = left.getValueObject(identifierSolver,
				functionSolver);
		
		// Do OR and AND optimization.
		if (operator == ExpressionSolver.BOOLEAN_OR
			|| operator == ExpressionSolver.BOOLEAN_AND) {
		
			if (leftOperand instanceof Boolean) {
			
				boolean leftBoolean = (Boolean) leftOperand;
				
				if (operator == ExpressionSolver.BOOLEAN_OR && leftBoolean) {
					return true;
				}
				if (operator == ExpressionSolver.BOOLEAN_AND && !leftBoolean) {
					return false;
				}
			}
			else {
				throw new EvaluateException(this, Resources.getString("middle.messageExpectingLeftBooleanValue"));
			}
		}
		
		// Process right operand.
		Object rightOperand = right.getValueObject(identifierSolver,
				functionSolver);
		
		// Set middle layer reference.
		server = identifierSolver.getAreaServer();
		
		// Get operator method name.
		String methodName = getMethodName();

		// Call binary operator.
		// Find object's method and invoke it.
		try {
			Method method = BinaryOperator.class.getMethod(methodName, Object.class, Object.class);
			Object result = method.invoke(this, leftOperand, rightOperand);
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
	 * Member access.
	 * @param identifierSolver
	 * @param functionSolver
	 * @return
	 */
	private Object memberAccess(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		// Evaluate left object.
		Object leftOperand = left.getValueObject(identifierSolver,
				functionSolver);
		// Set right operand this object.
		if (right instanceof ObjectElement) {
			((ObjectElement) right).setThisObject(leftOperand);
		}
		
		// Evaluate right object.
		Object rightOperand = right.getValueObject(identifierSolver,
				functionSolver);

		return rightOperand;
	}

	/**
	 * Boolean OR.
	 * @throws Exception 
	 */
	public boolean booleanOr(Object leftValue, Object rightValue) throws Exception {
		
		if (!(leftValue instanceof Boolean && rightValue instanceof Boolean)) {
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingBooleanValues"));
		}
		
		return (Boolean) leftValue || (Boolean) rightValue;
	}
	
	/**
	 * Boolean AND.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean booleanAnd(Object leftValue, Object rightValue) throws Exception {
		
		if (!(leftValue instanceof Boolean && rightValue instanceof Boolean)) {
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingBooleanValues"));
		}
		
		return (Boolean) leftValue && (Boolean) rightValue;
	}
	
	/**
	 * Boolean XOR.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean booleanXor(Object leftValue, Object rightValue) throws Exception {
		
		if (!(leftValue instanceof Boolean && rightValue instanceof Boolean)) {
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingBooleanValues"));
		}
		
		return (Boolean) leftValue ^ (Boolean) rightValue;
	}
	
	/**
	 * Not equal.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean notEqual(Object leftValue, Object rightValue) throws Exception {

		return !equal(leftValue, rightValue);
	}
	
	/**
	 * Is equal.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean equal(Object leftValue, Object rightValue) throws Exception {

		if (leftValue == null && rightValue == null) {
			return true;
		}
		if (leftValue == null || rightValue == null) {
			return false;
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			return leftValue.equals(rightValue);
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() == (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue == ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageCannotCompareOperands"));
	}
	
	/**
	 * Is greater.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean greater(Object leftValue, Object rightValue) throws Exception {

		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageCannotCompareNullOperand"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			if (leftValue instanceof Long) {
				return (Long) leftValue > (Long) rightValue;
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue > (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() > (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue > ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageCannotCompareOperands"));
	}
	
	/**
	 * Is greater or equal.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean greaterEqual(Object leftValue, Object rightValue) throws Exception {

		return greater(leftValue, rightValue) || equal(leftValue, rightValue);
	}
	
	/**
	 * Is less.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean less(Object leftValue, Object rightValue) throws Exception {

		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageCannotCompareNullOperand"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			if (leftValue instanceof Long) {
				return (Long) leftValue < (Long) rightValue;
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue < (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() < (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue < ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageCannotCompareOperands"));
	}
	
	/**
	 * Is less or equal.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public boolean lessEqual(Object leftValue, Object rightValue) throws Exception {
	
		return less(leftValue, rightValue) || equal(leftValue, rightValue);
	}
	
	/**
	 * Addition.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object addition(Object leftValue, Object rightValue) throws Exception {
		
		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageNullOperandAddition"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			
			if (leftValue instanceof Long) {
				return (Long) leftValue + (Long) rightValue;
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue + (Double) rightValue;
			}
			else if (leftValue instanceof String) {
				return (String) leftValue + (String) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() + (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue + ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, String.format(
				Resources.getString("middle.messageAdditionOperandNotNumberOrText"),
				leftValue.getClass().toString(), rightValue.getClass().toString()));
	}
	
	/**
	 * Subtraction.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object subtraction(Object leftValue, Object rightValue) throws Exception {
		
		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageNullOperandSubtraction"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			
			if (leftValue instanceof Long) {
				return (Long) leftValue - (Long) rightValue;
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue - (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() - (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue - ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageSubtractionOperandNotNumber"));
	}
	
	/**
	 * Multiplication.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object multiplication(Object leftValue, Object rightValue) throws Exception {
		
		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageNullOperandMultiplication"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			
			if (leftValue instanceof Long) {
				return (Long) leftValue * (Long) rightValue;
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue * (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() * (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue * ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageMultiplicationOperandNotNumber"));
	}
	
	/**
	 * Division.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object division(Object leftValue, Object rightValue) throws Exception {
		
		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageNullOperandDivision"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			
			if (leftValue instanceof Long) {
				return ((Long) leftValue).doubleValue() / ((Long) rightValue).doubleValue();
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue / (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() / (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue / ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageDivisionOperandNotNumber"));
	}
	
	/**
	 * Remainder.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object remainder(Object leftValue, Object rightValue) throws Exception {
		
		if (leftValue == null || rightValue == null) {
			throw new EvaluateException(this, Resources.getString("middle.messageNullOperandRemainder"));
		}
		if (leftValue.getClass() == rightValue.getClass()) {
			
			if (leftValue instanceof Long) {
				return ((Long) leftValue).doubleValue() % ((Long) rightValue).doubleValue();
			}
			else if (leftValue instanceof Double) {
				return (Double) leftValue % (Double) rightValue;
			}
		}
		if (leftValue instanceof Long && rightValue instanceof Double) {
			return ((Long) leftValue).doubleValue() % (Double) rightValue;
		}
		if (leftValue instanceof Double && rightValue instanceof Long) {
			return (Double) leftValue % ((Long) rightValue).doubleValue();
		}
		
		throw new EvaluateException(this, Resources.getString("middle.messageRemainderOperandNotNumber"));
	}
	
	/**
	 * AS operator.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object asOperator(Object leftValue, Object rightValue) throws Exception {
		
		// Check input.
		if (!(rightValue instanceof String)) {
			throw new EvaluateException(this, Resources.getString("middle.messageUnknownType"));
		}
		if (leftValue == null) {
			return null;
		}
		
		// Create exception object.
		Exception exception = new EvaluateException(this, Resources.getString("middle.messageCannotCastValue"));
		
		String type = (String) rightValue;
		if (type.equals(ExpressionSolver.stringTypeName)) {
			
			if (server != null && leftValue instanceof Slot) {
				
				Slot slot = (Slot) leftValue;
				server.loadSlotValue(slot);
			}
			return leftValue.toString();
		}
		if (type.equals(ExpressionSolver.booleanTypeName)) {
			if (leftValue instanceof Boolean) {
				return leftValue;
			}
			if (leftValue instanceof String) {
				String text = (String) leftValue;
				if (text.equals(ExpressionSolver.trueValueConstant)) {
					return true;
				}
				if (text.equals(ExpressionSolver.falseValueConstant)) {
					return false;
				}
			}
			if (leftValue instanceof Long) {
				Long longValue = (Long) leftValue;
				if (longValue == 1L) {
					return true;
				}
				if (longValue == 0L) {
					return false;
				}
			}
			if (leftValue instanceof Double) {
				Double doubleValue = (Double) leftValue;
				if (doubleValue == 1.0) {
					return true;
				}
				if (doubleValue == 0.0) {
					return false;
				}
			}
		}
		if (type.equals(ExpressionSolver.longTypeName)) {
			if (leftValue instanceof Long) {
				return leftValue;
			}
			if (leftValue instanceof String) {
				try {
					return Long.parseLong((String) leftValue);
				}
				catch (Exception e) {
					throw exception;
				}
			}
			if (leftValue instanceof Double) {
				return ((Double) leftValue).longValue();
			}
			if (leftValue instanceof Boolean) {
				return ((Boolean) leftValue) ? 1L : 0L;
			}
		}
		if (type.equals(ExpressionSolver.doubleTypeName)) {
			if (leftValue instanceof Double) {
				return leftValue;
			}
			if (leftValue instanceof String) {
				try {
					return Double.parseDouble((String) leftValue);
				}
				catch (Exception e) {
					throw exception;
				}
			}
			if (leftValue instanceof Long) {
				return ((Long) leftValue).doubleValue();
			}
			if (leftValue instanceof Boolean) {
				return ((Boolean) leftValue) ? 1.0 : 0.0;
			}
		}
	
		throw exception;
	}
	
	/**
	 * IS operator.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object isOperator(Object leftValue, Object rightValue) throws Exception {
		
		Exception exception = new EvaluateException(this, Resources.getString("middle.messageUnknownType"));
		// Check input.
		if (!(rightValue instanceof String)) {
			throw exception;
		}
		if (leftValue == null) {
			return null;
		}
		
		String type = (String) rightValue;

		if (type.equals(ExpressionSolver.booleanTypeName)) {
			return leftValue instanceof Boolean;
		}
		if (type.equals(ExpressionSolver.stringTypeName)) {
			return leftValue instanceof String;
		}
		if (type.equals(ExpressionSolver.longTypeName)) {
			return leftValue instanceof Long;
		}
		if (type.equals(ExpressionSolver.doubleTypeName)) {
			return leftValue instanceof Double;
		}
		
		throw exception;
	}

	/**
	 * Procedure parameter operator returns right value. Herein the left value is omitted but it is used in a function resolver.
	 * @param leftValue
	 * @param rightValue
	 * @return
	 * @throws Exception
	 */
	public Object procedureParameter(Object leftValue, Object rightValue) throws Exception {
		
		return rightValue;
	}
	
	/**
	 * Get operator.
	 * @return
	 */
	public int getOperator() {
		
		return operator;
	}

	/**
	 * Get left element.
	 * @return
	 */
	public ExpressionElement getLeft() {
		
		return left;
	}

	/**
	 * Get right element.
	 * @return
	 */
	public ExpressionElement getRight() {
		
		return right;
	}
}