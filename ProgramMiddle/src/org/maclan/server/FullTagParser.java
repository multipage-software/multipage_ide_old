/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.maclan.MiddleResult;
import org.multipage.util.Obj;

/**
 * @author
 *
 */
public class FullTagParser extends ParserBase {

	/**
	 * Token types.
	 */
	private static final int BEGIN = 1;
	private static final int END = 2;
	
	/**
	 * Text matchers.
	 */
	private Matcher beginMatcher;
	private Matcher endMatcher;
	
	/**
	 * Tag name.
	 */
	private String tagName;
	
	/**
	 * Skips begin tag if the flag is set to true
	 */
	private boolean skipBeginTag;
	
	/**
	 * Constructor.
	 * @param server
	 * @param tagName 
	 * @param text
	 * @param position
	 */
	public FullTagParser(AreaServer server, String tagName, StringBuilder text,
			int position) {
		
		super(server, text, position);
		
		this.tagName = tagName;
		this.skipBeginTag = false;

		// Create text matchers.
		createMatchers();
	}
	
	/**
	 * Constructor.
	 * @param server
	 * @param tagName 
	 * @param text
	 * @param skipBeginTag
	 * @param position
	 */
	public FullTagParser(AreaServer server, String tagName, StringBuilder text,
			int position, boolean skipBeginTag) {
		
		super(server, text, position);
		
		this.tagName = tagName;
		this.skipBeginTag = skipBeginTag;

		// Create text matchers.
		createMatchers();
	}

	/**
	 * Create matchers
	 */
	private void createMatchers() {
		
		String patternText = null;
		
		// Create begin matcher.
		patternText = String.format("\\[\\s*@\\s*%s\\b", tagName);
		beginMatcher = Pattern.compile(patternText).matcher(text);
		
		// Create end matcher.
		patternText = String.format("\\[\\s*/@\\s*%s\\s*]", tagName);
		endMatcher = Pattern.compile(patternText).matcher(text);
	}

	/**
	 * Get next token.
	 * @param text
	 * @return
	 */
	private Token next() throws Exception {
		
		Token [] tokens = new Token [2];

		if (beginMatcher.find(position)) {
			
			// Parse IF tag properties.
			Properties properties = new Properties();
			Obj<Integer> endPosition = new Obj<Integer>(beginMatcher.end());
			
			MiddleResult result = ServerUtilities.parseTagProperties(text, endPosition,
					properties, false);
			if (result.isNotOK()) {
				AreaServer.throwError("server.messageTagParseError", result.getMessage());
			}
			
			tokens[0] = new Token(BEGIN, beginMatcher.start(), endPosition.ref);
		}
		if (endMatcher.find(position)) {
			
			tokens[1] = new Token(END, endMatcher.start(), endMatcher.end());
		}
		
		return getFirstToken(tokens);
	}

	/**
	 * Parse text
	 * @param innerText
	 */
	public void parseFullCommand(StringBuilder innerText)
		throws Exception {

		// Parser states.
		final int EXIT = 0;
		final int BEGIN_STATE = 1;
		final int END_STATE = 2;
		
		int state = BEGIN_STATE;
		int beginTagEnd = 0;
		
		if (skipBeginTag) {
			beginTagEnd = position;
		}
		
		// Do loop until exit.
		while (state != EXIT) {
			
			token = next();
			
			switch (state) {
			case BEGIN_STATE:
				if (!skipBeginTag) {
					if (token.type != BEGIN) {
						AreaServer.throwError("server.messageExpectingFullTagStart", tagName);
					}
					beginTagEnd = token.end;
					consume(token);
					token = next();
				}
				else {
					skipBeginTag = false;
				}
				// If next token is BEGIN parse command sequence.
				if (token.type == BEGIN) {
					parseFullCommandSequence();
				}
				// If next is END.
				if (token.type == END) {
					int endTagBegin = token.start;
					// Set inner text and continue.
					setTextSubstring(innerText, beginTagEnd, endTagBegin);
					state = END_STATE;
					break;
				}
				
				AreaServer.throwError("server.messageExpectingTagEnd", tagName);
				
			case END_STATE:
				if (token.type != END) {
					AreaServer.throwError("server.messageExpectingTagEnd", tagName);
				}
				consume(token);
				token = next();
				state = EXIT;
				break;
			
			default:
				AreaServer.throwError("server.messageUnknownFullTagCommandParserState", tagName);
			}
		}
	}

	/**
	 * Parse command sequence.
	 */
	private void parseFullCommandSequence() throws Exception {
		
		while (true) {
			token = next();
			if (token.type != BEGIN) {
				return;
			}
	
			parseFullCommand(null);
		}
	}
}
