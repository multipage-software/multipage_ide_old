package com.maclan.expression;

import java.io.*;
import java.util.*;

import org.multipage.util.Obj;
import org.multipage.util.Resources;

/**
 * 
 * @author
 *
 */
public class ExpressionSolver extends ExpressionParser {

	/**
	 * Stack sentinel.
	 */
	private static final int SENTINEL = -1;

	/**
	 * Operators.
	 */
	public static final int UNARY_MINUS = -2;
	public static final int UNARY_PLUS = -3;

	/**
	 * Lookup table.
	 */
	private static final int[][] lookupTable = {
		
			{ MINUS, UNARY_MINUS },
			{ PLUS, UNARY_PLUS }
	};

	/**
	 * Types.
	 */
	public static final String booleanTypeName = "Boolean";
	public static final String longTypeName = "Long";
	public static final String doubleTypeName = "Double";
	public static final String stringTypeName = "Text";

	/**
	 * Boolean constants.
	 */
	public static final String trueValueConstant = "true";
	public static final String falseValueConstant = "false";

	/**
	 * Push operator in the stack.
	 * @param operator
	 */
	private static void push_operator(LinkedList<Integer >operatorStack,
			int operator) {

		operatorStack.addFirst(operator);
	}

	/**
	 * Push operand in the stack.
	 * @param operand
	 */
	private static void push_operand(LinkedList<ExpressionElement> operandStack,
			ExpressionElement operand) {

		operandStack.addFirst(operand);
	}

	/**
	 * Pop operator.
	 */
	private static int pop_operator(LinkedList<Integer> operatorStack) {

		return operatorStack.removeFirst();
	}

	/**
	 * Pop operand.
	 * @return
	 */
	private static ExpressionElement pop_operand(LinkedList<ExpressionElement> operandStack) {

		return operandStack.removeFirst();
	}

	/**
	 * Get top operand.
	 * @return
	 */
	private static ExpressionElement top_operand(LinkedList<ExpressionElement> operandStack) {

		return operandStack.getFirst();
	}

	/**
	 * Get top
	 * @return
	 */
	private static int top_operator(LinkedList<Integer> operatorStack) {

		return operatorStack.getFirst();
	}

	/**
	 * Get next token.
	 * @return
	 * @throws ParseException 
	 */
	private Token next(int index) throws ParseException {
		
		Token token = null;
		
		try{
			token = getToken(index);
		}
		catch (Throwable e) {
			
			throw new ParseException(e.getMessage());
		}
		if (token == null) {
			error(Resources.getString("middle.messageCannotGetNextToken"));
		}
		
		return token;
	}

	/**
	 * Get next token.
	 * @return
	 * @throws ParseException 
	 */
	private Token next() throws ParseException {
		
		return next(0);
	}
	
	/**
	 * Consume token.
	 * @return
	 * @throws ParseException 
	 */
	private Token consume() throws ParseException {

		try {
			return getNextToken();
		}
		catch (Throwable e) {
			
			throw new ParseException(e.getMessage());
		}
	}

	/**
	 * Consume count tokens.
	 * @param count
	 * @throws ParseException 
	 */
	private void consume(int count) throws ParseException {

		for (; count > 0; count--) {
			consume();
		}
	}

	/**
	 * Expect toke.
	 * @param tokenId
	 * @throws ParseException 
	 */
	private void expect(int tokenId) throws ParseException {
		
		Token nextToken = next();
		if (nextToken != null) {
			if (nextToken.kind == tokenId) {
				consume();
				return;
			}
		}
		error(String.format(Resources.getString("middle.messageFoundMisplacedToken"),
				tokenImage[tokenId], tokenImage[next().kind]));
	}
	
	/**
	 * Throws an exception.
	 * @param message 
	 * @throws ParseException
	 */
	private void error(String message) throws ParseException {
		
		Token token = next();
		String errorPositionText = "";
		
		if (token != null) {
			errorPositionText = String.format(
					Resources.getString("middle.messageFoundErrorTokenAtPosition"),
					token.beginLine, token.beginColumn) + " ";
		}
		
		throw new ParseException(errorPositionText + message);
	}

	/**
	 * Makes leaf.
	 * @param term
	 * @param kind 
	 * @return
	 */
	private static Operand mkLeaf(String term, int kind) {

		return new Operand(term, kind);
	}

	/**
	 * Make binary operator node.
	 * @param operator
	 * @param left
	 * @param right
	 * @return
	 */
	private static BinaryOperator mkNode(int operator, ExpressionElement left,
			ExpressionElement right) {

		return new BinaryOperator(operator, left, right);
	}

	/**
	 * Make unary operator node.
	 * @param operator
	 * @param left
	 * @return
	 */
	private static UnaryOperator mkNode(int operator,
			ExpressionElement right) {

		return new UnaryOperator(operator, right);
	}

