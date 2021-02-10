/**
 * 
 */
package com.maclan.server;

/**
 * @author user
 *
 */
public class Redirection {

	/**
	 * Redirection URI
	 */
	private String uri = "";
	
	/**
	 * Redirection active
	 */
	private boolean active = false;
	
	/**
	 * Set URI
	 * @param uri
	 */
	public void setUri(String uri) {
		
		this.uri = uri;
		active = true;
	}

	/**
	 * Get URI
	 * @return
	 */
	public String getUri() {
		
		return uri;
	}
	
	/**
	 * Returns true if the redirection was set
	 * @return
	 */
	public boolean isActive() {
		
		return active;
	}
}
