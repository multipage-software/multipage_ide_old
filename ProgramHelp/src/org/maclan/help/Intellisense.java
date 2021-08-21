/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 14-06-2021
 *
 */
package org.maclan.help;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.Caret;

import org.maclan.help.gnu_prolog.PrologUtility;
import org.multipage.gui.TextEditorPane;
import org.multipage.util.Obj;
import org.multipage.util.j;

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
	 * A delay in milliseconds between keystrokes that invoke intellisense window.
	 */
	private static final int intellisenseKeystrokeTimeSpanMs = 1000;
	
	/**
	 * Font used in extended suggestion.
	 */
	private static final String suggestionExtensionFont = "size=\"2\" color=\"#999999\"";
	
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
		 * Get string value of term functor value.
		 * @param compundTerm
		 * @param argumentIndex
		 * @param argumentTagName
		 * @return
		 */
		public static String getTermArgumentStringValue(CompoundTerm compundTerm, int argumentIndex, String argumentTagName) {
			
			// Get the argument.
			Term maclanTagTerm = compundTerm.args[argumentIndex];
			int termType = maclanTagTerm.getTermType();
			
			// Check argument type.
			if (termType != Term.COMPOUND) {
				return null;
			}
			
			// Convert reference type.
			CompoundTerm maclanTagCompoundTerm = (CompoundTerm) maclanTagTerm;
			
			// Check tag value.
			if (!argumentTagName.equals(maclanTagCompoundTerm.tag.functor.value)) {
				return null;
			}
			
			// Check arity.
			if (maclanTagCompoundTerm.tag.arity != 1) {
				return null;
			}
			
			// Get Maclan token name.
			Term maclanTagNameTerm = maclanTagCompoundTerm.args[0];
			termType = maclanTagNameTerm.getTermType();
			
			if (termType != Term.ATOM) {
				return null;
			}
			
			// Get string value.
			AtomTerm maclanTagAtomTerm = (AtomTerm) maclanTagNameTerm;
			String stringValue = maclanTagAtomTerm.value;
			
			return stringValue;
		}
		
		/**
		 * Get caption.
		 */
		@Override
		public String toString() {
			
			// Check source term.
			if (sourceTerm == null) {
				return "null";
			}
			
			// Check term type.
			int termType = sourceTerm.getTermType();
			if (termType != Term.COMPOUND) {
				return "bad_term";
			}
			
			CompoundTerm compundTerm = (CompoundTerm) sourceTerm;
			
			// Get term tag and arity.
			CompoundTermTag termTag = compundTerm.tag;
			int termArity = termTag.arity;
			
			// Check term tag.
			String tag = termTag.functor.value;
			if (!"maclan".equals(tag)) {
				return "not_maclan";
			}
			
			// Get Maclan tag name.
			String maclanTagName = null;
			if (termArity >= 1) {
				
				// Get first argument value.
				maclanTagName = getTermArgumentStringValue(compundTerm, 0, "tag");
			}
			
			// Try to get Maclan property.
			String maclanPropertyName = null;
			String maclanTagType = null;
			if (termArity >= 2) {
				
				// Try to get second argument as a tag type.
				maclanTagType = getTermArgumentStringValue(compundTerm, 1, "type");

				// Get second argument value.
				maclanPropertyName = getTermArgumentStringValue(compundTerm, 1, "property");
			}
			
			// Try to get Maclan property type.
			String maclanPropertyTypeName = null;
			if (termArity >= 3) {

				// Get third argument value.
				maclanPropertyTypeName = getTermArgumentStringValue(compundTerm, 2, "type");
			}
			
			// Compile caption.
			String caption = this.caption;
			if (maclanTagName != null) {
				
				if (maclanPropertyName != null) {
					
					if (maclanPropertyTypeName != null) {
						caption = String.format("<b>%s</b><font %s><i> of type %s in %s</i></font>", maclanPropertyName, suggestionExtensionFont, maclanPropertyTypeName, maclanTagName);
					}
					else {
						caption = String.format("<b>%s</b><font %s><i> in %s</i></font>", maclanPropertyName, suggestionExtensionFont, maclanTagName);
					}
				}
				else {
					
					if (maclanTagType != null) {
						
						caption = String.format("<b>%s</b><font %s><i> is %s</i></font>", maclanTagName, suggestionExtensionFont, maclanTagType);
					}
					else {
						caption = String.format("<b>%s</b>", maclanTagName);
					}
				}
			}
			
			return "<html>" + caption + "</html>";
		}
	}
	
	/**
	 * Input class.
	 */
	private static class Input {
		
		/**
		 * Text of the source code.
		 */
		public String sourceCode = null;
		
		/**
		 * Cursor position.
		 */
		public Integer cursorPosition = null;
		
		/**
		 * Caret position.
		 */
		public Caret caret = null;
		
		/**
		 * Reference to text panel.
		 */
		private JTextPane textPane = null;

		/**
		 * Set input data for the intellisense.
		 * @param sourceCode
		 * @param cursorPosition
		 * @param caret
		 * @param textPane
		 */
		public void set(String sourceCode, Integer cursorPosition, Caret caret, JTextPane textPane) {
			
			this.sourceCode = sourceCode;
			this.cursorPosition = cursorPosition;
			this.caret = caret;
			this.textPane = textPane;
		}
		
		/**
		 * Reset input values.
		 */
		public void reset() {
			
			set(null, null, null, null);
		}
		
		/**
		 * Check if the input is valid.
		 * @return
		 */
		public boolean isValid() {
			
			boolean isValid = this.sourceCode != null && this.cursorPosition != null && this.caret != null && this.textPane != null;
			return isValid;
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
	 * States and regular expressions for input string tokenizer.
	 */
	private static enum TokenType { initial, tag_start, white_space_separator, property_name, equal_sign, property_value, property_separator, tag_closing, text, end_tag };
	
	private static final Pattern tagStartRegex = Pattern.compile("\\G\\s*\\[\\s*@\\s*(\\w*)");
	private static final Pattern whiteSpaceSeparatorRegex = Pattern.compile("\\G\\s");
	private static final Pattern tagPropertyNameRegex = Pattern.compile("\\G\\s*(\\w+)");
	private static final Pattern tagEqualSignRegex = Pattern.compile("\\G\\s*=");
	private static final Pattern tagPropertyValueRegex = Pattern.compile("\\G\\s*([^,\\s\\]]*|\"\\S*\")");
	private static final Pattern tagPropertySeparatorRegex = Pattern.compile("\\G(?:\\s*,|\\s)");
	private static final Pattern tagClosingRegex = Pattern.compile("\\G\\s*\\]");
	private static final Pattern tagTextRegex = Pattern.compile("\\G(.+?)(?=\\[\\s*\\/?\\s*@)");
	private static final Pattern endTagRegex = Pattern.compile("\\G\\s*\\[\\s*\\/\\s*@\\s*(\\w*)");

	/**
	 * Maclan help lambda.
	 */
	private static Consumer<String> maclanHelpLambda = null;

	/**
	 * Intellisense timer.
	 */
	private static Timer intellisenseTimer;

	/**
	 * Intellisense input.
	 */
	private static Input input = new Input();
	
	/**
	 * Initialization.
	 */
	public static void initialize() {
		
		// Construct the environment
		prologEnvironment = new Environment();

		// Load definitions of external Java predicates in the "org.maclan.help.gnu_prolog" package.
		URL builtInUrl = Intellisense.class.getResource("/org/maclan/help/properties/java_externals.pl");
		String builtInFile = builtInUrl.getFile();
		prologEnvironment.ensureLoaded(AtomTerm.get(builtInFile));
		
		// Load main Prolog file for the intellisense.
		URL prologUrl = Intellisense.class.getResource("/org/maclan/help/properties/itellisense.pl");
		String prologFile = prologUrl.getFile();
		prologEnvironment.ensureLoaded(AtomTerm.get(prologFile));
		
		// Load Prolog file with maclan tags for the intellisense.
		URL maclanUrl = Intellisense.class.getResource("/org/maclan/help/properties/maclan.pl");
		String maclanFile = maclanUrl.getFile();
		prologEnvironment.ensureLoaded(AtomTerm.get(maclanFile));

		// Get the interpreter.
		prologInterpreter = prologEnvironment.createInterpreter();
		// Run the initialization
		prologEnvironment.runInitialization(prologInterpreter);
		
		// Initialize keystroke timer.
		intellisenseTimer = new Timer(intellisenseKeystrokeTimeSpanMs, e -> onIntellisense());
		intellisenseTimer.setRepeats(false);
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
			term.ref = consume(preparedSourceCode.ref, position, whiteSpaceSeparatorRegex, "whitespace_separator");
			if (term.ref != null) {
				termType.ref = TokenType.white_space_separator;
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
		tokenizerRules.put(TokenType.white_space_separator, new Runnable [] { tagPropertyNameLambda });
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
	 * @param maclanHelpLambda
	 */
	public static void applyTo(TextEditorPane textEditorPanel, Consumer<String> maclanHelpLambda) {
		
		// Set intellisense lambda function.
		textEditorPanel.intellisenseLambda = sourceCode -> cursorPosition -> caret -> textPane -> {
			
			// Initially, hide the window with intellisense suggestions.
			IntellisenseWindow.hideWindow();
			
			// Set input values.
			Intellisense.input.set(sourceCode, cursorPosition, caret, textPane);
			
			// Restart the intellisense timer.
			intellisenseTimer.start();
		};
		
		// Set Maclan help lambda.
		Intellisense.maclanHelpLambda  = maclanHelpLambda;
	}
	
	/**
	 * On intellisense.
	 */
	private static void onIntellisense() {
		
		// Check input values.
		if (Intellisense.input.isValid()) {
		
			// Get suggestions.
			LinkedList<Suggestion> suggestions = makeSuggestions(Intellisense.input.sourceCode, Intellisense.input.cursorPosition);
			
			// Display the suggestions.
			if (suggestions != null &&!suggestions.isEmpty()) {
				IntellisenseWindow.displayAtCaret(Intellisense.input.textPane, Intellisense.input.caret, suggestions);
			}
			else {
				IntellisenseWindow.hideWindow();
			}
		}
		
		// Reset input values.
		Intellisense.input.reset();
	}
	
	/**
	 * Display help page for the input intellisense suggestion.
	 * @param suggestion
	 */
	public static void displayHelpPage(Suggestion suggestion) {
		
		// Invoke Maclan help lambda function.
		if (maclanHelpLambda != null) {
			
			// Get suggestion ID.
			String maclanHelpId = suggestion.sourceTerm.toString();
			
			// Invoke Maclan help on suggestion ID.
			maclanHelpLambda.accept(maclanHelpId);
		}
	}
}
