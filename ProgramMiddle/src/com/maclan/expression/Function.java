package com.maclan.expression;

import java.lang.reflect.*;
import java.util.*;

import org.multipage.util.*;

/**
 * 
 * @author
 *
 */
public class Function implements ExpressionElement, ObjectElement {
	
	/**
	 * Embedded function names.
	 */
	private static final String conditionalFunctionName = "if";
	private static final String parseFunctionName = "parse";
	private static final String evaluationFunctionName = "evaluate";
	
	/**
	 * Module identifiers.
	 */
	private static final int NO_MODULE = 0;
	private static final int MATHEMATICS = 1;
	private static final int TEXT_PROCESSING = 2;

	/**
	 * Name.
	 */
	private String name;
	
	/**
	 * Function parameters.
	 */
	private ArrayList<ExpressionElement> parameters;
	
	/**
	 * This object reference.
	 */
	private Object thisObject;
	
	/**
	 * Constructor.
	 */
	public Function(String name, ArrayList<ExpressionElement> parameters) {
		
		this.name = name;
		this.parameters = parameters;
	}

	/**
	 * @param thisObject the thisObject to set
	 */
	@Override
	public void setThisObject(Object thisObject) {
		this.thisObject = thisObject;
	}

	/**
	 * Get child count.
	 */
	@Override
	public int getChildCount() {

		return parameters.size();
	}