	/**
	 * Returns true value if the first operator has 
	 * @param operatorX
	 * @param operatorY
	 * @return
	 */
	private static boolean isHigherPrecedence(int operatorX, int operatorY) {

		Integer binaryPrecedenceX = BinaryOperator.getPrecedence(operatorX);
		Integer binaryPrecedenceY = BinaryOperator.getPrecedence(operatorY);
		Integer unaryPrecedenceX = UnaryOperator.getPrecedence(operatorX);
		Integer unaryPrecedenceY = UnaryOperator.getPrecedence(operatorY);
		boolean isLeftAssociativeX = true;
		
		// If there are binary operators, compare it.
		if (binaryPrecedenceX != null && binaryPrecedenceY != null) {
			return binaryPrecedenceX > binaryPrecedenceY ||
				isLeftAssociativeX && binaryPrecedenceX == binaryPrecedenceY;
		}
		
		// If there is a unary and a binary operator.
		if (unaryPrecedenceX != null && binaryPrecedenceY != null) {
			return unaryPrecedenceX >= binaryPrecedenceY;
		}
		
		// If the second operator is unary operator.
		if (unaryPrecedenceY != null) {
			return false;
		}
		
		if (operatorX == SENTINEL) {
			return false;
		}
		
		if (operatorY == SENTINEL) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Solve ambiguity.
	 * @param operator
	 * @return
	 */
	private static int solveAbiguity(int operator) {

		for (int [] item : lookupTable) {
			if (item[0] == operator) {
				return item[1];
			}
		}

		return operator;
	}
	
	/**
	 * Get original operator.
	 * @param operator
	 * @return
	 */
	private static int getOriginalOperator(int operator) {

		for (int [] item : lookupTable) {
			if (item[1] == operator) {
				return item[0];
			}
		}

		return operator;
	}

	/**
	 * Get operator name.
	 * @return
	 */
	public static String getOperatorName(int operator) {

		int originalOperator = getOriginalOperator(operator);
		return ExpressionParserConstants.tokenImage[originalOperator];
	}

	/**
	 * Constructor.
	 * @param reader
	 */
	public ExpressionSolver(Reader reader) {
		super(reader);
	}

	/**
	 * Parse expression and build abstract syntax tree.
	 * (See: http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm#shunting_yard)
	 * @return
	 * @throws ParseException
	 */
	public final ExpressionElement parse() throws ParseException {
				
		// Reset stacks.
		LinkedList<Integer> operatorStack = new LinkedList<Integer>();
		LinkedList<ExpressionElement> operandStack = new LinkedList<ExpressionElement>();
		
		// Push sentinel.
		push_operator(operatorStack, SENTINEL);
		
		consume();

		processE(operatorStack, operandStack);
		
		if (next().kind != EOF) {
			error(String.format(Resources.getString("middle.messageEofTokenNotFound"),
					next().image));
		}
		
		return top_operand(operandStack);
	}

	/**
	 * Process E. (See: http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm#shunting_yard)
	 * @param operandStack 
	 * @param operatorStack 
	 * @throws ParseException 
	 */
	private void processE(LinkedList<Integer> operatorStack,
			LinkedList<ExpressionElement> operandStack)
	throws ParseException {

		processP(operatorStack, operandStack);

		while (BinaryOperator.isOperator(next().kind)) {
			
			pushOperator(next().kind, operatorStack, operandStack);
			consume();
			processP(operatorStack, operandStack);
		}
		
		while (top_operator(operatorStack) != SENTINEL) {
			
			popOperator(operatorStack, operandStack);
		}
	}

	/**
	 * Process P. (See: http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm#shunting_yard)
	 * @param operandStack 
	 * @param operatorStack 
	 * @throws ParseException 
	 */
	private void processP(LinkedList<Integer> operatorStack,
			LinkedList<ExpressionElement> operandStack)
	throws ParseException {
		
		int nextKind = next().kind;
		nextKind = solveAbiguity(nextKind);
		next().kind = nextKind;
		
		// If the next token is a terminal.
		if (nextKind == IDENTIFIER
				|| nextKind == NULL_LITERAL
				|| nextKind == BOOLEAN_LITERAL
				|| nextKind == INTEGER_LITERAL
				|| nextKind == FLOATING_POINT_LITERAL
				|| nextKind == STRING_LITERAL
				|| nextKind == TYPE_IDENTIFIER) {
			
			String idName = next().image;
			
			// If the current token is an identifier.
			if (nextKind == IDENTIFIER) {

				String exceptionMessage = null;
				
				try {
					// Try to read dot token.
					Token afterId = next(1);
					if (afterId.kind == LEFT_BRACKET) {
						
						exceptionMessage = Resources.getString("middle.messageExpectingFunctionParameters");
						
						// Process possible function parameters.
						consume(2);
						
						// Create operators list.
						ArrayList<ExpressionElement> parameters = new ArrayList<ExpressionElement>();
						
						// If the next token is not a right bracket.
						if (next().kind != RIGHT_BRACKET) {
							while (true) {
							
								// Create stacks.
								LinkedList<Integer> localOperatorStack = new LinkedList<Integer>();
								LinkedList<ExpressionElement> localOperandStack = new LinkedList<ExpressionElement>();

								// Process parameter.
								push_operator(localOperatorStack, SENTINEL);
								try {
									processE(localOperatorStack, localOperandStack);
								}
								catch (ParseException e) {
									exceptionMessage = e.getMessage();
									throw e;
								}
								pop_operator(localOperatorStack);
								
								ExpressionElement localExpression = top_operand(localOperandStack);
								
								// Add parameter.
								parameters.add(localExpression);
								
								if (next().kind == RIGHT_BRACKET) {
									consume();
									break;
								}
							
								if (next().kind != COMMA) {
									exceptionMessage = Resources.getString("middle.messageExpectingComma");
									throw new ParseException();
								}
								
								consume();
							}
						}
						else {
							consume();
						}
						
						// Create function.
						Function function = new Function(idName, parameters);
							
						push_operand(operandStack, function);
						return;
					}
				}
				catch (ParseException e) {
					
					if (exceptionMessage != null) {
						error(exceptionMessage);
					}
				}
			}
			
			push_operand(operandStack, mkLeaf(idName, nextKind));
			consume();
		}
		// If the next token is a left bracket.
		else if (nextKind == LEFT_BRACKET) {
			
			consume();
			push_operator(operatorStack, SENTINEL);
			processE(operatorStack, operandStack);
			expect(RIGHT_BRACKET);
			pop_operator(operatorStack);
		}
		else if (UnaryOperator.isOperator(nextKind)) {

			pushOperator(next().kind, operatorStack, operandStack);
			consume();
			processP(operatorStack, operandStack);
		}
		else if (nextKind == EOF) {
			error(Resources.getString("middle.messageUnexpectedEof"));
		}
		else {
			error(String.format(Resources.getString("middle.messageFoundMisplacedToken2"),
					tokenImage[nextKind]));
		}
	}

	/**
	 * Pop operator.
	 * (See: http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm#shunting_yard)
	 */
	private static void popOperator(LinkedList<Integer> operatorStack,
			LinkedList<ExpressionElement> operandStack) {

		if (BinaryOperator.isOperator(top_operator(operatorStack))) {
			
			ExpressionElement t1 = pop_operand(operandStack);
			ExpressionElement t0 = pop_operand(operandStack);
			push_operand(operandStack, mkNode(pop_operator(operatorStack), t0, t1));
		}
		else {
			push_operand(operandStack, mkNode(pop_operator(operatorStack),
					pop_operand(operandStack)));
		}
	}

	/**
	 * Push operator.
	 * (See: http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm#shunting_yard)
	 * @param operator
	 */
	private static void pushOperator(int operator, LinkedList<Integer> operatorStack,
			LinkedList<ExpressionElement> operandStack) {

		while (isHigherPrecedence(top_operator(operatorStack), operator)) {
			
			popOperator(operatorStack, operandStack);
		}
		push_operator(operatorStack, operator);
	}

	/**
	 * Evaluate expression to boolean value.
	 * @param expressionText
	 * @return
	 */
	public static boolean evaluateToBoolean(String expressionText,
			IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		Object resultObject = evaluate(expressionText, identifierSolver,
				functionSolver, null);
		
		if (resultObject instanceof Boolean) {
			return (Boolean) resultObject;
		}
		
		throw new Exception(Resources.getString("middle.messageResultIsNotBooleanValue"));
	}

	/**
	 * Evaluate expression.
	 * @param expressionText
	 * @return
	 */
	public static Object evaluate(String expressionText,
			IdentifierSolver identifierSolver,
			FunctionSolver functionSolver, Obj<ExpressionElement> root)
				throws Exception {
		
		// If the expression text is a string, return it.
		if (expressionText.length() >= 1 && expressionText.charAt(0) == '#') {
			return expressionText.substring(1);
		}

		// Replace end of lines.
		expressionText = expressionText.replace("\n", "");
		expressionText = expressionText.replace("\r", "");
		
		// Create string reader.
		java.io.StringReader stringReader = new java.io.StringReader(expressionText);
		java.io.Reader reader = new java.io.BufferedReader(stringReader);
		
		// Create solver object.
		ExpressionSolver expressionSolver = new ExpressionSolver(reader);
	
		// Create expression syntax tree.
		ExpressionElement expressionRoot = expressionSolver.parse();

		// On error exit.
		if (expressionRoot == null) {
			return null;
		}
		
		if (root != null) {
			root.ref = expressionRoot;
		}
		
		// Evaluate expression abstract syntax tree.
		return expressionRoot.getValueObject(identifierSolver, functionSolver);
	}
	
	/**
	 * Evaluate expression.
	 * @param expressionText
	 * @return
	 */
	public static Object evaluate(String expressionText,
			IdentifierSolver identifierSolver,
			FunctionSolver functionSolver) throws Exception {
		
		return evaluate(expressionText, identifierSolver, functionSolver, null);
	}
}
