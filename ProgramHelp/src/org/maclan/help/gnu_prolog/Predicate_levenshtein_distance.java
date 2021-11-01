/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 10-09-2021
 *
 */
package org.maclan.help.gnu_prolog;

import org.multipage.gui.Utility;
import org.multipage.util.j;

import gnu.prolog.term.IntegerTerm;
import gnu.prolog.term.Term;
import gnu.prolog.term.VariableTerm;
import gnu.prolog.vm.ExecuteOnlyCode;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologException;

/**
 * 
 * @author vakol
 *
 */
public class Predicate_levenshtein_distance extends ExecuteOnlyCode {
	
	/**
	 * Callback function.
	 */
	@Override
	public int execute(Interpreter interpreter, boolean backtrackMode, Term[] args) throws PrologException {
		
		// Check arity.
		if (args.length != 3) {
			return FAIL;
		}
		
		// Get pattern and the text.
		Term textTerm = args[0];
		Term patternTerm = args[1];
		
		if (!(args[2] instanceof VariableTerm)) {
			return FAIL;
		}
		
		VariableTerm distanceTerm = (VariableTerm) args[2];
		
		String text = textTerm.dereference().toString();
		String pattern = patternTerm.dereference().toString();
		
		// Remove apostrophes.
		text = PrologUtility.removeApostrophes(text);
		pattern = PrologUtility.removeApostrophes(pattern);
		
		// Get Levenshtein distance between two texts.
		int distance = Utility.getLevenshteinDistance(text, pattern);
		
		j.log("Levenshtein distance between %s and %s is %d ", text, pattern, distance);
		
		// Check the distance.
		if (distance < 0) {
			return FAIL;
		}
		
		IntegerTerm distanceValue = IntegerTerm.get(distance);
		
		// Unify output varaible.
		interpreter.unify(distanceTerm, distanceValue);
		
		return SUCCESS_LAST;
	}
}