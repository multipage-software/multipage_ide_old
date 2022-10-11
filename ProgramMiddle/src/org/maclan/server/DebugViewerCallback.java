package org.maclan.server;

/**
 * A callback interface for debug viewer frame
 * @author user
 *
 */
public interface DebugViewerCallback {
	
	/**
	 * Open file with debug viewer
	 * @param fileuri
	 */
	int openFile(String fileuri);
	
	/**
	 * Called whenever the session state changes (the parameter ready informs about prepared session ).
	 * @param ready
	 */
	void sessionStateChanged(boolean ready);
	
	/**
	 * Show user alert
	 * @param message
	 * @param timeout 
	 */
	void showUserAlert(String message, int timeout);
}
