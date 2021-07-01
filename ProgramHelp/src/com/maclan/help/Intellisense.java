/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 14-06-2021
 *
 */
package com.maclan.help;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.multipage.gui.TextEditorPane;
import org.multipage.util.Obj;
import org.multipage.util.j;

import com.maclan.help.gnu_prolog.PrologUtility;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.Interpreter.Goal;
import gnu.prolog.vm.PrologCode;

/**
 * 
 * @author vakol
 *
 */
public class Intellisense {
	
	/**
	 * Suggestion class.
	 */
	public static class Suggestion {
		
		/**
		 * Source Prolog term.
		 */
		public Term sourceTerm = null;
		
		/**
		 * Caption.
		 */
		public String caption = null;
		
		/**
		 * Add all suggestion terms.
		 * @param suggestions
		 * @param terms
		 */
		public static void addAll(LinkedList<Suggestion> suggestions, LinkedList<Term> terms) {
			
			terms.forEach(term -> {
					Suggestion suggestion = new Suggestion();
					suggestion.sourceTerm = term;
					suggestion.caption = term.toString();
					suggestions.add(suggestion);
				}); 
		}
		
		/**
		 * Get caption.
		 */
		@Override
		public String toString() {
			return caption;
		}
		
	}
	
	/**
	 * GNU Prolog environment.
	 */
	private static Environment prologEnvironment;
	
	/**
	 * GNU Prolog interpreter.
	 */
	private static Interpreter prologInterpreter;
	
	/**
	 * Regular enum and expressions for the tokenizer.
	 */
	private static enum TokenType { initial, tag_start, white_space_speparator, property_name, equal_sign, property_value, property_separator, tag_closing, text, end_tag };
	
	private static final Pattern tagStartRegex = Pattern.compile("\\G\\s*\\[\\s*@\\s*(\\w*)");
	private static final Pattern whiteSpaceSeparatorRegex = Pattern.compile("\\G\\s");
	private static final Pattern tagPropertyNameRegex = Pattern.compile("\\G\\s*(\\w+)");
	private static final Pattern tagEqualSignRegex = Pattern.compile("\\G\\s*=");
	private static final Pattern tagPropertyValueRegex = Pattern.compile("\\G\\s*([^\\s\\]]*|\"\\S*\")");
	private static final Pattern tagPropertySeparatorRegex = Pattern.compile("\\G(?:\\s*,|\\s)");
	private static final Pattern tagClosingRegex = Pattern.compile("\\G\\s*\\]");
	private static final Pattern tagTextRegex = Pattern.compile("\\G(.+?)(?=\\[\\s*\\/?\\s*@)");
	private static final Pattern endTagRegex = Pattern.compile("\\G\\s*\\[\\s*\\/\\s*@\\s*(\\w*)");
	

	/**
	 * Initialization.
	 */
	public static void initialize() {
		
		// Construct the environment
		prologEnvironment = new Environment();

		// Load definitions of external Java predicates in the "com.maclan.help.gnu_prolog" package.
		URL builtInUrl = Intellisense.class.getResource("/com/maclan/help/properties/java_externals.pl");
		String builtInFile = builtInUrl.getFile();
		prologEnvironment.ensureLoaded(AtomTerm.get(builtInFile));
		
		// Load main Prolog filename for the intellisense.
		URL prologUrl = Intellisense.class.getResource("/com/maclan/help/properties/itellisense.pl");
		String prologFile = prologUrl.getFile();
		prologEnvironment.ensureLoaded(AtomTerm.get(prologFile));

		// Get the interpreter.
		prologInterpreter = prologEnvironment.createInterpreter();
		// Run the initialization
		prologEnvironment.runInitialization(prologInterpreter);
	}

