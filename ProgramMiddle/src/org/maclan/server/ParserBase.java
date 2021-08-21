/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;


/**
 * @author
 *
 */
public class ParserBase {
	
	/**
	 * End of file token.
	 */
	protected static final Token EOF = new Token(0);

	/**
	 * Get first token.
	 * @param tokens
	 * @return
	 */
	protected static Token getFirstToken(Token [] tokens) {

		Token firstToken = null;
		
		for (Token token : tokens) {
			
			if (token != null) {
				if (firstToken == null) {
					firstToken = token;
				}
				else {
					if (token.start < firstToken.start) {
						firstToken = token;
					}
				}
			}
		}
		
		if (firstToken == null) {
			return EOF;
		}
		return firstToken;
	}

	/**
	 * Server reference.
	 */
	protected AreaServer server;
	
	/**
	 * Text reference.
	 */
	protected StringBuilder text;
	
	/**
	 * Position.
	 */
	protected int position;
	
	/**
	 * Token.
	 */
	protected Token token;
	
	/**
	 * Constructor.
	 */
	public ParserBase(AreaServer server, StringBuilder text,
			int position) {
		
		this.server = server;
		this.text = text;
		this.position = position;
	}
	
	/**
	 * Consume token.
	 * @param token
	 */
	protected void consume(Token token) {

		position = token.end;
	}

	/**
	 * Get position.
	 * @return
	 */
	public int getPosition() {

		return position;
	}

	/**
	 * Set text.
	 * @param textBuilder
	 * @param start
	 * @param end
	 */
	protected void setTextSubstring(StringBuilder textBuilder, int start,
			int end) {

		if (textBuilder != null) {
			String replace = text.substring(start, end);
			textBuilder.delete(0, textBuilder.length());
			textBuilder.append(replace);
		}
	}
}
