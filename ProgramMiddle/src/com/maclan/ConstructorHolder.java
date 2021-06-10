/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.util.LinkedList;

import org.multipage.gui.IdentifiedTreeNode;

/**
 * @author
 *
 */
public class ConstructorHolder implements IdentifiedTreeNode {

	/**
	 * Identifier.
	 */
	private long id;

	/**
	 * Old ID.
	 */
	private long oldId;

	/**
	 * Constructor name.
	 */
	private String name;

	/**
	 * Constructor alias.
	 */
	private String alias;
	
	/**
	 * Area reference.
	 */
	private long areaId;
	
	/**
	 * Sub object.
	 */
	private ConstructorSubObject subObject;

	/**
	 * Parent constructor group reference.
	 */
	private ConstructorGroup parentConstructorGroup;

	/**
	 * Sub relation name.
	 */
	private String subRelationName = "";

	/**
	 * Super relation name.
	 */
	private String superRelationName = "";
	
	/**
	 * Sub group alias.
	 */
	private String subGroupAliases = "";

	/**
	 * Area inheritance.
	 */
	private boolean inheritance = false;
	
	/**
	 * Changed flag.
	 */
	private boolean changed = false;

	/**
	 * Ask for related area.
	 */
	private boolean askForRelatedArea = false;

	/**
	 * Invisibility flag.
	 */
	private boolean invisible = false;

	/**
	 * Set home flag.
	 */
	private boolean setHome;

	/**
	 * Link to other constructor.
	 */
	private Long linkId;

	/**
	 * Old link ID. Used when importing data.
	 */
	private Long oldLinkId;

	/**
	 * Linked constructor holder.
	 */
	private ConstructorHolder linkedConstructorHolder;

	/**
	 * Is true if the constructor holder was linked.
	 */
	private boolean wasLinked = false;

	/**
	 * Constructor.
	 */
	public ConstructorHolder() {
		
	}

	/**
	 * Constructor.
	 * @param name
	 */
	public ConstructorHolder(String name) {
		
		this.name = name;
	}

	/**
	 * Clone constructor holder.
	 */
	public ConstructorHolder clone() {
		
		ConstructorHolder constructorHolder = new ConstructorHolder();
		
		constructorHolder.id = id;
		constructorHolder.oldId = oldId;
		constructorHolder.areaId = areaId;
		constructorHolder.name = name;
		constructorHolder.alias = alias;
		constructorHolder.parentConstructorGroup = parentConstructorGroup;
		constructorHolder.subObject = subObject;
		constructorHolder.inheritance = inheritance;
		constructorHolder.subRelationName = subRelationName;
		constructorHolder.superRelationName = superRelationName;
		constructorHolder.askForRelatedArea = askForRelatedArea;
		constructorHolder.subGroupAliases = subGroupAliases;
		constructorHolder.invisible = invisible;
		constructorHolder.setHome = setHome;
		constructorHolder.linkId = linkId;
		constructorHolder.oldLinkId = oldLinkId;
		constructorHolder.linkedConstructorHolder = linkedConstructorHolder;
		
		return constructorHolder;
	}

	/**
	 * Clone tree.
	 * @return
	 */
	public ConstructorHolder cloneTree() {
		
		// Create new groups and references empty lists.
		LinkedList<ConstructorGroup> newConstructorGroups = new LinkedList<ConstructorGroup>();
		LinkedList<ConstructorGroupRef> newConstructorGroupRefs = new LinkedList<ConstructorGroupRef>();

		// Clone tree.
		ConstructorHolder newConstructorHolder = cloneTreeHelper(null, newConstructorGroups, newConstructorGroupRefs);
		
		// Reconnect group references.
		ConstructorGroup.reconnectGroupReferences(newConstructorGroups, newConstructorGroupRefs);
		
		return newConstructorHolder;
	}

