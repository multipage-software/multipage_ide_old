/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 14-06-2021
 *
 */
package org.maclan.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.Caret;

import org.maclan.help.gnu_prolog.PrologUtility;
import org.multipage.gui.TextEditorPane;
import org.multipage.gui.Utility;
import org.multipage.util.Obj;
import org.multipage.util.Resources;
import org.multipage.util.j;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.CompoundTermTag;
import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.Interpreter.Goal;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.PrologException;
import gnu.prolog.vm.interpreter.Tracer;
import gnu.prolog.vm.interpreter.Tracer.TraceLevel;

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
	 * Treshold for unrelated IntelliSense suggestions.
	 */
	private static final Double suggestionsTreshold = 0.3;
	
	/**
	 * Colors for IntelliSense suggestions.
	 */
	private static final  int suggestionBaseColor = 0xDDDDDD;
	private static final int extendedSuggestionColor = 0x999999;
	
	/**
	 * Font used in extended suggestion.
	 */
	private static final String suggestionFontTemplate = "size=\"2\" color=\"%s\"";
	
	/**
	 * Font used in extended suggestion.
	 */
	private static final String suggestionExtensionFontTemplate = "size=\"2\" color=\"%s\"";
	
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
		 * Tag name.
		 */
		public String tagName = null;
		
		/**
		 * Tag type.
		 */
		public String tagType = null;
		
		/**
		 * Tag distance.
		 */
		public Double tagDistance = null;
		
		/**
		 * Replacement position for the tag.
		 */
		public Integer tagReplacementStart = null;
		public Integer tagReplacementEnd = null;
		
		/**
		 * Property name.
		 */
		public String propertyName = null;
		
		/**
		 * Property type.
		 */
		public String propertyType = null;
		
		/**
		 * Property distance.
		 */
		public Double propertyDistance = null;
		
		/**
		 * Replacement for the property.
		 */
		public Integer propertytReplacementStart = null;
		public Integer propertyReplacementEnd = null;
		
		/**
		 * Value from 0.0 to 1.0 of a normalized distance for this suggestion.
		 */
		private Double distance = null;
		
		/**
		 * Object factory.
		 * @param term
		 * @return
		 */
		private static Suggestion createInstance(Term term)
				throws Exception {
			
			// Check referenced source term.
			if (!(term instanceof CompoundTerm)) {
				Utility.throwException("org.maclan.help.messageBadIntellisenseSuggestionTerm");
			}
			
			// Create new instance.
			Suggestion suggestion = new Suggestion();
			// Set reference to the input term and text of displayed caption.
			suggestion.sourceTerm = term;
			
			// Initialize exception message.
			final String exceptionLocalizedMessage = "org.maclan.help.messageBadIntellisenseSuggestionTag";
			
			// Check Maclan term.
			Term maclanTerm = PrologUtility.getTermElement(suggestion.sourceTerm, "/maclan");
			if (maclanTerm == null) {
				Utility.throwException(exceptionLocalizedMessage);
			}

			// Check Maclan tag term.
			Term maclanTagTerm = PrologUtility.getTermElement(maclanTerm, "/maclan/tag");
			if (maclanTagTerm == null) {
				Utility.throwException(exceptionLocalizedMessage);
			}
			
			Term resultTerm = null;
			AtomTerm atomTerm = null;
			IntegerTerm integerTerm = null;
			
			// Get Maclan tag name and match.
			resultTerm = PrologUtility.getTermElement(maclanTagTerm, "/tag/#1");
			if (resultTerm instanceof AtomTerm) {
				
				atomTerm = (AtomTerm) resultTerm;
				suggestion.tagName = atomTerm.value;
				
				// Get the Maclan tag type.
				resultTerm = PrologUtility.getTermElement(maclanTagTerm, "/tag/type/#1");
				if (resultTerm instanceof AtomTerm) {
					
					atomTerm = (AtomTerm) resultTerm;
					suggestion.tagType = atomTerm.value;
					
					// Get tag distance.
					resultTerm = PrologUtility.getTermElement(maclanTagTerm, "/tag/distance/#1");
					if (resultTerm instanceof IntegerTerm) {
						
						integerTerm = (IntegerTerm) resultTerm;
						suggestion.tagDistance = (double) integerTerm.value;
						
						// Get tag match.
						resultTerm = PrologUtility.getTermElement(maclanTagTerm, "/tag/match/#1");
						if (resultTerm instanceof IntegerTerm) {
							
							integerTerm = (IntegerTerm) resultTerm;
							suggestion.tagReplacementStart = integerTerm.value;
							
							resultTerm = PrologUtility.getTermElement(maclanTagTerm, "/tag/match/#2");
							if (resultTerm instanceof IntegerTerm) {
								
								integerTerm = (IntegerTerm) resultTerm;
								suggestion.tagReplacementEnd = integerTerm.value;
							}
						}
					}
				}
			}
			
			// Check Maclan property.
			Term maclanPropertyTerm = PrologUtility.getTermElement(maclanTerm, "/maclan/property");
			if (maclanPropertyTerm instanceof CompoundTerm) {
				
				// Get property name.
				resultTerm = PrologUtility.getTermElement(maclanPropertyTerm, "/property/#1");
				if (resultTerm instanceof AtomTerm) {
					
					atomTerm = (AtomTerm) resultTerm;
					suggestion.propertyName = atomTerm.value;
					
					// Get property type.
					resultTerm = PrologUtility.getTermElement(maclanPropertyTerm, "/property/type/#1");
					if (resultTerm instanceof AtomTerm) {
						
						atomTerm = (AtomTerm) resultTerm;
						suggestion.propertyType = atomTerm.value;
						
						// Get property distance.
						resultTerm = PrologUtility.getTermElement(maclanPropertyTerm, "/property/distance/#1");
						if (resultTerm instanceof IntegerTerm) {
							
							integerTerm = (IntegerTerm) resultTerm;
							suggestion.propertyDistance = Double.valueOf(integerTerm.value);
						
							// Get property match.
							resultTerm = PrologUtility.getTermElement(maclanPropertyTerm, "/property/match/#1");
							if (resultTerm instanceof IntegerTerm) {
								
								integerTerm = (IntegerTerm) resultTerm;
								suggestion.propertytReplacementStart = integerTerm.value;
								
								// Get property match.
								resultTerm = PrologUtility.getTermElement(maclanPropertyTerm, "/property/match/#2");
								if (resultTerm instanceof IntegerTerm) {
									
									integerTerm = (IntegerTerm) resultTerm;
									suggestion.propertyReplacementEnd = integerTerm.value;
								}
							}
						}
					}
				}
			}
						
			// Create caption string.
			suggestion.caption = suggestion.createCaption();
			
			// Return the new instance.
			return suggestion;
		}
		
		/**
		 * Get distance.
		 * @return
		 */
		private Double getDistance() {
			
			Double distance = propertyDistance != null ? propertyDistance : tagDistance;
			return distance;
		}
		
		/**
		 * Get minimum and maximum distance.
		 * @param suggestions
		 * @return
		 */
		public static void getMinMaxDistance(List<Suggestion> suggestions,
				Obj<Double> minimumDistance, Obj<Double> maximumDistance) {
			
			// Traverse all input suggestions.
			suggestions.forEach(suggestion -> {
				
				// Get suggestion distance.
				Double distance = suggestion.getDistance();
				
				// Update minimum distance.
				if (minimumDistance.ref == null || distance < minimumDistance.ref) {
					minimumDistance.ref = distance;
				}
				
				// Update maximum distance.
				if (maximumDistance.ref == null || distance > maximumDistance.ref) {
					maximumDistance.ref = distance;
				}
				
			});
		}
		
		/**
		 * Save normal distances.
		 * @param suggestions
		 * @param maximumDistance 
		 * @param minimumDistance 
		 * @param minimumDistance
		 * @param maximumDistance
		 * @param tresholdDistance 
		 */
		private static void saveNormalDistances(List<Suggestion> suggestions, Double minimumDistance, Double maximumDistance, Double tresholdDistance) {
			
			final double L = 1.0;	 // Maximum normal distance.
			final double x0 = 0.5;   // Midpoint normal distance.
			final double k = 3.0;    // Grow rate. Unimportance of very distant suggestions.
			
			// Check marginal distances.
			if (minimumDistance == null || maximumDistance == null || minimumDistance > maximumDistance) {
				return;
			}
			
			// Save normalize distances.
			suggestions.forEach(suggestion -> {
				
				// Normalize distances.
				Double distance = suggestion.getDistance();
				if (distance != null) {
					
					suggestion.tagDistance = Utility.normalize(suggestion.tagDistance, minimumDistance, maximumDistance);
					
					// Apply logistic function to normal distance.
					suggestion.distance = Utility.normalize(distance, minimumDistance, maximumDistance);
					suggestion.distance = Utility.sigmoid(L, x0, k, distance);
					
					Double propertyDistance = suggestion.propertyDistance;
					if (propertyDistance != null) {
					
						propertyDistance = Utility.normalize(propertyDistance, minimumDistance, maximumDistance);
						
						// Exclude values below the treshold.
						if (tresholdDistance != null && suggestion.distance > tresholdDistance) {
							suggestion.distance = null;
						}
					}
				}
				else {
					suggestion.distance = null;
				}
				
			});
		}
		
		/**
		 * 
		 * Remove unrelated suggestions.
		 * @return
		 */
		public static void removeUnrelatedSuggestions(LinkedList<Suggestion> suggestions, Double treshold) {
			
			// Initialization.
			Obj<Double> minimumDistance = new Obj<Double>();
			Obj<Double> maximumDistance = new Obj<Double>();
			
			// Get minimum and maximum distance.
			getMinMaxDistance(suggestions, minimumDistance, maximumDistance);
			
			// Normalize distances.
			Suggestion.saveNormalDistances(suggestions, minimumDistance.ref, maximumDistance.ref, treshold);
			
			// TODO: debug list
			suggestions.forEach(suggestion -> j.log("%s\t%f", suggestion.tagName, suggestion.distance != null ? suggestion.distance : "null"));
			
			// Remove unrelated suggestions.
			List<Suggestion> relatedSuggestions = suggestions.stream().filter(suggestion -> suggestion.distance != null).collect(Collectors.toList());
			suggestions.clear();
			suggestions.addAll(relatedSuggestions);
		}

		/**
		 * Create caption string
		 * @return
		 */
		public String createCaption() {
			
			// Compile caption.
			String caption = this.caption != null ? this.caption : "unknown";
			if (this.tagName != null) {
				
				BiFunction<Double, Integer, String> getDistanceColor = (inputDistance, baseColor) -> {
					
					// Trim the base color intensity.
					int newColor = Utility.adjustColorIntesity(baseColor, inputDistance);
					
					// Create color string representation and return it.
					String outputColorString = String.format("#%06X", newColor);
					return outputColorString;
				};
				
				String mainFont;
				String extensionFont;
				
				// For a tag suggestion.
				if (propertyName == null) {
					if (tagType != null && tagDistance != null) {
						
						// Get main and extension CSS colors.
						mainFont = String.format(suggestionFontTemplate, getDistanceColor.apply(tagDistance, suggestionBaseColor));
						extensionFont = String.format(suggestionExtensionFontTemplate, getDistanceColor.apply(tagDistance, extendedSuggestionColor));
						
						// Create caption.
						caption = String.format(Resources.getString("org.maclan.help.textIntellisenseTagHint"),
								mainFont, tagName, extensionFont, tagType);
					}
				}
				// For a property suggestion.
				else if (propertyType != null) {
					
					// Get main and extension CSS colors.
					mainFont = String.format(suggestionFontTemplate, getDistanceColor.apply(propertyDistance, suggestionBaseColor));
					extensionFont = String.format(suggestionExtensionFontTemplate, getDistanceColor.apply(tagDistance, extendedSuggestionColor));
					
					// Create caption.
					caption = String.format(Resources.getString("org.maclan.help.textIntellisensePropertyHint"),
							mainFont, propertyName, extensionFont, propertyType, tagName);
				}
			}
			
			// Wrap caption into HTM tag and return resulting string.
			caption = "<html>" + caption + "</html>";
			return caption;
		}
		
		/**
		 * Add all suggestion terms.
		 * @param suggestions
		 * @param terms
		 */
		public static void addAll(LinkedList<Suggestion> suggestions, LinkedList<Term> terms) {
			
			// Load list of suggestions.
			terms.forEach(term -> {
				
					try {
						Suggestion suggestion = createInstance(term);
						if (suggestion != null) {
							suggestions.add(suggestion);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
				}); 
			
			// Sort suggestions by their distances.
			suggestions.sort((suggestion1, suggestion2) -> {
				
				// Compare tag distancess.
				int tagComparision = suggestion1.tagDistance.compareTo(suggestion2.tagDistance);
				if (tagComparision != 0) {
					return tagComparision;
				}
				
				// If they are same, compare property distances.
				if (suggestion1.propertyDistance == null || suggestion2.propertyDistance == null) {
					return 0;
				}
				
				int propertyDistances = suggestion1.propertyDistance.compareTo(suggestion2.propertyDistance);
				return propertyDistances;
			});
		}
		
		/**
		 * Get area alias of the help page related to the suggestion.
		 * @return
		 */
		public String getHelpAreaAlias() {
			
			// Format area alias form tag name.
			String areaAlias = String.format("%s_%s", this.tagName, this.tagType);
			
			// Try to use tag property.
			if (this.propertyName != null) {
				areaAlias += String.format("#%s_%s", this.propertyName, this.propertyType);
			}
			
			return areaAlias;
		}
		
		/**
		 * Get caption.
		 */
		@Override
		public String toString() {

			if (this.caption == null) {
				this.caption = createCaption();
			}
			return this.caption;
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
	
	private static final Pattern tagOpeningRegex = Pattern.compile("\\[\\s*@+", Pattern.MULTILINE);
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
	 * Helper regular expressions that enable to find replacement stop sign.
	 */
	private static Pattern tagDividerRegex = Pattern.compile("\\s+|\\s*\\]");
	private static Pattern propertyDividersRegex = Pattern.compile("\\s+|\\s*(\\,|\\])");
	
	/**
	 * Intellisense output callback.
	 */
	private static Consumer<Suggestion> output = null;
	
	/**
	 * Initialization.
	 */
	public static void initialize() {
		
		final Hashtable<Integer, String> argumentTypes = new Hashtable<Integer, String>();
		argumentTypes.put(-1, "UNKNOWN");
		argumentTypes.put(1, "VARIABLE");
		argumentTypes.put(2, "JAVA_OBJECT");
		argumentTypes.put(3, "FLOAT");
		argumentTypes.put(4, "INTEGER");
		argumentTypes.put(5, "ATOM");
		argumentTypes.put(6, "COMPOUND");
		
		// Construct the environment
		prologEnvironment = new Environment() {
			
			
			@Override
			public synchronized PrologCode loadPrologCode(CompoundTermTag tag) throws PrologException {
				
				Tracer tracer = null;
				
				// Get flag.
				boolean canLog = ProgramHelp.canLog();
				
				if (canLog) {
					
					ProgramHelp.log("PROLOG TRACER:");
					tracer = prologInterpreter.getTracer();
					tracer.setActive(true);
					tracer.addTrace(tag, EnumSet.of(TraceLevel.CALL, TraceLevel.REDO, TraceLevel.FAIL, TraceLevel.EXIT));
					tracer.addTracerEventListener(traceEvent -> {
						
						String argumentsText = "";
						Term [] arguments = traceEvent.getArgs();
						for (Term argument : arguments) {
							
							argumentsText += argumentsText.isEmpty() ? "" : ", ";
							
							argument = argument.dereference();
							
							String argumentText = argument.toString();
							int type = argument.getTermType();
							String typeText = argumentTypes.get(type);
							
							argumentsText += String.format("%s as %s", argumentText, typeText);
							
						}
						
						ProgramHelp.log("PROLOG EVENT: \t[l%s] %s, %s", traceEvent.getLevel().toString(), traceEvent.getTag(), argumentsText);
					});
				}
				
				// Delegate  call.
				PrologCode code = super.loadPrologCode(tag);
				
				if (canLog) {
					
					CompoundTermTag[] stack = tracer.getCallStack();
					
					ProgramHelp.log("");
					ProgramHelp.log("*******************************************************************************", tag);
					ProgramHelp.log("PROLOG TAG: %s", tag);
					ProgramHelp.log("PROLOG BYTECODE: \t%s\n", code);				
					ProgramHelp.log("PROLOG STACK: \t%s", Arrays.toString(stack));
					ProgramHelp.log("PROLOG STATUS:");
					tracer.reportStatus();
				}
				
				return code;
			}

		};

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
		
		// Get flag.
		boolean canLog = ProgramHelp.canLog();
		
		if (canLog) {
			ProgramHelp.log("\n---------------------------- PROLOG INTERPRETER START ----------------------------\n");
			ProgramHelp.log("PROLOG INPUT TOKENS: %s", inputTokens.toString());
		}
		
		// Initialization.
		LinkedList<Suggestion> suggestions = new LinkedList<Suggestion>();
		
		// Create query term.
		Term suggestionsAnswer = new VariableTerm("Suggestions");
		Term suggestionsGoal = new CompoundTerm(AtomTerm.get("get_suggestions"), new Term [] { inputTokens, suggestionsAnswer });
		
		// Run Prolog interpreter and get answer.
		synchronized (prologInterpreter) {
			
			try {
				
				Goal theGoal = prologInterpreter.prepareGoal(suggestionsGoal);
				
				if (canLog) {
					ProgramHelp.log("PROLOG GOAL: %s", suggestionsGoal.toString());
				}
				
				int result;
				LinkedList<Term> terms = new LinkedList<Term>();
				
				do {
					
					result = prologInterpreter.execute(theGoal);
					if (result == PrologCode.HALT || result == PrologCode.FAIL) {
						break;
					}
					
					Term resultingSuggestion = suggestionsAnswer.dereference();
					
					if (canLog) {
						ProgramHelp.log("PROLOG OUTPUT TERM: %s", resultingSuggestion.toString());
					}
					
					PrologUtility.addList(resultingSuggestion, terms);
					
					Suggestion.addAll(suggestions, terms);
				}
				while (result != PrologCode.SUCCESS_LAST);
			}
			catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		if (canLog) {
			ProgramHelp.log("\n---------------------------- PROLOG INTERPRETER STOP -----------------------------\n");
		}
		
		// Remove unrelated suggestions.
		Suggestion.removeUnrelatedSuggestions(suggestions, suggestionsTreshold);
		
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
		
		Obj<String> preparedSourceCode = new Obj<String>(null);
		
		// Get text starting from tag start to current cursor position.
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
		
		boolean canLog = ProgramHelp.canLog();
		if (canLog) {
			ProgramHelp.log(tokensTerm.toString());
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
		
		// Get match start.
		int start = matcher.start();
		if (start != position.ref) {
			return null;
		}
		
		// Get match end.
		int end = matcher.end();
		
		// Update position.
		position.ref = end;
		
		// Check the number of matching elements.
		int groupCount = matcher.groupCount();
		if (groupCount <= 0) {
			
			// Return simple term.
			resultingTerm = new CompoundTerm(CompoundTermTag.get(termName, 2), IntegerTerm.get(start), IntegerTerm.get(end));
			return resultingTerm;
		}
		
		if (groupCount > 1) {
			return null;
		}
		
		String atomTerm = matcher.group(1);
		
		start = matcher.start(1);
		end = matcher.end();
		
		// Return extended term.
		resultingTerm = new CompoundTerm(CompoundTermTag.get(termName, 3), AtomTerm.get(atomTerm), IntegerTerm.get(start), IntegerTerm.get(end));
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
			
			// Set output callback.
			Intellisense.output = selectedSuggestion -> {
				
				// Replace source code in the text panel.
				StringBuilder sourceCodeBuilder = new StringBuilder(sourceCode);
				Integer newCursorPosition = replaceSourceCode(textPane, caret, sourceCodeBuilder, cursorPosition, selectedSuggestion);
				if (newCursorPosition != null) {
					
					textPane.setText(sourceCodeBuilder.toString());
					
					// Close the IntelliSense window and set new cursor position with a delay.
					SwingUtilities.invokeLater(() -> IntellisenseWindow.hideWindow());
					
					javax.swing.Timer timer = new javax.swing.Timer(250, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent event) {
							
							// Try to set new cursor position.
							try {
								textPane.setSelectionStart(newCursorPosition);
								textPane.setSelectionEnd(newCursorPosition);
							}
							catch (Exception e) {
							}
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
				
				// Write record into log.
				boolean canLog = ProgramHelp.canLog();
				if (canLog) {
					ProgramHelp.log(selectedSuggestion.toString());
				}
			};
			
			// Restart the intellisense timer.
			intellisenseTimer.start();
		};
		
		// Set Maclan help lambda.
		Intellisense.maclanHelpLambda  = maclanHelpLambda;
	}
	
	/**
	 * Replace source code with suggestion.
	 * @param textPane
	 * @param cursorPosition
	 * @param sourceCode
	 * @param caret
	 * @param suggestion
	 */
	private static Integer replaceSourceCode(JTextPane textPane, Caret caret, StringBuilder sourceCode, Integer cursorPosition,
			Suggestion suggestion) {
		
		Integer genuineSourceLength = sourceCode.length();
		
		// Replace tag.
		cursorPosition = replaceSourceCode(sourceCode, cursorPosition,
				suggestion.tagName, suggestion.tagReplacementStart, tagDividerRegex);
		
		if (cursorPosition != null) {
			
			// Compute shift of following characters in the source code.
			int shift = sourceCode.length() - genuineSourceLength;
			
			// Try to replace property.
			if (suggestion.propertyName != null) {
				
				Integer cursorPositionForProperty = replaceSourceCode(sourceCode, cursorPosition,
						suggestion.propertyName, suggestion.propertytReplacementStart + shift, propertyDividersRegex);
				
				// Update resulting cursor position.
				if (cursorPositionForProperty != null) {
					cursorPosition = cursorPositionForProperty;
				}
			}
		}
		
		return cursorPosition;
	}
	
	/**
	 * Replace source code element.
	 * @param sourceCode
	 * @param cursorPosition
	 * @param relacement
	 * @param startPosition
	 * @param stopSignRegex
	 * @return
	 */
	private static Integer replaceSourceCode(StringBuilder sourceCode, int cursorPosition, String relacement,
			int startPosition, Pattern stopSignRegex) {
		
		// Try to find replacement end.
		int genuineSourceLength = sourceCode.length();
		int endPosition;
		
		Matcher matcher = stopSignRegex.matcher(sourceCode);
		if (matcher.find(startPosition)) {
			
			endPosition = matcher.start();
		}
		else {
			endPosition = genuineSourceLength;
		}
		
		// Replace source code text element.
		sourceCode.replace(startPosition, endPosition, relacement);
		
		// Compute text shift.
		int shift = sourceCode.length() - genuineSourceLength;
		
		// Return updated position.
		return endPosition + shift;
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
			String maclanHelpId = suggestion.getHelpAreaAlias();
			
			// Invoke Maclan help on suggestion ID.
			maclanHelpLambda.accept(maclanHelpId);
		}
	}
	
	/**
	 * Try to accept selected suggestion.
	 * @param selectedSuggestion
	 */
	public static void acceptSuggestion(Suggestion selectedSuggestion) {
		
		if (output != null) {
			output.accept(selectedSuggestion);
		}
	}
}
