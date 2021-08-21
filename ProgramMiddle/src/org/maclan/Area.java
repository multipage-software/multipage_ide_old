/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.multipage.gui.IdentifiedTreeNode;
import org.multipage.gui.Utility;
import org.multipage.util.Resources;
import org.multipage.util.j;

/**
 * Graph node.
 * @author
 *
 */
public class Area extends SlotHolder implements FlagElement, Element, ResContainer, Comparable<Object>, IdentifiedTreeNode {
	
	/**
	 * Dependent area.
	 */
	class DependentArea {
		
		/**
		 * Area reference.
		 */
		Area area;
		
		/**
		 * Area relation.
		 */
		AreaRelation relation;

		/**
		 * Constructor.
		 * @param area
		 * @param inheritance
		 * @param relationNameSub
		 * @param relationNameSuper
		 * @param hideSub 
		 * @param recursion 
		 */
		public DependentArea(Area area, boolean inheritance,
				String relationNameSub, String relationNameSuper, boolean hideSub,
				boolean recursion) {
			
			this.area = area;
			this.relation = new AreaRelation(inheritance, relationNameSub,
					relationNameSuper, hideSub, recursion);
		}

		/**
		 * Get area id.
		 * @return
		 */
		public long getId() {
			
			return area.getId();
		}

		/**
		 * Get area.
		 * @return
		 */
		public Area getArea() {
			
			return area;
		}

		/**
		 * Is area flag.
		 * @param flag
		 * @return
		 */
		public boolean isFlag(int flag) {
			
			return area.isFlag(flag);
		}

		/**
		 * Returns true value if there is an inheritance.
		 * @return
		 */
		public boolean isInheritance() {
			
			return relation.isInheritance();
		}

		/**
		 * Get relation name.
		 * @return
		 */
		public String getRelationNameSub() {
			
			return relation.getRelationNameSub();
		}

		/**
		 * Get relation name.
		 * @return
		 */
		public String getRelationNameSuper() {
			
			return relation.getRelationNameSuper();
		}

		/**
		 * Set inheritance.
		 * @param inheritance
		 */
		public void setInheritance(boolean inheritance) {
			
			relation.setInheritance(inheritance);
		}

		/**
		 * Set sub relation name.
		 * @param relationName
		 */
		public void setRelationNameSub(String relationName) {
			
			relation.setRelationNameSub(relationName);
		}

		/**
		 * Set super relation name.
		 * @param relationName
		 */
		public void setRelationNameSuper(String relationName) {
			
			relation.setRelationNameSuper(relationName);
		}

		/**
		 * Get relation.
		 * @return
		 */
		public AreaRelation getRelation() {
			
			return relation;
		}

		/**
		 * Returns true value if to hide sub areas.
		 * @return
		 */
		public boolean isHideSub() {
			
			return relation.isHideSub();
		}
		
		/**
		 * Returns true value is the relation is recursion.
		 * @return
		 */
		public boolean isRecursion() {
			
			return relation.isRecursion();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return area.toString();
		}
	}
	
	/**
	 * Queue item class.
	 * @author
	 *
	 */
	class QueueItem {

		Area area;
		boolean omit;
		
		QueueItem(Area area, boolean omit) {
			
			this.area = area;
			this.omit = omit;
		}
	}
	
	/**
	 * Show IDs flag.
	 */
	private static boolean showIds = false;

	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Description.
	 */
	private String description;
	
	/**
	 * Alias.
	 */
	private String alias;

	/**
	 * Visible flag.
	 */
	private boolean visible;

	/**
	 * Subareas.
	 */
	private LinkedList<DependentArea> subareas = new LinkedList<DependentArea>();
	
	/**
	 * Sub areas loaded flag.
	 */
	private boolean subAreasLoaded = false;

	/**
	 * Superareas.
	 */
	private LinkedList<DependentArea> superareas = new LinkedList<DependentArea>();
	
	/**
	 * Super areas loaded flag.
	 */
	private boolean superAreasLoaded = false;

	/**
	 * Slot aliases.
	 */
	private LinkedList<String> slotAliases = new LinkedList<String>();
	
	/**
	 * Slot names.
	 */
	private LinkedList<String> slotNames = new LinkedList<String>();
	
	/**
	 * Resource names.
	 */
	private LinkedList<String> resourceNames = new LinkedList<String>();
	
	/**
	 * Read only flag.
	 */
	private boolean readOnly = true;

	/**
	 * User object.
	 */
	private Object userObject;
	
	/**
	 * Auxiliary flag.
	 */
	private int flag = Flag.NONE;

	/**
	 * Is help flag.
	 */
	private boolean isHelp = false;

	/**
	 * Localized flag.
	 */
	private boolean localized = true;
	
	/**
	 * File name.
	 */
	private String fileName;
	
	/**
	 * Folder.
	 */
	private String folder;

	/**
	 * Version ID.
	 */
	private long versionId;

	/**
	 * Constructor group ID.
	 */
	private Long constructorGroupId;
	
	/**
	 * A descriptive list of constructors .
	 */
	private LinkedList<String> constructorListRows = new LinkedList<String>();

	/**
	 * Is constructor area flag.
	 */
	private boolean isConstructorArea;

	/**
	 * Related area reference. Can be null.
	 */
	private Area relatedArea;

	/**
	 * Related area loaded flag.
	 */
	private boolean isRelatedAreaLoaded = false;

	/**
	 * Constructor holder ID.
	 */
	private Long constructorHolderId;

	/**
	 * File extension.
	 */
	private String fileExtension;

