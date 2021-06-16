/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 14-06-2021
 *
 */
package com.maclan.help;

import java.net.URL;
import java.util.LinkedList;

import org.multipage.gui.TextEditorPane;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
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
	 * GNU Prolog environment.
	 */
	private static Environment prologEnvironment;
	
	/**
	 * GNU Prolog interpreter.
	 */
	private static Interpreter prologInterpreter;

	/**
	 * Initialization.
	 */
	public static void initialize() {
		
		// Construct the environment
		prologEnvironment = new Environment();

		// get the filename relative to the class file
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
	 */
	public static LinkedList<String> makeSuggestions(String sourceCode) {
		
		// Initialization.
		LinkedList<String> suggestions = new LinkedList<String>();
		
		// Create query term.
		Term inputTokens = CompoundTerm.getList(new Term [] { AtomTerm.get("MACLAN") });
		Term suggestionsAnswer = new VariableTerm("Suggestions");
		Term suggestionsGoal = new CompoundTerm(AtomTerm.get("suggestions"), new Term [] { inputTokens, suggestionsAnswer });
		
		// Run Prolog interpreter and get answer.
		synchronized (prologInterpreter) {
			
			try {
				
				Goal theGoal = prologInterpreter.prepareGoal(suggestionsGoal);
				int result;
				
				do {
					
					result = prologInterpreter.execute(theGoal);
					if (result == PrologCode.HALT || result == PrologCode.FAIL) {
						break;
					}
					
					Term resultingSuggestion = suggestionsAnswer.dereference();
					suggestions.add(resultingSuggestion.toString());
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
	 * Apply intellisense to the text .
	 * @param textEditorPanel
	 */
	public static void applyTo(TextEditorPane textEditorPanel) {
		
		// Create intellisense window.
		IntellisenseWindow.createNew(textEditorPanel);
		
		// Set intellisense lambda function.
		textEditorPanel.intellisenseLambda = sourceCode -> cursorPosition -> caret -> {
			
			// Get suggestions.
			LinkedList<String> suggestions = makeSuggestions(sourceCode);
			
			// Display the suggestions.
			if (!suggestions.isEmpty()) {
				IntellisenseWindow.displayAtCaret(textEditorPanel, caret, suggestions);
			}
			
			return suggestions;
		};
	}
}
