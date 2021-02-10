package org.multipage.generator;

/**
 * 
 * @author user
 *
 */
public enum EditorState {
	
	/**
	 * Enumerations
	 */
	initial(".lino { background-color: @linenoColor@; }"),
	debugging(".lino { background-color: @debugColor@; }"),
	notDebugging(".lino { background-color: @linenoColor@; }");
	
	/**
	 * Constants
	 */
	String linenoColor = "#DDDDDD";
	String debugColor = "#DD0000";

	/**
	 * CSS rule for code editor
	 */
	String cssRule;
	
	/**
	 * Constructor
	 * @param cssRule
	 */
	EditorState(String cssRule) {
		
		cssRule = cssRule.replaceAll("@debugColor@", debugColor);
		this.cssRule = cssRule.replaceAll("@linenoColor@", linenoColor);
	}
}
