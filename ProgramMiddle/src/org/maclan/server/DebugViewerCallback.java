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
	 * Show user alert
	 * @param message
	 * @param timeout 
	 */
	void showUserAlert(String message, int timeout);
}