	/**
	 * Clone tree.
	 * @param newConstructorGroups 
	 * @param newConstructorGroupRefs 
	 * @return
	 */
	public ConstructorHolder cloneTreeHelper(ConstructorGroup newParentConstructorGroup,
			LinkedList<ConstructorGroup> newConstructorGroups,
			LinkedList<ConstructorGroupRef> newConstructorGroupRefs) {
		
		ConstructorHolder newConstructorHolder = new ConstructorHolder();
		
		newConstructorHolder.areaId = areaId;
		newConstructorHolder.changed = changed;
		newConstructorHolder.id = id;
		newConstructorHolder.inheritance = inheritance;
		newConstructorHolder.name = name;
		newConstructorHolder.alias = alias;
		newConstructorHolder.parentConstructorGroup = newParentConstructorGroup;
		newConstructorHolder.subRelationName = subRelationName;
		newConstructorHolder.superRelationName = superRelationName;
		newConstructorHolder.askForRelatedArea = askForRelatedArea;
		newConstructorHolder.subGroupAliases = subGroupAliases;
		newConstructorHolder.invisible = invisible;
		newConstructorHolder.setHome = setHome;
		newConstructorHolder.linkId = linkId;
		newConstructorHolder.linkedConstructorHolder = linkedConstructorHolder;
		
		// Clone sub object.
		if (subObject instanceof ConstructorGroup) {
			
			ConstructorGroup constructorSubGroup = (ConstructorGroup) subObject;
			newConstructorHolder.setSubConstructorGroup(constructorSubGroup.cloneTree(newConstructorHolder,
					newConstructorGroups, newConstructorGroupRefs));
		}
		else if (subObject instanceof ConstructorGroupRef) {
			
			// Get old and create new group reference.
			ConstructorGroupRef oldGroupReference = (ConstructorGroupRef) subObject;
			ConstructorGroupRef newGroupReference = new ConstructorGroupRef();
			
			// Remember new group reference.
			newConstructorGroupRefs.add(newGroupReference);
			
			// Set new reference.
			newGroupReference.ref = oldGroupReference.ref;

			// Set constructor reference.
			newConstructorHolder.subObject = newGroupReference;
			newGroupReference.setParentConstructorHolder(newConstructorHolder);
		}
		
		return newConstructorHolder;
	}

	/**
	 * Get sub object.
	 * @return
	 */
	public ConstructorSubObject getSubObject() {
		
		return subObject;
	}

	/**
	 * Get name.
	 * @return the name
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Get trimmed name.
	 * @return the name
	 */
	public String getNameText() {
		
		return name == null ? "" : name;
	}
	
	/**
	 * Set changed.
	 */
	private void setChanged() {
		
		changed = true;
	}

	/**
	 * Set sub constructor group.
	 * @param constructorGroup
	 */
	public void setSubConstructorGroup(ConstructorSubObject constructorGroup) {
		
		this.subObject = constructorGroup;
		constructorGroup.setParentConstructorHolder(this);
		
		setChanged();
	}

	/**
	 * @return the parentConstructorGroup
	 */
	public ConstructorGroup getParentConstructorGroup() {
		return parentConstructorGroup;
	}

	/**
	 * @param parentConstructorGroup the parentConstructorGroup to set
	 */
	public void setParentConstructorGroup(ConstructorGroup parentConstructorGroup) {
		this.parentConstructorGroup = parentConstructorGroup;
		
		setChanged();
	}

	/**
	 * Reset sub object.
	 */
	public void clearSubObject() {
		
		subObject = null;
		setChanged();
	}

	/**
	 * Set name.
	 * @param name
	 */
	public void setName(String name) {
		
		if (name == null) {
			name = "";
		}
		this.name = name;
		
		setChanged();
	}

	/**
	 * Set ID.
	 * @param id
	 */
	public void setId(long id) {
		
		this.id = id;
		
		setChanged();
	}

	/**
	 * Set area ID.
	 * @param areaId
	 */
	public void setAreaId(Long areaId) {
		
		if (areaId == null) {
			areaId = 0L;
		}
		this.areaId = areaId;
		
		setChanged();
	}
	
	/**
	 * Get area ID.
	 */
	public long getAreaId() {
		
		return areaId;
	}

	/**
	 * Set inheritance.
	 * @param inheritance
	 */
	public void setInheritance(Boolean inheritance) {
		
		if (inheritance == null) {
			inheritance = false;
		}
		
		this.inheritance = inheritance;
		
		setChanged();
	}

