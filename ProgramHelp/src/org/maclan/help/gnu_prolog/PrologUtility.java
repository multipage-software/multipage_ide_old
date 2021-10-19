/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 01-07-2021
 *
 */
package org.maclan.help.gnu_prolog;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.multipage.gui.Utility;

import gnu.prolog.term.AtomTerm;
import gnu.prolog.term.CompoundTerm;
import gnu.prolog.term.FloatTerm;
import gnu.prolog.term.IntegerTerm;
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
	
	/**
	 * Convert input string to zero based argument index.
	 * @param inputString
	 * @return
	 */
	private static int toIndex(String inputString) {
		
		final Pattern argumentIndexRegex = Pattern.compile("#(\\d+)");
		
		// Match the input string against regular expression pattern.
		Matcher matcher = argumentIndexRegex.matcher(inputString);
		if (!matcher.matches()) {
			return -1;
		}
		
		// Get resulting argument index.
		int groupCount = matcher.groupCount();
		if (groupCount < 1) {
			return -1;
		}
		
		String argumentIndexString = matcher.group(1);
		try {
			int argumentIndex = Integer.parseInt(argumentIndexString);
			return argumentIndex - 1;
		}
		catch (Exception e) {
		}
		return -1;
	}
	
	/**
	 * Get term element using input path elements list.
	 * @param term
	 * @param thisTermIndex - should be initialize to -1 for recursion entrance purposes
	 * @param pathElements
	 */
	@SuppressWarnings("unchecked")
	private static Term getTermElement(Term term, int thisTermIndex, LinkedList<String> pathElements)
			throws Exception {
		
		// Check input path.
		if (pathElements.isEmpty()) {
			return term;
		}
		
		// Clone path so that input parameter is not changed by this call.
		pathElements = (LinkedList<String>) pathElements.clone();
		
		// Get head path element.
		String pathElement = pathElements.removeFirst();
		int pathLength = pathElements.size();
		int pathElementAsIndex = toIndex(pathElement);
		
		// Do checks depending on index value.
		if (pathElementAsIndex != -1) {
			
			// Check index value.
			if (thisTermIndex == pathElementAsIndex) {
				
				// If the path is not empty, call this method recursively.
				if (pathLength > 0) {
					term = getTermElement(term, -1, pathElements);
				}
				return term;
			}
			else {
				return null;
			}
		}
		
		// Get term name and possible arguments.
		String currentTermName = null;
		Term [] currentTermArguments = null;
		
		if (term instanceof AtomTerm) {
			AtomTerm atomTerm = (AtomTerm) term;
			currentTermName = atomTerm.value; 
		}
		else if (term instanceof IntegerTerm) {
			IntegerTerm integerTerm = (IntegerTerm) term;
			currentTermName = String.valueOf(integerTerm.value); 
		}
		else if (term instanceof FloatTerm) {
			FloatTerm floatTerm = (FloatTerm) term;
			currentTermName = String.valueOf(floatTerm.value); 
		}
		else if (term instanceof CompoundTerm) {
			CompoundTerm compoundTerm = (CompoundTerm) term;
			currentTermName = compoundTerm.tag.functor.value;
			currentTermArguments = compoundTerm.args;
		}
		else {
			Utility.throwException("org.maclan.help.messageBadIntellisenseSuggestionTag");
		}
		
		// Check atom term against the current element.
		if (!currentTermName.equals(pathElement)) {
			return null;
		}
		
		// If there are no arguments, return current term.
		if (currentTermArguments == null || currentTermArguments.length == 0) {
			
			// If the path is not empty, call this method recursively.
			if (pathLength > 0) {
				term = getTermElement(term, -1, pathElements);
			}
			return term;
		}
		
		// If the path is not empty, call this method recursively for all term arguments. Try to find matching term.
		int argumentIndex = 0;
		if (pathLength > 0) {
			
			for (Term currentTermArgument : currentTermArguments) {
				
				term = getTermElement(currentTermArgument, argumentIndex, pathElements);
				if (term != null) {
					return term;
				}
				
				argumentIndex++;
			}
			
			// Nothing has been found, so let return null.
			return null;
		}
		
		// Return current term.
		return term;
	}
	
	/**
	 * Get term element using input path string in following form functor1/functor2/.../functorN/argument (or /#argument_index).
	 * @param inputTerm
	 * @param pathString
	 */
	public static Term getTermElement(Term inputTerm, String pathString)
			throws Exception {
		
		// Check inputs.
		if (inputTerm == null || pathString == null || pathString.isEmpty()) {
			return null;
		}
		
		// Split path string with a slash and create path elements list.
		String [] splittedStrings = pathString.split("\\/");
		LinkedList<String> pathElements = new LinkedList<String>();
		
		Arrays.stream(splittedStrings).forEachOrdered(splittedStringsItem -> {
			if (!splittedStringsItem.strip().isEmpty()) {
				pathElements.add(splittedStringsItem);
			}
		});
		
		// Check the path elements.
		if (pathElements.isEmpty()) {
			return null;
		}
		
		// Delegate the call.
		Term foundTerm = getTermElement(inputTerm, -1, pathElements);
		return foundTerm;
	}
}