	/**
	 * Constructing area.
	 */
	private Area constructingArea;

	/**
	 * Constructing area set flag.
	 */
	private boolean constructingAreaSet = false;

	/**
	 * Can import flag.
	 */
	private boolean canImport = false;

	/**
	 * Project root flag.
	 */
	private boolean projectRoot = false;

	/**
	 * Start resource ID.
	 */
	private Long startResourceId;
	
	/**
	 * A list of area sources.
	 */
	private HashMap<Long, AreaSource> areaSources = new HashMap<Long, AreaSource>();
	
	/**
	 * This flag is true if this area has a start resource.
	 */
	private boolean isStartResource = false;

	/**
	 * If this flag is true, the area can be rendered
	 */
	private boolean isEnabled;

	/**
	 * Clear area.
	 */
	public void clear() {
		
		id = 0;
		description = "";
		alias = "";
		visible = false;
		subareas.clear();
		superareas.clear();
		slotAliases.clear();
		slotNames.clear();
		resourceNames.clear();
		isHelp = false;
		userObject = null;
		flag = Flag.NONE;
		fileName = null;
		folder = null;
		versionId = 0L;
		constructorGroupId = null;
		constructorListRows.clear();
		isConstructorArea = false;
		relatedArea = null;
		constructorHolderId = null;
		fileExtension = null;
		constructingArea = null;
		constructingAreaSet = false;
		canImport = false;
		projectRoot = false;
		startResourceId = null;
		areaSources.clear();
		isStartResource = false;
		
		super.clearSlots();
		clearExtended();
	}

	/**
	 * Extended clear.
	 */
	protected void clearExtended() {
		
	}


	/**
	 * Constructor.
	 * @param visible 
	 */
	public Area(long id, String description, boolean visible,
			String alias) {
		this(id, description, visible, alias, true);
	}

	/**
	 * Constructor.
	 * @param id
	 * @param description
	 * @param visible
	 * @param alias
	 * @param readOnly
	 */
	public Area(long id, String description, boolean visible,
			String alias, boolean readOnly) {

		this.id = id;
		if (description == null) {
			description = "";
		}
		this.description = description;
		this.visible = visible;
		if (alias == null) {
			alias = "";
		}
		this.alias = alias;
		this.readOnly = readOnly;
	}

	/**
	 * Constructor.
	 */
	public Area() {
		this(0L, "", true, "");
	}
	
	/**
	 * Constructor
	 * @param description
	 */
	public Area(String description) {
		this();
		
		this.description = description;
	}
	
	/**
	 * Create area.
	 * @param directory
	 */
	public Area(File directory) {
		this();
		
		if (!directory.isDirectory()) {
			return;
		}
		
		// Set description and remember directory
		this.description = directory.getName();
		this.setReadOnly(false);
		this.userObject = directory;
		
		// List directory files and add external providers.
		for (File file : directory.listFiles()) {
			
			if (file.isFile()) {
				
				String fileName = file.getName();
				Slot externalProvider = new Slot(this, fileName);
				externalProvider.setUserDefined(true);
				
				externalProvider.setExternalProvider(ExternalLinkParser.formatFileLink("UTF-8", file.toString()));
				externalProvider.setValueMeaning(SlotType.EXTERNAL_PROVIDER);
				externalProvider.setReadsInput(true);
				externalProvider.setWritesOutput(true);
				
				addSlot(externalProvider);
			}
		}
	}

	/**
	 * @return the id
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @param userObject the userObject to set
	 */
	public void setUser(Object user) {
		this.userObject = user;
	}

	/**
	 * @return the userObject
	 */
	public Object getUser() {
		return userObject;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return description;
	}

	/**
	 * Get existing sub area.
	 * @param subAreaId
	 * @return
	 */
	private Area getSubarea(long subAreaId) {
		
		for (DependentArea subArea : subareas) {
			if (subArea.getId() == subAreaId) {
				return subArea.getArea();
			}
		}
		return null;
	}

	/**
	 * Get existing super area.
	 * @param superAreaId
	 * @return
	 */
	public Area getSuperarea(long superAreaId) {

		for (DependentArea superArea : superareas) {
			if (superArea.getId() == superAreaId) {
				return superArea.getArea();
			}
		}
		return null;
	}

	/**
	 * Add subarea.
	 * @param subarea
	 * @param inheritance 
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub 
	 * @param recursion 
	 */
	public void addSubarea(Area subarea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean recursion) {

		subarea.addSuperareaLight(this, inheritance, relationNameSub,
				relationNameSuper, hideSub, recursion);
		
		Area existingSubarea = getSubarea(subarea.getId());
		if (existingSubarea == null) {
			
			subareas.add(new DependentArea(subarea, inheritance,
					relationNameSub, relationNameSuper, hideSub, recursion));
		}
	}

	/**
	 * Add sub area.
	 * @param subArea
	 * @param relation
	 */
	public void addSubarea(Area subArea, AreaRelation relation) {
		
		addSubarea(subArea, relation.isInheritance(), relation.getRelationNameSub(),
				relation.getRelationNameSuper(), relation.isHideSub(),
				false);
	}

	/**
	 * Add subarea.
	 * @param subarea
	 * @param inheritance 
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub 
	 * @param recursion
	 */
	public void addSubareaLight(Area subarea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean recursion) {

		Area existingSubarea = getSubarea(subarea.getId());
		if (existingSubarea == null) {
			subareas.add(new DependentArea(subarea, inheritance,
					relationNameSub, relationNameSuper, hideSub, recursion));
		}
	}