	/**
	 * Set sub relation name.
	 * @param subRelationName
	 */
	public void setSubRelationName(String subRelationName) {

		if (subRelationName != null && subRelationName.isEmpty()) {
			subRelationName = null;
		}
		this.subRelationName = subRelationName;
		
		setChanged();
	}

	/**
	 * Get sub relation name.
	 * @return
	 */
	public String getSubRelationName() {
		
		if (subRelationName == null) {
			return "";
		}
		return subRelationName;
	}

	/**
	 * Get sub relation name.
	 * @return
	 */
	public String getSubRelationNameNull() {
		
		if (subRelationName == null) {
			return null;
		}
		if (subRelationName.isEmpty()) {
			return null;
		}
		return subRelationName;
	}

	/**
	 * Set super relation name.
	 * @param superRelationName
	 */
	public void setSuperRelationName(String superRelationName) {
		
		if (superRelationName != null && superRelationName.isEmpty()) {
			superRelationName = null;
		}
		this.superRelationName = superRelationName;
		
		setChanged();
	}
	
	/**
	 * Get super relation name.
	 * @return
	 */
	public String getSuperRelationName() {
		
		if (superRelationName == null) {
			return "";
		}
		return superRelationName;
	}

	/**
	 * Get super relation name.
	 * @return
	 */
	public String getSuperRelationNameNull() {
		
		if (superRelationName == null) {
			return null;
		}
		if (superRelationName.isEmpty()) {
			return null;
		}
		return superRelationName;
	}

	/**
	 * Get ID.
	 * @return
	 */
	public long getId() {
		
		return id;
	}

	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Clear change flag.
	 */
	public void clearChanged() {
		
		changed = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		//return String.format("%s (%s) #%d", name, id, hashCode());
		return name;
	}

	/**
	 * Get inheritance flag.
	 * @return
	 */
	public boolean isInheritance() {
		
		return inheritance;
	}

	/**
	 * Remove dependencies.
	 */
	public void removeDependencies() {
		
		ConstructorGroup parentConstructorGroup = getParentConstructorGroup();
		subObject = null;
		
		if (parentConstructorGroup != null) {
			parentConstructorGroup.removeConstructorHolder(this);
		}
	}

	/**
	 * Get children.
	 */
	@Override
	public LinkedList getChildren() {
		
		LinkedList subObjects = new LinkedList();
		subObjects.add(subObject);
		
		return subObjects;
	}

	/**
	 * Get ask for related area flag.
	 * @return
	 */
	public boolean isAskForRelatedArea() {
		
		return askForRelatedArea;
	}

	/**
	 * Set ask for related area flag.
	 * @param askForRelatedArea
	 */
	public void setAskForRelatedArea(Boolean askForRelatedArea) {
		
		if (askForRelatedArea == null) {
			askForRelatedArea = false;
		}
		
		this.askForRelatedArea = askForRelatedArea;
		
		setChanged();
	}

	/**
	 * Get old ID.
	 * @return
	 */
	public Long getOldId() {
		
		return oldId;
	}
	
	/**
	 * Save old ID.
	 */
	public void saveOldId() {
		
		oldId = id;
	}

	/**
	 * Get path last name.
	 * @return
	 */
	public String getPathLastName() {
		
		String [] pathItems = name.split("/");
		return pathItems[pathItems.length - 1];
	}

	/**
	 * Set sub group aliases.
	 * @param aliases
	 */
	public void setSubGroupAliases(String aliases) {
		
		if (aliases == null) {
			aliases = "";
		}
		subGroupAliases = aliases;
		
		setChanged();
	}
	
	/**
	 * Get sub group aliases.
	 */
	public String getSubGroupAliases() {
		
		if (subGroupAliases == null) {
			return "";
		}
		return subGroupAliases;
	}

	/**
	 * Get sub group aliases.
	 * @return
	 */
	public String getSubGroupAliasesNull() {
		
		if (subGroupAliases == null) {
			return null;
		}
		if (subGroupAliases.isEmpty()) {
			return null;
		}
		return subGroupAliases;
	}

	/**
	 * Set invisibility flag.
	 * @param invisible
	 */
	public void setInvisible(Boolean invisible) {
		
		if (invisible == null) {
			invisible = false;
		}
		
		this.invisible = invisible;
		
		setChanged();
	}

