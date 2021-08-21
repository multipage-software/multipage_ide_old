/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.util.LinkedList;

/**
 * @author
 *
 */
public class NamespacesModel {
	
	/**
	 * Namespaces.
	 */
	private LinkedList<Namespace> namespaces = new LinkedList<Namespace>();
	
	/**
	 * Add new namespace.
	 */
	public void addNew(Namespace namespace) {
		
		namespaces.add(namespace);
	}

	/**
	 * Gets namespace.
	 * @param i
	 * @return
	 */
	private Namespace getNamespace(long id) {

		for (Namespace namespace : namespaces) {
			if (namespace.getId() == id) {
				return namespace;
			}
		}
		
		return null;
	}

	/**
	 * Get root namespace.
	 * @return
	 */
	public Namespace getRootNamespace() {

		return getNamespace(0);
	}

	/**
	 * Gets children of namespace.
	 * @param parentNamespace
	 * @param typesToLoad 
	 * @param index
	 * @return
	 */
	public LinkedList<Namespace> getNamespaceChildren(Namespace parentNamespace) {

		LinkedList<Namespace> result = new LinkedList<Namespace>();
		long parentId = parentNamespace.getId();
		
		// Namespaces.
		for (Namespace namespace : namespaces) {
			if (namespace.getParentNamespaceId() == parentId) {
				// If it is root node, continue loop.
				if (namespace.getId() == 0L) {
					continue;
				}
				result.add(namespace);
			}
		}

		return result;
	}

	/**
	 * Remove namespace.
	 */
	public MiddleResult removeNamespace(Namespace namespace) {
	
		if (namespaces.remove(namespace)) {
			return MiddleResult.OK;
		}
		else {
			return MiddleResult.ERROR_REMOVING_NAMESPACE;
		}
	}

	/**
	 * Removes all namespaces.
	 */
	public void removeAllNamespaces() {

		namespaces.clear();
	}

	/**
	 * Get namespace path.
	 * @param id
	 * @return
	 */
	public LinkedList<Namespace> getNamespacePath(long id) {

		LinkedList<Namespace> namespacePath = new LinkedList<Namespace>();
		
		while (true) {
			
			// Get current namespace.
			Namespace currentNamespace = getNamespace(id);
			if (currentNamespace == null) {
				return null;
			}
			// Add current namespace to the end of the list.
			namespacePath.addFirst(currentNamespace);
			// If the current namespace is the root namespace,
			// exit the loop.
			if (id == 0) {
				break;
			}
			// Get parent namespace.
			id = currentNamespace.getParentNamespaceId();
		}
		
		return namespacePath;
	}
}