	/**
	 * Adds superarea.
	 * @param inheritance
	 * @param relationNameSuper
	 * @param hideSub 
	 */
	public void addSuperareaLight(Area superarea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean recursion) {

		Area existingSuperarea = getSuperarea(superarea.getId());
		if (existingSuperarea == null) {
			superareas.add(new DependentArea(superarea, inheritance,
					relationNameSub, relationNameSuper, hideSub, recursion));
		}
	}
	
	/**
	 * Extract areas.
	 * @param dependentAreas
	 * @return
	 */
	private static LinkedList<Area> extractAreas(LinkedList<DependentArea> dependentAreas) {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		
		for (DependentArea dependentArea : dependentAreas) {
			
			areas.add(dependentArea.getArea());
		}
		
		return areas;
	}


	/**
	 * @return the subareas
	 */
	public LinkedList<Area> getSubareas() {
		
		return extractAreas(subareas);
	}

	/**
	 * Sets ID.
	 * @param id
	 */
	public void setId(long id) {

		this.id = id;
	}

	/**
	 * Show / hide IDs.
	 * @param show
	 */
	public static boolean setShowId(boolean show) {

		boolean oldValue = showIds;
		showIds = show;
		
		return oldValue;
	}

	/**
	 * @return the super areas
	 */
	public LinkedList<Area> getSuperareas() {
		
		return extractAreas(superareas);
	}

	/**
	 * Sets area flag.
	 * @param flag
	 */
	public void setFlag(int flag) {

		this.flag = flag;
	}

