/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan.server;

import java.util.LinkedList;

/**
 * @author
 *
 */
public class RenderedResource {

	/**
	 * Constructor types.
	 */
	public static final int EXTENSION = 0;
	public static final int FILENAME = 1;
	public static final int ORIGINAL_FILENAME = 2;
	
	/**
	 * Resource ID.
	 */
	private long resourceId;
	
	/**
	 * File name.
	 */
	private String fileName;
	
	/**
	 * Extension. (Is used to compile file name "resXX.EXTENSION".)
	 */
	private String extension;
	
	/**
	 * Rendered path.
	 */
	private LinkedList<String> renderedPaths = new LinkedList<String>();

	/**
	 * Constructor.
	 * @param resourceId
	 */
	public RenderedResource(long resourceId, String fileOrExt, int type) {
		
		this.resourceId = resourceId;
		
		// TODO: needs refactoring.
		if (type == FILENAME) {
			fileName = fileOrExt;
		}
		else if (type == EXTENSION) {
			extension = fileOrExt;
		}
		// On ORIGINAL_FILENAME
		else {
			fileName = fileOrExt;
		}
	}

	/**
	 * Constructor.
	 * @param resourceId
	 * @param fileName
	 */
	public RenderedResource(long resourceId, String fileName) {
		
		this.resourceId = resourceId;
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RenderedResource)) {
			return false;
		}
		RenderedResource resource = (RenderedResource) obj;
		
		return resourceId == resource.resourceId;
	}

	/**
	 * Get resource ID.
	 * @return
	 */
	public long getId() {
		
		return resourceId;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Add rendered path.
	 */
	public void addRenderedPath(String renderedPath) {
		
		// Find existing path.
		for (String pathItem : renderedPaths) {
			
			if (pathItem.equalsIgnoreCase(renderedPath)) {
				return;
			}
		}
		
		renderedPaths.add(renderedPath);
	}

	/**
	 * @return the renderedPaths
	 */
	public LinkedList<String> getRenderedPaths() {
		return renderedPaths;
	}
}
