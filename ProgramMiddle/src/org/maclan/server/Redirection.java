/**
 * 
 */
package org.maclan.server;

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
	 * Flag "direct".
	 */
	private boolean direct = false;
	
	/**
	 * Redirection active
	 */
	private boolean active = false;
	
	/**
	 * Set URI
	 * @param uri
	 * @param direct 
	 */
	public void setUri(String uri, boolean direct) {
		
		this.uri = uri;
		this.direct = direct;
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
	 * Returns true if this is a direct URL redirection.
	 * @return
	 */
	public boolean isDirect() {
		
		return direct;
	}
	
	/**
	 * Returns true if the redirection was set
	 * @return
	 */
	public boolean isActive() {
		
		return active;
	}
}
