/*
 * Copyright 2010-2019 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.util.HashSet;

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
	 * A flag when it is set, informs about successful embedding of the native SWT browser
	 */
	private static Boolean nativeBrowserSuccessful = null;
	
	/**
	 * Web view objects.
	 */
    private WebView webViewBrowser = null;
    private WebEngine webEngine = null;
	private Scene scene = null;
	
	/**
	 * SWT view objects.
	 */
	private SwtBrowserCanvas swtBrowserNative = null;
	
	/**
	 * Home URL.
	 */
	private String url;
	
	/**
	 * A reference to the tab label
	 */
	private TabLabel tabLabel;
	
	/**
	 * Java FX panel reference
	 */
	private JFXPanel javaFxPanel;
	
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
			
			// On the very first pass of this method the following nativeBrowserSuccessful flag is not set
			if (nativeBrowserSuccessful == null || nativeBrowserSuccessful) {
				
				// Try to create SWT browser (with native libraries support for some of the operating systems)
				nativeBrowserSuccessful = SwtBrowserCanvas.createInstance((SwtBrowserCanvas browser) -> {
					
					swtBrowserNative = browser;
					
					// Add SWT browser to the center of the panel.
					MonitorPanel.this.add(swtBrowserNative, BorderLayout.CENTER);
					return url;
				});
			}
			
			// Try to create JavaFX browser
			if (!nativeBrowserSuccessful) {
					
				// Remove SWT browser.
				swtBrowserNative = null;
				
				// Otherwise use JavaFX panel for webViewBrowser.
				javaFxPanel = new JFXPanel();
				
				// Add JavaFX panel.
				add(javaFxPanel, BorderLayout.CENTER);
				
				// Initialize scene.
				Platform.setImplicitExit(false);
				Platform.runLater(() -> {
						
					webViewBrowser = new WebView();
					webEngine = webViewBrowser.getEngine();
					
					webEngine.load(url);
					
					scene = new Scene(webViewBrowser, 750, 500, Color.web("#666970"));
					javaFxPanel.setScene(scene);
				});
			}
		});
		
		// Set listeners
		setListeners();
	}
	
	/**
	 * Set listeners
	 */
	private void setListeners() {
		
		// The "update all" request receiver.
		ConditionalEvents.receiver(this, Signal.updateAll, message -> {
			
			// Reload content of the monitor.
			if (isShowing()) {
				
				// Disable the signal temporarily.
				Signal.updateAll.disable();
				
				// Reload the content.
				reloadContent();
				
				// Enable the signal.
				SwingUtilities.invokeLater(() -> {
					Signal.updateAll.enable();
				});
			}
		});
	}
	
	/**
	 * Reload content of the monitor
	 */
	private void reloadContent() {
		
		SwingUtilities.invokeLater(() -> {
			
			// Impose reload of current browser
			if (swtBrowserNative != null) {
				swtBrowserNative.reload();
			}
			else {
				Platform.runLater(() -> {
					webEngine.reload();
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
		 
		// Remove receivers for the panel.
		ConditionalEvents.removeReceivers(this);
	}
	
	/**
	 * On tab panel change event.
	 */
	@Override
	public void onTabPanelChange(ChangeEvent e, int selectedIndex) {
		
	}

	@Override
	public void beforeTabPanelRemoved() {
		
		if (swtBrowserNative != null) {
			swtBrowserNative.dispose();
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
		
		// Set title
		tabState.title = tabLabel.getDescription();
		
		// Set state object components and return the state
		tabState.url = url;
		
		// Reset the area ID
		tabState.areaId = -1L;
		
		return tabState;
	}
	
	/**
	 * Set reference to a tab label
	 */
	@Override
	public void setTabLabel(TabLabel tabLabel) {
		
		this.tabLabel = tabLabel;
	}
	
	/**
	 * Set top area ID
	 */
	@Override
	public void setAreaId(Long topAreaId) {
		
	}
	
	/**
	 * Get selected area IDs.
	 */
	@Override
	public HashSet<Long> getSelectedAreaIds() {
		
		return null;
	}
}