	/**
	 * Get child.
	 */
	@Override
	public ExpressionElement getChild(int index) {
		
		if (index >= 0 && index < parameters.size()) {
			return parameters.get(index);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s(#%d)", name, parameters.size());
	}

	/**
	 * Adjust value.
	 * @param module
	 * @param value
	 * @return
	 */
	private Object adjustValue(Integer module, Object value) {

		if (module == MATHEMATICS) {
			if (value instanceof Long) {
				return ((Long) value).doubleValue();
			}
		}
		if (module == TEXT_PROCESSING) {
			
			if (value instanceof Double) {
				return ((Double) value).intValue();
			}
			if (value instanceof Long) {
				return ((Long) value).intValue();
			}
		}
		return value;
	}

	/**
	 * Adjust type.
	 * @param detectedClass 
	 * @param type
	 * @return
	 */
	private Class<?> adjustType(Class<? extends Object> type) {

		if (type == Double.class) {
			return double.class;
		}
		if (type == Long.class) {
			return long.class;
		}
		if (type == Boolean.class) {
			return boolean.class;
		}
		if (type == Integer.class) {
			return int.class;
		}

		return type;
	}

	/**
	 * Adjust result.
	 * @param result
	 * @return
	 */
	private Object adjustResult(Object result) {
		
		if (result instanceof Byte) {
			return ((Byte) result).longValue();
		}
		if (result instanceof Short) {
			return ((Short) result).longValue();
		}
		if (result instanceof Integer) {
			return ((Integer) result).longValue();
		}
		if (result instanceof Float) {
			return ((Float) result).doubleValue();
		}
		if (result instanceof Character) {
			return ((Character) result).toString();
		}

		return result;
	}

	/**
	 * Return function result.
	 */
	@Override
	public Object getValueObject(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		// Try to use embedded functions.
		Obj<Object> embeddedResult = new Obj<Object>();
		
		if (useEmbeddedFunctions(identifierSolver, functionSolver,
				embeddedResult)) {
			return embeddedResult.ref;
		}
		
		int module = NO_MODULE;
		if (thisObject instanceof Class<?>) {
			if ((Class<?>) thisObject == Math.class) {
				module = MATHEMATICS;
			}
		}
		else if (thisObject instanceof String) {
			module = TEXT_PROCESSING;
		}
		
		// Resolve operands.
		int parameterCount = parameters.size();
		Object [] parameterValues = new Object [parameterCount];
		Class<?> [] parameterTypes = new Class [parameterCount];
		
		for (int index = 0; index < parameterCount; index++) {
			
			// Get parameter abstract syntax sub tree.
			ExpressionElement parameter = parameters.get(index);
			
			Object parameterObject = null;
			
			// Resolve parameter.
			if (parameter instanceof BinaryOperator) {
				BinaryOperator binaryOperator = (BinaryOperator) parameter;
				
				// If it is a "procedure parameter" operator...
				if (binaryOperator.getOperator() == ExpressionSolver.PROCEDURE_PARAMETER) {
					
					ProcedureParameter procedureParameter = new ProcedureParameter();
					
					// Left parameter must be an identifier.
					ExpressionElement leftElement = binaryOperator.getLeft();
					String procedureParameterName = null;
					
					if (leftElement instanceof Operand) {
						Operand leftOperand = (Operand) leftElement;
						
						if (leftOperand.getKind() == ExpressionSolver.IDENTIFIER) {
							procedureParameterName = leftOperand.getName();
						}
					}
					
					if (procedureParameterName == null) {
						throw new EvaluateException(this, Resources.getString("middle.messageLeftPartOfrocedureParameterMustBeName"));
					}
					
					procedureParameter.setName(procedureParameterName);
					
					// Right element must be an object identifier or a value.
					ExpressionElement rightElement = binaryOperator.getRight();
					
					String rightElementObjectIdentifier = Operand.getObjectIdentifierName(rightElement);
					if (rightElementObjectIdentifier != null) {
						procedureParameter.setOutputObjectIdentifier(rightElementObjectIdentifier);
					}
					
					// Get right value.
					Object rightValue = null;
					
					try {
						rightValue = rightElement.getValueObject(identifierSolver, functionSolver);
						procedureParameter.setInputValue(rightValue);
					}
					catch (Exception e) {
						
						// If the right element is not an object identifier, throw caught exception.
						if (rightElementObjectIdentifier == null) {
							throw e;
						}
					}
					
					// Set parameter object.
					parameterObject = procedureParameter;
				}
			}
			
			// If a parameter object is not set, set it.
			if (parameterObject == null) {
				parameterObject = parameter.getValueObject(identifierSolver, functionSolver);
			}
			
			// Adjust value.
			parameterValues[index] = adjustValue(module, parameterObject);
			// Get parameter type.
			if (parameterValues[index] != null) {
				parameterTypes[index] = adjustType(parameterValues[index].getClass());
			}
		}
		
		try {
			// If it is text processing module.
			if (module == TEXT_PROCESSING) {
				// Try to call String object method.
				Method method = String.class.getDeclaredMethod(name, parameterTypes);
				return adjustResult(method.invoke(thisObject, parameterValues));
			}
			// If this is the mathematics module.
			if (module == MATHEMATICS) {
				// Try to call static Math method.
				Method method = Math.class.getDeclaredMethod(name, parameterTypes);
				return adjustResult(method.invoke(null, parameterValues));
			}
		}
		catch (InvocationTargetException e) {
			throw new EvaluateException(this, e.getTargetException().getLocalizedMessage());
		}
		catch (Exception e) {
		}

		// Solve method.
		return functionSolver.getValue(thisObject, name, parameterValues);
	}

	/**
	 * Use embedded functions.
	 * @param identifierSolver
	 * @param functionSolver
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private boolean useEmbeddedFunctions(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<Object> result) throws Exception {
		
		if (thisObject == null) {
			if (conditionalFunction(identifierSolver, functionSolver, result)) {
				return true;
			}
			if (parseFunction(identifierSolver, functionSolver, result)) {
				return true;
			}
			if (evaluateFunction(identifierSolver, functionSolver, result)) {
				return true;
			}

		}
		else {
			if (formatStringFunction(identifierSolver, functionSolver, result)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Use parse function.
	 * @param identifierSolver
	 * @param functionSolver
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private boolean parseFunction(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<Object> result) throws Exception {
		
		// If it is parse function.
		if (name.equals(parseFunctionName)) {
			// Check parameter.
			if (parameters.size() == 1) {
				
				// Evaluate parameter.
				ExpressionElement parameterElement = parameters.get(0);
				Object resultObject = parameterElement.getValueObject(identifierSolver,
						functionSolver);
				if (resultObject instanceof String) {
					
					String text = (String) resultObject;
					// Try to get value.
					if (text.equals(ExpressionSolver.trueValueConstant)) {
						result.ref = true;
						return true;
					}
					if (text.equals(ExpressionSolver.falseValueConstant)) {
						result.ref = false;
						return true;
					}
					try {
						result.ref = Long.parseLong(text);
						return true;
					}
					catch (Exception e) {
					}
					try {
						result.ref = Double.parseDouble(text);
						return true;
					}
					catch (Exception e) {
					}
				}
				
				result.ref = resultObject;
				return true;
			}
			
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingTextParameter"));
		}
		
		return false;

	}

	/**
	 * Use conditional function.
	 * @param identifierSolver
	 * @param functionSolver
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private boolean conditionalFunction(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<Object> result) throws Exception {
		
		// If it is a conditional function.
		if (name.equals(conditionalFunctionName)) {
			// Check number of parameters.
			if (parameters.size() == 3) {
				
				// Evaluate parameters.
				ExpressionElement conditionElement = parameters.get(0);
				Object condition = conditionElement.getValueObject(identifierSolver,
						functionSolver);
				if (condition instanceof Boolean) {
						
					// Evaluate second or third parameter.
					result.ref = parameters.get((Boolean) condition ? 1 : 2).getValueObject(
							identifierSolver, functionSolver);
					return true;
				}
				
				throw new EvaluateException(this, Resources.getString("middle.messageExpectingBooleanCondition"));
			}
			
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingThreeParametersWithCondition"));
		}
		
		return false;
	}

	/**
	 * Evaluate function.
	 * @param identifierSolver
	 * @param functionSolver
	 * @param result
	 * @return
	 */
	private boolean evaluateFunction(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<Object> result) throws Exception {
		
		// If it is evaluation function.
		if (name.equals(evaluationFunctionName)) {
			// Check number of parameters.
			if (parameters.size() == 1) {
				
				// Evaluate parameter.
				ExpressionElement parameterElement = parameters.get(0);
				Object parameterObject = parameterElement.getValueObject(
						identifierSolver, functionSolver);
				if (parameterObject instanceof String) {
					
					String expression = (String) parameterObject;
					result.ref = ExpressionSolver.evaluate(expression, identifierSolver, functionSolver, null);
					return true;
				}
			}
			throw new EvaluateException(this, Resources.getString("middle.messageExpectingTextParameter2"));
		}
		
		return false;
	}

	/**
	 * Format string function.
	 * @param identifierSolver
	 * @param functionSolver
	 * @param result
	 * @return
	 */
	private boolean formatStringFunction(IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<Object> result) throws Exception {
		
		// Check this object.
		if (!(thisObject instanceof Class<?>)) {
			return false;
		}
		if (!((Class<?>) thisObject).equals(String.class)) {
			return false;
		}
		
		int parametersCount = parameters.size();
		
		// Check parameters.
		if (parametersCount < 1) {
			throw new EvaluateException(this, 
					Resources.getString("middle.messageExpectingAtLeastOneString"));
		}
		// Get format.
		Object formatObject = parameters.get(0).getValueObject(identifierSolver,
				functionSolver);
		if (!(formatObject instanceof String)) {
			throw new EvaluateException(this, 
					Resources.getString("middle.messageFormatParameterNotText"));
		}
		
		// Create format command parameters.
		Object [] formatParameters = new Object [parametersCount - 1];
		for (int index = 0; index < parametersCount - 1; index++) {
			
			formatParameters[index] = parameters.get(index + 1).getValueObject(
					identifierSolver, functionSolver);
		}
		
		// Call format function.
		try {
			result.ref = String.format((String) formatObject, formatParameters);
		}
		catch (Exception e) {
			throw new EvaluateException(this, e.getLocalizedMessage());
		}

		return true;
	}
}