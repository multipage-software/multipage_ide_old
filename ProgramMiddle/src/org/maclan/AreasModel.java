/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.io.File;
import java.util.*;

import org.multipage.gui.Utility;

/**
 * @author
 *
 */
public class AreasModel {

	/**
	 * Areas.
	 */
	protected LinkedList<Area> areas = new LinkedList<Area>();
	
	/**
	 * A lookup table that maps Area IDs to Area objects.
	 */
	private HashMap<Long, Area> areasLookup = new HashMap<Long, Area>();
	
	/**
	 * Start area ID.
	 */
	private long homeAreaId = 0L;
	
	/**
	 * Versions.
	 */
	private LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
	
	/**
	 * Enumerations.
	 */
	private LinkedList<EnumerationObj> enumerations = new LinkedList<EnumerationObj>();

	/**
	 * MIME types.
	 */
	private ArrayList<MimeType> mimeTypes = new ArrayList<MimeType>();
	
	/**
	 * Time stamp for debugging.
	 */
	private String timeStamp;
	
	/**
	 * COnstructor.
	 */
	public AreasModel() {
		
		// Remember time stamp for debugging.
		long currentTimeMs = System.currentTimeMillis();
		this.timeStamp = Utility.formatTime(currentTimeMs);
	}
	
	/**
	 * Add new area.
	 */
	public void addNewArea(Area area) {

		// Add new item.
		areas.add(area);
		
		// Set lookup table.
		long areaId = area.getId();
		areasLookup.put(areaId, area);
	}
	
	/**
	 * Add edge.
	 * @param inheritance 
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub 
	 * @param reference 
	 */
	public void addSubarea(long areaId, long subareaId, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean reference) {
		
		Area area = getArea(areaId);
		Area subarea = getArea(subareaId);
		
		if (area != null && subarea != null) {
				
			area.addSubarea(subarea, inheritance, relationNameSub, relationNameSuper,
					hideSub, reference);
		}
	}

	/**
	 * Get area.
	 */
	public Area getArea(long id) {
		
		// Find area.
		Area area = areasLookup.get(id);
		return area;
	}

	/**
	 * @return the areas
	 */
	public LinkedList<Area> getAreas() {
		
		return areas;
	}

	/**
	 * Get child areas.
	 */
	public LinkedList<Area> getAreaChildren(long id) {

		Area area = getArea(id);

		return area.getSubareas();
	}

	/**
	 * Get root area.
	 * @return
	 */
	public Area getRootArea() {

		return getArea(0);
	}

	/**
	 * Remove all areas and edges.
	 */
	public void removeAllAreas() {
	
		this.areas.clear();
		this.areasLookup.clear();
	}

	/**
	 * Set all areas flags.
	 */
	public void setAllAreasFlags(int flag) {
		
		for (Area area : areas) {
			area.setFlag(flag);
		}
	}
	
	/**
	 * Resets areas overlaps flags. 
	 */
	public void resetAreasOverlapsFlags() {

		// Call the recursive method.
		resetSubareasOverlapsFlags(getRootArea());
	}
	
	/**
	 * Resets sub areas overlaps flags.
	 * @param area 
	 * @param existsOverlap 
	 */
	public void resetSubareasOverlapsFlags(Area area) {
		
		// If the flag is in the processing state, exit the method.
		if (area.isFlag(Flag.PROCESSED)) {
			return;
		}
		
		// If the area is set...
		if (area.isFlag(Flag.SET)) {
			
			// If the area has more than one super area and it has
			// a super area with not set or processing flag...
			int superAreasCount = area.getSuperareasCount();
			if (superAreasCount > 1 
					&& area.existSuperareaWithOneOfFlags(Flag.NONE, Flag.PROCESSED)) {
				
				// Clear the tree flags.
				setAreaSubTreeFlags(area, Flag.NONE | Flag.PROCESSED);
			}
		}
		
		// Do loop for all child areas.
		for (Area subArea : area.getSubareas()) {
			
			// If there is a recursion, skip current iteration.
			if (area.isRecursionUseSub(subArea.getId())) {
				continue;
			}
			
			resetSubareasOverlapsFlags(subArea);
		}
		
		area.setFlagBits(Flag.PROCESSED);
	}

