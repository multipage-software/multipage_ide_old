/*
 * Copyright 2010-2019 (C) vakol
 * 
 * Created on : 24-12-2019
 *
 */
package org.maclan;

/**
 * @author user
 *
 */
public abstract class ExternalLinkParser {
	
	/**
	 * External provider types.
	 */
	public static enum Type {
		
		FILE("file"),
		URL("url"),
		AREA("area"),
		UNKNOWN(null);
		
		/**
		 * Alias.
		 */
		public String alias;
		
		/**
		 * Constructor.
		 */
		Type(String alias) {
			
			this.alias = alias;
		}
	}

	/**
	 * File link format string.
	 */
	private static final String fileFormat = "file;%s;%s";
	
	/**
	 * Callback methods.
	 */
	public abstract MiddleResult onFile(String filePath, String encoding);

	/**
	 * Format file link.
	 * @param encoding
	 * @param filePath
	 * @return
	 */
	public static String formatFileLink(String encoding, String filePath) {
		
		String link = String.format(fileFormat, encoding, filePath);
		return link;
	}
	
	/**
	 * Parse link and invoke callback methods on the object.
	 * @param link - external provider link
	 */
	public MiddleResult parse(String link) {
		
		// The link must be in form "type:rest_of_the_link"
		
		if (link == null) {
			return MiddleResult.NULL_POINTER;
		}
		
		int end = link.length();
		
		try {
			
			// Parse the link
			int semicolon = link.indexOf(';');
			if (semicolon == -1) {
				return MiddleResult.EXTERNAL_LINK_SYNTAX_ERROR;
			}
				
			String type = link.substring(0, semicolon);
			
			semicolon++;
			
			// On file link
			if (Type.FILE.alias.contentEquals(type)) {
				
				// Get encoding and file path
				link = link.substring(semicolon, end);
				end = link.length();
				
				semicolon = link.indexOf(';');
				if (semicolon == -1) {
					return MiddleResult.EXTERNAL_LINK_SYNTAX_ERROR;
				}
				
				String encoding = link.substring(0, semicolon++);
				String filePath = link.substring(semicolon, end);
				
				// Invoke callback.
				return onFile(filePath, encoding);
			}
		}
		catch (Exception e) {
			
			return MiddleResult.exceptionToResult(e);
		}
		
		return MiddleResult.UNKNOWN_ERROR;
	}
	
	/**
	 * Get type of link.
	 * @param link
	 * @return
	 */
	public static Type getType(String link) {
		
		// Check link
		if (link == null) {
			return Type.UNKNOWN;
		}
		
		// Find link type.
		int semicolon = link.indexOf(';');
		if (semicolon == -1) {
			return Type.UNKNOWN;
		}
		
		String typeAlias = link.substring(0, semicolon);
		
		// Check types.
		for (Type type : new Type [] { Type.FILE, Type.URL, Type.AREA }) {
			
			if (type.alias.contentEquals(typeAlias)) {
				return type;
			}
		}
		
		return Type.UNKNOWN;
	}
}
