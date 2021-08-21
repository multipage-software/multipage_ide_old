/**
 * 
 */
package org.maclan;

import java.sql.Timestamp;

/**
 * @author user
 *
 */
public class Revision {
	
	/**
	 * Revision number
	 */
	public Long number;
	
	/**
	 * Time stamp
	 */
	public Timestamp created;
	
	/**
	 * Get string
	 */
	@Override
	public String toString() {
		
		return String.format("[%03d]   %s", number, created.toString());
	}
	
	/**
	 * Get string. Revision number is replaced with "*", if it is last revision
	 * @param last
	 * @return
	 */
	public String toString(boolean last) {
		
		return last ? String.format("[ * ]   %s", created.toString()) : toString();
	}
}
