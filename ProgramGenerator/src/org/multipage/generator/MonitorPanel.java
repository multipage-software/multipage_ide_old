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
public class MonitorPanel extends Panel implements TabItemInterface {

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
		
		// Ensure the panel is visible.
		setVisible(true);
		
		// Try to open SWT browser (with native code).
		SwingUtilities.invokeLater(() -> {
			
			boolean success = SwtBrowserCanvas.createInstance((SwtBrowserCanvas browser) -> {
				
				swtBrowser = browser;
				
				// Add SWT browser to the center of the panel.
				MonitorPanel.this.add(swtBrowser, BorderLayout.CENTER);
				return url;
			});
		
			if (!success) {
					
				// Remove SWT browser.
				swtBrowser = null;
				
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
	 * On tab panel change event.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
	}

	@Override
	public void beforeTabPanelRemoved() {
		
		if (swtBrowser != null) {
			swtBrowser.dispose();
		}
	}

	/**
	 * No tab text.
	 */
	@Override
	public String getTabDescription() {
		
		return "";
	}
	
	/**
	 * Update panel.
	 */
	@Override
	public void reload() {
		
	}

	@Override
	public TabState getTabState() {
		
		// Create new monitor tab state object
		MonitorTabState tabState = new MonitorTabState();
		
		// Set state object components and return the state
		tabState.url = url;
		
		return tabState;
	}
}
