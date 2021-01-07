/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

/**
 * @author
 *
 */
public class AreaResource extends Resource {

	/**
	 * Area reference.
	 */
	private AreaResourceExtension extension = new AreaResourceExtension();

	/**
	 * Constructor.
	 * @param id
	 * @param namespaceId
	 * @param description
	 * @param mimeTypeId
	 * @param visible
	 * @param isProtected
	 * @param blob
	 * @param localDescription 
	 * @param isSavedAsText
	 */
	public AreaResource(long id, long namespaceId, String title,
			long mimeTypeId, boolean visible, boolean isProtected,
			Long blob, Area area, String localDescription,
			boolean isSavedAsText) {
		
		super(id, namespaceId, title, mimeTypeId, visible, isProtected, blob, isSavedAsText);
		
		this.extension.area = area;
		this.extension.localDescription = localDescription;
	}

	/**
	 * Constructor.
	 * @param id
	 * @param namespaceId
	 * @param description
	 * @param mimeTypeId
	 * @param visible
	 * @param isProtected 
	 * @param blobId
	 * @param fileLength
	 * @param area
	 * @param localDescription
	 * @param isSavedAsText
	 */
	public AreaResource(long id, long namespaceId, String description,
			long mimeTypeId, boolean visible, boolean isProtected,
			Long blobId, Long fileLength, Area area,
			String localDescription, boolean isSavedAsText) {
		
		super(id, namespaceId, description, mimeTypeId, visible, isProtected,
				blobId, fileLength, isSavedAsText);
		
		this.extension.area = area;
		this.extension.localDescription = localDescription;
	}

	/**
	 * Constructor.
	 * @param name
	 */
	public AreaResource(String name) {

		super(name);
	}

	/**
	 * Constructor.
	 */
	public AreaResource() {

		super();
	}

	/**
	 * Constructor.
	 * @param resource
	 * @param area
	 * @param localDescription
	 */
	public AreaResource(Resource resource, Area area, String localDescription) {
		
		super.setFrom(resource);
		
		this.extension.area = area;
		this.extension.localDescription = localDescription;
	}

	/**
	 * Constructor.
	 * @param resource
	 * @param extension
	 */
	public AreaResource(Resource resource, AreaResourceExtension extension) {
		
		this(resource, extension.area, extension.localDescription);
	}

	/**
	 * Set local description.
	 */
	public void setLocalDescription(String localDescription) {
		this.extension.localDescription = localDescription;
	}

	/**
	 * Get local description.
	 */
	public String getLocalDescription() {
		return extension.localDescription;
	}
	
	/**
	 * Get area.
	 */
	public Area getArea() {
		return extension.area;
	}
	
	/**
	 * Clone object.
	 */
	public AreaResource clone() {
		
		AreaResource resource = new AreaResource(id, parentNamespaceId, description, mimeTypeId,
				visible, isProtected, blob, extension.area, extension.localDescription,
				isSavedAsText);
		
		resource.extension.id = extension.id;
		
		return resource;
	}

	/**
	 * Set resource.
	 * @param resource
	 */
	public void setFrom(AreaResource resource) {

		super.setFrom(resource);
		
		extension.area = resource.extension.area;
		extension.localDescription = resource.extension.localDescription;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		if (!extension.localDescription.isEmpty()) {
			return extension.localDescription;
		}
		else {
			return description;
		}
	}

	/**
	 * Create new base resource.
	 * @return
	 */
	public Resource newBaseResource() {
		
		Resource resource = super.clone();
		
		return resource;
	}

	/**
	 * Get area resource extension.
	 * @return
	 */
	public AreaResourceExtension getExtension() {
		
		return extension;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof AreaResource)) {
			return false;
		}
		
		AreaResource resource = (AreaResource) obj;
		
		if (!resource.id.equals(id)) {
			return false;
		}
		
		if (resource.extension == null && extension == null) {
			return true;
		}
		
		if (resource.extension == null || extension == null) {
			return false;
		}
		
		return resource.extension.equals(extension);
	}
}
