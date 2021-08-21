/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.*;
import java.util.Map.Entry;

/**
 * Server cache.
 * @author
 *
 */
public class ServerCache {

	/**
	 * Enable flag.
	 */
	private boolean enabled = false;
	
	/**
	 * Cached areas.
	 */
	private Hashtable<Long, Area> cachedAreas = new Hashtable<Long, Area>();
	
	/**
	 * Cached home area.
	 */
	private Area cachedHomeArea;
	
	/**
	 * Cached resources.
	 */
	private Hashtable<Long, Resource> cachedResources = new Hashtable<Long, Resource>();
	
	/**
	 * Cached area resources' extensions.
	 */
	private Hashtable<Long, LinkedList<AreaResourceExtension>> cachedAreaResourceExtensions = new Hashtable<Long, LinkedList<AreaResourceExtension>>();
	
	/**
	 * Cached MIME types.
	 */
	private Hashtable<Long, MimeType> cachedMimeTypes = new Hashtable<Long, MimeType>();

	/**
	 * Cached start language ID.
	 */
	private Long cachedStartLanguageId = null;

	/**
	 * Cached start resources.
	 */
	private LinkedList<StartResource> cachedStartResources = new LinkedList<StartResource>();
	
	/**
	 * Cached area start resources.
	 */
	private Hashtable<Long, Hashtable<Long, StartResource>> cachedAreaStartResourcesVersions = new Hashtable<Long, Hashtable<Long, StartResource>>();
	
	/**
	 * Cached versions.
	 */
	private LinkedList<VersionObj> cachedVersions = new LinkedList<VersionObj>();
	
	/**
	 * Cached enumerations.
	 */
	private Hashtable<Long, EnumerationObj> cachedEnumerations = new Hashtable<Long, EnumerationObj>();
	
	/**
	 * Clear cache.
	 */
	public void clear() {
		
		cachedAreas.clear();
		cachedHomeArea = null;
		cachedStartLanguageId = null;
		cachedResources.clear();
		cachedMimeTypes.clear();
		cachedStartResources.clear();
		cachedAreaStartResourcesVersions.clear();
		cachedVersions.clear();
	}
	
	/**
	 * Get cached area.
	 * @param areaId
	 * @return
	 */
	public Area getArea(long areaId) {

		if (!enabled) {
			return null;
		}
		return cachedAreas.get(areaId);
	}

	/**
	 * Put new area.
	 * @param newArea
	 */
	public void putArea(Area newArea) {
		
		if (!enabled) {
			return;
		}
		cachedAreas.put(newArea.getId(), newArea);
	}

	/**
	 * Put sub area.
	 * @param area
	 * @param subAreaId
	 * @param description
	 * @param visible
	 * @param localized
	 * @param alias
	 * @param fileName
	 * @param versionId 
	 * @param folder 
	 * @param inheritance
	 * @param recursion
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param constructorHolderId 
	 * @param fileExtension 
	 * @param projectRoot 
	 * @param isStartResource 
	 */
	public void putSubArea(Area area, long subAreaId, String description, boolean visible,
			boolean localized, String alias, String fileName, long versionId, String folder,
			boolean isConstructor, boolean inheritance,
			boolean recursion, String relationNameSub, String relationNameSuper,
			Long constructorHolderId, String fileExtension, Boolean projectRoot,
			boolean isStartResource) {
		
		Area subArea;
		
		if (enabled) {
		
			subArea = cachedAreas.get(subAreaId);
			if (subArea == null) {
				subArea = new Area();
				cachedAreas.put(subAreaId, subArea);
			}
		}
		else {
			subArea = new Area();
		}
		
		// Set sub area.
		subArea.setId(subAreaId);
		subArea.setVisible(visible);
		subArea.setLocalized(localized);
		subArea.setDescription(description);
		subArea.setAlias(alias);
		if (fileName != null && !fileName.isEmpty()) {
			subArea.setFileName(fileName);
		}
		subArea.setVersionId(versionId);
		subArea.setFolder(folder);
		subArea.setIsConstructorArea(isConstructor);
		subArea.setConstructorHolderId(constructorHolderId);
		subArea.setFileExtension(fileExtension);
		subArea.setProjectRoot(projectRoot);
		subArea.setIsStartResource(isStartResource);
		
		area.addSubareaLight(subArea, inheritance, relationNameSub, relationNameSuper,
				false, recursion);
	}