	/**
	 * Get invisibility flag.
	 * @return
	 */
	public boolean isInvisible() {
		
		return invisible;
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
		
		setChanged();
	}

	/**
	 * Get alias.
	 * @return
	 */
	public String getAlias() {
		
		if (alias == null) {
			return "";
		}
		return alias;
	}

	/**
	 * Get alias. If empty, return null.
	 * @return
	 */
	public String getAliasNull() {
		
		if (alias == null) {
			return null;
		}
		if (alias.isEmpty()) {
			return null;
		}

		return alias;
	}

	/**
	 * Set home flag.
	 * @param setHome
	 */
	public void setHome(Boolean setHome) {
		
		if (setHome == null) {
			setHome = false;
		}
		this.setHome = setHome;
		
		setChanged();
	}

	/**
	 * Returns true value if a set home flag is set.
	 * @return
	 */
	public boolean isSetHome() {
		
		return setHome;
	}

	/**
	 * Create constructor holder link.
	 * @param parentGroup
	 * @return
	 */
	public ConstructorHolder createLink(ConstructorGroup parentGroup) {
		
		ConstructorHolder constructorHolderLink = clone();
		
		constructorHolderLink.name = "";
		constructorHolderLink.alias = "";
		constructorHolderLink.areaId = 0L;
		constructorHolderLink.parentConstructorGroup = parentGroup;
		constructorHolderLink.subObject = null;
		constructorHolderLink.subRelationName = null;
		constructorHolderLink.superRelationName = null;
		constructorHolderLink.linkId = id;
		
		return constructorHolderLink;
	}

	/**
	 * Get link ID.
	 * @return
	 */
	public Long getLinkId() {
		
		return linkId;
	}

	/**
	 * Set link ID.
	 * @param linkId
	 */
	public void setLinkId(Long linkId) {
		
		this.linkId = linkId;
	}

	/**
	 * Returns true value if it is a link to other constructor.
	 * @return
	 */
	public boolean isLinkId() {
		
		return linkId != null;
	}

	/**
	 * Set link constructor holder.
	 * @param constructorHolder
	 */
	public void setLinkedConstructorHolder(ConstructorHolder constructorHolder) {
		
		this.linkedConstructorHolder = constructorHolder;
	}

	/**
	 * Get linked constructor holder.
	 * @return
	 */
	public ConstructorHolder getLinkedConstructorHolder() {
		
		return linkedConstructorHolder;
	}

	/**
	 * Returns true value if a linked constructor holder exists.
	 * @return
	 */
	public boolean isLinkObject() {
		
		return linkedConstructorHolder != null;
	}

	/**
	 * Set old link ID.
	 * @param oldLinkId
	 */
	public void setOldLinkId(Long oldLinkId) {
		
		this.oldLinkId = oldLinkId;
	}

	/**
	 * Get old link ID.
	 * @return
	 */
	public Long getOldLinkId() {
		
		return oldLinkId;
	}

	/**
	 * Utilize linked constructor holder.
	 * @param linkedConstructorHolder
	 * @return returns constructor alias
	 */
	public String utilizeLinkedConstrutor(ConstructorHolder linkedConstructorHolder) {
		
		alias = linkedConstructorHolder.alias;
		areaId = linkedConstructorHolder.areaId;
		askForRelatedArea = linkedConstructorHolder.askForRelatedArea;
		inheritance = linkedConstructorHolder.inheritance;
		invisible = linkedConstructorHolder.invisible;
		name = linkedConstructorHolder.name;
		setHome = linkedConstructorHolder.setHome;
		subGroupAliases = linkedConstructorHolder.subGroupAliases;
		wasLinked = true;
		
		linkId = null;
		linkedConstructorHolder = null;
		
		return alias;
	}

	/**
	 * Returns true if the constructor holder has to be selected.
	 * @return
	 */
	public boolean selectIt() {

		return name.matches(".*\\*\\s*$") && !wasLinked;
	}

	/**
	 * Set linked flag.
	 */
	public void wasLinked() {
		
		wasLinked = true;
	}

	/**
	 * Remove asterisk from the name.
	 */
	public void removeAsterisk() {
		
		name = name.replaceAll("\\*\\s*$", "");
	}
}
