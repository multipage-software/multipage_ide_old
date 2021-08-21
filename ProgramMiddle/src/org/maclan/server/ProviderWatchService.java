/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 15-04-2020
 *
 */
package org.maclan.server;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;

import org.maclan.MiddleUtility;
import org.maclan.Slot;

/**
 * 
 * @author user
 *
 */
public class ProviderWatchService {
	
	/**
	 * Watcher.
	 * @author user
	 *
	 */
	private static class Watcher {
		
		/**
		 * Watcher thread.
		 */
		private Thread thread;
		
		/**
		 * Watch service.
		 */
		private WatchService service;
		
		/**
		 * Watch key.
		 */
		private WatchKey key;
		
		/**
		 * Stop flag.
		 */
		private boolean stopThread = false;

		/**
		 * Start watch service thread.
		 */
		private void start(Path directory, BiConsumer<WatchEvent.Kind, String> eventConsumer) {
			
			String directoryText = directory.toString();
			
			// Create new thread.
			thread = new Thread(() -> {
				
				try {
					
					// Create watch service.
					service = FileSystems.getDefault().newWatchService();
					
					// Register directory in watch service.
					directory.register(service, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
					
					// Listen to events.
					while (!stopThread) {
						
						// Wait for key to be available.
						try {
							key = service.take();
						}
						catch (Exception e) {
							break;
						}
						
						for (WatchEvent<?> event : key.pollEvents()) {
							
							// Get event type and path.
							WatchEvent.Kind kind = event.kind();
							WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
							Path path= Paths.get(directoryText, pathEvent.context().toString());
							
							// Invoke callback.
							if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_DELETE) {
								eventConsumer.accept(kind, path.toString());
							}
						}
						
						// Reset key.
						key.reset();
					}
				}
				catch (Exception e) {
					System.err.format("ERROR: Watch Service exception : %s\n", e.getLocalizedMessage());
				}
				finally {
					
					try {
						service.close();
					}
					catch (Exception e) {
					}
				}
				
			}, "ProviderWatchService-" + directoryText);
			
			// Start thread.
			thread.start();
		}
		
		/**
		 * Stop watcher.
		 */
		public void stop() {
			
			stopThread = true;
			try {
				if (key != null) {
					key.cancel();
				}
			}
			catch (Exception e) {
			}
			try {
				if (service != null) {
					service.close();
				}
			}
			catch (Exception e) {
			}
			try {
				thread.join(200);
			}
			catch (Exception e) {
			}
			thread.interrupt();
		}
	}
	
	/**
	 * Watchers for directories.
	 */
	private static HashMap<String, Watcher> directoryWatchers = new HashMap<String, Watcher>();
	
	/**
	 * Paths to slots cache.
	 */
	private static HashMap<String, Long> pathsToSlots = new HashMap<String, Long>();
	
	/**
	 * Affected slots.
	 */
	public static HashSet<Long> affectedSlots = new HashSet<Long>();
	
	/**
	 * Main entry point. Register external provider.
	 * @param slot
	 */
	public void register(Slot slot) {
		
		// Check slot.
		String link = slot.getExternalProvider();
		if (link == null) {
			return;
		}
		
		// Get link path.
		Path path = MiddleUtility.getExternalLinkPath(link);
		if (path == null) {
			return;
		}
		
		// Get directory.
		Path directory = path.getParent();
		/*if (!directory.toFile().exists()) {
			return;
		}*/
		
		String directoryText = directory.toString();
		
		// Try to get exiting or start new directory watcher.
		synchronized (directoryWatchers) {
			
			Watcher watcher = directoryWatchers.get(directoryText);
			if (watcher == null) {
				
				watcher = new Watcher();
				watcher.start(directory, (kind, eventPath) -> {
					
					// On modified path event.
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						try {
							// Find slot.
							Long affectedSlotId = pathsToSlots.get(eventPath);
							if (affectedSlotId != null) {
								
								// Remember slot ID for which an external source code was changed.
								affectedSlots.add(affectedSlotId);
							}
						}
						catch (Exception e) {
						}
					}
				});
				directoryWatchers.put(directoryText, watcher);
			}
			
			// Update path / slot map.
			pathsToSlots.put(path.toString(), slot.getId());
		}
	}
	
	public static void clearCache() {
		
		directoryWatchers.clear();
		pathsToSlots.clear();
		affectedSlots.clear();
	}
	
	/**
	 * Unregister all watchers. Stop threads and clear caches.
	 */
	public static void unregisterAll() {
		
		stopAllWatchers();
		clearCache();
	}
	
	/**
	 * Stop all watchers.
	 */
	private static void stopAllWatchers() {
		
		synchronized (directoryWatchers) {
			
			directoryWatchers.forEach((path, watcher) -> {
				watcher.stop();
			});
		}
	}
	
	/**
	 * Stop watch services.
	 */
	@Override
	protected void finalize() throws Throwable {
		
		stopAllWatchers();
		System.err.format("WatchServices stopped.");
	}
}