	/**
	 * Returns true if the area has a super area with given flag
	 * value.
	 * @param flag
	 * @return
	 */
	public boolean existsSuperareaFlag(int flag) {

		// Do loop for super areas.
		LinkedList<Area> areas = extractAreas(superareas);
		for (Area superarea : areas) {
			if (superarea.flag == flag) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param flag 
	 * @return the flag
	 */
	public boolean isFlag(int flag) {
		
		if (flag == Flag.NONE) {
			return this.flag == Flag.NONE;
		}
		return (this.flag & flag) != 0;
	}

	/**
	 * Remove marked super areas.
	 * @param flag
	 */
	public void removeMarkedSuperAreas(int flag) {
		
		// Create list.
		LinkedList<DependentArea> dependentToRemove =
			new LinkedList<DependentArea>();

		// Remove marked super areas references.
		for (DependentArea superArea : superareas) {
			if (superArea.isFlag(flag)) {
				dependentToRemove.add(superArea);
			}
		}
		superareas.removeAll(dependentToRemove);
	}

	/**
	 * Remove marked sub areas.
	 * @param flag
	 */
	public void removeMarkedSubAreas(int flag) {

		// Create list.
		LinkedList<DependentArea> dependentToRemove =
			new LinkedList<DependentArea>();
		
		// Remove marked sub areas references.
		for (DependentArea subArea : subareas) {
			if (subArea.isFlag(flag)) {
				dependentToRemove.add(subArea);
			}
		}
		subareas.removeAll(dependentToRemove);
	}

	/**
	 * Returns true value if the list contains given area.
	 * @param dependentAreas
	 * @param area
	 * @return
	 */
	private boolean containsArea(LinkedList<DependentArea> dependentAreas,
			Area area) {
		
		for (DependentArea dependentArea : dependentAreas) {
			if (dependentArea.getId() == area.getId()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * If sub area already exists return true.
	 * @param subArea
	 * @return
	 */
	public boolean existsSubarea(Area subArea) {

		return containsArea(subareas, subArea);
	}

	/**
	 * Removes dependent area.
	 * @param dependentAreas
	 * @param area
	 */
	private void removeArea(LinkedList<DependentArea> dependentAreas,
			Area area) {
		
		for (DependentArea dependentArea : dependentAreas) {
			
			if (dependentArea.getId() == area.getId()) {
				dependentAreas.remove(dependentArea);
				break;
			}
		}
	}

	/**
	 * Remove sub area.
	 * @param subArea
	 */
	public void removeSubarea(Area subArea) {

		removeArea(subareas, subArea);
	}


	/**
	 * Remove super area.
	 * @param superArea
	 */
	public void removeSuperarea(Area superArea) {

		removeArea(superareas, superArea);
	}

	/**
	 * Returns true if a super area has the flag set.
	 * @return
	 */
	public boolean existSuperareaWithOneOfFlags(int ... flags) {

		LinkedList<Area> areas = extractAreas(superareas);
		
		// Do loop for super areas.
		for (Area superarea : areas) {
			
			if (superarea.isRecursionUseSub(id)) {
				continue;
			}
				
			for (int flag : flags) {
				if (superarea.flag == flag) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets flag.
	 * @return
	 */
	public int getFlag() {

		return flag;
	}

	/**
	 * Reset flag bits.
	 * @param bits
	 */
	public void resetFlagBits(int bits) {

		flag &= ~bits;
	}

	/**
	 * Set flag bits.
	 * @param bits
	 */
	public void setFlagBits(int bits) {

		flag |= bits;
	}

	/**
	 * Gets the number of super areas.
	 * @return
	 */
	public int getSuperareasCount() {

		return superareas.size();
	}

	/**
	 * Get sub areas count.
	 * @return
	 */
	public int getSubareasCount() {

		return subareas.size();
	}

	/**
	 * Set description.
	 * @param description2
	 */
	public void setDescription(String description) {

		if (description == null) {
			description = "";
		}
		this.description = description;
		
		// TODO: debug
		if (this.id == 710L) {
			j.log("AREA DESRIPTION HAS BEEN SET TO %s", description);
		}
	}

	/**
	 * Return true value if 
	 * @param superAreaId
	 * @return
	 */
	private boolean inheritsFromId(long superAreaId) {
		
		for (DependentArea dependentArea : superareas) {
			if (dependentArea.getId() == superAreaId
					&& dependentArea.isInheritance()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true value if this area inherits from the parentArea.
	 * @param parentArea
	 * @return
	 */
	public boolean inheritsFrom(Area parentArea) {

		if (parentArea == null) {
			return false;
		}
		return inheritsFromId(parentArea.getId());
	}

	/**
	 * Clones the area.
	 */
	public Area clone() {
		
		// Create new area.
		Area area = new Area(id, description, visible, alias);
		area.slotAliases.addAll(slotAliases);
		area.slotNames.addAll(slotNames);
		area.resourceNames.addAll(resourceNames);
		area.subAreasLoaded = subAreasLoaded;
		area.superAreasLoaded = superAreasLoaded;
		area.readOnly = readOnly;
		area.localized = localized;
		area.flag = flag;
		area.userObject = userObject;
		area.fileName = fileName;
		area.folder = folder;
		area.versionId = versionId;
		area.constructorGroupId = constructorGroupId;
		area.constructorListRows.addAll(constructorListRows);
		area.isConstructorArea = isConstructorArea;
		area.relatedArea = relatedArea;
		area.constructorHolderId = constructorHolderId;
		area.fileExtension = fileExtension;
		area.constructingArea = constructingArea;
		area.constructingAreaSet = constructingAreaSet;
		area.canImport = canImport;
		area.projectRoot = projectRoot;
		area.startResourceId = startResourceId;
		area.areaSources.putAll(areaSources);
		area.isStartResource = isStartResource;
		area.isEnabled = isEnabled;
		
		cloneExtended(area);
		
		// Create list of new areas.
		LinkedList<Area> newAreas = new LinkedList<Area>();
		newAreas.add(this);
		
		// Clone sub areas list.
		for (DependentArea dependentArea : subareas) {
			
			long subAreaId = dependentArea.getId();
			Area subArea = dependentArea.getArea();
			
			// Try to get area.
			Area newArea = MiddleUtility.getListItem(newAreas, subAreaId);
			if (newArea == null) {
				newArea = new Area(subArea.id, subArea.description, subArea.visible,
						subArea.alias);
				newAreas.add(newArea);
			}
			
			// Add the new area to the sub areas.
			boolean inheritance = dependentArea.isInheritance();
			String relationNameSub = dependentArea.getRelationNameSub();
			String relationNameSuper = dependentArea.getRelationNameSuper();
			boolean hideSub = dependentArea.isHideSub();
			boolean recursion = dependentArea.isRecursion();
			
			area.addSubareaLight(newArea, inheritance, relationNameSub,
					relationNameSuper, hideSub, recursion);
		}
		
		// Clone super areas list.
		for (DependentArea dependentArea : superareas) {
			
			long superAreaId = dependentArea.getId();
			Area superArea = dependentArea.getArea();
			
			// Try to get area.
			Area newArea = MiddleUtility.getListItem(newAreas, superAreaId);
			if (newArea == null) {
				newArea = new Area(superArea.id, superArea.description, superArea.visible,
						superArea.alias);
				newAreas.add(newArea);
			}
			
			// Add the new area to the super areas.
			boolean inheritance = dependentArea.isInheritance();
			String relationNameSub = dependentArea.getRelationNameSub();
			String relationNameSuper = dependentArea.getRelationNameSuper();
			boolean hideSub = dependentArea.isHideSub();
			boolean recursion = dependentArea.isRecursion();
			
			area.addSuperareaLight(newArea, inheritance, relationNameSub,
					relationNameSuper, hideSub, recursion);
		}

		return area;
	}

	/**
	 * Extended clone.
	 * @param area 
	 */
	protected void cloneExtended(Area area) {
		
	}

	/**
	 * @return the inheritsFrom
	 */
	public LinkedList<Area> getInheritsFrom() {
		
		LinkedList<Area> areas = new LinkedList<Area>();
		
		for (DependentArea dependentSuperArea : superareas) {
			if (dependentSuperArea.isInheritance()) {
				areas.add(dependentSuperArea.getArea());
			}
		}
		
		return areas;
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

	/**
	 * Get alias.
	 * @return
	 */
	public String getAlias() {

		return alias;
	}
	
	/**
	 * Get alias.
	 * @param showIds
	 * @return
	 */
	public String getAlias(boolean showIds) {
		
		if (alias == null || alias.isEmpty()) {
			return null;
		}
		if (showIds) {
			return "[" + id + "] " + alias;
		}
		return alias;
	}
	
	/**
	 * Set alias.
	 * @param alias
	 */
	public void setAlias(String alias) {

		if (alias == null) {
			alias = "";
		}
		this.alias = alias;
	}

	/**
	 * Get slot with given name or return null value if the
	 * slot doesn't exist.
	 * @param alias
	 * @return
	 */
	public Slot getSlot(String alias) {
		
		for (Slot slot : slots) {
			if (slot.getAlias().equals(alias)) {
				return slot;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof Area)) {
			return false;
		}
		Area area = (Area) object;
		
		return area.id == id;
	}

	/**
	 * Get dependent area.
	 * @param dependentAreas
	 * @param areaId
	 * @return
	 */
	private DependentArea getDependentArea(
			LinkedList<DependentArea> dependentAreas, long areaId) {
		
		for (DependentArea dependentArea : dependentAreas) {
			
			if (dependentArea.getId() == areaId) {
				return dependentArea;
			}
		}
		
		return null;
	}


	/**
	 * Set inheritance.
	 * @param superAreaId
	 * @param inherits
	 */
	public void setInheritanceLight(long superAreaId, boolean inherits) {
		
		// Set super area inheritance.
		DependentArea dependentSuperArea = getDependentArea(superareas,
				superAreaId);
		if (dependentSuperArea == null) {
			return;
		}
		
		dependentSuperArea.setInheritance(inherits);
	}

	/**
	 * Get subarea index.
	 * @param areaId
	 * @return
	 */
	public int getSubareaIndex(long areaId) {

		int index = 0;
		for (DependentArea subarea : subareas) {
			if (subarea.getId() == areaId) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Get super area index.
	 * @param areaId
	 * @return
	 */
	public int getSuperareaIndex(long areaId) {

		int index = 0;
		for (DependentArea superarea : superareas) {
			if (superarea.getId() == areaId) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Get inherited area index.
	 * @param areaId
	 * @return
	 */
	public int getInheritsFromIndex(long areaId) {

		int index = 0;
		for (DependentArea superarea : superareas) {
			if (superarea.getId() == areaId && superarea.isInheritance()) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Get sub edge name.
	 * @param subAreaId
	 * @return
	 */
	// TODO: obsolete
	public String getSubRelationName(long subAreaId) {
		
		DependentArea subArea = getDependentArea(subareas, subAreaId);
		if (subArea == null) {
			return "";
		}
		
		String name = subArea.getRelationNameSub();
		if (name == null) {
			name = "";
		}
		return name;
	}

	/**
	 * Get super edge name.
	 * @param superAreaId
	 * @return
	 */
	// TODO: obsolete
	public String getSuperRelationName(long superAreaId) {
		
		DependentArea superArea = getDependentArea(superareas, superAreaId);
		if (superArea == null) {
			return "";
		}
		
		String name = superArea.getRelationNameSuper();
		if (name == null) {
			name = "";
		}
		return name;
	}

	/**
	 * Set sub relation name.
	 * @param subAreaId
	 * @param relationName
	 */
	public void setSubRelationNameLight(long subAreaId, String relationName) {
		
		DependentArea subArea = getDependentArea(subareas, subAreaId);
		subArea.setRelationNameSub(relationName);
	}

	/**
	 * Set super relation name.
	 * @param superAreaId
	 * @param relationName
	 */
	public void setSuperRelationNameLight(long superAreaId, String relationName) {
		
		DependentArea superArea = getDependentArea(superareas, superAreaId);
		superArea.setRelationNameSuper(relationName);
	}

	/**
	 * Get description.
	 * @return
	 */
	@Override
	public String getDescriptionForced() {

		if (!description.isEmpty()) {
			return toString();
		}
		return alias;
	}

	/**
	 * Get description.
	 * @param showIds
	 * @return
	 */
	public String getDescriptionForced(boolean showIds) {

		if (!description.isEmpty()) {
			if (showIds) {
				return "[" + id + "] " + description;
			}
			else {
				return description;
			}
		}
		return alias;
	}
	
	/**
	 * Get description for diagram.
	 * @return
	 */
	public String getDescriptionForDiagram() {

		String text = description.isEmpty() ? alias : description;
		
		if (showIds) {
			return "[" + id + "] " + text;
		}
		else {
			return text;
		}
	}
	
	/**
	 * @param subAreasLoaded the subAreasLoaded to set
	 */
	public void setSubAreasLoaded(boolean subAreasLoaded) {
		this.subAreasLoaded = subAreasLoaded;
	}


	/**
	 * @return the subAreasLoaded
	 */
	public boolean isSubAreasLoaded() {
		return subAreasLoaded;
	}


	/**
	 * @param superAreasLoaded the superAreasLoaded to set
	 */
	public void setSuperAreasLoaded(boolean superAreasLoaded) {
		this.superAreasLoaded = superAreasLoaded;
	}


	/**
	 * @return the superAreasLoaded
	 */
	public boolean isSuperAreasLoaded() {
		return superAreasLoaded;
	}

	/**
	 * Add slot alias.
	 * @param slotAlias
	 */
	public void addSlotAlias(String slotAlias) {
		
		slotAliases.add(slotAlias);
	}

	/**
	 * Add slot name.
	 * @param slotName
	 */
	public void addSlotName(String slotName) {
		
		if (slotName != null && !slotName.isEmpty()) {
			slotNames.add(slotName);
		}
	}

	/**
	 * Get slot aliases count.
	 * @return
	 */
	public int getSlotAliasesCount() {
		
		return slotAliases.size();
	}

	/**
	 * Get slot aliases.
	 * @return
	 */
	public LinkedList<String> getSlotAliases() {
		
		return slotAliases;
	}

	/**
	 * Get slot names.
	 * @return
	 */
	public LinkedList<String> getSlotNames() {
		
		return slotNames;
	}

	/**
	 * Add resource name.
	 * @param localDescription
	 * @param resourceName
	 */
	public void addResource(String localDescription, String resourceName) {

		String resourceDescription = localDescription.isEmpty() ? resourceName
				: localDescription;
		resourceNames.add(resourceDescription);
	}
	
	/**
	 * Get resopurce names.
	 * @return
	 */
	public LinkedList<String> getResourceNames() {
		
		return resourceNames;
	}

	/**
	 * Get resource names count.
	 * @return
	 */
	public int getResourceNamesCount() {

		return resourceNames.size();
	}

	/**
	 * Get sub relation.
	 * @param subAreaId
	 * @return
	 */
	public AreaRelation getSubRelation(long subAreaId) {
		
		DependentArea subArea = getDependentArea(subareas, subAreaId);
		if (subArea == null) {
			return null;
		}
		
		return subArea.getRelation();
	}

	/**
	 * Get super relation.
	 * @param superAreaId
	 * @return
	 */
	public AreaRelation getSuperRelation(long superAreaId) {
		
		DependentArea superArea = getDependentArea(superareas, superAreaId);
		if (superArea == null) {
			return null;
		}
		
		return superArea.getRelation();
	}

	/**
	 * Get unique super area relations.
	 * @return
	 */
	public LinkedList<AreaRelation> getUniqueSuperAreaRelations() {
		
		LinkedList<AreaRelation> superAreaRelations = new LinkedList<AreaRelation>();
		
		for (DependentArea superArea : superareas) {
			
			AreaRelation superAreaRelation = superArea.getRelation();
			
			if (!AreaRelation.containsRelation(superAreaRelations, superAreaRelation)) {
				superAreaRelations.add(superAreaRelation);
			}
		}
		
		return superAreaRelations;
	}

	/**
	 * Get inheriting areas count
	 * @return
	 */
	public int getInheritsFromCount() {
		
		int count = 0;
		
		for (DependentArea superarea : superareas) {
			if (superarea.isInheritance()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Compare areas.
	 */
	@Override
	public int compareTo(Object o) {
		
		if (!(o instanceof Area)) {
			return -1;
		}
		
		Area area = (Area) o;
		return description.compareTo(area.description);
	}


	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}


	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Set help flag.
	 * @param isHelp
	 */
	public void setHelp(boolean isHelp) {
		
		this.isHelp  = isHelp;
	}
	
	/**
	 * Get help flag.
	 */
	public boolean isHelp() {
		return this.isHelp;
	}

	/**
	 * Set localized flag.
	 * @param localized
	 */
	public void setLocalized(boolean localized) {
		
		this.localized = localized;
	}


	/**
	 * @return the localized
	 */
	public boolean isLocalized() {
		return localized;
	}

	/**
	 * Returns true value if to hide sub areas. (Depends on is_sub relation)
	 * @param childId
	 * @return
	 */
	public boolean isHideSubUseSub(long childId) {

		for (DependentArea subarea : subareas) {
			if (subarea.getId() == childId) {
				if (subarea.getRelation().isHideSub()) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Returns true value if to hide sub areas. (Depends on is_sub relation)
	 * @param parentId
	 * @return
	 */
	public boolean isHideSubUseSuper(long parentId) {
		
		for (DependentArea superarea : superareas) {
			if (superarea.getId() == parentId) {
				if (superarea.getRelation().isHideSub()) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Set relation hide sub flag.
	 * @param subAreaId
	 * @param hideSub
	 */
	public void setHideSubUseSub(long childId, boolean hideSub) {
		
		for (DependentArea subarea : subareas) {
			if (subarea.getId() == childId) {
				
				subarea.getRelation().setHideSub(hideSub);
				break;
			}
		}
	}

	/**
	 * Set relation hide sub flag.
	 * @param parentId
	 * @param hideSub
	 */
	public void setHideSubUseSuper(long parentId, boolean hideSub) {
		
		for (DependentArea superarea : superareas) {
			if (superarea.getId() == parentId) {
				
				superarea.getRelation().setHideSub(hideSub);
				break;
			}
		}
	}

	/**
	 * Returns true value if the sub area edge is a recursion.
	 * @param subArea
	 * @return
	 */
	public boolean isRecursionUseSub(long childId) {
		
		for (DependentArea subarea : subareas) {
			if (subarea.getId() == childId) {
				if (subarea.getRelation().isRecursion()) {
					return true;
				}
			}
		}
		
		return false;
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
		
		if (fileName == null) {
			this.fileName = null;
			return;
		}
		
		if (fileName.isEmpty()) {
			this.fileName = null;
			return;
		}
		
		this.fileName = fileName;
	}

	/**
	 * Get next area.
	 * @param superArea
	 * @return
	 */
	public Area getNextArea(Area superArea) {
		
		Area nextArea = null;
		boolean thisAreaFound = false;
		
		// Do loop for subareas.
		for (Area subArea : superArea.getSubareas()) {
			
			if (thisAreaFound) {
				nextArea = subArea;
				break;
			}
			
			if (subArea.equals(this)) {
				thisAreaFound = true;
			}
		}
		
		return nextArea;
	}

	/**
	 * Get previous area.
	 * @param superArea
	 * @return
	 */
	public Area getPreviousArea(Area superArea) {

		Area previousArea = null;
		
		// Do loop for subareas.
		for (Area subArea : superArea.getSubareas()) {
			
			if (subArea.equals(this)) {
				return previousArea;
			}
			
			previousArea = subArea;
		}
		
		return null;
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {

		return folder;
	}
	
	/**
	 * Get folder name (OS dependent).
	 * @return
	 */
	public String getFolderOSDependent() {
	
		return Utility.getFolderOSDependent(folder);
	}

	/**
	 * @param folder the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * Set version ID.
	 * @param versionId
	 */
	public void setVersionId(long versionId) {
		
		this.versionId = versionId;
	}
	
	/**
	 * Get version ID.
	 * @return
	 */
	public long getVersionId() {
		
		return versionId;
	}

	/**
	 * Trace area.
	 * @param decorated
	 * @return
	 */
	public String trace(boolean decorated) {
		
		String trace = null;
		
		// Define table description.
		String [][] descriptor = {
				{"server.textAreaDescriptionTrace", description},
				{"server.textAreaAliasTrace", alias},
				{"server.textAreaIdTrace", String.valueOf(id)}
		};
		
		trace = MiddleUtility.createLocalizedTraceTable(descriptor, decorated);
		
		return trace;
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
	 * Get inherited folder.
	 * @return
	 */
	public String getInheritedFolder(long versionIdPar) {

		// Create queue.
		LinkedList<QueueItem> queue = new LinkedList<QueueItem>();
		LinkedList<Area> visited = new LinkedList<Area>();
		
		// Initialize queues.
		queue.add(new QueueItem(this, false));
		visited.add(this);
		
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
				if (currentArea.getVersionId() == versionIdPar
						&& (!folder.isEmpty() || currentArea.isStartArea())) {
					
					return folder.equals(File.separator) ? "" : folder;
				}
			}
			
			// Do loop for all super areas.
			for (Area superArea : currentArea.getSuperareas()) {
				
				if (!visited.contains(superArea)) {
					
					// Add super area to the queue.
					visited.add(superArea);
					queue.addLast(new QueueItem(superArea, queueItem.omit));
				}
			}
		}
		
		return "";
	}

	/**
	 * Set constructor group ID.
	 * @param constructorGroupId
	 */
	public void setConstructorGroupId(Long constructorGroupId) {
		
		this.constructorGroupId = constructorGroupId;
	}

	/**
	 * Returns true value if the area is a constructor source.
	 * @return
	 */
	public boolean isConstructorSource() {
		
		return constructorGroupId != null;
	}

	/**
	 * Get constructor group ID.
	 * @return
	 */
	public Long getConstructorGroupId() {
		
		return constructorGroupId;
	}

	/**
	 * Returns true value if the constructor is clear.
	 * @return
	 */
	public boolean isConstructorClear() {
		
		return constructorGroupId == null;
	}

	/**
	 * Returns true value if this area has a constructor.
	 * @return
	 */
	public boolean isAreaConstructor() {
		
		return !constructorListRows.isEmpty() || constructorGroupId != null;
	}

	/**
	 * Add new row into the constructor list.
	 * @param row
	 */
	public void addConstructorListRow(String row) {
		
		if (row != null && !row.isEmpty()) {
			constructorListRows.add('+' + row);
		}
	}
	
	/**
	 * Add ellipsis to the constructor list.
	 */
	public void addConstructorListEllipsis() {
		
		constructorListRows.add("...");
	}

	/**
	 * Add "more constructors exist" row.
	 */
	public void addConstructorListMoreGroups() {
		
		constructorListRows.add(Resources.getString("org.multipage.generator.textConstructorListMoreGroups"));
	}

	/**
	 * Add "constructor extension exists" row.
	 */
	public void addConstructorListExtensionExists() {
		
		constructorListRows.add(Resources.getString("org.multipage.generator.textConstructorExtensionExists"));
	}
	
	/**
	 * Get constructor holders' names.
	 * @return
	 */
	public LinkedList<String> getConstructorNameList() {
		
		return constructorListRows;
	}

	/**
	 * Get number of constructor holders.
	 * @return
	 */
	public int getConstructorHoldersCount() {
		
		return constructorListRows.size();
	}

	/**
	 * Set is constructor area flag.
	 * @param isConstructorArea
	 */
	public void setIsConstructorArea(boolean isConstructorArea) {
		
		this.isConstructorArea  = isConstructorArea;
	}
	
	/**
	 * Get is constructor area flag.
	 */
	public boolean isConstructorArea() {
		
		return isConstructorArea;
	}

	/**
	 * Creates true value if sub areas are hidden.
	 * @param parentArea 
	 * @return
	 */
	public boolean isSubareasHidden(Area childArea) {
		
		// Get sub relation.
		AreaRelation relation = getSubRelation(childArea.getId());
		if (relation == null) {
			return false;
		}
		
		return relation.isHideSub();
	}

	/**
	 * Get related area.
	 * @return
	 */
	public Area getRelatedArea() {
		
		return relatedArea;
	}
	
	/**
	 * Set related area.
	 * @param relatedArea
	 */
	public void setRelatedArea(Area relatedArea) {
		
		this.relatedArea = relatedArea;
		this.isRelatedAreaLoaded = true;
	}

	/**
	 * Clear related area.
	 */
	public void clearRelatedArea() {
		
		relatedArea = null;
	}

	/**
	 * Returns true value if a related area is loaded.
	 * @return
	 */
	public boolean isRelatedAreaLoaded() {
		
		return isRelatedAreaLoaded;
	}

	/**
	 * Set constructor holder ID.
	 * @param constructorHolderId
	 */
	public void setConstructorHolderId(Long constructorHolderId) {
		
		this.constructorHolderId = constructorHolderId;
	}
	
	/**
	 * Get constructor holder ID.
	 * @return
	 */
	public Long getConstructorHolderId() {
		
		return constructorHolderId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		
		return ((Long) id).hashCode();
	}

	/**
	 * Set file extension.
	 * @param fileExtension
	 */
	public void setFileExtension(String fileExtension) {
		
		if (fileExtension != null && fileExtension.isEmpty()) {
			this.fileExtension = null;
			return;
		}
		
		this.fileExtension = fileExtension;
	}

	/**
	 * Get file extension.
	 * @return
	 */
	public String getFileExtension() {
		
		if (fileExtension == null) {
			return "";
		}
		
		return fileExtension;
	}

	/**
	 * Get file extension. May be null.
	 * @return
	 */
	public String getFileExtensionNull() {
		
		if (fileExtension == null) {
			return null;
		}
		if (fileExtension.isEmpty()) {
			return null;
		}
		return fileExtension;
	}

	/**
	 * Get constructing area.
	 * @return
	 */
	public Area getConstructingArea() {
		
		return constructingArea;
	}

	/**
	 * Returns true value if a constructing area is set.
	 * @return
	 */
	public boolean isConstructingAreaSet() {
		
		return constructingAreaSet;
	}

	/**
	 * Set constructing area set flag.
	 * @param set
	 */
	public void setConstructingAreaSet(boolean set) {
		
		constructingAreaSet = set;
	}

	/**
	 * Set constructing area reference.
	 * @param constructingArea
	 */
	public void setConstructingArea(Area constructingArea) {
		
		this.constructingArea = constructingArea;
	}

	/**
	 * Get children.
	 */
	@Override
	public LinkedList getChildren() {
		
		return subareas;
	}
	
	/**
	 * Get can import flag.
	 * @return
	 */
	public boolean canImport() {
		
		return canImport;
	}
	
	/**
	 * Returns true value if it is basic area.
	 * @return
	 */
	public boolean isBasic() {
		
		return this.id == 0L;
	}
	
	/**
	 * Get project root flag.
	 * @return
	 */
	public boolean isProjectRoot() {
		
		return projectRoot;
	}

	/**
	 * Set project root flag.
	 * @param canImport
	 */
	public void setCanImport(Boolean canImport) {

		if (canImport == null) {
			canImport = false;
		}
		this.canImport = canImport;
	}

	/**
	 * Set project root flag.
	 * @param projectRoot
	 */
	public void setProjectRoot(Boolean projectRoot) {
		
		if (projectRoot == null) {
			projectRoot = false;
		}
		this.projectRoot = projectRoot;
	}

	/**
	 * Set start resource ID.
	 * @param startResourceId
	 */
	public void setStartResourceId(Long startResourceId) {
		
		this.startResourceId = startResourceId;
	}

	/**
	 * Gets true value if the area has a start resource.
	 * @return
	 */
	public boolean isStartArea() {
		
		return startResourceId != null || !areaSources.isEmpty() || isStartResource;
	}
	
	/**
	 * Check of this area has a start resource.
	 */
	public boolean isSource(long versionId) {
		
		// Old style start resource.
		if (startResourceId != null) {
			return true;
		}
		
		// Check area source.
		if (areaSources.get(versionId) != null) {
			return true;
		}
		
		return false;
	}

	/**
	 * Add area source. The area has to be a start area.
	 * @param versionId
	 */
	public void addSource(long resourceId, long versionId, boolean notLocalized) {
		
		// Create new area source and add it to the list.
		AreaSource areaSource = new AreaSource(id, resourceId, versionId, notLocalized);
		areaSources.put(versionId, areaSource);
	}
	
	/**
	 * Get area sources.
	 * @return
	 */
	public HashMap<Long, AreaSource> getAreaSourcesMap() {
		
		return areaSources;
	}

	/**
	 * Get area sources collection.
	 * @return
	 */
	public Collection<AreaSource> getAreaSourcesCollection() {
		
		return areaSources.values();
	}

	/**
	 * Set "is start resource" flag.
	 * @param isStartResource
	 */
	public void setIsStartResource(boolean isStartResource) {
		
		this.isStartResource = isStartResource;
	}

	/**
	 * Returns true value if the area is protected.
	 * @return
	 */
	public boolean isProtected() {
		
		return readOnly;
	}

	/**
	 * Set area enabled
	 * @param isEnabled
	 */
	public void setEnabled(boolean isEnabled) {
		
		this.isEnabled = isEnabled;
	}

	/**
	 * Return true value if the area is enabled
	 * @return
	 */
	public boolean isEnabled() {
		
		return isEnabled;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasSubareas() {
		
		return !subareas.isEmpty();
	}
	
	/**
	 * Inserts area
	 * @param area
	 */
	public void insert(Area area) {
		
		addSubareaLight(area, true, null, null, false, false);
	}
	
	/**
	 * Get path slots.
	 * @return
	 */
	public LinkedList<Slot> getPathSlots() {
		
		LinkedList<Slot> pathSlots = new LinkedList<Slot>();
		
		slots.forEach((slot) -> {
			if (slot.isPath()) {
				pathSlots.add(slot);
			}
		});
		return pathSlots;
	}
	
	/**
	 * Get slot by ID.
	 * @param id
	 * @return
	 */
	public Slot getSlot(Long id) {
		
		for (Slot currentSlot : slots) {
			long currentId = currentSlot.getId();
			if (currentId == id) {
				return currentSlot;
			}
		}
		return null;
	}
	
	/**
	 * Trim list of areas.
	 * @param areas
	 * @return
	 */
	public static LinkedList<Area> trim(LinkedList<Area> areas) {

		LinkedList<Area> trimmedAreas = areas.stream().filter(area -> area != null).collect(Collectors.toCollection(LinkedList::new));
		return trimmedAreas;
	}
	
	/**
	 * Clear slots.
	 */
	@Override
	public void clearSlots() {
		
		// Delegate call to parent object method.
		super.clearSlots();
		
		// Clear additional slot information.
		slotAliases.clear();
		slotNames.clear();
	}
	
	/**
	 * Clear area slots.
	 * @param areas
	 */
	public static void clearSlots(LinkedList<Area> areas) {
		
		// Clear slots in each area in the list.
		for (Area area : areas) {
			if (area != null) {
				area.clearSlots();
			}
		}
	}
}