	/**
	 * Gets number of areas with given flag.
	 * @param flag
	 * @return
	 */
	public int getAreasCountWithFlag(int flag) {

		int count = 0;
		
		for (Area area : areas) {
			if (area.isFlag(flag)) {
				count++;
			}
		}
		
		return count;
	}

	/**
	 * Returns true if the element is already in the list.
	 */
	public static <T extends Element> boolean alreadyInList(LinkedList<T> list, long id) {
		
		return MiddleUtility.getListItem(list, id) != null;
	}

	/**
	 * Remove area.
	 * @param area
	 */
	public void removeArea(Area area) {

		// Remove area from list.
		areas.remove(area);
		
		// Remove it from lookup table.
		Long areaId = area.getId();
		areasLookup.remove(areaId);
	}

	/**
	 * Remove areas.
	 * @param areasToRemove
	 */
	public void removeAreas(LinkedList<Area> areasToRemove) {

		// Clear area flags.
		setAllAreasFlags(Flag.NONE);

		// Set flags of areas which will be removed.
		for (Area areaToRemove : areasToRemove) {
			areaToRemove.setFlag(Flag.SET);
		}
		
		// Remove marked super areas, sub areas and areas.
		LinkedList<Area> areasClone = new LinkedList<Area>(areas);
		
		for (Area area : areasClone) {
			area.removeMarkedSuperAreas(Flag.SET);
			area.removeMarkedSubAreas(Flag.SET);
			if (area.isFlag(Flag.SET)) {
				
				// Remove area from list and from the lookup table.
				areas.remove(area);
				Long areaId = area.getId();
				areasLookup.remove(areaId);
			}
		}
	}

	/**
	 * Returns true if a circle exists in areas.
	 * @return
	 */
	public boolean existsCircleInAreas() {
		
		// Reset area flags.
		setAllAreasFlags(Flag.NONE);
		return existsCircle(getRootArea());
	}

	/**
	 * Returns true if a circle exists in area sub areas.
	 * @param area
	 * @return
	 */
	private boolean existsCircle(Area area) {

		// Set processing flag.
		area.setFlag(Flag.PROCESSING);
		
		// Do loop for sub areas.
		for (Area subArea : area.getSubareas()) {

			// If the sub area is already in the processing state
			// then it exists a circle (recursion) in the diagram.
			if (subArea.isFlag(Flag.PROCESSING)) {
				return true;
			}
			
			// If the sub area is already finishes then skip
			// its processing.
			if (subArea.isFlag(Flag.FINISHED)) {
				continue;
			}
			
			// If the sub area is a reference, skip current iteration.
			if (area.isRecursionUseSub(subArea.getId())) {
				continue;
			}
			
			// Call this method recursively for sub area and if it
			// exists a circle (recursive call) then exit the method
			// with true value.
			if (existsCircle(subArea)) {
				return true;
			}
		}
		
		// Set finished flag.
		area.setFlag(Flag.FINISHED);
		
		return false;
	}

	/**
	 * Sets area sub tree flags.
	 * @param rootArea
	 * @param flag
	 */
	public void setAreaSubTreeFlags(Area rootArea, int flag) {

		// If the parent area is already set, exit the method.
		if (rootArea.isFlag(flag)) {
			return;
		}
		
		rootArea.setFlag(flag);
		
		// Do loop for all sub areas.
		for (Area subArea : rootArea.getSubareas()) {
			
			// If there is a recursion, skip current iteration.
			if (rootArea.isRecursionUseSub(subArea.getId())) {
				continue;
			}
			
			// Call the method recursively.
			setAreaSubTreeFlags(subArea, flag);
		}
	}

