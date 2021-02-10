/*
 * Copyright 2010-2021 (C) vakol (see attached LICENSE file for additional info)
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import org.multipage.gui.Images;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Resources;

/**
 * External source code providers watch service.
 * @author user
 *
 */
public class SyncMain {
	
	/**
	 * Is standalone application flag.
	 */
	public static boolean isStandalone = false;
	
	/**
	 * Tray menu icon.
	 */
	private static TrayIcon trayIcon;
	
	/**
	 * Popup menu.
	 */
	private static PopupMenu popupMenu;
	
	/**
	 * Watcher thread.
	 */
	private static Thread watcherThread;
	
	/**
	 * Flag that stops watcher thread.
	 */
	private static boolean stopWatcher = false;
	
	/**
	 * Program lock.
	 */
	private static Lock programLock = new Lock();
	
	/**
	 * Area server client with access string and its format.
	 */
	private static AreaServerClient areaServerClient;
	private static String accessString;
	public static final String accessStringFormat = "%s;usr=%s;pwd=%s";
	
	/**
	 * Initialize module.
	 * @param args
	 */
	public static void initialize(String[] args)
		throws Exception {
		
		// Get Area server access string.
		accessString = acquireAccessString(args);
	}
	
	/**
	 * Initialize module.
	 * @param host
	 * @param port
	 * @param password 
	 */
	public static void initialize(String host, String user, String password) {
		
		accessString = String.format(accessStringFormat, host, user, password);
	}
	
	/**
	 * Start watch service.
	 */
	public static void startService(boolean asynchronous)
			throws Exception {
		
		// Start watch service.
		startWatchService();
		
		// Create tray menu.
		createTrayMenu();
		
		if (!asynchronous) {
			// Lock program.
			Lock.waitFor(programLock);
			
			// Exit log message.
			Utility.err("org.multipage.sync.messageProgramSyncStopped");
		}
	}
	
	/**
	 * Get area server access string.
	 */
	private static String acquireAccessString(String[] programArgs)
		throws Exception {
		
		// Try get from program arguments.
		String accessString = (programArgs.length >= 3 ? programArgs[2] : null);
		
		// Try to get from user input.
		if (accessString == null) {
			accessString = AccessStringFrame.showFrame();
		}
		
		return accessString;
	}

	/**
	 * Create tray menu.
	 */
	private static void createTrayMenu() {
		
		// Load tray icon.
		BufferedImage image = Images.getImage("org/multipage/gui/images/main.png");
		if (image == null) {
			Utility.show("org.multipage.sync.messageCannotLoadTrayIcon");
		}
		trayIcon = new TrayIcon(image);
		
		// Get system tray.
		SystemTray tray = SystemTray.getSystemTray();
		
		try {
			// Create and attach popupMenu menu.
			popupMenu = new PopupMenu();
			trayIcon.setPopupMenu(popupMenu);
			tray.add(trayIcon);
			
			// Load menu from area server.
			areaServerClient.loadMenu(popupMenu);
			// Add default menu items.
			loadDefaultMenu(popupMenu);
			
			// Menu item for program termination.
			if (isStandalone) {
				Utility.addPopupMenuItem(popupMenu, "org.multipage.sync.menuExitApplication", e -> {
					stop(trayIcon);
				});
			}
		}
		catch (Exception e) {
			Utility.show2(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Load default menu items.
	 * @param popupMenu
	 */
	private static void loadDefaultMenu(final PopupMenu popup) {
		
		// Add separator.
		int itemCount = popup.getItemCount();
		if (itemCount > 0) {
			popup.addSeparator();
		}
		
		// Update menu.
		MenuItem menuUpdate = new MenuItem(Resources.getString("org.multipage.sync.menuReloadMenu"));
		popup.add(menuUpdate);
		
		popup.addActionListener((e) -> {
			reloadMenu(popup);
		});
	}
	
	/**
	 * Reload tray menu.
	 */
	private static void reloadMenu(PopupMenu popup) {
		
		// Remove old menu items.
		popup.removeAll();
		// Load menu from area server.
		try {
			areaServerClient.loadMenu(popup);
		}
		catch (Exception e) {
			Utility.show("org.multipage.sync.messageCannotLoadMenu", e.getLocalizedMessage());
		}
		// Add default menu items.
		loadDefaultMenu(popup);
	}
	
	/**
	 * Update Sync.
	 */
	public static void update() {
		
		// Update popup menu.
		reloadMenu(popupMenu);
	}
	
	/**
	 * Stop watch service.
	 */
	public static void stop() {
		
		stop(trayIcon);
	}
	
	/**
	 * Stop watch service.
	 * @param trayIcon 
	 */
	private static void stop(TrayIcon trayIcon) {
		
		try {
			// Stop watch service.
			stopWatchService();
			
			// Stop tray icon.
			SystemTray tray = SystemTray.getSystemTray();
			tray.remove(trayIcon);
			
			// Inform user.
			if (isStandalone) {
				Utility.show("org.multipage.sync.messageProgramSyncStopped");
			}
		}
		catch (Exception e) {
			Utility.show2(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Start watch service.
	 */
	private static void startWatchService()
			throws Exception {
		
		// Get area server client.
		areaServerClient = AreaServerClient.newInstance(accessString);
		
		// Lambda function for thread.
		Runnable runnable = () -> {
			
			// Main loop.
			while (!stopWatcher) {
				
				
			}
			
			// Write log message.
			Utility.err("org.multipage.sync.messageWatherThreadStopped");
		};
		
		// Start watcher loop.
		watcherThread = new Thread(runnable, "WatcherThread");
		watcherThread.start();
	}
	
	/**
	 * Stop watch service.
	 */
	private static void stopWatchService() {
		
		try {
			stopWatcher = true;
			watcherThread.join(1000);
		}
		catch (Exception e) {
		}
		finally {
			watcherThread.interrupt();
		}
		
		Lock.notify(programLock);
	}
}