	/**
	 * Put super area.
	 * @param area
	 * @param superAreaId
	 * @param description
	 * @param visible
	 * @param localized
	 * @param alias
	 * @param fileName
	 * @param versionId 
	 * @param folder 
	 * @param inheritance
	 * @param recursion
	 * @param relationNameSuper
	 * @param relationNameSub
	 * @param constructorHolderId 
	 * @param fileExtension 
	 * @param projectRoot 
	 * @param isStartResource 
	 */
	public void putSuperArea(Area area, long superAreaId, String description, boolean visible,
			boolean localized, String alias, String fileName, long versionId, String folder,
			boolean isConstructor, boolean inheritance,
			boolean recursion, String relationNameSuper, String relationNameSub,
			Long constructorHolderId, String fileExtension, Boolean projectRoot, boolean isStartResource) {
		
		Area superArea;
		
		if (enabled) {
		
			superArea = cachedAreas.get(superAreaId);
			if (superArea == null) {
				superArea = new Area();
				cachedAreas.put(superAreaId, superArea);
			}
		}
		else {
			superArea = new Area();
		}
	
		// Set super area.
		superArea.setId(superAreaId);
		superArea.setVisible(visible);
		superArea.setDescription(description);
		superArea.setAlias(alias);
		superArea.setLocalized(localized);
		if (fileName != null && !fileName.isEmpty()) {
			superArea.setFileName(fileName);
		}
		superArea.setVersionId(versionId);
		superArea.setFolder(folder);
		superArea.setIsConstructorArea(isConstructor);
		superArea.setConstructorHolderId(constructorHolderId);
		superArea.setFileExtension(fileExtension);
		superArea.setProjectRoot(projectRoot);
		superArea.setIsStartResource(isStartResource);
		
		area.addSuperareaLight(superArea, inheritance, relationNameSub, relationNameSuper,
				false, recursion);
	}

	/**
	 * Get area ID
	 * @param alias
	 * @return
	 */
	public Area getArea(String alias) {
		
		if (!enabled) {
			return null;
		}
		
		Enumeration<Area> areas = cachedAreas.elements();
		while (areas.hasMoreElements()) {
			Area area = areas.nextElement();
			if (area.getAlias().equals(alias)) {
				return area;
			}
		}
		return null;
	}

	/**
	 * Get home area.
	 * @return
	 */
	public Area getHomeArea() {
		
		if (!enabled) {
			return null;
		}
		
		return cachedHomeArea;
	}