	/**
	 * Sets flags.
	 * @param <T>
	 * @param steps
	 * @param none
	 */
	public static void setFlags(LinkedList<? extends FlagElement> elements, int flag) {

		for (FlagElement element : elements) {
			
			element.setFlag(flag);
		}
	}

	/**
	 * @param homeAreaId the homeAreaId to set
	 */
	public void setHomeAreaId(long startAreaId) {
		this.homeAreaId = startAreaId;
	}

	/**
	 * Returns true value if it is start area.
	 * @param area
	 * @return
	 */
	public boolean isHomeArea(Area area) {
		
		if (area == null) {
			return false;
		}
		return area.getId() == homeAreaId;
	}

	/**
	 * Returns true value if an area with given ID exists.
	 * @param areaId
	 * @return
	 */
	public boolean existsArea(long areaId) {
		
		return getArea(areaId) != null;
	}

	/**
	 * @return the homeAreaId
	 */
	public long getHomeAreaId() {
		return homeAreaId;
	}
	
	/**
	 * Get home area.
	 */
	public Area getHomeArea() {
		
		return getArea(homeAreaId);
	}

	/**
	 * Add slot alias.
	 * @param slotAlias
	 * @param areaId
	 */
	public void addSlotAlias(String slotAlias, long areaId) {
		
		Area area = getArea(areaId);
		area.addSlotAlias(slotAlias);
	}
	

	/**
	 * Add slot name.
	 * @param slotAlias
	 * @param areaId
	 */
	public void addSlotName(String slotName, long areaId) {
		
		Area area = getArea(areaId);
		area.addSlotName(slotName);
	}


	/**
	 * Get area with given alias.
	 * @param alias
	 * @return
	 */
	public Area getArea(String alias) {

		for (Area area : areas) {
			if (alias.equals(area.getAlias())) {
				return area;
			}
		}
		return null;
	}

	/**
	 * Add area resource name.
	 * @param areaId
	 * @param localDescription
	 * @param resourceName
	 */
	public void addAreaResource(long areaId, String localDescription, String resourceName) {
		
		// Get area.
		Area area = getArea(areaId);
		if (area == null) {
			return;
		}
		
		// Add resource.
		area.addResource(localDescription, resourceName);
	}

	/**
	 * Get area and sub areas.
	 * @param areaId
	 * @return
	 */
	public LinkedList<Area> getAreaAndSubAreas(long areaId) {
		
		LinkedList<Area> areasFound = new LinkedList<Area>();
		
		// Get areas recursively.
		getAreaAndSubAreas(areaId, areasFound, 1, 0);
		
		return areasFound;
	}

	/**
	 * Get area and sub areas. (Hidden areas are not included.)
	 * @param areaId
	 * @param levels
	 * @return
	 */
	public LinkedList<Area> getAreaAndSubAreas(long areaId, int levels) {
		
		LinkedList<Area> areasFound = new LinkedList<Area>();
		
		// Get areas recursively.
		getAreaAndSubAreas(areaId, areasFound, 1, levels);
		
		return areasFound;
	}

	/**
	 * Get area and sub areas. (Hidden areas are not included.)
	 * @param areaId
	 * @param areasFound
	 */
	private void getAreaAndSubAreas(long areaId, LinkedList<Area> areasFound,
			int currentLevel, int levels) {
		
		// Check level.
		if (levels != 0) {
			if (currentLevel > levels) {
				return;
			}
		}
		
		Area area = null;
		// Get existing area.
		for (Area areaFound : areasFound) {
			if (areaFound.getId() == areaId) {
				area = areaFound;
			}
		}
		// Get new area.
		if (area == null) {
			area = getArea(areaId);
			if (area == null) {
				return;
			}
			areasFound.add(area);
		}
		
		// Call this method recursively for sub areas.
		for (Area subArea : area.getSubareas()) {
			
			getAreaAndSubAreas(subArea.getId(), areasFound, currentLevel + 1,
					levels);
		}
	}

