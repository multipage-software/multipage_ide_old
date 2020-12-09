/**
 * 
 */
package org.multipage.gui;

/**
 * @author user
 *
 */
public interface EditorValueHandler {

	/**
	 * Ask user for value.
	 */
	public boolean ask();
	
	/**
	 * Get area reference string.
	 */
	public String getText();
	
	/**
	 * Get area reference command.
	 */
	public String getValue();
}