	/**
	 * Make source code suggestions.
	 * @param cursorPosition 
	 */
	public static LinkedList<Suggestion> makeSuggestions(String sourceCode, Integer cursorPosition) {
		
		// Check input value.
		if (cursorPosition == null) {
			return null;
		}
		
		// Prepare source code.
		Term inputTokens = prepareForIntellisense(sourceCode, cursorPosition);
		if (inputTokens == null) {
			return null;
		}
		
		j.log("TOKENS %s", inputTokens.toString());
		
		// Initialization.
		LinkedList<Suggestion> suggestions = new LinkedList<Suggestion>();
		
		// Create query term.
		Term suggestionsAnswer = new VariableTerm("Suggestions");
		Term suggestionsGoal = new CompoundTerm(AtomTerm.get("get_suggestions"), new Term [] { inputTokens, suggestionsAnswer });
		
		// Run Prolog interpreter and get answer.
		synchronized (prologInterpreter) {
			
			try {
				
				Goal theGoal = prologInterpreter.prepareGoal(suggestionsGoal);
				int result;
				LinkedList<Term> terms = new LinkedList<Term>();
				
				do {
					
					result = prologInterpreter.execute(theGoal);
					if (result == PrologCode.HALT || result == PrologCode.FAIL) {
						break;
					}
					
					Term resultingSuggestion = suggestionsAnswer.dereference();
					PrologUtility.addList(resultingSuggestion, terms);
					
					Suggestion.addAll(suggestions, terms);
				}
				while (result != PrologCode.SUCCESS_LAST);
			}
			catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		// Return suggestions.
		return suggestions;
	}
	
	/**
	 * Prepare source code for the intellisense.
	 * @param sourceCode
	 * @param cursorPosition
	 * @return
	 */
	private static Term prepareForIntellisense(String sourceCode, int cursorPosition) {
		
		final Pattern tagOpeningRegex = Pattern.compile("\\[\\s*@+", Pattern.MULTILINE);
		
		Obj<String> preparedSourceCode = new Obj<String>(null);
		
		// Get text from last tag to the cursor position.
		String leadingPart = sourceCode.substring(0, cursorPosition);
		Integer tagStart = null;
		
		Matcher matcher = tagOpeningRegex.matcher(leadingPart);
		while (matcher.find()) {
			
			tagStart = matcher.start();
		}
		
		if (tagStart != null) {
			preparedSourceCode.ref = sourceCode.substring(tagStart, cursorPosition);
		}
		
		if (preparedSourceCode.ref == null) {
			return null;
		}
		
		int sourceLength = preparedSourceCode.ref.length();
		
		// Convert source code to a Prolog term list.
		LinkedList<Term> terms = new LinkedList<Term>();
		Obj<Integer> position = new Obj<Integer>(0);
		Obj<Term> term = new Obj<Term>();
		Obj<TokenType> termType = new Obj<TokenType>(TokenType.initial);
		
		// Lambda functions consuming and returning the tokens.
		Runnable tagStartLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagStartRegex, "tag_start");
			if (term.ref != null) {
				termType.ref = TokenType.tag_start;
			}
		};
		Runnable whiteSpaceSeparatorLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, whiteSpaceSeparatorRegex, "white_space_speparator");
			if (term.ref != null) {
				termType.ref = TokenType.white_space_speparator;
			}
		};
		Runnable tagPropertyNameLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagPropertyNameRegex, "property_name");
			if (term.ref != null) {
				termType.ref = TokenType.property_name;
			}
		};
		Runnable tagEqualSignLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagEqualSignRegex, "equal_sign");
			if (term.ref != null) {
				termType.ref = TokenType.equal_sign;
			}
		};
		Runnable tagPropertyValueLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagPropertyValueRegex, "property_value");
			if (term.ref != null) {
				termType.ref = TokenType.property_value;
			}
		};
		Runnable tagPropertySeparatorLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagPropertySeparatorRegex, "property_separator");
			if (term.ref != null) {
				termType.ref = TokenType.property_separator;
			}
		};
		Runnable tagClosingLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagClosingRegex, "tag_closing");
			if (term.ref != null) {
				termType.ref = TokenType.tag_closing;
			}
		};
		Runnable textLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, tagTextRegex, "text");
			if (term.ref != null) {
				termType.ref = TokenType.text;
			}
		};
		Runnable endTagLambda = () -> {
			term.ref = consume(preparedSourceCode.ref, position, endTagRegex, "end_tag");
			if (term.ref != null) {
				termType.ref = TokenType.end_tag;
			}
		};
		
		// List of tokenizer rules (transitions).
		LinkedHashMap<TokenType, Runnable []> tokenizerRules = new LinkedHashMap<TokenType, Runnable []>();
		
		tokenizerRules.put(TokenType.initial, new Runnable [] { tagStartLambda });
		tokenizerRules.put(TokenType.tag_start, new Runnable [] { tagClosingLambda, whiteSpaceSeparatorLambda });
		tokenizerRules.put(TokenType.white_space_speparator, new Runnable [] { tagPropertyNameLambda });
		tokenizerRules.put(TokenType.property_name, new Runnable [] { tagEqualSignLambda, tagClosingLambda, tagPropertySeparatorLambda });
		tokenizerRules.put(TokenType.equal_sign, new Runnable [] { tagPropertyValueLambda });
		tokenizerRules.put(TokenType.property_value, new Runnable [] { tagClosingLambda, tagPropertySeparatorLambda });
		tokenizerRules.put(TokenType.property_separator, new Runnable [] { tagPropertyNameLambda });
		tokenizerRules.put(TokenType.tag_closing, new Runnable [] {textLambda, endTagLambda, tagStartLambda });
		tokenizerRules.put(TokenType.text, new Runnable [] { tagStartLambda, endTagLambda });
		tokenizerRules.put(TokenType.end_tag, new Runnable [] { tagClosingLambda });

		// Tokenize the source code.
		while (position.ref < sourceLength) {
			
			// Get list of actions and invoke them.
			Runnable [] lambdaFunctions = tokenizerRules.get(termType.ref);
			if (lambdaFunctions == null || lambdaFunctions.length <= 0) {
				break;
			}
			
			for (Runnable lambdaFunction : lambdaFunctions) {
				
				lambdaFunction.run();
				if (term.ref != null) {
					break;
				}
			}
			
			if (term.ref == null) {
				break;
			}
			
			// Add new term to the list of terms.
			terms.add(term.ref);
		}
		
		// Create compound term.
		Term tokensTerm = CompoundTerm.getList(terms.toArray(new Term [0]));
		
		if (ProgramHelp.logLambda != null) {
			ProgramHelp.logLambda.accept(tokensTerm.toString());
		}
		return tokensTerm;
	}
	
	/**
	 * Consume tag start.
	 * @param preparedSourceCode
	 * @param position
	 * @return
	 */
	private static Term consume(String preparedSourceCode, Obj<Integer> position, Pattern regex, String termName) {

		Matcher matcher = regex.matcher(preparedSourceCode);
		boolean found = matcher.find(position.ref);
		
		Term resultingTerm = null;
		
		if (!found) {
			return null;
		}
		
		int start = matcher.start();
		if (start != position.ref) {
			return null;
		}
		
		position.ref = matcher.end();
		
		int groupCount = matcher.groupCount();
		if (groupCount <= 0) {
			
			resultingTerm = AtomTerm.get(termName);
			return resultingTerm;
		}
		
		if (groupCount > 1) {
			return null;
		}
		
		String atomTerm = matcher.group(1);
		
		resultingTerm = new CompoundTerm(CompoundTermTag.get(termName, 1), AtomTerm.get(atomTerm));
		return resultingTerm;
	}
	
	/**
	 * Apply intellisense to the text.
	 * @param textEditorPanel
	 */
	public static void applyTo(TextEditorPane textEditorPanel) {
		
		// Set intellisense lambda function.
		textEditorPanel.intellisenseLambda = sourceCode -> cursorPosition -> caret -> textPane -> {
			
			// Get suggestions.
			LinkedList<Suggestion> suggestions = makeSuggestions(sourceCode, cursorPosition);
			
			// Display the suggestions.
			if (suggestions != null &&!suggestions.isEmpty()) {
				IntellisenseWindow.displayAtCaret(textPane, caret, suggestions);
			}
			else {
				IntellisenseWindow.hideWindow();
			}
			
			return suggestions;
		};
	}
}
