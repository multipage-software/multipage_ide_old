/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.util.*;

import org.multipage.gui.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class ConstructorGroup extends ConstructorSubObject implements IdentifiedTreeNode {
	
	/**
	 * List of constructor holders.
	 */
	private LinkedList<ConstructorHolder> constructorHolders = new LinkedList<ConstructorHolder>();
	
	/**
	 * Identifier.
	 */
	private long id;
	
	/**
	 * Old identifier.
	 */
	private long oldId;

	/**
	 * Extension area ID.
	 */
	private Long extensionAreaId;
	
	/**
	 * Alias.
	 */
	private String alias = "";

	/**
	 * Constructor.
	 * @param id
	 */
	public ConstructorGroup(long id) {
		
		this.id = id;
	}

	/**
	 * Constructor.
	 */
	public ConstructorGroup() {
		
	}

	/**
	 * Clone group.
	 */
	public ConstructorGroup cloneShallow() {
		
		ConstructorGroup newConstructorGroup = new ConstructorGroup(id);
		newConstructorGroup.oldId = oldId;
		
		// List shallow copy.
		newConstructorGroup.constructorHolders.addAll(constructorHolders);
		
		newConstructorGroup.extensionAreaId = extensionAreaId;
		
		return newConstructorGroup;
	}

	/**
	 * Set tree identifiers.
	 * @param rootConstructorGroup
	 */
	public static void setTreeIdentifiers(ConstructorGroup rootConstructorGroup) {
		
		long identifier = 0L;
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(rootConstructorGroup);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get queue item.
			Object item = queue.removeFirst();
			
			// If it is a group.
			if (item instanceof ConstructorGroup) {
				
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				constructorGroup.id = identifier++;
				
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			
			// If it is constructor holder.
			else if (item instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				ConstructorSubObject subObject = constructorHolder.getSubObject();
				if (subObject instanceof ConstructorGroup) {
					
					queue.add(subObject);
				}
			}
		}
	}

	/**
	 * Update group references.
	 * @param newConstructorGroups 
	 * @param newConstructorGroupRefs
	 */
	public static void reconnectGroupReferences(LinkedList<ConstructorGroup> newConstructorGroups,
			LinkedList<ConstructorGroupRef> newConstructorGroupRefs) {

		// Do loop for all group references.
		for (ConstructorGroupRef newConstructorGroupRef : newConstructorGroupRefs) {
			
			// If the reference group exists among new groups, reconnect the reference to this new group.
			long referenceGroupId = newConstructorGroupRef.getId();
			
			for (ConstructorGroup newConstructorGroup : newConstructorGroups) {
				
				if (referenceGroupId == newConstructorGroup.getId()) {
					newConstructorGroupRef.setConstructorGroupReference(newConstructorGroup);
					break;
				}
			}
		}
	}
	
	/**
	 * Clone tree.
	 * @param parentConstructorHolder
	 * @param newConstructorGroups
	 * @param newConstructorGroupRefs
	 * @return
	 */
	public ConstructorGroup cloneTree(ConstructorHolder parentConstructorHolder, 
			LinkedList<ConstructorGroup> newConstructorGroups,
			LinkedList<ConstructorGroupRef> newConstructorGroupRefs) {
		
		ConstructorGroup newConstructorGroup = new ConstructorGroup();
		newConstructorGroup.id = id;
		newConstructorGroup.constructorHolders = new LinkedList<ConstructorHolder>();
		newConstructorGroup.setParentConstructorHolder(parentConstructorHolder);
		newConstructorGroup.extensionAreaId = extensionAreaId;
		
		// Remember new group.
		newConstructorGroups.add(newConstructorGroup);
		
		// Clone constructor holders.
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			ConstructorHolder newConstructorHolder = constructorHolder.cloneTreeHelper(newConstructorGroup,
					newConstructorGroups, newConstructorGroupRefs);
			newConstructorGroup.addConstructorHolder(newConstructorHolder);
		}
		return newConstructorGroup;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		//return String.format("%s (%d) #%d", Resources.getString("middle.textConstructorGroup"), id, hashCode());
		return Resources.getString("middle.textConstructorGroup");
	}

	
	/**
	 * Get constructor hoder.
	 * @param index
	 * @return
	 */
	public ConstructorHolder getConstructorHolder(int index) {
		
		try {
			return constructorHolders.get(index);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get constructor holder count.
	 * @return
	 */
	public int getConstructorHolderCount() {
		
		return constructorHolders.size();
	}

	/**
	 * Get constructor holder index.
	 * @param object
	 * @return
	 */
	public int getConstructorHolderIndex(Object object) {
		
		return constructorHolders.indexOf(object);
	}

	/**
	 * Returns true value if the constructor holder name already exists.
	 * @param name
	 * @return
	 */
	public boolean existsConstructorHolderName(String name) {
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			if (constructorHolder.getName().equals(name)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true value if the constructor holder alias already exists.
	 * @param alias
	 * @return
	 */
	public boolean existsConstructorHolderAlias(String alias) {
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			if (constructorHolder.getAlias().equals(alias)) {
				return true;
			}
		}
		
		return false;
	}
	/**
	 * Get constructor holders.
	 * @return
	 */
	public LinkedList<ConstructorHolder> getConstructorHolders() {
		
		return constructorHolders;
	}

	/**
	 * Remove constructor holder.
	 * @param name
	 */
	public void removeConstructorHolder(String name) {
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			if (constructorHolder.getName().equals(name)) {
				
				constructorHolders.remove(constructorHolder);
				changed = true;
				break;
			}
		}
	}

	/**
	 * Get constructor group.
	 */
	@Override
	public ConstructorGroup getConstructorGroup() {
		
		return this;
	}

	/**
	 * Set ID.
	 * @param id
	 */
	public void setId(long id) {
		
		this.id = id;
	}

	/**
	 * Get ID.
	 * @return
	 */
	public long getId() {
		
		return id;
	}

	/**
	 * Returns true value if the constructor group is empty. (It doesn't have any constructor holders.)
	 * @return
	 */
	public boolean isEmpty() {
		
		return constructorHolders.isEmpty();
	}

	/**
	 * Clear content.
	 */
	public void clear() {
		
		id = 0L;
		constructorHolders.clear();
		extensionAreaId = null;
		changed = true;
	}

	/**
	 * Returns true value if the constructor tree is changed.
	 * @return
	 */
	public boolean isTreeChanged() {
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				if (constructorGroup.isChanged()) {
					return true;
				}
				
				// Add sub constructor holders.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				if (constructorHolder.isChanged()) {
					return true;
				}
				
				// Add sub group into the queue..
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}
			}
		}
		
		return false;
	}

	/**
	 * Clear changed flags in the tree.
	 */
	public void clearTreeChanged() {
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				// Reset change flag.
				constructorHolder.clearChanged();
				
				// Add sub group into the queue.
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}

			}
			else if (item instanceof ConstructorGroup) {
				
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				constructorGroup.clearChanged();
				
				// Add sub constructor holders into the queue.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
		}
	}
	
	/**
	 * Clear changed flag.
	 */
	private void clearChanged() {
		
		changed = false;
	}

	/**
	 * Get old ID.
	 * @return
	 */
	public long getOldId() {
		
		return oldId;
	}

	/**
	 * Dump constructor tree
	 * @return
	 */
	public String dump() {
		
		Obj<String> text = new Obj<String>("");
		
		dump(this, text, 0);
		
		return text.ref;
	}

	/**
	 * Get indentation.
	 * @param indentation
	 * @return
	 */
	private static String getIndentationText(int indentation) {
		
		String indentText = "";
		
		for (int index = 0; index < indentation; index++) {
			indentText += "  |-";
		}
		
		return indentText;
	}
	/**
	 * Dump constructor tree (recursive).
	 * @param constructorGroup
	 * @param indentation
	 * @param text
	 */
	private static void dump(ConstructorGroup constructorGroup, Obj<String> text, int indentation) {
		
		String indentText = getIndentationText(indentation++);
		text.ref += indentText + String.format("Group (@%s) id=%d, old=%s\n", 
				Integer.toHexString(System.identityHashCode(constructorGroup)),
				constructorGroup.getId(), constructorGroup.getOldId());
		
		indentText = getIndentationText(indentation++);
		
		for (ConstructorHolder constructorHolder : constructorGroup.getConstructorHolders()) {
			text.ref += indentText + String.format("Constr. '%s' id=%d\n", constructorHolder.getName(), constructorHolder.getId());
			
			// Get sub object.
			ConstructorSubObject subObject = constructorHolder.getSubObject();
			if (subObject instanceof ConstructorGroupRef) {
				
				ConstructorGroup subGroup = subObject.getConstructorGroup();
				
				String groupReferenceIndent = getIndentationText(indentation + 1);
				text.ref += groupReferenceIndent + String.format("Group Ref (@%s) id=%d, old=%d\n",
						Integer.toHexString(System.identityHashCode(subGroup)),
						subGroup.getId(), subGroup.getOldId());
			}
			else if (subObject instanceof ConstructorGroup) {
				
				dump((ConstructorGroup) subObject, text, indentation + 1);
			}
		}
	}

	/**
	 * Save old constructor group ID.
	 */
	public void saveOldId() {
		
		oldId = id;
	}

	/**
	 * Add constructor holder.
	 * @param constructorHolder
	 */
	public void addConstructorHolder(ConstructorHolder constructorHolder) {
		
		// Add new constructor holder if it doesn't exist.
		long newId = constructorHolder.getId();
		for (ConstructorHolder constructor : constructorHolders) {
			if (constructor.getId() == newId) {
				return;
			}
		}
		
		constructorHolder.setParentConstructorGroup(this);
		constructorHolders.add(constructorHolder);
		
		changed = true;
	}

	/**
	 * Clear constructor holders.
	 */
	public void clearConstructorHolders() {
		
		constructorHolders.clear();
		changed = true;
	}

	/**
	 * Add constructor holders.
	 * @param constructorHolders
	 */
	public void addConstructorHolders(
			LinkedList<ConstructorHolder> constructorHolders) {
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			addConstructorHolder(constructorHolder);
		}
	}

	/**
	 * Returns true if the tree contains searched item.
	 * @param rootTreeItem
	 * @param searchedItem
	 * @return
	 */
	public static boolean treeContainsItem(Object rootTreeItem, Object searchedItem) {
		
		// Create queue and add root item to it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(rootTreeItem);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Pop first queue item.
			Object item = queue.removeFirst();
			
			// If the item matches, return true value.
			if (item.equals(searchedItem)) {
				return true;
			}
			
			// On group add sub constructors to the queue.
			if (item instanceof ConstructorGroup) {
				
				queue.addAll(((ConstructorGroup) item).getConstructorHolders());
			}
			// On constructor holder add sub group into the queue.
			else if (item instanceof ConstructorHolder) {
				
				ConstructorSubObject subObject = ((ConstructorHolder) item).getSubObject();
				
				if (subObject instanceof ConstructorGroup) {
					queue.add(subObject);
				}
			}
		}
		
		return false;
	}

	/**
	 * Remove constructor holder.
	 * @param constructorHolder
	 */
	public void removeConstructorHolder(ConstructorHolder constructorHolder) {
		
		constructorHolders.remove(constructorHolder);
	}

	/**
	 * Remove dependencies.
	 */
	public void removeDependencies() {
		
		constructorHolders.clear();
		
		ConstructorHolder parentConstructorHolder = getParentConstructorHolder();
		if (parentConstructorHolder != null) {
			
			parentConstructorHolder.clearSubObject();
			
			setParentConstructorHolder(null);
		}
	}

	/**
	 * Remove constructor group references.
	 * @param rootConstructorGroup
	 * @param referencedConstructorGroup
	 */
	public static void removeGroupReferences(ConstructorGroup rootConstructorGroup,
			ConstructorGroup referencedConstructorGroup) {
		
		// Create queue and add the root group into it.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(rootConstructorGroup);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Pop first queue item.
			Object item = queue.removeFirst();
			
			// If it is a group reference and it references the input group, remove it.
			if (item instanceof ConstructorGroupRef) {
				ConstructorGroupRef constructorGroupRef = (ConstructorGroupRef) item;
				
				if (constructorGroupRef.ref.equals(referencedConstructorGroup)) {
					constructorGroupRef.ref = null;
					
					// Remove parent link.
					ConstructorHolder parentConstructorHolder = constructorGroupRef.getParentConstructorHolder();
					if (parentConstructorHolder != null) {
						
						parentConstructorHolder.clearSubObject();
					}
				}
			}
			
			// If it is a group, add all sub constructors into the queue.
			else if (item instanceof ConstructorGroup) {
				
				queue.addAll(((ConstructorGroup) item).getConstructorHolders());
			}
			
			// If it is a constructor holder, add sub object to the queue.
			else if (item instanceof ConstructorHolder) {
				
				queue.add(((ConstructorHolder) item).getSubObject());
			}
		}
	}

	/**
	 * Get children.
	 */
	@Override
	public LinkedList getChildren() {
		
		return constructorHolders;
	}

	/**
	 * Get extension area ID.
	 * @return
	 */
	public Long getExtensionAreaId() {
		
		return extensionAreaId;
	}

	/**
	 * Set extension area ID.
	 * @param extensionAreaId
	 */
	public void setExtensionAreaId(Long extensionAreaId) {
		
		this.extensionAreaId = extensionAreaId;
	}

	/**
	 * Get constructor holder from its ID.
	 * @param constructorHolderId
	 * @return
	 */
	public ConstructorHolder getConstructorHolder(
			Long constructorHolderId) {
		
		if (constructorHolderId != null) {
		
			for (ConstructorHolder constructorHolder : constructorHolders) {
				if (constructorHolder.getId() == constructorHolderId) {
					
					return constructorHolder;
				}
			}
		}
		
		return null;
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
	 * Get alias.
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
	 * Returns true value if constructor alias already exists.
	 * @param currentConstructorId 
	 * @param alias 
	 * @return
	 */
	public boolean existsAlias(long currentConstructorId, String alias) {
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add sub constructor holders.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				if (constructorHolder.getId() != currentConstructorId
						&& constructorHolder.getAlias().equals(alias)) {
					return true;
				}
				
				// Add sub group into the queue..
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}
			}
		}
		
		return false;
	}

	/**
	 * Create constructor holders map.
	 * @return
	 */
	public HashMap<Long, ConstructorHolder> createConstructorHoldersMap() {
		
		HashMap<Long, ConstructorHolder> map = new HashMap<Long, ConstructorHolder>();
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add sub constructor holders.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				map.put(constructorHolder.getId(), constructorHolder);
				
				// Add sub group into the queue..
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Make constructor links using linkId.
	 */
	public void makeConstructorLinks() {
		
		// Get constructor holders map.
		HashMap<Long, ConstructorHolder> constructorsHoldersMap = createConstructorHoldersMap();
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add sub constructor holders.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				Long linkId = constructorHolder.getLinkId();
				
				// Make link to constructor object.
				if (linkId != null) {
					constructorHolder.setLinkedConstructorHolder(constructorsHoldersMap.get(linkId));
				}
				
				// Add sub group into the queue..
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}
			}
		}
	}

	/**
	 * Creates constructors table.
	 * @param useOldId
	 * @param constructorHolders 
	 * @return
	 */
	public Hashtable<Long, ConstructorHolder> createConstructorsTableAndList(
			boolean useOldId, LinkedList<ConstructorHolder> constructorHolders) {
		
		Hashtable<Long, ConstructorHolder> table = new Hashtable<Long, ConstructorHolder>();
		
		// Create queue.
		LinkedList<Object> queue = new LinkedList<Object>();
		queue.add(this);
		
		// Do loop until the queue is empty.
		while (!queue.isEmpty()) {
			
			// Get first item.
			Object item = queue.removeFirst();
			
			if (item instanceof ConstructorGroup) {
				ConstructorGroup constructorGroup = (ConstructorGroup) item;
				
				// Add sub constructor holders.
				queue.addAll(constructorGroup.getConstructorHolders());
			}
			else if (item instanceof ConstructorHolder) {
				
				ConstructorHolder constructorHolder = (ConstructorHolder) item;
				
				if (constructorHolders != null) {
					constructorHolders.add(constructorHolder);
				}
				
				Long id = useOldId ? constructorHolder.getOldId() : constructorHolder.getId();
				
				// Make link to constructor object.
				if (id != null) {
					table.put(id, constructorHolder);
				}
				
				// Add sub group into the queue..
				ConstructorSubObject constructorSubObject = constructorHolder.getSubObject();
				if (constructorSubObject instanceof ConstructorGroup) {
					
					queue.add(constructorSubObject);
				}
			}
		}
		
		return table;
	}
	
	/**
	 * Mark constructors as linked.
	 */
	public void markConstructorsAsLinked() {
		
		for (ConstructorHolder constructorHolder : constructorHolders) {
			
			constructorHolder.wasLinked();
		}
	}
}
