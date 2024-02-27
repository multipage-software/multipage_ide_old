/*
 * Copyright 2010-2019 (C) Vaclav Kolarcik
 * 
 * Created on : 26-04-2017
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Panel;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.TabPanelComponent;
import org.multipage.gui.UpdateSignal;
import org.multipage.util.j;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * 
 * @author user
 *
 */
public class MonitorPanel extends Panel implements TabPanelComponent {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Web view objects.
	 */
    private WebView webViewBrowser = null;
    private WebEngine webEngine = null;
	private Scene scene = null;
	
	/**
	 * SWT view objects.
	 */
	private SwtBrowserCanvas swtBrowser = null;
	
	/**
	 * Home URL.
	 */
	private String url;

	/**
	 * Create the panel.
	 * @param url 
	 */
	public MonitorPanel(String url) {
		
		this.url = url;
		
		// Initialize components.
		initComponents();
		// Post creation.
		postCreation(); //$hide$
	}
	
	/**
	 * Initialize components.
	 */
	private void initComponents() {
		setLayout(new BorderLayout(0, 0));
	}
	
	/**
	 * Post creation.
	 */
	private void postCreation() {
		
		// Ensure the panel is visible and open the browser.
		setVisible(true);
		openBroser(url);
		
		setListeners();
	}

	/**
	 * Open browser.
	 */
	private void openBroser(String url) {
		
		// TODO: <---FIX When this panel is removed the application exits.
		// Try to open SWT browser (with native code).
		SwingUtilities.invokeLater(() -> {
			
			// TODO: <---COPY to new version
			swtBrowser = SwtBrowserCanvas.createLater(browser -> {
				
				// Add SWT browser to the center of the panel.
				MonitorPanel.this.add(browser, BorderLayout.CENTER);
				// Load the URL provided.
				return url;
			});
		
			if (swtBrowser == null) {
				
				// Otherwise use JavaFX panel for webViewBrowser.
				JFXPanel javaFxPanel = new JFXPanel();
				
				// Add JavaFX panel.
				add(javaFxPanel, BorderLayout.CENTER);
				
				// Initialize scene.
				Platform.setImplicitExit(false);
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						
						webViewBrowser = new WebView();
						webEngine = webViewBrowser.getEngine();
						
						webEngine.load(url);
						
						scene = new Scene(webViewBrowser, 750, 500, Color.web("#666970"));
						javaFxPanel.setScene(scene);
					}
				});
			}
		});
	}
	
	/**
	 * Set listeners.
	 */
	// TODO: <---COPY to new version
	private void setListeners() {
		
		// Receive the "update" signal.
		ApplicationEvents.receiver(this, UpdateSignal.updateMonitorPanel, message -> {

			swtBrowser.reload();
		});
	}
	
	/**
	 * Load URL.
	 */
	public boolean load(String url) {
		
		// Delegate the call.
		if (webViewBrowser == null) {
			return false;
		}
		
		// Load URL into the webViewBrowser.
		return true;
	}
	
	/**
	 * Dispose monitor.
	 */
	public void dispose() {
		 

	}
	
	/**
	 * Recreate browser.
	 */
	// TODO: <---COPY to new version
	public void recreateBrowser() {
		
		swtBrowser.close();
		
		MonitorPanel.this.remove(swtBrowser);
		
		swtBrowser = SwtBrowserCanvas.createLater(browser -> {
			
			// Add SWT browser to the center of the panel.
			MonitorPanel.this.add(browser, BorderLayout.CENTER);
			// Load the URL provided.
			return url;
		});
	}
	
	/**
	 * On tab panel change event.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
	}
	
	/**
	 * Called before the tab panel is removed. 
	 */
	@Override
	public void beforeTabPanelRemoved() {
		
		if (swtBrowser != null) {
			swtBrowser.close();
		}
	}
	
	/**
	 * Called when the tab panel needs to recreate its content.
	 */
	@Override
	public void recreateContent() {
		
		recreateBrowser();
	}
}
