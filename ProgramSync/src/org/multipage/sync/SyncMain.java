/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 21-04-2020
 *
 */
package org.multipage.sync;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import org.multipage.gui.Images;
import org.multipage.gui.StateSerializer;
import org.multipage.gui.Utility;
import org.multipage.util.Lock;
import org.multipage.util.Obj;
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
	 * Program lock.
	 */
	private static Lock programLock = new Lock();
	
	/**
	 * Serialized data location.
	 */
	private static final String serilizedDataLocation = "org.multipage.sync.settings";
	
	/**
	 * Serializer
	 */
	private static StateSerializer serializer = new StateSerializer(serilizedDataLocation);
	
	/**
	 * Area server client with access string and its format.
	 */
	@SuppressWarnings("unused")
	private static AreaServerClient areaServerClient;

	/**
	 * A lambda function which closes main application. The function is set with setCloseEvent().
	 */
	private static Runnable closeMainApplicationEvent = null;
	
	/**
	 * Main application title.
	 */
	private static Supplier<String> mainApplicationTitleCallback = null;
	
	/**
	 * Reactivate GUI.
	 */
	private static Runnable reactivateGuiCallback = null;
	
	/**
	 * Set main application title callback.
	 * @param callback;
	 */
	public static void setMainApplicationTitleCallback(Supplier<String> callback) {
		
		mainApplicationTitleCallback = callback;
	}
	
	/**
	 * Get main application title.
	 * @return
	 */
	public static String getMainApplicationTitle() {
		
		// Check callback and possibly return a default value.
		if (mainApplicationTitleCallback == null) {
			return Resources.getString("org.multipage.sync.messageApplicationTitle");
		}
		
		return mainApplicationTitleCallback.get();
	}
	
	/**
	 * Set the reactivate GUI callback.
	 * @param callback
	 */
	public static void setReactivateGuiCallback(Runnable callback) {
		
		reactivateGuiCallback = callback;
	}
	
	/**
	 * Reactivate windows of the main GUI.
	 */
	public static void reactivateGui() {
		
		// Check the callback.
		if (reactivateGuiCallback == null) {
			return;
		}
		
		reactivateGuiCallback.run();
	}
	
	/**
	 * Initialize module.
	 * @param host
	 * @param port
	 * @param password 
	 * @param userDirectory 
	 */
	public static void setAccessString(String host, String user, String password) {
		
		// Delegate the call
		AreaServerClient.setAccessString(host, user, password);
	}
	
	/**
	 * Set close application event.
	 * @param event
	 */
	public static void setCloseEvent(Runnable event) {
		
		closeMainApplicationEvent = event;
	}
	
	/**
	 * Uninitialize the application.
	 */
	public static void unitialize() {
		
		serializer.startSavingSerializedStates();
	}
	
	/**
	 * Start watch service.
	 */
	public static void startService(boolean asynchronous)
			throws Exception {
		
		// Initialization.
		Obj<Exception> exception = new Obj<Exception>(null);
		
		// Create service thread
		Thread thread = new Thread(() -> {
			
			try {
				
				// Create tray menu.
				createTrayMenu();
				
				if (!asynchronous) {
					
					// Lock program.
					Lock.waitFor(programLock);
					
					// Exit log message.
					Utility.err("org.multipage.sync.messageProgramSyncStopped");
				}
			}
			catch (Exception e) {
				exception.ref = e;
			}
			
		}, "Sync-Service");
		
		// Start the new thread
		thread.start();
		thread.join();
		
		// Possibly throw exception
		if (exception.ref != null) {
			throw exception.ref;
		}
	}
	
	/**
	 * Create tray menu.
	 */
	private static void createTrayMenu() {
		
		// Load tray icon.
		BufferedImage image = Images.getImage("org/multipage/gui/images/main.png");
		if (image == null) {
			MessageDialog.show("org.multipage.sync.messageCannotLoadTrayIcon");
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
			areaServerClient = AreaServerClient.newInstance(popupMenu);
			//areaServerClient.loadMenu(false);
		}
		catch (Exception e) {
			MessageDialog.showDialog(e.getLocalizedMessage());
		}
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
	 * Close main application.
	 */
	public static void closeMainApplication() {
		
		// Delegate the call.
		if (closeMainApplicationEvent != null) {
			closeMainApplicationEvent.run();
		}
	}
}
