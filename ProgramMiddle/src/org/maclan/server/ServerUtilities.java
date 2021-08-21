/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;

import javax.swing.text.*;

import org.maclan.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class ServerUtilities {
	
	/**
	 * ELSE statement pattern.
	 */
	static final Pattern elsePattern = Pattern.compile("\\[\\s*@ELSE\\s*]",
			Pattern.CASE_INSENSITIVE);
	
	/**
	 * End tag pattern.
	 */
	static final Pattern endPattern = Pattern.compile("\\[\\s*\\/\\s*@\\s*\\w+\\s*]");
	
	/**
	 * REM end tag pattern.
	 */
	static final Pattern remEndPattern = Pattern.compile("\\[\\s*\\/\\s*@\\s*REM\\s*]");
	
	/**
	 * Output text.
	 * @param outputStream
	 * @param text
	 */
	public static void output(OutputStream outputStream, String text) {
		
		// If no output stream, exit the method.
		if (outputStream == null) {
			return;
		}

		try {
			// Output writer.
			PrintWriter out = new PrintWriter(
					new OutputStreamWriter(outputStream, "UTF8"), true);
			// Write string.
			out.println(text);
		}
		catch (UnsupportedEncodingException e) {
		}		
	}

	/**
	 * Output text.
	 * @param outputStream
	 * @param text
	 */
	public static void output(OutputStream outputStream, StringBuilder text) {

		try {
			// Output writer.
			PrintWriter out = new PrintWriter(
					new OutputStreamWriter(outputStream, "UTF8"), true);
			// Write string.
			for (int index = 0; index < text.length(); index++) {
				
				char character = text.charAt(index);
				out.print(character);
			}
			out.println();
		}
		catch (UnsupportedEncodingException e) {
		}		
	}
		
	/**
	 * Enumeration of states.
	 */
	private static final int BEFORE_PARAMETER_WHITESPACES = 0;
	private static final int PARAMETER_NAME_OR_END = 1;
	private static final int BEFORE_EQUAL_SIGN_WHITESPACES = 2;
	private static final int EQUAL_SIGN_OR_END = 3;
	private static final int BEFORE_VALUE_WHITESPACES = 4;
	private static final int VALUE = 5;
	private static final int TEXT_VALUE_START = 6;
	private static final int TEXT_VALUE = 7;
	
	/**
	 * Parse tag properties.
	 * @param text
	 * @param textPosition 
	 * @param properties
	 * @return
	 */
	public static MiddleResult parseTagProperties(StringBuilder text,
			Obj<Integer> textPosition, Properties properties) {
		
		return parseTagProperties(text, textPosition, properties, true);
	}
	
	/**
	 * Parse tag properties.
	 * @param text
	 * @param textPosition 
	 * @param properties
	 * @param checkAmbiguity
	 * @return
	 */
	public static MiddleResult parseTagProperties(StringBuilder text,
			Obj<Integer> textPosition, Properties properties,
			boolean checkAmbiguity) {

		// Reset properties.
		properties.clear();
		
		// Get text length.
		int textLength = text.length();
		
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		
		boolean endReached = false;
		
		MiddleResult ok = MiddleResult.OK;
		MiddleResult result = ok;

		// Initialize position and state.
		int position = textPosition.ref == null ? 0 : textPosition.ref;
		int state = BEFORE_PARAMETER_WHITESPACES;
		
		// Process text.
		while (!endReached && position < textLength && result == ok) {
			
			// Get character.
			char character = text.charAt(position);
			// Process parser state.
			switch (state) {

			case BEFORE_PARAMETER_WHITESPACES:
				if (isWhiteSpaceOrComma(character)) {
					position++;
					break;
				}
				state = PARAMETER_NAME_OR_END;
				break;
			case PARAMETER_NAME_OR_END:
				if (character == ']') {
					if (key.length() > 0) {
						result = setProperty(properties, key.toString(),
								"", checkAmbiguity);
						if (result != ok) {
							return result;
						}
					}
					endReached = true;
					break;
				}
				if (isIdentifier(character)) {
					key.append(character);
					position++;
					break;
				}
				if (key.length() == 0) {
					return MiddleResult.ERROR_PARSE_PROPERTY;
				}
				state = BEFORE_EQUAL_SIGN_WHITESPACES;
				break;
			case BEFORE_EQUAL_SIGN_WHITESPACES:
				if (isWhiteSpace(character)) {
					position++;
					break;
				}
				state = EQUAL_SIGN_OR_END;
				break;
			case EQUAL_SIGN_OR_END:
				if (character == ']') {
					if (key.length() > 0) {
						result = setProperty(properties, key.toString(),
								"", checkAmbiguity);
						if (result != ok) {
							return result;
						}
					}
					endReached = true;
					break;
				}
				if (character != '=') {
					result = setProperty(properties, key.toString(),
							"", checkAmbiguity);
					if (result != ok) {
						return result;
					}
					key = new StringBuilder();
					value = new StringBuilder();
					state = BEFORE_PARAMETER_WHITESPACES;
					break;
				}
				position++;
				state = BEFORE_VALUE_WHITESPACES;
				break;
			case BEFORE_VALUE_WHITESPACES:
				if (isWhiteSpace(character)) {
					position++;
					break;
				}
				if (character == '\"') {
					state = TEXT_VALUE_START;
				}
				else {
					state = VALUE;
				}
				break;
			case VALUE:
				if (!isWhiteSpaceOrCommaOrTagEnd(character)) {
					value.append(character);
					position++;
					break;
				}
				result = setProperty(properties, key.toString(),
						value.toString(), checkAmbiguity);
				if (result != ok) {
					return result;
				}
				key = new StringBuilder();
				value = new StringBuilder();
				state = BEFORE_PARAMETER_WHITESPACES;
				break;
			case TEXT_VALUE_START:
				if (character != '\"') {
					return MiddleResult.ERROR_PARSE_PROPERTY;
				}
				position++;
				state = TEXT_VALUE;
				break;
			case TEXT_VALUE:
				if (character == '\"') {
					result = setProperty(properties, key.toString(),
							value.toString(), checkAmbiguity);
					if (result != ok) {
						return result;
					}
					key = new StringBuilder();
					value = new StringBuilder();
					position++;
					state = BEFORE_PARAMETER_WHITESPACES;
					break;
				}
				if (character == '\\') {
					position++;
					if (position >= textLength) {
						return MiddleResult.ERROR_PARSE_PROPERTY;
					}
					character = text.charAt(position);
					if (character == '\\' || character == '\"') {
						value.append(character);
						position++;
						break;
					}
					if (character == 'n') {
						value.append('\\');
						value.append('n');
						position++;
						break;
					}
					if (character == 'r') {
						value.append('\\');
						value.append('r');
						position++;
						break;
					}
					return MiddleResult.ERROR_PARSE_PROPERTY;
				}
				value.append(character);
				position++;
				break;
			}	
		}

		if (endReached) {
			position++;
			textPosition.ref = position;
			return ok;
		}
		else {
			properties.clear();
			return result;
		}
	}

	/**
	 * Returns true if the character is an identifier character.
	 * @param character
	 * @return
	 */
	private static boolean isIdentifier(char character) {
		
		return character >= 'A' && character <= 'Z'
			|| character >= 'a' && character <= 'z'
			|| character >= '0' && character <= '9'
			|| character == '_' || character == '-'
		    || character == '.' || character == '$'
		    || character == '#';
	}

	/**
	 * Returns true value if the character is a whitespace.
	 * @param character
	 * @return
	 */
	private static boolean isWhiteSpace(char character) {
		
		return character == ' ' || character == '\r'
			|| character == '\n' || character == '\t'
		    || character == '\f';
	}

	/**
	 * Returns true value if the character is a whitespace or comma.
	 * @param character
	 * @return
	 */
	private static boolean isWhiteSpaceOrComma(char character) {
		
		return character == ' ' || character == '\r'
			|| character == '\n' || character == '\t'
		    || character == ',' || character == '\f';
	}

	/**
	 * 
	 * @param character
	 * @return
	 */
	private static boolean isWhiteSpaceOrCommaOrTagEnd(char character) {
		
		return isWhiteSpaceOrComma(character) || character == ']';
	}

	/**
	 * Set property.
	 * @param properties
	 * @param key
	 * @param value
	 * @param checkAmbiguity
	 * @return
	 */
	private static MiddleResult setProperty(Properties properties,
			String key, String value, boolean checkAmbiguity) {
		
		if (checkAmbiguity && properties.containsKey(key)) {
			return new MiddleResult(null, String.format(
							Resources.getString("server.messageParseErrorPropertyAlreadyExists"),
							key));
		}
		properties.setProperty(key, value);

		return MiddleResult.OK;
	}

	/**
	 * Get tag regular expression.
	 * @param tagName
	 * @return
	 */
	private static String getSingleTagRegex(String tagName) {
		
		return "\\[@\\s*" + tagName + "\\s*]";
	}

	/**
	 * Replace tags.
	 * @param text
	 * @param tagName
	 * @param tagText 
	 */
	public static String replaceTags(String text, String tagName, String tagText) {

		return text.replaceAll(getSingleTagRegex(tagName), tagText);
	}

	/**
	 * Returns true value if the tag exists.
	 * @param text
	 * @param tagName
	 * @return
	 */
	public static boolean existsTag(String text, String tagName) {
		
		Pattern pattern = Pattern.compile(getSingleTagRegex(tagName));
		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	/**
	 * Set default file coding.
	 * @param coding
	 */
	public static void setDefaultCoding(String coding) {
				
		try {
			System.setProperty("file.encoding",coding);
			Field charset;
	
			charset = Charset.class.getDeclaredField("defaultCharset");
	
			charset.setAccessible(true);
			charset.set(null,null);
		} 
		catch (Exception e) {

		}
	}

	/**
	 * Find tag start.
	 */
	public static boolean findTagStart(StringBuilder text,
			Obj<Integer> position, Obj<Integer> tagStartPosition) {
		
		final Pattern tagStartPattern = Pattern.compile("\\[\\s*@[^@]");
		
		Matcher matcher = tagStartPattern.matcher(text);
		if (!matcher.find(position.ref)) {
			return false;
		}
		
		if (tagStartPosition != null) {
			tagStartPosition.ref = matcher.start();
		}
		
		position.ref = matcher.end() - 1;
		return true;
	}

	/**
	 * Find tag start for next level.
	 */
	public static boolean findTagStartNextLevel(StringBuilder text,
			Obj<Integer> position, Obj<Integer> tagStartPosition) {
		
		final Pattern tagStartPattern = Pattern.compile("\\[\\s*@@");
		
		Matcher matcher = tagStartPattern.matcher(text);
		if (!matcher.find(position.ref)) {
			return false;
		}
		
		if (tagStartPosition != null) {
			tagStartPosition.ref = matcher.start();
		}
		
		position.ref = matcher.end() - 1;
		return true;
	}

	/**
	 * Find tag end for next level.
	 */
	public static boolean findTagEndNextLevel(StringBuilder text,
			Obj<Integer> position, Obj<Integer> tagStartPosition) {
		
		final Pattern tagStartPattern = Pattern.compile("\\[\\s*\\/\\s*@@");
		
		Matcher matcher = tagStartPattern.matcher(text);
		if (!matcher.find(position.ref)) {
			return false;
		}
		
		if (tagStartPosition != null) {
			tagStartPosition.ref = matcher.start();
		}
		
		position.ref = matcher.end() - 1;
		return true;
	}
	
	/**
	 * Skip.
	 * @param position 
	 * @param text 
	 * @param text
	 * @param textPointer
	 */
	public static void skip(StringBuilder text, Obj<Integer> position) {
		
		int textLength = text.length();
		
		while (position.ref < textLength) {
			char currentCharacter = text.charAt(position.ref);
			if (!isSkipCharacter(currentCharacter)) {
				return;
			}
			position.ref++;
		}
	}

	/**
	 * Skip character.
	 * @param character
	 * @return
	 */
	public static boolean isSkipCharacter(char character) {
		
		final char [] charactersToSkip = {' ', '\t', '\n', '\r'};
		
		for (char skipCharacter : charactersToSkip) {
			if (character == skipCharacter) {
				return true;
			}
		}
	
		return false;
	}

	/**
	 * Read tag name.
	 * @param text
	 * @param textPointer
	 * @return
	 */
	public static String readTagName(StringBuilder text,
			Obj<Integer> position) {
		
		// Skip.
		skip(text, position);
		
		StringBuilder tagName = new StringBuilder();
		int textLength = text.length();
		
		// Read characters until there is a skip character.
		while (position.ref < textLength) {
			
			char currentCharacter = text.charAt(position.ref);
			if (isSkipCharacter(currentCharacter)) {
				break;
			}
			if (currentCharacter == ']') {
				break;
			}
			
			tagName.append(currentCharacter);
			position.ref++;
		}
		
		return tagName.toString();
	}

	/**
	 * Find tag end.
	 */
	public static void findTagEnd(StringBuilder text, Obj<Integer> position) throws Exception {
		
		final Pattern tagEndPattern = Pattern.compile("\\s*]");
	
		Matcher matcher = tagEndPattern.matcher(text);
		if (!matcher.find(position.ref)) {
			AreaServer.throwError("server.messageCannotFindEndOfTag");
		}
		
		position.ref = matcher.end();
	}

	/**
	 * Removes only our private highlights
	 * @param textComponent
	 */
	public static void removeScriptCommandsHighlights(JTextComponent textComponent) {
		
	    Highlighter hilite = textComponent.getHighlighter();
	    Highlighter.Highlight[] hilites = hilite.getHighlights();

	    for (int i=0; i < hilites.length; i++) {
	        if (hilites[i].getPainter() instanceof ScriptCommandHighlightPainter) {
	            hilite.removeHighlight(hilites[i]);
	        }
	    }
	}

	/**
	 * Highlight script commands.
	 * @param textComponent
	 */
	public static void highlightScriptCommands(JTextComponent textComponent, Color color) {
		
	    // First remove all old highlights
	    removeScriptCommandsHighlights(textComponent);
	    
	    // Get highlighter.
	    Highlighter hiliter = textComponent.getHighlighter();
	    
	    // Highlight painter.
	    final Highlighter.HighlightPainter commandHighlightPainter = new ScriptCommandHighlightPainter(color);
	    
	    // Initialize position in text.
	    Obj<Integer> position = new Obj<Integer>(0);
	    
	    // Get component text and create string builder.
	    Document document = textComponent.getDocument();
	    int length = document.getLength();
	    String componentText = "";
		try {
			componentText = document.getText(0, length);
		}
		catch (BadLocationException e1) {
		}
	    
	    StringBuilder text = new StringBuilder(componentText);
	    
	    Properties properties = new Properties();
	    MiddleResult result;

	    Obj<Integer> tagStartPosition = new Obj<Integer>(0);
	    
	    // REM end matcher.
	    Matcher remEndMatcher = remEndPattern.matcher(text);
	    
		// Highlight start commands.
	    while (findTagStart(text, position, tagStartPosition)) {
	    	
	    	int start = tagStartPosition.ref;
	    	
	    	// Read tag name.
	    	String tagName = readTagName(text, position);
	    	if (tagName.isEmpty()) {
	    		continue;
	    	}
	    	
	    	// If it is a REM tag.
	    	if (tagName.equals("REM")) {
	    		
	    		// Find next REM end tag.
	    		if (remEndMatcher.find(position.ref)) {
	    			
	    			int end = remEndMatcher.end();
	    	    	try {
	    				hiliter.addHighlight(start, end, commandHighlightPainter);
	    			}
	    	    	catch (BadLocationException e) {
	    			}
	    	    	continue;
	    		}
	    	}
	    		    	
	    	// Try to read properties.
	    	result = parseTagProperties(text, position, properties);
	    	if (result.isNotOK()) {
	    		continue;
	    	}
	    	
	    	int end = position.ref;
	    	
	    	try {
				hiliter.addHighlight(start, end, commandHighlightPainter);
			}
	    	catch (BadLocationException e) {
			}
	    }
	    
	    // Highlight tags' ends.
	    Matcher matcher = endPattern.matcher(componentText);
	    
	    while (matcher.find()) {
	    	
	    	int start = matcher.start();
	    	int end = matcher.end();
	    	
	    	// If it is a REM tag skip it.
	    	String endTag = text.substring(start, end);
	    	if (endTag.matches("\\[\\s*\\/\\s*@\\s*REM\\s*]")) {
	    		continue;
	    	}
	    	
	    	try {
				hiliter.addHighlight(start, end, commandHighlightPainter);
			}
	    	catch (BadLocationException e) {
			}
	    }
	}
	
	/**
	 * Returns true value if the input text possibly contains tags.
	 * @param textString
	 * @return
	 */
	public static boolean possiblyContainsTags(String textString) {
		
		StringBuilder text = new StringBuilder(textString);
		Obj<Integer> position = new Obj<Integer>(0);
	    Obj<Integer> tagStartPosition = new Obj<Integer>(0);
	    
	    Properties properties = new Properties();
	    MiddleResult result = null;
	    
		// Try to find tag beginning.
	    while (findTagStart(text, position, tagStartPosition)) {
	    	
	    	// Try to read tag name.
	    	String tagName = readTagName(text, position);
	    	if (tagName.isEmpty()) {
	    		continue;
	    	}
	    		    	
			// Try to read properties.
	    	result = parseTagProperties(text, position, properties);
	    	if (result.isNotOK()) {
	    		continue;
	    	}
	    	
	    	return true;
	    }
	    
	    return false;
	}
}
