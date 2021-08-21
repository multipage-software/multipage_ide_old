/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.util.*;

/**
 * @author
 *
 */
public class RenderedFlag {
	
	/**
	 * Language ID.
	 */
	private long languageId;
	
	/**
	 * Absolute paths list.
	 */
	private LinkedList<String> absolutePaths = new LinkedList<String>();

	/**
	 * Constructor.
	 * @param languageId
	 */
	public RenderedFlag(long languageId) {
		
		this.languageId = languageId;
	}

	/**
	 * Add new absolute path.
	 * @param absolutePath
	 */
	private void add(String absolutePath) {
		
		for (String pathItem : absolutePaths) {
			
			if (pathItem.equals(absolutePath)) {
				return;
			}
		}
		
		absolutePaths.add(absolutePath);
	}

	/**
	 * Add rendered flag to set.
	 * @param renderedFlags
	 * @param languageId
	 * @param absolutePath
	 */
	public static void addToSet(HashMap<Long, RenderedFlag> renderedFlags, long languageId,
			String absolutePath) {
		
		// Get existing rendered flag.
		RenderedFlag renderedFlag = renderedFlags.get(languageId);
		
		if (renderedFlag == null) {
		
			// Create new rendered flag.
			renderedFlag = new RenderedFlag(languageId);
			renderedFlags.put(languageId, renderedFlag);
		}
		
		// Add path.
		renderedFlag.add(absolutePath);
	}

	/**
	 * Get language ID.
	 * @return
	 */
	public long getLanguageId() {
		
		return languageId;
	}

	/**
	 * Get absolute paths.
	 * @return
	 */
	public LinkedList<String> getAbsolutePaths() {
		
		return absolutePaths;
	}
}