	/**
	 * Get area and super areas.
	 * @param areaId
	 * @param levels
	 * @param inheritance
	 * @return
	 */
	public LinkedList<Area> getAreaAndSuperAreas(long areaId, int levels,
			boolean inheritance) {
		
		LinkedList<Area> areasFound = new LinkedList<Area>();
		
		getAreaAndSuperAreas(areaId, areasFound, inheritance, 1, levels);
		return areasFound;
	}

	/**
	 * Get area and super areas.
	 * @param areaId
	 * @param areasFound
	 * @param inheritance
	 * @param currentLevel
	 * @param levels
	 */
	private void getAreaAndSuperAreas(long areaId, LinkedList<Area> areasFound,
			boolean inheritance, int currentLevel, int levels) {
		
		// Check level.
		if (levels != 0) {
			if (currentLevel > levels) {
				return;
			}
		}
		
		Area area = null;
		// Get existing area.
		for (Area areaFound : areasFound) {
			if (areaFound.getId() == areaId) {
				area = areaFound;
			}
		}
		// Get new area.
		if (area == null) {
			area = getArea(areaId);
			if (area == null) {
				return;
			}
			areasFound.add(area);
		}
		
		// Call this method recursively for sub areas.
		for (Area subArea : inheritance ? area.getInheritsFrom() : area.getSuperareas()) {
			getAreaAndSuperAreas(subArea.getId(), areasFound, inheritance,
					currentLevel + 1, levels);
		}
	}

	/**
	 * Get start area.
	 * @param area
	 * @return
	 */
	public Area getStartArea(Area area, long versionId) {
		
		if (area.isSource(versionId)) {
			return area;
		}
		
		// Find in super areas.
		for (Area inheritedArea : area.getInheritsFrom()) {
			
			// Call the method recursively.
			Area startArea = getStartArea(inheritedArea, versionId);
			if (startArea != null) {
				return startArea;
			}
		}
		
		return null;
	}

	/**
	 * Get start area.
	 * @param areaId
	 * @param versionId
	 * @return
	 */
	public Area getStartArea(long areaId, long versionId) {
		
		// Get area.
		Area area = getArea(areaId);
		
		return getStartArea(area, versionId);
	}
	
	/**
	 * Set versions.
	 * @param versions
	 */
	public void setVersions(LinkedList<VersionObj> versions) {
		
		this.versions.clear();
		this.versions.addAll(versions);
	}

	/**
	 * Get versions.
	 * @return
	 */
	public LinkedList<VersionObj> getVersions() {
		
		return versions;
	}

	/**
	 * Clear model.
	 */
	public void clear() {
		
		homeAreaId = 0L;
		
		areas.clear();
		areasLookup.clear();
	}
	
	// Queue item.
	class QueueItem {

		Area area;
		boolean omit;
		
		QueueItem(Area area, boolean omit) {
			
			this.area = area;
			this.omit = omit;
		}
	}
	
