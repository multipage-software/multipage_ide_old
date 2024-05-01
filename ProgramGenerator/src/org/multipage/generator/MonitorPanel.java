/*
 * Copyright 2010-2019 (C) Vaclav Kolarcik
 * 
 * Created on : 26-04-2017
 *
 */
package org.multipage.generator;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.multipage.gui.ApplicationEvents;
import org.multipage.gui.GuiSignal;
import org.multipage.gui.Message;
import org.multipage.gui.NonCyclingReceiver;
import org.multipage.gui.UpdateSignal;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
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
public class MonitorPanel extends Panel implements TabItemInterface, NonCyclingReceiver {

	/**
	 * Version
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Regular expression for getting the requested area ID.
	 */
	private static final Pattern requestedAreaIdRegex = Pattern.compile("^.*?\\?.*?area_id=(?<areaId>\\d+).*$");
	
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
	 * List of previous messages.
	 */
	private LinkedList<Message> previousMessages = new LinkedList<Message>();

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
		openBrowser(url);
		
		setListeners();
	}

	/**
	 * Open browser.
	 */
	private void openBrowser(String url) {
		
		// TODO: <---FIX When this panel is removed the application exits.
		// Try to open SWT browser (with native code).
		SwingUtilities.invokeLater(() -> {
			
			swtBrowser = SwtBrowserCanvas.createLater(browserCanvas -> {
				
				// Add SWT browser into center of the panel.
				MonitorPanel.this.add(browserCanvas, BorderLayout.CENTER);
				
				// Load the URL provided.
				return url;
			}
			,
			urlChanged -> {
				onUrlChanged(urlChanged);
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
						
						// Set web engine action listeners.
						setWebEngineListeners(webEngine);
						
						webEngine.load(url);
						
						scene = new Scene(webViewBrowser, 750, 500, Color.web("#666970"));
						javaFxPanel.setScene(scene);
					}
				});
			}
		});
	}

	/**
	 * Callback that is called when browser URL changes.
	 * @param urlChanged
	 */
	private void onUrlChanged(String urlChanged) {
		
		// If there exists requested area ID, display the area properties.
		if (urlChanged == null || urlChanged.isEmpty()) {
			return;
		}
		
		Long currentAreaId = getCurrentAreaId(urlChanged);
		if (currentAreaId <= 0L) {
			return;
		}
		
		// Select new areas.
		HashSet<Long> selectedAreaIds = new HashSet<Long>();
		selectedAreaIds.add(currentAreaId);
		ApplicationEvents.transmit(MonitorPanel.this, GuiSignal.selectDiagramAreas, selectedAreaIds);
		
		// This patch resets SWT shells so that they do not grab input focus.
		ApplicationEvents.transmit(this, GuiSignal.resetSwtBrowser);
	}

	/**
	 * Set web engine listeners.
	 * @param webEngine2
	 */
	protected void setWebEngineListeners(WebEngine webEngine2) {
		
		try {
			ReadOnlyObjectProperty<javafx.concurrent.Worker.State> property = webEngine.getLoadWorker().stateProperty();
	
			ChangeListener<State> changeListener = new ChangeListener<State>() {
				@Override
				public void changed(ObservableValue<? extends State> arobserable, State oldState, State newState) {
					
					if (newState == State.SUCCEEDED) {
	                    String location = webEngine.getLocation();
	                    
	                    // TODO: <---FINISH IT
	                }
				}
			};
			property.addListener(changeListener);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set listeners.
	 */
	private void setListeners() {
		
		// Receive the "update" signal.
		ApplicationEvents.receiver(this, UpdateSignal.updateMonitorPanel, message -> {
			
			// Reload the browser.
			swtBrowser.reload();
			
			// If there exists requested area ID, display the area properties.
			String currentUrl = swtBrowser.getUrl();
			if (currentUrl == null || currentUrl.isEmpty()) {
				return;
			}
			
			Long currentAreaId = getCurrentAreaId(currentUrl);
			if (currentAreaId <= 0L) {
				return;
			}
			
			HashSet<Long> selectedAreaIds = new HashSet<Long>();
			selectedAreaIds.add(currentAreaId);
			ApplicationEvents.transmit(MonitorPanel.this, GuiSignal.selectDiagramAreas, selectedAreaIds);
		});
		
		// Receive the "reset SWT" signal.
		ApplicationEvents.receiver(this, GuiSignal.resetSwtBrowser, message -> {
			
			swtBrowser.enableSwt(false);
			swtBrowser.enableSwt(true);
		});
	}
	
	/**
	 * Return the requested area ID.
	 * @param url
	 * @return
	 */
	private Long getCurrentAreaId(String url) {
		
		try {
			Matcher matcher = requestedAreaIdRegex.matcher(url);
			boolean success = matcher.find();
			if (!success) {
				return -1L;
			}
			
			int groupCount = matcher.groupCount();
			if (groupCount != 1) {
				return -1L;
			}
			
			// Get areaId matching group value.
			String areaIdText = matcher.group("areaId");
			long areaId = Long.parseLong(areaIdText);
			
			return areaId;
		}
		catch (Exception e) {
		}
		return -1L;
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
		 
		// Close event listeners.
		ApplicationEvents.removeReceivers(this);
	}
	
	/**
	 * Recreate browser.
	 */
	public void recreateBrowser() {
		
		swtBrowser.close();
		
		MonitorPanel.this.remove(swtBrowser);
		
		swtBrowser = SwtBrowserCanvas.createLater(
			browser -> {
			
				// Add SWT browser to the center of the panel.
				MonitorPanel.this.add(browser, BorderLayout.CENTER);
				// Load the URL provided.
				return url;
			}
			,
			urlChanged -> {
				onUrlChanged(urlChanged);
			}
		);
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
	
	/**
	 * Get list of previous messages.
	 */
	@Override
	public LinkedList<Message> getPreviousMessages() {
		
		return previousMessages;
	}

	@Override
	public String getTabDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TabState getTabState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTabLabel(TabLabel tabLabel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAreaId(Long topAreaId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashSet<Long> getSelectedAreaIds() {
		// TODO Auto-generated method stub
		return null;
	}
}
