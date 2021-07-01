/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 01-07-2021
 *
 */
package com.maclan.help.gnu_prolog;

import java.util.LinkedList;

import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.Term;
import gnu.prolog.vm.TermConstants;

/**
 * @author vakol
 *
 */
public class PrologUtility {
	
	/**
	 * Remove leading and ending apostrophes.
	 * @param text
	 * @return
	 */
	public static String removeApostrophes(String text) {
		
		// Check input text.
		if (text == null || text.isEmpty()) {
			return text;
		}
		
		// Remove leading apostrophe.
		char firstCharacter = text.charAt(0);
		if (firstCharacter == '\'') {
			text = text.substring(1);
		}
		
		if (text.isEmpty()) {
			return text;
		}
		
		// Remove ending apostrophe.
		int lastPosition = text.length() - 1;
		char lastCharacter = text.charAt(lastPosition);
		if (lastCharacter == '\'') {
			text = text.substring(0, lastPosition);
		}
		
		return text;
	}

	/**
	 * Add list term items to the Java list.
	 * @param term
	 * @param list
	 */
	public static void addList(Term term, LinkedList<Term> list) {
		
		term = term.dereference();
		
		if (!(term instanceof CompoundTerm)) {
			return;
		}
			
		// Check list term.
		CompoundTerm compoundTerm = (CompoundTerm) term;
		boolean isList = TermConstants.listTag.equals(compoundTerm.tag);
		if (isList) {
			
			// Check arity.
			int arity = compoundTerm.tag.arity;
			if (arity != 2) {
				return;
			}
			
			// Get list head and output it.
			Term headTerm = compoundTerm.args[0];
			list.add(headTerm);
			
			// Get list tail.
			Term tailTerm = compoundTerm.args[1];
			
			// Call this method recursively.
			if (!TermConstants.emptyListAtom.equals(tailTerm)) {
				addList(tailTerm, list);
			}
		}
	}
}