	/**
	 * Get area path.
	 * @param area 
	 * @return
	 */
	private String getAreaAbsolutePath(Area area, VersionObj version) {

		// Create queue.
		LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
		LinkedList<Long> visited = new LinkedList<Long>();
		
		// Initialize queues.
		queue.add(new QueueItem(area, false));
		visited.add(area.getId());
		
		while (isContinueSearchFolder(queue)) {
			
			QueueItem queueItem = queue.removeFirst();
			
			Area currentArea = queueItem.area;
			
			// Check if folder exists.
			String folder = currentArea.getFolderOSDependent();
			if (!queueItem.omit) {
				
				if (folder == null) {
					folder = "";
				}
				
				// If the version matches and folder is not empty or the area is a start area.
				if (currentArea.getVersionId() == version.getId()
						&& (!folder.isEmpty() || currentArea.isStartArea())) {
					
					return folder.equals(File.separator) ? "" : folder;
				}
			}
			
			// Do loop for all super areas.
			for (Area superArea : currentArea.getSuperareas()) {
				
				long superAreaId = superArea.getId();
				
				if (!MiddleUtility.contains(visited, superAreaId)) {
					
					// Add super area to the queue.
					visited.add(superAreaId);
					queue.addLast(new QueueItem(superArea, queueItem.omit));
				}
			}
		}
		
		return null;
	}
	/**
	 * Returns true value if the search can continue.
	 * @param queue
	 * @return
	 */
	private static boolean isContinueSearchFolder(LinkedList<QueueItem> queue) {
		
		for (QueueItem item : queue) {
			
			if (!item.omit) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Get area full file name.
	 * @param area
	 * @return
	 */
	public String getAreaFullFileName(Area area, VersionObj version) {
		
		if (!area.isVisible()) {
			return null;
		}
		
		// Get area file name.
		String fileName = area.getFileName();
		if (fileName == null) {
			fileName = "";
		}
		
		// If the file name is empty, return null value.
		if (fileName.isEmpty()) {
			return null;
		}
		
		// Get area page path.
		String path = getAreaAbsolutePath(area, version);
		if (path == null) {
			path = "";
		}

		// If the path is empty, return file name.
		if (path.isEmpty()) {
			return fileName;
		}
		
		// Return full path file name.
		return path + File.separator + fileName;
	}

	/**
	 * Update areas references.
	 * @param areasList
	 */
	public void updateAreas(LinkedList<Area> areasList) {
		
		if (areasList == null) {
			return;
		}
		
		// Create new list.
		LinkedList<Area> newAreasList = new LinkedList<Area>();
		
		// Do loop for all old areas.
		for (Area oldArea : areasList) {
			
			Area newArea = getArea(oldArea.getId());
			if (newArea != null) {
				
				newAreasList.add(newArea);
			}
		}
		
		// Update input areas' list.
		areasList.clear();
		areasList.addAll(newAreasList);
	}

	/**
	 * Returns true value if an area with given alias already exists.
	 * @param areaAlias
	 * @return
	 */
	public boolean existsAreaAlias(String areaAlias) {
		
		for (Area area : areas) {
			
			String alias = area.getAlias();
			
			if (alias != null && !alias.isEmpty() && alias.equals(areaAlias)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Set enumerations.
	 * @param enumerations
	 */
	public void setEnumerations(LinkedList<EnumerationObj> enumerations) {
		
		this.enumerations = enumerations;
	}

	/**
	 * Return enumerations.
	 * @return
	 */
	public LinkedList<EnumerationObj> getEnumerations() {
		
		return enumerations;
	}

	/**
	 * Get enumeration value reference.
	 * @param enumerationValueId
	 * @return
	 */
	public EnumerationValue getEnumerationValue(long enumerationValueId) {
		
		for (EnumerationObj enumeration : enumerations) {
			
			for (EnumerationValue enumerationValue : enumeration.getValues()) {
				
				if (enumerationValue.getId() == enumerationValueId) {
					return enumerationValue;
				}
			}
		}
		
		return null;
	}

	/**
	 * Get enumeration object reference.
	 * @param enumerationId
	 * @return
	 */
	public EnumerationObj getEnumeration(long enumerationId) {
		
		for (EnumerationObj enumeration : enumerations) {
			if (enumeration.getId() == enumerationId) {
				return enumeration;
			}
		}
		return null;
	}

	/**
	 * Get enumeration values.
	 * @return
	 */
	public LinkedList<EnumerationObj> getEnumerations(LinkedList<Long> enumerationValuesIds) {
		
		LinkedList<EnumerationObj> selectedEnumerations = new LinkedList<EnumerationObj>();
		
		// Do loop for all enumerations.
		for (EnumerationObj enumeration : enumerations) {
			
			// If enumeration contains given value, add it to the list if it does'nt exist.
			for (EnumerationValue enumerationValue : enumeration.getValues()) {
				
				if (enumerationValuesIds.contains(enumerationValue.getId())) {
					
					boolean exists = false;
					for (EnumerationObj selectedEnumeration : selectedEnumerations) {
						if (selectedEnumeration.getId() == enumeration.getId()) {
							exists = true;
						}
					}
					
					if (!exists) {
						selectedEnumerations.add(enumeration);
					}
				}
			}
		}
		
		return selectedEnumerations;
	}

	/**
	 * Clear areas user data.
	 */
	public void clearAreasUserData() {
		
		for (Area area : areas) {
			area.setUser(null);
		}
	}

	/**
	 * Set MIME types.
	 * @param mimeTypes
	 */
	public void setMimeTypes(ArrayList<MimeType> mimeTypes) {
		
		this.mimeTypes = mimeTypes;
	}

	/**
	 * Get MIME types.
	 * @return
	 */
	public ArrayList<MimeType> getMimeTypes() {
		
		return mimeTypes;
	}

	/**
	 * Get areas.
	 * @param areasIds
	 * @return
	 */
	public LinkedList<Area> getAreas(LinkedList<Long> areasIds) {
		
		LinkedList<Area> areasResult = new LinkedList<Area>();
		
		for (long areaId : areasIds) {
			
			Area area = areasLookup.get(areaId);
			areasResult.add(area);
		}
		
		return areasResult;
	}

	/**
	 * Get version.
	 * @param versionId
	 * @return
	 */
	public VersionObj getVersion(long versionId) {

		// Do loop for all versions.
		for (VersionObj version : versions) {
			
			if (version.getId() == versionId) {
				return version;
			}
		}
		
		return null;
	}

	/**
	 * Get areas with given constructor group ID. (Areas are not source areas.)
	 * @param constructorGroupId
	 * @return
	 */
	private LinkedList<Area> getAreasWithConstructorGroupIdNotSource(long constructorGroupId) {
		
		LinkedList<Area> foundAreas = new LinkedList<Area>();
		
		if (constructorGroupId == 0) {
			return foundAreas;
		}
		
		// Do loop for all areas.
		for (Area area : areas) {
			
			// Exclude source areas.
			if (area.isConstructorSource()) {
				continue;
			}
			
			Long constructorGroupIdFound = area.getConstructorGroupId();
			if (constructorGroupIdFound == null) {
				continue;
			}
			
			// If the constructor group ID matches, return the area.
			if (constructorGroupIdFound == constructorGroupId) {
				foundAreas.add(area);
			}
		}
		
		return foundAreas;
	}

	/**
	 * Load areas' new constructor group IDs.
	 * @param constructorGroup
	 * @return
	 */
	public Hashtable<Long, Long> loadAreasConstructorGroupIds(
			ConstructorGroup constructorGroup) {
		
		Hashtable<Long, Long> table = new Hashtable<Long, Long>();
		
		// Trace constructor tree breath first.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(constructorGroup);
		
		while (!queue.isEmpty()) {
			
			// Get queue item.
			Object item = queue.removeFirst();
			
			// If it is a constructor group...
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroupNode = (ConstructorGroup) item;
				
				// Get old and new group ID and area ID.
				long oldGroupId = constructorGroupNode.getOldId();
				long newGroupId = constructorGroupNode.getId();
				
				LinkedList<Area> areas = getAreasWithConstructorGroupIdNotSource(oldGroupId);
				
				// Insert table items.
				for (Area area : areas) {
					table.put(area.getId(), newGroupId);
				}
				
				// Add sub constructor holders to the queue end.
				queue.addAll(constructorGroupNode.getConstructorHolders());
			}
			// If it is a constructor holder...
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// Get sub object.
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				
				// If the sub object is a constructor group (not constructor group reference),
				// add it to the queue end.
				if (subObject instanceof ConstructorGroup) {
					queue.add(subObject);
				}
			}
		}
		
		return table;
	}

	/**
	 * Returns true value if an area alias is unique against project root area.
	 * @param alias
	 * @param currentAreaId
	 * @return
	 */
	public boolean isAreaAliasUnique(String alias, long currentAreaId) {
		
		// Check input.
		if (alias == null || alias.isEmpty()) {
			return true;
		}
		
		// Get project root areas.
		Area currentArea = getArea(currentAreaId);
		if (currentArea == null) {
			return false;
		}
		
		LinkedList<Area> projectRootAreas = getProjectRootAreas(currentArea);
		LinkedList<Area> projectAreas = new LinkedList<Area>();
		
		// Try to find alias in areas with given project roots.
		LinkedList<Area> queue = new LinkedList<Area>();
		queue.addAll(projectRootAreas);
		
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			
			boolean exists = false;
			for (Area projectArea : projectAreas) {
				if (area.equals(projectArea)) {
					exists = true;
					break;
				}
			}
			
			// Insert sub areas that are not project roots.
			if (!exists) {
				
				projectAreas.add(area);
				LinkedList<Area> subAreas = area.getSubareas();
				
				for (Area subArea : subAreas) {
					
					if (subArea.getAlias().equals(alias) && subArea.getId() != currentAreaId) {
						return false;
					}
					
					queue.add(subArea);
				}
			}
		}
		
		return true;
	}

	/**
	 * Get project root area.
	 * @param currentArea
	 * @return
	 */
	private LinkedList<Area> getProjectRootAreas(Area currentArea) {
		
		LinkedList<Area> rootAreas = new LinkedList<Area>();
		
		// Try to find super areas with project_root flag set.
		LinkedList<Area> queue = new LinkedList<Area>();
		LinkedList<Area> tracedAreas = new LinkedList<Area>();
		
		queue.add(currentArea);
		
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			
			boolean exists = false;
			for (Area tracedArea : tracedAreas) {
				if (area.equals(tracedArea)) {
					exists = true;
					break;
				}
			}
			
			if (!exists) {
				tracedAreas.add(area);
				
				if (area.isProjectRoot()) {
					rootAreas.add(area);
				}
				else {
					// Add super areas to the queue.
					queue.addAll(area.getSuperareas());
				}
			}
		}
		
		// If there area no root areas, add global area to the result.
		if (rootAreas.isEmpty()) {
			rootAreas.add(getRootArea());
		}
		
		return rootAreas;
	}

	/**
	 * Get project areas.
	 * @param currentArea
	 * @return
	 */
	public LinkedList<Area> getProjectAreas(Area currentArea) {
		
		LinkedList<Area> projectAreas = new LinkedList<Area>();
		LinkedList<Area> projectRootAreas = getProjectRootAreas(currentArea);
		
		// Load project areas.
		LinkedList<Area> queue = new LinkedList<Area>();
		queue.addAll(projectRootAreas);
		
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			
			boolean exists = false;
			for (Area projectArea : projectAreas) {
				if (area.equals(projectArea)) {
					exists = true;
					break;
				}
			}
			
			if (!exists) {
				projectAreas.add(area);
				
				// Insert sub areas that are not project roots.
				LinkedList<Area> subAreas = area.getSubareas();
				queue.addAll(subAreas);
			}
		}
		
		return projectAreas;
	}
	
	/**
	 * Update isEnabled flags.
	 */
	public void updateDisabledAreas() {
		
		Area rootArea = getRootArea();
		LinkedList<Area> queue = new LinkedList<Area>();
		
		queue.add(rootArea);
		while (!queue.isEmpty()) {
			
			Area area = queue.removeFirst();
			boolean isEnabled = area.isEnabled();
			
			// Set sub areas.
			LinkedList<Area> subareas = area.getSubareas();
			for (Area subarea : subareas) {
				
				if (!isEnabled) {
					subarea.setEnabled(false);
				}
				queue.addLast(subarea);
			}
		}
	}
	
	/**
	 * Gets time stamp.
	 * @return
	 */
	public String getTimeStamp() {
		
		return this.timeStamp;
	}
}
