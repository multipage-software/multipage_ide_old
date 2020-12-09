/**
 * 
 */
package org.multipage.util;

/**
 * Input parameter for j.log(...)
 * @author user
 *
 */
public class LogParameter {
	
	/**
	 * Output type
	 */
	private String type = "out";
	
	/**
	 * Indentation
	 */
	private String indentation = "";
	
	/**
	 * Constructor
	 */
	public LogParameter(String type, String indentation) {
		
		this.type = type;
		this.indentation = indentation;
	}
	
	/**
	 * Get type
	 */
	public String getType() {
		
		return type;
	}
	
	/**
	 * Get indentation
	 */
	public String getIndentation() {
		
		return indentation;
	}
	
	/**
	 * Returns output type
	 */
	@Override
	public String toString() {
		
		return getType();
	}
}
