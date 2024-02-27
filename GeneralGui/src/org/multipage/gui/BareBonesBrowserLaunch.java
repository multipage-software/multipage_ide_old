package org.multipage.gui;

import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;

import javax.swing.JOptionPane;

/**
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application
 * in the user's default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * Public Domain Software -- Free to Use as You Like
 * @version 3.1, June 6, 2010
 */
public class BareBonesBrowserLaunch {

	static final String[] browsers = { "google-chrome", "firefox", "opera",
	  "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
	static final String errMsg = "Error attempting to launch web browser";

	/**
	* Opens the specified web page in the user's default browser
	* @param uri A web address (URi) of a web page (e.g.: "http://www.google.com/")
	*/
	public static void openURL(URI uri) {
   
		try {
			Desktop.getDesktop().browse(uri);
		}
		catch (Exception exception) {
			
			try {
	    		// Assume Unix or Linux.
	    		String selectedBrowser = null;
	    		for (String browser : browsers) {
	    			
	    			String [] command = new String[] {"which", browser};
	    			boolean browserExists = Runtime.getRuntime().exec(command).getInputStream().read() != -1;
	    			
					if (browserExists) {
					    selectedBrowser = browser;
					    
					    String uriText = uri.toASCIIString();
					    
					    // Open browser with URL.
						Runtime.getRuntime().exec(new String[] { selectedBrowser, uriText });
						break;
					}
	    		}
				
	    		// Throw exception.
				if (selectedBrowser == null) {
					throw new Exception(Arrays.toString(browsers));
				}
			}
			catch (Exception e) {
			    JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
			}
		}
	}
}
