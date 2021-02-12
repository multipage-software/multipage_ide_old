/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.io.File;
import java.util.LinkedList;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class ResourceConstructor {
	
	/**
	 * Load info class.
	 */
	public abstract class LoadInfo {
		
		/**
		 * Get load description.
		 */
		public abstract String getLoadDescription();
	}
	
	/**
	 * File load info class.
	 */
	public class FileLoadInfo extends LoadInfo {

		/**
		 * File reference.
		 */
		public File file;
		
		/**
		 * Constructor.
		 * @param file2
		 */
		public FileLoadInfo(File file) {

			this.file = file;
		}

		/**
		 * Get description.
		 */
		@Override
		public String getLoadDescription() {
			
			if (file == null) {
				return null;
			}
			return file.getName();
		}
	}
	
	/**
	 * Link load info.
	 */
	public class LinkLoadInfo extends LoadInfo {

		/**
		 * Resource reference.
		 */
		public Resource resource;
		
		/**
		 * Constructor.
		 * @param resource
		 */
		public LinkLoadInfo(Resource resource) {
			
			this.resource = resource;
		}

		/**
		 * Get description.
		 */
		@Override
		public String getLoadDescription() {
			
			if (resource == null) {
				return null;
			}
			return String.format(Resources.getString("middle.textLoadInfoLinkDescription"), resource.getDescription());
		}
	}

	/**
	 * Description.
	 */
	private String description = "";
	
	/**
	 * Visibility.
	 */
	private boolean visibility;

	/**
	 * Save as text.
	 */
	private boolean saveAsText;
	
	/**
	 * Link ID (used in import process).
	 */
	private Long oldLinkId;

	/**
	 * Changed flag.
	 */
	private boolean changed = false;
	
	/**
	 * Load info from wizard. (Not exported or imported!)
	 */
	private LoadInfo loadInfo;

	/**
	 * Editable flag.
	 */
	private boolean editable;

	/**
	 * Set changed flag.
	 */
	public void setChanged() {
		
		changed = true;
	}
	
	/**
	 * Constructor.
	 */
	public ResourceConstructor() {
		
	}

	/**
	 * Constructor.
	 * @param description
	 */
	public ResourceConstructor(String description) {
		
		this.description = description;
	}

	/**
	 * Constructor.
	 * @param areaResource
	 */
	public ResourceConstructor(AreaResource areaResource) {
		
		this.description = areaResource.getLocalDescription();
		this.visibility = areaResource.isVisible();
		this.saveAsText = areaResource.isSavedAsText();
	}

	/**
	 * Clone object.
	 */
	public ResourceConstructor clone() {
		
		ResourceConstructor newResource = new ResourceConstructor();
		
		newResource.description = description;
		newResource.visibility = visibility;
		newResource.saveAsText = saveAsText;
		newResource.loadInfo = loadInfo;
		newResource.editable = editable;
		
		return newResource;
	}

	/**
	 * Set description.
	 * @param description
	 */
	public void setDescription(String description) {
		
		if (description == null) {
			description = "";
		}
		this.description  = description;
		
		setChanged();
	}

	/**
	 * Get description.
	 * @return
	 */
	public String getDescription() {
		
		return description;
	}

	/**
	 * Set visibility.
	 * @param visibility
	 */
	public void setVisibility(boolean visibility) {
		
		this.visibility = visibility;
		setChanged();
	}

	/**
	 * Get visibility.
	 * @return
	 */
	public boolean getVisibility() {
		
		return visibility;
	}

	/**
	 * @return the loadInfo
	 */
	public LoadInfo getLoadInfo() {
		return loadInfo;
	}
	
	/**
	 * Set load info.
	 * @param file
	 */
	public void setLoadInfo(File file) {
		
		loadInfo = new FileLoadInfo(file);
	}
	
	/**
	 * Clear load info.
	 */
	public void clearLoadInfo() {
		
		loadInfo = null;
	}
	
	/**
	 * Set load info.
	 * @param linkedResource
	 */
	public void setLoadInfo(Resource linkedResource) {
		
		loadInfo = new LinkLoadInfo(linkedResource);
	}
	
	/**
	 * Is load info empty.
	 */
	public boolean isLoadInfoEmpty() {
		
		return loadInfo == null;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Clear changed flag.
	 */
	public void clearChanged() {
		
		changed = false;
	}

	/**
	 * Set save as text flag.
	 * @param saveAsText
	 */
	public void setSaveAsText(boolean saveAsText) {
		
		this.saveAsText = saveAsText;
		
		setChanged();
	}

	/**
	 * Returns save as text flag.
	 * @return
	 */
	public boolean isSaveAsText() {
		
		return saveAsText;
	}

	/**
	 * Set link.
	 * @param resourceLink
	 */
	public void setLink(Resource resourceLink) {
		
		setLoadInfo(resourceLink);
		
		setChanged();
	}

	/**
	 * Get resource link.
	 * @return
	 */
	public Resource getLink() {
		
		if (!(loadInfo instanceof LinkLoadInfo)) {
			return null;
		}
		
		return ((LinkLoadInfo) loadInfo).resource;
	}

	/**
	 * Get linked resource id.
	 * @return
	 */
	public Long getLinkId() {
		
		Resource resourceLink = getLink();
		if (resourceLink == null) {
			return null;
		}
		
		return resourceLink.getId();
	}

	/**
	 * Set link ID.
	 * @param linkId
	 */
	public void setOldLinkId(Long linkId) {
		
		this.oldLinkId = linkId;
	}
	
	/**
	 * Get old link ID.
	 */
	public Long getOldLinkId() {
		
		return oldLinkId;
	}

	/**
	 * Set editable flag.
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		
		this.editable = editable;
	}

	/**
	 * Get editable flag.
	 * @return
	 */
	public boolean isEditable() {
		
		return editable;
	}

	/**
	 * Returns true value if an editable resource exists.
	 * @param resources
	 * @return
	 */
	public static boolean existEditableResource(
			LinkedList<ResourceConstructor> resources) {
		
		for (ResourceConstructor resource : resources) {
			if (resource.isEditable()) {
				return true;
			}
		}
		return false;
	}
}
