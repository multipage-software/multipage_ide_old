/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * @author
 *
 */
public class Resource extends NamespaceElement {

	/**
	 * MIME type ID.
	 */
	protected long mimeTypeId;
	
	/**
	 * Visible.
	 */
	protected boolean visible;
	
	/**
	 * BLOB identifier.
	 */
	protected long blob;
	
	/**
	 * Buffered image.
	 */
	protected BufferedImage image;
	
	/**
	 * File length.
	 */
	protected long length = 0L;

	/**
	 * Is saved as text.
	 */
	protected boolean isSavedAsText = false;

	/**
	 * Is protected flag.
	 */
	protected boolean isProtected = false;

	/**
	 * Image size.
	 */
	private Dimension imageSize;
	
	/**
	 * Constructor.
	 * @param id
	 * @param namespaceId
	 * @param description
	 * @param mimeTypeId
	 * @param visible
	 * @param isProtected
	 * @param blob
	 * @param length
	 * @param isSavedAsText
	 */
	public Resource(long id, long namespaceId, String description,
			long mimeTypeId, boolean visible, boolean isProtected, 
			Long blob, Long fileLength, boolean isSavedAsText) {
		
		this(id, namespaceId, description, mimeTypeId, visible,
				isProtected, blob, isSavedAsText);
		
		this.length = fileLength;
	}

	/**
	 * Constructor.
	 * @param id
	 * @param namespaceId
	 * @param description
	 * @param mimeTypeId
	 * @param visible
	 * @param isProtected
	 * @param blob
	 * @param isSavedAsText
	 */
	public Resource(long id, long namespaceId, String description,
			long mimeTypeId, boolean visible, boolean isProtected,
			Long blob, boolean isSavedAsText) {

		this.id = id;
		this.parentNamespaceId = namespaceId;
		this.description = description;
		this.mimeTypeId = mimeTypeId;
		this.visible = visible;
		this.isProtected = isProtected;
		this.blob = blob == null ? 0L : blob;
		this.isSavedAsText = isSavedAsText;
	}

	/**
	 * Constructor.
	 * @param description
	 */
	public Resource(String description) {
		this(0L, 0L, description, 0L, false, false, 0L, true);
	}

	/**
	 * Constructor.
	 */
	public Resource() {
		this(0L, 0L, "", 0L, false, false, 0L, true);
	}

	/**
	 * Clone object.
	 */
	public Resource clone() {
		
		return new Resource(id, parentNamespaceId, description, mimeTypeId,
				visible, isProtected, blob, isSavedAsText);
	}
	
	/**
	 * @return the mimeTypeId
	 */
	public long getMimeTypeId() {
		return mimeTypeId;
	}

	/**
	 * @param mimeTypeId the mimeTypeId to set
	 */
	public void setMimeTypeId(long mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return description;
	}

	/**
	 * @param blob the blob to set
	 */
	public void setBlob(long blob) {
		this.blob = blob;
	}

	/**
	 * @return the blob
	 */
	public long getBlob() {
		return blob;
	}

	/**
	 * Set resource.
	 * @param resource
	 */
	public void setFrom(Resource resource) {

		id = resource.id;
		parentNamespaceId = resource.parentNamespaceId;
		description = resource.description;
		mimeTypeId = resource.mimeTypeId;
		visible = resource.visible;
		isProtected = resource.isProtected;
		blob = resource.blob;
		image = resource.image;
		length = resource.length;
		isSavedAsText = resource.isSavedAsText;
	}

	/**
	 * Saved as text flag.
	 * @return
	 */
	public boolean isSavedAsText() {

		return isSavedAsText;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * Get file length.
	 * @return
	 */
	public long getLength() {
		
		return length;
	}

	/**
	 * Set length.
	 * @param length
	 */
	public void setLength(Long length) {
		
		this.length = length == null ? 0L : length;
	}
	
	/**
	 * @param isSavedAsText the isSavedAsText to set
	 */
	public void setSavedAsText(boolean isSavedAsText) {
		this.isSavedAsText = isSavedAsText;
	}

	/**
	 * Get protected flag.
	 * @return
	 */
	public boolean isProtected() {
		
		return isProtected;
	}
	
	/**
	 * Set protected flag.
	 * @param isProtected
	 */
	public void setProtected(boolean isProtected) {
		
		this.isProtected = isProtected;
	}

	/**
	 * Get image dimensions.
	 * @return
	 */
	public Dimension getImageSize() {

		if (imageSize != null) {
			return imageSize;
		}
		
		// Try to compute it from the image.
		if (image != null) {
			
			int width = image.getWidth();
			int height = image.getHeight();
			
			setImageSize(width, height);
		}
		
		return imageSize;
	}

	/**
	 * Set image size.
	 * @param width
	 * @param height
	 */
	public void setImageSize(int width, int height) {
		
		imageSize = new Dimension(width, height);
	}
	
	/**
	 * Set image size.
	 * @param size
	 */
	public void setImageSize(Dimension size) {
		
		imageSize = size;
	}
}