	/**
	 * Set home area.
	 * @param area
	 */
	public void setHomeArea(Area area) {
		
		if (!enabled) {
			return;
		}
		
		cachedHomeArea = area;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get resource with given ID.
	 * @param resourceId
	 * @return
	 */
	public Resource getResource(long resourceId) {
		
		if (!enabled) {
			return null;
		}
		
		return cachedResources.get(resourceId);
	}

	/**
	 * Get MIME type with given ID.
	 * @param mimeTypeId
	 * @return
	 */
	public MimeType getMimeType(long mimeTypeId) {
		
		if (!enabled) {
			return null;
		}
		
		return cachedMimeTypes.get(mimeTypeId);
	}

	/**
	 * Put MIME type.
	 * @param mimeType
	 */
	public void putMimeType(MimeType mimeType) {
		
		if (!enabled) {
			return;
		}
		
		cachedMimeTypes.put(mimeType.getId(), mimeType);
	}

	/**
	 * Get start language ID.
	 * @return
	 */
	public Long getStartLanguageId() {
		
		if (!enabled) {
			return null;
		}
		return cachedStartLanguageId;
	}

	/**
	 * Set start language ID.
	 * @param ref
	 */
	public void setStartLanguageId(long startLanguageId) {
		
		cachedStartLanguageId = startLanguageId;
	}

	/**
	 * Set start resource.
	 * @param areaId
	 * @param versionId 
	 * @param startResource
	 */
	public void putStartResource(long areaId, long versionId, StartResource startResource) {
		
		if (!enabled) {
			return;
		}
				
		boolean found = false;
		
		// Try to find start resource.
		for (StartResource cachedStartResource : cachedStartResources) {
			
			if (startResource.resourceId == cachedStartResource.resourceId) {
				startResource = cachedStartResource;
				found = true;
				break;
			}
		}
		
		if (!found) {
			cachedStartResources.add(startResource);
		}
		
		// Get resources versions.
		Hashtable<Long, StartResource> startResourcesVersions = cachedAreaStartResourcesVersions.get(areaId);
		if (startResourcesVersions == null) {
			
			// Create new versions table.
			startResourcesVersions = new Hashtable<Long, StartResource>();
			
			// Save area start resources versions table.
			cachedAreaStartResourcesVersions.put(areaId, startResourcesVersions);
		}
		
		// Save version resource.
		startResourcesVersions.put(versionId, startResource);
	}

	/**
	 * Get area start resource
	 * @param versionId 
	 * @param area
	 * @return
	 */
	public StartResource getStartResource(long areaId, long versionId) {
		
		if (!enabled) {
			return null;
		}
		
		Hashtable<Long, StartResource> startResourcesVersions = cachedAreaStartResourcesVersions.get(areaId);
		if (startResourcesVersions == null) {
			return null;
		}
		
		return startResourcesVersions.get(versionId);
	}

	/**
	 * Get version object reference.
	 * @param versionId
	 * @return
	 */
	public VersionObj getVersion(long versionId) {
		
		if (!enabled) {
			return null;
		}
		
		// Find version.
		for (VersionObj cachedVersion : cachedVersions) {
			
			if (cachedVersion.getId() == versionId) {
				return cachedVersion;
			}
		}
		return null;
	}

	/**
	 * Get version object reference.
	 * @param versionAlias
	 * @return
	 */
	public VersionObj getVersion(String versionAlias) {
		
		if (!enabled) {
			return null;
		}
		
		// Find version.
		for (VersionObj cachedVersion : cachedVersions) {
			
			if (cachedVersion.getAlias().equals(versionAlias)) {
				return cachedVersion;
			}
		}
		return null;
	}

	/**
	 * Put version to list.
	 * @param version
	 */
	public void putVersion(VersionObj version) {
		
		if (!enabled) {
			return;
		}
		cachedVersions.add(version);
	}

	/**
	 * Get area resource.
	 * @param resourceName
	 * @param area
	 * @return
	 */
	public AreaResource getAreaResource(String resourceName, Area area) {
		
		if (!enabled) {
			return null;
		}
		
		// Try to find resources with given description.
		for (Entry<Long, Resource> entry : cachedResources.entrySet()) {
			
			Resource resource = entry.getValue();

			// Get area resource extensions.
			LinkedList<AreaResourceExtension> extensions = cachedAreaResourceExtensions.get(resource.getId());
			
			if (extensions != null) {
				for (AreaResourceExtension extension : extensions) {
					
					if (extension.area.equals(area)) {
						
						if ((!extension.localDescription.isEmpty() && extension.localDescription.equals(resourceName))
								|| (extension.localDescription.isEmpty() && resource.description.equals(resourceName))) {
						
							return new AreaResource(resource, extension);
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Put area resource.
	 * @param areaResource
	 */
	public void putAreaResource(AreaResource areaResource) {
		
		if (!enabled) {
			return;
		}
		
		long resourceId = areaResource.getId();
		
		// Try to get existing resource.
		Resource resource = cachedResources.get(resourceId);
		
		if (resource == null) {
			resource = areaResource.newBaseResource();
			cachedResources.put(resourceId, resource);
		}
		
		AreaResourceExtension resourceExtension = areaResource.getExtension();
		
		// Try to get resource extension.
		LinkedList<AreaResourceExtension> extensions = cachedAreaResourceExtensions.get(resourceId);
		if (extensions == null) {
			extensions = new LinkedList<AreaResourceExtension>();
			cachedAreaResourceExtensions.put(resourceId, extensions);
		}
		
		for (AreaResourceExtension extension : extensions) {
			
			if (extension.equals(resourceExtension)) {
				return;
			}
		}
		
		// Put area and resource extension.
		putArea(areaResource.getArea());
		
		extensions.add(resourceExtension);
	}

	/**
	 * Put resource.
	 * @param resource
	 */
	public void putResource(Resource resource) {
		
		if (!enabled) {
			return;
		}
		
		cachedResources.put(resource.getId(), resource);
	}

	/**
	 * Get area resource extension.
	 * @param resourceId
	 * @param areaId
	 * @return
	 */
	private AreaResourceExtension getAreaResourceExtension(long resourceId,
			long areaId) {
		
		if (!enabled) {
			return null;
		}
		
		LinkedList<AreaResourceExtension> extensions = cachedAreaResourceExtensions.get(resourceId);
		if (extensions == null) {
			return null;
		}
		
		for (AreaResourceExtension extension : extensions) {
			
			if (extension.area.getId() == areaId) {
				return extension;
			}
		}
		
		return null;
	}
	
	/**
	 * Get area resource.
	 * @param resourceId
	 * @param areaId
	 * @return
	 */
	public AreaResource getAreaResource(long resourceId, long areaId) {

		if (!enabled) {
			return null;
		}
		
		// Get cached resource.
		Resource resource = cachedResources.get(resourceId);
		
		if (resource == null) {
			return null;
		}
		
		// Get cached extension.
		AreaResourceExtension extension = getAreaResourceExtension(resourceId, areaId);
		
		if (extension == null) {
			return null;
		}
		
		return new AreaResource(resource, extension);
	}

	/**
	 * Get enumeration value.
	 * @param enumerationValueId
	 * @return
	 */
	public EnumerationValue getEnumerationValue(long enumerationValueId) {

		if (!enabled) {
			return null;
		}
		
		// Do loop for all cached enumerations.
		for (EnumerationObj enumeration : cachedEnumerations.values()) {
			
			EnumerationValue enumerationValue = enumeration.getValue(enumerationValueId);
			if (enumerationValue != null) {
				
				return enumerationValue;
			}
		}
		
		return null;
	}

	/**
	 * Put enumeration.
	 * @param enumerationId
	 * @param enumerationDescription
	 * @param enumerationValueId
	 * @param enumerationValueText
	 */
	public void putEnumeration(long enumerationId,
			String enumerationDescription, long enumerationValueId,
			String enumerationValueText) {
		
		// Check cache.
		if (!enabled) {
			return;
		}
		
		// Try to get existing enumeration.
		EnumerationObj enumeration = cachedEnumerations.get(enumerationId);
		if (enumeration == null) {
			enumeration = new EnumerationObj(enumerationId, enumerationDescription);
			
			cachedEnumerations.put(enumerationId, enumeration);
		}
		
		// Add enumeration value.
		enumeration.insertValue(enumerationValueId, enumerationValueText);
	}

	/**
	 * Get enumeration value object reference.
	 * @param description
	 * @param value
	 * @return
	 */
	public EnumerationValue getEnumerationValue(String description, String value) {
		
		// Check cache.
		if (!enabled) {
			return null;
		}
		
		// Find enumeration with given description.
		for (EnumerationObj enumeration : cachedEnumerations.values()) {
			
			if (enumeration.getDescription().equals(description)) {
				EnumerationValue enumerationValue = enumeration.getValue(value);
				
				return enumerationValue;
			}
		}
		
		return null;
	}

	/**
	 * Get enumeration.
	 * @param description
	 * @return
	 */
	public EnumerationObj getEnumeration(String description) {
			
		// Check cache.
		if (!enabled) {
			return null;
		}
		
		// Do loop for all enumerations.
		for (EnumerationObj enumeration : cachedEnumerations.values()) {
			
			if (enumeration.getDescription().equals(description)) {
				return enumeration;
			}
		}
		return null;
	}
}
